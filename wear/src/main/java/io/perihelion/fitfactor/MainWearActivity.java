package io.perihelion.fitfactor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class MainWearActivity extends Activity implements SensorEventListener {

    private final String TAG = this.getClass().getName();

    private static final int MOTO_360_HEART_RATE = 65538;
    private CircledImageView mCircledImageView;
    private TextView mTextView;

    Sensor mHeartRateSensor;
    Sensor mHeartRateSensorDeafult;
    Sensor mStepCountSensor;
    Sensor mStepDetectSensor;
    SensorManager mSensorManager;

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_wear);

        mSensorManager = ((SensorManager)getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(MOTO_360_HEART_RATE);
        mHeartRateSensorDeafult = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        mSensorManager.registerListener(this, mHeartRateSensorDeafult, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);

//        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
//        for (Sensor sensor : sensors) {
//            Log.d("Sensors", "" + sensor.getStringType() + " " + sensor.getType() + " " + sensor.getName());
//        }

        //View
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mCircledImageView = (CircledImageView) stub.findViewById(R.id.circle);
                mTextView = (TextView) stub.findViewById(R.id.value);
            }
        });

        Log.d(TAG, "Done setting up");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSensorManager != null){
            mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSensorManager!=null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG, "sensor event: " + sensorEvent.sensor.getName() + " " + sensorEvent.accuracy + " = " + sensorEvent.values[0]);
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            mTextView.setText("" + (int) sensorEvent.values[0]);
        }
//        mTextView.setText(sensorEvent.values[0] + " bpm");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "accuracy changed: " + i);
    }
}