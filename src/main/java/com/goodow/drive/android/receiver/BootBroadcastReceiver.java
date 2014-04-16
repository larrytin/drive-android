package com.goodow.drive.android.receiver;

import com.goodow.drive.android.data.DBOperator;
import com.goodow.realtime.json.Json;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class BootBroadcastReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
      // 记录开机
      DBOperator.addBootData(context, "T_BOOT", "OPEN_TIME", "LAST_TIME", "CLOSE_TIME", Json
          .createObject().set("OPEN_TIME", System.currentTimeMillis()).set("LAST_TIME",
              SystemClock.uptimeMillis()).set("CLOSE_TIME", System.currentTimeMillis()));
    }
  }
}
