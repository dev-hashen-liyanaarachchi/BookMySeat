package com.hash.bookmyseat.service;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeDetector implements SensorEventListener {


    private static final float SHAKE_THRESHOLD = 25.0f;
    private static final int SHAKE_WAIT_TIME_MS = 1000;
    private long lastShakeTime = 0;
    private OnShakeListener listener;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final String TAG = "ShakeDetector";


    private float lastX, lastY, lastZ;
    private long lastUpdate = 0;
    private float lastSpeed = 0;
    private int falseDetectCount = 0;

    public interface OnShakeListener {
        void onShake();
    }

    public ShakeDetector(Context context, OnShakeListener listener) {
        this.listener = listener;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accelerometer != null) {
            Log.d(TAG, "✅ Accelerometer found!");
        } else {
            Log.d(TAG, "❌ Accelerometer NOT found!");
        }
    }

    public void start() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "✅ Shake detector started");
        }
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            Log.d(TAG, "Shake detector stopped");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastUpdate;

            if (timeDiff > 100) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];


                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / timeDiff * 10000;


                if (speed > SHAKE_THRESHOLD) {
                    Log.d(TAG, "Movement speed: " + speed);
                }


                if (speed > SHAKE_THRESHOLD) {
                    long now = System.currentTimeMillis();
                    if (now - lastShakeTime > SHAKE_WAIT_TIME_MS) {

                        if (speed > lastSpeed * 1.5 || speed > SHAKE_THRESHOLD * 1.5) {
                            lastShakeTime = now;
                            Log.d(TAG, "🎯 REAL SHAKE DETECTED! Speed: " + speed);
                            if (listener != null) {
                                listener.onShake();
                            }
                            falseDetectCount = 0;
                        } else {
                            falseDetectCount++;
                            if (falseDetectCount > 3) {
                                Log.d(TAG, "⚠️ False shake ignored - Speed: " + speed);
                            }
                        }
                    }
                } else if (speed > 10 && speed < SHAKE_THRESHOLD) {

                    if (falseDetectCount < 10) {
                        Log.d(TAG, "Normal movement - Speed: " + speed);
                    }
                }

                lastUpdate = currentTime;
                lastX = x;
                lastY = y;
                lastZ = z;
                lastSpeed = speed;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}