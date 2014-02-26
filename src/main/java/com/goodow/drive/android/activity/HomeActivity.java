package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.data.DataRegistry;
import com.goodow.drive.android.player.PlayerRegistry;
import com.goodow.drive.android.settings.NetWorkListener;
import com.goodow.drive.android.settings.SettingsRegistry;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.State;
import com.goodow.realtime.channel.impl.ReconnectBusClient;
import com.goodow.realtime.core.Handler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.core.Platform;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class HomeActivity extends BaseActivity {
  private static boolean registried;
  private HandlerRegistration openHandlerReg;
  private final ReconnectBusClient connectBus = BusProvider.getConnectBus();
  private HandlerRegistration netWorkHandlerReg;
  private int schedulePeriodic;

  // 联网状态为1
  private int flag = 0;
  // 记录注册OnOpen状态
  private boolean registeredOnOpen = false;
  // 记录注册网络监听状态
  private boolean registeredNetWork = false;

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
        shutdown.set("shutdown", 0);
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
    subscribe();
    sendAnalyticsMessage();
    // 每隔1天,发送一次数据到服务器
    schedulePeriodic =
        Platform.scheduler().schedulePeriodic(24 * 60 * 60 * 1000, new Handler<Void>() {
          @Override
          public void handle(Void event) {
            sendAnalyticsMessage();
          }
        });
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    netWorkHandlerReg.unregisterHandler();
    Platform.scheduler().cancelTimer(schedulePeriodic);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (registeredNetWork) {
      return;
    }
    registeredNetWork = true;
    // 监听网络变化
    netWorkHandlerReg = bus.registerHandler(NetWorkListener.ADDR, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString("action");
        if (action != null && !"post".equalsIgnoreCase(action)) {
          return;
        }
        float netStrength = (float) body.getNumber("strength");
        if (netStrength <= 0.0f) {
          // 无网络
          flag = -1;
          // 由无网络变为有网络(此处不分3G,WIFI)
        } else if (flag == -1) {
          // 重连
          connectBus.reconnect();
          flag = 0;
        }
      }
    });
  }

  /**
   * 打开子级页面
   * 
   * @param type
   */
  private void open(String type) {
    JsonObject msg = Json.createObject();
    msg.set("action", "post");
    JsonArray tags = Json.createArray().push(type);
    msg.set(Constant.KEY_TAGS, tags);
    this.bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, null);
  }

  private void sendAnalyticsMessage() {
    if (State.OPEN == connectBus.getReadyState()) {
      // 请求将播放信息统计发送到服务器
      connectBus.send(Bus.LOCAL + Constant.ADDR_PLAYER + ".analytics.request", null, null);
    } else {
      Log.w("EventBus Status", connectBus.getReadyState().name());
      connectBus.reconnect();
      // 记录注册状态，如果已注册，不应重复注册
      if (registeredOnOpen) {
        return;
      }
      registeredOnOpen = true;// 注册
      // 监听网络状况
      openHandlerReg =
          connectBus.registerHandler(Bus.LOCAL_ON_OPEN, new MessageHandler<JsonObject>() {
            @Override
            public void handle(Message<JsonObject> message) {
              connectBus.send(Bus.LOCAL + Constant.ADDR_PLAYER + ".analytics.request", null, null);
              registeredOnOpen = false;
              openHandlerReg.unregisterHandler();
            }
          });
    }
  }

  private void subscribe() {
    if (!registried) {
      registried = true;
      new ViewRegistry(bus, this).subscribe();
      new PlayerRegistry(bus, this).subscribe();
      new SettingsRegistry(bus, this).subscribe();
      new DataRegistry(bus, this).subscribe();
    }
  }
}
