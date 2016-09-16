package vn.gcall.gcall2;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;

import tourguide.tourguide.Overlay;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;
import vn.gcall.gcall2.CustomAdapters.CustomListHotlineAdapter;
import vn.gcall.gcall2.DataStruct.Hotline;
import vn.gcall.gcall2.Helpers.SessionManager;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;

public class ManageActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private FloatingActionButton fab_addHotline;
    private Toolbar toolbar;
    ListView list_hotline;
    ImageView empty_rectangle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CustomListHotlineAdapter listAdapter;
    private ArrayList<Hotline> hotlineList;
    private static final String MASTER="master";
    private SessionManager manager;
    private TextView tv_message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);


        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Hotlines");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        manager=new SessionManager();
        fab_addHotline= (FloatingActionButton)findViewById(R.id.fab_addHotline);
        tv_message=(TextView) findViewById(R.id.message);
        fab_addHotline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getApplicationContext(),AddCellphoneNumberActivity.class);
                startActivity(intent);
            }
        });
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        list_hotline= (ListView)findViewById(R.id.list_hotline);
        hotlineList=new ArrayList<>();
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        empty_rectangle=(ImageView)findViewById(R.id.empty_number);
        listAdapter = new CustomListHotlineAdapter(this, hotlineList);
        list_hotline.setAdapter(listAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
//        swipeRefreshLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                swipeRefreshLayout.setRefreshing(true);
//                getListHotLine();
//            }
//        });
        if (hotlineList.size()==0){
            empty_rectangle.setVisibility(View.VISIBLE);
            list_hotline.setVisibility(View.GONE);
            tv_message.setVisibility(View.VISIBLE);
        }

        list_hotline.setLongClickable(true);
        list_hotline.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String hotline= ((TextView)view.findViewById(R.id.title_hotline_number)).getText().toString();
                final AlertDialog.Builder deleteHotlineDialogBuilder=new AlertDialog.Builder(ManageActivity.this);
                deleteHotlineDialogBuilder.setTitle("Delete Hotline");
                deleteHotlineDialogBuilder.setMessage("Are you sure to delete hotline "+hotline+"?");
                deleteHotlineDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final HashMap<String,String> params=new HashMap<String, String>();
                        params.put("sessionToken",TabViewActivity.token);
                        JSONObject postData=new JSONObject(params);
                        deleteHotline(postData,hotline);
//                        onBackPressed();
                    }
                });
                deleteHotlineDialogBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                deleteHotlineDialogBuilder.create().show();
                return true;
            }
        });
        list_hotline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedNumber= ((TextView)view.findViewById(R.id.title_hotline_number)).getText().toString();
                String type= ((TextView)view.findViewById(R.id.hiden_type)).getText().toString();
                Intent intent= new Intent(getApplicationContext(),HotlineDetail.class);
                intent.putExtra("HOTLINE",selectedNumber);
                intent.putExtra("TYPE",type);
                intent.putExtra("ROLE",MASTER);
                startActivity(intent);
                Log.d("Item",selectedNumber);
            }
        });
    }

    @Override
    public void onRefresh() {
        hotlineList.clear();
        getListHotLine();
    }

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                hotlineList.clear();
                getListHotLine();
            }
        });
    }

    private void getListHotLine(){
        String sessionToken=TabViewActivity.token;
        HashMap<String,String> params= new HashMap<>();
        params.put("sessionToken",sessionToken);
        invokeWS(params);
    }

    public void invokeWS(HashMap<String,String> postparams){
        swipeRefreshLayout.setRefreshing(true);
        final String URL= URLManager.getShowHotlineAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, new JSONObject(postparams), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.isNull("error")){
                        JSONArray groups=response.getJSONArray("data");
                        if(groups.length()>0){
                            for (int i=0;i<groups.length();i++){
                                String hl= groups.getJSONObject(i).getString("hotline").toString();
                                String t=groups.getJSONObject(i).getString("has").toString();
                                int le=Integer.parseInt(groups.getJSONObject(i).getString("length").toString());
                                Hotline hotline=new Hotline(hl,t,le);
                                hotlineList.add(hotline);
                                Log.d("hotline",hotlineList.get(i).getHotline());
                            }
                            listAdapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        empty_rectangle.setVisibility(View.GONE);

                        list_hotline.setVisibility(View.VISIBLE);
                        if(hotlineList.size()==0){
                            fab_addHotline.setVisibility(View.VISIBLE);
                            tv_message.setVisibility(View.VISIBLE);
                        }else {
                            fab_addHotline.setVisibility(View.GONE);
                            tv_message.setVisibility(View.GONE);
                        }
                    }else{
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
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }

    private void deleteHotline(JSONObject params,String subgroup){
        final String URL=URLManager.getDeleteGroupAPI(subgroup);
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.isNull("error")){
                        Toast.makeText(getApplicationContext(),"Hotline has been deleted",Toast.LENGTH_SHORT).show();
                        refreshList();
                    }else {
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

    private void refreshList(){
        hotlineList.clear();
        getListHotLine();
    }

}
