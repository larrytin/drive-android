package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.adapter.CommonPageAdapter;
import com.goodow.drive.android.view.FontTextView;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.ArrayList;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FavouriteActivity extends BaseActivity implements OnClickListener,
    OnPageChangeListener {

  private static final String LABEL_TAG = "tag";
  private static final String LABEL_ATTACHMENT = "attachment";
  private String currentLabel = LABEL_TAG;
  private ImageView iv_act_favour_back = null;
  private FontTextView ft_act_favour_item_activity = null;
  private FontTextView ft_act_favour_item_file = null;
  private ImageView iv_act_favour_result_pre = null;
  private ImageView iv_act_favour_result_next = null;
  private ViewPager vp_act_favour_result = null;
  private CommonPageAdapter myPageAdapter = null;
  private LinearLayout ll_act_favour_result_bar = null;
  private JsonArray activities = null;

  private final ArrayList<View> tempView = new ArrayList<View>();
  private final int numPerPage = 10; // 查询结果每页显示10条数据
  private final int numPerLine = 5; // 每条显示五个数据

  private int currentPageNum = 0;
  private int totalPageNum = 0;
  private LayoutInflater inflater = null;

  private HandlerRegistration registerPostHandler;

  private HandlerRegistration controlHandler;
  private HandlerRegistration deleteHandler;

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
    // 后退的点击事件
      case R.id.iv_act_favour_back:
        JsonObject msg = Json.createObject();
        msg.set("return", true);
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, msg, null);
        break;
      //
      case R.id.ft_act_favour_item_activity:
      case R.id.ft_act_favour_item_file:
        onLabelChange(v.getId());
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
    if (position == 0) {
      this.iv_act_favour_result_pre.setVisibility(View.INVISIBLE);
    } else {
      this.iv_act_favour_result_pre.setVisibility(View.VISIBLE);
    }
    if (position == totalPageNum - 1) {
      this.iv_act_favour_result_next.setVisibility(View.INVISIBLE);
    } else {
      this.iv_act_favour_result_next.setVisibility(View.VISIBLE);
    }

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
    this.sendQueryMessage(this.currentLabel);
  }

  @Override
  protected void onPause() {
    super.onPause();
    registerPostHandler.unregisterHandler();
    controlHandler.unregisterHandler();
    deleteHandler.unregisterHandler();
  }

  @Override
  protected void onResume() {
    super.onResume();
    registerPostHandler =
        bus.registerHandler(Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            String action = body.getString("action");
            // 仅仅处理action为post动作
            if (!"post".equalsIgnoreCase(action)) {
              return;
            }
            JsonObject queries = body.getObject(Constant.QUERIES);
            if (queries != null && queries.has(Constant.TYPE)
                && !Constant.DATAREGISTRY_TYPE_FAVOURITE.equals(queries.getString(Constant.TYPE))) {
              return;
            }
            activities = body.getArray("activities");
            bindDataToView(currentPageNum);
          }
        });
    controlHandler = bus.registerHandler(Constant.ADDR_CONTROL, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body.has("page")) {
          JsonObject page = body.getObject("page");
          if (page.has("goTo")) {
            vp_act_favour_result.setCurrentItem((int) page.getNumber("goTo"));
          } else if (page.has("move")) {
            int currentItem = vp_act_favour_result.getCurrentItem();
            vp_act_favour_result.setCurrentItem(currentItem + (int) page.getNumber("move"));
          }
        }
      }
    });
    deleteHandler =
        bus.registerHandler(Bus.LOCAL + Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            if (activities.length() == 0) {
              return;
            }
            if (!"ok".equals(body.getString("status"))) {
              // 数据库操作失败
              Toast.makeText(FavouriteActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
              return;
            }
            // 数据库操作成功
            int page = -1; // 当前页码
            JsonArray delActivities = body.getArray("activities");
            int delLen = delActivities.length();
            int index = -1;
            for (int i = 0; i < delLen; i++) {
              JsonObject delActivity = delActivities.getObject(i);
              int len = activities.length();
              // 查找相同的活动
              for (int j = 0; j < len; j++) {
                JsonObject activity = activities.get(j);

                JsonObject queries = activity.getObject(Constant.QUERIES);
                String type = queries.get(Constant.TYPE);
                String grade = queries.get(Constant.GRADE);
                String term = queries.get(Constant.TERM);
                String topic = queries.get(Constant.TOPIC);
                String title = activity.get(Constant.TITLE);

                JsonObject delQueries = delActivity.getObject(Constant.QUERIES);
                String delType = delQueries.get(Constant.TYPE);
                String delGrade = delQueries.get(Constant.GRADE);
                String delTerm = delQueries.get(Constant.TERM);
                String delTopic = delQueries.get(Constant.TOPIC);
                String delTitle = delActivity.get(Constant.TITLE);

                boolean isSameGrade =
                    (grade == null && delGrade == null)
                        || (grade != null && delGrade != null && grade.equals(delGrade));

                if ((type != null && type.equalsIgnoreCase(delType)) && isSameGrade
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
            if (page >= 0) {
              // 删除条件中有匹配的，滚动到最后一个匹配的条件所在的页面
              bindDataToView(page);
            } else {
              // 删除条件中没有匹配的，停留在当前页面
              bindDataToView(currentPageNum);
            }
          }
        });
  }

  /**
   * 把数据绑定到View对象
   * 
   * @param indexNum 初始化显示的页码
   */
  private void bindDataToView(int pageNum) {
    // 清空原有的结果
    this.ll_act_favour_result_bar.removeAllViews();
    this.vp_act_favour_result.removeAllViews();
    this.tempView.clear();

    // 如果数据是空就返回界面现实空白
    if (this.activities == null) {
      return;
    }

    int index = 0; // 下标计数器
    int counter = activities.length();
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
          LinearLayout.LayoutParams params =
              new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
          View view = null;
          if (this.currentLabel.equals(LABEL_TAG)) {
            view = this.buildTagItemView(this.activities.getObject(index), index, i);
            params.setMargins(10, 15, 10, 15);
          } else if (this.currentLabel.equals(LABEL_ATTACHMENT)) {
            view = this.buildAttachmentView(this.activities.getObject(index), index, i);
            params.setMargins(10, 5, 10, 5);
          }
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
    this.myPageAdapter = new CommonPageAdapter(this.tempView);
    this.vp_act_favour_result.setAdapter(this.myPageAdapter);

    if (this.totalPageNum > 1) {
      this.iv_act_favour_result_pre.setVisibility(View.INVISIBLE);
      this.iv_act_favour_result_next.setVisibility(View.VISIBLE);
    } else {
      this.iv_act_favour_result_pre.setVisibility(View.INVISIBLE);
      this.iv_act_favour_result_next.setVisibility(View.INVISIBLE);
    }

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
  private View buildAttachmentView(final JsonObject activity, final int index, int pageNum) {
    RelativeLayout itemContainer =
        (RelativeLayout) this.inflater.inflate(R.layout.activity_source_search_result_item, null);

    ImageView imageView =
        (ImageView) itemContainer.findViewById(R.id.iv_act_source_search_result_item_icon);
    imageView.setBackgroundResource(R.drawable.case_item_bg);

    String title = activity.getString(Constant.KEY_NAME);
    TextView textView =
        (TextView) itemContainer.findViewById(R.id.tv_act_source_search_result_item_filename);
    textView.setWidth(150);
    textView.setTextSize(16);
    textView.setMaxLines(2);
    textView.setGravity(Gravity.CENTER_HORIZONTAL);
    if (title.matches("^\\d{4}.*")) {
      textView.setText(title.substring(4, title.length()));
    } else {
      textView.setText(title);
    }

    RelativeLayout.LayoutParams params =
        new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    final ImageView imageViewFlag =
        (ImageView) itemContainer.findViewById(R.id.iv_act_source_search_result_item_flag);
    params.setMargins(100, 20, 0, 0);
    imageViewFlag.setLayoutParams(params);
    imageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (imageViewFlag.getVisibility() == View.VISIBLE) {
          imageViewFlag.setVisibility(View.INVISIBLE);
        } else {
          bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, Json.createObject().set("path",
              activity.getString(Constant.KEY_URL)), null);
        }
      }
    });

    imageView.setOnLongClickListener(new OnLongClickListener() {

      @Override
      public boolean onLongClick(View v) {
        if (imageViewFlag.getTag() == null
            || (!imageViewFlag.getTag().toString().equals("0") && !imageViewFlag.getTag()
                .toString().equals("1"))) {
          imageViewFlag.setBackgroundResource(R.drawable.favour_file_delete);
          imageViewFlag.setVisibility(View.VISIBLE);
          imageViewFlag.setTag("0");
        } else {
          imageViewFlag.setVisibility(View.INVISIBLE);
        }
        return true;
      }
    });

    imageViewFlag.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        JsonObject msg = Json.createObject();
        msg.set(Constant.KEY_ACTION, "delete");
        msg.set(Constant.KEY_STARS, Json.createArray().push(
            Json.createObject().set(Constant.KEY_TYPE, "attachment").set(Constant.KEY_KEY,
                activity.getString(Constant.KEY_ID))));
        bus.send(Bus.LOCAL + Constant.ADDR_TAG_STAR, msg, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            if ("ok".equalsIgnoreCase(body.getString(Constant.KEY_STATUS))) {
              Toast.makeText(FavouriteActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
              imageViewFlag.setBackgroundResource(R.drawable.source_favourited);
              imageViewFlag.setTag("1");
              activities.remove(index);
              bindDataToView(currentPageNum);
            } else {
              Toast.makeText(FavouriteActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
            }
          }
        });
      }
    });

    return itemContainer;
  }

  /**
   * 构建条目View对象
   * 
   * @param name 条目要显示的名称
   * @param index 条目的数据在数据集中的下标
   * @param pageNum 条目所处的页码
   * @return
   */
  private View buildTagItemView(final JsonObject activity, final int index, int pageNum) {
    RelativeLayout itemContainer = new RelativeLayout(this);
    itemContainer.setClickable(true);

    TextView textView = new TextView(this);
    textView.setId(index + 1);
    textView.setPadding(13, 10, 13, 0);
    textView.setTextColor(Color.BLACK);
    textView.setTextSize(18);
    textView.setMaxLines(2);
    JsonArray tags = Json.parse(activity.getString(Constant.KEY_TAG));
    String title = tags.getString(tags.length() - 1);
    if (title.matches("^\\d{4}.*")) {
      textView.setText(title.substring(4, title.length()));
    } else {
      textView.setText(title);
    }
    textView.setGravity(Gravity.CENTER_HORIZONTAL);
    RelativeLayout.LayoutParams textViewParams =
        new RelativeLayout.LayoutParams(120, LayoutParams.WRAP_CONTENT);
    textViewParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    textView.setLayoutParams(textViewParams);
    textView.setBackgroundResource(R.drawable.favour_selector_item);
    textView.setTag(activity);
    itemContainer.addView(textView);

    final ImageView imageView = new ImageView(this);
    RelativeLayout.LayoutParams imageViewParams =
        new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    imageView.setClickable(true);
    imageView.setBackgroundResource(R.drawable.favour_del);
    imageView.setVisibility(View.INVISIBLE);
    imageView.setTag(activity);
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
          JsonObject msg = Json.createObject();
          msg.set("action", "post");
          JsonObject activity = (JsonObject) ((TextView) v).getTag();
          msg.set("activity", activity);
          bus.send(Bus.LOCAL + Constant.ADDR_ACTIVITY, msg, null);
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
        JsonObject msg = Json.createObject();
        msg.set(Constant.KEY_ACTION, "delete");
        msg.set(Constant.KEY_STARS, Json.createArray().push(
            Json.createObject().set(Constant.KEY_TYPE, "tag").set(Constant.KEY_KEY,
                activity.getString(Constant.KEY_TAG))));
        bus.send(Bus.LOCAL + Constant.ADDR_TAG_STAR, msg, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            if ("ok".equalsIgnoreCase(body.getString(Constant.KEY_STATUS))) {
              Toast.makeText(FavouriteActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
              activities.remove(index);
              bindDataToView(currentPageNum);
            } else {
              Toast.makeText(FavouriteActivity.this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
            }
          }
        });
      }
    });

    return itemContainer;
  }

  /**
   * 初始化View
   */
  private void initView() {
    this.inflater = LayoutInflater.from(this);
    this.iv_act_favour_back = (ImageView) this.findViewById(R.id.iv_act_favour_back);
    this.iv_act_favour_back.setOnClickListener(this);
    this.ft_act_favour_item_activity =
        (FontTextView) this.findViewById(R.id.ft_act_favour_item_activity);
    this.ft_act_favour_item_activity.setOnClickListener(this);
    this.ft_act_favour_item_activity.setSelected(true);
    this.ft_act_favour_item_file = (FontTextView) this.findViewById(R.id.ft_act_favour_item_file);
    this.ft_act_favour_item_file.setOnClickListener(this);
    this.iv_act_favour_result_pre = (ImageView) this.findViewById(R.id.iv_act_favour_result_pre);
    this.iv_act_favour_result_pre.setOnClickListener(this);
    this.iv_act_favour_result_next = (ImageView) this.findViewById(R.id.iv_act_favour_result_next);
    this.iv_act_favour_result_next.setOnClickListener(this);
    this.vp_act_favour_result = (ViewPager) this.findViewById(R.id.vp_act_favour_result);
    this.vp_act_favour_result.setOnPageChangeListener(this);
    this.ll_act_favour_result_bar = (LinearLayout) this.findViewById(R.id.ll_act_favour_result_bar);
  }

  /**
   * 叶签切换的点击事件
   * 
   * @param id
   */
  private void onLabelChange(int id) {
    if (id == R.id.ft_act_favour_item_activity) {
      this.ft_act_favour_item_file.setSelected(false);
      this.ft_act_favour_item_activity.setSelected(true);
      this.currentLabel = LABEL_TAG;
    } else if (id == R.id.ft_act_favour_item_file) {
      this.ft_act_favour_item_file.setSelected(true);
      this.ft_act_favour_item_activity.setSelected(false);
      this.currentLabel = LABEL_ATTACHMENT;
      this.vp_act_favour_result.removeAllViews();
      this.ll_act_favour_result_bar.removeAllViews();
      this.iv_act_favour_result_pre.setVisibility(View.INVISIBLE);
      this.iv_act_favour_result_next.setVisibility(View.INVISIBLE);
    }
    this.sendQueryMessage(this.currentLabel);
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
    bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, msg, null);
  }

  /**
   * 构建查询的bus消息进行get查询
   */
  private void sendQueryMessage(String type) {
    JsonObject msg = Json.createObject();
    msg.set(Constant.KEY_TYPE, type);
    bus.send(Bus.LOCAL + Constant.ADDR_TAG_STAR_SEARCH, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        activities = (JsonArray) message.body();
        bindDataToView(currentPageNum);
      }
    });
  }

}
