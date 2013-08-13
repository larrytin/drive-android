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
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.goodow.android.drive.R;
import com.goodow.drive.android.toolutils.LoginNetRequestTask;
import com.goodow.drive.android.toolutils.SimpleProgressDialog;
import com.goodow.drive.android.toolutils.ToolsFunctionForThisProgect;

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

    Button loginButton = (Button) findViewById(R.id.login_Button);
    loginButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
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
          final LoginNetRequestTask loginNetRequestTask = new LoginNetRequestTask(LogInActivity.this, null);
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
    });
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      ToolsFunctionForThisProgect.quitApp(this);

      return true;
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onPause() {
    super.onPause();

    SimpleProgressDialog.resetByThisContext(this);
  }
}
