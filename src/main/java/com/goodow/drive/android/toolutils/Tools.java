package com.goodow.drive.android.toolutils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class Tools {
  public static String getMIMETypeByType(String type) {
    String mimeType = null;

    for (MIME_TYPE_Table eTable : MIME_TYPE_Table.values()) {
      if (type.equals(eTable.getType())) {
        mimeType = eTable.getMimeType();

      }
    }

    return mimeType;
  }

  public static String getTypeByMimeType(String mimeType) {
    String type = null;

    for (MIME_TYPE_Table eTable : MIME_TYPE_Table.values()) {
      if (eTable.getMimeType().equals(mimeType)) {
        type = eTable.getType();
      }
    }

    return type;
  }

  public static enum MIME_TYPE_Table {
    RES_3gp("3gp", "video/3gpp"), RES_pdf("pdf", "application/pdf"), RES_png("png", "image/png"), RES_txt("txt", "text/plain"), RES_doc("doc",
        "application/msword"), RES_xls("xls", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), RES_htm("htm", "text/html"), RES_html(
        "html", "text/html"), RES_bmp("bmp", "image/bmp"), RES_gif("gif", "image/gif"), RES_jpg("jpg", "image/jpeg"), RES_java("java", "text/plain"), RES_mp3(
        "mp3", "audio/mp3"), RES_mp4("mp4", "video/mp4");

    private final String type;
    private final String mimeType;

    private MIME_TYPE_Table(String type, String mimeType) {
      this.type = type;
      this.mimeType = mimeType;
    }

    public String getType() {

      return type;
    }

    public String getMimeType() {

      return mimeType;
    }

  }

  /**
   * 获取当前分辨率下指定单位对应的像素大小（根据设备信息） px,dip,sp -> px
   * 
   * Paint.setTextSize()单位为px
   * 
   * 代码摘自：TextView.setTextSize()
   * 
   * @param unit
   *          TypedValue.COMPLEX_UNIT_*
   * @param size
   * @return
   */
  public static float getRawSize(int unit, float size) {
    Context c = MyApplication.getApplication();
    Resources r;
    
    if (c == null)
      r = Resources.getSystem();
    else
      r = c.getResources();

    return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
  }
}
