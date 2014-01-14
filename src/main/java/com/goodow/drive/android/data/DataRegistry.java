package com.goodow.drive.android.data;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.widget.Toast;

public class DataRegistry {

  private final Bus bus = BusProvider.get();
  private Context context = null;

  public DataRegistry(Context context) {
    this.context = context;
  }

  public void subscribe() {
    // 活动详情
    bus.registerHandler(Constant.ADDR_ACTIVITY, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();

        // 仅仅处理get动作：数据查询
        if ("get".equalsIgnoreCase(body.getString("action"))) {
          JsonObject activity = body.getObject("activity");
          JsonObject msg = Json.createObject();
          msg.set("activity", activity);
          msg.set("files", DataProvider.getInstance().getFiles(activity));
          msg.set("action", "post");
          message.reply(msg);
          return;
        }

        // 仅处理判断是否已经收藏动作
        if ("head".equalsIgnoreCase(body.getString("action"))) {
          JsonObject activity = body.getObject("activity");
          boolean result = DBOperator.isHave(context, activity);
          if (result) {
            body.set("status", "ok");
          }
          message.reply(body);
          return;
        }

      }
    });
    bus.registerHandler(Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();

        // 仅处理put动作:收藏到数据库
        if ("put".equalsIgnoreCase(body.getString("action"))) {
          JsonArray activities = body.getArray("activities");
          boolean result = DBOperator.createFavourite(context, activities);
          JsonObject msg = Json.createObject();
          if (result) {
            msg.set("status", "ok");
          } else {
            msg.set("status", "error");
          }
          message.reply(msg);
          return;
        }

        // 仅处理delete动作:从数据库中删除
        if ("delete".equalsIgnoreCase(body.getString("action"))) {
          JsonArray delActivities = body.getArray("activities");
          boolean result = DBOperator.deleteFavourite(context, delActivities);
          if (result) {
            body.set("status", "ok");
          }
          bus.send(Bus.LOCAL + "" + Bus.LOCAL + Constant.ADDR_TOPIC, body, null);
          return;
        }

        // 仅处理get动作
        if (!"get".equalsIgnoreCase(body.getString("action"))) {
          return;
        }
        // 解析查询条件
        if (!body.has("query")) {
          return;
        }
        JsonObject query = body.getObject("query");
        String type = query.getString(Constant.TYPE);// 解析请求的模块类型
        if (Constant.DATAREGISTRY_TYPE_HARMONY.equals(type) // 和谐
            || Constant.DATAREGISTRY_TYPE_SHIP.equals(type)// 托班
            || Constant.DATAREGISTRY_TYPE_CASE.equals(type)// 示范课
            || Constant.DATAREGISTRY_TYPE_PREPARE.equals(type)// 入学准备
            || Constant.DATAREGISTRY_TYPE_SMART.equals(type)// 智能开发
            || Constant.DATAREGISTRY_TYPE_EBOOK.equals(type)) {// 电子书
          JsonObject msg = Json.createObject();
          msg.set("activities", DataProvider.getInstance().getActivities(query));
          message.reply(msg);
          // System.out.println(msg);
        } else if (Constant.DATAREGISTRY_TYPE_FAVOURITE.equals(type)) {
          // 收藏
          JsonObject msg = Json.createObject();
          JsonArray activitys = DBOperator.readAllFavourite(context);
          msg.set("activities", activitys);
          message.reply(msg);
        } else {
          Toast.makeText(context, "不支持" + type, Toast.LENGTH_SHORT).show();
          return;
        }
      }
    });

    bus.registerHandler(Constant.ADDR_ACTIVITY, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if ("post".equalsIgnoreCase(body.getString("action")) && body.has("activity")
            && body.has("files")) {
          // post动作,有activity有files
          // TODO
          return;
        }
        if (!"get".equalsIgnoreCase(body.getString("action")) || !body.has("activity")) {
          // 不是get动作或不含activity时终止
          return;
        }
        JsonObject activity = body.getObject("activity");
        JsonObject msg = Json.createObject();
        msg.set("activity", activity);
        msg.set("files", DataProvider.getInstance().getFiles(activity));
        message.reply(msg);
        // System.out.println(msg);
      }

    });

    bus.registerHandler(Constant.ADDR_FILE, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body.has("path")) {
          JsonObject msg = DataProvider.getInstance().getFoldersAndFiles(body.getString("path"));
          message.reply(msg);
          // System.out.println(msg);
        } else {
          Toast.makeText(context, "需要传入path", Toast.LENGTH_SHORT).show();
        }
      }

    });

  }
}
