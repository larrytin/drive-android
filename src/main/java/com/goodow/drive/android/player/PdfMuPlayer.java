package com.goodow.drive.android.player;

import com.goodow.android.drive.R;
import com.goodow.drive.android.activity.BaseActivity;
import com.goodow.realtime.json.JsonObject;

import com.artifex.mupdf.MuPDFActivity;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

/**
 * @title: SamplePDF.java
 * @package drive-android
 * @description: PDF阅读器调用示例
 * @author www.dingpengwei@gmail.com
 * @createDate 2013 2013-12-4 上午10:48:34
 * @updateDate 2013 2013-12-4 上午10:48:34
 * @version V1.0
 */
public class PdfMuPlayer extends BaseActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    JsonObject jsonObject = (JsonObject) getIntent().getExtras().getSerializable("msg");
    File newFile = new File(jsonObject.getString("path"));
    if (newFile.exists()) {
      Uri uri = Uri.parse(newFile.getPath());
      Intent intent = new Intent(this, MuPDFActivity.class);
      intent.setAction(Intent.ACTION_VIEW);
      intent.setData(uri);
      startActivity(intent);
      this.finish();
    } else {
      Toast.makeText(this, this.getString(R.string.pdf_file_no_exist), Toast.LENGTH_SHORT).show();
    }
    super.onCreate(savedInstanceState);
  }

}
