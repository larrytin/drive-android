package com.goodow.drive.android.data;

import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.Random;

import android.content.Context;
import android.widget.Toast;

public class DataRegistry {

  private final Bus bus = BusProvider.get();
  private Context context = null;

  public DataRegistry(Context context) {
    this.context = context;
  }

  public void subscribe() {
    bus.registerHandler(Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();

        // 仅处理put动作
        if ("put".equalsIgnoreCase(body.getString("action"))) {
          JsonArray activities = body.getArray("activities");
          DBOperator.createFavourite(context, activities);
          return;
        }
        // 仅处理delete动作
        if ("delete".equalsIgnoreCase(body.getString("action"))) {
          JsonArray delActivities = body.getArray("activities");
          boolean result = DBOperator.deleteFavourite(context, delActivities);
          if (result) {
            body.set("status", "ok");
          }
          bus.send(Bus.LOCAL + "" + Bus.LOCAL + Constant.ADDR_TOPIC, body, null);
          return;
        }

        // 解析查询条件
        JsonObject query = body.getObject("query");

        // 仅处理get动作
        if (!"get".equalsIgnoreCase(body.getString("action"))) {
          return;
        }
        String type = query.getString(Constant.TYPE);// 解析请求的模块类型
        if (Constant.DATAREGISTRY_TYPE_HARMONY.equals(type)) {
          // 和谐
          // String grade = query.getString(Constant.GRADE);
          // String term = query.getString(Constant.TERM);
          // String topic = query.getString(Constant.TOPIC);
          JsonObject msg = Json.createObject();
          JsonArray activities = Json.createArray();
          for (int i = 0; i < 30; i++) {
            JsonObject activity = Json.createObject();
            activity.set("title", "找朋友=" + i);
            activities.insert(i, activity);
          }
          msg.set("activities", activities);
          message.reply(msg);
        } else if (Constant.DATAREGISTRY_TYPE_SHIP.equals(type)) {
          // 托班
          String[] test = new String[] {"找", "找找", "找找藏藏", "找找藏"};
          JsonObject msg = Json.createObject();
          JsonArray activities = Json.createArray();
          for (int i = 0; i < 10; i++) {
            JsonObject activity = Json.createObject();
            activity.set("title", test[new Random().nextInt(4)] + i);
            activities.insert(i, activity);
          }
          msg.set("activities", activities);
          message.reply(msg);

        } else if (Constant.DATAREGISTRY_TYPE_CASE.equals(type)) {
          // 示范课
          // String grade = query.getString(Constant.GRADE);
          // String term = query.getString(Constant.TERM);
          // String topic = query.getString(Constant.TOPIC);
          JsonObject msg = Json.createObject();
          JsonArray activities = Json.createArray();
          for (int i = 0; i < 30; i++) {
            JsonObject activity = Json.createObject();
            activity.set("title", "找朋友=" + i);
            activities.insert(i, activity);
          }
          msg.set("activities", activities);
          message.reply(msg);
        } else if (Constant.DATAREGISTRY_TYPE_PREPARE.equals(type)) {
          // 入学准备
          JsonObject msg = Json.createObject();
          JsonArray activitys = Json.createArray();
          for (int i = 0; i < 30; i++) {
            JsonObject activity = Json.createObject();
            JsonObject tag = Json.createObject();
            tag.set(Constant.TERM, "上");
            tag.set(Constant.TOPIC, "健康");
            activity.set(Constant.TAGS, tag);
            activity.set(Constant.TITLE, "找朋友找朋友找朋友" + i);
            activitys.insert(i, activity);
          }
          msg.set("activities", activitys);
          message.reply(msg);
        } else if (Constant.DATAREGISTRY_TYPE_SMART.equals(type)) {
          // 智能开发
          JsonObject msg = Json.createObject();
          JsonArray activitys = Json.createArray();
          for (int i = 0; i < 30; i++) {
            JsonObject activity = Json.createObject();
            JsonObject tag = Json.createObject();
            tag.set(Constant.GRADE, "大班");
            tag.set(Constant.TERM, "上");
            tag.set(Constant.TOPIC, "健康");
            activity.set(Constant.TAGS, tag);
            activity.set(Constant.TITLE, "找朋友找朋友找朋友" + i);
            activitys.insert(i, activity);
          }
          msg.set("activities", activitys);
          message.reply(msg);
        } else if (Constant.DATAREGISTRY_TYPE_EBOOK.equals(type)) {
          // 图画书
          JsonObject msg = Json.createObject();
          JsonArray activitys = Json.createArray();
          for (int i = 0; i < 30; i++) {
            JsonObject activity = Json.createObject();
            JsonObject tag = Json.createObject();
            tag.set(Constant.TOPIC, "健康");
            activity.set(Constant.TAGS, tag);
            activity.set(Constant.TITLE, "找朋友找朋友找朋友" + i);
            activitys.insert(i, activity);
          }
          msg.set("activities", activitys);
          message.reply(msg);
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

  }
}
