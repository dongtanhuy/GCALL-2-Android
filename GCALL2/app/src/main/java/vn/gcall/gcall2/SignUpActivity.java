package vn.gcall.gcall2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import android.app.AlertDialog;
import dmax.dialog.SpotsDialog;
import vn.gcall.gcall2.Helpers.SessionManager;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.Helpers.Validation;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;
/*
* Agent enter information to create new account
* */
public class SignUpActivity extends AppCompatActivity {
    private static int MAX_LENGTH_PASSWORD=100;
    private static int MIN_LENGTH_PASSWORD=6;
    Button btn_signup;
    private android.app.AlertDialog loadingDialog;
    SessionManager manager;
    TextView textView_link_signin;
    EditText editText_fullname,editText_email, editText_phone, editText_password,editText_confirmPassword;
    CountryCodePicker countryCodePicker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_acitivity);
        btn_signup =(Button) findViewById(R.id.btn_signup);
        textView_link_signin=(TextView) findViewById(R.id.link_login);
        editText_fullname= (EditText) findViewById(R.id.input_fullname);
        editText_email=(EditText) findViewById(R.id.input_email);
        editText_phone=(EditText) findViewById(R.id.input_phone);
        editText_password=(EditText) findViewById(R.id.input_password);
        editText_confirmPassword=(EditText) findViewById(R.id.input_confirm_password);
        countryCodePicker=(CountryCodePicker)findViewById(R.id.countryCodePicker);
        manager=new SessionManager();
        if (manager.getBoolPreferences(getApplicationContext(),"IS_LOGGED_IN")){
            navigatetoTabView();
        }
//        manager.setFirtTimeLunch(getApplicationContext(),false);//false
        loadingDialog=new SpotsDialog(this,R.style.Custom);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();

            }
        });

        textView_link_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),SignInActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_up_acitivity, menu);
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
    * Set up parameter to send sign up request
    * */
    private void registerNewUser(){
        String email= editText_email.getText().toString();
        String fullname= editText_fullname.getText().toString();
        String phone=editText_phone.getText().toString();
        String password = editText_password.getText().toString();
        String confirmPassword=editText_confirmPassword.getText().toString();
        String countryCode=countryCodePicker.getSelectedCountryCode();
        String deviceToken= FirebaseInstanceId.getInstance().getToken();


        String submittedPhone=phoneNumberProcess(countryCode,phone);

        try{
            Log.d("HASH", Validation.hashMD5(password));
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        HashMap<String,String > params = new HashMap<String,String>();

        if (Validation.isNotNull(email)&&Validation.isNotNull(fullname)&&Validation.isNotNull(phone)&&Validation.isNotNull(password)&&Validation.isNotNull(confirmPassword)){
            if (Validation.validate(email)){
                if (Validation.checkLength(password,MAX_LENGTH_PASSWORD,MIN_LENGTH_PASSWORD)){
                    if(confirmPassword.equals(password)){
                        manager.setStringPreferences(getApplicationContext(),"EMAIL",email);
                        manager.setStringPreferences(getApplicationContext(),"PHONE",submittedPhone);
                        manager.setStringPreferences(getApplicationContext(),"FULLNAME",fullname);
                        //Form valid, put to server
                        params.put("email",email);
                        params.put("fullname",fullname);
                        try {
                            params.put("password",Validation.hashMD5(password));
                        }catch (NoSuchAlgorithmException e){
                            e.printStackTrace();
                        }catch (UnsupportedEncodingException e){
                            e.printStackTrace();
                        }
                        params.put("deviceType","android");
                        params.put("deviceToken",deviceToken);
                        params.put("phone",submittedPhone);
                        invokeWS(params);
                        loadingDialog.show();
                    }else {
                        Toast.makeText(getApplicationContext(),"These passwords don't match. Try again!",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Password must be at least 6 characters",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getApplicationContext(),"Invalid Email",Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getApplicationContext(),"Missing required field(s)",Toast.LENGTH_SHORT).show();
        }
    }
    /*
    * Perform RESTFul Web Service Innvocations
    * @param params: HashMap<String,String>
    * */
    public void invokeWS(HashMap<String,String> postdata){
        final JSONObject jsonObject= new JSONObject(postdata);
        Log.d("data", jsonObject.toString());
        final String URL = URLManager.getSignUpAPI();// "http://teamthuduc.gcall.vn:3000/api/signup";
        JsonObjectRequest req=new JsonObjectRequest(URL, new JSONObject(postdata), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", response.toString());
                try {
                    if(response.getInt("success")==1){
                        boolean islogin=true;
                        JSONObject data= response.getJSONObject("data");
                        String token=data.getString("sessionToken");
                        String email=data.getString("email");
                        String fullname=data.getString("fullname");
                        String phone=data.getString("phone");
                        manager.setStringPreferences(getApplicationContext(),"TOKEN",token);
                        manager.setStringPreferences(getApplicationContext(),"EMAIL",email);
                        manager.setStringPreferences(getApplicationContext(),"PHONE",phone);
                        manager.setStringPreferences(getApplicationContext(),"FULLNAME",fullname);
                        manager.setBoolPreferences(getApplicationContext(),"IS_LOGGED_IN",islogin);
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(),"New Account has been created",Toast.LENGTH_SHORT).show();
                        //Switch to log in screen
//                        navigateToSignInActivity(editText_email.getText().toString(),editText_password.getText().toString());
                        navigatetoTabView();
                    }else {
                        loadingDialog.dismiss();
                        Toast.makeText(getApplicationContext(),response.getString("error"),Toast.LENGTH_SHORT).show();
                    }
                    VolleyLog.v("Response:%n %s", response.toString(4));
                }catch(JSONException e){
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
    *   Navigate from SignUpActivity to SignIn Activity
    * */
    public void navigateToSignInActivity(String email, String password){
        Intent loginIntent = new Intent(getApplicationContext(),SignInActivity.class);
        loginIntent.putExtra("EMAIL",email);
        loginIntent.putExtra("PASSWORD",password);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }

    /*
    * Convert phone number into format: 00+countrycode+phonenum
    * */
    private String phoneNumberProcess(String prefix,String suffix){
        String result="00"+prefix;
        if (suffix.startsWith("0")){
            suffix=suffix.substring(1);
            Log.d("suffix",suffix);
        }
        result+=suffix;
        Log.d("phone after",result);
        return result;
    }


    /*
    * Navigate to tab view after sign in
    * */
    public void navigatetoTabView(){
        Intent tabview= new Intent(getApplicationContext(),TabViewActivity.class);
        if (manager.isFirtTimeLunch(getApplicationContext())){
            tabview.putExtra("PAGE_INDEX",3);
        }
        startActivity(tabview);
        finish();
    }
}
