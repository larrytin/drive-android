package com.goodow.drive.android.settings;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.activity.NotificationActivity;
import com.goodow.drive.android.toolutils.DeviceInformationTools;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

import java.io.File;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class SettingsRegistry {
  public static final String PREFIX = BusProvider.SID + "settings.";
  private final Context ctx;
  private final Bus bus;

  public SettingsRegistry(Bus bus, Context ctx) {
    this.bus = bus;
    this.ctx = ctx;
  }

  public void subscribe() {
    bus.registerHandler(BusProvider.SID + "audio", new MessageHandler<JsonObject>() {
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
          message.reply(msg);
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
    bus.registerHandler(PREFIX + "location.baidu", new MessageHandler<JsonObject>() {
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
                msg.set("errocode", location.getLocType());
                msg.set("latitude", location.getLatitude());
                msg.set("longtitude", location.getLongitude());
                msg.set("radius", location.getRadius());
                if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                  msg.set("address", location.getAddrStr());
                }
                message.reply(msg);
              }

              @Override
              public void onReceivePoi(BDLocation poiLocation) {
              }
            });
      }
    });
    bus.registerHandler(PREFIX + "location", new MessageHandler<JsonObject>() {
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
        message.reply(msg);
      }
    });
    bus.registerHandler(PREFIX + "information", new MessageHandler<JsonObject>() {
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
        message.reply(msg);
      }
    });

    bus.registerHandler(PREFIX + "brightness.view", new MessageHandler<JsonObject>() {
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
              mView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                  bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness",
                      1), null);
                }
              });
            }
            mWindowManager.updateViewLayout(mView, mLayoutParams);
          } else {
            Log.d(PREFIX, "removeview");
            mWindowManager.removeView(mView);
            mView = null;
          }
        }
      }
    });

    bus.registerHandler(BusProvider.SID + "input", new MessageHandler<JsonObject>() {
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

    bus.registerHandler(BusProvider.SID + "notification", new MessageHandler<JsonObject>() {
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

    bus.registerHandler(BusProvider.SID + "print", new MessageHandler<JsonObject>() {
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
  }
}
