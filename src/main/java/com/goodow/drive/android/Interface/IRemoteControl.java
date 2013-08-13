package com.goodow.drive.android.Interface;

import elemental.json.JsonArray;

public interface IRemoteControl {
  public void changeDoc(String docId);

  public void changePath(String mapId, String docId);

  public JsonArray getCurrentPath();

  public void setNotifyData(INotifyData iNotifyData);

}
