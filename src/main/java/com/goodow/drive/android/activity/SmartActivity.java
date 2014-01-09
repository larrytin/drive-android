package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SmartActivity extends BaseActivity implements OnClickListener,
    OnCheckedChangeListener, OnPageChangeListener {

  /**
   * 翻页是配器
   * 
   * @author dpw
   * 
   */
  private class MyPageAdapter extends PagerAdapter {
    private ArrayList<View> tempView = null;

    public MyPageAdapter(ArrayList<View> tempView) {
      this.tempView = tempView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      if (position >= this.tempView.size()) {
        return;
      }
      ((ViewPager) container).removeView(this.tempView.get(position));
    }

    @Override
    public int getCount() {
      if (this.tempView == null) {
        return 0;
      } else {
        return this.tempView.size();
      }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
      View view = this.tempView.get(position);
      container.addView(view);
      return view;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
      return arg0 == arg1;
    }
  }

  // 当前状态
  private String currentGrade = Constant.GRADE_LITTLE;
  private String currentTerm = Constant.TERM_SEMESTER0;
  private String currenTopic = Constant.DOMIAN_LANGUAGE;

  // 后退收藏锁屏
  private ImageView iv_act_smart_back = null;
  private ImageView iv_act_smart_coll = null;
  private ImageView iv_act_smart_loc = null;

  // 年级
  private RadioGroup rg_act_smart_grade = null;
  private RadioButton rb_act_smart_little = null;
  private RadioButton rb_act_smart_middle = null;
  private RadioButton rb_act_smart_big = null;
  private RadioButton rb_act_smart_pre = null;

  // 学期
  private RadioGroup rg_act_smart_term = null;
  private RadioButton rb_act_smart_top = null;
  private RadioButton rb_act_smart_bottom = null;

  // 分类
  private RadioButton[] topicRadioButtons = null;
  private RadioButton rb_act_smart_class_language = null;
  private RadioButton rb_act_smart_class_arithmetic = null;
  private RadioButton rb_act_smart_class_jigsaw = null;
  private RadioButton rb_act_smart_class_thinking = null;
  private RadioButton rb_act_smart_class_readable = null;
  private RadioButton rb_act_smart_class_body = null;

  private final int numPerPage = 8;// 查询结果每页显示8条数据
  private final int numPerLine = 4;// 每条显示四个数据

  private ViewPager vp_act_smart_result = null;
  private MyPageAdapter myPageAdapter = null;
  // 翻页按钮
  private ImageView rl_act_smart_result_pre = null;
  private ImageView rl_act_smart_result_next = null;
  // 页码状态
  private LinearLayout ll_act_smart_result_bar = null;
  private int totalPageNum = 0;

  // 数据集
  private JsonArray activities = null;
  private final ArrayList<View> nameViews = new ArrayList<View>();

  private final static String SHAREDNAME = "smartHistory";// 配置文件的名称
  private SharedPreferences sharedPreferences = null;

  boolean isLocal = true;

  private final MessageHandler<JsonObject> eventHandlerControl = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body.has("previous") && body.getBoolean("previous")) {
        vp_act_smart_result.arrowScroll(View.FOCUS_LEFT);
      } else if (body.has("next") && body.getBoolean("next")) {
        vp_act_smart_result.arrowScroll(View.FOCUS_RIGHT);
      }
    }
  };

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
          && !Constant.DATAREGISTRY_TYPE_SMART.equals(query.getString("type"))) {
        return;
      }
      readQuery(query);
      activities = body.getArray("activities");
      isLocal = activities == null;
      bindDataToView();
      bindHistoryDataToView();
      isLocal = true;
    }
  };

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
        Toast.makeText(SmartActivity.this, "无效的年级数值", Toast.LENGTH_SHORT).show();
      }
      if (tempTerm != null && isRightfulTerm(tempTerm)) {
        currentTerm = tempTerm;
        saveHistory(Constant.TERM, currentTerm);
      } else if (query.has(Constant.TERM) && !isRightfulTerm(tempTerm)) {
        Toast.makeText(SmartActivity.this, "无效的学期数值", Toast.LENGTH_SHORT).show();
      }
      if (tempClass != null && isRightfulTopic(tempClass)) {
        currenTopic = tempClass;
        saveHistory(Constant.TOPIC, currenTopic);
      } else if (query.has(Constant.TOPIC) && !isRightfulTopic(tempClass)) {
        Toast.makeText(SmartActivity.this, "无效的类别数值", Toast.LENGTH_SHORT).show();
      }
    }
  }

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
    if (Constant.DOMIAN_LANGUAGE.equals(topic) || Constant.DOMIAN_ARITHMETIC.equals(topic)
        || Constant.DOMIAN_THINKING.equals(topic) || Constant.DOMIAN_JIGSAW.equals(topic)
        || Constant.DOMIAN_READABLE.equals(topic) || Constant.DOMIAN_BODY.equals(topic)) {
      return true;
    }
    return false;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_smart);
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

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView() {
    this.ll_act_smart_result_bar.removeAllViews();
    this.vp_act_smart_result.removeAllViews();
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
              new LinearLayout.LayoutParams(133, LayoutParams.WRAP_CONTENT);
          params.setMargins(7, 40, 7, 40);
          textView.setLayoutParams(params);
          textView.setGravity(Gravity.CENTER_HORIZONTAL);
          textView.setPadding(15, 10, 20, 0);
          textView.setText(this.activities.getObject(index).getString(Constant.TITLE));
          textView.setBackgroundResource(R.drawable.harm_result_item_bg);
          textView.setClickable(true);
          textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              Toast.makeText(SmartActivity.this, "go:" + ((TextView) v).getText().toString(),
                  Toast.LENGTH_SHORT).show();
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
      this.ll_act_smart_result_bar.addView(imageView);
    }
    this.myPageAdapter = new MyPageAdapter(this.nameViews);
    this.vp_act_smart_result.setAdapter(this.myPageAdapter);

    if (this.totalPageNum > 1) {
      this.rl_act_smart_result_pre.setVisibility(View.INVISIBLE);
      this.rl_act_smart_result_next.setVisibility(View.VISIBLE);
    } else {
      this.rl_act_smart_result_pre.setVisibility(View.INVISIBLE);
      this.rl_act_smart_result_next.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * 初始化View对象 设置点击事件 设置光标事件监听 添加到对应集合
   */
  private void initView() {
    // 后退 收藏 所屏
    this.iv_act_smart_back = (ImageView) this.findViewById(R.id.iv_act_smart_back);
    this.iv_act_smart_coll = (ImageView) this.findViewById(R.id.iv_act_smart_coll);
    this.iv_act_smart_loc = (ImageView) this.findViewById(R.id.iv_act_smart_loc);
    this.iv_act_smart_back.setOnClickListener(this);
    this.iv_act_smart_coll.setOnClickListener(this);
    this.iv_act_smart_loc.setOnClickListener(this);

    // 初始化年级
    this.rg_act_smart_grade = (RadioGroup) this.findViewById(R.id.rg_act_smart_grade);
    this.rb_act_smart_little = (RadioButton) this.findViewById(R.id.rb_act_smart_little);
    this.rb_act_smart_middle = (RadioButton) this.findViewById(R.id.rb_act_smart_middle);
    this.rb_act_smart_big = (RadioButton) this.findViewById(R.id.rb_act_smart_big);
    this.rb_act_smart_pre = (RadioButton) this.findViewById(R.id.rb_act_smart_pre);

    int gradeChildren = this.rg_act_smart_grade.getChildCount();
    for (int i = 0; i < gradeChildren; i++) {
      RadioButton child = (RadioButton) this.rg_act_smart_grade.getChildAt(i);
      child.setOnCheckedChangeListener(this);
    }

    // 初始化学期
    this.rg_act_smart_term = (RadioGroup) this.findViewById(R.id.rg_act_smart_term);
    this.rb_act_smart_top = (RadioButton) this.findViewById(R.id.rb_act_smart_top);
    this.rb_act_smart_bottom = (RadioButton) this.findViewById(R.id.rb_act_smart_bottom);

    int termChildren = this.rg_act_smart_term.getChildCount();
    for (int i = 0; i < termChildren; i++) {
      RadioButton child = (RadioButton) this.rg_act_smart_term.getChildAt(i);
      child.setOnCheckedChangeListener(this);
    }

    // 初始化分类
    this.rb_act_smart_class_language =
        (RadioButton) this.findViewById(R.id.rb_act_smart_class_language);
    this.rb_act_smart_class_arithmetic =
        (RadioButton) this.findViewById(R.id.rb_act_smart_class_arithmetic);
    this.rb_act_smart_class_jigsaw =
        (RadioButton) this.findViewById(R.id.rb_act_smart_class_jigsaw);
    this.rb_act_smart_class_thinking =
        (RadioButton) this.findViewById(R.id.rb_act_smart_class_thinking);
    this.rb_act_smart_class_readable =
        (RadioButton) this.findViewById(R.id.rb_act_smart_class_readable);
    this.rb_act_smart_class_body = (RadioButton) this.findViewById(R.id.rb_act_smart_class_body);

    this.topicRadioButtons =
        new RadioButton[] {
            this.rb_act_smart_class_language, this.rb_act_smart_class_arithmetic,
            this.rb_act_smart_class_jigsaw, this.rb_act_smart_class_thinking,
            this.rb_act_smart_class_thinking, this.rb_act_smart_class_readable,
            this.rb_act_smart_class_body};
    int classChildren = this.topicRadioButtons.length;
    for (int i = 0; i < classChildren; i++) {
      this.topicRadioButtons[i].setOnCheckedChangeListener(this);
    }

    // 初始化查询结果视图
    this.vp_act_smart_result = (ViewPager) this.findViewById(R.id.vp_act_smart_result);
    this.vp_act_smart_result.setOnPageChangeListener(this);

    // 初始化查询结果控制
    this.rl_act_smart_result_pre = (ImageView) this.findViewById(R.id.rl_act_smart_result_pre);
    this.rl_act_smart_result_next = (ImageView) this.findViewById(R.id.rl_act_smart_result_next);
    this.rl_act_smart_result_pre.setOnClickListener(this);
    this.rl_act_smart_result_next.setOnClickListener(this);

    // 初始化结果数量视图
    this.ll_act_smart_result_bar = (LinearLayout) this.findViewById(R.id.ll_act_smart_result_bar);

  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_smart_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_smart_coll:
        bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, Json.createObject().set("action", "post").set(
            "query", Json.createObject().set("type", "收藏")), null);
        break;
      case R.id.iv_act_smart_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        Toast.makeText(this, "黑屏", Toast.LENGTH_LONG).show();
        break;

      // 查询结果翻页
      case R.id.rl_act_smart_result_pre:
      case R.id.rl_act_smart_result_next:
        this.onResultPrePageClick(v.getId());
        break;

      default:
        break;
    }
  }

  /**
   * 处理上下翻页的点击事件
   * 
   * @param id
   */
  private void onResultPrePageClick(int id) {
    JsonObject msg = Json.createObject();
    if (id == R.id.rl_act_smart_result_pre) {
      msg.set("previous", true);
    } else if (id == R.id.rl_act_smart_result_next) {
      msg.set("next", true);
    }
    bus.send(Bus.LOCAL + Constant.ADDR_VIEW_CONTROL, msg, eventHandlerControl);
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

  @Override
  public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    // TODO Auto-generated method stub
  }

  @Override
  public void onPageSelected(int position) {
    if (position == 0) {
      this.rl_act_smart_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_smart_result_pre.setVisibility(View.VISIBLE);
    }
    if (position == totalPageNum - 1) {
      this.rl_act_smart_result_next.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_smart_result_next.setVisibility(View.VISIBLE);
    }

    for (int i = 0; i < this.ll_act_smart_result_bar.getChildCount(); i++) {
      if (position == i) {
        this.ll_act_smart_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_current);
      } else {
        this.ll_act_smart_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_other);
      }
    }
  }

  @Override
  public void onPageScrollStateChanged(int state) {
    // TODO Auto-generated method stub
  }

  @Override
  protected void onPause() {
    bus.unregisterHandler(Constant.ADDR_TOPIC, eventHandler);
    bus.unregisterHandler(Constant.ADDR_VIEW_CONTROL, eventHandlerControl);
    super.onPause();
  }

  @Override
  protected void onResume() {
    bus.registerHandler(Constant.ADDR_TOPIC, eventHandler);
    bus.registerHandler(Constant.ADDR_VIEW_CONTROL, eventHandlerControl);
    super.onResume();
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (!isLocal) {
      return;
    }
    if (isChecked) {
      switch (buttonView.getId()) {
      // 年级的选中事件
        case R.id.rb_act_smart_little:
        case R.id.rb_act_smart_middle:
        case R.id.rb_act_smart_big:
        case R.id.rb_act_smart_pre:
          this.onGradeViewClick(buttonView.getId());
          break;
        // 学期的选中事件
        case R.id.rb_act_smart_top:
        case R.id.rb_act_smart_bottom:
          this.onTermViewClick(buttonView.getId());
          break;
        // 类别的选中事件
        case R.id.rb_act_smart_class_language:
        case R.id.rb_act_smart_class_arithmetic:
        case R.id.rb_act_smart_class_jigsaw:
        case R.id.rb_act_smart_class_thinking:
        case R.id.rb_act_smart_class_readable:
        case R.id.rb_act_smart_class_body:
          this.topicChooser(buttonView.getId());
          this.onMyClassViewClick(buttonView.getId());
          break;

        default:
          break;
      }
    }
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

  /**
   * 处理年级的点击事件
   * 
   * @param i
   */
  private void onGradeViewClick(int id) {
    switch (id) {
      case R.id.rb_act_smart_little:
        this.currentGrade = Constant.GRADE_LITTLE;
        break;
      case R.id.rb_act_smart_middle:
        this.currentGrade = Constant.GRADE_MID;
        break;
      case R.id.rb_act_smart_big:
        this.currentGrade = Constant.GRADE_BIG;
        break;
      case R.id.rb_act_smart_pre:
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
      case R.id.rb_act_smart_class_language:
        this.currenTopic = Constant.DOMIAN_LANGUAGE;
        break;
      case R.id.rb_act_smart_class_arithmetic:
        this.currenTopic = Constant.DOMIAN_ARITHMETIC;
        break;
      case R.id.rb_act_smart_class_jigsaw:
        this.currenTopic = Constant.DOMIAN_JIGSAW;
        break;
      case R.id.rb_act_smart_class_thinking:
        this.currenTopic = Constant.DOMIAN_THINKING;
        break;
      case R.id.rb_act_smart_class_readable:
        this.currenTopic = Constant.DOMIAN_READABLE;
        break;
      case R.id.rb_act_smart_class_body:
        this.currenTopic = Constant.DOMIAN_BODY;
        break;
      default:
        break;
    }
    this.saveHistory(Constant.TOPIC, this.currenTopic);
    this.sendQueryMessage();
  }

  /**
   * 处理学期的点击事件
   * 
   * @param i
   */
  private void onTermViewClick(int id) {
    switch (id) {
      case R.id.rb_act_smart_top:
        this.currentTerm = Constant.TERM_SEMESTER0;
        break;
      case R.id.rb_act_smart_bottom:
        this.currentTerm = Constant.TERM_SEMESTER1;
        break;
      default:
        break;
    }
    this.saveHistory(Constant.TERM, this.currentTerm);
    this.sendQueryMessage();
  }

  /**
   * 把查询完成的的历史记忆绑定到View
   */
  private void bindHistoryDataToView() {
    // 回显年级
    if (Constant.GRADE_LITTLE.equals(this.currentGrade)) {
      this.rb_act_smart_little.setChecked(true);
    } else if (Constant.GRADE_MID.equals(this.currentGrade)) {
      this.rb_act_smart_middle.setChecked(true);
    } else if (Constant.GRADE_BIG.equals(this.currentGrade)) {
      this.rb_act_smart_big.setChecked(true);
    } else if (Constant.GRADE_PRE.equals(this.currentGrade)) {
      this.rb_act_smart_pre.setChecked(true);
    }

    // 回显学期
    if (Constant.TERM_SEMESTER0.equals(this.currentTerm)) {
      this.rb_act_smart_top.setChecked(true);
    } else if (Constant.TERM_SEMESTER1.equals(this.currentTerm)) {
      this.rb_act_smart_bottom.setChecked(true);
    }
    // 回显分类
    if (Constant.DOMIAN_LANGUAGE.equals(this.currenTopic)) {
      this.rb_act_smart_class_language.setChecked(true);
    } else if (Constant.DOMIAN_ARITHMETIC.equals(this.currenTopic)) {
      this.rb_act_smart_class_arithmetic.setChecked(true);
    } else if (Constant.DOMIAN_JIGSAW.equals(this.currenTopic)) {
      this.rb_act_smart_class_jigsaw.setChecked(true);
    } else if (Constant.DOMIAN_THINKING.equals(this.currenTopic)) {
      this.rb_act_smart_class_thinking.setChecked(true);
    } else if (Constant.DOMIAN_READABLE.equals(this.currenTopic)) {
      this.rb_act_smart_class_readable.setChecked(true);
    } else if (Constant.DOMIAN_BODY.equals(this.currenTopic)) {
      this.rb_act_smart_class_body.setChecked(true);
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
   * 构建查询的bus消息
   */
  private void sendQueryMessage() {
    JsonObject msg = Json.createObject();
    msg.set("action", "get");
    JsonObject query = Json.createObject();
    query.set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_SMART);
    query.set(Constant.GRADE, this.currentGrade);
    query.set(Constant.TERM, this.currentTerm);
    query.set(Constant.TOPIC, this.currenTopic);
    msg.set("query", query);
    bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, eventHandler);
  }

}
