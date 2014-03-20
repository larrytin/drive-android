package com.goodow.drive.android.toolutils;

import com.goodow.android.drive.R;

import java.io.File;

import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

public final class FileTools {
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

  /**
   * 设置文件背景图片
   * 
   * @param imageView
   * @param name
   * @param thumbnail
   */
  public static void setImageThumbnalilUrl(ImageView imageView, String filePath, String thumbnail) {
    // 判断指定的缩略图是否存在
    if (thumbnail != null && new File(thumbnail).exists()) {
      // 加载存在的缩略图
      imageView.setImageURI(Uri.parse(thumbnail));
    } else {
      // 加载默认的缩略图
      if (filePath.endsWith(".mp3")) {
        imageView.setImageResource(R.drawable.behave_mp3);
      } else if (filePath.endsWith(".mp4")) {
        imageView.setImageResource(R.drawable.behave_mp4);
      } else if (filePath.endsWith(".swf")) {
        imageView.setImageResource(R.drawable.behave_flash);
      } else if (filePath.endsWith(".pdf")) {
        imageView.setImageResource(R.drawable.behave_ebook);
      } else if (filePath.endsWith(".jpg")) {
        imageView.setImageResource(R.drawable.behave_image);
      } else if (filePath.endsWith(".jpeg")) {
        imageView.setImageResource(R.drawable.behave_image);
      } else {
        imageView.setImageResource(R.drawable.behave_image);
      }
    }
  }

}
