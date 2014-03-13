package com.goodow.drive.android.data;

import com.goodow.drive.android.Constant;
import com.goodow.drive.android.toolutils.AvaliStoragePathTools;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
    return DBOperator.deleteAllTableData(context);
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
    return DBOperator.deleteFilesByIds(context, tags);
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
    return DBOperator.deleteStarRelation(context, stars);
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
    return DBOperator.deleteTagRelation(context, tags);
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
    ArrayList<String> storageCard = AvaliStoragePathTools.getStorageCard(context);
    if (attachments == null || storageCard.size() == 0) {
      return false;
    }
    for (int i = 0; i < attachments.length(); ++i) {
      JsonObject object = attachments.get(i);
      String url = object.getString(Constant.KEY_URL);
      if (url.startsWith(Constant.VIR1_PATH)) {
        String replace = url.replace(Constant.VIR1_PATH, storageCard.get(0));
        object.set(Constant.KEY_URL, replace);
      } else if (url.startsWith(Constant.VIR2_PATH) && storageCard.size() == 2) {
        String replace = url.replace(Constant.VIR2_PATH, storageCard.get(1));
        object.set(Constant.KEY_URL, replace);
      }
    }
    return DBOperator.createFile(context, attachments);
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
    ArrayList<String> storageCard = AvaliStoragePathTools.getStorageCard(context);
    if (attachment == null || storageCard.size() == 0) {
      return false;
    }
    String url = attachment.getString(Constant.KEY_URL);
    if (url.startsWith(Constant.VIR1_PATH)) {
      String replace = url.replace(Constant.VIR1_PATH, storageCard.get(0));
      attachment.set(Constant.KEY_URL, replace);
    } else if (url.startsWith(Constant.VIR2_PATH) && storageCard.size() == 2) {
      String replace = url.replace(Constant.VIR2_PATH, storageCard.get(1));
      attachment.set(Constant.KEY_URL, replace);
    }
    return DBOperator.createFile(context, Json.createArray().push(attachment));
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
    return DBOperator.createStarRelation(context, Json.createArray().push(star));
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
    return DBOperator.createTagRelation(context, tags);
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
    return DBOperator.createTagRelation(context, Json.createArray().push(tag));
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
    return DBOperator.readFilesByIds(context, Json.createArray().push(id)).getObject(0);
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
    if (DBOperator.readStarRelation(context, Json.createArray().push(star)).length() > 0) {
      return DBOperator.readStarRelation(context, Json.createArray().push(star)).getObject(0);
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
    return DBOperator.readSubTags(context, tags);
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
    return DBOperator.readTagInfo(context, tag);
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
    return DBOperator.readStarByType(context, type);
  }

  /**
   * 根据文件的标签属性查询文件
   * 
   * @param key
   * @return
   */
  public static JsonObject searchFilesByKey(Context context, JsonObject key) {
    // 排序条件
    final List<String> catagories =
        Arrays.asList("活动设计", "文学作品", "说明文字", "背景知识", "乐谱", "图片", "动态图", "参考图", "挂图", "轮廓图", "头饰",
            "手偶", "胸牌", "动画", "电子书", "视频", "游戏", "音频", "音效");
    // 排序集合
    Set<JsonObject> set = new TreeSet<JsonObject>(new Comparator<JsonObject>() {
      @Override
      public int compare(JsonObject o1, JsonObject o2) {
        int temp =
            catagories.indexOf(o1.getString(Constant.KEY_CATAGORY))
                - catagories.indexOf(o2.getString(Constant.KEY_CATAGORY));
        return temp == 0 ? 1 : temp;
      }
    });
    // 查询语句
    String sql = null;
    String sqlOfCounter = null;
    // 主题查询
    if (key.getString(Constant.KEY_CONTENTTYPE) == null) {
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
      sql =
          "SELECT F.*,R.TAG AS TAG FROM T_RELATION R INNER join T_FILE F ON F.UUID = R.KEY AND F.UUID IN("
              + sqlBuilder.toString() + ")";
      // 查询页码
      sqlOfCounter =
          "SELECT COUNT(*) AS TOTAL_NUM FROM T_FILE F where UUID = IN(" + sqlBuilder.toString()
              + ")";
    }
    // 资源搜索
    if ("全部".equals(key.getString(Constant.KEY_CONTENTTYPE))) {
      // 搜索-->“全部”标签下的文件
      int from = (int) key.getNumber(Constant.KEY_FROM);
      int size = (int) key.getNumber(Constant.KEY_SIZE);
      sql = "SELECT UUID FROM T_FILE ";
      if (key.getString(Constant.KEY_QUERY) != null) {
        sql =
            sql + "WHERE NAME LIKE '%" + key.getString(Constant.KEY_QUERY)
                + "%' UNION SELECT KEY FROM T_RELATION WHERE TAG LIKE '%"
                + key.getString(Constant.KEY_QUERY) + "%'";
      }
      // 查询页码
      sqlOfCounter = "SELECT COUNT(*) AS TOTAL_NUM FROM T_FILE WHERE UUID IN(" + sql + ")";

      sql = sql + "LIMIT " + size + " OFFSET " + from;

      sql =
          "SELECT F.*,R.TAG AS TAG FROM T_RELATION R INNER join T_FILE F ON F.UUID = R.KEY AND F.UUID IN("
              + sql + ")";
    }

    if (key.getString(Constant.KEY_CONTENTTYPE) != null
        && !"全部".equals(key.getString(Constant.KEY_CONTENTTYPE))) {
      // 搜索-->其他标签下的文件
      int from = (int) key.getNumber(Constant.KEY_FROM);
      int size = (int) key.getNumber(Constant.KEY_SIZE);
      StringBuilder sqlBuilder = new StringBuilder();
      JsonArray tags = key.getArray(Constant.KEY_TAGS);// 取tags的交集
      int len_tags = tags == null ? 0 : tags.length();
      for (int i = 0; i < len_tags; i++) {
        sqlBuilder.append(
            "SELECT KEY FROM T_RELATION WHERE TAG = '" + tags.getString(i)
                + "' AND TYPE = 'attachment' ").append("UNION ");
      }
      sqlBuilder.delete(sqlBuilder.lastIndexOf("UNION ") >= 0 ? sqlBuilder.lastIndexOf("UNION ")
          : 0, sqlBuilder.length());
      if ("".equals(sqlBuilder.toString().trim())) {// 二级检索条件是null
        sql = "SELECT UUID FROM T_FILE WHERE CONTENTTYPE = ";
      } else {
        sql =
            "SELECT UUID FROM T_FILE WHERE UUID IN ("
                + sqlBuilder.toString()
                + " AND TAG LIKE '%"
                + (key.getString(Constant.KEY_QUERY) == null ? "" : key
                    .getString(Constant.KEY_QUERY)) + "%'" + ") AND CONTENTTYPE = ";
      }
      sql =
          sql
              + "'"
              + key.getString(Constant.KEY_CONTENTTYPE)
              + "' AND NAME LIKE '%"
              + (key.getString(Constant.KEY_QUERY) == null ? "" : key.getString(Constant.KEY_QUERY));

      // 查询页码
      sqlOfCounter = "SELECT COUNT(*) AS TOTAL_NUM FROM T_FILE WHERE UUID IN(" + sql + ")";

      sql = sql + "%' LIMIT " + size + " OFFSET " + from;

      sql =
          "SELECT F.*,R.TAG AS TAG FROM T_RELATION R INNER join T_FILE F ON F.UUID = R.KEY AND F.UUID IN("
              + sql + ")";

    }

    JsonArray noOrderJsonArray = DBOperator.readFilesBySql(context, sql, null);
    for (int i = 0; i < noOrderJsonArray.length(); i++) {
      JsonObject object = noOrderJsonArray.getObject(i);
      if (catagories.contains(object.getString(Constant.KEY_CATAGORY))) {
        set.add(object);
      }
    }
    JsonArray orderJsonArray = Json.createArray();
    Iterator<JsonObject> iterator = set.iterator();
    while (iterator.hasNext()) {
      orderJsonArray.push(iterator.next());
    }

    JsonObject attachment =
        Json.createObject().set(Constant.KEY_SIZE, DBOperator.readFilesNum(context, sqlOfCounter));
    attachment.set(Constant.KEY_ATTACHMENTS, orderJsonArray);
    return attachment;
  }
}