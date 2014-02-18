/*
 * Copyright 2013 Goodow.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.goodow.drive.android;

import com.goodow.realtime.android.AndroidPlatform;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.impl.ReconnectBusClient;
import com.goodow.realtime.channel.impl.SimpleBus;
import com.goodow.realtime.channel.impl.WebSocketBusClient;
import com.goodow.realtime.json.Json;

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more
 * efficient means such as through injection directly into interested classes.
 */
public final class BusProvider {
  public static final String SID = "sid.drive.";
  private static final String HOST = "data.goodow.com:8080";
  static {
    AndroidPlatform.register();
  }
  private static final ReconnectBusClient ConnectBUS = new ReconnectBusClient("ws://" + HOST
      + "/eventbus/websocket", Json.createObject().set(ReconnectBusClient.AUTO_RECONNECT, false)
      .set(SimpleBus.MODE_MIX, true).set(WebSocketBusClient.PING_INTERVAL, Integer.MAX_VALUE));
  private static final ReconnectBusClient BUS = new ReconnectBusClient("ws://" + HOST
      + "/eventbus/websocket", Json.createObject().set(SimpleBus.MODE_MIX, true));

  // // 发布时使用
  // private static final Bus BUS = new SimpleBus(Json.createObject().set(SimpleBus.MODE_MIX,
  // true));

  public static Bus get() {
    return BUS;
  }

  public static ReconnectBusClient getConnectBus() {
    return ConnectBUS;
  }

  public static void reconnect() {
    BUS.reconnect();
  }

  private BusProvider() {
    // No instances.
  }
}