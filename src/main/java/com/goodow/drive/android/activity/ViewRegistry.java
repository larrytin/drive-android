package com.goodow.drive.android.activity;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
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
  protected static final String TAG = ViewRegistry.class.getSimpleName();
  private final Bus bus = BusProvider.get();
  private final Context ctx;

  public ViewRegistry(Context ctx) {
    this.ctx = ctx;
  }

  public void subscribe() {
    bus.registerHandler(Constant.ADDR_PREFIX_VIEW + "home", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, HomeActivity.class);
        intent.putExtra("msg", message.body());
        ctx.startActivity(intent);
      }
    });
    bus.registerHandler(Constant.ADDR_PREFIX_VIEW + "wifi", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        ctx.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
      }
    });
    bus.registerHandler(Constant.ADDR_PREFIX_VIEW + "resolution", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "设置分辨率输出", Toast.LENGTH_LONG).show();
      }
    });
    bus.registerHandler(Constant.ADDR_PREFIX_VIEW + "screenOffset",
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            // Intent intent = new Intent(ctx, HomeActivity.class);
            // ctx.startActivity(intent);
            Toast.makeText(ctx, "设置屏幕偏移", Toast.LENGTH_LONG).show();
          }
        });
    bus.registerHandler(Constant.ADDR_PREFIX_VIEW + "aboutUs", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "sid=" + BusProvider.SID, Toast.LENGTH_LONG).show();
      }
    });
    // 打开设置
    bus.registerHandler(Constant.ADDR_PREFIX_VIEW + "settings", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, SettingActivity.class);
        ctx.startActivity(intent);
      }
    });
    bus.registerHandler(Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString("action");
        if (!"post".equalsIgnoreCase(action)) {
          return;
        }
        JsonObject query = body.getObject("query");
        if (query == null || !query.has(Constant.TYPE)) {
          return;
        }
        String type = query.getString(Constant.TYPE);
        Intent intent = null;
        if (Constant.DATAREGISTRY_TYPE_HARMONY.equals(type)) {
          // 和谐
          intent = new Intent(ctx, HarmonyActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_FAVOURITE.equals(type)) {
          // 收藏
          intent = new Intent(ctx, FavouriteActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_SHIP.equals(type)) {
          // 托班
          intent = new Intent(ctx, CareClassesActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_CASE.equals(type)) {
          // 示范课
          intent = new Intent(ctx, CaseActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_PREPARE.equals(type)) {
          // 入学准备
          intent = new Intent(ctx, PrepareActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_SMART.equals(type)) {
          // 智能开发
          intent = new Intent(ctx, SmartActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_EBOOK.equals(type)) {
          // 图画书
          intent = new Intent(ctx, EbookActivity.class);
        } else {
          // 其他
          Toast.makeText(ctx, "不支持" + type, Toast.LENGTH_LONG).show();
          return;
        }
        intent.putExtra("msg", body);
        ctx.startActivity(intent);
      }
    });
    bus.registerHandler(Constant.ADDR_PREFIX_VIEW + "status", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, StatusBarActivity.class);
        ctx.startActivity(intent);
      }
    });
    // 标注
    bus.registerHandler(Constant.ADDR_PREFIX_VIEW + "scrawl", new MessageHandler<JsonObject>() {
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
