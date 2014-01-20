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

import java.util.Arrays;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
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
  private RelativeLayout rl_act_care_result_container = null;
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
  private HandlerRegistration postHandler;

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
        clearCurrent();
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
        clearCurrent();
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
            Constant.QUERIES, Json.createObject().set(Constant.TYPE, "收藏")), null);
        break;
      case R.id.bt_care_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        break;
      /**
       * MODIFY BY DPW
       */
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
        onCloudClick(v);
        break;

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
   * @param queries
   */
  public void readQuery(JsonObject queries) {
    if (queries != null) {
      String tempTerm = queries.getString(Constant.TERM);
      if (tempTerm != null) {
        if (!tempTerm.equals(Constant.TERM_SEMESTER0) && !tempTerm.equals(Constant.TERM_SEMESTER1)) {
          Toast.makeText(CareClassesActivity.this, "term错误", Toast.LENGTH_SHORT).show();
          return;
        }
        currentTerm = tempTerm;
      }
      String tempTopic = queries.getString(Constant.TOPIC);
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
    readQuery(body.getObject(Constant.QUERIES));
    if (activities == null) {
      sendQueryMessage();
    } else {
      this.activities = activities;
      bindDataToView();
      isLocal = false;
      bindHistoryDataToView();
      isLocal = true;
    }
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
        JsonObject queries = body.getObject(Constant.QUERIES);
        if (queries != null && queries.has(Constant.TYPE)
            && !Constant.DATAREGISTRY_TYPE_SHIP.equals(queries.getString(Constant.TYPE))) {
          return;
        }
        dataHandler(body);
      }
    });
  }

  private void bindDataToView() {
    /**
     * MODIFY BY DPW bind title which has number to tag
     */
    int len = this.activities.length();
    for (int i = 0; i < len && i < 10; i++) {
      Button itemButton = (Button) this.rl_act_care_result_container.getChildAt(i);
      itemButton.setOnClickListener(this);
      itemButton.setVisibility(View.VISIBLE);
      itemButton.setText(getSimpleTitle(activities.getObject(i).getString(Constant.TITLE)));
      itemButton.setTag(activities.getObject(i).getString(Constant.TITLE));
    }
  }

  /**
   * 历史记录回显
   */
  private void bindHistoryDataToView() {
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

  /**
   * DPW 清空当前的数据
   */
  private void clearCurrent() {
    if (this.activities != null) {
      this.activities.clear();
    }
    int len = this.rl_act_care_result_container.getChildCount();
    for (int i = 0; i < len; i++) {
      this.rl_act_care_result_container.getChildAt(i).setVisibility(View.GONE);
    }
  }

  private void dataHandler(JsonObject body) {
    JsonObject queries = body.getObject(Constant.QUERIES);
    readQuery(queries);
    activities = body.getArray("activities");
    isLocal = activities == null;
    if (activities != null) {
      bindDataToView();
    }
    bindHistoryDataToView();
    isLocal = true;
  }

  /**
   * 删除Title前的数字编号 DPW
   * 
   * @param title
   * @return
   */
  private String getSimpleTitle(String title) {
    if (title.matches("^\\d{4}.*")) {
      return title.substring(4, title.length());
    } else {
      return title;
    }
  }

  private void initView() {
    /**
     * MODIFY BY DPW
     */
    this.rl_act_care_result_container =
        (RelativeLayout) this.findViewById(R.id.rl_act_care_result_container);

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
   * 打开活动详情
   * 
   * @param title
   */
  private void onCloudClick(View view) {
    String tag = view.getTag().toString();
    if (tag == null) {
      Toast.makeText(this, "数据不完整", Toast.LENGTH_SHORT).show();
      return;
    }
    String title = tag.toString();
    JsonObject msg = Json.createObject();
    msg.set("action", "post");
    JsonObject activity = Json.createObject();
    JsonObject queries = Json.createObject();
    queries.set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_SHIP);
    queries.set(Constant.TERM, currentTerm);
    queries.set(Constant.TOPIC, currenTopic);
    activity.set(Constant.QUERIES, queries);
    activity.set(Constant.TITLE, title);
    msg.set("activity", activity);
    bus.send(Bus.LOCAL + Constant.ADDR_ACTIVITY, msg, null);
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
    JsonObject queries = Json.createObject();
    queries.set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_SHIP);
    queries.set(Constant.TERM, currentTerm);
    queries.set(Constant.TOPIC, currenTopic);
    msg.set(Constant.QUERIES, queries);
    bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        dataHandler(body);
      }
    });
  }

  private void setListener() {
    rg_care_classes_topic.setOnCheckedChangeListener(this);
    rg_care_classes_term.setOnCheckedChangeListener(this);
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
