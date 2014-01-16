package com.goodow.drive.android;

public interface Constant {

  String ADDR_CONTROL = BusProvider.SID + "control";
  String ADDR_PREFIX_VIEW = BusProvider.SID + "view.";
  String ADDR_TOPIC = BusProvider.SID + "topic";
  String ADDR_PLAYER = BusProvider.SID + "player";
  String ADDR_VIEW_CONTROL = ADDR_PREFIX_VIEW + "control";
  String ADDR_ACTIVITY = BusProvider.SID + "activity";
  String ADDR_FILE = BusProvider.SID + "file";

  String TYPE = "type"; // 功能模块的KEY
  String GRADE = "grade"; // 年级的KEY
  String TERM = "term"; // 学期的KEY
  String TOPIC = "topic"; // 主题的KEY
  String TITLE = "title"; // 活动ITEM显示名称的KEY
  String TAGS = "tags"; // 活动TAGS的KEY

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

  String DOMIAN_ARITHMETIC = "算术";
  String DOMIAN_JIGSAW = "拼图";
  String DOMIAN_THINKING = "思维";
  String DOMIAN_READABLE = "识字";
  String DOMIAN_BODY = "形体";
  String DOMIAN_READ = "阅读";
  String DOMIAN_WRITE = "书写";
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
