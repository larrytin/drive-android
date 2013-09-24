package com.goodow.drive.android.activity;

import com.goodow.android.drive.R;
import com.goodow.api.services.account.Account;
import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.drive.android.toolutils.LoginNetRequestTask;
import com.goodow.drive.android.toolutils.SimpleProgressDialog;
import com.goodow.realtime.android.CloudEndpointUtils;
import com.goodow.realtime.android.RealtimeModule;
import com.goodow.realtime.android.ServerAddress;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;

@SuppressLint("SetJavaScriptEnabled")
@ContentView(R.layout.activity_login)
public class LogInActivity extends RoboActivity {
  static {
    // To enable logging of HTTP requests and responses (including URL, headers, and content)
    Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.CONFIG);
  }
  private final String TAG = this.getClass().getSimpleName();

  @InjectView(R.id.username_EditText)
  private EditText usernameEditText;

  @InjectView(R.id.password_EditText)
  private EditText passwordEditText;

  //
  public void login(View view) {
    String errorMessageString = "登录成功!";
    String username = "";
    String password = "";

    do {
      username = usernameEditText.getText().toString().trim();
      if (TextUtils.isEmpty(username)) {
        errorMessageString = "用户名不能为空!";
        break;
      }

      password = passwordEditText.getText().toString().trim();
      if (TextUtils.isEmpty(password)) {
        errorMessageString = "密码不能为空!";
        break;
      }

      String[] params = { username, password };
      Account account = provideDevice(GlobalConstant.REALTIME_SERVER);
      final LoginNetRequestTask loginNetRequestTask = new LoginNetRequestTask(LogInActivity.this, null, account);
      SimpleProgressDialog.show(LogInActivity.this, new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          loginNetRequestTask.cancel(true);
        }
      });
      loginNetRequestTask.execute(params);

      Log.i(TAG, "username: " + username + " password: " + password);
      // 一切OK
      return;
    } while (false);

    // 用户输入的信息错误
    Toast.makeText(LogInActivity.this, errorMessageString, Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    GlobalDataCacheForMemorySingleton.getInstance.addActivity(this);

    // 快捷键
    SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
    boolean shortcut = sp.getBoolean("shortcut", false);
    if (!shortcut) {
      // 创建一个快捷图标.
      Intent intent = new Intent();
      intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

      // 设置快捷方式名称
      intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));

      // 设置快捷方式启动动作
      ComponentName comp = new ComponentName("com.goodow.android.drive", "com.goodow.drive.android.activity.LogInActivity");
      intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(comp));

      // 设置快捷方式的图标
      Parcelable icon = Intent.ShortcutIconResource.fromContext(LogInActivity.this, R.drawable.icon_launcher);
      intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

      // 发送一个无序广播进行创建动作
      sendBroadcast(intent);

      Toast.makeText(this, "创建了快捷方式", Toast.LENGTH_LONG).show();
      Editor editor = sp.edit();
      editor.putBoolean("shortcut", true);
      editor.commit();
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
    case MotionEvent.ACTION_DOWN:
      InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      inputMethodManager.hideSoftInputFromWindow(usernameEditText.getWindowToken(), 0);

      break;
    default:
      break;
    }

    return true;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    GlobalDataCacheForMemorySingleton.getInstance.removeActivity(this);
  }

  @Override
  protected void onPause() {
    super.onPause();

    SimpleProgressDialog.resetByThisContext(this);
  }

  @Provides
  @Singleton
  private Account provideDevice(@ServerAddress String serverAddress) {
    Account.Builder endpointBuilder =
        new Account.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), new HttpRequestInitializer() {
          @Override
          public void initialize(HttpRequest httpRequest) {

          }
        });
    endpointBuilder.setRootUrl(RealtimeModule.getEndpointRootUrl(serverAddress));
    return CloudEndpointUtils.updateBuilder(endpointBuilder).build();
  }
}
