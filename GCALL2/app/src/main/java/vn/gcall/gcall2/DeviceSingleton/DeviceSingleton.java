package vn.gcall.gcall2.DeviceSingleton;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.twilio.client.Connection;
import com.twilio.client.ConnectionListener;
import com.twilio.client.Device;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;
import com.twilio.client.Twilio;

import vn.gcall.gcall2.IncomingCallActivity;

/**
 * Created by This PC on 21/06/2016.
 * Singleton class, create Device Object to listen incomming connection and receive call (Twillio)
 * More detail about Deviec Object at: https://www.twilio.com/docs/api/client/android/device
 */
public class DeviceSingleton implements DeviceListener,ConnectionListener {
    private static final String TAG="DEVICE_SINGLETON";

    private static DeviceSingleton instance=null;
    private String capabilityToken;
    public Device clientDevice;
    public DeviceSingleton() {
        this.capabilityToken = "";
    }

    public static DeviceSingleton getInstance(){
        if(instance==null){
            instance=new DeviceSingleton();
        }
        return instance;
    }


    public String getCapabilityToken() {
        return capabilityToken;
    }

    public void setCapabilityToken(String capabilityToken) {
        this.capabilityToken = capabilityToken;
        createDevice(capabilityToken);
    }

    @Override
    public void onConnected(Connection connection) {
        Log.d(TAG,"onConnected");
    }

    @Override
    public void onConnecting(Connection connection) {
        Log.d(TAG,"onConnecting");
    }

    @Override
    public void onDisconnected(Connection connection) {
        Log.d(TAG,"onDisConnected");
    }

    @Override
    public void onDisconnected(Connection connection, int i, String s) {
        Log.d(TAG,"onDisConnected");
    }

    @Override
    public void onPresenceChanged(Device device, PresenceEvent presenceEvent) {
        Log.d(TAG,"onPresenceChanged");
    }

    @Override
    public void onStartListening(Device device) {
        Log.d(TAG,"Device is listening connection");
    }

    @Override
    public void onStopListening(Device device) {
        Log.d(TAG,"onStopListening");
    }

    @Override
    public void onStopListening(Device device, int i, String s) {
        Log.d(TAG,"onStopListening");
    }

    @Override
    public boolean receivePresenceEvents(Device device) {
        Log.d(TAG,"receivePresenceEvents");
        return false;
    }

    public void createDevice(String capabilityToken){
        try{
            if(clientDevice==null){
                clientDevice = Twilio.createDevice(capabilityToken,DeviceSingleton.getInstance());
            }else{
                clientDevice.updateCapabilityToken(capabilityToken);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

