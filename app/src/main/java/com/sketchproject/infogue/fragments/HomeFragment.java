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

public class HomeFragment extends Fragment {

    private ViewPager viewPager;
    private TabLayout tabLayout;

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

        ViewPagerAdapter adapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        adapter.addFragment(ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_LATEST), ArticleFragment.FEATURED_LATEST.toUpperCase());
        adapter.addFragment(ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_POPULAR), ArticleFragment.FEATURED_POPULAR.toUpperCase());
        adapter.addFragment(ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_TRENDING), ArticleFragment.FEATURED_TRENDING.toUpperCase());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        TabLayout.Tab tab;
        tab = tabLayout.getTabAt(0);
        if(tab != null){
            tab.setIcon(R.drawable.tab_icon_layer_selector);
        }
        tab = tabLayout.getTabAt(1);
        if(tab != null){
            tab.setIcon(R.drawable.tab_icon_star_selector);
        }
        tab = tabLayout.getTabAt(2);
        if(tab != null){
            tab.setIcon(R.drawable.tab_icon_whatshot_selector);
        }
    }

    public void homeRefresh(SwipeRefreshLayout swipeRefresh){
        ViewPagerAdapter section = (ViewPagerAdapter) viewPager.getAdapter();
        ArticleFragment articleFragment = (ArticleFragment) section.getItem(tabLayout.getSelectedTabPosition());
        articleFragment.refreshArticleList(swipeRefresh);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
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
