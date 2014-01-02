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
   * 创建收藏
   * 
   * @param context
   * @param activities
   * @return 成功执行的数据量
   */
  public static int createFavourite(Context context, JsonArray activities) {
    int result = 0;
    int len = activities.length();
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      for (int i = 0; i < len; i++) {
        JsonObject activity = activities.getObject(i);
        JsonObject tags = activity.getObject(Constant.TAGS);
        ContentValues contentValues = new ContentValues();
        contentValues.put("FAVOURITEID", UUID.randomUUID().toString());
        contentValues.put("TYPE", tags.getString(Constant.TYPE));
        contentValues.put("GRADE", tags.getString(Constant.GRADE));
        contentValues.put("TERM", tags.getString(Constant.TERM));
        contentValues.put("TOPIC", tags.getString(Constant.TOPIC));
        contentValues.put("TITLE", activity.getString(Constant.TITLE));
        contentValues.put("CREATETIME", DeviceInformationTools.getDateTime());
        contentValues.put("UPDATETIME", DeviceInformationTools.getDateTime());
        db.insert("T_FAVOURITE", null, contentValues);
        result++;
      }
      db.setTransactionSuccessful();
    } catch (Exception e) {
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
  public static boolean deleteFavourite(Context context, JsonObject activity) {
    boolean result = false;
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      JsonObject tags = activity.getObject(Constant.TAGS);
      db.execSQL(
          "DELETE FROM T_FAVOURITE WHERE TYPE = ? and GRADE = ? AND TERM = ? AND TOPIC = ? AND TITLE = ?",
          new Object[] {
              tags.getString(Constant.TYPE), tags.getString(Constant.GRADE),
              tags.getString(Constant.TERM), tags.getString(Constant.TOPIC),
              activity.getString(Constant.TITLE)});
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
   * 是否已经收藏了该活动
   * 
   * @param context
   * @param favouriteId
   */
  public static boolean isHave(Context context, JsonObject activity) {
    boolean result = false;
    JsonObject tags = activity.getObject(Constant.TAGS);
    DBHelper dbOpenHelper = DBHelper.getInstance(context);
    SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
    try {
      db.beginTransaction();
      String sql =
          "SELECT COUNT(FAVOURITEID) AS ISHAVE FROM T_FAVOURITE WHERE TYPE = ? and GRADE = ? AND TERM = ? AND TOPIC = ? AND TITLE = ?";
      String[] params =
          new String[] {
              tags.getString(Constant.TYPE), tags.getString(Constant.GRADE),
              tags.getString(Constant.TERM), tags.getString(Constant.TOPIC),
              activity.getString(Constant.TITLE)};
      Cursor cursor = db.rawQuery(sql, params);
      if (cursor.getCount() > 0) {
        result = true;
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
      int index = 0;
      while (cursor.moveToNext()) {
        JsonObject activity = Json.createObject();
        JsonObject tag = Json.createObject();
        String type = cursor.getString(cursor.getColumnIndex("TYPE"));
        String grade = cursor.getString(cursor.getColumnIndex("GRADE"));
        String term = cursor.getString(cursor.getColumnIndex("TERM"));
        String topic = cursor.getString(cursor.getColumnIndex("TOPIC"));
        String title = cursor.getString(cursor.getColumnIndex("TITLE"));
        tag.set(Constant.TYPE, type);
        tag.set(Constant.GRADE, grade);
        tag.set(Constant.TERM, term);
        tag.set(Constant.TOPIC, topic);
        activity.set(Constant.TAGS, tag);
        activity.set(Constant.TITLE, title);
        activitys.insert(index, activity);
        index++;
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
}
