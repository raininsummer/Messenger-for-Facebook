package me.rain.liteforfacebook;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.greysonparrelli.permiso.Permiso;
import com.mikepenz.actionitembadge.library.ActionItemBadge;
import com.mikepenz.actionitembadge.library.utils.BadgeStyle;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import im.delight.android.webview.AdvancedWebView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, BillingProcessor.IBillingHandler {
    static final String FACEBOOK_URL_BASE = "https://m.facebook.com/";
    private static final String FACEBOOK_URL_BASE_ENCODED = "https%3A%2F%2Fm.facebook.com%2F";
    //    private static final String FACEBOOK_URL_BASE_ENCODED_R = "https%3A%2F%2Ffacebook.com%2F";
    private static final List<String> HOSTNAMES = Arrays.asList("facebook.com", "*.facebook.com", "*.fbcdn.net", "*.akamaihd.net");
    private final BadgeStyle BADGE_SIDE_FULL = new BadgeStyle(BadgeStyle.Style.LARGE, R.layout.menu_badge_full, R.color.colorAccent, R.color.colorAccent, Color.WHITE);

    // Members
    SwipeRefreshLayout swipeView;
    NavigationView mNavigationView;
    View mCoordinatorLayoutView;
    private FloatingActionMenu mMenuFAB;
    private AdvancedWebView mWebView;
    BillingProcessor bp;

    private final View.OnClickListener mFABClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.textNewsFeed:
                    mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23feed_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "home.php'%7D%7D)()");
                    break;
                case R.id.textFAB:
                    mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('button%5Bname%3D%22view_overview%22%5D').click()%7Dcatch(_)%7Bwindow.location.href%3D%22" + FACEBOOK_URL_BASE_ENCODED + "%3Fpageload%3Dcomposer%22%7D%7D)()");
                    break;
                case R.id.photoFAB:
                    mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('button%5Bname%3D%22view_photo%22%5D').click()%7Dcatch(_)%7Bwindow.location.href%3D%22" + FACEBOOK_URL_BASE_ENCODED + "%3Fpageload%3Dcomposer_photo%22%7D%7D)()");
                    break;
                case R.id.checkinFAB:
                    mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('button%5Bname%3D%22view_location%22%5D').click()%7Dcatch(_)%7Bwindow.location.href%3D%22" + FACEBOOK_URL_BASE_ENCODED + "%3Fpageload%3Dcomposer_checkin%22%7D%7D)()");
                    break;
                default:
                    break;
            }
            mMenuFAB.close(true);
        }
    };
    private MenuItem mNotificationButton;
    private MenuItem mMessageButton;
    private CallbackManager callbackManager;
    private Snackbar loginSnackbar = null;
    @SuppressWarnings("FieldCanBeLocal") // Will be garbage collected as a local variable
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private boolean requiresReload = false;
    private String mUserLink = null;
    private SharedPreferences mPreferences;
    private ImageView profileImage;
    private ImageView profileCover;
    private TextView profileName;
    private AdView adView;
    private String PRODUCT_ID = "removeads";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_main);
        Permiso.getInstance().setActivity(this);

//        PackageInfo info;
//        try {
//            info = getPackageManager().getPackageInfo("me.rain.liteforfacebook", PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md;
//                md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                String something = new String(Base64.encode(md.digest(), 0));
//                //String something = new String(Base64.encodeBytes(md.digest()));
//                Log.e("hash key", something);
//                Snackbar.make(mCoordinatorLayoutView, something, Snackbar.LENGTH_LONG).show();
//            }
//        } catch (PackageManager.NameNotFoundException e1) {
//            Log.e("name not found", e1.toString());
//        } catch (NoSuchAlgorithmException e) {
//            Log.e("no such an algorithm", e.toString());
//        } catch (Exception e) {
//            Log.e("exception", e.toString());
//        }

        // Preferences
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                switch (key) {
                    case SettingsActivity.KEY_PREF_JUMP_TOP_BUTTON:
                        mNavigationView.getMenu().findItem(R.id.nav_jump_top).setVisible(prefs.getBoolean(key, false));
                        break;
                    case SettingsActivity.KEY_PREF_STOP_IMAGES:
                        mWebView.getSettings().setBlockNetworkImage(prefs.getBoolean(key, false));
                        requiresReload = true;
                        break;
                    case SettingsActivity.KEY_PREF_BACK_BUTTON:
                        mNavigationView.getMenu().findItem(R.id.nav_back).setVisible(prefs.getBoolean(key, false));
                        break;
                    case SettingsActivity.KEY_PREF_MESSAGING:
                        mNavigationView.getMenu().findItem(R.id.nav_messages).setVisible(prefs.getBoolean(key, false));
                        break;
                    case SettingsActivity.KEY_PREF_LOCATION:
                        if (prefs.getBoolean(key, false)) {
                            Permiso.getInstance().requestPermissions(new Permiso.IOnPermissionResult() {
                                @Override
                                public void onPermissionResult(Permiso.ResultSet resultSet) {
                                    if (resultSet.areAllPermissionsGranted()) {
                                        mWebView.setGeolocationEnabled(true);
                                    } else {
                                        Snackbar.make(mCoordinatorLayoutView, R.string.permission_denied, Snackbar.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onRationaleRequested(Permiso.IOnRationaleProvided callback, String... permissions) {
                                    // TODO Permiso.getInstance().showRationaleInDialog("Title", "Message", null, callback);
                                    callback.onRationaleProvided();
                                }
                            }, Manifest.permission.ACCESS_FINE_LOCATION);
                        }
                        break;
                    case SettingsActivity.KEY_PREF_MOST_RECENT_MENU:
                        boolean most_recent = prefs.getBoolean(key, true);
                        mNavigationView.getMenu().findItem(R.id.nav_news).setVisible(!most_recent);
                        mNavigationView.getMenu().findItem(R.id.nav_top_stories).setVisible(most_recent);
                        mNavigationView.getMenu().findItem(R.id.nav_most_recent).setVisible(most_recent);
                        requiresReload = true;
                        break;
                    case SettingsActivity.KEY_PREF_FAB_SCROLL:
                        if (!mPreferences.getBoolean(SettingsActivity.KEY_PREF_FAB_BTN, false)) {
                            mMenuFAB.showMenuButton(true);
                        } else {
                            mMenuFAB.hideMenuButton(true);
                        }
                        break;
                    case SettingsActivity.KEY_PREF_FAB_BTN:
                        mMenuFAB.hideMenuButton(true);
                        break;
                    case SettingsActivity.KEY_PREF_HIDE_EDITOR:
                        requiresReload = true;
                        break;
                    case SettingsActivity.KEY_PREF_HIDE_SPONSORED:
                        requiresReload = true;
                        break;
                    case SettingsActivity.KEY_PREF_HIDE_BIRTHDAYS:
                        requiresReload = true;
                        break;
                    case SettingsActivity.KEY_PREF_NOTIFICATIONS_ENABLED:
                        PollReceiver.scheduleAlarms(getApplicationContext(), false);
                        break;
                    case SettingsActivity.KEY_PREF_NOTIFICATION_INTERVAL:
                        PollReceiver.scheduleAlarms(getApplicationContext(), false);
                        break;
                    default:
                        break;
                }
            }
        };
        mPreferences.registerOnSharedPreferenceChangeListener(listener);

        // Setup the toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // Setup the DrawLayout
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // Create the badge for messages
        ActionItemBadge.update(this, mNavigationView.getMenu().findItem(R.id.nav_messages), (Drawable) null, BADGE_SIDE_FULL, Integer.MIN_VALUE);
        ActionItemBadge.update(this, mNavigationView.getMenu().findItem(R.id.nav_friendreq), (Drawable) null, BADGE_SIDE_FULL, Integer.MIN_VALUE);

        // Hide buttons if they are disabled
        if (!mPreferences.getBoolean(SettingsActivity.KEY_PREF_MESSAGING, false)) {
            mNavigationView.getMenu().findItem(R.id.nav_messages).setVisible(false);
        }
        if (!mPreferences.getBoolean(SettingsActivity.KEY_PREF_JUMP_TOP_BUTTON, false)) {
            mNavigationView.getMenu().findItem(R.id.nav_jump_top).setVisible(false);
        }
        if (!mPreferences.getBoolean(SettingsActivity.KEY_PREF_BACK_BUTTON, false)) {
            mNavigationView.getMenu().findItem(R.id.nav_back).setVisible(false);
        }
        boolean most_recent = mPreferences.getBoolean(SettingsActivity.KEY_PREF_MOST_RECENT_MENU, true);
        mNavigationView.getMenu().findItem(R.id.nav_news).setVisible(!most_recent);
        mNavigationView.getMenu().findItem(R.id.nav_top_stories).setVisible(most_recent);
        mNavigationView.getMenu().findItem(R.id.nav_most_recent).setVisible(most_recent);

        // Bind the Coordinator to member
        mCoordinatorLayoutView = findViewById(R.id.coordinatorLayout);

        // Start the Swipe to reload listener
        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        swipeView.setColorSchemeResources(R.color.colorPrimary);
        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mWebView.reload();
            }
        });

        // Inflate the FAB menu
        mMenuFAB = (FloatingActionMenu) findViewById(R.id.menuFAB);
        // Nasty hack to get the FAB menu button
        mMenuFAB.getChildAt(4).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mMenuFAB.hideMenu(true);
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Show your View after 3 seconds
                        mMenuFAB.showMenu(true);
                    }
                }, 3000);
                return false;
            }
        });
        findViewById(R.id.textNewsFeed).setOnClickListener(mFABClickListener);
        findViewById(R.id.textFAB).setOnClickListener(mFABClickListener);
        findViewById(R.id.photoFAB).setOnClickListener(mFABClickListener);
        findViewById(R.id.checkinFAB).setOnClickListener(mFABClickListener);

        adView = (AdView) findViewById(R.id.fragment_main_adview);
        // Load the WebView
        mWebView = (AdvancedWebView) findViewById(R.id.webview);
        mWebView.addPermittedHostnames(HOSTNAMES);
        mWebView.setGeolocationEnabled(mPreferences.getBoolean(SettingsActivity.KEY_PREF_LOCATION, false));

        mWebView.setListener(this, new WebViewListener(this, mWebView));
        mWebView.addJavascriptInterface(new JavaScriptInterfaces(this), "android");
        registerForContextMenu(mWebView);

        mWebView.getSettings().setBlockNetworkImage(mPreferences.getBoolean(SettingsActivity.KEY_PREF_STOP_IMAGES, false));
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        // Impersonate iPhone to prevent advertising garbage
        mWebView.getSettings().setUserAgentString("Mozilla/5.0 (BB10; Touch) AppleWebKit/537.1+ (KHTML, like Gecko) Version/10.0.0.1337 Mobile Safari/537.1+");

        // Long press
        registerForContextMenu(mWebView);
        mWebView.setLongClickable(true);
        mWebView.setWebChromeClient(new CustomWebChromeClient(this, mWebView, (FrameLayout) findViewById(R.id.fullscreen_custom_content)));

        // Add OnClick listener to Profile picture
        profileCover = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_cover);
        profileName = (TextView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_name);

        profileImage = (ImageView) mNavigationView.getHeaderView(0).findViewById(R.id.profile_picture);
        profileImage.setClickable(true);
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserLink != null) {
                    drawer.closeDrawers();
                    mWebView.loadUrl(mUserLink);
                }
            }
        });


        callbackManager = CallbackManager.Factory.create();

        FacebookCallback<LoginResult> loginResult = new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                mWebView.loadUrl(chooseUrl());
                updateUserInfo();
            }

            @Override
            public void onCancel() {
                checkLoggedInState();
            }

            @Override
            public void onError(FacebookException error) {
                Snackbar.make(mCoordinatorLayoutView, R.string.error_login, Snackbar.LENGTH_LONG).show();
                Log.e(Helpers.LogTag, error.toString());
                LoginManager.getInstance().logOut();
                checkLoggedInState();
            }
        };

        LoginManager.getInstance().setLoginBehavior(LoginBehavior.WEB_ONLY);
        LoginManager.getInstance().registerCallback(callbackManager, loginResult);

        if (checkLoggedInState()) {
            mWebView.loadUrl(chooseUrl());
            updateUserInfo();
        }
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(getString(R.string.admob_test_device_id))
                .build();
        adView.loadAd(adRequest);
        updateView();

        bp = new BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApwZHWZCnrQD3dCySU6+BcJ/HRMqYWxy3senidFz9Qvge2c+tMn5XgslngPcRUlsp6HMQv9RfygW86zCcB683TcOfG4voAfWEKO0FB040uDxaR/l6YJ8f8oxeIuBIExCnDg4FQfEkFcgUaLstfui6I3ea/vgS58iAaSN2CzGeZ5+UZGEYP2yIX8rWkhDf8I2n7r9+8hHK/u/WXKq+VlHbX7wUiSdy9Xs0j82NZOr6koWJ3q69GQz7k4CBjlz9vAmWTSdG5sTIvF3N5HUYBj/3G1AEj5wiVl70GGuNumhjrE5xQxo/RaIgroja79dOU8HEvZ6elCl6M7WjIEVpq2wNeQIDAQAB", this);
    }

    private void updateView() {
        // Hide Ads
        if (mPreferences.getBoolean(SettingsActivity.KEY_PREF_ADS, false)) {
            adView.setVisibility(View.GONE);
        } else {
            adView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
        Permiso.getInstance().setActivity(this);

        // Check if we need to show a page reload snackbar
        if (requiresReload) {
            Snackbar reloadSnackbar = Snackbar.make(mCoordinatorLayoutView, R.string.hide_editor_newsfeed_snackbar, Snackbar.LENGTH_LONG);
            reloadSnackbar.setAction(R.string.menu_refresh, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWebView.reload();
                }
            });
            reloadSnackbar.show();
            requiresReload = false;
        }
        registerForContextMenu(mWebView);
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        if (bp != null)
            bp.release();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
//        super.onActivityResult(requestCode, resultCode, data);
        mWebView.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Permiso.getInstance().onRequestPermissionResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        mNotificationButton = menu.findItem(R.id.action_notifications);
        mMessageButton = menu.findItem(R.id.action_message);

        ActionItemBadge.update(this, mNotificationButton, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu_notifications, null), ActionItemBadge.BadgeStyles.RED, Integer.MIN_VALUE);
        ActionItemBadge.update(this, mMessageButton, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu_messages, null), ActionItemBadge.BadgeStyles.RED, Integer.MIN_VALUE);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();
        if (id == R.id.action_notifications) {
            // Load the notification page
            mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23notifications_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "notifications.php'%7D%7D)()");
            Helpers.uncheckRadioMenu(mNavigationView.getMenu());
        } else if (id == R.id.action_message) {
            mWebView.loadUrl("https://m.facebook.com/messages/?more");
        }

        // Update the notifications
        JavaScriptHelpers.updateNums(mWebView);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_news:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23feed_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "home.php'%7D%7D)()");
                item.setChecked(true);
            case R.id.nav_top_stories:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('a%5Bhref*%3D%22%2Fhome.php%3Fsk%3Dh_nor%22%5D').click()%7Dcatch(_)%7Bwindow.location.href%3D%22" + FACEBOOK_URL_BASE_ENCODED + "home.php%3Fsk%3Dh_nor%22%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_most_recent:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('a%5Bhref*%3D%22%2Fhome.php%3Fsk%3Dh_chr%22%5D').click()%7Dcatch(_)%7Bwindow.location.href%3D%22" + FACEBOOK_URL_BASE_ENCODED + "home.php%3Fsk%3Dh_chr%22%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_me:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23me_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "me%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_friendreq:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23requests_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "friends%2Fcenter%2Frequests%2F'%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_messages:
                mWebView.loadUrl("https://m.facebook.com/messages/?more");
                JavaScriptHelpers.updateNums(mWebView);
                item.setChecked(true);
                break;
            case R.id.nav_messages_requests:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23messages_request_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "messages%2F%3Ffolder%3Dpending'%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_search:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23search_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "search%2F'%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_mainmenu:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23bookmarks_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "home.php'%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_photo:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23photo_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "profile.php%3Fv%3Dphotos%26soft%3Dcomposer%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_trending:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23trending_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "search%2Ftrending-news%2F%3Fref%3Dbookmark%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_online_friend:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23online_friend_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "buddylist.php%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_group:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23groups_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "groups%2F%3Fcategory%3Dmembership%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_page:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23page_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "pages%2Flaunchpoint%2F%3Ffrom%3Dpages_nav_discover%26ref%3Dbookmarks%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_events:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23events_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "events%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_on_this_day:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23on_this_day_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "onthisday%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_save:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23saved_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "saved%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_notes:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23notes_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "notes%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_pokes:
                mWebView.loadUrl("javascript:(function()%7Btry%7Bdocument.querySelector('%23pokes_jewel%20%3E%20a').click()%7Dcatch(_)%7Bwindow.location.href%3D'" + FACEBOOK_URL_BASE_ENCODED + "pokes%27%7D%7D)()");
                item.setChecked(true);
                break;
            case R.id.nav_fblogin:
                LoginManager.getInstance().logInWithReadPermissions(this, Helpers.FB_PERMISSIONS);
                break;
            case R.id.nav_jump_top:
                mWebView.scrollTo(0, 0);
                break;
            case R.id.nav_back:
                mWebView.goBack();
                break;
            case R.id.nav_reload:
                mWebView.reload();
                break;
            case R.id.nav_forward:
                mWebView.goForward();
                break;
            case R.id.nav_rate:
                rateApp();
                break;
            case R.id.nav_donate:
                //TOdo
                bp.purchase(this, PRODUCT_ID);
//                mPreferences.edit().putBoolean(SettingsActivity.KEY_PREF_ADS, true).apply();
                break;
            case R.id.nav_share:
                shareApp();
                break;
            case R.id.nav_settings:
                Intent settingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsActivity);
                break;
            default:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void rateApp() {
        Uri uri = Uri.parse("market://details?id=com.rain.liteforfacebook");
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=com.rain.liteforfacebook")));
        }
    }

    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "http://play.google.com/store/apps/details?id=com.rain.liteforfacebook");
        startActivity(Intent.createChooser(shareIntent, "Share App"));
    }

    public void setLoading(boolean loading) {
        // Toggle the WebView and Spinner visibility
//        mWebView.setVisibility(loading ? View.GONE : View.VISIBLE);
        swipeView.setRefreshing(loading);
    }

    public boolean checkLoggedInState() {
        if (loginSnackbar != null) {
            loginSnackbar.dismiss();
        }

        if (AccessToken.getCurrentAccessToken() != null && Helpers.getCookie() != null) {
            // Logged in, show webview
            mWebView.setVisibility(View.VISIBLE);

            // Hide login button
            mNavigationView.getMenu().findItem(R.id.nav_fblogin).setVisible(false);

            // Enable navigation buttons
            mNavigationView.getMenu().setGroupEnabled(R.id.group_fbnav, true);
            mNavigationView.getMenu().setGroupEnabled(R.id.group_fbnav_1, true);
            mNavigationView.getMenu().setGroupEnabled(R.id.group_fbnav_2, true);

            // Start the Notification service (if not already running)
            PollReceiver.scheduleAlarms(getApplicationContext(), false);
            return true;
        } else {
            // Not logged in (possibly logged into Facebook OAuth and/or webapp)
            loginSnackbar = Helpers.loginPrompt(mCoordinatorLayoutView);
            setLoading(false);
            mWebView.setVisibility(View.GONE);

            // Show login button
            mNavigationView.getMenu().findItem(R.id.nav_fblogin).setVisible(true);

            // Disable navigation buttons
            mNavigationView.getMenu().setGroupEnabled(R.id.group_fbnav, false);
            mNavigationView.getMenu().setGroupEnabled(R.id.group_fbnav_1, false);
            mNavigationView.getMenu().setGroupEnabled(R.id.group_fbnav_2, false);

            // Cancel the Notification service if we are logged out
            PollReceiver.scheduleAlarms(getApplicationContext(), true);

            // Kill the Feed URL, so we don't get the wrong notifications
            mPreferences.edit().putString("feed_uri", null).apply();
            return false;
        }
    }

    private void updateUserInfo() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                // Update header
                try {
                    String userID = object.getString("id");
                    mUserLink = object.getString("link");


//                    final View header = findViewById(R.id.header_layout);
                    // Set the user's name under the header
                    profileName.setText(object.optString("name"));

                    // Set the cover photo with resizing
                    Picasso.with(getApplicationContext()).load("https://graph.facebook.com/" + userID + "/picture?type=large").into(profileImage);

                    Picasso.with(getApplicationContext()).load(object.optJSONObject("cover").optString("source")).error(R.drawable.side_nav_bar).into(profileCover);

//                    Picasso.with(getApplicationContext()).load(object.getJSONObject("cover").getString("source")).resize(header.getWidth(), header.getHeight()).centerCrop().error(R.drawable.side_nav_bar).into(new Target() {
//                        @Override
//                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                            Log.v(Helpers.LogTag, "Set cover photo");
//                            header.setBackground(new BitmapDrawable(getResources(), bitmap));
//                        }
//
//                        @Override
//                        public void onBitmapFailed(Drawable errorDrawable) {
//                        }
//
//                        @Override
//                        public void onPrepareLoad(Drawable placeHolderDrawable) {
//                        }
//                    });
                } catch (NullPointerException e) {
                    Snackbar.make(mCoordinatorLayoutView, R.string.error_facebook_noconnection, Snackbar.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Snackbar.make(mCoordinatorLayoutView, R.string.error_facebook_error, Snackbar.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Snackbar.make(mCoordinatorLayoutView, R.string.error_super_wrong, Snackbar.LENGTH_LONG).show();
                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,cover,link");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void setNotificationNum(int num) {
        if (num > 0) {
            ActionItemBadge.update(mNotificationButton, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu_notifications_active, null), num);
//            sendNotification(num, 0);
        } else {
            // Hide the badge and show the washed-out button
            ActionItemBadge.update(mNotificationButton, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu_notifications, null), Integer.MIN_VALUE);
        }
    }

    public void setMessagesNum(int num) {
        // Only update message count if enabled
        if (mPreferences.getBoolean(SettingsActivity.KEY_PREF_MESSAGING, false)) {
            if (num > 0) {
                ActionItemBadge.update(mNavigationView.getMenu().findItem(R.id.nav_messages), num);
                ActionItemBadge.update(mMessageButton, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu_messages, null), num);
//                sendNotification(num, 1);
            } else {
                // Hide the badge and show the washed-out button
                ActionItemBadge.update(mNavigationView.getMenu().findItem(R.id.nav_messages), Integer.MIN_VALUE);
                ActionItemBadge.update(mMessageButton, ResourcesCompat.getDrawable(getResources(), R.drawable.ic_menu_messages, null), Integer.MIN_VALUE);
            }
        }
    }

    public void setRequestsNum(int num) {
        if (num > 0) {
            ActionItemBadge.update(mNavigationView.getMenu().findItem(R.id.nav_friendreq), num);
//            sendNotification(num, 2);
        } else {
            // Hide the badge and show the washed-out button
            ActionItemBadge.update(mNavigationView.getMenu().findItem(R.id.nav_friendreq), Integer.MIN_VALUE);
        }
    }

//    private void sendNotification(int num, int type) {
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        if (!preferences.getBoolean(SettingsActivity.KEY_PREF_NOTIFICATIONS_ENABLED, false)) {
//            return;
//        }
//        NotificationCompat.Builder mBuilder =
//                new NotificationCompat.Builder(this)
//                        .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
//                        .setSmallIcon(R.mipmap.ic_launcher)
//                        .setContentTitle(getString(R.string.app_name))
//                        .setAutoCancel(true)
//                        .setDefaults(-1);
//
//        // Intent depends on context
//        Intent resultIntent;
//
//        // If there are multiple notifications, mention the number
////        String text = getString(R.string.notification_multiple_text, num);
//
//        String text = num + (type == 0 ? " New Notifications" : type == 1 ? " New Messages" : " New Friend Requests");
//        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text)).setContentText(text);
//
//        // Set the url to the notification centre
//        resultIntent = new Intent(this, MainActivity.class);
//        resultIntent.setAction(Intent.ACTION_VIEW);
//        resultIntent.setData(Uri.parse(MainActivity.FACEBOOK_URL_BASE + (type == 0 ? "notifications/" : type == 1 ? "messages/" : "friends/center/requests/")));
//
//
//        // Notification Priority (make LED blink)
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            mBuilder.setPriority(Notification.PRIORITY_HIGH);
//        }
//
//        // Vibration
//        if (mPreferences.getBoolean(SettingsActivity.KEY_PREF_NOTIFICATION_VIBRATE, true)) {
//            mBuilder.setVibrate(new long[]{500, 500});
//        } else {
//            mBuilder.setVibrate(null);
//        }
//
//        // Create TaskStack to ensure correct back button behaviour
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(MainActivity.class);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//        mBuilder.setContentIntent(resultPendingIntent);
//
//        // Build the notification
//        Notification notification = mBuilder.build();
//
//        // Set the LED colour
//        notification.ledARGB = ContextCompat.getColor(this, R.color.colorPrimary);
//
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        // notifyID allows you to update the notification later on.
//        mNotificationManager.notify(type, notification);
//
//        Log.i(Helpers.LogTag, "Notification posted");
//    }

    private String chooseUrl() {
        // Handle intents
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (URLUtil.isValidUrl(intent.getStringExtra(Intent.EXTRA_TEXT))) {
                try {
                    Log.v(Helpers.LogTag, "Shared URL Intent");
                    return "https://mbasic.facebook.com/composer/?text=" + URLEncoder.encode(intent.getStringExtra(Intent.EXTRA_TEXT), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else if (Intent.ACTION_VIEW.equals(action) && intent.getData() != null && URLUtil.isValidUrl(intent.getData().toString())) {
            // If there is a intent containing a facebook link, go there
            Log.v(Helpers.LogTag, "Opened URL Intent");
            return intent.getData().toString();
        }

        // If nothing has happened at this point, we want the default url
        return FACEBOOK_URL_BASE;
    }

    @Override
    public void onProductPurchased(String productId, TransactionDetails details) {
        mPreferences.edit().putBoolean(SettingsActivity.KEY_PREF_ADS, true).apply();
        updateView();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        if (bp.isPurchased(PRODUCT_ID)) {
            mPreferences.edit().putBoolean(SettingsActivity.KEY_PREF_ADS, true).apply();
            updateView();
        }
    }

    @Override
    public void onBillingError(int errorCode, Throwable error) {
        showToast("Billing Error");
    }

    @Override
    public void onBillingInitialized() {

    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
