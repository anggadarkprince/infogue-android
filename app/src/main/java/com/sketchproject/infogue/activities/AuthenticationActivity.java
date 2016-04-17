package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.LoginFragment;
import com.sketchproject.infogue.fragments.RegisterFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class AuthenticationActivity extends AppCompatActivity {
    public static final String SCREEN_REQUEST = "AuthScreen";
    public static final String AFTER_LOGOUT = "AfterLogout";
    public static final String AFTER_LOGIN = "AfterLogin";
    public static final int LOGIN_SCREEN = 0;
    public static final int REGISTER_SCREEN = 1;

    private ViewPager viewPager;
    private boolean isAfterLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new LoginFragment(), "Log In");
        adapter.addFragment(new RegisterFragment(), "Register");

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 0) {
                    getSupportActionBar().setTitle("Contributor Sign In");
                } else {
                    getSupportActionBar().setTitle("Create an Account");
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isAfterLogout = extras.getBoolean(AFTER_LOGOUT, false);
            int screen = extras.getInt(SCREEN_REQUEST);
            if (screen == LOGIN_SCREEN) {
                setTabLoginActive();
            } else if (screen == REGISTER_SCREEN) {
                setTabRegisterActive();
            }
        }
        else{
            isAfterLogout = false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            if(isAfterLogout){
                launchMainActivity();
            }
            else{
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(isAfterLogout){
            launchMainActivity();
        }

        super.onBackPressed();
    }

    private void launchMainActivity(){
        Intent applicationIntent = new Intent(getBaseContext(), ApplicationActivity.class);
        applicationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        applicationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(applicationIntent);
    }

    public void setTabRegisterActive() {
        viewPager.setCurrentItem(REGISTER_SCREEN, true);
    }

    public void setTabLoginActive() {
        viewPager.setCurrentItem(LOGIN_SCREEN, true);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
