package io.perihelion.fitfactor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import io.perihelion.fitfactor.Fragments.LoginFragment;
import io.perihelion.fitfactor.Fragments.MainFragment;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, MessageApi.MessageListener {

    private final String TAG = this.getClass().getName();
    private GoogleApiClient mGoogleApiClient;
    private List<Callbacks> callbackList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (null != extras && null != extras.getString("com.parse.Data")) {
            String parseData = extras.getString("com.parse.Data");
            Log.d(TAG, parseData);
            try {
                JSONObject parseJSON = new JSONObject(parseData);
                String unlockUser = parseJSON.getString("forUser");

                ParseQuery<ParseObject> query = ParseQuery.getQuery("_User");
                query.getInBackground(unlockUser, new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (null != e) {
                            Log.d(TAG, e.getMessage());
                        }
                        if (null != object) {
                            Log.d(TAG, "found " + object.getClassName() + " " + object.getObjectId());
                            ParseObject stepCountUpdate = new ParseObject("FriendUnlock");
                            stepCountUpdate.put("unlockedBy", ParseUser.getCurrentUser());
                            stepCountUpdate.put("unlockFor", object);
                            stepCountUpdate.put("user", ParseUser.getCurrentUser());
                            stepCountUpdate.saveEventually();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        setContentView(R.layout.activity_main);
        callbackList = new ArrayList<Callbacks>();
        getFragmentManager().beginTransaction().add(R.id.container,
                ParseUser.getCurrentUser() == null ? new LoginFragment() : new MainFragment()).commit();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
        Log.d(getClass().getName(), "Got OnActivityResult");
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_plus_goal:
                int goal = ParseUser.getCurrentUser().getInt("goal");
                int currentValue = ParseUser.getCurrentUser().getInt("currentStepCount");
                ParseObject stepCountUpdate = new ParseObject("StepCountUpdate");
                stepCountUpdate.put("value", goal+currentValue);
                stepCountUpdate.put("sensor", "Manual Trigger");
                stepCountUpdate.put("user", ParseUser.getCurrentUser());
                stepCountUpdate.saveInBackground();

                for (Callbacks callbacks : callbackList)
                    callbacks.onStepCountUpdate(goal+currentValue);

            case R.id.action_reset:

                ParseQuery<ParseObject> query = ParseQuery.getQuery("StepCountUpdate");
                try {
                    List<ParseObject> stepCounts = query.find();
                    for (ParseObject step : stepCounts) {
                        step.deleteEventually();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                for (Callbacks callbacks : callbackList)
                    callbacks.onStepCountUpdate(0);

                //send force reset
        }

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.d(TAG, "Connected to Google Api Service");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    protected void onStop() {
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        try {
            String message = new String(messageEvent.getData(), "UTF-8");
            JSONObject jsonObject = new JSONObject(message);

            ParseObject stepCountUpdate = new ParseObject("StepCountUpdate");
            stepCountUpdate.put("value", jsonObject.getString("value"));
            stepCountUpdate.put("sensor", jsonObject.getString("sensor"));
            stepCountUpdate.put("user", ParseUser.getCurrentUser());
            stepCountUpdate.saveInBackground();

            for (Callbacks callbacks : callbackList)
                callbacks.onStepCountUpdate(Float.valueOf(jsonObject.getString("value")).intValue());

            Log.d(TAG, jsonObject.toString());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        for (Callbacks callbacks : callbackList) {
            if (callbacks.onBackPressed())
                return;
        }
        super.onBackPressed();

    }

    public void addActivityCallbacks(Callbacks callbacks) {
        callbackList.add(callbacks);
    }

    public void removeActivityCallbacks(Callbacks callbacks) {
        callbackList.remove(callbacks);
    }

    public interface Callbacks {
        public boolean onBackPressed();

        public void onStepCountUpdate(int value);
    }
}