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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.parse.FindCallback;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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
public class MainFragment extends Fragment implements MainActivity.Callbacks{
    private boolean listShowing = false;
    private int currentCount;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) getActivity()).addActivityCallbacks(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentCount = ParseUser.getCurrentUser().getInt(Constants.PARSE_CURRENT_STEPS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, null);
        loadAccountantDetail(view);
        findAccountableDetail(view);
        loadFriendList(view);
        loadStats(view);

        view.findViewById(R.id.accountant_container).setOnClickListener(onClickListener);
        view.findViewById(R.id.accountant_container).setOnLongClickListener(onLongClickListener);
        ((ListView) view.findViewById(R.id.friendChooser)).setOnItemClickListener(friendsListItemClickListener);
        return view;
    }

    private void loadAccountantDetail(final View view){
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser.get(Constants.FACEBOOK_ID_FRIEND_ACCOUNTANT)== null) {
            ((TextView) view.findViewById(R.id.accountant_text)).setText(getString(R.string.accountant_null));
            return;
        }

        //Set the Profile Picture of the Friend
        String accountantId = (String) currentUser.get(Constants.FACEBOOK_ID_FRIEND_ACCOUNTANT);
        ProfilePictureView profilePictureView = (ProfilePictureView) view.findViewById(R.id.accountant_pic);
        profilePictureView.setProfileId(accountantId);

        //Request the Accountant Stuff
        Request.newGraphPathRequest(ParseFacebookUtils.getSession(), "/"+accountantId, new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                Log.d(getClass().getName(), response.getGraphObject().getInnerJSONObject().toString());
                ((TextView) view.findViewById(R.id.accountant_text)).setText(
                        String.format(getString(R.string.format_accountant), response.getGraphObject().getProperty("first_name"))
                );
            }
        }).executeAsync();
    }
    private void findAccountableDetail(final View view){
        ParseQuery query = ParseUser.getQuery();
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, com.parse.ParseException e) {
                if (e == null) {
                    for(ParseObject object : objects) {
                        if(object.get(Constants.FACEBOOK_ID_FRIEND_ACCOUNTANT) != null
                                && object.get(Constants.FACEBOOK_ID_FRIEND_ACCOUNTANT).equals(ParseUser.getCurrentUser().get(Constants.FACEBOOK_ID))){
                            loadAccountableDetail(view, object.getString(Constants.FACEBOOK_ID));
                        }
                    }
                    ((TextView) view.findViewById(R.id.accountable_text)).setText(getString(R.string.accountable_null));
                } else {
                    ((TextView) view.findViewById(R.id.accountable_text)).setText(getString(R.string.accountable_null));
                }
            }
        });
    }

    private void loadAccountableDetail(final View view, String accountableId){
        //Set the Profile Picture of the Friend
        ProfilePictureView profilePictureView = (ProfilePictureView) view.findViewById(R.id.accountable_pic);
        profilePictureView.setProfileId(accountableId);

        //Request the Accountant Stuff
        Request.newGraphPathRequest(ParseFacebookUtils.getSession(), "/"+accountableId, new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                Log.d(getClass().getName(), response.getGraphObject().getInnerJSONObject().toString());
                ((TextView) view.findViewById(R.id.accountable_text)).setText(
                        String.format(getString(R.string.format_accountable), response.getGraphObject().getProperty("first_name"))
                );
            }
        }).executeAsync();
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
    private void loadStats(final View view){
        final int percentage = (int) Math.floor(
                ((double)currentCount/ParseUser.getCurrentUser().getInt(Constants.PARSE_GOAL))*100);

        String lockedString = (percentage>=100)?"Unlocked":"Locked";

        ((TextView) view.findViewById(R.id.main_current_completed)).setText(
                String.format(getString(R.string.format_goal_completed), percentage)
        );

        ((TextView) view.findViewById(R.id.status)).setText(
                String.format(getString(R.string.format_currently), lockedString)
        );

        ((ProgressBar) view.findViewById(R.id.progressBar)).setProgress(percentage);
        ((TextView) view.findViewById(R.id.main_final_goal)).setText(
                String.format(getString(R.string.format_goal_current), ParseUser.getCurrentUser().getInt("goal"), "steps")
        );

        Log.d(getClass().getName(), "Current completion is " + percentage + "%");
    }
    private void showList(){
        final View list = getView().findViewById(R.id.friendChooser);
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new FadeUpdateListener(list));
        animator.addUpdateListener(new FadeInverseUpdateListener(
                getView().findViewById(R.id.accountant_container),
                getView().findViewById(R.id.stats),
                getView().findViewById(R.id.status),
                getView().findViewById(R.id.accountability),
                getView().findViewById(R.id.accountable_container)
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
                getView().findViewById(R.id.accountant_container),
                getView().findViewById(R.id.stats),
                getView().findViewById(R.id.status),
                getView().findViewById(R.id.accountability),
                getView().findViewById(R.id.accountable_container)
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

    private AbsListView.OnItemClickListener friendsListItemClickListener = new AbsListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            //Get the User
            GraphUser user = ((FriendListAdapter) adapterView.getAdapter()).getItem(i);

            //Save the Facebook Id in the Database
            ParseUser.getCurrentUser().put(Constants.FACEBOOK_ID_FRIEND_ACCOUNTANT, user.getId());
            ParseUser.getCurrentUser().saveInBackground();

            //Load it into the Current View
            ((ProfilePictureView) getView().findViewById(R.id.accountant_pic)).setProfileId(user.getId());
            ((TextView) getView().findViewById(R.id.accountant_text)).setText(
                    String.format(getResources().getString(R.string.format_accountable),
                            user.getName())
            );

            hideList();
        }
    };
    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.accountant_container
                    && ParseUser.getCurrentUser().get(Constants.FACEBOOK_ID_FRIEND_ACCOUNTANT) == null)
                showList();
        }
    };

    private View.OnLongClickListener onLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
            showList();
            return false;
        }
    };

    @Override
    public boolean onBackPressed() {
        if(listShowing){
            hideList();
            return true;
        }
        else
            return false;
    }

    @Override
    public void onStepCountUpdate(int delta) {
        if(isAdded()){
            currentCount += delta;
            final int percentage = (int) Math.floor(((double)currentCount/ParseUser.getCurrentUser().getInt("goal"))*100);
            Log.d(getClass().getName(), "Goal: " + ParseUser.getCurrentUser().getInt("goal") + "\tCurrent: " + currentCount + "\tPercentage: " + percentage);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ProgressBar)getView().findViewById(R.id.progressBar)).setProgress(percentage);
                    ((TextView) getView().findViewById(R.id.main_current_completed)).setText(
                            String.format(getString(R.string.format_goal_completed), percentage)
                    );
                }
            });
        }
    }
    private class FadeInverseUpdateListener implements ValueAnimator.AnimatorUpdateListener{
        List<View> views;

        public FadeInverseUpdateListener(View... views){
            this.views = Arrays.asList(views);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            for(View view : views){
                view.setAlpha(1-(Float) valueAnimator.getAnimatedValue());
                checkVisibility(view);
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
            for(View view: views) {
                view.setAlpha((Float) valueAnimator.getAnimatedValue());
                checkVisibility(view);
            }
        }
    }
    private static void checkVisibility(View view){
        if(view.getAlpha() <= 0)
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
    }
}
