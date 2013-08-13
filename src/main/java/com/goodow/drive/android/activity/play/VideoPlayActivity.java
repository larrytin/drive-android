package com.goodow.drive.android.activity.play;

import java.io.File;
import java.io.IOException;

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

import com.goodow.android.drive.R;

public class VideoPlayActivity extends Activity {
	private final String TAG = this.getClass().getSimpleName();

	public static enum IntentExtraTagEnum {
		// mp4 ��Դ���
		MP4_NAME,
		// MP4 ��Դ����path
		MP4_PATH
	};

	private MediaPlayer mediaPlayer = new MediaPlayer();
	private String audioFilePath;
	private SurfaceView surfaceView;
	private int position;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_player);

		// ��ȡ���ⲿ�������� mp4��Դ����·��
		audioFilePath = getIntent().getStringExtra(
				IntentExtraTagEnum.MP4_PATH.name());

		// ��ȡ���ⲿ�������� mp4��Դ����·��
		String mp3Name = getIntent().getStringExtra(
				IntentExtraTagEnum.MP4_NAME.name());

		final TextView audioFileNameTextView = (TextView) this
				.findViewById(R.id.video_file_name_textView);
		audioFileNameTextView.setText(mp3Name);
		surfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
		surfaceView.getHolder().setFixedSize(176, 144);// ���÷ֱ���
		surfaceView.getHolder()
				.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		surfaceView.getHolder().addCallback(new SurfaceCallback());

		mediaPlayer = new MediaPlayer();
		ButtonClickListener listener = new ButtonClickListener();
		ImageButton playButton = (ImageButton) this
				.findViewById(R.id.play_ImageButton);
		ImageButton pauseButton = (ImageButton) this
				.findViewById(R.id.pause_ImageButton);
		ImageButton resetButton = (ImageButton) this
				.findViewById(R.id.reset_ImageButton);
		ImageButton stopButton = (ImageButton) this
				.findViewById(R.id.stop_ImageButton);
		playButton.setOnClickListener(listener);
		pauseButton.setOnClickListener(listener);
		resetButton.setOnClickListener(listener);
		stopButton.setOnClickListener(listener);
	}

	private final class SurfaceCallback implements SurfaceHolder.Callback {
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			if (position > 0 && audioFilePath != null) {
				try {
					play();
					mediaPlayer.seekTo(position);
					position = 0;
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				}
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mediaPlayer.isPlaying()) {
				position = mediaPlayer.getCurrentPosition();
				mediaPlayer.stop();
			}
		}
	}

	private final class ButtonClickListener implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (!Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				Toast.makeText(VideoPlayActivity.this, "SDCard������", Toast.LENGTH_SHORT).show();
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
					if (mediaPlayer.isPlaying())
						mediaPlayer.stop();
					break;
				}
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	private void play() throws IOException {
		File videoFile = new File(audioFilePath);
		mediaPlayer.reset();// ����Ϊ��ʼ״̬
		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		/* ����VideoӰƬ��SurfaceHolder���� */
		mediaPlayer.setDisplay(surfaceView.getHolder());
		mediaPlayer.setDataSource(videoFile.getAbsolutePath());
		mediaPlayer.prepare();// ����
		mediaPlayer.start();// ����
	}
}