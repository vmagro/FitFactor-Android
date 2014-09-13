package io.perihelion.fitfactor.Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

import java.util.List;

import io.perihelion.fitfactor.R;

/**
 * Created by vincente on 9/13/14
 */
public class FriendListAdapter extends BaseAdapter {
    private List<GraphUser> users;
    private Context context;
    public FriendListAdapter(Context context, List<GraphUser> users){
        this.context = context;
        this.users = users;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public GraphUser getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        ViewHolder viewHolder;
        if(view == null){
            view = View.inflate(context, R.layout.list_user_item, null);
            viewHolder = new ViewHolder();
            viewHolder.profilePictureView = (ProfilePictureView) view.findViewById(R.id.profile_picture);
            viewHolder.textView = (TextView) view.findViewById(R.id.profile_name);
            view.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) view.getTag();

        viewHolder.profilePictureView.setProfileId(users.get(i).getId());
        viewHolder.textView.setText(users.get(i).getName());

        return view;
    }

    class ViewHolder{
        TextView textView;
        ProfilePictureView profilePictureView;
    }
}
