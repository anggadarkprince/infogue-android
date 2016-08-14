package com.sketchproject.infogue.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sketchproject.infogue.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Home with tab fragment.
 * <p/>
 * Sketch Project Studio
 * Created by Angga on 6/04/2016 10.37.
 */
public class HomeFragment extends Fragment {
    public static final String ARG_NEW_INSTANCE = "newInstance";

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create HomeFragment
     *
     * @param newInstance set new instance
     * @return HomeFragment
     */
    public static HomeFragment newInstance(boolean newInstance) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_NEW_INSTANCE, newInstance);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Perform initialization of HomeFragment.
     *
     * @param savedInstanceState saved last state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            adapter = new ViewPagerAdapter(getChildFragmentManager());
            adapter.addFragment(ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_LATEST),
                    ArticleFragment.FEATURED_LATEST.toUpperCase());
            adapter.addFragment(ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_POPULAR),
                    ArticleFragment.FEATURED_POPULAR.toUpperCase());
            adapter.addFragment(ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_TRENDING),
                    ArticleFragment.FEATURED_TRENDING.toUpperCase());
        }
    }

    /**
     * Called after onCreate() and before onCreated()
     *
     * @param inflater           The LayoutInflater object that can be used to inflate view
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to (ApplicationActivity).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from previous
     * @return return the view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        viewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                ViewPagerAdapter section = (ViewPagerAdapter) viewPager.getAdapter();
                ArticleFragment articleFragment = (ArticleFragment) section.getItem(tabLayout.getSelectedTabPosition());
                articleFragment.scrollToTop();
            }
        });

        // setup tab icon
        TabLayout.Tab tab;
        tab = tabLayout.getTabAt(0);
        if (tab != null) {
            tab.setIcon(R.drawable.tab_icon_layer_selector);
        }
        tab = tabLayout.getTabAt(1);
        if (tab != null) {
            tab.setIcon(R.drawable.tab_icon_star_selector);
        }
        tab = tabLayout.getTabAt(2);
        if (tab != null) {
            tab.setIcon(R.drawable.tab_icon_whatshot_selector);
        }
        return rootView;
    }

    /**
     * Passed swipe refresh triggered from ApplicationActivity to current pager article content.
     *
     * @param swipeRefresh swipe view passed from activity
     */
    public void homeRefresh(SwipeRefreshLayout swipeRefresh) {
        ViewPagerAdapter section = (ViewPagerAdapter) viewPager.getAdapter();
        ArticleFragment articleFragment = (ArticleFragment) section.getItem(tabLayout.getSelectedTabPosition());
        articleFragment.refreshArticleList(swipeRefresh);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> mFragmentList = new ArrayList<>();
        private List<String> mFragmentTitleList = new ArrayList<>();

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
