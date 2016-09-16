package vn.gcall.gcall2.CustomAdapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import java.util.ArrayList;

import vn.gcall.gcall2.DataStruct.Agent;
import vn.gcall.gcall2.R;

/**
 * Created by This PC on 01/06/2016.
 * Listview Adapter supports to show detail of agent in a group/hotline
 */
public class AgentListAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<Agent> agents;
    private LayoutInflater inflater=null;
    public AgentListAdapter(Activity a, ArrayList<Agent> agentList){
        activity=a;
        agents=agentList;
        inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return agents.size();
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
        View view =convertView;
        if (view==null){
            view=inflater.inflate(R.layout.list_row_agent,null);
        }

        TextView agent_name= (TextView)view.findViewById(R.id.agent_name);
        agent_name.setText(agents.get(position).getFullname());

        TextView agent_email = (TextView)view.findViewById(R.id.agent_email);
        agent_email.setText(agents.get(position).getEmail());
        TextView agent_phone= (TextView)view.findViewById(R.id.agent_phone);
        agent_phone.setText(agents.get(position).getPhone());
        boolean checkAccepted= agents.get(position).isAccepted();
        TextView status= (TextView)view.findViewById(R.id.accept_status);
        if (checkAccepted){
            status.setVisibility(View.GONE);
        }else {
            status.setVisibility(View.VISIBLE);
        }
        ImageView imageView= (ImageView) view.findViewById(R.id.img_textDrawable);
        TextDrawable drawable= TextDrawable.builder().buildRound(agents.get(position).getFullname().substring(0,1), ContextCompat.getColor(activity.getApplicationContext(),R.color.primary));
        imageView.setImageDrawable(drawable);
        return view;
    }
}
