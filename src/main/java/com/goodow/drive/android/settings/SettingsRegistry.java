package com.goodow.drive.android.settings;

import com.goodow.realtime.channel.EventBus;
import com.goodow.realtime.channel.EventHandler;
import com.goodow.realtime.json.JsonObject;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

public class SettingsRegistry {
  private final static String TAG = SettingsRegistry.class.getSimpleName();
  private EventBus mEventBus;
  private String mSid;
  private Context mContext;

  public SettingsRegistry(EventBus mEventBus, String mSid, Context mContext) {
    super();
    this.mEventBus = mEventBus;
    this.mSid = mSid;
    this.mContext = mContext;
  }

  public void handlerEventBus() {
    mEventBus.registerHandler(mSid + "drive.settings.audio", new EventHandler<JsonObject>() {
      @Override
      public void handler(JsonObject message, EventHandler<JsonObject> reply) {
        AudioManager mAudioManager =
            (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        if (message.has("mute")) {
          mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI);
        } else if (message.has("volume")) {
          double volume = message.getNumber("volume");
          mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (mAudioManager
              .getStreamMaxVolume(AudioManager.STREAM_MUSIC) * volume), AudioManager.FLAG_SHOW_UI);
        } else if (message.has("increase")) {
          double inCrease = message.getNumber("increase");
          mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (mAudioManager
              .getStreamVolume(AudioManager.STREAM_MUSIC) + mAudioManager
              .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
              * inCrease), AudioManager.FLAG_SHOW_UI);
          Log.i(TAG, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + "");
        } else if (message.has("decrease")) {
          double decrease = message.getNumber("decrease");
          mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (mAudioManager
              .getStreamVolume(AudioManager.STREAM_MUSIC) - mAudioManager
              .getStreamMaxVolume(AudioManager.STREAM_MUSIC)
              * decrease), AudioManager.FLAG_SHOW_UI);
          Log.i(TAG, mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) + "");
        }
      }
    });
  }
}
