package com.goodow.drive.android.settings;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import android.content.Context;

public enum BaiduLocation {
  INSTANCE;
  public class MyLocationListener implements BDLocationListener {
    @Override
    public void onReceiveLocation(BDLocation location) {
    }

    @Override
    public void onReceivePoi(BDLocation poiLocation) {
    }
  }

  private LocationClient mLocationClient = null;
  private BDLocationListener myListener;
  private Context mContext;
  // 百度地图授权的key
  private static final String KEY = "rrTM3ndAtByixCIGQaDupKvu";

  private BaiduLocation() {
  }

  public LocationClient getLocationClient() {
    if (mLocationClient == null) {
      mLocationClient = new LocationClient(mContext);
    }
    return mLocationClient;
  }

  // 初始化操作
  public void init() {

    mLocationClient.setAK(KEY);
    myListener = new MyLocationListener();
    mLocationClient.registerLocationListener(myListener);
    setLocationOption();
  }

  public void setContext(Context ctx) {
    this.mContext = ctx;
  }

  // 设置相关参数
  private void setLocationOption() {
    LocationClientOption option = new LocationClientOption();
    option.setServiceName("keruixing");
    option.setPriority(LocationClientOption.NetWorkFirst); // 设置网络优先
    option.setOpenGps(true);
    option.setAddrType("all");// 返回的定位结果包含地址信息
    option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
    // option.setScanSpan(5000);// 设置发起定位请求的间隔时间为5000ms
    option.disableCache(true);// 禁止启用缓存定位
    // option.setPoiNumber(5); // 最多返回POI个数
    // option.setPoiDistance(1000); // poi查询距离
    // option.setPoiExtraInfo(true); // 是否需要POI的电话和地址等详细信息
    mLocationClient.setLocOption(option);
  }
}
