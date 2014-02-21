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
  private String currentGrade = Constant.LABEL_GRADE_LITTLE;
  private String currentTerm = Constant.LABEL_TERM_SEMESTER0;
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
  private final ArrayList<View> nameViews = new ArrayList<View>();

  private final static String SHAREDNAME = "harmonyHistory";// 配置文件的名称
  private SharedPreferences sharedPreferences = null;

  private final boolean isLocal = true;
  private HandlerRegistration postHandler;
  private HandlerRegistration controlHandler;

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
            Constant.QUERIES,
            Json.createObject().set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_FAVOURITE)), null);
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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_harmony);
    this.initView();
    this.readHistoryData();
    this.sendQueryMessage();
  }

  @Override
  protected void onPause() {
    super.onPause();
    postHandler.unregisterHandler();
    controlHandler.unregisterHandler();
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
            && !Constant.DATAREGISTRY_TYPE_HARMONY.equals(queries.getString(Constant.TYPE))) {
          return;
        }
      }
    });
    controlHandler = bus.registerHandler(Constant.ADDR_CONTROL, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body.has("page")) {
          JsonObject page = body.getObject("page");
          if (page.has("goTo")) {
            vp_act_harmony_result.setCurrentItem((int) page.getNumber("goTo"));
          } else if (page.has("move")) {
            int currentItem = vp_act_harmony_result.getCurrentItem();
            vp_act_harmony_result.setCurrentItem(currentItem + (int) page.getNumber("move"));
          }
        }
      }
    });
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView(JsonArray tags) {
    this.ll_act_harmony_result_bar.removeAllViews();
    this.vp_act_harmony_result.removeAllViews();
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
              new LinearLayout.LayoutParams(130, LayoutParams.WRAP_CONTENT);
          params.setMargins(7, 12, 7, 12);
          textView.setLayoutParams(params);
          textView.setTextSize(18);
          textView.setMaxLines(2);
          textView.setGravity(Gravity.CENTER_HORIZONTAL);
          textView.setPadding(15, 10, 18, 0);
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
                  Json.createArray().push(Constant.DATAREGISTRY_TYPE_HARMONY).push(currentGrade)
                      .push(currentTerm).push(currenTopic).push(title);
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
    if (Constant.LABEL_GRADE_LITTLE.equals(this.currentGrade)) {
      this.rb_act_harmony_little.setChecked(true);
    } else if (Constant.LABEL_GRADE_MID.equals(this.currentGrade)) {
      this.rb_act_harmony_middle.setChecked(true);
    } else if (Constant.LABEL_GRADE_BIG.equals(this.currentGrade)) {
      this.rb_act_harmony_big.setChecked(true);
    } else if (Constant.LABEL_GRADE_PRE.equals(this.currentGrade)) {
      this.rb_act_harmony_pre.setChecked(true);
    }

    // 回显学期
    if (Constant.LABEL_TERM_SEMESTER0.equals(this.currentTerm)) {
      this.rb_act_harmony_top.setChecked(true);
    } else if (Constant.LABEL_TERM_SEMESTER1.equals(this.currentTerm)) {
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
        this.currentGrade = Constant.LABEL_GRADE_LITTLE;
        break;
      case R.id.rb_act_harmony_middle:
        this.currentGrade = Constant.LABEL_GRADE_MID;
        break;
      case R.id.rb_act_harmony_big:
        this.currentGrade = Constant.LABEL_GRADE_BIG;
        break;
      case R.id.rb_act_harmony_pre:
        this.currentGrade = Constant.LABEL_GRADE_PRE;
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
    JsonObject page = Json.createObject();
    msg.set("page", page);
    if (id == R.id.rl_act_harmony_result_pre) {
      page.set("move", -1);
    } else if (id == R.id.rl_act_harmony_result_next) {
      page.set("move", 1);
    }
    bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, msg, null);
  }

  /**
   * 处理学期的点击事件
   * 
   * @param i
   */
  private void onTermViewClick(int id) {
    switch (id) {
      case R.id.rb_act_harmony_top:
        this.currentTerm = Constant.LABEL_TERM_SEMESTER0;
        break;
      case R.id.rb_act_harmony_bottom:
        this.currentTerm = Constant.LABEL_TERM_SEMESTER1;
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
    this.currentGrade = this.sharedPreferences.getString(Constant.GRADE, this.currentGrade);
    this.currentTerm = this.sharedPreferences.getString(Constant.TERM, this.currentTerm);
    this.currenTopic = this.sharedPreferences.getString(Constant.TOPIC, this.currenTopic);
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
    JsonObject msg =
        Json.createObject().set(
            Constant.KEY_TAGS,
            Json.createArray().push(Constant.DATAREGISTRY_TYPE_HARMONY).push(this.currentGrade)
                .push(this.currentTerm).push(this.currenTopic));
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_CHILDREN, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonArray tags = (JsonArray) message.body();
        bindDataToView(tags);
        bindHistoryDataToView();
      }
    });
  }

}
