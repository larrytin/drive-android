package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.BusProvider;
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
import android.view.View.OnFocusChangeListener;
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

public class HarmonyActivity extends BaseActivity implements OnCheckedChangeListener,
    OnFocusChangeListener, OnPageChangeListener, OnClickListener {
  /**
   * 定义年纪
   * 
   * @author dpw
   * 
   */
  private enum Grade {
    LITTLE("小班"), MIDDLE("中班"), BIG("大班"), PREPARE("学前");
    public static Grade checkContain(String value) {
      Grade[] values = Grade.values();
      for (Grade v : values) {
        if (v.value.equals(value)) {
          return v;
        }
      }
      return null;
    }

    private final String value;

    Grade(String value) {
      this.value = value;
    }
  }
  /**
   * 定义分类
   * 
   * @author dpw
   * 
   */
  private enum MyClass {
    HEALTH("健康"), LANGUAGE("语言"), WORLD("社会"), SCIENCE("科学"), MATH("数学"), MUSIC("艺术（音乐）"), ART(
        "艺术（美术）");
    public static MyClass checkContain(String value) {
      MyClass[] values = MyClass.values();
      for (MyClass v : values) {
        if (v.value.equals(value)) {
          return v;
        }
      }
      return null;
    }

    private final String value;

    MyClass(String value) {
      this.value = value;
    }
  }

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
  /**
   * 定义学期
   * 
   * @author dpw
   * 
   */
  private enum Term {
    SEMESTER1("上"), SEMESTER2("下");
    public static Term checkContain(String value) {
      Term[] values = Term.values();
      for (Term v : values) {
        if (v.value.equals(value)) {
          return v;
        }
      }
      return null;
    }

    private final String value;

    Term(String value) {
      this.value = value;
    }
  }

  // 当前状态
  private String currentGrade = Grade.LITTLE.value;
  private String currentTerm = Term.SEMESTER1.value;
  private String currentMyClass = MyClass.HEALTH.value;

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
  private MyPageAdapter myPageAdapter = null;
  // 翻页按钮
  private ImageView rl_act_harmony_result_pre = null;
  private ImageView rl_act_harmony_result_next = null;
  // 页码状态
  private LinearLayout ll_act_harmony_result_bar = null;

  // 数据集
  private final ArrayList<String> names = new ArrayList<String>();
  private final ArrayList<View> nameViews = new ArrayList<View>();

  private final static String SHAREDNAME = "harmonyHistory";// 配置文件的名称
  public final static String SHAREDNAME_GRADE = "grade";// 年级的KEY
  public final static String SHAREDNAME_TERM = "term";// 学期的KEY
  public final static String SHAREDNAME_CLASS = "domain";// 分类的KEY
  private SharedPreferences sharedPreferences = null;

  private final Bus bus = BusProvider.get();
  public static final String ADDR = BusProvider.SID + "category";
  // 测试命令
  // bus.publish("ding.drive.category",{"action":"post","domain":{"grade":"小班","term":"下","domain":"健康"},"subjects":[{"name":"找朋友"},{"name":"小鸭子"},{"name":"小鸭子0"},{"name":"小鸭子2"}]});
  private final MessageHandler<JsonObject> eventHandler = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      String action = body.getString("action");
      if (action != null && !"post".equalsIgnoreCase(action)) {
        return;
      }

      // 解析查询条件，如果是主动发起查询则domain为null
      JsonObject domain = body.getObject("domain");
      if (domain != null) {
        String tempGrade = domain.getString(SHAREDNAME_GRADE);
        String tempTerm = domain.getString(SHAREDNAME_TERM);
        String tempClass = domain.getString(SHAREDNAME_CLASS);
        if (tempGrade != null && Grade.checkContain(tempGrade) != null) {
          currentGrade = Grade.checkContain(tempGrade).value;
          saveHistory(SHAREDNAME_GRADE, currentGrade);
        }
        if (tempTerm != null && Term.checkContain(tempTerm) != null) {
          currentTerm = Term.checkContain(tempTerm).value;
          saveHistory(SHAREDNAME_TERM, currentTerm);
        }
        if (tempClass != null && MyClass.checkContain(tempClass) != null) {
          currentMyClass = MyClass.checkContain(tempClass).value;
          saveHistory(SHAREDNAME_CLASS, currentMyClass);
        }
      }

      JsonArray asArray = body.getArray("subjects");
      names.clear();
      if (asArray != null) {
        int length = asArray.length();
        for (int i = 0; i < length; i++) {
          names.add(asArray.getObject(i).getString("name"));
        }
        bindDataToView();
      }
      bindHistoryDataToView();
    }
  };

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
        bus.send(Bus.LOCAL + BaseActivity.CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_harmony_coll:
        bus.send(Bus.LOCAL + ViewRegistry.ADDR_TOPIC, Json.createObject().set("action", "post")
            .set("query", Json.createObject().set("type", "收藏")), null);
      case R.id.iv_act_harmony_loc:
        bus.send(Bus.LOCAL + BaseActivity.CONTROL, Json.createObject().set("brightness", 0), null);
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
    this.currentGrade = this.sharedPreferences.getString(SHAREDNAME_GRADE, Grade.LITTLE.value);
    this.currentTerm = this.sharedPreferences.getString(SHAREDNAME_TERM, Term.SEMESTER1.value);
    this.currentMyClass = this.sharedPreferences.getString(SHAREDNAME_CLASS, MyClass.HEALTH.value);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_harmony);
    this.initView();
    this.readHistoryData();
    this.bindHistoryDataToView();
  }

  @Override
  protected void onPause() {
    bus.unregisterHandler(ADDR, eventHandler);
    super.onPause();
  }

  @Override
  protected void onResume() {
    bus.registerHandler(ADDR, eventHandler);
    this.sendQueryMessage();
    super.onResume();
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView() {
    if (this.names == null) {
      return;
    }
    this.ll_act_harmony_result_bar.removeAllViews();
    this.vp_act_harmony_result.removeAllViews();
    this.nameViews.clear();

    int index = 0;// 下标计数器
    int counter = names.size();
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
          TextView textView = new TextView(this);
          LinearLayout.LayoutParams params =
              new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
          params.setMargins(7, 12, 7, 12);
          textView.setLayoutParams(params);
          textView.setGravity(Gravity.CENTER_HORIZONTAL);
          textView.setPadding(0, 10, 0, 0);
          textView.setText(this.names.get(index));
          textView.setBackgroundResource(R.drawable.harm_result_item_bg);
          textView.setClickable(true);
          textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              Toast.makeText(HarmonyActivity.this, "go:" + ((TextView) v).getText().toString(),
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
      this.ll_act_harmony_result_bar.addView(imageView);
    }
    this.myPageAdapter = new MyPageAdapter(this.nameViews);
    this.vp_act_harmony_result.setAdapter(this.myPageAdapter);
  }

  /**
   * 把查询完成的的历史记忆绑定到View
   */
  private void bindHistoryDataToView() {
    // 回显年级
    if (Grade.LITTLE.value.equals(this.currentGrade)) {
      this.rb_act_harmony_little.setChecked(true);
    } else if (Grade.MIDDLE.value.equals(this.currentGrade)) {
      this.rb_act_harmony_middle.setChecked(true);
    } else if (Grade.BIG.value.equals(this.currentGrade)) {
      this.rb_act_harmony_big.setChecked(true);
    } else if (Grade.PREPARE.value.equals(this.currentGrade)) {
      this.rb_act_harmony_pre.setChecked(true);
    }

    // 回显学期
    if (Term.SEMESTER1.value.equals(this.currentTerm)) {
      this.rb_act_harmony_top.setChecked(true);
    } else if (Term.SEMESTER2.value.equals(this.currentTerm)) {
      this.rb_act_harmony_bottom.setChecked(true);
    }
    // 回显分类
    if (MyClass.HEALTH.value.equals(this.currentMyClass)) {
      this.rb_act_harmony_class_health.setChecked(true);
    } else if (MyClass.LANGUAGE.value.equals(this.currentMyClass)) {
      this.rb_act_harmony_class_language.setChecked(true);
    } else if (MyClass.WORLD.value.equals(this.currentMyClass)) {
      this.rb_act_harmony_class_world.setChecked(true);
    } else if (MyClass.SCIENCE.value.equals(this.currentMyClass)) {
      this.rb_act_harmony_class_scinece.setChecked(true);
    } else if (MyClass.MATH.value.equals(this.currentMyClass)) {
      this.rb_act_harmony_class_math.setChecked(true);
    } else if (MyClass.MUSIC.value.equals(this.currentMyClass)) {
      this.rb_act_harmony_class_music.setChecked(true);
    } else if (MyClass.ART.value.equals(this.currentMyClass)) {
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
        this.currentGrade = Grade.LITTLE.value;
        break;
      case R.id.rb_act_harmony_middle:
        this.currentGrade = Grade.MIDDLE.value;
        break;
      case R.id.rb_act_harmony_big:
        this.currentGrade = Grade.BIG.value;
        break;
      case R.id.rb_act_harmony_pre:
        this.currentGrade = Grade.PREPARE.value;
        break;

      default:
        break;
    }
    this.saveHistory(SHAREDNAME_GRADE, this.currentGrade);
    this.sendQueryMessage();
  }

  /**
   * 处理年级的点击事件
   * 
   * @param id
   * @param hasFocus
   */
  private void onGradeViewFocus(int id, boolean hasFocus) {
  }

  /**
   * 处理类别的点击事件
   * 
   * @param i
   */
  private void onMyClassViewClick(int id) {
    switch (id) {
      case R.id.rb_act_harmony_class_health:
        this.currentMyClass = MyClass.HEALTH.value;
        break;
      case R.id.rb_act_harmony_class_language:
        this.currentMyClass = MyClass.LANGUAGE.value;
        break;
      case R.id.rb_act_harmony_class_world:
        this.currentMyClass = MyClass.WORLD.value;
        break;
      case R.id.rb_act_harmony_class_scinece:
        this.currentMyClass = MyClass.SCIENCE.value;
        break;
      case R.id.rb_act_harmony_class_math:
        this.currentMyClass = MyClass.MATH.value;
        break;
      case R.id.rb_act_harmony_class_music:
        this.currentMyClass = MyClass.MUSIC.value;
        break;
      case R.id.rb_act_harmony_class_art:
        this.currentMyClass = MyClass.ART.value;
        break;
      default:
        break;
    }
    this.saveHistory(SHAREDNAME_CLASS, this.currentMyClass);
    this.sendQueryMessage();
  }

  /**
   * 处理类别的点击事件
   * 
   * @param id
   * @param hasFocus
   */
  private void onMyClassViewFocus(int id, boolean hasFocus) {

  }

  /**
   * 处理上下翻页的点击事件
   * 
   * @param id
   */
  private void onResultPrePageClick(int id) {
    if (id == R.id.rl_act_harmony_result_pre) {
      this.vp_act_harmony_result.arrowScroll(View.FOCUS_LEFT);
    } else if (id == R.id.rl_act_harmony_result_next) {
      this.vp_act_harmony_result.arrowScroll(View.FOCUS_RIGHT);
    }
  }

  /**
   * 处理学期的点击事件
   * 
   * @param i
   */
  private void onTermViewClick(int id) {
    switch (id) {
      case R.id.rb_act_harmony_top:
        this.currentTerm = Term.SEMESTER1.value;
        break;
      case R.id.rb_act_harmony_bottom:
        this.currentTerm = Term.SEMESTER2.value;
        break;
      default:
        break;
    }
    this.saveHistory(SHAREDNAME_TERM, this.currentTerm);
    this.sendQueryMessage();
  }

  /**
   * 处理学期的点击事件
   * 
   * @param id
   * @param hasFocus
   */
  private void onTermViewFocus(int id, boolean hasFocus) {

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
    JsonObject category = Json.createObject();
    category.set(SHAREDNAME_GRADE, this.currentGrade);
    category.set(SHAREDNAME_TERM, this.currentTerm);
    category.set(SHAREDNAME_CLASS, this.currentMyClass);
    msg.set("category", category);
    bus.send(Bus.LOCAL + ADDR, msg, eventHandler);
  }

}
