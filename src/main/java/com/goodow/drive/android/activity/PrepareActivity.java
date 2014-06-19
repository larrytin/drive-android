package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.data.DBDataProvider;
import com.goodow.drive.android.view.PrepareResultView;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.Registration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
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
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_prepare)
public class PrepareActivity extends BaseActivity implements OnClickListener {

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
      PrepareResultView view;
      if (convertView != null) {
        view = (PrepareResultView) convertView;
      } else {
        view = new PrepareResultView(PrepareActivity.this);
      }
      if (parent.getChildCount() == position) {
        view.setLeftEyeEnable(false);
        view.setRightEyeEnable(false);
        JsonObject tag = attachments.getObject(position);
        String title = tag.getString(Constant.KEY_TAG);
        JsonArray array = tag.getArray(Constant.KEY_ATTACHMENTS);
        if (title.matches("^\\d{4}.*")) {
          view.setText(title.substring(4, title.length()));
        } else {
          view.setText(title);
        }
        for (int i = 0; i < array.length(); i++) {
          JsonObject object = array.getObject(i);
          final String filePath = object.getString(Constant.KEY_URL);
          if (filePath.endsWith(".pdf")) {
            view.setLeftEyeEnable(true);
            view.setOnLeftEyeClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                bus.sendLocal(Constant.ADDR_PLAYER, Json.createObject().set("path", filePath).set(
                    "play", 1), null);
              }
            });
          }
          if (filePath.endsWith(".swf")) {
            view.setRightEyeEnable(true);
            view.setOnRightEyeClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                bus.sendLocal(Constant.ADDR_PLAYER, Json.createObject().set("path", filePath).set(
                    "play", 1), null);
              }
            });
          }
        }
      } else {
      }
      return view;
    }

    public void reset(JsonArray attachments) {
      this.attachments = attachments;
    }
  }

  private final String[] termNames = {Constant.TERM_SEMESTER0, Constant.TERM_SEMESTER1};

  private final String[] topicNames = {
      Constant.DOMIAN_LANGUAGE, Constant.DOMIAN_THINKING, Constant.DOMIAN_READ_WRITE,
      Constant.DOMIAN_QUALITY};
  private static final Map<String, String> termMap = new HashMap<String, String>();
  static {
    termMap.put(Constant.TERM_SEMESTER0, Constant.LABEL_TERM_SEMESTER0);
    termMap.put(Constant.TERM_SEMESTER1, Constant.LABEL_TERM_SEMESTER1);
  }
  // 当前状态
  private String currentTerm = Constant.LABEL_TERM_SEMESTER0;
  private String currenTopic = Constant.DOMIAN_LANGUAGE;

  // 后退收藏锁屏
  @InjectView(R.id.iv_act_prepare_back)
  private ImageView iv_act_prepare_back;
  @InjectView(R.id.iv_act_prepare_coll)
  private ImageView iv_act_prepare_coll;
  @InjectView(R.id.iv_act_prepare_loc)
  private ImageView iv_act_prepare_loc;

  // 学期
  @InjectView(R.id.ll_act_prepare_term)
  private LinearLayout ll_act_prepare_term;
  @InjectView(R.id.ftv_act_prepare_top)
  private TextView ftv_act_prepare_top;
  @InjectView(R.id.ftv_act_prepare_bottom)
  private TextView ftv_act_prepare_bottom;

  // 分类
  private ImageView[] topicRadioButtons = null;
  @InjectView(R.id.ftv_act_prepare_class_language)
  private ImageView ftv_act_prepare_class_language;
  @InjectView(R.id.ftv_act_prepare_class_thinking)
  private ImageView ftv_act_prepare_class_thinking;
  @InjectView(R.id.ftv_act_prepare_class_read_write)
  private ImageView ftv_act_prepare_class_read_write;
  @InjectView(R.id.ftv_act_prepare_class_quality)
  private ImageView ftv_act_prepare_class_quality;

  private final int numPerPage = 8;// 查询结果每页显示8条数据
  @InjectView(R.id.vp_act_prepare_result)
  private GridView vp_act_prepare_result;

  // 翻页按钮
  @InjectView(R.id.rl_act_prepare_result_pre)
  private ImageView rl_act_prepare_result_pre;
  @InjectView(R.id.rl_act_prepare_result_next)
  private ImageView rl_act_prepare_result_next;
  // 页码状态
  @InjectView(R.id.ll_act_prepare_result_bar)
  private LinearLayout ll_act_prepare_result_bar;
  // 查询进度
  @InjectView(R.id.pb_act_result_progress)
  private ProgressBar pb_act_result_progress;

  private final static String SHAREDNAME = "prepareHistory";// 配置文件的名称
  private SharedPreferences sharedPreferences;

  private Registration postHandler;
  private Registration controlHandler;
  private Registration refreshHandler;

  private ResultAdapter resultAdapter;// 结果gridview适配器
  private int currentPageNum;// 当前结果页数
  private int totalAttachmentNum;// 结果总数

  private ExecutorService newCachedThreadPool;

  private TreeMap<String, JsonArray> treeMap;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_prepare_back:
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_prepare_coll:
        this.bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
            "favorite"), null);
        break;
      case R.id.iv_act_prepare_loc:
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        break;
      // 学期的选中事件
      case R.id.ftv_act_prepare_top:
      case R.id.ftv_act_prepare_bottom:
        currentPageNum = 0;
        this.onTermViewClick(v.getId());
        break;
      // 类别的选中事件
      case R.id.ftv_act_prepare_class_language:
      case R.id.ftv_act_prepare_class_thinking:
      case R.id.ftv_act_prepare_class_read_write:
      case R.id.ftv_act_prepare_class_quality:
        currentPageNum = 0;
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

  public void onPageSelected(int position) {
    int totalPageNum =
        (totalAttachmentNum / numPerPage) + (totalAttachmentNum % numPerPage > 0 ? 1 : 0);
    ll_act_prepare_result_bar.removeAllViews();
    for (int i = 0; i < totalPageNum; i++) {
      ImageView imageView = new ImageView(PrepareActivity.this);
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
      ll_act_prepare_result_bar.addView(imageView);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.readHistoryData();
    this.initView();
    // newCachedThreadPool = Executors.newCachedThreadPool();
    Bundle extras = this.getIntent().getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
    this.echoTerm();
    this.echoTopic();
    this.saveHistory(Constant.TOPIC, currenTopic);
    this.saveHistory(Constant.TERM, currentTerm);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Bundle extras = intent.getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
    this.echoTerm();
    this.echoTopic();
    this.saveHistory(Constant.TOPIC, currenTopic);
    this.saveHistory(Constant.TERM, currentTerm);
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
          Toast.makeText(PrepareActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
          return;
        }
        for (int i = 0; i < tags.length(); i++) {
          if (Constant.LABEL_THEMES.contains(tags.getString(i))) {
            return;
          }
        }
        if (tags.indexOf(Constant.DATAREGISTRY_TYPE_PREPARE) == -1) {
          tags.push(Constant.DATAREGISTRY_TYPE_PREPARE);
        }
        sendQueryMessage(buildTags(tags));
        echoTerm();
        echoTopic();
        saveHistory(Constant.TOPIC, currenTopic);
        saveHistory(Constant.TERM, currentTerm);
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
              bindDataToView();
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
   * 异步查询文件用
   * 
   * @param view
   * @param title
   */
  private void asyncQueryFiles(final PrepareResultView view, final String title) {
    JsonObject msg = Json.createObject();
    JsonArray tags =
        Json.createArray().push(Constant.DATAREGISTRY_TYPE_PREPARE).push(currentTerm).push(
            currenTopic).push(title);
    msg.set(Constant.KEY_TAGS, tags);
    new AsyncTask<JsonObject, Void, JsonObject>() {
      @Override
      protected JsonObject doInBackground(JsonObject... messages) {
        // long start = System.currentTimeMillis();
        JsonObject object = DBDataProvider.queryFilesByTagName(PrepareActivity.this, messages[0]);
        // long end = System.currentTimeMillis() - start;
        // System.out.println("end:" + end);
        return object;
      }

      @Override
      protected void onPostExecute(JsonObject result) {
        JsonArray attachments = result.getArray(Constant.KEY_ATTACHMENTS);
        for (int i = 0; i < attachments.length(); i++) {
          final String filePath = attachments.getObject(i).getString(Constant.KEY_URL);
          if (filePath.endsWith(".pdf")) {
            view.setLeftEyeEnable(true);
            view.setOnLeftEyeClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                bus.sendLocal(Constant.ADDR_PLAYER, Json.createObject().set("path", filePath).set(
                    "play", 1), null);
              }
            });
          }
          if (filePath.endsWith(".swf")) {
            view.setRightEyeEnable(true);
            view.setOnRightEyeClickListener(new OnClickListener() {
              @Override
              public void onClick(View v) {
                bus.sendLocal(Constant.ADDR_PLAYER, Json.createObject().set("path", filePath).set(
                    "play", 1), null);
              }
            });
          }
        }
      };
    }.executeOnExecutor(newCachedThreadPool, msg);
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView() {
    onPageSelected(currentPageNum);
    pb_act_result_progress.setVisibility(View.INVISIBLE);
    if (this.currentPageNum == 0) {
      // 第一页：向前的不显示
      this.rl_act_prepare_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_prepare_result_pre.setVisibility(View.VISIBLE);
    }
    if (this.currentPageNum < (this.totalAttachmentNum % this.numPerPage == 0
        ? this.totalAttachmentNum / this.numPerPage - 1 : this.totalAttachmentNum / this.numPerPage)) {
      // 小于总页数：向后的显示
      this.rl_act_prepare_result_next.setVisibility(View.VISIBLE);
    } else {
      this.rl_act_prepare_result_next.setVisibility(View.INVISIBLE);
    }
    JsonArray jsonArray = Json.createArray();
    Set<Entry<String, JsonArray>> entrySet = treeMap.entrySet();
    Iterator<Entry<String, JsonArray>> iterator = entrySet.iterator();
    for (int i = 0; i < numPerPage * currentPageNum + numPerPage; ++i) {
      if (iterator.hasNext()) {
        Entry<String, JsonArray> next = iterator.next();
        if (i >= numPerPage * currentPageNum) {
          JsonObject object = Json.createObject();
          object.set(Constant.KEY_TAG, next.getKey());
          object.set(Constant.KEY_ATTACHMENTS, next.getValue());
          jsonArray.push(object);
        }
      }
    }
    resultAdapter.reset(jsonArray);
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
    // 删除垃圾数据
    for (int i = 0; i < tags.length(); i++) {
      String tag = tags.getString(i);
      boolean isLegalTheme = Constant.LABEL_THEMES.contains(tag);
      boolean isLegalTerm = termMap.containsValue(tag);
      boolean isLegalTopic = topics.contains(tag);
      if (!isLegalTheme && !isLegalTerm && !isLegalTopic) {
        tags.remove(i--);
      }
    }

    // 如果默认的班级、学期、主题不在tags中就加入 如果存在就设置为当前
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

  private void combineTags(JsonArray tags) {
    // JsonObject temp =
    // Json.createObject().set(Constant.KEY_TAG, "★不要等一等").set(Constant.KEY_URL,
    // "/mnt/sdcard/goodow/drive/入学准备/语言/9005丹丹和多多/丹丹和多多-dh.pdf");
    // tags.push(temp);
    // JsonObject temp2 =
    // Json.createObject().set(Constant.KEY_TAG, "★不要等一等").set(Constant.KEY_URL,
    // "/mnt/sdcard/goodow/drive/入学准备/语言/9005丹丹和多多/丹丹和多多-dh.swf");
    // tags.push(temp2);
    treeMap = new TreeMap<String, JsonArray>(new Comparator<String>() {
      @Override
      public int compare(String str1, String str2) {
        if (str1.equals(str2)) {
          return 0;
        }
        if (str1.startsWith("★")) {
          return -1;
        } else {
          return 1;
        }
      }
    });
    for (int i = 0; i < tags.length(); i++) {
      JsonObject object = tags.getObject(i);
      String key = object.getString(Constant.KEY_TAG);
      if (treeMap.containsKey(key)) {
        treeMap.get(key).push(object);
      } else {
        treeMap.put(key, Json.createArray().push(object));
      }
    }
  }

  /**
   * 回显学期
   */
  private void echoTerm() {
    for (int i = 0; i < termNames.length; i++) {
      TextView child = (TextView) ll_act_prepare_term.getChildAt(i);
      child.setTextSize(24);
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
      ImageView child = this.topicRadioButtons[i];
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
    // 后退 收藏 所屏
    this.iv_act_prepare_back.setOnClickListener(this);
    this.iv_act_prepare_coll.setOnClickListener(this);
    this.iv_act_prepare_loc.setOnClickListener(this);

    int termChildren = this.ll_act_prepare_term.getChildCount();
    for (int i = 0; i < termChildren; i++) {
      TextView child = (TextView) this.ll_act_prepare_term.getChildAt(i);
      child.setSelected(false);
      if (this.currentTerm.equals(termMap.get(this.termNames[i]))) {
        child.setSelected(true);
      }
      child.setOnClickListener(this);
    }

    // 初始化分类
    this.topicRadioButtons =
        new ImageView[] {
            this.ftv_act_prepare_class_language, this.ftv_act_prepare_class_thinking,
            this.ftv_act_prepare_class_read_write, this.ftv_act_prepare_class_quality};

    int classChildren = this.topicRadioButtons.length;
    for (int i = 0; i < classChildren; i++) {
      this.topicRadioButtons[i].setOnClickListener(this);
      if (this.currenTopic.equals(this.topicRadioButtons[i].getTag())) {
        this.topicRadioButtons[i].setSelected(true);
      }
    }

    // 初始化查询结果视图
    resultAdapter = new ResultAdapter();
    vp_act_prepare_result.setAdapter(resultAdapter);

    // 初始化查询结果控制
    this.rl_act_prepare_result_pre.setOnClickListener(this);
    this.rl_act_prepare_result_next.setOnClickListener(this);

  }

  /**
   * 处理类别的点击事件
   * 
   * @param id
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
    this.sendQueryMessage(null);
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
    bus.sendLocal(Constant.ADDR_CONTROL, msg, null);
  }

  /**
   * 处理学期的点击事件
   * 
   * @param id
   */
  private void onTermViewClick(int id) {
    switch (id) {
      case R.id.ftv_act_prepare_top:
        this.currentTerm = Constant.LABEL_TERM_SEMESTER0;
        this.ftv_act_prepare_top.setSelected(true);
        this.ftv_act_prepare_bottom.setSelected(false);
        break;
      case R.id.ftv_act_prepare_bottom:
        this.currentTerm = Constant.LABEL_TERM_SEMESTER1;
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
    this.sendQueryMessage(null);
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
  private void sendQueryMessage(JsonArray tags) {
    JsonObject msg = Json.createObject();
    if (tags != null) {
      msg.set(Constant.KEY_TAGS, tags);
    } else {
      msg.set(Constant.KEY_TAGS, Json.createArray().push(Constant.DATAREGISTRY_TYPE_PREPARE).push(
          this.currentTerm).push(this.currenTopic));
    }
    pb_act_result_progress.setVisibility(View.VISIBLE);
    bus.sendLocal(Constant.ADDR_TAG_CHILDREN_ATTACHMENTS, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        totalAttachmentNum = (int) body.getNumber(Constant.KEY_COUNT);
        JsonArray tags = body.getArray(Constant.TAGS);
        combineTags(tags);
        bindDataToView();
      }
    });
  }

  private void topicChooser(int id) {
    int len = this.topicRadioButtons.length;
    for (int i = 0; i < len; i++) {
      ImageView child = this.topicRadioButtons[i];
      if (child.getId() == id) {
        child.setSelected(true);
      } else {
        child.setSelected(false);
      }
    }
  }
}
