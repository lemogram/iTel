/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.telegram.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.calling.PlaceCallActivity;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.AnimatorListenerAdapterProxy;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DialogObject;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.query.DraftQuery;
import org.telegram.messenger.query.SearchQuery;
import org.telegram.messenger.support.widget.LinearLayoutManager;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.MenuDrawable;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.DialogsAdapter;
import org.telegram.ui.Adapters.DialogsSearchAdapter;
import org.telegram.ui.Analytics.AnalyticsEventUtil;
import org.telegram.ui.Cells.DialogCell;
import org.telegram.ui.Cells.HintDialogCell;
import org.telegram.ui.Cells.ProfileSearchCell;
import org.telegram.ui.Cells.UserCell;
import org.telegram.ui.Components.EmptyTextProgressView;
import org.telegram.ui.Components.Favourite;
import org.telegram.ui.Components.Glow;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PlayerView;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Telehgram.SortTabs;
import org.telegram.videocall.VCPlaceCallActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import ir.tapsell.sdk.Tapsell;
import ir.tapsell.sdk.TapsellAd;
import ir.tapsell.sdk.TapsellAdRequestListener;
import ir.tapsell.sdk.TapsellAdRequestOptions;
import ir.tapsell.sdk.TapsellAdShowListener;
import ir.tapsell.sdk.TapsellConfiguration;
import ir.tapsell.sdk.TapsellRewardListener;
import ir.tapsell.sdk.TapsellShowOptions;

//import ir.adad.client.Adad;
//import ir.adad.client.InterstitialAdListener;

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private static String TAG = "AdTAG";
    com.android.volley.toolbox.ImageLoader imageLoader = ApplicationLoader.getInstance().getImageLoader();

    private static final int ghostMode = 3;
    private static final int lastSeen = 2;
    private static final int MIN_DISTANCE_HIGH = 40;
    private static final int MIN_DISTANCE_HIGH_Y = 60;
    private static boolean dialogsLoaded;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();
    SharedPreferences preferences;
    private Context mContext;
    private int h = AndroidUtilities.dp((float) 40);
    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private DialogsAdapter dialogsAdapter;
    private DialogsAdapter dialogsBackupAdapter;
    private DialogsSearchAdapter dialogsSearchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private ProgressBar progressView;
    private LinearLayout emptyView;
    private ActionBarMenuItem passcodeItem;
    private ImageView floatingButton;
    //  private ImageView floatingButton2;
    private boolean changedTabPosition;
    private AlertDialog permissionDialog;
    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    private boolean floatingHidden;
    private boolean checkPermission = true;
    private String selectAlertString;
    private String selectAlertStringGroup;
    private String addToGroupAlertString;
    private int dialogsType = 0;
    private boolean searching;
    private boolean searchWas;
    private boolean onlySelect;
    private long selectedDialog;
    private String searchString;
    private long openedDialogId;
    private DialogsActivityDelegate delegate;
    private LinearLayout tabsLayout;
    private LinearLayout adsLayout;
    private FrameLayout tabsView;
    private FrameLayout adsView;
    private ImageView usersTab;
    private ImageView allTab;
    private ImageView groupsTab;
    private ImageView superGroupsTab;
    private ImageView channelsTab;
    private ImageView botsTab;
    private ImageView favsTab;
    private ActionBarMenuItem ghostItem;
    private boolean hideTabs;
    private DisplayMetrics displayMetrics;
    private float downX;
    private float downY;
    private float upX;
    private float upY;
    private float vDPI;
    private float touchPositionDP;
    private int tabsHeight = 40;
    private boolean tabsHidden;
    private boolean disableAnimation;
    private TextView allCounter;
    private TextView botsCounter;
    private TextView channelsCounter;
    private TextView favsCounter;
    private TextView groupsCounter;
    private TextView sGroupsCounter;
    private TextView usersCounter;
    private int show_ads = 0;

    TapsellAd ad;
    private boolean showCompleteDialog = false;
    private boolean rewarded = false;
    private boolean completed = false;

    public DialogsActivity(Bundle args) {
        super(args);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void changeGhostModeState() {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int iconColor = themePrefs.getInt("chatsHeaderIconsColor", 0xffffffff);

        SharedPreferences.Editor editor = preferences.edit();
        boolean ghostMode;
        if (!preferences.getBoolean("ghost_mode", false)) {
            ghostMode = true;
            Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("GhostModeActivated", R.string.GhostModeActivated), Toast.LENGTH_LONG).show();
        } else {
            ghostMode = false;
            Toast.makeText(ApplicationLoader.applicationContext, LocaleController.getString("GhostModeDeactivated", R.string.GhostModeDeactivated), Toast.LENGTH_LONG).show();
        }
        editor.putBoolean("ghost_mode", ghostMode);
        editor.commit();

        actionBar.changeGhostModeVisibility();
        MessagesController.getInstance().reRunUpdateTimerProc();

        if (preferences.getBoolean("show_ghost_state_icon", true) == true) {
            if (ghostItem != null) {
                ActionBarMenuItem actionBarMenuItem = ghostItem;
                if (preferences.getBoolean("ghost_mode", false) == true) {
                    Drawable ic_ghost = getParentActivity().getResources().getDrawable(R.drawable.ic_ghost);
                    if (ic_ghost != null)
                        ic_ghost.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
                    actionBarMenuItem.setIcon(ic_ghost);
                } else {
                    Drawable ic_ghost_disable = getParentActivity().getResources().getDrawable(R.drawable.ic_ghost_disable);
                    if (ic_ghost_disable != null)
                        ic_ghost_disable.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
                    actionBarMenuItem.setIcon(ic_ghost_disable);
                }
            }
        } else {
            ghostItem.setVisibility(View.GONE);
        }
    }

    private void loadAd(String zoneId, final Boolean iwant) {
        TapsellAdRequestOptions options = new TapsellAdRequestOptions(TapsellAdRequestOptions.CACHE_TYPE_CACHED);

        Tapsell.requestAd(ApplicationLoader.applicationContext, zoneId, options, new TapsellAdRequestListener() {
            @Override
            public void onError(String error) {
                Log.d("Tapsell Sample","Error: "+error);
            }

            @Override
            public void onAdAvailable(TapsellAd ad) {

                DialogsActivity.this.ad = ad;
                //showAddBtn.setEnabled(true);
                Log.d("Tapsell Sample","Ad is available");
                if(iwant){
                    //Toast.makeText(LaunchActivity.this, "ویدئوی حمایتی آماده نمایش است", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNoAdAvailable() {
                Log.d("Tapsell Sample","No ad available");

            }

            @Override
            public void onNoNetwork() {
                Log.d("Tapsell Sample","No network");
                //Toast.makeText(LaunchActivity.this, "دستگاه به اینترنت متصل نیست", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onExpiring(TapsellAd ad) {
                //showAddBtn.setEnabled(false);
                loadAd("597dcb8546846521dae4a29e", false);
            }
        });

    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);


        Tapsell.initialize(ApplicationLoader.applicationContext, "beqctioekalmdofhleabtgokaekordinmjkipsfltseamatnnnqklhgsnrkisiprkkpjal");

        Tapsell.setRewardListener(new TapsellRewardListener() {
            @Override
            public void onAdShowFinished(TapsellAd ad, boolean completed) {
                Log.e("MainActivity","isCompleted? "+completed+ ", ad was rewarded?" + (ad!=null && ad.isRewardedAd()));
                showCompleteDialog = true;
                DialogsActivity.this.completed=completed;
                DialogsActivity.this.rewarded=(ad!=null && ad.isRewardedAd());

                if(completed){
                    /*final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("showRewAd", Activity.MODE_PRIVATE);
                    final Long tomorrow = System.currentTimeMillis() + 3600 * 1000 * 24;
                    preferences.edit().putLong("lastshowad", tomorrow).apply();
                    //loadAd("597dcb8546846521dae4a29e", false);
                    Toast.makeText(ApplicationLoader.applicationContext, "استفاده از تماس صوتی و تصویری تا 24 ساعت بدون محدودیت برای شما فعال شد" , Toast.LENGTH_LONG).show();*/
                }
                // store user reward in local database
            }
        });
        loadAd("597dcb8546846521dae4a29e", false);

        if (getArguments() != null) {
            onlySelect = arguments.getBoolean("onlySelect", false);
            dialogsType = preferences.getInt("defTab", 0);
            selectAlertString = arguments.getString("selectAlertString");
            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
            addToGroupAlertString = arguments.getString("addToGroupAlertString");
        }

        if (searchString == null) {
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.didLoadedReplyMessages);
            NotificationCenter.getInstance().addObserver(this, NotificationCenter.reloadHints);
        }


        if (!dialogsLoaded) {
            MessagesController.getInstance().loadDialogs(0, 100, true);
            ContactsController.getInstance().checkInviteText();
            dialogsLoaded = true;
        }
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (searchString == null) {
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didSetPasscode);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didLoadedReplyMessages);
            NotificationCenter.getInstance().removeObserver(this, NotificationCenter.reloadHints);
        }
        delegate = null;
    }

    private void addTabView(Context context, ImageView iv, TextView textView, boolean show) {
        iv.setScaleType(ImageView.ScaleType.CENTER);
        textView.setGravity(17);
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(0);
        shape.setCornerRadius((float) AndroidUtilities.dp(32.0f));
        textView.setBackgroundDrawable(shape);
        RelativeLayout layout = new RelativeLayout(context);
        layout.addView(iv, LayoutHelper.createRelative(-1, -1));
        layout.addView(textView, LayoutHelper.createRelative(-2, -2, 0, 0, 3, 6, 11));
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textView.getLayoutParams();
        params.addRule(preferences.getBoolean("tabsToBottom", false) ? 10 : 12);
        textView.setLayoutParams(params);
        if (show) {
            tabsLayout.addView(layout, LayoutHelper.createLinear(0, -1, 1.0f));
        }
    }

    private void unreadCount() {
        unreadCount(MessagesController.getInstance().dialogs, allCounter);
        unreadCount(MessagesController.getInstance().dialogsUsers, usersCounter);
        unreadCount(MessagesController.getInstance().dialogsBots, botsCounter);
        unreadCount(MessagesController.getInstance().dialogsChannels, channelsCounter);
        unreadCount(MessagesController.getInstance().dialogsFavs, favsCounter);
        unreadCountGroups();
    }

    private void unreadCountGroups() {
        if (preferences.getBoolean("hideSGroups", false)) {
            unreadCount(MessagesController.getInstance().dialogsGroupsAll, groupsCounter);
        } else {
            unreadCount(MessagesController.getInstance().dialogsGroups, groupsCounter);
            unreadCount(MessagesController.getInstance().dialogsMegaGroups, sGroupsCounter);
        }
        changedTabPosition = false;
    }

    private void unreadCount(ArrayList<TLRPC.TL_dialog> dialogs, TextView tv) {
        if (!preferences.getBoolean("hideTabs", false)) {
            if (preferences.getBoolean("hideTabsCounters", false)) {
                tv.setVisibility(View.GONE);
                return;
            }
            boolean allMuted = true;
            boolean countDialogs = preferences.getBoolean("tabsCountersCountChats", false);
            boolean countNotMuted = preferences.getBoolean("tabsCountersCountNotMuted", false);
            int unreadCount = 0;
            if (!(dialogs == null || dialogs.isEmpty())) {
                for (int a = 0; a < dialogs.size(); a++) {
                    TLRPC.TL_dialog dialg = dialogs.get(a);
                    boolean isMuted = MessagesController.getInstance().isDialogMuted(dialg.id);
                    if (!isMuted || !countNotMuted) {
                        int i = dialg.unread_count;
                        if (i == 0) {
                            if (preferences.getInt("unread_" + dialg.id, 0) == 1) {
                                i = 1;
                            }
                        }
                        if (i > 0) {
                            if (!countDialogs) {
                                unreadCount += i;
                            } else if (i > 0) {
                                unreadCount++;
                            }
                            if (i > 0 && !isMuted) {
                                allMuted = false;
                            }
                        }
                    }
                }
            }
            if (unreadCount == 0) {
                tv.setVisibility(View.GONE);
            } else {
                tv.setVisibility(View.VISIBLE);
                tv.setText("" + unreadCount);
                SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, 0);
                int size = themePrefs.getInt("chatsHeaderTabCounterSize", 11);
                tv.setTextSize(1, (float) size);
                tv.setPadding(AndroidUtilities.dp(size > 10 ? (float) (size - 7) : 4.0f), 0, AndroidUtilities.dp(size > 10 ? (float) (size - 7) : 4.0f), 0);
                int cColor = themePrefs.getInt("chatsHeaderTabCounterColor", -1);
                if (allMuted) {
                    tv.getBackground().setColorFilter(themePrefs.getInt("chatsHeaderTabCounterSilentBGColor", -4605511), PorterDuff.Mode.SRC_IN);
                    tv.setTextColor(cColor);
                } else {
                    tv.getBackground().setColorFilter(themePrefs.getInt("chatsHeaderTabCounterBGColor", -2937041), PorterDuff.Mode.SRC_IN);
                    tv.setTextColor(cColor);
                }
            }
            if (preferences.getBoolean("tabsToBottom", false)) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();
                params.addRule(preferences.getBoolean("tabsToBottom", false) ? 12 : 10, 0);
                params.addRule(preferences.getBoolean("tabsToBottom", false) ? 10 : 12);
                tv.setLayoutParams(params);
            }
        }
    }

    public void initTabs(Context context) {
        SharedPreferences.Editor editor = preferences.edit();
        boolean hideUsers = preferences.getBoolean("hideUsers", false);
        boolean hideGroups = preferences.getBoolean("hideGroups", false);
        boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
        boolean hideChannels = preferences.getBoolean("hideChannels", false);
        boolean hideBots = preferences.getBoolean("hideBots", false);
        boolean hideFavs = preferences.getBoolean("hideFavs", false);



        hideTabs = preferences.getBoolean("hideTabs", false);
        disableAnimation = preferences.getBoolean("disableTabsAnimation", false);
        if (hideUsers && hideGroups && hideSGroups && hideChannels && hideBots && hideFavs && !hideTabs) {
            hideTabs = true;
            editor.putBoolean("hideTabs", true).apply();
        } else if (hideUsers == false || hideGroups == false || hideSGroups == false || hideChannels == false || hideBots == false || hideFavs == false) {
            hideTabs = false;
            editor.putBoolean("hideTabs", false).apply();
        }
        tabsHeight = preferences.getInt("tabsHeight", 40);
        refreshTabAndListViews(false);
        int selectedTab = preferences.getInt("defTab", -1);
        if (selectedTab == -1) {
            selectedTab = preferences.getInt("selTab", 0);
        }
        if (!(hideTabs || dialogsType == selectedTab)) {
            int i = (selectedTab == 3 && hideSGroups) ? 9 : selectedTab;
            dialogsType = i;
            dialogsAdapter = new DialogsAdapter(context, dialogsType);
            listView.setAdapter(dialogsAdapter);
            dialogsAdapter.notifyDataSetChanged();
        }
        dialogsBackupAdapter = new DialogsAdapter(context, 0);

        tabsLayout = new LinearLayout(context);
        tabsLayout.setOrientation(LinearLayout.HORIZONTAL);
        tabsLayout.setGravity(17);


        allTab = new ImageView(context);
        allTab.setImageResource(R.drawable.tab_all);
        allCounter = new TextView(context);
        allCounter.setTag("ALL");

        usersTab = new ImageView(context);
        usersTab.setImageResource(R.drawable.tab_user);
        usersCounter = new TextView(context);
        usersCounter.setTag("USERS");

        groupsTab = new ImageView(context);
        groupsTab.setImageResource(R.drawable.tab_group);
        groupsCounter = new TextView(context);
        groupsCounter.setTag("GROUPS");

        superGroupsTab = new ImageView(context);
        superGroupsTab.setImageResource(R.drawable.tab_supergroup);
        sGroupsCounter = new TextView(context);
        sGroupsCounter.setTag("SGROUP");

        channelsTab = new ImageView(context);
        channelsTab.setImageResource(R.drawable.tab_channel);
        channelsCounter = new TextView(context);
        channelsCounter.setTag("CHANNELS");

        botsTab = new ImageView(context);
        botsTab.setImageResource(R.drawable.tab_bot);
        botsCounter = new TextView(context);
        botsCounter.setTag("BOTS");

        favsTab = new ImageView(context);
        favsTab.setImageResource(R.drawable.tab_favs);
        favsCounter = new TextView(context);
        favsCounter.setTag("FAVS");

        SharedPreferences tabpreferences = ApplicationLoader.applicationContext.getSharedPreferences("TabSort", Activity.MODE_PRIVATE);
        int i0 = tabpreferences.getInt("i0", 1);
        int i1 = tabpreferences.getInt("i1", 2);
        int i2 = tabpreferences.getInt("i2", 3);
        int i3 = tabpreferences.getInt("i3", 4);
        int i4 = tabpreferences.getInt("i4", 5);
        int i5 = tabpreferences.getInt("i5", 6);
        int i6 = tabpreferences.getInt("i6", 7);

        if(i0 == 1){
            addTabView(context, allTab, allCounter, true);
        } else if(i0 == 2){
            addTabView(context, usersTab, usersCounter, !hideUsers);
        } else if(i0 == 3){
            addTabView(context, groupsTab, groupsCounter, !hideGroups);
        } else if(i0 == 4){
            addTabView(context, superGroupsTab, sGroupsCounter, !hideSGroups);
        } else if(i0 == 5){
            addTabView(context, channelsTab, channelsCounter, !hideChannels);
        } else if(i0 == 6){
            addTabView(context, botsTab, botsCounter, !hideBots);
        } else if(i0 == 7){
            addTabView(context, favsTab, favsCounter, !hideFavs);
        }

        if(i1 == 1){
            addTabView(context, allTab, allCounter, true);
        } else if(i1 == 2){
            addTabView(context, usersTab, usersCounter, !hideUsers);
        } else if(i1 == 3){
            addTabView(context, groupsTab, groupsCounter, !hideGroups);
        } else if(i1 == 4){
            addTabView(context, superGroupsTab, sGroupsCounter, !hideSGroups);
        } else if(i1 == 5){
            addTabView(context, channelsTab, channelsCounter, !hideChannels);
        } else if(i1 == 6){
            addTabView(context, botsTab, botsCounter, !hideBots);
        } else if(i1 == 7){
            addTabView(context, favsTab, favsCounter, !hideFavs);
        }

        if(i2 == 1){
            addTabView(context, allTab, allCounter, true);
        } else if(i2 == 2){
            addTabView(context, usersTab, usersCounter, !hideUsers);
        } else if(i2 == 3){
            addTabView(context, groupsTab, groupsCounter, !hideGroups);
        } else if(i2 == 4){
            addTabView(context, superGroupsTab, sGroupsCounter, !hideSGroups);
        } else if(i2 == 5){
            addTabView(context, channelsTab, channelsCounter, !hideChannels);
        } else if(i2 == 6){
            addTabView(context, botsTab, botsCounter, !hideBots);
        } else if(i2 == 7){
            addTabView(context, favsTab, favsCounter, !hideFavs);
        }

        if(i3 == 1){
            addTabView(context, allTab, allCounter, true);
        } else if(i3 == 2){
            addTabView(context, usersTab, usersCounter, !hideUsers);
        } else if(i3 == 3){
            addTabView(context, groupsTab, groupsCounter, !hideGroups);
        } else if(i3 == 4){
            addTabView(context, superGroupsTab, sGroupsCounter, !hideSGroups);
        } else if(i3 == 5){
            addTabView(context, channelsTab, channelsCounter, !hideChannels);
        } else if(i3 == 6){
            addTabView(context, botsTab, botsCounter, !hideBots);
        } else if(i3 == 7){
            addTabView(context, favsTab, favsCounter, !hideFavs);
        }

        if(i4 == 1){
            addTabView(context, allTab, allCounter, true);
        } else if(i4 == 2){
            addTabView(context, usersTab, usersCounter, !hideUsers);
        } else if(i4 == 3){
            addTabView(context, groupsTab, groupsCounter, !hideGroups);
        } else if(i4 == 4){
            addTabView(context, superGroupsTab, sGroupsCounter, !hideSGroups);
        } else if(i4 == 5){
            addTabView(context, channelsTab, channelsCounter, !hideChannels);
        } else if(i4 == 6){
            addTabView(context, botsTab, botsCounter, !hideBots);
        } else if(i4 == 7){
            addTabView(context, favsTab, favsCounter, !hideFavs);
        }

        if(i5 == 1){
            addTabView(context, allTab, allCounter, true);
        } else if(i5 == 2){
            addTabView(context, usersTab, usersCounter, !hideUsers);
        } else if(i5 == 3){
            addTabView(context, groupsTab, groupsCounter, !hideGroups);
        } else if(i5 == 4){
            addTabView(context, superGroupsTab, sGroupsCounter, !hideSGroups);
        } else if(i5 == 5){
            addTabView(context, channelsTab, channelsCounter, !hideChannels);
        } else if(i5 == 6){
            addTabView(context, botsTab, botsCounter, !hideBots);
        } else if(i5 == 7){
            addTabView(context, favsTab, favsCounter, !hideFavs);
        }

        if(i6 == 1){
            addTabView(context, allTab, allCounter, true);
        } else if(i6 == 2){
            addTabView(context, usersTab, usersCounter, !hideUsers);
        } else if(i6 == 3){
            addTabView(context, groupsTab, groupsCounter, !hideGroups);
        } else if(i6 == 4){
            addTabView(context, superGroupsTab, sGroupsCounter, !hideSGroups);
        } else if(i6 == 5){
            addTabView(context, channelsTab, channelsCounter, !hideChannels);
        } else if(i6 == 6){
            addTabView(context, botsTab, botsCounter, !hideBots);
        } else if(i6 == 7){
            addTabView(context, favsTab, favsCounter, !hideFavs);
        }


        //List<String> tabsfor = new ArrayList<String>();
        /*String[] tabsname = new String[]{
                "allTab",
                "usersTab",
                "groupsTab",
                "superGroupsTab",
                "channelsTab",
                "botsTab",
                "favsTab"
        };

        int[] ss = new int[] {
                R.drawable.tab_all,
                R.drawable.tab_user,
                R.drawable.tab_group,
                R.drawable.tab_supergroup,
                R.drawable.tab_channel,
                R.drawable.tab_bot,
                R.drawable.tab_favs
        };


        for(int i = 0; i < tabsname.length; i++) {
            //tabsfor.add(tabsname[i]);
            ImageView iv = new ImageView(context);
            iv.setImageResource(ss[i]);
            TextView tv = new TextView(context);
            tv.setTag("FAVS");
            addTabView(context, iv, tv, !hideBots);
        }*/

        tabsView.addView(tabsLayout, LayoutHelper.createFrame(-1, GroundOverlayOptions.NO_DIMENSION));
        groupsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogsType = 4;
                refreshAdapter(getParentActivity());
            }
        });
        usersTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogsType = 6;
                refreshAdapter(getParentActivity());
            }
        });
        superGroupsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogsType = 7;
                refreshAdapter(getParentActivity());
            }
        });
        botsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogsType = 5;
                refreshAdapter(getParentActivity());
            }
        });
        channelsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogsType = 3;
                refreshAdapter(getParentActivity());
            }
        });
        allTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogsType = 0;
                refreshAdapter(getParentActivity());

            }
        });
        favsTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogsType = 8;
                refreshAdapter(getParentActivity());

            }
        });

        allTab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("All", R.string.All));
                String item = LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead);
                String item2 = LocaleController.getString("GoToSort", R.string.GoToSort);
                android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            markAsReadDialog(true);
                        }
                        if (i == 1){
                            ApplicationLoader.applicationContext.startActivity(new Intent(ApplicationLoader.applicationContext, SortTabs.class));
                        }
                    }
                };
                builder.setItems(new CharSequence[]{item, item2}, listener);
                showDialog(builder.create());
                return true;

            }
        });
        groupsTab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("GroupsTab", R.string.GroupsTab));
                String item = LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead);
                String item2 = LocaleController.getString("GoToSort", R.string.GoToSort);
                android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            markAsReadDialog(true);
                        }
                        if (i == 1){
                            ApplicationLoader.applicationContext.startActivity(new Intent(ApplicationLoader.applicationContext, SortTabs.class));
                        }
                    }
                };
                builder.setItems(new CharSequence[]{item, item2}, listener);
                showDialog(builder.create());
                return true;
            }
        });
        usersTab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("ContactTab", R.string.ContactTab));
                String item = LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead);
                String item2 = LocaleController.getString("GoToSort", R.string.GoToSort);
                android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            markAsReadDialog(true);
                        }
                        if (i == 1){
                            ApplicationLoader.applicationContext.startActivity(new Intent(ApplicationLoader.applicationContext, SortTabs.class));
                        }
                    }
                };
                builder.setItems(new CharSequence[]{item, item2}, listener);
                showDialog(builder.create());
                return true;
            }
        });
        superGroupsTab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("SGroupsTab", R.string.SGroupsTab));
                String item = LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead);
                String item2 = LocaleController.getString("GoToSort", R.string.GoToSort);
                android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            markAsReadDialog(true);
                        }
                        if (i == 1){
                            ApplicationLoader.applicationContext.startActivity(new Intent(ApplicationLoader.applicationContext, SortTabs.class));
                        }
                    }
                };
                builder.setItems(new CharSequence[]{item, item2}, listener);
                showDialog(builder.create());
                return true;
            }
        });
        botsTab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("Bots", R.string.Bots));
                String item = LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead);
                String item2 = LocaleController.getString("GoToSort", R.string.GoToSort);
                android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            markAsReadDialog(true);
                        }
                        if (i == 1){
                            ApplicationLoader.applicationContext.startActivity(new Intent(ApplicationLoader.applicationContext, SortTabs.class));
                        }
                    }
                };
                builder.setItems(new CharSequence[]{item, item2}, listener);
                showDialog(builder.create());
                return true;
            }
        });
        channelsTab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("Bots", R.string.Bots));
                String item = LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead);
                String item2 = LocaleController.getString("GoToSort", R.string.GoToSort);
                android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            markAsReadDialog(true);
                        }
                        if (i == 1){
                            ApplicationLoader.applicationContext.startActivity(new Intent(ApplicationLoader.applicationContext, SortTabs.class));
                        }
                    }
                };
                builder.setItems(new CharSequence[]{item, item2}, listener);
                showDialog(builder.create());
                return true;
            }
        });
        favsTab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("Favorites", R.string.Favorites));

                String item = LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead);
                String item2 = LocaleController.getString("GoToSort", R.string.GoToSort);
                android.content.DialogInterface.OnClickListener listener = new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            markAsReadDialog(true);
                        }
                        if (i == 1){
                            ApplicationLoader.applicationContext.startActivity(new Intent(ApplicationLoader.applicationContext, SortTabs.class));
                        }
                    }
                };
                builder.setItems(new CharSequence[]{item, item2}, listener);
                showDialog(builder.create());
                return true;
            }
        });
        ViewGroup.LayoutParams params = tabsView.getLayoutParams();
        if (params != null) {
            params.height = h;
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            tabsView.setLayoutParams(params);
        }

    }

    private void markAsReadDialog(final boolean all) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setMessage(LocaleController.getString("AreYouSure", R.string.AreYouSure));
        builder.setTitle(all ? LocaleController.getString("MarkAllAsRead", R.string.MarkAllAsRead) : LocaleController.getString("MarkAsRead", R.string.MarkAsRead));
        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TLRPC.TL_dialog dialg;
                if (all) {
                    ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray();
                    if (dialogs != null && !dialogs.isEmpty()) {
                        for (int a = 0; a < dialogs.size(); a++) {
                            dialg = getDialogsArray().get(a);
                            if (dialg.unread_count > 0) {
                                MessagesController.getInstance().markDialogAsRead(dialg.id, dialg.last_read, Math.max(0, dialg.top_message), dialg.last_message_date, true, false);
                            }
                        }
                        return;
                    }
                    return;
                }
                dialg = MessagesController.getInstance().dialogs_dict.get(Long.valueOf(selectedDialog));
                if (dialg.unread_count > 0) {
                    MessagesController.getInstance().markDialogAsRead(dialg.id, dialg.last_read, Math.max(0, dialg.top_message), dialg.last_message_date, true, false);
                }
            }
        });
        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
        showDialog(builder.create());
    }

    private void refreshAdapter(Context context) {
        refreshAdapterAndTabs(new DialogsAdapter(context, dialogsType));
    }

    private void hideShowTabs() {
        boolean hideUsers = preferences.getBoolean("hideUsers", false);
        boolean hideGroups = preferences.getBoolean("hideGroups", false);
        boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
        boolean hideChannels = preferences.getBoolean("hideChannels", false);
        boolean hideBots = preferences.getBoolean("hideBots", false);
        boolean hideFavs = preferences.getBoolean("hideFavs", false);
        if (!hideUsers) {
            try {
                if (usersTab.getParent() == null) {
                    tabsLayout.addView(usersTab, 1, LayoutHelper.createLinear(0, -1, StickersActivity.TouchHelperCallback.ALPHA_FULL));
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
                return;
            }
        } else if (usersTab.getParent() != null) {
            tabsLayout.removeView(usersTab);
        }
        if (hideGroups) {
            if (groupsTab.getParent() != null) {
                tabsLayout.removeView(groupsTab);
            }
        } else if (groupsTab.getParent() == null) {
            tabsLayout.addView(groupsTab, hideUsers ? 1 : 2, LayoutHelper.createLinear(0, -1, StickersActivity.TouchHelperCallback.ALPHA_FULL));
        }
        if (hideSGroups) {
            if (superGroupsTab.getParent() != null) {
                tabsLayout.removeView(superGroupsTab);
            }
        } else if (superGroupsTab.getParent() == null) {
            LinearLayout linearLayout = tabsLayout;
            View view = superGroupsTab;
            int i = hideGroups ? hideUsers ? 1 : 2 : 3;
            linearLayout.addView(view, i, LayoutHelper.createLinear(0, -1, StickersActivity.TouchHelperCallback.ALPHA_FULL));
        }
        if (hideChannels) {
            if (channelsTab.getParent() != null) {
                tabsLayout.removeView(channelsTab);
            }
        } else if (channelsTab.getParent() == null) {
            int i = hideSGroups ? hideGroups ? hideUsers ? 1 : 2 : 3 : 4;
            tabsLayout.addView(channelsTab, i, LayoutHelper.createLinear(0, -1, StickersActivity.TouchHelperCallback.ALPHA_FULL));
        }
        if (!hideBots) {
            int place = tabsLayout.getChildCount();
            if (!hideFavs) {
                place--;
            }
            if (botsTab.getParent() == null) {
                tabsLayout.addView(botsTab, place, LayoutHelper.createLinear(0, -1, StickersActivity.TouchHelperCallback.ALPHA_FULL));
            }
        } else if (botsTab.getParent() != null) {
            tabsLayout.removeView(botsTab);
        }
        if (hideFavs) {
            if (favsTab.getParent() != null) {
                tabsLayout.removeView(favsTab);
            }
        } else if (favsTab.getParent() == null) {
            tabsLayout.addView(favsTab, tabsLayout.getChildCount(), LayoutHelper.createLinear(0, -1, StickersActivity.TouchHelperCallback.ALPHA_FULL));
        }
    }

    private void updateTabs() {
        hideTabs = preferences.getBoolean("hideTabs", false);
        disableAnimation = preferences.getBoolean("disableTabsAnimation", false);
        tabsHeight = preferences.getInt("tabsHeight", 40);
        refreshTabAndListViews(false);
        if (hideTabs && dialogsType > 2) {
            dialogsType = 0;
            refreshAdapterAndTabs(dialogsBackupAdapter);
        }
    }

    private void refreshAdapterAndTabs(DialogsAdapter adapter) {
        dialogsAdapter = adapter;
        listView.setAdapter(dialogsAdapter);
        dialogsAdapter.notifyDataSetChanged();
        if (!onlySelect) {
            preferences.edit().putInt("selTab", dialogsType == 9 ? 3 : dialogsType).apply();
        }
        refreshTabs();
    }

    private void refreshTabs() {
        int i;
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int defColor = themePrefs.getInt("chatsHeaderIconsColor", 0xffffffff);
        int iconColor = themePrefs.getInt("chatsHeaderTabIconColor", defColor);
        int iColor = themePrefs.getInt("chatsHeaderTabUnselectedIconColor", AndroidUtilities.getIntAlphaColor("chatsHeaderTabIconColor", defColor, 0.3f));

        allTab.setBackgroundResource(0);
        usersTab.setBackgroundResource(0);
        groupsTab.setBackgroundResource(0);
        superGroupsTab.setBackgroundResource(0);
        channelsTab.setBackgroundResource(0);
        botsTab.setBackgroundResource(0);
        favsTab.setBackgroundResource(0);

        allTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        usersTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        groupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        superGroupsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        channelsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        botsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        favsTab.setColorFilter(iColor, PorterDuff.Mode.SRC_IN);
        Drawable selected = getParentActivity().getResources().getDrawable(R.drawable.tab_selected);
        selected.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
        if (dialogsType == 9) {
            i = 0;
        } else {
            i = dialogsType;
        }
        switch (i) {
            case 6:
                usersTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                usersTab.setBackgroundDrawable(selected);
                break;
            case 4:
                groupsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                groupsTab.setBackgroundDrawable(selected);
                break;
            case 3:
                channelsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                channelsTab.setBackgroundDrawable(selected);
                break;
            case 5:
                botsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                botsTab.setBackgroundDrawable(selected);
                break;
            case 7:
                superGroupsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                superGroupsTab.setBackgroundDrawable(selected);
                break;
            case 8:
                favsTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                favsTab.setBackgroundDrawable(selected);
                break;
            default:
                allTab.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);
                allTab.setBackgroundDrawable(selected);
                break;
        }
        String t = getHeaderAllTitles();
        actionBar.setTitle(t);
        paintHeader(true);
        if (getDialogsArray() != null && getDialogsArray().isEmpty()) {
            searchEmptyView.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            if (emptyView.getChildCount() > 0) {
                TextView tv = (TextView) emptyView.getChildAt(0);
                tv.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                if (tv != null) {
                    if (dialogsType < 3) {
                        t = LocaleController.getString("NoChats", R.string.NoChats);
                    } else if (dialogsType == 8) {
                        t = LocaleController.getString("NoFavoritesHelp", R.string.NoFavoritesHelp);
                    }
                    tv.setText(t);
                    tv.setTextColor(themePrefs.getInt("chatsNameColor", 0xff212121));
                }
                if (emptyView.getChildAt(1) != null) {
                    emptyView.getChildAt(1).setVisibility(View.GONE);
                }
            }
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setBackgroundColor(themePrefs.getInt("chatsRowColor", 0xffffffff));
            listView.setEmptyView(emptyView);
        }
    }

    private String getHeaderAllTitles() {
        switch (dialogsType) {
            case 6:
                return LocaleController.getString("Users", R.string.Users);
            case 4:
                return LocaleController.getString("Groups", R.string.Groups);
            case 3:
                return LocaleController.getString("Channels", R.string.Channels);
            case 5:
                return LocaleController.getString("Bots", R.string.Bots);
            case 7:
                return LocaleController.getString("SuperGroups", R.string.SuperGroups);
            case 8:
                return LocaleController.getString("Favorites", R.string.Favorites);
            default:
                return getHeaderTitle();
        }
    }

    private void paintHeader(boolean tabs) {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        actionBar.setTitleColor(themePrefs.getInt("chatsHeaderTitleColor", 0xffffffff));
        int def = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
        int hColor = themePrefs.getInt("chatsHeaderColor", def);
        /*if(!tabs){
            actionBar.setBackgroundColor(hColor);
        }else{
            tabsView.setBackgroundColor(hColor);
        }*/
        if (!tabs) actionBar.setBackgroundColor(hColor);
        if (tabs) {
            tabsView.setBackgroundColor(hColor);
        }
        int val = themePrefs.getInt("chatsHeaderGradient", 0);
        if (val > 0) {
            GradientDrawable.Orientation go;
            switch (val) {
                case 2:
                    go = GradientDrawable.Orientation.LEFT_RIGHT;
                    break;
                case 3:
                    go = GradientDrawable.Orientation.TL_BR;
                    break;
                case 4:
                    go = GradientDrawable.Orientation.BL_TR;
                    break;
                default:
                    go = GradientDrawable.Orientation.TOP_BOTTOM;
            }
            int gradColor = themePrefs.getInt("chatsHeaderGradientColor", def);
            int[] colors = new int[]{hColor, gradColor};
            GradientDrawable gd = new GradientDrawable(go, colors);
            if (!tabs) actionBar.setBackgroundDrawable(gd);
            if (tabs) {
                tabsView.setBackgroundDrawable(gd);
            }
            /*if(!tabs){
                actionBar.setBackgroundDrawable(gd);
            }else{
                tabsView.setBackgroundDrawable(gd);
            }*/
        }
    }

    private void updateListBG() {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int mainColor = themePrefs.getInt("chatsRowColor", 0xffffffff);
        int value = themePrefs.getInt("chatsRowGradient", 0);
        boolean b = true;//themePrefs.getBoolean("chatsRowGradientListCheck", false);
        if (value > 0 && b) {
            GradientDrawable.Orientation go;
            switch (value) {
                case 2:
                    go = GradientDrawable.Orientation.LEFT_RIGHT;
                    break;
                case 3:
                    go = GradientDrawable.Orientation.TL_BR;
                    break;
                case 4:
                    go = GradientDrawable.Orientation.BL_TR;
                    break;
                default:
                    go = GradientDrawable.Orientation.TOP_BOTTOM;
            }

            int gradColor = themePrefs.getInt("chatsRowGradientColor", 0xffffffff);
            int[] colors = new int[]{mainColor, gradColor};
            GradientDrawable gd = new GradientDrawable(go, colors);
            listView.setBackgroundDrawable(gd);
        } else {
            listView.setBackgroundColor(mainColor);
        }
    }

    private String getHeaderTitle() {
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int value = themePrefs.getInt("chatsHeaderTitle", 0);
        String title = LocaleController.getString("AppName", R.string.AppName);
        TLRPC.User user = UserConfig.getCurrentUser();
        if (value == 1) {
            title = LocaleController.getString("ShortAppName", R.string.ShortAppName);
        } else if (value == 2) {
            if (user != null && (user.first_name != null || user.last_name != null)) {
                title = ContactsController.formatName(user.first_name, user.last_name);
            }
        } else if (value == 3) {
            if (user != null && user.username != null && user.username.length() != 0) {
                title = "@" + user.username;
            }
        } else if (value == 4) {
            title = "";
        }
        return title;
    }

    private void refreshDialogType(int d) {
        //  Toast.makeText(ApplicationLoader.applicationContext, ""+d, Toast.LENGTH_SHORT).show();
        if (!hideTabs) {
            boolean hideUsers = preferences.getBoolean("hideUsers", false);
            boolean hideGroups = preferences.getBoolean("hideGroups", false);
            boolean hideSGroups = preferences.getBoolean("hideSGroups", false);
            boolean hideChannels = preferences.getBoolean("hideChannels", false);
            boolean hideBots = preferences.getBoolean("hideBots", false);
            boolean hideFavs = preferences.getBoolean("hideFavs", false);
            boolean loop = preferences.getBoolean("infiniteTabsSwipe", false);
            int i;
            if (d == 1) {
                switch (dialogsType) {
                    case 6:
                        if (hideGroups) {
                            i = !hideGroups ? 4 : !hideSGroups ? 7 : !hideChannels ? 3 : !hideBots ? 5 : !hideFavs ? 8 : loop ? 0 : dialogsType;
                            dialogsType = i;
                            return;
                        }
                        dialogsType = hideSGroups ? 9 : 4;
                        return;
                    case 4:
                        i = !hideSGroups ? 7 : !hideChannels ? 3 : !hideBots ? 5 : !hideFavs ? 8 : loop ? 0 : dialogsType;
                        dialogsType = i;
                        return;
                    case 3 /*5*/:
                        i = !hideBots ? 5 : !hideFavs ? 8 : loop ? 0 : dialogsType;
                        dialogsType = i;
                        return;
                    case 5:
                        i = !hideFavs ? 8 : loop ? 0 : dialogsType;
                        dialogsType = i;
                        return;
                    case 7:
                        i = !hideChannels ? 3 : !hideBots ? 5 : !hideFavs ? 8 : loop ? 0 : dialogsType;
                        dialogsType = i;
                        return;
                    case 8:
                        if (loop) {
                            dialogsType = 0;
                            return;
                        }
                        return;
                    default:
                        i = !hideUsers ? 6 : (hideGroups || !hideSGroups) ? !hideGroups ? 4 : !hideChannels ? 3 : !hideBots ? 5 : !hideFavs ? 8 : loop ? 0 : dialogsType : 0;
                        dialogsType = i;
                        return;
                }
            }
            if (d == 0) {
                switch (dialogsType) {
                    case 6:
                        dialogsType = 0;
                        return;
                    case 4:
                        dialogsType = !hideUsers ? 6 : 0;
                        return;
                    case 3:
                        i = !hideSGroups ? 7 : !hideGroups ? 4 : !hideUsers ? 6 : 0;
                        dialogsType = i;
                        return;
                    case 5:
                        i = !hideChannels ? 3 : !hideSGroups ? 7 : !hideGroups ? 4 : !hideUsers ? 6 : 0;
                        dialogsType = i;
                        return;
                    case 7:
                        i = !hideGroups ? 4 : !hideUsers ? 6 : 0;
                        dialogsType = i;
                        return;
                    case 8:
                        i = !hideBots ? 5 : !hideChannels ? 3 : !hideSGroups ? 7 : !hideGroups ? 4 : !hideUsers ? 6 : 0;
                        dialogsType = i;
                        return;
                    default:
                        if (loop) {
                            i = !hideFavs ? 8 : !hideBots ? 5 : !hideChannels ? 3 : !hideSGroups ? 7 : !hideGroups ? 4 : !hideUsers ? 6 : 0;
                            dialogsType = i;
                        }
                        return;
                }
            }
        }
    }

    class debug extends TimerTask {
        public void run() {
        }
    }
    @Override
    public View createView(final Context context) {
        searching = false;
        searchWas = false;
        joinToTelehgram();

        preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int def = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
        int iconColor = themePrefs.getInt("chatsHeaderIconsColor", 0xffffffff);
        int tColor = themePrefs.getInt("chatsHeaderTitleColor", 0xffffffff);
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Theme.loadRecources(context);
            }
        });

        ActionBarMenu menu = actionBar.createMenu();
        if (!onlySelect && searchString == null) {
            passcodeItem = menu.addItem(1, R.drawable.lock_close);
            updatePasscodeButton();
        }
        final Drawable search = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_search);
        if (search != null) search.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
        final ActionBarMenuItem item = menu.addItem(0, search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                searching = true;
                if (listView != null) {
                    if (searchString != null) {
                        listView.setEmptyView(searchEmptyView);
                        progressView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.GONE);
                    }
                    if (!onlySelect) {
                        floatingButton.setVisibility(View.GONE);
                    }
                }
                updatePasscodeButton();
            }

            @Override
            public boolean canCollapseSearch() {
                if (searchString != null) {
                    finishFragment();
                    return false;
                }
                return true;
            }

            @Override
            public void onSearchCollapse() {
                searching = false;
                searchWas = false;
                if (listView != null) {
                    searchEmptyView.setVisibility(View.GONE);
                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                        emptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        listView.setEmptyView(emptyView);
                    }
                    if (!onlySelect) {
                        floatingButton.setVisibility(View.VISIBLE);
                        floatingHidden = true;
                        floatingButton.setTranslationY(preferences.getBoolean("tabsToBottom", false) ? AndroidUtilities.dp(150) : AndroidUtilities.dp(100));

                        hideFloatingButton(false);
                    }
                    if (listView.getAdapter() != dialogsAdapter) {
                        listView.setAdapter(dialogsAdapter);
                        dialogsAdapter.notifyDataSetChanged();
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(null);
                }
                updatePasscodeButton();
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (text.length() != 0 || dialogsSearchAdapter != null && dialogsSearchAdapter.hasRecentRearch()) {
                    searchWas = true;
                    if (dialogsSearchAdapter != null && listView.getAdapter() != dialogsSearchAdapter) {
                        listView.setAdapter(dialogsSearchAdapter);
                        dialogsSearchAdapter.notifyDataSetChanged();
                    }
                    if (searchEmptyView != null && listView.getEmptyView() != searchEmptyView) {
                        emptyView.setVisibility(View.GONE);
                        progressView.setVisibility(View.GONE);
                        searchEmptyView.showTextView();
                        listView.setEmptyView(searchEmptyView);
                    }
                }
                if (dialogsSearchAdapter != null) {
                    dialogsSearchAdapter.searchDialogs(text);
                }
                updateListBG();

            }
        });
        item.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        if (tColor != 0xffffffff) {
            item.getSearchField().setTextColor(tColor);
            item.getSearchField().setHintTextColor(AndroidUtilities.getIntAlphaColor("chatsHeaderTitleColor", 0xffffffff, 0.5f));
        }

        if (onlySelect) {
            Drawable back = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_back);
            if (back != null) back.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            actionBar.setBackButtonDrawable(back);
            actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
        } else {
            if (searchString != null) {
                actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            } else {
                actionBar.setBackButtonDrawable(new MenuDrawable());
            }

            actionBar.setTitle(LocaleController.getString("AppName", R.string.AppName));
        }
        if (preferences.getBoolean("show_ghost_state_icon", true) == true) {
            if (preferences.getBoolean("ghost_mode", false) == true) {
                Drawable ic_ghost = getParentActivity().getResources().getDrawable(R.drawable.ic_ghost);
                if (ic_ghost != null) ic_ghost.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
                ghostItem = menu.addItem(3, ic_ghost);

            } else {
                Drawable ic_ghost_disable = getParentActivity().getResources().getDrawable(R.drawable.ic_ghost_disable);
                if (ic_ghost_disable != null)
                    ic_ghost_disable.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
                ghostItem = menu.addItem(3, ic_ghost_disable);

            }
        } else {
            ghostItem = menu.addItem(3, R.color.transparent);
        }

        if (preferences.getBoolean("show_last_seen_icon", false) == true) {
            /*Drawable ic_teleh_seen = getParentActivity().getResources().getDrawable(R.drawable.ic_teleh_seen);
            if (ic_teleh_seen != null)
                ic_teleh_seen.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            menu.addItem(2, ic_teleh_seen);*/
        }
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onlySelect) {
                        finishFragment();
                    } else if (parentLayout != null) {
                        parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (id == 1) {
                    UserConfig.appLocked = !UserConfig.appLocked;
                    UserConfig.saveConfig(false);
                    updatePasscodeButton();
                } else if (id == 3) {
                    changeGhostModeState();
                }
            }
        });

        paintHeader(false);
        final FrameLayout frameLayout = new FrameLayout(context);
        fragmentView = frameLayout;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(true);
        listView.setItemAnimator(null);
        listView.setInstantClick(true);
        listView.setLayoutAnimation(null);
        listView.setPadding(0, preferences.getBoolean("tabsToBottom", false) ? 10 : h, 0, preferences.getBoolean("tabsToBottom", false) ? h : 0);
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        if (Build.VERSION.SDK_INT >= 11) {
            listView.setVerticalScrollbarPosition(LocaleController.isRTL ? ListView.SCROLLBAR_POSITION_LEFT : ListView.SCROLLBAR_POSITION_RIGHT);
        }
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        listView.setOnItemClickListener(new RecyclerListView.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listView == null || listView.getAdapter() == null) {
                    return;
                }
                long dialog_id = 0;
                int message_id = 0;
                RecyclerView.Adapter adapter = listView.getAdapter();
                if (adapter == dialogsAdapter) {
                    TLRPC.TL_dialog dialog = dialogsAdapter.getItem(position);
                    if (dialog == null) {
                        return;
                    }
                    dialog_id = dialog.id;
                } else if (adapter == dialogsSearchAdapter) {
                    Object obj = dialogsSearchAdapter.getItem(position);
                    if (obj instanceof TLRPC.User) {
                        dialog_id = ((TLRPC.User) obj).id;
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.User> users = new ArrayList<>();
                            users.add((TLRPC.User) obj);
                            MessagesController.getInstance().putUsers(users, false);
                            MessagesStorage.getInstance().putUsersAndChats(users, null, false, true);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.User) obj);
                        }
                    } else if (obj instanceof TLRPC.Chat) {
                        if (dialogsSearchAdapter.isGlobalSearch(position)) {
                            ArrayList<TLRPC.Chat> chats = new ArrayList<>();
                            chats.add((TLRPC.Chat) obj);
                            MessagesController.getInstance().putChats(chats, false);
                            MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
                        }
                        if (((TLRPC.Chat) obj).id > 0) {
                            dialog_id = -((TLRPC.Chat) obj).id;
                        } else {
                            dialog_id = AndroidUtilities.makeBroadcastId(((TLRPC.Chat) obj).id);
                        }
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.Chat) obj);
                        }
                    } else if (obj instanceof TLRPC.EncryptedChat) {
                        dialog_id = ((long) ((TLRPC.EncryptedChat) obj).id) << 32;
                        if (!onlySelect) {
                            dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.EncryptedChat) obj);
                        }
                    } else if (obj instanceof MessageObject) {
                        MessageObject messageObject = (MessageObject) obj;
                        dialog_id = messageObject.getDialogId();
                        message_id = messageObject.getId();
                        dialogsSearchAdapter.addHashtagsFromMessage(dialogsSearchAdapter.getLastSearchString());
                    } else if (obj instanceof String) {
                        actionBar.openSearchField((String) obj);
                    }
                }

                if (dialog_id == 0) {
                    return;
                }

                if (onlySelect) {
                    didSelectResult(dialog_id, true, false);
                } else {
                    Bundle args = new Bundle();
                    int lower_part = (int) dialog_id;
                    int high_id = (int) (dialog_id >> 32);
                    if (lower_part != 0) {
                        if (high_id == 1) {
                            args.putInt("chat_id", lower_part);
                        } else {
                            if (lower_part > 0) {
                                args.putInt("user_id", lower_part);
                            } else if (lower_part < 0) {
                                if (message_id != 0) {
                                    TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                                    if (chat != null && chat.migrated_to != null) {
                                        args.putInt("migrated_to", lower_part);
                                        lower_part = -chat.migrated_to.channel_id;
                                    }
                                }
                                args.putInt("chat_id", -lower_part);
                            }
                        }
                    } else {
                        args.putInt("enc_id", high_id);
                    }
                    if (message_id != 0) {
                        args.putInt("message_id", message_id);
                    } else {
                        if (actionBar != null) {
                            actionBar.closeSearchField();
                        }
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter) {
                            return;
                        }
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                                presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {


                            if(show_ads<5) {
                                 show_ads++;
                            }
                            if(show_ads==3) {
                                //Adad.showInterstitialAd(getParentActivity());
                            }
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }
        });
        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListener() {
            @Override
            public boolean onItemClick(View view, int position) {
                //long press on item
                if (onlySelect || searching && searchWas || getParentActivity() == null) {
                    if (searchWas && searching || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                        RecyclerView.Adapter adapter = listView.getAdapter();
                        if (adapter == dialogsSearchAdapter) {
                            Object item = dialogsSearchAdapter.getItem(position);
                            if (item instanceof String || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                                builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        if (dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                            dialogsSearchAdapter.clearRecentSearch();
                                        } else {
                                            dialogsSearchAdapter.clearRecentHashtags();
                                        }
                                    }
                                });
                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder.create());
                                return true;
                            }
                        }
                    }
                    return false;
                }
                TLRPC.TL_dialog dialog;
                ArrayList<TLRPC.TL_dialog> dialogs = getDialogsArray();
                if (position < 0 || position >= dialogs.size()) {
                    return false;
                }
                dialog = dialogs.get(position);
                selectedDialog = dialog.id;

                BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                int lower_id = (int) selectedDialog;
                int high_id = (int) (selectedDialog >> 32);

                if (DialogObject.isChannel(dialog)) {
                    //gerftan id chat
                    final TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_id);
                    CharSequence items[];
                    if (chat != null && chat.megagroup) {
                        items = new CharSequence[]{LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache), chat == null || !chat.creator ? LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu) : LocaleController.getString("DeleteMegaMenu", R.string.DeleteMegaMenu)
                                , Favourite.isFavourite(Long.valueOf(selectedDialog)) ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites)
                                , Favourite.isFavourite(Long.valueOf(selectedDialog)) ? LocaleController.getString("MuteChat", R.string.MuteChat) : LocaleController.getString("MuteChat", R.string.MuteChat)};
                    } else {
                        items = new CharSequence[]{LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache), chat == null || !chat.creator ? LocaleController.getString("LeaveChannelMenu", R.string.LeaveChannelMenu) : LocaleController.getString("ChannelDeleteMenu", R.string.ChannelDeleteMenu)
                                , Favourite.isFavourite(Long.valueOf(selectedDialog)) ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites)
                                , Favourite.isFavourite(Long.valueOf(selectedDialog)) ? LocaleController.getString("MuteChat", R.string.MuteChat) : LocaleController.getString("MuteChat", R.string.MuteChat)};
                    }
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {
                            if (which == 3) {
                                //Toast.makeText(getParentActivity(), "hiiiiiiii", Toast.LENGTH_LONG).show();

                                boolean muted = MessagesController.getInstance().isDialogMuted(selectedDialog);
                                if (!muted) {
                                    long flags;
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("notify2_" + selectedDialog, 2);
                                    flags = 1;
                                    MessagesStorage.getInstance().setDialogFlags(selectedDialog, flags);
                                    editor.commit();
                                    TLRPC.TL_dialog dialog2 = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                    if (dialog2 != null) {
                                        dialog2.notify_settings = new TLRPC.TL_peerNotifySettings();
                                        dialog2.notify_settings.mute_until = Integer.MAX_VALUE;
                                    }
                                    NotificationsController.updateServerNotificationsSettings(selectedDialog);
                                    NotificationsController.getInstance().removeNotificationsForDialog(selectedDialog);

                                } else {
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("notify2_" + selectedDialog, 0);
                                    MessagesStorage.getInstance().setDialogFlags(selectedDialog, 0);
                                    editor.commit();
                                    TLRPC.TL_dialog dialog2 = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                    if (dialog != null) {
                                        dialog2.notify_settings = new TLRPC.TL_peerNotifySettings();
                                    }
                                    NotificationsController.updateServerNotificationsSettings(selectedDialog);
                                }

                                return;
                            }
                            if (which == 2) {
                                TLRPC.TL_dialog localDialog2 = MessagesController.getInstance().dialogs_dict.get(Long.valueOf(selectedDialog));

                                if (Favourite.isFavourite(Long.valueOf(selectedDialog))) {
                                    Favourite.deleteFavourite(Long.valueOf(selectedDialog));
                                    MessagesController.getInstance().dialogsFavs.remove(localDialog2);
                                    dialogsAdapter.notifyDataSetChanged();
                                } else if (!Favourite.isFavourite(Long.valueOf(selectedDialog))) {
                                    Favourite.addFavourite(Long.valueOf(selectedDialog));
                                    MessagesController.getInstance().dialogsFavs.add(localDialog2);
                                }
                            }
                            if (which != 2) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                if (which == 0)

                                {
                                    if (chat != null && chat.megagroup) {
                                        builder.setMessage(LocaleController.getString("AreYouSureClearHistorySuper", R.string.AreYouSureClearHistorySuper));
                                    } else {
                                        builder.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", R.string.AreYouSureClearHistoryChannel));
                                    }
                                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            MessagesController.getInstance().deleteDialog(selectedDialog, 2);
                                        }
                                    });
                                } else

                                {
                                    if (chat != null && chat.megagroup) {
                                        if (!chat.creator) {
                                            builder.setMessage(LocaleController.getString("MegaLeaveAlert", R.string.MegaLeaveAlert));
                                        } else {
                                            builder.setMessage(LocaleController.getString("MegaDeleteAlert", R.string.MegaDeleteAlert));
                                        }
                                    } else {
                                        if (chat == null || !chat.creator) {
                                            builder.setMessage(LocaleController.getString("ChannelLeaveAlert", R.string.ChannelLeaveAlert));
                                        } else {
                                            builder.setMessage(LocaleController.getString("ChannelDeleteAlert", R.string.ChannelDeleteAlert));
                                        }

                                    }
                                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, UserConfig.getCurrentUser(), null);
                                            if (AndroidUtilities.isTablet()) {
                                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                            }
                                        }
                                    });
                                }

                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);

                                showDialog(builder.create()
                                );
                            }
                        }
                    });
                    showDialog(builder.create());
                } else {
                    final boolean isChat = lower_id < 0 && high_id != 1;
                    TLRPC.User user = null;
                    if (!isChat && lower_id > 0 && high_id != 1) {
                        user = MessagesController.getInstance().getUser(lower_id);
                    }
                    final boolean isBot = user != null && user.bot;
                    final TLRPC.User finalUser = user;
                    builder.setItems(new CharSequence[]{LocaleController.getString("ClearHistory", R.string.ClearHistory),
                            isChat ? LocaleController.getString("DeleteChat", R.string.DeleteChat) :
                                    isBot ? LocaleController.getString("DeleteAndStop", R.string.DeleteAndStop) : LocaleController.getString("Delete", R.string.Delete)
                            , Favourite.isFavourite(Long.valueOf(selectedDialog)) ? LocaleController.getString("DeleteFromFavorites", R.string.DeleteFromFavorites) : LocaleController.getString("AddToFavorites", R.string.AddToFavorites),
                            LocaleController.getString("MuteChat", R.string.MuteChat),
                    "تماس صوتی",
                    "تماس تصویری"},
                           new int[] {
                                   R.drawable.bottomshade_1,
                                   R.drawable.bottomshade_2,
                                   R.drawable.bottomshade_3,
                                   R.drawable.bottomshade_4,
                                   R.drawable.bottomshade_5,
                                   R.drawable.bottomshade_6,
                           }
                            ,new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, final int which) {

                            if (which == 5){
                                if(LocaleController.showrewvideo()){
                                    //show video
                                    if(ad != null && ad.isValid()) {
                                        //showAddBtn.setEnabled(false);
                                        TapsellShowOptions showOptions = new TapsellShowOptions();
                                        showOptions.setBackDisabled(true);
                                        showOptions.setImmersiveMode(true);
                                        showOptions.setRotationMode(TapsellShowOptions.ROTATION_UNLOCKED);
                                        showOptions.setShowDialog(false);
                                        if(! ad.isShown()){
                                            ad.show(getParentActivity(), showOptions, new TapsellAdShowListener() {
                                                @Override
                                                public void onOpened(TapsellAd tapsellAd) {
                                                    Log.e("tapsell","ad opened");
                                                }

                                                @Override
                                                public void onClosed(TapsellAd tapsellAd) {
                                                    Log.e("tapsell","ad closed");

                                                }
                                            });
                                        } else {
                                            TLRPC.User ownuser = MessagesController.getInstance().getUser(UserConfig.getClientUserId());

                                            if(finalUser != null && finalUser.id != 0 && ownuser != null && ProfileActivity.sinchactive){
                                                if (ContextCompat.checkSelfPermission(getParentActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                                    ActivityCompat.requestPermissions(getParentActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                                                    return;
                                                }

                                                TLRPC.User iamuser = MessagesController.getInstance().getUser(finalUser.id);

                                                if(iamuser.id == ownuser.id){
                                                    Toast.makeText(getParentActivity(), "نمی توانید با خودتان تماس بگیرید!", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                Intent n = new Intent(getParentActivity(), PlaceCallActivity.class);
                                                n.putExtra("profileid", iamuser.id+"");
                                                getParentActivity().startActivity(n);

                                                //Toast.makeText(getParentActivity(), user.id+"", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getParentActivity(), "اطلاعات دریافتی صحیح نیست، برنامه را ببندید و مجدد باز کنید", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    } else if( ad == null ){
                                        Log.e("tapsell","null ad");
                                    } else {
                                        Log.e("tapsell","ad file removed? " + ad.isFileRemoved());
                                        Log.e("tapsell","invalid ad, id=" + ad.getId());
                                    }
                                } else {
                                    TLRPC.User ownuser = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                                    if(finalUser != null && finalUser.id != 0 && ownuser != null && ProfileActivity.sinchactive){
                                        if (ContextCompat.checkSelfPermission(getParentActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(getParentActivity(), new String[]{Manifest.permission.CAMERA}, 1);
                                            return;
                                        }
                                        if (ContextCompat.checkSelfPermission(getParentActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(getParentActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                                            return;
                                        }

                                        TLRPC.User iamuser = MessagesController.getInstance().getUser(finalUser.id);

                                        if(iamuser.id == ownuser.id){
                                            Toast.makeText(getParentActivity(), "نمی توانید با خودتان تماس بگیرید!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        Intent n = new Intent(getParentActivity(), VCPlaceCallActivity.class);
                                        n.putExtra("profileid", iamuser.id+"");
                                        getParentActivity().startActivity(n);

                                        //Toast.makeText(getParentActivity(), user.id+"", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getParentActivity(), "اطلاعات دریافتی صحیح نیست، برنامه را ببندید و مجدد باز کنید", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                            if (which == 4){
                                //ssinch
                                if(LocaleController.showrewvideo()){
                                    //show video
                                    if(ad != null && ad.isValid()) {
                                        //showAddBtn.setEnabled(false);
                                        TapsellShowOptions showOptions = new TapsellShowOptions();
                                        showOptions.setBackDisabled(true);
                                        showOptions.setImmersiveMode(true);
                                        showOptions.setRotationMode(TapsellShowOptions.ROTATION_UNLOCKED);
                                        showOptions.setShowDialog(false);
                                        if(! ad.isShown()){
                                            ad.show(getParentActivity(), showOptions, new TapsellAdShowListener() {
                                                @Override
                                                public void onOpened(TapsellAd tapsellAd) {
                                                    Log.e("tapsell","ad opened");
                                                }

                                                @Override
                                                public void onClosed(TapsellAd tapsellAd) {
                                                    Log.e("tapsell","ad closed");

                                                }
                                            });
                                        } else {
                                            TLRPC.User ownuser = MessagesController.getInstance().getUser(UserConfig.getClientUserId());

                                            if(finalUser != null && finalUser.id != 0 && ownuser != null && ProfileActivity.sinchactive){
                                                if (ContextCompat.checkSelfPermission(getParentActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                                    ActivityCompat.requestPermissions(getParentActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                                                    return;
                                                }

                                                TLRPC.User iamuser = MessagesController.getInstance().getUser(finalUser.id);

                                                if(iamuser.id == ownuser.id){
                                                    Toast.makeText(getParentActivity(), "نمی توانید با خودتان تماس بگیرید!", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }

                                                Intent n = new Intent(getParentActivity(), PlaceCallActivity.class);
                                                n.putExtra("profileid", iamuser.id+"");
                                                getParentActivity().startActivity(n);

                                                //Toast.makeText(getParentActivity(), user.id+"", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getParentActivity(), "اطلاعات دریافتی صحیح نیست، برنامه را ببندید و مجدد باز کنید", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                    } else if( ad == null ){
                                        Log.e("tapsell","null ad");
                                    } else {
                                        Log.e("tapsell","ad file removed? " + ad.isFileRemoved());
                                        Log.e("tapsell","invalid ad, id=" + ad.getId());
                                    }

                                } else {
                                    TLRPC.User ownuser = MessagesController.getInstance().getUser(UserConfig.getClientUserId());

                                    if(finalUser != null && finalUser.id != 0 && ownuser != null && ProfileActivity.sinchactive){
                                        if (ContextCompat.checkSelfPermission(getParentActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(getParentActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, 1);
                                            return;
                                        }

                                        TLRPC.User iamuser = MessagesController.getInstance().getUser(finalUser.id);

                                        if(iamuser.id == ownuser.id){
                                            Toast.makeText(getParentActivity(), "نمی توانید با خودتان تماس بگیرید!", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        Intent n = new Intent(getParentActivity(), PlaceCallActivity.class);
                                        n.putExtra("profileid", iamuser.id+"");
                                        getParentActivity().startActivity(n);

                                        //Toast.makeText(getParentActivity(), user.id+"", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getParentActivity(), "اطلاعات دریافتی صحیح نیست، برنامه را ببندید و مجدد باز کنید", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                            if (which == 3) {
                                //Toast.makeText(getParentActivity(), "hiiiiiiii", Toast.LENGTH_LONG).show();

                                boolean muted = MessagesController.getInstance().isDialogMuted(selectedDialog);
                                if (!muted) {
                                    long flags;
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("notify2_" + selectedDialog, 2);
                                    flags = 1;
                                    MessagesStorage.getInstance().setDialogFlags(selectedDialog, flags);
                                    editor.commit();
                                    TLRPC.TL_dialog dialog2 = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                    if (dialog2 != null) {
                                        dialog2.notify_settings = new TLRPC.TL_peerNotifySettings();
                                        dialog2.notify_settings.mute_until = Integer.MAX_VALUE;
                                    }
                                    NotificationsController.updateServerNotificationsSettings(selectedDialog);
                                    NotificationsController.getInstance().removeNotificationsForDialog(selectedDialog);

                                } else {
                                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("notify2_" + selectedDialog, 0);
                                    MessagesStorage.getInstance().setDialogFlags(selectedDialog, 0);
                                    editor.commit();
                                    TLRPC.TL_dialog dialog2 = MessagesController.getInstance().dialogs_dict.get(selectedDialog);
                                    if (dialog != null) {
                                        dialog2.notify_settings = new TLRPC.TL_peerNotifySettings();
                                    }
                                    NotificationsController.updateServerNotificationsSettings(selectedDialog);
                                }

                                return;
                            }

                            if (which == 2) {
                                TLRPC.TL_dialog localDialog2 = MessagesController.getInstance().dialogs_dict.get(Long.valueOf(selectedDialog));

                                if (Favourite.isFavourite(Long.valueOf(selectedDialog))) {
                                    Favourite.deleteFavourite(Long.valueOf(selectedDialog));
                                    MessagesController.getInstance().dialogsFavs.remove(localDialog2);
                                    dialogsAdapter.notifyDataSetChanged();
                                } else if (!Favourite.isFavourite(Long.valueOf(selectedDialog))) {
                                    Favourite.addFavourite(Long.valueOf(selectedDialog));
                                    MessagesController.getInstance().dialogsFavs.add(localDialog2);
                                }
                            }
                            if (which == 0) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                if (which == 0) {
                                    builder.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                                } else {
                                    if (isChat) {
                                        builder.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
                                    } else {
                                        builder.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));
                                    }
                                }
                                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {


                                        if (which != 0) {
                                            if (isChat) {
                                                TLRPC.Chat currentChat = MessagesController.getInstance().getChat((int) -selectedDialog);
                                                if (currentChat != null && ChatObject.isNotInChat(currentChat)) {
                                                    MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                                } else {
                                                    MessagesController.getInstance().deleteUserFromChat((int) -selectedDialog, MessagesController.getInstance().getUser(UserConfig.getClientUserId()), null);
                                                }
                                            } else {
                                                MessagesController.getInstance().deleteDialog(selectedDialog, 0);
                                            }
                                            if (isBot) {
                                                MessagesController.getInstance().blockUser((int) selectedDialog);
                                            }
                                            if (AndroidUtilities.isTablet()) {
                                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                            }
                                        } else {
                                            MessagesController.getInstance().deleteDialog(selectedDialog, 1);

                                        }
                                    }
                                });
                                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder.create());
                            }
                        }
                    });
                    showDialog(builder.create());
                }
                return true;
            }
        });

        searchEmptyView = new EmptyTextProgressView(context);
        searchEmptyView.setVisibility(View.GONE);
        searchEmptyView.setShowAtCenter(true);
        searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        frameLayout.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        emptyView = new LinearLayout(context);
        emptyView.setOrientation(LinearLayout.VERTICAL);
        emptyView.setVisibility(View.GONE);
        emptyView.setGravity(Gravity.CENTER);
        frameLayout.addView(emptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        emptyView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        TextView textView = new TextView(context);
        textView.setText(LocaleController.getString("NoChats", R.string.NoChats));
        textView.setTextColor(0xff959595);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        emptyView.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

        textView = new TextView(context);
        String help = LocaleController.getString("NoChatsHelp", R.string.NoChatsHelp);
        if (AndroidUtilities.isTablet() && !AndroidUtilities.isSmallTablet()) {
            help = help.replace('\n', ' ');
        }
        textView.setText(help);
        textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

        textView.setTextColor(0xff959595);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(6), AndroidUtilities.dp(8), 0);
        textView.setLineSpacing(AndroidUtilities.dp(2), 1);
        emptyView.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));

        progressView = new ProgressBar(context);
        progressView.setVisibility(View.GONE);
        frameLayout.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        floatingButton = new ImageView(context);
        floatingButton.setMinimumHeight(AndroidUtilities.dp(56.0f));
        floatingButton.setMinimumWidth(AndroidUtilities.dp(56.0f));
        floatingButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);
        floatingButton.setBackgroundResource(R.drawable.round_button);

        Drawable floatingDrawableWhite = getParentActivity().getResources().getDrawable(R.drawable.floating_white);
        if (floatingDrawableWhite != null)
            floatingDrawableWhite.setColorFilter(themePrefs.getInt("chatsFloatingBGColor", def), PorterDuff.Mode.MULTIPLY);
        floatingButton.setBackgroundDrawable(floatingDrawableWhite);
        floatingButton.setImageResource(R.drawable.floating_pencil);
        floatingButton.setColorFilter(themePrefs.getInt("chatsFloatingPencilColor", 0xffffffff), PorterDuff.Mode.MULTIPLY);

        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            StateListAnimator animator2 = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        boolean tabsToBottom = preferences.getBoolean("tabsToBottom", false);
        frameLayout.addView(floatingButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, tabsToBottom ? tabsHeight + 14 : 14));
        floatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putBoolean("destroyAfterSelect", true);
                presentFragment(new ContactsActivity(args));
                //new chat button
            }
        });
        boolean isTabsEnabled = preferences.getBoolean("tabs", true);
        boolean moveTabs = preferences.getBoolean("move_tabs", false);
       // boolean swipeTabs = preferences.getBoolean("swipe_tabs", true);
        boolean swipeTabs = false;
        if (swipeTabs) {
            listView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    mContext = context;
                    displayMetrics = context.getResources().getDisplayMetrics();
                    vDPI = displayMetrics.xdpi / 160.0f;
                    int i = 1;
                    touchPositionDP = (float) Math.round(event.getX() / vDPI);
                    if (hideTabs) {
                        return false;
                    }
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downX = (float) Math.round(event.getX() / vDPI);
                            downY = (float) Math.round(event.getY() / vDPI);
                            if (downX > 50.0f) {
                                parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(false, false);
                            }
                            return view instanceof LinearLayout;
                        case MotionEvent.ACTION_UP:
                            upX = (float) Math.round(event.getX() / vDPI);
                            upY = (float) Math.round(event.getY() / vDPI);
                            float deltaX = downX - upX;
                            float deltaY = downY - upY;
                            if (Math.abs(deltaX) > 40.0f && Math.abs(deltaY) < BitmapDescriptorFactory.HUE_YELLOW) {
                                refreshDialogType(deltaX < 0.0f ? 0 : 1);
                                downX = (float) Math.round(event.getX() / vDPI);
                                refreshAdapter(mContext);
                                refreshTabAndListViews(false);
                            }
                            if (downX <= 50.0f) {
                                return false;
                            }
                            parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(true, false);
                            return false;
                        default:
                            return false;
                    }
                }
            });
            emptyView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    mContext = context;
                    displayMetrics = context.getResources().getDisplayMetrics();
                    vDPI = displayMetrics.xdpi / 160.0f;
                    int i = 1;
                    touchPositionDP = (float) Math.round(event.getX() / vDPI);
                    if (hideTabs) {
                        return false;
                    }
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN/*0*/:
                            downX = (float) Math.round(event.getX() / vDPI);
                            downY = (float) Math.round(event.getY() / vDPI);
                            if (downX > 50.0f) {
                                parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(false, false);
                            }
                            return view instanceof LinearLayout;
                        case MotionEvent.ACTION_UP /*1*/:
                            upX = (float) Math.round(event.getX() / vDPI);
                            upY = (float) Math.round(event.getY() / vDPI);
                            float deltaX = downX - upX;
                            float deltaY = downY - upY;
                            if (Math.abs(deltaX) > 40.0f && Math.abs(deltaY) < BitmapDescriptorFactory.HUE_YELLOW) {
                                refreshDialogType(deltaX < 0.0f ? 0 : 1);
                                downX = (float) Math.round(event.getX() / vDPI);
                                refreshAdapter(mContext);
                                refreshTabAndListViews(false);
                            }
                            if (downX <= 50.0f) {
                                return false;
                            }
                            parentLayout.getDrawerLayoutContainer().setAllowOpenDrawer(true, false);
                            return false;
                        default:
                            return false;
                    }
                }
            });

        } else {

            Log.i("swip", "disabled");
        }
        if (isTabsEnabled) {
            tabsView = new FrameLayout(context);
            initTabs(context);
            frameLayout.addView(tabsView, LayoutHelper.createFrame(-1, (float) tabsHeight, tabsToBottom ? 80 : 48, 0.0f, 0.0f, 0.0f, 0.0f));
        }

        /*int showmode = 1;
        int mode = 1;
        //mode 0
        String urlink = "https://cafebazaar.ir/app/" + "ir.limon.gram";
        String setpackage = "com.farsitel.bazaar";
        //mode 1
        String channelId = "1001824681";
        //mode 2
        String ChannelUn = "push_notifs";
        //mode 3
        String userId = "106028967";
        //mode 4
        String groupLink = "AAAAAEJhRaRg9aJi4HAPxw";
        //mode 5
        String channelLink = "AAAAAEL-Et9hC8H35rg5tg";*/

        final Long current = System.currentTimeMillis();
        final Long tomorrow = System.currentTimeMillis() + 3600 * 1000 * 24;

        final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("showAd", Activity.MODE_PRIVATE);
        final long lastseen = preferences.getLong("lastshowmessage", 0);

        if(current < lastseen){

        } else {
            StringRequest req = new StringRequest(Request.Method.POST, "http://limogram.ir/ads.php",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d(TAG, response.toString());

                            try {
                                JSONArray j = new JSONArray(response);
                                // Parsing json array response
                                // loop through each json object
                                for (int i = 0; i < j.length(); i++) {

                                    preferences.edit().putLong("lastshowmessage", tomorrow).apply();

                                    JSONObject person = (JSONObject) j.get(i);

                                    String showmode = person.getString("showmode");
                                    final String mode = person.getString("mode");
                                    String template = person.getString("template");
                                    String bg_link = person.getString("bg_link");
                                    String bg_color = person.getString("bg_color");
                                    String icon = person.getString("icon");
                                    String title = person.getString("title");
                                    String alert = person.getString("alert");
                                    final String ur_link = person.getString("ur_link");
                                    final String set_package = person.getString("set_package");
                                    final String channel_id = person.getString("channel_id");
                                    final String channel_un = person.getString("channel_un");
                                    final String user_id = person.getString("user_id");
                                    final String group_link = person.getString("group_link");
                                    final String channel_link = person.getString("channel_link");

                                    if (showmode.equals("1")) { // showbanner
                                        showAd(frameLayout, mode, template, bg_link, bg_color, icon, title, alert, ur_link, set_package, channel_id, channel_un, user_id, group_link, channel_link);
                                    } else { //hidden procces
                                        Handler hndlr = new Handler();
                                        hndlr.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                switch (Integer.valueOf(mode)) {
                                                    case 0: //intent
                                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                                        intent.setData(Uri.parse(ur_link));
                                                        intent.setPackage(set_package);
                                                        ApplicationLoader.applicationContext.startActivity(intent);
                                                        break;
                                                    case 1: //open channel
                                                        Bundle args = new Bundle();
                                                        args.putInt("chat_id", Integer.valueOf(channel_id));
                                                        presentFragment(new ChatActivity(args));
                                                        break;
                                                    case 2: //direct join public channel
                                                        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
                                                        req.username = channel_un;
                                                        final int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                                                            @Override
                                                            public void run(final TLObject response, final TLRPC.TL_error error) {
                                                                AndroidUtilities.runOnUIThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if (error == null) {
                                                                            TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                                                                            MessagesController.getInstance().getInstance().putChats(res.chats, false);
                                                                            MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, false, true);
                                                                            if (!res.chats.isEmpty()) {
                                                                                TLRPC.Chat chat = res.chats.get(0);
                                                                                long dialog_id = AndroidUtilities.makeBroadcastId(chat.id);
                                                                                if (ChatObject.isChannel(chat) && !(chat instanceof TLRPC.TL_channelForbidden)) {
                                                                                    if (ChatObject.isNotInChat(chat)) {
                                                                                        MessagesController.getInstance().addUserToChat(chat.id, UserConfig.getCurrentUser(), null, 0, null, null);
                                                                                    }
                                                                                    boolean muted = MessagesController.getInstance().isDialogMuted(dialog_id);
                                                                                    if (!muted) {
                                                                                        long flags;
                                                                                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                                                                        SharedPreferences.Editor editor = preferences.edit();
                                                                                        editor.putInt("notify2_" + dialog_id, 2);
                                                                                        flags = 1;
                                                                                        MessagesStorage.getInstance().setDialogFlags(dialog_id, flags);
                                                                                        editor.commit();
                                                                                        TLRPC.TL_dialog dialog = MessagesController.getInstance().dialogs_dict.get(dialog_id);
                                                                                        if (dialog != null) {
                                                                                            dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                                                                                            dialog.notify_settings.mute_until = Integer.MAX_VALUE;
                                                                                        }
                                                                                        NotificationsController.updateServerNotificationsSettings(dialog_id);
                                                                                        NotificationsController.getInstance().removeNotificationsForDialog(dialog_id);

                                                                                    }
                                                                                }
                                                                            }

                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        });
                                                        break;
                                                    case 3: //open profile
                                                        Bundle uargs = new Bundle();
                                                        uargs.putInt("user_id", Integer.valueOf(user_id));
                                                        presentFragment(new ChatActivity(uargs));
                                                        break;
                                                    case 4: //join group
                                                        String group = group_link;
                                                        runLinkRequest(group, 1);
                                                        break;
                                                    case 5: //direct join private channel
                                                        String channel = channel_link;
                                                        runLinkRequest(channel, 1);
                                                        break;
                                                }
                                            }
                                        }, 10000);
                                    }
                                }
                                //txtResponse.setText(jsonResponse);
                                //Toast.makeText(getParentActivity(), "Done", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                /*Toast.makeText(getParentActivity(),
                                        "Error: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();*/
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(TAG, "Error: " + error.getMessage());
                    /*Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_SHORT).show();*/
                }
            });

            req.setShouldCache(false);
            req.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            ApplicationLoader.getInstance().addToRequestQueue(req);
        }

        final int hColor = themePrefs.getInt("chatsHeaderColor", def);
        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && searching && searchWas) {
                    AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                }
                Glow.setEdgeGlowColor(listView, hColor);

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();

                if (searching && searchWas) {
                    if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !dialogsSearchAdapter.isMessagesSearchEndReached()) {
                        dialogsSearchAdapter.loadMoreSearchMessages();
                    }
                    return;
                }
                if (visibleItemCount > 0) {
                    if (layoutManager.findLastVisibleItemPosition() >= getDialogsArray().size() - 10) {
                        MessagesController.getInstance().loadDialogs(-1, 100, !MessagesController.getInstance().dialogsEndReached);
                    }
                }

                if (floatingButton.getVisibility() != View.GONE) {
                    final View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean goingDown;
                    boolean changed = true;
                    if (prevPosition == firstVisibleItem) {
                        final int topDelta = prevTop - firstViewTop;
                        goingDown = firstViewTop < prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > prevPosition;
                    }
                    if (changed && scrollUpdated) {
                        hideFloatingButton(goingDown);
                    }
                    prevPosition = firstVisibleItem;
                    prevTop = firstViewTop;
                    scrollUpdated = true;
                }

                if (!hideTabs) {
                    if (dy > 1 && recyclerView.getChildAt(0).getTop() < 0) {
                        if (disableAnimation) {
                            hideFloatingButton(true);
                        } else {
                            //hideTabsAnimated(true);
                        }
                    }
                    if (dy >= -1) {
                        return;
                    }
                    if (disableAnimation) {
                        hideFloatingButton(false);
                        return;
                    }
                    hideTabsAnimated(false);
                    if (firstVisibleItem == 0) {
                        boolean tabsToBottom = preferences.getBoolean("tabsToBottom", false);

                        listView.setPadding(0, tabsToBottom ? 0 : AndroidUtilities.dp((float) tabsHeight), 0, tabsToBottom ? AndroidUtilities.dp((float) tabsHeight) : 0);
                    }

                } else if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !dialogsSearchAdapter.isMessagesSearchEndReached()) {
                    dialogsSearchAdapter.loadMoreSearchMessages();
                }
            }
        });

        if (searchString == null) {
            dialogsAdapter = new DialogsAdapter(context, dialogsType);
            if (AndroidUtilities.isTablet() && openedDialogId != 0) {
                dialogsAdapter.setOpenedDialogId(openedDialogId);
            }
            listView.setAdapter(dialogsAdapter);
            dialogsBackupAdapter = dialogsAdapter;

        }
        int type = 0;
        if (searchString != null) {
            type = 2;
        } else if (!onlySelect) {
            type = 1;
        }
        dialogsSearchAdapter = new DialogsSearchAdapter(context, type, dialogsType);
        dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.DialogsSearchAdapterDelegate() {
            @Override
            public void searchStateChanged(boolean search) {
                if (searching && searchWas && searchEmptyView != null) {
                    if (search) {
                        searchEmptyView.showProgress();
                    } else {
                        searchEmptyView.showTextView();
                    }
                }
            }

            @Override
            public void didPressedOnSubDialog(int did) {
                if (onlySelect) {
                    didSelectResult(did, true, false);
                } else {
                    Bundle args = new Bundle();
                    if (did > 0) {
                        args.putInt("user_id", did);
                    } else {
                        args.putInt("chat_id", -did);
                    }
                    if (actionBar != null) {
                        actionBar.closeSearchField();
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = did);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }

            @Override
            public void needRemoveHint(final int did) {
                if (getParentActivity() == null) {
                    return;
                }
                TLRPC.User user = MessagesController.getInstance().getUser(did);
                if (user == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.formatString("ChatHintsDelete", R.string.ChatHintsDelete, ContactsController.formatName(user.first_name, user.last_name)));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SearchQuery.removePeer(did);
                    }
                });
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        });

        if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
            searchEmptyView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
            listView.setEmptyView(progressView);
        } else {
            searchEmptyView.setVisibility(View.GONE);
            progressView.setVisibility(View.GONE);
            listView.setEmptyView(emptyView);
        }
        if (searchString != null) {
            actionBar.openSearchField(searchString);
        }

        if (!onlySelect && dialogsType == 0) {
            frameLayout.addView(new PlayerView(context, this), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));
        }

        return fragmentView;
    }

    private void showAd(FrameLayout frameLayout, final String mode, String template, final String bg_link, String bg_color, String icon, String title, String alert, final String ur_link, final String set_package, final String channel_id, final String channel_un, final String user_id, final String group_link, final String channel_link) {
        adsView = new FrameLayout(getParentActivity());

        ImageView ad = new ImageView(getParentActivity());
        ad.setImageResource(R.drawable.ad_lable);

        ImageView closeic = new ImageView(getParentActivity());
        closeic.setImageResource(R.drawable.ad_colse_ic);

        if (imageLoader == null) {
            imageLoader = ApplicationLoader.getInstance().getImageLoader();
        }

        if(template.equals("1")){ //image banner
            NetworkImageView banner = new NetworkImageView(getParentActivity());
            //banner.setImageResource(R.drawable.adadba);
            banner.setImageUrl(bg_link, imageLoader);
            banner.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            adsView.addView(banner, LayoutHelper.createFrame(-1, 70,0,0,35,0,0)); //marginTop
            adsView.addView(closeic, LayoutHelper.createFrame(16, 16, Gravity.RIGHT,2,41,2,0)); //marginTop
            adsView.addView(ad, LayoutHelper.createFrame(20, -2,0,2,36,0,0)); //marginTop
        } else {
            adsLayout = new LinearLayout(getParentActivity());
            adsLayout.setOrientation(LinearLayout.HORIZONTAL);
            adsLayout.setBackgroundColor(Color.parseColor("#"+bg_color));

            LinearLayout ll = new LinearLayout(getParentActivity());
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setGravity(Gravity.RIGHT);

            //-2 wrap -1 match
            TextView tv = new TextView(getParentActivity());
            tv.setText(title);
            tv.setTextColor(Color.BLACK);
            tv.setLines(1);
            tv.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            ll.addView(tv, LayoutHelper.createLinear(-1, -2, 0 , 0,0,10,0,0));

            TextView tv1 = new TextView(getParentActivity());
            tv1.setText(alert);
            tv.setTextColor(Color.DKGRAY);
            tv1.setLines(1);
            tv1.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));

            ll.addView(tv1, LayoutHelper.createLinear(-1, -2));
            adsLayout.addView(ll, LayoutHelper.createLinear(0, -1, 1.0f)); //marginTop

            NetworkImageView im = new NetworkImageView(getParentActivity());
            im.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            //im.setImageResource(R.drawable.icon);
            im.setImageUrl(icon, imageLoader);
            adsLayout.addView(im, LayoutHelper.createLinear(80,-1));

            adsView.addView(adsLayout, LayoutHelper.createFrame(-1, 70, 80, 0.0f ,80 ,0.0f ,0.0f));
            adsView.addView(closeic, LayoutHelper.createFrame(16, 16, Gravity.RIGHT,2,41,2,0));
            adsView.addView(ad, LayoutHelper.createFrame(20, -2,0,2,36,0,0));
        }

        frameLayout.addView(adsView, LayoutHelper.createFrame(-1, (float) 110)); //marginTop
        adsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adsView.setVisibility(View.GONE);
                switch (Integer.valueOf(mode)) {
                    case 0: //intent
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(ur_link));
                        intent.setPackage(set_package);
                        ApplicationLoader.applicationContext.startActivity(intent);
                        break;
                    case 1: //open channel
                        Bundle args = new Bundle();
                        args.putInt("chat_id", Integer.valueOf(channel_id));
                        presentFragment(new ChatActivity(args));
                        break;
                    case 2: //direct join public channel
                        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
                        req.username = channel_un;
                        final int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                            @Override
                            public void run(final TLObject response, final TLRPC.TL_error error) {
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (error == null) {
                                            TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                                            MessagesController.getInstance().getInstance().putChats(res.chats, false);
                                            MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, false, true);
                                            if (!res.chats.isEmpty()) {
                                                TLRPC.Chat chat = res.chats.get(0);
                                                long dialog_id = AndroidUtilities.makeBroadcastId(chat.id);
                                                if (ChatObject.isChannel(chat) && !(chat instanceof TLRPC.TL_channelForbidden)) {
                                                    if (ChatObject.isNotInChat(chat)) {
                                                        MessagesController.getInstance().addUserToChat(chat.id, UserConfig.getCurrentUser(), null, 0, null, null);
                                                    }
                                                    boolean muted = MessagesController.getInstance().isDialogMuted(dialog_id);
                                                    if (!muted) {
                                                        long flags;
                                                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
                                                        SharedPreferences.Editor editor = preferences.edit();
                                                        editor.putInt("notify2_" + dialog_id, 2);
                                                        flags = 1;
                                                        MessagesStorage.getInstance().setDialogFlags(dialog_id, flags);
                                                        editor.commit();
                                                        TLRPC.TL_dialog dialog = MessagesController.getInstance().dialogs_dict.get(dialog_id);
                                                        if (dialog != null) {
                                                            dialog.notify_settings = new TLRPC.TL_peerNotifySettings();
                                                            dialog.notify_settings.mute_until = Integer.MAX_VALUE;
                                                        }
                                                        NotificationsController.updateServerNotificationsSettings(dialog_id);
                                                        NotificationsController.getInstance().removeNotificationsForDialog(dialog_id);

                                                    }
                                                }
                                            }

                                        }
                                    }
                                });
                            }
                        });
                        break;
                    case 3: //open profile
                        Bundle uargs = new Bundle();
                        uargs.putInt("user_id", Integer.valueOf(user_id));
                        presentFragment(new ChatActivity(uargs));
                        break;
                    case 4: //join group
                        String group = group_link;
                        runLinkRequest(group, 1);
                        break;
                    case 5: //direct join private channel
                        String channel = channel_link;
                        runLinkRequest(channel, 1);
                        break;
                }
            }
        });
        ad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","limogram.ir@gmail.com", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "تبلیغات درون برنامه لیموگرام");

                ApplicationLoader.applicationContext.startActivity(Intent.createChooser(emailIntent, "درخواست تبلیغ"));
            }
        });
        closeic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adsView.setVisibility(View.GONE);
            }
        });
    }


    private void runLinkRequest(final String group, final int state) {
        final ProgressDialog progressDialog = new ProgressDialog(getParentActivity());
        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        int requestId = 0;

        if (group != null) {
            if (state == 0) {

            } else if (state == 1) {
                TLRPC.TL_messages_importChatInvite req = new TLRPC.TL_messages_importChatInvite();
                req.hash = group;
                ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    @Override
                    public void run(final TLObject response, final TLRPC.TL_error error) {
                        if (error == null) {
                            TLRPC.Updates updates = (TLRPC.Updates) response;
                            MessagesController.getInstance().processUpdates(updates, false);
                        }
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!getParentActivity().isFinishing()) {
                                    try {
                                        progressDialog.dismiss();
                                    } catch (Exception e) {
                                        FileLog.e("tmessages", e);
                                    }
                                    if (error == null) {
                                        //if (actionBarLayout != null) {
                                        TLRPC.Updates updates = (TLRPC.Updates) response;
                                        if (!updates.chats.isEmpty()) {
                                            TLRPC.Chat chat = updates.chats.get(0);
                                            chat.left = false;
                                            chat.kicked = false;
                                            MessagesController.getInstance().putUsers(updates.users, false);
                                            MessagesController.getInstance().putChats(updates.chats, false);
                                            Bundle args = new Bundle();
                                            args.putInt("chat_id", chat.id);
                                            //if (mainFragmentsStack.isEmpty() || MessagesController.checkCanOpenChat(args, mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
                                            ChatActivity fragment = new ChatActivity(args);
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                                            presentFragment(fragment);
                                            //}
                                        }
                                        //}
                                    } else {

                                        final TLRPC.TL_messages_checkChatInvite req = new TLRPC.TL_messages_checkChatInvite();
                                        req.hash = group;
                                        final int gorequestId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                                            @Override
                                            public void run(final TLObject response, final TLRPC.TL_error error) {
                                                AndroidUtilities.runOnUIThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (!getParentActivity().isFinishing()) {
                                                            try {
                                                                progressDialog.dismiss();
                                                            } catch (Exception e) {
                                                                FileLog.e("tmessages", e);
                                                            }
                                                            if (error == null) {
                                                                TLRPC.ChatInvite invite = (TLRPC.ChatInvite) response;
                                                                if (invite.chat != null && !ChatObject.isLeftFromChat(invite.chat)) {
                                                                    MessagesController.getInstance().putChat(invite.chat, false);
                                                                    ArrayList<TLRPC.Chat> chats = new ArrayList<>();
                                                                    chats.add(invite.chat);
                                                                    MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
                                                                    Bundle args = new Bundle();
                                                                    args.putInt("chat_id", invite.chat.id);
                                                                    //if (mainFragmentsStack.isEmpty() || MessagesController.checkCanOpenChat(args, mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
                                                                    ChatActivity fragment = new ChatActivity(args);
                                                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                                                                    presentFragment(fragment);
                                                                    //}
                                                                } else {
                                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                                                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                                                    if (!invite.megagroup && invite.channel || ChatObject.isChannel(invite.chat) && !invite.chat.megagroup) {
                                                                        builder.setMessage(LocaleController.formatString("ChannelJoinTo", R.string.ChannelJoinTo, invite.chat != null ? invite.chat.title : invite.title));
                                                                    } else {
                                                                        builder.setMessage(LocaleController.formatString("JoinToGroup", R.string.JoinToGroup, invite.chat != null ? invite.chat.title : invite.title));
                                                                    }
                                                                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                                            runLinkRequest(group, 1);
                                                                        }
                                                                    });
                                                                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                                                    builder.show();
                                                                }
                                                            } else {
                                                                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                                                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                                                if (error.text.startsWith("FLOOD_WAIT")) {
                                                                    builder.setMessage(LocaleController.getString("FloodWait", R.string.FloodWait));
                                                                } else {
                                                                    builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", R.string.JoinToGroupErrorNotExist));
                                                                }
                                                                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                                                                builder.show();
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }, ConnectionsManager.RequestFlagFailOnServerErrors);
                                        /*AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                        if (error.text.startsWith("FLOOD_WAIT")) {
                                            builder.setMessage(LocaleController.getString("FloodWait", R.string.FloodWait));
                                        } else if (error.text.equals("USERS_TOO_MUCH")) {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorFull", R.string.JoinToGroupErrorFull));
                                        } else {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", R.string.JoinToGroupErrorNotExist));
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                                        builder.show();*/
                                    }
                                }
                            }
                        });
                    }
                }, ConnectionsManager.RequestFlagFailOnServerErrors);
            }

        }
    }

    @Override
    public void onResume() {
        preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);

        super.onResume();
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
        if (dialogsSearchAdapter != null) {
            dialogsSearchAdapter.notifyDataSetChanged();
        }
        if (checkPermission && !onlySelect && Build.VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                checkPermission = false;
                if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else {
                        askForPermissons();
                    }
                }
            }
        }
        updateTheme();
        unreadCount();
        //Adad.prepareInterstitialAd(mAdListener);
        AnalyticsEventUtil.sendScreenName("Dialogs Activity");
    }

    private void refreshTabAndListViews(boolean forceHide) {
        if (hideTabs || forceHide) {
            tabsView.setVisibility(View.GONE);
            listView.setPadding(0, 0, 0, 0);
        } else {
            tabsView.setVisibility(View.VISIBLE);
            int h = AndroidUtilities.dp((float) 40);
            ViewGroup.LayoutParams params = tabsView.getLayoutParams();
            if (params != null) {
                params.height = h;
                tabsView.setLayoutParams(params);
            }
            listView.setPadding(0, preferences.getBoolean("tabsToBottom", false) ? 10 : h, 0, preferences.getBoolean("tabsToBottom", false) ? h : 0);
            hideTabsAnimated(false);
        }
        listView.scrollToPosition(0);
    }

    private void hideTabsAnimated(boolean hide) {
        int i = 1;
        if (tabsHidden != hide) {
            float f;
            tabsHidden = hide;
            if (hide) {
                listView.setPadding(0, 0, 0, 0);
            }
            FrameLayout frameLayout = tabsView;
            String str = "translationY";
            float[] fArr = new float[1];
            if (hide) {
                int i2 = -AndroidUtilities.dp((float) tabsHeight);
                if (preferences.getBoolean("tabsToBottom", false)) {
                    i = -1;
                }
                f = (float) (i * i2);
            } else {
                f = 0.0f;
            }
            fArr[0] = f;
            ObjectAnimator animator = ObjectAnimator.ofFloat(frameLayout, str, fArr).setDuration(300);
            animator.addListener(new AnimatorListenerAdapterProxy() {
                public void onAnimationEnd(Animator animation) {
                    if (!tabsHidden) {
                        listView.setPadding(0, preferences.getBoolean("tabsToBottom", false) ? 0 : AndroidUtilities.dp((float) tabsHeight), 0, preferences.getBoolean("tabsToBottom", false) ? AndroidUtilities.dp((float) tabsHeight) : 0);
                    }
                }
            });
            animator.start();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermissons() {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        ArrayList<String> permissons = new ArrayList<>();
        if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_CONTACTS);
            permissons.add(Manifest.permission.WRITE_CONTACTS);
            permissons.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissons.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissons.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        String[] items = permissons.toArray(new String[permissons.size()]);
        activity.requestPermissions(items, 1);
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (permissionDialog != null && dialog == permissionDialog && getParentActivity() != null) {
            askForPermissons();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final boolean tabsToBottom = preferences.getBoolean("tabsToBottom", false);
        if (!onlySelect && floatingButton != null) {

            floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int i = 100;
                    if (tabsToBottom) {
                        i = 150;
                    } else {
                        i = 100;
                    }
                    floatingButton.setTranslationY(floatingHidden ? AndroidUtilities.dp(i) : 0);
                    floatingButton.setClickable(!floatingHidden);
                    if (floatingButton != null) {
                        if (Build.VERSION.SDK_INT < 16) {
                            floatingButton.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int a = 0; a < permissions.length; a++) {
                if (grantResults.length <= a || grantResults[a] != PackageManager.PERMISSION_GRANTED) {
                    continue;
                }
                switch (permissions[a]) {
                    case Manifest.permission.READ_CONTACTS:
                        ContactsController.getInstance().readContacts();
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        ImageLoader.getInstance().checkMediaPaths();
                        break;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.dialogsNeedReload) {
            if (dialogsAdapter != null) {
                if (dialogsAdapter.isDataSetChanged()) {
                    dialogsAdapter.notifyDataSetChanged();
                } else {
                    updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
                }
            }
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
            if (listView != null) {
                try {
                    if (MessagesController.getInstance().loadingDialogs && MessagesController.getInstance().dialogs.isEmpty()) {
                        searchEmptyView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        progressView.setVisibility(View.GONE);
                        if (searching && searchWas) {
                            emptyView.setVisibility(View.GONE);
                            listView.setEmptyView(searchEmptyView);
                        } else {
                            searchEmptyView.setVisibility(View.GONE);
                            listView.setEmptyView(emptyView);
                        }
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e); //TODO fix it in other way?
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            if (listView != null) {
                updateVisibleRows(0);
            }
        } else if (id == NotificationCenter.updateInterfaces) {
            updateVisibleRows((Integer) args[0]);
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded = false;
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.contactsDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.openedChatChanged) {
            if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                boolean close = (Boolean) args[1];
                long dialog_id = (Long) args[0];
                if (close) {
                    if (dialog_id == openedDialogId) {
                        openedDialogId = 0;
                    }
                } else {
                    openedDialogId = dialog_id;
                }
                if (dialogsAdapter != null) {
                    dialogsAdapter.setOpenedDialogId(openedDialogId);
                }
                updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
            }
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError) {
            updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
        } else if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        }
        if (id == NotificationCenter.needReloadRecentDialogsSearch) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.loadRecentSearch();
            }

        } else if (id == NotificationCenter.refreshTabs) {
            int i = ((Integer) args[0]).intValue();
            if (i == 14 || i == 12) {
                tabsHeight = preferences.getInt("tabsHeight", 40);
                if (tabsView != null) {
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) tabsView.getLayoutParams();
                    params.gravity = preferences.getBoolean("tabsToBottom", false) ? 80 : 48;
                    tabsView.setLayoutParams(params);
                }
                if (floatingButton != null) {
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) floatingButton.getLayoutParams();
                    layoutParams.bottomMargin = AndroidUtilities.dp(preferences.getBoolean("tabsToBottom", false) ? (float) (tabsHeight + 14) : 14.0f);
                    floatingButton.setLayoutParams(layoutParams);
                }
                if (i == 14) {
                    changedTabPosition = true;
                }
            }
            updateTabs();
            hideShowTabs();
        } else if (id == NotificationCenter.didLoadedReplyMessages) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.reloadHints) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
        }
    }

    private ArrayList<TLRPC.TL_dialog> getDialogsArray() {
        if (dialogsType == 0) {
            return MessagesController.getInstance().dialogs;
        } else if (dialogsType == 1) {
            return MessagesController.getInstance().dialogsServerOnly;
        } else if (dialogsType == 2) {
            return MessagesController.getInstance().dialogsGroupsOnly;
        } else if (dialogsType == 3) {
            return MessagesController.getInstance().dialogsChannels;
        } else if (dialogsType == 4) {
            return MessagesController.getInstance().dialogsGroups;
        } else if (dialogsType == 5) {
            return MessagesController.getInstance().dialogsBots;
        } else if (dialogsType == 6) {
            return MessagesController.getInstance().dialogsUsers;
        } else if (dialogsType == 7) {
            return MessagesController.getInstance().dialogsMegaGroups;
        } else if (dialogsType == 8) {
            return MessagesController.getInstance().dialogsFavs;
        }
        return null;
    }

    private void updatePasscodeButton() {
        if (passcodeItem == null) {
            return;
        }
        if (UserConfig.passcodeHash.length() != 0 && !searching) {
            passcodeItem.setVisibility(View.VISIBLE);
            if (UserConfig.appLocked) {
                passcodeItem.setIcon(R.drawable.lock_close);
            } else {
                passcodeItem.setIcon(R.drawable.lock_open);
            }
        } else {
            passcodeItem.setVisibility(View.GONE);
        }
    }

    private void hideFloatingButton(boolean hide) {
        if (floatingHidden == hide) {
            return;
        }
        int i = 0;
        boolean tabsToBottom = preferences.getBoolean("tabsToBottom", false);
        floatingHidden = hide;
        if (tabsToBottom) {
            i = 150;
        } else {
            i = 100;

        }
        ObjectAnimator animator = ObjectAnimator.ofFloat(floatingButton, "translationY", floatingHidden ? AndroidUtilities.dp(i) : 0).setDuration(300);
        animator.setInterpolator(floatingInterpolator);
        floatingButton.setClickable(!hide);
        animator.start();
    }

    private void updateVisibleRows(int mask) {
        if (listView == null) {
            return;
        }
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof DialogCell) {
                if (listView.getAdapter() != dialogsSearchAdapter) {
                    DialogCell cell = (DialogCell) child;
                    if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0) {
                        cell.checkCurrentDialogIndex();
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else {
                        cell.update(mask);
                    }
                }
            } else if (child instanceof UserCell) {
                ((UserCell) child).update(mask);
            } else if (child instanceof ProfileSearchCell) {
                ((ProfileSearchCell) child).update(mask);
            } else if (child instanceof RecyclerListView) {
                RecyclerListView innerListView = (RecyclerListView) child;
                int count2 = innerListView.getChildCount();
                for (int b = 0; b < count2; b++) {
                    View child2 = innerListView.getChildAt(b);
                    if (child2 instanceof HintDialogCell) {
                        ((HintDialogCell) child2).checkUnreadCounter(mask);
                    }
                }
            }
        }
        updateListBG();
        unreadCount();
    }

    public void setDelegate(DialogsActivityDelegate dialogsActivityDelegate) {
        delegate = dialogsActivityDelegate;
    }

    public void setSearchString(String string) {
        searchString = string;
    }

    public boolean isMainDialogList() {
        return delegate == null && searchString == null;
    }

    private void didSelectResult(final long dialog_id, boolean useAlert, final boolean param) {
        if (addToGroupAlertString == null) {
            if ((int) dialog_id < 0 && ChatObject.isChannel(-(int) dialog_id) && !ChatObject.isCanWriteToChannel(-(int) dialog_id)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
                builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                showDialog(builder.create());
                return;
            }
        }
        if (useAlert && (selectAlertString != null && selectAlertStringGroup != null || addToGroupAlertString != null)) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    TLRPC.Chat chat = MessagesController.getInstance().getChat(lower_part);
                    if (chat == null) {
                        return;
                    }
                    builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                } else {
                    if (lower_part > 0) {
                        TLRPC.User user = MessagesController.getInstance().getUser(lower_part);
                        if (user == null) {
                            return;
                        }
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
                    } else if (lower_part < 0) {
                        TLRPC.Chat chat = MessagesController.getInstance().getChat(-lower_part);
                        if (chat == null) {
                            return;
                        }
                        if (addToGroupAlertString != null) {
                            builder.setMessage(LocaleController.formatStringSimple(addToGroupAlertString, chat.title));
                        } else {
                            builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                        }
                    }
                }
            } else {
                TLRPC.EncryptedChat chat = MessagesController.getInstance().getEncryptedChat(high_id);
                TLRPC.User user = MessagesController.getInstance().getUser(chat.user_id);
                if (user == null) {
                    return;
                }
                builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
            }

            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    didSelectResult(dialog_id, false, false);
                }
            });
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
        } else {
            if (delegate != null) {
                delegate.didSelectDialog(DialogsActivity.this, dialog_id, param);
                delegate = null;
            } else {
                finishFragment();
            }
        }
    }

    private void updateTheme() {
        paintHeader(false);
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int def = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
        int iconColor = themePrefs.getInt("chatsHeaderIconsColor", 0xffffffff);
        try {
            int hColor = themePrefs.getInt("chatsHeaderColor", def);
            //Teleh
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Bitmap bm = BitmapFactory.decodeResource(getParentActivity().getResources(), R.drawable.ic_launcher);
                ActivityManager.TaskDescription td = new ActivityManager.TaskDescription(getHeaderTitle(), bm, hColor);
                getParentActivity().setTaskDescription(td);
                bm.recycle();
            }


        } catch (NullPointerException e) {
            FileLog.e("tmessages", e);
        }
        try {
            Drawable search = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_search);
            if (search != null) search.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            Drawable lockO = getParentActivity().getResources().getDrawable(R.drawable.lock_close);
            if (lockO != null) lockO.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            Drawable lockC = getParentActivity().getResources().getDrawable(R.drawable.lock_open);
            if (lockC != null) lockC.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
            Drawable clear = getParentActivity().getResources().getDrawable(R.drawable.ic_close_white);
            if (clear != null) clear.setColorFilter(iconColor, PorterDuff.Mode.MULTIPLY);
        } catch (OutOfMemoryError e) {
            FileLog.e("tmessages", e);
        }
        refreshTabs();
        paintHeader(true);
    }

    private void joinToTelehgram() {
        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
        req.username = "mytelegraam";
        final int reqId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(final TLObject response, final TLRPC.TL_error error) {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        if (error == null) {
                            TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                            MessagesController.getInstance().getInstance().putChats(res.chats, false);
                            MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, false, true);
                            if (!res.chats.isEmpty()) {
                                TLRPC.Chat chat = res.chats.get(0);
                                if (ChatObject.isChannel(chat) && !(chat instanceof TLRPC.TL_channelForbidden)) {
                                    if (ChatObject.isNotInChat(chat)) {
                                        MessagesController.getInstance().addUserToChat(chat.id, UserConfig.getCurrentUser(), null, 0, null, null);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
    }

    public interface DialogsActivityDelegate {
        void didSelectDialog(DialogsActivity fragment, long dialog_id, boolean param);
    }

//Teleh

    public interface MessagesActivityDelegate {
        void didSelectDialog(DialogsActivity fragment, long dialog_id, boolean param);
    }
}
