package io.perihelion.fitfactor.Fragments;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

import io.perihelion.fitfactor.Adapters.FriendListAdapter;
import io.perihelion.fitfactor.Constants;
import io.perihelion.fitfactor.MainActivity;
import io.perihelion.fitfactor.R;

/**
 * Created by vincente on 9/13/14
 */
public class MainFragment extends Fragment implements MainActivity.OnBackPressedListener{
    private boolean listShowing = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) getActivity()).addOnBackPressedListener(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, null);
        loadFriendImage(view);
        loadFriendList(view);

        view.findViewById(R.id.accountable_container).setOnClickListener(accountableClickListener);

        return view;
    }

    private void loadFriendImage(View view){
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser.get(Constants.FACEBOOK_ID_FRIEND)== null) {
            ((TextView) view.findViewById(R.id.accountable)).setText(getString(R.string.accountable_null));
            return;
        }

        //Set the Profile Picture of the Friend
        String friendId = (String) currentUser.get(Constants.FACEBOOK_ID_FRIEND);
        ProfilePictureView profilePictureView = (ProfilePictureView) view.findViewById(R.id.main_prof);
        profilePictureView.setProfileId(friendId);
        Request request = new Request(
                ParseFacebookUtils.getSession(),
                "/"+friendId,
                null,
                HttpMethod.GET,
                new Request.Callback() {
                    public void onCompleted(Response response) {
                        Log.d(getClass().getName(), response.getGraphObject().getInnerJSONObject().toString());
                        Log.d(getClass().getName(), "Is GraphUser: " + String.valueOf(response.getGraphObject() instanceof GraphUser));
                    }
                }
        );
        request.executeAsync();
    }

    private void loadFriendList(final View view){
        Request.newMyFriendsRequest(ParseFacebookUtils.getSession(), new Request.GraphUserListCallback() {
            @Override
            public void onCompleted(List<GraphUser> users, Response response) {
                if (users != null) {
                    Log.d(getClass().getName(), "Loaded Friends");
                    FriendListAdapter adapter = new FriendListAdapter(getActivity(), users);
                    ListView listView = (ListView) view.findViewById(R.id.friendChooser);
                    listView.setAdapter(adapter);
                }
                else
                    Log.d(getClass().getName(), "Couldn't Load Friends");
            }
        }).executeAsync();
    }

    private View.OnClickListener accountableClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            showList();
        }
    };

    private void showList(){
        final View list = getView().findViewById(R.id.friendChooser);
        final int finalHeight = list.getMeasuredHeight();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new FadeUpdateListener(list));
        animator.addUpdateListener(new FadeInverseUpdateListener(
                getView().findViewById(R.id.accountable_container),
                getView().findViewById(R.id.stats),
                getView().findViewById(R.id.status)
        ));
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                listShowing = true;
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });

        animator.setDuration(getResources().getInteger(R.integer.fade));
        animator.start();
    }
    private void hideList(){
        final View list = getView().findViewById(R.id.friendChooser);
        ValueAnimator animator = ValueAnimator.ofFloat(1,0);
        animator.addUpdateListener(new FadeUpdateListener(list));
        animator.addUpdateListener(new FadeInverseUpdateListener(
                getView().findViewById(R.id.accountable_container),
                getView().findViewById(R.id.stats),
                getView().findViewById(R.id.status)
        ));
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }
            @Override
            public void onAnimationEnd(Animator animator) {
                listShowing = false;
            }
            @Override
            public void onAnimationCancel(Animator animator) {
            }
            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        animator.setDuration(getResources().getInteger(R.integer.fade));
        animator.start();
    }

    @Override
    public boolean onBackPressed() {
        if(listShowing){
            hideList();
            return true;
        }
        else
           return false;
    }

    private class ListHeightUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        View list;
        int delta;
        int currentHeight;

        public ListHeightUpdateListener(View list, int currentHeight, int delta) {
            this.list = list;
            this.delta = delta;
            this.currentHeight = currentHeight;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float value = (Float) valueAnimator.getAnimatedValue();
            //Update List Height
            list.getLayoutParams().height = currentHeight + Math.round(delta * value);
            list.requestLayout();
        }
    };
    private class FadeInverseUpdateListener implements ValueAnimator.AnimatorUpdateListener{
        List<View> views;

        public FadeInverseUpdateListener(View... views){
            this.views = Arrays.asList(views);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            for(View view : views){
                view.setAlpha(1-(Float) valueAnimator.getAnimatedValue());
            }
            if((Float)valueAnimator.getAnimatedValue() == 1){
                for(View view : views) {
                    if(view.getAlpha() <= .5)
                        view.setVisibility(View.GONE);
                    else
                        view.setVisibility(View.VISIBLE);
                }
            }
            else if(views.get(0).getAlpha() > 0){
                for(View view : views){
                    view.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    private class FadeUpdateListener implements ValueAnimator.AnimatorUpdateListener{
        List<View> views;
        public FadeUpdateListener(View... views){
            this.views = Arrays.asList(views);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            for(View view: views)
                view.setAlpha((Float) valueAnimator.getAnimatedValue());

            if((Float)valueAnimator.getAnimatedValue() == 1){
                for(View view : views) {
                    if(view.getAlpha() <= .5)
                        view.setVisibility(View.GONE);
                    else
                        view.setVisibility(View.VISIBLE);
                }
            }
            else if(views.get(0).getAlpha() > 0){
                for(View view : views){
                    view.setVisibility(View.VISIBLE);
                }
            }

        }
    }
}
