package com.yt.reader.format.txt;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.yt.reader.R;
import com.yt.reader.base.BookPageFactory;
import com.yt.reader.database.DBSchema;
import com.yt.reader.model.Book;
import com.yt.reader.utils.Constant;
import com.yt.reader.utils.FactoryUtils;
import com.yt.reader.utils.FileUtils;
import com.yt.reader.utils.ParserUtils;

public class TXTPageFactory extends BookPageFactory {
	private int width, height;// 屏幕尺寸
	private int vWidth, vHeight;// 绘制区尺寸
	private TXTActivity context;
	private static MappedByteBuffer buffer;// 存放文件的buffer
	private int start = 0, end = 0;// 当前阅读的起止buffer偏移
	private long len;// buffer总长度
	private int lineNum;// 显示的最大行数
	private Vector<String> lines = new Vector<String>();// 待显示的内容
	private Paint paint;
	private String charset;// 编码
	private final int NUMBER = 4096;// 一次性从buffer中取出的字节数
	private SharedPreferences style;// 字体样式的配置文件
	private int marginWidth, marginHeight, spacing;
	private int defaultTextColor;

	public TXTPageFactory(int width, int height, TXTActivity context) {
		super(width, height);
		this.width = width;
		this.height = height;
		this.context = context;
		setTextStyle();
	}

	@Override
	public void openbook(Book book) throws IOException {
		String path = book.getPath() + "/" + book.getName();
		start = end = (int) book.getCurrentLocation();
		charset = FileUtils.getTXTCharset(new File(path));
		File file;
		Log.v("convertFileEncode", book.getName() + " old charset: " + charset);
		if (!charset.equals("UTF-8")) {
			long t1 = System.currentTimeMillis();
			FileUtils.convertFileEncode(new File(path), charset, "UTF-8");
			long t2 = System.currentTimeMillis();
			charset = "UTF-8";
			Log.v("convertFileEncode", "Total time: " + (t2 - t1) / 1000);
			file = new File(path);
			len = file.length();
			Log.v("totalPage", "old:" + book.getTotalPage() + " | new:" + len);
			book.setTotalPage(len);
			ContentResolver resolver = context.getContentResolver();
			ContentValues values = new ContentValues();
			values.put(DBSchema.COLUMN_BOOK_SIZE, FileUtils.getFilesize(file));
			values.put(DBSchema.COLUMN_BOOK_TOTAL_PAGE, len);// 转换编码后要更改book
																// length
			resolver.update(DBSchema.CONTENT_URI_BOOK, values, BaseColumns._ID
					+ " = " + book.getId(), null);
		} else {
			file = new File(path);
			len = file.length();
			Log.v("convertFileEncode", "No need");
		}
		buffer = new RandomAccessFile(file, "r").getChannel().map(
				FileChannel.MapMode.READ_ONLY, 0, len);
	}

	@Override
	public void onDraw(Canvas c, String searchText) {
		Log.v("onDraw","PageFactory onDraw...");
		paint.setColor(defaultTextColor);
		if (lines.size() == 0) {
			try {
				gotoPage(start, false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		c.drawColor(style.getInt(DBSchema.COLUMN_STYLE_BG_COLOR,
				Constant.STYLE_DEFAULT_BG_COLOR));
		int y = marginHeight * 2 - spacing;

		if (null == searchText) {
			for (String line : lines) {
				y += paint.getTextSize() + spacing;
				c.drawText(line, marginWidth, y, paint);
			}
		} else {
			StringBuffer sb = new StringBuffer();
			for (String line : lines) {
				sb.append(line);
			}
			String pageStr = sb.toString();
			List<Integer> list = new ArrayList<Integer>();
			int index = pageStr.indexOf(searchText);
			while (-1 < index) {
				list.add(index);
				pageStr = pageStr.substring(index + searchText.length());
				index = pageStr.indexOf(searchText);
			}
			for (int i = 1; i < list.size(); i++) {
				list.set(i, list.get(i) + list.get(i - 1) + searchText.length());
			}
			if (list.size() == 0) {// TODO 当前页没找到搜索结果
				for (String line : lines) {
					y += paint.getTextSize() + spacing;
					c.drawText(line, marginWidth, y, paint);
				}
			} else {
				int paintLoc = list.get(0);
				int paintLen = 0;
				boolean shouldHighlight = false;
				for (String line : lines) {
					y += paint.getTextSize() + spacing;
					String tempLine = line;
					String s;
					int drawX = marginWidth;
					if (shouldHighlight) {
						s = tempLine.substring(0,
								paintLoc + searchText.length() - paintLen);
						if (paintLoc + searchText.length() - paintLen <= tempLine
								.length()) {
							shouldHighlight = false;
							s = tempLine.substring(0,
									paintLoc + searchText.length() - paintLen);
						} else {
							s = tempLine;
						}
						drawX += drawTextWithBg(c, drawX, y, s);
						paintLen += s.length();
						list.remove(0);
						if (list.size() > 0) {
							paintLoc = list.get(0);
						} else {
							paintLoc = sb.toString().length();
						}
						if (tempLine.equals(searchText)) {
							continue;
						}
						tempLine = tempLine.substring(s.length());
					}
					while (paintLoc < paintLen + tempLine.length()) {
						s = tempLine.substring(0, paintLoc - paintLen);
						if (null != s) {
							c.drawText(s, drawX, y, paint);
							drawX += paint.measureText(s);
							paintLen += s.length();
						} else {
							s = "";
						}
						tempLine = tempLine.substring(s.length());
						if (searchText.length() <= tempLine.length()) {
							drawX += drawTextWithBg(c, drawX, y, searchText);
							paintLen += searchText.length();
							tempLine = tempLine.substring(searchText.length());
							list.remove(0);
							if (list.size() > 0) {
								paintLoc = list.get(0);
							} else {
								paintLoc = sb.toString().length();
							}
						} else {
							drawX += drawTextWithBg(c, drawX, y, tempLine);
							paintLen += tempLine.length();
							shouldHighlight = true;
							tempLine = null;
							break;
						}
					}
					if (null != tempLine && !"".equals(tempLine)) {
						c.drawText(tempLine, drawX, y, paint);
						drawX += paint.measureText(tempLine);
						paintLen += tempLine.length();
					}
				}
			}
		}
		if (end >= len | start >= len) {
			context.pageNumView.setText("100%");
		} else {
			float fPercent = (float) (start * 1.0 / len);
			DecimalFormat df = new DecimalFormat("#0.0");
			String strPercent = df.format(fPercent * 100) + "%";
			context.pageNumView.setText(strPercent);
		}
	}

	@Override
	public void gotoPage(long location, boolean handleMessyCode)
			throws IOException {
		if (location < 0) {
			location = 0;
		} else if (location >= len) {// 跳转到最后一页，显示最后约1024字节处的内容
			location = len - 1024;
			gotoPage(location, true);
			return;
		}
		start = getRealStart((int) location, handleMessyCode);
		end = start;
		lines.clear();
		byte[] pageBuf = pageContent(start, true);
		String pageString = new String(pageBuf, charset);
		int i = 0;
		while (pageString.length() > 0 && i < lineNum) {
			String lineDivision = "";
			int lineDivisionLoc = -1;
			if ((lineDivisionLoc = pageString.indexOf("\r\n")) != -1) {
				lineDivision = "\r\n";
			} else if ((lineDivisionLoc = pageString.indexOf("\n")) != -1) {
				lineDivision = "\n";
			} else if ((lineDivisionLoc = pageString.indexOf("\r")) != -1) {
				lineDivision = "\r";
			}
			int charLen = paint.breakText(pageString, true, vWidth, null);
			if (lineDivisionLoc > -1 && lineDivisionLoc <= charLen) {
				String str = pageString.substring(0, lineDivisionLoc);
				end += (str + lineDivision).getBytes(charset).length;
				pageString = pageString
						.substring((str + lineDivision).length());
				if (null != str && !"".equals(str)) {// 去空行
					lines.add(str);
					i++;
				}
				continue;
			}
			int division = ParserUtils.getDivision(charLen, pageString);
			lines.add(pageString.substring(0, division));
			end += lines.get(i).getBytes(charset).length;
			pageString = pageString.substring(division);
			i++;
		}
	}

	@Override
	public void prePage() throws IOException {
		if (start <= 0) {
			start = 0;
			gotoPage(start, false);
			return;
		}
		Vector<String> tLines = new Vector<String>();
		String strParagraph = "";
		while (tLines.size() < lineNum && start > 0) {
			Vector<String> paraLines = new Vector<String>();
			byte[] paraBuf = readParagraphBack(start);
			start -= paraBuf.length;
			try {
				strParagraph = new String(paraBuf, charset);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			strParagraph = removeBlankLines(strParagraph);

			while (strParagraph.length() > 0) {
				int nSize = paint.breakText(strParagraph, true, vWidth, null);
				int division = ParserUtils.getDivision(nSize, strParagraph);
				paraLines.add(strParagraph.substring(0, division));
				strParagraph = strParagraph.substring(division);
			}
			tLines.addAll(0, paraLines);
		}
		while (tLines.size() > lineNum) {
			try {
				start += tLines.get(0).getBytes(charset).length;
				tLines.remove(0);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		end = start;
		gotoPage(start, false);
	}

	@Override
	public void nextPage() throws IOException {
		if (this.islastPage())
			return;
		start = end;
		gotoPage(start, false);
	}

	@Override
	public boolean isfirstPage() {
		return start == 0;
	}

	@Override
	public boolean islastPage() {
		return end >= len;
	}

	@Override
	public long getReadingLocation() {
		return start;
	}

	@Override
	public void setReadingLocation(long location) {
		start = (int) location;
	}

	@Override
	public void addBookmark() {
		Cursor cursor = context.getContentResolver().query(
				DBSchema.CONTENT_URI_BOOKMARK,
				new String[] { BaseColumns._ID },
				DBSchema.COLUMN_CHAPTER_BOOKID + "=? AND "
						+ DBSchema.COLUMN_BOOKMARK_LOCATION + "=?",
				new String[] { context.book.getId() + "", start + "" }, null);

		if (cursor.moveToFirst()) {
			Toast.makeText(context, R.string.bookmark_exist, Toast.LENGTH_SHORT)
					.show();
			return;
		}
		String str = "";
		final int tlen = 60;
		byte[] buf = new byte[tlen];
		if (len < start + tlen) {
			for (int i = start; i < len; i++) {
				buf[i] = buffer.get(i);
			}
		} else {
			for (int i = 0; i < tlen; i++) {
				buf[i] = buffer.get(start + i);
			}
		}
		try {
			str = new String(buf, charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		str = removeBlankLines(str);
		ContentValues values = new ContentValues();
		values.put(DBSchema.COLUMN_BOOKMARK_LOCATION, start);
		values.put(DBSchema.COLUMN_BOOKMARK_DESCRIPTION, str);
		values.put(DBSchema.COLUMN_BOOKMARK_BOOKID, context.book.getId());
		context.getContentResolver().insert(DBSchema.CONTENT_URI_BOOKMARK,
				values);
		Toast.makeText(context, R.string.bookmark_added, Toast.LENGTH_SHORT)
				.show();
	}

	/**
	 * 删除空行
	 * 
	 * @param para
	 * @return
	 */
	private String removeBlankLines(String para) {
		para = para.replaceAll("\r\n", "");
		para = para.replaceAll("\n", "");
		return para.replaceAll("\r", "");
	}

	/**
	 * 从fromPos查找从语义和语法意义上真正合法的开始位置
	 * 
	 * @param fromPos
	 * @param handleMessyCode
	 *            如果为真，则fromPos可能不是合法的开始位置
	 * @return
	 */
	private int getRealStart(int fromPos, boolean handleMessyCode) {
		if (!handleMessyCode)
			return fromPos;
		int rStart = fromPos;
		char c;
		byte b1;
		while (rStart > 0 && rStart < len - 1) {
			c = buffer.getChar(rStart);
			b1 = buffer.get(rStart);
			if (b1 == 0x0a || b1 == 0x0d) {
				if (charset.equals("UTF-16LE")) {
					rStart += 2;
					break;
				} else {
					rStart++;
					break;
				}
			}
			if (ParserUtils.isPunctuation(c) || c == ' ' || c == '　') {
				rStart++;
				break;
			}
			rStart--;
		}
		if (rStart >= len)
			rStart = (int) len;
		return rStart;
	}

	/**
	 * 得到展示页的内容
	 * 
	 * @param fromPos
	 * @param isPageDown
	 *            是否为下一页
	 * @return
	 */
	private byte[] pageContent(int fromPos, boolean isPageDown) {
		byte[] arr;
		if (isPageDown) {
			int rlen = NUMBER;
			if (fromPos + NUMBER >= len) {
				rlen = (int) (len - fromPos);
			}
			arr = new byte[rlen];
			for (int i = 0; i < rlen; i++) {
				arr[i] = buffer.get(fromPos + i);
			}
		} else {
			if (fromPos - NUMBER < 0) {
				arr = new byte[fromPos];
				for (int i = 0; i < fromPos; i++) {
					arr[i] = buffer.get(i);
				}
			} else {
				int loc = fromPos - NUMBER;
				int rStart = getRealStart(loc, true);
				int rNumber = fromPos - rStart;
				arr = new byte[rNumber];
				for (int i = 0; i < rNumber; i++) {
					arr[i] = buffer.get(rStart + i);
				}
			}
		}
		return arr;
	}

	protected byte[] readParagraphBack(int nFromPos) {
		int nEnd = nFromPos;
		int i;
		byte b0, b1;
		if (charset.equals("UTF-16LE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = buffer.get(i);
				b1 = buffer.get(i + 1);
				if ((b0 == 0x0a || b0 == 0x0d) && b1 == 0x00 && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}

		} else if (charset.equals("UTF-16BE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = buffer.get(i);
				b1 = buffer.get(i + 1);
				if (b0 == 0x00 && (b1 == 0x0a || b1 == 0x0d) && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}
		} else {
			i = nEnd - 1;
			while (i > 0) {
				b0 = buffer.get(i);
				if (b0 == 0x0a && i != nEnd - 1) {
					i++;
					break;
				}
				i--;
			}
		}
		if (i < 0)
			i = 0;
		int nParaSize = nEnd - i;
		int j;
		byte[] buf = new byte[nParaSize];
		for (j = 0; j < nParaSize; j++) {
			buf[j] = buffer.get(i + j);
		}
		return buf;
	}

	@Override
	public void setTextStyle() {
		style = context.getSharedPreferences(Constant.STYLE_REFERENCE, 0);
		paint = FactoryUtils.getTextStyle(context);
		marginWidth = style.getInt(DBSchema.COLUMN_STYLE_MARGIN_WIDTH,
				Constant.STYLE_DEFAULT_MARGIN_WIDTH);
		marginHeight = style.getInt(DBSchema.COLUMN_STYLE_MARGIN_HEIGHT,
				Constant.STYLE_DEFAULT_MARGIN_HEIGHT);
		vWidth = width - marginWidth * 2;
		vHeight = height - marginHeight * 2;
		spacing = style.getInt(DBSchema.COLUMN_STYLE_LINE_SPACING,
				Constant.STYLE_DEFAULT_LINE_SPACING);
		lineNum = (int) ((vHeight - spacing) / (paint.getTextSize() + spacing)) - 2; // 可显示的行数,-2为了页面美观
		defaultTextColor = style.getInt(DBSchema.COLUMN_STYLE_TEXT_COLOR,
				Constant.STYLE_DEFAULT_TEXT_COLOR);
	}

	/**
	 * 高亮显示drawText。
	 * 
	 * @param canvas
	 * @param x
	 * @param y
	 * @param drawText
	 * @return drawText的宽度。
	 */
	private float drawTextWithBg(Canvas canvas, float x, float y,
			String drawText) {
		y -= paint.getTextSize();
		Paint paintPath = new Paint();
		paintPath.setColor(Color.LTGRAY);
		Path path = new Path();
		float width = paint.measureText(drawText);
		RectF rect = new RectF(x, y, x + width, y + paint.getTextSize() + 5);
		path.addRect(rect, Direction.CW);
		canvas.drawPath(path, paintPath);
		canvas.drawTextOnPath(drawText, path, 0, paint.getTextSize(), paint);
		return width;
	}

}
