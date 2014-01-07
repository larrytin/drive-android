package com.goodow.drive.android.player;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.activity.BaseActivity;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import java.io.File;
import java.util.List;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

public class WebViewFlashPlayer extends BaseActivity {
  private WebView flashWebView;
  private ImageView mImageView;
  private final static String TAG = WebViewFlashPlayer.class.getSimpleName();

  @Override
  public void onPause() {
    super.onPause();
    Log.d(TAG, "onPause()");
    if (null != flashWebView) {
      flashWebView.onPause();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d(TAG, "onResume()");
    if (null != flashWebView) {
      flashWebView.onResume();
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.actvity_flash_webview);
    mImageView = (ImageView) this.findViewById(R.id.iv_act_favour_back);
    mImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
      }
    });
    flashWebView = (WebView) findViewById(R.id.flash_webView_player);
    WebSettings mWebSettings = flashWebView.getSettings();
    mWebSettings.setPluginState(PluginState.ON);
    // 检测flash插件是否存在
    if (checkinstallornotadobeflashapk()) {
      flashPlay(getIntent());
    } else {
      Toast.makeText(this, R.string.prompt_flash_Plugins, Toast.LENGTH_LONG).show();
    }
    Log.d(TAG, "onCreate()");
  }

  // 退出时关闭flash播放
  @Override
  protected void onDestroy() {
    Log.d(TAG, "onDestroy()");
    super.onDestroy();
    if (flashWebView != null) {
      flashWebView.destroy();
    }
    System.gc();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    Log.d(TAG, "onNewIntent()");
    if (checkinstallornotadobeflashapk()) {
      flashPlay(intent);
    } else {
      Toast.makeText(this, R.string.prompt_flash_Plugins, Toast.LENGTH_LONG).show();
    }
    super.onNewIntent(intent);

  }

  @Override
  protected void onRestart() {
    Log.d(TAG, "onRestart()");
    super.onRestart();
  }

  @Override
  protected void onStart() {
    Log.d(TAG, "onStart()");
    super.onStart();
  }

  @Override
  protected void onStop() {
    Log.d(TAG, "onStop()");
    super.onStop();
  }

  // 检查机子是否安装的有Adobe Flash相关APK
  private boolean checkinstallornotadobeflashapk() {
    PackageManager pm = getPackageManager();
    List<PackageInfo> infoList = pm.getInstalledPackages(PackageManager.GET_SERVICES);
    for (PackageInfo info : infoList) {
      if ("com.adobe.flashplayer".equals(info.packageName)) {
        return true;
      }
    }
    return false;
  }

  private void flashPlay(Intent intent) {
    // 得到路径
    JsonObject msg = (JsonObject) intent.getExtras().get("msg");
    String path = msg.get("path");
    Log.d(TAG, path);
    File mFile = new File(path);
    if (mFile.exists()) {
      flashWebView.loadUrl("file:/" + path);
    } else {
      Toast.makeText(this, R.string.pdf_file_no_exist, Toast.LENGTH_LONG).show();
    }
  }

}
