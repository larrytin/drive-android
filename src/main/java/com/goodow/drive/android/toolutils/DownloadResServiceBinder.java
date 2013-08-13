package com.goodow.drive.android.toolutils;

import com.goodow.drive.android.service.MediaDownloadService.MyBinder;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class DownloadResServiceBinder implements ServiceConnection {
	private static MyBinder binder;

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		binder = (MyBinder) service;

	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		binder = null;
	}

	public static MyBinder getDownloadResServiceBinder() {
		return binder;
	}

}
