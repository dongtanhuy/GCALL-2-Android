package vn.gcall.gcall2;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.Helpers.Validation;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;
/*
* Change password of agent
* Agent have to enter the current password, the new password and re-type the new password
* and post to server to change password
* */
public class ChangePasswordActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private static int MAX_LENGTH_PASSWORD=100;
    private static int MIN_LENGTH_PASSWORD=6;
    private EditText text_oldPass,text_newPass,text_confirmPass;
    private TextInputLayout inputLayoutNewPwd,inputLayoutConfirm;
    private android.app.AlertDialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Change password");
        loadingDialog=new SpotsDialog(this,R.style.Custom);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        inputLayoutNewPwd=(TextInputLayout) findViewById(R.id.input_layout_newPass);
        inputLayoutConfirm=(TextInputLayout) findViewById(R.id.input_layout_confirm);

        text_oldPass=(EditText)findViewById(R.id.oldPassword);
        text_newPass=(EditText)findViewById(R.id.newPassword);
        text_confirmPass=(EditText)findViewById(R.id.confirm_Password);


        Button btn_changePwd=(Button) findViewById(R.id.btn_change_pwd);
        btn_changePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
    }

    /*
    * Validate the input end set up the parameter to send request
    * */
    private void changePassword(){
        String oldPass=text_oldPass.getText().toString();
        String newPass=text_newPass.getText().toString();
        String confirm=text_confirmPass.getText().toString();
        if (Validation.isNotNull(oldPass)&&Validation.isNotNull(newPass)&&Validation.isNotNull(confirm)){
            if (Validation.checkLength(newPass,MAX_LENGTH_PASSWORD,MIN_LENGTH_PASSWORD)){
                if (confirm.equals(newPass)){
                    HashMap<String,String> params=new HashMap<String, String>();
                    params.put("sessionToken",TabViewActivity.token);
                    try {
                        params.put("oldpass",Validation.hashMD5(oldPass));
                    }catch (NoSuchAlgorithmException e){
                        e.printStackTrace();
                    }catch (UnsupportedEncodingException e){
                        e.printStackTrace();
                    }
                    try {
                        params.put("newpass",Validation.hashMD5(newPass));
                    }catch (NoSuchAlgorithmException e){
                        e.printStackTrace();
                    }catch (UnsupportedEncodingException e){
                        e.printStackTrace();
                    }
                    JSONObject postData=new JSONObject(params);
                    invokeWS(postData);
                    loadingDialog.show();
                }else {
                    inputLayoutConfirm.setError("These passwords don't match. Try again!");
                    text_confirmPass.requestFocus();
                }
            }else {
                inputLayoutNewPwd.setError("Password must be at least 6 characters");
                text_newPass.requestFocus();
            }
        }else{
            Toast.makeText(getApplicationContext(),"Missing required field(s)",Toast.LENGTH_SHORT).show();
        }
    }
        });
    }

    /*
    * Call API to change the password
    * */
    private void invokeWS(JSONObject params){
        final String URL= URLManager.getChangePasswordAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if(response.isNull("error")){
                        Toast.makeText(getApplicationContext(),"Password Updated",Toast.LENGTH_LONG).show();
                        loadingDialog.dismiss();
                        onBackPressed();
                    }else{
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
}
