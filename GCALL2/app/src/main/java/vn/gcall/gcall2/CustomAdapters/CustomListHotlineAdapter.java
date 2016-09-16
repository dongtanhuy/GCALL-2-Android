package vn.gcall.gcall2.CustomAdapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import vn.gcall.gcall2.DataStruct.Hotline;
import vn.gcall.gcall2.HotlineInfo;
import vn.gcall.gcall2.R;

/**
 * Created by This PC on 24/05/2016.
 * Listview adapter support to show lists of hotline in Manage screen
 */
public class CustomListHotlineAdapter extends BaseAdapter  {
    private Activity activity;
    private ArrayList<Hotline> hotlineList;
    private LayoutInflater inflater=null;

    private static final String GROUPS="subgroup";
    private static final String AGENTS="agent";

    public CustomListHotlineAdapter(Activity a, ArrayList<Hotline> hl){
        activity=a;
        hotlineList=hl;
        inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return hotlineList.size();
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
        if (convertView == null)
            view=inflater.inflate(R.layout.list_row,null);
        TextView hotline_title=(TextView) view.findViewById(R.id.title_hotline_number);
        TextView numberOfGroup=(TextView) view.findViewById(R.id.numOfGroup);
        ImageView image=(ImageView) view.findViewById(R.id.image_type);

        final String title= hotlineList.get(position).getHotline();
        hotline_title.setText(title);

        String number=Integer.toString(hotlineList.get(position).getLength());
        numberOfGroup.setText(number);

        String kind=hotlineList.get(position).getType();
        TextView hidenType=(TextView) view.findViewById(R.id.hiden_type);
        hidenType.setText(kind);
        switch (kind){
            case GROUPS:
                image.setImageResource(R.drawable.icon_groups);
                break;
            case AGENTS:
                image.setImageResource(R.drawable.icon_agents);
                break;
            default:
                image.setImageResource(R.drawable.icon_groups);
                break;
        }

        Button btn_info=(Button) view.findViewById(R.id.btn_info);
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(activity.getApplicationContext(),HotlineInfo.class);
                i.putExtra("HOTLINE",title);
                activity.startActivity(i);
            }
        });
        return view;
    }

}
