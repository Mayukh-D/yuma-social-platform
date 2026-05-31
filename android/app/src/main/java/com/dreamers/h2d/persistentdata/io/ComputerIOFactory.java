package com.dreamers.h2d.persistentdata.io;

import android.content.Context;

import java.io.*;

public class ComputerIOFactory implements IOFactory {

	private static Context appContext;

	public static void init(Context context) {
		appContext = context.getApplicationContext();
	}

	private File getFile(String filename) {
		File dir = new File(appContext.getFilesDir(), "saved");
		if (!dir.exists()) dir.mkdirs();
		return new File(dir, filename + ".txt");
	}

	@Override
	public Writer writer(String filename) {
		try {
			return new FileWriter(getFile(filename));
		} catch (IOException ignored) {
			return null;
		}
	}

	@Override
	public Reader reader(String filename) {
		try {
			File f = getFile(filename);
			if (!f.exists() || f.length() == 0) return null;
			return new FileReader(f);
		} catch (IOException ignored) {
			return null;
		}
	}
}