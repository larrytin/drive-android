package com.goodow.drive.android.adapter;

import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.goodow.android.drive.R;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.drive.android.global_data_cache.GlobalConstant.MenuTypeEnum;

public class LeftMenuAdapter extends ArrayAdapter<MenuTypeEnum> {

  public LeftMenuAdapter(Context context, int resource, int textViewResourceId, List<MenuTypeEnum> objects) {
    super(context, resource, textViewResourceId, objects);

  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;
    if (null == row) {
      row = ((Activity) this.getContext()).getLayoutInflater().inflate(R.layout.row_leftmenu, parent, false);

    }

    MenuTypeEnum item = getItem(position);
    row.setTag(item);

    TextView listItem = (TextView) row.findViewById(R.id.listItem_leftMenu);
    listItem.setText(item.getMenuName());

    ImageView img_left = (ImageView) row.findViewById(R.id.leftImage_leftMenu);

    switch (item) {
    case USER_NAME:
      listItem.setText(GlobalDataCacheForMemorySingleton.getInstance().getUserName());
      img_left.setImageResource(R.drawable.ic_drive_owned_by_me);

      break;
    case USER_REMOTE_DATA:
      img_left.setImageResource(R.drawable.ic_drive_my_drive);

      break;
    case USER_LESSON_DATA:
      img_left.setImageResource(R.drawable.ic_type_folder);

      break;
    case USER_OFFLINE_DATA:
      img_left.setImageResource(R.drawable.ic_type_zip);

      break;
    // case LOCAL_RES:
    // img_left.setImageResource(R.drawable.ic_type_zip);
    //
    // break;
    default:

      break;
    }

    return row;
  }
}
