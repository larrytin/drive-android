package com.goodow.drive.android.toolutils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * 对 ProgressDialog 的简单封装 特点 : 1.调用者必须保证 show() 和 dismiss()的成对调用; 2.包含引用计数器机制, 不会创建多个ProgressDialog实例, 当最后一个引用计数器为0时,才会销毁ProgressDialog对象
 * 3.在Activity中的 onDestroy()中调用此reset()方法
 * 
 * @author zhihua.tang
 */
public final class SimpleProgressDialog {
  private static final String TAG = "SimpleProgressDialog";

  private static ProgressDialog progressDialog;

  // 引用计数器
  private static int referenceCounter = 0;
  // 用于调试, show 和 dismiss 没有成对调用
  private static Context lastContext;

  /**
   * 关闭一个 ProgressDialog
   */
  public static synchronized void dismiss(Context context) {
    if (context != lastContext) {
      assert false : "context != lastContext";
      return;
    }

    referenceCounter--;

    DebugLog.i(TAG, "调用 dismiss(), 最新计数器=" + referenceCounter);

    if (lastContext == null || referenceCounter <= 0 || progressDialog == null) {
      reset();
    }
  }

  /**
   * 当前 ProgressDialog 正在显示中
   * 
   * @return
   */
  public static synchronized boolean isShowing() {
    return referenceCounter > 0 ? true : false;
  }

  /**
   * 重置ProgressDialog(在Activity中, 必须在 onDestroy()中调用此方法, 否则可能触发 IllegalArgumentException: View not attached to window manager
   */
  public static synchronized void resetByThisContext(Context context) {
    if (context == lastContext) {
      reset();
    }
  }

  /**
   * 启动一个 ProgressDialog
   * 
   * @param context
   */
  public static synchronized void show(Context context, final DialogInterface.OnCancelListener dialogCancelDelegate) {
    if (context == null) {
      DebugLog.e(TAG, "入参 context 为 null ! ");
      return;
    }

    if (context != lastContext) {
      // 如果context变化了, 证明切换了Activity
      reset();
    }

    referenceCounter++;

    DebugLog.i(TAG, "在类 <" + context.getClass().getSimpleName() + "> 中调用 show(), 最新计数器=" + referenceCounter);

    if (progressDialog == null) {
      progressDialog = ProgressDialog.show(context, "网络访问中", "请耐心等待...");
      progressDialog.setCancelable(true);
      progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          // 当用户按下 BACK 按键时, 要关闭 ProgressDialog
          reset();

          if (dialogCancelDelegate != null) {
            dialogCancelDelegate.onCancel(progressDialog);
          }
        }
      });

      lastContext = context;
    }
  }

  private static synchronized void reset() {
    lastContext = null;
    referenceCounter = 0;
    if (progressDialog != null) {
      if (progressDialog.isShowing()) {
        progressDialog.dismiss();
      }
      progressDialog = null;
    }
  }

  private SimpleProgressDialog() {

  }
}
