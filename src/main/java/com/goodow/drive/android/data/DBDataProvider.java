package com.goodow.drive.android.data;

import com.goodow.drive.android.Constant;
import com.goodow.drive.android.toolutils.AvaliStoragePathTools;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

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
    if (attachments == null) {
      return false;
    }
    for (int i = 0; i < attachments.length(); ++i) {
      JsonObject object = attachments.getObject(i);
      if (!AvaliStoragePathTools.replacePath(object)) {
        return false;
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
    if (attachment == null) {
      return false;
    }
    if (!AvaliStoragePathTools.replacePath(attachment)) {
      return false;
    }
    return DBOperator.createFile(context, Json.createArray().push(attachment));
  }

  /**
   * 插入N个文件信息
   * 
   * @param context
   * @param sqls
   * @return
   * @status tested
   */
  public static boolean insertFileBySql(Context context, JsonArray sqls) {
    if (sqls == null) {
      return false;
    }
    return DBOperator.createFileInfoBySql(context, sqls);
  }

  /**
   * 插入N个文件信息以及其标签对应关系
   * 
   * @param context
   * @param tag
   * @return
   * @status tested
   */
  public static boolean insertFileInfo(Context context, JsonArray attachments) {
    if (attachments == null) {
      return false;
    }
    int len = attachments.length();
    JsonArray attas = Json.createArray();
    JsonArray tags = Json.createArray();
    for (int i = 0; i < len; i++) {
      JsonObject attachment = attachments.getObject(i);
      String id = attachment.getString("_id");
      String title = attachment.getString("title");
      String contentType = attachment.getString("contentType");
      double contentLength = attachment.getNumber("contentLength");
      String url = attachment.getString("url");
      String thumbnail = attachment.getString("thumbnail");
      // 创建文件
      JsonObject atta =
          Json.createObject().set(Constant.KEY_ID, id).set(Constant.KEY_NAME, title).set(
              Constant.KEY_CONTENTTYPE, contentType).set(Constant.KEY_CONTENTLENGTH, contentLength)
              .set(Constant.KEY_URL, url).set(Constant.KEY_THUMBNAIL, thumbnail);
      attas.push(atta);

      JsonArray array = attachment.getArray("tags");
      // 创建文件和标签的对应
      for (int j = 0; j < array.length(); j++) {
        String tag = array.getString(j).trim();
        if (tag == null) {
          // 如果当前列是NULL该文件就不和该标签建立任何关系
          continue;
        }
        if (j == 8) {
          String[] splits = tag.split(",");
          for (String split : splits) {
            if (split == null || split.trim().equals("")) {
              // 忽略无效关键字
              continue;
            }
            tags.push(Json.createObject().set(Constant.KEY_TYPE, "attachment").set(
                Constant.KEY_KEY, id).set(Constant.KEY_LABEL, split.trim()));
          }
        } else {
          if (tag.matches("^\\d{4}.*")) {
            tag = tag.substring(4, tag.length());
          }
          tags.push(Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY,
              id).set(Constant.KEY_LABEL, tag));
        }
      }

      // 创建活动标签和主题|班级|学期|领域标签的对应
      String acitvity = array.getString(5).trim();
      for (int j = 1; j < 5; j++) {
        String tag = array.getString(j).trim();
        if (tag != null) {
          if (acitvity.matches("^\\d{4}.*")) {
            acitvity = acitvity.substring(4, acitvity.length());
          }
          tags.push(Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY,
              acitvity).set(Constant.KEY_LABEL, tag));
        }
      }

      // 创建搜索标签对应
      if (array.length() > 6 && array.getString(6) != null && array.getString(7) != null) {
        tags.push(Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY,
            array.getString(7).trim()).set(Constant.KEY_LABEL, array.getString(6).trim()));
      }
    }

    return DBOperator.createFile(context, attas) && DBOperator.createTagRelation(context, tags);
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

  public static JsonObject queryFilesByTagName(Context context, JsonObject key) {
    JsonArray tags = key.getArray(Constant.KEY_TAGS);
    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder.append("SELECT F.UUID AS UUID,F.FILEPATH AS FILEPATH FROM T_FILE F,(");
    int len_tags = tags.length();
    String[] params = new String[len_tags];
    for (int i = 0; i < len_tags; i++) {
      String tag = tags.getString(i);
      sqlBuilder.append("SELECT KEY FROM T_RELATION WHERE TAG = ? AND TYPE = 'attachment' ");
      if (i != len_tags - 1) {
        sqlBuilder.append("INTERSECT ");
      }
      params[i] = tag;
    }
    sqlBuilder.append(") T WHERE F.UUID = T.KEY");
    JsonArray jsonArray =
        DBOperator.readFilesByTagNameWithSql(context, sqlBuilder.toString(), params);
    JsonObject result = Json.createObject().set(Constant.KEY_ATTACHMENTS, jsonArray);
    return result;
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
   * 查询N个标签关系映射下的标签及其对应的文件,入学准备用
   * 
   * @param context
   * @param key
   * @return
   */
  public static JsonObject querySubTagsAndAttachments(Context context, JsonObject key) {
    JsonArray tags = key.getArray(Constant.KEY_TAGS);
    StringBuilder sqlBuilder = new StringBuilder();
    sqlBuilder
        .append("SELECT F.UUID AS UUID,F.FILEPATH AS FILEPATH,R.TAG AS TAG FROM T_FILE F,(SELECT R.KEY,T.* FROM T_RELATION R,(");
    int len_tags = tags.length();
    String[] params = new String[2 * len_tags];
    for (int i = 0; i < len_tags; i++) {
      String tag = tags.getString(i);
      sqlBuilder.append("SELECT KEY AS TAG FROM T_RELATION WHERE TAG = ? AND TYPE = 'tag' ");
      if (i != len_tags - 1) {
        sqlBuilder.append("INTERSECT ");
      }
      params[i] = tag;
    }
    sqlBuilder.append(") T,(");
    for (int i = 0; i < len_tags; i++) {
      String tag = tags.getString(i);
      sqlBuilder.append("SELECT KEY FROM T_RELATION WHERE TAG = ? AND TYPE = 'attachment' ");
      if (i != len_tags - 1) {
        sqlBuilder.append("INTERSECT ");
      }
      params[i + len_tags] = tag;
    }
    sqlBuilder
        .append(") C WHERE R.TAG = T.TAG AND R.TYPE = 'attachment' AND R.KEY = C.KEY) R WHERE F.UUID = R.KEY");

    JsonArray noOrderJsonArray =
        DBOperator.readSubTagsAndAttachmentsBySql(context, sqlBuilder.toString(), params);
    JsonObject result = Json.createObject().set(Constant.KEY_COUNT, noOrderJsonArray.length());
    result.set(Constant.KEY_TAGS, noOrderJsonArray);
    return result;
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
    // 排序条件
    final List<String> sortList =
        Arrays.asList("健康", "托班", "语言", "社会", "数学", "科学", "艺术(美术)", "艺术(音乐)", "教学图片", "参考图", "挂图",
            "轮廓图", "头饰", "手偶", "胸牌", "文学作品动画", "音乐作品动画", "数学教学动画", "其他动画", "文学作品动画", "音乐作品动画",
            "数学教学动画", "其他动画", "教学用视频", "教学示范课", "音乐表演视频", "音乐作品音频", "文学作品音频", "音效");
    // 排序集合
    Set<String> set = new TreeSet<String>(new Comparator<String>() {
      @Override
      public int compare(String str1, String str2) {
        int temp = sortList.indexOf(str1) - sortList.indexOf(str2);
        return temp == 0 ? 1 : temp;
      }
    });
    JsonArray noOrderJsonArray = DBOperator.readSubTags(context, tags);
    for (int i = 0; i < noOrderJsonArray.length(); ++i) {
      String str = noOrderJsonArray.getString(i);
      set.add(str);
    }
    JsonArray orderJsonArray = Json.createArray();
    Iterator<String> iterator = set.iterator();
    while (iterator.hasNext()) {
      orderJsonArray.push(iterator.next());
    }
    return orderJsonArray;
  }

  /**
   * 分页查询N个标签关系映射下的标签
   * 
   * @param context
   * @param object
   * @return
   * @status tested
   */
  public static JsonObject querySubTagsInfoBySql(Context context, JsonObject key) {
    // 排序条件
    final List<String> sortList =
        Arrays.asList("健康", "托班", "语言", "社会", "数学", "科学", "艺术(美术)", "艺术(音乐)", "教学图片", "参考图", "挂图",
            "轮廓图", "头饰", "手偶", "胸牌", "文学作品动画", "音乐作品动画", "数学教学动画", "其他动画", "文学作品动画", "音乐作品动画",
            "数学教学动画", "其他动画", "教学用视频", "教学示范课", "音乐表演视频", "音乐作品音频", "文学作品音频", "音效");
    // 排序集合
    Set<String> set = new TreeSet<String>(new Comparator<String>() {
      @Override
      public int compare(String str1, String str2) {
        int temp = sortList.indexOf(str1) - sortList.indexOf(str2);
        return temp == 0 ? 1 : temp;
      }
    });

    int from = 0;
    if (key.has(Constant.KEY_FROM)) {
      from = (int) key.getNumber(Constant.KEY_FROM);
    }
    int size = 10;
    if (key.has(Constant.KEY_SIZE)) {
      size = (int) key.getNumber(Constant.KEY_SIZE);
    }
    JsonArray tags = key.getArray(Constant.KEY_TAGS);
    StringBuilder sqlBuilder = new StringBuilder();
    int len_tags = tags.length();
    String[] params = new String[len_tags];
    for (int i = 0; i < len_tags; i++) {
      String tag = tags.getString(i);
      sqlBuilder.append("SELECT KEY FROM T_RELATION WHERE TAG = ? AND TYPE = 'tag' ").append(
          "INTERSECT ");
      params[i] = tag;
    }
    sqlBuilder.delete(sqlBuilder.lastIndexOf("INTERSECT ") >= 0 ? sqlBuilder
        .lastIndexOf("INTERSECT ") : 0, sqlBuilder.length());

    String sql = sqlBuilder.toString();

    // 查询页码
    String sqlOfCounter = "SELECT COUNT(*) AS TOTAL_NUM FROM (" + sql + ")";

    // 分页
    sql = "select KEY from (" + sql + ") LIMIT " + size + " OFFSET " + from;

    JsonArray noOrderJsonArray = DBOperator.readSubTagsBySql(context, sql, params);
    for (int i = 0; i < noOrderJsonArray.length(); ++i) {
      String str = noOrderJsonArray.getString(i);
      set.add(str);
    }
    JsonArray orderJsonArray = Json.createArray();
    Iterator<String> iterator = set.iterator();
    while (iterator.hasNext()) {
      orderJsonArray.push(iterator.next());
    }

    JsonObject result =
        Json.createObject().set(Constant.KEY_COUNT,
            DBOperator.readFilesNum(context, sqlOfCounter, params));
    result.set(Constant.KEY_TAGS, orderJsonArray);

    return result;
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
   * 分页查询收藏列表
   * 
   * @param context
   * @param key
   * @return
   * @status tested
   */
  public static JsonObject readStarByTypeByKey(Context context, JsonObject key) {
    String type = Constant.KEY_TAG;
    if (key.has(Constant.KEY_TYPE)) {
      type = key.getString(Constant.KEY_TYPE);
    }
    int from = 0;
    if (key.has(Constant.KEY_FROM)) {
      from = (int) key.getNumber(Constant.KEY_FROM);
    }
    int size = 10;
    if (key.has(Constant.KEY_SIZE)) {
      size = (int) key.getNumber(Constant.KEY_SIZE);
    }

    String sql = "SELECT * FROM T_STAR WHERE TYPE = ? LIMIT " + size + " OFFSET " + from;
    if (type.equals("attachment")) {
      sql =
          "SELECT * FROM T_FILE WHERE UUID IN ( SELECT TAG FROM T_STAR WHERE TYPE = ? ) LIMIT "
              + size + " OFFSET " + from;
    }

    String sqlOfCounter = "SELECT COUNT(*) AS TOTAL_NUM FROM T_STAR WHERE TYPE = '" + type + "'";
    JsonObject result =
        Json.createObject().set(Constant.KEY_COUNT,
            DBOperator.readFilesNum(context, sqlOfCounter, null));
    if (Constant.KEY_TAG.equals(type)) {
      result.set(Constant.KEY_TAGS, DBOperator.readStarByTypeBySql(context, type, sql));
    } else {
      result.set(Constant.KEY_ATTACHMENTS, DBOperator.readStarByTypeBySql(context, type, sql));
    }
    return result;
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
        Arrays.asList("素材-活动设计", "素材-文学作品", "素材-说明文字", "素材-背景知识", "素材-乐谱", "素材-动画", "素材-电子书", "素材-视频","素材-教学图片", "素材-动态图",
            "素材-参考图", "素材-挂图", "素材-轮廓图", "素材-头饰", "素材-手偶", "素材-胸牌",
            "素材-音频", "素材-音效");
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
    int from = 0;
    if (key.has(Constant.KEY_FROM)) {
      from = (int) key.getNumber(Constant.KEY_FROM);
    }
    int size = 10;
    if (key.has(Constant.KEY_SIZE)) {
      size = (int) key.getNumber(Constant.KEY_SIZE);
    }
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
          "SELECT F.*,R.TAG AS TAG FROM T_RELATION R INNER join T_FILE F ON F.UUID = R.KEY AND R.TAG IN ('素材-活动设计','素材-文学作品','素材-说明文字','素材-背景知识','素材-乐谱','素材-教学图片','素材-动态图','素材-参考图','素材-挂图','素材-轮廓图','素材-头饰','素材-手偶','素材-胸牌','素材-动画','素材-电子书','素材-视频','素材-音频','素材-音效') AND F.UUID IN("
              + sqlBuilder.toString() + ")";
      // 查询页码
      sqlOfCounter =
          "SELECT COUNT(*) AS TOTAL_NUM FROM T_FILE F where UUID IN(" + sqlBuilder.toString() + ")";

      // 分页
      sql = sql + " LIMIT " + size + " OFFSET " + from;
    }
    // 资源搜索
    if ("全部".equals(key.getString(Constant.KEY_CONTENTTYPE))) {
      // 搜索-->“全部”标签下的文件
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
          "SELECT F.*,R.TAG AS TAG FROM T_RELATION R INNER join T_FILE F ON F.UUID = R.KEY AND R.TAG IN ('素材-活动设计','素材-文学作品','素材-说明文字','素材-背景知识','素材-乐谱','素材-教学图片','素材-动态图','素材-参考图','素材-挂图','素材-轮廓图','素材-头饰','素材-手偶','素材-胸牌','素材-动画','素材-电子书','素材-视频','素材-音频','素材-音效') AND F.UUID IN("
              + sql + ")";
    }

    if (key.getString(Constant.KEY_CONTENTTYPE) != null
        && !"全部".equals(key.getString(Constant.KEY_CONTENTTYPE))) {
      // 搜索-->其他标签下的文件
      StringBuilder sqlBuilder = new StringBuilder();
      JsonArray tags = key.getArray(Constant.KEY_TAGS);// 取tags的交集
      int len_tags = tags == null ? 0 : tags.length();
      String query =
          key.getString(Constant.KEY_QUERY) == null ? "" : key.getString(Constant.KEY_QUERY);
      // 符合条件的文件ID
      sqlBuilder
          .append("SELECT F.*,R.TAG AS TAG FROM T_RELATION R INNER join T_FILE F ON F.UUID = R.KEY AND R.TAG IN ('素材-活动设计','素材-文学作品','素材-说明文字','素材-背景知识','素材-乐谱','素材-教学图片','素材-动态图','素材-参考图','素材-挂图','素材-轮廓图','素材-头饰','素材-手偶','素材-胸牌','素材-动画','素材-电子书','素材-视频','素材-音频','素材-音效') AND F.UUID IN(SELECT F.UUID FROM T_FILE F JOIN T_RELATION R ON F.UUID = R.KEY AND F.CONTENTTYPE = '");
      sqlBuilder.append(key.getString(Constant.KEY_CONTENTTYPE));
      sqlBuilder.append("' AND R.TAG LIKE '%");
      sqlBuilder.append(query);
      sqlBuilder.append("%' AND F.UUID IN (");
      sqlBuilder.append("SELECT KEY FROM T_RELATION WHERE TAG = '");
      sqlBuilder.append(tags.getString(0)).append("' AND TYPE = 'attachment'");
      if (len_tags > 1) {// 有二级分类
        sqlBuilder.append(" INTERSECT SELECT KEY FROM T_RELATION WHERE TAG IN ");
        String substring = tags.toString().substring(tags.getString(0).length() + 4);
        sqlBuilder.append("(").append(substring).delete(sqlBuilder.length() - 1,
            sqlBuilder.length()).append(") AND TYPE = 'attachment'");
      }
      sqlBuilder
          .append(") GROUP BY F.UUID UNION SELECT F.UUID FROM T_FILE F JOIN T_RELATION R ON F.UUID = R.KEY AND F.CONTENTTYPE = '");
      sqlBuilder.append(key.getString(Constant.KEY_CONTENTTYPE));
      sqlBuilder.append("' AND F.NAME LIKE '%");
      sqlBuilder.append(query);
      sqlBuilder.append("%' AND F.UUID IN (");
      sqlBuilder.append("SELECT KEY FROM T_RELATION WHERE TAG = '");
      sqlBuilder.append(tags.getString(0)).append("' AND TYPE = 'attachment'");
      if (len_tags > 1) {// 有二级分类
        sqlBuilder.append(" INTERSECT SELECT KEY FROM T_RELATION WHERE TAG IN ");
        String substring = tags.toString().substring(tags.getString(0).length() + 4);
        sqlBuilder.append("(").append(substring).delete(sqlBuilder.length() - 1,
            sqlBuilder.length()).append(") AND TYPE = 'attachment'");
      }
      sqlBuilder.append(") GROUP BY F.UUID");
      // 查询页码
      sqlOfCounter = "SELECT COUNT(*) AS TOTAL_NUM FROM (" + sqlBuilder + "))";
      sqlBuilder.append(" LIMIT ").append(size).append(" OFFSET ").append(from);// 分页
      sqlBuilder.append(")");
      sql = sqlBuilder.toString();
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
        Json.createObject().set(Constant.KEY_COUNT,
            DBOperator.readFilesNum(context, sqlOfCounter, null));
    attachment.set(Constant.KEY_ATTACHMENTS, orderJsonArray);
    return attachment;
  }
}
