package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.Registration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import android.content.Intent;
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
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_earlyreading)
public class EarlyReadingActivity extends BaseActivity implements OnClickListener {
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
        convertView = View.inflate(EarlyReadingActivity.this, R.layout.result_common, null);
        ResultAdapterHolder holder = new ResultAdapterHolder();
        holder.iv_common_result = (ImageView) convertView.findViewById(R.id.iv_common_result);
        holder.iv_common_result.getLayoutParams().width = 114;
        holder.iv_common_result.getLayoutParams().height = 114;
        holder.tv_common_result = (TextView) convertView.findViewById(R.id.tv_common_result);
        holder.tv_common_result.getLayoutParams().width = 150;
        convertView.setTag(holder);
      }
      final JsonObject attachment = attachments.getObject(position);
      ResultAdapterHolder holder = (ResultAdapterHolder) convertView.getTag();
      // FileTools.setImageThumbnalilUrl(holder.iv_common_result, attachment
      // .getString(Constant.KEY_URL), attachment.getString(Constant.KEY_THUMBNAIL));
      holder.iv_common_result.setBackgroundResource(R.drawable.common_ebook);
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

  // 后退收藏锁屏
  @InjectView(R.id.iv_act_ebook_back)
  private ImageView iv_act_ebook_back;
  @InjectView(R.id.iv_act_ebook_coll)
  private ImageView iv_act_ebook_coll;
  @InjectView(R.id.iv_act_ebook_loc)
  private ImageView iv_act_ebook_loc;

  private final int numPerPage = 8;// 查询结果每页显示8条数据
  @InjectView(R.id.vp_act_ebook_result)
  private GridView vp_act_ebook_result;
  // 翻页按钮
  @InjectView(R.id.rl_act_ebook_result_pre)
  private ImageView rl_act_ebook_result_pre;
  @InjectView(R.id.rl_act_ebook_result_next)
  private ImageView rl_act_ebook_result_next;
  // 查询进度
  @InjectView(R.id.pb_act_result_progress)
  private ProgressBar pb_act_result_progress;

  // 页码状态
  @InjectView(R.id.ll_act_ebook_result_bar)
  private LinearLayout ll_act_ebook_result_bar;

  private Registration postHandler;

  private Registration controlHandler;

  private Registration refreshHandler;

  private ResultAdapter resultAdapter;// 结果gridview适配器
  private int currentPageNum;// 当前结果页数
  private int totalAttachmentNum;// 结果总数

  @InjectExtra(value = "msg", optional = true)
  private JsonObject msg;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_ebook_back:
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_ebook_coll:
        this.bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
            "favorite"), null);
        break;
      case R.id.iv_act_ebook_loc:
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
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
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.initView();
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
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
          Toast.makeText(EarlyReadingActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
          return;
        }
        for (int i = 0; i < tags.length(); i++) {
          if (Constant.LABEL_THEMES.contains(tags.getString(i))) {
            return;
          }
        }
        if (tags.indexOf(Constant.DATAREGISTRY_TYPE_READ) == -1) {
          tags.push(Constant.DATAREGISTRY_TYPE_READ);
        }
        sendQueryMessage(buildTags(tags));
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
      this.rl_act_ebook_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_ebook_result_pre.setVisibility(View.VISIBLE);
    }
    if (this.currentPageNum < (this.totalAttachmentNum % this.numPerPage == 0
        ? this.totalAttachmentNum / this.numPerPage - 1 : this.totalAttachmentNum / this.numPerPage)) {
      // 小于总页数：向后的显示
      this.rl_act_ebook_result_next.setVisibility(View.VISIBLE);
    } else {
      this.rl_act_ebook_result_next.setVisibility(View.INVISIBLE);
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
    // 删除垃圾数据
    for (int i = 0; i < tags.length(); i++) {
      String tag = tags.getString(i);
      boolean isLegalTheme = Constant.LABEL_THEMES.contains(tag);
      if (!isLegalTheme) {
        tags.remove(i--);
      }
    }
    return tags;
  }

  /**
   * 初始化View对象 设置点击事件 设置光标事件监听 添加到对应集合
   */
  private void initView() {
    this.iv_act_ebook_back.setOnClickListener(this);
    this.iv_act_ebook_coll.setOnClickListener(this);
    this.iv_act_ebook_loc.setOnClickListener(this);
    resultAdapter = new ResultAdapter();
    this.vp_act_ebook_result.setAdapter(resultAdapter);
    this.rl_act_ebook_result_pre.setOnClickListener(this);
    this.rl_act_ebook_result_next.setOnClickListener(this);
  }

  private void onPageSelected(int position) {
    int totalPageNum =
        (totalAttachmentNum / numPerPage) + (totalAttachmentNum % numPerPage > 0 ? 1 : 0);
    ll_act_ebook_result_bar.removeAllViews();
    for (int i = 0; i < totalPageNum; i++) {
      ImageView imageView = new ImageView(EarlyReadingActivity.this);
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
      ll_act_ebook_result_bar.addView(imageView);
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
    if (id == R.id.rl_act_ebook_result_pre) {
      page.set("move", -1);
    } else if (id == R.id.rl_act_ebook_result_next) {
      page.set("move", 1);
    }
    bus.sendLocal(Constant.ADDR_CONTROL, msg, null);
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
      msg.set(Constant.KEY_TAGS, Json.createArray().push(Constant.DATAREGISTRY_TYPE_READ));
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
