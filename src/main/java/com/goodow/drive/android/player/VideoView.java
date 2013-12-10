package com.goodow.drive.android.player;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

class VideoView extends SurfaceView implements MediaPlayerControl {
  /**
   * 自定义尺寸 改变的事件监听（接口）
   */
  public interface MySizeChangeLinstener {
    public void doMyThings();// 自定义大小
  }

  private String TAG = "VideoView";
  private Context mContext;
  private Uri mUri;// URI 视频地址

  private int mDuration;// 持续时间
  private SurfaceHolder mSurfaceHolder = null;//
  private MediaPlayer mMediaPlayer = null;// 媒体播放器
  private boolean mIsPrepared;// 是否候缓好的
  private int mVideoWidth;// 视频宽度
  private int mVideoHeight;// 视频高度
  private int mSurfaceWidth;// Surface宽度
  private int mSurfaceHeight;// Surface高度
  private MediaController mMediaController;// 媒体控制器
  private OnCompletionListener mOnCompletionListener;// 视频播放完成事件监听
  private MediaPlayer.OnPreparedListener mOnPreparedListener;// 视频准备好事件监听
  private int mCurrentBufferPercentage;// 当前缓冲百分比
  private OnErrorListener mOnErrorListener;// 播放出错事件监听
  private MySizeChangeLinstener mMyChangeLinstener;// 视频显示尺寸 改变的事件监听
  private boolean mStartWhenPrepared;// 开始什么时候缓冲好

  private int mSeekWhenPrepared;// 指定进度什么时候缓冲好

  // 视频显示尺寸大小改变 监听事件
  MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
      new MediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
          mVideoWidth = mp.getVideoWidth();
          mVideoHeight = mp.getVideoHeight();

          if (mMyChangeLinstener != null) {
            mMyChangeLinstener.doMyThings();
          }

          if (mVideoWidth != 0 && mVideoHeight != 0) {
            getHolder().setFixedSize(mVideoWidth, mVideoHeight);
          }
        }
      };

  // 注册在媒体文件加载完毕，可以播放时调用的回调函数。
  MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
    @Override
    public void onPrepared(MediaPlayer mp) {
      // briefly show the mediacontroller
      mIsPrepared = true;
      if (mOnPreparedListener != null) {
        mOnPreparedListener.onPrepared(mMediaPlayer);
      }
      if (mMediaController != null) {
        mMediaController.setEnabled(true);
      }
      mVideoWidth = mp.getVideoWidth();
      mVideoHeight = mp.getVideoHeight();
      if (mVideoWidth != 0 && mVideoHeight != 0) {
        // Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
        getHolder().setFixedSize(mVideoWidth, mVideoHeight);
        if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
          // We didn't actually change the size (it was already at the size
          // we need), so we won't get a "surface changed" callback, so
          // start the video here instead of in the callback.
          if (mSeekWhenPrepared != 0) {
            mMediaPlayer.seekTo(mSeekWhenPrepared);
            mSeekWhenPrepared = 0;
          }
          if (mStartWhenPrepared) {
            mMediaPlayer.start();
            mStartWhenPrepared = false;
            if (mMediaController != null) {
              mMediaController.show();
            }
          } else if (!isPlaying() && (mSeekWhenPrepared != 0 || getCurrentPosition() > 0)) {
            if (mMediaController != null) {
              // Show the media controls when we're paused into a video and make 'em stick.
              mMediaController.show(0);
            }
          }
        }
      } else {
        // We don't know the video size yet, but should start anyway.
        // The video size might be reported to us later.
        if (mSeekWhenPrepared != 0) {
          mMediaPlayer.seekTo(mSeekWhenPrepared);
          mSeekWhenPrepared = 0;
        }
        if (mStartWhenPrepared) {
          mMediaPlayer.start();
          mStartWhenPrepared = false;
        }
      }
    }
  };

  // 注册在媒体文件播放完毕时调用的回调函数
  private MediaPlayer.OnCompletionListener mCompletionListener =
      new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
          if (mMediaController != null) {
            mMediaController.hide();
          }
          if (mOnCompletionListener != null) {
            mOnCompletionListener.onCompletion(mMediaPlayer);
          }
        }
      };

  // 注册在设置或播放过程中发生错误时调用的回调函数。如果未指定回调函数， 或回调函数返回假，VideoView 会通知用户发生了错误。
  private MediaPlayer.OnErrorListener mErrorListener = new MediaPlayer.OnErrorListener() {
    @Override
    public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
      if (mMediaController != null) {
        mMediaController.hide();
      }

      /* If an error handler has been supplied, use it and finish. */
      if (mOnErrorListener != null) {
        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
          return true;
        }
      }

      /*
       * Otherwise, pop up an error dialog so the user knows that something bad has happened. Only
       * try and pop up the dialog if we're attached to a window. When we're going away and no
       * longer have a window, don't bother showing the user an error.
       */
      if (getWindowToken() != null) {
        /*
         * Resources r = mContext.getResources(); int messageId;
         * 
         * if (framework_err == MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
         * messageId =
         * com.android.internal.R.string.VideoView_error_text_invalid_progressive_playback; } else {
         * messageId = com.android.internal.R.string.VideoView_error_text_unknown; }
         * 
         * new AlertDialog.Builder(mContext)
         * .setTitle(com.android.internal.R.string.VideoView_error_title) .setMessage(messageId)
         * .setPositiveButton(com.android.internal.R.string.VideoView_error_button, new
         * DialogInterface.OnClickListener() { public void onClick(DialogInterface dialog, int
         * whichButton) { If we get here, there is no onError listener, so at least inform them that
         * the video is over.
         * 
         * if (mOnCompletionListener != null) { mOnCompletionListener.onCompletion(mMediaPlayer); }
         * } }) .setCancelable(false) .show();
         */
      }
      return true;
    }
  };

  // 缓冲更新 事件监听
  private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
      new MediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
          mCurrentBufferPercentage = percent;
        }
      };

  // 对surface对象的状态改变 进行监听
  SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
      mSurfaceWidth = w;
      mSurfaceHeight = h;
      if (mMediaPlayer != null && mIsPrepared && mVideoWidth == w && mVideoHeight == h) {
        if (mSeekWhenPrepared != 0) {
          mMediaPlayer.seekTo(mSeekWhenPrepared);
          mSeekWhenPrepared = 0;
        }
        mMediaPlayer.start();
        if (mMediaController != null) {
          mMediaController.show();
        }
      }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      mSurfaceHolder = holder;
      openVideo();// 打开视频
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
      // after we return from this we can't use the surface any more
      mSurfaceHolder = null;
      if (mMediaController != null) {
        mMediaController.hide();
      }
      if (mMediaPlayer != null) {
        mMediaPlayer.reset();// 重置
        mMediaPlayer.release();
        mMediaPlayer = null;
      }
    }
  };

  public VideoView(Context context) {
    super(context);
    mContext = context;
    initVideoView();
  }

  public VideoView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
    mContext = context;
    initVideoView();
  }

  /**
   * @param context 视图运行的应用程序上下文，通过它可以访问当前主题、资源等等。
   * @param attrs 用于视图的 XML 标签属性集合
   * @param defStyle 应用到视图的默认风格。如果为 0 则不应用（包括当前主题中的）风格。 该值可以是当前主题中的属性资源，或者是明确的风格资源 ID。
   */
  public VideoView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
    initVideoView();
  }

  @Override
  public boolean canPause() {
    return false;
  }

  @Override
  public boolean canSeekBackward() {
    return false;
  }

  @Override
  public boolean canSeekForward() {
    return false;
  }

  @Override
  public int getBufferPercentage() {// 获得缓冲区的百分比
    if (mMediaPlayer != null) {
      return mCurrentBufferPercentage;
    }
    return 0;
  }

  // public void setMediaController(MediaController controller) {//设置媒体控制器
  // if (mMediaController != null) {
  // mMediaController.hide();
  // }
  // mMediaController = controller;
  // attachMediaController();//附加媒体控制器
  // }

  @Override
  public int getCurrentPosition() {// 获得当前播放位置
    if (mMediaPlayer != null && mIsPrepared) {
      return mMediaPlayer.getCurrentPosition();
    }
    return 0;
  }

  @Override
  public int getDuration() {// 获得所播放视频的总时间
    if (mMediaPlayer != null && mIsPrepared) {
      if (mDuration > 0) {
        return mDuration;
      }
      mDuration = mMediaPlayer.getDuration();
      return mDuration;
    }
    mDuration = -1;
    return mDuration;
  }

  public int getVideoHeight() {
    return mVideoHeight;
  }

  public int getVideoWidth() {
    return mVideoWidth;
  }

  @Override
  public boolean isPlaying() {// 判断是否正在播放视频
    if (mMediaPlayer != null && mIsPrepared) {
      return mMediaPlayer.isPlaying();
    }
    return false;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (mIsPrepared && keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP
        && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_MENU
        && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL
        && mMediaPlayer != null && mMediaController != null) {
      if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
        if (mMediaPlayer.isPlaying()) {
          pause();
          mMediaController.show();
        } else {
          start();
          mMediaController.hide();
        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP && mMediaPlayer.isPlaying()) {
        pause();
        mMediaController.show();
      } else {
        toggleMediaControlsVisiblity();// 切换媒体控制器是否可见
      }
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {// 实现该方法来处理触屏事件
    if (mIsPrepared && mMediaPlayer != null && mMediaController != null) {
      toggleMediaControlsVisiblity();// 切换媒体控制器是否可见
    }
    return false;
  }

  @Override
  public boolean onTrackballEvent(MotionEvent ev) {// 实现该方法来处理轨迹球动作事件。
    if (mIsPrepared && mMediaPlayer != null && mMediaController != null) {
      toggleMediaControlsVisiblity();// 切换媒体控制器是否可见
    }
    return false;
  }

  @Override
  public void pause() {// 暂停播放
    if (mMediaPlayer != null && mIsPrepared) {
      if (mMediaPlayer.isPlaying()) {
        mMediaPlayer.pause();
      }
    }
    mStartWhenPrepared = false;
  }

  @Override
  public void seekTo(int msec) {// 设置播放位置
    if (mMediaPlayer != null && mIsPrepared) {
      mMediaPlayer.seekTo(msec);
    } else {
      mSeekWhenPrepared = msec;
    }
  }

  /**
   * 设置自定义尺寸 改变的事件监听
   * 
   * @param l 视频显示尺寸 改变的事件监听
   */
  public void setMySizeChangeLinstener(MySizeChangeLinstener l) {
    mMyChangeLinstener = l;
  }

  /**
   * Register a callback to be invoked when the end of a media file has been reached during
   * playback.
   * 
   * @param l The callback that will be run
   */
  public void setOnCompletionListener(OnCompletionListener l)// 注册在媒体文件播放完毕时调用的回调函数
  {
    mOnCompletionListener = l;
  }

  /**
   * Register a callback to be invoked when an error occurs during playback or setup. If no listener
   * is specified, or if the listener returned false, VideoView will inform the user of any errors.
   * 
   * @param l The callback that will be run
   */
  public void setOnErrorListener(OnErrorListener l)// 注册在设置或播放过程中发生错误时调用的回调函数。如果未指定回调函数，
                                                   // 或回调函数返回假，VideoView 会通知用户发生了错误。
  {
    mOnErrorListener = l;
  }

  /**
   * Register a callback to be invoked when the media file is loaded and ready to go.
   * 
   * @param l The callback that will be run
   */
  public void setOnPreparedListener(MediaPlayer.OnPreparedListener l)// 注册在媒体文件加载完毕，可以播放时调用的回调函数
  {
    mOnPreparedListener = l;
  }

  public void setVideoPath(String path) {// 设置视频文件路径
    setVideoURI(Uri.parse(path));
  }

  /**
   * 设置视频宽、高
   * 
   * @param width
   * @param height
   */
  public void setVideoScale(int width, int height) {
    LayoutParams lp = getLayoutParams();
    lp.height = height;
    lp.width = width;
    setLayoutParams(lp);

  }

  public void setVideoURI(Uri uri) {// 设置视频文件URI
    mUri = uri;
    mStartWhenPrepared = false;// 开始什么时候缓冲好
    mSeekWhenPrepared = 0;// 指定进度什么时候缓冲好
    openVideo();// 打开视频
    requestLayout();// 当view确定自身已经不再适合现有的区域时，该view本身调用这个方法要求parent view重新调用他的onMeasure
                    // onLayout来对重新设置自己位置。
    invalidate();// View本身调用迫使view重画
  }

  @Override
  public void start() {// 开始播放
    if (mMediaPlayer != null && mIsPrepared) {
      mMediaPlayer.start();
      mStartWhenPrepared = false;
    } else {
      mStartWhenPrepared = true;
    }
  }

  public void stopPlayback() {// 停止视频播放
    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }

  /**
   * 评估视图及其内容，以决定其宽度和高度. 此方法由 measure(int, int) 调用，子类可以重载以提供更精确、更有效率的衡量其内容尺寸的方法
   */
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // Log.i("@@@@", "onMeasure");
    int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
    int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
    /*
     * if (mVideoWidth > 0 && mVideoHeight > 0) { if ( mVideoWidth * height > width * mVideoHeight )
     * { //Log.i("@@@", "image too tall, correcting"); height = width * mVideoHeight / mVideoWidth;
     * } else if ( mVideoWidth * height < width * mVideoHeight ) { //Log.i("@@@",
     * "image too wide, correcting"); width = height * mVideoWidth / mVideoHeight; } else {
     * //Log.i("@@@", "aspect ratio is correct: " + //width+"/"+height+"="+
     * //mVideoWidth+"/"+mVideoHeight); } }
     */
    // Log.i("@@@@@@@@@@", "setting size: " + width + 'x' + height);
    setMeasuredDimension(width, height);// 设置测量视频尺寸
  }

  private void attachMediaController() {// 附加媒体控制器
    if (mMediaPlayer != null && mMediaController != null) {
      mMediaController.setMediaPlayer(this);
      View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
      mMediaController.setAnchorView(anchorView);
      mMediaController.setEnabled(mIsPrepared);
    }
  }

  @SuppressWarnings("deprecation")
  private void initVideoView() {// 初始化VideoView
    mVideoWidth = 0;
    mVideoHeight = 0;
    getHolder().addCallback(mSHCallback);// 对surface对象的状态进行监听
    getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// 设置surfaceview不维护自己的缓冲区，而是等待屏幕的渲染引擎将内容推送到用户面前
    setFocusable(true);// 可获得焦点
    setFocusableInTouchMode(true);// 在触摸模式可获得焦点
    requestFocus();
  }

  private void openVideo() {// 打开视频
    if (mUri == null || mSurfaceHolder == null) {
      // not ready for playback just yet, will try again later
      return;
    }
    // Tell the music playback service to pause
    Intent i = new Intent("com.android.music.musicservicecommand");
    i.putExtra("command", "pause");
    mContext.sendBroadcast(i);

    if (mMediaPlayer != null) {
      mMediaPlayer.reset();// 重置
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
    try {
      mMediaPlayer = new MediaPlayer();// 媒体播放器
      mMediaPlayer.setOnPreparedListener(mPreparedListener);
      mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
      mMediaPlayer.setOnCompletionListener(mCompletionListener);
      mMediaPlayer.setOnErrorListener(mErrorListener);
      mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
      mIsPrepared = false;
      mDuration = -1;
      Log.v(TAG, "reset duration to -1 in openVideo");
      mCurrentBufferPercentage = 0;
      mMediaPlayer.setDataSource(mContext, mUri);
      mMediaPlayer.setDisplay(mSurfaceHolder);
      mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mMediaPlayer.setScreenOnWhilePlaying(true);
      mMediaPlayer.prepareAsync();// 为了不阻塞主线程而异步准备
      attachMediaController();// 附加媒体控制器
    } catch (IOException ex) {
      Log.w(TAG, "Unable to open content: " + mUri, ex);
      return;
    } catch (IllegalArgumentException ex) {
      Log.w(TAG, "Unable to open content: " + mUri, ex);
      return;
    }
  }

  // 切换媒体控制器是否可见
  private void toggleMediaControlsVisiblity() {
    if (mMediaController.isShowing()) {
      mMediaController.hide();
    } else {
      mMediaController.show();
    }
  }
}
