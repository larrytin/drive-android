package com.goodow.drive.android.toolutils;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

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
   * 获取有效路径
   * 
   * @return 返回有效的绝对路径
   */
  public static ArrayList<String> getStorageCard() {
    // 读取系统外卡挂载的路径
    readMountsFile();
    // 读取系统文件对挂载目录的设置
    readVoldFile();
    // 对直接从挂载的路径下获取外卡的路径和通过读取系统文件获得的挂载路径进行对比
    compareMountsWithVold();
    return mMounts;
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

  public AvaliStoragePathTools() {
  }

}