package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.view.EbookAttachmentsView;
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

public class EbookActivity extends BaseActivity implements OnClickListener {

  // 后退收藏锁屏
  private ImageView iv_act_ebook_back;
  private ImageView iv_act_ebook_coll;
  private ImageView iv_act_ebook_loc;

  private LinearLayout ll_result_1;
  private LinearLayout ll_result_2;

  private HandlerRegistration postHandler;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_ebook_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_ebook_coll:
        this.bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set(
            Constant.KEY_REDIRECTTO, "favorite"), null);
        break;
      case R.id.iv_act_ebook_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        break;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_ebook);
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
          Toast.makeText(EbookActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
          return;
        }
        for (int i = 0; i < tags.length(); i++) {
          if (Constant.LABEL_THEMES.contains(tags.getString(i))) {
            return;
          }
        }
        if (tags.indexOf(Constant.DATAREGISTRY_TYPE_SHIP_EBOOK) == -1) {
          tags.push(Constant.DATAREGISTRY_TYPE_SHIP_EBOOK);
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
    iv_act_ebook_back = (ImageView) findViewById(R.id.iv_act_ebook_back);
    iv_act_ebook_coll = (ImageView) findViewById(R.id.iv_act_ebook_coll);
    iv_act_ebook_loc = (ImageView) findViewById(R.id.iv_act_ebook_loc);
    iv_act_ebook_back.setOnClickListener(this);
    iv_act_ebook_coll.setOnClickListener(this);
    iv_act_ebook_loc.setOnClickListener(this);
    ll_result_1 = (LinearLayout) findViewById(R.id.ll_result_1);
    ll_result_2 = (LinearLayout) findViewById(R.id.ll_result_2);
  }

  /**
   * 构建查询的bus消息
   * 
   * @param v
   */
  private void sendQueryMessage(JsonArray tags) {
    JsonObject msg = Json.createObject();
    if (tags == null || tags.length() != 1) {
      return;
    }
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_ATTACHMENT_SEARCH, msg,
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            JsonArray attachments = body.getArray(Constant.KEY_ATTACHMENTS);
            for (int i = 0; i < attachments.length(); i++) {
              JsonObject object = attachments.getObject(i);
              String name = object.getString(Constant.KEY_NAME);
              if ("小班上".equals(name)) {
                setResultInfo(ll_result_1.getChildAt(0), object);
              } else if ("中班上".equals(name)) {
                setResultInfo(ll_result_1.getChildAt(1), object);
              } else if ("大班上".equals(name)) {
                setResultInfo(ll_result_1.getChildAt(2), object);
              } else if ("小班下".equals(name)) {
                setResultInfo(ll_result_2.getChildAt(0), object);
              } else if ("中班下".equals(name)) {
                setResultInfo(ll_result_2.getChildAt(1), object);
              } else if ("大班下".equals(name)) {
                setResultInfo(ll_result_2.getChildAt(2), object);
              }
            }
          }
        });
  }

  /**
   * 设置结果条目的点击事件和文件名称
   * 
   * @param view
   * @param object
   */
  private void setResultInfo(View view, JsonObject object) {
    EbookAttachmentsView eView = (EbookAttachmentsView) view;
    eView.setText(object.getString(Constant.KEY_NAME));
    final String filePath = object.getString(Constant.KEY_URL);
    eView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, Json.createObject().set("path", filePath).set(
            "play", 1), null);
      }
    });
    eView.setVisibility(View.VISIBLE);
  }
}