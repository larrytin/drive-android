package com.goodow.drive.android.toolutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;

public class UnzipAsserts {

  /**
   * 
   * @param mContext
   * @param assertName assert目录下文件
   * @param outputDirectory 输出目录
   * @param isReWrite 是否覆盖
   * @throws IOException
   */
  public static void unZip(final Context mContext, final String assertName,
      final String outputDirectory, final boolean isReWrite) throws IOException {
    File file = new File(outputDirectory);
    if (!file.exists()) {
      file.mkdirs();
    }
    InputStream inputStream = mContext.getAssets().open(assertName);
    ZipInputStream zipInputStream = new ZipInputStream(inputStream);
    File mfile;
    ZipEntry zipEntry = zipInputStream.getNextEntry();
    byte[] buffer = new byte[1024 * 1024];
    int count = 0;
    while (zipEntry != null) {
      mfile = new File(outputDirectory + File.separator + zipEntry.getName());
      if (zipEntry.isDirectory()) {
        if (isReWrite || !mfile.exists()) {
          mfile.mkdir();
        }
      } else {
        if (isReWrite || !mfile.exists()) {
          FileOutputStream fileOutputStream = new FileOutputStream(mfile);
          while ((count = zipInputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, count);
          }
          fileOutputStream.close();
        }
      }
      zipEntry = zipInputStream.getNextEntry();
    }
    zipInputStream.close();
  }
}
