package vn.gcall.gcall2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import vn.gcall.gcall2.CustomAdapters.AgentListAdapter;
import vn.gcall.gcall2.DataStruct.Agent;
import vn.gcall.gcall2.Helpers.SessionManager;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.Helpers.Validation;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;

/**
 * Created by This PC on 04/06/2016.
 * Show detail of a subgroup, a subgroup maybe content a list of subgroup of list of agent (not both)
 * User can add more agent or subgroup in subgroup  (not both)
 */
public class SecondLayerSubgroupDetail extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    Toolbar toolbar;
    FloatingActionButton fab_add_agent,fab_add_group;
    FloatingActionsMenu fam;
    private ArrayList<Agent> agents;
    private AgentListAdapter agentListAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String hotlineExtension="";
    private String finalHotlineExtesion="";
    private String type="";
    private int  extension;
    private String role;
    private static final String AGENTS="agent";
    private static final String MASTER="master";
    private SwitchCompat self_adding_switch;
    private boolean isMasterAnAgent;
    private SessionManager manager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotline_detail);
        Bundle extras= getIntent().getExtras();

        if (extras!=null){
            hotlineExtension=extras.getString("HOTLINE");
            type=extras.getString("CONTENT_TYPE");
            extension=extras.getInt("EXTENSION");
            role=extras.getString("ROLE");
        }
        if(role.equals(AGENTS)){
            finalHotlineExtesion=hotlineExtension;
        }else{
            finalHotlineExtesion=hotlineExtension+"-"+extension;
        }
        TextView textView=(TextView)findViewById(R.id.url_text);
        textView.setText("Use "+"https://call.gcall.vn/"+finalHotlineExtesion+" to call");
        Log.d("FINAL",finalHotlineExtesion);
        manager=new SessionManager();
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(finalHotlineExtesion);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        agents=new ArrayList<>();
        agentListAdapter= new AgentListAdapter(this,agents);
        ListView agent_list_final = (ListView) findViewById(R.id.list_agent_group);
        swipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                getData();
            }
        });
        if (type.equals(AGENTS)){
            agent_list_final.setAdapter(agentListAdapter);
        }else {
            TextView message= (TextView)findViewById(R.id.message);
            message.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
        if (role.equals(MASTER)){
            agent_list_final.setLongClickable(true);
            /*
            * User long click on an item to delete a agent
            * */
            agent_list_final.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (type.equals(AGENTS)){
                        final String agentEmail=((TextView)view.findViewById(R.id.agent_email)).getText().toString();
                        final AlertDialog.Builder deleteAgentDialogBuilder=new AlertDialog.Builder(SecondLayerSubgroupDetail.this);
                        deleteAgentDialogBuilder.setTitle("Delete Agent");
                        deleteAgentDialogBuilder.setMessage("Are you sure to delete agent has email: "+agentEmail+"?");
                        /*
                        * Click YES to send request to delete agent
                        * */
                        deleteAgentDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final HashMap<String,String> params=new HashMap<String, String>();
                                params.put("sessionToken",TabViewActivity.token);
                                params.put("email",agentEmail);
                                if (agentEmail.equals(manager.getStringPreferences(getApplicationContext(),"EMAIL"))){
                                    manager.setBoolPreferences(getApplicationContext(),finalHotlineExtesion,false);
                                }
                                JSONObject postData=new JSONObject(params);
                                deleteAgent(postData,finalHotlineExtesion);
                            }
                        });
                        deleteAgentDialogBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        deleteAgentDialogBuilder.create().show();
                    }
                    return true;
                }
            });
        }

        fam=(FloatingActionsMenu)findViewById(R.id.fab_add_popup) ;
        fab_add_agent=(FloatingActionButton) findViewById(R.id.fab_addAgent);
        self_adding_switch=(SwitchCompat) findViewById(R.id.self_adding_switch);
        isMasterAnAgent=manager.getBoolPreferences(getApplicationContext(),finalHotlineExtesion);
        self_adding_switch.setChecked(isMasterAnAgent);
        RelativeLayout layout_switch=(RelativeLayout) findViewById(R.id.layout_switch);
        if (role.equals(AGENTS)){
            fam.setVisibility(View.GONE);
            layout_switch.setVisibility(View.GONE);
        }

        /*
        * Auto add/delete current agent become a agent of this group
        * */
        self_adding_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HashMap<String,String> params=new HashMap<String, String>();
                params.put("sessionToken",TabViewActivity.token);
                params.put("email",manager.getStringPreferences(getApplicationContext(),"EMAIL"));
                JSONObject postData=new JSONObject(params);
                if (isChecked){
                    addNewAgentWithAccept(postData,finalHotlineExtesion);
                    manager.setBoolPreferences(getApplicationContext(),finalHotlineExtesion,true);
                }else {
                    deleteAgent(postData,finalHotlineExtesion);
                    manager.setBoolPreferences(getApplicationContext(),finalHotlineExtesion,false);
                }
            }
        });

        /*
        * Add other agent in this hotline
        * Open a dialog to enter the email of agent want to add
        * */
        fab_add_agent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(SecondLayerSubgroupDetail.this);
                LayoutInflater inflater= getLayoutInflater();
                dialogBuider.setTitle("Add Agent");
                dialogBuider.setMessage("Enter email address of agent:");
                final View view= inflater.inflate(R.layout.add_agent_dialog,null);
                dialogBuider.setView(view);
                /*
                * Click SAVE to send a request that add a new agent in hotline
                * */
                dialogBuider.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String agentEmail= ((EditText)view.findViewById(R.id.input_agent_email)).getText().toString();
                        if (Validation.isNotNull(agentEmail)){
                            if(Validation.validate(agentEmail)){
                                final HashMap<String,String> params=new HashMap<>();
                                params.put("email",agentEmail);
                                params.put("sessionToken",TabViewActivity.token);
                                JSONObject postData= new JSONObject(params);
                                addNewAgent(postData,finalHotlineExtesion);
                            }else {
                                Toast.makeText(getApplicationContext(),"Invalid Email format!",Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            Toast.makeText(getApplicationContext(),"Please enter email of agent!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialogBuider.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialogBuider.create().show();
            }
        });
        fab_add_group=(FloatingActionButton)findViewById(R.id.fab_addGroup);
        fab_add_group.setVisibility(View.GONE);
    }

    /*
    * Refresh the list of subgroups or agents
    * */
    @Override
    public void onRefresh() {
        agents.clear();
        getData();
    }

    /*
    * Set up parameters to send request to get detail of hotline
    * */
    private void getData(){
        HashMap<String,String> params= new HashMap<>();
        params.put("sessionToken",TabViewActivity.token);
        params.put("role",role);
        invokeWS(params,finalHotlineExtesion);
    }

    /*
    * Call API to get hotline detail
    * */
    private void invokeWS(HashMap<String,String> params, String hotline){
        swipeRefreshLayout.setRefreshing(true);
        final String URL= URLManager.getShowInsideHotlineAPI(hotline);// "http://teamthuduc.gcall.vn:3000/api/"+hotline;
        final JsonObjectRequest request = new JsonObjectRequest(URL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("LASTLAYER",response.toString());
                try {
                    if(response.isNull("error")){
                        JSONArray agentList=response.getJSONArray("data");
                        if(agentList.length()>0){
                            for (int i=0;i<agentList.length();i++){
                                String email=agentList.getJSONObject(i).getString("email");
                                String name=agentList.getJSONObject(i).getString("fullname");
                                String phone=agentList.getJSONObject(i).getString("phone");
                                Boolean accept=agentList.getJSONObject(i).getBoolean("accepted");
                                Agent agent= new Agent(name,email,phone,accept);
                                agents.add(agent);
                            }
                            agentListAdapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
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
    * Call API to add new agent
    * */
    private void addNewAgent(JSONObject postData,String hotline){
        final String URL=URLManager.getAddAgentAPI(hotline);//"http://teamthuduc.gcall.vn:3000/api/"+hotline+"/add/agent";
        final JsonObjectRequest request = new JsonObjectRequest(URL, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.isNull("error")){
                        Toast.makeText(getApplicationContext(),"New Agent has been created",Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }else {
                        Toast.makeText(getApplicationContext(),response.getJSONObject("error").getString("error"),Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                Log.d("ADDAGENT",response.toString());
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
    * Call API to delete agent
    * */
    private void deleteAgent(JSONObject params,String hotline){
        final String URL=URLManager.getDeleteAgentAPI(hotline);
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.isNull("error")){
                        Toast.makeText(getApplicationContext(),"Agent has been deleted",Toast.LENGTH_SHORT).show();
                        onBackPressed();
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

    /*
    * Call API to add current agent to this hotline
    * */
    private void addNewAgentWithAccept(JSONObject postData,final String hotline){
        final String URL=URLManager.getAddAgentAPI(hotline);
        final JsonObjectRequest request = new JsonObjectRequest(URL, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.isNull("error")){
                        postInvitationRespond(finalHotlineExtesion);
                        onBackPressed();
                    }else {
                        Toast.makeText(getApplicationContext(),response.getString("error"),Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                Log.d("ADDAGENT",response.toString());
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
    * Auto accept invitation when adding current agent to hotline
    * */
    private void postInvitationRespond(String hotline){
        final String URL=URLManager.getRespondInvitationAPI(hotline);
        final JSONObject params=new JSONObject();
        try {
            params.put("sessionToken", TabViewActivity.token);
            params.put("deviceType","android");
            params.put("voipToken", FirebaseInstanceId.getInstance().getToken());
            params.put("accept",1);
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
                        Toast.makeText(getApplicationContext(),"You have been add in to new group!",Toast.LENGTH_SHORT).show();
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
}
