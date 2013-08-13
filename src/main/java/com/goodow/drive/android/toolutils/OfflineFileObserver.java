package com.goodow.drive.android.toolutils;

import java.io.File;
import java.io.IOException;
import java.lang.Thread.State;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import android.util.Log;

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

import elemental.json.JsonObject;

public enum OfflineFileObserver {

  OFFLINEFILEOBSERVER;

  private String TAG = this.getClass().getSimpleName();

  private BlockingQueue<JsonObject> unLoginDownloadQueue = new LinkedBlockingDeque<JsonObject>();

  private Document doc;
  private Model model;
  private CollaborativeMap root;
  private CollaborativeList list;

  private Model model_unlogin;
  private CollaborativeList list_unlogin;

  private EventHandler<ValuesAddedEvent> listAddEventHandler;
  private EventHandler<ValuesRemovedEvent> listRemoveEventHandler;

  private UnloginDownloadThread unloginDownloadThread = new UnloginDownloadThread();

  private class UnloginDownloadThread extends Thread {
    @Override
    public void run() {
      try {
        while (true) {
          JsonObject json = unLoginDownloadQueue.take();

          String attachmentId = json.getString("attachmentId");
          String userId = json.getString("userId");
          String docId = "@tmp/" + userId + "/" + GlobalConstant.DocumentIdAndDataKey.OFFLINEDOCID.getValue();

          startObservation(docId, attachmentId);
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
    }
  }

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

  public CollaborativeList getList() {
    return list;
  }

  public void addFile(final String attachmentId, boolean isLogin) {
    final Model newModel;
    final CollaborativeList newList;
    if (isLogin) {
      newModel = model;
      newList = list;
    } else {
      newModel = model_unlogin;
      newList = list_unlogin;
    }

    if (null != attachmentId) {
      new Thread() {
        public void run() {
          try {
            Attachment attachment = MyApplication.getAttachment();
            Get get = attachment.get(attachmentId);
            com.goodow.api.services.attachment.model.Attachment execute = get.execute();

            out: do {
              if (null == execute || execute.getId() == null) {

                break out;
              }

              for (int i = 0; i < newList.length(); i++) {
                CollaborativeMap map = newList.get(i);
                if (execute.getBlobKey().equals(map.get("blobKey"))) {
                  newList.remove(i);
                }
              }

              CollaborativeMap newFile = newModel.createMap(null);
              newFile.set("title", execute.getFilename());

              for (Tools.MIME_TYPE_Table mimeType : Tools.MIME_TYPE_Table.values()) {
                if (execute.getContentType().equals(mimeType.getMimeType())) {
                  newFile.set("type", mimeType.getType());
                }
              }

              newFile.set("url", DriveModule.DRIVE_SERVER + "/serve?id=" + attachmentId);
              newFile.set("progress", "0");
              newFile.set("status", GlobalConstant.DownloadStatusEnum.WAITING.getStatus());
              newFile.set("blobKey", execute.getBlobKey());

              newList.push(newFile);

              Log.i(TAG, "Add a new download rescourse :" + newFile.toString());
            } while (false);
          } catch (IOException e) {
            e.printStackTrace();
          }
        };
      }.start();
    }
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

      // 删除本地文件
      File file = new File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/" + blobKey);
      if (file.exists()) {
        file.delete();
      }
    }
  }

  public void startObservation(String docId, final String attachmentId) {
    initEventHandler();

    DocumentLoadedHandler onLoaded = new DocumentLoadedHandler() {
      @Override
      public void onLoaded(Document document) {
        doc = document;

        if (null == attachmentId) {
          // 当前用户的离线文件夹
          model = doc.getModel();
          root = model.getRoot();

          list = root.get(GlobalConstant.DocumentIdAndDataKey.OFFLINEKEY.getValue());
          list.addValuesAddedListener(listAddEventHandler);
          list.addValuesRemovedListener(listRemoveEventHandler);
        } else {
          // 远程推送下载的离线文件夹
          model_unlogin = doc.getModel();
          root = model_unlogin.getRoot();

          list_unlogin = root.get(GlobalConstant.DocumentIdAndDataKey.OFFLINEKEY.getValue());
          list_unlogin.addValuesAddedListener(listAddEventHandler);
          list_unlogin.addValuesRemovedListener(listRemoveEventHandler);

          addFile(attachmentId, false);
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
          model_unlogin = model_;
          root = model_unlogin.getRoot();

          root.set(GlobalConstant.DocumentIdAndDataKey.OFFLINEKEY.getValue(), model_unlogin.createList());
        }
      }
    };

    Realtime.load(docId, onLoaded, initializer, null);
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
              File file = new File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/" + resource.get("blobKey"));

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
        }
      };
    } while (false);
  }
}
