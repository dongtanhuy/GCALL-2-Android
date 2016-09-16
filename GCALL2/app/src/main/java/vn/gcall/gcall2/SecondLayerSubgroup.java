package vn.gcall.gcall2;

import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.ArrayAdapter;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import vn.gcall.gcall2.CustomAdapters.AgentListAdapter;
import vn.gcall.gcall2.CustomAdapters.SubGroupListAdapter;
import vn.gcall.gcall2.DataStruct.Agent;
import vn.gcall.gcall2.DataStruct.SubGroup;
import vn.gcall.gcall2.Helpers.SessionManager;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.Helpers.Validation;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;
/*
* Show detail of a subgroup, a subgroup maybe content a list of subgroup of list of agent (not both)
* User can add more agent or subgroup in subgroup  (not both)
* */
public class SecondLayerSubgroup extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    FloatingActionButton fab_add_agent,fab_add_group;
    Toolbar toolbar;
    private static final String MASTER="master";
    //Agent data
    private ArrayList<Agent> agents;
    //Subgroup data
    private ArrayList<SubGroup> subGroups;
    AgentListAdapter agentAdapter=null;
    SubGroupListAdapter groupListAdapter=null;
    private String hotline="";
    private String hotlineExtension="";
    private String type="";
    private int  extension;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String GROUPS="subgroup";
    private static final String AGENTS="agent";
    private ArrayList<Integer>inUsedExt=null;
    private String role;
    private ListView agent_group_Second_list;
    private SwitchCompat self_adding_switch;
    private boolean isMasterAnAgent;
    private SessionManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotline_detail);

        Bundle extras= getIntent().getExtras();

        if (extras!=null){
            hotline=extras.getString("HOTLINE");
            type=extras.getString("CONTENT_TYPE");
            extension=extras.getInt("EXTENSION");
            role=extras.getString("ROLE");
        }
        manager=new SessionManager();
        Log.d("SECONDTYPE",type);
        hotlineExtension=hotline+"-"+Integer.toString(extension);
        TextView textView=(TextView)findViewById(R.id.url_text);
        textView.setText("Use "+"https://call.gcall.vn/"+hotlineExtension+" to call");
        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(hotlineExtension);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        agents= new ArrayList<>();
        subGroups=new ArrayList<>();
        inUsedExt=new ArrayList<>();
        agentAdapter= new AgentListAdapter(this,agents);
        groupListAdapter= new SubGroupListAdapter(this,subGroups);
        agent_group_Second_list= (ListView) findViewById(R.id.list_agent_group);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
//        swipeRefreshLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                swipeRefreshLayout.setRefreshing(true);
//                getData();
//            }
//        });
        if(type.equals(GROUPS)){
            agent_group_Second_list.setAdapter(groupListAdapter);
        }else if(type.equals(AGENTS)){
            agent_group_Second_list.setAdapter(agentAdapter);
        }else {
            TextView message= (TextView)findViewById(R.id.message);
            message.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
        if(role.equals(MASTER)){
            agent_group_Second_list.setLongClickable(true);
            /*
            * User long click on an item to delete a group or a agent
            * */
            agent_group_Second_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if (type.equals(AGENTS)){
                        final String agentEmail=((TextView)view.findViewById(R.id.agent_email)).getText().toString();
                        final AlertDialog.Builder deleteAgentDialogBuilder=new AlertDialog.Builder(SecondLayerSubgroup.this);
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
                                    manager.setBoolPreferences(getApplicationContext(),hotlineExtension,false);
                                }
                                JSONObject postData=new JSONObject(params);
                                deleteAgent(postData,hotlineExtension);
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
                    if (type.equals(GROUPS)){
                        final String subgroup=hotlineExtension+"-"+((TextView)view.findViewById(R.id.ext_text)).getText().toString();
                        final AlertDialog.Builder deleteGroupDialogBuilder=new AlertDialog.Builder(SecondLayerSubgroup.this);
                        deleteGroupDialogBuilder.setTitle("Delete Group");
                        deleteGroupDialogBuilder.setMessage("Are you sure to delete group "+subgroup+"?");
                        /*
                        * Click YES to send request to delete agent
                        * */
                        deleteGroupDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final HashMap<String,String> params=new HashMap<String, String>();
                                params.put("sessionToken",TabViewActivity.token);
                                JSONObject postData=new JSONObject(params);
                                deleteGroup(postData,subgroup);
                            }
                        });
                        deleteGroupDialogBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        deleteGroupDialogBuilder.create().show();
                    }
                    return true;
                }
            });
        }
        /*
        * If type==group, click on an item will show the detail of this group, navigate to the second layer subgroup detail
        * */
        agent_group_Second_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(type.equals(GROUPS)){
                    int selectedExt=Integer.parseInt(((TextView)view.findViewById(R.id.ext_text)).getText().toString());
                    String subgroupContent= ((TextView)view.findViewById(R.id.hidenType)).getText().toString();
                    //////////
                    Log.d("SECONDEXT",Integer.toString(selectedExt));
                    Log.d("SECONDCONTENT",subgroupContent);
                    Log.d("WHOLEHOTLINE",hotlineExtension);
                    //////////
                    Intent intent = new Intent(getApplicationContext(),SecondLayerSubgroupDetail.class);
                    intent.putExtra("EXTENSION",selectedExt);
                    intent.putExtra("CONTENT_TYPE",subgroupContent);
                    intent.putExtra("HOTLINE",hotlineExtension);
                    intent.putExtra("ROLE",role);
                    startActivity(intent);
                }
            }
        });

        fab_add_agent=(FloatingActionButton) findViewById(R.id.fab_addAgent);
        self_adding_switch=(SwitchCompat) findViewById(R.id.self_adding_switch);
        isMasterAnAgent=manager.getBoolPreferences(getApplicationContext(),hotlineExtension);
        self_adding_switch.setChecked(isMasterAnAgent);
        RelativeLayout layout_switch=(RelativeLayout) findViewById(R.id.layout_switch);
        if (type.equals(GROUPS)) {
            fab_add_agent.setVisibility(View.GONE);
            layout_switch.setVisibility(View.GONE);
        }
        /*
        * Auto add/delete current agent become a agent of this hotline
        * */
        self_adding_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                HashMap<String,String> params=new HashMap<String, String>();
                params.put("sessionToken",TabViewActivity.token);
                params.put("email",manager.getStringPreferences(getApplicationContext(),"EMAIL"));
                JSONObject postData=new JSONObject(params);
                if (isChecked){
                    addNewAgentWithAccept(postData,hotlineExtension);
                    manager.setBoolPreferences(getApplicationContext(),hotlineExtension,true);
                }else {
                    deleteAgent(postData,hotlineExtension);
                    manager.setBoolPreferences(getApplicationContext(),hotlineExtension,false);
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
                final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(SecondLayerSubgroup.this);
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
                                addNewAgent(postData,hotlineExtension);
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
        //Will visible when in full funciton account
//        fab_add_group.setVisibility(View.GONE);
        //-----------------------------------------//
        if(type.equals(AGENTS))
            fab_add_group.setVisibility(View.GONE);

        /*
        * Creata a new subgroup in this hotline
        * Open a dialog for user to enter the name,description and the extension of the new subgroup
        * */
        fab_add_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(SecondLayerSubgroup.this);
                LayoutInflater inflater= getLayoutInflater();
                dialogBuider.setTitle("Create new Group");
                dialogBuider.setMessage("Enter group name and decription:");
                final View view=inflater.inflate(R.layout.add_subgroup_dialog,null);
                dialogBuider.setView(view);
                ArrayList<Integer> ext_list=HotlineDetail.createArrayList();
                ext_list.removeAll(inUsedExt);
                final ArrayAdapter<Integer> listExtensionAdapter= new ArrayAdapter<>(getApplicationContext(),R.layout.spinner_item,ext_list);
                final MaterialBetterSpinner ExtSpinner= (MaterialBetterSpinner) view.findViewById(R.id.ext_spinner);
                ExtSpinner.setAdapter(listExtensionAdapter);
                /*
                * Click YES: validate the input and set up parameter to send request to add new subgroup
                * */
                dialogBuider.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String groupName= ((EditText)view.findViewById(R.id.input_GroupName)).getText().toString();
                        String groupDes= ((EditText)view.findViewById(R.id.input_GroupDescription)).getText().toString();
                        final String extNumber=ExtSpinner.getText().toString();
                        if (Validation.isNotNull(groupName)&&Validation.isNotNull(groupDes)&&Validation.isNotNull(extNumber)){
                            final HashMap<String,String> params=new HashMap<>();
                            params.put("sessionToken",TabViewActivity.token);
                            params.put("name",groupName);
                            params.put("description",groupDes);
                            params.put("extension",extNumber);
                            JSONObject postData= new JSONObject(params);
                            addNewGroup(postData,hotlineExtension);
                        }else {
                            Toast.makeText(getApplicationContext(),"Don't leave any fields blank",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialogBuider.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog= dialogBuider.create();
                if (inUsedExt.size()>=10){
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
                dialog.show();
            }
        });
    }
    /*
    * Refresh the list of subgroups or agents
    * */
    @Override
    public void onRefresh() {
        agents.clear();
        subGroups.clear();
        getData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                agents.clear();
                subGroups.clear();
                getData();
                agent_group_Second_list.refreshDrawableState();
                agent_group_Second_list.invalidate();
            }
        });
    }

    /*
    * Set up parameters to send request to get detail of hotline
    * */
    private void getData(){
        HashMap<String, String> params= new HashMap<>();
        params.put("sessionToken",TabViewActivity.token);
        params.put("role",MASTER);
        invokeWS(params,hotlineExtension);
    }
    /*
    * Call API to get hotline detail
    * */
    private void invokeWS(HashMap<String,String> params,String hotline){
        swipeRefreshLayout.setRefreshing(true);
        final String URL= URLManager.getShowInsideHotlineAPI(hotline);
        final JsonObjectRequest request= new JsonObjectRequest(URL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("SECONDGROUP",response.toString());
                try{
                    if(response.isNull("error")){
                        if(response.getString("type").equals(GROUPS)){
                            JSONArray subgroupList=response.getJSONArray("data");
                            if(subgroupList.length()>0){
                                for (int i=0;i<subgroupList.length();i++){
                                    String name=subgroupList.getJSONObject(i).getString("name");
                                    String des=subgroupList.getJSONObject(i).getString("description");
                                    String insideType=subgroupList.getJSONObject(i).getString("has");
                                    int ext= Integer.parseInt(subgroupList.getJSONObject(i).getString("extension"));
                                    inUsedExt.add(ext);
                                    int quantity=Integer.parseInt(subgroupList.getJSONObject(i).getString("length"));
                                    SubGroup subGroup=new SubGroup(name,des,insideType,ext,quantity);
                                    subGroups.add(subGroup);
                                }
                                groupListAdapter.notifyDataSetChanged();
                            }

                        }else {
                            JSONArray agentList=response.getJSONArray("data");
                            if(agentList.length()>0){
                                for (int i=0;i<agentList.length();i++){
                                    String email=agentList.getJSONObject(i).getString("email");
                                    if (email.equals(manager.getStringPreferences(getApplicationContext(),"EMAIL"))) {
                                        manager.setBoolPreferences(getApplicationContext(), hotlineExtension, true);
                                    }else {
                                        manager.setBoolPreferences(getApplicationContext(),hotlineExtension,false);
                                    }
                                    String name=agentList.getJSONObject(i).getString("fullname");
                                    String phone=agentList.getJSONObject(i).getString("phone");
                                    Boolean accept=agentList.getJSONObject(i).getBoolean("accepted");
                                    Agent agent= new Agent(name,email,phone,accept);
                                    agents.add(agent);
                                }
                                agentAdapter.notifyDataSetChanged();
                            }
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
    * Call API to add new subgroup
    * */
    private void addNewGroup(JSONObject postData, String hotline){
        final String URL=URLManager.getAddSubgroupAPI(hotline);//"http://teamthuduc.gcall.vn:3000/api/"+hotline+"/add/subgroup";
        Log.d("URL",URL);
        Log.d("POST",postData.toString());
        final JsonObjectRequest request= new JsonObjectRequest(URL, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.isNull("error")){
                        Toast.makeText(getApplicationContext(),"New Subgroup has been created",Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }else {
                        Toast.makeText(getApplicationContext(),response.getString("error"),Toast.LENGTH_SHORT).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
                Log.d("AddGroup",response.toString());
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
    * Call API to delete a subgroup
    * */
    private void deleteGroup(JSONObject params,String subgroup){
        final String URL=URLManager.getDeleteGroupAPI(subgroup);
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("DEL",response.toString());
                try {
                    if (response.isNull("error")){
                        Toast.makeText(getApplicationContext(),"Subgroup has been deleted",Toast.LENGTH_SHORT).show();
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
                        postInvitationRespond(hotlineExtension);
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
