package com.goodow.drive.android.player;

import com.goodow.android.drive.R;
import com.goodow.drive.android.BusProvider;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.activity.BaseActivity;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.Message;
import com.goodow.realtime.channel.MessageHandler;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;

import java.io.File;
import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
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
            if (mediaPlayer.isPlaying()) {
              msg.set("play", 2);
            } else {
              msg.set("play", 1);
            }
            break;
          case R.id.stop_Button:// 重播
            msg.set("play", 3);
            break;
          case R.id.sound_Button:// 声音
            sound_Button.setClickable(false);
            sound_Button.setImageResource(R.drawable.common_player_mute);
            progress_sound_SeekBar.setProgress(0);
            bus.send(Bus.LOCAL + BusProvider.SID + "audio", Json.createObject().set("action",
                "post").set("volume", 0.0), null);
            return;
        }
        bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, msg, null);
      } catch (Exception e) {// 抛出异常
        e.printStackTrace();
      }
    }
  }
  private class SoundBroadCastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      progress_sound_SeekBar.setProgress(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC));
      if (mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 0) {
        sound_Button.setImageResource(R.drawable.common_player_mute);
        sound_Button.setClickable(false);
      } else {
        sound_Button.setImageResource(R.drawable.common_player_sound);
        sound_Button.setClickable(true);
        sound_Button.setOnClickListener(listener);
      }
    }
  }

  private AudioManager mAudioManager;
  private SoundBroadCastReceiver soundBroadCastReceiver;

  private ButtonClickListener listener;
  private ImageView playButton;
  private ImageView stopButton;
  // 声音/静音
  private ImageView sound_Button;
  private SeekBar progress_sound_SeekBar;
  private final MediaPlayer mediaPlayer = new MediaPlayer();
  private String audioFilePath;
  // 进度拖条
  private SeekBar progressSeekBar = null;
  // 当前时间和总时间
  private TextView curtimeAndTotalTime = null;

  // 音频文件的名字
  private TextView audioFileNameTextView;
  private boolean isVisible = false;
  private final Handler handler = new Handler();
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

        curtimeAndTotalTime.setText(timerCalculate(position / 1000) + " / "
            + timerCalculate(mMax / 1000));
      } else if (100 == progress) {
        curtimeAndTotalTime.setText(timerCalculate(mMax / 1000) + " / "
            + timerCalculate(mMax / 1000));
      }

      // 每秒钟更新一次
      handler.postDelayed(updatesb, 1000);
    }
  };

  private final MessageHandler<JsonObject> controlHandler = new MessageHandler<JsonObject>() {
    @Override
    public void handle(Message<JsonObject> message) {
      JsonObject msg = message.body();
      if (msg.has("path")) {
        return;
      }
      handleControl(msg);
    }
  };
  private ImageView mImageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.activity_audio_player);
    this.sound_Button = (ImageView) this.findViewById(R.id.sound_Button);
    mAudioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
    soundBroadCastReceiver = new SoundBroadCastReceiver();
    this.progress_sound_SeekBar = (SeekBar) this.findViewById(R.id.progress_sound_SeekBar);
    this.progress_sound_SeekBar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
    // 设置声音拖动事件
    this.progress_sound_SeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

      @Override
      public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
          bus.send(Bus.LOCAL + BusProvider.SID + "audio", Json.createObject().set("action", "post")
              .set("volume", (float) progress / progress_sound_SeekBar.getMax()), null);
        }
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    // 发送消息获取音量
    bus.send(Bus.LOCAL + BusProvider.SID + "audio", Json.createObject().set("action", "get"),
        new MessageHandler<JsonObject>() {
          @Override
          public void handle(Message<JsonObject> message) {
            JsonObject body = message.body();
            boolean isMute = body.getBoolean("mute");
            float volume = (float) body.getNumber("volume");
            if (isMute) {
              sound_Button.setImageResource(R.drawable.common_player_mute);
              sound_Button.setClickable(false);
              progress_sound_SeekBar.setProgress(0);
            } else {
              sound_Button.setImageResource(R.drawable.common_player_sound);
              sound_Button.setClickable(true);
              progress_sound_SeekBar.setProgress((int) (volume * mAudioManager
                  .getStreamMaxVolume(AudioManager.STREAM_MUSIC)));
            }
          }
        });

    mImageView = (ImageView) this.findViewById(R.id.iv_act_favour_back);
    mImageView.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        bus.send(Bus.LOCAL + Constant.ADDR_CONTROL, Json.createObject().set("return", true), null);
      }
    });
    audioFileNameTextView = (TextView) this.findViewById(R.id.audio_file_name_textView);
    JsonObject msg = (JsonObject) getIntent().getExtras().getSerializable("msg");
    audioFilePath = msg.getString("path");
    File mFile = new File(audioFilePath);
    if (mFile.exists()) {
      String mp3Name = audioFilePath.substring(audioFilePath.lastIndexOf("/") + 1);
      audioFileNameTextView.setMaxEms(10);
      audioFileNameTextView.setText(mp3Name);

      listener = new ButtonClickListener();
      playButton = (ImageView) this.findViewById(R.id.play_Button);
      stopButton = (ImageView) this.findViewById(R.id.stop_Button);
      playButton.setOnClickListener(listener);
      stopButton.setOnClickListener(listener);

      progressSeekBar = (SeekBar) findViewById(R.id.progress_rate_SeekBar);
      curtimeAndTotalTime = (TextView) findViewById(R.id.curtime_and_total_time_TextView);
      progressSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          isVisible = true;
          if (fromUser) {
            JsonObject msg = Json.createObject();
            msg.set("progress", (double) progress / progressSeekBar.getMax());
            bus.send(Bus.LOCAL + Constant.ADDR_PLAYER, msg, null);
          }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
          if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
          }
          tag = false;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          tag = true;
        }
      });

      mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer arg0) {
        }
      });

      try {
        start();
        this.pause_button();
      } catch (Exception e) {
      }

      handleControl(msg);
    } else {
      Toast.makeText(this, R.string.pdf_file_no_exist, Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onDestroy() {
    mediaPlayer.release();
    super.onDestroy();
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    JsonObject msg = (JsonObject) intent.getExtras().getSerializable("msg");
    audioFilePath = msg.getString("path");
    File mFile = new File(audioFilePath);
    if (mFile.exists()) {
      String mp3Name = audioFilePath.substring(audioFilePath.lastIndexOf("/") + 1);
      audioFileNameTextView.setText(mp3Name);
      mediaPlayer.reset();
      try {
        start();
        this.pause_button();
      } catch (Exception e) {
      }
      handleControl(msg);
    } else {
      Toast.makeText(this, R.string.pdf_file_no_exist, Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onPause() {// 如果突然电话到来，停止播放音乐
    super.onPause();
    this.isVisible = false;
    if (mediaPlayer.isPlaying()) {
      mediaPlayer.pause();
    }

    // Always unregister when an handler no longer should be on the bus.
    bus.unregisterHandler(Constant.ADDR_PLAYER, controlHandler);
    this.unregisterReceiver(soundBroadCastReceiver);
  }

  @Override
  protected void onResume() {
    this.isVisible = true;
    super.onResume();

    // Register handlers so that we can receive event messages.
    bus.registerHandler(Constant.ADDR_PLAYER, controlHandler);
    IntentFilter mIntentFilter = new IntentFilter();
    mIntentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
    this.registerReceiver(soundBroadCastReceiver, mIntentFilter);
  }

  private void handleControl(JsonObject msg) {
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
      mediaPlayer.seekTo((int) (mediaPlayer.getDuration() * progress));
      mediaPlayer.start();
    }
  }

  // 正在播放时候，暂停，button变为继续
  private void pause_button() {
    if (mediaPlayer.isPlaying()) {
      // pauseButton.setText("继续");
      mediaPlayer.pause();
      this.playButton.setImageResource(R.drawable.common_player_play);
    }
  }

  private void play_button() {
    isVisible = true;

    if (!mediaPlayer.isPlaying() && mediaPlayer.getCurrentPosition() != mediaPlayer.getDuration()) {
      mediaPlayer.start();
      playButton.setImageResource(R.drawable.common_player_pause);
    }
    handler.post(updatesb);
  }

  private void replay_button() {
    isVisible = true;

    mediaPlayer.seekTo(0);
    mediaPlayer.start();
    progressSeekBar.setProgress(0);
    stopButton.setEnabled(true);

    handler.post(updatesb);

    this.playButton.setImageResource(R.drawable.common_player_pause);
  }

  private void start() throws IOException {
    progressSeekBar.setProgress(0);

    mediaPlayer.setVolume(100, 100);
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mediaPlayer.setDataSource(audioFilePath);
    mediaPlayer.prepare();

    handler.post(updatesb);
    // 一进去就开始播放
    mediaPlayer.start();
  }

  private void stop_Button() {
    mediaPlayer.seekTo(0);
    mediaPlayer.pause();

    progressSeekBar.setProgress(0);
    curtimeAndTotalTime.setText(timerCalculate(0) + " / "
        + timerCalculate(mediaPlayer.getDuration() / 1000));
  }

  private String timerCalculate(int secondTime) {
    String timeStr = null;
    int hour = 0;
    int minute = 0;
    int second = 0;
    if (secondTime <= 0) {
      return "00:00";
    } else {
      minute = secondTime / 60;
      if (minute < 60) {
        second = secondTime % 60;
        timeStr = unitFormat(minute) + ":" + unitFormat(second);
      } else {
        hour = minute / 60;
        if (hour > 99) {
          return "99:59:59";
        }
        minute = minute % 60;
        second = secondTime - hour * 3600 - minute * 60;
        timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
      }
    }
    return timeStr;
  }

  private String unitFormat(int i) {
    String retStr = null;
    if (i >= 0 && i < 10) {
      retStr = "0" + Integer.toString(i);
    } else {
      retStr = "" + i;
    }
    return retStr;
  }
}