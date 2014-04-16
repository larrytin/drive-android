package com.goodow.drive.android.player;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.activity.BaseActivity;
import com.goodow.drive.android.data.DBOperator;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.JsonObject;

import com.artifex.mupdf.MuPDFActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

public class PlayerRegistry {
  private final Context ctx;
  private final Bus bus;
  private final SharedPreferences usagePreferences;

  public PlayerRegistry(Bus bus, Context ctx) {
    this.bus = bus;
    this.ctx = ctx;
    usagePreferences = ctx.getSharedPreferences(BaseActivity.USAGE_STATISTIC, Context.MODE_PRIVATE);
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
    bus.registerHandler(BusProvider.SID + "systime.analytics.request",
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject analytics = DBOperator.readBootData(ctx, "T_BOOT", "OPEN_TIME", "LAST_TIME");
            final int id = (int) analytics.getNumber("id");
            analytics.remove("id");
            // 如果拿到的信息为空，则不发送
            if (analytics.getArray("timestamp").length() == 0) {
              return;
            }
            analytics.set("sid", BusProvider.SID.split("[.]")[0]);
            // 发送开机数据到服务器
            bus.send("sid.drive.systime.analytics", analytics, new MessageHandler<JsonObject>() {
              @Override
              public void handle(Message<JsonObject> message) {
                JsonObject msg = message.body();
                // 发送成功后，清除记录
                if (!("ok".equals(msg.getString("status")))) {
                  return;
                }
                DBOperator.deleteUserData(ctx, "T_BOOT", id);
              }
            });
          }
        });
    bus.registerHandler(Constant.ADDR_PLAYER + ".analytics.request",
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject analytics =
                DBOperator.readUserPlayerData(ctx, "T_PLAYER", "FILE_NAME", "OPEN_TIME",
                    "LAST_TIME");
            final int id = (int) analytics.getNumber("id");
            analytics.remove("id");
            // 如果拿到的信息为空，则不发送
            if (analytics.getArray("analytics").length() == 0) {
              return;
            }
            analytics.set("sid", BusProvider.SID.split("[.]")[0]);
            // 发送统计信息到服务器
            bus.send("sid.drive.player.analytics", analytics, new MessageHandler<JsonObject>() {
              @Override
              public void handle(Message<JsonObject> message) {
                JsonObject msg = message.body();
                // 发送成功后，清除记录
                if (!("ok".equals(msg.getString("status")))) {
                  return;
                }
                DBOperator.deleteUserData(ctx, "T_PLAYER", id);
              }
            });
          }
        });
  }
}