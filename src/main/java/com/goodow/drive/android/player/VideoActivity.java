package com.goodow.drive.android.player;

import com.goodow.android.drive.R;
import com.goodow.drive.android.activity.BaseActivity;
import com.goodow.drive.android.player.VideoView.MySizeChangeLinstener;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * @title: VideoActivity.java
 * @package drive-android
 * @description: Video播放
 * @author www.dingpengwei@gmail.com
 * @createDate 2013 2013-12-4 上午11:32:28
 * @updateDate 2013 2013-12-4 上午11:32:28
 * @version V1.0
 */
public class VideoActivity extends BaseActivity {
  private PopupWindow popupVolumn;
  private PopupWindow popupBrightness;

  private boolean isOnline = false;// 是否在线播放
  private boolean isChangedVideo = false;// 是否改变视频

  private int playedTime;// 已播放时间

  private VideoView videoView = null;// 视频视图
  private GestureDetector gestureDetector = null;// 手势识别

  private View titleView = null;
  private PopupWindow titleWindow = null;

  private View controlView = null;// 控制器视图
  private PopupWindow controlerWindow = null;// 控制器
  private SeekBar sb_media_controler_seekbar = null;// 可拖拽的进度条
  private TextView tv_media_controler_duration = null;// 视频的总时间
  private TextView tv_media_controler_has_played = null;// 播放时间

  private static int screenWidth = 0;// 屏幕宽度
  private static int screenHeight = 0;// 屏幕高度
  private static int controlHeight = 0;// 控制器高度

  private final static int TIME = 5000;// 控制器显示持续时间(毫秒)

  private boolean isControllerShow = true;// 是否显示控制器
  private boolean isPaused = false;// 是否暂停
  private boolean isFullScreen = false;// 是否全屏

  private ImageButton ibtn_media_controler_play_pause;
  private Button btn_media_controler_sound;
  private Button btn_media_controler_brightness;

  public AudioManager audiomanage;
  private int maxVolume, currentVolume;

  private View popupWindow_view;
  private TextView brightnessPercent;
  private SeekBar brightnessSeekbar;
  private TextView volumePercent;
  private SeekBar volumeSeekbar;

  private final static int PROGRESS_CHANGED = 0;

  private final static int HIDE_CONTROLER = 1;

  private Handler subHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case PROGRESS_CHANGED:// 进度改变
          int i = videoView.getCurrentPosition();
          sb_media_controler_seekbar.setProgress(i);
          if (isOnline) {
            int j = videoView.getBufferPercentage();
            sb_media_controler_seekbar.setSecondaryProgress(j * sb_media_controler_seekbar.getMax()
                / 100);
          } else {
            sb_media_controler_seekbar.setSecondaryProgress(0);
          }
          i /= 1000;
          int minute = i / 60;
          int hour = minute / 60;
          int second = i % 60;
          minute %= 60;
          tv_media_controler_has_played.setText(String.format("%02d:%02d:%02d", hour, minute,
              second));
          sendEmptyMessageDelayed(PROGRESS_CHANGED, 100);
          break;
        case HIDE_CONTROLER:// 隐藏控制器
          hideController();// 隐藏控制器
          break;
      }
      super.handleMessage(msg);
    }
  };
  private final static int SCREEN_FULL = 0;

  private final static int SCREEN_DEFAULT = 1;

  @Override
  public void onBackPressed() {

  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    getScreenSize();// 获得屏幕尺寸大小
    if (isControllerShow) {
      cancelDelayHide();// 取消隐藏延迟
      hideController();// 隐藏控制器
      showController();// 显示控制器
      hideControllerDelay();// 延迟隐藏控制器
    }
    if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
    } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    }
    super.onConfigurationChanged(newConfig);
  }

  @SuppressWarnings({"deprecation", "static-access"})
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.video_activity_media);
    this.getScreenSize();// 获得屏幕尺寸大小
    this.controlView = getLayoutInflater().inflate(R.layout.video_media_controler, null);
    this.controlerWindow = new PopupWindow(this.controlView);
    this.tv_media_controler_duration =
        (TextView) this.controlView.findViewById(R.id.tv_media_controler_duration);
    this.tv_media_controler_has_played =
        (TextView) this.controlView.findViewById(R.id.tv_media_controler_has_played);

    this.titleView = getLayoutInflater().inflate(R.layout.video_media_title, null);
    this.titleWindow = new PopupWindow(this.titleView, this.screenWidth, 60);

    Button bt_media_title_back = (Button) titleView.findViewById(R.id.bt_media_title_back);
    Button bt_media_title_screen = (Button) titleView.findViewById(R.id.bt_media_title_screen);

    this.videoView = (VideoView) findViewById(R.id.vv);

    this.ibtn_media_controler_play_pause =
        (ImageButton) this.controlView.findViewById(R.id.ibtn_media_controler_play_pause);
    this.btn_media_controler_sound =
        (Button) this.controlView.findViewById(R.id.btn_media_controler_sound);
    this.btn_media_controler_brightness =
        (Button) this.controlView.findViewById(R.id.btn_media_controler_brightness);

    Looper.myQueue().addIdleHandler(new IdleHandler() {
      @Override
      public boolean queueIdle() {// 空闲的队列
        if (titleWindow != null && videoView.isShown()) {
          titleWindow.showAtLocation(videoView, Gravity.TOP, 1280, 0);
          // titleWindow.update(0, 0, 0, 0);
        }
        if (controlerWindow != null && videoView.isShown()) {
          controlerWindow.showAtLocation(videoView, Gravity.BOTTOM, 0, 0);
          controlerWindow.update(0, 0, screenWidth, controlHeight);
        }
        subHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
        return false;
      }
    });

    // 返回监听
    bt_media_title_back.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        VideoActivity.this.finish();
      }
    });
    // 全屏监听
    bt_media_title_screen.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        if (isFullScreen) {
          setVideoScale(SCREEN_DEFAULT);
        } else {
          setVideoScale(SCREEN_FULL);
        }
        isFullScreen = !isFullScreen;
        if (isControllerShow) {
          showController();
        }
      }
    });

    // 播放错误监听
    this.videoView.setOnErrorListener(new OnErrorListener() {
      @Override
      public boolean onError(MediaPlayer mp, int what, int extra) {
        videoView.stopPlayback();
        isOnline = false;
        if (what == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
          // 文件格式错误
        } else if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
          // 服务器错误
        } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) {
          new AlertDialog.Builder(VideoActivity.this).setTitle("对不起").setMessage("未指定播放器异常。")
              .setPositiveButton("知道了", new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  videoView.stopPlayback();
                }
              }).setCancelable(false).show();
        } else if (what == MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING) {

        } else if (what == MediaPlayer.MEDIA_INFO_METADATA_UPDATE) {
          // 收到一个新的元数据
        } else if (what == MediaPlayer.MEDIA_INFO_NOT_SEEKABLE) {
          // 中途断开了
        } else if (what == MediaPlayer.MEDIA_INFO_UNKNOWN) {
          // 位置信息
        } else if (what == MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING) {
          new AlertDialog.Builder(VideoActivity.this).setTitle("对不起").setMessage(
              "您所播的视频格式不正确，播放已停止。").setPositiveButton("知道了", new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              videoView.stopPlayback();
            }
          }).setCancelable(false).show();
        }
        return false;
      }
    });

    this.videoView.setMySizeChangeLinstener(new MySizeChangeLinstener() {
      @Override
      public void doMyThings() {
        setVideoScale(SCREEN_DEFAULT);// 设置视频显示尺寸
      }
    });

    this.videoView.setOnPreparedListener(new OnPreparedListener() {// 注册在媒体文件加载完毕，可以播放时调用的回调函数
          @Override
          public void onPrepared(MediaPlayer arg0) {// 加载
            setVideoScale(SCREEN_DEFAULT);
            isFullScreen = false;
            if (isControllerShow) {
              showController();
            }

            int i = videoView.getDuration();
            sb_media_controler_seekbar.setMax(i);
            i /= 1000;
            int minute = i / 60;
            int hour = minute / 60;
            int second = i % 60;
            minute %= 60;
            tv_media_controler_duration.setText(String.format("%02d:%02d:%02d", hour, minute,
                second));

            videoView.start();
            ibtn_media_controler_play_pause.setImageResource(R.drawable.video_selector_player_play);
            hideControllerDelay();
            subHandler.sendEmptyMessage(PROGRESS_CHANGED);
          }
        });

    // 注册在媒体文件播放完毕时调用的回调函数
    this.videoView.setOnCompletionListener(new OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer arg0) {
        isOnline = false;
        videoView.stopPlayback();
        VideoActivity.this.finish();
      }
    });

    this.ibtn_media_controler_play_pause.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        cancelDelayHide();// 取消隐藏延迟
        if (isPaused) {
          videoView.start();
          ibtn_media_controler_play_pause.setImageResource(R.drawable.video_selector_player_pause);
          hideControllerDelay();// 延迟隐藏控制器
        } else {
          videoView.pause();
          ibtn_media_controler_play_pause.setImageResource(R.drawable.video_selector_player_play);
        }
        isPaused = !isPaused;
      }
    });

    this.btn_media_controler_sound.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        getPopupVolumn();
        popupVolumn.showAtLocation(findViewById(R.id.vv), Gravity.CENTER, 0, 0);
      }
    });

    this.btn_media_controler_brightness.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        getPopupBrightness();
        popupBrightness.showAtLocation(findViewById(R.id.vv), Gravity.CENTER, 0, 0);
      }
    });

    this.sb_media_controler_seekbar =
        (SeekBar) controlView.findViewById(R.id.sb_media_controler_seekbar);
    this.sb_media_controler_seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
        if (fromUser) {
          videoView.seekTo(progress);// 设置播放位置
          if (!isOnline) {

          }
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar arg0) {
        subHandler.removeMessages(HIDE_CONTROLER);
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        subHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
      }
    });

    this.gestureDetector = new GestureDetector(new SimpleOnGestureListener() {
      @Override
      public boolean onDoubleTap(MotionEvent e) {
        if (isFullScreen) {
          setVideoScale(SCREEN_DEFAULT);// 设置视频显示尺寸
        } else {
          setVideoScale(SCREEN_FULL);// 设置视频显示尺寸
        }
        isFullScreen = !isFullScreen;
        if (isControllerShow) {
          showController();// 显示控制器
        }
        return true;
      }

      @Override
      public void onLongPress(MotionEvent e) {// 长按屏幕
        if (isPaused) {
          videoView.start();
          ibtn_media_controler_play_pause.setImageResource(R.drawable.video_selector_player_pause);
          cancelDelayHide();// 取消隐藏延迟
          hideControllerDelay();// 延迟隐藏控制器
        } else {
          videoView.pause();
          ibtn_media_controler_play_pause.setImageResource(R.drawable.video_selector_player_play);
          cancelDelayHide();// 延迟隐藏控制器
          showController();// 延迟隐藏控制器
        }
        isPaused = !isPaused;
        // super.onLongPress(e);
      }

      @Override
      public boolean onSingleTapConfirmed(MotionEvent e) {// 轻击屏幕
        if (!isControllerShow) {// 是否显示控制器
          showController();// 显示控制器
          hideControllerDelay();// 显示控制器
        } else {
          cancelDelayHide();// 取消隐藏延迟
          hideController();// 取消隐藏延迟
        }
        // return super.onSingleTapConfirmed(e);
        return true;
      }
    });

    Uri uri = getIntent().getData();
    if (uri != null) {
      this.videoView.stopPlayback();// 停止视频播放
      this.videoView.setVideoURI(uri);// 设置视频文件URI
      this.isOnline = true;
      this.ibtn_media_controler_play_pause.setImageResource(R.drawable.video_selector_player_pause);
      ((TextView) this.titleView.findViewById(R.id.tv_media_title_name)).setText("我叫大白菜");
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {// 实现该方法来处理触屏事件
    boolean result = gestureDetector.onTouchEvent(event);

    if (!result) {
      if (event.getAction() == MotionEvent.ACTION_UP) {
      }
      result = super.onTouchEvent(event);
    }
    return result;
  }

  /**
   * 创建PopupBrightness
   */
  protected void initPopuptBrightness() {
    popupWindow_view = getLayoutInflater().inflate( // 获取自定义布局文件ppsplayer_volume_controler.xml的视图
        R.layout.video_media_brightness, null, false);
    popupBrightness =
        new PopupWindow(popupWindow_view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
            true);// 创建PopupWindow实例

    // 注意要加这句代码，点击弹出窗口其它区域才会让窗口消失
    popupBrightness.setBackgroundDrawable(new ColorDrawable(0x00000000));

    brightnessPercent =
        (TextView) popupWindow_view.findViewById(R.id.brightness_controler_percent_text);
    brightnessSeekbar = (SeekBar) popupWindow_view.findViewById(R.id.brightness_controler_seekbar);

    brightnessSeekbar.setMax(100);
    brightnessSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        setScreenBrightness((float) seekBar.getProgress() / 100);
      }
    });
  }

  /**
   * 创建PopupVolumn
   */
  protected void initPopuptVolumn() {
    popupWindow_view = getLayoutInflater().inflate( // 获取自定义布局文件ppsplayer_volume_controler.xml的视图
        R.layout.video_media_volume, null, false);
    popupVolumn =
        new PopupWindow(popupWindow_view, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
            true);// 创建PopupWindow实例

    // 注意要加这句代码，点击弹出窗口其它区域才会让窗口消失
    popupVolumn.setBackgroundDrawable(new ColorDrawable(0x00000000));

    volumePercent = (TextView) popupWindow_view.findViewById(R.id.volume_controler_percent_text);
    volumeSeekbar = (SeekBar) popupWindow_view.findViewById(R.id.volume_controler_seekbar);
    audiomanage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    maxVolume = audiomanage.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // 获取系统最大音量
    volumeSeekbar.setMax(maxVolume); // 拖动条最高值与系统最大声匹配
    currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
    volumeSeekbar.setProgress(currentVolume);
    volumePercent.setText(currentVolume * 100 / maxVolume + " %");

    volumeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() // 调音监听器
        {
          @Override
          public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
            audiomanage.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            currentVolume = audiomanage.getStreamVolume(AudioManager.STREAM_MUSIC); // 获取当前值
            volumeSeekbar.setProgress(currentVolume);
            volumePercent.setText(currentVolume * 100 / maxVolume + " %");
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
          }
        });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
      videoView.stopPlayback();// 停止视频播放
      int result = data.getIntExtra("CHOOSE", -1);
      if (result != -1) {
        isOnline = false;
        isChangedVideo = true;
      } else {
        String url = data.getStringExtra("CHOOSE_URL");
        if (url != null) {
          videoView.setVideoPath(url);// 设置视频文件路径
          isOnline = true;
          isChangedVideo = true;
        }
      }

      return;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onDestroy() {
    if (controlerWindow.isShowing()) {
      controlerWindow.dismiss();
      titleWindow.dismiss();
    }
    subHandler.removeMessages(PROGRESS_CHANGED);
    subHandler.removeMessages(HIDE_CONTROLER);

    if (videoView.isPlaying()) {
      videoView.stopPlayback();// 停止视频播放
    }
    super.onDestroy();
  }

  @Override
  protected void onPause() {
    playedTime = videoView.getCurrentPosition();
    videoView.pause();
    super.onPause();
  }

  @Override
  protected void onResume() {// 恢复挂起的播放器
    if (!isChangedVideo) {
      videoView.seekTo(playedTime);// 设置播放位置 playedTime已播放时间
      videoView.start();
    } else {
      isChangedVideo = false;
    }

    if (videoView.isPlaying()) {
      hideControllerDelay();// 延迟隐藏控制器
    }
    super.onResume();
  }

  private void cancelDelayHide() {// 取消隐藏延迟
    subHandler.removeMessages(HIDE_CONTROLER);
  }

  /*
   * 获取PopupWindow实例
   */
  private void getPopupBrightness() {

    if (null != popupBrightness) {
      popupBrightness.dismiss();
      return;
    } else {
      initPopuptBrightness();
    }
  }

  /*
   * 获取PopupWindow实例
   */
  private void getPopupVolumn() {

    if (null != popupVolumn) {
      popupVolumn.dismiss();
      return;
    } else {
      initPopuptVolumn();
    }
  }

  @SuppressWarnings("deprecation")
  private void getScreenSize() {// 获得屏幕尺寸大小
    Display display = getWindowManager().getDefaultDisplay();
    screenHeight = display.getHeight();
    screenWidth = display.getWidth();
    controlHeight = screenHeight / 8;
  }

  private void hideController() {// 隐藏控制器
    if (controlerWindow.isShowing()) {
      controlerWindow.update(0, 0, screenWidth, 0);
      // titleWindow.update(0, 0, screenWidth, 0);
      titleWindow.dismiss();
      isControllerShow = false;
    }
  }

  private void hideControllerDelay() {// 延迟隐藏控制器
    subHandler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
  }

  /**
   * 设置屏幕亮度
   * 
   * @param b
   */
  private void setScreenBrightness(float b) {
    // 取得window属性保存在layoutParams中
    WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
    if (b == 0.0f) {
      b = 0.01f;
    }
    layoutParams.screenBrightness = b;// b已经除以100
    getWindow().setAttributes(layoutParams);
    // 显示修改后的亮度
    layoutParams = getWindow().getAttributes();
    brightnessPercent.setText(String.valueOf(layoutParams.screenBrightness));
  }

  private void setVideoScale(int flag) {// 设置视频显示尺寸
    switch (flag) {
      case SCREEN_FULL:// 全屏
        videoView.setVideoScale(screenWidth, screenHeight);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        break;
      case SCREEN_DEFAULT:// 标准
        int videoWidth = videoView.getVideoWidth();
        int videoHeight = videoView.getVideoHeight();
        int mWidth = screenWidth;
        int mHeight = screenHeight - 25;

        if (videoWidth > 0 && videoHeight > 0) {
          if (videoWidth * mHeight > mWidth * videoHeight) {
            mHeight = mWidth * videoHeight / videoWidth;
          } else if (videoWidth * mHeight < mWidth * videoHeight) {
            mWidth = mHeight * videoWidth / videoHeight;
          } else {

          }
        }

        videoView.setVideoScale(mWidth, mHeight);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        break;
    }
  }

  private void showController() {// 显示控制器
    controlerWindow.update(0, 0, screenWidth, controlHeight);
    if (isFullScreen) {
      // titleWindow.update(0, 0, screenWidth, 60);
      if (titleWindow != null) {
        titleWindow.showAtLocation(videoView, Gravity.TOP, 0, 0);
        // titleWindow.update(0, 0, 0, 0);
      }
    } else {
      // titleWindow.update(0, 0, screenWidth, 60);
      if (titleWindow != null) {
        titleWindow.showAtLocation(videoView, Gravity.TOP, 0, 0);
        // titleWindow.update(0, 0, 0, 0);
      }
    }
    isControllerShow = true;
  }
}
