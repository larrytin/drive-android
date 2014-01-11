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
import android.view.View.OnFocusChangeListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class HarmonyActivity extends BaseActivity implements OnCheckedChangeListener,
    OnFocusChangeListener, OnPageChangeListener, OnClickListener {

  // 当前状态
  private String currentGrade = Constant.GRADE_LITTLE;
  private String currentTerm = Constant.TERM_SEMESTER0;
  private String currenTopic = Constant.DOMIAN_HEALTH;

  // 后退收藏锁屏
  private ImageView iv_act_harmony_back = null;
  private ImageView iv_act_harmony_coll = null;
  private ImageView iv_act_harmony_loc = null;

  // 年级
  private RadioGroup rg_act_harmony_grade = null;
  private RadioButton rb_act_harmony_little = null;
  private RadioButton rb_act_harmony_middle = null;
  private RadioButton rb_act_harmony_big = null;
  private RadioButton rb_act_harmony_pre = null;

  // 学期
  private RadioGroup rg_act_harmony_term = null;
  private RadioButton rb_act_harmony_top = null;
  private RadioButton rb_act_harmony_bottom = null;

  // 分类
  private RadioGroup rg_act_harmony_class = null;
  private RadioButton rb_act_harmony_class_health = null;
  private RadioButton rb_act_harmony_class_language = null;
  private RadioButton rb_act_harmony_class_world = null;
  private RadioButton rb_act_harmony_class_scinece = null;
  private RadioButton rb_act_harmony_class_math = null;
  private RadioButton rb_act_harmony_class_music = null;
  private RadioButton rb_act_harmony_class_art = null;

  private final int numPerPage = 18;// 查询结果每页显示18条数据
  private final int numPerLine = 6;// 每条显示六个数据

  private ViewPager vp_act_harmony_result = null;
  private CommonPageAdapter myPageAdapter = null;
  // 翻页按钮
  private ImageView rl_act_harmony_result_pre = null;
  private ImageView rl_act_harmony_result_next = null;
  // 页码状态
  private LinearLayout ll_act_harmony_result_bar = null;
  private int totalPageNum = 0;

  // 数据集
  private JsonArray activities = null;
  private final ArrayList<View> nameViews = new ArrayList<View>();

  private final static String SHAREDNAME = "harmonyHistory";// 配置文件的名称
  private SharedPreferences sharedPreferences = null;

  /**
   * 翻页控制
   */
  private final MessageHandler<JsonObject> controlHandler = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body.has("previous") && body.getBoolean("previous")) {
        vp_act_harmony_result.arrowScroll(View.FOCUS_LEFT);
      } else if (body.has("next") && body.getBoolean("next")) {
        vp_act_harmony_result.arrowScroll(View.FOCUS_RIGHT);
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
          && !Constant.DATAREGISTRY_TYPE_HARMONY.equals(query.getString("type"))) {
        return;
      }
      dataHandler(body);
    }
  };

  private boolean isLocal = true;

  // 判定是否时有效的班级数值
  public boolean isRightfulGrade(String grade) {
    if (Constant.GRADE_LITTLE.equals(grade) || Constant.GRADE_MID.equals(grade)
        || Constant.GRADE_BIG.equals(grade) || Constant.GRADE_PRE.equals(grade)) {
      return true;
    }
    return false;
  }

  // 判定是否时有效的年级数值
  public boolean isRightfulTerm(String term) {
    if (Constant.TERM_SEMESTER0.equals(term) || Constant.TERM_SEMESTER1.equals(term)) {
      return true;
    }
    return false;
  }

  // 判定是否时有效的类别数值
  public boolean isRightfulTopic(String topic) {
    if (Constant.DOMIAN_HEALTH.equals(topic) || Constant.DOMIAN_LANGUAGE.equals(topic)
        || Constant.DOMIAN_WORLD.equals(topic) || Constant.DOMIAN_SCIENCE.equals(topic)
        || Constant.DOMIAN_MATH.equals(topic) || Constant.DOMIAN_MUSIC.equals(topic)
        || Constant.DOMIAN_ART.equals(topic)) {
      return true;
    }
    return false;
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (!isLocal) {
      return;
    }
    if (isChecked) {
      switch (buttonView.getId()) {
      // 年级的选中事件
        case R.id.rb_act_harmony_little:
        case R.id.rb_act_harmony_middle:
        case R.id.rb_act_harmony_big:
        case R.id.rb_act_harmony_pre:
          this.onGradeViewClick(buttonView.getId());
          break;
        // 学期的选中事件
        case R.id.rb_act_harmony_top:
        case R.id.rb_act_harmony_bottom:
          this.onTermViewClick(buttonView.getId());
          break;
        // 类别的选中事件
        case R.id.rb_act_harmony_class_health:
        case R.id.rb_act_harmony_class_language:
        case R.id.rb_act_harmony_class_world:
        case R.id.rb_act_harmony_class_scinece:
        case R.id.rb_act_harmony_class_math:
        case R.id.rb_act_harmony_class_music:
        case R.id.rb_act_harmony_class_art:
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
      case R.id.iv_act_harmony_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_harmony_coll:
        bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, Json.createObject().set("action", "post").set(
            "query", Json.createObject().set("type", Constant.DATAREGISTRY_TYPE_FAVOURITE)), null);
        break;
      case R.id.iv_act_harmony_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        Toast.makeText(this, "黑屏", Toast.LENGTH_LONG).show();
        break;

      // 查询结果翻页
      case R.id.rl_act_harmony_result_pre:
      case R.id.rl_act_harmony_result_next:
        this.onResultPrePageClick(v.getId());
        break;

      default:
        break;
    }
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {

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
      this.rl_act_harmony_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_harmony_result_pre.setVisibility(View.VISIBLE);
    }
    if (position == totalPageNum - 1) {
      this.rl_act_harmony_result_next.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_harmony_result_next.setVisibility(View.VISIBLE);
    }

    for (int i = 0; i < this.ll_act_harmony_result_bar.getChildCount(); i++) {
      if (position == i) {
        this.ll_act_harmony_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_current);
      } else {
        this.ll_act_harmony_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_other);
      }
    }
  }

  /**
   * 查询历史数据
   */
  public void readHistoryData() {
    this.sharedPreferences = this.getSharedPreferences(SHAREDNAME, MODE_PRIVATE);
    this.currentGrade = this.sharedPreferences.getString(Constant.GRADE, this.currentGrade);
    this.currentTerm = this.sharedPreferences.getString(Constant.TERM, this.currentTerm);
    this.currenTopic = this.sharedPreferences.getString(Constant.TOPIC, this.currenTopic);
  }

  /**
   * 解析条件
   * 
   * @param query
   */
  public void readQuery(JsonObject query) {
    if (query != null) {
      String tempGrade = query.getString(Constant.GRADE);
      String tempTerm = query.getString(Constant.TERM);
      String tempClass = query.getString(Constant.TOPIC);
      if (tempGrade != null && isRightfulGrade(tempGrade)) {
        currentGrade = tempGrade;
        saveHistory(Constant.GRADE, currentGrade);
      } else if (query.has(Constant.GRADE) && !isRightfulGrade(tempGrade)) {
        Toast.makeText(HarmonyActivity.this, "无效的年级数值", Toast.LENGTH_SHORT).show();
      }
      if (tempTerm != null && isRightfulTerm(tempTerm)) {
        currentTerm = tempTerm;
        saveHistory(Constant.TERM, currentTerm);
      } else if (query.has(Constant.TERM) && !isRightfulTerm(tempTerm)) {
        Toast.makeText(HarmonyActivity.this, "无效的学期数值", Toast.LENGTH_SHORT).show();
      }
      if (tempClass != null && isRightfulTopic(tempClass)) {
        currenTopic = tempClass;
        saveHistory(Constant.TOPIC, currenTopic);
      } else if (query.has(Constant.TOPIC) && !isRightfulTopic(tempClass)) {
        Toast.makeText(HarmonyActivity.this, "无效的类别数值", Toast.LENGTH_SHORT).show();
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_harmony);
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
    this.ll_act_harmony_result_bar.removeAllViews();
    this.vp_act_harmony_result.removeAllViews();
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
          params.setMargins(7, 12, 7, 12);
          textView.setLayoutParams(params);
          textView.setGravity(Gravity.CENTER_HORIZONTAL);
          textView.setPadding(15, 10, 18, 0);
          textView.setText(this.activities.getObject(index).getString(Constant.TITLE));
          textView.setBackgroundResource(R.drawable.harm_result_item_bg);
          textView.setClickable(true);
          textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              JsonObject msg = Json.createObject();
              JsonObject tags = Json.createObject();
              tags.set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_HARMONY);
              tags.set(Constant.GRADE, currentGrade);
              tags.set(Constant.TERM, currentTerm);
              tags.set(Constant.TOPIC, currenTopic);
              msg.set(Constant.TAGS, tags);
              msg.set(Constant.TITLE, ((TextView) v).getText().toString());
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
      this.ll_act_harmony_result_bar.addView(imageView);
    }
    this.myPageAdapter = new CommonPageAdapter(this.nameViews);
    this.vp_act_harmony_result.setAdapter(this.myPageAdapter);

    if (this.totalPageNum > 1) {
      this.rl_act_harmony_result_pre.setVisibility(View.INVISIBLE);
      this.rl_act_harmony_result_next.setVisibility(View.VISIBLE);
    } else {
      this.rl_act_harmony_result_pre.setVisibility(View.INVISIBLE);
      this.rl_act_harmony_result_next.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * 把查询完成的的历史记忆绑定到View
   */
  private void bindHistoryDataToView() {
    // 回显年级
    if (Constant.GRADE_LITTLE.equals(this.currentGrade)) {
      this.rb_act_harmony_little.setChecked(true);
    } else if (Constant.GRADE_MID.equals(this.currentGrade)) {
      this.rb_act_harmony_middle.setChecked(true);
    } else if (Constant.GRADE_BIG.equals(this.currentGrade)) {
      this.rb_act_harmony_big.setChecked(true);
    } else if (Constant.GRADE_PRE.equals(this.currentGrade)) {
      this.rb_act_harmony_pre.setChecked(true);
    }

    // 回显学期
    if (Constant.TERM_SEMESTER0.equals(this.currentTerm)) {
      this.rb_act_harmony_top.setChecked(true);
    } else if (Constant.TERM_SEMESTER1.equals(this.currentTerm)) {
      this.rb_act_harmony_bottom.setChecked(true);
    }
    // 回显分类
    if (Constant.DOMIAN_HEALTH.equals(this.currenTopic)) {
      this.rb_act_harmony_class_health.setChecked(true);
    } else if (Constant.DOMIAN_LANGUAGE.equals(this.currenTopic)) {
      this.rb_act_harmony_class_language.setChecked(true);
    } else if (Constant.DOMIAN_WORLD.equals(this.currenTopic)) {
      this.rb_act_harmony_class_world.setChecked(true);
    } else if (Constant.DOMIAN_SCIENCE.equals(this.currenTopic)) {
      this.rb_act_harmony_class_scinece.setChecked(true);
    } else if (Constant.DOMIAN_MATH.equals(this.currenTopic)) {
      this.rb_act_harmony_class_math.setChecked(true);
    } else if (Constant.DOMIAN_MUSIC.equals(this.currenTopic)) {
      this.rb_act_harmony_class_music.setChecked(true);
    } else if (Constant.DOMIAN_ART.equals(this.currenTopic)) {
      this.rb_act_harmony_class_art.setChecked(true);
    }
  }

  /**
   * 数据解析
   * 
   * @param body
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
    this.iv_act_harmony_back = (ImageView) this.findViewById(R.id.iv_act_harmony_back);
    this.iv_act_harmony_coll = (ImageView) this.findViewById(R.id.iv_act_harmony_coll);
    this.iv_act_harmony_loc = (ImageView) this.findViewById(R.id.iv_act_harmony_loc);
    this.iv_act_harmony_back.setOnClickListener(this);
    this.iv_act_harmony_coll.setOnClickListener(this);
    this.iv_act_harmony_loc.setOnClickListener(this);

    // 初始化年级
    this.rg_act_harmony_grade = (RadioGroup) this.findViewById(R.id.rg_act_harmony_grade);
    this.rb_act_harmony_little = (RadioButton) this.findViewById(R.id.rb_act_harmony_little);
    this.rb_act_harmony_middle = (RadioButton) this.findViewById(R.id.rb_act_harmony_middle);
    this.rb_act_harmony_big = (RadioButton) this.findViewById(R.id.rb_act_harmony_big);
    this.rb_act_harmony_pre = (RadioButton) this.findViewById(R.id.rb_act_harmony_pre);

    int gradeChildren = this.rg_act_harmony_grade.getChildCount();
    for (int i = 0; i < gradeChildren; i++) {
      RadioButton child = (RadioButton) this.rg_act_harmony_grade.getChildAt(i);
      child.setOnCheckedChangeListener(this);
      child.setOnFocusChangeListener(this);
    }

    // 初始化学期
    this.rg_act_harmony_term = (RadioGroup) this.findViewById(R.id.rg_act_harmony_term);
    this.rb_act_harmony_top = (RadioButton) this.findViewById(R.id.rb_act_harmony_top);
    this.rb_act_harmony_bottom = (RadioButton) this.findViewById(R.id.rb_act_harmony_bottom);

    int termChildren = this.rg_act_harmony_term.getChildCount();
    for (int i = 0; i < termChildren; i++) {
      RadioButton child = (RadioButton) this.rg_act_harmony_term.getChildAt(i);
      child.setOnCheckedChangeListener(this);
      child.setOnFocusChangeListener(this);
    }

    // 初始化分类
    this.rg_act_harmony_class = (RadioGroup) this.findViewById(R.id.rg_act_harmony_class);
    this.rb_act_harmony_class_health =
        (RadioButton) this.findViewById(R.id.rb_act_harmony_class_health);
    this.rb_act_harmony_class_language =
        (RadioButton) this.findViewById(R.id.rb_act_harmony_class_language);
    this.rb_act_harmony_class_world =
        (RadioButton) this.findViewById(R.id.rb_act_harmony_class_world);
    this.rb_act_harmony_class_scinece =
        (RadioButton) this.findViewById(R.id.rb_act_harmony_class_scinece);
    this.rb_act_harmony_class_math =
        (RadioButton) this.findViewById(R.id.rb_act_harmony_class_math);
    this.rb_act_harmony_class_music =
        (RadioButton) this.findViewById(R.id.rb_act_harmony_class_music);
    this.rb_act_harmony_class_art = (RadioButton) this.findViewById(R.id.rb_act_harmony_class_art);

    int classChildren = this.rg_act_harmony_class.getChildCount();
    for (int i = 0; i < classChildren; i++) {
      RadioButton child = (RadioButton) this.rg_act_harmony_class.getChildAt(i);
      child.setOnCheckedChangeListener(this);
      child.setOnFocusChangeListener(this);
    }

    // 初始化查询结果视图
    this.vp_act_harmony_result = (ViewPager) this.findViewById(R.id.vp_act_harmony_result);
    this.vp_act_harmony_result.setOnPageChangeListener(this);

    // 初始化查询结果控制
    this.rl_act_harmony_result_pre = (ImageView) this.findViewById(R.id.rl_act_harmony_result_pre);
    this.rl_act_harmony_result_next =
        (ImageView) this.findViewById(R.id.rl_act_harmony_result_next);
    this.rl_act_harmony_result_pre.setOnClickListener(this);
    this.rl_act_harmony_result_next.setOnClickListener(this);

    // 初始化结果数量视图
    this.ll_act_harmony_result_bar =
        (LinearLayout) this.findViewById(R.id.ll_act_harmony_result_bar);

  }

  /**
   * 处理年级的点击事件
   * 
   * @param i
   */
  private void onGradeViewClick(int id) {
    switch (id) {
      case R.id.rb_act_harmony_little:
        this.currentGrade = Constant.GRADE_LITTLE;
        break;
      case R.id.rb_act_harmony_middle:
        this.currentGrade = Constant.GRADE_MID;
        break;
      case R.id.rb_act_harmony_big:
        this.currentGrade = Constant.GRADE_BIG;
        break;
      case R.id.rb_act_harmony_pre:
        this.currentGrade = Constant.GRADE_PRE;
        break;
      default:
        break;
    }
    this.saveHistory(Constant.GRADE, this.currentGrade);
    this.sendQueryMessage();
  }

  /**
   * 处理类别的点击事件
   * 
   * @param i
   */
  private void onMyClassViewClick(int id) {
    switch (id) {
      case R.id.rb_act_harmony_class_health:
        this.currenTopic = Constant.DOMIAN_HEALTH;
        break;
      case R.id.rb_act_harmony_class_language:
        this.currenTopic = Constant.DOMIAN_LANGUAGE;
        break;
      case R.id.rb_act_harmony_class_world:
        this.currenTopic = Constant.DOMIAN_WORLD;
        break;
      case R.id.rb_act_harmony_class_scinece:
        this.currenTopic = Constant.DOMIAN_SCIENCE;
        break;
      case R.id.rb_act_harmony_class_math:
        this.currenTopic = Constant.DOMIAN_MATH;
        break;
      case R.id.rb_act_harmony_class_music:
        this.currenTopic = Constant.DOMIAN_MUSIC;
        break;
      case R.id.rb_act_harmony_class_art:
        this.currenTopic = Constant.DOMIAN_ART;
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
    if (id == R.id.rl_act_harmony_result_pre) {
      msg.set("previous", true);
    } else if (id == R.id.rl_act_harmony_result_next) {
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
      case R.id.rb_act_harmony_top:
        this.currentTerm = Constant.TERM_SEMESTER0;
        break;
      case R.id.rb_act_harmony_bottom:
        this.currentTerm = Constant.TERM_SEMESTER1;
        break;
      default:
        break;
    }
    this.saveHistory(Constant.TERM, this.currentTerm);
    this.sendQueryMessage();
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
   * 构建查询的bus消息
   */
  private void sendQueryMessage() {
    JsonObject msg = Json.createObject();
    msg.set("action", "get");
    JsonObject query = Json.createObject();
    query.set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_HARMONY);
    query.set(Constant.GRADE, this.currentGrade);
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

}
