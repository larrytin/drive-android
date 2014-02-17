package com.goodow.drive.android.data;

import com.goodow.drive.android.Constant;
import com.goodow.drive.android.toolutils.DeviceInformationTools;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.UUID;

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
   * 创建类别
   * 
   * @param context
   * @param catagories
   * @return 执行结果
   */
  public static boolean createCatagories(Context context, JsonArray catagories) {
    boolean result = false;
    int len = catagories.length();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      for (int i = 0; i < len; i++) {
        JsonObject catagory = catagories.getObject(i);
        ContentValues contentValues = new ContentValues();
        contentValues.put("UUID", catagory.getString("id"));
        contentValues.put("PUUID", catagory.getString("parent"));
        contentValues.put("NAME", catagory.getString("name"));
        contentValues.put("DESCRIPTION", "description");
        contentValues.put("CREATETIME", DeviceInformationTools.getDateTime());
        contentValues.put("UPDATETIME", DeviceInformationTools.getDateTime());
        db.insert("T_CATAGORY", null, contentValues);
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
   * 创建收藏
   * 
   * @param context
   * @param activities
   * @return 成功执行的数据量
   */
  public static boolean createFavourite(Context context, JsonArray activities) {
    boolean result = false;
    int len = activities.length();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      for (int i = 0; i < len; i++) {
        JsonObject activity = activities.getObject(i);
        JsonObject queries = activity.getObject(Constant.QUERIES);
        ContentValues contentValues = new ContentValues();
        contentValues.put("FAVOURITEID", UUID.randomUUID().toString());
        contentValues.put("TYPE", queries.getString(Constant.TYPE));
        contentValues.put("GRADE", queries.getString(Constant.GRADE));
        contentValues.put("TERM", queries.getString(Constant.TERM));
        contentValues.put("TOPIC", queries.getString(Constant.TOPIC));
        contentValues.put("TITLE", activity.getString(Constant.TITLE));
        contentValues.put("CREATETIME", DeviceInformationTools.getDateTime());
        contentValues.put("UPDATETIME", DeviceInformationTools.getDateTime());
        db.insert("T_FAVOURITE", null, contentValues);
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
   * 清空数据库
   * 
   * @param context
   * @param favouriteId
   * @return 执行结果
   */
  public static boolean deleteAll(Context context) {
    boolean result = false;
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      db.execSQL("DELETE FROM T_FAVOURITE");
      db.setTransactionSuccessful();
      result = true;
      db.setTransactionSuccessful();
    } catch (Exception e) {
      result = false;
    } finally {
      db.endTransaction();
      db.close();
    }
    return result;
  }

  /**
   * 删除收藏
   * 
   * @param context
   * @param activity
   * @return 是否删除成功
   */
  public static boolean deleteFavourite(Context context, JsonArray activities) {
    boolean result = false;
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    int len = activities.length();
    try {
      db.beginTransaction();
      for (int i = 0; i < len; i++) {
        JsonObject activity = activities.get(i);
        JsonObject queries = activity.getObject(Constant.QUERIES);

        if (queries.getString(Constant.GRADE) == null) {
          db.execSQL(
              "DELETE FROM T_FAVOURITE WHERE TYPE = ? AND TERM = ? AND TOPIC = ? AND TITLE = ?",
              new Object[] {
                  queries.getString(Constant.TYPE), queries.getString(Constant.TERM),
                  queries.getString(Constant.TOPIC), activity.getString(Constant.TITLE)});
        } else {
          db.execSQL(
              "DELETE FROM T_FAVOURITE WHERE TYPE = ? and GRADE = ? AND TERM = ? AND TOPIC = ? AND TITLE = ?",
              new Object[] {
                  queries.getString(Constant.TYPE), queries.getString(Constant.GRADE),
                  queries.getString(Constant.TERM), queries.getString(Constant.TOPIC),
                  activity.getString(Constant.TITLE)});
        }
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
   * 是否已经收藏了该活动
   * 
   * @param context
   * @param favouriteId
   */
  public static boolean isHave(Context context, JsonObject activity) {
    boolean result = false;
    JsonObject queries = activity.getObject(Constant.QUERIES);
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      String sql = null;
      String[] params = null;
      if (queries.getString(Constant.GRADE) == null) {
        sql =
            "SELECT COUNT(FAVOURITEID) AS ISHAVE FROM T_FAVOURITE WHERE TYPE = ? AND TERM = ? AND TOPIC = ? AND TITLE = ?";
        params =
            new String[] {
                queries.getString(Constant.TYPE), queries.getString(Constant.TERM),
                queries.getString(Constant.TOPIC), activity.getString(Constant.TITLE)};
      } else {
        sql =
            "SELECT COUNT(FAVOURITEID) AS ISHAVE FROM T_FAVOURITE WHERE TYPE = ? and GRADE = ? AND TERM = ? AND TOPIC = ? AND TITLE = ?";
        params =
            new String[] {
                queries.getString(Constant.TYPE), queries.getString(Constant.GRADE),
                queries.getString(Constant.TERM), queries.getString(Constant.TOPIC),
                activity.getString(Constant.TITLE)};
      }
      Cursor cursor = db.rawQuery(sql, params);
      if (cursor.moveToNext()) {
        if (cursor.getInt(cursor.getColumnIndex("ISHAVE")) > 0) {
          result = true;
        }
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
      result = false;
    } finally {
      db.endTransaction();
      db.close();
    }
    return result;
  }

  /**
   * 读取所有收藏
   * 
   * @return
   */
  public static JsonArray readAllFavourite(Context context) {
    JsonArray activitys = Json.createArray();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    try {
      db.beginTransaction();
      cursor = db.rawQuery("SELECT * FROM T_FAVOURITE", null);
      while (cursor.moveToNext()) {
        JsonObject activity = Json.createObject();
        JsonObject queries = Json.createObject();
        String type = cursor.getString(cursor.getColumnIndex("TYPE"));
        String grade = cursor.getString(cursor.getColumnIndex("GRADE"));
        String term = cursor.getString(cursor.getColumnIndex("TERM"));
        String topic = cursor.getString(cursor.getColumnIndex("TOPIC"));
        String title = cursor.getString(cursor.getColumnIndex("TITLE"));
        queries.set(Constant.TYPE, type);
        queries.set(Constant.GRADE, grade);
        queries.set(Constant.TERM, term);
        queries.set(Constant.TOPIC, topic);
        activity.set(Constant.QUERIES, queries);
        activity.set(Constant.TITLE, title);
        activitys.push(activity);
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
    return activitys;
  }

  /**
   * 根据父ID查询其下的所有文件
   * 
   * @param context
   * @param catagory
   * @return
   */
  public static JsonArray readAllFileByParent(Context context, String parentId) {
    JsonArray files = Json.createArray();
    String sql =
        "SELECT UUID,FULLNAME,SHORTNAME,CONTENTTYPE,SIZE,FILEPATH,THUMBNAILS FROM T_FILE WHERE UUID IN (SELECT FILE_ID FROM T_CATAGORY_FILE WHERE CATAGORY_ID = '"
            + parentId + "')";
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sql, null);
      while (cursor.moveToNext()) {
        JsonObject file = Json.createObject();
        file.set("id", cursor.getString(cursor.getColumnIndex("UUID")));
        file.set("name", cursor.getString(cursor.getColumnIndex("FULLNAME")));
        file.set("title", cursor.getString(cursor.getColumnIndex("SHORTNAME")));
        file.set("contentType", cursor.getString(cursor.getColumnIndex("CONTENTTYPE")));
        file.set("contentLenght", cursor.getString(cursor.getColumnIndex("SIZE")));
        file.set("url", cursor.getString(cursor.getColumnIndex("FILEPATH")));
        file.set("thumbnail", cursor.getString(cursor.getColumnIndex("THUMBNAILS")));
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
   * 根据若干类别的名称查询其下的所有子类别的ID
   * 
   * @param context
   * @param catagory
   * @return
   */
  public static JsonArray readAllLastIdCatagroyByParent(Context context, JsonArray catagories) {
    if (catagories == null) {
      return null;
    }
    int len_files = catagories.length();
    if (len_files == 0) {
      return null;
    }
    String sql =
        "SELECT UUID FROM T_CATAGORY WHERE NAME = '" + catagories.getObject(0).getString("name")
            + "'";
    if (len_files > 1) {
      for (int i = 1; i < len_files; i++) {
        sql =
            "SELECT UUID FROM T_CATAGORY WHERE NAME = '"
                + catagories.getObject(i).getString("name") + "' AND PUUID IN (" + sql + ")";
      }
    }
    sql = "SELECT UUID,PUUID,NAME FROM T_CATAGORY WHERE PUUID IN (" + sql + ")";
    catagories.clear();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    Cursor cursor = null;
    try {
      db.beginTransaction();
      cursor = db.rawQuery(sql, null);
      while (cursor.moveToNext()) {
        JsonObject catagory = Json.createObject();
        catagory.set("id", cursor.getString(cursor.getColumnIndex("UUID")));
        catagory.set("parent", cursor.getString(cursor.getColumnIndex("PUUID")));
        catagory.set("name", cursor.getString(cursor.getColumnIndex("NAME")));
        catagories.push(catagory);
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
    return catagories;
  }
}
