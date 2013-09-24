package com.goodow.drive.android.activity.play;

import com.goodow.android.drive.R;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;

import java.io.File;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class VideoPlayActivity extends Activity {
  public static enum IntentExtraTagEnum {
    // mp4 资源名称
    MP4_NAME,
    // MP4资源完整path
    MP4_PATH
  }

  private final class ButtonClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      // 判断SD卡是否存在
      if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
        Toast.makeText(VideoPlayActivity.this, "SDCard不存在", Toast.LENGTH_SHORT).show();
        return;
      }

      try {
        switch (v.getId()) {
        case R.id.play_ImageButton:
          play();
          break;

        case R.id.pause_ImageButton:
          if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
          } else {
            mediaPlayer.start();
          }
          break;
        case R.id.reset_ImageButton:
          if (mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(0);
          } else {
            play();
          }
          break;
        case R.id.stop_ImageButton:
          if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
          }
          break;
        }
      } catch (Exception e) {
        Log.e(TAG, e.toString());
      }
    }
  };

  private final class SurfaceCallback implements SurfaceHolder.Callback {
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
      if (position > 0 && audioFilePath != null) {
        try {
          play();
          mediaPlayer.seekTo(position);
          position = 0;
        } catch (Exception e) {
          Log.e(TAG, e.toString());
        }
      }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
      if (mediaPlayer.isPlaying()) {
        position = mediaPlayer.getCurrentPosition();
        mediaPlayer.stop();
      }
    }
  }

  private final String TAG = this.getClass().getSimpleName();
  private MediaPlayer mediaPlayer = new MediaPlayer();
  private String audioFilePath;

  private SurfaceView surfaceView;

  private int position;

  @SuppressWarnings("deprecation")
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    GlobalDataCacheForMemorySingleton.getInstance.addActivity(this);

    setContentView(R.layout.activity_video_player);

    // 获取从外部传进来的 mp4资源完整路径
    audioFilePath = getIntent().getStringExtra(IntentExtraTagEnum.MP4_PATH.name());

    // 获取从外部传进来的 mp4资源名字
    String mp3Name = getIntent().getStringExtra(IntentExtraTagEnum.MP4_NAME.name());

    final TextView audioFileNameTextView = (TextView) this.findViewById(R.id.video_file_name_textView);
    audioFileNameTextView.setText(mp3Name);
    surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
    surfaceView.getHolder().setFixedSize(176, 144);// 设置Surface分辨率
    surfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    surfaceView.getHolder().addCallback(new SurfaceCallback());

    mediaPlayer = new MediaPlayer();
    ButtonClickListener listener = new ButtonClickListener();
    ImageButton playButton = (ImageButton) this.findViewById(R.id.play_ImageButton);
    ImageButton pauseButton = (ImageButton) this.findViewById(R.id.pause_ImageButton);
    ImageButton resetButton = (ImageButton) this.findViewById(R.id.reset_ImageButton);
    ImageButton stopButton = (ImageButton) this.findViewById(R.id.stop_ImageButton);
    playButton.setOnClickListener(listener);
    pauseButton.setOnClickListener(listener);
    resetButton.setOnClickListener(listener);
    stopButton.setOnClickListener(listener);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    GlobalDataCacheForMemorySingleton.getInstance.removeActivity(this);
  }

  // 播放
  private void play() {
    File videoFile = new File(audioFilePath);
    // 视频文件是否存在
    if (videoFile.exists() && videoFile.length() > 0) {
      try {
        mediaPlayer.reset();// ̬重置为初始状态
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        /* 设置Video影片以SurfaceHolder播放 */
        mediaPlayer.setDisplay(surfaceView.getHolder());
        mediaPlayer.setDataSource(videoFile.getAbsolutePath());
        mediaPlayer.prepare();// 准备
        mediaPlayer.start();// 播放
      } catch (Exception e) {
        e.printStackTrace();
        Toast.makeText(this, "播放失败", Toast.LENGTH_LONG).show();
      }
    }

  }
}