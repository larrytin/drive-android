package com.goodow.drive.android;

import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.ArrayList;
import java.util.UUID;

public class InitData {

  public static final JsonArray RELATION_TABLE_DATA = Json.createArray();
  public static final JsonArray FILE_TABLE_DATA = Json.createArray();

  private static final JsonArray RELATION_THEME = Json.createArray();
  private static final ArrayList<String[]> tagsList = new ArrayList<String[]>();

  private static final String[] tags00 = {"和谐", "小班", "上学期", "健康"};
  private static final String[] tags01 = {"和谐", "小班", "上学期", "语言"};
  private static final String[] tags02 = {"和谐", "小班", "上学期", "社会"};
  private static final String[] tags03 = {"和谐", "小班", "上学期", "科学"};
  private static final String[] tags04 = {"和谐", "小班", "上学期", "数学"};
  private static final String[] tags05 = {"和谐", "小班", "上学期", "艺术(音乐)"};
  private static final String[] tags06 = {"和谐", "小班", "上学期", "艺术(美术)"};

  private static final String[] tags07 = {"和谐", "小班", "下学期", "健康"};
  private static final String[] tags08 = {"和谐", "小班", "下学期", "语言"};
  private static final String[] tags09 = {"和谐", "小班", "下学期", "社会"};
  private static final String[] tags10 = {"和谐", "小班", "下学期", "科学"};
  private static final String[] tags11 = {"和谐", "小班", "下学期", "数学"};
  private static final String[] tags12 = {"和谐", "小班", "下学期", "艺术(音乐)"};
  private static final String[] tags13 = {"和谐", "小班", "下学期", "艺术(美术)"};

  private static final String[] tags14 = {"和谐", "中班", "上学期", "健康"};
  private static final String[] tags15 = {"和谐", "中班", "上学期", "语言"};
  private static final String[] tags16 = {"和谐", "中班", "上学期", "社会"};
  private static final String[] tags17 = {"和谐", "中班", "上学期", "科学"};
  private static final String[] tags18 = {"和谐", "中班", "上学期", "数学"};
  private static final String[] tags19 = {"和谐", "中班", "上学期", "艺术(音乐)"};
  private static final String[] tags20 = {"和谐", "中班", "上学期", "艺术(美术)"};

  private static final String[] tags21 = {"和谐", "中班", "下学期", "健康"};
  private static final String[] tags22 = {"和谐", "中班", "下学期", "语言"};
  private static final String[] tags23 = {"和谐", "中班", "下学期", "社会"};
  private static final String[] tags24 = {"和谐", "中班", "下学期", "科学"};
  private static final String[] tags25 = {"和谐", "中班", "下学期", "数学"};
  private static final String[] tags26 = {"和谐", "中班", "下学期", "艺术(音乐)"};
  private static final String[] tags27 = {"和谐", "中班", "下学期", "艺术(美术)"};

  private static final String[] tags28 = {"和谐", "大班", "上学期", "健康"};
  private static final String[] tags29 = {"和谐", "大班", "上学期", "语言"};
  private static final String[] tags30 = {"和谐", "大班", "上学期", "社会"};
  private static final String[] tags31 = {"和谐", "大班", "上学期", "科学"};
  private static final String[] tags32 = {"和谐", "大班", "上学期", "数学"};
  private static final String[] tags33 = {"和谐", "大班", "上学期", "艺术(音乐)"};
  private static final String[] tags34 = {"和谐", "大班", "上学期", "艺术(美术)"};

  private static final String[] tags35 = {"和谐", "大班", "下学期", "健康"};
  private static final String[] tags36 = {"和谐", "大班", "下学期", "语言"};
  private static final String[] tags37 = {"和谐", "大班", "下学期", "社会"};
  private static final String[] tags38 = {"和谐", "大班", "下学期", "科学"};
  private static final String[] tags39 = {"和谐", "大班", "下学期", "数学"};
  private static final String[] tags40 = {"和谐", "大班", "下学期", "艺术(音乐)"};
  private static final String[] tags41 = {"和谐", "大班", "下学期", "艺术(美术)"};

  private static final String[] tags42 = {"和谐", "学前班", "上学期", "健康"};
  private static final String[] tags43 = {"和谐", "学前班", "上学期", "语言"};
  private static final String[] tags44 = {"和谐", "学前班", "上学期", "社会"};
  private static final String[] tags45 = {"和谐", "学前班", "上学期", "科学"};
  private static final String[] tags46 = {"和谐", "学前班", "上学期", "数学"};
  private static final String[] tags47 = {"和谐", "学前班", "上学期", "艺术(音乐)"};
  private static final String[] tags48 = {"和谐", "学前班", "上学期", "艺术(美术)"};

  private static final String[] tags49 = {"和谐", "学前班", "下学期", "健康"};
  private static final String[] tags50 = {"和谐", "学前班", "下学期", "语言"};
  private static final String[] tags51 = {"和谐", "学前班", "下学期", "社会"};
  private static final String[] tags52 = {"和谐", "学前班", "下学期", "科学"};
  private static final String[] tags53 = {"和谐", "学前班", "下学期", "数学"};
  private static final String[] tags54 = {"和谐", "学前班", "下学期", "艺术(音乐)"};
  private static final String[] tags55 = {"和谐", "学前班", "下学期", "艺术(美术)"};

  private static final String[] tags0000 = {"示范课", "小班", "上学期", "健康"};
  private static final String[] tags0001 = {"示范课", "小班", "上学期", "语言"};
  private static final String[] tags0002 = {"示范课", "小班", "上学期", "社会"};
  private static final String[] tags0003 = {"示范课", "小班", "上学期", "科学"};
  private static final String[] tags0004 = {"示范课", "小班", "上学期", "数学"};
  private static final String[] tags0005 = {"示范课", "小班", "上学期", "艺术(音乐)"};
  private static final String[] tags0006 = {"示范课", "小班", "上学期", "艺术(美术)"};

  private static final String[] tags0007 = {"示范课", "小班", "下学期", "健康"};
  private static final String[] tags0008 = {"示范课", "小班", "下学期", "语言"};
  private static final String[] tags0009 = {"示范课", "小班", "下学期", "社会"};
  private static final String[] tags0010 = {"示范课", "小班", "下学期", "科学"};
  private static final String[] tags0011 = {"示范课", "小班", "下学期", "数学"};
  private static final String[] tags0012 = {"示范课", "小班", "下学期", "艺术(音乐)"};
  private static final String[] tags0013 = {"示范课", "小班", "下学期", "艺术(美术)"};

  private static final String[] tags0014 = {"示范课", "中班", "上学期", "健康"};
  private static final String[] tags0015 = {"示范课", "中班", "上学期", "语言"};
  private static final String[] tags0016 = {"示范课", "中班", "上学期", "社会"};
  private static final String[] tags0017 = {"示范课", "中班", "上学期", "科学"};
  private static final String[] tags0018 = {"示范课", "中班", "上学期", "数学"};
  private static final String[] tags0019 = {"示范课", "中班", "上学期", "艺术(音乐)"};
  private static final String[] tags0020 = {"示范课", "中班", "上学期", "艺术(美术)"};

  private static final String[] tags0021 = {"示范课", "中班", "下学期", "健康"};
  private static final String[] tags0022 = {"示范课", "中班", "下学期", "语言"};
  private static final String[] tags0023 = {"示范课", "中班", "下学期", "社会"};
  private static final String[] tags0024 = {"示范课", "中班", "下学期", "科学"};
  private static final String[] tags0025 = {"示范课", "中班", "下学期", "数学"};
  private static final String[] tags0026 = {"示范课", "中班", "下学期", "艺术(音乐)"};
  private static final String[] tags0027 = {"示范课", "中班", "下学期", "艺术(美术)"};

  private static final String[] tags0028 = {"示范课", "大班", "上学期", "健康"};
  private static final String[] tags0029 = {"示范课", "大班", "上学期", "语言"};
  private static final String[] tags0030 = {"示范课", "大班", "上学期", "社会"};
  private static final String[] tags0031 = {"示范课", "大班", "上学期", "科学"};
  private static final String[] tags0032 = {"示范课", "大班", "上学期", "数学"};
  private static final String[] tags0033 = {"示范课", "大班", "上学期", "艺术(音乐)"};
  private static final String[] tags0034 = {"示范课", "大班", "上学期", "艺术(美术)"};

  private static final String[] tags0035 = {"示范课", "大班", "下学期", "健康"};
  private static final String[] tags0036 = {"示范课", "大班", "下学期", "语言"};
  private static final String[] tags0037 = {"示范课", "大班", "下学期", "社会"};
  private static final String[] tags0038 = {"示范课", "大班", "下学期", "科学"};
  private static final String[] tags0039 = {"示范课", "大班", "下学期", "数学"};
  private static final String[] tags0040 = {"示范课", "大班", "下学期", "艺术(音乐)"};
  private static final String[] tags0041 = {"示范课", "大班", "下学期", "艺术(美术)"};

  private static final String[] tags0042 = {"示范课", "学前班", "上学期", "健康"};
  private static final String[] tags0043 = {"示范课", "学前班", "上学期", "语言"};
  private static final String[] tags0044 = {"示范课", "学前班", "上学期", "社会"};
  private static final String[] tags0045 = {"示范课", "学前班", "上学期", "科学"};
  private static final String[] tags0046 = {"示范课", "学前班", "上学期", "数学"};
  private static final String[] tags0047 = {"示范课", "学前班", "上学期", "艺术(音乐)"};
  private static final String[] tags0048 = {"示范课", "学前班", "上学期", "艺术(美术)"};

  private static final String[] tags0049 = {"示范课", "学前班", "下学期", "健康"};
  private static final String[] tags0050 = {"示范课", "学前班", "下学期", "语言"};
  private static final String[] tags0051 = {"示范课", "学前班", "下学期", "社会"};
  private static final String[] tags0052 = {"示范课", "学前班", "下学期", "科学"};
  private static final String[] tags0053 = {"示范课", "学前班", "下学期", "数学"};
  private static final String[] tags0054 = {"示范课", "学前班", "下学期", "艺术(音乐)"};
  private static final String[] tags0055 = {"示范课", "学前班", "下学期", "艺术(美术)"};

  // //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  private static final String[] SEARCH_ALL = {
      "健康", "语言", "社会", "科学", "数学", "艺术", "算数", "拼图", "思维", "识字", "形体", "阅读与书写", "学些品质", "冰波童话",
      "其他领域"};
  private static final String[] SEARCH_TEXT = {
      "艺术", "算数", "拼图", "思维", "识字", "形体", "阅读与书写", "学些品质", "冰波童话", "其他领域"};
  private static final String[] SEARCH_AUDIO = {
      "健康", "语言", "拼图", "思维", "识字", "形体", "阅读与书写", "学些品质", "冰波童话", "其他领域"};
  private static final String[] SEARCH_VIDEO = {
      "健康", "语言", "科学", "识字", "形体", "学些品质", "冰波童话", "其他领域"};
  private static final String[] SEARCH_IMAGE = {
      "健康", "语言", "社会", "科学", "数学", "艺术", "算数", "拼图", "冰波童话", "其他领域"};
  private static final String[] SEARCH_GAME = {
      "健康", "语言", "社会", "科学", "数学", "识字", "形体", "阅读与书写", "学些品质", "冰波童话", "其他领域"};
  private static final String[] SEARCH_BOOK = {"健康", "阅读与书写", "学些品质", "冰波童话", "其他领域"};
  private static final String[] SEARCH_ANIM = {"健康", "语言", "其他领域"};

  private static final JsonArray RELATION_SEARCH = Json.createArray();
  private static final JsonArray FILE_SEARCH = Json.createArray();

  static {
    tagsList.add(tags00);
    tagsList.add(tags01);
    tagsList.add(tags02);
    tagsList.add(tags03);
    tagsList.add(tags04);
    tagsList.add(tags05);
    tagsList.add(tags06);
    tagsList.add(tags07);
    tagsList.add(tags08);
    tagsList.add(tags09);

    tagsList.add(tags10);
    tagsList.add(tags11);
    tagsList.add(tags12);
    tagsList.add(tags13);
    tagsList.add(tags14);
    tagsList.add(tags15);
    tagsList.add(tags16);
    tagsList.add(tags17);
    tagsList.add(tags18);
    tagsList.add(tags19);

    tagsList.add(tags20);
    tagsList.add(tags21);
    tagsList.add(tags22);
    tagsList.add(tags23);
    tagsList.add(tags24);
    tagsList.add(tags25);
    tagsList.add(tags26);
    tagsList.add(tags27);
    tagsList.add(tags28);
    tagsList.add(tags29);

    tagsList.add(tags30);
    tagsList.add(tags31);
    tagsList.add(tags32);
    tagsList.add(tags33);
    tagsList.add(tags34);
    tagsList.add(tags35);
    tagsList.add(tags36);
    tagsList.add(tags37);
    tagsList.add(tags38);
    tagsList.add(tags39);

    tagsList.add(tags40);
    tagsList.add(tags41);
    tagsList.add(tags42);
    tagsList.add(tags43);
    tagsList.add(tags44);
    tagsList.add(tags45);
    tagsList.add(tags46);
    tagsList.add(tags47);
    tagsList.add(tags48);
    tagsList.add(tags49);

    tagsList.add(tags50);
    tagsList.add(tags51);
    tagsList.add(tags52);
    tagsList.add(tags53);
    tagsList.add(tags54);
    tagsList.add(tags55);
    // /////////////////////////////////
    tagsList.add(tags0000);
    tagsList.add(tags0001);
    tagsList.add(tags0002);
    tagsList.add(tags0003);
    tagsList.add(tags0004);
    tagsList.add(tags0005);
    tagsList.add(tags0006);
    tagsList.add(tags0007);
    tagsList.add(tags0008);
    tagsList.add(tags0009);

    tagsList.add(tags0010);
    tagsList.add(tags0011);
    tagsList.add(tags0012);
    tagsList.add(tags0013);
    tagsList.add(tags0014);
    tagsList.add(tags0015);
    tagsList.add(tags0016);
    tagsList.add(tags0017);
    tagsList.add(tags0018);
    tagsList.add(tags0019);

    tagsList.add(tags0020);
    tagsList.add(tags0021);
    tagsList.add(tags0022);
    tagsList.add(tags0023);
    tagsList.add(tags0024);
    tagsList.add(tags0025);
    tagsList.add(tags0026);
    tagsList.add(tags0027);
    tagsList.add(tags0028);
    tagsList.add(tags0029);

    tagsList.add(tags0030);
    tagsList.add(tags0031);
    tagsList.add(tags0032);
    tagsList.add(tags0033);
    tagsList.add(tags0034);
    tagsList.add(tags0035);
    tagsList.add(tags0036);
    tagsList.add(tags0037);
    tagsList.add(tags0038);
    tagsList.add(tags0039);

    tagsList.add(tags0040);
    tagsList.add(tags0041);
    tagsList.add(tags0042);
    tagsList.add(tags0043);
    tagsList.add(tags0044);
    tagsList.add(tags0045);
    tagsList.add(tags0046);
    tagsList.add(tags0047);
    tagsList.add(tags0048);
    tagsList.add(tags0049);

    tagsList.add(tags0050);
    tagsList.add(tags0051);
    tagsList.add(tags0052);
    tagsList.add(tags0053);
    tagsList.add(tags0054);
    tagsList.add(tags0055);

    // 和谐
    for (int i = 0; i < tagsList.size() / 2; i++) {
      String[] strings = tagsList.get(i);
      for (int j = 0; j < strings.length; j++) {
        for (int k = 0; k < 5; k++) {
          // 每个topic下创建五个活动
          JsonObject relation =
              Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY,
                  strings[0] + strings[1] + strings[2] + strings[3] + k).set(Constant.KEY_LABEL,
                  strings[j]);
          RELATION_THEME.push(relation);

          // 每个活动下创建五个文件
          for (int l = 0; l < 5; l++) {
            String uuid = UUID.randomUUID().toString();
            JsonObject file = Json.createObject();
            file.set(Constant.KEY_ID, uuid);
            file.set(Constant.KEY_TITLE, strings[0] + strings[1] + strings[2] + strings[3] + k
                + "文件" + l);
            file.set(Constant.KEY_NAME, strings[0] + strings[1] + strings[2] + strings[3] + k
                + "文件" + l + ".mp3");
            file.set(Constant.KEY_CONTENTTYPE, "audio/mp3");
            file.set(Constant.KEY_CONTENTLENGTH, "123");
            file.set(Constant.KEY_URL, "/mnt/sdcard/abc.mp3");
            file.set(Constant.KEY_THUMBNAIL, "123456789");
            FILE_SEARCH.push(file);

            JsonObject fileRelationActivity =
                Json.createObject().set(Constant.KEY_TYPE, "attachment")
                    .set(Constant.KEY_KEY, uuid).set(Constant.KEY_LABEL,
                        strings[0] + strings[1] + strings[2] + strings[3] + k);
            RELATION_THEME.push(fileRelationActivity);

            JsonObject fileRelationTopic =
                Json.createObject().set(Constant.KEY_TYPE, "attachment")
                    .set(Constant.KEY_KEY, uuid).set(Constant.KEY_LABEL, strings[3]);
            RELATION_THEME.push(fileRelationTopic);

            JsonObject fileRelationTerm =
                Json.createObject().set(Constant.KEY_TYPE, "attachment")
                    .set(Constant.KEY_KEY, uuid).set(Constant.KEY_LABEL, strings[2]);
            RELATION_THEME.push(fileRelationTerm);

            JsonObject fileRelationGrade =
                Json.createObject().set(Constant.KEY_TYPE, "attachment")
                    .set(Constant.KEY_KEY, uuid).set(Constant.KEY_LABEL, strings[1]);
            RELATION_THEME.push(fileRelationGrade);

            JsonObject fileRelationTheme =
                Json.createObject().set(Constant.KEY_TYPE, "attachment")
                    .set(Constant.KEY_KEY, uuid).set(Constant.KEY_LABEL, strings[0]);
            RELATION_THEME.push(fileRelationTheme);
          }
        }
      }
    }

    // 示范课
    for (int i = tagsList.size() / 2; i < tagsList.size(); i++) {
      String[] strings = tagsList.get(i);
      for (int j = 0; j < strings.length; j++) {
        // 每个topic下创建五个文件
        for (int l = 0; l < 5; l++) {
          String uuid = UUID.randomUUID().toString();
          JsonObject file = Json.createObject();
          file.set(Constant.KEY_ID, uuid);
          file.set(Constant.KEY_TITLE, strings[0] + strings[1] + strings[2] + strings[3] + "文件" + l);
          file.set(Constant.KEY_NAME, strings[0] + strings[1] + strings[2] + strings[3] + "文件" + l
              + ".mp3");
          file.set(Constant.KEY_CONTENTTYPE, "video/mp4");
          file.set(Constant.KEY_CONTENTLENGTH, "123");
          file.set(Constant.KEY_URL, "/mnt/sdcard/abc.mp4");
          file.set(Constant.KEY_THUMBNAIL, "123456789");
          FILE_SEARCH.push(file);

          JsonObject fileRelationTopic =
              Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY, uuid)
                  .set(Constant.KEY_LABEL, strings[3]);
          RELATION_THEME.push(fileRelationTopic);

          JsonObject fileRelationTerm =
              Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY, uuid)
                  .set(Constant.KEY_LABEL, strings[2]);
          RELATION_THEME.push(fileRelationTerm);

          JsonObject fileRelationGrade =
              Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY, uuid)
                  .set(Constant.KEY_LABEL, strings[1]);
          RELATION_THEME.push(fileRelationGrade);

          JsonObject fileRelationTheme =
              Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY, uuid)
                  .set(Constant.KEY_LABEL, strings[0]);
          RELATION_THEME.push(fileRelationTheme);
        }
      }
    }

    // 全部
    for (int i = 0; i < SEARCH_ALL.length; i++) {
      JsonObject relation =
          Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY, SEARCH_ALL[i])
              .set(Constant.KEY_LABEL, "全部");
      RELATION_SEARCH.push(relation);

      for (int j = 0; j < 5; j++) {
        // 给每个标签初始化五个文件
        String uuid = UUID.randomUUID().toString();
        JsonObject file = Json.createObject();
        file.set(Constant.KEY_ID, uuid);
        file.set(Constant.KEY_TITLE, "搜索-" + SEARCH_ALL[i] + "-文件" + j);
        file.set(Constant.KEY_NAME, "搜索-" + SEARCH_ALL[i] + "-文件" + j + ".mp3");
        file.set(Constant.KEY_CONTENTTYPE, "audio/mp3");
        file.set(Constant.KEY_CONTENTLENGTH, "123");
        file.set(Constant.KEY_URL, "/mnt/sdcard/abc.mp3");
        file.set(Constant.KEY_THUMBNAIL, "123456789");
        FILE_SEARCH.push(file);

        JsonObject relationFile1 =
            Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY, uuid)
                .set(Constant.KEY_LABEL, "全部");
        RELATION_SEARCH.push(relationFile1);

        JsonObject relationFile2 =
            Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY, uuid)
                .set(Constant.KEY_LABEL, SEARCH_ALL[i]);
        RELATION_SEARCH.push(relationFile2);

      }
    }

    // 动画
    for (int i = 0; i < SEARCH_ANIM.length; i++) {
      JsonObject relation =
          Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY, SEARCH_ANIM[i])
              .set(Constant.KEY_LABEL, "动画");
      RELATION_SEARCH.push(relation);
    }
    // 音频
    for (int i = 0; i < SEARCH_AUDIO.length; i++) {
      JsonObject relation =
          Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY, SEARCH_AUDIO[i])
              .set(Constant.KEY_LABEL, "音频");
      RELATION_SEARCH.push(relation);
    }
    // 图书书
    for (int i = 0; i < SEARCH_BOOK.length; i++) {
      JsonObject relation =
          Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY, SEARCH_BOOK[i])
              .set(Constant.KEY_LABEL, "图画书");
      RELATION_SEARCH.push(relation);
    }
    // 游戏
    for (int i = 0; i < SEARCH_GAME.length; i++) {
      JsonObject relation =
          Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY, SEARCH_GAME[i])
              .set(Constant.KEY_LABEL, "游戏");
      RELATION_SEARCH.push(relation);
    }
    // 图片
    for (int i = 0; i < SEARCH_IMAGE.length; i++) {
      JsonObject relation =
          Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY, SEARCH_IMAGE[i])
              .set(Constant.KEY_LABEL, "图片");
      RELATION_SEARCH.push(relation);
    }
    // 文本
    for (int i = 0; i < SEARCH_TEXT.length; i++) {
      JsonObject relation =
          Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY, SEARCH_TEXT[i])
              .set(Constant.KEY_LABEL, "文本");
      RELATION_SEARCH.push(relation);
    }
    // 视频
    for (int i = 0; i < SEARCH_VIDEO.length; i++) {
      JsonObject relation =
          Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY, SEARCH_VIDEO[i])
              .set(Constant.KEY_LABEL, "视频");
      RELATION_SEARCH.push(relation);
    }

    // 整理TAG数据
    for (int i = 0; i < RELATION_THEME.length(); i++) {
      RELATION_TABLE_DATA.push(RELATION_THEME.getObject(i));
    }
    for (int i = 0; i < RELATION_SEARCH.length(); i++) {
      RELATION_TABLE_DATA.push(RELATION_SEARCH.getObject(i));
    }

    // 整理FILE数据
    for (int i = 0; i < FILE_SEARCH.length(); i++) {
      FILE_TABLE_DATA.push(FILE_SEARCH.getObject(i));
    }
  }
}
