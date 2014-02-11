package com.goodow.drive.android.data;

import com.goodow.drive.android.Constant;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;

/**
 * 数据库数据提供者
 * 
 * @author dpw
 * 
 */
public class DBDataProvider {

  /**
   * 清空数据库数据
   * 
   * @param context
   * @return
   * @status tested
   */
  public static boolean deleteAllData(Context context) {
    return DBOperator2.deleteAllTableData(context);
  }

  /**
   * 删除N个文件
   * 
   * @param context
   * @param tag
   * @return
   * @status tested
   */
  public static boolean deleteFiles(Context context, JsonArray tags) {
    return DBOperator2.deleteFilesByIds(context, tags);
  }

  /**
   * 删除N个收藏映射
   * 
   * @param context
   * @param stars
   * @return
   * @status tested
   */
  public static boolean deleteStarRelation(Context context, JsonArray stars) {
    return DBOperator2.deleteStarRelation(context, stars);
  }

  /**
   * 删除N个关系映射
   * 
   * @param context
   * @param tag
   * @return
   * @status tested
   */
  public static boolean deleteTagRelation(Context context, JsonArray tags) {
    return DBOperator2.deleteTagRelation(context, tags);
  }

  /**
   * 插入N个文件信息
   * 
   * @param context
   * @param tag
   * @return
   * @status tested
   */
  public static boolean insertFile(Context context, JsonArray attachments) {
    return DBOperator2.createFile(context, attachments);
  }

  /**
   * 插入一个文件信息
   * 
   * @param context
   * @param tag
   * @return
   * @status tested
   */
  public static boolean insertFile(Context context, JsonObject attachment) {
    return DBOperator2.createFile(context, Json.createArray().push(attachment));
  }

  /**
   * 插入一个收藏映射的信息
   * 
   * @param context
   * @param star
   * @return
   * @status tested
   */
  public static boolean insertStarRelation(Context context, JsonObject star) {
    return DBOperator2.createStarRelation(context, Json.createArray().push(star));
  }

  /**
   * 插入N个关系映射的信息
   * 
   * @param context
   * @param tag
   * @return
   * @status tested
   */
  public static boolean insertTagRelation(Context context, JsonArray tags) {
    return DBOperator2.createTagRelation(context, tags);
  }

  /**
   * 插入一个关系映射的信息
   * 
   * @param context
   * @param tag
   * @return
   * @status tested
   */
  public static boolean insertTagRelation(Context context, JsonObject tag) {
    return DBOperator2.createTagRelation(context, Json.createArray().push(tag));
  }

  /**
   * 根据文件的ID查询一个文件的详细信息
   * 
   * @param context
   * @param tag
   * @return
   * @status tested
   */
  public static JsonObject queryFileById(Context context, String id) {
    return DBOperator2.readFilesByIds(context, Json.createArray().push(id)).getObject(0);
  }

  /**
   * 查询一个收藏映射关系的信息
   * 
   * @param context
   * @param star
   * @return
   * @status tested
   */
  public static JsonObject queryStarInfo(Context context, JsonObject star) {
    if (DBOperator2.readStarRelation(context, Json.createArray().push(star)).length() > 0) {
      return DBOperator2.readStarRelation(context, Json.createArray().push(star)).getObject(0);
    }
    return null;
  }

  /**
   * 查询N个标签关系映射下的标签
   * 
   * @param context
   * @param tag
   * @return
   * @status tested
   */
  public static JsonArray querySubTagsInfo(Context context, JsonArray tags) {
    return DBOperator2.readSubTags(context, tags);
  }

  /**
   * 查询一个关系映射的详细信息
   * 
   * @param context
   * @param tag
   * @return
   * @status test useless
   */
  public static JsonObject queryTagInfo(Context context, JsonObject tag) {
    return DBOperator2.readTagInfo(context, tag);
  }

  /**
   * 查询收藏列表
   * 
   * @param context
   * @param type
   * @return
   * @status tested
   */
  public static JsonArray readStarByType(Context context, String type) {
    return DBOperator2.readStarByType(context, type);
  }

  /**
   * 根据文件的标签属性查询文件
   * 
   * @param key
   * @return
   */
  public static JsonObject searchFilesByKey(Context context, JsonObject key) {
    String sql = null;
    if ("全部".equals(key.getString(Constant.KEY_CONTENTTYPE))) {
      // 搜索-->“全部”标签下的文件
      int from = (int) key.getNumber(Constant.KEY_FROM);
      int size = (int) key.getNumber(Constant.KEY_SIZE);
      sql = "SELECT * FROM T_FILE ";
      if (key.getString(Constant.KEY_QUERY) != null) {
        sql = sql + "WHERE NAME LIKE '%" + key.getString(Constant.KEY_QUERY) + "%' ";
      }
      sql = sql + "limit " + size + " offset " + from;
      JsonObject attachment =
          Json.createObject().set(Constant.KEY_SIZE, DBOperator2.readFilesNum(context));
      attachment.set(Constant.KEY_ATTACHMENTS, DBOperator2.readFilesByKey(context, sql));
      return attachment;
    }

    JsonObject attachment =
        Json.createObject().set(Constant.KEY_SIZE, DBOperator2.readFilesNum(context));
    attachment.set(Constant.KEY_ATTACHMENTS, DBOperator2.readFilesByKey(context, key));
    return attachment;
  }
}