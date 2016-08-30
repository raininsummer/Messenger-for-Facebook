package me.rain.liteforfacebook;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.webkit.JavascriptInterface;

@SuppressWarnings("unused")
class JavaScriptInterfaces {
    private final MainActivity mContext;
    private final SharedPreferences mPreferences;

    // Instantiate the interface and set the context
    JavaScriptInterfaces(MainActivity c) {
        mContext = c;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(c);
    }

    @JavascriptInterface
    public void loadingCompleted() {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mContext.setLoading(false);
            }
        });
    }

    @JavascriptInterface
    public void getCurrent(final String value) {
        if (value.equals("null")) {
            return;
        }
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (value) {
                    case "top_stories":
                        if (mPreferences.getBoolean(SettingsActivity.KEY_PREF_MOST_RECENT_MENU, true)) {
                            mContext.mNavigationView.setCheckedItem(R.id.nav_top_stories);
                        } else {
                            mContext.mNavigationView.setCheckedItem(R.id.nav_news);
                        }
                        break;
                    case "most_recent":
                        if (mPreferences.getBoolean(SettingsActivity.KEY_PREF_MOST_RECENT_MENU, true)) {
                            mContext.mNavigationView.setCheckedItem(R.id.nav_most_recent);
                        } else {
                            mContext.mNavigationView.setCheckedItem(R.id.nav_news);
                        }
                        break;
                    case "requests_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_friendreq);
                        break;
                    case "messages_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_messages);
                        break;
                    case "messages_request_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_messages_requests);
                        break;
                    case "notifications_jewel":
                        Helpers.uncheckRadioMenu(mContext.mNavigationView.getMenu());
                        break;
                    case "search_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_search);
                        break;
                    case "bookmarks_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_mainmenu);
                        break;
                    case "me_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_me);
                        break;
                    case "photo_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_photo);
                        break;
                    case "trending_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_trending);
                        break;
                    case "online_friend_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_online_friend);
                        break;
                    case "groups_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_group);
                        break;
                    case "page_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_page);
                        break;
                    case "events_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_events);
                        break;
                    case "on_this_day_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_on_this_day);
                        break;
                    case "saved_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_save);
                        break;
                    case "notes_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_notes);
                        break;
                    case "pokes_jewel":
                        mContext.mNavigationView.setCheckedItem(R.id.nav_pokes);
                        break;
                    default:
                        Helpers.uncheckRadioMenu(mContext.mNavigationView.getMenu());
                        break;
                }
            }
        });
    }

    @JavascriptInterface
    public void getNums(final String notifications, final String messages, final String requests) {
        final int notifications_int = Helpers.isInteger(notifications) ? Integer.parseInt(notifications) : 0;
        final int messages_int = Helpers.isInteger(messages) ? Integer.parseInt(messages) : 0;
        final int requests_int = Helpers.isInteger(requests) ? Integer.parseInt(requests) : 0;
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mContext.setNotificationNum(notifications_int);
                mContext.setMessagesNum(messages_int);
                mContext.setRequestsNum(requests_int);
            }
        });
    }
}
