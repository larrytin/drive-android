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
import com.goodow.realtime.channel.EventBus;
import com.goodow.realtime.channel.EventBus.EventBusHandler;
import com.goodow.realtime.json.Json;

import java.util.logging.Logger;

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be replaced with a more
 * efficient means such as through injection directly into interested classes.
 */
public final class BusProvider {
  public static final String SID = "sid.drive.";
  public static final String EVENTBUS_OPEN = EventBus.LOCAL + "eventbus.open";
  public static final String EVENTBUS_CLOSE = EventBus.LOCAL + "eventbus.close";
  private static final String HOST = "data.goodow.com:8080";
  private static final Logger log = Logger.getLogger(BusProvider.class.getName());
  static {
    AndroidPlatform.register();
  }
  private static final EventBus BUS = new EventBus("ws://" + HOST + "/eventbus/websocket", null);
  static {
    BUS.setListener(new EventBusHandler() {
      @Override
      public void onClose() {
        log.info("EventBus closed");
        BUS.publish(EventBus.LOCAL + EVENTBUS_CLOSE, Json.createObject());
      }

      @Override
      public void onOpen() {
        BUS.publish(EventBus.LOCAL + EVENTBUS_OPEN, Json.createObject());
      }
    });
  }

  public static EventBus get() {
    return BUS;
  }

  private BusProvider() {
    // No instances.
  }
}