package vn.gcall.gcall2;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.ToolTip;
import tourguide.tourguide.TourGuide;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.Helpers.Validation;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;
/*
* Activity show feature create hotline by client's cellphone number
* Client enters their cellphone number then it will be post to server and server will send back
* a SMS message content verification code to this phone number
* */
public class AddCellphoneNumberActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private CountryCodePicker countryPicker;
    private AppCompatButton btn_done;
    private android.app.AlertDialog loadingDialog;
    private TextInputLayout phoneWrapper;
    private EditText input_phone;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_cellphone_number);

        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Creat hotline");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        countryPicker = (CountryCodePicker) findViewById(R.id.countryCodePicker);
        btn_done=(AppCompatButton)findViewById(R.id.btn_done);
        phoneWrapper=(TextInputLayout)findViewById(R.id.phone_wrapper);
        phoneWrapper.setErrorEnabled(true);
        input_phone=(EditText)findViewById(R.id.input_add_hotline);

        loadingDialog=new SpotsDialog(this,R.style.Custom);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);

        btn_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createHotline();
            }
        });
    }

    /*
    * Validate input and create parameter to send to API
    * */
    private void createHotline(){
        String input=input_phone.getText().toString();
        String countryCode=countryPicker.getSelectedCountryCodeWithPlus();
        Log.d("CountryCode",countryCode);
        Log.d("Phone",input);
        if (Validation.isNotNull(input)){
            if (Validation.checkPhone(input)){
                phone=phoneNumberProcess(countryCode,input);
                HashMap<String, String> params=new HashMap<>();
                params.put("sessionToken",TabViewActivity.token);
                params.put("phone",phone);
                JSONObject postData=new JSONObject(params);
                invokeAPI(postData);
                loadingDialog.show();
            }else{
                phoneWrapper.setError("Invalid phone number format");
                input_phone.requestFocus();
            }
        }else{
            phoneWrapper.setError("Don't leave this field blank");
            input_phone.requestFocus();
        }

    }
    /*
    * Change phone number into format 00+countrycode+phonenum
    * */
    private String phoneNumberProcess(String prefix,String suffix){
        String result=prefix;
        if (suffix.startsWith("0")){
            suffix=suffix.substring(1);
            Log.d("suffix",suffix);
        }
        result+=suffix;
        Log.d("phone after",result);
        return result;
    }

    /*
    *   Call API and send request then receive response
    * */
    private void invokeAPI(JSONObject params){
        final String URL= URLManager.getCreateHotlineAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.getInt("success")==1){
                        loadingDialog.dismiss();
                        navigateToVerifyActivity();
                    }else {
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(),response.getString("error"),Toast.LENGTH_LONG).show();
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
    * Navigate to Verify activity after get response
    * */
    private void navigateToVerifyActivity(){
        Intent intent=new Intent(AddCellphoneNumberActivity.this,VerifyActivity.class);
        intent.putExtra("PHONE_NUM",phone);
        startActivity(intent);
        finish();
    }
}
