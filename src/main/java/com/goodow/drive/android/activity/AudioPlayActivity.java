package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.realtime.json.JsonObject;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
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

public class AudioPlayActivity extends Activity {
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

          case R.id.stop_Button:// 停止
            mediaPlayer.seekTo(0);
            mediaPlayer.pause();

            pauseButton.setText("暂停");
            progressSeekBar.setProgress(0);
            curtimeAndTotalTime.setText("时间：" + 0 / 1000 + " 秒" + " / " + mediaPlayer.getDuration()
                / 1000 + " 秒");

            pauseButton.setEnabled(false);
            stopButton.setEnabled(false);

            break;
        }
      } catch (Exception e) {// 抛出异常
        e.printStackTrace();
      }
    }
  };

  private static final String TAG = AudioPlayActivity.class.getSimpleName();
  private ButtonClickListener listener;
  private Button stopButton;
  private final MediaPlayer mediaPlayer = new MediaPlayer();

  private String audioFilePath;

  // 进度拖条
  private SeekBar progressSeekBar = null;

  // 当前时间和总时间
  private TextView curtimeAndTotalTime = null;

  // 音频文件的名字
  private TextView audioFileNameTextView;

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
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_audio_player);
    audioFileNameTextView = (TextView) this.findViewById(R.id.audio_file_name_textView);
    JsonObject jsonObject = (JsonObject) getIntent().getExtras().getSerializable("msg");
    audioFilePath = "/mnt/sdcard/" + jsonObject.getString("path");
    String mp3Name = audioFilePath.substring(audioFilePath.lastIndexOf("/") + 1);
    audioFileNameTextView.setText(mp3Name);

    listener = new ButtonClickListener();
    final Button playButton = (Button) this.findViewById(R.id.play_Button);
    pauseButton = (Button) this.findViewById(R.id.pause_Button);
    stopButton = (Button) this.findViewById(R.id.stop_Button);
    playButton.setOnClickListener(listener);
    pauseButton.setOnClickListener(listener);
    stopButton.setOnClickListener(listener);

    progressSeekBar = (SeekBar) findViewById(R.id.progress_rate_SeekBar);
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
        curtimeAndTotalTime.setText("时间：" + 0 / 1000 + " 秒" + " / " + mediaPlayer.getDuration()
            / 1000 + " 秒");

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
    super.onDestroy();
    Log.i(TAG, "onDestroy()");
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    JsonObject jsonObject = (JsonObject) intent.getExtras().getSerializable("msg");
    audioFilePath = "/mnt/sdcard/" + jsonObject.getString("path");
    Log.i(TAG, audioFilePath);
    String mp3Name = audioFilePath.substring(audioFilePath.lastIndexOf("/") + 1);
    Log.i(TAG, mp3Name);
    audioFileNameTextView.setText(mp3Name);
    // Resets the MediaPlayer to its uninitialized state. After calling this method, you will have
    // to initialize it again by setting the data source and calling prepare().
    mediaPlayer.reset();
    try {
      play();
    } catch (IOException e) {
      e.printStackTrace();
    }
    Log.i(TAG, "onNewIntent()");
  }

  @Override
  protected void onPause() {// 如果突然电话到来，停止播放音乐
    this.isVisible = false;
    if (mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
    }
    super.onPause();
    Log.i(TAG, "onPause()");
  }

  @Override
  protected void onResume() {
    this.isVisible = true;
    super.onResume();
    Log.i(TAG, "onResume");
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