package com.goodow.drive.android.player;

import com.goodow.android.drive.R;
import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.core.Registration;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

@SuppressLint("JavascriptInterface")
class FlashView extends RelativeLayout implements OnTouchListener {
  private final class CallJava {
    public void consoleFlashProgress(float progressSize, int total) {
      showFlashProgress(progressSize, total);
    }
  }

  private class FlashViewBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      sound_progress.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    }
  }

  private final Bus bus = BusProvider.get();
  private String flashPath;
  private WebView flash_view;
  private ProgressBar play_progress;
  private SeekBar sound_progress;
  private ImageButton play;

  private ImageButton stop;
  private ImageButton replay;
  private int width;

  private int height;
  private boolean playing;

  private Handler handler;

  private AudioManager audioManager;

  private LinearLayout mControlLinearLayout;
  private float startY;

  private final Context mContext;
  private FlashViewBroadCastReceiver flashViewBroadCastReceiver;

  Runnable update_progress = new Runnable() {
    @Override
    public void run() {
      flash_view.loadUrl("javascript:showcount()");
      handler.postDelayed(update_progress, 1000);
    }
  };
  private Registration controlHandler;

  // 构造方法
  public FlashView(Context context) {
    super(context);
    mContext = context;
    onCreate();
  }

  // 构造方法，必有
  public FlashView(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    onCreate();
  }

  public String getFlashPath() {
    return flashPath;
  }

  public void load() {
    flash_view.loadUrl("file:///android_asset/index.html");
  }

  // 初始化界面
  public void onCreate() {
    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.flash_view, FlashView.this);
    mControlLinearLayout = (LinearLayout) findViewById(R.id.flash_back_layout);
    // 获取屏幕的宽和高
    width = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
    height = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();
    // bottom_height = BitmapFactory.decodeResource(getResources(), R.drawable.play).getHeight();
    // 加载播放flash控件
    flash_view = (WebView) findViewById(R.id.flash_web_view);
    flash_view.getSettings().setJavaScriptEnabled(true);
    flash_view.getSettings().setPluginState(PluginState.ON);
    flash_view.setWebChromeClient(new WebChromeClient());
    flash_view.getSettings().setAllowFileAccess(true);
    // flash_view.getSettings().setPluginsEnabled(true);
    flash_view.getSettings().setSupportZoom(true);
    flash_view.getSettings().setAppCacheEnabled(true);
    flash_view.addJavascriptInterface(new CallJava(), "CallJava");
    flash_view.getLayoutParams().height = height;
    flash_view.loadUrl("file:///android_asset/index.html");
    flash_view.setOnTouchListener(this);

    // 加载播放进度条
    play_progress = (ProgressBar) findViewById(R.id.flash_play_progress);
    play_progress.getLayoutParams().width = width / 4;
    // 加载播放按钮
    play = (ImageButton) findViewById(R.id.flash_button_play);
    play.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        bus.sendLocal(Constant.ADDR_PLAYER, Json.createObject().set("play", playing ? 2 : 1), null);
      }
    });

    replay = (ImageButton) findViewById(R.id.flash_button_replay);
    // 重新播放
    replay.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        bus.sendLocal(Constant.ADDR_PLAYER, Json.createObject().set("play", 3), null);
      }
    });
    flashViewBroadCastReceiver = new FlashViewBroadCastReceiver();
    // 加载声音进度条
    sound_progress = (SeekBar) findViewById(R.id.flash_sound_progress);
    audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    sound_progress.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
    sound_progress.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    sound_progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          // audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, -2);
          JsonObject msg = Json.createObject();
          msg.set("volume", (float) progress / sound_progress.getMax());
          bus.sendLocal(Constant.ADDR_SETTINGS_AUDIO, msg, null);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // 加载停止按钮
    stop = (ImageButton) findViewById(R.id.flash_button_stop);
    stop.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        bus.sendLocal(Constant.ADDR_PLAYER, Json.createObject().set("play", 0), null);
      }
    });
    // 实时更新进度
    handler = new Handler();
  }

  public void onDestory() {
    flash_view.clearCache(true);
    flash_view.removeAllViews();
  }

  /**
   * 失去焦点时，调用
   */
  public void onPause() {
    pause();
    mContext.unregisterReceiver(flashViewBroadCastReceiver);

    controlHandler.unregister();
  }

  /**
   * 获得焦点时，调用
   */
  public void onResume() {
    IntentFilter mIntentFilter = new IntentFilter();
    mIntentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
    mContext.registerReceiver(flashViewBroadCastReceiver, mIntentFilter);

    controlHandler =
        bus.registerLocalHandler(Constant.ADDR_PLAYER, new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject msg = message.body();
            if (msg.has("path")) {
              return;
            }
            if (msg.has("play")) {
              switch ((int) msg.getNumber("play")) {
                case 0:
                  // 停止
                case 1:
                  // 播放
                  playButton();
                  break;
                case 2:
                  // 暂停
                  pauseButton();
                  break;
                case 3:
                  // 重播
                  replay();
                  break;
                default:
                  Toast.makeText(getContext(), "不支持的播放模式, play=" + msg.getNumber("play"),
                      Toast.LENGTH_LONG).show();
                  break;
              }
            }
          }
        });
  }

  @Override
  public boolean onTouch(View v, MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        startY = event.getY();
      case MotionEvent.ACTION_UP:
        if (startY - event.getY() > 200) {
          mControlLinearLayout.animate().alpha(0f).setDuration(1000).setListener(
              new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                  mControlLinearLayout.setVisibility(View.GONE);
                }
              });
        }
        if (event.getY() - startY > 200) {
          mControlLinearLayout.animate().alpha(1.0f).setDuration(1000).setListener(
              new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                  mControlLinearLayout.setVisibility(View.VISIBLE);
                }
              });
        }
      default:
        break;
    }
    return false;
  }

  public void pause() {
    if (null != flashPath) {
      flash_view.loadUrl("javascript:Pause()");
      handler.removeCallbacks(update_progress);
      play.setImageResource(R.drawable.play);
      playing = false;
    }
  }

  public void setFlashPath(String flashPath) {
    this.flashPath = flashPath;
  }

  public void show() {
    if (flash_view.getLayoutParams().height != height) {
      flash_view.getLayoutParams().height = height;
    }
  }

  public void showError() {
    flash_view.loadUrl("javascript:error()");
  }

  public void showFlashProgress(float progressSize, int total) {
    int size = (int) progressSize;
    play_progress.setProgress(size);
  }

  public void start() {
    if (null != flashPath) {
      flash_view.loadUrl("javascript:Pause()");
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      flash_view.loadUrl("javascript:loadSWF(\"" + flashPath + "\", \"" + width + "\", \"" + height
          + "\")");
      flash_view.loadUrl("javascript:Play()");
      handler.post(update_progress);
      play.setImageResource(R.drawable.pause);
      playing = true;
    }
    show();
  }

  public void stop() {
    // 暂停，就会无声音
    pause();
    onDestory();
    ((Activity) getContext()).finish();
  }

  private void pauseButton() {
    if (playing) {
      pause();
    }
  }

  private void playButton() {
    if (!playing) {
      start();
    }
  }

  // 重新播放
  private void replay() {
    flash_view.reload();
    load();
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    start();
  }

}
