package com.goodow.drive.android.toolutils;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.goodow.api.services.account.Account;
import com.goodow.api.services.account.model.AccountInfo;
import com.goodow.drive.android.activity.MainActivity;
import com.goodow.drive.android.global_data_cache.GlobalConstant;
import com.goodow.drive.android.global_data_cache.GlobalDataCacheForMemorySingleton;
import com.goodow.realtime.Realtime;

public class LoginNetRequestTask extends AsyncTask<String, String, AccountInfo> {
  private final String TAG = this.getClass().getSimpleName();

  private Activity activity;
  private Account account = MyApplication.getAccount();
  private String userName;
  private Dialog dialog;

  /**
   * If the dialog is null, that is the class of activity is LoginActivity.
   * 
   * @param activity
   * @param dialog
   */
  public LoginNetRequestTask(Activity activity, Dialog dialog) {
    super();
    this.activity = activity;
    this.dialog = dialog;
  }

  @Override
  protected AccountInfo doInBackground(String... params) {
    AccountInfo accountInfo = null;
    try {
      userName = params[0];

      accountInfo = account.login(params[0], params[1]).execute();

    } catch (IOException e) {
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

      String docId = "@tmp/" + GlobalDataCacheForMemorySingleton.getInstance().getUserId() + "/" + GlobalConstant.DocumentIdAndDataKey.OFFLINEDOCID.getValue();

      OfflineFileObserver.OFFLINEFILEOBSERVER.startObservation(docId, null);

      if (null == dialog) {
        Intent intent = new Intent(activity, MainActivity.class);
        activity.startActivity(intent);
        activity.finish();

      } else {
        dialog.dismiss();
        MainActivity mainActivity = (MainActivity) activity;
        mainActivity.goObservation();
        mainActivity.notifyFragment();

      }

    } while (false);

    SimpleProgressDialog.dismiss(activity);

    if (!TextUtils.isEmpty(errorMessage)) {
      Toast.makeText(activity, errorMessage, Toast.LENGTH_SHORT).show();
    }
  }
}
