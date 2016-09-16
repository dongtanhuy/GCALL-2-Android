package vn.gcall.gcall2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import vn.gcall.gcall2.Helpers.SessionManager;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.Helpers.Validation;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;
/*
* Sign in function
* Agent enters email and password to sign in
* */

public class SignInActivity extends AppCompatActivity {

    Button btn_login;
    EditText editText_email, editText_password;
    TextView textView_signupLink;
    private android.app.AlertDialog loadingDialog;
    SessionManager manager;
    private String token;
    private String email;
    private String fullname;
    private String phone;
    private boolean islogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        manager= new SessionManager();
        if (manager.getBoolPreferences(getApplicationContext(),"IS_LOGGED_IN")){
            navigatetoTabView();
        }
//        manager.setFirtTimeLunch(getApplicationContext(),false);//false
        loadingDialog=new SpotsDialog(this,R.style.Custom);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        btn_login=(Button) findViewById(R.id.btn_login);
        textView_signupLink=(TextView)findViewById(R.id.link_signup);

        editText_email=(EditText)findViewById(R.id.input_email);
        editText_password=(EditText)findViewById(R.id.input_password);
        Intent i= getIntent();
        Bundle extra= i.getExtras();
        if (extra!=null){
            editText_email.setText(extra.getString("EMAIL"));
            editText_password.setText(extra.getString("PASSWORD"));
        }
        //Log in button handler
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        //Sign up link handler
        textView_signupLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    /*
    *   Login user account, triggered by click on login button
    * */
    private void loginUser(){
        String email =editText_email.getText().toString();
        String password=editText_password.getText().toString()  ;
        try{
            Log.d("HASH", Validation.hashMD5(password));
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        HashMap<String, String> params = new HashMap<String, String>();
        if (Validation.isNotNull(email)&&Validation.isNotNull(password)){
            //EMail check
            if(Validation.validate(email)){
                //Form valid, put to server
                //Put email
                params.put("email",email);
                //Put password
                try {
                    params.put("password",Validation.hashMD5(password));
                }catch (NoSuchAlgorithmException e){
                    e.printStackTrace();
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                invokeWS(params);
                loadingDialog.show();
            }else{
                Toast.makeText(getApplicationContext(),"Invalid Email",Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"Missing required field(s)",Toast.LENGTH_SHORT).show();
        }

    }

    /*
    * Perform RESTFul Web Service Innvocations
    * @param params: HashMap<String,String>
    * */
    public void invokeWS(HashMap<String, String> postparams){
        //New token from firebase
        String deviceToken= FirebaseInstanceId.getInstance().getToken();
        String voipToken=FirebaseInstanceId.getInstance().getToken();

        ///
        postparams.put("deviceType","android");
        postparams.put("deviceToken",deviceToken);
        postparams.put("voipToken",voipToken);
        final JSONObject jsonObject = new JSONObject(postparams);
        Log.d("data", jsonObject.toString());
        final String URL = URLManager.getSignInAPI();
        JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(postparams), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d("Response", response.toString());
                    if(response.getInt("success")==1){
                        //Add session detail
                        JSONObject data= response.getJSONObject("data");
                        token=data.getString("sessionToken");
                        email=data.getString("email");
                        fullname=data.getString("fullname");
                        phone=data.getString("phone");
                        islogin=true;
                        manager.setStringPreferences(getApplicationContext(),"TOKEN",token);
                        manager.setStringPreferences(getApplicationContext(),"EMAIL",email);
                        manager.setStringPreferences(getApplicationContext(),"PHONE",phone);
                        manager.setStringPreferences(getApplicationContext(),"FULLNAME",fullname);
                        manager.setBoolPreferences(getApplicationContext(),"IS_LOGGED_IN",islogin);
                        loadingDialog.dismiss();
                        navigatetoTabView();
                    }else {
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(),response.getString("error"),Toast.LENGTH_SHORT).show();
                    }
                    VolleyLog.v("Response:%n %s", response.toString(4));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance().getRequestQueue().add(req);
    }


    /*
    * Navigate to tabview after login
    * */
    public void navigatetoTabView(){
        Intent tabview= new Intent(getApplicationContext(),TabViewActivity.class);
        if (manager.isFirtTimeLunch(getApplicationContext())){
            tabview.putExtra("PAGE_INDEX",3);
        }
        startActivity(tabview);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
