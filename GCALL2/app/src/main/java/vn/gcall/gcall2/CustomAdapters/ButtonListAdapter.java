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

import vn.gcall.gcall2.DataStruct.CustomButton;
import vn.gcall.gcall2.R;

/**
 * Created by This PC on 14/06/2016.
 * Listview adapter supports to show button in More tab
 */
public class ButtonListAdapter extends BaseAdapter {
    private Activity activity;
    private ArrayList<CustomButton> ButtonNames;
    private LayoutInflater inflater=null;
    public ButtonListAdapter(Activity a,ArrayList<CustomButton> listButton ){
        activity=a;
        ButtonNames=listButton;
        inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return ButtonNames.size();
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
        View view = convertView;
        if (view==null){
            view=inflater.inflate(R.layout.list_row_button,null);
        }

        ImageView imageView=(ImageView)view.findViewById(R.id.icon_button);
        imageView.setImageResource(ButtonNames.get(position).getResourceID());
        TextView buttonLabel=(TextView)view.findViewById(R.id.button_label);
        buttonLabel.setText(ButtonNames.get(position).getLabel());
        return view;
    }
}
