package vn.gcall.gcall2;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;


import dmax.dialog.SpotsDialog;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;

/*
* Show general information of hotline about: price,register date, expiration date,...
* */
public class HotlineInfo extends AppCompatActivity {
    private Button btn_upgrade;
    private TextView tv_pricing,tv_registeredDate,tv_expiredDate,tv_timeUsed,tv_noOfAgents;
    private Toolbar toolbar;
    private android.app.AlertDialog loadingDialog;
    private String hotline="";
    private String pricing="";
    private final static String ISO8601DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotline_info);

        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            hotline=extras.getString("HOTLINE");
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Hotline information");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        tv_pricing=(TextView) findViewById(R.id.tv_pricing);
        tv_registeredDate=(TextView)findViewById(R.id.tv_registeredDate);
        tv_expiredDate=(TextView)findViewById(R.id.tv_expiredDate);
        tv_timeUsed=(TextView)findViewById(R.id.tv_timeUsed);
        tv_noOfAgents=(TextView)findViewById(R.id.tv_noOfAgents);
        btn_upgrade=(Button) findViewById(R.id.btn_upgrade);

        loadingDialog=new SpotsDialog(this,R.style.Custom);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        getData();

        if (pricing.equals("premium")){
            btn_upgrade.setVisibility(View.GONE);
        }
        btn_upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i=new Intent(getApplicationContext(),UpgradeActivity.class);
                i.putExtra("HOTLINE",hotline);
                i.putExtra("PRICING",pricing);
                startActivity(i);
            }
        });
    }
    /*
    * Set up parameter to get info
    * */
    private void getData(){
        HashMap<String,String> params=new HashMap<>();
        params.put("sessionToken",TabViewActivity.token);
        params.put("hotline",hotline);
        JSONObject postData=new JSONObject(params);
        invokeWS(postData);
    }

    /*
    * Call API to get Info
    * */
    private void invokeWS(JSONObject params){
        final String URL= URLManager.getShowHotlineInfoAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("INFO",response.toString());
                loadingDialog.dismiss();
                try{
                    if (response.isNull("error")){
                        JSONObject data=response.getJSONObject("data");
                        pricing=data.getString("pricing");
                        tv_pricing.setText(pricing);
                        tv_registeredDate.setText(ISODateParser(data.getString("registerAt")));
                        if (pricing.equals("free")){
                            tv_expiredDate.setText("--");
                        }else {
                            tv_expiredDate.setText(ISODateParser(data.getString("expireAt")));
                        }
                        tv_timeUsed.setText(data.getString("timeUsed"));
                        tv_noOfAgents.setText(data.getString("numberOfAgents"));
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
                loadingDialog.dismiss();
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
    * Convert iso date to calendar date
    * */
    private String ISODateParser(String isoDate){
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault()) ;
        String result="";
        SimpleDateFormat dateformat = new SimpleDateFormat(ISO8601DATEFORMAT, Locale.getDefault());
        try {
            Date date = dateformat.parse(isoDate);
            calendar.setTime(date);
            String dd=String.valueOf(calendar.get(Calendar.DATE));
            String MM=String.valueOf(calendar.get(Calendar.MONTH));
            String YY=String.valueOf(calendar.get(Calendar.YEAR));
            Log.d("DATE", date.toString());
            result=dd+"/"+MM+"/"+YY;
        }catch (ParseException e){
            e.printStackTrace();
        }
        Log.d("DATE", result);
        return result;
    }
}
