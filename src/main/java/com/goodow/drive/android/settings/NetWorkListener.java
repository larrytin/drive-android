package com.goodow.drive.android.settings;

import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.Registration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import com.google.inject.Inject;
import com.google.inject.Provider;

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
      // 当前的3G信号强度
      float currentG3Strength = strength / MAX_3G_STRENGTH;
      // 强度变化大于0.2
      if (Math.abs(currentG3Strength - g3Strength) > 0.2 || TYPE_NONE.equals(NetWorkName)) {
        g3Strength = currentG3Strength;
        JsonObject info =
            Json.createObject().set("action", "post").set(Constant.TYPE, getType()).set("strength",
                getStrength());
        bus.publishLocal(Constant.ADDR_CONNECTIVITY, info);
      }
    }
  }

  @Inject
  Bus bus;

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
  public static final String TYPE_CABLE = "有线网络";

  public static final String TYPE_NONE = "none";

  public String NetWorkName = null;

  private final float MAX_3G_STRENGTH = 31;

  @Inject
  private Provider<Context> context;
  @Inject
  private TelephonyManager tel = null;
  private MyPhoneStateListener myListener = null;
  private float g3Strength = 0;
  private float wifiStrength = 0;

  /**
   * 监听服务
   */
  private final BroadcastReceiver netWorkStatusReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      float currentWifiStrength = getWifiStrength();
      if (Math.abs(currentWifiStrength - wifiStrength) > 0.2 || TYPE_NONE.equals(NetWorkName)
          || getStrength() == 0) {
        wifiStrength = currentWifiStrength;
        JsonObject info =
            Json.createObject().set("action", "post").set(Constant.TYPE, getType()).set("strength",
                getStrength());
        bus.publishLocal(Constant.ADDR_CONNECTIVITY, info);
      }
    }
  };
  private Registration getHander;

  // 注册监听器
  public void registerReceiver() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
    intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
    this.myListener = new MyPhoneStateListener();
    this.tel.listen(myListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

    this.context.get().registerReceiver(netWorkStatusReceiver, intentFilter);
    getHander =
        this.bus.registerLocalHandler(Constant.ADDR_CONNECTIVITY, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            String action = message.body().getString("action");
            if (!"get".equalsIgnoreCase(action)) {
              return;
            }
            // 信息服务反馈
            JsonObject info =
                Json.createObject().set("action", "post").set(Constant.TYPE, getType()).set(
                    "strength", getStrength());
            message.reply(info, null);
          }
        });
  }

  // 解除监听器
  public void unRegisterReceiver() {
    this.context.get().unregisterReceiver(netWorkStatusReceiver);
    getHander.unregister();
  }

  /**
   * 获取信号强度
   */
  private float getStrength() {
    String type = getType();
    if (WIFI.equalsIgnoreCase(type)) {
      return wifiStrength;
    } else if (TYPE_2G.equals(type) || TYPE_3G.equals(type) || TYPE_4G.equals(type)) {
      return g3Strength;
    } else {
      return 0.0f;
    }
  }

  /**
   * 获取信号类型
   * 
   * @param info
   * @return
   */
  private String getType() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.get().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = connectivityManager.getActiveNetworkInfo();
    if (info != null) {
      String type = info.getTypeName();
      if (type.equalsIgnoreCase(WIFI)) {
        this.NetWorkName = TYPE_WIFI;
        return NetWorkName;
      } else if (type.equalsIgnoreCase(MOBILE)) {
        type = info.getSubtypeName();
        if (type.equalsIgnoreCase(GSM) || type.equalsIgnoreCase(GPRS)
            || type.equalsIgnoreCase(EDGE)) {
          NetWorkName = TYPE_2G;
          return NetWorkName;
        } else if (type.toLowerCase().startsWith(CDMA) || type.toLowerCase().equals(UMTS)
            || type.toLowerCase().equals(ONEXRTT) || type.toLowerCase().equals(EHRPD)
            || type.toLowerCase().equals(HSUPA) || type.toLowerCase().equals(HSDPA)
            || type.toLowerCase().equals(HSPA)) {
          NetWorkName = TYPE_3G;
          return NetWorkName;
        } else if (type.toLowerCase().equals(LTE) || type.toLowerCase().equals(UMB)
            || type.toLowerCase().equals(HSPA_PLUS)) {
          NetWorkName = TYPE_4G;
          return NetWorkName;
        }
      } else if (info.isConnected()) {
        if (info.getState() == NetworkInfo.State.CONNECTED) {
          NetWorkName = TYPE_CABLE;
          return NetWorkName;
        }
      }
    } else {
      this.NetWorkName = TYPE_NONE;
      return NetWorkName;
    }
    this.NetWorkName = TYPE_UNKNOWN;
    return NetWorkName;
  }

  /**
   * 获取wifi信号强度
   * 
   * @return
   */
  private float getWifiStrength() {
    WifiManager wifiManager = (WifiManager) context.get().getSystemService(Context.WIFI_SERVICE);
    int rssi = wifiManager.getConnectionInfo().getRssi();
    int level = WifiManager.calculateSignalLevel(rssi, 10);
    float percentage = (float) (level / 10.0);
    return percentage;
  }
}
