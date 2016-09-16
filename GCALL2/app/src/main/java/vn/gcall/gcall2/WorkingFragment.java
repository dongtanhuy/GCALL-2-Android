package vn.gcall.gcall2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.HashMap;

import vn.gcall.gcall2.CustomAdapters.AgentInGroupAdapter;
import vn.gcall.gcall2.DataStruct.SubGroup;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;


/**
 * A simple {@link Fragment} subclass.
 * Show list of group that current agent join
 */
public class WorkingFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    ListView list_hotline;
    ImageView empty_rectangle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private AgentInGroupAdapter listAdapter;
    private ArrayList<SubGroup> agentInGroupList;
    private static final String AGENT="agent";
    public WorkingFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_working, container, false);
        list_hotline= (ListView)view.findViewById(R.id.list_hotline);
        agentInGroupList=new ArrayList<>();
        swipeRefreshLayout=(SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh_layout);
        empty_rectangle=(ImageView)view.findViewById(R.id.empty_number);
        listAdapter = new AgentInGroupAdapter(getActivity(), agentInGroupList);
        list_hotline.setAdapter(listAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                getListHotLine();
            }
        });
        if (agentInGroupList.size()==0){
            empty_rectangle.setVisibility(View.VISIBLE);
            list_hotline.setVisibility(View.GONE);
        }

        list_hotline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String groupID=((TextView)view.findViewById(R.id.groupID)).getText().toString();
                Intent intent = new Intent(getContext(),SecondLayerSubgroupDetail.class);
                intent.putExtra("CONTENT_TYPE",AGENT);
                intent.putExtra("HOTLINE",groupID);
                intent.putExtra("ROLE",AGENT);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onRefresh() {
        agentInGroupList.clear();
        getListHotLine();
    }

    /*
    * Set up parameter to get list of groups
    * */
    private void getListHotLine(){
        String sessionToken=TabViewActivity.token;
        HashMap<String,String> params= new HashMap<>();
        params.put("sessionToken",sessionToken);
        invokeWS(params);
    }

    /*
    * Call API to get list of groups
    * */
    public void invokeWS(HashMap<String,String> postparams){
        swipeRefreshLayout.setRefreshing(true);
        final String URL= URLManager.getShowAgentHotlineAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, new JSONObject(postparams), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("AGENT",response.toString());
                try{
                    if(response.isNull("error")){
                        JSONArray groups=response.getJSONArray("data");
                        if(groups.length()>0){
                            for (int i=0;i<groups.length();i++){
                                String id= groups.getJSONObject(i).getString("groupId").toString();
                                String t=groups.getJSONObject(i).getString("has").toString();
                                String name=groups.getJSONObject(i).getString("name").toString();
                                int le=Integer.parseInt(groups.getJSONObject(i).getString("length").toString());
                                SubGroup subGroup=new SubGroup(id,name,t,le);
                                agentInGroupList.add(subGroup);
                                Log.d("hotline",agentInGroupList.get(i).getHotline());
                            }
                            listAdapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        empty_rectangle.setVisibility(View.GONE);
                        list_hotline.setVisibility(View.VISIBLE);
                    }else{
                        Toast.makeText(getContext(),response.getJSONObject("error").getString("error"),Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
