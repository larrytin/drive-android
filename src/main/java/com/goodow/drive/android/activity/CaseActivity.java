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
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore.Images.Thumbnails;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CaseActivity extends BaseActivity implements OnFocusChangeListener,
    OnPageChangeListener, OnClickListener {

  /**
   * 异步加载缩略图片
   * 
   * @author dpw
   * 
   */
  class MyAsyncTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView imageView = null;

    public MyAsyncTask(ImageView imageView) {
      this.imageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
      Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(params[0], Thumbnails.MINI_KIND);
      bitmap = ThumbnailUtils.extractThumbnail(bitmap, 72, 50);
      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      this.imageView.setImageBitmap(result);
      super.onPostExecute(result);
    }
  }
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
      sendQueryMessage();
      for (int i = 0; i < gradeNames.length; i++) {
        TextView child = (TextView) ll_act_case_grade.getChildAt(i);
        child.setSelected(false);
        if (currentGrade.equals(gradeNames[i])) {
          child.setSelected(true);
        }
      }
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
      currentTerm = ((TextView) v).getText().toString();
      saveHistory(Constant.TERM, currentTerm);
      sendQueryMessage();
      for (int i = 0; i < termNames.length; i++) {
        TextView child = (TextView) ll_act_case_term.getChildAt(i);
        child.setSelected(false);
        if (currentTerm.equals(termNames[i])) {
          child.setSelected(true);
        }
      }
    }
  }

  /**
   * 主题的点击事件
   * 
   * @author dpw
   * 
   */
  private class OnTopicClickListener implements OnClickListener {
    @Override
    public void onClick(View v) {
      currenTopic = ((TextView) v).getText().toString();
      saveHistory(Constant.TOPIC, currenTopic);
      sendQueryMessage();
      for (int i = 0; i < topicNames.length; i++) {
        TextView child = (TextView) ll_act_case_class.getChildAt(i);
        child.setSelected(false);
        if (currenTopic.equals(topicNames[i])) {
          child.setSelected(true);
        }
      }
    }
  }

  private final String[] gradeNames = {
      Constant.LABEL_GRADE_LITTLE, Constant.LABEL_GRADE_MID, Constant.LABEL_GRADE_BIG,
      Constant.LABEL_GRADE_PRE};
  private final String[] termNames = {Constant.TERM_SEMESTER0, Constant.TERM_SEMESTER1};
  private final String[] topicNames = {
      Constant.DOMIAN_HEALTH, Constant.DOMIAN_LANGUAGE, Constant.DOMIAN_WORLD,
      Constant.DOMIAN_SCIENCE, Constant.DOMIAN_MATH, Constant.DOMIAN_MUSIC, Constant.DOMIAN_ART};
  private static final Map<String, String> termMap = new HashMap<String, String>();
  static {
    termMap.put(Constant.TERM_SEMESTER0, Constant.LABEL_TERM_SEMESTER0);
    termMap.put(Constant.TERM_SEMESTER1, Constant.LABEL_TERM_SEMESTER1);
  }

  // 当前状态
  private String currentGrade = Constant.LABEL_GRADE_LITTLE;
  private String currentTerm = Constant.LABEL_TERM_SEMESTER0;
  private String currenTopic = Constant.DOMIAN_HEALTH;
  // 后退收藏锁屏
  private ImageView iv_act_case_back = null;
  private ImageView iv_act_case_coll = null;

  private ImageView iv_act_case_loc = null;
  // 年级
  private LinearLayout ll_act_case_grade = null;
  // 学期
  private LinearLayout ll_act_case_term = null;
  // 分类
  private LinearLayout ll_act_case_class = null;
  private final int numPerPage = 10;// 查询结果每页显示18条数据

  private final int numPerLine = 5;// 每条显示六个数据
  private ViewPager vp_act_case_result = null;
  private CommonPageAdapter myPageAdapter = null;
  // 翻页按钮
  private ImageView rl_act_case_result_pre = null;
  private ImageView rl_act_case_result_next = null;

  // 页码状态
  private LinearLayout ll_act_case_result_bar = null;
  private int totalPageNum = 0;

  private final ArrayList<View> nameViews = new ArrayList<View>();
  private final static String SHAREDNAME = "caseHistory";// 配置文件的名称
  private SharedPreferences sharedPreferences = null;

  private HandlerRegistration postHandler;
  private HandlerRegistration controlHandler;
  private LayoutInflater inflater = null;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_case_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_case_coll:
        bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, Json.createObject().set("action", "post").set(
            Constant.QUERIES, Json.createObject().set(Constant.TYPE, "收藏")), null);
        break;
      case R.id.iv_act_case_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        break;

      // 查询结果翻页
      case R.id.rl_act_case_result_pre:
      case R.id.rl_act_case_result_next:
        this.onResultPrePageClick(v.getId());
        break;

      default:
        break;
    }
  }

  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    // TODO Auto-generated method stub

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
      this.rl_act_case_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_case_result_pre.setVisibility(View.VISIBLE);
    }
    if (position == totalPageNum - 1) {
      this.rl_act_case_result_next.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_case_result_next.setVisibility(View.VISIBLE);
    }

    for (int i = 0; i < this.ll_act_case_result_bar.getChildCount(); i++) {
      if (position == i) {
        this.ll_act_case_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_current);
      } else {
        this.ll_act_case_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_other);
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_case);
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
            && !Constant.DATAREGISTRY_TYPE_CASE.equals(queries.getString(Constant.TYPE))) {
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
            vp_act_case_result.setCurrentItem((int) page.getNumber("goTo"));
          } else if (page.has("move")) {
            int currentItem = vp_act_case_result.getCurrentItem();
            vp_act_case_result.setCurrentItem(currentItem + (int) page.getNumber("move"));
          }
        }
      }
    });
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView(JsonArray attachments) {
    this.ll_act_case_result_bar.removeAllViews();
    this.vp_act_case_result.removeAllViews();
    this.nameViews.clear();
    if (attachments == null) {
      return;
    }

    int index = 0;// 下标计数器
    int counter = attachments.length();
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
          final View itemView = this.buildItemView(index, attachments.getObject(index));
          LinearLayout.LayoutParams params =
              new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
          params.setMargins(10, 10, 10, 10);
          itemView.setLayoutParams(params);
          itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, Json.createObject().set("path",
                  itemView.getTag().toString()), null);
            }
          });
          innerContainer.addView(itemView);
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
      this.ll_act_case_result_bar.addView(imageView);
    }
    this.myPageAdapter = new CommonPageAdapter(this.nameViews);
    this.vp_act_case_result.setAdapter(this.myPageAdapter);
    if (this.totalPageNum > 1) {
      this.rl_act_case_result_pre.setVisibility(View.INVISIBLE);
      this.rl_act_case_result_next.setVisibility(View.VISIBLE);
    } else {
      this.rl_act_case_result_pre.setVisibility(View.INVISIBLE);
      this.rl_act_case_result_next.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * 构建子View
   * 
   * @param index
   * @return
   */
  private View buildItemView(int index, JsonObject attachment) {
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    LinearLayout itemLayout = new LinearLayout(this);
    itemLayout.setLayoutParams(params);
    itemLayout.setClickable(true);
    itemLayout.setOrientation(LinearLayout.VERTICAL);

    RelativeLayout.LayoutParams itemImageViewParams =
        new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    itemImageViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
    itemImageViewParams.setMargins(0, 30, 0, 0);
    RelativeLayout imageLayout = new RelativeLayout(this);

    ImageView thumbnail = new ImageView(this);
    thumbnail.setLayoutParams(itemImageViewParams);
    new MyAsyncTask(thumbnail).execute(attachment.getString(Constant.KEY_URL));
    imageLayout.addView(thumbnail);

    RelativeLayout.LayoutParams itemImageViewParams2 = new RelativeLayout.LayoutParams(100, 100);
    itemImageViewParams2.addRule(RelativeLayout.CENTER_IN_PARENT);
    ImageView itemImageView = new ImageView(this);
    itemImageView.setImageResource(R.drawable.case_item_bg);
    itemImageView.setLayoutParams(itemImageViewParams2);
    imageLayout.addView(itemImageView);
    itemLayout.addView(imageLayout);

    TextView textView = new TextView(this);
    textView.setWidth(150);
    textView.setTextSize(18);
    textView.setMaxLines(2);
    textView.setGravity(Gravity.CENTER_HORIZONTAL);
    String title = attachment.getString(Constant.KEY_NAME);
    itemLayout.setTag(attachment.getString(Constant.KEY_URL));
    if (title.matches("^\\d{4}.*")) {
      textView.setText(title.substring(4, title.length()));
    } else {
      textView.setText(title);
    }
    itemLayout.addView(textView);

    return itemLayout;
  }

  /**
   * 初始化View对象 设置点击事件 设置光标事件监听 添加到对应集合
   */
  private void initView() {
    this.inflater = this.getLayoutInflater();
    // 后退 收藏 所屏
    this.iv_act_case_back = (ImageView) this.findViewById(R.id.iv_act_case_back);
    this.iv_act_case_coll = (ImageView) this.findViewById(R.id.iv_act_case_coll);
    this.iv_act_case_loc = (ImageView) this.findViewById(R.id.iv_act_case_loc);
    this.iv_act_case_back.setOnClickListener(this);
    this.iv_act_case_coll.setOnClickListener(this);
    this.iv_act_case_loc.setOnClickListener(this);

    // 初始化年级
    this.ll_act_case_grade = (LinearLayout) this.findViewById(R.id.ll_act_case_grade);
    int gradeChildren = this.gradeNames.length;
    for (int i = 0; i < gradeChildren; i++) {
      int layoutId = R.layout.common_item_grade_short;
      if (this.gradeNames[3].equals(this.gradeNames[i])) {
        layoutId = R.layout.common_item_grade_long;
      }
      TextView child = (TextView) this.inflater.inflate(layoutId, null);
      child.setSelected(false);
      if (this.currentGrade.equals(this.gradeNames[i])) {
        child.setSelected(true);
      }
      child.setOnClickListener(new OnGradeClickListener());
      child.setText(this.gradeNames[i]);
      this.ll_act_case_grade.addView(child);
    }
    // 初始化学期
    this.ll_act_case_term = (LinearLayout) this.findViewById(R.id.ll_act_case_term);
    int termChildren = this.termNames.length;
    for (int i = 0; i < termChildren; i++) {
      TextView child = (TextView) this.inflater.inflate(R.layout.common_item_grade_short, null);
      child.setSelected(false);
      if (this.currentTerm.equals(this.termNames[i])) {
        child.setSelected(true);
      }
      child.setOnClickListener(new OnTermClickListener());
      child.setText(this.termNames[i]);
      this.ll_act_case_term.addView(child);
    }
    // 初始化分类
    this.ll_act_case_class = (LinearLayout) this.findViewById(R.id.ll_act_case_class);
    int topicChildren = this.topicNames.length;
    for (int i = 0; i < topicChildren; i++) {
      int layoutId = R.layout.common_item_class_short;
      if (Constant.DOMIAN_MUSIC.equals(this.topicNames[i])
          || Constant.DOMIAN_ART.equals(this.topicNames[i])) {
        layoutId = R.layout.common_item_class_long;
      }
      LinearLayout.LayoutParams params =
          new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      params.setMargins(5, 5, 5, 0);
      TextView child = (TextView) this.inflater.inflate(layoutId, null);
      child.setLayoutParams(params);
      child.setSelected(false);
      if (this.currenTopic.equals(this.topicNames[i])) {
        child.setSelected(true);
      }
      child.setOnClickListener(new OnTopicClickListener());
      child.setText(this.topicNames[i]);
      this.ll_act_case_class.addView(child);
    }

    // 初始化查询结果视图
    this.vp_act_case_result = (ViewPager) this.findViewById(R.id.vp_act_case_result);
    this.vp_act_case_result.setOnPageChangeListener(this);

    // 初始化查询结果控制
    this.rl_act_case_result_pre = (ImageView) this.findViewById(R.id.rl_act_case_result_pre);
    this.rl_act_case_result_next = (ImageView) this.findViewById(R.id.rl_act_case_result_next);
    this.rl_act_case_result_pre.setOnClickListener(this);
    this.rl_act_case_result_next.setOnClickListener(this);

    // 初始化结果数量视图
    this.ll_act_case_result_bar = (LinearLayout) this.findViewById(R.id.ll_act_case_result_bar);

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
    if (id == R.id.rl_act_case_result_pre) {
      page.set("move", -1);
    } else if (id == R.id.rl_act_case_result_next) {
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
            Json.createArray().push(Constant.DATAREGISTRY_TYPE_CASE).push(this.currentGrade).push(
                termMap.get(this.currentTerm)).push(this.currenTopic));
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_ATTACHMENT_SEARCH, msg,
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            JsonArray attachments = body.getArray(Constant.KEY_ATTACHMENTS);
            bindDataToView(attachments);
          }
        });
  }

}
