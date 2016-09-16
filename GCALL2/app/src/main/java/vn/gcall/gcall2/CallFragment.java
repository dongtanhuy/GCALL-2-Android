package vn.gcall.gcall2;


import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import vn.gcall.gcall2.CustomAdapters.CallLogAdapter;
import vn.gcall.gcall2.DataStruct.CallLog;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;


/**
 * A simple {@link Fragment} subclass.
 * Show the list of call log and the list of unsolved call
 * Unsolved call= call comes from Safary Browser
 */
public class CallFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private ListView callLog_listview,unsolved_listview;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<CallLog> callLogs,unsolvedLogs;
    private CallLogAdapter callLogAdapter,unsolvedAdapter;
    private int skip;
    private boolean hasNext=false,isCallLog=true;
    public CallFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_call, container, false);
        skip = 0;
        unsolved_listview=(ListView) view.findViewById(R.id.unsolved_listview);
        unsolvedLogs=new ArrayList<>();
        unsolvedAdapter=new CallLogAdapter(getActivity(),unsolvedLogs);
        unsolved_listview.setAdapter(unsolvedAdapter);

        /*
        * Handle event when user click on an unsolved call log
        * Show a dialog to notify check if user want to call back this user by cell phone
        * */
        unsolved_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String objID=((TextView)view.findViewById(R.id.hiden_objID)).getText().toString();
                Log.d("OBJID",objID);
                final String caller=((TextView)view.findViewById(R.id.callLogNumber)).getText().toString();
                AlertDialog.Builder dialogBuider=new AlertDialog.Builder(getContext());
                dialogBuider.setTitle("Taking care this customer");
                dialogBuider.setMessage("Do you want to call back to this customer?");

                /*
                * If they click YES, send a request to server to notify that this agent will call back to this customer
                * then navigate to phone screen of the phone
                * */
                dialogBuider.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String,String> params=new HashMap<String, String>();
                        params.put("sessionToken",TabViewActivity.token);
                        params.put("objectId",objID);
                        JSONObject postData=new JSONObject(params);
                        postResponseSovling(postData,caller);
                    }
                });
                dialogBuider.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                dialogBuider.create().show();
            }
        });

        callLog_listview = (ListView) view.findViewById(R.id.callLog_listview);
        callLogs = new ArrayList<>();
        callLogAdapter = new CallLogAdapter(getActivity(), callLogs);


        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        /*
        * Send request to get list of call log
        * */
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                getCallLog();

            }
        });

        callLog_listview.setAdapter(callLogAdapter);
        /*
        * Continue loading more call logs when user scroll to the end of the list
        * */
        callLog_listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(firstVisibleItem+visibleItemCount == totalItemCount && totalItemCount!=0)
                {
                    Log.d("SCROLL","end list");
                    if (hasNext) {
                        getCallLog();
                    }
                }
            }
        });
        return view;
    }
    /*
    * Refresh the listview
    * */
    @Override
    public void onRefresh() {
        skip=0;
        callLogs.clear();
        getCallLog();
    }

    /*
    * Open Dialer of the phone
    * */
    private void openDialer(String phoneNum){
        Intent intent=new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:"+phoneNum));
        startActivity(intent);
    }

    /*
    * Set up parameters to post to server to get list of call logs
    * */
    private void getCallLog() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("sessionToken", TabViewActivity.token);
            jsonObject.put("skip", skip);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        invokeWS(jsonObject);
    }

    /*
    * Send a request to notify that agent will call back to customer and open dialer
    * */
    private void postResponseSovling(JSONObject params,final String caller){
        final String URL=URLManager.getResponseSolvingAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getInt("success")==1){
                        unsolvedLogs.clear();
                        getUnsolved();
                        openDialer(caller);
                    }else {
                        Toast.makeText(getContext(),response.getString("error"),Toast.LENGTH_SHORT).show();
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


    /*
    * Call the API to get the list of call logs
    * */
    private void invokeWS(JSONObject params){
        final String URL= URLManager.getShowCallogsAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("CALLRES",response.toString());
                Log.d("SKIP",Integer.toString(skip));
                try{
                    hasNext=response.getBoolean("hasNext");
                    if(response.isNull("error")){
                        ArrayList<CallLog> temp=new ArrayList<>();
                        JSONArray data=response.getJSONArray("data");
                        for (int i=0;i<data.length();i++){
                            String gap=data.getJSONObject(i).getString("gap");
                            String duration=data.getJSONObject(i).getString("duration");
                            String from=data.getJSONObject(i).getString("from");
                            String status=data.getJSONObject(i).getString("status");
                            String groupName=data.getJSONObject(i).getString("groupName");
                            String gid=data.getJSONObject(i).getString("groupId");
                            CallLog log=new CallLog(gap,duration,from,groupName,status,gid);
                            temp.add(log);
                        }
                        callLogs.addAll(temp);
                        skip+=15;
                        callLogAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }else{
                        Toast.makeText(getContext(),response.getString("error"),Toast.LENGTH_SHORT);
                    }
                }catch (JSONException e){
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.activity_main_action,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_unsolved://Show list unsolved call logs
                isCallLog=false;
                swipeRefreshLayout.setVisibility(View.GONE);
                unsolved_listview.setVisibility(View.VISIBLE);
                unsolvedLogs.clear();
                getUnsolved();
                return true;
            case R.id.action_callLog://Show list of call log (Miss call, successfull call...)
                isCallLog=true;
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                unsolved_listview.setVisibility(View.GONE);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * Set up parameters to post to server to get list of unsolved call logs
    * */
    private void getUnsolved(){
        HashMap<String,String> params=new HashMap<>();
        params.put("sessionToken",TabViewActivity.token);
        JSONObject postData=new JSONObject(params);
        invokeGetUnsolved(postData);
    }


    /*
    * Call API that get list of unsolved cal logs
    * */
    private void invokeGetUnsolved(final JSONObject params){
        swipeRefreshLayout.setRefreshing(true);
        final String URL=URLManager.getGetUnsolvedAsAgentAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    Log.d("UNSOLVED",response.toString());
                    if (response.isNull("error")){
                        JSONArray data=response.getJSONArray("data");
                        for (int i=0;i<data.length();i++){
                            String gap=data.getJSONObject(i).getString("gap");
                            String from=data.getJSONObject(i).getString("caller");
                            String groupName=data.getJSONObject(i).getString("groupName");
                            String objID=data.getJSONObject(i).getString("objectId");
                            String gid=data.getJSONObject(i).getString("groupId");
                            CallLog log=new CallLog(gap,from,groupName,objID,gid);
                            unsolvedLogs.add(log);
                        }
                        unsolvedAdapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }else {
                        Toast.makeText(getContext(),response.getString("error"),Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e){
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
}
