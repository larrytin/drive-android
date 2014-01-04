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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EbookActivity extends BaseActivity implements OnCheckedChangeListener,
    OnPageChangeListener, OnClickListener {
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
  private String currenTopic = Constant.DOMIAN_HEALTH;

  // 后退收藏锁屏
  private ImageView iv_act_ebook_back = null;
  private ImageView iv_act_ebook_coll = null;
  private ImageView iv_act_ebook_loc = null;

  // 分类
  private RadioGroup rg_act_ebook_class = null;
  private RadioButton rb_act_ebook_class_health = null;
  private RadioButton rb_act_ebook_class_language = null;
  private RadioButton rb_act_ebook_class_world = null;
  private RadioButton rb_act_ebook_class_scinece = null;
  private RadioButton rb_act_ebook_class_math = null;

  private final int numPerPage = 8;// 查询结果每页显示8条数据
  private final int numPerLine = 4;// 每条显示四个数据

  private ViewPager vp_act_ebook_result = null;
  private MyPageAdapter myPageAdapter = null;
  // 翻页按钮
  private ImageView rl_act_ebook_result_pre = null;
  private ImageView rl_act_ebook_result_next = null;
  // 页码状态
  private LinearLayout ll_act_ebook_result_bar = null;

  // 数据集
  private JsonArray activities = null;
  private final ArrayList<View> nameViews = new ArrayList<View>();

  private final static String SHAREDNAME = "ebookHistory";// 配置文件的名称
  private SharedPreferences sharedPreferences = null;

  private final MessageHandler<JsonObject> eventHandlerControl = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body.has("previous") && body.getBoolean("previous")) {
        vp_act_ebook_result.arrowScroll(View.FOCUS_LEFT);
      } else if (body.has("next") && body.getBoolean("next")) {
        vp_act_ebook_result.arrowScroll(View.FOCUS_RIGHT);
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
      if (query != null && query.has("type") && !"电子书".equals(query.getString("type"))) {
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

  boolean isLocal = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_ebook);
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

  /**
   * 把查询完成的的历史记忆绑定到View
   */
  private void bindHistoryDataToView() {
    // 回显分类
    if (Constant.DOMIAN_HEALTH.equals(this.currenTopic)) {
      this.rb_act_ebook_class_health.setChecked(true);
    } else if (Constant.DOMIAN_LANGUAGE.equals(this.currenTopic)) {
      this.rb_act_ebook_class_language.setChecked(true);
    } else if (Constant.DOMIAN_WORLD.equals(this.currenTopic)) {
      this.rb_act_ebook_class_world.setChecked(true);
    } else if (Constant.DOMIAN_SCIENCE.equals(this.currenTopic)) {
      this.rb_act_ebook_class_scinece.setChecked(true);
    } else if (Constant.DOMIAN_MATH.equals(this.currenTopic)) {
      this.rb_act_ebook_class_math.setChecked(true);
    }
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView() {
    if (this.activities == null) {
      return;
    }
    this.ll_act_ebook_result_bar.removeAllViews();
    this.vp_act_ebook_result.removeAllViews();
    this.nameViews.clear();

    int index = 0;// 下标计数器
    int counter = activities.length();
    int times =
        (counter % this.numPerPage == 0) ? (counter / numPerPage) : (counter / this.numPerPage + 1);
    // 页码数量
    for (int i = 0; i < times; i++) {
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
          View view = buildItemView(index);
          LinearLayout.LayoutParams params =
              new LinearLayout.LayoutParams(120, LayoutParams.WRAP_CONTENT);
          params.setMargins(22, 5, 22, 18);
          view.setLayoutParams(params);
          view.setClickable(true);
          view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              Toast.makeText(EbookActivity.this,
                  "go:" + ((TextView) ((LinearLayout) v).getChildAt(1)).getText().toString(),
                  Toast.LENGTH_SHORT).show();
            }
          });
          innerContainer.addView(view);
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
      this.ll_act_ebook_result_bar.addView(imageView);
    }
    this.myPageAdapter = new MyPageAdapter(this.nameViews);
    this.vp_act_ebook_result.setAdapter(this.myPageAdapter);
  }

  /**
   * 构建子View
   * 
   * @param index
   * @return
   */
  private View buildItemView(int index) {
    JsonObject activity = this.activities.getObject(index);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    LinearLayout itemLayout = new LinearLayout(this);
    itemLayout.setLayoutParams(params);
    itemLayout.setClickable(true);
    itemLayout.setOrientation(LinearLayout.VERTICAL);

    RelativeLayout.LayoutParams itemImageViewParams2 = new RelativeLayout.LayoutParams(120, 100);
    itemImageViewParams2.addRule(RelativeLayout.CENTER_HORIZONTAL);
    ImageView itemImageView = new ImageView(this);
    itemImageView.setImageResource(R.drawable.ebook_flash);
    itemImageView.setLayoutParams(itemImageViewParams2);
    itemLayout.addView(itemImageView);

    TextView textView = new TextView(this);
    textView.setWidth(150);
    textView.setGravity(Gravity.CENTER_HORIZONTAL);
    textView.setText(activity.getString(Constant.TITLE));
    itemLayout.addView(textView);

    return itemLayout;
  }

  /**
   * 解析条件
   * 
   * @param query
   */
  public void readQuery(JsonObject query) {
    if (query != null) {
      String tempClass = query.getString(Constant.TOPIC);
      if (tempClass != null && isRightfulTopic(tempClass)) {
        currenTopic = tempClass;
        saveHistory(Constant.TOPIC, currenTopic);
      } else if (query.has(Constant.TOPIC) && !isRightfulTopic(tempClass)) {
        Toast.makeText(this, "无效的类别数值", Toast.LENGTH_SHORT).show();
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
    if (Constant.DOMIAN_HEALTH.equals(topic) || Constant.DOMIAN_LANGUAGE.equals(topic)
        || Constant.DOMIAN_WORLD.equals(topic) || Constant.DOMIAN_SCIENCE.equals(topic)
        || Constant.DOMIAN_MATH.equals(topic) || Constant.DOMIAN_MUSIC.equals(topic)
        || Constant.DOMIAN_ART.equals(topic)) {
      return true;
    }
    return false;
  }

  /**
   * 初始化View对象 设置点击事件 设置光标事件监听 添加到对应集合
   */
  private void initView() {
    // 后退 收藏 所屏
    this.iv_act_ebook_back = (ImageView) this.findViewById(R.id.iv_act_ebook_back);
    this.iv_act_ebook_coll = (ImageView) this.findViewById(R.id.iv_act_ebook_coll);
    this.iv_act_ebook_loc = (ImageView) this.findViewById(R.id.iv_act_ebook_loc);
    this.iv_act_ebook_back.setOnClickListener(this);
    this.iv_act_ebook_coll.setOnClickListener(this);
    this.iv_act_ebook_loc.setOnClickListener(this);

    // 初始化分类
    this.rg_act_ebook_class = (RadioGroup) this.findViewById(R.id.rg_act_ebook_class);
    this.rb_act_ebook_class_health =
        (RadioButton) this.findViewById(R.id.rb_act_ebook_class_health);
    this.rb_act_ebook_class_language =
        (RadioButton) this.findViewById(R.id.rb_act_ebook_class_language);
    this.rb_act_ebook_class_world = (RadioButton) this.findViewById(R.id.rb_act_ebook_class_world);
    this.rb_act_ebook_class_scinece =
        (RadioButton) this.findViewById(R.id.rb_act_ebook_class_scinece);
    this.rb_act_ebook_class_math = (RadioButton) this.findViewById(R.id.rb_act_ebook_class_math);
    int classChildren = this.rg_act_ebook_class.getChildCount();
    for (int i = 0; i < classChildren; i++) {
      RadioButton child = (RadioButton) this.rg_act_ebook_class.getChildAt(i);
      child.setOnCheckedChangeListener(this);
    }

    // 初始化查询结果视图
    this.vp_act_ebook_result = (ViewPager) this.findViewById(R.id.vp_act_ebook_result);
    this.vp_act_ebook_result.setOnPageChangeListener(this);

    // 初始化查询结果控制
    this.rl_act_ebook_result_pre = (ImageView) this.findViewById(R.id.rl_act_ebook_result_pre);
    this.rl_act_ebook_result_next = (ImageView) this.findViewById(R.id.rl_act_ebook_result_next);
    this.rl_act_ebook_result_pre.setOnClickListener(this);
    this.rl_act_ebook_result_next.setOnClickListener(this);

    // 初始化结果数量视图
    this.ll_act_ebook_result_bar = (LinearLayout) this.findViewById(R.id.ll_act_ebook_result_bar);

  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_ebook_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_ebook_coll:
        bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, Json.createObject().set("action", "post").set(
            "query", Json.createObject().set("type", "收藏")), null);
        break;
      case R.id.iv_act_ebook_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        Toast.makeText(this, "黑屏", Toast.LENGTH_LONG).show();
        break;

      // 查询结果翻页
      case R.id.rl_act_ebook_result_pre:
      case R.id.rl_act_ebook_result_next:
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
  public void onPageSelected(int position) {
    for (int i = 0; i < this.ll_act_ebook_result_bar.getChildCount(); i++) {
      if (position == i) {
        this.ll_act_ebook_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_current);
      } else {
        this.ll_act_ebook_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_other);
      }
    }
  }

  @Override
  public void onPageScrollStateChanged(int state) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (!isLocal) {
      return;
    }
    if (isChecked) {
      switch (buttonView.getId()) {
      // 类别的选中事件
        case R.id.rb_act_ebook_class_health:
        case R.id.rb_act_ebook_class_language:
        case R.id.rb_act_ebook_class_world:
        case R.id.rb_act_ebook_class_scinece:
        case R.id.rb_act_ebook_class_math:
          this.onMyClassViewClick(buttonView.getId());
          break;

        default:
          break;
      }
    }
  }

  /**
   * 处理类别的点击事件
   * 
   * @param i
   */
  private void onMyClassViewClick(int id) {
    switch (id) {
      case R.id.rb_act_ebook_class_health:
        this.currenTopic = Constant.DOMIAN_HEALTH;
        break;
      case R.id.rb_act_ebook_class_language:
        this.currenTopic = Constant.DOMIAN_LANGUAGE;
        break;
      case R.id.rb_act_ebook_class_world:
        this.currenTopic = Constant.DOMIAN_WORLD;
        break;
      case R.id.rb_act_ebook_class_scinece:
        this.currenTopic = Constant.DOMIAN_SCIENCE;
        break;
      case R.id.rb_act_ebook_class_math:
        this.currenTopic = Constant.DOMIAN_MATH;
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
    if (id == R.id.rl_act_ebook_result_pre) {
      msg.set("previous", true);
    } else if (id == R.id.rl_act_ebook_result_next) {
      msg.set("next", true);
    }
    bus.send(Bus.LOCAL + Constant.ADDR_VIEW_CONTROL, msg, eventHandlerControl);
  }

  /**
   * 查询历史数据
   */
  public void readHistoryData() {
    this.sharedPreferences = this.getSharedPreferences(SHAREDNAME, MODE_PRIVATE);
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
    JsonObject msg = Json.createObject();
    msg.set("action", "get");
    JsonObject query = Json.createObject();
    query.set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_EBOOK);
    query.set(Constant.TOPIC, this.currenTopic);
    msg.set("query", query);
    bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, eventHandler);
  }

}
