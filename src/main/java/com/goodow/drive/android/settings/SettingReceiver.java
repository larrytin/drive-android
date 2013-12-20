package com.goodow.drive.android.settings;

import com.goodow.drive.android.BusProvider;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class SettingReceiver {

  /**
   * 监听3G信号强度的变化
   */
  private class MyPhoneStateListener extends PhoneStateListener {
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
      super.onSignalStrengthsChanged(signalStrength);
      int strength = signalStrength.getGsmSignalStrength();
      if (strength <= 0) {
        g3Strength = 0.0f;
      } else if (strength > 0 && strength <= 10) {
        g3Strength = 0.3f;
      } else if (strength > 10 && strength < 31) {
        g3Strength = 1.0f;
      } else {
        g3Strength = 0.0f;
      }
    }
  }

  private final Bus bus = BusProvider.get();
  // 信息服务地址
  public static final String ADDR = BusProvider.SID + "connectivity";
  private final MessageHandler<JsonObject> eventHandler = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      String action = message.body().getString("action");
      if (!"get".equalsIgnoreCase(action)) {
        return;
      }

      // 信息服务反馈
      JsonObject wifi = Json.createObject().set("strength", getWifiStrength());
      JsonObject _3g = Json.createObject().set("strength", g3Strength);
      JsonObject info = Json.createObject().set("wifi", wifi).set("3g", _3g);

      info.set("time", currentTime);
      message.reply(info);
    }
  };

  private Context context = null;
  private TelephonyManager tel = null;
  private MyPhoneStateListener myListener = null;
  private String currentTime = "";
  private float g3Strength = 0;

  /**
   * 监听服务
   */
  private final BroadcastReceiver netWorkStatusReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      // 分钟变化
      if (action.equals(Intent.ACTION_TIME_TICK)) {
        currentTime = getSystemTime();
      }

      JsonObject wifi = Json.createObject().set("strength", getWifiStrength());
      JsonObject _3g = Json.createObject().set("strength", g3Strength);
      JsonObject info = Json.createObject().set("action", "post").set("wifi", wifi).set("3g", _3g);

      info.set("time", currentTime);
      bus.send(Bus.LOCAL + ADDR, info, null);
    }
  };

  public SettingReceiver(Context context) {
    this.context = context;
  }

  // 注册监听器
  public void registerReceiver() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
    intentFilter.addAction(Intent.ACTION_TIME_TICK);

    this.tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    this.myListener = new MyPhoneStateListener();
    this.tel.listen(myListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    this.currentTime = this.getSystemTime();

    this.context.registerReceiver(netWorkStatusReceiver, intentFilter);
    this.bus.registerHandler(ADDR, eventHandler);
  }

  // 解除监听器
  public void unRegisterReceiver() {
    this.context.unregisterReceiver(netWorkStatusReceiver);
    this.bus.unregisterHandler(ADDR, eventHandler);
  }

  /**
   * 获取当前时间
   * 
   * @return
   */
  private String getSystemTime() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat date = new SimpleDateFormat("MM月dd日", Locale.CHINA);
    SimpleDateFormat time = new SimpleDateFormat("hh:mm", Locale.CHINA);
    GregorianCalendar cal = new GregorianCalendar();
    String result =
        date.format(calendar.getTime()) + " "
            + (cal.get(GregorianCalendar.AM_PM) == 0 ? "AM" : "PM")
            + time.format(calendar.getTime());
    return result;
  }

  /**
   * 获取wifi信号强度
   * 
   * @return
   */
  private float getWifiStrength() {
    float strength = 0;
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    WifiInfo info = wifiManager.getConnectionInfo();
    int rssi = info.getRssi();
    if (-65 <= rssi && rssi <= -40) {
      strength = 1.0f;
    } else if (-80 <= rssi && rssi < -65) {
      strength = 0.3f;
    } else {
      strength = 0.0f;
    }
    return strength;
  }
}
