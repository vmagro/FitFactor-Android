package io.perihelion.fitfactor.Fragments;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

import io.perihelion.fitfactor.R;

/**
 * Created by vincente on 9/12/14
 */
public class LoginFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity.getActionBar() != null)
            activity.getActionBar().hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, null);
        view.findViewById(R.id.fb_login).setOnClickListener(OnFbClickListener);

        return view;
    }

    private View.OnClickListener OnFbClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            makeMeRequest();
            showProgress(true);
        }
    };

    private void makeMeRequest() {
        List<String> permissions = Arrays.asList("public_profile", "user_friends", "user_about_me",
                "user_relationships", "user_birthday", "user_location");
        Log.d(getClass().getName(), "Permissions: " + String.valueOf(permissions==null) + "\tActivity: " + String.valueOf(getActivity()==null));
        ParseFacebookUtils.logIn(permissions, getActivity(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                showProgress(false);
                if (user == null) {
                    Log.d(getClass().getName(),
                            "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d(getClass().getName(),
                            "User signed up and logged in through Facebook!");
//                    showUserDetailsActivity();
                } else {
                    Log.d(getClass().getName(),
                            "User logged in through Facebook!");
//                    showUserDetailsActivity();
                }
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void showProgress(boolean show) {
        TransitionDrawable background = (TransitionDrawable) getActivity().findViewById(R.id.rootLayout).getBackground();
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(getView().findViewById(R.id.loadingContainer), "alpha",
                show?0:1, show?1:0);
        objectAnimator.setDuration(getResources().getInteger(R.integer.fade));

        if (show) {
            background.startTransition(getResources().getInteger(R.integer.fade));
        } else {
            background.reverseTransition(getResources().getInteger(R.integer.fade));
        }

        objectAnimator.start();
    }
}