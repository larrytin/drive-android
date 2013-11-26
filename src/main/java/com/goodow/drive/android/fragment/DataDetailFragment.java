package com.goodow.drive.android.fragment;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Interface.ILocalFragment;
import com.goodow.drive.android.activity.MainActivity;
import com.goodow.drive.android.global_data_cache.GlobalConstant.DownloadStatusEnum;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.drive.android.toolutils.OfflineFileObserver;
import com.goodow.drive.android.toolutils.Tools;
import com.goodow.realtime.CollaborativeMap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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
import android.widget.TextView;
import android.widget.ToggleButton;

public class DataDetailFragment extends Fragment implements ILocalFragment {

  private class InitImageBitmapTask extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... params) {
      Bitmap bitmap = null;
      try {
        URLConnection connection = (new URL(params[0]).openConnection());
        InputStream bitmapStream = connection.getInputStream();
        bitmap = BitmapFactory.decodeStream(bitmapStream);
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

  private CollaborativeMap file;
  private TextView fileName;
  private ProgressBar progressBar;
  public ImageView imageView;

  private ToggleButton downloadToggle;

  public DataDetailFragment() {
    super();
  }

  @Override
  public void backFragment() {
    MainActivity activity = (MainActivity) getActivity();

    activity.setDataDetailLayoutState(View.INVISIBLE);

    activity.setLocalFragment(activity.getLastiRemoteDataFragment());
  }

  @Override
  public void connectUi() {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.goodow.drive.android.Interface.ILocalFragment#doSearch(java.lang.String)
   */
  @Override
  public void doSearch(String search) {
    // TODO Auto-generated method stub

  }

  public void initView() {
    if (null != file) {
      fileName.setText((String) file.get("label"));

      imageView.setVisibility(View.VISIBLE);
      String thumbnail = file.get("thumbnail");
      if (null != thumbnail) {
        // 修正缩略图地址
        thumbnail = Tools.modifyThumbnailAddress(thumbnail);
        InitImageBitmapTask ibt = new InitImageBitmapTask();
        ibt.execute(thumbnail);
      } else {
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),
            R.drawable.ic_thumbnail));
      }

      String blobKey = file.get("blobKey");
      boolean isOffline = false;
      // CollaborativeList list = OfflineFileObserver.OFFLINEFILEOBSERVER.getList();
      // for (int i = 0; i < list.length(); i++) {
      // CollaborativeMap map = list.get(i);
      //
      // if (null != blobKey && blobKey.equals(map.get("blobKey"))) {
      // File localFile = new
      // File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/" + blobKey);
      // if (localFile.exists()) {
      // isOffline = true;
      // }
      // }
      // }
      // 本地文件
      // File files = new File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath()
      // + "/" + blobKey);
      String filePath =
          GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/" + blobKey;
      // 加入下载的内容，里面有flash类型,那么加上".swf"
      if (file.get("type").equals("application/x-shockwave-flash")) {
        filePath = filePath + ".swf";
      }
      File files = new File(filePath);
      if (files.exists()) {
        isOffline = true;
      }
      downloadToggle.setOnCheckedChangeListener(null);
      downloadToggle.setChecked(isOffline);
      downloadToggle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
          if (isChecked) {
            file.set("status", DownloadStatusEnum.WAITING.getStatus());
            String attachmentId = file.get("id");
            OfflineFileObserver.OFFLINEFILEOBSERVER.addFile(attachmentId, true, null, null);
          } else {
            OfflineFileObserver.OFFLINEFILEOBSERVER.removeFile(file);
          }
        }
      });

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
  public void loadDocument() {
    // TODO Auto-generated method stub

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_datadetail, container, false);
  }

  @Override
  public void onResume() {
    super.onResume();

    MainActivity activity = (MainActivity) getActivity();
    if (null != activity) {
      activity.setLocalFragmentForDetail(this);

      fileName = (TextView) activity.findViewById(R.id.fileName);
      progressBar = (ProgressBar) activity.findViewById(R.id.thumbnailProgressBar);
      imageView = (ImageView) activity.findViewById(R.id.thumbnail);
      downloadToggle = (ToggleButton) activity.findViewById(R.id.downloadButton);
    }
  }

  public void setFile(CollaborativeMap file) {
    this.file = file;
  }

}
