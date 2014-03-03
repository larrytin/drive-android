package com.goodow.drive.android.player;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.activity.BehaveActivity;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import com.artifex.mupdf.MuPDFActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;

public class PlayerRegistry {
  private final Context ctx;
  private final Bus bus;
  private SharedPreferences usagePreferences;

  public PlayerRegistry(Bus bus, Context ctx) {
    this.bus = bus;
    this.ctx = ctx;
    usagePreferences =
        ctx.getSharedPreferences(BehaveActivity.USAGE_STATISTIC, Context.MODE_PRIVATE);
  }

  public void subscribe() {
    bus.registerHandler(Constant.ADDR_PLAYER, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (!body.has("path")) {
          return;
        }
        String path = body.getString("path");
        Intent intent = null;
        if (path.endsWith(".pdf")) {
          bus.send(Bus.LOCAL + Constant.ADDR_PLAYER + ".pdf.mu", message.body(), null);
          return;
        } else if (path.endsWith(".mp4")) {
          intent = new Intent(ctx, VideoActivity.class);
        } else if (path.endsWith(".mp3")) {
          intent = new Intent(ctx, AudioPlayActivity.class);
        } else if (path.endsWith(".jpg")) {
          intent = new Intent(ctx, PicturePlayAcivity.class);
        } else if (path.endsWith(".swf")) {
          bus.send(Bus.LOCAL + Constant.ADDR_PLAYER + ".swf.webview", message.body(), null);
          return;
        } else {
          Toast.makeText(ctx, "不支持" + path, Toast.LENGTH_LONG).show();
          return;
        }
        intent.putExtra("msg", message.body());
        ctx.startActivity(intent);
      }
    });

    bus.registerHandler(Constant.ADDR_PLAYER + ".pdf.jz", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, PdfPlayer.class);
        intent.putExtra("msg", message.body());
        ctx.startActivity(intent);
      }
    });
    bus.registerHandler(Constant.ADDR_PLAYER + ".pdf.mu", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, MuPDFActivity.class);
        intent.putExtra("msg", message.body());
        ctx.startActivity(intent);
      }
    });

    bus.registerHandler(Constant.ADDR_PLAYER + ".swf.button", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, FlashPlayerActivity.class);
        intent.putExtra("msg", message.body());
        ctx.startActivity(intent);
      }
    });
    bus.registerHandler(Constant.ADDR_PLAYER + ".swf.webview", new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        Intent intent = new Intent(ctx, WebViewFlashPlayer.class);
        intent.putExtra("msg", message.body());
        ctx.startActivity(intent);
      }
    });
    BusProvider.getConnectBus().registerHandler(Constant.ADDR_PLAYER + ".analytics.request",
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            Map<String, Set<String>> fileOpenInfo =
                (Map<String, Set<String>>) usagePreferences.getAll();
            JsonObject msg = Json.createObject();
            JsonArray analytics = Json.createArray();
            for (Entry<String, Set<String>> entry : fileOpenInfo.entrySet()) {
              JsonObject jsonObject = Json.createObject();
              jsonObject.set("attachment", entry.getKey());
              JsonArray timeJsonArray = Json.createArray();
              Set<String> timeStamp = entry.getValue();
              // 将时间戳转化long
              List<Long> timeStampList = new ArrayList<Long>();
              for (String time : timeStamp) {
                timeStampList.add(Long.parseLong(time));
              }
              // 排序
              Collections.sort(timeStampList);
              for (Long time : timeStampList) {
                timeJsonArray.push(time);
              }
              jsonObject.set("timestamp", timeJsonArray);
              analytics.push(jsonObject);
            }
            // 如果拿到的信息为空，则不发送
            if (analytics.length() == 0) {
              return;
            }
            msg.set("sid", BusProvider.SID.split("[.]")[0]);
            msg.set("analytics", analytics);
            // 发送统计信息到服务器
            BusProvider.getConnectBus().send(Constant.ADDR_PLAYER + ".analytics", msg,
                new MessageHandler<JsonObject>() {
                  @Override
                  public void handle(Message<JsonObject> message) {
                    JsonObject msg = message.body();
                    // 发送成功后，清除记录
                    if (!("ok".equals(msg.getString("status")) && msg.has("ack"))) {
                      return;
                    }
                    // 得到返回的时间戳
                    long lastTimestamp = (long) msg.getNumber("ack");
                    Map<String, Set<String>> fileOpenInfo =
                        (Map<String, Set<String>>) usagePreferences.getAll();
                    // 删除比返回时间戳小的记录
                    for (Map.Entry<String, Set<String>> entry : fileOpenInfo.entrySet()) {
                      String attachmentKey = entry.getKey();
                      Set<String> timestampValue = entry.getValue();
                      boolean remove = false;
                      for (String timestamp : timestampValue) {
                        if (Long.parseLong(timestamp) <= lastTimestamp) {
                          remove = timestampValue.remove(timestamp);
                        }
                      }
                      if (remove) {
                        continue;
                      }
                      if (timestampValue.isEmpty()) {
                        usagePreferences.edit().remove(attachmentKey).commit();
                      } else {
                        Editor editor = usagePreferences.edit();
                        editor.remove(attachmentKey).commit();
                        editor.putStringSet(attachmentKey, timestampValue).commit();
                      }
                    }
                  }
                });
          }
        });
  }
}
