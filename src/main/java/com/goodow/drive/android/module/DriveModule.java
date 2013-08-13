package com.goodow.drive.android.module;

import com.goodow.api.services.account.Account;
import com.goodow.api.services.attachment.Attachment;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.drive.android.toolutils.MyApplication;
import com.goodow.realtime.android.CloudEndpointUtils;
import com.goodow.realtime.android.RealtimeModule;
import com.goodow.realtime.android.ServerAddress;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.io.File;

import android.os.Environment;

public class DriveModule extends AbstractModule {
  public static final String DRIVE_SERVER = "http://192.168.1.15:8880";

  // public static final String DRIVE_SERVER = "http://server.drive.goodow.com";

  @Override
  protected void configure() {
    File file;
    String filePathString = "";
    File storage;

    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      storage = Environment.getExternalStorageDirectory();
      filePathString = "/retech";

    } else {
      storage = Environment.getDataDirectory();
      filePathString = "/data/com.goodow.drive.android/retech";

    }

    file = new File(storage, filePathString);

    if (!file.exists()) {
      file.mkdir();

    }

    bind(MyApplication.class).asEagerSingleton();

    GlobalDataCacheForMemorySingleton.getInstance().setStoragePaht(file.getAbsolutePath());

  }

  @Provides
  @Singleton
  Attachment provideAttachment() {
    Attachment.Builder endpointBuilder = new Attachment.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), new HttpRequestInitializer() {
      @Override
      public void initialize(HttpRequest httpRequest) {

      }
    });
    endpointBuilder.setRootUrl(RealtimeModule.getEndpointRootUrl(DRIVE_SERVER));
    return CloudEndpointUtils.updateBuilder(endpointBuilder).build();
  }

  @Provides
  @Singleton
  Account provideDevice(@ServerAddress String serverAddress) {
    Account.Builder endpointBuilder = new Account.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), new HttpRequestInitializer() {
      @Override
      public void initialize(HttpRequest httpRequest) {

      }
    });
    endpointBuilder.setRootUrl(RealtimeModule.getEndpointRootUrl(serverAddress));
    return CloudEndpointUtils.updateBuilder(endpointBuilder).build();
  }
}
