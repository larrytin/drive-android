package com.goodow.drive.android.toolutils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class FolderSize {
  // 递归
  public static long getFileSize(File f, Map<String, Long> map) throws Exception// 取得文件夹大小
  {
    long size = 0;
    File flist[] = f.listFiles();
    for (int i = 0; i < flist.length; i++) {
      // 文件夹
      if (flist[i].isDirectory()) {
        size = size + getFileSize(flist[i], map);
      } else {
        // 文件
        size = size + flist[i].length();
        map.put(flist[i].getName(), flist[i].lastModified());
      }
    }
    return size;
  }

  /**
   * Map 按值（value）排序
   * 
   * @param map
   * @return Map.Entry[]
   */
  public static Map.Entry[] getSortedHashtableByValue(Map<String, Long> map) {
    Set set = map.entrySet();
    Map.Entry[] entries = (Map.Entry[]) set.toArray(new Map.Entry[set.size()]);
    Arrays.sort(entries, new Comparator() {
      @Override
      public int compare(Object arg0, Object arg1) {
        Long key1 = Long.valueOf(((Map.Entry) arg0).getValue().toString());
        Long key2 = Long.valueOf(((Map.Entry) arg1).getValue().toString());
        return key1.compareTo(key2);
      }
    });
    return entries;
  }

  public static void main(String[] args) {
    Map<String, Long> map = new HashMap<String, Long>();
    FolderSize folder = new FolderSize();
    long fileSize = 0;
    String path = "F:\\kuaipan";
    File ff = new File(path);
    if (ff.isDirectory()) { // 如果路径是文件夹的时候
      try {
        fileSize = folder.getFileSize(ff, map);
        System.out.println("文件夹大小：" + fileSize);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    Entry[] sortedHashtableByValue = folder.getSortedHashtableByValue(map);
    for (Entry entry : sortedHashtableByValue) {
      System.out.println((String) entry.getKey() + entry.getValue());
    }
  }
}
