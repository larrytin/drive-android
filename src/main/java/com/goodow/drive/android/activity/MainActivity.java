package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.player.PlayerRegistry;
import com.goodow.drive.android.settings.SettingsRegistry;
import com.goodow.realtime.channel.EventBus;
import com.goodow.realtime.channel.EventHandler;
import com.goodow.realtime.core.WebSocket.State;
import com.goodow.realtime.json.JsonElement;

import android.os.Bundle;

public class MainActivity extends BaseActivity {
  // private static final Logger log = Logger.getLogger(MainActivity.class.getName());
  private final EventBus eb = BusProvider.get();
  private static boolean registried;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_main);

    if (eb.getReadyState() == State.OPEN) {
      subscribe();
    } else {
      eb.registerHandler(BusProvider.EVENTBUS_OPEN, new EventHandler<JsonElement>() {
        @Override
        public void handler(JsonElement message, EventHandler<JsonElement> reply) {
          subscribe();
        }
      });
    }
  }

  private void subscribe() {
    if (!registried) {
      registried = true;
      new PlayerRegistry(MainActivity.this).subscribe();
      new SettingsRegistry(MainActivity.this).subscribe();
    }
  }
}