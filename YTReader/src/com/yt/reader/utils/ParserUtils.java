package com.yt.reader.utils;

import java.text.DecimalFormat;

/**
 * 用于解析文本的帮助类。
 * 
 * @author lsj
 * 
 */
public class ParserUtils {

	/**
	 * 判断是否为英文字符
	 * 
	 * @param c
	 * @return 如果c为英文字符，返回true；否则，返回false。
	 */
	public static boolean isEnglishLetter(char c) {
		return (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z');
	}

	/**
	 * 判断是否为标点符号
	 * 
	 * @return
	 */
	public static boolean isPunctuation(char c) {
		return c == '.' || c == ',' || c == ')' || c == '!' || c == '?'
				|| c == '；' || c == '。' || c == '，' || c == '）' || c == '！'
				|| c == '？' || c == '、' || c == ':' || c == '：' || c == '\''
				|| c == '"' || c == '‘' || c == '”';
	}

	/**
	 * 根据文本的语法和语义划分一行的长度。
	 * 
	 * @param size
	 *            行的初始字符数
	 * @param para
	 *            文件段
	 * @return 处理后的每行字符数
	 */
	public static int getDivision(int size, String para) {
		if (null == para || size <= 1)
			return size;
		else if (para.length() <= size) {
			return para.length();
		}
		String line = para.substring(0, size);
		char c = line.charAt(size - 1);
		if (isEnglishLetter(c) && isEnglishLetter(para.charAt(size))) {
			String[] arr = line.split("\\s");
			int len = arr[arr.length - 1].length();
			if (line.length() - len == 0) {
				size = line.length();
			} else {
				size = line.length() - len;
			}
		}

		c = para.charAt(size);
		if (c == '.' || c == ',' || c == ')' || c == '!' || c == '?'
				|| c == '；' || c == '。' || c == '，' || c == '）' || c == '！'
				|| c == '？' || c == '、') {
			size++;
			if (para.length() > size) {
				c = para.charAt(size);
				if (c == '\'' || c == '"' || c == '‘' || c == '”') {
					size++;
				}
			}
		} else if (c == ':' || c == '：') {
			size++;
		}


		return size;
	}

	/**
	 * 去掉s的前导空格，包括全角空格。
	 * 
	 * @param s
	 * @return
	 */
	public static String trim(String s) {
		s = s.trim();
		return s.replaceFirst("　*", "");
	}

	/**
	 * 计算location所在位置占总偏移的百比分
	 * 
	 * @param location
	 * @param totalPage
	 * @return
	 */
	public static String getPercent(long location, long totalPage) {
		float fPercent = (float) (location * 1.0 / totalPage);
		DecimalFormat df = new DecimalFormat("#0.0");
		return df.format(fPercent * 100) + "%";
	}

}
