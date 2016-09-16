package vn.gcall.gcall2.DataStruct;

import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;

/**
 * Created by This PC on 28/06/2016.
 * Singleton class manages get call log from API
 */
public class CallLogParams {
    private String callSID;
    private String accountSID;
    private String groupID;
    private String authToken;
    private static CallLogParams instance=null;

    public CallLogParams(){
        callSID=accountSID=groupID=authToken="";
    }

    public static CallLogParams getInstance(){
        if(instance==null){
            instance=new CallLogParams();
        }
        return instance;
    }

    public void setAccountSID(String accountSID) {
        this.accountSID = accountSID;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setCallSID(String callSID) {
        this.callSID = callSID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public void sendCallLogRequest(String sessionToken){
        final String URL= URLManager.getCreatCallLogAPI();
        JSONObject jsonObject=new JSONObject();
        try{
            jsonObject.put("sessionToken",sessionToken);
            jsonObject.put("callSid",callSID);
            jsonObject.put("accountSid",accountSID);
            jsonObject.put("authToken",authToken);
            jsonObject.put("groupId",groupID);
        }catch (JSONException e){
            e.printStackTrace();
        }
        Log.d("CALL PARAMS",jsonObject.toString());
        final JsonObjectRequest request=new JsonObjectRequest(URL, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Call Log",response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }
}

