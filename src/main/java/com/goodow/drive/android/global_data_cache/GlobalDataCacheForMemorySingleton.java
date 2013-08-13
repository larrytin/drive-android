package com.goodow.drive.android.global_data_cache;

import java.io.File;

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

	// private List<Activity> activities = new ArrayList<Activity>();
	//
	// public void addActivity(Activity activity) {
	// for (int i = 0; i < activities.size(); i++) {
	// if (activity.getClass().getSimpleName()
	// .equals(activities.get(i).getClass().getSimpleName())) {
	// activities.remove(i);
	// }
	// }
	//
	// activities.add(activity);
	// }
	//
	// public void exit() {
	// for (Activity activity : activities) {
	// activity.finish();
	// }
	//
	// System.exit(0);
	// }

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

	public String getPasswordForLastSuccessfulLogon() {
		return passwordForLastSuccessfulLogon;
	}

	public String getScreenSize() {
		return screenSize;
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

	public String getOfflineResDirPath() {
		File offline = new File(storagePaht + "/res");
		if (!offline.exists()) {
			offline.mkdir();
		}

		return storagePaht + "/res";
	}

	/**
	 * @param access_token
	 *            the access_token to set
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

	public synchronized void setPasswordForLastSuccessfulLogon(
			String passwordForLastSuccessfulLogon) {
		this.passwordForLastSuccessfulLogon = passwordForLastSuccessfulLogon;
	}

	public synchronized void setScreenSize(String screenSize) {
		this.screenSize = screenSize;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setStoragePaht(String storagePaht) {
		this.storagePaht = storagePaht;
	}

	public String getStoragePaht() {
		return storagePaht;
	}

	public synchronized void setUsernameForLastSuccessfulLogon(
			String usernameForLastSuccessfulLogon) {
		this.usernameForLastSuccessfulLogon = usernameForLastSuccessfulLogon;
	}

}
