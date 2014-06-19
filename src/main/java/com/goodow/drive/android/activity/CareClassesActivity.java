package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.Registration;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_careclass)
public class CareClassesActivity extends BaseActivity implements OnClickListener,
    OnFocusChangeListener {
  // 条目topic
  @InjectView(R.id.ll_care_classes_topic)
  private LinearLayout ll_care_classes_topic;
  // 学期
  @InjectView(R.id.ll_care_classes_term)
  private LinearLayout ll_care_classes_term;
  // 返回键
  @InjectView(R.id.bt_care_back)
  private ImageView bt_care_back;
  // 收藏键
  @InjectView(R.id.bt_care_coll)
  private ImageView bt_care_coll;
  // 黑屏
  @InjectView(R.id.bt_care_loc)
  private ImageView bt_care_loc;
  private SharedPreferences sharedPreferences;
  private final static String SHAREDNAME = "careClassesHistory";

  @InjectExtra(value = "msg", optional = true)
  private JsonObject msg;

  private final String[] termNames = {Constant.LABEL_TERM_SEMESTER0, Constant.LABEL_TERM_SEMESTER1};
  // 活动topic
  private String[] topic;
  // 活动topic上学期
  private final String[] topic_1 = new String[] {
      "我有一个幼儿园", "找找.藏藏", "飘飘,跳跳,滚滚", "我会……", "小小手", "好吃哎", "汽车嘀嘀嘀", "快乐红色", "暖暖的……"};
  // 活动topic下学期
  private final String[] topic_2 = new String[] {
      "我想长大", "亲亲热热一家人", "小动物来了", "绿绿的……", "快乐的声音", "大大小小", "从头玩到脚", "特别喜欢你", "清凉一夏"};
  // 当前选中状态
  private String currentTerm = Constant.LABEL_TERM_SEMESTER0;
  private String currenTopic = "";
  private Registration postHandler;
  private Registration refreshHandler;
  @InjectView(R.id.ll_act_care_result_container_1)
  private LinearLayout ll_act_care_result_container_1;
  @InjectView(R.id.ll_act_care_result_container_2)
  private LinearLayout ll_act_care_result_container_2;
  @InjectView(R.id.ib_care_ebook)
  private ImageButton ib_care_ebook;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.bt_care_back:
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.bt_care_coll:
        this.bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
            "favorite"), null);
        break;
      case R.id.bt_care_loc:
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
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
      case R.id.ib_care_ebook:
        JsonObject msg = Json.createObject();
        msg.set("action", "post");
        JsonArray tags = Json.createArray().push(Constant.DATAREGISTRY_TYPE_SHIP_EBOOK);
        msg.set(Constant.KEY_TAGS, tags);
        bus.sendLocal(Constant.ADDR_TOPIC, msg, null);
        break;

    }
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (hasFocus) {
      ((RadioButton) v).setChecked(true);
      v.setBackgroundResource(R.drawable.care_item_bg_selected);
      v.setLayoutParams(new LinearLayout.LayoutParams(10, 10));
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
    this.readHistoryData();
    this.initView();
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
    this.setIntent(intent);
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
    postHandler.unregister();
    refreshHandler.unregister();
  }

  @Override
  protected void onResume() {
    super.onResume();
    postHandler = bus.subscribeLocal(Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
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
        bus.subscribeLocal(Constant.ADDR_VIEW_REFRESH, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            sendQueryMessage(null);
          }
        });
  }

  private void bindDataToView(JsonArray tags) {
    for (int i = 0; i < ll_act_care_result_container_1.getChildCount(); i++) {
      ll_act_care_result_container_1.getChildAt(i).setVisibility(View.GONE);
    }
    for (int i = 0; i < ll_act_care_result_container_2.getChildCount(); i++) {
      ll_act_care_result_container_2.getChildAt(i).setVisibility(View.GONE);
    }
    int len_tags = tags.length();
    for (int i = 0; i < len_tags && i < 10; i++) {
      TextView itemButton;
      if (i < 5) {
        itemButton = (TextView) this.ll_act_care_result_container_1.getChildAt(i);
      } else {
        itemButton = (TextView) this.ll_act_care_result_container_2.getChildAt(i - 5);
      }
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
      child.setTextSize(24);
      child.setSelected(false);
      if (termNames[i].equals(this.currentTerm)) {
        if (i == 0) {
          topic = topic_1;
        } else if (i == 1) {
          topic = topic_2;
        }
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
      child.setText(this.topic[i]);
      setSelect(child, false);
      if (this.currenTopic.equals(child.getText().toString())) {
        setSelect(child, true);
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
    this.bt_care_back.setOnClickListener(this);
    this.bt_care_coll.setOnClickListener(this);
    this.bt_care_loc.setOnClickListener(this);
    ib_care_ebook.setOnClickListener(this);
    int len_ll_care_classes_term = this.ll_care_classes_term.getChildCount();
    for (int i = 0; i < len_ll_care_classes_term; i++) {
      TextView term = (TextView) ll_care_classes_term.getChildAt(i);
      term.setSelected(false);
      if (this.currentTerm.equals(termNames[i])) {
        term.setSelected(true);
      }
      term.setOnClickListener(this);
    }
    int len_ll_care_classes_topic = this.ll_care_classes_topic.getChildCount();
    topic = topic_1;
    for (int i = 0; i < len_ll_care_classes_topic; i++) {
      TextView topic = (TextView) this.ll_care_classes_topic.getChildAt(i);
      topic.setText(this.topic[i]);
      setSelect(topic, false);
      if (this.currenTopic.equals(topic.getText())) {
        setSelect(topic, true);
      }
      topic.setOnClickListener(this);
    }
    for (int i = 0; i < ll_act_care_result_container_1.getChildCount(); i++) {
      ll_act_care_result_container_1.getChildAt(i).setOnClickListener(this);
    }
    for (int i = 0; i < ll_act_care_result_container_2.getChildCount(); i++) {
      ll_act_care_result_container_2.getChildAt(i).setOnClickListener(this);
    }
  }

  /**
   * 
   * @param view
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
    bus.sendLocal(Constant.ADDR_ACTIVITY, msg, null);
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
        if (i == 0) {
          topic = topic_1;
        } else if (i == 1) {
          topic = topic_2;
        }
        child.setSelected(true);
        this.currentTerm = termNames[i];
        this.saveHistory(Constant.TERM, this.currentTerm);
      }
    }
    currenTopic = "";
    echoTopic();
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
      setSelect(child, false);
      if (id == child.getId()) {
        setSelect(child, true);
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
    bus.sendLocal(Constant.ADDR_TAG_CHILDREN, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        JsonArray tags = body.getArray(Constant.TAGS);
        bindDataToView(tags);
      }
    });
  }

  private void setSelect(View view, boolean bool) {
    LinearLayout.LayoutParams params =
        (android.widget.LinearLayout.LayoutParams) view.getLayoutParams();
    if (bool) {
      params.width =
          getResources().getDimensionPixelSize(R.dimen.act_care_topic_item_width_selected);
      params.height =
          getResources().getDimensionPixelSize(R.dimen.act_care_topic_item_height_selected);
      params.topMargin =
          getResources().getDimensionPixelSize(R.dimen.act_care_topic_item_margin_selected);
      params.bottomMargin =
          getResources().getDimensionPixelSize(R.dimen.act_care_topic_item_margin_selected);
    } else {
      params.width = getResources().getDimensionPixelSize(R.dimen.act_care_topic_item_width);
      params.height = getResources().getDimensionPixelSize(R.dimen.act_care_topic_item_height);
      params.topMargin = getResources().getDimensionPixelSize(R.dimen.act_care_topic_item_margin);
      params.bottomMargin =
          getResources().getDimensionPixelSize(R.dimen.act_care_topic_item_margin);
    }
    view.setLayoutParams(params);
    view.setSelected(bool);
  }

}
