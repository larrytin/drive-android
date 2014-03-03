package com.goodow.drive.android.activity;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.settings.SettingsRegistry;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.State;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.JsonObject;

import android.app.Activity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * @title: BaseActivity.java
 * @package drive-android
 * @description: TODO
 * @author www.dingpengwei@gmail.com
 * @createDate 2013 2013-12-4 上午11:33:07
 * @updateDate 2013 2013-12-4 上午11:33:07
 * @version V1.0
 */
public class BaseActivity extends Activity {
  private static final String BRIGHTNESS = SettingsRegistry.PREFIX + "brightness.light";
  protected final Bus bus = BusProvider.get();

  private HandlerRegistration controlHandler;
  private HandlerRegistration brightnessHandler;

  @Override
  protected void onPause() {
    super.onPause();
    // Always unregister when an handler no longer should be on the bus.
    controlHandler.unregisterHandler();
    brightnessHandler.unregisterHandler();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (bus.getReadyState() == State.CLOSED || bus.getReadyState() == State.CLOSING) {
      Log.w("EventBus Status", bus.getReadyState().name());
      BusProvider.reconnect();
    }
    controlHandler = bus.registerHandler(Constant.ADDR_CONTROL, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = message.body();
        if (msg.has("return")) {
          finish();
          // 屏幕亮度
        } else if (msg.has("brightness")) {
          bus.send(Bus.LOCAL + SettingsRegistry.PREFIX + "brightness.view", msg, null);
        } else if (msg.has("shutdown")) {
          if (msg.getNumber("shutdown") == 0) {
            Toast.makeText(BaseActivity.this, "关机", Toast.LENGTH_LONG).show();
          } else if (msg.getNumber("shutdown") == 1) {
            Toast.makeText(BaseActivity.this, "重启", Toast.LENGTH_LONG).show();
          }
        }
      }
    });
    brightnessHandler = bus.registerHandler(BRIGHTNESS, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = message.body();
        if (msg.has("brightness")) {
          double strength = msg.getNumber("brightness");
          // 调节亮度,此方法只对平板，手机有效
          WindowManager.LayoutParams lp = getWindow().getAttributes();
          if ((0 < strength | strength == 0) && strength <= 1) {
            android.provider.Settings.System.putInt(getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, (int) strength * 255); // 0-255
            lp.screenBrightness = (float) strength;
          }
          getWindow().setAttributes(lp);
        }
      }
    });
  }
}
