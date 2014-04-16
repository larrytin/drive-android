package com.goodow.drive.android.activity;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.data.DBOperator;
import com.goodow.drive.android.settings.SettingsRegistry;
import com.goodow.drive.android.toolutils.DeviceInformationTools;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.State;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

/**
 * @title: BaseActivity.java
 * @package drive-android
 * @description: TODO
 * @author www.dingpengwei@gmail.com
 * @createDate 2013 2013-12-4 上午11:33:07
 * @updateDate 2013 2013-12-4 上午11:33:07
 * @version V1.0
 */
public class BaseActivity extends Activity {
  private static final String BRIGHTNESS = SettingsRegistry.PREFIX + "brightness.light";
  protected final Bus bus = BusProvider.get();

  private HandlerRegistration controlHandler;
  private HandlerRegistration brightnessHandler;
  public SharedPreferences usagePreferences;
  public static final String USAGE_STATISTIC = "USAGE_STATISTIC";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    BusProvider.SID = DeviceInformationTools.getLocalMacAddressFromWifiInfo(this) + ".drive.";
    usagePreferences = getSharedPreferences(USAGE_STATISTIC, Context.MODE_PRIVATE);
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onPause() {
    super.onPause();
    // Always unregister when an handler no longer should be on the bus.
    controlHandler.unregisterHandler();
    brightnessHandler.unregisterHandler();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (bus.getReadyState() == State.CLOSED || bus.getReadyState() == State.CLOSING) {
      Log.w("EventBus Status", bus.getReadyState().name());
      BusProvider.reconnect();
    }
    controlHandler = bus.registerHandler(Constant.ADDR_CONTROL, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = message.body();
        if (msg.has("return")) {
          finish();
          // 屏幕亮度
        } else if (msg.has("brightness")) {
          bus.send(Bus.LOCAL + SettingsRegistry.PREFIX + "brightness.view", msg, null);
        } else if (msg.has("shutdown")) {
          // // 方案一:应用需要装在system/app下
          // if (msg.getNumber("shutdown") == 0) {
          // try {
          // // 获得ServiceManager类
          // Class<?> ServiceManager = Class.forName("android.os.ServiceManager");
          // // 获得ServiceManager的getService方法
          // Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
          // // 调用getService获取RemoteService
          // Object oRemoteService = getService.invoke(null, Context.POWER_SERVICE);
          // // 获得IPowerManager.Stub类
          // Class<?> cStub = Class.forName("android.os.IPowerManager$Stub");
          // // 获得asInterface方法
          // Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
          // // 调用asInterface方法获取IPowerManager对象
          // Object oIPowerManager = asInterface.invoke(null, oRemoteService);
          // // 获得shutdown()方法
          // Method shutdown =
          // oIPowerManager.getClass().getMethod("shutdown", boolean.class, boolean.class);
          // // 调用shutdown()方法
          // shutdown.invoke(oIPowerManager, false, true);
          // } catch (Exception e) {
          // e.printStackTrace();
          // }
          // } else if (msg.getNumber("shutdown") == 1) {
          // PowerManager pm =
          // (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
          // pm.reboot(null);
          // }
          // 方案二:应用需要申请root权限,有一定延迟时间
          if (msg.getNumber("shutdown") == 0) {
            ProcessBuilder pb = new ProcessBuilder("/system/bin/sh");
            pb.redirectErrorStream(true);
            try {
              Process process = pb.start();
              PrintWriter pw =
                  new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
              pw.println("su");
              Thread.sleep(300);
              pw.println("reboot -p");
              pw.println("exit");
              pw.close();
              process.destroy();
            } catch (Exception e) {
              e.printStackTrace();
            }
          } else if (msg.getNumber("shutdown") == 1) {
            ProcessBuilder pb = new ProcessBuilder("/system/bin/sh");
            pb.redirectErrorStream(true);
            try {
              Process process = pb.start();
              PrintWriter pw =
                  new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
              pw.println("su");
              Thread.sleep(300);
              pw.println("reboot");
              pw.println("exit");
              pw.close();
              process.destroy();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        }
      }
    });
    brightnessHandler = bus.registerHandler(BRIGHTNESS, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = message.body();
        if (msg.has("brightness")) {
          double strength = msg.getNumber("brightness");
          // 调节亮度,此方法只对平板，手机有效
          WindowManager.LayoutParams lp = getWindow().getAttributes();
          if ((0 < strength | strength == 0) && strength <= 1) {
            android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, (int) strength * 255); // 0-255
            lp.screenBrightness = (float) strength;
          }
          getWindow().setAttributes(lp);
        }
      }
    });
  }

  protected void saveOnDatabases() {
    String fileName = usagePreferences.getString("tmpFileName", "");
    long openTime = usagePreferences.getLong("tmpOpenTime", -1);
    // 播放时间，小于5s，忽略
    long lastTime = SystemClock.uptimeMillis() - usagePreferences.getLong("tmpSystemLast", -1);
    if (lastTime > 5000 & !TextUtils.isEmpty(fileName)) {
      // 将播放数据存储到数据库
      DBOperator.addUserData(this, "T_PLAYER", "FILE_NAME", "OPEN_TIME", "LAST_TIME", Json
          .createObject().set("FILE_NAME", fileName).set("OPEN_TIME", openTime).set("LAST_TIME",
              lastTime));
    }
  }
}
