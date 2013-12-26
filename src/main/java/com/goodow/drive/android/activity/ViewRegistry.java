package com.goodow.drive.android.activity;

import com.goodow.drive.android.BusProvider;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class ViewRegistry {
  public static final String PREFIX = BusProvider.SID + "view.";
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
        // Intent intent = new Intent(ctx, HomeActivity.class);
        // ctx.startActivity(intent);
        Toast.makeText(ctx, "打开和谐页", Toast.LENGTH_LONG).show();
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

  }
}