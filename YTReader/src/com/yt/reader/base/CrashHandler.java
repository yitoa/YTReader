package com.yt.reader.base;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import com.yt.reader.MainActivity;
import com.yt.reader.utils.DateUtils;
import com.yt.reader.utils.FileUtils;

/**
 * 处理未捕捉的异常。
 * 
 * @author lsj
 * 
 */
public class CrashHandler implements UncaughtExceptionHandler {
	private Context context;

	public CrashHandler(Context context) {
		this.context = context;
	}

	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		System.gc();
		System.gc();
		Log.v("exception", exception.getCause().toString());
		final StringWriter stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		String filePath = FileUtils.getSDPath() + "/readerError.txt";
		try {
			BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(filePath), "UTF-8"));
			output.append("\n===============[Exception]"
					+ DateUtils.dateToString(new Date(), "yyyy-MM-dd hh:mm:ss")
					+ "===============\n");
			output.append(stackTrace.toString());
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.v("exception", stackTrace.toString());
		Intent intent = new Intent(context, MainActivity.class);
		context.startActivity(intent);

		if (context instanceof Activity) {
			((Activity) context).finish();
		}
		Process.killProcess(Process.myPid());
		System.exit(1);
	}

	public static void uncaughtError(Context context, Error error) {
		System.gc();
		System.gc();
		Log.e("error", error.getCause().toString());
		final StringWriter stackTrace = new StringWriter();
		error.printStackTrace(new PrintWriter(stackTrace));
		String filePath = FileUtils.getSDPath() + "/readerError.txt";
		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(filePath,
					true));
			output.append("\n===============[Error]"
					+ DateUtils.dateToString(new Date(), "yyyy-MM-dd hh:mm:ss")
					+ "===============\n");
			output.append(stackTrace.toString());
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.e("error", stackTrace.toString());
		Intent intent = new Intent(context, MainActivity.class);
		context.startActivity(intent);

		if (context instanceof Activity) {
			((Activity) context).finish();
		}
		Process.killProcess(Process.myPid());
		System.exit(1);
	}

}
