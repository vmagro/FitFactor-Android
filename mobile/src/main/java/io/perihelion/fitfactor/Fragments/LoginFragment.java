package io.perihelion.fitfactor.Fragments;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

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
            showProgress(true);
            makeMeRequest();
        }
    };


    @SuppressWarnings("ConstantConditions")
    private void showProgress(boolean show) {
        if(!isAdded())
            return;
        final View progressContainer = getView().findViewById(R.id.loadingContainer);
        final View fbButton = getView().findViewById(R.id.fb_login);

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(show?0:1, show?1:0);
        valueAnimator.setDuration(getResources().getInteger(R.integer.fade));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                progressContainer.setAlpha((Float) valueAnimator.getAnimatedValue());
                fbButton.setAlpha(1-(Float) valueAnimator.getAnimatedValue());
            }
        });

        valueAnimator.start();
    }

    private void makeMeRequest() {
        List<String> permissions = Arrays.asList("public_profile", "user_friends", "user_about_me",
                "user_relationships", "user_birthday", "user_location");
        ParseFacebookUtils.logIn(permissions, getActivity(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                Log.d(getClass().getName(), "Done");
                if (user == null) {
                    Log.d(getClass().getName(),
                            "Uh oh. The user cancelled the Facebook login");
                } else if (user.isNew()) {
                    Log.d(getClass().getName(),
                            "User signed up and logged in through Facebook!");
//                    showMainFragment(user);
                    linkUser(user);
                } else {
                    Log.d(getClass().getName(),
                            "User logged in through Facebook!");
//                    showMainFragment(user);
                    linkUser(user);
                }
            }
        });
    }

    private void linkUser(final ParseUser user){
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("userid", user);
        installation.saveInBackground();
        if (!ParseFacebookUtils.isLinked(user)) {
            ParseFacebookUtils.link(user, getActivity(), new SaveCallback() {
                @Override
                public void done(ParseException ex) {
                    if (ParseFacebookUtils.isLinked(user)) {
                        Log.d(getClass().getName(), "Woohoo, user logged in with Facebook!");
                        showMainFragment(user);
                    }
                }
            });
        }
        else {
            Log.d(getClass().getName(), "User is already Linked!");
            showMainFragment(user);
        }
    }

    private void showMainFragment(ParseUser user){
        Log.d(getClass().getName(), "UserName: " + user.getUsername() + "\tEmail: " + user.getEmail());

        getFragmentManager().beginTransaction().replace(R.id.container, new MainFragment()).addToBackStack(null).commit();
    }
}