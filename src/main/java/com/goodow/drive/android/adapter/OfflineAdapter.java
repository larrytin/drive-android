package com.goodow.drive.android.adapter;

import java.io.File;

import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.goodow.android.drive.R;
import com.goodow.drive.android.Interface.IOnItemClickListener;
import com.goodow.drive.android.activity.MainActivity;
import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.drive.android.toolutils.Tools;
import com.goodow.drive.android.toolutils.ToolsFunctionForThisProgect;
import com.goodow.realtime.CollaborativeList;
import com.goodow.realtime.CollaborativeMap;
import com.goodow.realtime.EventHandler;
import com.goodow.realtime.ValueChangedEvent;

public class OfflineAdapter extends BaseAdapter {
  private final CollaborativeList offlineList;
  private final MainActivity activity;
  private final IOnItemClickListener onItemClickListener;

  public OfflineAdapter(MainActivity activity, CollaborativeList offlineList, IOnItemClickListener onItemClickListener) {
    this.offlineList = offlineList;
    this.activity = activity;
    this.onItemClickListener = onItemClickListener;
  }

  @Override
  public int getCount() {
    int count = (offlineList == null ? 0 : offlineList.length());
    Log.i("offlineAdapter", count + "");
    Log.i("offlineAdapter", offlineList == null ? "offlineList为空" : offlineList.toString());
    return count;
  }

  @Override
  public Object getItem(int position) {
    return offlineList.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    View row = activity.getLayoutInflater().inflate(R.layout.row_offlinelist, parent, false);

    final CollaborativeMap item = (CollaborativeMap) getItem(position);

    ImageView imageView = (ImageView) row.findViewById(R.id.offlineFileIcon);
    String type = "";
    for (Tools.MIME_TYPE_Table mimeType : Tools.MIME_TYPE_Table.values()) {
      if (item.get("type").equals(mimeType.getMimeType())) {
        type = mimeType.getType();
      }
    }
    imageView.setImageResource(ToolsFunctionForThisProgect.getFileIconByFileFullName("." + type));

    final TextView offlinefilename = (TextView) row.findViewById(R.id.offlineFileName);
    offlinefilename.setSelected(true);// 跑马灯效果
    offlinefilename.setText((String) item.get("label"));

    final ProgressBar progressBar = (ProgressBar) row.findViewById(R.id.downloadBar);
    final TextView downloadText = (TextView) row.findViewById(R.id.downloadText);
    String progress = item.get("progress");
    if (null != progress) {
      progressBar.setProgress(Integer.parseInt(progress));
      downloadText.setText(progress + " %");
    }

    final TextView downloadStatus = (TextView) row.findViewById(R.id.downloadStatus);
    downloadStatus.setText((String) item.get("status"));

    final EventHandler<ValueChangedEvent> downloadHandler = new EventHandler<ValueChangedEvent>() {
      @Override
      public void handleEvent(ValueChangedEvent event) {
        String newValue = event.getProperty();

        if ("progress".equals(newValue)) {
          int newProgress = Integer.parseInt((String) event.getNewValue());
          progressBar.setProgress(newProgress);
          downloadText.setText(newProgress + " %");
        }

        if ("status".equals(newValue)) {
          String newStatus = (String) event.getNewValue();
          downloadStatus.setText(newStatus);
        }
      }
    };
    item.addValueChangedListener(downloadHandler);

    ImageButton button = (ImageButton) row.findViewById(R.id.delButton);
    button.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onItemClickListener.onItemClick(item);
      }
    });

    if ("application/x-print".equals(item.get("type"))) {
      button.setVisibility(View.GONE);
    }

    String filePath = GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/" + item.get("blobKey");
    // 加入下载的内容，里面有flash类型,那么加上".swf"
    if (item.get("type").equals("application/x-shockwave-flash")) {
      filePath = filePath + ".swf";
    }
    File file = new File(filePath);

    if (!file.exists()) {
      // RelativeLayout relativeLayout = (RelativeLayout)
      // row.findViewById(R.id.progressMess);
      // relativeLayout.setVisibility(View.GONE);

      progressBar.setProgress(0);
      downloadText.setText(0 + " %");
      downloadStatus.setText(GlobalConstant.DownloadStatusEnum.WAITING.getStatus());
    }

    row.setTag(item);
    return row;

    /**
     * 动态显示下载进度第二套方案
     */
    // if (DownloadResServiceBinder.getDownloadResServiceBinder()
    // .getDownloadResBlobKey().equals(item.get("blobKey"))) {
    //
    // // 使下载service能够更改UI界面,即修改进度条
    // SimpleDownloadResources.getInstance
    // .setDownloadProcess(new IDownloadProcess() {
    // @Override
    // public void downLoadProgress(int progress) {
    // progressBar.setProgress(progress);
    // downloadText.setText(progress + " %");
    //
    // }
    //
    // @Override
    // public void downLoadFinish() {
    // progressBar.setProgress(100);
    // downloadText.setText(100 + " %");
    // }
    // });
    // }
  }

  // private View row;
  // private ProgressBar progressBar;
  // private TextView textView;
  //
  // @SuppressLint("HandlerLeak")
  // private Handler handler = new Handler() {
  // @Override
  // public void handleMessage(Message msg) {
  // switch (msg.what) {
  // case 1:
  // int progress = msg.getData().getInt("progress");
  //
  // if (null != textView) {
  // ((TextView) row.findViewById(R.id.downloadText)).setText(progress + " %");
  // }
  //
  // if (null != progressBar) {
  // ((ProgressBar) activity.findViewById(10)).setProgress(progress);
  // progressBar.setProgress(progress);
  // }
  //
  // break;
  // case -1:
  // if (null != textView) {
  // textView.setText("100 %");
  // }
  //
  // if (null != progressBar) {
  // progressBar.setProgress(100);
  // }
  //
  // break;
  // }
  // }
  // };

}
