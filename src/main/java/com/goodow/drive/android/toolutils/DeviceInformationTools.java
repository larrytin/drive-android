package com.goodow.drive.android.toolutils;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

public class DeviceInformationTools {
  /*
   * 如果一个手机设备第一次启动随即产生的一个数字，如果系统改变，该号可能会改变。
   */
  public static String getAndroidId(Context mContext) {
    return android.provider.Settings.System.getString(mContext.getContentResolver(), "android_id");
  }

  /*
   * 对于GSM手机返回IMEI，对于CDMA手机返回MEID,如果设备不可用则返回NULL android:name="android.permission.READ_PHONE_STATE"
   */
  public static String getIMEI(Context mContext) {
    TelephonyManager mTelephonyManager =
        (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    return mTelephonyManager.getDeviceId();
  }

  // android.permission.ACCESS_WIFI_STATE
  // wifi连接时， 取得device的IP address
  public static String getIp(final Context context) {
    WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
    int ipAddress = mWifiInfo.getIpAddress();
    // 格式化IP address，例如：格式化前：1828825280，格式化后：192.168.1.109
    return String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
        (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
  }

  /**
   * 获得的ipv4地址
   * 
   * @return
   */
  public static String getLocalIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
          .hasMoreElements();) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
            .hasMoreElements();) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          if (!inetAddress.isLoopbackAddress()
              && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
            return inetAddress.getHostAddress().toString();
          }
        }
      }
    } catch (SocketException e) {
      e.printStackTrace();
    }

    return null;
  }

  // 根据Wifi信息获取本地Mac
  public static String getLocalMacAddressFromWifiInfo(Context context) {
    WifiManager mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
    return mWifiInfo.getMacAddress();
  }

  public static String getOsCodeName() {
    return android.os.Build.VERSION.CODENAME;
  }

  // 获得系统的型号
  public static String getOsModel() {
    return android.os.Build.MODEL;
  }

  // 获取device的os version 固件版本
  public static String getOsVersion() {
    return android.os.Build.VERSION.RELEASE;
  }

  // 获得SDK版本
  public static String getSDK() {
    return android.os.Build.VERSION.SDK;
  }

  public static int getSDKINT() {
    return android.os.Build.VERSION.SDK_INT;
  }

}
