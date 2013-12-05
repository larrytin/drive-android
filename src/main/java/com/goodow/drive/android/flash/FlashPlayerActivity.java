package com.goodow.drive.android.flash;

import com.goodow.android.drive.R;
import com.goodow.drive.android.toolutils.DebugLog;
import com.goodow.realtime.json.JsonObject;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

public class FlashPlayerActivity extends Activity {
  private final static String TAG = FlashPlayerActivity.class.getSimpleName();
  private FlashView flash;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_flash);
    flash = (FlashView) findViewById(R.id.flash);
    // 检测flash插件是否存在
    if (checkinstallornotadobeflashapk()) {
      Intent intent = getIntent();
      // 得到路径
      JsonObject msg = (JsonObject) getIntent().getExtras().get("msg");
      String path = msg.get("path");
      path = "/mnt/sdcard/" + path;
      DebugLog.i(TAG, path);
      flash.load();
      flash.setFlashPath(path);
      flash.start();
    } else {
      Toast.makeText(this, R.string.prompt_flash_Plugins, Toast.LENGTH_LONG).show();
    }

  }

  @Override
  protected void onDestroy() {
    this.finish();
    System.gc();
    super.onDestroy();
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

}
