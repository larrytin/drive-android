package com.goodow.drive.android.fragment;

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Interface.ILocalFragment;
import com.goodow.drive.android.activity.MainActivity;
import com.goodow.drive.android.adapter.OfflineAdapter;
import com.goodow.drive.android.toolutils.OfflineFileObserver;
import com.goodow.realtime.CollaborativeList;

public class OfflineListFragment extends ListFragment implements ILocalFragment {
  private OfflineAdapter adapter;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

      adapter.notifyDataSetChanged();
    }
  };

  @Override
  public void onResume() {
    super.onResume();

    MainActivity activity = (MainActivity) getActivity();
    if (null != activity) {
      activity.setActionBarTitle("离线文件");
    }
    RelativeLayout relativeLayout = (RelativeLayout) getActivity().findViewById(R.id.mainConnect);
    relativeLayout.setVisibility(View.GONE);

    CollaborativeList list = OfflineFileObserver.OFFLINEFILEOBSERVER.getList();
    adapter = new OfflineAdapter((MainActivity) this.getActivity(), list);
    setListAdapter(adapter);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_folderlist, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    MainActivity activity = (MainActivity) getActivity();
    activity.setLocalFragment(this);

    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("NEW_RES_DOWNLOADING");
    activity.registerReceiver(broadcastReceiver, intentFilter);
  }

  @Override
  public void onPause() {
    super.onPause();

    ((MainActivity) getActivity()).unregisterReceiver(broadcastReceiver);
  }

  @Override
  public void connectUi() {
    // TODO Auto-generated method stub

  }

  @Override
  public void loadDocument() {
    // TODO Auto-generated method stub

  }

  @Override
  public void backFragment() {
    // TODO Auto-generated method stub

  }
}