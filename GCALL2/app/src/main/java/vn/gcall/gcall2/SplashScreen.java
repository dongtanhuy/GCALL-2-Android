package vn.gcall.gcall2;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import vn.gcall.gcall2.Helpers.SessionManager;
/*
* Show logo
* */
public class SplashScreen extends Activity {
    private SessionManager manager;
    //Splash screen timer
    private static int SPLASH_TIME_OUT=3000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        manager=new SessionManager();
//        manager.setFirtTimeLunch(getApplicationContext(),true);//false
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (manager.isFirtTimeLunch(getApplicationContext())){
                    Log.d("FIRST_splash","LUNCH");
                    Intent i = new Intent(SplashScreen.this, Intro.class);
                    startActivity(i);
                }else
                {
                    Intent intent= new Intent(SplashScreen.this,SignInActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        },SPLASH_TIME_OUT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
