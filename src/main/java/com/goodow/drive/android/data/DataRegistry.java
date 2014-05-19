package com.goodow.drive.android.data;

import com.goodow.drive.android.Constant;
import com.goodow.drive.android.toolutils.DeviceInformationTools;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.os.AsyncTask;

public class DataRegistry {

  private Context context = null;
  private final Bus bus;

  public DataRegistry(Bus bus, Context context) {
    this.bus = bus;
    this.context = context;
  }

  public void subscribe() {
    bus.registerHandler("drive." + DeviceInformationTools.getLocalMacAddressFromWifiInfo(context),
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(final Message<JsonObject> message) {
            JsonObject body = message.body();
            String path = body.getString("path");
            JsonObject msg = body.getObject("msg");
            bus.sendLocal(path, msg, new MessageHandler<JsonObject>() {
              @Override
              public void handle(Message<JsonObject> messageInner) {
                JsonObject bodyInner = messageInner.body();
                message.reply(bodyInner);
              }
            });
          }
        });
    // 标签映射的增删改查
    bus.registerLocalHandler(Constant.ADDR_TAG, new MessageHandler<JsonObject>() {
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
            bus.publishLocal(Constant.ADDR_VIEW_REFRESH, null);
          }
          message.reply(msg);
        } else if ("delete".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 删除
          JsonArray tags = body.getArray(Constant.KEY_TAGS);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.deleteTagRelation(context, tags)) {
            msg.set(Constant.KEY_STATUS, "ok");
            // 通知界面刷新
            bus.publishLocal(Constant.ADDR_VIEW_REFRESH, null);
          }
          message.reply(msg);
        }
      }
    });
    // 查询同时属于N标签的子标签及其文件
    bus.registerLocalHandler(Constant.ADDR_TAG_CHILDREN_ATTACHMENTS,
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(final Message<JsonObject> message) {
            new AsyncTask<Message<?>, Void, JsonObject>() {
              @Override
              protected JsonObject doInBackground(Message<?>... messages) {
                return DBDataProvider.querySubTagsAndAttachments(context, (JsonObject) messages[0]
                    .body());
              }

              @Override
              protected void onPostExecute(JsonObject result) {
                message.reply(result);// 分页查询接口
              };
            }.execute(message);
          }
        });
    // 查询同时属于N标签的子标签
    bus.registerLocalHandler(Constant.ADDR_TAG_CHILDREN, new MessageHandler<JsonObject>() {
      @Override
      public void handle(final Message<JsonObject> message) {
        new AsyncTask<Message<?>, Void, JsonObject>() {
          @Override
          protected JsonObject doInBackground(Message<?>... messages) {
            return DBDataProvider.querySubTagsInfoBySql(context, (JsonObject) messages[0].body());
          }

          @Override
          protected void onPostExecute(JsonObject result) {
            message.reply(result);// 分页查询接口
          };
        }.execute(message);
      }
    });
    // 收藏的增删改查
    bus.registerLocalHandler(Constant.ADDR_TAG_STAR, new MessageHandler<JsonObject>() {
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
            bus.publishLocal(Constant.ADDR_VIEW_REFRESH, null);
          }
          message.reply(msg);
        } else if ("delete".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 删除收藏
          JsonArray stars = body.getArray(Constant.KEY_STARS);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.deleteStarRelation(context, stars)) {
            msg.set(Constant.KEY_STATUS, "ok");
            // 通知界面刷新
            bus.publishLocal(Constant.ADDR_VIEW_REFRESH, null);
          }
          message.reply(msg);
        }
      }
    });
    // 查询收藏列表
    bus.registerLocalHandler(Constant.ADDR_TAG_STAR_SEARCH, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        message.reply(DBDataProvider.readStarByTypeByKey(context, body));// 分页查询接口
      }
    });
    // 数据库批量测试数据
    bus.registerLocalHandler(Constant.ADDR_DB, new MessageHandler<JsonObject>() {
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
          if (DBDataProvider.insertFileInfo(context, body.getArray("datas"))) {
            message.reply(Json.createObject().set(Constant.KEY_STATUS, "ok"));
          }
        }
      }
    });
    // 文件的增删改查询
    bus.registerLocalHandler(Constant.ADDR_TAG_ATTACHMENT, new MessageHandler<JsonObject>() {
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
            bus.publishLocal(Constant.ADDR_VIEW_REFRESH, null);
          }
          message.reply(msg);
        } else if ("delete".equalsIgnoreCase(body.getString(Constant.KEY_ACTION))) {
          // 删除文件
          JsonArray ids = body.getArray(Constant.KEY_IDS);
          JsonObject msg = Json.createObject();
          if (DBDataProvider.deleteFiles(context, ids)) {
            msg.set(Constant.KEY_STATUS, "ok");
            // 通知界面刷新
            bus.publishLocal(Constant.ADDR_VIEW_REFRESH, null);
          }
          message.reply(msg);
        }
      }
    });

    // 文件的搜索
    bus.registerLocalHandler(Constant.ADDR_TAG_ATTACHMENT_SEARCH, new MessageHandler<JsonObject>() {
      @Override
      public void handle(final Message<JsonObject> message) {
        new AsyncTask<Message<?>, Void, JsonObject>() {
          @Override
          protected JsonObject doInBackground(Message<?>... messages) {
            return DBDataProvider.searchFilesByKey(context, (JsonObject) messages[0].body());
          }

          @Override
          protected void onPostExecute(JsonObject result) {
            message.reply(result);// 分页查询接口
          };
        }.execute(message);
      }
    });
  }
}
