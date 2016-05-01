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

/**
 * A {@link AppCompatActivity} subclass, featured screen show slide show image and feature.
 * <p>
 * Sketch Project Studio
 * Created by Angga on 1/04/2016 10.37.
 */
public class FeaturedActivity extends AppCompatActivity {
    private ImageView pagination1;
    private ImageView pagination2;
    private ImageView pagination3;

    /**
     * Perform initialization of FeaturedActivity.
     *
     * @param savedInstanceState saved last state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_featured);

        pagination1 = (ImageView) findViewById(R.id.page1);
        pagination2 = (ImageView) findViewById(R.id.page2);
        pagination3 = (ImageView) findViewById(R.id.page3);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        if (mViewPager != null) {
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
        }

        Button buttonSignIn = (Button) findViewById(R.id.btn_sign_in);
        if (buttonSignIn != null) {
            buttonSignIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent loginActivity = new Intent(FeaturedActivity.this, AuthenticationActivity.class);
                    startActivity(loginActivity);
                }
            });
        }

        Button buttonGetStarted = (Button) findViewById(R.id.btn_get_started);
        if (buttonGetStarted != null) {
            buttonGetStarted.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent mainActivity = new Intent(FeaturedActivity.this, ApplicationActivity.class);
                    startActivity(mainActivity);
                    finish();
                }
            });
        }

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * Change pager content and set the pagination.
     *
     * @param position index position of pager view
     */
    private void changePage(int position) {
        pagination1.setImageResource(R.drawable.circle_featured_normal);
        pagination2.setImageResource(R.drawable.circle_featured_normal);
        pagination3.setImageResource(R.drawable.circle_featured_normal);

        switch (position) {
            case 0:
                pagination1.setImageResource(R.drawable.circle_featured_active);
                break;
            case 1:
                pagination2.setImageResource(R.drawable.circle_featured_active);
                break;
            case 2:
                pagination3.setImageResource(R.drawable.circle_featured_active);
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
                    featuredBackground.setImageResource(R.drawable.bg_featured_1);
                    featuredContent.setImageResource(R.drawable.img_featured_1);
                    break;
                case 2:
                    featuredBackground.setImageResource(R.drawable.bg_featured_2);
                    featuredContent.setImageResource(R.drawable.img_featured_2);
                    break;
                case 3:
                    featuredBackground.setImageResource(R.drawable.bg_featured_3);
                    featuredContent.setImageResource(R.drawable.img_featured_3);
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
