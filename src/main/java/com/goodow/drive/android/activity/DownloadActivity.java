package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;

public class DownloadActivity extends Activity {
  /**
   * InnerClass: 后台下载一张
   */
  private class DownlaodTask extends AsyncTask<String, Void, Bitmap> {
    @Override
    protected Bitmap doInBackground(String... params) {
      HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) {
          request.setParser(new JsonObjectParser(JSON_FACTORY));
        }
      });

      Bitmap bitmap = null;
      HttpRequest request;
      try {
        request = requestFactory.buildGetRequest(new GenericUrl(params[0]));
        HttpResponse response = request.execute();
        InputStream is_Save = response.getContent();
        InputStream is_Bitmap = response.getContent();
        bitmap = BitmapFactory.decodeStream(is_Bitmap);// 此方法会破坏输入流的数据,使其无法正常read进行保存下载操作,所以需要定义两个输入流

        // 下载至本地start
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = is_Save.read(buffer)) != -1) {
          bos.write(buffer, 0, len);
        }

        byte[] dates = bos.toByteArray();
        OutputStream os = openFileOutput("another.jpg", MODE_PRIVATE);
        os.write(dates);
        // 下载至本地end

        os.close();
        is_Save.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
      return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
      super.onPostExecute(result);
      image.setImageBitmap(result);
    }
  }

  /**
   * InnerClass: 后台下载多张
   */
  private class DownlaodTasks extends AsyncTask<String, Bitmap, Long> {
    @Override
    protected Long doInBackground(String... params) {
      long downloadCount = 0;

      HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
        @Override
        public void initialize(HttpRequest request) {
          request.setParser(new JsonObjectParser(JSON_FACTORY));
        }
      });

      Bitmap bitmap = null;
      HttpRequest request;
      try {

        for (int i = 0; i < params.length; i++) {
          request = requestFactory.buildGetRequest(new GenericUrl(params[i]));
          HttpResponse response = request.execute();
          InputStream is = response.getContent();
          bitmap = BitmapFactory.decodeStream(is);

          if (null != bitmap) {
            downloadCount++;

            try {
              Thread.sleep(3000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }

            publishProgress(bitmap);
          }
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
      return downloadCount;
    }

    @Override
    protected void onProgressUpdate(Bitmap... values) {
      super.onProgressUpdate(values);

      image.setImageBitmap(values[0]);
    }
  }

  public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();
  public static final JsonFactory JSON_FACTORY = new JacksonFactory();
  public static final String URL = "http://imgsrc.baidu.com/forum/crop%3D144%2C0%2C278%2C236%3Bwh%3D200%2C170%3B/sign=16617bd69358d109d0acf3f2ec69f88b/5d2048fbfbedab64c87a5568f636afc379311e39.jpg";
  public static final String URL_2 = "http://tieba.baidu.com/tb/0618fangyuan-11.jpg";
  public static final String URL_3 = "http://imgsrc.baidu.com/forum/crop%3D0%2C23%2C272%2C231%3Bwh%3D200%2C170%3B/sign=a8033945d000baa1ae631dfb7a209520/d24133d12f2eb938ac6e8e5bd4628535e4dd6fba.jpg";

  private ImageView image;

  /**
   * Called when the activity is first created.
   * 
   * @param savedInstanceState
   *          If the activity is being re-initialized after previously being
   *          shut down then this Bundle contains the data it most recently
   *          supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is
   *          null.</b>
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_download);

    image = (ImageView) findViewById(R.id.img);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  public void onSave(View view) {
    DownlaodTask dt = new DownlaodTask();
    dt.execute(URL);
  }

  public void onSaves(View view) {
    DownlaodTasks dt = new DownlaodTasks();
    dt.execute(URL, URL_2, URL_3);
  }

}
