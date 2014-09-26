package com.yt.reader.utils;

import java.math.BigDecimal;

public class MathUtils {

	public static double round(double number, int scale,int roundMode) {
		return new BigDecimal(number).setScale(scale,roundMode).doubleValue();
	}
}
