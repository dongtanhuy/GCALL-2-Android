package vn.gcall.gcall2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.viewbadger.BadgeView;
import com.twilio.client.Connection;
import com.twilio.client.ConnectionListener;
import com.twilio.client.Device;
import com.twilio.client.DeviceListener;
import com.twilio.client.PresenceEvent;


import java.util.ArrayList;
import java.util.List;

import vn.gcall.gcall2.Helpers.NotificationCounter;
import vn.gcall.gcall2.Helpers.SessionManager;
/*
* Activity content 4 tab view of application
* */
public class TabViewActivity extends AppCompatActivity implements DeviceListener,ConnectionListener {
    SessionManager manager;
    public static String email,token,fullname,phone;
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int[] tabIcons={
            R.drawable.icon_call,
            R.drawable.icon_working,
            R.drawable.icon_notification,
            R.drawable.icon_more
    };
    private int[] tabIcons_selected={
//            R.drawable.icon_call_selected,
            R.drawable.icon_call_selected,
            R.drawable.icon_working_selected,
            R.drawable.icon_notification_selected,
            R.drawable.icon_more_selected
    };
    private static final int MIC_PERMISSION_REQUEST_CODE = 1;
    private int page;

    public static NotificationCounter notificationCounter;
    private BadgeView notificationBadgeView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab_view);
        manager=new SessionManager();
        token=manager.getStringPreferences(getApplicationContext(),"TOKEN");
        email=manager.getStringPreferences(getApplicationContext(),"EMAIL");
        phone=manager.getStringPreferences(getApplicationContext(),"PHONE");
        fullname=manager.getStringPreferences(getApplicationContext(),"FULLNAME");
        Boolean islogin=manager.getBoolPreferences(getApplicationContext(),"IS_LOGGED_IN");
        Log.d("EMAIL",email);
        Log.d("PHONE",phone);
        Log.d("FULLNAME",fullname);
        Log.d("ISLOG",Boolean.toString(islogin));
        if (!islogin){
            navigatetoSigninActivity();
        }

        notificationCounter=new NotificationCounter(getApplicationContext());
        toolbar=(Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("GCALL");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        viewPager= (ViewPager) findViewById(R.id.viewpager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setupViewPager(viewPager);
        int defaultPage=0;
        page=getIntent().getIntExtra("PAGE_INDEX",defaultPage);
        viewPager.setCurrentItem(page);
        tabLayout=(TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons(notificationCounter);
        if (!checkPermissionForMicrophone()) {
            requestPermissionForMicrophone();
        }

    }

    /*
    * Set up icon for each tab
    * */
    private void setupTabIcons(NotificationCounter notificationCounter){
        ImageView callTab=(ImageView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_tab,null);
        callTab.setImageResource(tabIcons[0]);
        tabLayout.getTabAt(0).setCustomView(callTab);

        ImageView workingTab=(ImageView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_tab,null);
        workingTab.setImageResource(tabIcons[1]);
        tabLayout.getTabAt(1).setCustomView(workingTab);

        ImageView notiTab=(ImageView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_tab,null);
        notiTab.setImageResource(tabIcons[2]);
        tabLayout.getTabAt(2).setCustomView(notiTab);
        notificationBadgeView=notificationCounter.getBadgeView(notiTab);

        ImageView moreTab=(ImageView) LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_tab,null);
        moreTab.setImageResource(tabIcons[3]);
        tabLayout.getTabAt(3).setCustomView(moreTab);


        TabLayout.Tab initTab=tabLayout.getTabAt(page);
        ImageView tv=(ImageView) initTab.getCustomView();
        tv.setImageResource(tabIcons_selected[page]);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ImageView v=(ImageView) tab.getCustomView();
                v.setImageResource(tabIcons_selected[tabLayout.getSelectedTabPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ImageView v=(ImageView) tab.getCustomView();
                v.setImageResource(tabIcons[tabLayout.getSelectedTabPosition()]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
//                tab.getIcon().setColorFilter(Color.parseColor("#662F8F"), PorterDuff.Mode.SRC_IN);
            }
        });
    }

    /*
    * Set up view pager for each tab to show the content
    * */
    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CallFragment(),"Call");
        adapter.addFragment(new WorkingFragment(),"Working");
        adapter.addFragment(new NotificationFragment(),"Notification");
        adapter.addFragment(new MoreFragment(),"More");
        viewPager.setAdapter(adapter);
    }

    /*
    * Adater for view pager
    * */
    class ViewPagerAdapter extends FragmentPagerAdapter{
        private final List<Fragment> fragmentList= new ArrayList<>();
        private final List<String> framentTitleList= new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager){
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title){
            fragmentList.add(fragment);
            framentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
//            return framentTitleList.get(position);
            return null;
        }
    }

    /*
    * Back to sign in view if session expire
    * */
    public void navigatetoSigninActivity(){
        Intent intent= new Intent(getApplicationContext(),SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    private boolean checkPermissionForMicrophone() {
        int resultMic = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (resultMic == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
    private void requestPermissionForMicrophone() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
            Toast.makeText(getApplicationContext().getApplicationContext(),
                    "Microphone permissions needed. Please allow in App Settings for additional functionality.",
                    Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MIC_PERMISSION_REQUEST_CODE);
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            /*
             * Determine if the receiving Intent has an extra for the incoming connection. If so,
             * remove it from the Intent to prevent handling it again next time the Activity is resumed
             */
            Device device = intent.getParcelableExtra(Device.EXTRA_DEVICE);
            Connection incomingConnection = intent.getParcelableExtra(Device.EXTRA_CONNECTION);
            Log.d("TAGK", "onResume connection");
            if (incomingConnection == null && device == null) {
                return;
            }
            intent.removeExtra(Device.EXTRA_DEVICE);
            intent.removeExtra(Device.EXTRA_CONNECTION);
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

    }

    @Override
    public void onDisconnected(Connection connection, int i, String s) {

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

    @Override
    public void onBackPressed() {
        Log.d("BACK","pressed");
        finish();
    }

}
