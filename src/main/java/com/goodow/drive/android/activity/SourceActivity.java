package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.SparseArray;
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
        catagory1.remove(((TextView) v).getText().toString());
      } else {
        v.setSelected(true);
        catagory1.add(((TextView) v).getText().toString());
      }
    }
  }

  private TextView tv_act_source_tip = null;
  private ImageView iv_act_source_result_pre = null;
  private LinearLayout ll_act_source_result = null;
  private ImageView iv_act_source_result_next = null;

  // private LinearLayout ll_act_source_result_bar = null;
  private int totalPageNum = 0;// 总的数据量
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
  private int currentContentType = 0;// 搜索一级标签
  private final List<String> catagory1 = new ArrayList<String>();// 二级标签

  private HandlerRegistration controlHandler;

  private static final SparseArray<String> idNames = new SparseArray<String>();
  private static final SparseArray<String> idContentTypes = new SparseArray<String>();

  static {
    idNames.put(R.id.iv_act_source_catagory0_all, "全部");
    idNames.put(R.id.iv_act_source_catagory0_text, "文本");
    idNames.put(R.id.iv_act_source_catagory0_image, "图片");
    idNames.put(R.id.iv_act_source_catagory0_animation, "动画");
    idNames.put(R.id.iv_act_source_catagory0_audio, "视频");
    idNames.put(R.id.iv_act_source_catagory0_video, "音频");
    idNames.put(R.id.iv_act_source_catagory0_ebook, "图画书");
    idNames.put(R.id.iv_act_source_catagory0_game, "游戏");

    idContentTypes.put(R.id.iv_act_source_catagory0_all, "全部");
    idContentTypes.put(R.id.iv_act_source_catagory0_text, "application/pdf");
    idContentTypes.put(R.id.iv_act_source_catagory0_image, "image/jpeg");
    idContentTypes.put(R.id.iv_act_source_catagory0_animation, "application/x-shockwave-flash");
    idContentTypes.put(R.id.iv_act_source_catagory0_audio, "video/mp4");
    idContentTypes.put(R.id.iv_act_source_catagory0_video, "audio/mpeg");
    idContentTypes.put(R.id.iv_act_source_catagory0_ebook, "application/x-shockwave-flash");
    idContentTypes.put(R.id.iv_act_source_catagory0_game, "application/x-shockwave-flash");

  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.iv_act_source_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_source_coll:
        bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, Json.createObject().set("action", "post").set(
            Constant.QUERIES,
            Json.createObject().set(Constant.TYPE, Constant.DATAREGISTRY_TYPE_FAVOURITE)), null);
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
      case R.id.iv_act_source_catagory0_ebook:
      case R.id.iv_act_source_catagory0_game:
        this.onCatagory0Click(v.getId());
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
  }

  @Override
  protected void onPause() {
    super.onPause();
    controlHandler.unregisterHandler();
  }

  @Override
  protected void onResume() {
    super.onResume();
    controlHandler = bus.registerHandler(Constant.ADDR_CONTROL, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body.has("page")) {
          JsonObject page = body.getObject("page");
          if (page.has("goTo")) {
          } else if (page.has("move")) {
          }
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
    for (int i = 0; i < this.totalPageNum; i++) {
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
    if (this.currentPageNum < this.totalPageNum) {
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
      TextView child = (TextView) this.inflater.inflate(R.layout.soruce_item_sub_tag, null);
      child.setText(tags.getString(i));
      child.setOnClickListener(new OnSubCatagoryClick());
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
    RelativeLayout itemContainer =
        (RelativeLayout) this.inflater.inflate(R.layout.activity_source_search_result_item, null);

    ImageView imageView =
        (ImageView) itemContainer.findViewById(R.id.iv_act_source_search_result_item_icon);
    imageView.setBackgroundResource(R.drawable.case_item_bg);

    String title = attachment.getString(Constant.KEY_NAME);
    TextView textView =
        (TextView) itemContainer.findViewById(R.id.tv_act_source_search_result_item_filename);
    if (title.matches("^\\d{4}.*")) {
      textView.setText(title.substring(4, title.length()));
    } else {
      textView.setText(title);
    }

    final ImageView imageViewFlag =
        (ImageView) itemContainer.findViewById(R.id.iv_act_source_search_result_item_flag);
    new MyAsyncTask(imageViewFlag).execute(attachment.getString(Constant.KEY_ID));
    imageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // 打开文件
        bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, Json.createObject().set("path",
            attachment.getString(Constant.KEY_URL)).set("play", 1), null);
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
  private void onCatagory0Click(int id) {
    this.catagory1.clear();
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
      this.currentContentType = 0;
      this.tv_act_source_tip.setText(this.getString(R.string.string_source_tip0));
    } else {
      current.setSelected(true);
      this.tv_act_source_tip.setText(null);
      this.currentContentType = id;
      if (id != R.id.iv_act_source_catagory0_all) {
        this.sendQuerySubCatagory(idNames.get(id));
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
    } else if (id == R.id.iv_act_source_result_next && this.currentPageNum < this.totalPageNum) {
      // 向后翻页
      this.currentPageNum++;
    }
    this.sendQueryMessage(this.currentContentType, this.catagory1, this.et_act_source_tags
        .getText().toString().trim());
  }

  /**
   * 搜索按钮的点击事件
   * 
   * @param id
   */
  private void onSearchButtonClick(int id) {
    if (this.currentContentType == 0
        && this.catagory1.size() == 0
        && (this.et_act_source_tags.getText().toString() == null || this.et_act_source_tags
            .getText().toString().trim().equals(""))) {
      Toast.makeText(this, this.getString(R.string.string_source_tip0), Toast.LENGTH_SHORT).show();
    } else {
      this.pb_act_source_search_progress.setVisibility(View.VISIBLE);
      this.sendQueryMessage(this.currentContentType, this.catagory1, this.et_act_source_tags
          .getText().toString().trim());
    }
  }

  /**
   * 构建查询文件的bus消息
   */
  private void sendQueryMessage(int id, List<String> tags, String query) {
    JsonObject msg = Json.createObject();
    msg.set(Constant.KEY_CONTENTTYPE, idContentTypes.get(id));// 2131296367
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
            totalPageNum = (int) body.getNumber(Constant.KEY_SIZE);
            JsonArray attachments = body.getArray(Constant.KEY_ATTACHMENTS);
            bindDataToView(attachments);
          }
        });
  }

  /**
   * 构建查询的bus消息查询子级分类
   */
  private void sendQuerySubCatagory(String parentTag) {
    JsonObject msg = Json.createObject().set(Constant.KEY_TAGS, Json.createArray().push(parentTag));
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_CHILDREN, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonArray tags = (JsonArray) message.body();
        bindSubTagToView(tags);
      }
    });
  }
}
