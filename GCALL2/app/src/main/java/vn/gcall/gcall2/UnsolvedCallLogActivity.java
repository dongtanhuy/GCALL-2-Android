package vn.gcall.gcall2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.jaredrummler.materialspinner.MaterialSpinner;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import vn.gcall.gcall2.CustomAdapters.CallLogAdapter;
import vn.gcall.gcall2.DataStruct.CallLog;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;
/*
* Show list of unsolved call log for master of a hotline
* */
public class UnsolvedCallLogActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private MaterialSpinner spinner;
    private ArrayList<String> hotlinesList;
    private ListView unsolvedList;
    private CallLogAdapter adapter;
    private ArrayList<CallLog> unsolvedCallog;
    private android.app.AlertDialog loadingDialog;
    private final String FIRST_ELEMENT="Select hotline";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unsolved_call_log);

        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Unsolved calls history");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        loadingDialog=new SpotsDialog(this,R.style.Custom);
        spinner=(MaterialSpinner) findViewById(R.id.hotline_spiner);
        unsolvedList=(ListView) findViewById(R.id.unsolved_listview);
        hotlinesList=new ArrayList<>();
        getHotlinesList();
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();
        unsolvedCallog=new ArrayList<>();
        adapter=new CallLogAdapter(this,unsolvedCallog);
        hotlinesList.add(0,FIRST_ELEMENT);
        spinner.setItems(hotlinesList);
        /*
        * Choose a hotline and load the call log of this hotline
        * */
        spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                if(!item.equals(FIRST_ELEMENT)){
                    getUnsolevdByHotline(item);
                    loadingDialog.show();
                }else{
                    unsolvedCallog.clear();
                }
                unsolvedList.setAdapter(adapter);
            }
        });
    }

    /*
    * Set up parameter to get list of hotlines
    * */
    private void getHotlinesList(){
        HashMap<String,String> params= new HashMap<>();
        params.put("sessionToken",TabViewActivity.token);
        JSONObject postData=new JSONObject(params);
        invokeGetHotline(postData);
    }

    /*
    * Call API to get list of hotlines
    * */
    private void invokeGetHotline(JSONObject params){
        final String URL= URLManager.getShowHotlineAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.isNull("error")){
                        JSONArray data=response.getJSONArray("data");
                        for (int i=0;i<data.length();i++){
                            hotlinesList.add(data.getJSONObject(i).getString("hotline"));
                        }
                        loadingDialog.dismiss();
                    }else {
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(),response.getString("error"),Toast.LENGTH_SHORT).show();
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
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    /*
    * Set up parameter to get list of unsolved call
    * */
    private void getUnsolevdByHotline(String hotline){
        HashMap<String,String> params=new HashMap<>();
        params.put("sessionToken",TabViewActivity.token);
        JSONObject postData=new JSONObject(params);
        invokeFilterUnsolved(postData,hotline);
    }

    /*
    * Call API to get list of unsolved call
    * */
    private void invokeFilterUnsolved(JSONObject params,String hotline){
        final String URL=URLManager.getUnsolvedFilter(hotline);
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.isNull("error")){
                        JSONArray data=response.getJSONArray("data");
                        for (int i=0;i<data.length();i++){
                            String gap=data.getJSONObject(i).getString("gap");
                            String from=data.getJSONObject(i).getString("caller");
                            String status;
                            if (data.getJSONObject(i).isNull("solvedBy")){
                                status="unsolved";
                            }else {
                                JSONObject solvedBy=data.getJSONObject(i).getJSONObject("solvedBy");
                                status="solved by:"+solvedBy.getString("fullname");
                            }
                            String groupName=data.getJSONObject(i).getString("groupName");
                            String gid=data.getJSONObject(i).getString("groupId");
                            CallLog log=new CallLog(gap,"0 secs",from,groupName,status,gid);
                            unsolvedCallog.add(log);
                        }
                        adapter.notifyDataSetChanged();
                        loadingDialog.dismiss();
                    }else {
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(),response.getString("error"),Toast.LENGTH_SHORT).show();
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
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }
}
