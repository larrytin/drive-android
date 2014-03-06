package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.adapter.CommonPageAdapter;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class SmartActivity extends BaseActivity implements OnClickListener, OnPageChangeListener {

  /**
   * 班级的点击事件
   * 
   * @author dpw
   * 
   */
  private class OnGradeClickListener implements OnClickListener {
    @Override
    public void onClick(View v) {
      currentGrade = ((TextView) v).getText().toString();
      saveHistory(Constant.GRADE, currentGrade);
      sendQueryMessage(null);
      echoGrade();
    }
  }

  /**
   * 学期的点击事件
   * 
   * @author dpw
   * 
   */
  private class OnTermClickListener implements OnClickListener {
    @Override
    public void onClick(View v) {
      currentTerm = termMap.get(((TextView) v).getText().toString());
      saveHistory(Constant.TERM, currentTerm);
      sendQueryMessage(null);
      echoTerm();
    }
  }

  private final String[] gradeNames = {
      Constant.LABEL_GRADE_LITTLE, Constant.LABEL_GRADE_MID, Constant.LABEL_GRADE_BIG,};
  private final String[] termNames = {Constant.TERM_SEMESTER0, Constant.TERM_SEMESTER1};
  private static final Map<String, String> termMap = new HashMap<String, String>();
  static {
    termMap.put(Constant.TERM_SEMESTER0, Constant.LABEL_TERM_SEMESTER0);
    termMap.put(Constant.TERM_SEMESTER1, Constant.LABEL_TERM_SEMESTER1);
  }
  // 当前状态
  private String currentGrade = Constant.LABEL_GRADE_LITTLE;
  private String currentTerm = Constant.LABEL_TERM_SEMESTER0;

  // 后退收藏锁屏
  private ImageView iv_act_smart_back = null;
  private ImageView iv_act_smart_coll = null;
  private ImageView iv_act_smart_loc = null;

  // 年级
  private LinearLayout ll_act_smart_grade = null;
  // 学期
  private LinearLayout ll_act_smart_term = null;

  private final int numPerPage = 10;// 查询结果每页显示10条数据
  private final int numPerLine = 5;// 每条显示5个数据

  private ViewPager vp_act_smart_result = null;
  private CommonPageAdapter myPageAdapter = null;
  // 翻页按钮
  private ImageView rl_act_smart_result_pre = null;
  private ImageView rl_act_smart_result_next = null;
  // 页码状态
  private LinearLayout ll_act_smart_result_bar = null;
  private int totalPageNum = 0;

  // 数据集
  private final ArrayList<View> nameViews = new ArrayList<View>();
  private final static String SHAREDNAME = "smartHistory";// 配置文件的名称
  private SharedPreferences sharedPreferences = null;

  private HandlerRegistration postHandler;
  private HandlerRegistration controlHandler;
  private HandlerRegistration refreshHandler;
  private LayoutInflater inflater = null;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_smart_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_smart_coll:
        this.bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set(
            Constant.KEY_REDIRECTTO, "favorite"), null);
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
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_smart);
    this.readHistoryData();
    this.initView();
    Bundle extras = this.getIntent().getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
    this.echoGrade();
    this.echoTerm();
    this.saveHistory(Constant.TERM, currentTerm);
    this.saveHistory(Constant.GRADE, currentGrade);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Bundle extras = intent.getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
    this.echoGrade();
    this.echoTerm();
    this.saveHistory(Constant.TERM, currentTerm);
    this.saveHistory(Constant.GRADE, currentGrade);
  }

  @Override
  protected void onPause() {
    super.onPause();
    postHandler.unregisterHandler();
    controlHandler.unregisterHandler();
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
          Toast.makeText(SmartActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
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
        sendQueryMessage(buildTags(tags));
        echoGrade();
        echoTerm();
        saveHistory(Constant.TERM, currentTerm);
        saveHistory(Constant.GRADE, currentGrade);
      }
    });
    controlHandler = bus.registerHandler(Constant.ADDR_CONTROL, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body.has("page")) {
          JsonObject page = body.getObject("page");
          if (page.has("goTo")) {
            vp_act_smart_result.setCurrentItem((int) page.getNumber("goTo"));
          } else if (page.has("move")) {
            int currentItem = vp_act_smart_result.getCurrentItem();
            vp_act_smart_result.setCurrentItem(currentItem + (int) page.getNumber("move"));
          }
        }
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

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView(JsonArray tags) {
    this.ll_act_smart_result_bar.removeAllViews();
    this.vp_act_smart_result.removeAllViews();
    this.nameViews.clear();
    if (tags == null) {
      return;
    }

    int index = 0;// 下标计数器
    int counter = tags.length();
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
          textView.setTextSize(18);
          textView.setMaxLines(2);
          final String title = tags.getString(index);
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
              msg.set(Constant.KEY_ACTION, "post");
              msg.set(Constant.KEY_TITLE, title);
              JsonArray tags =
                  Json.createArray().push(Constant.DATAREGISTRY_TYPE_EDUCATION).push(currentGrade)
                      .push(currentTerm).push(title);
              msg.set(Constant.KEY_TAGS, tags);
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
      this.ll_act_smart_result_bar.addView(imageView);
    }
    this.myPageAdapter = new CommonPageAdapter(this.nameViews);
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
   * 构建查询TAGS
   * 
   * @param tags
   * @return
   */
  private JsonArray buildTags(JsonArray tags) {
    List<String> grades = Arrays.asList(this.gradeNames);
    // 删除垃圾数据
    for (int i = 0; i < tags.length(); i++) {
      String tag = tags.getString(i);
      boolean isLegalTheme = Constant.LABEL_THEMES.contains(tag);
      boolean isLegalGrade = grades.contains(tag);
      boolean isLegalTerm = termMap.containsValue(tag);
      if (!isLegalTheme && !isLegalGrade && !isLegalTerm) {
        tags.remove(i--);
      }
    }

    // 如果默认的班级、学期、主题不在tags中就加入 如果存在就设置为当前
    for (int i = 0; i < tags.length(); i++) {
      if (grades.contains(tags.getString(i))) {
        this.currentGrade = tags.getString(i);
        break;
      }
    }
    if (tags.indexOf(this.currentGrade) == -1) {
      tags.push(this.currentGrade);
    }

    for (int i = 0; i < tags.length(); i++) {
      if (termMap.containsValue(tags.getString(i))) {
        this.currentTerm = tags.getString(i);
        break;
      }
    }

    if (tags.indexOf(this.currentTerm) == -1) {
      tags.push(this.currentTerm);
    }

    return tags;
  }

  /**
   * 回显班级
   */
  private void echoGrade() {
    for (int i = 0; i < gradeNames.length; i++) {
      TextView child = (TextView) ll_act_smart_grade.getChildAt(i);
      child.setSelected(false);
      if (currentGrade.equals(gradeNames[i])) {
        child.setSelected(true);
      }
    }
  }

  /**
   * 回显学期
   */
  private void echoTerm() {
    for (int i = 0; i < termNames.length; i++) {
      TextView child = (TextView) ll_act_smart_term.getChildAt(i);
      child.setSelected(false);
      if (currentTerm.equals(termMap.get(termNames[i]))) {
        child.setSelected(true);
      }
    }
  }

  /**
   * 初始化View对象 设置点击事件 设置光标事件监听 添加到对应集合
   */
  private void initView() {
    this.inflater = this.getLayoutInflater();
    // 后退 收藏 所屏
    this.iv_act_smart_back = (ImageView) this.findViewById(R.id.iv_act_smart_back);
    this.iv_act_smart_coll = (ImageView) this.findViewById(R.id.iv_act_smart_coll);
    this.iv_act_smart_loc = (ImageView) this.findViewById(R.id.iv_act_smart_loc);
    this.iv_act_smart_back.setOnClickListener(this);
    this.iv_act_smart_coll.setOnClickListener(this);
    this.iv_act_smart_loc.setOnClickListener(this);

    // 初始化年级
    this.ll_act_smart_grade = (LinearLayout) this.findViewById(R.id.ll_act_smart_grade);
    int gradeChildren = this.gradeNames.length;
    for (int i = 0; i < gradeChildren; i++) {
      TextView child = (TextView) this.inflater.inflate(R.layout.common_item_grade_short, null);
      child.setSelected(false);
      if (this.currentGrade.equals(this.gradeNames[i])) {
        child.setSelected(true);
      }
      child.setOnClickListener(new OnGradeClickListener());
      child.setText(this.gradeNames[i]);
      this.ll_act_smart_grade.addView(child);
    }
    // 初始化学期
    this.ll_act_smart_term = (LinearLayout) this.findViewById(R.id.ll_act_smart_term);
    int termChildren = this.termNames.length;
    for (int i = 0; i < termChildren; i++) {
      TextView child = (TextView) this.inflater.inflate(R.layout.common_item_grade_short, null);
      child.setSelected(false);
      if (this.currentTerm.equals(termMap.get(this.termNames[i]))) {
        child.setSelected(true);
      }
      child.setOnClickListener(new OnTermClickListener());
      child.setText(this.termNames[i]);
      this.ll_act_smart_term.addView(child);
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

  /**
   * 处理上下翻页的点击事件
   * 
   * @param id
   */
  private void onResultPrePageClick(int id) {
    JsonObject msg = Json.createObject();
    JsonObject page = Json.createObject();
    msg.set("page", page);
    if (id == R.id.rl_act_smart_result_pre) {
      page.set("move", -1);
    } else if (id == R.id.rl_act_smart_result_next) {
      page.set("move", 1);
    }
    bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, msg, null);
  }

  /**
   * 查询历史数据
   */
  private void readHistoryData() {
    this.sharedPreferences = this.getSharedPreferences(SHAREDNAME, MODE_PRIVATE);
    this.currentGrade = this.sharedPreferences.getString(Constant.GRADE, this.currentGrade);
    this.currentTerm = this.sharedPreferences.getString(Constant.TERM, this.currentTerm);
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
  private void sendQueryMessage(JsonArray tags) {
    JsonObject msg = Json.createObject();
    if (tags != null) {
      msg.set(Constant.KEY_TAGS, tags);
    } else {
      msg.set(Constant.KEY_TAGS, Json.createArray().push(Constant.DATAREGISTRY_TYPE_EDUCATION)
          .push(this.currentGrade).push(this.currentTerm));
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
