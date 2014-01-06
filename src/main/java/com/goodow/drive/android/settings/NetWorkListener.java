package com.goodow.drive.android.settings;

import com.goodow.drive.android.BusProvider;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class NetWorkListener {

  public static final String WIFI = "wifi";
  // mobile
  public static final String MOBILE = "mobile";
  // 2G network types
  public static final String GSM = "gsm";
  public static final String GPRS = "gprs";
  public static final String EDGE = "edge";
  // 3G network types
  public static final String CDMA = "cdma";
  public static final String UMTS = "umts";
  public static final String HSPA = "hspa";
  public static final String HSUPA = "hsupa";
  public static final String HSDPA = "hsdpa";
  public static final String ONEXRTT = "1xrtt";
  public static final String EHRPD = "ehrpd";
  // 4G network types
  public static final String LTE = "lte";
  public static final String UMB = "umb";
  public static final String HSPA_PLUS = "hspa+";
  // return type
  public static final String TYPE_UNKNOWN = "unknown";
  public static final String TYPE_WIFI = "wifi";
  public static final String TYPE_2G = "cell_2g";
  public static final String TYPE_3G = "cell_3g";
  public static final String TYPE_4G = "cell_4g";
  public static final String TYPE_NONE = "none";

  private float MAX_3G_STRENGTH = 31;

  /**
   * 监听3G信号强度的变化
   */
  private class MyPhoneStateListener extends PhoneStateListener {
    int strength = 0;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
      super.onSignalStrengthsChanged(signalStrength);
      if (signalStrength.isGsm()) {
        if (signalStrength.getGsmSignalStrength() != 99) {
          strength = signalStrength.getGsmSignalStrength();
        }
      } else {
        signalStrength.getCdmaDbm();
      }
      g3Strength = strength / MAX_3G_STRENGTH;

      JsonObject info =
          Json.createObject().set("action", "post").set("type", getType()).set("strength",
              getStrength());
      bus.send(Bus.LOCAL + ADDR, info, null);
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
      JsonObject info =
          Json.createObject().set("action", "post").set("type", getType()).set("strength",
              getStrength());
      message.reply(info);
    }
  };

  private Context context = null;
  private TelephonyManager tel = null;
  private MyPhoneStateListener myListener = null;
  private float g3Strength = 0;

  /**
   * 监听服务
   */
  private final BroadcastReceiver netWorkStatusReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      JsonObject info =
          Json.createObject().set("action", "post").set("type", getType()).set("strength",
              getStrength());
      bus.send(Bus.LOCAL + ADDR, info, null);
    }
  };

  public NetWorkListener(Context context) {
    this.context = context;
  }

  // 注册监听器
  public void registerReceiver() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);

    this.tel = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    this.myListener = new MyPhoneStateListener();
    this.tel.listen(myListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    this.context.registerReceiver(netWorkStatusReceiver, intentFilter);
    this.bus.registerHandler(ADDR, eventHandler);
  }

  // 解除监听器
  public void unRegisterReceiver() {
    this.context.unregisterReceiver(netWorkStatusReceiver);
    this.bus.unregisterHandler(ADDR, eventHandler);
  }

  /**
   * 获取信号类型
   * 
   * @param info
   * @return
   */
  private String getType() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = connectivityManager.getActiveNetworkInfo();
    if (info != null) {
      String type = info.getTypeName();
      if (type.equalsIgnoreCase(WIFI)) {
        return TYPE_WIFI;
      } else if (type.equalsIgnoreCase(MOBILE)) {
        type = info.getSubtypeName();
        if (type.equalsIgnoreCase(GSM) || type.equalsIgnoreCase(GPRS)
            || type.equalsIgnoreCase(EDGE)) {
          return TYPE_2G;
        } else if (type.toLowerCase().startsWith(CDMA) || type.toLowerCase().equals(UMTS)
            || type.toLowerCase().equals(ONEXRTT) || type.toLowerCase().equals(EHRPD)
            || type.toLowerCase().equals(HSUPA) || type.toLowerCase().equals(HSDPA)
            || type.toLowerCase().equals(HSPA)) {
          return TYPE_3G;
        } else if (type.toLowerCase().equals(LTE) || type.toLowerCase().equals(UMB)
            || type.toLowerCase().equals(HSPA_PLUS)) {
          return TYPE_4G;
        }
      }
    } else {
      return TYPE_NONE;
    }
    return TYPE_UNKNOWN;
  }

  /**
   * 获取信号强度
   */
  private float getStrength() {
    String type = getType();
    if (WIFI.equalsIgnoreCase(type)) {
      return getWifiStrength();
    } else if (TYPE_2G.equals(type) || TYPE_3G.equals(type) || TYPE_4G.equals(type)) {
      return g3Strength;
    } else {
      return 0.0f;
    }
  }

  /**
   * 获取wifi信号强度
   * 
   * @return
   */
  private float getWifiStrength() {
    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    int rssi = wifiManager.getConnectionInfo().getRssi();
    int level = WifiManager.calculateSignalLevel(rssi, 10);
    float percentage = (float) (level / 10.0);
    return percentage;
  }
}
