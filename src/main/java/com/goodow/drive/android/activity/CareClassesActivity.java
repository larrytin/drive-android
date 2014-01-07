package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.Arrays;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class CareClassesActivity extends BaseActivity implements OnCheckedChangeListener,
    OnClickListener, OnFocusChangeListener {
  // 条目topic
  private RadioGroup rg_care_classes_topic;
  private RadioButton rb_care_button1;
  private RadioButton rb_care_button2;
  private RadioButton rb_care_button3;
  private RadioButton rb_care_button4;
  private RadioButton rb_care_button5;
  private RadioButton rb_care_button6;
  private RadioButton rb_care_button7;
  private RadioButton rb_care_button8;
  private RadioButton rb_care_button9;
  // 学期
  private RadioGroup rg_care_classes_term;
  private RadioButton rb_term_0;
  private RadioButton rb_term_1;
  // 云朵activity
  private Button iv_care_cloud1;
  private Button iv_care_cloud2;
  private Button iv_care_cloud3;
  private Button iv_care_cloud4;
  private Button iv_care_cloud5;
  private Button iv_care_cloud6;
  private Button iv_care_cloud7;
  private Button iv_care_cloud8;
  private Button iv_care_cloud9;
  private Button iv_care_cloud10;
  // 返回键
  private Button bt_care_back;
  // 收藏键
  private Button bt_care_coll;
  // 黑屏
  private Button bt_care_loc;
  // sp
  private SharedPreferences sharedPreferences = null;
  private final static String SHAREDNAME = "careClassesHistory";

  private boolean isLocal = true;

  // 活动topic
  private final String[] topic = new String[] {
      "我有一个幼儿园", "找找,藏藏", "飘飘,跳跳,滚滚", "我会……", "小小手", "好吃哎", "汽车嘀嘀嘀", "快乐红色", "暖暖的……"};
  // 当前选中状态
  private String currentTerm = Constant.TERM_SEMESTER0;

  private String currenTopic = topic[0];

  private JsonArray activities;
  private final MessageHandler<JsonObject> eventHandler = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      String action = body.getString("action");
      // 仅仅处理action为null或post动作
      if (action != null && !"post".equalsIgnoreCase(action)) {
        return;
      }
      JsonObject query = body.getObject("query");
      if (query != null && query.has("type")
          && !Constant.DATAREGISTRY_TYPE_SHIP.equals(query.getString("type"))) {
        return;
      }
      readQuery(query);
      activities = body.getArray("activities");
      isLocal = activities == null;
      if (activities != null) {
        bindDataToView();
      }
      bindHistoryDataToView();
      isLocal = true;
    }
  };

  @Override
  public void onCheckedChanged(RadioGroup group, int checkedId) {
    // if (!isLocal) {
    // return;
    // }
    if (group.getId() == R.id.rg_care_classes_topic) {
      int index = Integer.parseInt((String) findViewById(checkedId).getTag());
      setTopicCheckedDrawable(checkedId);
      findViewById(checkedId).requestFocus();
      currenTopic = topic[index];
      if (isLocal) {
        sendQueryMessage();
      }
      saveDataToSP(Constant.TOPIC, currenTopic);
    }
    if (group.getId() == R.id.rg_care_classes_term) {
      switch (checkedId) {
        case R.id.rb_term_0:
          currentTerm = Constant.TERM_SEMESTER0;
          break;
        case R.id.rb_term_1:
          currentTerm = Constant.TERM_SEMESTER1;
          break;
      }
      if (isLocal) {
        sendQueryMessage();
      }
      saveDataToSP(Constant.TERM, currentTerm);
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.bt_care_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.bt_care_coll:
        bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, Json.createObject().set("action", "post").set(
            "query", Json.createObject().set("type", "收藏")), null);
        break;
      case R.id.bt_care_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        // Toast.makeText(this, "黑屏", Toast.LENGTH_LONG).show();
        break;
      case R.id.bt_care_cloud1:
      case R.id.bt_care_cloud2:
      case R.id.bt_care_cloud3:
      case R.id.bt_care_cloud4:
      case R.id.bt_care_cloud5:
      case R.id.bt_care_cloud6:
      case R.id.bt_care_cloud7:
      case R.id.bt_care_cloud8:
      case R.id.bt_care_cloud9:
      case R.id.bt_care_cloud10:

    }
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (hasFocus) {
      ((RadioButton) v).setChecked(true);
      v.setBackgroundResource(R.drawable.care_item_bg_selected);
    } else {
      if (!((RadioButton) v).isChecked()) {
        v.setBackgroundResource(R.drawable.care_item_bg);
      }
    }
  }

  /**
   * 查询历史数据
   */
  public void readDataFromSP() {
    sharedPreferences = getSharedPreferences(SHAREDNAME, MODE_PRIVATE);
    currentTerm = sharedPreferences.getString(Constant.TERM, currentTerm);
    currenTopic = sharedPreferences.getString(Constant.TOPIC, currenTopic);
  }

  /**
   * 解析条件
   * 
   * @param query
   */
  public void readQuery(JsonObject query) {
    if (query != null) {
      String tempTerm = query.getString(Constant.TERM);
      if (tempTerm != null) {
        if (!tempTerm.equals(Constant.TERM_SEMESTER0) && !tempTerm.equals(Constant.TERM_SEMESTER1)) {
          Toast.makeText(CareClassesActivity.this, "term错误", Toast.LENGTH_SHORT).show();
          return;
        }
        currentTerm = tempTerm;
      }
      String tempTopic = query.getString(Constant.TOPIC);
      if (tempTopic != null) {
        if (!Arrays.asList(topic).contains(tempTopic)) {
          Toast.makeText(CareClassesActivity.this, "topic错误", Toast.LENGTH_SHORT).show();
          return;
        }
        currenTopic = tempTopic;
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.care_classes_activity);
    initView();
    setListener();
    readDataFromSP();
    Bundle extras = this.getIntent().getExtras();
    JsonObject body = (JsonObject) extras.get("msg");
    JsonArray activities = body.getArray("activities");
    if (activities == null) {
      sendQueryMessage();
    } else {
      readQuery(body.getObject("query"));
      this.activities = activities;
      bindDataToView();
      isLocal = false;
      bindHistoryDataToView();
      isLocal = true;
    }
  }

  @Override
  protected void onPause() {
    bus.unregisterHandler(Constant.ADDR_TOPIC, eventHandler);
    super.onPause();
  }

  @Override
  protected void onResume() {
    bus.registerHandler(Constant.ADDR_TOPIC, eventHandler);
    super.onResume();
  }

  private void bindDataToView() {
    if (null == activities || activities.length() != 10) {
      return;
    }
    iv_care_cloud1.setText(activities.getObject(0).getString(Constant.TITLE));
    iv_care_cloud2.setText(activities.getObject(1).getString(Constant.TITLE));
    iv_care_cloud3.setText(activities.getObject(2).getString(Constant.TITLE));
    iv_care_cloud4.setText(activities.getObject(3).getString(Constant.TITLE));
    iv_care_cloud5.setText(activities.getObject(4).getString(Constant.TITLE));
    iv_care_cloud6.setText(activities.getObject(5).getString(Constant.TITLE));
    iv_care_cloud7.setText(activities.getObject(6).getString(Constant.TITLE));
    iv_care_cloud8.setText(activities.getObject(7).getString(Constant.TITLE));
    iv_care_cloud9.setText(activities.getObject(8).getString(Constant.TITLE));
    iv_care_cloud10.setText(activities.getObject(9).getString(Constant.TITLE));
  }

  /**
   * 历史记录回显
   */
  private void bindHistoryDataToView() {
    Log.d("System.out", currenTopic + currentTerm);
    // 回显学期
    if (Constant.TERM_SEMESTER0.equals(this.currentTerm)) {
      this.rb_term_0.setChecked(true);
    } else if (Constant.TERM_SEMESTER1.equals(this.currentTerm)) {
      this.rb_term_1.setChecked(true);
    }
    // 回显topic
    int topicIndex = Arrays.asList(topic).indexOf(currenTopic);
    ((RadioButton) rg_care_classes_topic.findViewWithTag(String.valueOf(topicIndex)))
        .setChecked(true);
  }

  private void initView() {
    rb_care_button1 = (RadioButton) findViewById(R.id.rb_care_button1);
    rb_care_button2 = (RadioButton) findViewById(R.id.rb_care_button2);
    rb_care_button3 = (RadioButton) findViewById(R.id.rb_care_button3);
    rb_care_button4 = (RadioButton) findViewById(R.id.rb_care_button4);
    rb_care_button5 = (RadioButton) findViewById(R.id.rb_care_button5);
    rb_care_button6 = (RadioButton) findViewById(R.id.rb_care_button6);
    rb_care_button7 = (RadioButton) findViewById(R.id.rb_care_button7);
    rb_care_button8 = (RadioButton) findViewById(R.id.rb_care_button8);
    rb_care_button9 = (RadioButton) findViewById(R.id.rb_care_button9);
    rb_care_button1.setText("1." + topic[0]);
    rb_care_button2.setText("2." + topic[1]);
    rb_care_button3.setText("3." + topic[2]);
    rb_care_button4.setText("4." + topic[3]);
    rb_care_button5.setText("5." + topic[4]);
    rb_care_button6.setText("6." + topic[5]);
    rb_care_button7.setText("7." + topic[6]);
    rb_care_button8.setText("8." + topic[7]);
    rb_care_button9.setText("9." + topic[8]);
    iv_care_cloud1 = (Button) findViewById(R.id.bt_care_cloud1);
    iv_care_cloud2 = (Button) findViewById(R.id.bt_care_cloud2);
    iv_care_cloud3 = (Button) findViewById(R.id.bt_care_cloud3);
    iv_care_cloud4 = (Button) findViewById(R.id.bt_care_cloud4);
    iv_care_cloud5 = (Button) findViewById(R.id.bt_care_cloud5);
    iv_care_cloud6 = (Button) findViewById(R.id.bt_care_cloud6);
    iv_care_cloud7 = (Button) findViewById(R.id.bt_care_cloud7);
    iv_care_cloud8 = (Button) findViewById(R.id.bt_care_cloud8);
    iv_care_cloud9 = (Button) findViewById(R.id.bt_care_cloud9);
    iv_care_cloud10 = (Button) findViewById(R.id.bt_care_cloud10);
    rg_care_classes_topic = (RadioGroup) findViewById(R.id.rg_care_classes_topic);
    bt_care_back = (Button) findViewById(R.id.bt_care_back);
    bt_care_coll = (Button) findViewById(R.id.bt_care_coll);
    bt_care_loc = (Button) findViewById(R.id.bt_care_loc);
    rg_care_classes_term = (RadioGroup) findViewById(R.id.rg_care_classes_term);
    rb_term_0 = (RadioButton) findViewById(R.id.rb_term_0);
    rb_term_1 = (RadioButton) findViewById(R.id.rb_term_1);
    rb_care_button1.setOnFocusChangeListener(this);
    rb_care_button2.setOnFocusChangeListener(this);
    rb_care_button3.setOnFocusChangeListener(this);
    rb_care_button4.setOnFocusChangeListener(this);
    rb_care_button5.setOnFocusChangeListener(this);
    rb_care_button6.setOnFocusChangeListener(this);
    rb_care_button7.setOnFocusChangeListener(this);
    rb_care_button8.setOnFocusChangeListener(this);
    rb_care_button9.setOnFocusChangeListener(this);
  }

  /**
   * 保存数据到sp用于回显
   * 
   * @param key 名称
   * @param value 值
   */
  private void saveDataToSP(String key, String value) {
    if (sharedPreferences != null) {
      Editor edit = sharedPreferences.edit();
      edit.putString(key, value);
      edit.commit();
    }
  }

  /**
   * 构建查询的bus消息
   */
  private void sendQueryMessage() {
    JsonObject msg = Json.createObject();
    msg.set("action", "get");
    JsonObject query = Json.createObject();
    query.set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_SHIP);
    query.set(Constant.TERM, currentTerm);
    query.set(Constant.TOPIC, currenTopic);
    msg.set("query", query);
    bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, eventHandler);
  }

  private void setListener() {
    rg_care_classes_topic.setOnCheckedChangeListener(this);
    rg_care_classes_term.setOnCheckedChangeListener(this);
    iv_care_cloud1.setOnClickListener(this);
    iv_care_cloud2.setOnClickListener(this);
    iv_care_cloud3.setOnClickListener(this);
    iv_care_cloud4.setOnClickListener(this);
    iv_care_cloud5.setOnClickListener(this);
    iv_care_cloud6.setOnClickListener(this);
    iv_care_cloud7.setOnClickListener(this);
    iv_care_cloud8.setOnClickListener(this);
    iv_care_cloud9.setOnClickListener(this);
    iv_care_cloud10.setOnClickListener(this);
    bt_care_back.setOnClickListener(this);
    bt_care_coll.setOnClickListener(this);
    bt_care_loc.setOnClickListener(this);
  }

  /**
   * 设置topic条目背景
   * 
   * @param id
   */
  private void setTopicCheckedDrawable(int id) {
    rb_care_button1.setBackgroundResource(R.drawable.care_item_bg);
    rb_care_button2.setBackgroundResource(R.drawable.care_item_bg);
    rb_care_button3.setBackgroundResource(R.drawable.care_item_bg);
    rb_care_button4.setBackgroundResource(R.drawable.care_item_bg);
    rb_care_button5.setBackgroundResource(R.drawable.care_item_bg);
    rb_care_button6.setBackgroundResource(R.drawable.care_item_bg);
    rb_care_button7.setBackgroundResource(R.drawable.care_item_bg);
    rb_care_button8.setBackgroundResource(R.drawable.care_item_bg);
    rb_care_button9.setBackgroundResource(R.drawable.care_item_bg);
    findViewById(id).setBackgroundResource(R.drawable.care_item_bg_selected);
  }
}
