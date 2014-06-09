package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.realtime.json.JsonObject;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_notification)
public class NotificationActivity extends BaseActivity {
  @InjectView(R.id.tv_act_not_content)
  TextView contentTextView;
  @InjectView(R.id.bt_act_not_choose)
  Button chooseButton;
  @InjectExtra(value = "msg", optional = true)
  JsonObject mJsonObject;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if(mJsonObject != null) {
      contentTextView.setText(mJsonObject.getString("content"));
    }
    chooseButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        NotificationActivity.this.finish();
      }
    });
  }
}
