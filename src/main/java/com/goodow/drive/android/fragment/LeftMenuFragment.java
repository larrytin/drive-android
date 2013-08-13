package com.goodow.drive.android.fragment;

import java.util.ArrayList;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;
import com.goodow.android.drive.R;
import com.goodow.drive.android.activity.MainActivity;
import com.goodow.drive.android.adapter.LeftMenuAdapter;
import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.global_data_cache.GlobalConstant.MenuTypeEnum;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;

public class LeftMenuFragment extends ListFragment {
  private LeftMenuAdapter adapter;
  private MainActivity mainActivity;

  private ArrayList<MenuTypeEnum> MENULIST = new ArrayList<MenuTypeEnum>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mainActivity = (MainActivity) getActivity();

    for (MenuTypeEnum type : MenuTypeEnum.values()) {
      MENULIST.add(type);

    }
    adapter = new LeftMenuAdapter(getActivity(), R.layout.row_leftmenu, 0, MENULIST);
    setListAdapter(adapter);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    return inflater.inflate(R.layout.fragment_leftmenu, container, false);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    MenuTypeEnum menuTypeEnum = (MenuTypeEnum) v.getTag();

    switch (menuTypeEnum) {
    case USER_NAME:
      mainActivity.showChangeUserDialog();

      break;
    case USER_REMOTE_DATA:
      String favoritesDocId = "@tmp/" + GlobalDataCacheForMemorySingleton.getInstance().getUserId() + "/"
          + GlobalConstant.DocumentIdAndDataKey.FAVORITESDOCID.getValue();
      mainActivity.getRemoteControlObserver().changeDoc(favoritesDocId);

      break;
    case USER_LESSON_DATA:
      String lessonDocId = "@tmp/" + GlobalDataCacheForMemorySingleton.getInstance().getUserId() + "/"
          + GlobalConstant.DocumentIdAndDataKey.LESSONDOCID.getValue();
      mainActivity.getRemoteControlObserver().changeDoc(lessonDocId);

      break;
    case USER_OFFLINE_DATA:
      String offlineDocId = "@tmp/" + GlobalDataCacheForMemorySingleton.getInstance().getUserId() + "/"
          + GlobalConstant.DocumentIdAndDataKey.OFFLINEDOCID.getValue();
      mainActivity.getRemoteControlObserver().changeDoc(offlineDocId);

      break;
    // case LOCAL_RES:
    // FragmentTransaction fragmentTransaction;
    // fragmentTransaction =
    // mainActivity.getFragmentManager().beginTransaction();
    // fragmentTransaction.replace(R.id.contentLayout,
    // mainActivity.getLocalResFragment());
    // fragmentTransaction.commit();
    //
    // break;
    default:

      break;
    }

    mainActivity.hideLeftMenuLayout();
    mainActivity.setDataDetailLayoutState(View.INVISIBLE);
  }

  public void hiddenView() {
    View view = getView();
    Animation out = AnimationUtils.makeOutAnimation(getActivity(), false);
    view.startAnimation(out);
    view.setVisibility(View.INVISIBLE);
  }

  public void setViewLayout(int x) {
    View view = getView();
    view.layout(x, view.getTop(), view.getRight(), view.getBottom());
  }

  public void showView() {
    View view = getView();
    Animation in = AnimationUtils.makeInAnimation(getActivity(), true);
    view.startAnimation(in);
    view.setVisibility(View.VISIBLE);
  }

  public void notifyData() {
    if (null != adapter) {
      adapter.notifyDataSetChanged();
    }
  }
}
