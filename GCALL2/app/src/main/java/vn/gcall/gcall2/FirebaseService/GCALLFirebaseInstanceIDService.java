package vn.gcall.gcall2.FirebaseService;

import android.util.Log;

import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by This PC on 16/06/2016.
 * Call to create device token to Receive Notification from Firebase Cloud Messaging
 * More detail at: https://firebase.google.com/docs/cloud-messaging/android/client
 */
public class GCALLFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG="GCALLFirebaseIDService";

    @Override
    public void onTokenRefresh() {
        String token= FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG,"refreshToken "+token);
    }
}
