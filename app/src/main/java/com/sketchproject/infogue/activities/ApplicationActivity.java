package com.sketchproject.infogue.activities;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.fragments.HomeFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.IconizedMenu;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.Constant;

import java.lang.reflect.Method;

public class ApplicationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        ArticleFragment.OnArticleFragmentInteractionListener,
        ConnectionDetector.OnLostConnectionListener {

    private NavigationView navigationView;

    private SessionManager session;

    private ConnectionDetector connectionDetector;

    private AlertDialog dialogExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);

        connectionDetector = new ConnectionDetector(getBaseContext());
        connectionDetector.setLostConnectionListener(this);
        session = new SessionManager(getBaseContext());

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

        MenuItem home = navigationView.getMenu().getItem(0).getSubMenu().getItem(0);
        onNavigationItemSelected(home);
        populateMenu();

        View navigationHeader = navigationView.getHeaderView(0);

        handleNavigationLayout(navigationHeader);

        if (!session.getSessionData(SessionManager.KEY_USER_LEARNED, false)) {
            drawer.openDrawer(GravityCompat.START);
            session.setSessionData(SessionManager.KEY_USER_LEARNED, true);
        }
    }

    /**
     * Decide to show which header and set according login status.
     *
     * @param navigationHeader handle header view
     */
    private void handleNavigationLayout(View navigationHeader) {
        ViewGroup headerSigned = (RelativeLayout) navigationHeader.findViewById(R.id.signed_header);
        ViewGroup headerUnsigned = (LinearLayout) navigationHeader.findViewById(R.id.unsigned_header);

        if (session.isLoggedIn()) {
            // Select signed as visible view
            headerSigned.setVisibility(View.VISIBLE);
            headerUnsigned.setVisibility(View.GONE);

            // Avatar image view delegating event and download image async
            ImageView mAvatarImage = (ImageView) navigationHeader.findViewById(R.id.avatar);
            mAvatarImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProfile();
                }
            });
            Glide.with(this).load(session.getSessionData(SessionManager.KEY_AVATAR, Constant.URL_AVATAR_DEFAULT))
                    .dontAnimate()
                    .into(mAvatarImage);

            // Cover image view delegating event and download image async with cross fade effect
            ImageView mCoverImage = (ImageView) navigationHeader.findViewById(R.id.cover);
            Glide.with(this).load(session.getSessionData(SessionManager.KEY_COVER, Constant.URL_COVER_DEFAULT))
                    .crossFade()
                    .into(mCoverImage);

            // Name view delegating event
            TextView mNameView = (TextView) navigationHeader.findViewById(R.id.name);
            mNameView.setText(session.getSessionData(SessionManager.KEY_NAME, "Anonymous"));
            mNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProfile();
                }
            });

            // Set location text view
            TextView mLocationView = (TextView) navigationHeader.findViewById(R.id.location);
            mLocationView.setText(session.getSessionData(SessionManager.KEY_LOCATION, "No Location"));

            // Delegating create article event
            Button mCreateArticleButton = (Button) navigationHeader.findViewById(R.id.btn_create_article);
            mCreateArticleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (session.isLoggedIn()) {
                        Intent createArticleIntent = new Intent(ApplicationActivity.this, ArticleCreateActivity.class);
                        startActivity(createArticleIntent);
                    } else {
                        signOutUser();
                    }
                }
            });

            // Delegating sign out event
            ImageButton mSignOutButton = (ImageButton) navigationHeader.findViewById(R.id.btn_sign_out);
            mSignOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmSignOut();
                }
            });
        } else {
            // Select unsigned as visible view
            headerSigned.setVisibility(View.GONE);
            headerUnsigned.setVisibility(View.VISIBLE);

            final Intent authIntent = new Intent(getBaseContext(), AuthenticationActivity.class);

            // Delegating sign in event
            Button mSignInButton = (Button) navigationHeader.findViewById(R.id.btn_sign_in);
            mSignInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isSessionActive()) {
                        authIntent.putExtra(AuthenticationActivity.SCREEN_REQUEST, AuthenticationActivity.LOGIN_SCREEN);
                        startActivity(authIntent);
                    }
                }
            });

            // Delegating sign up event
            Button mSignUpButton = (Button) navigationHeader.findViewById(R.id.btn_sign_up);
            mSignUpButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isSessionActive()) {
                        authIntent.putExtra(AuthenticationActivity.SCREEN_REQUEST, AuthenticationActivity.REGISTER_SCREEN);
                        startActivity(authIntent);
                    }
                }
            });
        }
    }

    private boolean isSessionActive() {
        if (session.isLoggedIn()) {
            finish();
            Intent applicationIntent = new Intent(getBaseContext(), ApplicationActivity.class);
            startActivity(applicationIntent);
            return true;
        }
        return false;
    }

    /**
     * Show confirm dialog before exit.
     */
    private void confirmExit() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
        builder.setTitle("Infogue.id");
        builder.setMessage("Do you want to exit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNeutralButton("Open Infogue.id", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.BASE_URL));
                startActivity(browserIntent);
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
        AppHelper.dialogButtonTheme(this, dialog);
    }

    /**
     * Show dialog confirmation before signin out.
     */
    private void confirmSignOut() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
        builder.setTitle("Sign Out");
        builder.setMessage("Do you want to sign out?");
        builder.setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                signOutUser();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogExit = builder.create();
        dialogExit.show();
        AppHelper.dialogButtonTheme(this, dialogExit);
    }

    /**
     * Signing out user and make sure persist process is success.
     */
    private void signOutUser() {
        if (session.logoutUser()) {
            Intent loginIntent = new Intent(ApplicationActivity.this, AuthenticationActivity.class);
            loginIntent.putExtra(AuthenticationActivity.AFTER_LOGOUT, true);
            loginIntent.putExtra(AuthenticationActivity.SCREEN_REQUEST, AuthenticationActivity.LOGIN_SCREEN);

            // Closing all the Activities
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(loginIntent);

            finish();
        } else {
            // Notify remove persistent session data is failed
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.article_form), "Signing out failed!", Snackbar.LENGTH_LONG);

            // noinspection deprecation
            snackbar.setActionTextColor(getResources().getColor(R.color.light));
            snackbar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    signOutUser();
                }
            }).show();

            View snackbarView = snackbar.getView();

            // noinspection deprecation
            snackbarView.setBackgroundColor(getResources().getColor(R.color.color_danger));
        }
    }

    /**
     * Call profile activity and passing session data.
     */
    private void showProfile() {
        Intent profileIntent = new Intent(getBaseContext(), ProfileActivity.class);
        // Populate extra data from session
        profileIntent.putExtra(SessionManager.KEY_ID, session.getSessionData(SessionManager.KEY_ID, 0));
        profileIntent.putExtra(SessionManager.KEY_USERNAME, session.getSessionData(SessionManager.KEY_USERNAME, null));
        profileIntent.putExtra(SessionManager.KEY_NAME, session.getSessionData(SessionManager.KEY_NAME, null));
        profileIntent.putExtra(SessionManager.KEY_LOCATION, session.getSessionData(SessionManager.KEY_LOCATION, null));
        profileIntent.putExtra(SessionManager.KEY_ABOUT, session.getSessionData(SessionManager.KEY_ABOUT, null));
        profileIntent.putExtra(SessionManager.KEY_AVATAR, session.getSessionData(SessionManager.KEY_AVATAR, null));
        profileIntent.putExtra(SessionManager.KEY_COVER, session.getSessionData(SessionManager.KEY_COVER, null));
        profileIntent.putExtra(SessionManager.KEY_ARTICLE, session.getSessionData(SessionManager.KEY_ARTICLE, 0));
        profileIntent.putExtra(SessionManager.KEY_FOLLOWER, session.getSessionData(SessionManager.KEY_FOLLOWER, 0));
        profileIntent.putExtra(SessionManager.KEY_FOLLOWING, session.getSessionData(SessionManager.KEY_FOLLOWING, 0));
        profileIntent.putExtra(SessionManager.KEY_IS_FOLLOWING, false);
        startActivity(profileIntent);
    }

    /**
     * Create category menu on navigation drawer.
     */
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

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (dialogExit != null && dialogExit.isShowing()) {
                dialogExit.cancel();
            } else {
                confirmExit();
            }
        }
    }

    /**
     * Create option menu.
     *
     * @param menu content of option menu
     * @return boolean
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (session.isLoggedIn()) {
            getMenuInflater().inflate(R.menu.account, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu, menu);
        }

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    /**
     * Force application show icon in option menu.
     *
     * @param view which handle option menu
     * @param menu content of option menu
     * @return boolean
     */
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

    /**
     * Select option menu.
     *
     * @param item of selected option menu
     * @return boolean
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_login:
                if (!isSessionActive()) {
                    Intent loginIntent = new Intent(getBaseContext(), AuthenticationActivity.class);
                    startActivity(loginIntent);
                }
                break;
            case R.id.action_feedback: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_FEEDBACK));
                startActivity(browserIntent);
                break;
            }
            case R.id.action_help: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_HELP));
                startActivity(browserIntent);
                break;
            }
            case R.id.action_rating: {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_APP));
                startActivity(browserIntent);
                break;
            }
            case R.id.action_about:
                Intent aboutIntent = new Intent(getBaseContext(), AboutActivity.class);
                startActivity(aboutIntent);
                break;
            case R.id.action_exit:
                confirmExit();
                break;
            case R.id.action_logout:
                confirmSignOut();
                break;
            case R.id.action_settings:
                if (session.isLoggedIn()) {
                    Log.i("INFOGUE/App", "Setting");
                } else {
                    signOutUser();
                }
                break;
            case R.id.action_profile:
                if (session.isLoggedIn()) {
                    showProfile();
                } else {
                    signOutUser();
                }
                break;
            case R.id.action_article:
                if (session.isLoggedIn()) {
                    Intent articleIntent = new Intent(getBaseContext(), ArticleActivity.class);

                    articleIntent.putExtra(SessionManager.KEY_ID, session.getSessionData(SessionManager.KEY_ID, 0));
                    articleIntent.putExtra(SessionManager.KEY_USERNAME, session.getSessionData(SessionManager.KEY_USERNAME, null));

                    startActivity(articleIntent);
                } else {
                    signOutUser();
                }
                break;
            case R.id.action_follower:
                if (session.isLoggedIn()) {
                    Intent followerIntent = new Intent(getBaseContext(), FollowerActivity.class);

                    followerIntent.putExtra(FollowerActivity.SCREEN_REQUEST, FollowerActivity.FOLLOWER_SCREEN);
                    followerIntent.putExtra(SessionManager.KEY_ID, session.getSessionData(SessionManager.KEY_ID, 0));
                    followerIntent.putExtra(SessionManager.KEY_USERNAME, session.getSessionData(SessionManager.KEY_USERNAME, null));

                    startActivity(followerIntent);
                } else {
                    signOutUser();
                }
                break;
            case R.id.action_following:
                if (session.isLoggedIn()) {
                    Intent followingIntent = new Intent(getBaseContext(), FollowerActivity.class);

                    followingIntent.putExtra(FollowerActivity.SCREEN_REQUEST, FollowerActivity.FOLLOWING_SCREEN);
                    followingIntent.putExtra(SessionManager.KEY_ID, session.getSessionData(SessionManager.KEY_ID, 0));
                    followingIntent.putExtra(SessionManager.KEY_USERNAME, session.getSessionData(SessionManager.KEY_USERNAME, null));

                    startActivity(followingIntent);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Interaction with article row.
     *
     * @param article contain article model data
     */
    @Override
    public void onArticleFragmentInteraction(View view, Article article) {
        if (connectionDetector.isNetworkAvailable()) {
            Log.i("INFOGUE/Article", article.getId() + " " + article.getSlug() + " " + article.getTitle());
            connectionDetector.dismissNotification();
        } else {
            onLostConnectionNotified(getBaseContext());
        }
    }

    @Override
    public void onArticlePopupInteraction(final View view, final Article article) {
        IconizedMenu popup = new IconizedMenu(new ContextThemeWrapper(view.getContext(), R.style.AppTheme_PopupOverlay), view);
        popup.inflate(R.menu.article);
        popup.setGravity(Gravity.END);
        popup.setOnMenuItemClickListener(new IconizedMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (connectionDetector.isNetworkAvailable()) {
                    if (id == R.id.action_view) {
                        Toast.makeText(view.getContext(), "view " + article.getTitle(), Toast.LENGTH_LONG).show();
                    } else if (id == R.id.action_browse) {
                        Toast.makeText(view.getContext(), "browse " + article.getTitle(), Toast.LENGTH_LONG).show();
                    } else if (id == R.id.action_share) {
                        Toast.makeText(view.getContext(), "share " + article.getTitle(), Toast.LENGTH_LONG).show();
                    } else if (id == R.id.action_rate) {
                        Toast.makeText(view.getContext(), "rate 5 " + article.getTitle(), Toast.LENGTH_LONG).show();
                    }
                    connectionDetector.dismissNotification();
                } else{
                    onLostConnectionNotified(getBaseContext());
                }

                return false;
            }
        });
        popup.show();
    }

    @Override
    public void onArticleLongClickInteraction(final View view, final Article article) {
        final CharSequence[] items = {
                "View / Open", "Browse in Web", "Share Article", "Give 5 Stars"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (connectionDetector.isNetworkAvailable()) {
                    Toast.makeText(view.getContext(), items[item] + article.getTitle(), Toast.LENGTH_LONG).show();
                } else{
                    onLostConnectionNotified(getBaseContext());
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Select navigation menu.
     *
     * @param item of selected navigation drawer menu
     * @return boolean
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment;
        String title;
        boolean logo;

        int id = item.getItemId();
        String category = item.getTitle().toString();

        if (id == R.id.nav_website) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.BASE_URL));
            startActivity(browserIntent);
        } else if (id == R.id.nav_rating) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.URL_APP));
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

    @Override
    public void onLostConnectionNotified(Context context) {
        connectionDetector.snackbarDisconnectNotification(findViewById(android.R.id.content), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionDetector.dismissNotification();

                if (!connectionDetector.isNetworkAvailable()) {
                    connectionDetector.snackbarDisconnectNotification(findViewById(android.R.id.content), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onLostConnectionNotified(getBaseContext());
                        }
                    }, Constant.jokes[(int) Math.floor(Math.random() * Constant.jokes.length)] + " stole my internet T_T", "RETRY");
                } else {
                    connectionDetector.snackbarConnectedNotification(findViewById(android.R.id.content), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            connectionDetector.dismissNotification();
                        }
                    });
                }
            }
        });
    }

}
