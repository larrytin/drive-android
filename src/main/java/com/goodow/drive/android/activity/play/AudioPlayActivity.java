package com.goodow.drive.android.activity.play;

import com.goodow.android.drive.R;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

@SuppressLint("SdCardPath")
public class AudioPlayActivity extends Activity {
  public static enum IntentExtraTagEnum {
    // mp3 资源名称
    MP3_NAME,
    // MP3 资源完整path
    MP3_PATH
  }

  private final class ButtonClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      try {
        switch (v.getId()) {// 通过传过来的Buttonid可以判断Button的类型
        case R.id.play_Button:// 播放
          isVisible = true;

          mediaPlayer.seekTo(0);
          mediaPlayer.start();

          pauseButton.setText("暂停");
          pauseButton.setEnabled(true);
          stopButton.setEnabled(true);

          handler.post(start);
          break;
        case R.id.pause_Button:// 暂停&继续
          if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            pauseButton.setText("继续");
          } else {
            mediaPlayer.start();
            pauseButton.setText("暂停");
          }

          break;
        // case R.id.reset_Button:
        // if (mediaPlayer.isPlaying()) {
        // mediaPlayer.seekTo(0);// 让它从0开始播放
        // } else {
        // play();// 如果它没有播放，就让它开始播放
        // }
        // break;
        case R.id.stop_Button:// 停止
          mediaPlayer.seekTo(0);
          mediaPlayer.pause();

          pauseButton.setText("暂停");
          progressSeekBar.setProgress(0);
          curtimeAndTotalTime.setText("时间：" + 0 / 1000 + " 秒" + " / " + mediaPlayer.getDuration() / 1000 + " 秒");

          pauseButton.setEnabled(false);
          stopButton.setEnabled(false);

          break;
        }
      } catch (Exception e) {// 抛出异常
        Log.e(TAG, e.toString());
      }
    }
  };

  private final String TAG = this.getClass().getSimpleName();
  private ButtonClickListener listener;
  private Button stopButton;
  private final MediaPlayer mediaPlayer = new MediaPlayer();

  private String audioFilePath;

  // 进度拖条
  private SeekBar progressSeekBar = null;

  // 当前进度
  // private TextView curProgressText = null;
  // 当前时间和总时间
  private TextView curtimeAndTotalTime = null;

  private Button pauseButton;

  private boolean isVisible = false;

  private final Handler handler = new Handler();

  private final Runnable start = new Runnable() {
    @Override
    public void run() {
      handler.post(updatesb);
    }
  };

  private final Runnable updatesb = new Runnable() {
    @Override
    public void run() {
      if (!isVisible) {
        return;
      }

      int position = mediaPlayer.getCurrentPosition();
      int mMax = mediaPlayer.getDuration();
      int sMax = progressSeekBar.getMax();
      int progress = progressSeekBar.getProgress();
      if (100 != progress) {
        progressSeekBar.setProgress(position * sMax / mMax);
        curtimeAndTotalTime.setText("时间：" + position / 1000 + " 秒" + " / " + mMax / 1000 + " 秒");
      } else if (100 == progress) {
        curtimeAndTotalTime.setText("时间：" + mMax / 1000 + " 秒" + " / " + mMax / 1000 + " 秒");
      }

      // 每秒钟更新一次
      handler.postDelayed(updatesb, 1000);
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    GlobalDataCacheForMemorySingleton.getInstance.addActivity(this);

    setContentView(R.layout.activity_audio_player);

    // 获取从外部传进来的 mp3资源完整路径
    audioFilePath = getIntent().getStringExtra(IntentExtraTagEnum.MP3_PATH.name());

    // 获取从外部传进来的 mp3资源完整路径
    String mp3Name = getIntent().getStringExtra(IntentExtraTagEnum.MP3_NAME.name());

    final TextView audioFileNameTextView = (TextView) this.findViewById(R.id.audio_file_name_textView);
    audioFileNameTextView.setText(mp3Name);

    listener = new ButtonClickListener();
    final Button playButton = (Button) this.findViewById(R.id.play_Button);
    pauseButton = (Button) this.findViewById(R.id.pause_Button);
    // final Button resetButton = (Button) this.findViewById(R.id.reset_Button);
    stopButton = (Button) this.findViewById(R.id.stop_Button);
    playButton.setOnClickListener(listener);
    pauseButton.setOnClickListener(listener);
    stopButton.setOnClickListener(listener);
    // resetButton.setOnClickListener(listener);

    progressSeekBar = (SeekBar) findViewById(R.id.progress_rate_SeekBar);
    // curProgressText = (TextView)
    // findViewById(R.id.current_progress_TextView);
    curtimeAndTotalTime = (TextView) findViewById(R.id.curtime_and_total_time_TextView);
    progressSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        isVisible = true;
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
        if (mediaPlayer.isPlaying()) {
          mediaPlayer.pause();
        }
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
        int mMax = mediaPlayer.getDuration();
        int dest = seekBar.getProgress();
        int sMax = progressSeekBar.getMax();
        mediaPlayer.seekTo(mMax * dest / sMax);
        mediaPlayer.start();

        pauseButton.setText("暂停");
        pauseButton.setEnabled(true);
        stopButton.setEnabled(true);
      }
    });

    mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer arg0) {
        mediaPlayer.seekTo(0);

        pauseButton.setText("暂停");
        progressSeekBar.setProgress(0);
        curtimeAndTotalTime.setText("时间：" + 0 / 1000 + " 秒" + " / " + mediaPlayer.getDuration() / 1000 + " 秒");

        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);
      }
    });

    pauseButton.setEnabled(false);
    stopButton.setEnabled(false);
    try {
      play();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onDestroy() {
    mediaPlayer.release();

    GlobalDataCacheForMemorySingleton.getInstance.removeActivity(this);

    super.onDestroy();
  }

  @Override
  protected void onPause() {// 如果突然电话到来，停止播放音乐
    this.isVisible = false;
    if (mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
    }
    super.onPause();
  }

  @Override
  protected void onResume() {
    this.isVisible = true;
    super.onResume();
  }

  private void play() throws IOException {
    progressSeekBar.setProgress(0);

    mediaPlayer.setVolume(100, 100);
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mediaPlayer.setDataSource(audioFilePath);
    mediaPlayer.prepare();

    handler.post(start);
  }
}