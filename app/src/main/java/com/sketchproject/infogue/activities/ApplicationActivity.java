package com.sketchproject.infogue.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.fragments.HomeFragment;
import com.sketchproject.infogue.fragments.dummy.DummyArticleContent;

import java.lang.reflect.Method;

public class ApplicationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ArticleFragment.OnArticleFragmentInteractionListener {

    public static final String AUTH_ACTIVITY = "authentication-activity";

    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.img_logo_small);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);

        View navigationHeader = navigationView.getHeaderView(0);

        ImageView avatar = (ImageView) navigationHeader.findViewById(R.id.avatar);
        Glide.with(this).load("http://infogue.id/images/contributors/twitter-294039766.jpg")
                .dontAnimate()
                .into(avatar);

        ImageView cover = (ImageView) navigationHeader.findViewById(R.id.cover);
        Glide.with(this).load("http://infogue.id/images/covers/twitter-294039766.jpg")
                .crossFade()
                .into(cover);

        Button mSignInButton = (Button) navigationHeader.findViewById(R.id.btn_sign_in);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(ApplicationActivity.this, AuthenticationActivity.class);
                loginIntent.putExtra(AUTH_ACTIVITY, AuthenticationActivity.LOGIN_SCREEN);
                startActivity(loginIntent);
            }
        });

        Button mSignUpButton = (Button) navigationHeader.findViewById(R.id.btn_sign_up);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(ApplicationActivity.this, AuthenticationActivity.class);
                registerIntent.putExtra(AUTH_ACTIVITY, AuthenticationActivity.REGISTER_SCREEN);
                startActivity(registerIntent);
            }
        });

        Button mCreateArticleButton = (Button) navigationHeader.findViewById(R.id.btn_create_article);
        mCreateArticleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageButton mSignOutButton = (ImageButton) navigationHeader.findViewById(R.id.btn_sign_out);
        mSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(ApplicationActivity.this, AuthenticationActivity.class);
                loginIntent.putExtra(AUTH_ACTIVITY, AuthenticationActivity.LOGIN_SCREEN);
                startActivity(loginIntent);
            }
        });

        MenuItem home = navigationView.getMenu().getItem(0).getSubMenu().getItem(0);
        onNavigationItemSelected(home);
        populateMenu();

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void populateMenu() {
        SubMenu navMenu = navigationView.getMenu().getItem(1).getSubMenu();

        String[] dataNav = {"News", "Economic", "Entertainment", "Sport", "Science", "Technology", "Education", "Photo", "Video", "Others"};
        int[] dataNavId = {11, 12, 13, 14, 15, 16, 17, 18, 19, 20};

        for (int i = 0; i < dataNav.length; i++) {
            navMenu.add(1, dataNavId[i], Menu.CATEGORY_ALTERNATIVE, dataNav[i])
                    .setCheckable(true)
                    .setIcon(R.drawable.ic_circle);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.account, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }

        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_login) {
            Intent loginActivity = new Intent(ApplicationActivity.this, AuthenticationActivity.class);
            startActivity(loginActivity);
        } else if (id == R.id.action_about) {
            Intent aboutActivity = new Intent(ApplicationActivity.this, AboutActivity.class);
            startActivity(aboutActivity);
        } else if (id == R.id.action_exit) {
            finish();
        } else if (id == R.id.action_logout) {
            Intent loginActivity = new Intent(ApplicationActivity.this, AuthenticationActivity.class);
            startActivity(loginActivity);
            finish();
        } else if (id == R.id.action_settings) {

        } else if (id == R.id.action_profile) {
            Intent profileActivity = new Intent(ApplicationActivity.this, ProfileActivity.class);
            profileActivity.putExtra("id", 1);
            profileActivity.putExtra("username", "imeldadwi");
            profileActivity.putExtra("name", "Imelda Dwi Agustine");
            profileActivity.putExtra("location", "Jakarta, Indonesia");
            profileActivity.putExtra("avatar", R.drawable.dummy_avatar);
            startActivity(profileActivity);
        } else if (id == R.id.action_article) {
            Intent articleActivity = new Intent(ApplicationActivity.this, ArticleActivity.class);
            articleActivity.putExtra("id", 1);
            articleActivity.putExtra("username", "imeldadwi");
            startActivity(articleActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onArticleFragmentInteraction(DummyArticleContent.DummyItem item) {
        Log.i("ARTICLE", item.id + " " + item.slug + " " + item.details);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment;
        String title;
        boolean logo;

        int id = item.getItemId();
        String category = item.getTitle().toString();

        if (id == R.id.nav_website) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://infogue.id"));
            startActivity(browserIntent);
        } else if (id == R.id.nav_rating) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.sketchproject.infogue"));
            startActivity(browserIntent);
        } else if (id == R.id.nav_about) {
            Intent aboutActivity = new Intent(ApplicationActivity.this, AboutActivity.class);
            startActivity(aboutActivity);
        } else {
            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
                title = "";
                logo = true;
            } else if (id == R.id.nav_random) {
                fragment = ArticleFragment.newInstance(1, "random");
                title = "Random";
                logo = false;
            } else if (id == R.id.nav_headline) {
                fragment = ArticleFragment.newInstance(1, "headline");
                title = "Headline";
                logo = false;
            } else {
                fragment = ArticleFragment.newInstance(1, id, category);
                title = category;
                logo = false;
            }

            if (fragment != null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container_body, fragment);
                fragmentTransaction.commit();

                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(title);
                    actionBar.setDisplayUseLogoEnabled(logo);
                }
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}
