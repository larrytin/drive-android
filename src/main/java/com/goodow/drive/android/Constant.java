package com.goodow.drive.android;

public interface Constant {

  String ADDR_CONTROL = BusProvider.SID + "control";
  String ADDR_PREFIX_VIEW = BusProvider.SID + "view.";
  String ADDR_TOPIC = BusProvider.SID + "topic";
  String ADDR_PLAYER = BusProvider.SID + "player";
  String ADDR_ACTIVITY = BusProvider.SID + "activity";
  String ADDR_FILE = BusProvider.SID + "file";

  String ADDR_DB_CLEAN = BusProvider.SID + "db.clean";
  String ADDR_TAG = BusProvider.SID + "tag";
  String ADDR_TAG_CHILDREN = ADDR_TAG + ".children";
  String ADDR_TAG_STAR = BusProvider.SID + "star";
  String ADDR_TAG_ATTACHMENT = BusProvider.SID + "attachment";
  String ADDR_TAG_ATTACHMENT_SEARCH = ADDR_TAG_ATTACHMENT + ".search";

  String TYPE = "type"; // 功能模块的KEY
  String GRADE = "grade"; // 年级的KEY
  String TERM = "term"; // 学期的KEY
  String TOPIC = "topic"; // 主题的KEY
  String TITLE = "title"; // 活动ITEM显示名称的KEY
  String QUERIES = "queries"; // 活动QUERIES的KEY

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

  // sd卡
  String STORAGE_DIR = "/mnt/sdcard/";

  String FILE_FORMAT_PDF = "pdf";
  String FILE_FORMAT_SWF = "swf";
  String FILE_FORMAT_MP3 = "mp3";
  String FILE_FORMAT_MP4 = "mp4";
  String FILE_FORMAT_IMAGE = "image";
  String FILE_NAME = "filename";
  String FILE_PATH = "path";

  String GRADE_LITTLE = "小";
  String GRADE_MID = "中";
  String GRADE_BIG = "大";
  String GRADE_PRE = "学前";
  String TERM_SEMESTER0 = "上";
  String TERM_SEMESTER1 = "下";
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
  String DOMIAN_QUALITY = "学习品质";

  String DATAREGISTRY_TYPE_HARMONY = "和谐";
  String DATAREGISTRY_TYPE_SHIP = "托班";
  String DATAREGISTRY_TYPE_CASE = "示范课";
  String DATAREGISTRY_TYPE_PREPARE = "入学准备";
  String DATAREGISTRY_TYPE_SMART = "智能开发";
  String DATAREGISTRY_TYPE_EBOOK = "电子书";
  String DATAREGISTRY_TYPE_FAVOURITE = "收藏";

  String DATA_PATH = "goodow/drive";// 数据存放位置
  String FONT_PATH = "fonts/font.ttf";// 字体的路径

}
