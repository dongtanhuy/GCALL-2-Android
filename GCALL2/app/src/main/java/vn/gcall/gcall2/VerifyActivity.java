package vn.gcall.gcall2;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.Helpers.Validation;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;
/*
* Agent enter verification code and send to server
* */
public class VerifyActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private String phone,code;
    private EditText input_code;
    private AppCompatButton btn_send;
    private TextView textViewMessage;
    private TextInputLayout code_wrapper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        Bundle extras=getIntent().getExtras();
        if (extras!=null){
            phone=extras.getString("PHONE_NUM");
        }

        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Verification");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        textViewMessage=(TextView) findViewById(R.id.message);
        input_code=(EditText)findViewById(R.id.verification_code);
        btn_send=(AppCompatButton) findViewById(R.id.btn_sendCode);
        code_wrapper=(TextInputLayout) findViewById(R.id.code_wrapper);
        code_wrapper.setErrorEnabled(true);

        textViewMessage.setText("We sent code to "+phone+". Please check your phone and input code below.");
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode();
            }
        });
    }
    /*
    * Set up parameter and validate input
    * */
    private void sendVerificationCode(){
        code=input_code.getText().toString();
        if (Validation.isNotNull(code)){
            HashMap<String, String> params=new HashMap<>();
            params.put("sessionToken",TabViewActivity.token);
            params.put("phone",phone);
            params.put("code",code);
            JSONObject postData=new JSONObject(params);
            invokeAPI(postData);
        }else {
            code_wrapper.setError("Please enter the code you have received");
            input_code.requestFocus();
        }
    }

    /*
    * Call API to send verification code
    * */
    private void invokeAPI(JSONObject params){
        final String URL= URLManager.getVerifiyHotlineAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("VERIFY",response.toString());
                try{
                    if (response.getInt("success")==1){
                        Toast.makeText(getApplicationContext(),"Success",Toast.LENGTH_LONG).show();
                        onBackPressed();
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),response.getString("error"),Toast.LENGTH_LONG).show();
                        input_code.requestFocus();
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
