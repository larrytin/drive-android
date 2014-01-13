package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.adapter.CommonPageAdapter;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class PrepareActivity extends BaseActivity implements OnCheckedChangeListener,
    OnPageChangeListener, OnClickListener {

  // 当前状态
  private String currentTerm = Constant.TERM_SEMESTER0;
  private String currenTopic = Constant.DOMIAN_LANGUAGE;

  // 后退收藏锁屏
  private ImageView iv_act_prepare_back = null;
  private ImageView iv_act_prepare_coll = null;
  private ImageView iv_act_prepare_loc = null;

  // 学期
  private RadioGroup rg_act_prepare_term = null;
  private RadioButton rb_act_prepare_top = null;
  private RadioButton rb_act_prepare_bottom = null;

  // 分类
  private RadioButton[] topicRadioButtons = null;
  private RadioButton rb_act_prepare_class_language = null;
  private RadioButton rb_act_prepare_class_thinking = null;
  private RadioButton rb_act_prepare_class_read = null;
  private RadioButton rb_act_prepare_class_write = null;
  private RadioButton rb_act_prepare_class_quality = null;

  private final int numPerPage = 8;// 查询结果每页显示8条数据
  private final int numPerLine = 4;// 每条显示四个数据

  private ViewPager vp_act_prepare_result = null;
  private CommonPageAdapter myPageAdapter = null;
  // 翻页按钮
  private ImageView rl_act_prepare_result_pre = null;
  private ImageView rl_act_prepare_result_next = null;
  // 页码状态
  private LinearLayout ll_act_prepare_result_bar = null;
  private int totalPageNum = 0;

  // 数据集
  private JsonArray activities = null;
  private final ArrayList<View> nameViews = new ArrayList<View>();

  private final static String SHAREDNAME = "prepareHistory";// 配置文件的名称
  private SharedPreferences sharedPreferences = null;

  /**
   * 翻页处理
   */
  private final MessageHandler<JsonObject> controlHandler = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body.has("previous") && body.getBoolean("previous")) {
        vp_act_prepare_result.arrowScroll(View.FOCUS_LEFT);
      } else if (body.has("next") && body.getBoolean("next")) {
        vp_act_prepare_result.arrowScroll(View.FOCUS_RIGHT);
      }
    }
  };

  /**
   * post动作处理
   */
  private final MessageHandler<JsonObject> postHandler = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      String action = body.getString("action");
      // 仅仅处理action为post动作
      if (!"post".equalsIgnoreCase(action)) {
        return;
      }
      JsonObject query = body.getObject("query");
      if (query != null && query.has("type")
          && !Constant.DATAREGISTRY_TYPE_PREPARE.equals(query.getString("type"))) {
        return;
      }
      dataHandler(body);
    }
  };

  private boolean isLocal = true;

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (!isLocal) {
      return;
    }
    if (isChecked) {
      switch (buttonView.getId()) {
      // 学期的选中事件
        case R.id.rb_act_prepare_top:
        case R.id.rb_act_prepare_bottom:
          this.onTermViewClick(buttonView.getId());
          break;
        // 类别的选中事件
        case R.id.rb_act_prepare_class_language:
        case R.id.rb_act_prepare_class_thinking:
        case R.id.rb_act_prepare_class_read:
        case R.id.rb_act_prepare_class_write:
        case R.id.rb_act_prepare_class_quality:
          this.topicChooser(buttonView.getId());
          this.onMyClassViewClick(buttonView.getId());
          break;

        default:
          break;
      }
    }
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_prepare_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_prepare_coll:
        bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, Json.createObject().set("action", "post").set(
            "query", Json.createObject().set("type", Constant.DATAREGISTRY_TYPE_FAVOURITE)), null);
        break;
      case R.id.iv_act_prepare_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        Toast.makeText(this, "黑屏", Toast.LENGTH_LONG).show();
        break;

      // 查询结果翻页
      case R.id.rl_act_prepare_result_pre:
      case R.id.rl_act_prepare_result_next:
        this.onResultPrePageClick(v.getId());
        break;

      default:
        break;
    }
  }

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onPageScrollStateChanged(int state) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onPageSelected(int position) {
    if (position == 0) {
      this.rl_act_prepare_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_prepare_result_pre.setVisibility(View.VISIBLE);
    }
    if (position == totalPageNum - 1) {
      this.rl_act_prepare_result_next.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_prepare_result_next.setVisibility(View.VISIBLE);
    }

    for (int i = 0; i < this.ll_act_prepare_result_bar.getChildCount(); i++) {
      if (position == i) {
        this.ll_act_prepare_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_current);
      } else {
        this.ll_act_prepare_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_other);
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_prepare);
    this.initView();
    this.readHistoryData();
    Bundle extras = this.getIntent().getExtras();
    JsonObject body = (JsonObject) extras.get("msg");
    JsonArray activities = body.getArray("activities");
    if (activities == null) {
      this.sendQueryMessage();
    } else {
      this.readQuery(body.getObject("query"));
      this.activities = activities;
      this.bindDataToView();
      this.isLocal = false;
      this.bindHistoryDataToView();
      this.isLocal = true;
    }
  }

  @Override
  protected void onPause() {
    bus.unregisterHandler(Constant.ADDR_TOPIC, postHandler);
    bus.unregisterHandler(Constant.ADDR_VIEW_CONTROL, controlHandler);
    super.onPause();
  }

  @Override
  protected void onResume() {
    bus.registerHandler(Constant.ADDR_TOPIC, postHandler);
    bus.registerHandler(Constant.ADDR_VIEW_CONTROL, controlHandler);
    super.onResume();
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView() {
    this.ll_act_prepare_result_bar.removeAllViews();
    this.vp_act_prepare_result.removeAllViews();
    this.nameViews.clear();
    if (this.activities == null) {
      return;
    }

    int index = 0;// 下标计数器
    int counter = activities.length();
    this.totalPageNum =
        (counter % this.numPerPage == 0) ? (counter / numPerPage) : (counter / this.numPerPage + 1);
    // 页码数量
    for (int i = 0; i < totalPageNum; i++) {
      LinearLayout rootContainer = new LinearLayout(this);
      rootContainer.setOrientation(LinearLayout.VERTICAL);
      // 行数量
      for (int j = 0; j < numPerPage / numPerLine; j++) {
        if (index >= counter) {
          break;
        }
        LinearLayout innerContainer = new LinearLayout(this);
        innerContainer.setOrientation(LinearLayout.HORIZONTAL);
        // 列数量
        for (int k = 0; k < numPerLine; k++) {
          if (index >= counter) {
            break;
          }
          // 构建ItemView对象
          TextView textView = new TextView(this);
          LinearLayout.LayoutParams params =
              new LinearLayout.LayoutParams(130, LayoutParams.WRAP_CONTENT);
          params.setMargins(12, 40, 12, 40);
          textView.setLayoutParams(params);
          textView.setGravity(Gravity.CENTER_HORIZONTAL);
          textView.setPadding(15, 10, 18, 0);
          final String title = this.activities.getObject(index).getString(Constant.TITLE);
          if (title.matches("^\\d{4}.*")) {
            textView.setText(title.substring(4, title.length()));
          } else {
            textView.setText(title);
          }
          textView.setBackgroundResource(R.drawable.harm_result_item_bg);
          textView.setClickable(true);
          textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              JsonObject msg = Json.createObject();
              JsonObject tags = Json.createObject();
              tags.set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_PREPARE);
              tags.set(Constant.TERM, currentTerm);
              tags.set(Constant.TOPIC, currenTopic);
              msg.set(Constant.TAGS, tags);
              msg.set(Constant.TITLE, title);
              msg.set("action", "post");
              bus.send(Bus.LOCAL + Constant.ADDR_ACTIVITY, msg, null);
            }
          });
          innerContainer.addView(textView);
          index++;
        }
        rootContainer.addView(innerContainer);
      }
      this.nameViews.add(rootContainer);
      ImageView imageView = new ImageView(this);
      if (i == 0) {
        imageView.setBackgroundResource(R.drawable.common_result_dot_current);
      } else {
        imageView.setBackgroundResource(R.drawable.common_result_dot_other);
      }
      this.ll_act_prepare_result_bar.addView(imageView);
    }
    this.myPageAdapter = new CommonPageAdapter(this.nameViews);
    this.vp_act_prepare_result.setAdapter(this.myPageAdapter);

    if (this.totalPageNum > 1) {
      this.rl_act_prepare_result_pre.setVisibility(View.INVISIBLE);
      this.rl_act_prepare_result_next.setVisibility(View.VISIBLE);
    } else {
      this.rl_act_prepare_result_pre.setVisibility(View.INVISIBLE);
      this.rl_act_prepare_result_next.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * 把查询完成的的历史记忆绑定到View
   */
  private void bindHistoryDataToView() {
    // 回显学期
    if (Constant.TERM_SEMESTER0.equals(this.currentTerm)) {
      this.rb_act_prepare_top.setChecked(true);
    } else if (Constant.TERM_SEMESTER1.equals(this.currentTerm)) {
      this.rb_act_prepare_bottom.setChecked(true);
    }
    // 回显分类
    if (Constant.DOMIAN_LANGUAGE.equals(this.currenTopic)) {
      this.rb_act_prepare_class_language.setChecked(true);
      this.topicChooser(this.rb_act_prepare_class_language.getId());
    } else if (Constant.DOMIAN_THINKING.equals(this.currenTopic)) {
      this.rb_act_prepare_class_thinking.setChecked(true);
      this.topicChooser(this.rb_act_prepare_class_thinking.getId());
    } else if (Constant.DOMIAN_READ.equals(this.currenTopic)) {
      this.rb_act_prepare_class_read.setChecked(true);
      this.topicChooser(this.rb_act_prepare_class_read.getId());
    } else if (Constant.DOMIAN_WRITE.equals(this.currenTopic)) {
      this.rb_act_prepare_class_write.setChecked(true);
      this.topicChooser(this.rb_act_prepare_class_write.getId());
    } else if (Constant.DOMIAN_QUALITY.equals(this.currenTopic)) {
      this.rb_act_prepare_class_quality.setChecked(true);
      this.topicChooser(this.rb_act_prepare_class_quality.getId());
    }
  }

  /**
   * 数据处理
   */
  private void dataHandler(JsonObject body) {
    JsonObject query = body.getObject("query");
    readQuery(query);
    activities = body.getArray("activities");
    isLocal = activities == null;
    bindDataToView();
    bindHistoryDataToView();
    isLocal = true;
  }

  /**
   * 初始化View对象 设置点击事件 设置光标事件监听 添加到对应集合
   */
  private void initView() {
    // 后退 收藏 所屏
    this.iv_act_prepare_back = (ImageView) this.findViewById(R.id.iv_act_prepare_back);
    this.iv_act_prepare_coll = (ImageView) this.findViewById(R.id.iv_act_prepare_coll);
    this.iv_act_prepare_loc = (ImageView) this.findViewById(R.id.iv_act_prepare_loc);
    this.iv_act_prepare_back.setOnClickListener(this);
    this.iv_act_prepare_coll.setOnClickListener(this);
    this.iv_act_prepare_loc.setOnClickListener(this);

    // 初始化学期
    this.rg_act_prepare_term = (RadioGroup) this.findViewById(R.id.rg_act_prepare_term);
    this.rb_act_prepare_top = (RadioButton) this.findViewById(R.id.rb_act_prepare_top);
    this.rb_act_prepare_bottom = (RadioButton) this.findViewById(R.id.rb_act_prepare_bottom);

    int termChildren = this.rg_act_prepare_term.getChildCount();
    for (int i = 0; i < termChildren; i++) {
      RadioButton child = (RadioButton) this.rg_act_prepare_term.getChildAt(i);
      child.setOnCheckedChangeListener(this);
    }

    // 初始化分类
    this.rb_act_prepare_class_language =
        (RadioButton) this.findViewById(R.id.rb_act_prepare_class_language);
    this.rb_act_prepare_class_thinking =
        (RadioButton) this.findViewById(R.id.rb_act_prepare_class_thinking);
    this.rb_act_prepare_class_read =
        (RadioButton) this.findViewById(R.id.rb_act_prepare_class_read);
    this.rb_act_prepare_class_write =
        (RadioButton) this.findViewById(R.id.rb_act_prepare_class_write);
    this.rb_act_prepare_class_quality =
        (RadioButton) this.findViewById(R.id.rb_act_prepare_class_quality);

    this.topicRadioButtons =
        new RadioButton[] {
            this.rb_act_prepare_class_language, this.rb_act_prepare_class_thinking,
            this.rb_act_prepare_class_read, this.rb_act_prepare_class_write,
            this.rb_act_prepare_class_quality};

    int classChildren = this.topicRadioButtons.length;
    for (int i = 0; i < classChildren; i++) {
      this.topicRadioButtons[i].setOnCheckedChangeListener(this);
    }

    // 初始化查询结果视图
    this.vp_act_prepare_result = (ViewPager) this.findViewById(R.id.vp_act_prepare_result);
    this.vp_act_prepare_result.setOnPageChangeListener(this);

    // 初始化查询结果控制
    this.rl_act_prepare_result_pre = (ImageView) this.findViewById(R.id.rl_act_prepare_result_pre);
    this.rl_act_prepare_result_next =
        (ImageView) this.findViewById(R.id.rl_act_prepare_result_next);
    this.rl_act_prepare_result_pre.setOnClickListener(this);
    this.rl_act_prepare_result_next.setOnClickListener(this);

    // 初始化结果数量视图
    this.ll_act_prepare_result_bar =
        (LinearLayout) this.findViewById(R.id.ll_act_prepare_result_bar);

  }

  // 判定是否时有效的年级数值
  private boolean isRightfulTerm(String term) {
    if (Constant.TERM_SEMESTER0.equals(term) || Constant.TERM_SEMESTER1.equals(term)) {
      return true;
    }
    return false;
  }

  // 判定是否时有效的类别数值
  private boolean isRightfulTopic(String topic) {
    if (Constant.DOMIAN_LANGUAGE.equals(topic) || Constant.DOMIAN_THINKING.equals(topic)
        || Constant.DOMIAN_READ.equals(topic) || Constant.DOMIAN_WRITE.equals(topic)
        || Constant.DOMIAN_QUALITY.equals(topic)) {
      return true;
    }
    return false;
  }

  /**
   * 处理类别的点击事件
   * 
   * @param i
   */
  private void onMyClassViewClick(int id) {
    switch (id) {
      case R.id.rb_act_prepare_class_language:
        this.currenTopic = Constant.DOMIAN_LANGUAGE;
        break;
      case R.id.rb_act_prepare_class_thinking:
        this.currenTopic = Constant.DOMIAN_THINKING;
        break;
      case R.id.rb_act_prepare_class_read:
        this.currenTopic = Constant.DOMIAN_READ;
        break;
      case R.id.rb_act_prepare_class_write:
        this.currenTopic = Constant.DOMIAN_WRITE;
        break;
      case R.id.rb_act_prepare_class_quality:
        this.currenTopic = Constant.DOMIAN_QUALITY;
        break;
      default:
        break;
    }
    this.saveHistory(Constant.TOPIC, this.currenTopic);
    this.sendQueryMessage();
  }

  /**
   * 处理上下翻页的点击事件
   * 
   * @param id
   */
  private void onResultPrePageClick(int id) {
    JsonObject msg = Json.createObject();
    if (id == R.id.rl_act_prepare_result_pre) {
      msg.set("previous", true);
    } else if (id == R.id.rl_act_prepare_result_next) {
      msg.set("next", true);
    }
    bus.send(Bus.LOCAL + Constant.ADDR_VIEW_CONTROL, msg, null);
  }

  /**
   * 处理学期的点击事件
   * 
   * @param i
   */
  private void onTermViewClick(int id) {
    switch (id) {
      case R.id.rb_act_prepare_top:
        this.currentTerm = Constant.TERM_SEMESTER0;
        break;
      case R.id.rb_act_prepare_bottom:
        this.currentTerm = Constant.TERM_SEMESTER1;
        break;
      default:
        break;
    }
    this.saveHistory(Constant.TERM, this.currentTerm);
    this.sendQueryMessage();
  }

  /**
   * 查询历史数据
   */
  private void readHistoryData() {
    this.sharedPreferences = this.getSharedPreferences(SHAREDNAME, MODE_PRIVATE);
    this.currentTerm = this.sharedPreferences.getString(Constant.TERM, this.currentTerm);
    this.currenTopic = this.sharedPreferences.getString(Constant.TOPIC, this.currenTopic);
  }

  /**
   * 解析条件
   * 
   * @param query
   */
  private void readQuery(JsonObject query) {
    if (query != null) {
      String tempTerm = query.getString(Constant.TERM);
      String tempClass = query.getString(Constant.TOPIC);
      if (tempTerm != null && isRightfulTerm(tempTerm)) {
        currentTerm = tempTerm;
        saveHistory(Constant.TERM, currentTerm);
      } else if (query.has(Constant.TERM) && !isRightfulTerm(tempTerm)) {
        Toast.makeText(this, "无效的学期数值", Toast.LENGTH_SHORT).show();
      }
      if (tempClass != null && isRightfulTopic(tempClass)) {
        currenTopic = tempClass;
        saveHistory(Constant.TOPIC, currenTopic);
      } else if (query.has(Constant.TOPIC) && !isRightfulTopic(tempClass)) {
        Toast.makeText(this, "无效的类别数值", Toast.LENGTH_SHORT).show();
      }
    }
  }

  /**
   * 保存到历史数据
   * 
   * @param key
   * @param value
   */
  private void saveHistory(String key, String value) {
    if (this.sharedPreferences != null) {
      Editor edit = this.sharedPreferences.edit();
      edit.putString(key, value);
      edit.commit();
    }
  }

  /**
   * 构建查询的bus消息,get动作处理
   */
  private void sendQueryMessage() {
    JsonObject msg = Json.createObject();
    msg.set("action", "get");
    JsonObject query = Json.createObject();
    query.set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_PREPARE);
    query.set(Constant.TERM, this.currentTerm);
    query.set(Constant.TOPIC, this.currenTopic);
    msg.set("query", query);
    bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        dataHandler(body);
      }
    });
  }

  private void topicChooser(int id) {
    int len = this.topicRadioButtons.length;
    for (int i = 0; i < len; i++) {
      RadioButton radioButton = this.topicRadioButtons[i];
      if (radioButton.getId() == id) {
        radioButton.setChecked(true);
      } else {
        radioButton.setChecked(false);
      }
    }
  }
}
