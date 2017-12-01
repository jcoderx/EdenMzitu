package cn.xudaodao.file;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class WriteFileManager {
	private static volatile WriteFileManager instance = null;
	private static final String EDEN_MZITU_FILE = "/Users/trustdata/Documents/eden_mzitu.txt";

	private FileOutputStream fileOutputStream = null;

	private WriteFileManager() {
	}

	public static WriteFileManager getInstance() {
		if (instance == null) {
			synchronized (WriteFileManager.class) {
				if (instance == null) {
					instance = new WriteFileManager();
				}
			}
		}
		return instance;
	}

	public void init() {
		try {
			fileOutputStream = new FileOutputStream(EDEN_MZITU_FILE);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public synchronized void writeLine(String text) {
		try {
			fileOutputStream.write(text.getBytes("UTF-8"));
			fileOutputStream.write("\r\n".getBytes("UTF-8"));
			fileOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		if (fileOutputStream != null) {
			try {
				fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
