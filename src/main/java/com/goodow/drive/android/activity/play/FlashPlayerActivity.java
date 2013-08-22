package com.goodow.drive.android.activity.play;

import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.TextView;
import com.goodow.android.drive.R;
import com.goodow.drive.android.toolutils.JSInvokeClass;

@SuppressLint("SetJavaScriptEnabled")
public class FlashPlayerActivity extends Activity {

  public static enum IntentExtraTagEnum {
    // flash 资源名称
    FLASH_NAME,
    // flash 资源完整path(已经下载到了本地)
    FLASH_PATH_OF_LOCAL_FILE,
    // flash 资源的网络url
    FLASH_PATH_OF_SERVER_URL
  };

  private String localFlashFilePath;
  private String filePath;
  private WebView flashWebView;
  private Handler mHandler = new Handler();
  private ProgressDialog mProgressDialog;

  @SuppressLint("JavascriptInterface")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_flash_player);

    // 获取从外部传进来的 flash资源完整路径
    if (getIntent().hasExtra(IntentExtraTagEnum.FLASH_PATH_OF_LOCAL_FILE.name())) {
      filePath = "file:///android_asset/flash_loading.html";
      localFlashFilePath = getIntent().getStringExtra(IntentExtraTagEnum.FLASH_PATH_OF_LOCAL_FILE.name());
    } else {
      // url
      filePath = getIntent().getStringExtra(IntentExtraTagEnum.FLASH_PATH_OF_SERVER_URL.name());
    }

    // 获取从外部传进来的 flash资源完整路径
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
    // WebView启用javascript脚本执行
    webSettings.setJavaScriptEnabled(true);
    webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

    JSInvokeClass jsInvokeClass = new JSInvokeClass();
    jsInvokeClass.setFlashFilePath(localFlashFilePath);
    flashWebView.addJavascriptInterface(jsInvokeClass, "CallJava");

    try {
      Thread.sleep(500);// 主线程暂停下，否则容易白屏，原因未知
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    mProgressDialog = ProgressDialog.show(this, "请稍等...", "加载flash中...", true);
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
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    // getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }

  // 退出时关闭flash播放
  @Override
  protected void onDestroy() {
    super.onDestroy();
    flashWebView.destroy();
    this.finish();
    System.gc();
  }

  // 按下Back按键时关闭flash播放
  @Override
  public void onBackPressed() {
    flashWebView.destroy();
    this.finish();
    System.gc();
    super.onBackPressed();
  }

  // 后台运行
  @Override
  protected void onUserLeaveHint() {
    flashWebView.destroy();
    this.finish();
    System.gc();
    super.onUserLeaveHint();
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

  // 安装Adobe Flash APK
  @SuppressLint("JavascriptInterface")
  private void installadobeapk() {
    flashWebView.loadUrl("file:///android_asset/go_market.html");
    flashWebView.addJavascriptInterface(new AndroidBridge(), "android");
  }

  public class AndroidBridge {
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
