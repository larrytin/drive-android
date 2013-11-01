package com.goodow.drive.android.toolutils;

import com.goodow.api.services.account.Account;
import com.goodow.api.services.account.model.AccountInfo;
import com.goodow.drive.android.activity.MainActivity;
import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.realtime.Realtime;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class LoginNetRequestTask extends AsyncTask<String, String, AccountInfo> {
  private final String TAG = this.getClass().getSimpleName();

  public final static String LOGINPREFERENCESNAME = "LoginPreferencesName";
  public final static String USERNAME = "UserName";
  public final static String PASSWORD = "PassWord";

  private final Activity activity;
  private final Account account;
  private String userName;
  private String password;
  private final Dialog dialog;
  Exception exceptionThrown = null;

  /**
   * If the dialog is null, that is the class of activity is LoginActivity.
   * 
   * @param activity
   * @param dialog
   */
  public LoginNetRequestTask(Activity activity, Dialog dialog, Account account) {
    super();
    this.activity = activity;
    this.dialog = dialog;
    this.account = account;
  }

  @Override
  protected AccountInfo doInBackground(String... params) {
    AccountInfo accountInfo = null;
    try {
      userName = params[0].trim();
      password = params[1].trim();

      // accountInfo = account.login(userName, password).execute();
      // 如果一次登录没有成功，那么再尝试，最多三次
      accountInfo = fireNetwork(0, userName, password);

    } catch (IOException e) {
      exceptionThrown = e;
      e.printStackTrace();
    }
    return accountInfo;
  }

  @Override
  protected void onPostExecute(AccountInfo result) {
    String errorMessage = "";

    do {
      if (this.isCancelled()) {
        break;
      }
      if (exceptionThrown != null) {
        errorMessage = "网络连接异常,请检查网络!";
        break;
      }

      if (null == result || result.containsKey("error_message")) {
        errorMessage = "用户名或者密码错误!";
        break;
      }

      String userId = result.getUserId();
      String token = result.getToken();
      if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(token)) {
        errorMessage = "服务器异常!";
        break;
      }

      GlobalDataCacheForMemorySingleton.getInstance.setUserId(userId);
      GlobalDataCacheForMemorySingleton.getInstance.setAccess_token(token);
      GlobalDataCacheForMemorySingleton.getInstance.setUserName(userName);

      File file = new File(GlobalDataCacheForMemorySingleton.getInstance.getUserResDirPath());
      if (!file.exists()) {
        file.mkdir();
      }

      // 通知
      Realtime.authorize(userId, token);
      Log.i(TAG, "userId: " + userId + "\n token: " + token);

      String docId =
          "@tmp/" + GlobalDataCacheForMemorySingleton.getInstance().getUserId() + "/"
              + GlobalConstant.DocumentIdAndDataKey.OFFLINEDOCID.getValue();
      // 遍历离线目录的大小
      GlobalConstant.getFoldInformation();
      OfflineFileObserver.OFFLINEFILEOBSERVER.startObservation(docId, null);

      if (null == dialog) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();
      } else {
        if (dialog.isShowing()) {
          dialog.dismiss();
        }

        MainActivity mainActivity = (MainActivity) activity;
        mainActivity.goObservation();
        mainActivity.notifyFragment();
      }

      // 保存帐号密码,用于崩溃时自动登录
      SharedPreferences sharedPreferences =
          activity.getSharedPreferences(LOGINPREFERENCESNAME, Activity.MODE_PRIVATE);
      Editor editor = sharedPreferences.edit();
      editor.putString(USERNAME, userName);
      editor.putString(PASSWORD, password);

      editor.apply();
    } while (false);

    SimpleProgressDialog.dismiss(activity);

    if (!TextUtils.isEmpty(errorMessage)) {
      Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
    }
  }

  private AccountInfo fireNetwork(int retry, String params0, String params1) throws IOException {
    AccountInfo accountInfo = null;
    try {
      accountInfo = account.login(params0, params1).execute();
    } catch (EOFException e) {
      if (retry < 2) {
        Log.i(TAG, "重试登陆" + retry);
        return fireNetwork(retry++, params0, params1);
      }
      throw e;
    }
    return accountInfo;
  }
}
