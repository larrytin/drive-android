package com.goodow.drive.android.player;

import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.activity.BaseActivity;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import java.io.File;
import java.io.IOException;

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
import android.widget.Toast;

public class AudioPlayActivity extends BaseActivity {
  private final class ButtonClickListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
      try {
        JsonObject msg = Json.createObject();
        switch (v.getId()) {// 通过传过来的Buttonid可以判断Button的类型
          case R.id.play_Button:// 播放
            msg.set("play", 1);
            break;
          case R.id.pause_Button:// 暂停&继续
            msg.set("play", 2);
            break;
          case R.id.stop_Button:// 重播
            msg.set("play", 3);
            break;
        }
        bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, msg, null);
      } catch (Exception e) {// 抛出异常
        e.printStackTrace();
      }
    }
  }

  private static final String TAG = AudioPlayActivity.class.getSimpleName();
  private ButtonClickListener listener;
  private Button stopButton;;
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
  // private final Runnable start = new Runnable() {
  // @Override
  // public void run() {
  // handler.post(updatesb);
  // }
  // };

  private boolean tag = true;
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
        if (tag) {
          progressSeekBar.setProgress(position * sMax / mMax);
        }
        curtimeAndTotalTime.setText("时间：" + position / 1000 + " 秒" + " / " + mMax / 1000 + " 秒");
      } else if (100 == progress) {
        curtimeAndTotalTime.setText("时间：" + mMax / 1000 + " 秒" + " / " + mMax / 1000 + " 秒");
      }

      // 每秒钟更新一次
      handler.postDelayed(updatesb, 1000);
    }
  };
  private final MessageHandler<JsonObject> eventHandler = new MessageHandler<JsonObject>() {

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
            stop_Button();
            break;
          case 1:
            // 播放
            play_button();
            break;
          case 2:
            // 暂停
            pause_button();
            break;
          case 3:
            // 重播
            replay_button();
            break;
          default:
            Toast.makeText(AudioPlayActivity.this, "不支持的播放模式, play=" + msg.getNumber("play"),
                Toast.LENGTH_LONG).show();
            break;
        }
      }
      if (msg.has("progress")) {
        double progress = msg.getNumber("progress");
        // progressSeekBar.setProgress((int) progress * progressSeekBar.getMax());
        Log.d(TAG, (int) progress * progressSeekBar.getMax() + "");
        Log.d(TAG, progress * progressSeekBar.getMax() + ":handler");
        Log.d(TAG, "test:" + progress * progressSeekBar.getMax() + "");
        mediaPlayer.seekTo((int) (mediaPlayer.getDuration() * progress));
        mediaPlayer.start();
      }
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_audio_player);
    audioFileNameTextView = (TextView) this.findViewById(R.id.audio_file_name_textView);
    JsonObject jsonObject = (JsonObject) getIntent().getExtras().getSerializable("msg");
    audioFilePath = Constant.STORAGE_DIR + jsonObject.getString("path");
    File mFile = new File(audioFilePath);
    Log.d(TAG, audioFilePath);
    if (mFile.exists()) {
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
          Log.d(TAG, "onProgressChanged()");
          isVisible = true;
          if (fromUser) {
            Log.d(TAG, "onProgressChanged()+user");
            JsonObject msg = Json.createObject();
            msg.set("progress", (double) progress / progressSeekBar.getMax());
            bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, msg, null);
          }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
          Log.d(TAG, "onStartTrackingTouch()");
          if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
          }
          tag = false;
          // Log.d(TAG, seekBar.getProgress() + "");
          // System.out.println();
          // progressSeekBar.setProgress(seekBar.getProgress());
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          Log.d(TAG, "onstoptrackingtouch");
          tag = true;
          // int mMax = mediaPlayer.getDuration();
          // int dest = seekBar.getProgress();
          // int sMax = progressSeekBar.getMax();
          // mediaPlayer.seekTo(mMax * dest / sMax);
          // mediaPlayer.start();
          // pauseButton.setText("暂停");
          // pauseButton.setEnabled(true);
          // stopButton.setEnabled(true);

          // JsonObject msg = Json.createObject();
          // msg.set("progress", (double) seekBar.getProgress() / progressSeekBar.getMax());
          // bus.send(Bus.LOCAL + CONTROL, msg, null);
          // Log.d(TAG, "onstoptrackingtouch:" + seekBar.getProgress() / progressSeekBar.getMax());
        }
      });

      mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer arg0) {
          // mediaPlayer.seekTo(0);
          //
          // pauseButton.setText("暂停");
          // progressSeekBar.setProgress(0);
          // curtimeAndTotalTime.setText("时间：" + 0 / 1000 + " 秒" + " / " + mediaPlayer.getDuration()
          // / 1000 + " 秒");
          //
          // pauseButton.setEnabled(false);
          // stopButton.setEnabled(false);
        }
      });

      // pauseButton.setEnabled(false);
      // stopButton.setEnabled(false);
      try {
        play();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      Toast.makeText(this, R.string.pdf_file_no_exist, Toast.LENGTH_LONG).show();
    }

  }

  @Override
  protected void onDestroy() {
    mediaPlayer.release();
    super.onDestroy();
    Log.d(TAG, "onDestroy()");
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    JsonObject jsonObject = (JsonObject) intent.getExtras().getSerializable("msg");
    audioFilePath = Constant.STORAGE_DIR + jsonObject.getString("path");
    File mFile = new File(audioFilePath);
    Log.d(TAG, audioFilePath);
    if (mFile.exists()) {
      String mp3Name = audioFilePath.substring(audioFilePath.lastIndexOf("/") + 1);
      Log.d(TAG, mp3Name);
      audioFileNameTextView.setText(mp3Name);
      // Resets the MediaPlayer to its uninitialized state. After calling this method, you will have
      // to initialize it again by setting the data source and calling prepare().
      mediaPlayer.reset();
      try {
        play();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      Toast.makeText(this, R.string.pdf_file_no_exist, Toast.LENGTH_LONG).show();
    }

    Log.d(TAG, "onNewIntent()");
  }

  @Override
  protected void onPause() {// 如果突然电话到来，停止播放音乐
    super.onPause();
    this.isVisible = false;
    if (mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
    }

    // Always unregister when an handler no longer should be on the bus.
    bus.unregisterHandler(Constant.ADDR_PLAYER, eventHandler);
  }

  @Override
  protected void onResume() {
    this.isVisible = true;
    super.onResume();

    // Register handlers so that we can receive event messages.
    bus.registerHandler(Constant.ADDR_PLAYER, eventHandler);
  }

  // 正在播放时候，暂停，button变为继续
  private void pause_button() {
    if (mediaPlayer.isPlaying()) {
      // pauseButton.setText("继续");
      mediaPlayer.pause();
    }
  }

  // private void pause_Button() {
  // if (mediaPlayer.isPlaying()) {
  // mediaPlayer.pause();
  // pauseButton.setText("继续");
  // } else {
  // mediaPlayer.start();
  // pauseButton.setText("暂停");
  // }
  // }

  private void play() throws IOException {
    progressSeekBar.setProgress(0);

    mediaPlayer.setVolume(100, 100);
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mediaPlayer.setDataSource(audioFilePath);
    mediaPlayer.prepare();

    handler.post(updatesb);
    // 一进去就开始播放
    mediaPlayer.start();
  }

  private void play_button() {
    isVisible = true;
    if (!mediaPlayer.isPlaying() && mediaPlayer.getCurrentPosition() != mediaPlayer.getDuration()) {
      mediaPlayer.start();
      // pauseButton.setText("暂停");
    }
    // else if (mediaPlayer.getCurrentPosition() == mediaPlayer.getDuration()) {
    // Log.d(TAG, "play_button");
    // mediaPlayer.seekTo(0);
    // mediaPlayer.start();
    // pauseButton.setText("暂停");
    // pauseButton.setEnabled(true);
    // stopButton.setEnabled(true);
    // }
    // handler.post(start);
    // pauseButton.setEnabled(true);
    // stopButton.setEnabled(true);
    handler.post(updatesb);
  }

  // private void play_Button() {
  // isVisible = true;
  //
  // mediaPlayer.seekTo(0);
  // mediaPlayer.start();
  //
  // pauseButton.setText("暂停");
  // pauseButton.setEnabled(true);
  // stopButton.setEnabled(true);
  //
  // handler.post(start);
  // }

  private void replay_button() {
    isVisible = true;

    mediaPlayer.seekTo(0);
    mediaPlayer.start();
    progressSeekBar.setProgress(0);
    pauseButton.setText("暂停");
    pauseButton.setEnabled(true);
    stopButton.setEnabled(true);

    handler.post(updatesb);
  }

  private void stop_Button() {
    mediaPlayer.seekTo(0);
    mediaPlayer.pause();

    // pauseButton.setText("暂停");
    progressSeekBar.setProgress(0);
    curtimeAndTotalTime.setText("时间：" + 0 / 1000 + " 秒" + " / " + mediaPlayer.getDuration() / 1000
        + " 秒");
    //
    // pauseButton.setEnabled(false);
    // stopButton.setEnabled(false);
  }
}