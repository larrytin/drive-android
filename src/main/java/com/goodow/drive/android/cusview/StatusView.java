package com.goodow.drive.android.cusview;

import com.goodow.android.drive.R;
import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.settings.SettingReceiver;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 定义状态栏
 * 
 * @author dpw
 * 
 */
public class StatusView extends LinearLayout {

  private final Bus bus = BusProvider.get();
  private String netType = "";
  private int currentImageId = R.drawable.status_network_null;
  private String currentTime = "";
  private TextView netTypeView = null;
  private ImageView netStatusView = null;
  private TextView currentTimeView = null;
  SettingReceiver settingReceiver;

  private final MessageHandler<JsonObject> eventHandler = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      String action = body.getString("action");
      if (action != null && !"post".equalsIgnoreCase(action)) {
        return;
      }

      float netStrength = R.drawable.status_network_null;

      // 仅仅有wifi
      if (body.has("wifi") && !body.has("3g")) {
        netType = "WIFI";
        netStrength = (float) (body.getObject("wifi")).getNumber("strength");
        if (netStrength <= 0.0f) {
          netType = "无网络";
        }
      }

      // 仅仅有3g
      if (body.has("3g") && !body.has("wifi")) {
        netType = "3G";
        netStrength = (float) (body.getObject("3g")).getNumber("strength");
        if (netStrength <= 0.0f) {
          netType = "无网络";
        }
      }

      // 有wifi和3g
      if (body.has("3g") && body.has("wifi")) {
        float tempWifiStrength = (float) (body.getObject("wifi")).getNumber("strength");
        float temp3GStrength = (float) (body.getObject("3g")).getNumber("strength");
        if (tempWifiStrength > 0.0f) { // 只要wifi强度大于零就显示WIFI忽略3G
          netType = "WIFI";
          netStrength = tempWifiStrength;
        } else if (temp3GStrength > 0.0f) {// WIFI强度小于零3G大于零就显示3G
          netType = "3G";
          netStrength = temp3GStrength;
        } else {// 都小于零
          netType = "无网络";
          netStrength = 0.0f;
        }
      }

      if (netStrength <= 0.0f) {
        currentImageId = R.drawable.status_network_null;
      } else if (netStrength > 0.0f && netStrength <= 30.0f) {
        currentImageId = R.drawable.status_network_mid;
      } else if (netStrength > 30.0f && netStrength <= 100.0f) {
        currentImageId = R.drawable.status_network_all;
      }
      String tempCurrenttime = body.getString("time");
      if (tempCurrenttime != null) {
        currentTime = tempCurrenttime;
      }

      invalidate();
    }
  };

  public StatusView(Context context) {
    super(context);
  }

  public StatusView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.settingReceiver = new SettingReceiver(context);
    this.setPadding(10, 10, 10, 10);
    this.setOrientation(LinearLayout.HORIZONTAL);

    this.netTypeView = new TextView(context);
    this.netTypeView.setTextColor(Color.parseColor("#666666"));
    this.netTypeView.setText(netType);
    this.netTypeView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    this.netTypeView.setGravity(Gravity.CENTER);
    this.netTypeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
    LinearLayout.LayoutParams textViewParams =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    textViewParams.weight = 2;
    this.netTypeView.setLayoutParams(textViewParams);
    this.addView(netTypeView);

    this.netStatusView = new ImageView(context);
    this.netStatusView.setImageResource(R.drawable.status_network_null);
    LinearLayout.LayoutParams imageViewParams =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    imageViewParams.weight = 1;
    this.netStatusView.setLayoutParams(imageViewParams);
    this.addView(netStatusView);

    this.currentTimeView = new TextView(context);
    this.currentTimeView.setTextColor(Color.parseColor("#666666"));
    this.currentTimeView.setText(this.currentTime);
    this.currentTimeView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
    LinearLayout.LayoutParams timeViewParams =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    timeViewParams.weight = 3;
    this.currentTimeView.setLayoutParams(timeViewParams);
    this.currentTimeView.setGravity(Gravity.CENTER);
    this.currentTimeView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
    this.addView(currentTimeView);
  }

  public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    this.settingReceiver.registerReceiver();
    bus.registerHandler(SettingReceiver.ADDR, eventHandler);
    bus.send(Bus.LOCAL + SettingReceiver.ADDR, Json.createObject().set("action", "get"),
        eventHandler);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    this.settingReceiver.unRegisterReceiver();
    this.bus.unregisterHandler(SettingReceiver.ADDR, eventHandler);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (this.netTypeView != null) {
      this.netTypeView.setText(this.netType);
    }
    if (this.netStatusView != null) {
      this.netStatusView.setImageResource(this.currentImageId);
    }
    if (this.currentTimeView != null) {
      this.currentTimeView.setText(this.currentTime);
    }
    super.onDraw(canvas);
  }
}