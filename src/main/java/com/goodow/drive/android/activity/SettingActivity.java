package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_setting)
public class SettingActivity extends BaseActivity implements OnClickListener {
  @InjectView(R.id.bt_setting_about)
  private Button bt_setting_about;
  @InjectView(R.id.bt_setting_reboot)
  private Button bt_setting_reboot;
  @InjectView(R.id.bt_setting_screen_offset)
  private Button bt_setting_screen_offset;
  @InjectView(R.id.bt_setting_wifi)
  private Button bt_setting_wifi;
  @InjectView(R.id.bt_setting_reset)
  private Button bt_setting_reset;
  @InjectView(R.id.bt_setting_register)
  private Button bt_setting_register;
  @InjectView(R.id.iv_common_back)
  private ImageView iv_common_back;
  private final long[] mSetting = new long[5];
  private SharedPreferences authSp;
  @InjectView(R.id.iv_hidden)
  private View iv_hidden;

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.iv_hidden:// 隐藏的打开全部设置，左上角连点5次
        System.arraycopy(mSetting, 1, mSetting, 0, mSetting.length - 1);
        mSetting[mSetting.length - 1] = SystemClock.uptimeMillis();
        System.out.println(mSetting[mSetting.length - 1]);
        if (mSetting[0] >= (mSetting[mSetting.length - 1] - 1000)) {
          bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
              "settings.all"), null);
        }
        break;
      case R.id.bt_setting_wifi:// wifi设置
        bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
            "settings.wifi"), null);
        break;
      case R.id.bt_setting_screen_offset:// 屏幕偏移
        bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
            "settings.screenOffset"), null);
        break;
      case R.id.bt_setting_about:// 关于我们
        bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
            "aboutUs"), null);
        break;
      case R.id.bt_setting_reboot:// 重启
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("shutdown", 1), null);
        break;
      case R.id.bt_setting_reset:// 重置
        new AlertDialog.Builder(this).setMessage("您确定重置吗？").setPositiveButton("确定",
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                authSp.edit().clear().commit();
                // // 跳到首页时，弹出注册界面
                // authSp.edit().putBoolean("resetkey", true).commit();
                // bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set("redirectTo",
                // "home"), null);
                // 重启
                bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("shutdown", 1), null);
              }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        }).create().show();
        break;
      case R.id.iv_common_back://
        JsonObject msg = Json.createObject();
        msg.set("return", true);
        bus.sendLocal(Constant.ADDR_CONTROL, msg, null);
        break;
      case R.id.bt_setting_register:
        Intent mIntent = new Intent(this, HomeActivity.class);
        mIntent.putExtra("register", true);
        startActivity(mIntent);
        break;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setListener();
    authSp = getSharedPreferences(HomeActivity.AUTH, Context.MODE_PRIVATE);
    if (authSp.getBoolean("reset", false)) {
      bt_setting_reset.setVisibility(View.VISIBLE);
    }
    if (!authSp.getBoolean("register", false)) {
      bt_setting_register.setVisibility(View.VISIBLE);
    }
  }

  private void setListener() {
    bt_setting_about.setOnClickListener(this);
    bt_setting_reboot.setOnClickListener(this);
    bt_setting_screen_offset.setOnClickListener(this);
    bt_setting_wifi.setOnClickListener(this);
    iv_common_back.setOnClickListener(this);
    bt_setting_reset.setOnClickListener(this);
    bt_setting_register.setOnClickListener(this);
    iv_hidden.setOnClickListener(this);
  }
}
