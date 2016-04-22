package com.sketchproject.infogue.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sketchproject.infogue.R;
import com.sketchproject.infogue.models.Article;

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
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        adapter.addFragment(ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_LATEST), ArticleFragment.FEATURED_LATEST.toUpperCase());
        adapter.addFragment(ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_POPULAR), ArticleFragment.FEATURED_POPULAR.toUpperCase());
        adapter.addFragment(ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_TRENDING), ArticleFragment.FEATURED_TRENDING.toUpperCase());

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
        return rootView;
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
