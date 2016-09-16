package vn.gcall.gcall2.CustomAdapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;

import vn.gcall.gcall2.DataStruct.Notification;
import vn.gcall.gcall2.R;

/**
 * Created by This PC on 23/06/2016.
 * Listview Adapter supports to show list of invitations to join a group/hotline in Notification tab
 */
public class NotificationListAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<Notification> notifications;
    private LayoutInflater inflater=null;

    public NotificationListAdapter(Activity a, ArrayList<Notification> notificationArrayList){
        activity=a;
        notifications=notificationArrayList;
        inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view= convertView;
        if (view==null){
            view=inflater.inflate(R.layout.list_row_notification,null);
        }

        TextView from_agent=(TextView) view.findViewById(R.id.from_agent);
        from_agent.setText(notifications.get(position).getAddedBy()+" invited you");

        TextView from_groupName=(TextView) view.findViewById(R.id.from_groupName);
        from_groupName.setText("Invite join group "+notifications.get(position).getGroupName());

        TextView groupID=(TextView)view.findViewById(R.id.groupID);
        groupID.setText(notifications.get(position).getGroupID());

        return view;
    }
}
