package vn.gcall.gcall2.CustomAdapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import vn.gcall.gcall2.DataStruct.SubGroup;
import vn.gcall.gcall2.R;

/**
 * Created by This PC on 25/06/2016.
 * Listview Adapter supports to Show general information in of subgroup in working tab
 */
public class AgentInGroupAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<SubGroup> subGroups;
    private LayoutInflater inflater=null;

    public AgentInGroupAdapter(Activity a,ArrayList<SubGroup> groups){
        activity=a;
        subGroups=groups;
        inflater=(LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return subGroups.size();
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
        View view=convertView;
        if(view==null){
            view=inflater.inflate(R.layout.list_row_agent_in_group,null);
        }

        TextView groupName=(TextView)view.findViewById(R.id.groupName);
        TextView groupID=(TextView)view.findViewById(R.id.groupID);
        TextView size=(TextView)view.findViewById(R.id.noOfGroup);

        groupName.setText(subGroups.get(position).getGroupName());
        groupID.setText(subGroups.get(position).getGroupID());
        size.setText(Integer.toString(subGroups.get(position).getSize()));

        return view;
    }
}
