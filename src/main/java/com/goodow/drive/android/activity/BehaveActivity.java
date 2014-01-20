package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.adapter.CommonPageAdapter;
import com.goodow.drive.android.data.DataProvider;
import com.goodow.drive.android.toolutils.FontUtil;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.io.File;
import java.util.ArrayList;

import android.graphics.Color;
import android.net.Uri;
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

  // 当前活动属性
  private JsonObject activity = null;
  // 数据集
  private JsonArray files = null;
  private final ArrayList<View> nameViews = new ArrayList<View>();

  private HandlerRegistration postHandler;
  private HandlerRegistration controlHandler;

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
        msg.set("action", "put");
        JsonArray activities = Json.createArray();
        activities.insert(0, this.activity);
        msg.set("activities", activities);
        bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            if ("ok".equalsIgnoreCase(body.getString("status"))) {
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
    JsonObject body = (JsonObject) extras.get("msg");
    this.activity = body.getObject("activity");
    this.files = body.getArray("files");
    if (files == null) {
      this.sendQueryMessage();
    } else {
      this.bindDataToView();
      this.sendQueryIsHeadMessage();
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
        dataHandler(body);
      }
    });
    controlHandler =
        bus.registerHandler(Constant.ADDR_VIEW_CONTROL, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            if (body.has("previous") && body.getBoolean("previous")) {
              vp_act_behave_result.arrowScroll(View.FOCUS_LEFT);
            } else if (body.has("next") && body.getBoolean("next")) {
              vp_act_behave_result.arrowScroll(View.FOCUS_RIGHT);
            }
          }
        });
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView() {
    String title = activity.getString(Constant.TITLE);
    if (title.matches("^\\d{4}.*")) {
      this.tv_act_behave_title.setText(title.substring(4, title.length()));
    } else {
      this.tv_act_behave_title.setText(title);
    }

    if (this.files == null) {
      return;
    }

    this.ll_act_behave_result_bar.removeAllViews();
    this.vp_act_behave_result.removeAllViews();
    this.nameViews.clear();

    int index = 0;// 下标计数器
    int counter = files.length();
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
          params.setMargins(0, 48, 0, 0);
          innerContainer.setLayoutParams(params);
        }
        // 列数量
        for (int k = 0; k < numPerLine; k++) {
          if (index >= counter) {
            break;
          }
          // 构建ItemView对象
          View view = buildItemView(index);
          LinearLayout.LayoutParams params =
              new LinearLayout.LayoutParams(120, LayoutParams.WRAP_CONTENT);
          params.setMargins(10, 5, 10, 18);
          view.setLayoutParams(params);
          view.setClickable(true);

          view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              String path = v.getTag().toString();
              bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, Json.createObject().set("path", path).set(
                  "play", 1), null);
            }
          });
          innerContainer.addView(view);
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
  private View buildItemView(int index) {
    JsonObject file = this.files.getObject(index);
    String fileName = file.getString(Constant.FILE_NAME);
    String filePath = file.getString(Constant.FILE_PATH);
    LinearLayout.LayoutParams params =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    LinearLayout itemLayout = new LinearLayout(this);
    itemLayout.setLayoutParams(params);
    itemLayout.setClickable(true);
    itemLayout.setOrientation(LinearLayout.VERTICAL);

    RelativeLayout.LayoutParams itemImageViewParams2 =
        new RelativeLayout.LayoutParams(120, LayoutParams.WRAP_CONTENT);
    itemImageViewParams2.addRule(RelativeLayout.CENTER_HORIZONTAL);
    ImageView itemImageView = new ImageView(this);
    itemImageView.setLayoutParams(itemImageViewParams2);
    itemLayout.addView(itemImageView);

    TextView textView = new TextView(this);
    textView.setWidth(220);
    textView.setTextSize(16);
    textView.setGravity(Gravity.CENTER_HORIZONTAL);
    textView.setTextColor(Color.WHITE);
    textView.setSingleLine(true);
    textView.setMaxEms(10);
    itemLayout.addView(textView);
    itemLayout.setTag(filePath);
    this.setThumbnailsImage(itemImageView, fileName);
    if (fileName.matches("^\\d{4}.*")) {
      textView.setText(fileName.substring(4, fileName.length()));
    } else {
      textView.setText(fileName);
    }
    return itemLayout;
  }

  /**
   * 数据解析
   * 
   * @param body
   */
  private void dataHandler(JsonObject body) {
    JsonObject tempActivity = body.getObject("activity");
    if (tempActivity != null) {
      activity = tempActivity;
    }
    files = body.getArray("files");
    bindDataToView();
    sendQueryIsHeadMessage();
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
    if (id == R.id.iv_act_behave_result_pre) {
      msg.set("previous", true);
    } else if (id == R.id.iv_act_behave_result_next) {
      msg.set("next", true);
    }
    bus.send(Bus.LOCAL + Constant.ADDR_VIEW_CONTROL, msg, null);
  }

  /**
   * 构建查询的bus消息,查询是否已经收藏
   */
  private void sendQueryIsHeadMessage() {
    JsonObject msg = Json.createObject();
    msg.set("action", "head");
    msg.set("activity", activity);
    bus.send(Bus.LOCAL + Constant.ADDR_ACTIVITY, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if ("ok".equalsIgnoreCase(body.getString("status"))) {
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
    msg.set("action", "get");
    msg.set("activity", activity);
    bus.send(Bus.LOCAL + Constant.ADDR_ACTIVITY, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        dataHandler(body);
      }
    });
  }

  /**
   * 给item设置缩略图
   * 
   * @param view
   * @param name
   */
  private void setThumbnailsImage(ImageView view, String name) {
    JsonObject tags = this.activity.getObject(Constant.TAGS);
    // 构建缩略图路径
    String filePath =
        DataProvider.getInstance().getPath(tags) + this.activity.getString(Constant.TITLE)
            + "/缩略图/" + name + ".png";
    // 判断指定的缩略图是否存在
    if (new File(filePath).exists()) {
      // 加载存在的缩略图
      view.setImageURI(Uri.parse(filePath));
    } else {
      // 加载默认的缩略图
      if (name.endsWith(".mp3")) {
        view.setImageResource(R.drawable.behave_mp3);
      } else if (name.endsWith(".mp4")) {
        view.setImageResource(R.drawable.behave_mp4);
      } else if (name.endsWith(".swf")) {
        view.setImageResource(R.drawable.behave_flash);
      } else if (name.endsWith(".pdf")) {
        view.setImageResource(R.drawable.behave_ebook);
      } else if (name.endsWith(".jpg")) {
        view.setImageResource(R.drawable.behave_image);
      }
    }
  }

}
