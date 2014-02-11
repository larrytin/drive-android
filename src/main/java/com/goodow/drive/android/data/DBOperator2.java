package com.goodow.drive.android.data;

import com.goodow.drive.android.Constant;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 对数据库的操作
 * 
 * @author dpw
 * 
 */

public class DBOperator2 {

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
        System.out.println(attachment);
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
   * @param key
   * @return
   */
  public static JsonArray readFilesByKey(Context context, JsonObject key) {
    String sql = null;
    String[] params = null;
    StringBuilder sqlBuilder = new StringBuilder();
    JsonArray tags = key.getArray(Constant.KEY_TAGS);// 取tags的并集
    int len_tags = tags == null ? 0 : tags.length();
    for (int i = 0; i < len_tags; i++) {
      sqlBuilder.append(
          "SELECT KEY FROM T_RELATION WHERE TAG = '" + tags.getString(i)
              + "' AND TYPE = 'attachment' ").append("INTERSECT ");
    }
    sqlBuilder.delete(sqlBuilder.lastIndexOf("INTERSECT ") >= 0 ? sqlBuilder
        .lastIndexOf("INTERSECT ") : 0, sqlBuilder.length());

    if (key.getString(Constant.KEY_QUERY) != null
        && key.getString(Constant.KEY_CONTENTTYPE) != null) {
      sqlBuilder.append(" SELECT KEY FROM T_RELATION WHERE TAG LIKE '%"
          + key.getString(Constant.KEY_QUERY) + "%' AND TYPE = 'attachment' ");
      sql =
          "SELECT * FROM T_FILE WHERE UUID IN (" + sqlBuilder.toString() + ") AND CONTENTTYPE = ?"
              + " AND NAME LIKE '%" + key.getString(Constant.KEY_QUERY) + "%'";
      params = new String[] {key.getString(Constant.KEY_CONTENTTYPE)};
    } else if (key.getString(Constant.KEY_QUERY) == null
        && key.getString(Constant.KEY_CONTENTTYPE) != null) {
      sql =
          "SELECT * FROM T_FILE WHERE UUID IN (" + sqlBuilder.toString() + ") AND CONTENTTYPE = ? ";
      params = new String[] {key.getString(Constant.KEY_CONTENTTYPE)};
    } else if (key.getString(Constant.KEY_QUERY) != null
        && key.getString(Constant.KEY_CONTENTTYPE) == null) {
      sqlBuilder.append(" SELECT KEY FROM T_RELATION WHERE TAG LIKE '%"
          + key.getString(Constant.KEY_QUERY) + "%' AND TYPE = 'attachment' ");
      sql =
          "SELECT * FROM T_FILE WHERE UUID IN (" + sqlBuilder.toString() + ")"
              + " AND NAME LIKE '%" + key.getString(Constant.KEY_QUERY) + "%'";
    } else if (key.getString(Constant.KEY_QUERY) == null
        && key.getString(Constant.KEY_CONTENTTYPE) == null) {
      sql = "SELECT * FROM T_FILE WHERE UUID IN (" + sqlBuilder.toString() + ")";
    }
    if (key.has(Constant.KEY_SIZE) && key.has(Constant.KEY_FROM)) {
      sql =
          sql + " limit " + (int) key.getNumber(Constant.KEY_SIZE) + " offset "
              + (int) key.getNumber(Constant.KEY_FROM);
    }
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
   * @param key
   * @return
   */
  public static JsonArray readFilesByKey(Context context, String sql) {
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    JsonArray files = Json.createArray();
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sql, null);
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
   * 查询文件总数量
   * 
   * @param context
   * @return
   */
  public static int readFilesNum(Context context) {
    int result = 0;
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    try {
      db.beginTransaction();
      cursor = db.rawQuery("SELECT COUNT(*) AS TOTAL_NUM FROM T_FILE", null);
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
}
