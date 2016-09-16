package vn.gcall.gcall2.CustomAdapters;

import android.app.Activity;
import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import vn.gcall.gcall2.DataStruct.HotlineToBuy;
import vn.gcall.gcall2.R;

/**
 * Created by This PC on 25/05/2016.
 * Listview adapter support to show list of hotline client can buy in Buy hotline screen (Old version
 */
public class CustomListBuyHotlineAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<HotlineToBuy> buyHotlineList;
    LayoutInflater inflater=null;

    public CustomListBuyHotlineAdapter(Activity a,ArrayList<HotlineToBuy> buyList){
        activity=a;
        buyHotlineList=buyList;
        inflater=(LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return buyHotlineList.size();
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
        TextView alias=(TextView) view.findViewById(R.id.title_hotline_number);
        TextView hidenNumber=(TextView) view.findViewById(R.id.hiden_number);
        String hiden= buyHotlineList.get(position).getNumber().toLowerCase();
        hidenNumber.setText(hiden);
        String title= PhoneNumberUtils.formatNumber(buyHotlineList.get(position).getNumber().toString(),"US");
        alias.setText(title);

        return view;
    }
}
