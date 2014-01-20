package com.goodow.drive.android.data;

import com.goodow.drive.android.Constant;
import com.goodow.drive.android.toolutils.AvaliStoragePathTools;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

public class DataProvider {
  // 过滤器,jpg,mp3,mp4,pdf,swf
  public class fileFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String file) {
      return file.toLowerCase().endsWith(".jpg") || file.toLowerCase().endsWith(".mp3")
          || file.toLowerCase().endsWith(".mp4") || file.toLowerCase().endsWith(".pdf")
          || file.toLowerCase().endsWith(".swf");
    }
  }

  private String storage_dir = Constant.STORAGE_DIR;
  private static DataProvider provider = new DataProvider();

  public static DataProvider getInstance() {
    return provider;
  }

  /**
   * 比较器,根据文件类型排序
   */
  private final Comparator<File> comparator = new Comparator<File>() {

    @Override
    public int compare(File lhs, File rhs) {
      String lhString = lhs.getName().substring(lhs.getName().indexOf(".") + 1);
      String rhString = rhs.getName().substring(rhs.getName().indexOf(".") + 1);
      return lhString.compareTo(rhString) != 0 ? lhString.compareTo(rhString) : 1;
    }
  };

  private DataProvider() {
    // 对sd卡的路径进行判断,如果外置sd卡/goodow/drive不存在，那么使用内置的sd卡的/goodow/drive（默认存在)
    ArrayList<String> storageCards = AvaliStoragePathTools.getStorageCard();
    for (int i = storageCards.size() - 1; i >= 0; i--) {
      if (new File(storageCards.get(i) + File.separator + Constant.DATA_PATH).exists()) {
        storage_dir = storageCards.get(i) + File.separator;
        break;
      }
    }
  }

  /**
   * 获取activities列表
   * 
   * @param queries 查询条件
   * @return activities列表
   */
  public JsonArray getActivities(JsonObject queries) {
    String type = queries.getString(Constant.TYPE);
    String term = queries.getString(Constant.TERM);
    String grade = queries.getString(Constant.GRADE);
    String topic = queries.getString(Constant.TOPIC);
    String path = getPath(type, grade, term, topic);
    File dir = new File(path);
    File[] files = dir.listFiles();
    JsonArray activities = Json.createArray();
    if (files == null) {
      return null;
    }
    for (int i = 0; i < files.length; i++) {
      if (files[i].isDirectory()) {
        JsonObject activity = Json.createObject();
        activity.set(Constant.TITLE, files[i].getName());
        activity.set(Constant.QUERIES, queries);
        activities.push(activity);
      }
    }
    return activities;
  }

  /**
   * 获取一个activity下的文件信息
   * 
   * @param activity 要查询的activity
   * @return 返回activity下文件信息
   */
  public JsonArray getFiles(JsonObject activity) {
    if (!activity.has(Constant.QUERIES)) {
      return null;
    }
    JsonObject queries = activity.getObject(Constant.QUERIES);
    String title = activity.getString(Constant.TITLE);
    String type = queries.getString(Constant.TYPE);
    String term = queries.getString(Constant.TERM);
    String grade = queries.getString(Constant.GRADE);
    String topic = queries.getString(Constant.TOPIC);
    String path = getPath(type, grade, term, topic, title);
    File mFile = new File(path);
    File[] files = mFile.listFiles(new fileFilter());
    JsonArray filesJsonArray = Json.createArray();
    if (files == null) {
      return null;
    }
    TreeSet<File> treeSet = new TreeSet<File>(comparator);
    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile()) {
        treeSet.add(files[i]);
      }
    }
    if (!treeSet.isEmpty()) {
      for (File file : treeSet) {
        JsonObject fileJsonObject = Json.createObject();
        fileJsonObject.set("filename", file.getName());
        fileJsonObject.set("path", file.getAbsolutePath());
        filesJsonArray.push(fileJsonObject);
      }
    }
    return filesJsonArray;
  }

  /**
   * 获取一个目录下的所有文件和子目录
   * 
   * @param path 目录路径
   * @return
   */
  public JsonObject getFoldersAndFiles(String path) {
    path = storage_dir + path;
    File file = new File(path);
    File[] files = file.listFiles();
    JsonObject body = Json.createObject();
    if (files == null) {
      return body;
    }
    JsonArray foldersArray = Json.createArray();
    JsonArray filesArray = Json.createArray();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile()) {
        filesArray.push(files[i].getName());
      }
      if (files[i].isDirectory()) {
        foldersArray.push(files[i].getName());
      }
    }
    if (foldersArray.length() > 0) {
      body.set("folders", foldersArray);
    }
    if (filesArray.length() > 0) {
      body.set("files", filesArray);
    }
    return body;
  }

  /**
   * 拼接字符串得到路径
   * 
   * @param queries
   * @return tags所标记相对路径转换为绝对路径
   * @author DPW
   */
  public String getPath(JsonObject queries) {
    return storage_dir + Constant.DATA_PATH + "/" + queries.getString(Constant.TYPE) + "/"
        + queries.getString(Constant.GRADE) + "/" + queries.getString(Constant.TERM) + "/"
        + queries.getString(Constant.TOPIC) + "/";
  }

  /**
   * 拼接字符串得到路径
   * 
   * @param str 要拼接的字符串
   * @return 返回拼接后的字符串
   */
  private String getPath(String... str) {
    StringBuilder path = new StringBuilder();
    path.append(storage_dir);
    path.append(Constant.DATA_PATH);
    if (null == str) {
      return null;
    }
    for (String string : str) {
      if (null != string) {
        path.append(File.separator);
        path.append(string);
      }
    }
    return path.toString();
  }

}
