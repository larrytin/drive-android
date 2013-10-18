package com.goodow.drive.android.global_data_cache;

import com.goodow.drive.android.toolutils.FolderSize;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GlobalConstant {
  public static enum DocumentIdAndDataKey {
    // 文件id
    FAVORITESDOCID("favorites" + docId),
    //
    LESSONDOCID("lesson" + docId),
    //
    REMOTECONTROLDOCID("remotecontrol" + docId),
    //
    OFFLINEDOCID("offlinedoc" + docId),

    // 属性key
    FOLDERSKEY("folders"),
    //
    FILESKEY("files"),
    //
    OFFLINEKEY("offline"),
    //
    CURRENTPATHKEY("currentpath"),
    //
    CURRENTDOCIDKEY("currentdocid"),
    //
    PATHKEY("path"),
    //
    PLAYFILE("playfile");

    public static DocumentIdAndDataKey getEnumWithValue(String value) {
      if (null != value) {
        for (DocumentIdAndDataKey item : DocumentIdAndDataKey.values()) {
          if (item.getValue().equals(value)) {

            return item;
          }
        }
      }

      return null;
    }

    private final String value;

    private DocumentIdAndDataKey(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  public static enum DownloadStatusEnum {
    WAITING("等待下载"), DOWNLOADING("正在下载"), COMPLETE("下载完成"), UNDOWNLOADING("未下载");

    private final String status;

    private DownloadStatusEnum(String status) {
      this.status = status;
    }

    public String getStatus() {
      return status;
    }
  }

  public static enum MenuTypeEnum {
    //
    USER_NAME("用户帐户名称"),
    //
    USER_LESSON_DATA("我的课程"),
    //
    USER_REMOTE_DATA("我的收藏夹"),
    //
    // LOCAL_RES("本地资源"),
    //
    USER_OFFLINE_DATA("离线文件");

    private final String menuName;

    private MenuTypeEnum(String menuName) {
      this.menuName = menuName;
    }

    public String getMenuName() {
      return menuName;
    }
  }

  /**
   * @author Administrator 对应支持的文件格式;PRINT("print")是手偶格式
   */
  public static enum SupportResTypeEnum {
    DOC("doc"), PDF("pdf"), MP3("mp3"), MP4("mp4"), FLASH("swf"), TEXT("txt"), PNG("png"), JPEG(
        "jpg"), EXCEL("xls"), PRINT("print");
    private final String typeName;

    private SupportResTypeEnum(String typeName) {
      this.typeName = typeName;
    }

    public String getTypeName() {
      return typeName;
    }
  }

  static {
    Map<String, List<String>> map = new HashMap<String, List<String>>();
    List<String> list_goodow = new ArrayList<String>();
    list_goodow.add("http://realtime.goodow.com");
    list_goodow.add("http://server.drive.goodow.com");
    list_goodow.add("21");
    List<String> list_wuxi = new ArrayList<String>();
    list_wuxi.add("http://drive.retechcorp.com:8080");
    list_wuxi.add("http://drive.retechcorp.com:8880");
    list_wuxi.add("07");
    List<String> list_beijing = new ArrayList<String>();
    list_beijing.add("http://192.168.11.39:8080");
    list_beijing.add("http://192.168.11.39:8880");
    list_beijing.add("25");
    List<String> list_ceshi = new ArrayList<String>();
    list_ceshi.add("http://61.177.139.216:8084");
    list_ceshi.add("http://61.177.139.216:8880");
    list_ceshi.add("25");
    map.put("goodow", list_goodow);
    map.put("wuxi", list_wuxi);
    map.put("beijing", list_beijing);
    map.put("ceshi", list_ceshi);
    // 配置时，修改此处。
    // 无锡内网
    // mList = map.get("wuxi");
    // 北京内网
    // mList = map.get("beijing");
    // 外网goodow
    mList = map.get("goodow");
    // 测试
    // mList = map.get("ceshi");
  }
  public static List<String> mList;
  public static String REALTIME_SERVER = mList.get(0);
  // 外网
  // public static String REALTIME_SERVER = "http://realtime.goodow.com";
  // 北京内网
  // public static String REALTIME_SERVER = "http://192.168.11.39:8080";
  // 无锡内网
  // public static String REALTIME_SERVER = "http://drive.retechcorp.com:8080";

  // 北京+外网
  // private static String docId = "21";

  // 无锡
  // private static String docId = "07";
  private static String docId = mList.get(2);
  // 离线文件夹的大小
  public static long OfflineResourceSize;
  // 离线文件的文件名和时间
  public static Map<String, Long> fileInfo = new HashMap<String, Long>();

  // 设置文件夹的大小
  public static long FolderSizeLimite = 300 * 1024 * 1024;

  public static void getFoldInformation() {
    // 获得下载目录下的文件大小，及文件名和文件的修改时间(等同于创建时间)
    try {
      GlobalConstant.OfflineResourceSize =
          FolderSize.getFileSize(new File(GlobalDataCacheForMemorySingleton.getInstance()
              .getOfflineResDirPath()), GlobalConstant.fileInfo);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private GlobalConstant() {

  }
}
