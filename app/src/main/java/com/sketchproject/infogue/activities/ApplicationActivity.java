package com.sketchproject.infogue.activities;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.sketchproject.infogue.R;
import com.sketchproject.infogue.fragments.ArticleFragment;
import com.sketchproject.infogue.fragments.HomeFragment;
import com.sketchproject.infogue.models.Article;
import com.sketchproject.infogue.models.Category;
import com.sketchproject.infogue.models.Repositories.CategoryRepository;
import com.sketchproject.infogue.models.Repositories.SubcategoryRepository;
import com.sketchproject.infogue.models.Subcategory;
import com.sketchproject.infogue.modules.ConnectionDetector;
import com.sketchproject.infogue.modules.IconizedMenu;
import com.sketchproject.infogue.modules.ObjectPooling;
import com.sketchproject.infogue.modules.SessionManager;
import com.sketchproject.infogue.modules.VolleySingleton;
import com.sketchproject.infogue.utils.AppHelper;
import com.sketchproject.infogue.utils.Constant;
import com.sketchproject.infogue.utils.UrlHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        ArticleFragment.OnArticleFragmentInteractionListener,
        ConnectionDetector.OnLostConnectionListener,
        ConnectionDetector.OnConnectionEstablished {

    private NavigationView navigationView;
    private View navigationHeader;
    private SwipeRefreshLayout swipeRefreshLayout;
    private SessionManager session;
    private ConnectionDetector connectionDetector;
    private AlertDialog dialogConfirmation;
    private ObjectPooling objectPooling;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_application);

        connectionDetector = new ConnectionDetector(getBaseContext());
        connectionDetector.setLostConnectionListener(this);
        session = new SessionManager(getBaseContext());
        objectPooling = new ObjectPooling();

        progress = new ProgressDialog(this);
        progress.setMessage("Downloading Category Menu");
        progress.setIndeterminate(true);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setLogo(R.drawable.img_logo_small);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        if (drawer != null) {
            drawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.setCheckedItem(R.id.nav_home);
        }

        MenuItem home = null;
        if (navigationView != null) {
            home = navigationView.getMenu().getItem(0).getSubMenu().getItem(0);
        }
        onNavigationItemSelected(home);

        navigationHeader = navigationView.getHeaderView(0);

        handleNavigationLayout();

        if (!session.getSessionData(SessionManager.KEY_USER_LEARNED, false)) {
            if (drawer != null) {
                drawer.openDrawer(GravityCompat.START);
            }
            downloadCategoryMenu();
        } else {
            CategoryRepository categoryRepository = new CategoryRepository();
            populateMenu(categoryRepository.retrieveData());
        }

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setColorSchemeResources(R.color.color_hazard, R.color.color_info, R.color.color_warning);
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Fragment appFragment = getSupportFragmentManager().findFragmentById(R.id.container_body);
                    if (appFragment instanceof HomeFragment) {
                        HomeFragment homeFragment = (HomeFragment) appFragment;
                        homeFragment.homeRefresh(swipeRefreshLayout);
                    } else {
                        ArticleFragment articleFragment = (ArticleFragment) appFragment;
                        articleFragment.refreshArticleList(swipeRefreshLayout);
                    }
                }
            });
        }
    }

    public void setSwipeEnable(boolean state) {
        swipeRefreshLayout.setEnabled(state);
    }

    private void downloadCategoryMenu() {
        progress.show();
        JsonObjectRequest menuRequest = new JsonObjectRequest(Request.Method.GET, Constant.URL_API_CATEGORY, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String status = response.getString("status");
                            JSONArray categories = response.getJSONArray("menus");

                            if (status.equals(Constant.REQUEST_SUCCESS)) {
                                CategoryRepository categoryRepository = new CategoryRepository();
                                categoryRepository.clearData();
                                SubcategoryRepository subcategoryRepository = new SubcategoryRepository();
                                subcategoryRepository.clearData();

                                for (int i = 0; i < categories.length(); i++) {
                                    JSONObject categoryObject = categories.getJSONObject(i);

                                    Category category = new Category();
                                    category.setId(categoryObject.getInt(Category.COLUMN_ID));
                                    category.setCategory(categoryObject.getString(Category.COLUMN_CATEGORY));

                                    categoryRepository.createData(category);

                                    JSONArray subcategories = categoryObject.getJSONArray("subcategories");
                                    for (int j = 0; j < subcategories.length(); j++) {
                                        JSONObject subcategoryObject = subcategories.getJSONObject(j);

                                        Subcategory subcategory = new Subcategory();
                                        subcategory.setId(subcategoryObject.getInt(Subcategory.COLUMN_ID));
                                        subcategory.setCategoryId(subcategoryObject.getInt(Subcategory.COLUMN_CATEGORY_ID));
                                        subcategory.setSubcategory(subcategoryObject.getString(Subcategory.COLUMN_SUBCATEGORY));
                                        subcategory.setLabel(subcategoryObject.getString(Subcategory.COLUMN_LABEL));

                                        subcategoryRepository.createData(subcategory);
                                    }

                                    populateMenu(categoryRepository.retrieveData());
                                }
                                session.setSessionData(SessionManager.KEY_USER_LEARNED, true);
                            } else {
                                progress.cancel();
                                confirmRetry();
                            }
                        } catch (JSONException e) {
                            progress.cancel();
                            e.printStackTrace();
                        }
                        progress.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.cancel();
                        confirmRetry();
                    }
                }
        );
        menuRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(menuRequest);
    }

    /**
     * Decide to show which header and set according login status.
     */
    private void handleNavigationLayout() {
        ViewGroup headerSigned = (RelativeLayout) navigationHeader.findViewById(R.id.signed_header);
        ViewGroup headerUnsigned = (LinearLayout) navigationHeader.findViewById(R.id.unsigned_header);

        if (session.isLoggedIn()) {
            // Select signed as visible view
            headerSigned.setVisibility(View.VISIBLE);
            headerUnsigned.setVisibility(View.GONE);

            updateSideProfile(false);

            // Delegating create article event
            Button mCreateArticleButton = (Button) navigationHeader.findViewById(R.id.btn_save_article);
            mCreateArticleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (session.isLoggedIn()) {
                        Intent createArticleIntent = new Intent(ApplicationActivity.this, ArticleCreateActivity.class);
                        createArticleIntent.putExtra(ArticleCreateActivity.CALLED_FROM_MAIN, true);
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

    private void updateSideProfile(boolean skipCache){
        // Avatar image view delegating event and download image async
        ImageView mAvatarImage = (ImageView) navigationHeader.findViewById(R.id.avatar);
        mAvatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfile();
            }
        });
        if(skipCache){
            Glide.clear(mAvatarImage);
        }
        Glide.with(this).load(session.getSessionData(SessionManager.KEY_AVATAR, Constant.URL_AVATAR_DEFAULT))
                .placeholder(R.drawable.placeholder_square)
                .centerCrop()
                .dontAnimate()
                .into(mAvatarImage);

        // Cover image view delegating event and download image async with cross fade effect
        ImageView mCoverImage = (ImageView) navigationHeader.findViewById(R.id.cover);
        if(skipCache){
            Glide.clear(mCoverImage);
        }
        Glide.with(this).load(session.getSessionData(SessionManager.KEY_COVER, Constant.URL_COVER_DEFAULT))
                .centerCrop()
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

    private void confirmRetry() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
        builder.setTitle(R.string.action_retry);
        builder.setMessage(R.string.message_request_timeout);
        builder.setPositiveButton(R.string.action_retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                downloadCategoryMenu();
            }
        });
        builder.setNegativeButton(R.string.action_exit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        dialogConfirmation = builder.create();
        dialogConfirmation.show();
        AppHelper.dialogButtonTheme(this, dialogConfirmation);
    }

    /**
     * Show confirm dialog before exit.
     */
    private void confirmExit() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.message_exit_confirm);
        builder.setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(R.string.action_open_infogue, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.BASE_URL));
                startActivity(browserIntent);
            }
        });

        dialogConfirmation = builder.create();
        dialogConfirmation.show();
        AppHelper.dialogButtonTheme(this, dialogConfirmation);
    }

    /**
     * Show dialog confirmation before sign out.
     */
    private void confirmSignOut() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme_NoActionBar));
        builder.setTitle(R.string.action_sign_out);
        builder.setMessage(R.string.message_logout_confirm);
        builder.setPositiveButton(R.string.action_sign_out, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                signOutUser();
            }
        });
        builder.setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialogConfirmation = builder.create();
        dialogConfirmation.show();
        AppHelper.dialogButtonTheme(this, dialogConfirmation);
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
            View view = findViewById(R.id.container_body);
            final Snackbar snackbar;
            if (view != null) {
                snackbar = Snackbar.make(view, R.string.message_logout_failed, Snackbar.LENGTH_LONG);
                // noinspection deprecation
                snackbar.setActionTextColor(getResources().getColor(R.color.light));
                snackbar.setAction(R.string.action_retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                        signOutUser();
                    }
                }).show();
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundResource(R.color.color_danger);
            }
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
        profileIntent.putExtra(SessionManager.KEY_STATUS, session.getSessionData(SessionManager.KEY_STATUS, null));
        profileIntent.putExtra(SessionManager.KEY_ARTICLE, session.getSessionData(SessionManager.KEY_ARTICLE, 0));
        profileIntent.putExtra(SessionManager.KEY_FOLLOWER, session.getSessionData(SessionManager.KEY_FOLLOWER, 0));
        profileIntent.putExtra(SessionManager.KEY_FOLLOWING, session.getSessionData(SessionManager.KEY_FOLLOWING, 0));
        profileIntent.putExtra(SessionManager.KEY_IS_FOLLOWING, false);
        startActivity(profileIntent);
    }

    /**
     * Create category menu on navigation drawer.
     */
    private void populateMenu(List<Category> menu) {
        SubMenu navMenu = navigationView.getMenu().getItem(1).getSubMenu();
        navMenu.clear();
        for (int i = 0; i < menu.size(); i++) {
            Category category = menu.get(i);
            navMenu.add(1, category.getId(), Menu.CATEGORY_ALTERNATIVE, category.getCategory())
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
        if (drawer != null) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (dialogConfirmation != null && dialogConfirmation.isShowing()) {
                    dialogConfirmation.cancel();
                } else {
                    confirmExit();
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SettingsActivity.SETTING_RESULT_CODE) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                updateSideProfile(true);
            }
        }
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
                    Intent settingIntent = new Intent(getBaseContext(), SettingsActivity.class);
                    startActivityForResult(settingIntent, SettingsActivity.SETTING_RESULT_CODE);
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
            Intent postIntent = new Intent(getBaseContext(), PostActivity.class);
            postIntent.putExtra(Article.ARTICLE_ID, article.getId());
            postIntent.putExtra(Article.ARTICLE_SLUG, article.getSlug());
            postIntent.putExtra(Article.ARTICLE_FEATURED, article.getFeatured());
            postIntent.putExtra(Article.ARTICLE_TITLE, article.getTitle());
            startActivity(postIntent);
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
                        Intent postIntent = new Intent(getBaseContext(), PostActivity.class);
                        postIntent.putExtra(Article.ARTICLE_ID, article.getId());
                        postIntent.putExtra(Article.ARTICLE_SLUG, article.getSlug());
                        postIntent.putExtra(Article.ARTICLE_FEATURED, article.getFeatured());
                        postIntent.putExtra(Article.ARTICLE_TITLE, article.getTitle());
                        startActivity(postIntent);
                    } else if (id == R.id.action_browse) {
                        String articleUrl = UrlHelper.getArticleUrl(article.getSlug());
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                        startActivity(browserIntent);
                    } else if (id == R.id.action_share) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareArticleText(article.getSlug()));
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_intent_share)));
                    } else if (id == R.id.action_rate) {
                        givePerfectRating(article);
                    }
                    connectionDetector.dismissNotification();
                } else {
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
                getString(R.string.action_long_open),
                getString(R.string.action_long_browse),
                getString(R.string.action_long_share),
                getString(R.string.action_long_rate)
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (connectionDetector.isNetworkAvailable()) {
                    if (items[item].toString().equals(getString(R.string.action_long_open))) {
                        Intent postIntent = new Intent(getBaseContext(), PostActivity.class);
                        postIntent.putExtra(Article.ARTICLE_ID, article.getId());
                        postIntent.putExtra(Article.ARTICLE_SLUG, article.getSlug());
                        postIntent.putExtra(Article.ARTICLE_FEATURED, article.getFeatured());
                        postIntent.putExtra(Article.ARTICLE_TITLE, article.getTitle());
                        startActivity(postIntent);
                    } else if (items[item].toString().equals(getString(R.string.action_long_browse))) {
                        String articleUrl = UrlHelper.getArticleUrl(article.getSlug());
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl));
                        startActivity(browserIntent);
                    } else if (items[item].toString().equals(getString(R.string.action_long_share))) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, UrlHelper.getShareArticleText(article.getSlug()));
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.label_intent_share)));
                    } else if (items[item].toString().equals(getString(R.string.action_long_rate))) {
                        givePerfectRating(article);
                    }
                } else {
                    onLostConnectionNotified(getBaseContext());
                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void givePerfectRating(final Article article){
        StringRequest postRequest = new StringRequest(Request.Method.POST, Constant.URL_API_RATE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject result = new JSONObject(response);
                            String status = result.getString("status");
                            String message = result.getString("message");

                            if (status.equals(Constant.REQUEST_SUCCESS)) {
                                Log.i("Infogue/Rate", "Average rating for article id : " + article.getId() + " is " + message);
                            } else {
                                String errorMessage = getString(R.string.error_unknown) + "\r\nYour rating was discarded";
                                AppHelper.toastColored(getBaseContext(), errorMessage, Color.parseColor("#ddd1205e"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse networkResponse = error.networkResponse;
                        String errorMessage = getString(R.string.error_server);
                        if (networkResponse == null) {
                            if (error.getClass().equals(TimeoutError.class)) {
                                errorMessage = getString(R.string.error_timeout) + "\r\nYour rating was discarded";
                            } else {
                                errorMessage = getString(R.string.error_unknown);
                            }
                        } else {
                            try {
                                String result = new String(networkResponse.data);
                                JSONObject response = new JSONObject(result);
                                String status = response.getString("status");
                                String message = response.getString("message");

                                if (status.equals(Constant.REQUEST_FAILURE) && networkResponse.statusCode == 500) {
                                    errorMessage = message;
                                } else {
                                    errorMessage = getString(R.string.error_unknown);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        AppHelper.toastColored(getBaseContext(), errorMessage, Color.parseColor("#ddd1205e"));
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put(Article.ARTICLE_FOREIGN, String.valueOf(article.getId()));
                params.put(Article.ARTICLE_RATE, String.valueOf(5));
                return params;
            }
        };
        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(postRequest);

        AppHelper.toastColored(getBaseContext(), "Awesome!, you give 5 Stars on \n\r\"" + article.getTitle() + "\"", Color.parseColor("#ddd1205e"));
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
        String subtitle;
        int elevation;
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
                Object objectFragment = objectPooling.find("home");
                if (objectFragment == null) {
                    fragment = new HomeFragment();
                    objectPooling.pool(fragment, "home");
                } else {
                    fragment = (HomeFragment) objectFragment;
                }
                title = "";
                subtitle = "";
                elevation = 0;
                logo = true;
            } else if (id == R.id.nav_random) {
                Object objectFragment = objectPooling.find(ArticleFragment.FEATURED_RANDOM);
                if (objectFragment == null) {
                    fragment = ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_RANDOM);
                    objectPooling.pool(fragment, ArticleFragment.FEATURED_RANDOM);
                } else {
                    fragment = (ArticleFragment) objectFragment;
                }
                title = "Featured Article";
                subtitle = "Random";
                elevation = 2;
                logo = false;
            } else if (id == R.id.nav_headline) {
                Object objectFragment = objectPooling.find(ArticleFragment.FEATURED_HEADLINE);
                if (objectFragment == null) {
                    fragment = ArticleFragment.newInstanceFeatured(1, ArticleFragment.FEATURED_HEADLINE);
                    objectPooling.pool(fragment, ArticleFragment.FEATURED_HEADLINE);
                } else {
                    fragment = (ArticleFragment) objectFragment;
                }
                title = "Featured Article";
                subtitle = "Headline";
                elevation = 2;
                logo = false;
            } else {
                Object objectFragment = objectPooling.find(category);
                if (objectFragment == null) {
                    fragment = ArticleFragment.newInstanceCategory(1, id, AppHelper.createSlug(category));
                    objectPooling.pool(fragment, category);
                } else {
                    fragment = (ArticleFragment) objectFragment;
                }
                title = category;
                subtitle = "";
                elevation = 2;
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
                    if (!subtitle.trim().isEmpty()) {
                        actionBar.setSubtitle(subtitle);
                    } else {
                        actionBar.setSubtitle(null);
                    }
                    actionBar.setDisplayUseLogoEnabled(logo);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AppBarLayout appBarLayout = ((AppBarLayout) findViewById(R.id.appBar));
                    if (appBarLayout != null) {
                        appBarLayout.setElevation(elevation);
                    }
                }
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }

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
                    }, Constant.jokes[(int) Math.floor(Math.random() * Constant.jokes.length)] + " stole my internet T_T", getString(R.string.action_retry));
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

    @Override
    public void onConnectionEstablished(Context context) {
        connectionDetector.snackbarConnectedNotification(findViewById(android.R.id.content), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectionDetector.dismissNotification();
            }
        });
    }
}
