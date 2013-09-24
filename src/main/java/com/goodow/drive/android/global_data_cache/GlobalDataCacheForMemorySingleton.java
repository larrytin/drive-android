package com.goodow.drive.android.global_data_cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;

/**
 * 需要全局缓存的数据
 * 
 * @author zhihua.tang
 */
public enum GlobalDataCacheForMemorySingleton {
  getInstance;

  public static synchronized GlobalDataCacheForMemorySingleton getInstance() {

    return getInstance;
  }

  // 客户端应用版本号
  private String clientVersion;

  // 客户端 Android 版本号
  private String clientAVersion;

  // 屏幕大小
  private String screenSize;

  // 用户最后一次登录成功时的用户名
  private String usernameForLastSuccessfulLogon;

  // 用户最后一次登录成功时的密码
  private String passwordForLastSuccessfulLogon;

  private String userId;

  private String access_token;

  private String userName;

  private String storagePaht;

  private final List<Activity> activities = new ArrayList<Activity>();

  public void addActivity(Activity activity) {
    if (activities.contains(activity)) {
      activities.remove(activity);
    }

    activities.add(activity);
  }

  public void exit() {
    for (Activity activity : activities) {
      activity.finish();
    }
  }

  /**
   * @return the access_token
   */
  public String getAccess_token() {
    return access_token;
  }

  public String getClientAVersion() {
    return clientAVersion;
  }

  public String getClientVersion() {
    return clientVersion;
  }

  public String getOfflineResDirPath() {
    File offline = new File(storagePaht + "/res");
    if (!offline.exists()) {
      offline.mkdir();
    }

    return storagePaht + "/res";
  }

  public String getPasswordForLastSuccessfulLogon() {
    return passwordForLastSuccessfulLogon;
  }

  public String getScreenSize() {
    return screenSize;
  }

  public String getStoragePaht() {
    return storagePaht;
  }

  /**
   * @return the userId
   */
  public String getUserId() {
    return userId;
  }

  /**
   * @return the userName
   */
  public String getUserName() {
    return userName;
  }

  public String getUsernameForLastSuccessfulLogon() {
    return usernameForLastSuccessfulLogon;
  }

  public String getUserResDirPath() {
    if (null == userId) {
      return null;
    } else {
      return storagePaht + "/" + userId;
    }
  }

  public void removeActivity(Activity activity) {
    activities.remove(activity);
  }

  /**
   * @param access_token the access_token to set
   */
  public void setAccess_token(String access_token) {
    this.access_token = access_token;
  }

  public synchronized void setClientAVersion(String clientAVersion) {
    this.clientAVersion = clientAVersion;
  }

  public synchronized void setClientVersion(String clientVersion) {
    this.clientVersion = clientVersion;
  }

  public synchronized void setPasswordForLastSuccessfulLogon(String passwordForLastSuccessfulLogon) {
    this.passwordForLastSuccessfulLogon = passwordForLastSuccessfulLogon;
  }

  public synchronized void setScreenSize(String screenSize) {
    this.screenSize = screenSize;
  }

  public void setStoragePaht(String storagePaht) {
    this.storagePaht = storagePaht;
  }

  /**
   * @param userId the userId to set
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * @param userName the userName to set
   */
  public void setUserName(String userName) {
    this.userName = userName;
  }

  public synchronized void setUsernameForLastSuccessfulLogon(String usernameForLastSuccessfulLogon) {
    this.usernameForLastSuccessfulLogon = usernameForLastSuccessfulLogon;
  }

}
