package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.adapter.CommonPageAdapter;
import com.goodow.drive.android.toolutils.FileTools;
import com.goodow.drive.android.toolutils.FontUtil;
import com.goodow.drive.android.view.MarqueeTextView;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 活动详情
 * 
 * @author dpw
 * 
 */
public class BehaveActivity extends BaseActivity implements OnPageChangeListener, OnClickListener {

  private ImageView iv_act_behave_behaveite = null;
  private ImageView iv_act_behave_back = null;
  private TextView tv_act_behave_title = null;
  private String title = null;
  // 当前活动属性
  private JsonArray currentTags = null;

  private final int numPerPage = 14;// 查询结果每页显示12条数据
  private final int numPerLine = 7;// 每条显示6个数据

  private ViewPager vp_act_behave_result = null;
  private CommonPageAdapter myPageAdapter = null;

  // 翻页按钮
  private ImageView iv_act_behave_result_pre = null;
  private ImageView iv_act_behave_result_next = null;
  // 页码状态
  private LinearLayout ll_act_behave_result_bar = null;
  private int totalPageNum = 0;

  // 数据集
  private final ArrayList<View> nameViews = new ArrayList<View>();

  private HandlerRegistration postHandler;
  private HandlerRegistration controlHandler;
  public static final String USAGE_STATISTIC = "USAGE_STATISTIC";
  private SharedPreferences usagePreferences;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退
      case R.id.iv_act_behave_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;

      // 收藏
      case R.id.iv_act_behave_behaveite:
        JsonObject msg = Json.createObject();
        msg.set(Constant.KEY_ACTION, "post");
        msg.set(Constant.KEY_STAR, Json.createObject().set(Constant.KEY_TYPE, "tag").set(
            Constant.KEY_KEY, this.currentTags.toJsonString()));
        bus.send(Bus.LOCAL + Constant.ADDR_TAG_STAR, msg, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            if ("ok".equalsIgnoreCase(body.getString(Constant.KEY_STATUS))) {
              Toast.makeText(BehaveActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
              iv_act_behave_behaveite.setClickable(false);
              iv_act_behave_behaveite.setImageResource(R.drawable.behave_favourited);
            } else {
              Toast.makeText(BehaveActivity.this, "收藏失败，请重试", Toast.LENGTH_SHORT).show();
            }
          }
        });
        break;

      // 查询结果翻页
      case R.id.iv_act_behave_result_pre:
      case R.id.iv_act_behave_result_next:
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
      this.iv_act_behave_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.iv_act_behave_result_pre.setVisibility(View.VISIBLE);
    }
    if (position == totalPageNum - 1) {
      this.iv_act_behave_result_next.setVisibility(View.INVISIBLE);
    } else {
      this.iv_act_behave_result_next.setVisibility(View.VISIBLE);
    }

    for (int i = 0; i < this.ll_act_behave_result_bar.getChildCount(); i++) {
      if (position == i) {
        this.ll_act_behave_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_current);
      } else {
        this.ll_act_behave_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_other);
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_behave);
    this.initView();
    Bundle extras = this.getIntent().getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    if (msg.has(Constant.KEY_TITLE) && msg.getString(Constant.KEY_TITLE) != null) {
      this.title = msg.getString(Constant.KEY_TITLE);
      this.currentTags = msg.getArray(Constant.KEY_TAGS);
      this.sendQueryMessage();
    } else {
      Toast.makeText(this, "数据不完整，请重试", Toast.LENGTH_SHORT).show();
    }
    usagePreferences = getSharedPreferences(USAGE_STATISTIC, Context.MODE_PRIVATE);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    this.cleanHistory();
    Bundle extras = intent.getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    if (msg.has(Constant.KEY_TITLE) && msg.getString(Constant.KEY_TITLE) != null) {
      this.title = msg.getString(Constant.KEY_TITLE);
      this.currentTags = msg.getArray(Constant.KEY_TAGS);
      this.sendQueryMessage();
    } else {
      Toast.makeText(this, "数据不完整，请重试", Toast.LENGTH_SHORT).show();
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
    postHandler = bus.registerHandler(Constant.ADDR_ACTIVITY, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString("action");
        // 仅仅处理action为null或post动作
        if (!"post".equalsIgnoreCase(action)) {
          return;
        }
        currentTags = body.getArray(Constant.KEY_TAGS);
        if (currentTags == null) {
          Toast.makeText(BehaveActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
          return;
        }
        for (int i = 0; i < currentTags.length(); i++) {
          if (Constant.LABEL_THEMES.contains(currentTags.getString(i))) {
            return;
          }
        }
        if (body.has(Constant.KEY_TITLE) && body.getString(Constant.KEY_TITLE) != null) {
          title = body.getString(Constant.KEY_TITLE);
          sendQueryMessage();
        } else {
          Toast.makeText(BehaveActivity.this, "数据不完整，请重试", Toast.LENGTH_SHORT).show();
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
            vp_act_behave_result.setCurrentItem((int) page.getNumber("goTo"));
          } else if (page.has("move")) {
            int currentItem = vp_act_behave_result.getCurrentItem();
            vp_act_behave_result.setCurrentItem(currentItem + (int) page.getNumber("move"));
          }
        }
      }
    });
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView(JsonArray attachments) {
    if (this.title != null && this.title.matches("^\\d{4}.*")) {
      this.tv_act_behave_title.setText(this.title.substring(4, this.title.length()));
    } else {
      this.tv_act_behave_title.setText(this.title);
    }
    if (attachments.length() > 0) {
      this.iv_act_behave_behaveite.setVisibility(View.VISIBLE);
    }
    this.ll_act_behave_result_bar.removeAllViews();
    this.vp_act_behave_result.removeAllViews();
    this.nameViews.clear();

    int index = 0;// 下标计数器
    int counter = attachments.length();
    // 页码数量
    totalPageNum =
        (counter % this.numPerPage == 0) ? (counter / numPerPage) : (counter / this.numPerPage + 1);
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
        // 使第二行和第一行之间有距离
        if (j == 1) {
          LinearLayout.LayoutParams params =
              new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
          params.setMargins(0, getResources().getDimensionPixelSize(
              R.dimen.act_behave_result_item_margin), 0, 0);
          innerContainer.setLayoutParams(params);
        }
        // 列数量
        for (int k = 0; k < numPerLine; k++) {
          if (index >= counter) {
            break;
          }
          // 构建ItemView对象
          View view = buildItemView(index, attachments.getObject(index));
          view.setClickable(true);
          innerContainer.addView(view);
          index++;
        }
        rootContainer.addView(innerContainer);
      }
      this.nameViews.add(rootContainer);
      ImageView imageView = new ImageView(this);
      LayoutParams layoutParams =
          new LayoutParams(getResources().getDimensionPixelSize(R.dimen.common_result_dot_width),
              getResources().getDimensionPixelSize(R.dimen.common_result_dot_height));
      imageView.setLayoutParams(layoutParams);
      if (i == 0) {
        imageView.setBackgroundResource(R.drawable.common_result_dot_current);
      } else {
        imageView.setBackgroundResource(R.drawable.common_result_dot_other);
      }
      this.ll_act_behave_result_bar.addView(imageView);
    }
    this.myPageAdapter = new CommonPageAdapter(this.nameViews);
    this.vp_act_behave_result.setAdapter(this.myPageAdapter);

    if (this.totalPageNum > 1) {
      this.iv_act_behave_result_pre.setVisibility(View.INVISIBLE);
      this.iv_act_behave_result_next.setVisibility(View.VISIBLE);
    } else {
      this.iv_act_behave_result_pre.setVisibility(View.INVISIBLE);
      this.iv_act_behave_result_next.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * 构建子View
   * 
   * @param index
   * @return
   */
  private View buildItemView(int index, JsonObject attachment) {
    String fileName = attachment.getString(Constant.KEY_NAME);
    final String filePath = attachment.getString(Constant.KEY_URL);
    final String attachmentId = attachment.getString(Constant.KEY_ID);
    String thumbnail = attachment.getString(Constant.KEY_ATTACHMENT);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(
            R.dimen.act_behave_result_item_width), getResources().getDimensionPixelSize(
            R.dimen.act_behave_result_item_height));
    params.setMargins(10, 18, 10, 18);
    LinearLayout itemLayout = new LinearLayout(this);
    itemLayout.setLayoutParams(params);
    itemLayout.setOrientation(LinearLayout.VERTICAL);

    LinearLayout.LayoutParams itemImageViewParams2 =
        new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(
            R.dimen.act_behave_result_item_image_width), getResources().getDimensionPixelSize(
            R.dimen.act_behave_result_item_image_height));
    itemImageViewParams2.gravity = Gravity.CENTER_HORIZONTAL;
    ImageView itemImageView = new ImageView(this);
    itemImageView.setClickable(true);
    itemImageView.setLayoutParams(itemImageViewParams2);
    itemLayout.addView(itemImageView);

    itemImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, Json.createObject().set("path", filePath).set(
            "play", 1), null);
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

    TextView textView = new MarqueeTextView(this);
    LayoutParams layoutParams =
        new LayoutParams(getResources().getDimensionPixelSize(
            R.dimen.act_behave_result_item_text_width), LayoutParams.WRAP_CONTENT);
    textView.setLayoutParams(layoutParams);
    textView.setTextSize(getResources().getDimensionPixelSize(
        R.dimen.act_behave_result_item_textSize));
    textView.setGravity(Gravity.CENTER_HORIZONTAL);
    textView.setTextColor(Color.WHITE);
    textView.setSingleLine(true);
    textView.setFocusable(true);
    textView.setEllipsize(TruncateAt.MARQUEE);
    textView.setMarqueeRepeatLimit(-1);
    itemLayout.addView(textView);
    itemLayout.setTag(filePath);
    FileTools.setImageThumbnalilUrl(itemImageView, filePath, thumbnail);
    textView.setText(fileName);
    return itemLayout;
  }

  /**
   * 界面以及数据归零
   */
  private void cleanHistory() {
    this.tv_act_behave_title.setText(null);
    this.iv_act_behave_behaveite.setVisibility(View.INVISIBLE);
    this.vp_act_behave_result.removeAllViews();
    this.iv_act_behave_result_next.setVisibility(View.INVISIBLE);
    this.iv_act_behave_result_pre.setVisibility(View.INVISIBLE);
    this.totalPageNum = 0;
    this.nameViews.clear();
  }

  /**
   * 初始化View对象 设置点击事件 设置光标事件监听 添加到对应集合
   */
  private void initView() {
    this.iv_act_behave_behaveite = (ImageView) this.findViewById(R.id.iv_act_behave_behaveite);
    this.iv_act_behave_behaveite.setOnClickListener(this);
    this.iv_act_behave_back = (ImageView) this.findViewById(R.id.iv_act_behave_back);
    this.iv_act_behave_back.setOnClickListener(this);
    this.tv_act_behave_title = (TextView) this.findViewById(R.id.tv_act_behave_title);
    this.tv_act_behave_title.setTypeface(FontUtil.getInstance(this).getTypeFace());

    this.iv_act_behave_result_pre = (ImageView) this.findViewById(R.id.iv_act_behave_result_pre);
    this.iv_act_behave_result_pre.setOnClickListener(this);
    this.iv_act_behave_result_next = (ImageView) this.findViewById(R.id.iv_act_behave_result_next);
    this.iv_act_behave_result_next.setOnClickListener(this);
    this.vp_act_behave_result = (ViewPager) this.findViewById(R.id.vp_act_behave_result);
    this.vp_act_behave_result.setOnPageChangeListener(this);

    this.ll_act_behave_result_bar = (LinearLayout) this.findViewById(R.id.ll_act_behave_result_bar);

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
    if (id == R.id.iv_act_behave_result_pre) {
      page.set("move", -1);
    } else if (id == R.id.iv_act_behave_result_next) {
      page.set("move", 1);
    }
    bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, msg, null);
  }

  /**
   * 构建查询的bus消息,查询是否已经收藏
   */
  private void sendQueryIsHeadMessage() {
    JsonObject msg = Json.createObject();
    msg.set("action", "get");
    msg.set(Constant.KEY_STAR, Json.createObject().set(Constant.KEY_TYPE, "tag").set(
        Constant.KEY_KEY, this.currentTags.toJsonString()));
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_STAR, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          iv_act_behave_behaveite.setClickable(false);
          iv_act_behave_behaveite.setImageResource(R.drawable.behave_favourited);
        } else {
          iv_act_behave_behaveite.setClickable(true);
          iv_act_behave_behaveite.setImageResource(R.drawable.behave_favourite);
        }
      }
    });
  }

  /**
   * 构建查询的bus消息
   */
  private void sendQueryMessage() {
    JsonObject msg = Json.createObject();
    msg.set(Constant.KEY_TAGS, this.currentTags);
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_ATTACHMENT_SEARCH, msg,
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            JsonArray attachments = body.getArray(Constant.KEY_ATTACHMENTS);
            bindDataToView(attachments);
            sendQueryIsHeadMessage();
          }
        });
  }
}
