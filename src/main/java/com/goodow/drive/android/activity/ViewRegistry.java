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
  public static final String PREFIX = BusProvider.SID + "view.";
  protected static final String TAG = ViewRegistry.class.getSimpleName();
  private final Bus bus = BusProvider.get();
  private final Context ctx;

  public ViewRegistry(Context ctx) {
    this.ctx = ctx;
  }

  public void subscribe() {
    bus.registerHandler(PREFIX + "home", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, HomeActivity.class);
        intent.putExtra("msg", message.body());
        ctx.startActivity(intent);
      }
    });
    bus.registerHandler(PREFIX + "wifi", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "设置wifi", Toast.LENGTH_LONG).show();
      }
    });
    bus.registerHandler(PREFIX + "resolution", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "设置分辨率输出", Toast.LENGTH_LONG).show();
      }
    });
    bus.registerHandler(PREFIX + "screenOffset", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "设置屏幕偏移", Toast.LENGTH_LONG).show();
      }
    });
    bus.registerHandler(PREFIX + "aboutUs", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "关于我们", Toast.LENGTH_LONG).show();
      }
    });
    // 打开收藏夹
    bus.registerHandler(PREFIX + "favorite", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "打开收藏夹", Toast.LENGTH_LONG).show();
      }
    });
    // 打开和谐页
    bus.registerHandler(PREFIX + "harmony", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, HarmonyActivity.class);
        ctx.startActivity(intent);
      }
    });
    // 打开设置
    bus.registerHandler(PREFIX + "settings", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, SettingActivity.class);
        ctx.startActivity(intent);
      }
    });
    // 打开托班
    bus.registerHandler(PREFIX + "toddler", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "打开托班", Toast.LENGTH_LONG).show();
      }
    });
    // 打开教学示范课
    bus.registerHandler(PREFIX + "case", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "打开教学示范课", Toast.LENGTH_LONG).show();
      }
    });
    // 打开入学准备
    bus.registerHandler(PREFIX + "readiness", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "打开入学准备", Toast.LENGTH_LONG).show();
      }
    });
    // 打开智能开发
    bus.registerHandler(PREFIX + "intelligence", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "打开智能开发", Toast.LENGTH_LONG).show();
      }
    });
    // 打开图画书
    bus.registerHandler(PREFIX + "ebook", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "打开图画书", Toast.LENGTH_LONG).show();
      }
    });
    bus.registerHandler(PREFIX + "status", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, StatusBarActivity.class);
        ctx.startActivity(intent);
      }
    });
    // 标注
    bus.registerHandler(PREFIX + "scrawl", new MessageHandler<JsonObject>() {
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
