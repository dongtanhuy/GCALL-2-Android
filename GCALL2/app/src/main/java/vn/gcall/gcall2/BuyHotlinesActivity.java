package vn.gcall.gcall2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hbb20.CountryCodePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import vn.gcall.gcall2.CustomAdapters.CustomListBuyHotlineAdapter;
import vn.gcall.gcall2.DataStruct.HotlineToBuy;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;
/*
* Activity to show all hotline that User can buy (Old version)
* User selects a hotline the in the list then a confirm diaglog is shown
* User click YES to buy choosen hotline, NO to cancel
* */
public class BuyHotlinesActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private Toolbar toolbar;
    private ListView list_buy_hotline;
    private ArrayList<HotlineToBuy> hotlineArrayBuy= new ArrayList<>();
    private CountryCodePicker countryCodePicker;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CustomListBuyHotlineAdapter listhotlieAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_hotlines);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Buy Hotlines");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        countryCodePicker= (CountryCodePicker) findViewById(R.id.countryCodePicker);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh_layout);
        list_buy_hotline=(ListView) findViewById(R.id.list__buy_hotline);
        listhotlieAdapter= new CustomListBuyHotlineAdapter(this,hotlineArrayBuy);
        list_buy_hotline.setAdapter(listhotlieAdapter);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                getHotlineBuy();
            }
        });

        /*
        * Handle event when client click on an item to choose a hotline
        * */
        list_buy_hotline.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String selected= ((TextView)view.findViewById(R.id.hiden_number)).getText().toString();
                Toast.makeText(getApplicationContext(),selected,Toast.LENGTH_SHORT).show();
                final AlertDialog.Builder confirmDialog = new AlertDialog.Builder(BuyHotlinesActivity.this);
                confirmDialog.setTitle("Buy new hotline");
                confirmDialog.setMessage("Are you sure to buy this phone number?");
                /*
                * If they click YES, post request to server that: user want to buy this hotline
                * */
                confirmDialog.setPositiveButton("SURE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HashMap<String, String> params= new HashMap<>();
                        params.put("phone",selected);
                        params.put("sessionToken",TabViewActivity.token);
                        invokeBuyHotline(params);
                    }
                });

                confirmDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                confirmDialog.show();
            }
        });
    }
    /*
    * Refresh the list view
    * */
    @Override
    public void onRefresh() {
        hotlineArrayBuy.clear();
        getHotlineBuy();
    }

    /*
    * Set up the parameter before sending request to get the list of Hotlines-to-by
    * */
    private void getHotlineBuy(){
        String country=countryCodePicker.getSelectedCountryNameCode();
        invokeWS(country);
    }
    /*
    * Call the API to get list of hotlines-to-buy and handle the response
    * */
    private void invokeWS(String params){
        swipeRefreshLayout.setRefreshing(true);
        final String URL= URLManager.getGetListHotlineToBuy(params);
        Log.d("URL",URL);
        final JsonObjectRequest request = new JsonObjectRequest( URL,new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    Log.d("TEST",response.getString("data").toString());
                    if (response.isNull("error")){
                        JSONArray buyGroups= response.getJSONArray("data");
                        if(buyGroups.length()>0) {
                            for (int i = 0; i < buyGroups.length(); i++) {
                                HotlineToBuy hotlineToBuy = new HotlineToBuy(buyGroups.getJSONObject(i).getString("phoneNumber"), buyGroups.getJSONObject(i).getString("friendlyName"));
                                hotlineArrayBuy.add(hotlineToBuy);
                            }
                            listhotlieAdapter.notifyDataSetChanged();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }else{
                        Toast.makeText(getApplication(),response.getString("error"),Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
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

    /*
    * Call the API that user confirm to buy a selected hotline
    * */
    private void invokeBuyHotline(HashMap<String,String> postdata){
        final String URL =URLManager.getBuyHotlineAPI();
        final JsonObjectRequest request = new JsonObjectRequest(URL, new JSONObject(postdata), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.getInt("success")==1){
                        Toast.makeText(getApplicationContext(),"Buy hotline successfully",Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(getApplicationContext(),response.getJSONObject("error").getString("error").toString(),Toast.LENGTH_SHORT).show();
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
