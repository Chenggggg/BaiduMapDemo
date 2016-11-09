package com.example.overdo.testmap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by overdo on 16-11-8.
 */

public class MyOrientationListener implements SensorEventListener {

    private SensorManager mSensorManager;
    private Context mContext;
    private Sensor mSensor;
    private float lastX;

    public MyOrientationListener(Context context) {
        this.mContext = context;
    }

    public void start() {

        //获取传感器管理者和传感器
        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            //获得方向传感器
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        if (mSensor != null) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //精度的改变

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        //方向发生变化
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION){
            float x = sensorEvent.values[SensorManager.DATA_X];

            if(Math.abs(x - lastX) >= 1.0){
                //回调通知主界面更新
                if(mOnOrientationListener != null){
                    mOnOrientationListener.OnOrientationChanged(x);
                }
            }

            lastX = x;
        }
    }



    private OnOrientationListener mOnOrientationListener;

    public void setOnOrientationListener(OnOrientationListener listener){
        this.mOnOrientationListener = listener;
    }

    public interface OnOrientationListener{
        void OnOrientationChanged(float x);
    }

}
