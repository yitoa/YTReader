package com.yt.reader.optionmenu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

import android.util.Log;

import com.yt.reader.format.pdf.SearchItem;
import com.yt.reader.model.Book;
import com.yt.reader.utils.FileUtils;
import com.yt.reader.utils.ParserUtils;

public class TextSearch {
	static {
		Log.v("loadLibrary", "load search lib");
		System.loadLibrary("grep");
		System.loadLibrary("iconv");
	}

	public native void grep(String text, String path, String logPath);

	public native void iconv(String fromCharset, String toCharset,
			String fromPath, String toPath);

	public static List<Long> searchResultLocation;// 存放搜索结果位置或页码

	public static List<String> searchResultDescription;// 存放搜索结果描述信息,与searchResultLocation元素位置对应
	
	public static List<SearchItem> searchResultRect;//存放搜索结果的高亮显示，用于类似于PDF等需要从底层画图的格式

	private static MappedByteBuffer buffer;

	/**
	 * 在book中搜索与searchText匹配的行，并将匹配结果存入searchResult。
	 * 
	 * @param book
	 * @param map
	 * @param searchText
	 */
	public static void search(Book book, String searchText) {
		try {
			read(book);
		} catch (FileNotFoundException e1) {
			return;
		} catch (IOException e1) {
			return;
		}
		String logPath = FileUtils.getSDPath() + "/searchResultLog.log";
		File file = new File(logPath);
		if (file.exists()) {
			file.delete();
		}
		new TextSearch().grep(searchText,
				book.getPath() + "/" + book.getName(), logPath);
		while (!file.exists()) {
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			int total = 0;
			while ((line = reader.readLine()) != null) {
				if (-1 < line.indexOf(":")) {
					try {
						long location = Long.parseLong(line.substring(0,
								line.indexOf(":")));
						int start = (int) location;
						int end = start
								+ Integer.parseInt(line.substring(line
										.lastIndexOf(":") + 1));
						String description = "";
						if (++total < 500) {
							byte[] buf = new byte[end - start];
							for (int i = start; i < end; i++) {
								buf[i - start] = buffer.get(i);
							}
							String str = new String(buf);
							int index = str.indexOf(searchText);
							if (index < 0) {
								description = str;// TODO 搜索bug
							} else {
								String str1 = str.substring(0, index);
								String[] arr = str1
										.split("\\.|,|\\s|:|'|\"|\\?|!|，|。|；|：|“|”|‘|’|？|！");
								if (arr.length > 0)
									description = str.substring(str1
											.lastIndexOf(arr[arr.length - 1]));
								else
									// searchText是段的第一个单词
									description = str;
							}
							if (null != description
									&& description.length() > 50) {
								description = description.substring(0, 50);
							}
						}
						Log.v("searchResult", location + ":" + description);
						TextSearch.searchResultLocation.add(location);
						TextSearch.searchResultDescription.add(ParserUtils
								.trim(description));
					} catch (NumberFormatException e) {
						continue;
					}
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}

		if (file.exists()) {
			file.delete();
		}
	}

	private static void read(Book book) throws FileNotFoundException,
			IOException {
		String path = book.getPath() + "/" + book.getName();
		File f = new File(path);
		buffer = new RandomAccessFile(f, "r").getChannel().map(
				FileChannel.MapMode.READ_ONLY, 0, f.length());
	}

}
