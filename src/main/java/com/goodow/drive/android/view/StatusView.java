package com.goodow.drive.android.view;

import com.goodow.android.drive.R;
import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.settings.NetWorkListener;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.View;
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
  private NetWorkListener settingReceiver;
  private Context context = null;

  private final BroadcastReceiver timeTickreceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      // 分钟变化
      if (action.equals(Intent.ACTION_TIME_TICK)) {
        currentTime = getSystemTime();
        update();
      }
    }
  };

  private final MessageHandler<JsonObject> eventHandler = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject body = message.body();
      String action = body.getString("action");
      if (action != null && !"post".equalsIgnoreCase(action)) {
        return;
      }

      float netStrength = R.drawable.status_network_null;

      if (body.has(Constant.TYPE)) {
        netType = body.getString(Constant.TYPE);
        if (NetWorkListener.WIFI.equalsIgnoreCase(netType)) {
          netType = "WIFI ";
        } else if (NetWorkListener.TYPE_2G.equals(netType)
            || NetWorkListener.TYPE_3G.equals(netType) || NetWorkListener.TYPE_4G.equals(netType)) {
          netType = "3G ";
        }
        netStrength = (float) body.getNumber("strength");
        if (netStrength <= 0.0f) {
          netType = "无网络 ";
        }
      }
      if (netStrength <= 0.0f) {
        currentImageId = R.drawable.status_network_null;
      } else if (netStrength > 0.0f && netStrength <= 0.3f) {
        currentImageId = R.drawable.status_network_mid;
      } else if (netStrength > 0.3f && netStrength <= 1.0f) {
        currentImageId = R.drawable.status_network_all;
      }
      update();
    }
  };
  private HandlerRegistration controlhandler;

  public StatusView(Context context) {
    super(context);
    this.context = context;
  }

  public StatusView(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    View.inflate(context, R.layout.include_status, this);
    this.settingReceiver = new NetWorkListener(context);
    this.netTypeView = (TextView) findViewById(R.id.tv_status_netTypeView);
    this.netStatusView = (ImageView) findViewById(R.id.iv_status_netStatusView);
    this.currentTimeView = (TextView) findViewById(R.id.tv_status_currentTimeView);
    this.currentTime = this.getSystemTime();
    update();
  }

  public StatusView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    IntentFilter timeTickFilter = new IntentFilter(Intent.ACTION_TIME_TICK);
    this.context.registerReceiver(timeTickreceiver, timeTickFilter);
    this.settingReceiver.registerReceiver();
    controlhandler = bus.registerLocalHandler(NetWorkListener.ADDR, eventHandler);
    bus.sendLocal(NetWorkListener.ADDR, Json.createObject().set("action", "get"), eventHandler);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    this.settingReceiver.unRegisterReceiver();
    this.context.unregisterReceiver(timeTickreceiver);
    controlhandler.unregisterHandler();
  }

  /**
   * 获取当前时间
   * 
   * @return
   */
  private String getSystemTime() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat date = new SimpleDateFormat(" MM月dd日", Locale.CHINA);
    SimpleDateFormat time = new SimpleDateFormat(" hh:mm", Locale.CHINA);
    GregorianCalendar cal = new GregorianCalendar();
    String result =
        date.format(calendar.getTime()) + (cal.get(GregorianCalendar.AM_PM) == 0 ? " AM" : " PM")
            + time.format(calendar.getTime());
    return result;
  }

  private void update() {
    if (this.netTypeView != null) {
      this.netTypeView.setText(this.netType);
    }
    if (this.netStatusView != null) {
      this.netStatusView.setImageResource(this.currentImageId);
    }
    if (this.currentTimeView != null) {
      this.currentTimeView.setText(this.currentTime);
    }
  }
}