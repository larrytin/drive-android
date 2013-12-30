package com.goodow.drive.android.activity;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.toolutils.DeviceInformationTools;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

public class ViewRegistry {
  public static final String ADDR_PREFIX = BusProvider.SID + "view.";
  public static final String ADDR_TOPIC = BusProvider.SID + "topic";
  protected static final String TAG = ViewRegistry.class.getSimpleName();
  private final Bus bus = BusProvider.get();
  private final Context ctx;

  public ViewRegistry(Context ctx) {
    this.ctx = ctx;
  }

  public void subscribe() {
    bus.registerHandler(ADDR_PREFIX + "home", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, HomeActivity.class);
        intent.putExtra("msg", message.body());
        ctx.startActivity(intent);
      }
    });
    bus.registerHandler(ADDR_PREFIX + "wifi", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "设置wifi", Toast.LENGTH_LONG).show();
      }
    });
    bus.registerHandler(ADDR_PREFIX + "resolution", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "设置分辨率输出", Toast.LENGTH_LONG).show();
      }
    });
    bus.registerHandler(ADDR_PREFIX + "screenOffset", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "设置屏幕偏移", Toast.LENGTH_LONG).show();
      }
    });
    bus.registerHandler(ADDR_PREFIX + "aboutUs", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "关于我们", Toast.LENGTH_LONG).show();
      }
    });
    // 打开设置
    bus.registerHandler(ADDR_PREFIX + "settings", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, SettingActivity.class);
        ctx.startActivity(intent);
      }
    });
    bus.registerHandler(ADDR_TOPIC, new MessageHandler<JsonObject>() {
      String type;

      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString("action");
        if (!"post".equalsIgnoreCase(action)) {
          return;
        }
        JsonObject query = body.getObject("query");
        if (query == null || !query.has("type") || query.getString("type").equals(type)) {
          return;
        }
        type = query.getString("type");
        Intent intent = null;
        if ("和谐".equals(type)) {
          intent = new Intent(ctx, HarmonyActivity.class);
        } else if ("收藏".equals(type)) {
          Toast.makeText(ctx, "打开收藏夹", Toast.LENGTH_LONG).show();
        } else if ("托班".equals(type)) {
          Toast.makeText(ctx, "打开托班", Toast.LENGTH_LONG).show();
        } else if ("示范课".equals(type)) {
          Toast.makeText(ctx, "打开教学示范课", Toast.LENGTH_LONG).show();
        } else if ("入学准备".equals(type)) {
          Toast.makeText(ctx, "打开入学准备", Toast.LENGTH_LONG).show();
        } else if ("智能开发".equals(type)) {
          Toast.makeText(ctx, "打开智能开发", Toast.LENGTH_LONG).show();
        } else if ("电子书".equals(type)) {
          Toast.makeText(ctx, "打开图画书", Toast.LENGTH_LONG).show();
        } else {
          Toast.makeText(ctx, "不支持" + type, Toast.LENGTH_LONG).show();
          return;
        }
        intent.putExtra("msg", body);
        ctx.startActivity(intent);
      }
    });
    bus.registerHandler(ADDR_PREFIX + "status", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, StatusBarActivity.class);
        ctx.startActivity(intent);
      }
    });
    // 标注
    bus.registerHandler(ADDR_PREFIX + "scrawl", new MessageHandler<JsonObject>() {
      private final WindowManager mWindowManager = (WindowManager) ctx.getApplicationContext()
          .getSystemService(Context.WINDOW_SERVICE);
      private LayoutParams mLayoutParams;
      private DrawView mDrawView;

      @Override
      public void handle(Message<JsonObject> message) {
        if (mDrawView == null) {
          mDrawView =
              new DrawView(ctx, DeviceInformationTools.getScreenWidth(ctx), DeviceInformationTools
                  .getScreenHeight(ctx));
          mLayoutParams = new WindowManager.LayoutParams();
          mLayoutParams.format = PixelFormat.RGBA_8888;
          mLayoutParams.flags =
              LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_LAYOUT_IN_SCREEN;
          mLayoutParams.type = LayoutParams.TYPE_PHONE;
          Log.d(TAG, "init mDrawView");
        }
        JsonObject draw = message.body();
        if (draw.has("annotation")) {
          if (draw.getBoolean("annotation")) {
            mWindowManager.addView(mDrawView, mLayoutParams);
          } else {
            mWindowManager.removeView(mDrawView);
            mDrawView = null;
          }
        } else if (draw.has("clear")) {
          mDrawView.clear();
        }
      }
    });

  }
}
