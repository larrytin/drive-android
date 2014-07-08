package com.goodow.drive.android.player;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import com.goodow.android.drive.R;
import com.goodow.drive.android.PDFConstant;
import com.goodow.drive.android.PDFDeviceInformationTools;
import com.goodow.drive.android.activity.BaseActivity;
import com.goodow.drive.android.pdf.DriveAndroidJZPdfView;
import com.goodow.realtime.channel.Bus;
import com.goodow.realtime.json.Json;
import com.goodow.realtime.json.JsonObject;
import com.google.inject.Inject;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import java.io.File;

/**
 * Created by dpw on 7/8/14.
 */
public class JZPdfActivity extends BaseActivity implements View.OnClickListener, OnLoadCompleteListener, OnPageChangeListener {
    private DriveAndroidJZPdfView pdfView;
    private float currentScale = 2.4f;
    private int currentPage = 0;
    @Inject
    private Bus bus;
    /*
     * override PDFVIEW LIB invoke when page load complete
     */
    @Override
    public void loadComplete(int nbPages) {
        float pdfViewWidth = pdfView.getOptimalPageWidth();
        int screenWidth = PDFDeviceInformationTools.getScreenWidth(this);
        float fitScale = (float) screenWidth / pdfViewWidth;
        pdfView.setScaleX(fitScale);
        pdfView.zoomCenteredTo(1.0f, new PointF(PDFDeviceInformationTools.getScreenWidth(this) / 2, 0));
        pdfView.loadPages();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.view.View.OnClickListener#onClick(android.view.View) 处理屏幕按钮点击事件
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.bt_pdf_pre_page){
            bus.sendLocal(PDFConstant.ADDR_PLAYER, Json.createObject().set("page", Json.createObject().set("move", -1)), null);
        }
        if(id == R.id.bt_pdf_next_page){
            bus.sendLocal(PDFConstant.ADDR_PLAYER, Json.createObject().set("page", Json.createObject().set("move", 1)), null);
        }
        if(id == R.id.bt_pdf_max){
            bus.sendLocal(PDFConstant.ADDR_PLAYER, Json.createObject().set("zoomBy", 1.2), null);
        }
        if(id == R.id.bt_pdf_min){
            bus.sendLocal(PDFConstant.ADDR_PLAYER, Json.createObject().set("zoomBy", 0.8), null);
        }
        if(id == R.id.iv_back){
            this.saveOnDatabases();
            this.finish();
        }
    }

    @Override
    public void onBackPressed() {
        this.saveOnDatabases();
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        pdfView.zoomTo(this.currentScale);
        super.onConfigurationChanged(newConfig);
    }

    /*
     * override PDFVIEW LIB invoke when page change
     */
    @Override
    public void onPageChanged(int page, int pageCount) {
        this.currentPage = page;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);
        pdfView = (DriveAndroidJZPdfView) findViewById(R.id.pdfView);
        this.buildPdfView(this.getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        this.buildPdfView(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                currentPage = savedInstanceState.getInt("currentPage");
                currentScale = savedInstanceState.getInt("currentScale");
                pdfView.jumpTo(savedInstanceState.getInt("currentPage"));
                pdfView.loadPages();
            }
        }, 200);
        super.onRestoreInstanceState(savedInstanceState);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putFloat("currentScale", this.currentScale);
        outState.putInt("currentPage", this.currentPage);
        super.onSaveInstanceState(outState);
    }

    /*
     * 加载文档
     */
    private void buildPdfView(Intent intent) {
        JsonObject jsonObject = (JsonObject) intent.getExtras().getSerializable("msg");
        File newFile = new File(jsonObject.getString("path"));
        if (newFile.exists()) {
            pdfView.fromFile(newFile).defaultPage(1).onPageChange(this).onLoad( this).load();
        } else {
            Toast.makeText(this, this.getString(R.string.pdf_file_no_exist), Toast.LENGTH_SHORT).show();
        }
    }
}
