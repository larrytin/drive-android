package com.goodow.drive.android.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库以及表创建和更新
 * 
 * @author dpw
 * 
 */

public class DBHelper extends SQLiteOpenHelper {

  private static final String DBNAME = "keruixing";
  private static DBHelper dbHelper = null;

  public static DBHelper getInstance(Context context) {
    if (dbHelper == null) {
      synchronized (DBNAME) {
        if (dbHelper == null) {
          dbHelper = new DBHelper(context);
        }
      }
    }
    return dbHelper;
  }

  private DBHelper(Context context) {
    super(context, DBNAME, null, 1);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {

    db.execSQL("CREATE TABLE T_FAVOURITE(" + "id integer primary key autoincrement,"
        + "FAVOURITEID varchar(80)," + "TYPE varchar(100)," + "GRADE varchar(100),"
        + "TERM varchar(100)," + "TOPIC varchar(100)," + "TITLE varchar(100),"
        + "CREATETIME varchar(20)," + "UPDATETIME varchar(20))");
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }

}