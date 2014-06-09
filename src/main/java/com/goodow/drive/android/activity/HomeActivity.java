package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.data.DBDataProvider;
import com.goodow.drive.android.data.DBHelper;
import com.goodow.drive.android.data.DBOperator;
import com.goodow.drive.android.data.DataRegistry;
import com.goodow.drive.android.player.PlayerRegistry;
import com.goodow.drive.android.settings.BaiduLocation;
import com.goodow.drive.android.settings.SettingsRegistry;
import com.goodow.drive.android.toolutils.SimpleProgressDialog;
import com.goodow.drive.android.toolutils.UnzipAsserts;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.channel.State;
import com.goodow.realtime.core.Handler;
import com.goodow.realtime.core.Platform;
import com.goodow.realtime.core.Registration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonArray;
import com.goodow.realtime.json.JsonObject;
import com.goodow.realtime.store.CollaborativeMap;
import com.goodow.realtime.store.Document;
import com.goodow.realtime.store.Model;
import com.goodow.realtime.store.Store;

import com.google.inject.Inject;

import com.baidu.location.LocationClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_home)
public class HomeActivity extends BaseActivity {
  public static final String TAG = HomeActivity.class.getSimpleName();
  private static final String DBFILENAME = "sqlite.dump";
  private static boolean registried;
  private static final String ID = "drive_android/time";
  private static final String DRIVE_ANDROID = "drive_android";

  private Registration netWorkHandlerReg;

  public static final String AUTH = "AuthImformation";
  private SharedPreferences authSp;
  // 联网状态为1
  private int flag = 0;
  // 记录注册网络监听状态
  private boolean registeredNetWork = false;
  @Inject
  private ConnectivityManager mConnectivityManager;
  private NetworkInfo networkInfo;
  private LocationClient mLocationClient;

  private boolean registeredOnOpen1 = false;

  private Registration openHandlerReg1;
  private Registration openHandlerReg;

  // TODO:如果为true，校验
  private boolean openAuth = true;

  // 更新开机时间
  private int updateBoot;
  // 试用
  private int trialSch;
  // 限制使用
  public static int updateLimit;
  // 定时校验
  private int authPeriodic;

  // 弹出框
  public static int netConnectRegister;
  public static int netConnectCheck;

  // 周期校验时间间隔
  private int authPeriodicTime = 3 * 60 * 1000;
  // 限制使用时间
  private long limitTotalTime = 3 * 60 * 1000;
  // 试用时间
  private int trailTime = 10 * 60 * 1000;

  private static final int REG = 1;
  private static final int PROPMT = 2;
  private static final int PROPMTWINDOW = 3;
  private static final int PROPMTWINDOW_LOCK = 4;

  // 限制使用状态,如果为true，限制使用中
  public static boolean limitStatus = false;

  @Inject
  private Store store;
  @Inject
  private ViewRegistry viewRegistry;
  @Inject
  private PlayerRegistry playerRegistry;
  @Inject
  private SettingsRegistry settingsRegistry;
  @Inject
  private DataRegistry dataRegistry;

  public static boolean prompt = false;// 窗口的状态

  private android.os.Handler mHandler = new android.os.Handler() {
    @Override
    public void handleMessage(android.os.Message msg) {
      switch (msg.what) {
        case REG:
          registerDialog(HomeActivity.this, false);
          break;
        case PROPMT:
          Toast.makeText(HomeActivity.this, R.string.string_register_prompt_delaynetwork,
              Toast.LENGTH_LONG).show();
          break;
        case PROPMTWINDOW:
          if (prompt) {
            return;
          }
          promptWindow((String) msg.obj, true);
          prompt = true;
          break;
        case PROPMTWINDOW_LOCK:
          if (prompt) {
            return;
          }
          promptWindow((String) msg.obj, false);
          prompt = true;
          break;
        default:
          break;
      }
    };
  };

  public void onClick(View v) {

    switch (v.getId()) {
    // 收藏
      case R.id.iv_act_main_coll:
        if (openAuth) {
          checkActivate(Json.createObject().set(Constant.KEY_REDIRECTTO, "favorite"),
              Constant.ADDR_VIEW);
        } else {
          this.bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
              "favorite"), null);
        }
        break;
      // 锁屏
      case R.id.iv_act_main_loc:
        JsonObject brightness = Json.createObject();
        brightness.set("brightness", 0);
        this.bus.sendLocal(Constant.ADDR_CONTROL, brightness, null);
        break;
      // 设置
      case R.id.iv_act_main_set:
        this.bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set(Constant.KEY_REDIRECTTO,
            "settings"), null);
        break;
      // 关机
      case R.id.iv_act_main_clo:
        JsonObject shutdown = Json.createObject();
        shutdown.set("shutdown", 0);
        this.bus.sendLocal(Constant.ADDR_CONTROL, shutdown, null);
        break;
      // 年龄
      case R.id.iv_act_age_care:// 托班
        open(Constant.DATAREGISTRY_TYPE_SHIP);
        break;
      case R.id.iv_act_age_small:// 小班
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.LABEL_GRADE_LITTLE);
        break;
      case R.id.iv_act_age_middle:// 中班
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.LABEL_GRADE_MID);
        break;
      case R.id.iv_act_age_large:// 大班
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.LABEL_GRADE_BIG);
        break;
      case R.id.iv_act_age_pre:// 学前班
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.LABEL_GRADE_PRE);
        break;
      // 领域
      case R.id.iv_act_topic_health:// 健康
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.DOMIAN_HEALTH);
        break;
      case R.id.iv_act_topic_languge:// 语言
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.DOMIAN_LANGUAGE);
        break;
      case R.id.iv_act_topic_community:// 社会
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.DOMIAN_WORLD);
        break;
      case R.id.iv_act_topic_science:// 科学
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.DOMIAN_SCIENCE);
        break;
      case R.id.iv_act_topic_math:// 数学
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.DOMIAN_MATH);
        break;
      case R.id.iv_act_topic_music:// 艺术(音乐)
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.DOMIAN_MUSIC);
        break;
      case R.id.iv_act_topic_art:// 艺术(美术)
        open(Constant.DATAREGISTRY_TYPE_HARMONY, Constant.DOMIAN_ART);
        break;
      // 资源类型
      case R.id.iv_act_source_text:// 文本
        open(Constant.DATAREGISTRY_TYPE_SOURCE, "活动设计");
        break;
      case R.id.iv_act_source_pic:// 图片
        open(Constant.DATAREGISTRY_TYPE_SOURCE, "图片");
        break;
      case R.id.iv_act_source_anim:// 动画
        open(Constant.DATAREGISTRY_TYPE_SOURCE, "动画");
        break;
      case R.id.iv_act_source_video:// 视频
        open(Constant.DATAREGISTRY_TYPE_SOURCE, "视频");
        break;
      case R.id.iv_act_source_music:// 音频
        open(Constant.DATAREGISTRY_TYPE_SOURCE, "音频");
        break;
      case R.id.iv_act_source_ebook:// 电子书
        open(Constant.DATAREGISTRY_TYPE_SOURCE, "电子书");
        break;
      // 特色课程
      case R.id.iv_act_main_pre:// 入学准备
        this.open(Constant.DATAREGISTRY_TYPE_PREPARE);
        break;
      case R.id.iv_act_main_edu:// 安全教育
        this.open(Constant.DATAREGISTRY_TYPE_EDUCATION);
        break;
      case R.id.iv_act_main_read:// 早期阅读
        this.open(Constant.DATAREGISTRY_TYPE_READ);
      default:
        break;
    }
  }

  /**
   * 屏蔽返回键
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  /**
   * 
   * @param mString 显示的字样
   * @param flag 对话框是否消失
   */
  public void promptWindow(String mString, boolean flag) {
    final WindowManager wm =
        (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
    final View view = View.inflate(getApplicationContext(), R.layout.register_prompt, null);
    LayoutParams params = new WindowManager.LayoutParams();
    params.width = 643;
    params.height = 476;
    params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
    params.y = 200;
    params.type = LayoutParams.TYPE_PHONE;
    params.format = PixelFormat.RGBA_8888;
    wm.addView(view, params);
    final TextView tv_register_prompt = (TextView) view.findViewById(R.id.tv_register_prompt);
    final TextView tv_register_reboot = (TextView) view.findViewById(R.id.tv_register_reboot);
    final TextView tv_register_shutdown = (TextView) view.findViewById(R.id.tv_register_shutdown);
    tv_register_prompt.setText(mString);
    if (flag) {
      openHandlerReg = bus.registerLocalHandler(Bus.ON_OPEN, new MessageHandler<JsonObject>() {
        @Override
        public void handle(Message<JsonObject> message) {
          wm.removeView(view);
          sendAuth();
          prompt = false;
          Platform.scheduler().cancelTimer(updateLimit); // 取消限制使用
          openHandlerReg.unregister();
        }
      });
    }
    tv_register_reboot.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        wm.removeView(view);
        // 重启
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("shutdown", 1), null);
      }
    });
    tv_register_shutdown.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        wm.removeView(view);
        // 关机
        bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("shutdown", 0), null);
      }
    });
  }

  /**
   * 试用十分钟，跳到主页
   */
  public void trialTenMinutes() {
    authSp.edit().putBoolean("trial", true).commit();
    trialSch = Platform.scheduler().scheduleDelay(trailTime, new Handler<Void>() {
      @Override
      public void handle(Void event) {
        bus.sendLocal(Constant.ADDR_VIEW_PROMPT, Json.createObject().set("status", false), null);
        authSp.edit().putBoolean("trial", false).commit();
        ComponentName component =
            ((ActivityManager) HomeActivity.this.getSystemService(Context.ACTIVITY_SERVICE))
                .getRunningTasks(1).get(0).topActivity;
        String topClassName = component.getClassName();
        if (!"com.goodow.drive.android.activity.HomeActivity".equals(topClassName)) {
          bus.sendLocal(Constant.ADDR_VIEW, Json.createObject().set("redirectTo", "home"), null);
        }
        if (!authSp.getBoolean("register", false)) {
          mHandler.sendEmptyMessage(REG);
        }
      }
    });
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    authSp = this.getSharedPreferences(AUTH, Context.MODE_PRIVATE);
    subscribe();
    networkInfo = mConnectivityManager.getActiveNetworkInfo();
    BaiduLocation.INSTANCE.setContext(getApplicationContext());
    mLocationClient = BaiduLocation.INSTANCE.getLocationClient();
    BaiduLocation.INSTANCE.init();
    if (openAuth) {
      openRegisterAndAuth();
    }
    // 每隔一分钟更新一次开机时间数据
    updateBoot = Platform.scheduler().schedulePeriodic(60 * 1000, new Handler<Void>() {
      @Override
      public void handle(Void event) {
        DBOperator.updateBoot(HomeActivity.this, "T_BOOT", "LAST_TIME", "CLOSE_TIME", Json
            .createObject().set("LAST_TIME", SystemClock.uptimeMillis()).set("CLOSE_TIME",
                System.currentTimeMillis()));
      }
    });
    copyDataBasesBy();
    new Thread() {
      @Override
      public void run() {
        try {
          UnzipAsserts.unZip(HomeActivity.this, "attachments.zip", "/mnt/sdcard", false);
        } catch (IOException e) {
          e.printStackTrace();
        }
      };
    }.start();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Platform.scheduler().cancelTimer(updateBoot);
    Platform.scheduler().cancelTimer(trialSch);
    Platform.scheduler().cancelTimer(updateLimit);
    Platform.scheduler().cancelTimer(authPeriodic);
    Platform.scheduler().cancelTimer(netConnectCheck);
    Platform.scheduler().cancelTimer(netConnectRegister);
    netWorkHandlerReg.unregister();
    mLocationClient.stop();
    SimpleProgressDialog.resetByThisContext(HomeActivity.this);
  }

  @Override
  protected void onNewIntent(android.content.Intent intent) {
    Bundle extras = intent.getExtras();
    if (extras.getBoolean("register")) {
      Platform.scheduler().cancelTimer(trialSch);// 取消试用十分钟
      authSp.edit().putBoolean("trial", false).commit();
      bus.sendLocal(Constant.ADDR_VIEW_PROMPT, Json.createObject().set("status", false), null);
      registerDialog(HomeActivity.this, false);
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (registeredNetWork) {
      return;
    }
    registeredNetWork = true;
    netWorkHandlerReg =
        bus.registerHandler(Constant.ADDR_CONNECTIVITY, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            String action = body.getString("action");
            if (action != null && !"post".equalsIgnoreCase(action)) {
              return;
            }
            float netStrength = (float) body.getNumber("strength");
            if (netStrength <= 0.0f) {
              // 无网络
              flag = -1;
              // 由无网络变为有网络(此处不分3G,WIFI)
            } else if (flag == -1) {
              // 重连
              bus.sendLocal("Bus_Reconnet", null, null);
              flag = 0;
            }
          }
        });
    mLocationClient.start();
    Handler<Document> onLoaded = new Handler<Document>() {
      @Override
      public void handle(Document document) {
        CollaborativeMap root = document.getModel().getRoot().get(DRIVE_ANDROID);
        authSp.edit()
            .putInt("authPeriodicTime", ((Double) root.get("authPeriodicTime")).intValue())
            .putLong("limitTotalTime", ((Double) root.get("limitTotalTime")).longValue()).putInt(
                "trailTime", ((Double) root.get("trailTime")).intValue()).commit();
      }
    };
    Handler<Model> opt_initializer = new Handler<Model>() {
      @Override
      public void handle(Model model) {
        CollaborativeMap map = model.createMap(null);
        map.set("authPeriodicTime", authPeriodicTime);
        map.set("limitTotalTime", limitTotalTime);
        map.set("trailTime", trailTime);
        model.getRoot().set(DRIVE_ANDROID, map);
      }
    };
    store.load(ID, onLoaded, opt_initializer, null);
  }

  private void checkActivate(JsonObject msg, String address) {
    // 1.试用 2.限制使用 3.正常使用
    if (authSp.getBoolean("trial", false)
        || (authSp.getBoolean("register", false) && authSp.getBoolean("limit", false))
        || (authSp.getBoolean("register", false) && authSp.getBoolean("normal", false))) {
      this.bus.sendLocal(address, msg, null);
      // 未注册
    } else if (!authSp.getBoolean("register", false)) {
      registerDialog(HomeActivity.this, false);
    } else if (authSp.getBoolean("lock", false)) {
      Toast.makeText(HomeActivity.this, R.string.string_register_prompt_locked, Toast.LENGTH_LONG)
          .show();
      // 注册，限制使用时间已用完
    } else if (!authSp.getBoolean("limit", true)) {
      Toast.makeText(HomeActivity.this, R.string.string_register_prompt_limit_finished,
          Toast.LENGTH_LONG).show();
      networkInfo = mConnectivityManager.getActiveNetworkInfo();
      if (networkInfo == null) {
        Toast.makeText(this, R.string.string_register_unnetwork, Toast.LENGTH_LONG).show();
      }
      sendAuth();// 校验
    }
  }

  /**
   * 将数据库从assets目录拷贝到databases目录下
   */
  private void copyDataBases() {
    final String dataBaseDir = "data/data/" + HomeActivity.this.getPackageName() + "/databases";
    final File dbFile = new File(dataBaseDir, DBHelper.DBNAME);
    if (!(dbFile.exists() && dbFile.length() > 0)) {
      new Thread() {
        @Override
        public void run() {
          try {
            InputStream is = HomeActivity.this.getAssets().open(DBFILENAME);
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
          }
        }
      }.start();
    }
  }

  private void copyDataBasesBy() {
    try {
      HomeActivity.this.getAssets().open(DBFILENAME);
      copyDataBases();// 数据库拷贝
    } catch (IOException e) {
      copyDataBasesBySql();// sql语句初始化
    }
  }

  /**
   * 通过读取sql文件初始化数据库
   * 
   * @author:DingPengwei
   * @date:May 5, 2014 7:03:38 PM
   */
  private void copyDataBasesBySql() {
    final String dataBaseDir = "data/data/" + HomeActivity.this.getPackageName() + "/databases";
    final File dbFile = new File(dataBaseDir, DBHelper.DBNAME);
    if (!(dbFile.exists() && dbFile.length() > 0)) {
      new Thread() {
        @Override
        public void run() {
          JsonArray sqls = Json.createArray();
          try {
            InputStream open = HomeActivity.this.getAssets().open("init.sql");
            InputStreamReader inputStreamReader = new InputStreamReader(open, "utf8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
              sqls.push(line);
            }
            open.close();
            inputStreamReader.close();
            bufferedReader.close();
          } catch (Exception e) {
            e.printStackTrace();
          }
          DBDataProvider.insertFileBySql(HomeActivity.this, sqls);
        };
      }.start();
    }
  }

  private void limitUse() {
    long time = authSp.getLong("limitTime", 0l);
    // 更新限制使用时间
    authSp.edit().putLong("limitTime",
        time + SystemClock.uptimeMillis() - authSp.getLong("startLimit", 0l)).commit();
    if (authSp.getLong("limitTime", 0l) >= authSp.getLong("limitTotalTime", limitTotalTime)) {
      authSp.edit().putBoolean("limit", false).commit();
      // 让系统异常，限制使用中消失
      bus.sendLocal(Constant.ADDR_VIEW_PROMPT, Json.createObject().set("status", false), null);
      android.os.Message msg = android.os.Message.obtain();
      msg.obj = getResources().getString(R.string.string_register_prompt_limit_finished);
      msg.what = PROPMTWINDOW;
      mHandler.sendMessage(msg);
    }
  }

  // /**
  // * 发送用户行为数据
  // */
  // private void sendAnalyticsMessage() {
  // if (State.OPEN == bus.getReadyState()) {
  // // 请求将播放信息统计发送到服务器
  // bus.send(Bus.LOCAL + Constant.ADDR_PLAYER + ".analytics.request", null, null);
  // bus.send(Bus.LOCAL + BusProvider.SID + "systime.analytics.request", null, null);
  // } else {
  // Log.w("EventBus Status", bus.getReadyState().name());
  // BusProvider.reconnect();
  // // 记录注册状态，如果已注册，不应重复注册
  // if (registeredOnOpen) {
  // return;
  // }
  // registeredOnOpen = true;// 注册
  // // 监听网络状况
  // openHandlerReg = bus.registerHandler(Bus.LOCAL_ON_OPEN, new MessageHandler<JsonObject>() {
  // @Override
  // public void handle(Message<JsonObject> message) {
  // bus.send(Bus.LOCAL + Constant.ADDR_PLAYER + ".analytics.request", null, null);
  // bus.send(Bus.LOCAL + BusProvider.SID + "systime.analytics.request", null, null);
  // registeredOnOpen = false;
  // openHandlerReg.unregister();
  // }
  // });
  // }
  // }

  private void notNetLimiteMode() {
    // 记录开始限制使用的时间
    authSp.edit().putLong("startLimit", SystemClock.uptimeMillis()).commit();
    limitStatus = true;
    if (authSp.getLong("limitTime", 0l) >= authSp.getLong("limitTotalTime", limitTotalTime)) {
      android.os.Message msg = android.os.Message.obtain();
      msg.obj = getResources().getString(R.string.string_register_prompt_limit_finished);
      msg.what = PROPMTWINDOW;
      mHandler.sendMessage(msg);
      return;
    }
    authSp.edit().putBoolean("limit", true).commit();
    bus.sendLocal(Constant.ADDR_VIEW_PROMPT, Json.createObject().set("status", true).set("content",
        getResources().getString(R.string.string_register_prompt_limit_use)), null);
    updateLimit = Platform.scheduler().schedulePeriodic(60 * 1000, new Handler<Void>() {
      @Override
      public void handle(Void arg0) {
        limitUse();
      }
    });
  }

  /**
   * 打开子级页面
   * 
   * @param
   */
  private void open(String... string) {
    JsonObject msg = Json.createObject();
    msg.set("action", "post");
    JsonArray tags = Json.createArray();
    for (String type : string) {
      tags.push(type);
    }
    msg.set(Constant.KEY_TAGS, tags);
    if (openAuth) {
      checkActivate(msg, Constant.ADDR_TOPIC);
    } else {
      this.bus.sendLocal(Constant.ADDR_TOPIC, msg, null);
    }
  }

  private void openRegisterAndAuth() {
    authSp.edit().putInt("FailTime", 0).commit(); // 失败次数
    authSp.edit().putBoolean("normal", false).commit(); // 校验成功标记设置
    // 注册成功
    if (authSp.getBoolean("register", false)) {
      // && State.OPEN == bus.getReadyState()
      if (networkInfo != null) { // 网络良好，校验
        bus.sendLocal(Constant.ADDR_AUTH_REQUEST, Json.createObject(), null);
        SimpleProgressDialog.show(HomeActivity.this, new OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
          }
        });
        netConnectCheck = Platform.scheduler().scheduleDelay(20 * 1000, new Handler<Void>() {
          @Override
          public void handle(Void event) {
            SimpleProgressDialog.dismiss(HomeActivity.this);
            mHandler.sendEmptyMessage(PROPMT);
            authSp.edit().putBoolean("limit", true).commit();
            bus.sendLocal(Constant.ADDR_VIEW_PROMPT, Json.createObject().set("status", true).set(
                "content", getResources().getString(R.string.string_register_prompt_limit_use)),
                null);
            limitStatus = true;
            // 记录开始限制使用的时间
            authSp.edit().putLong("startLimit", SystemClock.uptimeMillis()).commit();
            updateLimit = Platform.scheduler().schedulePeriodic(60 * 1000, new Handler<Void>() {
              @Override
              public void handle(Void arg0) {
                limitUse();
              }
            });;
          }
        });
      } else if (!authSp.getBoolean("lock", false)) { // 网络不通，且未锁定
        notNetLimiteMode();
      } else { // 网络不通，锁定
        android.os.Message msg = android.os.Message.obtain();
        msg.obj = getResources().getString(R.string.string_register_prompt_locked);
        msg.what = PROPMTWINDOW_LOCK;
        mHandler.sendMessage(msg);
      }
    } else if (networkInfo == null) {
      // 试用10分钟
      bus.sendLocal(Constant.ADDR_VIEW_PROMPT, Json.createObject().set("status", true), null);
      trialTenMinutes();
    } else {
      registerDialog(this, true);
    }
    if (authSp.getBoolean("limit", false) && !limitStatus) {
      limitStatus = true;// 限制使用中
      // 记录开始限制使用的时间
      authSp.edit().putLong("startLimit", SystemClock.uptimeMillis()).commit();
      updateLimit = Platform.scheduler().schedulePeriodic(60 * 1000, new Handler<Void>() {
        @Override
        public void handle(Void arg0) {
          limitUse();
        }
      });
    }
    authPeriodic =
        Platform.scheduler().schedulePeriodic(authSp.getInt("authPeriodicTime", authPeriodicTime),
            new Handler<Void>() {
              @Override
              public void handle(Void arg0) {
                // 排除未注册情况
                if (!authSp.getBoolean("register", false)) {
                  return;
                }
                // 定期校验
                // 无网络，进入限制使用
                networkInfo = mConnectivityManager.getActiveNetworkInfo();
                if (networkInfo == null && authSp.getBoolean("register", false)) {
                  if (prompt) {
                    return; // 如果已经弹出提示框
                  }
                  if (!limitStatus) {
                    notNetLimiteMode();
                  }
                }
                // 有网络，发送数据
                sendAuth();
              }
            });
  }

  /**
   * 注册对话框
   * 
   * @param bool
   */
  private void registerDialog(Context context, boolean bool) {
    final AlertDialog mAlertDialog = new AlertDialog.Builder(context).create();
    View mView = View.inflate(context, R.layout.register_login, null);
    mAlertDialog.setView(mView);
    mAlertDialog.show();
    mAlertDialog.setCancelable(false);
    Window mWindow = mAlertDialog.getWindow();
    mWindow.setContentView(R.layout.register_login);
    final EditText schoolname = (EditText) mWindow.findViewById(R.id.et_schoolname);
    final EditText schoolname1 = (EditText) mWindow.findViewById(R.id.et_schoolname1);
    final TextView tv_register_submit = (TextView) mWindow.findViewById(R.id.tv_register_submit);
    final TextView tv_register_close = (TextView) mWindow.findViewById(R.id.tv_register_close);
    final TextView tv_register_try = (TextView) mWindow.findViewById(R.id.tv_register_try);
    tv_register_submit.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        networkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || bus.getReadyState() != State.OPEN) {
          Toast.makeText(HomeActivity.this, R.string.string_register_unnetwork, Toast.LENGTH_LONG)
              .show();
          return;
        }
        String schName = schoolname.getText().toString().trim();
        String schName1 = schoolname1.getText().toString().trim();
        if (!TextUtils.isEmpty(schName) && !TextUtils.isEmpty(schName1)) {
          mAlertDialog.cancel();
          // 提交注册信息
          bus.sendLocal(Constant.ADDR_AUTH_REQUEST, Json.createObject().set("schoolName", schName)
              .set("contacts", schName1), null);
          SimpleProgressDialog.show(HomeActivity.this, new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
          });
          netConnectRegister = Platform.scheduler().scheduleDelay(20 * 1000, new Handler<Void>() {
            @Override
            public void handle(Void event) {
              if (SimpleProgressDialog.isShowing()) {
                SimpleProgressDialog.dismiss(HomeActivity.this);
                mHandler.sendEmptyMessage(PROPMT);
              }
            }
          });
        } else if (TextUtils.isEmpty(schName1) | TextUtils.isEmpty(schName)) {
          Toast.makeText(getApplicationContext(), R.string.string_register_input_notnull,
              Toast.LENGTH_LONG).show();
        }
      }
    });
    if (bool) {
      tv_register_close.setVisibility(View.GONE);
      tv_register_try.setVisibility(View.VISIBLE);
      tv_register_try.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          mAlertDialog.cancel();
          trialTenMinutes();
          bus.sendLocal(Constant.ADDR_VIEW_PROMPT, Json.createObject().set("status", true), null);
          authSp.edit().putBoolean("trial", true).commit();
        }
      });
    } else {
      tv_register_close.setVisibility(View.VISIBLE);
      tv_register_try.setVisibility(View.GONE);
      tv_register_close.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          mAlertDialog.cancel();
          // 关机
          bus.sendLocal(Constant.ADDR_CONTROL, Json.createObject().set("shutdown", 0), null);
        }
      });
    }
  }

  /**
   * 发送校验请求
   */
  private void sendAuth() {
    if (State.OPEN == bus.getReadyState()) {
      // 校验
      bus.sendLocal(Constant.ADDR_AUTH_REQUEST, Json.createObject(), null);
    } else {
      // 记录注册状态，如果已注册，不应重复注册
      if (registeredOnOpen1) {
        return;
      }
      registeredOnOpen1 = true;// 注册
      // 监听网络状况
      openHandlerReg1 = bus.registerLocalHandler(Bus.ON_OPEN, new MessageHandler<JsonObject>() {
        @Override
        public void handle(Message<JsonObject> message) {
          bus.sendLocal(Constant.ADDR_AUTH_REQUEST, Json.createObject(), null);
          registeredOnOpen1 = false;
          openHandlerReg1.unregister();
        }
      });
    }
  }

  private void subscribe() {
    if (!registried) {
      registried = true;
      viewRegistry.subscribe();
      playerRegistry.subscribe();
      settingsRegistry.subscribe();
      dataRegistry.subscribe();
    }
  }
}
