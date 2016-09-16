package vn.gcall.gcall2.FirebaseService;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.twilio.client.Device;
import com.twilio.client.Twilio;

import org.json.JSONObject;

import java.util.Map;

import vn.gcall.gcall2.DeviceSingleton.DeviceSingleton;
import vn.gcall.gcall2.Helpers.SessionManager;
import vn.gcall.gcall2.IncomingCallActivity;
import vn.gcall.gcall2.R;
import vn.gcall.gcall2.TabViewActivity;


/**
 * Created by This PC on 16/06/2016.
 * Receive Notification from and show on screen
 * If notification contents token then listen incoming connection to receive call
 */
public class GCALLFirebaseMessagingService extends FirebaseMessagingService{
    private static final String TAG="GCALLMessagingService";
    private static String token="";
    SessionManager manager=new SessionManager();;
    DeviceSingleton singleton;
    Intent intent;
    private Device device;

    private static String authToken;
    private static String accountSID;
    private static String groupID;
    public static PendingIntent pendingIntent;
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data= remoteMessage.getData();
        JSONObject body=new JSONObject(data) ;
        Log.d(TAG,"Notification content "+body.toString());
        setToken(remoteMessage.getData().get(manager.getStringPreferences(this,"PHONE")));

        setAuthToken(remoteMessage.getData().get("authToken"));
        setAccountSID(remoteMessage.getData().get("accountSid"));
        setGroupID(remoteMessage.getData().get("groupId"));

        if(token==null){
            if (data.get("type").toString().equals("unsolved")){
                sendVoipNotifiction(data.get("message"),0);
            }else if (data.get("type").toString().equals("upgrade")){
                sendVoipNotifiction(data.get("alert"),3);
            }else if (data.get("type").toString().equals("expiration")){
                sendVoipNotifiction(data.get("alert"),3);
            }else {
                sendVoipNotifiction(data.get("alert"),2);
            }
        }else{
            singleton=new DeviceSingleton();
            singleton.setCapabilityToken(token);
            singleton.createDevice(token);
            device=singleton.clientDevice;
            intent= new Intent(this,IncomingCallActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);

            if(!Twilio.isInitialized()){
                Twilio.initialize(getApplicationContext(), new Twilio.InitListener() {
                    @Override
                    public void onInitialized() {
                        singleton=new DeviceSingleton();
                        singleton.setCapabilityToken(token);
                        singleton.createDevice(token);
                        singleton.clientDevice.setIncomingIntent(pendingIntent);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d("ERRORK",e.toString());
                        Toast.makeText(getApplicationContext().getApplicationContext(), "Failed to initialize the Twilio Client SDK", Toast.LENGTH_LONG).show();
                    }
                });
            }else {
                singleton.clientDevice.setIncomingIntent(pendingIntent);
            }
        }
    }

    private void setToken(String s){
        token=s;
    }

    public static String getAuthToken() {
        return authToken;
    }

    public static String getAccountSID() {
        return accountSID;
    }

    public static String getGroupID() {
        return groupID;
    }

    public static void setAccountSID(String accountSID) {
        GCALLFirebaseMessagingService.accountSID = accountSID;
    }

    public static void setGroupID(String groupID) {
        GCALLFirebaseMessagingService.groupID = groupID;
    }

    public static void setAuthToken(String authToken) {
        GCALLFirebaseMessagingService.authToken = authToken;
    }

    public static String getToken(){
        return token;
    }
    private void sendVoipNotifiction(String messageBody,int pageIndex){
        String title="";
        if(pageIndex==2){
            title="New group invited";
        }else if(pageIndex==0){
            title="GCall";
        }
        Intent intent= new Intent(this,TabViewActivity.class);
        intent.putExtra("PAGE_INDEX",pageIndex);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent= PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long [] pattern={0,1000};
        Bitmap bm= BitmapFactory.decodeResource(getResources(), R.drawable.icon_gcall);
        NotificationCompat.Builder notificationBuider= new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.icon_status_bar)
                .setSound(defaultSound)
                .setVibrate(pattern)
                .setLargeIcon(bm)
                .setContentIntent(pendingIntent)
                .setContentText(messageBody)
                .setContentTitle(title);
        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,notificationBuider.build());
    }

}
