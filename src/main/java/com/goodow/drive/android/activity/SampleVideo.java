package com.goodow.drive.android.activity;

import com.goodow.drive.android.video.VideoActivity;

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
    Intent intent = new Intent(this, VideoActivity.class);
    intent.setData(Uri.parse("file:///mnt/sdcard/" + getIntent().getExtras().getString("path")));
    this.startActivity(intent);
    super.onCreate(savedInstanceState);
  }
}
