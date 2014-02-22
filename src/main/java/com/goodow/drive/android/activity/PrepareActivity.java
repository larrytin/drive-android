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
import java.util.HashMap;
import java.util.Map;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class PrepareActivity extends BaseActivity implements OnPageChangeListener, OnClickListener {

  private final String[] termNames = {Constant.TERM_SEMESTER0, Constant.TERM_SEMESTER1};
  private static final Map<String, String> termMap = new HashMap<String, String>();
  static {
    termMap.put(Constant.TERM_SEMESTER0, Constant.LABEL_TERM_SEMESTER0);
    termMap.put(Constant.TERM_SEMESTER1, Constant.LABEL_TERM_SEMESTER1);
  }
  // 当前状态
  private String currentTerm = Constant.LABEL_TERM_SEMESTER0;
  private String currenTopic = Constant.DOMIAN_LANGUAGE;

  // 后退收藏锁屏
  private ImageView iv_act_prepare_back = null;
  private ImageView iv_act_prepare_coll = null;
  private ImageView iv_act_prepare_loc = null;

  // 学期
  private LinearLayout ll_act_prepare_term = null;
  private TextView ftv_act_prepare_top = null;
  private TextView ftv_act_prepare_bottom = null;

  // 分类
  private TextView[] topicRadioButtons = null;
  private TextView ftv_act_prepare_class_language = null;
  private TextView ftv_act_prepare_class_thinking = null;
  private TextView ftv_act_prepare_class_read_write = null;
  private TextView ftv_act_prepare_class_quality = null;

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

  private final ArrayList<View> nameViews = new ArrayList<View>();

  private final static String SHAREDNAME = "prepareHistory";// 配置文件的名称
  private SharedPreferences sharedPreferences = null;

  private HandlerRegistration postHandler;
  private HandlerRegistration controlHandler;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_prepare_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_prepare_coll:
        bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, Json.createObject().set("action", "post").set(
            Constant.QUERIES,
            Json.createObject().set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_FAVOURITE)), null);
        break;
      case R.id.iv_act_prepare_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        Toast.makeText(this, "黑屏", Toast.LENGTH_LONG).show();
        break;
      // 学期的选中事件
      case R.id.ftv_act_prepare_top:
      case R.id.ftv_act_prepare_bottom:
        this.onTermViewClick(v.getId());
        break;
      // 类别的选中事件
      case R.id.ftv_act_prepare_class_language:
      case R.id.ftv_act_prepare_class_thinking:
      case R.id.ftv_act_prepare_class_read_write:
      case R.id.ftv_act_prepare_class_quality:
        this.topicChooser(v.getId());
        this.onMyClassViewClick(v.getId());
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
    this.readHistoryData();
    this.initView();
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
            && !Constant.DATAREGISTRY_TYPE_PREPARE.equals(queries.getString(Constant.TYPE))) {
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
            vp_act_prepare_result.setCurrentItem((int) page.getNumber("goTo"));
          } else if (page.has("move")) {
            int currentItem = vp_act_prepare_result.getCurrentItem();
            vp_act_prepare_result.setCurrentItem(currentItem + (int) page.getNumber("move"));
          }
        }
      }
    });
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView(JsonArray tags) {
    this.ll_act_prepare_result_bar.removeAllViews();
    this.vp_act_prepare_result.removeAllViews();
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
          params.setMargins(12, 40, 12, 40);
          textView.setLayoutParams(params);
          textView.setGravity(Gravity.CENTER_HORIZONTAL);
          textView.setPadding(15, 10, 18, 0);
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
                  Json.createArray().push(Constant.DATAREGISTRY_TYPE_PREPARE).push(currentTerm)
                      .push(currenTopic).push(title);
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
    this.ll_act_prepare_term = (LinearLayout) this.findViewById(R.id.ll_act_prepare_term);
    this.ftv_act_prepare_top = (TextView) this.findViewById(R.id.ftv_act_prepare_top);
    this.ftv_act_prepare_bottom = (TextView) this.findViewById(R.id.ftv_act_prepare_bottom);

    int termChildren = this.ll_act_prepare_term.getChildCount();
    for (int i = 0; i < termChildren; i++) {
      TextView child = (TextView) this.ll_act_prepare_term.getChildAt(i);
      child.setSelected(false);
      if (this.currentTerm.equals(this.termNames[i])) {
        child.setSelected(true);
      }
      child.setOnClickListener(this);
    }

    // 初始化分类
    this.ftv_act_prepare_class_language =
        (TextView) this.findViewById(R.id.ftv_act_prepare_class_language);
    this.ftv_act_prepare_class_thinking =
        (TextView) this.findViewById(R.id.ftv_act_prepare_class_thinking);
    this.ftv_act_prepare_class_read_write =
        (TextView) this.findViewById(R.id.ftv_act_prepare_class_read_write);
    this.ftv_act_prepare_class_quality =
        (TextView) this.findViewById(R.id.ftv_act_prepare_class_quality);

    this.topicRadioButtons =
        new TextView[] {
            this.ftv_act_prepare_class_language, this.ftv_act_prepare_class_thinking,
            this.ftv_act_prepare_class_read_write, this.ftv_act_prepare_class_quality};

    int classChildren = this.topicRadioButtons.length;
    for (int i = 0; i < classChildren; i++) {
      this.topicRadioButtons[i].setOnClickListener(this);
      if (this.currenTopic.equals(this.topicRadioButtons[i].getText().toString())) {
        this.topicRadioButtons[i].setSelected(true);
      }
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

  /**
   * 处理类别的点击事件
   * 
   * @param i
   */
  private void onMyClassViewClick(int id) {
    switch (id) {
      case R.id.ftv_act_prepare_class_language:
        this.currenTopic = Constant.DOMIAN_LANGUAGE;
        break;
      case R.id.ftv_act_prepare_class_thinking:
        this.currenTopic = Constant.DOMIAN_THINKING;
        break;
      case R.id.ftv_act_prepare_class_read_write:
        this.currenTopic = Constant.DOMIAN_READ_WRITE;
        break;
      case R.id.ftv_act_prepare_class_quality:
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
    JsonObject page = Json.createObject();
    msg.set("page", page);
    if (id == R.id.rl_act_prepare_result_pre) {
      page.set("move", -1);
    } else if (id == R.id.rl_act_prepare_result_next) {
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
      case R.id.ftv_act_prepare_top:
        this.currentTerm = Constant.TERM_SEMESTER0;
        this.ftv_act_prepare_top.setSelected(true);
        this.ftv_act_prepare_bottom.setSelected(false);
        break;
      case R.id.ftv_act_prepare_bottom:
        this.currentTerm = Constant.TERM_SEMESTER1;
        break;
      default:
        break;
    }
    int len = this.ll_act_prepare_term.getChildCount();
    for (int i = 0; i < len; i++) {
      if (this.ll_act_prepare_term.getChildAt(i).getId() == id) {
        this.ll_act_prepare_term.getChildAt(i).setSelected(true);
      } else {
        this.ll_act_prepare_term.getChildAt(i).setSelected(false);
      }
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
    JsonObject msg =
        Json.createObject().set(
            Constant.KEY_TAGS,
            Json.createArray().push(Constant.DATAREGISTRY_TYPE_PREPARE).push(this.currentTerm)
                .push(termMap.get(this.currenTopic)));
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_CHILDREN, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonArray tags = (JsonArray) message.body();
        bindDataToView(tags);
      }
    });
  }

  private void topicChooser(int id) {
    int len = this.topicRadioButtons.length;
    for (int i = 0; i < len; i++) {
      TextView child = this.topicRadioButtons[i];
      if (child.getId() == id) {
        child.setSelected(true);
      } else {
        child.setSelected(false);
      }
    }
  }
}
