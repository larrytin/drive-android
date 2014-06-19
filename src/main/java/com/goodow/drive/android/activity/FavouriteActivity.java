package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.toolutils.FileTools;
import com.goodow.drive.android.view.FavouriteAttachmentsView;
import com.goodow.drive.android.view.FavouriteTagsView;
import com.goodow.drive.android.view.FontTextView;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.Registration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;
import roboguice.inject.ContentView;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

@ContentView(R.layout.activity_favourite)
public class FavouriteActivity extends BaseActivity implements OnClickListener {

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
      return viewSwitcher(attachments, position, convertView, parent);
    }

    public void reset(JsonArray attachments) {
      this.attachments = attachments;
    }
  }

  private static final String LABEL_TAG = "tag";
  private static final String LABEL_ATTACHMENT = "attachment";
  private static final Map<String, String> LABEL_RELATION = new HashMap<String, String>();
  static {
    LABEL_RELATION.put("活动", LABEL_TAG);
    LABEL_RELATION.put("文件", LABEL_ATTACHMENT);
  }
  private String currentTopic = LABEL_TAG;
  @InjectView(R.id.iv_act_favour_back)
  private ImageView iv_act_favour_back;
  @InjectView(R.id.ft_act_favour_item_activity)
  private FontTextView ft_act_favour_item_activity;
  @InjectView(R.id.ft_act_favour_item_file)
  private FontTextView ft_act_favour_item_file;
  @InjectView(R.id.iv_act_favour_result_pre)
  private ImageView iv_act_favour_result_pre;
  @InjectView(R.id.iv_act_favour_result_next)
  private ImageView iv_act_favour_result_next;
  @InjectView(R.id.vp_act_favour_result)
  private GridView vp_act_favour_result;
  @InjectView(R.id.ll_act_favour_result_bar)
  private LinearLayout ll_act_favour_result_bar;

  private final int numPerPageActivity = 15; // 查询结果每页显示数据条数
  private final int numPerPageAttachment = 10; // 查询结果每页显示数据条数
  private int numPerPage = numPerPageActivity; // 查询结果每页显示数据条数

  private Registration registerPostHandler;
  private Registration controlHandler;

  private Registration refreshHandler;

  private boolean isEditMode;// 是否处于编辑模式

  private ResultAdapter resultAdapter;// 结果gridview适配器
  private int currentPageNum;// 当前结果页数
  private int totalAttachmentNum;// 结果总数
  // 查询进度
  @InjectView(R.id.pb_act_result_progress)
  private ProgressBar pb_act_result_progress;

  @InjectExtra(value = "msg", optional = true)
  private JsonObject msg;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退的点击事件
      case R.id.iv_act_favour_back:
        JsonObject msg = Json.createObject();
        msg.set("return", true);
        bus.sendLocal(Constant.ADDR_CONTROL, msg, null);
        break;
      //
      case R.id.ft_act_favour_item_activity:
      case R.id.ft_act_favour_item_file:
        onLabelChange(v.getId());
        isEditMode = false;
        break;
      // 翻页的点击事件
      case R.id.iv_act_favour_result_pre:
      case R.id.iv_act_favour_result_next:
        this.onResultPrePageClick(v.getId());
        break;
      default:
        break;
    }
  }

  public void onPageSelected(int position) {
    int totalPageNum =
        (totalAttachmentNum / numPerPage) + (totalAttachmentNum % numPerPage > 0 ? 1 : 0);
    ll_act_favour_result_bar.removeAllViews();
    for (int i = 0; i < totalPageNum; i++) {
      ImageView imageView = new ImageView(FavouriteActivity.this);
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
      ll_act_favour_result_bar.addView(imageView);
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.initView();
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.buildTags(tags);
    this.sendQueryMessage(this.currentTopic);
    this.echoTopic();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.buildTags(tags);
    this.sendQueryMessage(this.currentTopic);
    this.echoTopic();
  }

  @Override
  protected void onPause() {
    super.onPause();
    registerPostHandler.unregister();
    controlHandler.unregister();
    refreshHandler.unregister();
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerPostHandler =
        bus.subscribeLocal(Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
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
              Toast.makeText(FavouriteActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
              return;
            }
            for (int i = 0; i < tags.length(); i++) {
              if (Constant.LABEL_THEMES.contains(tags.getString(i))) {
                return;
              }
            }
            if (tags.indexOf(Constant.DATAREGISTRY_TYPE_FAVOURITE) == -1) {
              tags.push(Constant.DATAREGISTRY_TYPE_FAVOURITE);
            }
            buildTags(tags);
            sendQueryMessage(currentTopic);
            echoTopic();
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
              sendQueryMessage(currentTopic);
            }
          }
        });

    refreshHandler =
        bus.subscribeLocal(Constant.ADDR_VIEW_REFRESH, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            sendQueryMessage(currentTopic);
          }
        });
  }

  /**
   * 把数据绑定到View对象
   * 
   * @param indexNum 初始化显示的页码
   */
  private void bindDataToView(JsonArray tags) {
    onPageSelected(currentPageNum);
    pb_act_result_progress.setVisibility(View.INVISIBLE);
    if (this.currentPageNum == 0) {
      // 第一页：向前的不显示
      this.iv_act_favour_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.iv_act_favour_result_pre.setVisibility(View.VISIBLE);
    }
    if (this.currentPageNum < (this.totalAttachmentNum % this.numPerPage == 0
        ? this.totalAttachmentNum / this.numPerPage - 1 : this.totalAttachmentNum / this.numPerPage)) {
      // 小于总页数：向后的显示
      this.iv_act_favour_result_next.setVisibility(View.VISIBLE);
    } else {
      this.iv_act_favour_result_next.setVisibility(View.INVISIBLE);
    }
    resultAdapter.reset(tags);
    resultAdapter.notifyDataSetChanged();
  }

  /**
   * 构建查询TAGS
   * 
   * @param tags
   * @return
   */
  private JsonArray buildTags(JsonArray tags) {
    if (tags == null) {
      return null;
    }
    // 删除垃圾数据
    for (int i = 0; i < tags.length(); i++) {
      String tag = tags.getString(i);
      boolean isLegalTheme = Constant.LABEL_THEMES.contains(tag);
      boolean isLegalTopic = LABEL_RELATION.containsKey(tag);
      if (!isLegalTheme && !isLegalTopic) {
        tags.remove(i--);
      }
    }

    // 如果默认的班级、学期、主题不在tags中就加入 如果存在就设置为当前
    for (int i = 0; i < tags.length(); i++) {
      if (LABEL_RELATION.containsKey(tags.getString(i))) {
        this.currentTopic = LABEL_RELATION.get(tags.getString(i));
        break;
      }
    }
    if (tags.indexOf(this.currentTopic) == -1) {
      tags.push(this.currentTopic);
    }
    return tags;
  }

  /**
   * 回显主题
   */
  private void echoTopic() {
    currentPageNum = 0;
    if (this.currentTopic.equals(LABEL_TAG)) {
      this.ft_act_favour_item_file.setSelected(false);
      this.ft_act_favour_item_activity.setSelected(true);
    } else if (this.currentTopic.equals(LABEL_ATTACHMENT)) {
      this.ft_act_favour_item_file.setSelected(true);
      this.ft_act_favour_item_activity.setSelected(false);
    }
  }

  /**
   * 初始化View
   */
  private void initView() {
    this.iv_act_favour_back.setOnClickListener(this);
    this.ft_act_favour_item_activity.setOnClickListener(this);
    this.ft_act_favour_item_activity.setSelected(true);
    this.ft_act_favour_item_file.setOnClickListener(this);
    this.iv_act_favour_result_pre.setOnClickListener(this);
    this.iv_act_favour_result_next.setOnClickListener(this);
    resultAdapter = new ResultAdapter();
    this.vp_act_favour_result.setAdapter(resultAdapter);
  }

  /**
   * 叶签切换的点击事件
   * 
   * @param id
   */
  private void onLabelChange(int id) {
    if (id == R.id.ft_act_favour_item_activity) {
      this.currentTopic = LABEL_TAG;
      this.numPerPage = this.numPerPageActivity;
    } else if (id == R.id.ft_act_favour_item_file) {
      this.currentTopic = LABEL_ATTACHMENT;
        this.numPerPage = this.numPerPageAttachment;
    }
    this.echoTopic();
    this.sendQueryMessage(this.currentTopic);
  }

  /**
   * 翻页的点击事件
   * 
   * @param id
   */
  private void onResultPrePageClick(int id) {
    JsonObject msg = Json.createObject();
    JsonObject page = Json.createObject();
    msg.set("page", page);
    if (id == R.id.iv_act_favour_result_pre) {
      page.set("move", -1);
    } else if (id == R.id.iv_act_favour_result_next) {
      page.set("move", 1);
    }
    bus.sendLocal(Constant.ADDR_CONTROL, msg, null);
  }

  /**
   * 构建查询的bus消息进行get查询
   */
  private void sendQueryMessage(String type) {
    JsonObject msg = Json.createObject();
    msg.set(Constant.KEY_FROM, numPerPage * currentPageNum);
    msg.set(Constant.KEY_SIZE, numPerPage);
    msg.set(Constant.KEY_TYPE, type);
    pb_act_result_progress.setVisibility(View.VISIBLE);
    bus.sendLocal(Constant.ADDR_TAG_STAR_SEARCH, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        totalAttachmentNum = (int) body.getNumber(Constant.KEY_COUNT);
        JsonArray attachments = null;
        if (currentTopic.equals(LABEL_TAG)) {
          attachments = body.getArray(Constant.KEY_TAGS);
        } else if (currentTopic.equals(LABEL_ATTACHMENT)) {
          attachments = body.getArray(Constant.KEY_ATTACHMENTS);
        }
        bindDataToView(attachments);
      }
    });
  }

  private View viewSwitcher(final JsonArray attachments, final int position, View convertView,
      ViewGroup parent) {
    if (this.currentTopic.equals(LABEL_TAG)) {
      final FavouriteTagsView favouriteTagsView;
      if (convertView != null && convertView instanceof FavouriteTagsView) {
        favouriteTagsView = (FavouriteTagsView) convertView;
      } else {
        favouriteTagsView = new FavouriteTagsView(FavouriteActivity.this);
      }
      FrameLayout delButton = favouriteTagsView.getDelButton();
      final JsonObject activity = attachments.getObject(position);
      final JsonArray tags = Json.parse(activity.getString(Constant.KEY_TAG));
      final String title = tags.getString(tags.length() - 1);
      if (title.matches("^\\d{4}.*")) {
        favouriteTagsView.setText(title.substring(4, title.length()));
      } else {
        favouriteTagsView.setText(title);
      }
      favouriteTagsView.setDeleteSate(isEditMode);
      // 点击事件 更改删除状态或打开一个活动
      favouriteTagsView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (isEditMode) {
            for (int i = 0; i < vp_act_favour_result.getChildCount(); ++i) {
              ((FavouriteTagsView) vp_act_favour_result.getChildAt(i)).setDeleteSate(false);
            }
            isEditMode = false;
          } else {
            JsonObject msg = Json.createObject();
            msg.set(Constant.KEY_ACTION, "post");
            msg.set(Constant.KEY_TITLE, title);
            msg.set(Constant.KEY_TAGS, tags);
            bus.sendLocal(Constant.ADDR_ACTIVITY, msg, null);
          }
        }
      });
      // 长按事件 唤出删除状态或取消删除状态
      favouriteTagsView.setOnLongClickListener(new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          if (isEditMode) {
            isEditMode = false;
            for (int i = 0; i < vp_act_favour_result.getChildCount(); ++i) {
              ((FavouriteTagsView) vp_act_favour_result.getChildAt(i)).setDeleteSate(false);
            }
          } else {
            isEditMode = true;
            for (int i = 0; i < vp_act_favour_result.getChildCount(); ++i) {
              ((FavouriteTagsView) vp_act_favour_result.getChildAt(i)).setDeleteSate(true);
            }
          }
          return true;
        }
      });
      // 删除点击事件
      delButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          JsonObject msg = Json.createObject();
          msg.set(Constant.KEY_ACTION, "delete");
          msg.set(Constant.KEY_STARS, Json.createArray().push(
              Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY,
                  activity.getString(Constant.KEY_TAG))));
          bus.sendLocal(Constant.ADDR_TAG_STAR, msg, new MessageHandler<JsonObject>() {
            @Override
            public void handle(Message<JsonObject> message) {
              JsonObject body = message.body();
              if ("ok".equalsIgnoreCase(body.getString(Constant.KEY_STATUS))) {
                Toast.makeText(FavouriteActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                attachments.remove(position);
                bindDataToView(attachments);
              } else {
                Toast.makeText(FavouriteActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
              }
            }
          });
        }
      });
      return favouriteTagsView;
    } else if (this.currentTopic.equals(LABEL_ATTACHMENT)) {
      final FavouriteAttachmentsView favouriteAttachmentsView;
      if (convertView != null && convertView instanceof FavouriteAttachmentsView) {
        favouriteAttachmentsView = (FavouriteAttachmentsView) convertView;
      } else {
        favouriteAttachmentsView = new FavouriteAttachmentsView(FavouriteActivity.this);
      }
      ImageView delButton = favouriteAttachmentsView.getDelButton();
      ImageView imageView = favouriteAttachmentsView.getImageView();
      final JsonObject attachment = attachments.getObject(position);
      String fileName = attachment.getString(Constant.KEY_NAME);
      FileTools.setImageThumbnalilUrl(imageView, attachment.getString(Constant.KEY_URL), attachment
          .getString(Constant.KEY_THUMBNAIL));
      favouriteAttachmentsView.setText(fileName);
      favouriteAttachmentsView.setDeleteSate(isEditMode);
      favouriteAttachmentsView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (isEditMode) {
            for (int i = 0; i < vp_act_favour_result.getChildCount(); ++i) {
              ((FavouriteAttachmentsView) vp_act_favour_result.getChildAt(i)).setDeleteSate(false);
            }
            isEditMode = false;
          } else {
            bus.sendLocal(Constant.ADDR_PLAYER, Json.createObject().set("path",
                attachment.getString(Constant.KEY_URL)).set("play", 1), null);
          }
        }
      });

      favouriteAttachmentsView.setOnLongClickListener(new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          if (isEditMode) {
            isEditMode = false;
            for (int i = 0; i < vp_act_favour_result.getChildCount(); ++i) {
              ((FavouriteAttachmentsView) vp_act_favour_result.getChildAt(i)).setDeleteSate(false);
            }
          } else {
            isEditMode = true;
            for (int i = 0; i < vp_act_favour_result.getChildCount(); ++i) {
              ((FavouriteAttachmentsView) vp_act_favour_result.getChildAt(i)).setDeleteSate(true);
            }
          }
          return true;
        }
      });

      delButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          JsonObject msg = Json.createObject();
          msg.set(Constant.KEY_ACTION, "delete");
          msg.set(Constant.KEY_STARS, Json.createArray().push(
              Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY,
                  attachment.getString(Constant.KEY_ID))));
          bus.sendLocal(Constant.ADDR_TAG_STAR, msg, new MessageHandler<JsonObject>() {
            @Override
            public void handle(Message<JsonObject> message) {
              JsonObject body = message.body();
              if ("ok".equalsIgnoreCase(body.getString(Constant.KEY_STATUS))) {
                Toast.makeText(FavouriteActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                attachments.remove(position);
                bindDataToView(attachments);
              } else {
                Toast.makeText(FavouriteActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
              }
            }
          });
        }
      });
      return favouriteAttachmentsView;
    }
    return null;
  }
}
