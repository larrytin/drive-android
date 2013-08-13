package com.goodow.drive.android.toolutils;

import java.io.File;

import android.text.TextUtils;

public final class FileTools {
	private FileTools() {
		
	}
	
	/**
	 * @param filePath 文件的完整路径
	 * @return
	 */
	public static boolean fileIsExist(String filePath) {
		boolean isExist = false;
		do {
			if (TextUtils.isEmpty(filePath)) {
				break;
			}
			File file = new File(filePath);
			if (!file.exists()) {
				break;
			}
			isExist = true;
		} while (false);
		return isExist;
	}
}
