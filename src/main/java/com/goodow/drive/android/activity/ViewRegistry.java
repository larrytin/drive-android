package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.toolutils.DeviceInformationTools;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import com.google.inject.Inject;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ViewRegistry {
  @Inject
  private Context ctx;
  @Inject
  private Bus bus;

  public void subscribe() {
    /**
     * 打开VIEW[主页，收藏，设置等]
     */
    bus.subscribeLocal(Constant.ADDR_VIEW, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String redirectto = body.getString(Constant.KEY_REDIRECTTO);
        if ("home".equals(redirectto)) {
          // 去home页面
          Intent intent = new Intent(ctx, HomeActivity.class);
          intent.putExtra("msg", message.body());
          ctx.startActivity(intent);
        }
        if ("favorite".equals(redirectto)) {
          // 去收藏
          Intent intent = new Intent(ctx, FavouriteActivity.class);
          intent.putExtra("msg", body);
          ctx.startActivity(intent);
        }
        if ("settings".equals(redirectto)) {
          Intent intent = new Intent(ctx, SettingActivity.class);
          ctx.startActivity(intent);
        }
        if ("settings.wifi".equals(redirectto)) {// 打开杰科盒子的wifi设置
          // ctx.startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
          Intent intent = new Intent();
          intent.setComponent(new ComponentName("com.giec.settings",
              "com.giec.settings.WifiSettings"));
          ctx.startActivity(intent);
        }
        if ("settings.all".equals(redirectto)) {// 打开杰科盒子的完整设置
          Intent intent = new Intent();
          intent.setComponent(new ComponentName("com.giec.settings",
              "com.giec.settings.MainSettingsActivity"));
          ctx.startActivity(intent);
        }
        if ("settings.screenOffset".equals(redirectto)) {// 打开杰科盒子的屏幕偏移设置
          Intent intent = new Intent();
          intent.setComponent(new ComponentName("com.giec.settings",
              "com.giec.settings.ScreenScaleSettings"));
          ctx.startActivity(intent);
        }
        if ("aboutUs".equals(redirectto)) {
          final WindowManager wm =
              (WindowManager) ctx.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
          final View view = View.inflate(ctx.getApplicationContext(), R.layout.about_us, null);
          LayoutParams params = new WindowManager.LayoutParams();
          params.width = 643;
          params.height = 476;
          params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
          params.y = 200;
          params.type = LayoutParams.TYPE_PHONE;
          params.format = PixelFormat.RGBA_8888;
          wm.addView(view, params);
          TextView tv_mac = (TextView) view.findViewById(R.id.tv_mac);
          tv_mac.setText("MAC地址：" + DeviceInformationTools.getLocalMacAddressFromWifiInfo(ctx));
          Button btn_close = (Button) view.findViewById(R.id.btn_close);
          btn_close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              wm.removeView(view);
            }
          });
        }
      }
    });

    /**
     * 打开活动详情
     */
    bus.subscribeLocal(Constant.ADDR_ACTIVITY, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = message.body();
        if (!"post".equals(msg.getString("action"))) {
          return;
        }
        Intent intent = new Intent(ctx, BehaveActivity.class);
        intent.putExtra("msg", msg);
        ctx.startActivity(intent);
      }
    });

    /**
     * 
     */
    bus.subscribeLocal(Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString(Constant.KEY_ACTION);
        if (!"post".equalsIgnoreCase(action)) {
          return;
        }
        JsonArray tags = body.getArray(Constant.KEY_TAGS);
        if (tags == null) {
          Toast.makeText(ctx, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
          return;
        }
        int len_tags = tags.length();
        String type = null;
        for (int i = 0; i < len_tags; i++) {
          if (Constant.LABEL_THEMES.contains(tags.getString(i))) {
            type = tags.getString(i);
            break;
          }
        }
        Intent intent = null;
        if (Constant.DATAREGISTRY_TYPE_HARMONY.equals(type)) {
          // 和谐
          intent = new Intent(ctx, HarmonyActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_SHIP.equals(type)) {
          // 托班
          intent = new Intent(ctx, CareClassesActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_CASE.equals(type)) {
          // 示范课
          intent = new Intent(ctx, CaseActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_PREPARE.equals(type)) {
          // 入学准备
          intent = new Intent(ctx, PrepareActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_EDUCATION.equals(type)) {
          // 安全教育
          intent = new Intent(ctx, SecurityActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_READ.equals(type)) {
          // 早期阅读
          intent = new Intent(ctx, EarlyReadingActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_SOURCE.equals(type)) {
          // 资源库
          intent = new Intent(ctx, SourceActivity.class);
        } else if (Constant.DATAREGISTRY_TYPE_SHIP_EBOOK.equals(type)) {
          // 托班-电子书
          intent = new Intent(ctx, EbookActivity.class);
        } else {
          return;
        }
        intent.putExtra("msg", body);
        ctx.startActivity(intent);
      }
    });
    bus.subscribeLocal(Constant.ADDR_VIEW_STATUS, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, StatusBarActivity.class);
        ctx.startActivity(intent);
      }
    });

    /**
     * 提示使用
     */
    bus.subscribeLocal(Constant.ADDR_VIEW_PROMPT, new MessageHandler<JsonObject>() {
      private LayoutParams mLayoutParams;
      private TextView mView;
      private final WindowManager mWindowManager = (WindowManager) ctx.getApplicationContext()
          .getSystemService(Context.WINDOW_SERVICE);
      private boolean mSwitch = true;

      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject msg = message.body();
        if (msg.has("status")) {
          if (mView == null && msg.getBoolean("status")) {
            mView = new TextView(ctx);
            mView.setText(R.string.string_register_try);
            mView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            mView.setTextColor(Color.parseColor("#666666"));
            if (msg.has("content")) {
              mView.setText(msg.getString("content"));
            }
            mView.setTextSize(18);
            mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            mLayoutParams.format = PixelFormat.RGBA_8888;
            mLayoutParams.x = 1020;
            mLayoutParams.y = 45;
            mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            mLayoutParams.type = LayoutParams.TYPE_SYSTEM_OVERLAY;
          }
          if (msg.getBoolean("status")) {
            if (mSwitch) {
              mWindowManager.addView(mView, mLayoutParams);
              mSwitch = false;
            }
          } else {
            if (!mSwitch) {
              mWindowManager.removeView(mView);
              mView = null;
              mSwitch = true;
            }
          }
        }
      }
    });
    // 标注
    bus.subscribeLocal(Constant.ADDR_VIEW_SCRAWL, new MessageHandler<JsonObject>() {
      private final WindowManager mWindowManager = (WindowManager) ctx.getApplicationContext()
          .getSystemService(Context.WINDOW_SERVICE);
      private LayoutParams mLayoutParams;
      private DrawView mDrawView;
      private boolean mSwitch = true;

      @Override
      public void handle(Message<JsonObject> message) {
        if (mDrawView == null) {
          mDrawView =
              new DrawView(ctx, DeviceInformationTools.getScreenWidth(ctx), DeviceInformationTools
                  .getScreenHeight(ctx));
          mLayoutParams = new WindowManager.LayoutParams();
          mLayoutParams.gravity = Gravity.LEFT;
          mLayoutParams.format = PixelFormat.RGBA_8888;
          mLayoutParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL;
          // mLayoutParams.width = WindowManager.LayoutParams.FILL_PARENT;
          mLayoutParams.x = 0;
          mLayoutParams.y = 0;
          mLayoutParams.width = DeviceInformationTools.getScreenWidth(ctx) - 80;
          mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
          mLayoutParams.type = LayoutParams.TYPE_PHONE;
        }
        JsonObject draw = message.body();
        if (draw.has("annotation")) {
          if (draw.getBoolean("annotation")) {
            if (mSwitch) {
              mWindowManager.addView(mDrawView, mLayoutParams);
              mSwitch = false;
            }
          } else {
            if (!mSwitch) {
              mWindowManager.removeView(mDrawView);
              mDrawView = null;
              mSwitch = true;
            }
          }
        } else if (draw.has("clear")) {
          if (!mSwitch) {
            mDrawView.clear();
          }
        }
      }
    });
  }
}
