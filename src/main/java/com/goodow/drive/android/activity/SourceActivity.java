package com.goodow.drive.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;
import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.toolutils.FileTools;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.Registration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import java.util.*;
import java.util.Map.Entry;

/**
 * 资源库
 * 
 * @author dpw
 * 
 */
public class SourceActivity extends BaseActivity implements OnClickListener {

  /**
   * 二级类别点事件调用
   * 
   * @param
   */
  private class OnSubCatagoryClick implements OnClickListener {
    @Override
    public void onClick(View v) {
      cleanSearchResult();
      int tagIndex = Integer.parseInt(v.getTag(R.id.tag_first).toString());
      boolean isLast = Boolean.parseBoolean(v.getTag(R.id.tag_second).toString());
      int selfIndex = Integer.parseInt(v.getTag(R.id.tag_third).toString());
      int parentStartY = Integer.parseInt(v.getTag(R.id.tag_fourth).toString());
        for(int i=tagIndex;i<subTags.size();i++){
          subTags.remove(i);
          i--;
      }
      boolean isCancle = false;
      if (v.isSelected()) {
          RadioGroup parent = (RadioGroup)v.getParent();
          int childCount = parent.getChildCount();
          for(int i=0;i<childCount;i++){
              parent.getChildAt(i).setSelected(false);
          }
          v.setSelected(false);
          isCancle = true;
      } else {
          RadioGroup parent = (RadioGroup)v.getParent();
          int childCount = parent.getChildCount();
          for(int i=0;i<childCount;i++){
              parent.getChildAt(i).setSelected(false);
          }
          v.setSelected(true);
          subTags.add(((RadioButton) v).getText().toString());
      }
      setKeyList();
      currentindex = tagIndex;
      if(!isLast){
          bindSubTagToView(currentindex,parentStartY,selfIndex,isCancle);
      }
    }
  }

  private TextView tv_act_source_keys_list = null;
  public void setKeyList(){
      StringBuffer buffer = new StringBuffer();
      for(String string:subTags){
          buffer.append(string + " + ");
      }
      buffer.deleteCharAt(buffer.lastIndexOf("+"));
      this.tv_act_source_keys_list.setText(buffer.toString());
  }

  /**
   * 结果适配器
   * 
   * @author dpw
   * 
   */
  private class ResultAdapter extends BaseAdapter {

    private Context context = null;
    private JsonArray attachments = null;

    public ResultAdapter(Context context) {
      this.context = context;
    }

    @Override
    public int getCount() {
      if (this.attachments != null) {
        return this.attachments.length();
      }
      return 0;
    }

    @Override
    public Object getItem(int position) {
      if (this.attachments != null) {
        return this.attachments.getObject(position);
      }
      return null;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View itemView = null;
      if (convertView != null) {
        itemView = convertView;
      } else {
        itemView =
            LayoutInflater.from(this.context).inflate(R.layout.activity_source_search_result_item,
                null);
      }
      final JsonObject attachment = this.attachments.getObject(position);
      String fileName = attachment.getString(Constant.KEY_NAME);
      final String attachmentId = attachment.getString(Constant.KEY_ID);
      ImageView imageView =
          (ImageView) itemView.findViewById(R.id.iv_act_source_search_result_item_icon);
      FileTools.setImageThumbnalilUrl(imageView, attachment.getString(Constant.KEY_URL), attachment
          .getString(Constant.KEY_THUMBNAIL));
      TextView textView =
          (TextView) itemView.findViewById(R.id.tv_act_source_search_result_item_filename);
      textView.setText(fileName);
      final ImageView imageViewFlag =
          (ImageView) itemView.findViewById(R.id.iv_act_source_search_result_item_flag);
      imageViewFlag.setVisibility(View.INVISIBLE);
      sendQuryTagStar(imageViewFlag, attachment.getString(Constant.KEY_ID));
      imageView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          // 打开文件
          bus.sendLocal(Constant.ADDR_PLAYER, Json.createObject().set("path",
              attachment.getString(Constant.KEY_URL)).set("play", 1), null);
          Editor editor = usagePreferences.edit();
          editor.putString("tmpFileName", attachmentId);
          editor.putLong("tmpOpenTime", System.currentTimeMillis());
          editor.putLong("tmpSystemLast", SystemClock.uptimeMillis());
          editor.commit();
        }
      });

      imageView.setOnLongClickListener(new OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
          if (imageViewFlag.getTag() == null
              || (!status_unstar.equals(imageViewFlag.getTag().toString()) && !status_stared
                  .equals(imageViewFlag.getTag().toString()))) {
            // 0处于已点击未收藏 1处于已经收藏 非0非1处于原始状态
            imageViewFlag.setBackgroundResource(R.drawable.source_favourite);
            imageViewFlag.setVisibility(View.VISIBLE);
            imageViewFlag.setTag(status_unstar);
          }
          return true;
        }
      });

      imageViewFlag.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          if (imageViewFlag.getTag().toString().equals(status_unstar)) {
            // 0处于已点击未收藏
            JsonObject msg = Json.createObject();
            msg.set(Constant.KEY_ACTION, "post");
            msg.set(Constant.KEY_STAR, Json.createObject().set(Constant.KEY_TYPE, "attachment")
                .set(Constant.KEY_KEY, attachment.getString(Constant.KEY_ID)));
            bus.sendLocal(Constant.ADDR_TAG_STAR, msg, new MessageHandler<JsonObject>() {
              @Override
              public void handle(Message<JsonObject> message) {
                JsonObject body = message.body();
                if ("ok".equalsIgnoreCase(body.getString(Constant.KEY_STATUS))) {
                  Toast.makeText(SourceActivity.this, "收藏成功", Toast.LENGTH_SHORT).show();
                  imageViewFlag.setBackgroundResource(R.drawable.source_favourited);
                  imageViewFlag.setTag(status_stared);
                } else {
                  Toast.makeText(SourceActivity.this, "收藏失败，请重试", Toast.LENGTH_SHORT).show();
                }
              }
            });
          }
        }
      });
      return itemView;
    }

    public void reset(JsonArray attachments) {
      this.attachments = attachments;
    }
  }

  private final static String status_unstar = "0";

  private final static String status_stared = "1";
  private TextView tv_act_source_tip = null;
  private ImageView iv_act_source_result_pre = null;
  private GridView gr_act_source_result = null;
  private ResultAdapter resultAdapter = null;
  private ImageView iv_act_source_result_next = null;
  private int totalAttachmentNum = 0;// 总的数据量

  private int currentPageNum = 0;// 当前页码
  private final int numPerPage = 10;// 查询结果每页显示10条数据

  private ProgressBar pb_act_source_search_progress = null;

  private LinearLayout ll_act_source_catagory0 = null;
  private LinearLayout ll_act_source_catagory1 = null;
  private EditText et_act_source_tags = null;
  private ImageView iv_act_source_search_button = null;
  // 当前的contentType对应的ID
  private String currentContentType = null;// 搜索一级标签

  private final List<String> subTags = new ArrayList<String>();// 根据一级标签查询得到的二级标签
  private JsonArray queryingTags = null;// 控制台传递的混合标签
  private Registration postHandler;
  private Registration controlHandler;
  private static final Map<Object, String> idContentTypes = new HashMap<Object, String>();
  private static final Map<Object, String> idTags = new HashMap<Object, String>();

  private RelativeLayout ll_act_source_sub_catagorys = null;

  private final static String CATAGORY0_ALL = "全部";
  private final static String CATAGORY0_TEXT = "文本";
  private final static String CATAGORY0_PIC = "图片";
  private final static String CATAGORY0_ANIM = "动画";
  private final static String CATAGORY0_VIDEO = "视频";
  private final static String CATAGORY0_AUDIO = "音频";
  private final static String CATAGORY0_EBOOK = "电子书";
  private static JsonObject root = null;
  private CheckBox cb_act_source_subcat_status0 = null;
  private CheckBox cb_act_source_subcat_status1 = null;

  private int currentindex = 0;
  private int preItemHeight = 25;
  private Stack<JsonArray> subCatagoryStack = new Stack<JsonArray>();
  static {
    idContentTypes.put(R.id.iv_act_source_catagory0_all, "全部");
    idContentTypes.put(R.id.iv_act_source_catagory0_text, "application/pdf");
    idContentTypes.put(R.id.iv_act_source_catagory0_image, "image/jpeg");
    idContentTypes.put(R.id.iv_act_source_catagory0_animation, "application/x-shockwave-flash");
    idContentTypes.put(R.id.iv_act_source_catagory0_video, "video/mp4");
    idContentTypes.put(R.id.iv_act_source_catagory0_audio, "audio/mpeg");
    idContentTypes.put(R.id.iv_act_source_catagory0_ebook, "application/x-shockwave-flash");

    idTags.put(R.id.iv_act_source_catagory0_all, CATAGORY0_ALL);
    idTags.put(R.id.iv_act_source_catagory0_text, CATAGORY0_TEXT);
    idTags.put(R.id.iv_act_source_catagory0_image, CATAGORY0_PIC);
    idTags.put(R.id.iv_act_source_catagory0_animation, CATAGORY0_ANIM);
    idTags.put(R.id.iv_act_source_catagory0_video, CATAGORY0_VIDEO);
    idTags.put(R.id.iv_act_source_catagory0_audio, CATAGORY0_AUDIO);
    idTags.put(R.id.iv_act_source_catagory0_ebook, CATAGORY0_EBOOK);


    JsonArray grades = Json.createArray().push("托班").push("小班").push("中班").push("大班").push("学前班");
    JsonObject topic = Json.createObject().set("健康", grades).set("语言", grades).set("社会", grades).set("科学", grades).set("数学", grades).set("艺术(音乐)",grades).set("艺术(美术)",grades);
    JsonArray topic_of_pic_sub1 = Json.createArray().push("文学艺术").push("人物").push("动物").push("植物").push("物品").push("食品").push("交通工具").push("风景名胜").push("公共设施")
            .push("节庆及活动").push("科学技术").push("自然现象").push("数学操作材料").push("其他");
    JsonArray topic_of_pic_sub23 = Json.createArray().push("健康").push("语言").push("社会").push("科学").push("数学").push("艺术(音乐)").push("艺术(美术)");
    JsonArray topic_of_pic_sub4_array = Json.createArray().push("动物").push("植物").push("人物").push("其他");
    JsonObject TEXT_SUB = Json.createObject().set("活动设计", topic).set("文学作品", null).set("乐谱",null);
    JsonObject PIC_SUB = Json.createObject().set("实物图与绘制图", topic_of_pic_sub1).set("挂图", topic_of_pic_sub23).set("参考图", topic_of_pic_sub23).set("轮廓图", null)
            .set("头饰",topic_of_pic_sub4_array).set("手偶",topic_of_pic_sub4_array).set("胸牌",topic_of_pic_sub4_array);
    JsonArray ANIM_SUB = Json.createArray().push("文学作品动画").push("音乐作品动画").push("演示动画").push("数学教学动画");
    JsonArray topic_of_video_sub1 = Json.createArray().push("动物").push("植物").push("自然风光");
    JsonArray topic_of_video_sub2 = Json.createArray().push("舞蹈").push("歌表演").push("音乐游戏").push("打击乐器演奏").push("基本舞步示范");
    JsonObject VIDEO_SUB = Json.createObject().set("教学视频", topic_of_video_sub1).set("音乐表演视频", topic_of_video_sub2).set("教学示范课", topic).set("动物模仿动作",null);
    JsonArray AUDIO_SUB = Json.createArray().push("文学作品音频").push("音乐作品音频").push("音效");
    JsonArray EBOOK_SUB = Json.createArray().push("托班").push("安全教育");

    root = Json.createObject().set("文本",TEXT_SUB).set("图片",PIC_SUB).set("动画",ANIM_SUB).set("视频",VIDEO_SUB).set("音频",AUDIO_SUB).set("电子书", EBOOK_SUB);
  }

    @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.iv_act_source_back:
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
        break;
      case R.id.iv_act_source_coll:
        bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
            "favorite"), null);
        break;
      case R.id.iv_act_source_loc:
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("brightness", 0), null);
        break;
      case R.id.iv_act_source_catagory0_all:
      case R.id.iv_act_source_catagory0_text:
      case R.id.iv_act_source_catagory0_image:
      case R.id.iv_act_source_catagory0_animation:
      case R.id.iv_act_source_catagory0_audio:
      case R.id.iv_act_source_catagory0_video:
      case R.id.iv_act_source_catagory0_ebook:
        this.onContentTypeClick(v.getId());
        break;
      case R.id.iv_act_source_search_button:
        this.onSearchButtonClick(v.getId());
        break;
      case R.id.iv_act_source_result_pre:
      case R.id.iv_act_source_result_next:
        this.onResultPrePageClick(v.getId());
        break;
        case R.id.cb_act_source_subcat_status0:
        case R.id.cb_act_source_subcat_status1:
            if(v.isSelected()){
                this.cb_act_source_subcat_status0.setSelected(false);
                this.cb_act_source_subcat_status1.setSelected(false);
                this.ll_act_source_sub_catagorys.setVisibility(View.GONE);
            }else{
                this.cb_act_source_subcat_status0.setSelected(true);
                this.cb_act_source_subcat_status1.setSelected(true);
                this.ll_act_source_sub_catagorys.setVisibility(View.VISIBLE);
            }
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
      if (idTags.containsValue(tag)) {
        this.currentContentType = idContentTypes.get(tag);
        echoContentType(tag);
      }
    }
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
    postHandler.unregister();
    controlHandler.unregister();
  }

  @Override
  protected void onResume() {
    super.onResume();
    postHandler = bus.subscribeLocal(Constant.ADDR_TOPIC, new MessageHandler<JsonObject>() {
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
    controlHandler =
        bus.subscribeLocal(Constant.ADDR_CONTROL, new MessageHandler<JsonObject>() {
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
              sendQueryMessage(currentContentType, subTags, et_act_source_tags.getText().toString().trim());
            }
          }
        });
  }

  /**
   * 把查询完成的结果绑定到结果View
   */
  private void bindDataToView(JsonArray attachments) {
    this.pb_act_source_search_progress.setVisibility(View.INVISIBLE);
    if (attachments == null || attachments.length() == 0) {
      this.tv_act_source_tip.setVisibility(View.VISIBLE);
      this.tv_act_source_tip.setText(Html.fromHtml(this.getString(R.string.string_source_tip1)));
    } else {
      this.iv_act_source_result_pre.setClickable(true);
      this.iv_act_source_result_next.setClickable(true);
      this.gr_act_source_result.setVisibility(View.VISIBLE);
      this.tv_act_source_tip.setText(null);
    }
    this.resultAdapter.reset(attachments);
    this.resultAdapter.notifyDataSetChanged();

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
   * @param
   */
  private void bindSubTagToView(int parentIndex,int parentStartY,int selfIndex,boolean isCancle){
      for(int i=this.ll_act_source_sub_catagorys.getChildCount() - 1; i > currentindex - 1 ; i --){
          this.ll_act_source_sub_catagorys.removeViewAt(i);
      }
      if(isCancle){
          return;
      }
      JsonObject tempObject = root;
      JsonObject jsonObject = null;
      JsonArray jsonArray = null;
      boolean isLast = false;
      boolean isNull = false;
      for(String tag:subTags){
          Object subObject = tempObject.get(tag);
          if(subObject == null){
             isNull = true;
             break;
          }
          if(subObject instanceof JsonObject){
              jsonArray = null;
              jsonObject = (JsonObject)subObject;
              isLast = false;
          }else if(subObject instanceof JsonArray){
              jsonObject = null;
              jsonArray = (JsonArray)subObject;
              isLast = true;
          }
          tempObject = jsonObject;
      }
      if(isNull){
          return;
      }
      JsonArray keys = null;
      if(jsonObject != null){
          keys = jsonObject.keys();
      }else if(jsonArray != null){
          keys = jsonArray;
      }
      ScrollView scrollView = new ScrollView(this);
      RadioGroup radioGroup = new RadioGroup(this);
      if(parentIndex == 0){
          radioGroup.setBackgroundResource(R.drawable.source_hierarchy_sub_0);
      }else if(parentIndex == 1){
          radioGroup.setBackgroundResource(R.drawable.source_hierarchy_sub_1);
      }else if(parentIndex == 2){
          radioGroup.setBackgroundResource(R.drawable.source_hierarchy_sub_2);
      }

      LayoutParams layoutParams = new LayoutParams(210, ViewGroup.LayoutParams.WRAP_CONTENT);
      if(parentIndex == 0){
          layoutParams.setMargins(parentIndex * 200, selfIndex * preItemHeight,0,0);
      }else{
          layoutParams.setMargins(parentIndex * 190, parentStartY  + selfIndex * preItemHeight,0,0);
      }
      scrollView.setLayoutParams(layoutParams);
      radioGroup.setOrientation(RadioGroup.VERTICAL);
      for(int i=0;i<keys.length();i++){
          RadioButton radioButton = (RadioButton) this.getLayoutInflater().inflate(R.layout.soruce_item_sub_search,null);
          radioButton.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50));
          radioButton.setText(keys.get(i).toString());
          if(parentIndex == 0 && isLast == false){
              radioButton.setBackgroundResource(R.drawable.source_selector_hierarchy_sub_0_item);
              radioButton.setTextColor(Color.parseColor("#FFFFFF"));
          }
          if(parentIndex == 1 && isLast == false){
              radioButton.setBackgroundResource(R.drawable.source_selector_hierarchy_sub_1_item);
          }
          if(isLast){
              radioButton.setButtonDrawable(R.drawable.source_selector_arrow);
              radioButton.setTextColor(Color.parseColor("#000000"));
          }
          radioButton.setTag(R.id.tag_first, (parentIndex + 1));
          radioButton.setTag(R.id.tag_second,isLast);
          radioButton.setTag(R.id.tag_third,i);
          radioButton.setTag(R.id.tag_fourth,parentStartY + selfIndex * preItemHeight);
          radioButton.setOnClickListener(new OnSubCatagoryClick());
          radioGroup.addView(radioButton);
      }
      scrollView.addView(radioGroup);
      this.ll_act_source_sub_catagorys.addView(scrollView);
  }

  private void bindSubTagToView(JsonArray tags) {
    subCatagoryStack.push(tags);
    this.ll_act_source_sub_catagorys.removeAllViews();
    for(JsonArray tag:subCatagoryStack){
        int len = tag.length();
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.HORIZONTAL);
        //LinearLayout innerSubCatagory = new LinearLayout(this);
        //innerSubCatagory.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        //innerSubCatagory.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < len; i++) {
            String tagString = tag.getString(i);
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(tagString);
            radioButton.setButtonDrawable(R.drawable.source_selector_catagory1);
            //DrawableLeftTextView view = new DrawableLeftTextView(SourceActivity.this);
            //view.setTag(index);
            //TextView child = (TextView) view.findViewById(R.id.tv_subtag);
            //child.setText(tags.getString(i));
            //view.setOnClickListener(new OnSubCatagoryClick());
            radioButton.setOnClickListener(new OnSubCatagoryClick());
            // 查询得到的二级标签中是否包含了控制台传递过来的二级标签
            for (int j = 0; this.queryingTags != null && j < this.queryingTags.length(); j++) {
                String queryingTag = this.queryingTags.getString(j);
                if (tag.equals(queryingTag)) {
                    this.subTags.add(tagString);
                   // view.setSelected(true);
                    break;
                }
            }
            LayoutParams layoutParams =
                    new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(getResources().getDimensionPixelOffset(
                    R.dimen.act_source_search_subtag_margin), 0, getResources().getDimensionPixelOffset(
                    R.dimen.act_source_search_subtag_margin), 0);
            //view.setLayoutParams(layoutParams);
            //innerSubCatagory.addView(view);
            radioGroup.addView(radioButton);
        }
       //this.ll_act_source_sub_catagorys.addView(innerSubCatagory);
        this.ll_act_source_sub_catagorys.addView(radioGroup);
    }
  }

  /**
   * 清空查询结果
   */
  private void cleanSearchResult() {
    this.iv_act_source_result_pre.setVisibility(View.INVISIBLE);
    this.iv_act_source_result_next.setVisibility(View.INVISIBLE);
    this.et_act_source_tags.setText(null);
    totalAttachmentNum = 0;
    currentPageNum = 0;
    resultAdapter.reset(null);
    resultAdapter.notifyDataSetChanged();
  }

  /**
   * 回显contentType
   * 
   * @param
   */
  private void echoContentType(String tag) {
    Set<Entry<Object, String>> entrySet = idTags.entrySet();
    for (Entry<Object, String> entry : entrySet) {
      if (entry.getKey() instanceof Integer && entry.getValue().equals(tag)) {
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
    this.gr_act_source_result = (GridView) this.findViewById(R.id.gr_act_source_result);
    this.resultAdapter = new ResultAdapter(this);
    this.gr_act_source_result.setAdapter(this.resultAdapter);
    this.iv_act_source_result_next = (ImageView) this.findViewById(R.id.iv_act_source_result_next);
    this.iv_act_source_result_next.setOnClickListener(this);

    this.tv_act_source_tip = (TextView) this.findViewById(R.id.tv_act_source_tip);
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
    this.ll_act_source_sub_catagorys = (RelativeLayout)this.findViewById(R.id.ll_act_source_sub_catagorys);
    this.tv_act_source_keys_list = (TextView)this.findViewById(R.id.tv_act_source_keys_list);
    this.cb_act_source_subcat_status0 = (CheckBox) this.findViewById(R.id.cb_act_source_subcat_status0);
    this.cb_act_source_subcat_status1 = (CheckBox) this.findViewById(R.id.cb_act_source_subcat_status1);
    this.cb_act_source_subcat_status0.setSelected(true);
    this.cb_act_source_subcat_status1.setSelected(true);
    this.cb_act_source_subcat_status0.setOnClickListener(this);
    this.cb_act_source_subcat_status1.setOnClickListener(this);

  }


  /**
   * 一级类别点事件调用
   * 
   * @param id
   */
  private void onContentTypeClick(int id) {
    this.subTags.clear();
    currentindex = 0;
    this.subCatagoryStack.clear();
    this.subTags.add(idTags.get(id));
    ll_act_source_catagory1.removeAllViews();
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
        //this.sendQuerySubCatagory(Json.createArray().push(this.subTags.get(0)));
        bindSubTagToView(0,30,0,false);
      }

      if(id == R.id.iv_act_source_catagory0_all){
          this.ll_act_source_sub_catagorys.removeAllViews();
          this.cb_act_source_subcat_status0.setSelected(false);
          this.cb_act_source_subcat_status1.setSelected(false);
          this.ll_act_source_sub_catagorys.setVisibility(View.GONE);
      }else{
          this.cb_act_source_subcat_status0.setSelected(true);
          this.cb_act_source_subcat_status1.setSelected(true);
          this.ll_act_source_sub_catagorys.setVisibility(View.VISIBLE);
      }
    }
    setKeyList();
  }

  /**
   * 处理上下翻页的点击事件
   * 
   * @param id
   */
  private void onResultPrePageClick(int id) {
    this.iv_act_source_result_pre.setClickable(false);
    this.iv_act_source_result_next.setClickable(false);
    this.pb_act_source_search_progress.setVisibility(View.VISIBLE);
    if (id == R.id.iv_act_source_result_pre && this.currentPageNum >= 0) {
      // 向前翻页
      this.currentPageNum--;
    } else if (id == R.id.iv_act_source_result_next
        && this.currentPageNum <= this.totalAttachmentNum) {
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
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    this.totalAttachmentNum = 0;
    this.currentPageNum = 0;
    if (this.currentContentType == null || this.et_act_source_tags.getText().toString() == null) {
      Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
      tv_act_source_tip.startAnimation(shake);// 抖动提醒.
      // Toast.makeText(this, this.getString(R.string.string_source_tip0),
      // Toast.LENGTH_SHORT).show();
    } else {
      tv_act_source_tip.setVisibility(View.INVISIBLE);
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
    bus.sendLocal(Constant.ADDR_TAG_ATTACHMENT_SEARCH, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        totalAttachmentNum = (int) body.getNumber(Constant.KEY_COUNT);
        JsonArray attachments = body.getArray(Constant.KEY_ATTACHMENTS);
        bindDataToView(attachments);
      }
    });
  }

  /**
   * 构建查询的bus消息查询子级分类
   */
  private void sendQuerySubCatagory(JsonArray tags) {
    JsonObject msg =
        Json.createObject().set(Constant.KEY_TAGS, tags);
    msg.set(Constant.KEY_FROM, 0);
    msg.set(Constant.KEY_SIZE, 20);// 假设每个一级搜索下的标签不多于20
    bus.sendLocal(Constant.ADDR_TAG_CHILDREN, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        JsonArray tags = body.getArray(Constant.KEY_TAGS);
        bindSubTagToView(tags);
      }
    });
  }
  private void sendQuerySubCatagory(List<String> list) {
    JsonArray tags = Json.createArray();
    for(String string:list){
        tags.push(string);
    }
    this.sendQuerySubCatagory(tags);
  }
  private void sendQuerySubCatagory(String string) {
    JsonArray tags = Json.createArray();
    tags.push(string);
    this.sendQuerySubCatagory(tags);
  }

  /**
   * 查询文件收藏信息
   * 
   * @param starView
   * @param params
   */
  private void sendQuryTagStar(final View starView, String params) {
    JsonObject msg = Json.createObject();
    msg.set("action", "get");
    msg.set(Constant.KEY_STAR, Json.createObject().set(Constant.KEY_TYPE, "attachment").set(
        Constant.KEY_KEY, params));
    bus.sendLocal(Constant.ADDR_TAG_STAR, msg, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        if (body != null) {
          starView.setVisibility(View.VISIBLE);
          starView.setBackgroundResource(R.drawable.source_favourited);
          starView.setTag(status_stared);
        } else {
          starView.setVisibility(View.GONE);
          starView.setTag(null);
        }
      }
    });
  }
}
