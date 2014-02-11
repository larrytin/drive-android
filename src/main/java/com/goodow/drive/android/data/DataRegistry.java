package com.goodow.drive.android.data;

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

  private Context context = null;
  private final Bus bus;

  public DataRegistry(Bus bus, Context context) {
    this.bus = bus;
    this.context = context;
  }

  public void subscribe() {
    // 标签映射的增删改查
    bus.registerHandler(Constant.ADDR_TAG, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if ("get".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 查询标签明细 (未使用)
          // JsonObject tag = body.getObject(Constant.KEY_TAG);
          // message.reply(DBDataProvider.queryTagInfo(context, tag));
        } else if ("post".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 添加戓修改
          JsonObject tag = body.getObject(Constant.KEY_TAG);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.insertTagRelation(context, tag)) {
            msg.set(Constant.KEY_STATUS, "ok");
          }
          message.reply(msg);
        } else if ("delete".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 删除
          JsonArray tags = body.getArray(Constant.KEY_TAGS);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.deleteTagRelation(context, tags)) {
            msg.set(Constant.KEY_STATUS, "ok");
          }
          message.reply(msg);
        }
      }
    });
    // 查询同时属于N标签的子标签
    bus.registerHandler(Constant.ADDR_TAG_CHILDREN, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonArray tags = message.body().getArray(Constant.KEY_TAGS);
        message.reply(DBDataProvider.querySubTagsInfo(context, tags));
      }
    });
    // 收藏的增删改查
    bus.registerHandler(Constant.ADDR_TAG_STAR, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if ("get".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 查询收藏明细
          JsonObject star = body.getObject(Constant.KEY_STAR);
          message.reply(DBDataProvider.queryStarInfo(context, star));
        } else if ("post".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 添加戓修改
          JsonObject star = body.getObject(Constant.KEY_STAR);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.insertStarRelation(context, star)) {
            msg.set(Constant.KEY_STATUS, "ok");
          }
          message.reply(msg);
        } else if ("delete".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 删除收藏
          JsonArray stars = body.getArray(Constant.KEY_STARS);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.deleteStarRelation(context, stars)) {
            msg.set(Constant.KEY_STATUS, "ok");
          }
          message.reply(msg);
        }
      }
    });
    // 查询收藏列表
    bus.registerHandler(Constant.ADDR_TAG_STAR_SEARCH, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        message.reply(DBDataProvider.readStarByType(context, body.getString(Constant.KEY_TYPE)));
      }
    });
    // 数据库批量测试数据
    bus.registerHandler(Constant.ADDR_DB, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString(Constant.KEY_ACTION);
        if ("delete".equals(action)) {
          // 清空数据库数据
          if (DBDataProvider.deleteAllData(context)) {
            message.reply(Json.createObject().set(Constant.KEY_STATUS, "ok"));
          }
        } else if ("put".equals(action)) {
          String tableName = body.getString("table");
          if ("T_FILE".equals(tableName)) {
            // 向文件表中插入数据
            if (DBDataProvider.insertFile(context, body.getArray("data"))) {
              message.reply(Json.createObject().set(Constant.KEY_STATUS, "ok"));
            }
          } else if ("T_RELATION".equals(tableName)) {
            // 向标签映射表中插入数据
            if (DBDataProvider.insertTagRelation(context, body.getArray("data"))) {
              message.reply(Json.createObject().set(Constant.KEY_STATUS, "ok"));
            }
          }
        }
      }
    });
    // 文件的增删改查询
    bus.registerHandler(Constant.ADDR_TAG_ATTACHMENT, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if ("get".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 查询收藏明细
          String id = body.getString(Constant.KEY_ID);
          message.reply(DBDataProvider.queryFileById(context, id));
        } else if ("post".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 添加戓修改
          JsonObject attachment = body.getObject(Constant.KEY_ATTACHMENT);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.insertFile(context, attachment)) {
            msg.set(Constant.KEY_STATUS, "ok");
          }
          message.reply(msg);
        } else if ("delete".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 删除文件
          JsonArray ids = body.getArray(Constant.KEY_IDS);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.deleteFiles(context, ids)) {
            msg.set(Constant.KEY_STATUS, "ok");
          }
          message.reply(msg);
        }
      }
    });

    // 文件的搜索
    bus.registerHandler(Constant.ADDR_TAG_ATTACHMENT_SEARCH, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject key = message.body();
        message.reply(DBDataProvider.searchFilesByKey(context, key));
      }
    });

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
          bus.send(Bus.LOCAL + Bus.LOCAL + Constant.ADDR_TOPIC, body, null);
          return;
        }

        // 仅处理get动作
        if (!"get".equalsIgnoreCase(body.getString("action"))) {
          return;
        }
        // 解析查询条件
        if (!body.has(Constant.QUERIES)) {
          return;
        }
        JsonObject queries = body.getObject(Constant.QUERIES);
        String type = queries.getString(Constant.TYPE);// 解析请求的模块类型
        if (Constant.DATAREGISTRY_TYPE_HARMONY.equals(type) // 和谐
            || Constant.DATAREGISTRY_TYPE_SHIP.equals(type)// 托班
            || Constant.DATAREGISTRY_TYPE_CASE.equals(type)// 示范课
            || Constant.DATAREGISTRY_TYPE_PREPARE.equals(type)// 入学准备
            || Constant.DATAREGISTRY_TYPE_SMART.equals(type)// 智能开发
            || Constant.DATAREGISTRY_TYPE_EBOOK.equals(type)) {// 电子书
          JsonObject msg = Json.createObject();
          msg.set("activities", DataProvider.getInstance().getActivities(queries));
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
