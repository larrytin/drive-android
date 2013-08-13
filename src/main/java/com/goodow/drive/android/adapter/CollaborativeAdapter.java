package com.goodow.drive.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.goodow.android.drive.R;
import com.goodow.drive.android.toolutils.Tools;
import com.goodow.drive.android.toolutils.ToolsFunctionForThisProgect;
import com.goodow.realtime.CollaborativeList;
import com.goodow.realtime.CollaborativeMap;

public class CollaborativeAdapter extends BaseAdapter {
  public static abstract interface OnItemClickListener {
    public abstract void onItemClick(CollaborativeMap file);
  }

  private CollaborativeList folderList;
  private CollaborativeList fileList;
  private LayoutInflater layoutInflater;
  private OnItemClickListener onItemClickListener;

  public CollaborativeAdapter(Context context, CollaborativeList folderList, CollaborativeList fileList, OnItemClickListener onItemClickListener) {
    this.folderList = folderList;
    this.fileList = fileList;
    this.layoutInflater = LayoutInflater.from(context);
    this.onItemClickListener = onItemClickListener;
  }

  @Override
  public int getCount() {
    int count = 0;

    do {
      if (null == folderList && null == fileList) {
        break;
      }

      if ((null == fileList || fileList.length() == 0) && null != folderList && folderList.length() != 0) {
        count = folderList.length() + 1;
        break;
      }

      if ((null == folderList || folderList.length() == 0) && null != fileList && fileList.length() != 0) {
        count = fileList.length() + 1;
        break;
      }

      if (folderList.length() != 0 && fileList.length() != 0) {
        count = folderList.length() + fileList.length() + 2;
        break;
      }

    } while (false);

    return count;
  }

  @Override
  public Object getItem(int position) {
    Object object = null;
    do {
      if (null == folderList && null == fileList) {
        break;
      }

      if (position == 0) {
        break;// 分组标题("文件夹"or"文件")
      }

      position = position - 1;

      if (null != folderList && folderList.length() != 0) {
        if (position < folderList.length()) {
          object = folderList.get(position);// 子元素-文件夹
          break;
        }

        if (position == folderList.length()) {
          break;// 分组标题("文件夹"or"文件")
        }

        position = position - 1 - folderList.length();
      }

      if (null != fileList) {
        object = fileList.get(position);// 子元素-文件
        break;
      }

    } while (false);

    return object;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final CollaborativeMap item = (CollaborativeMap) getItem(position);
    String textViewContent = "";
    View row = convertView;

    if (0 == position && null != folderList && folderList.length() != 0) {
      row = layoutInflater.inflate(R.layout.row_foldergroup, parent, false);
      textViewContent = "文件夹";
    } else if ((0 == position && (null == folderList || folderList.length() == 0)) || (0 != position && null == item)) {
      row = layoutInflater.inflate(R.layout.row_foldergroup, parent, false);
      textViewContent = "文件";
    } else {
      row = layoutInflater.inflate(R.layout.row_folderlist, parent, false);

      row.setTag(item);

      ImageView img_left = (ImageView) row.findViewById(R.id.leftImage);
      ImageButton button = (ImageButton) row.findViewById(R.id.delButton);

      if (null != folderList && position < folderList.length() + 1) {
        img_left.setImageResource(R.drawable.ic_type_folder);
        button.setVisibility(View.INVISIBLE);
      } else {
        img_left.setImageResource(ToolsFunctionForThisProgect.getFileIconByFileFullName("." + Tools.getTypeByMimeType((String) item.get("type"))));

        button.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            onItemClickListener.onItemClick(item);
          }
        });
      }

      textViewContent = (String) item.get("label");
    }

    TextView listItem = (TextView) row.findViewById(R.id.listItem);
    listItem.setText(textViewContent);

    return row;
  }

  @Override
  public boolean isEnabled(int position) {
    if (null == getItem(position)) {
      return false;
    }

    return super.isEnabled(position);
  }

  public void setFileList(CollaborativeList fileList) {
    this.fileList = fileList;
  }

  public void setFolderList(CollaborativeList folderList) {
    this.folderList = folderList;
  }

}
