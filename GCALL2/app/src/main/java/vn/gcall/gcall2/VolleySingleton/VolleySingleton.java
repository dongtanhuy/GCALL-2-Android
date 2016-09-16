package vn.gcall.gcall2.VolleySingleton;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import vn.gcall.gcall2.GcallApplication;
/*
* Singleton class manage API request
* More detail at: https://androidresearch.wordpress.com/2014/02/01/android-volley-tutorial/
* */
public class VolleySingleton {
    private static VolleySingleton volleyInstance = null;
    private RequestQueue requestQueue;

    private VolleySingleton() {
        requestQueue = Volley.newRequestQueue(GcallApplication.getGcallApplicationContext());
    }

    public RequestQueue getRequestQueue() {
        return requestQueue;
    }

    public static VolleySingleton getInstance() {
        if (volleyInstance == null) {
            volleyInstance = new VolleySingleton();
        }
        return volleyInstance;
    }
}
