package com.sketchproject.infogue.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.dummy.DummyArticleContent;

public class HomeFragment extends Fragment implements ArticleFragment.OnArticleFragmentInteractionListener {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
        viewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_list);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_stars);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_whatshot);
    }

    @Override
    public void onArticleFragmentInteraction(DummyArticleContent.DummyItem item) {
        Log.i("RESULT", item.slug + " " + item.details);
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
            if (position == 0) {
                return ArticleFragment.newInstance(1, "latest");
            } else if (position == 1) {
                return ArticleFragment.newInstance(1, "popular");
            } else if (position == 2) {
                return ArticleFragment.newInstance(1, "trending");
            }

            return ArticleFragment.newInstance(1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "LATEST";
            } else if (position == 1) {
                return "POPULAR";
            } else if (position == 2) {
                return "TRENDING";
            }

            return null;
        }
    }
}
