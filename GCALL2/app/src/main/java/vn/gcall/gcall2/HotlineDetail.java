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
import com.getbase.floatingactionbutton.FloatingActionsMenu;
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
* Show detail of a hotline, a hotline maybe content a list of subgroup of list of agent (not both)
* User can add more agent or subgroup in hotline  (not both)
* */
public class HotlineDetail extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{
    FloatingActionsMenu fam;
    FloatingActionButton fab_add_agent,fab_add_group;
    //Agent data
    private ArrayList<Agent> agents;
    //Subgroup data
    private ArrayList<SubGroup> subGroups;
    private SessionManager manager;
    ListView agent_group_list;
    private AgentListAdapter agentAdapter=null;
    private SubGroupListAdapter groupListAdapter=null;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String GROUPS="subgroup";
    private static final String AGENTS="agent";
    Toolbar toolbar;
    private String hotline="";
    private String type="";
    private String role;
    private SwitchCompat self_adding_switch;
    private ArrayList<Integer>inUsedExt=null;
    private boolean isMasterAnAgent;

    private final String PRICE_FREE="free";
    private final String PRICE_STARTUP="startup";
    private final String PRICE_PREMIUM="premium";
    private String hotline_pricing="";

    private final int MAX_AGENT_FREE=2;
    private final int MAX_AGENT_STARTUP=5;
    private final int MAX_AGENT_PREMIUM=15;
    private int totalAgent=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotline_detail);
        Bundle extras= getIntent().getExtras();
        if (extras!=null){
            hotline=extras.getString("HOTLINE");
            type=extras.getString("TYPE");
            role=extras.getString("ROLE");
        }
        manager=new SessionManager();
        TextView textView=(TextView)findViewById(R.id.url_text);
        textView.setText("Use "+"https://call.gcall.vn/"+hotline+" to call");
        Log.d("FIRSTTYPE",type);
        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(hotline);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        inUsedExt=new ArrayList<>();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        agents=new ArrayList<>();
        subGroups=new ArrayList<>();
        agentAdapter= new AgentListAdapter(this,agents);
        groupListAdapter= new SubGroupListAdapter(this,subGroups);
        agent_group_list= (ListView) findViewById(R.id.list_agent_group);
        swipeRefreshLayout=(SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
//        swipeRefreshLayout.post(new Runnable() {
//            @Override
//            public void run() {
//                swipeRefreshLayout.setRefreshing(true);
//                getData();
//            }
//        });
        fam=(FloatingActionsMenu) findViewById(R.id.fab_add_popup);
        if (type.equals(GROUPS)){
            agent_group_list.setAdapter(groupListAdapter);
        }else if(type.equals(AGENTS)){
            agent_group_list.setAdapter(agentAdapter);
        }else {
            TextView message= (TextView)findViewById(R.id.message);
            message.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }

        agent_group_list.setLongClickable(true);
        /*
        * User long click on an item to delete a group or a hotline
        * */
        agent_group_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (type.equals(AGENTS)){//If type == agent, long click will delete an agent
                    final String agentEmail=((TextView)view.findViewById(R.id.agent_email)).getText().toString();
                    final AlertDialog.Builder deleteAgentDialogBuilder=new AlertDialog.Builder(HotlineDetail.this);
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
                                manager.setBoolPreferences(getApplicationContext(),hotline,false);
                            }
                            JSONObject postData=new JSONObject(params);
                            deleteAgent(postData,hotline);
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
                if (type.equals(GROUPS)){//If type == subgroup, long click will delete an group
                    final String subgroup=hotline+"-"+((TextView)view.findViewById(R.id.ext_text)).getText().toString();
                    final AlertDialog.Builder deleteGroupDialogBuilder=new AlertDialog.Builder(HotlineDetail.this);
                    deleteGroupDialogBuilder.setTitle("Delete Group");
                    deleteGroupDialogBuilder.setMessage("Are you sure to delete group "+subgroup+"?");
                    /*
                    * Click YES to delete a subgroup
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
        /*
        * If type==group, click on an item will show the detail of this group, navigate to the second layer subgroup detail
        * */
        agent_group_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (type.equals(GROUPS)){
                    int selectedExt=Integer.parseInt(((TextView)view.findViewById(R.id.ext_text)).getText().toString());
                    String subgroupContent= ((TextView)view.findViewById(R.id.hidenType)).getText().toString();
                    Intent intent = new Intent(getApplicationContext(),SecondLayerSubgroup.class);
                    intent.putExtra("EXTENSION",selectedExt);
                    intent.putExtra("CONTENT_TYPE",subgroupContent);
                    intent.putExtra("HOTLINE",hotline);
                    intent.putExtra("ROLE",role);
                    startActivity(intent);
                }
            }
        });


        fab_add_agent=(FloatingActionButton) findViewById(R.id.fab_addAgent);
        self_adding_switch=(SwitchCompat) findViewById(R.id.self_adding_switch);
        isMasterAnAgent=manager.getBoolPreferences(getApplicationContext(),hotline);
        self_adding_switch.setChecked(isMasterAnAgent);
        RelativeLayout layout_switch=(RelativeLayout) findViewById(R.id.layout_switch);
        if (type.equals(GROUPS)){
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
                    addNewAgentWithAccept(postData,hotline);
                    manager.setBoolPreferences(getApplicationContext(),hotline,true);
                }else {
                    deleteAgent(postData,hotline);
                    manager.setBoolPreferences(getApplicationContext(),hotline,false);
                }
            }
        });
        /*
        * Add other agent in this hotline
        * Open a dialog to enter the email of agent want to add
        * */
//        fab_add_agent.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(HotlineDetail.this);
//                LayoutInflater inflater= getLayoutInflater();
//                dialogBuider.setTitle("Add Agent");
//                dialogBuider.setMessage("Enter email address of agent:");
//                final View view= inflater.inflate(R.layout.add_agent_dialog,null);
//                dialogBuider.setView(view);
//                /*
//                * Click SAVE to send a request that add a new agent in hotline
//                * */
//                dialogBuider.setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        String agentEmail= ((EditText)view.findViewById(R.id.input_agent_email)).getText().toString();
//                        if (Validation.isNotNull(agentEmail)){
//                            if(Validation.validate(agentEmail)){
//                                final HashMap<String,String> params=new HashMap<>();
//                                params.put("email",agentEmail);
//                                params.put("sessionToken",TabViewActivity.token);
//                                JSONObject postData= new JSONObject(params);
//                                addNewAgent(postData,hotline);
//                            }else {
//                                Toast.makeText(getApplicationContext(),"Invalid Email format!",Toast.LENGTH_SHORT).show();
//                            }
//                        }else {
//                            Toast.makeText(getApplicationContext(),"Please enter email of agent!",Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//
//                dialogBuider.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//
//                dialogBuider.create().show();
//            }
//        });

        fab_add_group=(FloatingActionButton)findViewById(R.id.fab_addGroup);
        //Will visible when in full funciton account
//        fab_add_group.setVisibility(View.GONE);
        //-----------------------------------------//
        if(type.equals(AGENTS)){
            fab_add_group.setVisibility(View.GONE);
        }
        /*
        * Creata a new subgroup in this hotline
        * Open a dialog for user to enter the name,description and the extension of the new subgroup
        * */
//        fab_add_group.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(hotline_pricing.equals(PRICE_FREE)||hotline_pricing.equals(PRICE_STARTUP)){
//                    final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(HotlineDetail.this);
//                    dialogBuider.setTitle("Need more group?");
//                    dialogBuider.setMessage("You just only create group in PREMIUM hotline!\nDo you want to upgrade?");
//                    dialogBuider.setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(getApplicationContext(),"MOVE TO UPGRADE",Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    dialogBuider.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    });
//                    AlertDialog dialog= dialogBuider.create();
//                    dialog.show();
//                }
//                if (hotline_pricing.equals(PRICE_PREMIUM)){
//                    final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(HotlineDetail.this);
//                    LayoutInflater inflater= getLayoutInflater();
//                    dialogBuider.setTitle("Create new Group");
//                    dialogBuider.setMessage("Enter group name and decription:");
//                    final View view=inflater.inflate(R.layout.add_subgroup_dialog,null);
//                    dialogBuider.setView(view);
//                    ArrayList<Integer> ext_list=createArrayList();
//                    ext_list.removeAll(inUsedExt);
//                    final ArrayAdapter<Integer> listExtensionAdapter= new ArrayAdapter<>(getApplicationContext(),R.layout.spinner_item,ext_list);
//                    final MaterialBetterSpinner ExtSpinner= (MaterialBetterSpinner) view.findViewById(R.id.ext_spinner);
//                    ExtSpinner.setAdapter(listExtensionAdapter);
//                    /*
//                    * Click YES: validate the input and set up parameter to send request to add new subgroup
//                    * */
//                    dialogBuider.setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            String groupName= ((EditText)view.findViewById(R.id.input_GroupName)).getText().toString();
//                            String groupDes= ((EditText)view.findViewById(R.id.input_GroupDescription)).getText().toString();
//                            final String extNumber=ExtSpinner.getText().toString();
//                            if (Validation.isNotNull(groupName)&&Validation.isNotNull(groupDes)&&Validation.isNotNull(extNumber)){
//                                final HashMap<String,String> params=new HashMap<>();
//                                params.put("sessionToken",TabViewActivity.token);
//                                params.put("name",groupName);
//                                params.put("description",groupDes);
//                                params.put("extension",extNumber);
//                                JSONObject postData= new JSONObject(params);
//                                addNewGroup(postData,hotline);
//                            }else {
//                                Toast.makeText(getApplicationContext(),"Don't leave any fields blank",Toast.LENGTH_SHORT).show();
//                            }
//
//                        }
//                    });
//
//                    dialogBuider.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    });
//                    AlertDialog dialog= dialogBuider.create();
//                    if (inUsedExt.size()>=10){
//                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
//                    }
//                    dialog.show();
//                }
//            }
//        });
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
            }
        });
    }

    /*
    * Set up parameters to send request to get detail of hotline
    * */
    private void getData(){
        HashMap<String, String> params= new HashMap<>();
        params.put("sessionToken",TabViewActivity.token);
        params.put("role",role);
        invokeWS(params,hotline);
    }

    public static ArrayList<Integer> createArrayList(){
        ArrayList<Integer> result=new ArrayList<>();
        for (int i=0;i<=9;i++){
            result.add(i);
        }
        return result;
    }
    /*
    * Call API to get hotline detail
    * */
    private void invokeWS(HashMap<String, String>  params, final String hotline){
        swipeRefreshLayout.setRefreshing(true);
        final String URL= URLManager.getShowInsideHotlineAPI(hotline);//"http://teamthuduc.gcall.vn:3000/api/"+hotline;
        final JsonObjectRequest request= new JsonObjectRequest(URL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("FIRSTGROUP",response.toString());
                try{
                    if(response.isNull("error")){
                        hotline_pricing=response.getString("pricing");
                        if(response.getString("type").equals(GROUPS)){
                            JSONArray subgroupList=response.getJSONArray("data");
                            if (subgroupList.length()>0){
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
                                        manager.setBoolPreferences(getApplicationContext(), hotline, true);
                                    }else {
                                        manager.setBoolPreferences(getApplicationContext(),hotline,false);
                                    }
                                    String name=agentList.getJSONObject(i).getString("fullname");
                                    String phone=agentList.getJSONObject(i).getString("phone");
                                    Boolean accept=agentList.getJSONObject(i).getBoolean("accepted");
                                    Agent agent= new Agent(name,email,phone,accept);
                                    agents.add(agent);
                                }
                                agentAdapter.notifyDataSetChanged();
                            }
                            totalAgent=agentList.length();
                        }
                        swipeRefreshLayout.setRefreshing(false);

                        /*
                        * Handle click event on fab_add_group button
                        * */
                        fab_add_group.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fab_add_groupOnclick(hotline_pricing);
                            }
                        });

                        /*
                        * Handle click event on fab_add_agent button
                        * */
                        fab_add_agent.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                fab_add_agentOnclick(hotline_pricing,totalAgent);
                            }
                        });
                    }else {
                        Toast.makeText(getApplication(),response.getString("error"),Toast.LENGTH_SHORT).show();
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
    private void addNewGroup(JSONObject postData,final String hotline){
        final String URL=URLManager.getAddSubgroupAPI(hotline);//"http://teamthuduc.gcall.vn:3000/api/"+hotline+"/add/subgroup";
        Log.d("URL",URL);
        final JsonObjectRequest request= new JsonObjectRequest(URL, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.isNull("error")){
                        Toast.makeText(getApplicationContext(),"New Subgroup has been created",Toast.LENGTH_SHORT).show();
                        onBackPressed();
                    }else {
                        Toast.makeText(getApplication(),response.getString("error"),Toast.LENGTH_SHORT).show();
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
        final String URL=URLManager.getAddAgentAPI(hotline);//"http://teamthuduc.gcall.vn:3000/api/"+hotline+"/add/agent";
        final JsonObjectRequest request = new JsonObjectRequest(URL, postData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.isNull("error")){
                        postInvitationRespond(hotline);
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

    /*
    * Creata a new subgroup in this hotline
    * Open a dialog for user to enter the name,description and the extension of the new subgroup
    * If hotline is not premium, show dialog to ask client to upgrade
    * */
    private void fab_add_groupOnclick(final String hotline_pricing){
        if(hotline_pricing.equals(PRICE_FREE)||hotline_pricing.equals(PRICE_STARTUP)){
            final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(HotlineDetail.this);
            dialogBuider.setTitle("Need more groups?");
            dialogBuider.setMessage("You just only create group in PREMIUM hotline!\nDo you want to upgrade?");
            /*
            * Click UPGRADE to move to Upgrade Activity
            * */
            dialogBuider.setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i=new Intent(getApplicationContext(),UpgradeActivity.class);
                    i.putExtra("HOTLINE",hotline);
                    i.putExtra("PRICING",hotline_pricing);
                    startActivity(i);
                }
            });
            dialogBuider.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog dialog= dialogBuider.create();
            dialog.show();
        }
        if (hotline_pricing.equals(PRICE_PREMIUM)){
            final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(HotlineDetail.this);
            LayoutInflater inflater= getLayoutInflater();
            dialogBuider.setTitle("Create new Group");
            dialogBuider.setMessage("Enter group name and decription:");
            final View view=inflater.inflate(R.layout.add_subgroup_dialog,null);
            dialogBuider.setView(view);
            ArrayList<Integer> ext_list=createArrayList();
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
                        addNewGroup(postData,hotline);
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
    }

    /*
    * Add other agent in this hotline
    * Open a dialog to enter the email of agent want to add
    * If number of agent exceed accept
    * */
    private void fab_add_agentOnclick( final String hotline_pricing,int numOfAgents){
        if(numOfAgents>=MAX_AGENT_FREE&&hotline_pricing.equals(PRICE_FREE)){
            final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(HotlineDetail.this);
            dialogBuider.setTitle("Need more agent?");
            dialogBuider.setMessage("You have maximum 2 agents in this FREE hotline!\nDo you want to upgrade?");
            /*
            * Click UPGRADE to move to Upgrade Activity
            * */
            dialogBuider.setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i=new Intent(getApplicationContext(),UpgradeActivity.class);
                    i.putExtra("HOTLINE",hotline);
                    i.putExtra("PRICING",hotline_pricing);
                    startActivity(i);
                }
            });
            dialogBuider.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog dialog= dialogBuider.create();
            dialog.show();
        }else if (hotline_pricing.equals(PRICE_STARTUP)&&numOfAgents>=MAX_AGENT_STARTUP){
            final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(HotlineDetail.this);
            dialogBuider.setTitle("Need more agent?");
            dialogBuider.setMessage("You have maximum 5 agents in this START-UP hotline!\nDo you want to upgrade?");
            /*
            * Click UPGRADE to move to Upgrade Activity
            * */
            dialogBuider.setPositiveButton("Upgrade", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i=new Intent(getApplicationContext(),UpgradeActivity.class);
                    i.putExtra("HOTLINE",hotline);
                    i.putExtra("PRICING",hotline_pricing);
                    startActivity(i);
                }
            });
            dialogBuider.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog dialog= dialogBuider.create();
            dialog.show();
        }else {
            final AlertDialog.Builder dialogBuider= new AlertDialog.Builder(HotlineDetail.this);
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
                            addNewAgent(postData,hotline);
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
    }
}
