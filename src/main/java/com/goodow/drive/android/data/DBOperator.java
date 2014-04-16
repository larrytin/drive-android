package com.goodow.drive.android.data;

import com.goodow.drive.android.Constant;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 对数据库的操作
 * 
 * @author dpw
 * 
 */

public class DBOperator {

  /**
   * 添加开机数据
   * 
   * @param context
   * @param tableName
   * @param openTime
   * @param lastTime
   * @param closeTime
   * @param jsonObject
   * @return
   */
  public static boolean addBootData(Context context, String tableName, String openTime,
      String lastTime, String closeTime, JsonObject jsonObject) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(openTime, jsonObject.getNumber(openTime));
    values.put(lastTime, jsonObject.getNumber(lastTime));
    values.put(closeTime, jsonObject.getNumber(closeTime));
    long rawid = db.insert(tableName, null, values);
    db.close();
    if (rawid > 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 
   * @param context
   * @param tableName 表名
   * @param openTime 字段名
   * @param lastTime
   * @param jsonObject
   * @return
   */
  public static boolean addUserData(Context context, String tableName, String fileName,
      String openTime, String lastTime, JsonObject jsonObject) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(openTime, jsonObject.getNumber(openTime));
    values.put(lastTime, jsonObject.getNumber(lastTime));
    values.put(fileName, jsonObject.getString(fileName));
    long rawid = db.insert(tableName, null, values);
    db.close();
    if (rawid > 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 创建N个文件
   * 
   * @param context
   * @param attachments
   * @return
   * @status tested
   */
  public static boolean createFile(Context context, JsonArray attachments) {
    String sql =
        "REPLACE INTO T_FILE(UUID,NAME,CONTENTTYPE,SIZE,FILEPATH,THUMBNAILS)VALUES(?,?,?,?,?,?)";
    boolean result = false;
    int len = attachments.length();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      for (int i = 0; i < len; i++) {
        JsonObject attachment = attachments.getObject(i);
        db.execSQL(sql, new String[] {
            attachment.getString(Constant.KEY_ID), attachment.getString(Constant.KEY_NAME),
            attachment.getString(Constant.KEY_CONTENTTYPE),
            attachment.getNumber(Constant.KEY_CONTENTLENGTH) + "",
            attachment.getString(Constant.KEY_URL), attachment.getString(Constant.KEY_THUMBNAIL)});
      }
      db.setTransactionSuccessful();
      result = true;
    } catch (Exception e) {
      result = false;
    } finally {
      db.endTransaction();
      db.close();
    }
    return result;
  }

  /**
   * 创建N个收藏的映射关系
   * 
   * @param context
   * @param stars
   * @return
   * @status tested
   */
  public static boolean createStarRelation(Context context, JsonArray stars) {
    String sql = "REPLACE INTO T_STAR(TYPE,TAG)VALUES(?,?)";
    boolean result = false;
    int len = stars.length();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      for (int i = 0; i < len; i++) {
        JsonObject star = stars.getObject(i);
        db.execSQL(sql, new String[] {
            star.getString(Constant.KEY_TYPE), star.getString(Constant.KEY_KEY)});
      }
      db.setTransactionSuccessful();
      result = true;
    } catch (Exception e) {
      result = false;
    } finally {
      db.endTransaction();
      db.close();
    }
    return result;
  }

  /**
   * 创建一个标签的映射关系
   * 
   * @param context
   * @param tags
   * @return
   * @status tested
   */
  public static boolean createTagRelation(Context context, JsonArray tags) {
    String sql = "REPLACE INTO T_RELATION(TYPE,KEY,TAG)VALUES(?,?,?)";
    boolean result = false;
    int len = tags.length();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      for (int i = 0; i < len; i++) {
        JsonObject tag = tags.getObject(i);
        db.execSQL(sql, new String[] {
            tag.getString(Constant.KEY_TYPE), tag.getString(Constant.KEY_KEY),
            tag.getString(Constant.KEY_LABEL)});
      }
      db.setTransactionSuccessful();
      result = true;
    } catch (Exception e) {
      e.printStackTrace();
      result = false;
    } finally {
      db.endTransaction();
      db.close();
    }
    return result;
  }

  /**
   * 清空数据库表中的数据
   * 
   * @param context
   * @return
   */
  public static boolean deleteAllTableData(Context context) {
    String[] tables = {"T_STAR", "T_FILE", "T_RELATION"};
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    boolean result = false;
    try {
      db.beginTransaction();
      for (int i = 0; i < tables.length; i++) {
        db.delete(tables[i], null, null);
      }
      db.setTransactionSuccessful();
      result = true;
    } catch (Exception e) {
    } finally {
      db.endTransaction();
      db.close();
    }
    return result;
  }

  /**
   * 删除N个标签的映射关系
   * 
   * @param context
   * @param ids
   * @return
   * @status tested
   */
  public static boolean deleteFilesByIds(Context context, JsonArray ids) {
    String sql = "UUID = ? ";
    boolean result = false;
    int len = ids.length();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      int rows = 0;
      for (int i = 0; i < len; i++) {
        rows = rows + db.delete("T_FILE", sql, new String[] {ids.getString(i)});
      }
      if (rows == len) {
        db.setTransactionSuccessful();
        result = true;
      }
    } catch (Exception e) {
      result = false;
    } finally {
      db.endTransaction();
      db.close();
    }
    return result;
  }

  /**
   * 删除N个收藏的映射关系
   * 
   * @param context
   * @param stars
   * @return
   * @status tested
   */
  public static boolean deleteStarRelation(Context context, JsonArray stars) {
    String sql = "TYPE = ? AND TAG = ?";
    boolean result = false;
    int len = stars.length();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      int rows = 0;
      for (int i = 0; i < len; i++) {
        JsonObject tag = stars.getObject(i);
        rows =
            rows
                + db.delete("T_STAR", sql, new String[] {
                    tag.getString(Constant.KEY_TYPE), tag.getString(Constant.KEY_KEY)});
      }
      if (rows == len) {
        db.setTransactionSuccessful();
        result = true;
      }
    } catch (Exception e) {
      result = false;
    } finally {
      db.endTransaction();
      db.close();
    }
    return result;
  }

  /**
   * 删除N个标签的映射关系
   * 
   * @param context
   * @param tags
   * @return
   * @status tested
   */
  public static boolean deleteTagRelation(Context context, JsonArray tags) {
    String sql = "TYPE = ? AND KEY = ? AND TAG = ?";
    boolean result = false;
    int len = tags.length();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      int rows = 0;
      for (int i = 0; i < len; i++) {
        JsonObject tag = tags.getObject(i);
        rows =
            rows
                + db.delete("T_RELATION", sql, new String[] {
                    tag.getString(Constant.KEY_TYPE), tag.getString(Constant.KEY_KEY),
                    tag.getString(Constant.KEY_LABEL)});
      }
      if (rows == len) {
        db.setTransactionSuccessful();
        result = true;
      }
    } catch (Exception e) {
      result = false;
    } finally {
      db.endTransaction();
      db.close();
    }
    return result;
  }

  /**
   * 删除用户数据
   * 
   * @param context
   * @param tableName
   * @param id
   * @return
   */
  public static boolean deleteUserData(Context context, String tableName, int id) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    int result = db.delete(tableName, id + "<=?", new String[] {id + ""});
    db.close();
    if (result > 0) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * 读取开关机器信息
   * 
   * @param context
   * @param tableName
   * @param openTime
   * @param lastTime
   * @return
   */
  public static JsonObject readBootData(Context context, String tableName, String openTime,
      String lastTime) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    JsonObject result = Json.createObject();
    JsonArray jsonArray = Json.createArray();
    String[] strings = new String[2];
    strings[0] = openTime;
    strings[1] = lastTime;
    Cursor cursorId = db.rawQuery("select max(id)  from " + tableName, null);
    int id = 0;
    if (cursorId.moveToNext()) {
      id = cursorId.getInt(0);
      result.set("id", id);
    }
    Cursor cursor = db.query(tableName, strings, "id<" + id, null, null, null, null);
    while (cursor.moveToNext()) {
      JsonObject timestamp = Json.createObject();
      long open = cursor.getLong(cursor.getColumnIndex(openTime));
      long last = cursor.getLong(cursor.getColumnIndex(lastTime));
      timestamp.set("openTime", open);
      timestamp.set("lastTime", last);
      // 过滤掉小于5分钟的数据
      // TODO：
      if (last > 5000) {
        jsonArray.push(timestamp);
      }
    }
    result.set("timestamp", jsonArray);
    return result;
  }

  /**
   * 根据提供的若干文件的ID查询详细信息的集合
   * 
   * @param context
   * @param ids
   * @return
   * @status tested
   */
  public static JsonArray readFilesByIds(Context context, JsonArray ids) {
    StringBuilder sqlBuilder = new StringBuilder();
    int len_ids = ids.length();
    String[] params = new String[len_ids];
    for (int i = 0; i < len_ids; i++) {
      sqlBuilder.append("SELECT * FROM T_FILE WHERE UUID = ? ").append("JOIN ");
      params[i] = ids.getString(i);
    }
    sqlBuilder.delete(sqlBuilder.lastIndexOf("JOIN ") >= 0 ? sqlBuilder.lastIndexOf("JOIN ") : 0,
        sqlBuilder.length());
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    JsonArray files = Json.createArray();
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sqlBuilder.toString(), params);
      while (cursor.moveToNext()) {
        JsonObject file = Json.createObject();
        file.set(Constant.KEY_ID, cursor.getString(cursor.getColumnIndex("UUID")));
        file.set(Constant.KEY_NAME, cursor.getString(cursor.getColumnIndex("NAME")));
        file.set(Constant.KEY_CONTENTTYPE, cursor.getString(cursor.getColumnIndex("CONTENTTYPE")));
        file.set(Constant.KEY_CONTENTLENGTH, cursor.getInt(cursor.getColumnIndex("SIZE")));
        file.set(Constant.KEY_URL, cursor.getString(cursor.getColumnIndex("FILEPATH")));
        file.set(Constant.KEY_THUMBNAIL, cursor.getString(cursor.getColumnIndex("THUMBNAILS")));
        files.push(file);
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
    } finally {
      db.endTransaction();
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return files;
  }

  /**
   * 根据文件的标签属性查询文件
   * 
   * @param context
   * @param sql
   * @return
   * @status checking
   */
  public static JsonArray readFilesBySql(Context context, String sql, String[] params) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    JsonArray files = Json.createArray();
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sql, params);
      while (cursor.moveToNext()) {
        JsonObject file = Json.createObject();
        file.set(Constant.KEY_ID, cursor.getString(cursor.getColumnIndex("UUID")));
        file.set(Constant.KEY_NAME, cursor.getString(cursor.getColumnIndex("NAME")));
        file.set(Constant.KEY_CONTENTTYPE, cursor.getString(cursor.getColumnIndex("CONTENTTYPE")));
        file.set(Constant.KEY_CONTENTLENGTH, cursor.getInt(cursor.getColumnIndex("SIZE")));
        file.set(Constant.KEY_URL, cursor.getString(cursor.getColumnIndex("FILEPATH")));
        file.set(Constant.KEY_THUMBNAIL, cursor.getString(cursor.getColumnIndex("THUMBNAILS")));
        file.set(Constant.KEY_CATAGORY, cursor.getString(cursor.getColumnIndex("TAG")));
        files.push(file);
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
    } finally {
      db.endTransaction();
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return files;
  }

  /**
   * 用于异步查询tag及其对应的文件
   * 
   * @param context
   * @param sql
   * @param params
   * @return
   */
  public static JsonArray readFilesByTagNameWithSql(Context context, String sql, String[] params) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    JsonArray files = Json.createArray();
    try {
      db.setLockingEnabled(false);
      cursor = db.rawQuery(sql, params);
      while (cursor.moveToNext()) {
        JsonObject file = Json.createObject();
        file.set(Constant.KEY_ID, cursor.getString(cursor.getColumnIndex("UUID")));
        file.set(Constant.KEY_URL, cursor.getString(cursor.getColumnIndex("FILEPATH")));
        files.push(file);
      }
    } catch (Exception e) {
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return files;
  }

  /**
   * 查询文件总数量
   * 
   * @param context
   * @param sql
   * @return
   */
  public static int readFilesNum(Context context, String sql, String[] params) {
    int result = 0;
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sql, params);
      if (cursor.moveToNext()) {
        result = cursor.getInt((cursor.getColumnIndex("TOTAL_NUM")));
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
    } finally {
      db.endTransaction();
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return result;
  }

  /**
   * 根据提供的类型查询收藏信息的集合
   * 
   * @param context
   * @param type
   * @return TAGS或ATTACHMENTS
   * @status tested
   */
  public static JsonArray readStarByType(Context context, String type) {
    String sql = "SELECT * FROM T_STAR WHERE TYPE = ? ";
    if (type.equals("attachment")) {
      sql = "SELECT * FROM T_FILE WHERE UUID IN ( SELECT TAG FROM T_STAR WHERE TYPE = ? )";
    }
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    JsonArray result = Json.createArray();
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sql, new String[] {type});
      if (type.equals("attachment")) {
        while (cursor.moveToNext()) {
          JsonObject file = Json.createObject();
          file.set(Constant.KEY_ID, cursor.getString(cursor.getColumnIndex("UUID")));
          file.set(Constant.KEY_NAME, cursor.getString(cursor.getColumnIndex("NAME")));
          file.set(Constant.KEY_CONTENTTYPE, cursor.getString(cursor.getColumnIndex("CONTENTTYPE")));
          file.set(Constant.KEY_CONTENTLENGTH, cursor.getInt(cursor.getColumnIndex("SIZE")));
          file.set(Constant.KEY_URL, cursor.getString(cursor.getColumnIndex("FILEPATH")));
          file.set(Constant.KEY_THUMBNAIL, cursor.getString(cursor.getColumnIndex("THUMBNAILS")));
          result.push(file);
        }
      } else {
        while (cursor.moveToNext()) {
          result.push(Json.createObject().set(Constant.KEY_TYPE,
              cursor.getString(cursor.getColumnIndex("TYPE"))).set(Constant.KEY_TAG,
              cursor.getString(cursor.getColumnIndex("TAG"))));
        }
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
    } finally {
      db.endTransaction();
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return result;
  }

  /**
   * 根据提供的类型查询收藏信息的集合
   * 
   * @param context
   * @param type
   * @return TAGS或ATTACHMENTS
   * @status tested
   */
  public static JsonArray readStarByTypeBySql(Context context, String type, String sql) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    JsonArray result = Json.createArray();
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sql, new String[] {type});
      if (type.equals("attachment")) {
        while (cursor.moveToNext()) {
          JsonObject file = Json.createObject();
          file.set(Constant.KEY_ID, cursor.getString(cursor.getColumnIndex("UUID")));
          file.set(Constant.KEY_NAME, cursor.getString(cursor.getColumnIndex("NAME")));
          file.set(Constant.KEY_CONTENTTYPE, cursor.getString(cursor.getColumnIndex("CONTENTTYPE")));
          file.set(Constant.KEY_CONTENTLENGTH, cursor.getInt(cursor.getColumnIndex("SIZE")));
          file.set(Constant.KEY_URL, cursor.getString(cursor.getColumnIndex("FILEPATH")));
          file.set(Constant.KEY_THUMBNAIL, cursor.getString(cursor.getColumnIndex("THUMBNAILS")));
          result.push(file);
        }
      } else {
        while (cursor.moveToNext()) {
          result.push(Json.createObject().set(Constant.KEY_TYPE,
              cursor.getString(cursor.getColumnIndex("TYPE"))).set(Constant.KEY_TAG,
              cursor.getString(cursor.getColumnIndex("TAG"))));
        }
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
    } finally {
      db.endTransaction();
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return result;
  }

  /**
   * 根据提供的若干收藏信息查询详细信息的集合
   * 
   * @param context
   * @param stars
   * @return
   * @status tested
   */
  public static JsonArray readStarRelation(Context context, JsonArray stars) {
    StringBuilder sqlBuilder = new StringBuilder();
    int len_stars = stars.length();
    String[] params = new String[len_stars * 2];
    for (int i = 0; i < len_stars; i++) {
      JsonObject star = stars.getObject(i);
      sqlBuilder.append("SELECT * FROM T_STAR WHERE TYPE = ? AND TAG = ?").append("JOIN ");
      params[i * 2] = star.getString(Constant.KEY_TYPE);
      params[i * 2 + 1] = star.getString(Constant.KEY_KEY);
    }
    sqlBuilder.delete(sqlBuilder.lastIndexOf("JOIN ") >= 0 ? sqlBuilder.lastIndexOf("JOIN ") : 0,
        sqlBuilder.length());
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    JsonArray result = Json.createArray();
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sqlBuilder.toString(), params);
      while (cursor.moveToNext()) {
        result.push(Json.createObject().set(Constant.KEY_TYPE,
            cursor.getString(cursor.getColumnIndex("TYPE"))).set(Constant.KEY_TAG,
            cursor.getString(cursor.getColumnIndex("TAG"))));
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
    } finally {
      db.endTransaction();
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return result;
  }

  /**
   * 根据提供的若干标签查询子标签
   * 
   * @param context
   * @param tags
   * @return
   * @status tested
   */
  public static JsonArray readSubTags(Context context, JsonArray tags) {
    StringBuilder sqlBuilder = new StringBuilder();
    int len_tags = tags.length();
    String[] params = new String[len_tags];
    for (int i = 0; i < len_tags; i++) {
      String tag = tags.getString(i);
      sqlBuilder.append("SELECT KEY FROM T_RELATION WHERE TAG = ? AND TYPE = 'tag'").append(
          "INTERSECT ");
      params[i] = tag;
    }
    sqlBuilder.delete(sqlBuilder.lastIndexOf("INTERSECT ") >= 0 ? sqlBuilder
        .lastIndexOf("INTERSECT ") : 0, sqlBuilder.length());
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    JsonArray result = Json.createArray();
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sqlBuilder.toString(), params);
      while (cursor.moveToNext()) {
        result.push(cursor.getString(cursor.getColumnIndex("KEY")));
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
    } finally {
      db.endTransaction();
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return result;
  }

  /**
   * 根据提供的若干标签查询子标签及其文件
   * 
   * @param context
   * @param sql
   * @param params
   * @return
   */
  public static JsonArray readSubTagsAndAttachmentsBySql(Context context, String sql,
      String[] params) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    JsonArray result = Json.createArray();
    try {
      cursor = db.rawQuery(sql, params);
      while (cursor.moveToNext()) {
        JsonObject file = Json.createObject();
        file.set(Constant.KEY_ID, cursor.getString(cursor.getColumnIndex("UUID")));
        file.set(Constant.KEY_URL, cursor.getString(cursor.getColumnIndex("FILEPATH")));
        file.set(Constant.KEY_TAG, cursor.getString(cursor.getColumnIndex("TAG")));
        result.push(file);
      }
    } catch (Exception e) {
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return result;
  }

  public static JsonArray readSubTagsBySql(Context context, String sql, String[] params) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    JsonArray result = Json.createArray();
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sql, params);
      while (cursor.moveToNext()) {
        result.push(cursor.getString(cursor.getColumnIndex("KEY")));
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
    } finally {
      db.endTransaction();
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return result;
  }

  /**
   * 根据标签提供的部分信息查询详细信息
   * 
   * @param context
   * @param tag
   * @return
   * @status test useless
   */
  public static JsonObject readTagInfo(Context context, JsonObject tag) {
    String sql = "SELECT * FROM T_RELATION WHERE KEY = ?";
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sql, new String[] {tag.getString(Constant.KEY_KEY)});
      if (cursor.moveToNext()) {
        tag.set(Constant.KEY_TYPE, cursor.getString(cursor.getColumnIndex("TYPE")));
        tag.set(Constant.KEY_KEY, cursor.getString(cursor.getColumnIndex("KEY")));
        tag.set(Constant.KEY_LABEL, cursor.getString(cursor.getColumnIndex("TAG")));
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
    } finally {
      db.endTransaction();
      if (cursor != null) {
        cursor.close();
      }
      db.close();
    }
    return tag;
  }

  /**
   * 读取用户播放数据信息
   * 
   * @param context
   * @param tableName
   * @param fileName
   * @param openTime
   * @param lastTime
   * @return
   */
  public static JsonObject readUserPlayerData(Context context, String tableName, String fileName,
      String openTime, String lastTime) {
    JsonObject result = Json.createObject();
    JsonArray jsonArray = Json.createArray();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    String[] strings = new String[3];
    strings[0] = fileName;
    strings[1] = openTime;
    strings[2] = lastTime;
    Cursor cursorId = db.rawQuery("select max(id)  from " + tableName, null);
    if (cursorId.moveToNext()) {
      result.set("id", cursorId.getInt(0));
    }
    Cursor cursor = db.query(tableName, strings, null, null, null, null, null);
    while (cursor.moveToNext()) {
      JsonObject timestamp = Json.createObject();
      String name = cursor.getString(cursor.getColumnIndex(fileName));
      long open = cursor.getLong(cursor.getColumnIndex(openTime));
      long last = cursor.getLong(cursor.getColumnIndex(lastTime));
      timestamp.set("openTime", open);
      timestamp.set("lastTime", last);
      boolean tag = false;
      for (int i = 0; i < jsonArray.length(); i++) {
        if (jsonArray.getObject(i).getString("attachment").equals(name)) {
          tag = true;
          JsonArray tmpArray = jsonArray.getObject(i).getArray("timestamp");
          tmpArray.push(timestamp);
          jsonArray.getObject(i).set("timestamp", tmpArray);
          break;
        }
      }
      if (!tag) {
        JsonObject jsonObject = Json.createObject();
        jsonObject.set("attachment", name);
        jsonObject.set("timestamp", Json.createArray().push(timestamp));
        jsonArray.push(jsonObject);
      }
    }
    cursor.close();
    cursorId.close();
    db.close();
    result.set("analytics", jsonArray);
    return result;
  }

  /**
   * 更新开机信息
   * 
   * @param context
   * @param tableName
   * @param lastTime 持续时间
   * @param closeTime 当前时间
   * @param jsonObject
   */
  public static void updateBoot(Context context, String tableName, String lastTime,
      String closeTime, JsonObject jsonObject) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(lastTime, jsonObject.getNumber(lastTime));
    values.put(closeTime, jsonObject.getNumber(closeTime));
    Cursor cursorId = db.rawQuery("select max(id)  from " + tableName, null);
    int id = 0;
    if (cursorId.moveToNext()) {
      id = cursorId.getInt(0);
    }
    db.update(tableName, values, "id=?", new String[] {id + ""});
    cursorId.close();
    db.close();
  }
}