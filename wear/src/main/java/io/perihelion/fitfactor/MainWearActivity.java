package io.perihelion.fitfactor;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.wearable.view.CircledImageView;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class MainWearActivity extends Activity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = this.getClass().getName();

    private static final int MOTO_360_HEART_RATE = 65538;
    private CircledImageView mCircledImageView;
    private TextView mTextView;

    Sensor mHeartRateSensor;
    Sensor mHeartRateSensorDeafult;
    Sensor mStepCountSensor;
    Sensor mStepDetectSensor;
    SensorManager mSensorManager;
    GoogleApiClient mGoogleApiClient;

    private float currentStepCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_standalone);
        mCircledImageView = (CircledImageView) findViewById(R.id.circle);
        mTextView = (TextView) findViewById(R.id.sensorCount);

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        mHeartRateSensor = mSensorManager.getDefaultSensor(MOTO_360_HEART_RATE);
        mHeartRateSensorDeafult = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mStepCountSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSensorManager.registerListener(this, mHeartRateSensorDeafult, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepCountSensor, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mStepDetectSensor, SensorManager.SENSOR_DELAY_NORMAL);

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor s: sensors) {
            Log.d(TAG, s.getName() + " " + s.getType() + " " + s.getStringType());
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSensorManager != null)
            mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        Log.d(TAG, "sensor event: " + sensorEvent.sensor.getName() + " " + sensorEvent.accuracy + " = " + sensorEvent.values[0]);
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            mTextView.setText("" + (int) sensorEvent.values[0]);
            float delta = sensorEvent.values[0] - currentStepCount;
            currentStepCount = sensorEvent.values[0];
            new SendMessageTask(sensorEvent.sensor.getName(), delta).execute();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        Log.d(TAG, "accuracy changed: " + i);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Connected to Google Api Service");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "connection failed");
    }

    private class SendMessageTask extends AsyncTask<Void, Void, Void> {

        String sensorName;
        float value;

        public SendMessageTask(String name, float value) {
            this.sensorName = name;
            this.value = value;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject json = new JSONObject();
            try {
                json.put("sensor", sensorName);
                json.put("value", String.valueOf(value));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d(TAG, json.toString());

            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            for (Node node : nodes.getNodes()) {
                try {
                    PendingResult<MessageApi.SendMessageResult> result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), TAG, json.toString().getBytes("UTF-8"));
                    result.setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                            Log.d(TAG, "status: " + sendMessageResult.getStatus().getStatusMessage() + " " + sendMessageResult.getStatus().toString());
                        }
                    });
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}