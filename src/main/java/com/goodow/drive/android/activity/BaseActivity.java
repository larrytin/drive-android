package com.goodow.drive.android.activity;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.settings.SettingsRegistry;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.JsonObject;

import android.app.Activity;
import android.view.WindowManager;

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
  public static final String CONTROL = BusProvider.SID + "control";
  public static final String BRIGHTNESS = SettingsRegistry.PREFIX + "brightness.light";
  protected final Bus bus = BusProvider.get();

  private final MessageHandler<JsonObject> brightnessHandler = new MessageHandler<JsonObject>() {

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
  };
  private final MessageHandler<JsonObject> controlHandler = new MessageHandler<JsonObject>() {

    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject msg = message.body();
      if (msg.has("return")) {
        finish();
        // 屏幕亮度
      } else if (msg.has("brightness")) {
        bus.send(Bus.LOCAL + SettingsRegistry.PREFIX + "brightness.view", msg, null);
      }
    }
  };

  @Override
  protected void onPause() {
    super.onPause();
    // Always unregister when an handler no longer should be on the bus.
    bus.unregisterHandler(CONTROL, controlHandler);
    bus.unregisterHandler(BRIGHTNESS, brightnessHandler);
  }

  @Override
  protected void onResume() {
    super.onResume();
    // Register handlers so that we can receive event messages.
    bus.registerHandler(CONTROL, controlHandler);
    bus.registerHandler(BRIGHTNESS, brightnessHandler);
  }
}
