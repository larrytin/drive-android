package com.goodow.drive.android.flash;

import com.goodow.android.drive.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class FlashView extends RelativeLayout {
  private final class CallJava {
    public void consoleFlashProgress(float progressSize, int total) {
      showFlashProgress(progressSize, total);
    }
  }

  private final static String TAG = FlashView.class.getSimpleName();
  private String flashPath;
  private WebView flash_view;
  private ProgressBar play_progress;
  private SeekBar sound_progress;
  private ImageButton play;
  private ImageButton stop;

  private ImageButton replay;
  private int width;
  private int height;
  private int bottom_height;

  private boolean playing;
  private Handler handler;

  private AudioManager audioManager;

  Runnable update_progress = new Runnable() {
    @Override
    public void run() {
      flash_view.loadUrl("javascript:showcount()");
      handler.postDelayed(update_progress, 1000);
    }
  };

  // 构造方法
  public FlashView(Context context) {
    super(context);
    onCreate();
  }

  // 构造方法，必有
  public FlashView(Context context, AttributeSet attrs) {
    super(context, attrs);
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
    // 获取屏幕的宽和高
    width = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();
    height = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getHeight();
    bottom_height = BitmapFactory.decodeResource(getResources(), R.drawable.play).getHeight();
    // 加载播放flash控件
    flash_view = (WebView) findViewById(R.id.flash_web_view);
    flash_view.getSettings().setJavaScriptEnabled(true);
    flash_view.getSettings().setPluginState(PluginState.ON);
    flash_view.setWebChromeClient(new WebChromeClient());
    flash_view.getSettings().setAllowFileAccess(true);
    flash_view.getSettings().setPluginsEnabled(true);
    flash_view.getSettings().setSupportZoom(true);
    flash_view.getSettings().setAppCacheEnabled(true);
    flash_view.addJavascriptInterface(new CallJava(), "CallJava");
    flash_view.getLayoutParams().height = height - bottom_height;
    flash_view.loadUrl("file:///android_asset/index.html");

    // 加载播放进度条
    play_progress = (ProgressBar) findViewById(R.id.flash_play_progress);
    play_progress.getLayoutParams().width = width / 4;
    // 加载播放按钮
    play = (ImageButton) findViewById(R.id.flash_button_play);
    play.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (!playing) {
          start();
        } else {
          pause();
        }
      }
    });

    replay = (ImageButton) findViewById(R.id.flash_button_replay);
    // 重新播放
    replay.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        replay();
      }

    });

    // 加载声音进度条
    sound_progress = (SeekBar) findViewById(R.id.flash_sound_progress);
    audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    sound_progress.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
    sound_progress.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
    sound_progress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, -2);
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
        stop();
      }
    });
    // 实时更新进度
    handler = new Handler();
    Log.i(TAG, "onCreate");
  }

  public void pause() {
    Log.i(TAG, "pause");
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
    if (flash_view.getLayoutParams().height != height - bottom_height) {
      flash_view.getLayoutParams().height = height - bottom_height;
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
    Log.i(TAG, "start");
    if (null != flashPath) {
      flash_view.loadUrl("javascript:Pause()");
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      flash_view.loadUrl("javascript:loadSWF(\"" + flashPath + "\", \"" + width + "\", \""
          + (height - bottom_height) + "\")");
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
    ((Activity) getContext()).finish();
  }

  // 重新播放
  private void replay() {
    Log.i(TAG, "replay");
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
