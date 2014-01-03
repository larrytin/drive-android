package com.goodow.drive.android.player;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class PlayerRegistry {
  private static final String TAG = PlayerRegistry.class.getSimpleName();
  private final Bus bus = BusProvider.get();
  private final Context ctx;

  public PlayerRegistry(Context ctx) {
    this.ctx = ctx;
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
        Intent intent = new Intent(ctx, PdfMuPlayer.class);
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
  }
}
