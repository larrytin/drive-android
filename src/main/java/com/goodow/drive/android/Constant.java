package com.goodow.drive.android;

public interface Constant {

  String ADDR_CONTROL = BusProvider.SID + "control";
  String ADDR_PREFIX_VIEW = BusProvider.SID + "view.";
  String ADDR_TOPIC = BusProvider.SID + "topic";
  String ADDR_PLAYER = BusProvider.SID + "player";
  String ADDR_VIEW_CONTROL = ADDR_PREFIX_VIEW + "control";

  String TYPE = "type"; // 功能模块的KEY
  String GRADE = "grade"; // 年级的KEY
  String TERM = "term"; // 学期的KEY
  String TOPIC = "topic"; // 主题的KEY
  String TITLE = "title"; // 活动ITEM显示名称的KEY
  String TAGS = "tags"; // 活动TAGS的KEY

  String STORAGE_DIR = "/mnt/sdcard/";

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
  String DOMIAN_MUSIC = "艺术（音乐）";
  String DOMIAN_ART = "艺术（美术）";
}
