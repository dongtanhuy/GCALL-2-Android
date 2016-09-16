package vn.gcall.gcall2;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;
import vn.gcall.gcall2.Helpers.SessionManager;
import vn.gcall.gcall2.Helpers.URLManager;
import vn.gcall.gcall2.VolleySingleton.VolleySingleton;


/**
 * A simple {@link Fragment} subclass.
 * Show the buttons of some function
 * Using listview to implement buttons
 */
public class MoreFragment extends Fragment {
    private Button btn_profile,btn_manage,btn_help,btn_signout,btn_unsolved;
    SessionManager manager;
    public MoreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_more, container, false);
        manager= new SessionManager();

        btn_profile=(Button) view.findViewById(R.id.btn_profile);
        btn_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),AgentProfileActivity.class);
                startActivity(intent);
            }
        });

        btn_manage=(Button)view.findViewById(R.id.btn_manage);
        if(manager.isFirtTimeLunch(getContext())){
            new MaterialTapTargetPrompt.Builder(getActivity())
                    .setTarget(btn_manage)
                    .setPrimaryText("Create First hotline")
                    .setSecondaryText("Click this button to create your first hotline")
                    .setBackgroundColour(Color.parseColor("#555555"))
                    .setSecondaryTextColour(Color.WHITE)
                    .setFocalColour(Color.parseColor("#555555"))
                    .setOnHidePromptListener(new MaterialTapTargetPrompt.OnHidePromptListener()
                    {
                        @Override
                        public void onHidePrompt(MotionEvent event, boolean tappedTarget)
                        {
                            //Do something such as storing a value so that this prompt is never shown again
                            manager.setFirtTimeLunch(getContext(),false);
                        }

                        @Override
                        public void onHidePromptComplete()
                        {

                        }
                    })
                    .show();
        }

        btn_manage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getContext(),ManageActivity.class);
                startActivity(intent);
            }
        });

        btn_unsolved=(Button)view.findViewById(R.id.btn_unsolved);
        btn_unsolved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),UnsolvedCallLogActivity.class);
                startActivity(intent);
            }
        });

        btn_help=(Button)view.findViewById(R.id.btn_help);
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getContext(),HelpActivity.class);
                startActivity(intent);
            }
        });

        btn_signout=(Button) view.findViewById(R.id.btn_signout);
        btn_signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceToken= FirebaseInstanceId.getInstance().getToken();
                HashMap<String,String> params= new HashMap<String, String>();
                params.put("sessionToken",TabViewActivity.token);
                params.put("deviceToken",deviceToken);
                signoutRequest(params);
                manager.deleteSession(getContext());
                if (!manager.getBoolPreferences(getContext(),"IS_LOGGED_IN")){
                    navigatetoSignInView();
                }
            }
        });
        return view ;
    }
    /*
    * Navigate to sign in activity after signing out
    * */
    private void navigatetoSignInView(){
        Intent intent= new Intent(getContext(),SignInActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    /*
    * Send request to sign out
    * */
    private void signoutRequest(HashMap<String,String> params){
        final String URL= URLManager.getSignOutAPI();
        final JsonObjectRequest request=new JsonObjectRequest(URL, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try{
                    if (response.getInt("success")==0){
                        Toast.makeText(getContext(),response.getString("error"),Toast.LENGTH_SHORT).show();
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
