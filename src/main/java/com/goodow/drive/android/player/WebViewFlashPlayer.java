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
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class WebViewFlashPlayer extends BaseActivity {
  private WebView flashWebView;
  private ImageView mImageView;
  private LinearLayout mLinearLayout;

  @Override
  public void onPause() {
    super.onPause();
    if (null != flashWebView) {
      flashWebView.onPause();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
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
        System.gc();
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
      }
    });
    flashWebView = (WebView) findViewById(R.id.flash_webView_player);
    mLinearLayout = (LinearLayout) this.findViewById(R.id.ll_act_flash_webview);
    WebSettings mWebSettings = flashWebView.getSettings();
    mWebSettings.setPluginState(PluginState.ON);
    // 检测flash插件是否存在
    if (checkinstallornotadobeflashapk()) {
      flashPlay(getIntent());
    } else {
      Toast.makeText(this, R.string.prompt_flash_Plugins, Toast.LENGTH_LONG).show();
    }
  }

  // 退出时关闭flash播放
  @Override
  protected void onDestroy() {
    super.onDestroy();
    mLinearLayout.removeView(flashWebView);
    flashWebView.removeAllViews();
    if (flashWebView != null) {
      flashWebView.destroy();
    }
    System.gc();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (checkinstallornotadobeflashapk()) {
      flashPlay(intent);
    } else {
      Toast.makeText(this, R.string.prompt_flash_Plugins, Toast.LENGTH_LONG).show();
    }
    super.onNewIntent(intent);

  }

  @Override
  protected void onRestart() {
    super.onRestart();
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop() {
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
    File mFile = new File(path);
    if (mFile.exists()) {
      flashWebView.loadUrl("file://" + path);
    } else {
      Toast.makeText(this, R.string.pdf_file_no_exist, Toast.LENGTH_LONG).show();
    }
  }

}
