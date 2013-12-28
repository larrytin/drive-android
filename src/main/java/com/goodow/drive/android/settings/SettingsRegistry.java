package com.goodow.drive.android.settings;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.toolutils.DeviceInformationTools;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class SettingsRegistry {
  private final static String TAG = SettingsRegistry.class.getSimpleName();
  public static final String PREFIX = BusProvider.SID + "settings.";
  private final Bus bus = BusProvider.get();
  private final Context ctx;

  public SettingsRegistry(Context ctx) {
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
        if ("post".equalsIgnoreCase(action)) {
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
            Log.d(TAG, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + "");
          }
          return;
        }
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
          msg.set("NetWorkType", networkInfo.getTypeName());
        } else if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
          msg.set("NetWorkType", networkInfo.getTypeName());
          TelephonyManager mTelephonyManager =
              (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
          // 返回值MCC + MNC
          String operator = mTelephonyManager.getNetworkOperator();
          msg.set("MCC+MNC", operator);
          int MCC = Integer.parseInt(operator.substring(0, 3));
          int MNC = Integer.parseInt(operator.substring(3));
          if (MNC == 0 || MCC == 1) {
            // 中国移动和中国联通获取LAC、CID的方式
            GsmCellLocation gsmLocation = (GsmCellLocation) mTelephonyManager.getCellLocation();
            msg.set("CID", gsmLocation.getCid());
            msg.set("LAC", gsmLocation.getLac());
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
    // 重启
    bus.registerHandler(PREFIX + "reboot", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Toast.makeText(ctx, "重启", Toast.LENGTH_LONG).show();
      }
    });
    bus.registerHandler(PREFIX + "brightness.view", new MessageHandler<JsonObject>() {
      private LayoutParams mLayoutParams;
      private View mView;
      private final WindowManager mWindowManager = (WindowManager) ctx.getApplicationContext()
          .getSystemService(Context.WINDOW_SERVICE);

      @Override
      public void handle(Message<JsonObject> message) {
        Log.d(TAG, "brightness");
        JsonObject msg = message.body();
        if (msg.has("brightness")) {
          if (mView == null) {
            Log.d(TAG, "addview");
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
          Log.d(TAG, mLayoutParams.alpha + "");
          if (mLayoutParams.alpha != 0) {
            Log.d(TAG, "setlayout");
            mWindowManager.updateViewLayout(mView, mLayoutParams);
          } else {
            Log.d(PREFIX, "removeview");
            mWindowManager.removeView(mView);
            mView = null;
          }
        }
      }
    });

  }
}
