package vn.gcall.gcall2;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.twilio.client.Connection;
import com.twilio.client.ConnectionListener;
import com.twilio.client.Device;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;

import java.util.Map;

import vn.gcall.gcall2.DataStruct.CallLogParams;
import vn.gcall.gcall2.FirebaseService.GCALLFirebaseMessagingService;


/**
 * Created by This PC on 08/06/2016.
 * Receive incomming connection and make the call
 * More detail at: https://www.twilio.com/docs/quickstart/php/android-client/incoming-connections
 */
public class IncomingCallActivity extends AppCompatActivity implements DeviceListener,ConnectionListener{
    private Connection connection;
    private Connection pendingConnection;
    private Device device;
    private Map<String,String> connectionParameter;
    private Chronometer chronometer;
    private boolean muteMircophone;
    private boolean speakerPhone;
    private TextView incomingNumber;
    private RingtoneManager ringtoneManager;
    private Uri soundURI;
    private Ringtone ringtone;
    private static final int MIC_PERMISSION_REQUEST_CODE = 1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incomming_call);
        ringtoneManager = new RingtoneManager(getApplicationContext());
        soundURI= Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.country);
        ringtone=ringtoneManager.getRingtone(getApplicationContext(),soundURI);
        chronometer=(Chronometer) findViewById(R.id.time_counter);
        incomingNumber=(TextView)findViewById(R.id.incomming_number);
        if (!checkPermissionForMicrophone()) {
            requestPermissionForMicrophone();
        }

        final Button btn_silent=(Button)findViewById(R.id.btn_silent);
        btn_silent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelRingtoneAndVibrate();
            }
        });

        final Button btn_mute=(Button) findViewById(R.id.btn_mute);
        /*
        * Enable/Disable microphone
        * */
        btn_mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muteMircophone = !muteMircophone;
                TextView mute_text=(TextView)findViewById(R.id.mute_text);
                if (connection != null) {
                    connection.setMuted(muteMircophone);
                }
                if (muteMircophone) {
                    btn_mute.setBackground(ContextCompat.getDrawable(IncomingCallActivity.this,R.drawable.icon_mute_press));
                    mute_text.setText("unmute");
                } else {
                    btn_mute.setBackground(ContextCompat.getDrawable(IncomingCallActivity.this,R.drawable.icon_mute));
                    mute_text.setText("mute");
                }
            }
        });

        final Button btn_speaker= (Button) findViewById(R.id.btn_speaker);
        /*
        * Enable/Disable speaker
        * */
        btn_speaker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakerPhone = !speakerPhone;
                TextView speaker_text=(TextView)findViewById(R.id.speaker_text);
                AudioManager audioManager = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setSpeakerphoneOn(speakerPhone);

                if (speakerPhone) {
                    btn_speaker.setBackground(ContextCompat.getDrawable(IncomingCallActivity.this,R.drawable.icon_speaker_press));
                    speaker_text.setText("micro");
                } else {
                    btn_speaker.setBackground(ContextCompat.getDrawable(IncomingCallActivity.this,R.drawable.icon_speaker));
                    speaker_text.setText("speaker");
                }
            }
        });
        final Button btn_accept= (Button) findViewById(R.id.btn_pickup);
        /*
        * Accept to answer call
        * */
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answer();
                cancelRingtoneAndVibrate();
                setCallUI();
            }
        });

        Button btn_reject=(Button) findViewById(R.id.btn_reject);
        /*
        * Reject the call
        * */
        btn_reject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pendingConnection != null) {
                    pendingConnection.reject();
                    cancelRingtoneAndVibrate();
                }
                disconnect();
                startActivity(new Intent(getApplicationContext(),TabViewActivity.class));
                finish();
                onBackPressed();
            }
        });
        Button btn_hangup= (Button) findViewById(R.id.btn_hangup);
        /*
        * End the call after conversation
        * */
        btn_hangup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disconnect();
                startActivity(new Intent(getApplicationContext(),TabViewActivity.class));
                resetCallUI();
                finish();
                onBackPressed();
            }
        });
        Intent i = getIntent();
        device = i.getParcelableExtra(Device.EXTRA_DEVICE);
        connection = i.getParcelableExtra(Device.EXTRA_CONNECTION);
        connectionParameter=connection.getParameters();
        incomingNumber.setText(connectionParameter.get(Connection.IncomingParameterFromKey));
        CallLogParams params=new CallLogParams();
        params.setAccountSID(GCALLFirebaseMessagingService.getAccountSID());
        params.setAuthToken(GCALLFirebaseMessagingService.getAuthToken());
        params.setGroupID(GCALLFirebaseMessagingService.getGroupID());
        params.setCallSID(connectionParameter.get(Connection.IncomingParameterCallSIDKey));
        params.sendCallLogRequest(TabViewActivity.token);
        Log.d("Number",connectionParameter.get(Connection.IncomingParameterFromKey));
        Log.d("Params", connectionParameter.toString());
        sendCallingNotification(connectionParameter.get(Connection.IncomingParameterFromKey));
        if (i == null && device == null) {
            finish();
        }
        i.removeExtra(Device.EXTRA_DEVICE);
        i.removeExtra(Device.EXTRA_CONNECTION);

        pendingConnection = connection;

        pendingConnection.setConnectionListener(new ConnectionListener() {

            @Override
            public void onConnecting(Connection connection) {
                Log.d("CONNECTING","Connecting");
            }

            @Override
            public void onConnected(Connection connection) {
                Log.d("CONNECTED","Connected");
            }

            @Override
            public void onDisconnected(Connection connection) {
                disconnect();
                resetCallUI();
                cancelRingtoneAndVibrate();
                startActivity(new Intent(getApplicationContext(),TabViewActivity.class));
                finish();
                Log.d("DISCONNECT","Disconected");
            }

            @Override
            public void onDisconnected(Connection connection, int i, String s) {
                Log.d("DISCONNECT_ERROR","Disconected");
            }
        });

        device.setDeviceListener(new DeviceListener() {
            @Override
            public void onStartListening(Device device) {
                Log.d("CONN", "BEFORE 1");
            }

            @Override
            public void onStopListening(Device device) {

            }

            @Override
            public void onStopListening(Device device, int i, String s) {

            }

            @Override
            public boolean receivePresenceEvents(Device device) {
                showRingtoneAndVibrate();
                return false;
            }

            @Override
            public void onPresenceChanged(Device device, PresenceEvent presenceEvent) {
                Log.d("CONN", "BEFORE 3");
            }
        });
    }

    /*
    * Set UI when get incomming call
    * */
    private void setCallUI(){
        findViewById(R.id.voice_function).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_hangup).setVisibility(View.VISIBLE);
        findViewById(R.id.layout_accept).setVisibility(View.GONE);
        findViewById(R.id.layout_silent).setVisibility(View.GONE);
        findViewById(R.id.layout_reject).setVisibility(View.GONE);
        findViewById(R.id.incomming_text).setVisibility(View.GONE);
        chronometer.setVisibility(View.VISIBLE);
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    /*
    * Reset UI After conversation
    * */
    private void resetCallUI(){
        muteMircophone = false;
        speakerPhone = false;
        AudioManager audioManager = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(speakerPhone);
        chronometer.stop();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();

        Intent intent= getIntent();

        if(intent!=null){
            Device device= intent.getParcelableExtra(Device.EXTRA_DEVICE);
            Connection incomingConnection= intent.getParcelableExtra(Device.EXTRA_CONNECTION);

            if(incomingConnection==null&&device==null){
                return;
            }
            intent.removeExtra(Device.EXTRA_DEVICE);
            intent.removeExtra(Device.EXTRA_CONNECTION);

            pendingConnection = incomingConnection;

            Log.d("ALO","Alo");
        }
    }

    /*
    * Disconnect after rejecting or hanging up
    * */
    private void disconnect() {
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    /*
    * Accept the connection and create conversation
    * */
    public void answer() {
        pendingConnection.accept();
        Activity parent = getParent();
        if (parent instanceof ConnectionListener){
            pendingConnection.setConnectionListener( (ConnectionListener) parent);
        } else {
            Log.d("TAG", "FAILED");
        }
        connection = pendingConnection;
        pendingConnection = null;
    }


    /*
    * Make voice and ring tone when get incoming call
    * */
    private void showRingtoneAndVibrate(){
        long[] v = {0,1000,0};
        Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(v,0);
        ringtone.play();
    }


    /*
    * Disable ring tone and vibrate when reject, anwser,hangup or mute
    * */
    private void cancelRingtoneAndVibrate(){
        if (Context.VIBRATOR_SERVICE!=null){
            String vs=Context.VIBRATOR_SERVICE;
            Vibrator vibrator=(Vibrator)getApplicationContext().getSystemService(vs);
            vibrator.cancel();
            ringtone.stop();
        }
    }


    /*
    * Create calling notification
    * */
    private void sendCallingNotification(String incomingNumber){
        Intent intent= new Intent(this,TabViewActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bitmap bm= BitmapFactory.decodeResource(getResources(),R.drawable.icon_gcall);
        NotificationCompat.Builder notificationBuider= new NotificationCompat.Builder(this)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.icon_status_bar)
                .setAutoCancel(true)
                .setLargeIcon(bm)
                .setContentIntent(GCALLFirebaseMessagingService.pendingIntent)
                .setContentText("You have a call from "+incomingNumber)
                .setContentTitle("GCall");
        NotificationManager notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0,notificationBuider.build());
    }

    private boolean checkPermissionForMicrophone() {
        int resultMic = ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.RECORD_AUDIO);
        if (resultMic == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    private void requestPermissionForMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(getApplicationContext().getApplicationContext(),
                    "Microphone permissions needed. Please allow in App Settings for additional functionality.",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onConnecting(Connection connection) {
        Log.d("CONNECT","Connecting");
    }

    @Override
    public void onConnected(Connection connection) {
        Log.d("CONNECT","Connected");
    }

    @Override
    public void onDisconnected(Connection inconnection) {
        disconnect();
        resetCallUI();
        cancelRingtoneAndVibrate();
        onBackPressed();
        finish();
        Log.d("INCOMING","On disconnect");
    }

    @Override
    public void onDisconnected(Connection connection, int i, String s) {
        disconnect();
        resetCallUI();
        cancelRingtoneAndVibrate();
        onBackPressed();
        finish();
        Log.d("INCOMING","On disconnect error");
    }

    @Override
    public void onStartListening(Device device) {
        Log.d("TAGK", "Device has started listening for incoming connections");
    }

    @Override
    public void onStopListening(Device device) {
        Log.d("TAGK", "Device has stop listening for incoming connections");
//        device.disconnectAll();
        onBackPressed();
    }

    @Override
    public void onStopListening(Device device, int i, String s) {
        Log.d("TAGK", "Device has stop listening for incoming connections");
    }

    @Override
    public boolean receivePresenceEvents(Device device) {
        Log.d("CONN", "BEFORE 2");
        return false;
    }

    @Override
    public void onPresenceChanged(Device device, PresenceEvent presenceEvent) {

    }
}
