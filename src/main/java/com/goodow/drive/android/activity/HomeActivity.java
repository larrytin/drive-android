package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import android.os.Bundle;
import android.view.View;

public class HomeActivity extends BaseActivity {
  public void onClick(View v) {
    switch (v.getId()) {
    // 收藏
      case R.id.iv_act_main_coll:
        this.open("收藏");
        break;
      // 锁屏
      case R.id.iv_act_main_loc:
        JsonObject brightness = Json.createObject();
        brightness.set("brightness", 0);
        this.bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, brightness, null);
        break;
      // 设置
      case R.id.iv_act_main_set:
        this.bus.send(Bus.LOCAL + Constant.ADDR_PREFIX_VIEW + "settings", null, null);
        break;
      // 关机
      case R.id.iv_act_main_clo:
        JsonObject shutdown = Json.createObject();
        shutdown.set("shutdown", true);
        this.bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, shutdown, null);
        break;
      // 和谐
      case R.id.iv_act_main_har:
        this.open("和谐");
        break;
      // 托班
      case R.id.iv_act_main_ship:
        this.open("托班");
        break;
      // 示范课
      case R.id.iv_act_main_tech:
        this.open("示范课");
        break;
      // 入学准备
      case R.id.iv_act_main_pre:
        this.open("入学准备");
        break;
      // 智能开发
      case R.id.iv_act_main_smart:
        this.open("智能开发");
        break;
      // 图画书
      case R.id.iv_act_main_ebk:
        this.open("电子书");
        break;

      default:
        break;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_home);
  }

  /**
   * 打开子级页面
   * 
   * @param type
   */
  private void open(String type) {
    JsonObject msg = Json.createObject();
    msg.set("action", "post");
    JsonObject tags = Json.createObject();
    tags.set("type", type);
    msg.set("query", tags);
    this.bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, null);
  }

}
