package vn.gcall.gcall2.Helpers;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.readystatesoftware.viewbadger.BadgeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import vn.gcall.gcall2.TabViewActivity;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;

/**
 * Created by This PC on 04/08/2016.
 * Class count the number of notifications to show in badger view (in Notification tab)
 */
public class NotificationCounter {
    private Context context;
    private BadgeView notiBadgeView;
    private int notificationCount=0;

    private Runnable block=new Runnable() {
        @Override
        public void run() {
            HashMap<String, String> params=new HashMap<>();
            params.put("sessionToken", TabViewActivity.token);
            final String URL= URLManager.getInvitationAPI();
            final JsonObjectRequest request=new JsonObjectRequest(URL, new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try{
                        if (response.isNull("error")){
                            int temp= response.getJSONArray("data").length();
                            Log.d("Res-noti",response.toString());
                            setNotificationCount(temp);
                            showBadgeView();
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            VolleySingleton.getInstance().getRequestQueue().add(request);
        }
    };

    public Runnable getBlock() {
        return block;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount = notificationCount;
    }

    public void getNotificationNumber(Runnable block){
        block.run();
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public NotificationCounter(Context c){
        this.context=c;
        getNotificationNumber(block);
    }

    public BadgeView getBadgeView(View view){
        notiBadgeView=new BadgeView(context,view);
        return notiBadgeView;
    }

    private void showBadgeView(){
        String value="";
        if (getNotificationCount()>0){
            value=String.valueOf(getNotificationCount());
            notiBadgeView.setText(value);
            notiBadgeView.show();
        }else {
            notiBadgeView.hide();
        }
    }
}
