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
import com.goodow.realtime.channel.impl.BusClient;
import com.goodow.realtime.channel.impl.BusClientHandler;

import java.util.logging.Logger;

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more
 * efficient means such as through injection directly into interested classes.
 */
public final class BusProvider {
  public static final String SID = "sid.drive.";
  public static final String EVENTBUS_OPEN = Bus.LOCAL + "eventbus.open";
  public static final String EVENTBUS_CLOSE = Bus.LOCAL + "eventbus.close";
  private static final String HOST = "data.goodow.com:8080";
  private static final Logger log = Logger.getLogger(BusProvider.class.getName());
  static {
    AndroidPlatform.register();
  }
  private static final BusClient BUS = new BusClient("ws://" + HOST + "/eventbus/websocket", null);
  static {
    BUS.setListener(new BusClientHandler() {
      @Override
      public void onClose() {
        log.info("BusClient closed");
        BUS.publish(Bus.LOCAL + EVENTBUS_CLOSE, null);
      }

      @Override
      public void onOpen() {
        BUS.publish(Bus.LOCAL + EVENTBUS_OPEN, null);
      }
    });
  }

  public static Bus get() {
    return BUS;
  }

  private BusProvider() {
    // No instances.
  }
}