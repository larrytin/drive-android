package com.goodow.drive.android.toolutils;

import com.goodow.drive.android.module.DriveModule;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public class Tools {
  /**
   * @author Administrator 各种MIME TYPE,欢迎添加;
   */
  public static enum MIME_TYPE_Table {
    RES_3gp("3gp", "video/3gpp"), RES_pdf("pdf", "application/pdf"), RES_png("png", "image/png"), RES_txt(
        "txt", "text/plain"), RES_doc("doc", "application/msword"), RES_xls("xls",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"), RES_htm("htm",
        "text/html"), RES_html("html", "text/html"), RES_bmp("bmp", "image/bmp"), RES_gif("gif",
        "image/gif"), RES_jpg("jpg", "image/jpeg"), RES_java("java", "text/plain"), RES_mp3("mp3",
        "audio/mp3"), RES_mp4("mp4", "video/mp4"), RES_flash("swf", "application/x-shockwave-flash"), RES_PRINT(
        "print", "application/x-print");

    private final String type;
    private final String mimeType;

    private MIME_TYPE_Table(String type, String mimeType) {
      this.type = type;
      this.mimeType = mimeType;
    }

    public String getMimeType() {

      return mimeType;
    }

    public String getType() {

      return type;
    }

  }

  // private final static String TAG = Tools.class.getSimpleName();
  private static final Logger LOGGER = Logger.getLogger(Tools.class.getName());

  /**
   * 根据支持格式获得对应的MIME TYPE
   * 
   * @param type SupportResTypeEnum.typename
   * @return MIME_TYPE_Table.mimeType
   */
  public static String getMIMETypeByType(String type) {
    String mimeType = null;

    for (MIME_TYPE_Table eTable : MIME_TYPE_Table.values()) {
      if (type.equals(eTable.getType())) {
        mimeType = eTable.getMimeType();

      }
    }

    return mimeType;
  }

  /**
   * 获取当前分辨率下指定单位对应的像素大小（根据设备信息） px,dip,sp -> px
   * 
   * Paint.setTextSize()单位为px
   * 
   * 代码摘自：TextView.setTextSize()
   * 
   * @param unit TypedValue.COMPLEX_UNIT_*
   * @param size
   * @return
   */
  public static float getRawSize(int unit, float size) {
    Context c = MyApplication.getApplication();
    Resources r;

    if (c == null) {
      r = Resources.getSystem();
    } else {
      r = c.getResources();
    }

    return TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
  }

  /**
   * 根据MIME TYPE获得对应的支持格式
   * 
   * @param mimeType MIME_TYPE_Table.mimeType
   * @return SupportResTypeEnum.typename
   */
  public static String getTypeByMimeType(String mimeType) {
    String type = null;

    for (MIME_TYPE_Table eTable : MIME_TYPE_Table.values()) {
      if (eTable.getMimeType().equals(mimeType)) {
        type = eTable.getType();
      }
    }

    return type;
  }

  public static String modifyThumbnailAddress(String thumbnailurl) {
    if (!DriveModule.DRIVE_SERVER.contains(".goodow.com")) {
      StringBuilder sb = new StringBuilder(DriveModule.DRIVE_SERVER);
      String file = null;
      try {
        URL url = new URL(thumbnailurl);
        file = url.getFile();
      } catch (MalformedURLException e) {
        LOGGER.log(Level.SEVERE, "Malformed URL Exception", e);
      }
      sb.append(file);
      thumbnailurl = sb.toString();
    }
    return thumbnailurl;
  }
}
