package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SecurityActivity extends BaseActivity implements OnClickListener {

  // 后退收藏锁屏
  private ImageView iv_act_security_back;
  private ImageView iv_act_security_coll;
  private ImageView iv_act_security_loc;

  private HandlerRegistration postHandler;

  // 教师用书 幼儿用书
  private LinearLayout ll_act_security_teacher;
  private LinearLayout ll_act_security_children;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_security_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_security_coll:
        this.bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set(
            Constant.KEY_REDIRECTTO, "favorite"), null);
        break;
      case R.id.iv_act_security_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        break;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_security);
    this.initView();
    Bundle extras = this.getIntent().getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(tags);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Bundle extras = intent.getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(tags);
  }

  @Override
  protected void onPause() {
    super.onPause();
    postHandler.unregisterHandler();
  }

  @Override
  protected void onResume() {
    super.onResume();
    postHandler = bus.registerHandler(Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString("action");
        // 仅仅处理action为post动作
        if (!"post".equalsIgnoreCase(action)) {
          return;
        }
        JsonArray tags = body.getArray(Constant.KEY_TAGS);
        if (tags == null) {
          Toast.makeText(SecurityActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
          return;
        }
        for (int i = 0; i < tags.length(); i++) {
          if (Constant.LABEL_THEMES.contains(tags.getString(i))) {
            return;
          }
        }
        if (tags.indexOf(Constant.DATAREGISTRY_TYPE_EDUCATION) == -1) {
          tags.push(Constant.DATAREGISTRY_TYPE_EDUCATION);
        }
        sendQueryMessage(tags);
      }
    });

  }

  /**
   * 初始化View对象 设置点击事件 设置光标事件监听 添加到对应集合
   */
  private void initView() {
    // 后退 收藏 所屏
    iv_act_security_back = (ImageView) findViewById(R.id.iv_act_security_back);
    iv_act_security_coll = (ImageView) findViewById(R.id.iv_act_security_coll);
    iv_act_security_loc = (ImageView) findViewById(R.id.iv_act_security_loc);
    iv_act_security_back.setOnClickListener(this);
    iv_act_security_coll.setOnClickListener(this);
    iv_act_security_loc.setOnClickListener(this);
    // 教师用书 幼儿用书
    ll_act_security_teacher = (LinearLayout) findViewById(R.id.ll_act_security_teacher);
    ll_act_security_children = (LinearLayout) findViewById(R.id.ll_act_security_children);
    for (int i = 0; i < ll_act_security_teacher.getChildCount(); i++) {
      ll_act_security_teacher.getChildAt(i).setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          JsonArray tags =
              Json.createArray().push(Constant.DATAREGISTRY_TYPE_EDUCATION).push("教师用书").push(
                  v.getTag());
          sendQueryMessage(tags);
        }
      });
    }
    for (int i = 0; i < ll_act_security_children.getChildCount(); i++) {
      ll_act_security_children.getChildAt(i).setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          JsonArray tags =
              Json.createArray().push(Constant.DATAREGISTRY_TYPE_EDUCATION).push("幼儿用书").push(
                  v.getTag());
          sendQueryMessage(tags);
        }
      });
    }
  }

  /**
   * 构建查询的bus消息
   */
  private void sendQueryMessage(JsonArray tags) {
    JsonObject msg = Json.createObject();
    if (tags == null || tags.length() != 3) {
      return;
    }
    msg.set(Constant.KEY_TAGS, tags);
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_ATTACHMENT_SEARCH, msg,
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            JsonArray attachments = body.getArray(Constant.KEY_ATTACHMENTS);
            String filePath = attachments.getObject(0).getString(Constant.KEY_URL);
            bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, Json.createObject().set("path", filePath)
                .set("play", 1), null);
          }
        });
  }
}
