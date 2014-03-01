package com.goodow.drive.android.data;

import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;

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
            // 通知界面刷新
            bus.publish(Bus.LOCAL + Constant.ADDR_VIEW_REFRESH, null);
          }
          message.reply(msg);
        } else if ("delete".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 删除
          JsonArray tags = body.getArray(Constant.KEY_TAGS);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.deleteTagRelation(context, tags)) {
            msg.set(Constant.KEY_STATUS, "ok");
            // 通知界面刷新
            bus.publish(Bus.LOCAL + Constant.ADDR_VIEW_REFRESH, null);
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
            // 通知界面刷新
            bus.publish(Bus.LOCAL + Constant.ADDR_VIEW_REFRESH, null);
          }
          message.reply(msg);
        } else if ("delete".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 删除收藏
          JsonArray stars = body.getArray(Constant.KEY_STARS);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.deleteStarRelation(context, stars)) {
            msg.set(Constant.KEY_STATUS, "ok");
            // 通知界面刷新
            bus.publish(Bus.LOCAL + Constant.ADDR_VIEW_REFRESH, null);
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
            // 通知界面刷新
            bus.publish(Bus.LOCAL + Constant.ADDR_VIEW_REFRESH, null);
          }
          message.reply(msg);
        } else if ("delete".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 删除文件
          JsonArray ids = body.getArray(Constant.KEY_IDS);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.deleteFiles(context, ids)) {
            msg.set(Constant.KEY_STATUS, "ok");
            // 通知界面刷新
            bus.publish(Bus.LOCAL + Constant.ADDR_VIEW_REFRESH, null);
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
  }
}
