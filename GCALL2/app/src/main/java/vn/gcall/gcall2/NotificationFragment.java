package vn.gcall.gcall2;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import vn.gcall.gcall2.CustomAdapters.NotificationListAdapter;
import vn.gcall.gcall2.DataStruct.Notification;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;


/**
 * A simple {@link Fragment} subclass.
 * Show list of invitation notification
 */
public class NotificationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private ListView notificationListview;
    private ArrayList<Notification> notifications;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static NotificationListAdapter notificationListAdapter;
    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_notification, container, false);
        notifications=new ArrayList<>();
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        notificationListview=(ListView)view.findViewById(R.id.notifications_listview);
        notificationListAdapter=new NotificationListAdapter(getActivity(),notifications);
        notificationListview.setAdapter(notificationListAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                getNotification();
                TabViewActivity.notificationCounter.getNotificationNumber(TabViewActivity.notificationCounter.getBlock());
            }
        });
        /*
        * Click an item, a dialog will be shown
        * */
        notificationListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String groupID=((TextView)view.findViewById(R.id.groupID)).getText().toString();
                String groupName=((TextView)view.findViewById(R.id.from_groupName)).getText().toString();
                String addedBy=((TextView)view.findViewById(R.id.from_agent)).getText().toString();
                final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                builder.setTitle("Invite join group");
                builder.setMessage(addedBy+" invited you to group "+groupName);
                final JSONObject params=new JSONObject();
                try {
                    params.put("sessionToken", TabViewActivity.token);
                    params.put("deviceType","android");
                    params.put("voipToken", FirebaseInstanceId.getInstance().getToken());
                }catch (JSONException e){
                    e.printStackTrace();
                }
                /*
                * Click ACCEPT to the accept invitation to join a group/hotline
                * */
                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postInvitationRespond(params,groupID,true);
                    }
                });
                /*
                * Click REJECT to the decline invitation to join a group/hotline
                * */
                builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postInvitationRespond(params,groupID,false);
                    }
                });
                AlertDialog dialog=builder.create();
                dialog.show();
            }
        });
        return view;
    }

    @Override
    public void onRefresh() {
        notifications.clear();
        getNotification();
    }
    /*
    * Set up parameter to get notifications
    * */
    private void getNotification(){
        String sessionToken=TabViewActivity.token;
        HashMap<String,String> params=new HashMap<>();
        params.put("sessionToken",sessionToken);
        invokeGetNotification(params);
    }

    /*
    * Call API to get notifications
    * */
    private void invokeGetNotification(HashMap<String,String> params){
        swipeRefreshLayout.setRefreshing(true);
        final String URL= URLManager.getInvitationAPI();
        final JsonObjectRequest request= new JsonObjectRequest(URL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    Log.d("Notification",response.getString("data"));
                    if (response.isNull("error")){
                        JSONArray data=response.getJSONArray("data");
                        TabViewActivity.notificationCounter.setNotificationCount(data.length());
                        TabViewActivity.notificationCounter.getNotificationNumber(TabViewActivity.notificationCounter.getBlock());
                        for (int i=0;i<data.length();i++) {
                            String id = data.getJSONObject(i).getString("groupId");
                            String name = data.getJSONObject(i).getString("groupName");
                            String hotline = data.getJSONObject(i).getString("hotline");
                            String addedBy = data.getJSONObject(i).getString("addedBy");
                            Notification notification = new Notification(id, name, hotline, addedBy);
                            notifications.add(notification);
                            Log.d("Notification ", "list " + notifications.get(i).getGroupID());
                        }
                        notificationListAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }else {
                        Toast.makeText(getContext(),response.getJSONObject("error").getString("error"),Toast.LENGTH_SHORT).show();
                    }
                }catch(JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                swipeRefreshLayout.setRefreshing(false);
                error.printStackTrace();
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    /*
    * Call API to response the invitation
    * */
    private void postInvitationRespond(JSONObject params, String hotline, final boolean isAccept){
        final String URL=URLManager.getRespondInvitationAPI(hotline);
        try {
            if (isAccept){
                params.put("accept",1);
            }else params.put("accept",0);
        }catch (JSONException e){
            e.printStackTrace();
        }
        Log.d("Notification","Invitation "+params.toString());
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Notification","res "+response.toString());
                try{
                    if (response.isNull("error")){
                        if(isAccept){
                            Toast.makeText(getContext(),"You have been add in to new group!",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getContext(),"Invitation have been removed",Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getContext(),response.getString("error"),Toast.LENGTH_SHORT).show();
                    }
                    notifications.clear();
                    getNotification();
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
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance().getRequestQueue().add(request);
        TabViewActivity.notificationCounter.getNotificationNumber(TabViewActivity.notificationCounter.getBlock());
    }
}
