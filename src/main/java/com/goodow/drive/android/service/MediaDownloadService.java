package com.goodow.drive.android.service;

import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.global_data_cache.GlobalConstant.DownloadStatusEnum;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.drive.android.module.DriveModule;
import com.goodow.drive.android.toolutils.FolderSize;
import com.goodow.drive.android.toolutils.MyApplication;
import com.goodow.drive.android.toolutils.OfflineFileObserver;
import com.goodow.drive.android.toolutils.Tools;
import com.goodow.realtime.CollaborativeMap;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.media.MediaHttpDownloader;
import com.google.api.client.googleapis.media.MediaHttpDownloaderProgressListener;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.Thread.State;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;

public class MediaDownloadService extends Service {

  public final class MyBinder extends Binder {
    public void addResDownload(final CollaborativeMap res) {
      startResDownloadTread(res);
    }

    public void clearResDownload() {
      MediaDownloadService.this.downloadUrlQueue.clear();
    }

    public String getDownloadResBlobKey() {
      return MediaDownloadService.this.downloadRes.get("blobKey");
    }

    public void removeResDownload(final CollaborativeMap res) {
      MediaDownloadService.this.downloadUrlQueue.remove(res);
    }
  }

  /**
   * InnerClass: Media下载监听
   */
  private class CustomProgressListener implements MediaHttpDownloaderProgressListener {
    @Override
    public void progressChanged(final MediaHttpDownloader downloader) {
      switch (downloader.getDownloadState()) {
        case MEDIA_IN_PROGRESS:
          downloadRes.set("progress", Integer.toString((int) (downloader.getProgress() * 100)));

          break;
        case MEDIA_COMPLETE:
          downloadRes.set("progress", "100");
          downloadRes.set("status", DownloadStatusEnum.COMPLETE.getStatus());
          // 下载完成以后，文件名设置为空，下载状态为false
          Editor editor = sp.edit();
          editor.putString("DownLoadFileName", null);
          editor.putBoolean("DownLoading", false);
          editor.commit();

          Intent intent = new Intent();
          intent.setAction("DATA_CONTROL");
          MyApplication.getApplication().getBaseContext().sendBroadcast(intent);

          break;
        default:

          break;
      }
    }
  }

  private class ResDownloadThread extends Thread {
    @Override
    public void run() {
      try {
        while (true) {
          downloadRes = MediaDownloadService.this.downloadUrlQueue.take();
          if (sp.getBoolean("DownLoading", false)) {
            File tmpFile =
                new File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/"
                    + sp.getString("DownLoadFileName", null));
            if (tmpFile.exists()) {
              tmpFile.delete();
            }
          }
          String filePath =
              GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/"
                  + downloadRes.get("blobKey");
          // 加入下载的内容，里面有flash类型,那么加上".swf"
          if (downloadRes.get("type").equals("application/x-shockwave-flash")) {
            filePath = filePath + ".swf";
          }
          File newFile = new File(filePath);
          // File newFile = new
          // File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/" +
          // downloadRes.get("blobKey"));
          // 本地文件不存在则开启下载,或者存在，但是大小为0
          if (!newFile.exists() || newFile.length() == 0) {
            downloadRes.set("status", GlobalConstant.DownloadStatusEnum.DOWNLOADING.getStatus());
            String urlString = downloadRes.get("url");
            // 下载缩略图
            if (downloadRes.get("type").toString().startsWith("image/")) {
              String thumbnailurl = downloadRes.get("thumbnail");
              // 修正缩略图地址
              thumbnailurl = Tools.modifyThumbnailAddress(thumbnailurl);
              int width = getResources().getDisplayMetrics().widthPixels;
              int height = getResources().getDisplayMetrics().heightPixels;
              urlString = thumbnailurl + "=s" + ((width > height ? width : height) * 8) / 10;
            }
            long fileLength = 0;
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            if (conn.getResponseCode() == 200) {
              fileLength = conn.getContentLength();// 根据响应获取文件大小
            }
            String fileName = null;
            // 如果文件夹过大，删除最早的一个文件
            while ((GlobalConstant.OfflineResourceSize + fileLength) > GlobalConstant.FolderSizeLimite) {
              fileName =
                  (String) FolderSize.getSortedHashtableByValue(GlobalConstant.fileInfo)[0]
                      .getKey();
              String filePath1 =
                  GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/";
              File file = new File(filePath1 + fileName);
              if (file.exists() && file.isFile()) {
                long tmpfilelength = file.length();
                if (file.delete()) {
                  GlobalConstant.OfflineResourceSize =
                      GlobalConstant.OfflineResourceSize - tmpfilelength;
                  // 移除集合中的数据
                  GlobalConstant.fileInfo.remove(fileName);
                  // TODO:可以删除list里卖弄的数据
                } else {
                  // 初始化
                  GlobalConstant.getFoldInformation();
                }
              } else {
                // 初始化
                GlobalConstant.getFoldInformation();
              }
            }
            // 更新文件夹的大小
            GlobalConstant.OfflineResourceSize = GlobalConstant.OfflineResourceSize + fileLength;
            // 集合中添加新的文件
            GlobalConstant.fileInfo.put((String) (downloadRes.get("type").equals(
                "application/x-shockwave-flash") ? downloadRes.get("blobKey") + ".swf"
                : downloadRes.get("blobKey")), System.currentTimeMillis());
            doDownLoad(urlString);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private final IBinder myBinder = new MyBinder();
  private final BlockingQueue<CollaborativeMap> downloadUrlQueue =
      new LinkedBlockingDeque<CollaborativeMap>();
  private ResDownloadThread resDownloadThread = new ResDownloadThread();

  private final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

  private final JsonFactory JSON_FACTORY = new JacksonFactory();

  private CollaborativeMap downloadRes;

  private ConnectivityManager connectivityManager;

  private NetworkInfo info;
  private final Timer timer = new Timer();
  private TimerTask task;

  private SharedPreferences sp;

  // 用来接收网络状态改变的广播
  private final BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
    // 标记
    private boolean receiver = false;

    @Override
    public void onReceive(Context context, Intent intent) {

      String action = intent.getAction();
      if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
        // Log.d("mark", "网络状态已经改变");
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
          if (receiver) {
            // String name = info.getTypeName();
            // Log.d("mark", "当前网络名称：" + name);
            receiver = false;
            getDownloadThreadState();
          }
        } else {
          // Log.d("mark", "没有可用网络");
          receiver = true;
        }
      }
    }
  };

  @Override
  public IBinder onBind(Intent intent) {
    return myBinder;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    // 获得sharedPreferences
    sp = getSharedPreferences("config", MODE_PRIVATE);
    // 注册广播
    IntentFilter mFilter = new IntentFilter();
    mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    registerReceiver(mConnectivityReceiver, mFilter);
    // 初始化计时器任务
    task = new TimerTask() {

      @Override
      public void run() {
        getDownloadThreadState();
        // 每10s将离线文件加载一次到下载到队列里面
        String docId =
            "@tmp/" + GlobalDataCacheForMemorySingleton.getInstance().getUserId() + "/"
                + GlobalConstant.DocumentIdAndDataKey.OFFLINEDOCID.getValue();
        OfflineFileObserver.OFFLINEFILEOBSERVER.startObservation(docId, null);
      }
    };
    timer.schedule(task, 10000, 10000);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    // 注销广播
    unregisterReceiver(mConnectivityReceiver);
    if (timer != null) {
      timer.cancel();
    }
  }

  private void doDownLoad(String... params) {
    try {
      String filePath =
          GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/"
              + downloadRes.get("blobKey");
      // 加入下载的内容，里面有flash类型,那么加上".swf"
      if (downloadRes.get("type").equals("application/x-shockwave-flash")) {
        filePath = filePath + ".swf";
      }
      // 存入正在下载的文件名，及其下载状态
      Editor editor = sp.edit();
      editor.putString("DownLoadFileName", filePath.substring(filePath.lastIndexOf("/") + 1));
      editor.putBoolean("DownLoading", true);
      editor.commit();
      File newFile = new File(filePath);

      FileOutputStream outputStream = new FileOutputStream(newFile);

      MediaHttpDownloader downloader =
          new MediaHttpDownloader(HTTP_TRANSPORT, new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest request) {
              request.setParser(new JsonObjectParser(JSON_FACTORY));
            }
          });

      if (DriveModule.DRIVE_SERVER.equals("http://server.drive.goodow.com")) {
        downloader.setDirectDownloadEnabled(false);// 设为多块下载
        downloader.setChunkSize(MediaHttpUploader.MINIMUM_CHUNK_SIZE);// 设置每一块的大小
      } else {
        downloader.setDirectDownloadEnabled(true); // 设为单块下载
      }

      downloader.setProgressListener(new CustomProgressListener());// 设置监听器

      downloader.download(new GenericUrl(params[0]), outputStream);// 启动下载
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 获取当前线程的状态,并处理下载任务
   */
  private void getDownloadThreadState() {
    State state = resDownloadThread.getState();
    switch (state) {
    // 线程被阻塞，在等待一个锁。
      case BLOCKED:

        break;
      // 线程已被创建，但从未启动
      case NEW:
        resDownloadThread.start();

        break;
      // 线程可能已经运行
      case RUNNABLE:

        break;
      // 线程已被终止
      case TERMINATED:
        // note : 如果下载线程被异常终止了, 就重新创建一个
        resDownloadThread = new ResDownloadThread();
        resDownloadThread.start();

        break;
      // 线程正在等待一个指定的时间。
      case TIMED_WAITING:

        break;
      default:

        break;
    }
  }

  private void startResDownloadTread(final CollaborativeMap res) {
    // 遍历队列,若有相同的URL则不添加
    Iterator<CollaborativeMap> iterator = downloadUrlQueue.iterator();
    add : do {
      while (iterator.hasNext()) {
        CollaborativeMap item = iterator.next();
        if (item.get("url").equals(res.get("url"))) {
          break add;
        }
      }

      downloadUrlQueue.add(res);
    } while (false);

    getDownloadThreadState();
  }

}
