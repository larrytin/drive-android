package com.goodow.drive.android.service;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.Thread.State;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.global_data_cache.GlobalConstant.DownloadStatusEnum;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.drive.android.module.DriveModule;
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

public class MediaDownloadService extends Service {
  private final IBinder myBinder = new MyBinder();

  private BlockingQueue<CollaborativeMap> downloadUrlQueue = new LinkedBlockingDeque<CollaborativeMap>();
  private ResDownloadThread resDownloadThread = new ResDownloadThread();
  private HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
  private JsonFactory JSON_FACTORY = new JacksonFactory();
  private CollaborativeMap downloadRes;

  private void startResDownloadTread(final CollaborativeMap res) {
    //遍历队列,若有相同的URL则不添加
    Iterator<CollaborativeMap>iterator = downloadUrlQueue.iterator();
    add: do{
      while (iterator.hasNext()) {
        CollaborativeMap item = iterator.next();
        if(item.get("url").equals(res.get("url"))){
          
          break add;
        }
      }
      
      downloadUrlQueue.add(res);
    }while(false);
    
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

  private class ResDownloadThread extends Thread {
    public void run() {
      try {
        while (true) {
          downloadRes = MediaDownloadService.this.downloadUrlQueue.take();

          File newFile = new File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/" + downloadRes.get("blobKey"));
          // 本地文件不存在则开启下载
          if (!newFile.exists()) {
            downloadRes.set("status", GlobalConstant.DownloadStatusEnum.DOWNLOADING.getStatus());

            final String urlString = downloadRes.get("url");
            doDownLoad(urlString);
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public final class MyBinder extends Binder {
    public String getDownloadResBlobKey() {
      return MediaDownloadService.this.downloadRes.get("blobKey");
    }

    public void addResDownload(final CollaborativeMap res) {
      startResDownloadTread(res);
    }

    public void removeResDownload(final CollaborativeMap res) {
      MediaDownloadService.this.downloadUrlQueue.remove(res);
    }

    public void clearResDownload() {
      MediaDownloadService.this.downloadUrlQueue.clear();
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

        break;
      default:

        break;
      }
    }
  }

  private void doDownLoad(String... params) {
    try {
      File newFile = new File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/" + downloadRes.get("blobKey"));
      FileOutputStream outputStream = new FileOutputStream(newFile);

      MediaHttpDownloader downloader = new MediaHttpDownloader(HTTP_TRANSPORT, new HttpRequestInitializer() {
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

  @Override
  public IBinder onBind(Intent intent) {
    return myBinder;
  }

}
