package com.goodow.drive.android;

import java.util.Arrays;
import java.util.List;

public interface Constant {

  String ADDR_CONTROL = BusProvider.SID + "control";
  String ADDR_VIEW = BusProvider.SID + "view";
  String ADDR_VIEW_REFRESH = ADDR_VIEW + ".refresh";
  String ADDR_PREFIX_VIEW = BusProvider.SID + "view.";
  String ADDR_TOPIC = BusProvider.SID + "topic";
  String ADDR_PLAYER = BusProvider.SID + "player";
  String ADDR_ACTIVITY = BusProvider.SID + "activity";
  String ADDR_FILE = BusProvider.SID + "file";

  String ADDR_DB = BusProvider.SID + "db";
  String ADDR_TAG = BusProvider.SID + "tag";
  String ADDR_TAG_CHILDREN = ADDR_TAG + ".children";
  String ADDR_TAG_STAR = BusProvider.SID + "star";
  String ADDR_TAG_STAR_SEARCH = ADDR_TAG_STAR + ".search";
  String ADDR_TAG_ATTACHMENT = BusProvider.SID + "attachment";
  String ADDR_TAG_ATTACHMENT_SEARCH = ADDR_TAG_ATTACHMENT + ".search";

  String TYPE = "type"; // 功能模块的KEY
  String GRADE = "grade"; // 年级的KEY
  String TERM = "term"; // 学期的KEY
  String TOPIC = "topic"; // 主题的KEY
  String TITLE = "title"; // 活动ITEM显示名称的KEY
  String QUERIES = "queries"; // 活动QUERIES的KEY
  String CONTENTTYPE = "contentType"; // 搜索的类型
  String TAGS = "tags"; // 搜索的标签
  String QUERY = "query"; // 搜索的标签
  String FILES = "files"; // 搜索的标签
  String TYPEID = "typeId"; // 搜索类别的ID

  String KEY_ACTION = "action";
  String KEY_TYPE = "type";//
  String KEY_QUERY = "query";//
  String KEY_KEY = "key";//
  String KEY_STAR = "star";//
  String KEY_STARS = "stars";//
  String KEY_TAG = "tag";//
  String KEY_TAGS = "tags";//
  String KEY_LABEL = "label";//
  String KEY_STATUS = "status";//
  String KEY_ID = "id";//
  String KEY_IDS = "ids";//
  String KEY_TITLE = "title";//
  String KEY_NAME = "name";//
  String KEY_CONTENTTYPE = "contentType";//
  String KEY_CONTENTLENGTH = "contentLength";//
  String KEY_URL = "url";//
  String KEY_THUMBNAIL = "thumbnail";//
  String KEY_ATTACHMENT = "attachment";//
  String KEY_ATTACHMENTS = "attachments";//
  String KEY_FROM = "from";//
  String KEY_SIZE = "size";//
  String KEY_REDIRECTTO = "redirectTo";//

  // sd卡
  String STORAGE_DIR = "/mnt/sdcard/";

  String FILE_FORMAT_PDF = "pdf";
  String FILE_FORMAT_SWF = "swf";
  String FILE_FORMAT_MP3 = "mp3";
  String FILE_FORMAT_MP4 = "mp4";
  String FILE_FORMAT_IMAGE = "image";
  String FILE_NAME = "filename";
  String FILE_PATH = "path";

  String LABEL_GRADE_LITTLE = "小班";
  String LABEL_GRADE_MID = "中班";
  String LABEL_GRADE_BIG = "大班";
  String LABEL_GRADE_PRE = "学前班";
  String TERM_SEMESTER0 = "上";
  String TERM_SEMESTER1 = "下";
  String LABEL_TERM_SEMESTER0 = "上学期";
  String LABEL_TERM_SEMESTER1 = "下学期";
  String DOMIAN_HEALTH = "健康";
  String DOMIAN_LANGUAGE = "语言";
  String DOMIAN_WORLD = "社会";
  String DOMIAN_SCIENCE = "科学";
  String DOMIAN_MATH = "数学";
  String DOMIAN_MUSIC = "艺术(音乐)";
  String DOMIAN_ART = "艺术(美术)";
  String DOMIAN_FAIRYTALE = "冰波童话";
  String DOMIAN_HAPPY_BABY = "快乐宝贝";
  String DOMIAN_OTHER = "其它";

  String DOMIAN_ARITHMETIC = "算术";
  String DOMIAN_JIGSAW = "拼图";
  String DOMIAN_THINKING = "思维";
  String DOMIAN_READABLE = "识字";
  String DOMIAN_BODY = "形体";
  String DOMIAN_READ_WRITE = "阅读与书写";
  String DOMIAN_QUALITY = "习惯与学习品质";

  String DATAREGISTRY_TYPE_HARMONY = "和谐";
  String DATAREGISTRY_TYPE_SHIP = "托班";
  String DATAREGISTRY_TYPE_CASE = "示范课";
  String DATAREGISTRY_TYPE_PREPARE = "入学准备";
  String DATAREGISTRY_TYPE_EDUCATION = "安全教育";
  String DATAREGISTRY_TYPE_READ = "早期阅读";
  String DATAREGISTRY_TYPE_FAVOURITE = "收藏";
  String DATAREGISTRY_TYPE_SOURCE = "资源库";

  // 主题名称集合
  List<String> LABEL_THEMES = Arrays.asList(new String[] {
      DATAREGISTRY_TYPE_HARMONY, DATAREGISTRY_TYPE_SHIP, DATAREGISTRY_TYPE_CASE,
      DATAREGISTRY_TYPE_PREPARE, DATAREGISTRY_TYPE_EDUCATION, DATAREGISTRY_TYPE_READ,
      DATAREGISTRY_TYPE_FAVOURITE, DATAREGISTRY_TYPE_SOURCE});

  String DATA_PATH = "goodow/drive";// 数据存放位置
  String FONT_PATH = "fonts/font.ttf";// 字体的路径

  String VIR1_PATH = "attachments/sd1";// 模拟的sd1路径
  String VIR2_PATH = "attachments/sd2";// 模拟的sd2路径

}
