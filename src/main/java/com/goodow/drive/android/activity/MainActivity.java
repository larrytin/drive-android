package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.data.DataRegistry;
import com.goodow.drive.android.player.PlayerRegistry;
import com.goodow.drive.android.settings.SettingsRegistry;

import android.os.Bundle;

public class MainActivity extends BaseActivity {
  // private static final Logger log = Logger.getLogger(MainActivity.class.getName());
  private static boolean registried;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_main);

    subscribe();
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