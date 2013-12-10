package com.goodow.drive.android.player;

import com.goodow.realtime.json.JsonObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * @title: SampleVideo.java
 * @package drive-android
 * @description: Video播放器调用示例
 * @author www.dingpengwei@gmail.com
 * @createDate 2013 2013-12-4 上午11:57:29
 * @updateDate 2013 2013-12-4 上午11:57:29
 * @version V1.0
 */
public class SampleVideo extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    JsonObject jsonObject = (JsonObject) getIntent().getExtras().getSerializable("msg");
    Intent intent = new Intent(this, VideoActivity.class);
    intent.setData(Uri.parse("file:///mnt/sdcard/" + jsonObject.getString("path")));
    this.startActivity(intent);
    super.onCreate(savedInstanceState);
  }
}
