package com.example.overdo.testmap;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.LogoPosition;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;


public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private int num;
    private Context mContext = this;

    private LocationClient mLocationClient;
    private boolean isFirstIn = true;
    private double mLatitude;
    private double mLongitide;
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener mOrientationListener;
    private float mCurrentX;
    private MyLocationConfiguration.LocationMode mLocationMode;

    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        initView();
        initLocation();

        //初始化位置
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                            , Manifest.permission.READ_PHONE_STATE}, 0);
        } else {
            //初始化位置
            centerToMyLocation();
        }
    }


    private void initLocation() {

        mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;

        //１．声明locationclient类
        mLocationClient = new LocationClient(this);

        myLocationListener locationListener = new myLocationListener();
        mLocationClient.registerLocationListener(locationListener);

        //2.配置定位SDK参数
        /**
         * 设置定位参数包括：定位模式（高精度定位模式、低功耗定位模式和仅用设备定位模式）
         * 返回坐标类型，是否打开GPS，是否返回地址信息、位置语义化信息、POI信息等等。
         * 通过LocationClientOption累设置参数
         */
        LocationClientOption option = new LocationClientOption();

        //设置定位模式
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        option.setScanSpan(1000);//发起定位请求间隔时间
        option.setIsNeedAddress(true);//可选，设置是否需要位置信息，默认不需要
        option.setOpenGps(true);//可选，设置是否使用ＧＰＳ，默认ｆａｌｓｅ
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，
        // 可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，
        // 可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，
        // 并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，
        // 设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要


        mLocationClient.setLocOption(option);

        //初始化自定义图标
        mIconLocation = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);

        //初始化方向监听
        mOrientationListener = new MyOrientationListener(mContext);
        mOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void OnOrientationChanged(float x) {
                mCurrentX = x;
            }
        });

    }

    private void initView() {

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.id_mbmapView);
        mBaiduMap = mMapView.getMap();

        //缩放比例设置
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.f);
        mBaiduMap.setMapStatus(msu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.id_map_normal:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;

            case R.id.id_map_sate:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;

            case R.id.id_map_none:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
                break;

            case R.id.id_map_traffic:
                if (mBaiduMap.isTrafficEnabled() == true) {
                    mBaiduMap.setTrafficEnabled(false);
                    item.setTitle("实时交通(off)");
                } else {
                    item.setTitle("实时交通(on)");
                    mBaiduMap.setTrafficEnabled(true);
                }
                break;

            case R.id.id_map_heat:
                if (mBaiduMap.isBaiduHeatMapEnabled() == true) {
                    mBaiduMap.setBaiduHeatMapEnabled(false);
                    item.setTitle("热力图(off)");
                } else {
                    mBaiduMap.setBaiduHeatMapEnabled(true);
                    item.setTitle("热力图(on)");
                }
                break;

            case R.id.id_map_logoposition:
                mMapView.setLogoPosition(LogoPosition.values()[num % 6]);
                num++;
                break;
            case R.id.id_map_location:
                centerToMyLocation();
                break;
            case R.id.id_mode_normal:
                mLocationMode = MyLocationConfiguration.LocationMode.NORMAL;
                break;
            case R.id.id_mode_folowing:
                mLocationMode = MyLocationConfiguration.LocationMode.FOLLOWING;
                break;
            case R.id.id_mode_compass:
                mLocationMode = MyLocationConfiguration.LocationMode.COMPASS;
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }

        //启动方向传感器
        mOrientationListener.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //关闭定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        //关闭方向传感器
        mOrientationListener.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    //获取到位置后回调
    public class myLocationListener implements BDLocationListener {


        @Override
        public void onReceiveLocation(BDLocation location) {

            Log.d(TAG, "onReceiveLocation: " + location.getAddress().address + mBaiduMap.isMyLocationEnabled());
            Log.d(TAG, "onReceiveLocation: LocType =" + location.getLocType()
                    + location.getCoorType() + location.getAddrStr() + location.getLongitude()
                    + location.getLatitude() + "  " + location.getGpsAccuracyStatus());

            MyLocationData myLocationData = new MyLocationData.Builder()
                    .direction(mCurrentX)
                    .accuracy(location.getRadius())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();

            mBaiduMap.setMyLocationData(myLocationData);

            //设置自定义图标
            MyLocationConfiguration configuration = new MyLocationConfiguration(
                    mLocationMode, true, mIconLocation);
            mBaiduMap.setMyLocationConfigeration(configuration);


            //更新经纬度
            mLatitude = location.getLatitude();
            mLongitide = location.getLongitude();

            //第一次进入自动定位到所在位置
            if (isFirstIn) {

                centerToMyLocation();
                isFirstIn = false;
            }


        }
    }

    private void centerToMyLocation() {
        LatLng latLng = new LatLng(mLatitude, mLongitide);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);

        mBaiduMap.animateMapStatus(msu);

        //缩放比例设置
        MapStatusUpdate msu2 = MapStatusUpdateFactory.zoomTo(18.f);
        mBaiduMap.setMapStatus(msu2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 0:

                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        || grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    //初始化位置
                    centerToMyLocation();
                } else {

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION
                                    , Manifest.permission.READ_PHONE_STATE}, 0);

                    Toast.makeText(mContext, "权限未被授予", Toast.LENGTH_SHORT).show();

                }
                break;
        }

    }
}
