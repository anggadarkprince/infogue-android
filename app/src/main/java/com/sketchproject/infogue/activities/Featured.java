package com.sketchproject.infogue.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.sketchproject.infogue.R;

public class Featured extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;

    private Button buttonSignIn;

    private Button buttonGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                changePage(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int position) {
            }
        });

        buttonSignIn = (Button) findViewById(R.id.btn_sign_in);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginActivity = new Intent(Featured.this, Authentication.class);
                startActivity(loginActivity);
            }
        });

        buttonGetStarted = (Button) findViewById(R.id.btn_get_started);
        buttonGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivity = new Intent(Featured.this, Application.class);
                startActivity(mainActivity);
                finish();
            }
        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * @param position index position of pager view
     */
    private void changePage(int position) {
        ImageView view1 = (ImageView) findViewById(R.id.page1);
        view1.setImageResource(R.drawable.circle_featured_normal);

        ImageView view2 = (ImageView) findViewById(R.id.page2);
        view2.setImageResource(R.drawable.circle_featured_normal);

        ImageView view3 = (ImageView) findViewById(R.id.page3);
        view3.setImageResource(R.drawable.circle_featured_normal);

        switch (position) {
            case 0:
                view1.setImageResource(R.drawable.circle_featured_active);
                break;
            case 1:
                view2.setImageResource(R.drawable.circle_featured_active);
                break;
            case 2:
                view3.setImageResource(R.drawable.circle_featured_active);
                break;
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * @param sectionNumber index of page
         * @return PlaceholderFragment
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_featured, container, false);

            ImageView featuredBackground = (ImageView) rootView.findViewById(R.id.featured_background);
            ImageView featuredContent = (ImageView) rootView.findViewById(R.id.featured_content);

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 1:
                    featuredBackground.setImageDrawable(getResources().getDrawable(R.drawable.bg_featured_1));
                    featuredContent.setImageDrawable(getResources().getDrawable(R.drawable.img_featured_1));
                    break;
                case 2:
                    featuredBackground.setImageDrawable(getResources().getDrawable(R.drawable.bg_featured_2));
                    featuredContent.setImageDrawable(getResources().getDrawable(R.drawable.img_featured_2));
                    break;
                case 3:
                    featuredBackground.setImageDrawable(getResources().getDrawable(R.drawable.bg_featured_3));
                    featuredContent.setImageDrawable(getResources().getDrawable(R.drawable.img_featured_3));
                    break;
            }

            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }
}