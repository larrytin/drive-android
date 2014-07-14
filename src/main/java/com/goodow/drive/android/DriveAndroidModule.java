package com.goodow.drive.android;

import android.content.Context;
import com.goodow.drive.android.toolutils.DeviceInformationTools;
import com.goodow.realtime.android.AndroidPlatform;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.impl.ReconnectBus;
import com.goodow.realtime.channel.impl.ReliableSubscribeBus;
import com.goodow.realtime.channel.impl.WebSocketBus;
import com.goodow.realtime.core.Handler;
import com.goodow.realtime.java.JavaWebSocket;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.store.Store;
import com.goodow.realtime.store.impl.StoreImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DriveAndroidModule extends AbstractModule {
  private static final String SERVER = "dpw.goodow.com:1986";
  private static final String URL = "ws://" + SERVER + "/channel/websocket";

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
    ReliableSubscribeBus bus = (ReliableSubscribeBus) store.getBus();
    final ReconnectBus reconnectBus = (ReconnectBus) bus.getDelegate();
    bus.subscribeLocal("Bus_Reconnet", new Handler<Message>() {
        @Override
        public void handle(Message event) {
            reconnectBus.connect(URL, null);
        }
    });
    return bus;
  }

  @Provides
  @Singleton
  Store provideStore(Provider<Context> contextProvider) {
      return new StoreImpl(URL, Json.createObject().set(WebSocketBus.SESSION, DeviceInformationTools
              .getLocalMacAddressFromWifiInfo(contextProvider.get())));
  }
}
