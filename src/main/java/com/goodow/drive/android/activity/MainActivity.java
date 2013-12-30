package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.data.DataRegistry;
import com.goodow.drive.android.player.PlayerRegistry;
import com.goodow.drive.android.settings.SettingsRegistry;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.State;
import com.goodow.realtime.json.JsonElement;

import android.app.Activity;
import android.os.Bundle;

public class MainActivity extends Activity {
  // private static final Logger log = Logger.getLogger(MainActivity.class.getName());
  private final Bus bus = BusProvider.get();
  private static boolean registried;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_main);

    if (bus.getReadyState() == State.OPEN) {
      subscribe();
    } else {
      bus.registerHandler(Bus.LOCAL_ON_OPEN, new MessageHandler<JsonElement>() {
        @Override
        public void handle(Message<JsonElement> message) {
          subscribe();
        }
      });
    }
  }

  private void subscribe() {
    if (!registried) {
      registried = true;
      new ViewRegistry(this).subscribe();
      new PlayerRegistry(this).subscribe();
      new SettingsRegistry(this).subscribe();
      new DataRegistry(this).subscribe();
    }
  }
}