package com.goodow.drive.android.toolutils;

import com.goodow.api.services.attachment.Attachment;
import com.goodow.api.services.attachment.Attachment.Get;
import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.drive.android.module.DriveModule;
import com.goodow.realtime.CollaborativeList;
import com.goodow.realtime.CollaborativeMap;
import com.goodow.realtime.Document;
import com.goodow.realtime.DocumentLoadedHandler;
import com.goodow.realtime.EventHandler;
import com.goodow.realtime.Model;
import com.goodow.realtime.ModelInitializerHandler;
import com.goodow.realtime.Realtime;
import com.goodow.realtime.ValuesAddedEvent;
import com.goodow.realtime.ValuesRemovedEvent;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import elemental.json.JsonObject;

public enum OfflineFileObserver {
  OFFLINEFILEOBSERVER;

  private class UnloginDownloadThread extends Thread {
    @Override
    public void run() {
      try {
        while (true) {
          JsonObject json = unLoginDownloadQueue.take();

          String attachmentId = json.getString("attachmentId");
          String userId = json.getString("userId");
          String docId =
              "@tmp/" + userId + "/" + GlobalConstant.DocumentIdAndDataKey.OFFLINEDOCID.getValue();

          startObservation(docId, attachmentId);
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

  private final String TAG = this.getClass().getSimpleName();

  private final BlockingQueue<JsonObject> unLoginDownloadQueue =
      new LinkedBlockingDeque<JsonObject>();
  private Model model;
  private CollaborativeMap root;

  private CollaborativeList list;
  private EventHandler<ValuesAddedEvent> listAddEventHandler;

  private EventHandler<ValuesRemovedEvent> listRemoveEventHandler;

  private UnloginDownloadThread unloginDownloadThread = new UnloginDownloadThread();

  public void addAttachment(JsonObject json) {
    unLoginDownloadQueue.add(json);

    State state = unloginDownloadThread.getState();
    switch (state) {
      case BLOCKED:

        break;
      case NEW:
        unloginDownloadThread.start();

        break;
      case RUNNABLE:

        break;
      case TERMINATED:
        unloginDownloadThread = new UnloginDownloadThread();
        unloginDownloadThread.start();

        break;
      case TIMED_WAITING:

        break;
      default:

        break;
    }
  }

  // 为保证并发事件的安全性,需传入Model、CollaborativeList
  public void addFile(final String attachmentId, boolean isLogin, Model offLineModel,
      CollaborativeList offLineList) {
    final Model newModel;
    final CollaborativeList newList;
    if (isLogin) {
      newModel = model;
      newList = list;
    } else {
      newModel = offLineModel;
      newList = offLineList;
    }

    if (null != attachmentId) {
      new AsyncTask<Void, Void, com.goodow.api.services.attachment.model.Attachment>() {

        @Override
        protected com.goodow.api.services.attachment.model.Attachment doInBackground(Void... params) {
          com.goodow.api.services.attachment.model.Attachment execute = null;
          try {
            Attachment attachment = MyApplication.getAttachment();
            Get get = attachment.get(attachmentId);
            execute = get.execute();
          } catch (IOException e) {
            e.printStackTrace();
          }
          return execute;
        }

        @Override
        protected void onPostExecute(com.goodow.api.services.attachment.model.Attachment execute) {
          // TODO Auto-generated method stub
          super.onPostExecute(execute);
          out : do {
            if (null == execute || execute.getId() == null) {
              break out;
            }

            CollaborativeMap newFile = newModel.createMap(null);

            if (null == newFile) {
              break;
            }

            for (int i = 0; i < newList.length(); i++) {
              CollaborativeMap map = newList.get(i);
              if (execute.getBlobKey().equals(map.get("blobKey"))) {
                newList.remove(i);
              }
            }

            newFile.set("url", DriveModule.DRIVE_SERVER + "/serve?id=" + attachmentId);
            newFile.set("progress", "0");
            newFile.set("status", GlobalConstant.DownloadStatusEnum.WAITING.getStatus());

            newFile.set("label", execute.getFilename());
            newFile.set("blobKey", execute.getBlobKey());
            newFile.set("id", execute.getId());
            newFile.set("type", execute.getContentType());

            String thumbnail = execute.getThumbnail();
            if (null != thumbnail) {
              // 修正缩略图地址
              thumbnail = Tools.modifyThumbnailAddress(thumbnail);
              newFile.set("thumbnail", thumbnail);
            }

            newList.push(newFile);

            Log.i(TAG, "new download rescource:" + newList.toString());
          } while (false);
        }

      }.execute();
      // new Thread() {
      // @Override
      // public void run() {
      // try {
      // Attachment attachment = MyApplication.getAttachment();
      // Get get = attachment.get(attachmentId);
      // com.goodow.api.services.attachment.model.Attachment execute =
      // get.execute();
      //
      // out: do {
      // if (null == execute || execute.getId() == null) {
      // break out;
      // }
      //
      // CollaborativeMap newFile = newModel.createMap(null);
      //
      // if (null == newFile) {
      // break;
      // }
      //
      // for (int i = 0; i < newList.length(); i++) {
      // CollaborativeMap map = newList.get(i);
      // if (execute.getBlobKey().equals(map.get("blobKey"))) {
      // newList.remove(i);
      // }
      // }
      //
      // newFile.set("url", DriveModule.DRIVE_SERVER + "/serve?id=" +
      // attachmentId);
      // newFile.set("progress", "0");
      // newFile.set("status",
      // GlobalConstant.DownloadStatusEnum.WAITING.getStatus());
      //
      // newFile.set("label", execute.getFilename());
      // newFile.set("blobKey", execute.getBlobKey());
      // newFile.set("id", execute.getId());
      // newFile.set("type", execute.getContentType());
      //
      // String thumbnail = execute.getThumbnail();
      // if (null != thumbnail) {
      // if
      // (DriveModule.DRIVE_SERVER.endsWith("http://192.168.1.15:8880")) {
      // StringBuffer stringBuffer = new
      // StringBuffer(DriveModule.DRIVE_SERVER);
      // stringBuffer.append(thumbnail.substring(thumbnail.indexOf("8880")
      // + 4));
      // stringBuffer.append("=s218");
      // thumbnail = stringBuffer.toString();
      // }
      // newFile.set("thumbnail", thumbnail);
      // }
      //
      // newList.push(newFile);
      //
      // Log.i(TAG, "new download rescource:" + newList.toString());
      // } while (false);
      // } catch (IOException e) {
      // e.printStackTrace();
      // }
      // };
      // }.start();
    }
  }

  public CollaborativeList getList() {
    Log.i("offlineAdapter", "OfflineFileObserver中的getList方法list=" + (list == null) + "");
    return list;
  }

  public void initEventHandler() {
    do {
      if (listAddEventHandler != null) {
        break;
      }

      listAddEventHandler = new EventHandler<ValuesAddedEvent>() {
        @Override
        public void handleEvent(ValuesAddedEvent event) {
          Object[] adds = event.getValues();

          if (null != adds) {
            for (Object o : adds) {
              CollaborativeMap resource = (CollaborativeMap) o;
              String filePath =
                  GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/"
                      + resource.get("blobKey");
              // 加入下载的内容，里面有flash类型,那么加上".swf"
              if (resource.get("type").equals("application/x-shockwave-flash")) {
                filePath = filePath + ".swf";
              }
              File file = new File(filePath);
              // File file = new
              // File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath()
              // + "/" +
              // resource.get("blobKey"));

              if (!file.exists()) {
                // 本地文件不存在,添加下载任务
                DownloadResServiceBinder.getDownloadResServiceBinder().addResDownload(resource);
              } else {
                // 本地文件已存在,直接显示下载成功
                resource.set("progress", "100");
                resource.set("status", GlobalConstant.DownloadStatusEnum.COMPLETE.getStatus());
              }
            }
          }

          Intent intent = new Intent();
          intent.setAction("CHANGE_OFFLINE_STATE");
          MyApplication.getApplication().getBaseContext().sendBroadcast(intent);
        }
      };
    } while (false);

    do {
      if (listRemoveEventHandler != null) {

        break;
      }

      listRemoveEventHandler = new EventHandler<ValuesRemovedEvent>() {
        @Override
        public void handleEvent(ValuesRemovedEvent event) {
          Object[] adds = event.getValues();

          if (null != adds) {
            for (Object o : adds) {
              CollaborativeMap resource = (CollaborativeMap) o;
              DownloadResServiceBinder.getDownloadResServiceBinder().removeResDownload(resource);
            }
          }

          Intent intent = new Intent();
          intent.setAction("CHANGE_OFFLINE_STATE");
          MyApplication.getApplication().getBaseContext().sendBroadcast(intent);
        }
      };
    } while (false);
  }

  public void removeFile(CollaborativeMap removefile) {
    if (null != removefile && null != removefile.get("blobKey")) {
      String blobKey = removefile.get("blobKey");

      // 删除offline下的map
      for (int i = 0; i < list.length(); i++) {
        CollaborativeMap map = list.get(i);
        if (blobKey.equals(map.get("blobKey"))) {
          list.remove(i);
        }
      }
      String filePath =
          GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/" + blobKey;
      // 加入下载的内容，里面有flash类型,那么加上".swf"
      if (removefile.get("type").equals("application/x-shockwave-flash")) {
        filePath = filePath + ".swf";
      }
      File file = new File(filePath);

      // 删除本地文件
      // File file = new
      // File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath()
      // +
      // "/" + blobKey);
      if (file.exists()) {
        file.delete();

        Intent intent = new Intent();
        intent.setAction("DATA_CONTROL");
        MyApplication.getApplication().getBaseContext().sendBroadcast(intent);
      }
    }
  }

  public void startObservation(String docId, final String attachmentId) {
    initEventHandler();

    DocumentLoadedHandler onLoaded = new DocumentLoadedHandler() {
      @Override
      public void onLoaded(Document document) {
        if (null == attachmentId) {
          // 当前用户的离线文件夹
          model = document.getModel();
          root = model.getRoot();

          list = root.get(GlobalConstant.DocumentIdAndDataKey.OFFLINEKEY.getValue());
          Intent intent = new Intent();
          intent.setAction("com.goodow.drive.android.offlineFileObserver");
          MyApplication.getApplication().getBaseContext().sendBroadcast(intent);
          if (null != list) {
            list.addValuesAddedListener(listAddEventHandler);
            list.addValuesRemovedListener(listRemoveEventHandler);

            for (int i = 0; i < list.length(); i++) {
              CollaborativeMap item = list.get(i);

              String filePath =
                  GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/"
                      + item.get("blobKey");
              // 加入下载的内容，里面有flash类型,那么加上".swf"
              if (item.get("type").equals("application/x-shockwave-flash")) {
                filePath = filePath + ".swf";
              }
              File file = new File(filePath);
              // File file = new
              // File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath()
              // + "/" +
              // item.get("blobKey"));
              if (!file.exists()) {
                // 加入到下载队列
                DownloadResServiceBinder.getDownloadResServiceBinder().addResDownload(item);
              } else {
                // 文件存在，但是未下载完成
                if (!item.get("status").equals(
                    GlobalConstant.DownloadStatusEnum.COMPLETE.getStatus())) {
                  DownloadResServiceBinder.getDownloadResServiceBinder().addResDownload(item);
                }
              }
            }
          }
        } else {
          // 远程推送下载的离线文件夹
          // Model等数据不缓存至单例的成员变量中是为其并发的安全性考虑
          Model model_unlogin = document.getModel();
          CollaborativeMap root = model_unlogin.getRoot();

          CollaborativeList list_unlogin =
              root.get(GlobalConstant.DocumentIdAndDataKey.OFFLINEKEY.getValue());
          if (null != list_unlogin) {
            list_unlogin.addValuesAddedListener(listAddEventHandler);
            list_unlogin.addValuesRemovedListener(listRemoveEventHandler);
          }

          addFile(attachmentId, false, model_unlogin, list_unlogin);
        }
      }
    };

    ModelInitializerHandler initializer = new ModelInitializerHandler() {
      @Override
      public void onInitializer(Model model_) {
        if (null == attachmentId) {
          model = model_;
          root = model.getRoot();

          root.set(GlobalConstant.DocumentIdAndDataKey.OFFLINEKEY.getValue(), model.createList());
        } else {
          Model model_unlogin = model_;
          CollaborativeMap root = model_unlogin.getRoot();

          root.set(GlobalConstant.DocumentIdAndDataKey.OFFLINEKEY.getValue(), model_unlogin
              .createList());
        }
      }
    };

    Realtime.load(docId, onLoaded, initializer, null);
  }
}
