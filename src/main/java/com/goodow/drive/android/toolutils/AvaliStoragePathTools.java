package com.goodow.drive.android.toolutils;

import com.goodow.drive.android.Constant;
import com.goodow.realtime.json.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import android.content.Context;
import android.widget.Toast;

/**
 * 
 * 
 * 用途：用于在不确定可使用的路径下，通过此类的接口 getStorageCard()返回有效路径; 思路：
 * 1、读取系统对外卡挂载的路径/proc/mounts，获取挂载的外卡路径并与系统对挂载路径的设置文件
 * /system/etc/vold.fstab进行匹配，如果与设置文件的路径一致，则将挂载路径保存在mMounts中，不一致则删除;
 * 
 * 
 * 
 * 只提供 getStorageCard()
 */
public final class AvaliStoragePathTools {

  private static ArrayList<String> mMounts = new ArrayList<String>();// 挂载路径和本地Flash可用路径的集合
  private static ArrayList<String> mVold = new ArrayList<String>();// /system/etc/vold.fstab文件中对所有挂载路径的集合

  /**
   * 获取有效路径(包含验证有无goodow目录)
   * 
   * @return 返回有效的绝对路径
   */
  public static ArrayList<String> getStorageCard(Context context) {
    ArrayList<String> storageCard = getStorageCard();
    if (storageCard.size() == 1) {
      File file = new File(storageCard.get(0) + "/goodow");
      if (!file.exists()) {
        storageCard.remove(0);
      }
    } else if (storageCard.size() > 1) {
      File file = new File(storageCard.get(1) + "/goodow");
      if (!file.exists()) {
        storageCard.remove(1);
      }
    }
    if (storageCard.size() == 0) {
      Toast.makeText(context.getApplicationContext(), "未插入SD卡或SD卡中未包含资源文件夹", Toast.LENGTH_LONG)
          .show();
    }
    return storageCard;
  }

  /**
   * 替换attachment中url,thumbnail的sd卡路径
   * 
   * @param attachment
   * @return 是否替换成功
   */
  public static boolean replacePath(JsonObject attachment) {
    ArrayList<String> storageCard = getStorageCard();
    if (storageCard.size() == 0) {
      return false;
    }
    String url = attachment.getString(Constant.KEY_URL);
    String thumbnail = attachment.getString(Constant.KEY_THUMBNAIL);
    if (url.startsWith(Constant.VIR1_PATH)) {
      replaceString(attachment, Constant.VIR1_PATH, storageCard.get(0));
    } else if (url.startsWith(Constant.VIR2_PATH) && storageCard.size() == 2) {
      replaceString(attachment, Constant.VIR2_PATH, storageCard.get(1));
    }
    return true;
  }

  /**
   * 对直接从挂载的路径下获取外卡的路径和通过读取系统文件获得的挂载路径进行对比
   */
  private static void compareMountsWithVold() {
    int i = 0;
    while (true) {
      // 得到外部挂载sd卡个数
      int j = mMounts.size();
      if (i >= j) {
        mVold.clear();
        return;
      }
      // 如果与设置文件的路径一致，则将挂载路径保存在mMounts中，不一致则删除;
      String str = mMounts.get(i);
      if (!mVold.contains(str)) {
        ArrayList<String> localArrayList = mMounts;
        int k = i - 1;
        localArrayList.remove(i);
        i = k;
      }
      i++;
    }
  }

  /**
   * 获取有效路径
   * 
   * @return 返回有效的绝对路径
   */
  private static ArrayList<String> getStorageCard() {
    // 读取系统外卡挂载的路径
    readMountsFile();
    // 读取系统文件对挂载目录的设置
    readVoldFile();
    // 对直接从挂载的路径下获取外卡的路径和通过读取系统文件获得的挂载路径进行对比
    compareMountsWithVold();
    return mMounts;
  }

  /**
   * 读取系统外卡挂载的路径
   */
  private static void readMountsFile() {
    mMounts.add("/mnt/sdcard");
    Scanner localScanner = null;
    try {
      File localFile = new File("/proc/mounts");
      localScanner = new Scanner(localFile);
      while (true) {
        if (!localScanner.hasNext()) {
          return;
        }
        String str1 = localScanner.nextLine();
        if (!str1.startsWith("/dev/block/vold/")) {
          continue;
        }
        String str2 = str1.split(" ")[1];
        if (str2.equals("/mnt/sdcard")) {
          continue;
        }
        mMounts.add(str2);
      }
    } catch (Exception localException) {
      while (true) {
        localException.printStackTrace();
      }
    } finally {
      if (localScanner != null) {
        localScanner.close();
      }
    }
  }

  /**
   * 读取系统文件对挂载目录的设置
   */
  private static void readVoldFile() {
    mVold.add("/mnt/sdcard");
    Scanner localScanner = null;
    try {
      File localFile = new File("/system/etc/vold.fstab");
      localScanner = new Scanner(localFile);
      while (true) {
        if (!localScanner.hasNext()) {
          return;
        }
        String str1 = localScanner.nextLine();
        if (!str1.startsWith("dev_mount")) {
          continue;
        }
        String str2 = str1.split(" ")[2];
        if (str2.contains(":")) {
          int i = str2.indexOf(":");
          str2 = str2.substring(0, i);
        }
        if (str2.equals("/mnt/sdcard")) {
          continue;
        }
        mVold.add(str2);
      }
    } catch (Exception localException) {
      while (true) {
        localException.printStackTrace();
      }
    } finally {
      if (localScanner != null) {
        localScanner.close();
      }
    }
  }

  private static void replaceString(JsonObject attachment, String target, String replace) {
    String url = attachment.getString(Constant.KEY_URL);
    String thumbnail = attachment.getString(Constant.KEY_THUMBNAIL);
    String replaceUrl = url.replace(target, replace);
    String replaceThumbnail = thumbnail.replace(target, replace);
    attachment.set(Constant.KEY_URL, replaceUrl);
    attachment.set(Constant.KEY_THUMBNAIL, replaceThumbnail);
  }

  public AvaliStoragePathTools() {
  }

}