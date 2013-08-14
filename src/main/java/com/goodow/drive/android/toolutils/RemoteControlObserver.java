package com.goodow.drive.android.toolutils;

import java.io.File;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.goodow.drive.android.Interface.INotifyData;
import com.goodow.drive.android.Interface.IRemoteControl;
import com.goodow.drive.android.Interface.ISwitchFragment;
import com.goodow.drive.android.activity.play.AudioPlayActivity;
import com.goodow.drive.android.activity.play.VideoPlayActivity;
import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.global_data_cache.GlobalConstant.DocumentIdAndDataKey;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.realtime.CollaborativeList;
import com.goodow.realtime.CollaborativeMap;
import com.goodow.realtime.Document;
import com.goodow.realtime.DocumentLoadedHandler;
import com.goodow.realtime.EventHandler;
import com.goodow.realtime.Model;
import com.goodow.realtime.ModelInitializerHandler;
import com.goodow.realtime.Realtime;
import com.goodow.realtime.ValueChangedEvent;
import com.goodow.realtime.ValuesAddedEvent;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import elemental.json.impl.JreJsonString;

public class RemoteControlObserver implements IRemoteControl {
  private final String TAG = getClass().getSimpleName();

  private ISwitchFragment iswitchfragment;
  private Document doc;
  private Model model;
  private CollaborativeMap root;
  private CollaborativeList playFileList;

  private INotifyData iNotifyData;

  private EventHandler<ValuesAddedEvent> playFileHandler = new EventHandler<ValuesAddedEvent>() {
    @Override
    public void handleEvent(ValuesAddedEvent event) {
      CollaborativeMap map = (CollaborativeMap) event.getValues()[0];

      File file = new File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/" + map.get("blobKey"));

      if (file.exists()) {
        Intent intent = null;

        String resPath = GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/";

        if (GlobalConstant.SupportResTypeEnum.MP3.getTypeName().equals(Tools.getTypeByMimeType((String) map.get("type")))) {
          intent = new Intent(MyApplication.getApplication(), AudioPlayActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.putExtra(AudioPlayActivity.IntentExtraTagEnum.MP3_NAME.name(), (String) map.get("label"));
          intent.putExtra(AudioPlayActivity.IntentExtraTagEnum.MP3_PATH.name(), resPath + (String) map.get("blobKey"));
        } else if (GlobalConstant.SupportResTypeEnum.MP4.getTypeName().equals(Tools.getTypeByMimeType((String) map.get("type")))) {
          intent = new Intent(MyApplication.getApplication(), VideoPlayActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.putExtra(VideoPlayActivity.IntentExtraTagEnum.MP4_NAME.name(), (String) map.get("label"));
          intent.putExtra(VideoPlayActivity.IntentExtraTagEnum.MP4_PATH.name(), resPath + (String) map.get("blobKey"));
        } else if (GlobalConstant.SupportResTypeEnum.FLASH.getTypeName().equals(Tools.getTypeByMimeType((String) map.get("type")))) {
          // TODO
        } else {
          intent = new Intent();
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.setAction(Intent.ACTION_VIEW);
          String type = map.get("type");
          intent.setDataAndType(Uri.fromFile(file), type);
        }
        MyApplication.getApplication().startActivity(intent);
        // getActivity().startActivity(Intent.createChooser(intent,
        // "请选择打开程序…"));
      } else {
        Toast.makeText(MyApplication.getApplication(), "请先下载该文件.", Toast.LENGTH_SHORT).show();
      }
    }
  };

  private EventHandler<ValueChangedEvent> handler = new EventHandler<ValueChangedEvent>() {
    @Override
    public void handleEvent(ValueChangedEvent event) {
      String property = event.getProperty();
      if (GlobalConstant.DocumentIdAndDataKey.PATHKEY.getValue().equals(property)) {
        JsonObject newJson = (JsonObject) event.getNewValue();
        Log.i(TAG, "new path: " + newJson.toString());

        updateUi(newJson);

        if (null != iNotifyData) {
          iNotifyData.notifyData(newJson);
        }
      }
    }
  };

  public RemoteControlObserver(ISwitchFragment iswitchfragment) {
    this.iswitchfragment = iswitchfragment;
  }

  @Override
  public JsonArray getCurrentPath() {
    if (null != root) {
      JsonObject map = root.get(GlobalConstant.DocumentIdAndDataKey.PATHKEY.getValue());

      return map.get(GlobalConstant.DocumentIdAndDataKey.CURRENTPATHKEY.getValue());
    }

    return null;
  }

  @Override
  public void changeDoc(String docId) {
    iNotifyData = null;

    if (null != root) {
      JsonObject map = root.get(GlobalConstant.DocumentIdAndDataKey.PATHKEY.getValue());
      JsonArray jsonArray = map.get(GlobalConstant.DocumentIdAndDataKey.CURRENTPATHKEY.getValue());
      for (int i = jsonArray.length() - 1; i > 0; i--) {
        jsonArray.remove(i);
      }
      jsonArray.set(0, "root");

      map.put(GlobalConstant.DocumentIdAndDataKey.CURRENTPATHKEY.getValue(), jsonArray);
      map.put(GlobalConstant.DocumentIdAndDataKey.CURRENTDOCIDKEY.getValue(), docId);

      root.set(GlobalConstant.DocumentIdAndDataKey.PATHKEY.getValue(), map);
    }
  }

  @Override
  public void changePath(String mapId, String docId) {
    if (null != root) {
      JsonObject map = root.get(GlobalConstant.DocumentIdAndDataKey.PATHKEY.getValue());

      JsonArray jsonArray = map.get(GlobalConstant.DocumentIdAndDataKey.CURRENTPATHKEY.getValue());

      if (null != mapId) {
        jsonArray.set(jsonArray.length(), mapId);
      } else {
        if (jsonArray.length() > 0) {
          jsonArray.remove(jsonArray.length() - 1);
        }
      }

      map.put(GlobalConstant.DocumentIdAndDataKey.CURRENTPATHKEY.getValue(), jsonArray);
      map.put(GlobalConstant.DocumentIdAndDataKey.CURRENTDOCIDKEY.getValue(), docId);
      root.set(GlobalConstant.DocumentIdAndDataKey.PATHKEY.getValue(), map);
    }
  }

  @Override
  public void setNotifyData(INotifyData iNotifyData) {
    this.iNotifyData = iNotifyData;
  }

  private void updateUi(JsonObject map) {
    JreJsonString jreJsonString = (JreJsonString) (map.get(GlobalConstant.DocumentIdAndDataKey.CURRENTDOCIDKEY.getValue()));

    if (null != jreJsonString) {
      String lastDocId = jreJsonString.asString();

      lastDocId = lastDocId.substring(lastDocId.lastIndexOf("/") + 1, lastDocId.length());

      DocumentIdAndDataKey doc = DocumentIdAndDataKey.getEnumWithValue(lastDocId);

      iswitchfragment.switchFragment(doc);
    }
  }

  public void startObservation(String docId) {
    if (null != root) {
      root.removeValueChangedListener(handler);
    }

    DocumentLoadedHandler onLoaded = new DocumentLoadedHandler() {
      @Override
      public void onLoaded(Document document) {
        doc = document;
        model = doc.getModel();
        root = model.getRoot();

        playFileList = root.get(GlobalConstant.DocumentIdAndDataKey.PLAYFILE.getValue());
        if (null == playFileList) {
          playFileList = model.createList();
          root.set(GlobalConstant.DocumentIdAndDataKey.PLAYFILE.getValue(), playFileList);
        }
        playFileList.addValuesAddedListener(playFileHandler);

        JsonObject map = root.get(GlobalConstant.DocumentIdAndDataKey.PATHKEY.getValue());
        Log.i(TAG, GlobalDataCacheForMemorySingleton.getInstance.getUserName() + "-root: " + root.toString());

        root.addValueChangedListener(handler);

        JreJsonString jreJsonString = (JreJsonString) (map.get(GlobalConstant.DocumentIdAndDataKey.CURRENTDOCIDKEY.getValue()));
        if (null != jreJsonString) {
          String lastDocId = jreJsonString.asString();

          lastDocId = lastDocId.substring(lastDocId.lastIndexOf("/") + 1, lastDocId.length());

          DocumentIdAndDataKey doc = DocumentIdAndDataKey.getEnumWithValue(lastDocId);

          if (null != doc) {
            iswitchfragment.switchFragment(doc);
          } else {
            changeDoc("@tmp/" + GlobalDataCacheForMemorySingleton.getInstance().getUserId() + "/"
                + GlobalConstant.DocumentIdAndDataKey.FAVORITESDOCID.getValue());
          }
        }
      }
    };

    ModelInitializerHandler initializer = new ModelInitializerHandler() {
      @Override
      public void onInitializer(Model model_) {
        model = model_;
        root = model.getRoot();

        JsonObject jsonObject = Json.createObject();
        JsonArray jsonArray = Json.createArray();
        jsonArray.set(0, "root");

        jsonObject.put(GlobalConstant.DocumentIdAndDataKey.CURRENTPATHKEY.getValue(), jsonArray);
        jsonObject.put(GlobalConstant.DocumentIdAndDataKey.CURRENTDOCIDKEY.getValue(), "");

        root.set(GlobalConstant.DocumentIdAndDataKey.PATHKEY.getValue(), jsonObject);
      }
    };

    Realtime.load(docId, onLoaded, initializer, null);
  }

  public void removeHandler() {
    if (null != playFileList) {
      playFileList.removeListListener(playFileHandler);
    }

    if (null != root) {
      root.removeValueChangedListener(handler);
    }
  }

  @Override
  public void playFile(CollaborativeMap file) {
    if (null != file) {
      CollaborativeMap playFile = model.createMap(null);
      playFile.set("label", file.get("label"));
      playFile.set("blobKey", file.get("blobKey"));
      playFile.set("type", file.get("type"));

      if (50 < playFileList.length()) {
        playFileList.clear();
      }

      playFileList.push(playFile);
    } else {
      assert false : "入参file为null!";
    }
  }
}
