package com.goodow.drive.android.activity.play;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.goodow.android.drive.R;

@SuppressLint("SdCardPath")
public class AudioPlayActivity extends Activity {
	private final String TAG = this.getClass().getSimpleName();

	public static enum IntentExtraTagEnum {
		// mp3 资源名称
		MP3_NAME,
		// MP3 资源完整path
		MP3_PATH
	};

	private MediaPlayer mediaPlayer = new MediaPlayer();
	private String audioFilePath;

	// 进度 拖动条
	private SeekBar progressSeekBar = null;

	// 当前进度
	private TextView curProgressText = null;
	// 当前时间和总时间
	private TextView curtimeAndTotalTime = null;

	private Button pauseButton;

	private boolean isVisible = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_audio_player);

		// 获取从外部传进来的 mp3资源完整路径
		audioFilePath = getIntent().getStringExtra(
				IntentExtraTagEnum.MP3_PATH.name());

		// 获取从外部传进来的 mp3资源完整路径
		String mp3Name = getIntent().getStringExtra(
				IntentExtraTagEnum.MP3_NAME.name());

		final TextView audioFileNameTextView = (TextView) this
				.findViewById(R.id.audio_file_name_textView);
		audioFileNameTextView.setText(mp3Name);

		final ButtonClickListener listener = new ButtonClickListener();
		final Button playButton = (Button) this.findViewById(R.id.play_Button);
		pauseButton = (Button) this.findViewById(R.id.pause_Button);
		final Button resetButton = (Button) this
				.findViewById(R.id.reset_Button);
		final Button stopButton = (Button) this.findViewById(R.id.stop_Button);
		playButton.setOnClickListener(listener);
		pauseButton.setOnClickListener(listener);
		resetButton.setOnClickListener(listener);
		stopButton.setOnClickListener(listener);

		progressSeekBar = (SeekBar) findViewById(R.id.progress_rate_SeekBar);
		curProgressText = (TextView) findViewById(R.id.current_progress_TextView);
		curtimeAndTotalTime = (TextView) findViewById(R.id.curtime_and_total_time_TextView);
		progressSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						/* 如果拖动进度发生改变，则显示当前进度值 */
						curProgressText.setText("当前进度: " + progress);
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						curProgressText.setText("拖动中...");
					}

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						int dest = seekBar.getProgress();
						int mMax = mediaPlayer.getDuration();
						int sMax = progressSeekBar.getMax();
						mediaPlayer.seekTo(mMax * dest / sMax);
					}
				});

		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer arg0) {
				// 播放音频结束, 重置
			}
		});

	}

	@Override
	protected void onPause() {// 如果突然电话到来，停止播放音乐
		this.isVisible = false;
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		this.isVisible = true;
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mediaPlayer.release();
		super.onDestroy();
		Log.i(TAG, "onDestroy()");
	}

	private final class ButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Button button = (Button) v;// 得到button
			try {
				switch (v.getId()) {//通过传过来的Buttonid可以判断Button的类型
				case R.id.play_Button:// 播放
					pauseButton.setText("暂停");
					play();
					break;

				case R.id.pause_Button:
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.pause();
						button.setText("继续");// 让这个按钮上的文字显示为继续
					} else {
						mediaPlayer.start();// 继续播放
						button.setText("暂停");
					}
					break;

				case R.id.reset_Button:
					if (mediaPlayer.isPlaying()) {
						mediaPlayer.seekTo(0);// 让它从0开始播放
					} else {
						play();// 如果它没有播放，就让它开始播放
					}
					break;

				case R.id.stop_Button:
					if (mediaPlayer.isPlaying())
						mediaPlayer.stop();// 如果它正在播放的话，就让他停止ֹ
					break;
				}
			} catch (Exception e) {// 抛出异常
				Log.e(TAG, e.toString());
			}
		}
	}

	private void play() throws IOException {
		mediaPlayer.setVolume(100, 100);
		mediaPlayer.reset();
		mediaPlayer.setDataSource(audioFilePath);
		mediaPlayer.prepare();
		mediaPlayer.start();// 播放

		startSeekBarUpdate();
	}

	private Handler handler = new Handler();

	private void startSeekBarUpdate() {
		handler.post(start);
	}

	private Runnable start = new Runnable() {
		@Override
		public void run() {
			handler.post(updatesb);
		}
	};
	private Runnable updatesb = new Runnable() {
		@Override
		public void run() {
			if (!isVisible) {
				return;
			}

			int position = mediaPlayer.getCurrentPosition();
			int mMax = mediaPlayer.getDuration();
			int sMax = progressSeekBar.getMax();
			if (mMax != 0) {
				progressSeekBar.setProgress(position * sMax / mMax);
				curtimeAndTotalTime.setText("当前播放时间" + position / 1000 + "秒"
						+ "\n歌曲总时间: " + mMax / 1000 + "秒");
			}

			// 每秒钟更新一次
			handler.postDelayed(updatesb, 1000);
		}
	};
}