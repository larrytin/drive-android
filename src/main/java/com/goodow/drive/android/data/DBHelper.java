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

    /*
     * 创建收藏表
     */
    db.execSQL("CREATE TABLE T_FAVOURITE(" + "id integer primary key autoincrement,"
        + "FAVOURITEID varchar(80)," + "TYPE varchar(100)," + "GRADE varchar(100),"
        + "TERM varchar(100)," + "TOPIC varchar(100)," + "TITLE varchar(100),"
        + "CREATETIME varchar(20)," + "UPDATETIME varchar(20))");

    /*
     * 创建文件表
     */
    db.execSQL("CREATE TABLE T_FILE(UUID varchar(80) primary key NOT NULL ,"
        + "FULLNAME varchar(200) NOT NULL ,SHORTNAME varchar(50) NOT NULL ,"
        + "CONTENTTYPE varchar(300) DEFAULT NULL ,SIZE int(11) NOT NULL ,"
        + "FILEPATH varchar(500) NOT NULL ,THUMBNAILS varchar(500) DEFAULT NULL ,"
        + "CREATETIME varchar(45) DEFAULT NULL ,UPDATETIME varchar(45) DEFAULT NULL )");

    /*
     * 创建标签映射表
     */
    db.execSQL("CREATE TABLE T_RELATION(TYPE varchar(80) NOT NULL ,"
        + "KEY varchar(80) NOT NULL ,TAG varchar(80) NOT NULL ,"
        + "CREATETIME varchar(45) DEFAULT NULL ,UPDATETIME varchar(45) DEFAULT NULL, PRIMARY KEY (TYPE,KEY,TAG))");

    /*
     * 创建收藏映射表
     */
    db.execSQL("CREATE TABLE T_STAR(TYPE varchar(80) NOT NULL, "
        + "TAG varchar(500) NOT NULL ,USER_ID varchar(80) DEFAULT NULL ,"
        + "CREATETIME varchar(45) DEFAULT NULL ,UPDATETIME varchar(45) DEFAULT NULL, PRIMARY KEY (TYPE,TAG))");

  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }

}
