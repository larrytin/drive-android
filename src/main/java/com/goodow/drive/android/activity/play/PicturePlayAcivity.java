package com.goodow.drive.android.activity.play;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import com.goodow.android.drive.R;

@ContentView(R.layout.activity_picture_player)
public class PicturePlayAcivity extends RoboActivity {
  public static String PICTUREURL = "pictureUrl";
 
  @InjectView(R.id.picture)
  private ImageView imageView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    String pictureUrl = intent.getStringExtra(PICTUREURL);

    if (null != pictureUrl) {
      new InitImageBitmapTask().execute(pictureUrl);
    }
  }

  private class InitImageBitmapTask extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... params) {
      Bitmap bitmap = null;
      
      try {
        URLConnection connection = (new URL(params[0]).openConnection());
        InputStream bitmapStream = connection.getInputStream();
        bitmap = BitmapFactory.decodeStream(bitmapStream);
      } catch (IOException e) {
        e.printStackTrace();
      }
      
      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      super.onPostExecute(result);
      imageView.setImageBitmap(result);
    }
  }
}
