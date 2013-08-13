package com.goodow.drive.android.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import com.goodow.android.drive.R;
import com.goodow.drive.android.Interface.ILocalFragment;
import com.goodow.drive.android.activity.MainActivity;
import com.goodow.drive.android.activity.play.VideoPlayActivity;
import com.goodow.drive.android.adapter.LocalResAdapter;
import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.drive.android.toolutils.Tools;

public class LocalResFragment extends ListFragment implements ILocalFragment {
  private LocalResAdapter localResAdapter;

  private ArrayList<File> folderList = new ArrayList<File>();
  // 保存当前级文件的父文件路径
  private String parentDirectory = null;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_folderlist, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    ((MainActivity) getActivity()).setLocalFragment(this);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    File file = new File((String) v.getTag());

    if (file.isDirectory()) {
      parentDirectory = file.getParentFile().getAbsolutePath();
      initDataSource(file);
    } else {
      if (file.exists()) {
        String fileName = file.getName();
        Map<String, String> map = new HashMap<String, String>();
        map.put("label", fileName);
        map.put("type", fileName.substring(fileName.lastIndexOf(".") + 1));
        map.put("blobKey", fileName);

        Intent intent = null;

        String resPath = GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath() + "/";

        if (GlobalConstant.SupportResTypeEnum.MP4.getTypeName().equals(map.get("type"))) {
          intent = new Intent(getActivity(), VideoPlayActivity.class);

          intent.putExtra(VideoPlayActivity.IntentExtraTagEnum.MP4_NAME.name(), (String) map.get("label"));
          intent.putExtra(VideoPlayActivity.IntentExtraTagEnum.MP4_PATH.name(), resPath + (String) map.get("blobKey"));
        } else if (GlobalConstant.SupportResTypeEnum.FLASH.getTypeName().equals(map.get("type"))) {
          // TODO
        } else {
          intent = new Intent();

          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.setAction(Intent.ACTION_VIEW);
          String type = Tools.getMIMETypeByType((String) map.get("type"));
          intent.setDataAndType(Uri.fromFile(file), type);
        }

        getActivity().startActivity(intent);
      }
    }
  }

  public void backFragment() {
    if (null != parentDirectory) {
      initDataSource(new File(parentDirectory));

      if (parentDirectory.equals(GlobalDataCacheForMemorySingleton.getInstance().getOfflineResDirPath())) {
        
        parentDirectory = null;// 如果返回至用户文件夹,则置空父文件路径
      } else {
        
        parentDirectory = new File(parentDirectory).getParentFile().getAbsolutePath();
      }
    } else {
      
      Toast.makeText(this.getActivity(), R.string.backFolderErro, Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    if (null == localResAdapter) {
     
      localResAdapter = new LocalResAdapter(folderList, this);
    }

    setListAdapter(localResAdapter);

    initDataSource(new File(GlobalDataCacheForMemorySingleton.getInstance.getOfflineResDirPath()));
  }

  public void delFile(File file) {
    if (file == null) {
     
      assert false : "入参file为空!";
      return;
    }

    if (file.isDirectory()) {
      for (File item : file.listFiles()) {
        if (item.isDirectory()) {
          
          delFile(item);
        } else {
          
          item.delete();
        }
      }
    }

    file.delete();
  }

  public void initDataSource(File dir) {
    if (dir == null) {
      
      assert false : "入参file为空!";
      return;
    }

    if (dir.exists() && dir.isDirectory()) {
      folderList.clear();

      for (File file : dir.listFiles()) {
        
        folderList.add(file);
      }
    }

    localResAdapter.notifyDataSetChanged();
  }

  @Override
  public void connectUi() {
    // TODO Auto-generated method stub

  }

  @Override
  public void loadDocument() {
    // TODO Auto-generated method stub
    
  }
}
