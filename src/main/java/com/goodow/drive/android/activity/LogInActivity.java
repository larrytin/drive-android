package com.goodow.drive.android.activity;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.goodow.android.drive.R;
import com.goodow.api.services.account.Account;
import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.toolutils.LoginNetRequestTask;
import com.goodow.drive.android.toolutils.SimpleProgressDialog;
import com.goodow.realtime.android.CloudEndpointUtils;
import com.goodow.realtime.android.RealtimeModule;
import com.goodow.realtime.android.ServerAddress;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.Provides;
import com.google.inject.Singleton;

@SuppressLint("SetJavaScriptEnabled")
@ContentView(R.layout.activity_login)
public class LogInActivity extends RoboActivity {
  private final String TAG = this.getClass().getSimpleName();

  @InjectView(R.id.username_EditText)
  private EditText usernameEditText;

  @InjectView(R.id.password_EditText)
  private EditText passwordEditText;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onPause() {
    super.onPause();

    SimpleProgressDialog.resetByThisContext(this);
  }

  public void login(View view) {
    String errorMessageString = "登录成功!";
    String username = "";
    String password = "";

    do {
      username = usernameEditText.getText().toString();
      if (TextUtils.isEmpty(username)) {
        errorMessageString = "用户名不能为空!";
        break;
      }

      password = passwordEditText.getText().toString();
      if (TextUtils.isEmpty(password)) {
        errorMessageString = "密码不能为空!";
        break;
      }

      String[] params = { username, password };
<<<<<<< HEAD
      Account account = provideDevice("http://192.168.1.15:8080");
      final LoginNetRequestTask loginNetRequestTask = new LoginNetRequestTask(
          LogInActivity.this, null, account);
=======
      Account account = provideDevice(GlobalConstant.SERVER);
      final LoginNetRequestTask loginNetRequestTask = new LoginNetRequestTask(LogInActivity.this, null, account);
>>>>>>> branch 'master' of https://github.com/goodow/drive-android.git
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
    Toast.makeText(LogInActivity.this, errorMessageString, Toast.LENGTH_SHORT)
        .show();
  }

  @Provides
  @Singleton
  private Account provideDevice(@ServerAddress String serverAddress) {
    Account.Builder endpointBuilder = new Account.Builder(
        AndroidHttp.newCompatibleTransport(), new JacksonFactory(),
        new HttpRequestInitializer() {
          @Override
          public void initialize(HttpRequest httpRequest) {

          }
        });
    endpointBuilder
        .setRootUrl(RealtimeModule.getEndpointRootUrl(serverAddress));
    return CloudEndpointUtils.updateBuilder(endpointBuilder).build();
  }
}
