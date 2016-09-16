package vn.gcall.gcall2.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by This PC on 16/05/2016.
 * Manage the log in session and check first time run
 */
public class SessionManager {
    private static final String PREF_NAME="GCALL";
    private static final String CHECK_FIRST_TIME="FIRST_TIME";
    int PRIVATE_MODE=0;

    public void setStringPreferences(Context context,String key,String value){
        SharedPreferences.Editor editor= context.getSharedPreferences(PREF_NAME,PRIVATE_MODE).edit();
        editor.putString(key,value);
        editor.commit();
    }
    public void setBoolPreferences(Context context, String key, Boolean value){
        SharedPreferences.Editor editor= context.getSharedPreferences(PREF_NAME,PRIVATE_MODE).edit();
        editor.putBoolean(key,value);
        editor.commit();
    }

    public String getStringPreferences(Context context, String key){
        SharedPreferences pref= context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        String result=pref.getString(key,"");
        return result;
    }

    public Boolean getBoolPreferences(Context context, String key){
        SharedPreferences pref= context.getSharedPreferences(PREF_NAME,PRIVATE_MODE);
        Boolean result=pref.getBoolean(key,false);
        return result;
    }

    public void deleteSession(Context context){
        SharedPreferences.Editor editor=context.getSharedPreferences(PREF_NAME,PRIVATE_MODE).edit();
        editor.clear();
        editor.commit();

    }

    public void setFirtTimeLunch(Context context,boolean isFirstTime){
        SharedPreferences.Editor editor=context.getSharedPreferences(CHECK_FIRST_TIME,PRIVATE_MODE).edit();
        editor.putBoolean("FIRST_TIME_RUN",isFirstTime);
        editor.commit();
    }

    public boolean isFirtTimeLunch(Context context){
        return context.getSharedPreferences(CHECK_FIRST_TIME,PRIVATE_MODE).getBoolean("FIRST_TIME_RUN",true);
    }
}
