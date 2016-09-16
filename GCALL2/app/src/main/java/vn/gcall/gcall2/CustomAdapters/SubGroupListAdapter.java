package vn.gcall.gcall2.CustomAdapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import vn.gcall.gcall2.DataStruct.SubGroup;
import vn.gcall.gcall2.R;

/**
 * Created by This PC on 02/06/2016.
 * Listview Adapter supports to show detail of subgroup in a group/hotline
 */
public class SubGroupListAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<SubGroup> subGroups;
    private LayoutInflater inflater=null;

    private static final String GROUPS="subgroup";
    private static final String AGENTS="agent";
    public SubGroupListAdapter(Activity a, ArrayList<SubGroup> gpoups){
        activity=a;
        subGroups=gpoups;
        inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
        View view= convertView;
        if(view==null)
            view=inflater.inflate(R.layout.list_row_subgroup,null);
        TextView ext_tex= (TextView) view.findViewById(R.id.ext_text);
        ext_tex.setText(Integer.toString(subGroups.get(position).getExtension()));

        TextView subGroupname= (TextView) view.findViewById(R.id.GroupName);
        subGroupname.setText(subGroups.get(position).getGroupName());

        TextView groupDes = (TextView) view.findViewById(R.id.groupDes);
        groupDes.setText(subGroups.get(position).getGroupDescription());


        String subtype= subGroups.get(position).getTypeInside();
        TextView hidenType=(TextView) view.findViewById(R.id.hidenType);
        hidenType.setText(subtype);
        ImageView typeImage=(ImageView) view.findViewById(R.id.type_icon);
        switch (subtype){
            case GROUPS:
                typeImage.setImageResource(R.drawable.icon_groups);
                break;
            case AGENTS:
                typeImage.setImageResource(R.drawable.icon_agents);
                break;
            default:
                typeImage.setImageResource(R.drawable.icon_groups);
                break;
        }

        TextView quantiy=(TextView) view.findViewById(R.id.noOfGroup);
        quantiy.setText(Integer.toString(subGroups.get(position).getSize()));


        return view;
    }
}
