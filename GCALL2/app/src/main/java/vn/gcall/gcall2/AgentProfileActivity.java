package vn.gcall.gcall2;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

import vn.gcall.gcall2.Helpers.SessionManager;
/*
* Show agent profile and a button to change password
* */
public class AgentProfileActivity extends Activity {
    private String agentName,agentEmail,agentPhone;
    private SessionManager manager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agent_profile);

        TextView avaName=(TextView) findViewById(R.id.agent_name);
        TextView profile_agent_name=(TextView)findViewById(R.id.profile_agent_name);
        TextView profile_agent_email=(TextView)findViewById(R.id.profile_agent_email);
        TextView profile_agent_phone=(TextView)findViewById(R.id.profile_agent_phone);
        ImageView imageView=(ImageView) findViewById(R.id.image_textDrawable);
        Button bnt_changePwd=(Button)findViewById(R.id.btn_change_pwd);
        manager=new SessionManager();
        agentEmail=manager.getStringPreferences(getApplicationContext(),"EMAIL");
        agentName=manager.getStringPreferences(getApplicationContext(),"FULLNAME");
        agentPhone=manager.getStringPreferences(getApplicationContext(),"PHONE");

        TextDrawable drawable= TextDrawable.builder().buildRound(agentName.substring(0,1), ContextCompat.getColor(getApplicationContext(),R.color.primary));
        imageView.setImageDrawable(drawable);
        avaName.setText(agentName);
        profile_agent_name.setText(agentName);
        profile_agent_email.setText(agentEmail);
        profile_agent_phone.setText(agentPhone);

        bnt_changePwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AgentProfileActivity.this,ChangePasswordActivity.class);
                startActivity(intent);
            }
        });
    }

}
