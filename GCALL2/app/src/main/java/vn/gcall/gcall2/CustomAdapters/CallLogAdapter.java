package vn.gcall.gcall2.CustomAdapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import vn.gcall.gcall2.DataStruct.CallLog;
import vn.gcall.gcall2.R;

/**
 * Created by This PC on 28/06/2016.
 * Listview Adapter supports to show call log in Call tab, unsolved screen
 */
public class CallLogAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<CallLog> callLogArrayList;
    private LayoutInflater inflater=null;
    private static final String BUSY="busy";
    private static final String NOANSWER="no-answer";
    private static final String COMPLETED="completed";
    private static final String UNSOLVED="unsolved";
    public CallLogAdapter(Activity a, ArrayList<CallLog> callLogs){
        activity=a;
        callLogArrayList=callLogs;
        inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return callLogArrayList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=convertView;
        if(view==null){
            view=inflater.inflate(R.layout.list_row_call_log,null);
        }

        String from=callLogArrayList.get(position).getFrom();
        String group=callLogArrayList.get(position).getGroup();
        String date=callLogArrayList.get(position).getDateCreated();
        String duration=callLogArrayList.get(position).getDuration();
        String status=callLogArrayList.get(position).getStatus();
        String objID=callLogArrayList.get(position).getObjectID();
        String gid=callLogArrayList.get(position).getGroupID();
        TextView objectID=(TextView)view.findViewById(R.id.hiden_objID);
        objectID.setText(objID);

        TextView callLogNumber=(TextView)view.findViewById(R.id.callLogNumber);
        callLogNumber.setText(from);
        TextView call_to=(TextView)view.findViewById(R.id.call_to);
        TextView call_hotline=(TextView)view.findViewById(R.id.callto_hotline);
        if(gid.equals(group)){
            call_to.setText("--");
            call_hotline.setText(group);
        }else{
            call_to.setText(group);
            String[] separate=gid.split("-");
            call_hotline.setText(separate[0]);
        }



        TextView call_status=(TextView)view.findViewById(R.id.call_status);
        call_status.setText(status);

        TextView call_duration=(TextView) view.findViewById(R.id.call_duration);

        if (duration.equals("0 secs")){
            view.findViewById(R.id.layout_duration).setVisibility(View.GONE);
        }else {
            view.findViewById(R.id.layout_duration).setVisibility(View.VISIBLE);
            call_duration.setText(duration);
        }

        TextView timestamp=(TextView)view.findViewById(R.id.timestamp);
        timestamp.setText(date);

        ImageView imageView=(ImageView)view.findViewById(R.id.typeOfCall);
        if (status.equals(COMPLETED)||status.equals(BUSY)){
            imageView.setImageResource(R.drawable.icon_incomingcall);
        }else if (status.equals(NOANSWER)||status.equals(UNSOLVED)){
            imageView.setImageResource(R.drawable.icon_missedcall);
            callLogNumber.setTextColor(Color.RED);
        }else{//status="solved by+ agent anme"
            imageView.setImageResource(R.drawable.icon_missedcall);
        }

        return view;
    }
}
