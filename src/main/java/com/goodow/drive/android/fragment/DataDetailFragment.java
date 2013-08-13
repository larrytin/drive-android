package com.goodow.drive.android.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

import android.R.integer;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import com.goodow.android.drive.R;
import com.goodow.drive.android.Interface.ILocalFragment;
import com.goodow.drive.android.activity.MainActivity;
import com.goodow.drive.android.global_data_cache.GlobalConstant.DownloadStatusEnum;
import com.goodow.drive.android.toolutils.OfflineFileObserver;
import com.goodow.realtime.CollaborativeList;
import com.goodow.realtime.CollaborativeMap;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

public class DataDetailFragment extends Fragment implements ILocalFragment {
  private static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private CollaborativeMap file;
  private TextView fileName;
  private ProgressBar progressBar;
  public ImageView imageView;
  private Switch downloadSwitch;

  public void backFragment() {
    MainActivity activity = (MainActivity) getActivity();

    activity.setDataDetailLayoutState(View.INVISIBLE);

    activity.setLocalFragment(activity.getLastiRemoteDataFragment());
  }

  public void setFile(CollaborativeMap file) {
    this.file = file;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_datadetail, container, false);
  }

  @Override
  public void onResume() {
    super.onResume();

    fileName = (TextView) getActivity().findViewById(R.id.fileName);
    progressBar = (ProgressBar) getActivity().findViewById(R.id.thumbnailProgressBar);
    imageView = (ImageView) getActivity().findViewById(R.id.thumbnail);

    downloadSwitch = (Switch) getActivity().findViewById(R.id.downloadButton);
    downloadSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          file.set("status", DownloadStatusEnum.WAITING.getStatus());
          String attachmentId = file.get("id");
          OfflineFileObserver.OFFLINEFILEOBSERVER.addFile(attachmentId, true);
        } else {
          OfflineFileObserver.OFFLINEFILEOBSERVER.removeFile(file);
        }
      }
    });
  }

  public void initView() {
    if (null != file) {
      fileName.setText((String) file.get("label"));

      progressBar.setVisibility(View.VISIBLE);
      imageView.setVisibility(View.GONE);
      InitImageBitmapTask ibt = new InitImageBitmapTask();
      ibt.execute((String) file.get("thumbnail"));

      boolean isOffline = false;
      CollaborativeList list = OfflineFileObserver.OFFLINEFILEOBSERVER.getList();
      for (int i = 0; i < list.length(); i++) {
        CollaborativeMap map = list.get(i);
        if (file.get("blobKey").equals(map.get("blobKey"))) {
          isOffline = true;
        }
      }
      downloadSwitch.setChecked(isOffline);

      // Comparator<Object> comparator = new Comparator<Object>() {
      // @Override
      // public int compare(Object obj1, Object obj2) {
      // CollaborativeMap file1 = (CollaborativeMap) obj1;
      // CollaborativeMap file2 = (CollaborativeMap) obj2;
      // do {
      // if (null == file1 || null == file1.get("blobKey")) {
      //
      // break;
      // }
      //
      // if (null == file2 || null == file2.get("blobKey")) {
      //
      // break;
      // }
      //
      // String blobKey1 = file1.get("blobKey");
      // String blobKey2 = file2.get("blobKey");
      //
      // if (blobKey1.equals(blobKey2)) {
      // return 0;
      // }
      // } while (false);
      //
      // return 1;
      // }
      // };
      //
      // if (0 ==
      // OfflineFileObserver.OFFLINEFILEOBSERVER.getList().indexOf(file,
      // comparator)) {
      // downloadSwitch.setChecked(true);
      // } else {
      // downloadSwitch.setChecked(false);
      // }
    }
  }

  @Override
  public void connectUi() {
    // TODO Auto-generated method stub

  }

  @Override
  public void loadDocument() {
    // TODO Auto-generated method stub

  }

  private class InitImageBitmapTask extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... params) {
      HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) {
          request.setParser(new JsonObjectParser(JSON_FACTORY));
        }
      });

      Bitmap bitmap = null;
      HttpRequest request;
      try {
        request = requestFactory.buildGetRequest(new GenericUrl(params[0]));
        HttpResponse response = request.execute();
        InputStream is_Bitmap = response.getContent();
        bitmap = BitmapFactory.decodeStream(is_Bitmap);
      } catch (IOException e) {
        e.printStackTrace();
      }

      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      super.onPostExecute(result);
      progressBar.setVisibility(View.GONE);
      imageView.setVisibility(View.VISIBLE);
      imageView.setImageBitmap(result);
    }
  }
}
