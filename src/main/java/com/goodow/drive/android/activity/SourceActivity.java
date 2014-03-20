package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.toolutils.FileTools;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 资源库
 * 
 * @author dpw
 * 
 */
public class SourceActivity extends BaseActivity implements OnClickListener {
  /**
   * 异步加载收藏标记
   * 
   * @author dpw
   * 
   */
  class MyAsyncTask extends AsyncTask<String, Void, Boolean> {
    private View view = null;

    public MyAsyncTask(View view) {
      this.view = view;
    }

    @Override
    protected Boolean doInBackground(String... params) {
      JsonObject msg = Json.createObject();
      msg.set("action", "get");
      msg.set(Constant.KEY_STAR, Json.createObject().set(Constant.KEY_TYPE, "attachment").set(
          Constant.KEY_KEY, params[0]));
      bus.send(Bus.LOCAL + Constant.ADDR_TAG_STAR, msg, new MessageHandler<JsonObject>() {
        @Override
        public void handle(Message<JsonObject> message) {
          JsonObject body = message.body();
          if (body != null) {
            view.setVisibility(View.VISIBLE);
            view.setBackgroundResource(R.drawable.source_favourited);
          }
        }
      });
      return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
      if (result) {
        view.setVisibility(View.VISIBLE);
      }
      super.onPostExecute(result);
    }
  }

  /**
   * 二级类别点事件调用
   * 
   * @param id
   */
  private class OnSubCatagoryClick implements OnClickListener {
    @Override
    public void onClick(View v) {
      if (v.isSelected()) {
        v.setSelected(false);
        subTags.remove(((TextView) v).getText().toString());
      } else {
        v.setSelected(true);
        subTags.add(((TextView) v).getText().toString());
      }
    }
  }

  private TextView tv_act_source_tip = null;
  private ImageView iv_act_source_result_pre = null;
  private LinearLayout ll_act_source_result = null;
  private ImageView iv_act_source_result_next = null;

  // private LinearLayout ll_act_source_result_bar = null;
  private int totalAttachmentNum = 0;// 总的数据量
  private int currentPageNum = 0;// 当前页码
  private final int numPerPage = 10;// 查询结果每页显示10条数据
  private final int numPerLine = 5;// 每条显示六个数据

  private LayoutInflater inflater = null;
  private TextView tv_act_source_search_result_tip = null;

  private ProgressBar pb_act_source_search_progress = null;
  private LinearLayout ll_act_source_catagory0 = null;
  private LinearLayout ll_act_source_catagory1 = null;
  private EditText et_act_source_tags = null;
  private ImageView iv_act_source_search_button = null;

  // 当前的contentType对应的ID
  private String currentContentType = null;// 搜索一级标签
  private final List<String> subTags = new ArrayList<String>();// 根据一级标签查询得到的二级标签
  private JsonArray queryingTags = null;// 控制台传递的混合标签
  private HandlerRegistration postHandler;
  private HandlerRegistration controlHandler;
  private SharedPreferences usagePreferences;
  private static final Map<Object, String> idContentTypes = new HashMap<Object, String>();
  static {
    idContentTypes.put("全部", "全部");
    idContentTypes.put("文本", "application/pdf");
    idContentTypes.put("图片", "image/jpeg");
    idContentTypes.put("动画", "application/x-shockwave-flash");
    idContentTypes.put("视频", "video/mp4");
    idContentTypes.put("音频", "audio/mpeg");
    idContentTypes.put(R.id.iv_act_source_catagory0_all, "全部");
    idContentTypes.put(R.id.iv_act_source_catagory0_text, "application/pdf");
    idContentTypes.put(R.id.iv_act_source_catagory0_image, "image/jpeg");
    idContentTypes.put(R.id.iv_act_source_catagory0_animation, "application/x-shockwave-flash");
    idContentTypes.put(R.id.iv_act_source_catagory0_video, "video/mp4");
    idContentTypes.put(R.id.iv_act_source_catagory0_audio, "audio/mpeg");
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.iv_act_source_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_source_coll:
        bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
            "favorite"), null);
        break;
      case R.id.iv_act_source_loc:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        Toast.makeText(this, "黑屏", Toast.LENGTH_LONG).show();
        break;
      case R.id.iv_act_source_catagory0_all:
      case R.id.iv_act_source_catagory0_text:
      case R.id.iv_act_source_catagory0_image:
      case R.id.iv_act_source_catagory0_animation:
      case R.id.iv_act_source_catagory0_audio:
      case R.id.iv_act_source_catagory0_video:
        this.onContentTypeClick(v.getId());
        break;
      case R.id.iv_act_source_search_button:
        this.onSearchButtonClick(v.getId());
        break;
      case R.id.iv_act_source_result_pre:
      case R.id.iv_act_source_result_next:
        this.onResultPrePageClick(v.getId());
        break;

      default:
        break;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_source);
    this.initView();
    Bundle extras = this.getIntent().getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    this.queryingTags = msg.getArray(Constant.KEY_TAGS);
    for (int i = 0; this.queryingTags != null && i < this.queryingTags.length(); i++) {
      String tag = this.queryingTags.getString(i);
      if (idContentTypes.containsKey(tag)) {
        this.currentContentType = idContentTypes.get(tag);
      }
    }
    if (this.currentContentType != null) {
      // 如果从控制台传递的参数中包含了contentType就查询子集分类
      this.echoContentType(this.currentContentType);
    }
    usagePreferences = getSharedPreferences(BehaveActivity.USAGE_STATISTIC, Context.MODE_PRIVATE);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Bundle extras = intent.getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    this.queryingTags = msg.getArray(Constant.KEY_TAGS);
    for (int i = 0; i < this.queryingTags.length(); i++) {
      String tag = this.queryingTags.getString(i);
      if (idContentTypes.containsKey(tag)) {
        this.currentContentType = idContentTypes.get(tag);
      }
    }
    if (this.currentContentType != null) {
      // 如果从控制台传递的参数中包含了contentType就查询子集分类
      this.echoContentType(this.currentContentType);
    }
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
        String action = body.getString(Constant.KEY_ACTION);
        // 仅仅处理action为post动作
        if (!"post".equalsIgnoreCase(action)) {
          return;
        }
        queryingTags = body.getArray(Constant.KEY_TAGS);
        if (queryingTags == null) {
          Toast.makeText(SourceActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
          return;
        }
        for (int i = 0; i < queryingTags.length(); i++) {
          if (Constant.LABEL_THEMES.contains(queryingTags.getString(i))) {
            return;
          }
        }
        for (int i = 0; i < queryingTags.length(); i++) {
          String tag = queryingTags.getString(i);
          if (idContentTypes.containsKey(tag)) {
            currentContentType = idContentTypes.get(tag);
          }
        }
        if (currentContentType != null) {
          // 如果从控制台传递的参数中包含了contentType就查询子集分类
          echoContentType(currentContentType);
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
            pb_act_source_search_progress.setVisibility(View.VISIBLE);
            currentPageNum = (int) page.getNumber("goTo");
          } else if (page.has("move")) {
            currentPageNum = currentPageNum + (int) page.getNumber("move");
          }
          sendQueryMessage(currentContentType, subTags, et_act_source_tags.getText().toString()
              .trim());

        }
      }
    });
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView(JsonArray attachments) {
    this.pb_act_source_search_progress.setVisibility(View.INVISIBLE);
    // this.ll_act_source_result_bar.removeAllViews();
    this.ll_act_source_result.removeAllViews();
    if (attachments == null || attachments.length() == 0) {
      this.tv_act_source_search_result_tip.setVisibility(View.VISIBLE);
      this.tv_act_source_tip.setText(Html.fromHtml(this.getString(R.string.string_source_tip1)));
      return;
    }
    this.tv_act_source_search_result_tip.setVisibility(View.INVISIBLE);
    this.tv_act_source_tip.setText(null);
    int index = 0;// 下标计数器
    int counter = attachments.length();
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
        View view = this.buildItemView(attachments.getObject(index), index);
        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(150, LayoutParams.WRAP_CONTENT);
        params.setMargins(10, 10, 10, 10);
        view.setLayoutParams(params);
        innerContainer.addView(view);
        index++;
      }
      this.ll_act_source_result.addView(innerContainer);
    }
    for (int i = 0; i < this.totalAttachmentNum; i++) {
      ImageView imageView = new ImageView(this);
      if (this.currentPageNum == 0) {
        imageView.setBackgroundResource(R.drawable.common_result_dot_current);
      } else {
        imageView.setBackgroundResource(R.drawable.common_result_dot_other);
      }
      // this.ll_act_source_result_bar.addView(imageView);
    }

    if (this.currentPageNum == 0) {
      // 第一页：向前的不显示
      this.iv_act_source_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.iv_act_source_result_pre.setVisibility(View.VISIBLE);
    }
    if (this.currentPageNum < (this.totalAttachmentNum % this.numPerPage == 0
        ? this.totalAttachmentNum / this.numPerPage - 1 : this.totalAttachmentNum / this.numPerPage)) {
      // 小于总页数：向后的显示
      this.iv_act_source_result_next.setVisibility(View.VISIBLE);
    } else {
      this.iv_act_source_result_next.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * 显示标签的子标签
   * 
   * @param tags
   */
  private void bindSubTagToView(JsonArray tags) {
    int len = tags.length();
    for (int i = 0; i < len; i++) {
      String tag = tags.getString(i);
      TextView child = (TextView) this.inflater.inflate(R.layout.soruce_item_sub_tag, null);
      child.setText(tags.getString(i));
      child.setOnClickListener(new OnSubCatagoryClick());
      // 查询得到的二级标签中是否包含了控制台传递过来的二级标签
      for (int j = 0; this.queryingTags != null && j < this.queryingTags.length(); j++) {
        String queryingTag = this.queryingTags.getString(j);
        if (tag.equals(queryingTag)) {
          this.subTags.add(tag);
          child.setSelected(true);
          break;
        }
      }
      this.ll_act_source_catagory1.addView(child);
    }
  }

  /**
   * 构建条目View对象
   * 
   * @param name 条目要显示的名称
   * @param index 条目的数据在数据集中的下标
   * @return
   */
  private View buildItemView(final JsonObject attachment, int index) {
    String fileName = attachment.getString(Constant.KEY_NAME);
    RelativeLayout itemContainer =
        (RelativeLayout) this.inflater.inflate(R.layout.activity_source_search_result_item, null);
    final String attachmentId = attachment.getString(Constant.KEY_ID);
    ImageView imageView =
        (ImageView) itemContainer.findViewById(R.id.iv_act_source_search_result_item_icon);
    FileTools.setImageThumbnalilUrl(imageView, attachment.getString(Constant.KEY_URL), attachment
        .getString(Constant.KEY_THUMBNAIL));

    TextView textView =
        (TextView) itemContainer.findViewById(R.id.tv_act_source_search_result_item_filename);
    textView.setText(fileName);

    final ImageView imageViewFlag =
        (ImageView) itemContainer.findViewById(R.id.iv_act_source_search_result_item_flag);
    new MyAsyncTask(imageViewFlag).execute(attachment.getString(Constant.KEY_ID));// 异步夹在收藏标记
    imageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // 打开文件
        bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, Json.createObject().set("path",
            attachment.getString(Constant.KEY_URL)).set("play", 1), null);
        // acctachment
        // 此处记录打开的时间
        Set<String> fileOpenInfo =
            usagePreferences.getStringSet(attachmentId, new TreeSet<String>());
        fileOpenInfo.add(System.currentTimeMillis() + "");
        Editor editor = usagePreferences.edit();
        // 如果存在，移除key
        if (usagePreferences.contains(attachmentId)) {
          editor.remove(attachmentId).commit();
        }
        editor.putStringSet(attachmentId, fileOpenInfo).commit();
      }
    });

    imageView.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        if (imageViewFlag.getTag() == null
            || (!"0".equals(imageViewFlag.getTag().toString()) && !"1".equals(imageViewFlag
                .getTag().toString()))) {
          // 0处于已点击未收藏 1处于已经收藏 非0非1处于原始状态
          imageViewFlag.setBackgroundResource(R.drawable.source_favourite);
          imageViewFlag.setVisibility(View.VISIBLE);
          imageViewFlag.setTag("0");
        }
        return true;
      }
    });

    imageViewFlag.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (imageViewFlag.getTag().toString().equals("0")) {
          // 0处于已点击未收藏
          JsonObject msg = Json.createObject();
          msg.set(Constant.KEY_ACTION, "post");
          msg.set(Constant.KEY_STAR, Json.createObject().set(Constant.KEY_TYPE, "attachment").set(
              Constant.KEY_KEY, attachment.getString(Constant.KEY_ID)));
          bus.send(Bus.LOCAL + Constant.ADDR_TAG_STAR, msg, new MessageHandler<JsonObject>() {
            @Override
            public void handle(Message<JsonObject> message) {
              JsonObject body = message.body();
              if ("ok".equalsIgnoreCase(body.getString(Constant.KEY_STATUS))) {
                Toast.makeText(SourceActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
                imageViewFlag.setBackgroundResource(R.drawable.source_favourited);
                imageViewFlag.setTag("1");
              } else {
                Toast.makeText(SourceActivity.this, "收藏失败，请重试", Toast.LENGTH_SHORT).show();
              }
            }
          });
        }
      }
    });

    return itemContainer;
  }

  /**
   * 清空查询结果
   */
  private void cleanSearchResult() {
    this.ll_act_source_result.removeAllViews();
    // this.ll_act_source_result_bar.removeAllViews();
    this.iv_act_source_result_pre.setVisibility(View.INVISIBLE);
    this.iv_act_source_result_next.setVisibility(View.INVISIBLE);
    this.et_act_source_tags.setText(null);
    this.ll_act_source_catagory1.removeAllViews();
  }

  /**
   * 回显contentType
   * 
   * @param currentContentType
   */
  private void echoContentType(String currentContentType) {
    Set<Entry<Object, String>> entrySet = idContentTypes.entrySet();
    for (Entry<Object, String> entry : entrySet) {
      if (entry.getKey() instanceof Integer && entry.getValue().equals(currentContentType)) {
        this.onContentTypeClick(Integer.parseInt(entry.getKey().toString()));
        break;
      }
    }
  }

  /**
   * 初始化View对象 设置点击事件 设置光标事件监听
   */
  private void initView() {
    this.iv_act_source_result_pre = (ImageView) this.findViewById(R.id.iv_act_source_result_pre);
    this.iv_act_source_result_pre.setOnClickListener(this);
    this.ll_act_source_result = (LinearLayout) this.findViewById(R.id.ll_act_source_result);
    this.iv_act_source_result_next = (ImageView) this.findViewById(R.id.iv_act_source_result_next);
    this.iv_act_source_result_next.setOnClickListener(this);
    // this.ll_act_source_result_bar = (LinearLayout)
    // this.findViewById(R.id.ll_act_source_result_bar);

    this.tv_act_source_tip = (TextView) this.findViewById(R.id.tv_act_source_tip);
    this.tv_act_source_search_result_tip =
        (TextView) this.findViewById(R.id.tv_act_source_search_result_tip);
    this.pb_act_source_search_progress =
        (ProgressBar) this.findViewById(R.id.pb_act_source_search_progress);
    this.ll_act_source_catagory0 = (LinearLayout) this.findViewById(R.id.ll_act_source_catagory0);
    int len_catagory0 = this.ll_act_source_catagory0.getChildCount();
    for (int i = 0; i < len_catagory0; i++) {
      this.ll_act_source_catagory0.getChildAt(i).setOnClickListener(this);
    }
    this.ll_act_source_catagory1 = (LinearLayout) this.findViewById(R.id.ll_act_source_catagory1);
    int len_catagory1 = this.ll_act_source_catagory1.getChildCount();
    for (int i = 0; i < len_catagory1; i++) {
      this.ll_act_source_catagory1.getChildAt(i).setOnClickListener(this);
    }
    this.et_act_source_tags = (EditText) this.findViewById(R.id.et_act_source_tags);
    this.iv_act_source_search_button =
        (ImageView) this.findViewById(R.id.iv_act_source_search_button);
    this.iv_act_source_search_button.setOnClickListener(this);

    this.inflater = LayoutInflater.from(this);
  }

  /**
   * 一级类别点事件调用
   * 
   * @param id
   */
  private void onContentTypeClick(int id) {
    this.subTags.clear();
    int len = this.ll_act_source_catagory0.getChildCount();
    for (int i = 0; i < len; i++) {
      if (id != this.ll_act_source_catagory0.getChildAt(i).getId()) {
        this.ll_act_source_catagory0.getChildAt(i).setSelected(false);
      }
    }
    ImageView current = (ImageView) this.findViewById(id);
    this.cleanSearchResult();
    if (current.isSelected()) {
      current.setSelected(false);
      this.currentContentType = null;
      this.tv_act_source_tip.setText(this.getString(R.string.string_source_tip0));
      this.queryingTags = null;
    } else {
      current.setSelected(true);
      this.tv_act_source_tip.setText(null);
      this.currentContentType = idContentTypes.get(id);
      if (id != R.id.iv_act_source_catagory0_all) {
        this.sendQuerySubCatagory(this.currentContentType);
      }
    }
  }

  /**
   * 处理上下翻页的点击事件
   * 
   * @param id
   */
  private void onResultPrePageClick(int id) {
    this.pb_act_source_search_progress.setVisibility(View.VISIBLE);
    if (id == R.id.iv_act_source_result_pre && this.currentPageNum >= 0) {
      // 向前翻页
      this.currentPageNum--;
    } else if (id == R.id.iv_act_source_result_next
        && this.currentPageNum < this.totalAttachmentNum) {
      // 向后翻页
      this.currentPageNum++;
    }
    this.sendQueryMessage(this.currentContentType, this.subTags, this.et_act_source_tags.getText()
        .toString().trim());
  }

  /**
   * 搜索按钮的点击事件
   * 
   * @param id
   */
  private void onSearchButtonClick(int id) {
    this.totalAttachmentNum = 0;
    this.currentPageNum = 0;
    if (this.currentContentType == null
        && this.subTags.size() == 0
        && (this.et_act_source_tags.getText().toString() == null || this.et_act_source_tags
            .getText().toString().trim().equals(""))) {
      Toast.makeText(this, this.getString(R.string.string_source_tip0), Toast.LENGTH_SHORT).show();
    } else {
      this.pb_act_source_search_progress.setVisibility(View.VISIBLE);
      this.sendQueryMessage(this.currentContentType == null ? "全部" : this.currentContentType,
          this.subTags, this.et_act_source_tags.getText().toString().trim());
    }
  }

  /**
   * 构建查询文件的bus消息
   */
  private void sendQueryMessage(String contentType, List<String> tags, String query) {
    JsonObject msg = Json.createObject();
    msg.set(Constant.KEY_CONTENTTYPE, contentType);// 2131296367
    msg.set(Constant.KEY_FROM, this.numPerPage * this.currentPageNum);
    msg.set(Constant.KEY_SIZE, this.numPerPage);
    if (tags.size() > 0) {
      JsonArray tagArray = Json.createArray();
      for (int i = 0; i < tags.size(); i++) {
        tagArray.push(tags.get(i));
      }
      msg.set(Constant.KEY_TAGS, tagArray);
    }
    if (query != null && !query.trim().equals("")) {
      msg.set(Constant.KEY_QUERY, this.et_act_source_tags.getText().toString());
    }
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_ATTACHMENT_SEARCH, msg,
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            totalAttachmentNum = (int) body.getNumber(Constant.KEY_SIZE);
            JsonArray attachments = body.getArray(Constant.KEY_ATTACHMENTS);
            bindDataToView(attachments);
          }
        });
  }

  /**
   * 构建查询的bus消息查询子级分类
   */
  private void sendQuerySubCatagory(String contentType) {
    JsonObject msg =
        Json.createObject().set(Constant.KEY_TAGS, Json.createArray().push(contentType));
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_CHILDREN, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonArray tags = (JsonArray) message.body();
        bindSubTagToView(tags);
      }
    });
  }
}
