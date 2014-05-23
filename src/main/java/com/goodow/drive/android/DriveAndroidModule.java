package com.goodow.drive.android;

import com.goodow.drive.android.toolutils.DeviceInformationTools;
import com.goodow.realtime.android.AndroidPlatform;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.java.JavaWebSocket;
import com.goodow.realtime.store.Store;
import com.goodow.realtime.store.impl.DefaultStore;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;

public class DriveAndroidModule extends AbstractModule {
  private static final String SERVER = "ldh.goodow.com:1986";

  static {
    AndroidPlatform.register();
    // adb shell setprop log.tag.JavaWebSocket DEBUG
    Logger.getLogger(JavaWebSocket.class.getName()).setLevel(Level.ALL);
  }

  @Override
  protected void configure() {
  }

  @Provides
  @Singleton
  Bus provideBus(Store store) {
    return store.getBus();
  }

  @Provides
  @Singleton
  Store provideStore(Provider<Context> contextProvider) {
    return new DefaultStore("ws://" + SERVER + "/channel/websocket", null).authorize(
        DeviceInformationTools.getLocalMacAddressFromWifiInfo(contextProvider.get()), "");
  }
}
