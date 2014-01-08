package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.realtime.json.JsonObject;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class NotificationActivity extends BaseActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    JsonObject jsonObject = (JsonObject) getIntent().getExtras().getSerializable("msg");
    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
    this.setContentView(R.layout.activity_notification);
    TextView contentTextView = (TextView) findViewById(R.id.tv_act_not_content);
    Button chooseButton = (Button) findViewById(R.id.bt_act_not_choose);
    contentTextView.setText(jsonObject.getString("content"));
    chooseButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        NotificationActivity.this.finish();
      }
    });

  }
}
