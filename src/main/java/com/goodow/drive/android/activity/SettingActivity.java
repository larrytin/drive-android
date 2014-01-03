package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class SettingActivity extends BaseActivity implements OnClickListener {
  private Button bt_setting_about;
  private Button bt_setting_reboot;
  private Button bt_setting_resolution;
  private Button bt_setting_screen_offset;
  private Button bt_setting_wifi;
  private ImageView iv_common_back;

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.bt_setting_wifi:// wifi设置
        bus.send(Bus.LOCAL + Constant.ADDR_PREFIX_VIEW + "wifi", null, null);
        break;
      case R.id.bt_setting_resolution:// 分辨率输出
        bus.send(Bus.LOCAL + Constant.ADDR_PREFIX_VIEW + "resolution", null, null);
        break;
      case R.id.bt_setting_screen_offset:// 屏幕偏移
        bus.send(Bus.LOCAL + Constant.ADDR_PREFIX_VIEW + "screenOffset", null, null);
        break;
      case R.id.bt_setting_about:// 关于我们
        bus.send(Bus.LOCAL + Constant.ADDR_PREFIX_VIEW + "aboutUs", null, null);
        break;
      case R.id.bt_setting_reboot:// 重启
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("shutdown", 1), null);
        break;
      case R.id.iv_common_back://
        JsonObject msg = Json.createObject();
        msg.set("return", true);
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, msg, null);
        break;

    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.setting_activity);
    initView();
    setListener();
  }

  private void initView() {
    bt_setting_about = (Button) findViewById(R.id.bt_setting_about);
    bt_setting_reboot = (Button) findViewById(R.id.bt_setting_reboot);
    bt_setting_resolution = (Button) findViewById(R.id.bt_setting_resolution);
    bt_setting_screen_offset = (Button) findViewById(R.id.bt_setting_screen_offset);
    bt_setting_wifi = (Button) findViewById(R.id.bt_setting_wifi);
    iv_common_back = (ImageView) findViewById(R.id.iv_common_back);
  }

  private void setListener() {
    bt_setting_about.setOnClickListener(this);
    bt_setting_reboot.setOnClickListener(this);
    bt_setting_resolution.setOnClickListener(this);
    bt_setting_screen_offset.setOnClickListener(this);
    bt_setting_wifi.setOnClickListener(this);
    iv_common_back.setOnClickListener(this);
  }
}
