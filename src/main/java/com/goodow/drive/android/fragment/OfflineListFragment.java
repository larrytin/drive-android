package com.goodow.drive.android.fragment;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Interface.ILocalFragment;
import com.goodow.drive.android.Interface.IOnItemClickListener;
import com.goodow.drive.android.activity.MainActivity;
import com.goodow.drive.android.adapter.OfflineAdapter;
import com.goodow.drive.android.toolutils.OfflineFileObserver;
import com.goodow.realtime.CollaborativeList;
import com.goodow.realtime.CollaborativeMap;

import android.app.ActionBar;
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

public class OfflineListFragment extends ListFragment implements ILocalFragment {
  private final String TAG = getClass().getSimpleName();

  private OfflineAdapter adapter;

  protected CollaborativeList list;

  private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      adapter.notifyDataSetChanged();
      // 当得到数据后，刷新离线的listView
      if ("com.goodow.drive.android.offlineFileObserver".equals(intent.getAction())) {
        Log.i("offlineAdapter", "接收到了广播");
        list = OfflineFileObserver.OFFLINEFILEOBSERVER.getList();
        adapter =
            new OfflineAdapter((MainActivity) OfflineListFragment.this.getActivity(), list,
                new IOnItemClickListener() {
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
      }
    }
  };

  public OfflineListFragment() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.goodow.drive.android.Interface.ILocalFragment#backFragment()
   */
  @Override
  public void backFragment() {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.goodow.drive.android.Interface.ILocalFragment#connectUi()
   */
  @Override
  public void connectUi() {
    // TODO Auto-generated method stub

  }

  @Override
  public void doSearch(String search) {
    // TODO Auto-generated method stub

  }

  @Override
  public void loadDocument() {
    // TODO Auto-generated method stub
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    ActionBar actionBar = getActivity().getActionBar();
    actionBar.setTitle("离线文件");
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setDisplayShowCustomEnabled(false);

    list = OfflineFileObserver.OFFLINEFILEOBSERVER.getList();
    Log.i("offlineAdapter", "OfflineListFragment中list=" + (list == null) + "");
    // 此处第一次进入时，list为空。
    adapter =
        new OfflineAdapter((MainActivity) this.getActivity(), list, new IOnItemClickListener() {
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
      intentFilter.addAction("com.goodow.drive.android.offlineFileObserver");
      activity.registerReceiver(broadcastReceiver, intentFilter);

      RelativeLayout relativeLayout = (RelativeLayout) activity.findViewById(R.id.mainConnect);
      relativeLayout.setVisibility(View.GONE);

      // 隐藏搜索图标
      activity.showOrHiddenSearchView(false);
    }
  }
}