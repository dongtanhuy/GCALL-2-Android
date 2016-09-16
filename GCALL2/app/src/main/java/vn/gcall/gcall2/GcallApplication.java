package vn.gcall.gcall2;


import android.app.Application;
import android.content.Context;

public class GcallApplication extends Application {
    private static GcallApplication gcallApplication;
    @Override
    public void onCreate() {
        super.onCreate();
        gcallApplication = this;
    }

    public static GcallApplication getInstance() {
        return gcallApplication;
    }

    public static Context getGcallApplicationContext(){
        return gcallApplication.getApplicationContext();
    }
}
