package com.sketchproject.infogue.activities;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.LoginFragment;
import com.sketchproject.infogue.fragments.RegisterFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * A login screen that offers login via email/password.
 */
public class AuthenticationActivity extends AppCompatActivity {

    public static int LOGIN_SCREEN = 0;
    public static int REGISTER_SCREEN = 1;

    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0);
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
            int screen = extras.getInt(ApplicationActivity.AUTH_ACTIVITY);
            if (screen == LOGIN_SCREEN) {
                setTabLoginActive();
            } else if (screen == REGISTER_SCREEN) {
                setTabRegisterActive();
            }
        }
    }

    public void setTabRegisterActive() {
        viewPager.setCurrentItem(REGISTER_SCREEN);
    }

    public void setTabLoginActive() {
        viewPager.setCurrentItem(LOGIN_SCREEN);
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
