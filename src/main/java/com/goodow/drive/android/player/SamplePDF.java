package com.goodow.drive.android.player;

import com.goodow.realtime.json.JsonObject;

import com.artifex.mupdf.MuPDFActivity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * @title: SamplePDF.java
 * @package drive-android
 * @description: PDF阅读器调用示例
 * @author www.dingpengwei@gmail.com
 * @createDate 2013 2013-12-4 上午10:48:34
 * @updateDate 2013 2013-12-4 上午10:48:34
 * @version V1.0
 */
public class SamplePDF extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    JsonObject jsonObject = (JsonObject) getIntent().getExtras().getSerializable("msg");
    File newFile = new File("/mnt/sdcard/" + jsonObject.getString("path"));
    if (newFile.exists()) {
      Uri uri = Uri.parse(newFile.getPath());
      Intent intent = new Intent(this, MuPDFActivity.class);
      intent.setAction(Intent.ACTION_VIEW);
      intent.setData(uri);
      startActivity(intent);
      this.finish();
    }
    super.onCreate(savedInstanceState);
  }

}
