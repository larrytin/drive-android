package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FavouriteActivity extends BaseActivity implements OnClickListener,
    OnPageChangeListener {

  /**
   * 自定义条目删除ImageView
   * 
   * @author dpw
   * 
   */
  private class MyImageView extends ImageView {
    private int index; // 要删除的ID

    public MyImageView(Context context, int index) {
      super(context);
      this.index = index;
      this.setClickable(true);
      this.setBackgroundResource(R.drawable.favour_del);
      this.setVisibility(View.INVISIBLE);
    }

    public int getIndex() {
      return index;
    }
  }
  /**
   * 收藏的条目适配器
   * 
   * @author dpw
   * 
   */
  private class MyPageAdapter extends PagerAdapter {
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
      if (position >= tempView.size()) {
        return;
      }
      ((ViewPager) container).removeView(tempView.get(position));
    }

    @Override
    public int getCount() {
      if (tempView == null) {
        return 0;
      } else {
        return tempView.size();
      }
    }

    @Override
    public int getItemPosition(Object object) {
      return POSITION_NONE;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
      View view = tempView.get(position);
      container.addView(view);
      return view;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
      return arg0 == arg1;
    }
  }

  public static final String ADDR_CONTROL = BusProvider.SID + "view.control";

  private final MessageHandler<JsonObject> eventHandlerControl = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      if (body.has("previous") && body.getBoolean("previous")) {
        vp_act_favour_result.arrowScroll(View.FOCUS_LEFT);
      } else if (body.has("next") && body.getBoolean("next")) {
        vp_act_favour_result.arrowScroll(View.FOCUS_RIGHT);
      }
    }
  };

  private final MessageHandler<JsonObject> eventHandler = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      String action = body.getString("action");
      // 仅仅处理action为null或post动作或delete动作
      if (action != null && !"post".equalsIgnoreCase(action) && !"delete".equalsIgnoreCase(action)) {
        return;
      }
      JsonObject query = body.getObject("query");
      if (query != null && query.has("type") && !"收藏".equals(query.getString("type"))) {
        return;
      }

      int page = -1; // 当前页码
      if (action == null || "post".equalsIgnoreCase(action)) {
        activities = body.getArray("activities");
      } else if ("delete".equalsIgnoreCase(action)) {
        if (activities == null) {
          return;
        }
        JsonArray delActivities = body.getArray("activities");
        int delLen = delActivities.length();
        int index = -1;
        for (int i = 0; i < delLen; i++) {
          JsonObject delActivity = delActivities.getObject(i);
          int len = activities.length();
          // 查找相同的活动
          for (int j = 0; j < len; j++) {
            JsonObject activity = activities.get(j);

            JsonObject tag = activity.getObject(Constant.TAGS);
            String grade = tag.get(Constant.GRADE);
            String term = tag.get(Constant.TERM);
            String topic = tag.get(Constant.TOPIC);
            String title = activity.get(Constant.TITLE);

            JsonObject delTag = delActivity.getObject(Constant.TAGS);
            String delGrade = delTag.get(Constant.GRADE);
            String delTerm = delTag.get(Constant.TERM);
            String delTopic = delTag.get(Constant.TOPIC);
            String delTitle = delActivity.get(Constant.TITLE);

            if ((grade != null && grade.equalsIgnoreCase(delGrade))
                && (term != null && term.equalsIgnoreCase(delTerm))
                && (topic != null && topic.equalsIgnoreCase(delTopic))
                && (title != null && title.equalsIgnoreCase(delTitle))) {
              index = j;
              break;
            }
          }
          // 在删除条件匹配的情况下执行
          if (index >= 0) {
            activities.remove(index);
          }
        }
        // 在删除条件匹配的情况下执行
        if (index >= 0) {
          page = index / numPerPage;
        }
      }
      if (page >= 0) {
        // 删除条件中有匹配的，滚动到最后一个匹配的条件所在的页面
        bindDataToView(page);
      } else {
        // 删除条件中没有匹配的，停留在当前页面
        bindDataToView(currentPageNum);
      }
    }
  };

  private ImageView iv_act_favour_back = null;
  private ImageView iv_act_favour_result_pre = null;
  private ImageView iv_act_favour_result_next = null;
  private ViewPager vp_act_favour_result = null;
  private MyPageAdapter myPageAdapter = null;
  private LinearLayout ll_act_favour_result_bar = null;
  private JsonArray activities = null;

  private ArrayList<View> tempView = new ArrayList<View>();
  private final int numPerPage = 15; // 查询结果每页显示15条数据
  private final int numPerLine = 5; // 每条显示五个数据

  private int currentPageNum = 0;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退的点击事件
      case R.id.iv_act_favour_back:
        JsonObject msg = Json.createObject();
        msg.set("return", true);
        bus.send(Bus.LOCAL + BaseActivity.CONTROL, msg, null);
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

  @Override
  public void onPageScrolled(int arg0, float arg1, int arg2) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onPageScrollStateChanged(int arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void onPageSelected(int position) {
    this.currentPageNum = position;
    for (int i = 0; i < this.ll_act_favour_result_bar.getChildCount(); i++) {
      if (position == i) {
        this.ll_act_favour_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_current);
      } else {
        this.ll_act_favour_result_bar.getChildAt(i).setBackgroundResource(
            R.drawable.common_result_dot_other);
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_favourite);
    this.initView();
    this.bindDataToView(0);
    Bundle extras = this.getIntent().getExtras();
    JsonObject body = (JsonObject) extras.get("msg");
    JsonArray activities = body.getArray("activities");
    if (activities == null) {
      this.sendQueryMessage();
    } else {
      this.activities = activities;
      bindDataToView(0);
    }
  }

  @Override
  protected void onPause() {
    bus.unregisterHandler(Constant.ADDR_TOPIC, eventHandler);
    bus.unregisterHandler(ADDR_CONTROL, eventHandlerControl);
    super.onPause();
  }

  @Override
  protected void onResume() {
    bus.registerHandler(Constant.ADDR_TOPIC, eventHandler);
    bus.registerHandler(ADDR_CONTROL, eventHandlerControl);
    super.onResume();
  }

  /**
   * 把数据绑定到View对象
   * 
   * @param indexNum 初始化显示的页码
   */
  private void bindDataToView(int pageNum) {
    if (this.activities == null) {
      return;
    }
    this.ll_act_favour_result_bar.removeAllViews();
    this.vp_act_favour_result.removeAllViews();
    this.tempView.clear();

    int index = 0; // 下标计数器
    int counter = activities.length();
    int times =
        (counter % this.numPerPage == 0) ? (counter / numPerPage) : (counter / this.numPerPage + 1);
    // 页码数量
    for (int i = 0; i < times; i++) {
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
          View view = this.buildItemView(this.activities.getObject(index), index, i);
          LinearLayout.LayoutParams params =
              new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
          params.setMargins(10, 15, 10, 15);
          view.setLayoutParams(params);
          innerContainer.addView(view);
          index++;
        }
        rootContainer.addView(innerContainer);
      }
      this.tempView.add(rootContainer);
      // 构建页码栏
      ImageView imageView = new ImageView(this);
      if (i == 0) {
        imageView.setBackgroundResource(R.drawable.common_result_dot_current);
      } else {
        imageView.setBackgroundResource(R.drawable.common_result_dot_other);
      }
      this.ll_act_favour_result_bar.addView(imageView);
    }
    this.myPageAdapter = new MyPageAdapter();
    this.vp_act_favour_result.setAdapter(this.myPageAdapter);
    this.vp_act_favour_result.setCurrentItem(pageNum);
  }

  /**
   * 构建条目View对象
   * 
   * @param name 条目要显示的名称
   * @param index 条目的数据在数据集中的下标
   * @param pageNum 条目所处的页码
   * @return
   */
  private View buildItemView(JsonObject activity, int index, int pageNum) {
    RelativeLayout itemContainer = new RelativeLayout(this);
    itemContainer.setClickable(true);

    TextView textView = new TextView(this);
    textView.setId(index + 1);
    textView.setPadding(0, 10, 0, 0);
    textView.setTextColor(Color.BLACK);
    textView.setText(activity.getString(Constant.TITLE));
    textView.setGravity(Gravity.CENTER_HORIZONTAL);
    RelativeLayout.LayoutParams textViewParams =
        new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    textViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    textView.setLayoutParams(textViewParams);
    textView.setBackgroundResource(R.drawable.favour_selector_item);
    itemContainer.addView(textView);

    final MyImageView imageView = new MyImageView(this, index);
    RelativeLayout.LayoutParams imageViewParams =
        new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    imageViewParams.addRule(RelativeLayout.RIGHT_OF, index + 1); // 要和TextView的ID保持一直
    imageViewParams.addRule(RelativeLayout.CENTER_IN_PARENT);
    imageView.setLayoutParams(imageViewParams);
    itemContainer.addView(imageView);

    /*
     * 点击事件 取消删除状态或打开一个活动
     */
    textView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        TextView texViewTemp = (TextView) v;
        if (texViewTemp.isSelected()) {
          texViewTemp.setSelected(false);
          imageView.setVisibility(View.INVISIBLE);
        } else {
          Toast.makeText(FavouriteActivity.this, "open", Toast.LENGTH_SHORT).show();
        }
      }
    });

    /*
     * 长按事件 唤出删除状态或取消删除状态
     */
    textView.setOnLongClickListener(new OnLongClickListener() {

      @Override
      public boolean onLongClick(View v) {
        TextView texViewTemp = (TextView) v;
        if (texViewTemp.isSelected()) {
          texViewTemp.setSelected(false);
          imageView.setVisibility(View.INVISIBLE);
        } else {
          texViewTemp.setSelected(true);
          imageView.setVisibility(View.VISIBLE);
        }
        return true;
      }
    });

    /*
     * 删除点击事件
     */
    imageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        MyImageView tempView = (MyImageView) v;
        JsonObject msg = Json.createObject();
        msg.set("action", "delete");
        JsonArray delActivities = Json.createArray();
        delActivities.insert(0, activities.get(tempView.getIndex()));
        msg.set("activities", delActivities);
        bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, eventHandler);
      }
    });

    return itemContainer;
  }

  /**
   * 初始化View
   */
  private void initView() {
    this.iv_act_favour_back = (ImageView) this.findViewById(R.id.iv_act_favour_back);
    this.iv_act_favour_back.setOnClickListener(this);
    this.iv_act_favour_result_pre = (ImageView) this.findViewById(R.id.iv_act_favour_result_pre);
    this.iv_act_favour_result_pre.setOnClickListener(this);
    this.iv_act_favour_result_next = (ImageView) this.findViewById(R.id.iv_act_favour_result_next);
    this.iv_act_favour_result_next.setOnClickListener(this);
    this.vp_act_favour_result = (ViewPager) this.findViewById(R.id.vp_act_favour_result);
    this.vp_act_favour_result.setOnPageChangeListener(this);
    this.ll_act_favour_result_bar = (LinearLayout) this.findViewById(R.id.ll_act_favour_result_bar);
  }

  /**
   * 翻页的点击事件
   * 
   * @param id
   */
  private void onResultPrePageClick(int id) {
    JsonObject msg = Json.createObject();
    if (id == R.id.iv_act_favour_result_pre) {
      msg.set("previous", true);
    } else if (id == R.id.iv_act_favour_result_next) {
      msg.set("next", true);
    }
    bus.send(Bus.LOCAL + ADDR_CONTROL, msg, eventHandlerControl);
  }

  /**
   * 构建查询的bus消息
   */
  private void sendQueryMessage() {
    JsonObject msg = Json.createObject();
    msg.set("action", "get");
    JsonObject type = Json.createObject();
    type.set("type", "收藏");
    msg.set("query", type);
    bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, eventHandler);
  }

}
