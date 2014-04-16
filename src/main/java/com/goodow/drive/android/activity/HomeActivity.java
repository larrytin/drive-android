package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.data.DBOperator;
import com.goodow.drive.android.data.DataRegistry;
import com.goodow.drive.android.player.PlayerRegistry;
import com.goodow.drive.android.settings.BaiduLocation;
import com.goodow.drive.android.settings.NetWorkListener;
import com.goodow.drive.android.settings.SettingsRegistry;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.State;
import com.goodow.realtime.core.Handler;
import com.goodow.realtime.core.HandlerRegistration;
import com.goodow.realtime.core.Platform;
import com.goodow.realtime.java.JavaWebSocket;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;

import com.baidu.location.LocationClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class HomeActivity extends BaseActivity {
  static {
    Logger.getLogger(JavaWebSocket.class.getName()).setLevel(Level.ALL);
  }
  public static final String TAG = HomeActivity.class.getSimpleName();
  private static final String DATABASENAME = "keruixing";
  private static boolean registried;
  private HandlerRegistration openHandlerReg;
  private HandlerRegistration netWorkHandlerReg;
  private int schedulePeriodic;
  public static final String AUTH = "AuthImformation";
  private SharedPreferences authSp = null;
  // 联网状态为1
  private int flag = 0;
  // 记录注册OnOpen状态
  private boolean registeredOnOpen = false;
  // 记录注册网络监听状态
  private boolean registeredNetWork = false;
  private ConnectivityManager mConnectivityManager;
  private NetworkInfo networkInfo;
  private LocationClient mLocationClient;

  private boolean registeredOnOpen1 = false;

  private HandlerRegistration openHandlerReg1;

  private boolean ConnectStatus = true;
  // 1分钟 TODO:
  private int periodicTime = 60000;
  private int schPeriodicTime;

  // 标记
  private boolean unConnect = false;

  // 4天校驗一次 TODO:
  private int number = 1;

  // TODO:如果为false，不校验，默认不校验
  private boolean openAuth = false;

  private int updateBoot;

  public void onClick(View v) {

    switch (v.getId()) {
    // 收藏
      case R.id.iv_act_main_coll:
        if (openAuth) {
          checkActivate(Json.createObject().set(Constant.KEY_REDIRECTTO, "favorite"), Bus.LOCAL
              + Constant.ADDR_VIEW);
        } else {
          this.bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set(
              Constant.KEY_REDIRECTTO, "favorite"), null);
        }
        break;
      // 锁屏
      case R.id.iv_act_main_loc:
        JsonObject brightness = Json.createObject();
        brightness.set("brightness", 0);
        this.bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, brightness, null);
        break;
      // 设置
      case R.id.iv_act_main_set:
        this.bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set(
            Constant.KEY_REDIRECTTO, "settings"), null);
        break;
      // 关机
      case R.id.iv_act_main_clo:
        JsonObject shutdown = Json.createObject();
        shutdown.set("shutdown", 0);
        this.bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, shutdown, null);
        break;
      // 和谐
      case R.id.iv_act_main_har:
        this.open("和谐");
        break;
      // 托班
      case R.id.iv_act_main_ship:
        this.open("托班");
        break;
      // 示范课
      case R.id.iv_act_main_tech:
        this.open("示范课");
        break;
      // 入学准备
      case R.id.iv_act_main_pre:
        this.open("入学准备");
        break;
      // 智能开发
      case R.id.iv_act_main_edu:
        this.open("安全教育");
        break;
      // 图画书
      case R.id.iv_act_main_read:
        this.open("早期阅读");
        break;
      // 资源库
      case R.id.iv_act_main_source:
        if (openAuth) {
          checkActivate(Json.createObject().set(Constant.KEY_REDIRECTTO, "repository"), Bus.LOCAL
              + Constant.ADDR_VIEW);
        } else {
          bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
              "repository"), null);
        }
        break;

      default:
        break;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_home);
    authSp = this.getSharedPreferences(AUTH, Context.MODE_PRIVATE);
    subscribe();
    sendAnalyticsMessage();
    // 每隔1天,发送一次数据到服务器 TODO:发布时修改时间为一天
    schedulePeriodic = Platform.scheduler().schedulePeriodic(1 * 60 * 1000, new Handler<Void>() {
      @Override
      public void handle(Void event) {
        sendAnalyticsMessage();
        if (openAuth) {
          number--;
          if (number == 0) {
            sendAuth();
            number = 1;// TODO:修改几天校验一次
          }
        }
      }
    });
    updateBoot = Platform.scheduler().schedulePeriodic(60000, new Handler<Void>() {
      @Override
      public void handle(Void event) {
        DBOperator.updateBoot(HomeActivity.this, "T_BOOT", "LAST_TIME", "CLOSE_TIME", Json
            .createObject().set("LAST_TIME", SystemClock.uptimeMillis()).set("CLOSE_TIME",
                System.currentTimeMillis()));
      }
    });
    BaiduLocation.INSTANCE.setContext(getApplicationContext());
    mLocationClient = BaiduLocation.INSTANCE.getLocationClient();
    BaiduLocation.INSTANCE.init();
    // 数据库打包，将数据库放到asset目录下即可，数据库名为：keruixing TODO：数据库
    copyDataBases();
    if (openAuth) {
      checkAuth();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    netWorkHandlerReg.unregisterHandler();
    Platform.scheduler().cancelTimer(schedulePeriodic);
    Platform.scheduler().cancelTimer(updateBoot);
    mLocationClient.stop();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (registeredNetWork) {
      return;
    }
    registeredNetWork = true;
    // 监听网络变化
    netWorkHandlerReg = bus.registerHandler(NetWorkListener.ADDR, new MessageHandler<JsonObject>() {
      @Override
      public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        String action = body.getString("action");
        if (action != null && !"post".equalsIgnoreCase(action)) {
          return;
        }
        float netStrength = (float) body.getNumber("strength");
        if (netStrength <= 0.0f) {
          // 标记
          if (openAuth && !unConnect) {
            unConnect = true;
            // 断网,10分钟后，不让用户使用；
            schPeriodicTime = Platform.scheduler().scheduleDelay(periodicTime, new Handler<Void>() {
              @Override
              public void handle(Void event) {
                ConnectStatus = false;
                ComponentName component =
                    ((ActivityManager) HomeActivity.this.getSystemService(Context.ACTIVITY_SERVICE))
                        .getRunningTasks(1).get(0).topActivity;
                String topClassName = component.getClassName();
                if (!"com.goodow.drive.android.activity.HomeActivity".equals(topClassName)
                    && !"com.goodow.drive.android.activity.NotificationActivity"
                        .equals(topClassName)) {
                  bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set("redirectTo",
                      "home"), null);
                }
                if (!"com.goodow.drive.android.activity.NotificationActivity".equals(topClassName)) {
                  bus.send(Bus.LOCAL + BusProvider.SID + "notification", Json.createObject().set(
                      "content", "您无法继续使用，请联网操作"), null);
                }
              }
            });
          }
          // 无网络
          flag = -1;
          // 由无网络变为有网络(此处不分3G,WIFI)
        } else if (flag == -1) {
          // 重连
          BusProvider.reconnect();
          flag = 0;
          if (openAuth) {
            unConnect = false;
            Platform.scheduler().cancelTimer(schPeriodicTime);
            ConnectStatus = true;
          }
        } else if (openAuth) {
          unConnect = false;
          Platform.scheduler().cancelTimer(schPeriodicTime);
          ConnectStatus = true;
        }
      }
    });
    mLocationClient.start();
  }

  private void checkActivate(JsonObject msg, String address) {
    // 联网且激活时
    if (authSp.getBoolean("activate", false) && ConnectStatus) {
      this.bus.send(address, msg, null);
    } else if (authSp.getBoolean("activate", false) && !ConnectStatus) {
      Toast.makeText(this, "当前无网络，请联网继续操作...", Toast.LENGTH_LONG).show();
      // 锁定或未激活
    } else {
      if (authSp.getBoolean("locked", false)) {
        Toast.makeText(this, "当前设备被锁定", Toast.LENGTH_LONG).show();
      }
      networkInfo = mConnectivityManager.getActiveNetworkInfo();
      // 无网络
      if (networkInfo == null) {
        Toast.makeText(this, "当前无网络，请联网...", Toast.LENGTH_LONG).show();
      }
      // 校驗
      sendAuth();
    }
  }

  private void checkAuth() {
    mConnectivityManager =
        (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
    networkInfo = mConnectivityManager.getActiveNetworkInfo();
    if (networkInfo != null) {
      ConnectStatus = true;
    } else {
      // 未激活之前
      if (!authSp.contains("activate")) {
        bus.send(Bus.LOCAL + BusProvider.SID + "notification", Json.createObject().set("content",
            "激活设备,请关闭wifi,保持3G联网状态"), null);
      }
      unConnect = true;// 无网络
      schPeriodicTime = Platform.scheduler().scheduleDelay(periodicTime, new Handler<Void>() {
        @Override
        public void handle(Void event) {
          ConnectStatus = false;
          ComponentName component =
              ((ActivityManager) HomeActivity.this.getSystemService(Context.ACTIVITY_SERVICE))
                  .getRunningTasks(1).get(0).topActivity;
          String topClassName = component.getClassName();
          if (!"com.goodow.drive.android.activity.HomeActivity".equals(topClassName)
              && !"com.goodow.drive.android.activity.NotificationActivity".equals(topClassName)) {
            bus.send(Bus.LOCAL + Constant.ADDR_VIEW, Json.createObject().set("redirectTo", "home"),
                null);
          }
          if (!"com.goodow.drive.android.activity.NotificationActivity".equals(topClassName)) {
            bus.send(Bus.LOCAL + BusProvider.SID + "notification", Json.createObject().set(
                "content", "您无法继续使用，请联网操作"), null);
          }
        }
      });
    }
    sendAuth();
  }

  /**
   * 将数据库从assets目录拷贝到databases目录下
   */
  private void copyDataBases() {
    final String dataBaseDir = "data/data/" + HomeActivity.this.getPackageName() + "/databases";
    final File dbFile = new File(dataBaseDir, DATABASENAME);
    if (!(dbFile.exists() && dbFile.length() > 0)) {
      new Thread() {
        @Override
        public void run() {
          try {
            InputStream is = HomeActivity.this.getAssets().open(DATABASENAME);
            File filedir = new File(dataBaseDir);
            if (!filedir.exists()) {
              filedir.mkdir();
            }
            FileOutputStream fos = new FileOutputStream(new File(dbFile.getAbsolutePath()));
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
              fos.write(buffer, 0, len);
            }
            is.close();
            fos.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
        };
      }.start();
    }
  }

  /**
   * 打开子级页面
   * 
   * @param type
   */
  private void open(String type) {
    JsonObject msg = Json.createObject();
    msg.set("action", "post");
    JsonArray tags = Json.createArray().push(type);
    msg.set(Constant.KEY_TAGS, tags);
    if (openAuth) {
      checkActivate(msg, Bus.LOCAL + Constant.ADDR_TOPIC);
    } else {
      this.bus.send(Bus.LOCAL + Constant.ADDR_TOPIC, msg, null);
    }

  }

  private void sendAnalyticsMessage() {
    if (State.OPEN == bus.getReadyState()) {
      // 请求将播放信息统计发送到服务器
      bus.send(Bus.LOCAL + Constant.ADDR_PLAYER + ".analytics.request", null, null);
      bus.send(Bus.LOCAL + BusProvider.SID + "systime.analytics.request", null, null);
    } else {
      Log.w("EventBus Status", bus.getReadyState().name());
      BusProvider.reconnect();
      // 记录注册状态，如果已注册，不应重复注册
      if (registeredOnOpen) {
        return;
      }
      registeredOnOpen = true;// 注册
      // 监听网络状况
      openHandlerReg = bus.registerHandler(Bus.LOCAL_ON_OPEN, new MessageHandler<JsonObject>() {
        @Override
        public void handle(Message<JsonObject> message) {
          bus.send(Bus.LOCAL + Constant.ADDR_PLAYER + ".analytics.request", null, null);
          bus.send(Bus.LOCAL + BusProvider.SID + "systime.analytics.request", null, null);
          registeredOnOpen = false;
          openHandlerReg.unregisterHandler();
        }
      });
    }
  }

  private void sendAuth() {
    if (State.OPEN == bus.getReadyState()) {
      // 校验
      bus.send(Bus.LOCAL + BusProvider.SID + "auth.request", null, null);
    } else {
      BusProvider.reconnect();
      // 记录注册状态，如果已注册，不应重复注册
      if (registeredOnOpen1) {
        return;
      }
      registeredOnOpen1 = true;// 注册
      // 监听网络状况
      openHandlerReg1 = bus.registerHandler(Bus.LOCAL_ON_OPEN, new MessageHandler<JsonObject>() {
        @Override
        public void handle(Message<JsonObject> message) {
          bus.send(Bus.LOCAL + BusProvider.SID + "auth.request", null, null);
          registeredOnOpen1 = false;
          openHandlerReg1.unregisterHandler();
        }
      });
    }
  }

  private void subscribe() {
    if (!registried) {
      registried = true;
      new ViewRegistry(bus, this).subscribe();
      new PlayerRegistry(bus, this).subscribe();
      new SettingsRegistry(bus, this).subscribe();
      new DataRegistry(bus, this).subscribe();
    }
  }

}
