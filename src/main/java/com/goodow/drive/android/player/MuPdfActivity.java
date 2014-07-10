package com.goodow.drive.android.player;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import com.goodow.android.drive.R;
import com.goodow.drive.android.Constant;
import com.goodow.drive.android.PDFConstant;
import com.goodow.drive.android.activity.BaseActivity;
import com.goodow.drive.android.data.DBOperator;
import com.goodow.drive.android.pdf.DriveAndroidMuPdfActivity;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.channel.State;
import com.goodow.realtime.json.Json;
import com.google.inject.Inject;

/**
 * Created by dpw on 7/10/14.
 */
public class MuPdfActivity extends DriveAndroidMuPdfActivity implements View.OnClickListener {

    @Inject
    private Bus bus;
    private WindowManager windowManager;
    private WindowManager.LayoutParams paramsBack;
    private WindowManager.LayoutParams paramsControllBar;
    private Button back;
    private View controllBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        this.windowManager = (WindowManager) this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        this.back = new Button(this);
        this.back.setBackgroundResource(R.drawable.common_selector_back);
        this.back.setOnClickListener(this);
        this.controllBar = this.getLayoutInflater().inflate(com.goodow.drive.android.R.layout.demo,null);
        this.paramsBack = new WindowManager.LayoutParams();
        this.paramsBack.width = this.paramsBack.WRAP_CONTENT;
        this.paramsBack.height = this.paramsBack.WRAP_CONTENT;
        this.paramsBack.gravity = Gravity.LEFT | Gravity.TOP;
        this.paramsBack.type = WindowManager.LayoutParams.TYPE_PHONE;
        this.paramsBack.format = PixelFormat.RGBA_8888;
        this.paramsBack.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        this.paramsControllBar = new WindowManager.LayoutParams();
        this.paramsControllBar.width = WindowManager.LayoutParams.WRAP_CONTENT;
        this.paramsControllBar.height = WindowManager.LayoutParams.WRAP_CONTENT;
        this.paramsControllBar.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        this.paramsControllBar.type = WindowManager.LayoutParams.TYPE_PHONE;
        this.paramsControllBar.format = PixelFormat.RGBA_8888;
        this.paramsControllBar.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }


    @Override
    protected void onResume() {
        super.onResume();
        this.windowManager.addView(this.back, this.paramsBack);
        this.windowManager.addView(this.controllBar, this.paramsControllBar);
        bus.sendLocal(PDFConstant.ADDR_PLAYER, Json.createObject().set("zoomBy", 2.5), null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.windowManager.removeView(this.back);
        this.windowManager.removeView(this.controllBar);
    }

    @Override
    public void onClick(View v) {
        if(v == back){
            saveOnDatabases();
            this.finish();
        }
        if(v.getId() == R.id.bt_pdf_pre_page){
            bus.sendLocal(PDFConstant.ADDR_PLAYER, Json.createObject().set("page", Json.createObject().set("move", -1)), null);
        }
        if(v.getId() == R.id.bt_pdf_next_page){
            bus.sendLocal(PDFConstant.ADDR_PLAYER, Json.createObject().set("page", Json.createObject().set("move", 1)), null);
        }
        if(v.getId() == R.id.bt_pdf_max){
            bus.sendLocal(PDFConstant.ADDR_PLAYER, Json.createObject().set("zoomBy", 1.2), null);
        }
        if(v.getId() == R.id.bt_pdf_min){
            bus.sendLocal(PDFConstant.ADDR_PLAYER, Json.createObject().set("zoomBy", 0.8), null);
        }
    }

    @Override
    public void onBackPressed() {
        saveOnDatabases();
        super.onBackPressed();
    }

    protected void saveOnDatabases() {
        SharedPreferences usagePreferences = getSharedPreferences(BaseActivity.USAGE_STATISTIC, Context.MODE_MULTI_PROCESS);
        String fileName = usagePreferences.getString("tmpFileName", "");
        long openTime = usagePreferences.getLong("tmpOpenTime", -1);
        // 播放时间，小于5s，忽略
        long lastTime = SystemClock.uptimeMillis() - usagePreferences.getLong("tmpSystemLast", -1);
        if (lastTime > 5000 & !TextUtils.isEmpty(fileName)) {
            // 将播放数据存储到数据库
            DBOperator.addUserData(this, "T_PLAYER", "FILE_NAME", "OPEN_TIME", "LAST_TIME", Json
                    .createObject().set("FILE_NAME", fileName).set("OPEN_TIME", openTime).set("LAST_TIME",
                            lastTime));
            if (bus.getReadyState() == State.OPEN) {
                bus.sendLocal(Constant.ADDR_PLAYER_ANALYTICS_REQUEST, null, null);
            }
        }
    }

}
