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
 * A {@link AppCompatActivity} subclass, login screen that offers login via email/password.
 *
 * Sketch Project Studio
 * Created by Angga on 1/04/2016 10.37.
 */
public class AuthenticationActivity extends AppCompatActivity {

    public static final String SCREEN_REQUEST = "AuthScreen";
    public static final String AFTER_LOGOUT = "AfterLogout";
    public static final String AFTER_LOGIN = "AfterLogin";

    public static final int LOGIN_SCREEN = 0;
    public static final int REGISTER_SCREEN = 1;

    private ViewPager viewPager;
    private boolean isAfterLogout;

    /**
     * Perform initialization of AuthenticationActivity.
     *
     * @param savedInstanceState saved last state
     */
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
        adapter.addFragment(new LoginFragment(), getString(R.string.label_title_login));
        adapter.addFragment(new RegisterFragment(), getString(R.string.label_title_register));

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        if (viewPager != null) {
            viewPager.setAdapter(adapter);
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (position == 0) {
                        getSupportActionBar().setTitle(R.string.title_activity_sign_in);
                    } else {
                        getSupportActionBar().setTitle(R.string.title_activity_sign_up);
                    }
                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }

        // collect extras data from activity which sent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isAfterLogout = extras.getBoolean(AFTER_LOGOUT, false);
            int screen = extras.getInt(SCREEN_REQUEST);
            if (screen == LOGIN_SCREEN) {
                setTabLoginActive();
            } else if (screen == REGISTER_SCREEN) {
                setTabRegisterActive();
            }
        } else {
            isAfterLogout = false;
        }
    }

    /**
     * Select action for option menu.
     *
     * @param item of selected option menu
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (isAfterLogout) {
                launchMainActivity();
            } else {
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * If isAfterLogout is true it means this activity called from ApplicationActivity
     * it finished for good to prevent leave pieces state before logout,and there is nothing in
     * back stack so relaunch the new one.
     */
    @Override
    public void onBackPressed() {
        if (isAfterLogout) {
            launchMainActivity();
        }
        super.onBackPressed();
    }

    /**
     * Launch new ApplicationActivity in new task and make sure clear all back stack in top.
     */
    private void launchMainActivity() {
        Intent applicationIntent = new Intent(getBaseContext(), ApplicationActivity.class);
        applicationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        applicationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(applicationIntent);
    }

    /**
     * Set current tab at login section (index 0 default)
     */
    public void setTabLoginActive() {
        viewPager.setCurrentItem(LOGIN_SCREEN, true);
    }

    /**
     * Set current tab at register section (index 1)
     */
    public void setTabRegisterActive() {
        viewPager.setCurrentItem(REGISTER_SCREEN, true);
    }

    /**
     * View pager to handle content of tab and related title.
     */
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
