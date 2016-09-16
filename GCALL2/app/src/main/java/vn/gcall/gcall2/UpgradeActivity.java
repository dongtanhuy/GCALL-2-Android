package vn.gcall.gcall2;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;
/*
* Show buttons that user can click to upgrade the hotline
* */
public class UpgradeActivity extends Activity {
    private Button btn_startup,btn_premium;
    private ImageButton btn_close;
    private String currentHotline="",currentPricing="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upgrade);

        btn_premium=(Button)findViewById(R.id.btn_premium);
        btn_startup=(Button)findViewById(R.id.btn_startup);
        btn_close=(ImageButton) findViewById(R.id.btn_close);

        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            currentHotline=extras.getString("HOTLINE");
            currentPricing=extras.getString("PRICING");
        }

        if (currentPricing.equals("startup")){
            btn_startup.setVisibility(View.GONE);
        }else if(currentPricing.equals("premium")) {
            btn_startup.setVisibility(View.GONE);
            btn_premium.setVisibility(View.GONE);
        }

        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        btn_premium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postData("premium");

            }
        });

        btn_startup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postData("startup");
            }
        });
    }

    /*
    * Set up params to send upgrade request
    * */
    private void postData(String pricing){
        HashMap<String,String>params=new HashMap<String, String>();
        params.put("sessionToken",TabViewActivity.token);
        params.put("hotline",currentHotline);
        params.put("pricing",pricing);
        JSONObject postData=new JSONObject(params);
        invokeWS(postData);
    }


    /*
    * Call API to send upgrade request
    * */
    private void invokeWS(JSONObject params){
        Log.d("PRICE",params.toString());
        final String URL= URLManager.getRequestUpgradeHotlineAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("RES",response.toString());
                try{
                    if(response.isNull("error")){
                        Toast.makeText(getApplicationContext(),"We will send an email to you to confirm",Toast.LENGTH_LONG).show();
                        onBackPressed();
                        finish();
                    }else {
                        Toast.makeText(getApplicationContext(),response.get("error").toString(),Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance().getRequestQueue().add(request);
    }
}
