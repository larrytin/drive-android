package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.adapter.CommonPageAdapter;
import com.goodow.drive.android.toolutils.FileTools;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class EbookActivity extends BaseActivity implements OnPageChangeListener, OnClickListener {
  // 后退收藏锁屏
  private ImageView iv_act_ebook_back = null;
  private ImageView iv_act_ebook_coll = null;
  private ImageView iv_act_ebook_loc = null;

  private final int numPerPage = 8;// 查询结果每页显示8条数据
  private final int numPerLine = 4;// 每条显示四个数据

  private ViewPager vp_act_ebook_result = null;
  private CommonPageAdapter myPageAdapter = null;
  // 翻页按钮
  private ImageView rl_act_ebook_result_pre = null;
  private ImageView rl_act_ebook_result_next = null;
  // 页码状态
  private LinearLayout ll_act_ebook_result_bar = null;
  private int totalPageNum = 0;

  private final ArrayList<View> nameViews = new ArrayList<View>();

  private HandlerRegistration postHandler;
  private HandlerRegistration controlHandler;
  private HandlerRegistration refreshHandler;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退 收藏 锁屏
      case R.id.iv_act_ebook_back:
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_ebook_coll:
        this.bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set(
            Constant.KEY_REDIRECTTO, "favorite"), null);
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
  public void onPageScrollStateChanged(int state) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onPageSelected(int position) {
    if (position == 0) {
      this.rl_act_ebook_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_ebook_result_pre.setVisibility(View.VISIBLE);
    }
    if (position == totalPageNum - 1) {
      this.rl_act_ebook_result_next.setVisibility(View.INVISIBLE);
    } else {
      this.rl_act_ebook_result_next.setVisibility(View.VISIBLE);
    }

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
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_ebook);
    this.initView();
    Bundle extras = this.getIntent().getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    Bundle extras = intent.getExtras();
    JsonObject msg = (JsonObject) extras.get("msg");
    JsonArray tags = msg.getArray(Constant.KEY_TAGS);
    this.sendQueryMessage(this.buildTags(tags));
  }

  @Override
  protected void onPause() {
    super.onPause();
    postHandler.unregisterHandler();
    controlHandler.unregisterHandler();
    refreshHandler.unregisterHandler();
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
        JsonArray tags = body.getArray(Constant.KEY_TAGS);
        if (tags == null) {
          Toast.makeText(EbookActivity.this, "数据不完整，请检查确认后重试", Toast.LENGTH_SHORT).show();
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
    controlHandler = bus.registerHandler(Constant.ADDR_CONTROL, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body.has("page")) {
          JsonObject page = body.getObject("page");
          if (page.has("goTo")) {
            vp_act_ebook_result.setCurrentItem((int) page.getNumber("goTo"));
          } else if (page.has("move")) {
            int currentItem = vp_act_ebook_result.getCurrentItem();
            vp_act_ebook_result.setCurrentItem(currentItem + (int) page.getNumber("move"));
          }
        }
      }
    });

    refreshHandler =
        bus.registerHandler(Constant.ADDR_VIEW_REFRESH, new MessageHandler<JsonObject>() {
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
    this.ll_act_ebook_result_bar.removeAllViews();
    this.vp_act_ebook_result.removeAllViews();
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
          View view = buildItemView(index, attachments.getObject(index));
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
      this.ll_act_ebook_result_bar.addView(imageView);
    }
    this.myPageAdapter = new CommonPageAdapter(this.nameViews);
    this.vp_act_ebook_result.setAdapter(this.myPageAdapter);

    if (this.totalPageNum > 1) {
      this.rl_act_ebook_result_pre.setVisibility(View.INVISIBLE);
      this.rl_act_ebook_result_next.setVisibility(View.VISIBLE);
    } else {
      this.rl_act_ebook_result_pre.setVisibility(View.INVISIBLE);
      this.rl_act_ebook_result_next.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * 构建子View
   * 
   * @param index
   * @return
   */
  private View buildItemView(int index, final JsonObject attachment) {
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(getResources().getDimensionPixelOffset(
            R.dimen.act_read_result_item_width), getResources().getDimensionPixelOffset(
            R.dimen.act_read_result_item_height));
    params.setMargins(22, 5, 22, 18);
    LinearLayout itemLayout = new LinearLayout(this);
    itemLayout.setLayoutParams(params);
    itemLayout.setClickable(true);
    itemLayout.setOrientation(LinearLayout.VERTICAL);

    RelativeLayout.LayoutParams itemImageViewParams2 =
        new RelativeLayout.LayoutParams(getResources().getDimensionPixelOffset(
            R.dimen.act_read_result_item_image_width), getResources().getDimensionPixelOffset(
            R.dimen.act_read_result_item_image_height));
    itemImageViewParams2.addRule(RelativeLayout.CENTER_HORIZONTAL);
    ImageView itemImageView = new ImageView(this);
    FileTools.setImageThumbnalilUrl(itemImageView, attachment.getString(Constant.KEY_URL),
        attachment.getString(Constant.KEY_THUMBNAIL));
    itemImageView.setLayoutParams(itemImageViewParams2);
    itemLayout.addView(itemImageView);

    TextView textView = new TextView(this);
    textView.setWidth(150);
    textView.setTextSize(getResources().getDimensionPixelOffset(
        R.dimen.act_read_result_item_textSize));
    textView.setMaxLines(2);
    textView.setGravity(Gravity.CENTER_HORIZONTAL);
    String title = attachment.getString(Constant.KEY_NAME);
    textView.setText(title);
    itemLayout.addView(textView);
    itemLayout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, Json.createObject().set("path",
            attachment.getString(Constant.KEY_URL)), null);
      }
    });

    return itemLayout;
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
    // 后退 收藏 所屏
    this.iv_act_ebook_back = (ImageView) this.findViewById(R.id.iv_act_ebook_back);
    this.iv_act_ebook_coll = (ImageView) this.findViewById(R.id.iv_act_ebook_coll);
    this.iv_act_ebook_loc = (ImageView) this.findViewById(R.id.iv_act_ebook_loc);
    this.iv_act_ebook_back.setOnClickListener(this);
    this.iv_act_ebook_coll.setOnClickListener(this);
    this.iv_act_ebook_loc.setOnClickListener(this);

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
    bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, msg, null);
  }

  /**
   * 构建查询的bus消息
   */
  private void sendQueryMessage(JsonArray tags) {
    JsonObject msg = Json.createObject();
    if (tags != null) {
      msg.set(Constant.KEY_TAGS, tags);
    } else {
      msg.set(Constant.KEY_TAGS, Json.createArray().push(Constant.DATAREGISTRY_TYPE_READ));
    }
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
