package com.goodow.drive.android.activity.play;

import java.util.List;
import com.goodow.android.drive.R;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.TextUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.TextView;

@SuppressLint("SetJavaScriptEnabled")
public class FlashPlayerActivity extends Activity {

  public static enum IntentExtraTagEnum {
    // flash ��Դ���
    FLASH_NAME,
    // flash ��Դ����path(�Ѿ����ص��˱���)
    FLASH_PATH_OF_LOCAL_FILE,
    // flash ��Դ������url
    FLASH_PATH_OF_SERVER_URL
  };

  private String filePath;
  private WebView flashWebView;
  private Handler mHandler = new Handler();
  private ProgressDialog mProgressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_flash_player);

    if (getIntent().hasExtra(IntentExtraTagEnum.FLASH_PATH_OF_LOCAL_FILE.name())) {
      filePath = getIntent().getStringExtra(IntentExtraTagEnum.FLASH_PATH_OF_LOCAL_FILE.name());
    } else {
      filePath = getIntent().getStringExtra(IntentExtraTagEnum.FLASH_PATH_OF_SERVER_URL.name());
    }

    String flashfileName = getIntent().getStringExtra(IntentExtraTagEnum.FLASH_NAME.name());

    final TextView flashFileNameTextView = (TextView) this.findViewById(R.id.flash_file_name_textView);
    if (!TextUtils.isEmpty(flashfileName)) {
      flashFileNameTextView.setText(flashfileName);
    }
    flashWebView = (WebView) findViewById(R.id.flash_webView);
    setTitle("Flash播放器");
    setTitleColor(Color.RED);
    WebSettings webSettings = flashWebView.getSettings();
    webSettings.setPluginState(PluginState.ON);
    webSettings.setSupportZoom(true);
    // WebView����Javascript�ű�ִ��
    // webSettings.setJavaScriptEnabled(true);
    // webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

    // flashWebView.addJavascriptInterface(new JSInvokeClass(), "CallJava");

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    mProgressDialog = ProgressDialog.show(this, "请稍等...", "加载Flash中...", true);
    flashWebView.setWebChromeClient(new WebChromeClient() {
      @Override
      public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        System.out.println("newProgress:" + String.valueOf(newProgress));
        if (newProgress == 100) {
          new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
              mProgressDialog.dismiss();
            }
          }, 500);
        }
      }
    });
    if (checkinstallornotadobeflashapk()) {
      flashWebView.loadUrl(filePath);
    } else {
      installadobeapk();
    }

  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    flashWebView.destroy();
    this.finish();
    System.gc();
  }

  @Override
  public void onBackPressed() {
    flashWebView.destroy();
    this.finish();
    System.gc();
    super.onBackPressed();
  }

  @Override
  protected void onUserLeaveHint() {
    flashWebView.destroy();
    this.finish();
    System.gc();
    super.onUserLeaveHint();
  }

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

  @SuppressLint("JavascriptInterface")
  private void installadobeapk() {
    flashWebView.addJavascriptInterface(new AndroidBridge(), "android");
    flashWebView.loadUrl("file:///android_asset/go_market.html");
  }

  private class AndroidBridge {
    public void goMarket() {
      mHandler.post(new Runnable() {
        public void run() {
          Intent installIntent = new Intent("android.intent.action.VIEW");
          installIntent.setData(Uri.parse("market://details?id=com.adobe.flashplayer"));
          startActivity(installIntent);
        }
      });
    }
  }

}
