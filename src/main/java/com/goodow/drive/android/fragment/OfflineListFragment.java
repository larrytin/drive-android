package com.goodow.drive.android.fragment;

import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Interface.ILocalFragment;
import com.goodow.drive.android.activity.MainActivity;
import com.goodow.drive.android.adapter.OfflineAdapter;
import com.goodow.drive.android.adapter.CollaborativeAdapter.OnItemClickListener;
import com.goodow.drive.android.toolutils.OfflineFileObserver;
import com.goodow.realtime.CollaborativeList;
import com.goodow.realtime.CollaborativeMap;

public class OfflineListFragment extends ListFragment implements ILocalFragment {
  private final String TAG = getClass().getSimpleName();

  private OfflineAdapter adapter;

  private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      adapter.notifyDataSetChanged();
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    CollaborativeList list = OfflineFileObserver.OFFLINEFILEOBSERVER.getList();
    adapter = new OfflineAdapter((MainActivity) this.getActivity(), list, new OnItemClickListener() {
      @Override
      public void onItemClick(CollaborativeMap file) {
        MainActivity activity = (MainActivity) OfflineListFragment.this.getActivity();

        DataDetailFragment dataDetailFragment = activity.getDataDetailFragment();
        dataDetailFragment.setFile(file);
        dataDetailFragment.initView();

        activity.setDataDetailLayoutState(View.VISIBLE);
        activity.setLocalFragmentForDetail(dataDetailFragment);
      }
    });
    setListAdapter(adapter);
  };

  @Override
  public void onResume() {
    super.onResume();

    Log.i(TAG, "onResume()");

    MainActivity activity = (MainActivity) getActivity();
    if (null != activity) {
      Log.i(TAG, "onResume()-activty is not null");

      activity.setLocalFragment(this);
      activity.setLastiRemoteDataFragment(this);

      activity.setActionBarTitle("离线文件");

      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction("CHANGE_OFFLINE_STATE");

      activity.registerReceiver(broadcastReceiver, intentFilter);

      RelativeLayout relativeLayout = (RelativeLayout) activity.findViewById(R.id.mainConnect);
      relativeLayout.setVisibility(View.GONE);
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_folderlist, container, false);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    CollaborativeMap item = (CollaborativeMap) v.getTag();

    ((MainActivity) getActivity()).getRemoteControlObserver().playFile(item);
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