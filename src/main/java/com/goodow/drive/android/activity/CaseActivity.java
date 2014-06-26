package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.toolutils.FileTools;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.Registration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_case)
public class CaseActivity extends BaseActivity implements OnClickListener {

  /**
   * 班级的点击事件
   * 
   * @author dpw
   * 
   */
  private class OnGradeClickListener implements OnClickListener {
    @Override
    public void onClick(View v) {
      currentPageNum = 0;
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
      currentPageNum = 0;
      currentTerm = termMap.get(((TextView) v).getText().toString());
      saveHistory(Constant.TERM, currentTerm);
      sendQueryMessage(null);
      echoTerm();
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
      currentPageNum = 0;
      currenTopic = ((TextView) v).getText().toString();
      saveHistory(Constant.TOPIC, currenTopic);
      sendQueryMessage(null);
      echoTopic();
    }
  }

  private class ResultAdapter extends BaseAdapter {
    private JsonArray attachments = null;

    @Override
    public int getCount() {
      if (attachments != null) {
        return attachments.length();
      }
      return 0;
    }

    @Override
    public Object getItem(int position) {
      if (attachments != null) {
        return attachments.getObject(position);
      }
      return null;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = View.inflate(CaseActivity.this, R.layout.result_common, null);
        ResultAdapterHolder holder = new ResultAdapterHolder();
        holder.iv_common_result = (ImageView) convertView.findViewById(R.id.iv_common_result);
        holder.tv_common_result = (TextView) convertView.findViewById(R.id.tv_common_result);
        convertView.setTag(holder);
      }
      final JsonObject attachment = attachments.getObject(position);
      ResultAdapterHolder holder = (ResultAdapterHolder) convertView.getTag();
      FileTools.setImageThumbnalilUrl(holder.iv_common_result, attachment
          .getString(Constant.KEY_URL), attachment.getString(Constant.KEY_THUMBNAIL));

      String title = attachment.getString(Constant.KEY_NAME);
      holder.tv_common_result.setText(title);
      convertView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          bus.sendLocal(Constant.ADDR_PLAYER, Json.createObject().set("path",
              attachment.getString(Constant.KEY_URL)), null);
        }
      });
      return convertView;
    }

    public void reset(JsonArray attachments) {
      this.attachments = attachments;
    }
  }
  private class ResultAdapterHolder {
    private ImageView iv_common_result;
    private TextView tv_common_result;
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
  @InjectView(R.id.iv_act_case_back)
  private ImageView iv_act_case_back;
  @InjectView(R.id.iv_act_case_coll)
  private ImageView iv_act_case_coll;
  @InjectView(R.id.iv_act_case_loc)
  private ImageView iv_act_case_loc;
  // 年级
  @InjectView(R.id.ll_act_case_grade)
  private LinearLayout ll_act_case_grade;
  // 学期
  @InjectView(R.id.ll_act_case_term)
  private LinearLayout ll_act_case_term;

  // 分类
  @InjectView(R.id.ll_act_case_class)
  private LinearLayout ll_act_case_class;
  private final int numPerPage = 10;// 查询结果每页显示10条数据
  @InjectView(R.id.vp_act_case_result)
  private GridView vp_act_case_result;

  // 翻页按钮
  @InjectView(R.id.rl_act_case_result_pre)
  private ImageView rl_act_case_result_pre;
  @InjectView(R.id.rl_act_case_result_next)
  private ImageView rl_act_case_result_next;

  // 页码状态
  @InjectView(R.id.ll_act_case_result_bar)
  private LinearLayout ll_act_case_result_bar;
  // 查询进度
  @InjectView(R.id.pb_act_result_progress)
  private ProgressBar pb_act_result_progress;

  private final static String SHAREDNAME = "caseHistory";// 配置文件的名称

  private SharedPreferences sharedPreferences;
  @InjectExtra(value = "msg", optional = true)
  private JsonObject msg;
  private Registration postHandler;;
  private Registration controlHandler;

  private Registration refreshHandler;

  private LayoutInflater inflater = null;

  private ResultAdapter resultAdapter;// 结果gridview适配器
  private int currentPageNum;// 当前结果页数
  private int totalAttachmentNum;// 结果总数

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_case_back:
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_case_coll:
        this.bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
            "favorite"), null);
        break;
      case R.id.iv_act_case_loc:
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
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
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.readHistoryData();
    this.initView();
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
    this.echoGrade();
    this.echoTerm();
    this.echoTopic();
    this.saveHistory(Constant.TOPIC, currenTopic);
    this.saveHistory(Constant.TERM, currentTerm);
    this.saveHistory(Constant.GRADE, currentGrade);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
    this.echoGrade();
    this.echoTerm();
    this.echoTopic();
    this.saveHistory(Constant.TOPIC, currenTopic);
    this.saveHistory(Constant.TERM, currentTerm);
    this.saveHistory(Constant.GRADE, currentGrade);
  }

  @Override
  protected void onPause() {
    super.onPause();
    postHandler.unregister();
    controlHandler.unregister();
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
          Toast.makeText(CaseActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
          return;
        }
        for (int i = 0; i < tags.length(); i++) {
          if (Constant.LABEL_THEMES.contains(tags.getString(i))) {
            return;
          }
        }
        if (tags.indexOf(Constant.DATAREGISTRY_TYPE_CASE) == -1) {
          tags.push(Constant.DATAREGISTRY_TYPE_CASE);
        }
        sendQueryMessage(buildTags(tags));
        echoGrade();
        echoTerm();
        echoTopic();
        saveHistory(Constant.TOPIC, currenTopic);
        saveHistory(Constant.TERM, currentTerm);
        saveHistory(Constant.GRADE, currentGrade);
      }
    });
    controlHandler =
        bus.subscribeLocal(Constant.ADDR_CONTROL, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            if (body.has("page")) {
              JsonObject page = body.getObject("page");
              if (page.has("goTo")) {
                currentPageNum = (int) page.getNumber("goTo");
              } else if (page.has("move")) {
                currentPageNum = currentPageNum + (int) page.getNumber("move");
              }
              sendQueryMessage(null);
            }
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

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView(JsonArray attachments) {
    onPageSelected(currentPageNum);
    pb_act_result_progress.setVisibility(View.INVISIBLE);
    if (this.currentPageNum == 0) {
      // 第一页：向前的不显示
      this.rl_act_case_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_case_result_pre.setVisibility(View.VISIBLE);
    }
    if (this.currentPageNum < (this.totalAttachmentNum % this.numPerPage == 0
        ? this.totalAttachmentNum / this.numPerPage - 1 : this.totalAttachmentNum / this.numPerPage)) {
      // 小于总页数：向后的显示
      this.rl_act_case_result_next.setVisibility(View.VISIBLE);
    } else {
      this.rl_act_case_result_next.setVisibility(View.INVISIBLE);
    }
    resultAdapter.reset(attachments);
    resultAdapter.notifyDataSetChanged();
  }

  /**
   * 构建查询TAGS
   * 
   * @param tags
   * @return
   */
  private JsonArray buildTags(JsonArray tags) {
    List<String> topics = Arrays.asList(this.topicNames);
    List<String> grades = Arrays.asList(this.gradeNames);
    // 删除垃圾数据
    for (int i = 0; i < tags.length(); i++) {
      String tag = tags.getString(i);
      boolean isLegalTheme = Constant.LABEL_THEMES.contains(tag);
      boolean isLegalGrade = grades.contains(tag);
      boolean isLegalTerm = termMap.containsValue(tag);
      boolean isLegalTopic = topics.contains(tag);
      if (!isLegalTheme && !isLegalGrade && !isLegalTerm && !isLegalTopic) {
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
   * 回显班级
   */
  private void echoGrade() {
    for (int i = 0; i < gradeNames.length; i++) {
      TextView child = (TextView) ll_act_case_grade.getChildAt(i);
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
      TextView child = (TextView) ll_act_case_term.getChildAt(i);
      child.setSelected(false);
      if (currentTerm.equals(termMap.get(termNames[i]))) {
        child.setSelected(true);
      }
    }
  }

  /**
   * 回显主题
   */
  private void echoTopic() {
    for (int i = 0; i < topicNames.length; i++) {
      TextView child = (TextView) ll_act_case_class.getChildAt(i);
      child.setSelected(false);
      if (currenTopic.equals(topicNames[i])) {
        child.setSelected(true);
      }
    }
  }

  /**
   * 初始化View对象 设置点击事件 设置光标事件监听 添加到对应集合
   */
  private void initView() {
    this.inflater = this.getLayoutInflater();
    this.iv_act_case_back.setOnClickListener(this);
    this.iv_act_case_coll.setOnClickListener(this);
    this.iv_act_case_loc.setOnClickListener(this);
    int gradeChildren = this.gradeNames.length;

    LayoutParams longWidthParams =
        new LayoutParams(getResources().getDimensionPixelSize(R.dimen.commen_grade_width_long),
            getResources().getDimensionPixelSize(R.dimen.common_grade_height));
    LayoutParams commonWidthParams =
        new LayoutParams(getResources().getDimensionPixelSize(R.dimen.common_grade_width),
            getResources().getDimensionPixelSize(R.dimen.common_grade_height));
    for (int i = 0; i < gradeChildren; i++) {
      int layoutId = R.layout.common_item_grade_short;
      TextView child = (TextView) this.inflater.inflate(layoutId, null);
      if (this.gradeNames[3].equals(this.gradeNames[i])) {
        child.setLayoutParams(longWidthParams);
      } else {
        child.setLayoutParams(commonWidthParams);
      }
      child.setSelected(false);
      if (this.currentGrade.equals(this.gradeNames[i])) {
        child.setSelected(true);
      }
      child.setOnClickListener(new OnGradeClickListener());
      child.setText(this.gradeNames[i]);
      this.ll_act_case_grade.addView(child);
    }
    int termChildren = this.termNames.length;
    for (int i = 0; i < termChildren; i++) {
      TextView child = (TextView) this.inflater.inflate(R.layout.common_item_grade_short, null);
      child.setLayoutParams(commonWidthParams);
      child.setSelected(false);
      if (this.currentTerm.equals(this.termNames[i])) {
        child.setSelected(true);
      }
      child.setOnClickListener(new OnTermClickListener());
      child.setText(this.termNames[i]);
      this.ll_act_case_term.addView(child);
    }
    int topicChildren = this.topicNames.length;
    longWidthParams =
        new LayoutParams(getResources().getDimensionPixelSize(R.dimen.common_class_width_long),
            getResources().getDimensionPixelSize(R.dimen.common_class_height));
    longWidthParams.setMargins(getResources()
        .getDimensionPixelSize(R.dimen.common_class_marginLeft), getResources()
        .getDimensionPixelSize(R.dimen.common_class_marginTop), getResources()
        .getDimensionPixelSize(R.dimen.common_class_marginRight), 0);
    commonWidthParams =
        new LayoutParams(getResources().getDimensionPixelSize(R.dimen.common_class_width),
            getResources().getDimensionPixelSize(R.dimen.common_class_height));
    commonWidthParams.setMargins(getResources().getDimensionPixelSize(
        R.dimen.common_class_marginLeft), getResources().getDimensionPixelSize(
        R.dimen.common_class_marginTop), getResources().getDimensionPixelSize(
        R.dimen.common_class_marginRight), 0);
    for (int i = 0; i < topicChildren; i++) {
      int layoutId = R.layout.common_item_class_short;
      TextView child;
      if (Constant.DOMIAN_MUSIC.equals(this.topicNames[i])
          || Constant.DOMIAN_ART.equals(this.topicNames[i])) {
        layoutId = R.layout.common_item_class_long;
        child = (TextView) this.inflater.inflate(layoutId, null);
        child.setLayoutParams(longWidthParams);
      } else {
        child = (TextView) this.inflater.inflate(layoutId, null);
        child.setLayoutParams(commonWidthParams);
      }
      child.setTextSize(getResources().getDimensionPixelSize(R.dimen.common_class_textSize));
      child.setPadding(0, getResources().getDimensionPixelSize(R.dimen.common_class_paddingTop), 0,
          0);
      child.setSelected(false);
      if (this.currenTopic.equals(this.topicNames[i])) {
        child.setSelected(true);
      }
      child.setOnClickListener(new OnTopicClickListener());
      child.setText(this.topicNames[i]);
      this.ll_act_case_class.addView(child);
    }
    resultAdapter = new ResultAdapter();
    this.vp_act_case_result.setAdapter(resultAdapter);
    this.rl_act_case_result_pre.setOnClickListener(this);
    this.rl_act_case_result_next.setOnClickListener(this);
  }

  private void onPageSelected(int position) {
    int totalPageNum =
        (totalAttachmentNum / numPerPage) + (totalAttachmentNum % numPerPage > 0 ? 1 : 0);
    ll_act_case_result_bar.removeAllViews();
    for (int i = 0; i < totalPageNum; i++) {
      ImageView imageView = new ImageView(CaseActivity.this);
      LayoutParams layoutParams =
          new LinearLayout.LayoutParams(getResources().getDimensionPixelOffset(
              R.dimen.common_result_dot_width), getResources().getDimensionPixelOffset(
              R.dimen.common_result_dot_height));
      imageView.setLayoutParams(layoutParams);
      if (position == i) {
        imageView.setBackgroundResource(R.drawable.common_result_dot_current);
      } else {
        imageView.setBackgroundResource(R.drawable.common_result_dot_other);
      }
      ll_act_case_result_bar.addView(imageView);
    }
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
    bus.sendLocal(Constant.ADDR_CONTROL, msg, null);
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
  private void sendQueryMessage(JsonArray tags) {
    JsonObject msg = Json.createObject();
    msg.set(Constant.KEY_FROM, numPerPage * currentPageNum);
    msg.set(Constant.KEY_SIZE, numPerPage);
    if (tags != null) {
      msg.set(Constant.KEY_TAGS, tags);
    } else {
      msg.set(Constant.KEY_TAGS, Json.createArray().push(Constant.DATAREGISTRY_TYPE_CASE).push(
          this.currentGrade).push(this.currentTerm).push(this.currenTopic));
    }
    pb_act_result_progress.setVisibility(View.VISIBLE);
    bus.sendLocal(Constant.ADDR_TAG_ATTACHMENT_SEARCH, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        JsonArray attachments = body.getArray(Constant.KEY_ATTACHMENTS);
        totalAttachmentNum = (int) body.getNumber(Constant.KEY_COUNT);
        bindDataToView(attachments);
      }
    });
  }

}
