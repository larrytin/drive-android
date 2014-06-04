package com.goodow.drive.android.settings;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.activity.HomeActivity;
import com.goodow.drive.android.activity.NotificationActivity;
import com.goodow.drive.android.data.DBOperator;
import com.goodow.drive.android.toolutils.DeviceInformationTools;
import com.goodow.drive.android.toolutils.SimpleProgressDialog;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.Handler;
import com.goodow.realtime.core.Platform;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.platform.comapi.basestruct.GeoPoint;

import java.io.File;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class SettingsRegistry {
  private final Context ctx;
  private final Bus bus;
  private SharedPreferences authSp = null;

  public SettingsRegistry(Bus bus, Context ctx) {
    this.bus = bus;
    this.ctx = ctx;
    authSp = ctx.getSharedPreferences(HomeActivity.AUTH, Context.MODE_PRIVATE);
  }

  public void subscribe() {
    bus.registerLocalHandler(Constant.ADDR_AUDIO, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString("action");
        AudioManager mAudioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
        if ("get".equalsIgnoreCase(action)) {
          JsonObject msg = Json.createObject();
          boolean mute =
              mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0 ? false : true;
          double volume =
              (double) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                  / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
          if (mute) {
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            volume =
                (double) mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    / mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
          }
          msg.set("mute", mute).set("volume", volume);
          message.reply(msg,null);
          return;
        }
        if (action == null || "post".equalsIgnoreCase(action)) {
          // 静音
          if (body.has("mute")) {
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, body.getBoolean("mute"));
            // if (body.getBoolean("mute")) {
            // mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            // } else {
            // mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            // }
            // 设置音量
          } else if (body.has("volume")) {
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            double volume = body.getNumber("volume");
            mAudioManager
                .setStreamVolume(AudioManager.STREAM_MUSIC, (int) (mAudioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volume),
                    AudioManager.FLAG_SHOW_UI);
            // 设置增幅
          } else if (body.has("range")) {
            mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            double range = body.getNumber("range");
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC) + mAudioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                * range), AudioManager.FLAG_SHOW_UI);
          }
          return;
        }
      }
    });
    bus.registerLocalHandler(Constant.ADDR_SETTINGS_LOCATION_BAIDU,
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(final Message<JsonObject> message) {
            // 请求位置
            BaiduLocation.INSTANCE.getLocationClient().requestLocation();
            BaiduLocation.INSTANCE.getLocationClient().registerLocationListener(
                new BDLocationListener() {
                  @Override
                  public void onReceiveLocation(BDLocation location) {
                    if (location == null) {
                      return;
                    }
                    JsonObject msg = Json.createObject();
                    msg.set("time", location.getTime());
                    msg.set("errorcode", location.getLocType());
                    msg.set("latitude", location.getLatitude());
                    msg.set("longitude", location.getLongitude());
                    msg.set("radius", location.getRadius());
                    if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                      msg.set("address", location.getAddrStr());
                    }
                    message.reply(msg,null);
                  }

                  @Override
                  public void onReceivePoi(BDLocation poiLocation) {
                  }
                });
          }
        });
    bus.registerLocalHandler(Constant.ADDR_SETTINGS_LOCATION, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = Json.createObject();
        ConnectivityManager mConnectivityManager =
            (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
          msg.set("networkType", "wifi");
        } else if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
          msg.set("networkType", "mobile");
          TelephonyManager mTelephonyManager =
              (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
          // 返回值MCC + MNC
          String operator = mTelephonyManager.getNetworkOperator();
          msg.set("mobileCountryCode", operator.substring(0, 3));
          msg.set("mobileNetworkCode", operator.substring(4));
          int MNC = Integer.parseInt(operator.substring(4));
          if (MNC == 0 || MNC == 1) {
            // 中国移动和中国联通获取LAC、CID的方式
            GsmCellLocation gsmLocation = (GsmCellLocation) mTelephonyManager.getCellLocation();
            msg.set("cellId", gsmLocation.getCid());
            msg.set("locationAreaCode", gsmLocation.getLac());
            msg.set("PSC", gsmLocation.getPsc());
          } else if (MNC == 3) {
            CdmaCellLocation cdmaLocation = (CdmaCellLocation) mTelephonyManager.getCellLocation();
            msg.set("BID", cdmaLocation.getBaseStationId());
            msg.set("NID", cdmaLocation.getNetworkId());
            msg.set("SID", cdmaLocation.getSystemId());
            msg.set("Latitude", cdmaLocation.getBaseStationLatitude());
            msg.set("Longitude", cdmaLocation.getBaseStationLongitude());
          }
        }
        message.reply(msg,null);
      }
    });
    bus.registerLocalHandler(Constant.ADDR_SETTINGS_INFORMATION, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = Json.createObject();
        JsonObject hardwareMsg = Json.createObject();
        JsonObject softwareMsg = Json.createObject();
        // Hardware

        msg.set("hardware", hardwareMsg);
        hardwareMsg.set("MAC", DeviceInformationTools.getLocalMacAddressFromWifiInfo(ctx));
        hardwareMsg.set("IMEI", DeviceInformationTools.getIMEI(ctx));
        hardwareMsg.set("SCREENHEIGH", DeviceInformationTools.getScreenHeight(ctx));
        hardwareMsg.set("SCREENWIDTH", DeviceInformationTools.getScreenWidth(ctx));
        // Software
        msg.set("software", softwareMsg);
        softwareMsg.set("AndroidId", DeviceInformationTools.getAndroidId(ctx));
        softwareMsg.set("IP", DeviceInformationTools.getIp(ctx));
        softwareMsg.set("Model", DeviceInformationTools.getOsModel());
        softwareMsg.set("Version", DeviceInformationTools.getOsVersion());
        softwareMsg.set("SDK", DeviceInformationTools.getSDK());
        message.reply(msg,null);
      }
    });

    bus.registerLocalHandler(Constant.ADDR_SETTINGS_BRIGHTNESS_VIEW,
        new MessageHandler<JsonObject>() {
          private LayoutParams mLayoutParams;
          private View mView;
          private final WindowManager mWindowManager = (WindowManager) ctx.getApplicationContext()
              .getSystemService(Context.WINDOW_SERVICE);

          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject msg = message.body();
            if (msg.has("brightness")) {
              if (mView == null) {
                mView = new View(ctx);
                mView.setBackgroundColor(Color.BLACK);
                mLayoutParams = new LayoutParams();
                // 位图格式,系统选择一个at least 1 alpha bit
                mLayoutParams.format = PixelFormat.TRANSPARENT;
                // 系统顶层窗口,显示在其他一切内容之上
                mLayoutParams.type = LayoutParams.TYPE_SYSTEM_OVERLAY;
                // 窗口占满整个屏幕，忽略周围的装饰边框
                mLayoutParams.flags |= LayoutParams.FLAG_LAYOUT_IN_SCREEN;
                mWindowManager.addView(mView, mLayoutParams);
              }
              float strength = (float) msg.getNumber("brightness");
              // 改变窗体透明度,0完全透明,1完全不透明
              mLayoutParams.alpha = 1.0f - strength;
              // strength=0,既alpha=1全黑,strength=1,既alpha=0还原
              if (mLayoutParams.alpha != 0) {
                if (strength == 0) {
                  mWindowManager.removeView(mView);
                  mLayoutParams.type = LayoutParams.TYPE_PRIORITY_PHONE;
                  mWindowManager.addView(mView, mLayoutParams);
                  mView.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                      if (MotionEvent.ACTION_DOWN == event.getAction()) {
                        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("brightness",
                            1), null);
                      }
                      return true;
                    }
                  });
                }
                mWindowManager.updateViewLayout(mView, mLayoutParams);
              } else {
                mWindowManager.removeView(mView);
                mView = null;
              }
            }
          }
        });

    bus.registerLocalHandler(Constant.ADDR_INPUT, new MessageHandler<JsonObject>() {
      private final Instrumentation inst = new Instrumentation();

      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = message.body();
        if (msg.has("key")) {
          int key = (int) msg.getNumber("key");
          // left:21 right:22 down:20 up:19 center:23
          sendInputKeyEvent(key);
        }
      }

      private void sendInputKeyEvent(final int number) {
        new Thread() {
          @Override
          public void run() {
            inst.sendKeyDownUpSync(number);
          };
        }.start();
      }
    });

    bus.registerLocalHandler(Constant.ADDR_NOTIFICATION, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = message.body();
        if (msg.has("content")) {
          Intent intent = new Intent(ctx, NotificationActivity.class);
          intent.putExtra("msg", msg);
          ctx.startActivity(intent);
        }
      }
    });

    bus.registerLocalHandler(Constant.ADDR_PRINT, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = message.body();
        if (!msg.has("path")) {
          return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setComponent(new ComponentName("com.dynamixsoftware.printhand",
            "com.dynamixsoftware.printhand.ui.ActivityPreviewImages"));
        intent.setType("image/*");
        File file = new File(msg.getString("path"));
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
        ctx.startActivity(intent);
      }
    });

    // 由服务器来处理验证信息
    bus.registerLocalHandler(Constant.ADDR_AUTH_REQUEST, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        final JsonObject messageBody = message.body();
        // 百度地图
        bus.sendLocal(Constant.ADDR_SETTINGS_LOCATION_BAIDU, null,
            new MessageHandler<JsonObject>() {
              @Override
              public void handle(Message<JsonObject> message) {
                JsonObject msg = message.body();
                // 发送的验证消息
                final JsonObject send = Json.createObject();
                send.set("sid", DeviceInformationTools.getLocalMacAddressFromWifiInfo(ctx));
                send.set("imei", DeviceInformationTools.getIMEI(ctx));
                send.set("errorcode", msg.getNumber("errorcode"));
                // 定位成功
                if (msg.getNumber("errorcode") == 161.0) {
                  double latitude = msg.getNumber("latitude");
                  double longitude = msg.getNumber("longitude");
                  send.set("latitude", latitude);
                  send.set("longitude", longitude);
                  send.set("radius", msg.getNumber("radius"));
                  send.set("address", msg.getString("address"));
                  // 临时的存储latitude,longitude
                  authSp.edit().putFloat("latitudetmp", (float) latitude).putFloat("longitudetmp",
                      (float) longitude).putFloat("radius", (float) msg.getNumber("radius"))
                      .putString("address", msg.getString("address")).commit();
                  if (authSp.getBoolean("register", false) && authSp.getFloat("latitude", -1) != -1
                      && authSp.getFloat("longitude", -1) != -1) {
                    // GeoPoint(int latitudeE6, int longitudeE6)
                    // latitudeE6 - 纬度坐标，单位是微度
                    // longitudeE6 - 经度坐标，单位是微度
                    GeoPoint p1LL =
                        new GeoPoint((int) (authSp.getFloat("latitude", -1) * 1E6), (int) (authSp
                            .getFloat("longitude", -1) * 1E6));
                    GeoPoint p2LL = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
                    double distance = DistanceUtil.getDistance(p1LL, p2LL);// 單位：米
                    send.set("distance", distance);
                  } else if (!authSp.getBoolean("register", false)
                      && authSp.getFloat("latitude1", -1) != -1
                      && authSp.getFloat("longitude1", -1) != -1) {
                    GeoPoint p1LL =
                        new GeoPoint((int) (authSp.getFloat("latitude1", -1) * 1E6), (int) (authSp
                            .getFloat("longitude1", -1) * 1E6));
                    GeoPoint p2LL = new GeoPoint((int) (latitude * 1E6), (int) (longitude * 1E6));
                    double distance = DistanceUtil.getDistance(p1LL, p2LL);// 單位：米
                    send.set("distance", distance);
                  }
                }
                if (messageBody.has("schoolName")) {
                  send.set("schoolName", messageBody.getString("schoolName"));
                  send.set("contacts", messageBody.getString("contacts"));
                }
                authInformation(send);
              }
            });
      }

      private void authInformation(final JsonObject send) {
        bus.send(Constant.ADDR_SERVER_AUTH, send, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject msg = message.body();
            if (!(msg.has("status") || msg.has("content") || msg.has("lock") || msg.has("reset"))) {
              return;
            }
            double status = msg.getNumber("status");
            boolean reset = msg.getBoolean("reset");
            boolean lock = msg.getBoolean("lock");
            // 校验通过
            if (status == 0.0) {
              if (!authSp.getBoolean("register", false)) { // 第一次注册时，存储地理位置
                Editor mEditor = authSp.edit();
                mEditor.putFloat("latitude", authSp.getFloat("latitudetmp", -1));
                mEditor.putFloat("longitude", authSp.getFloat("longitudetmp", -1));
                mEditor.putBoolean("register", true);
                mEditor.commit();
                Toast.makeText(ctx, R.string.string_register_success, Toast.LENGTH_LONG).show();// 提示注册成功
                authSp.edit().putBoolean("limit", false).putBoolean("normal", true).putLong(
                    "limitTime", 0l).commit();
                Platform.scheduler().cancelTimer(HomeActivity.netConnectRegister); // 取消注册
              } else {
                authSp.edit().putBoolean("limit", false).putBoolean("normal", true).putLong(
                    "limitTime", 0l).commit();
                bus.sendLocal(Constant.ADDR_VIEW_PROMPT, Json.createObject().set("status", false),
                    null);
                Platform.scheduler().cancelTimer(HomeActivity.updateLimit); // 取消限制
                Platform.scheduler().cancelTimer(HomeActivity.autoShutDown); // 取消自动关机
                Platform.scheduler().cancelTimer(HomeActivity.netConnectCheck);// 取消校验对话框
                Platform.scheduler().cancelTimer(HomeActivity.mLimiteShutDown);// 取消限制使用关机
              }
              // 对话框消失
              SimpleProgressDialog.dismiss(ctx);
              authSp.edit().putInt("FailTime", 0).commit(); // 计数器清0
              DBOperator.updateBootAddress(ctx, "T_BOOT", "LATITUDE", "LONGITUDE", "RADIUS",
                  "ADDRESS", Json.createObject()
                      .set("LATITUDE", authSp.getFloat("latitudetmp", -1)).set("LONGITUDE",
                          authSp.getFloat("longitudetmp", -1)).set("RADIUS",
                          authSp.getFloat("radius", -1)).set("ADDRESS",
                          authSp.getString("address", "")));
              // 发送数据行为数据
              bus.sendLocal(Constant.ADDR_SYSTIME_ANALYTICS_REQUEST, null, null);
            } else if (status == 1.0) { // 校验
              if (!authSp.getBoolean("register", false)) {
                Editor mEditor = authSp.edit();
                mEditor.putFloat("latitude1", authSp.getFloat("latitudetmp", -1));
                mEditor.putFloat("longitude1", authSp.getFloat("longitudetmp", -1));
                mEditor.commit();
              }
              authSp.edit().putInt("FailTime", authSp.getInt("FailTime", 0) + 1).commit();
              // 超过三次后，不再发送,校验失败
              if (authSp.getInt("FailTime", 0) < 4) {
                // 重新发送数据
                bus.sendLocal(Constant.ADDR_AUTH_REQUEST, Json.createObject(), null);
              } else {
                bus.sendLocal(Constant.ADDR_NOTIFICATION, Json.createObject().set("content",
                    "三分钟后关机"), null);
                // 校验失败
                Platform.scheduler().scheduleDelay(30 * 60 * 1000, new Handler<Void>() {
                  @Override
                  public void handle(Void event) {
                    // 关机
                    bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("shutdown", 0),
                        null);
                  }
                });
              }
            } else if (status == 2.0) { // 注册
              // 清空缓存
              authSp.edit().remove("latitude1").remove("longitude1").commit();
              // 重新发送数据
              bus.sendLocal(Constant.ADDR_AUTH_REQUEST, Json.createObject(), null);
            }
            // 重置
            authSp.edit().putBoolean("reset", reset).commit();
            // 锁定
            authSp.edit().putBoolean("lock", lock).commit();
            if (msg.has("content")) {
              bus.sendLocal(Constant.ADDR_NOTIFICATION, Json.createObject().set("content",
                  msg.getString("content")), null);
            }
          }
        });
      }
    });
  }
}
