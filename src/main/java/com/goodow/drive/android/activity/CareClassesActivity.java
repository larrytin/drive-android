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
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CareClassesActivity extends BaseActivity implements OnClickListener,
    OnFocusChangeListener {
  // 条目topic
  private LinearLayout ll_care_classes_topic = null;
  // 学期
  private LinearLayout ll_care_classes_term = null;
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

  private final String[] termNames = {Constant.LABEL_TERM_SEMESTER0, Constant.LABEL_TERM_SEMESTER1};
  // 活动topic
  private final String[] topic = new String[] {
      "我有一个幼儿园", "找找,藏藏", "飘飘,跳跳,滚滚", "我会……", "小小手", "好吃哎", "汽车嘀嘀嘀", "快乐红色", "暖暖的……"};
  // 当前选中状态
  private String currentTerm = Constant.LABEL_TERM_SEMESTER0;
  private String currenTopic = topic[0];
  private HandlerRegistration postHandler;
  private HandlerRegistration refreshHandler;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.bt_care_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.bt_care_coll:
        this.bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set(
            Constant.KEY_REDIRECTTO, "favorite"), null);
        break;
      case R.id.bt_care_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        break;
      case R.id.ftv_term_0:
      case R.id.ftv_term_1:
        this.onTermClick(v.getId());
        break;
      case R.id.ftv_care_button1:
      case R.id.ftv_care_button2:
      case R.id.ftv_care_button3:
      case R.id.ftv_care_button4:
      case R.id.ftv_care_button5:
      case R.id.ftv_care_button6:
      case R.id.ftv_care_button7:
      case R.id.ftv_care_button8:
      case R.id.ftv_care_button9:
        this.onTopicClick(v.getId());
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
        this.onCloudClick(v);
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
  public void readHistoryData() {
    this.sharedPreferences = getSharedPreferences(SHAREDNAME, MODE_PRIVATE);
    this.currentTerm = sharedPreferences.getString(Constant.TERM, currentTerm);
    this.currenTopic = sharedPreferences.getString(Constant.TOPIC, currenTopic);
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
    this.setContentView(R.layout.care_classes_activity);
    this.readHistoryData();
    this.initView();
    Bundle extras = this.getIntent().getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
    this.echoTerm();
    this.echoTopic();
    this.saveHistory(Constant.TOPIC, currenTopic);
    this.saveHistory(Constant.TERM, currentTerm);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Bundle extras = intent.getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
    this.echoTerm();
    this.echoTopic();
    this.saveHistory(Constant.TOPIC, currenTopic);
    this.saveHistory(Constant.TERM, currentTerm);
  }

  @Override
  protected void onPause() {
    super.onPause();
    postHandler.unregisterHandler();
    refreshHandler.unregisterHandler();
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
          Toast.makeText(CareClassesActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
          return;
        }
        for (int i = 0; i < tags.length(); i++) {
          if (Constant.LABEL_THEMES.contains(tags.getString(i))) {
            return;
          }
        }
        if (tags.indexOf(Constant.DATAREGISTRY_TYPE_SHIP) == -1) {
          tags.push(Constant.DATAREGISTRY_TYPE_SHIP);
        }
        sendQueryMessage(buildTags(tags));
        echoTerm();
        echoTopic();
        saveHistory(Constant.TOPIC, currenTopic);
        saveHistory(Constant.TERM, currentTerm);
      }
    });

    refreshHandler =
        bus.registerHandler(Constant.ADDR_VIEW_REFRESH, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            sendQueryMessage(null);
          }
        });
  }

  private void bindDataToView(JsonArray tags) {
    int len = this.rl_act_care_result_container.getChildCount();
    for (int i = 0; i < len; i++) {
      this.rl_act_care_result_container.getChildAt(i).setVisibility(View.GONE);
    }
    int len_tags = tags.length();
    for (int i = 0; i < len_tags && i < 10; i++) {
      Button itemButton = (Button) this.rl_act_care_result_container.getChildAt(i);
      itemButton.setOnClickListener(this);
      itemButton.setVisibility(View.VISIBLE);
      itemButton.setText(getSimpleTitle(tags.getString(i)));
      itemButton.setTag(tags.getString(i));
    }
  }

  /**
   * 构建查询TAGS
   * 
   * @param tags
   * @return
   */
  private JsonArray buildTags(JsonArray tags) {
    List<String> topics = Arrays.asList(this.topic);
    List<String> terms = Arrays.asList(this.termNames);
    // 删除垃圾数据
    for (int i = 0; i < tags.length(); i++) {
      String tag = tags.getString(i);
      boolean isLegalTheme = Constant.LABEL_THEMES.contains(tag);
      boolean isLegalTerm = terms.contains(tag);
      boolean isLegalTopic = topics.contains(tag);
      if (!isLegalTheme && !isLegalTerm && !isLegalTopic) {
        tags.remove(i--);
      }
    }

    // 如果默认的学期、主题不在tags中就加入 如果存在就设置为当前
    for (int i = 0; i < tags.length(); i++) {
      if (terms.contains(tags.getString(i))) {
        this.currentTerm = tags.getString(i);
        break;
      }
    }

    if (tags.indexOf(this.currentTerm) == -1) {
      tags.push(this.currentTerm);
    }

    for (int i = 0; i < tags.length(); i++) {
      if (topics.contains(tags.getString(i))) {
        this.currenTopic = tags.getString(i);
        break;
      }
    }
    if (tags.indexOf(this.currenTopic) == -1) {
      tags.push(this.currenTopic);
    }
    return tags;
  }

  /**
   * 回显学期
   */
  private void echoTerm() {
    int len = this.ll_care_classes_term.getChildCount();
    for (int i = 0; i < len; i++) {
      TextView child = (TextView) this.ll_care_classes_term.getChildAt(i);
      child.setSelected(false);
      if (termNames[i].equals(this.currentTerm)) {
        child.setSelected(true);
        this.saveHistory(Constant.TERM, this.currentTerm);
      }
    }
  }

  /**
   * 回显主题
   */
  private void echoTopic() {
    int len = this.ll_care_classes_topic.getChildCount();
    for (int i = 0; i < len; i++) {
      TextView child = (TextView) this.ll_care_classes_topic.getChildAt(i);
      child.setSelected(false);
      if (this.currenTopic.equals(child.getText().toString())) {
        child.setSelected(true);
        this.saveHistory(Constant.TOPIC, this.currenTopic);
      }
    }
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
    this.bt_care_back = (Button) findViewById(R.id.bt_care_back);
    this.bt_care_coll = (Button) findViewById(R.id.bt_care_coll);
    this.bt_care_loc = (Button) findViewById(R.id.bt_care_loc);
    this.bt_care_back.setOnClickListener(this);
    this.bt_care_coll.setOnClickListener(this);
    this.bt_care_loc.setOnClickListener(this);

    this.ll_care_classes_term = (LinearLayout) findViewById(R.id.ll_care_classes_term);
    int len_ll_care_classes_term = this.ll_care_classes_term.getChildCount();
    for (int i = 0; i < len_ll_care_classes_term; i++) {
      TextView term = (TextView) ll_care_classes_term.getChildAt(i);
      term.setSelected(false);
      if (this.currentTerm.equals(termNames[i])) {
        term.setSelected(true);
      }
      term.setOnClickListener(this);
    }

    this.ll_care_classes_topic = (LinearLayout) findViewById(R.id.ll_care_classes_topic);
    int len_ll_care_classes_topic = this.ll_care_classes_topic.getChildCount();
    for (int i = 0; i < len_ll_care_classes_topic; i++) {
      TextView topic = (TextView) this.ll_care_classes_topic.getChildAt(i);
      topic.setText(this.topic[i]);
      topic.setSelected(false);
      if (this.currenTopic.equals(topic.getText())) {
        topic.setSelected(true);
      }
      topic.setOnClickListener(this);
    }

    this.rl_act_care_result_container =
        (RelativeLayout) this.findViewById(R.id.rl_act_care_result_container);
    int len_rl_act_care_result_container = this.rl_act_care_result_container.getChildCount();
    for (int i = 0; i < len_rl_act_care_result_container; i++) {
      rl_act_care_result_container.getChildAt(i).setOnClickListener(this);
    }
  }

  /**
   * 打开活动详情
   * 
   * @param title
   */
  private void onCloudClick(View view) {
    String title = view.getTag().toString();
    if (title == null) {
      Toast.makeText(this, "数据不完整", Toast.LENGTH_SHORT).show();
      return;
    }
    JsonObject msg = Json.createObject();
    msg.set(Constant.KEY_ACTION, "post");
    msg.set(Constant.KEY_TITLE, title);
    JsonArray tags =
        Json.createArray().push(Constant.DATAREGISTRY_TYPE_SHIP).push(currentTerm)
            .push(currenTopic).push(title);
    msg.set(Constant.KEY_TAGS, tags);
    bus.send(Bus.LOCAL + Constant.ADDR_ACTIVITY, msg, null);
  }

  /**
   * 学期的点击事件
   * 
   * @param id
   */
  private void onTermClick(int id) {
    int len = this.ll_care_classes_term.getChildCount();
    for (int i = 0; i < len; i++) {
      TextView child = (TextView) this.ll_care_classes_term.getChildAt(i);
      child.setSelected(false);
      if (id == child.getId()) {
        child.setSelected(true);
        this.currentTerm = termNames[i];
        this.saveHistory(Constant.TERM, this.currentTerm);
      }
    }
    this.sendQueryMessage(null);
  }

  /**
   * 主题的点击事件
   * 
   * @param id
   */
  private void onTopicClick(int id) {
    int len = this.ll_care_classes_topic.getChildCount();
    for (int i = 0; i < len; i++) {
      TextView child = (TextView) this.ll_care_classes_topic.getChildAt(i);
      child.setSelected(false);
      if (id == child.getId()) {
        child.setSelected(true);
        this.currenTopic = child.getText().toString();
        this.saveHistory(Constant.TOPIC, this.currenTopic);
      }
    }
    this.sendQueryMessage(null);
  }

  /**
   * 保存数据到sp用于回显
   * 
   * @param key 名称
   * @param value 值
   */
  private void saveHistory(String key, String value) {
    if (sharedPreferences != null) {
      Editor edit = sharedPreferences.edit();
      edit.putString(key, value);
      edit.commit();
    }
  }

  /**
   * 构建查询的bus消息
   */
  private void sendQueryMessage(JsonArray tags) {
    JsonObject msg = Json.createObject();
    if (tags != null) {
      msg.set(Constant.KEY_TAGS, tags);
    } else {
      msg.set(Constant.KEY_TAGS, Json.createArray().push(Constant.DATAREGISTRY_TYPE_SHIP).push(
          this.currentTerm).push(this.currenTopic));
    }
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_CHILDREN, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonArray tags = (JsonArray) message.body();
        bindDataToView(tags);
      }
    });
  }
}
