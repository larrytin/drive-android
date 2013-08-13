package com.goodow.drive.android.adapter;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.goodow.android.drive.R;
import com.goodow.drive.android.fragment.LocalResFragment;
import com.goodow.drive.android.toolutils.ToolsFunctionForThisProgect;

public class LocalResAdapter extends BaseAdapter {
  private ArrayList<File> dataSource;
  private LocalResFragment localResFragment;

  public LocalResAdapter(ArrayList<File> dataSource, LocalResFragment localResFragment) {
    super();
    this.dataSource = dataSource;
    this.localResFragment = localResFragment;
  }

  @Override
  public int getCount() {
    if (dataSource != null) {
      return dataSource.size();
    } else {
      return 0;
    }
  }

  @Override
  public Object getItem(int position) {
    return dataSource.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;
    if (null == row) {
      row = localResFragment.getActivity().getLayoutInflater().inflate(R.layout.row_folderlist, parent, false);
    }

    final File item = (File) getItem(position);

    ImageButton delButton = (ImageButton) row.findViewById(R.id.delButton);
    delButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog alertDialog = new AlertDialog.Builder(localResFragment.getActivity())
            .setPositiveButton(R.string.dailogOK, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                if (null != item) {
                  String localPath = item.getParentFile().getAbsolutePath();

                  localResFragment.delFile(item);

                  localResFragment.initDataSource(new File(localPath));
                }
              }
            }).setNegativeButton(R.string.dailogCancel, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {

              }
            }).setMessage(R.string.del_DailogMessage).create();

        alertDialog.show();

      }
    });

    TextView listItem = (TextView) row.findViewById(R.id.listItem);
    String fileNameString = item.getName();

    int index = fileNameString.lastIndexOf(".");
    if (index > 0) {
      listItem.setText(fileNameString.substring(0, index));
    } else {
      listItem.setText(fileNameString);
    }

    ImageView img_left = (ImageView) row.findViewById(R.id.leftImage);
    img_left.setImageResource(ToolsFunctionForThisProgect.getFileIconByFileFullName(fileNameString));

    row.setTag(item.getAbsolutePath());
    return row;
  }
}