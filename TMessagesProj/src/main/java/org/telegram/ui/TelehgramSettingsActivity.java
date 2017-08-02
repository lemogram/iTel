/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.telegram.ui;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MediaController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Analytics.AnalyticsEventUtil;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Cells.EmptyCell;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Telehgram.FontSelectActivity;
import org.telegram.ui.Telehgram.theming.ThemingActivity;
import org.telegram.ui.Telehgram.theming.market.ThemeMarket;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class TelehgramSettingsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate, PhotoViewer.PhotoViewerProvider {

    private final static int edit_name = 1;
    private final static int logout = 2;
    ArrayList<Integer> options = new ArrayList();
    private int TypeCount = 8;
    private ListView listView;
    private ListAdapter listAdapter;
    private BackupImageView avatarImage;
    private TextView nameTextView;
    private TextView onlineTextView;
    private View extraHeightView;
    private View shadowView;
    private int fontDesRow;
    private int fontRow;
    private int hiddenTabsRow;
    private int defaultTabRow;
    private int directForwardRow;
    private int enableTabletMode;
    private int enable24HourFormat;
    private int PersianDateRow;
    private int ThemesRow;
    private int telehThemeRow;
    private int hidePhoneRow;
    private int extraHeight;
    private int swipeTabRow;
    private int overscrollRow;
    private int emptyRow;
    private int settingsSectionRow;
    private int settingsSectionRow2;
    private int directShareRow;
    private int supportSectionRow;
    private int supportSectionRow2;
    private int askQuestionRow;
    private int telegramFaqRow;
    private int sendLogsRow;
    private int switchBackendButtonRow;
    private int versionRow;
    private int contactsSectionRow;
    private int contactsReimportRow;
    private int contactsSortRow;
    private int typingStatusRow;
    private int confirmatinAudioRow;
    private int previewStickerRow;
    private int showMutualRow;
    private int ghostModeRow;
    private int showGhostModeRow;
    private int showExactCountRow;
    private int enableRTLRow;
    private int moveTabsToBottom;
    private int rowCount;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.updateInterfaces);
        rowCount = 0;
        overscrollRow = rowCount++;
        emptyRow = rowCount++;
        settingsSectionRow = rowCount++;
        settingsSectionRow2 = rowCount++;
        enableTabletMode = rowCount++;
        enable24HourFormat = rowCount++;
        PersianDateRow = rowCount++;
        fontRow = rowCount++;
        // passwordRow = rowCount++;
        ThemesRow = rowCount++;
        telehThemeRow = rowCount++;
        hidePhoneRow = rowCount++;
        showGhostModeRow = rowCount++;
        ghostModeRow = rowCount++;
        showExactCountRow = rowCount++;
        enableRTLRow = rowCount++;
        moveTabsToBottom = rowCount++;
        typingStatusRow = rowCount++;
        showMutualRow = rowCount++;
        confirmatinAudioRow = rowCount++;
        previewStickerRow = rowCount++;
        directForwardRow = rowCount++;
        swipeTabRow = rowCount++;
        hiddenTabsRow = rowCount++;
        defaultTabRow = rowCount++;
        if (Build.VERSION.SDK_INT >= 23) {
            directShareRow = rowCount++;
        }
        supportSectionRow = rowCount++;
        supportSectionRow2 = rowCount++;
        askQuestionRow = rowCount++;
        telegramFaqRow = rowCount++;
        if (BuildVars.DEBUG_VERSION) {
            sendLogsRow = rowCount++;
            switchBackendButtonRow = rowCount++;
        }
        versionRow = rowCount++;
        //contactsSectionRow = rowCount++;
        //contactsReimportRow = rowCount++;
        //contactsSortRow = rowCount++;

        MessagesController.getInstance().loadFullUser(UserConfig.getCurrentUser(), classGuid, true);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (avatarImage != null) {
            avatarImage.setImageDrawable(null);
        }
        MessagesController.getInstance().cancelLoadFullUser(UserConfig.getClientUserId());
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.updateInterfaces);
    }

    @Override
    public View createView(final Context context) {
        actionBar.setBackgroundColor(AvatarDrawable.getProfileBackColorForId(5));
        actionBar.setItemsBackgroundColor(AvatarDrawable.getButtonColorForId(5));
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setAddToContainer(false);
        extraHeight = 88;
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == edit_name) {
                    presentFragment(new ChangeNameActivity());
                } else if (id == logout) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setMessage(LocaleController.getString("AreYouSureLogout", R.string.AreYouSureLogout));
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MessagesController.getInstance().performLogout(true);
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                }
            }
        });
        ActionBarMenu menu = actionBar.createMenu();
        //Teleh
        Drawable other = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_other);
        ActionBarMenuItem item = menu.addItem(0, other);
        //End Teleh
        item.addSubItem(edit_name, LocaleController.getString("EditName", R.string.EditName), 0);
        item.addSubItem(logout, LocaleController.getString("LogOut", R.string.LogOut), 0);

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context) {
            @Override
            protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
                if (child == listView) {
                    boolean result = super.drawChild(canvas, child, drawingTime);
                    if (parentLayout != null) {
                        int actionBarHeight = 0;
                        int childCount = getChildCount();
                        for (int a = 0; a < childCount; a++) {
                            View view = getChildAt(a);
                            if (view == child) {
                                continue;
                            }
                            if (view instanceof ActionBar && view.getVisibility() == VISIBLE) {
                                if (((ActionBar) view).getCastShadows()) {
                                    actionBarHeight = view.getMeasuredHeight();
                                }
                                break;
                            }
                        }
                        parentLayout.drawHeaderShadow(canvas, actionBarHeight);
                    }
                    return result;
                } else {
                    return super.drawChild(canvas, child, drawingTime);
                }
            }
        };
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new ListView(context);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        AndroidUtilities.setListViewEdgeEffectColor(listView, AvatarDrawable.getProfileBackColorForId(5));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                if (i == PersianDateRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    boolean persianDate = preferences.getBoolean("persian_date", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor = preferences.edit();
                    editor.putBoolean("persian_date", !persianDate);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!persianDate);
                        return;
                    }
                    return;

                }
                if (i == swipeTabRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Context.MODE_PRIVATE);
                    boolean swipeTabs = preferences.getBoolean("swipe_tabs", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("swipe_tabs", !swipeTabs);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!swipeTabs);
                    }
                    if (AndroidUtilities.isTablet()) {
                        reLunchApp();
                        return;
                    } else {
                        restartApp();
                        return;
                    }

                }
                if (i == hiddenTabsRow) {
                    if (getParentActivity() != null) {
                        final boolean[] maskValues = new boolean[9];
                        BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                        builder.setApplyTopPadding(false);
                        LinearLayout linearLayout = new LinearLayout(getParentActivity());
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Context.MODE_PRIVATE);
                        for (int a = 0; a < 6; a++) {
                            String name = null;
                            name = null;
                            if (a == 0) {
                                name = LocaleController.getString("FavoriteTab", R.string.FavoriteTab);
                                maskValues[a] = preferences.getBoolean("hideFavs", false);
                            } else if (a == 1) {
                                name = LocaleController.getString("ContactTab", R.string.ContactTab);
                                maskValues[a] = preferences.getBoolean("hideUsers", false);
                            } else if (a == 2) {
                                name = LocaleController.getString("GroupsTab", R.string.GroupsTab);
                                maskValues[a] = preferences.getBoolean("hideGroups", false);
                            } else if (a == 3) {
                                name = LocaleController.getString("SGroupsTab", R.string.SGroupsTab);
                                maskValues[a] = preferences.getBoolean("hideSGroups", false);
                            } else if (a == 4) {
                                name = LocaleController.getString("ChannelTab", R.string.ChannelTab);
                                maskValues[a] = preferences.getBoolean("hideChannels", false);
                            } else if (a == 5) {
                                name = LocaleController.getString("RobotTab", R.string.RobotTab);
                                maskValues[a] = preferences.getBoolean("hideBots", false);
                            }
                            CheckBoxCell checkBoxCell = new CheckBoxCell(getParentActivity());
                            checkBoxCell.setTag(Integer.valueOf(a));
                            checkBoxCell.setBackgroundResource(R.drawable.list_selector);
                            linearLayout.addView(checkBoxCell, LayoutHelper.createLinear(-1, 48));
                            checkBoxCell.setText(name, "", maskValues[a], false);
                            checkBoxCell.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CheckBoxCell cell = (CheckBoxCell) v;
                                    int num = (Integer) cell.getTag();
                                    maskValues[num] = !maskValues[num];
                                    cell.setChecked(maskValues[num], true);
                                }
                            });
                        }
                        BottomSheet.BottomSheetCell cell = new BottomSheet.BottomSheetCell(getParentActivity(), 1);
                        cell.setBackgroundResource(R.drawable.list_selector);
                        cell.setTextAndIcon(LocaleController.getString("Save", R.string.Save).toUpperCase(), 0);
                        cell.setTextColor(Theme.AUTODOWNLOAD_SHEET_SAVE_TEXT_COLOR);
                        cell.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Context.MODE_PRIVATE).edit();
                                try {
                                    if (visibleDialog != null) {
                                        visibleDialog.dismiss();
                                    }
                                } catch (Throwable e) {
                                    FileLog.e("tmessages", e);
                                }
                                int tabCount = 1;
                                for (int a = 0; a < 6; a++) {
                                    if (a == 0) {
                                        editor.putBoolean("hideFavs", maskValues[a]);
                                        editor.commit();
                                        if (maskValues[a]) {
                                            tabCount++;
                                        }
                                    } else if (a == 1) {
                                        editor.putBoolean("hideUsers", maskValues[a]);
                                        editor.commit();
                                        if (maskValues[a]) {
                                            tabCount++;
                                        }
                                    } else if (a == 2) {
                                        editor.putBoolean("hideGroups", maskValues[a]);
                                        editor.commit();
                                        if (maskValues[a]) {
                                            tabCount++;
                                        }
                                    } else if (a == 3) {
                                        editor.putBoolean("hideSGroups", maskValues[a]);
                                        editor.commit();
                                        if (maskValues[a]) {
                                            tabCount++;
                                        }
                                    } else if (a == 4) {
                                        editor.putBoolean("hideChannels", maskValues[a]);
                                        editor.commit();
                                        if (maskValues[a]) {
                                            tabCount++;
                                        }
                                    } else if (a == 5) {
                                        editor.putBoolean("hideBots", maskValues[a]);
                                        editor.commit();
                                        if (maskValues[a]) {
                                            tabCount++;
                                        }
                                    }
                                }
                                editor.putInt("tab_count", tabCount);
                                editor.commit();
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                                processSelectedOption(preferences.getInt("defTab", 0));
                                restartApp();
                            }
                        });
                        linearLayout.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, Gravity.TOP));
                        builder.setCustomView(linearLayout);
                        showDialog(builder.create());
                        return;
                    }
                    return;
                }
                if (i == defaultTabRow) {
                    AlertDialog.Builder builder2 = new AlertDialog.Builder(getParentActivity());
                    ArrayList<CharSequence> items = new ArrayList();
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    items.add(LocaleController.getString("AllTab", R.string.AllTab));
                    options.add(0);
                    if (!preferences.getBoolean("hideFavs", false)) {
                        items.add(LocaleController.getString("FavoriteTab", R.string.FavoriteTab));
                        options.add(8);
                    }
                    if (!preferences.getBoolean("hideUsers", false)) {
                        items.add(LocaleController.getString("ContactTab", R.string.ContactTab));
                        options.add(6);
                    }
                    if (!preferences.getBoolean("hideGroups", false)) {
                        items.add(LocaleController.getString("GroupsTab", R.string.GroupsTab));
                        options.add(4);
                    }
                    if (!preferences.getBoolean("hideSGroups", false)) {
                        items.add(LocaleController.getString("SGroupsTab", R.string.SGroupsTab));
                        options.add(7);
                    }
                    if (!preferences.getBoolean("hideChannels", false)) {
                        items.add(LocaleController.getString("ChannelTab", R.string.ChannelTab));
                        options.add(3);
                    }
                    if (!preferences.getBoolean("hideBots", false)) {
                        items.add(LocaleController.getString("RobotTab", R.string.RobotTab));
                        options.add(5);
                    }

                    CharSequence[] arrayOfCharSequence = items.toArray(new CharSequence[items.size()]);
                    DialogInterface.OnClickListener local3 = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface paramAnonymous2DialogInterface, int i) {
                            if (i >= 0 && i < options.size()) {
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                                processSelectedOption((options.get(i)).intValue());
                                restartApp();
                            }
                        }
                    };
                    builder2.setItems(arrayOfCharSequence, local3);
                    builder2.setTitle(LocaleController.getString("DefaultTab", R.string.DefaultTab));
                    AlertDialog localAlertDialog = builder2.create();
                    showDialog(localAlertDialog);
                    return;
                } else if (i == fontRow) {
                    presentFragment(new FontSelectActivity());
                    return;
                } else if (i == ThemesRow) {

                    presentFragment(new ThemingActivity());
                    return;
                } else if (i == telehThemeRow) {
                    getParentActivity().startActivity(new Intent(getParentActivity(), ThemeMarket.class));
                    return;
                }
                if (i == directForwardRow) {
                    if (getParentActivity() != null) {
                        final boolean[] maskValues = new boolean[6];
                        BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                        builder.setApplyTopPadding(false);
                        LinearLayout linearLayout = new LinearLayout(getParentActivity());
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                        for (int a = 0; a < 4; a++) {
                            String name = null;
                            if (a == 0) {
                                name = LocaleController.getString("ContactTab", R.string.ContactTab);
                                maskValues[a] = preferences.getBoolean("direct_contact", false);
                            } else if (a == 1) {
                                name = LocaleController.getString("GroupsTab", R.string.GroupsTab);
                                maskValues[a] = preferences.getBoolean("direct_group", false);
                            } else if (a == 2) {
                                name = LocaleController.getString("ChannelTab", R.string.ChannelTab);
                                maskValues[a] = preferences.getBoolean("direct_channel", true);
                            } else if (a == 3) {
                                name = LocaleController.getString("RobotTab", R.string.RobotTab);
                                maskValues[a] = preferences.getBoolean("direct_bot", true);
                            }
                            CheckBoxCell checkBoxCell = new CheckBoxCell(getParentActivity());
                            checkBoxCell.setTag(Integer.valueOf(a));
                            checkBoxCell.setBackgroundResource(R.drawable.list_selector);
                            linearLayout.addView(checkBoxCell, LayoutHelper.createLinear(-1, 48));
                            checkBoxCell.setText(name, "", maskValues[a], true);
                            checkBoxCell.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    CheckBoxCell cell = (CheckBoxCell) v;
                                    int num = ((Integer) cell.getTag()).intValue();
                                    maskValues[num] = !maskValues[num];
                                    cell.setChecked(maskValues[num], true);
                                }
                            });
                        }
                        BottomSheet.BottomSheetCell cell = new BottomSheet.BottomSheetCell(getParentActivity(), 1);
                        cell.setBackgroundResource(R.drawable.list_selector);
                        cell.setTextAndIcon(LocaleController.getString("Save", R.string.Save).toUpperCase(), 0);
                        cell.setTextColor(Theme.AUTODOWNLOAD_SHEET_SAVE_TEXT_COLOR);
                        cell.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    if (visibleDialog != null) {
                                        visibleDialog.dismiss();
                                    }
                                } catch (Throwable e) {
                                    FileLog.e("tmessages", e);
                                }
                                for (int a = 0; a < 4; a++) {
                                    SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                                    if (a == 0) {
                                        editor.putBoolean("direct_contact", maskValues[a]);
                                        editor.commit();
                                    } else if (a == 1) {
                                        editor.putBoolean("direct_group", maskValues[a]);
                                        editor.commit();
                                    } else if (a == 2) {
                                        editor.putBoolean("direct_channel", maskValues[a]);
                                        editor.commit();
                                    } else if (a == 3) {
                                        editor.putBoolean("direct_bot", maskValues[a]);
                                        editor.commit();
                                    }
                                }
                                if (listView != null) {
                                    listView.invalidateViews();
                                }
                            }
                        });
                        linearLayout.addView(cell, LayoutHelper.createLinear(-1, 48));
                        builder.setCustomView(linearLayout);
                        showDialog(builder.create());
                        return;
                    }
                    return;
                }
                if (i == previewStickerRow) {
                    boolean previewSticker = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("preview_sticker", false);
                    SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                    editor.putBoolean("preview_sticker", !previewSticker);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!previewSticker);
                    }
                    return;
                }
                if (i == confirmatinAudioRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    boolean confirmatinAudio = preferences.getBoolean("confirmatin_audio", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("confirmatin_audio", !confirmatinAudio);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!confirmatinAudio);
                    }
                    return;
                }
                if (i == typingStatusRow) {
                    boolean hideTyping = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("hide_typing", false);
                    SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).edit();
                    editor.putBoolean("hide_typing", !hideTyping);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hideTyping);
                    }
                    return;
                }
                if (i == showMutualRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    boolean mutualContact = preferences.getBoolean("mutual_contact", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("mutual_contact", !mutualContact);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!mutualContact);
                    }
                    return;
                }
                if (i == ghostModeRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    boolean ghostMode = preferences.getBoolean("ghost_mode", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("ghost_mode", !ghostMode);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!ghostMode);
                        Toast.makeText(ApplicationLoader.applicationContext, "حالت روح تغییر کرد", Toast.LENGTH_LONG).show();
                    }
                    restartApp();
                    return;
                } else if (i == hidePhoneRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Context.MODE_PRIVATE);
                    boolean hidePhone = preferences.getBoolean("hide_phone", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("hide_phone", !hidePhone);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hidePhone);
                    }
                    if (AndroidUtilities.isTablet()) {
                        reLunchApp();
                        return;
                    } else {
                        restartApp();
                        return;
                    }
                } else if (i == showGhostModeRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    boolean hidePhone = preferences.getBoolean("show_ghost_state_icon", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("show_ghost_state_icon", !hidePhone);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!hidePhone);
                    }
                    if (AndroidUtilities.isTablet()) {
                        reLunchApp();
                        return;
                    } else {
                        restartApp();
                        return;
                    }
                } else if (i == showExactCountRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    boolean showExactCount = preferences.getBoolean("exact_count", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    if (!showExactCount) {
                        editor.putBoolean("exact_count", true);
                        editor.commit();
                    } else {
                        editor.putBoolean("exact_count", false);
                        editor.commit();
                    }

                    if (view instanceof TextCheckCell) {
                        if (!showExactCount) {
                            ((TextCheckCell) view).setChecked(true);

                        } else {
                            ((TextCheckCell) view).setChecked(false);
                        }
                    }
                } else if (i == enableRTLRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    boolean isRTL = preferences.getBoolean("is_rtl", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    if (!isRTL) {
                        editor.putBoolean("is_rtl", true);
                        editor.commit();
                    } else {
                        editor.putBoolean("is_rtl", false);
                        editor.commit();
                    }

                    if (view instanceof TextCheckCell) {
                        if (!isRTL) {
                            ((TextCheckCell) view).setChecked(true);

                        } else {
                            ((TextCheckCell) view).setChecked(false);
                        }
                    }
                    Context context = getParentActivity().getBaseContext();
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        intent.addFlags(0x8000);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1, pendingIntent);
                    System.exit(2);
                } else if (i == moveTabsToBottom) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    boolean tabsToBottom = preferences.getBoolean("tabsToBottom", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    if (!tabsToBottom) {
                        editor.putBoolean("tabsToBottom", true);
                        editor.commit();
                    } else {
                        editor.putBoolean("tabsToBottom", false);
                        editor.commit();
                    }

                    if (view instanceof TextCheckCell) {
                        if (!tabsToBottom) {
                            ((TextCheckCell) view).setChecked(true);

                        } else {
                            ((TextCheckCell) view).setChecked(false);
                        }
                    }
                    Context context = getParentActivity().getBaseContext();
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        intent.addFlags(0x8000);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1, pendingIntent);
                    System.exit(2);
                } else if (i == enableTabletMode) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    boolean animations = preferences.getBoolean("tablet_mode", true);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("tablet_mode", !animations);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!animations);
                    }
                    if (AndroidUtilities.isTablet()) {
                        reLunchApp();
                        return;
                    } else {
                        restartApp();
                        return;
                    }

                } else if (i == enable24HourFormat) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    boolean status = preferences.getBoolean("enable24HourFormat", false);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("enable24HourFormat", !status);
                    editor.commit();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(!status);
                        Toast.makeText(getParentActivity(), "برای اعمال، یکبار برنامه را به طور کامل ببندید و دوباره باز کنید.", Toast.LENGTH_LONG).show();
                    }
                    Context context = getParentActivity().getBaseContext();
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                        intent.addFlags(0x8000);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1, pendingIntent);
                    System.exit(2);
                } else if (i == askQuestionRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    final TextView message = new TextView(getParentActivity());
                    message.setText(Html.fromHtml(LocaleController.getString("AskAQuestionInfo", R.string.AskAQuestionInfo)));
                    message.setTextSize(18);
                    message.setLinkTextColor(0xff316f9f);
                    message.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(5), AndroidUtilities.dp(8), AndroidUtilities.dp(6));
                    message.setMovementMethod(new LinkMovementMethodMy());

                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setView(message);
                    builder.setPositiveButton(LocaleController.getString("AskButton", R.string.AskButton), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            performAskAQuestion();
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (i == sendLogsRow) {
                    sendLogs();
                } else if (i == directShareRow) {
                    MediaController.getInstance().toggleDirectShare();
                    if (view instanceof TextCheckCell) {
                        ((TextCheckCell) view).setChecked(MediaController.getInstance().canDirectShare());
                    }
                } else if (i == switchBackendButtonRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setMessage(LocaleController.getString("AreYouSure", R.string.AreYouSure));
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ConnectionsManager.getInstance().switchBackend();
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                } else if (i == telegramFaqRow) {
                    Browser.openUrl(getParentActivity(), LocaleController.getString("TelegramFaqUrl", R.string.TelegramFaqUrl));
                } else if (i == contactsReimportRow) {
                    //not implemented
                } else if (i == contactsSortRow) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("SortBy", R.string.SortBy));
                    builder.setItems(new CharSequence[]{
                            LocaleController.getString("Default", R.string.Default),
                            LocaleController.getString("SortFirstName", R.string.SortFirstName),
                            LocaleController.getString("SortLastName", R.string.SortLastName)
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("sortContactsBy", which);
                            editor.commit();
                            if (listView != null) {
                                listView.invalidateViews();
                            }
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showDialog(builder.create());
                }
            }
        });

        frameLayout.addView(actionBar);

        extraHeightView = new View(context);
        extraHeightView.setPivotY(0);
        extraHeightView.setBackgroundColor(AvatarDrawable.getProfileBackColorForId(5));
        frameLayout.addView(extraHeightView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 88));

        shadowView = new View(context);
        shadowView.setBackgroundResource(R.drawable.header_shadow);
        frameLayout.addView(shadowView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 3));

        avatarImage = new BackupImageView(context);
        avatarImage.setRoundRadius(AndroidUtilities.dp(21));
        avatarImage.setPivotX(0);
        avatarImage.setPivotY(0);
        frameLayout.addView(avatarImage, LayoutHelper.createFrame(42, 42, Gravity.TOP | Gravity.LEFT, 64, 0, 0, 0));
        avatarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                if (user != null && user.photo != null && user.photo.photo_big != null) {
                    PhotoViewer.getInstance().setParentActivity(getParentActivity());
                    PhotoViewer.getInstance().openPhoto(user.photo.photo_big, TelehgramSettingsActivity.this);
                }
            }
        });

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xffffffff);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity(Gravity.LEFT);
        nameTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        nameTextView.setPivotX(0);
        nameTextView.setPivotY(0);
        frameLayout.addView(nameTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        onlineTextView = new TextView(context);
        onlineTextView.setTextColor(AvatarDrawable.getProfileTextColorForId(5));
        onlineTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        onlineTextView.setLines(1);
        onlineTextView.setMaxLines(1);
        onlineTextView.setSingleLine(true);
        onlineTextView.setEllipsize(TextUtils.TruncateAt.END);
        onlineTextView.setGravity(Gravity.LEFT);
        frameLayout.addView(onlineTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.TOP, 118, 0, 48, 0));

        needLayout();

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (totalItemCount == 0) {
                    return;
                }
                int height = 0;
                View child = view.getChildAt(0);
                if (child != null) {
                    if (firstVisibleItem == 0) {
                        height = AndroidUtilities.dp(88) + (child.getTop() < 0 ? child.getTop() : 0);
                    }
                    if (extraHeight != height) {
                        extraHeight = height;
                        needLayout();
                    }
                }
            }
        });

        return fragmentView;
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        MediaController.getInstance().checkAutodownloadSettings();
    }

    @Override
    public void updatePhotoAtIndex(int index) {

    }

    @Override
    public PhotoViewer.PlaceProviderObject getPlaceForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        if (fileLocation == null) {
            return null;
        }
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        if (user != null && user.photo != null && user.photo.photo_big != null) {
            TLRPC.FileLocation photoBig = user.photo.photo_big;
            if (photoBig.local_id == fileLocation.local_id && photoBig.volume_id == fileLocation.volume_id && photoBig.dc_id == fileLocation.dc_id) {
                int coords[] = new int[2];
                avatarImage.getLocationInWindow(coords);
                PhotoViewer.PlaceProviderObject object = new PhotoViewer.PlaceProviderObject();
                object.viewX = coords[0];
                object.viewY = coords[1] - AndroidUtilities.statusBarHeight;
                object.parentView = avatarImage;
                object.imageReceiver = avatarImage.getImageReceiver();
                object.dialogId = UserConfig.getClientUserId();
                object.thumb = object.imageReceiver.getBitmap();
                object.size = -1;
                object.radius = avatarImage.getImageReceiver().getRoundRadius();
                object.scale = avatarImage.getScaleX();
                return object;
            }
        }
        return null;
    }

    @Override
    public Bitmap getThumbForPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
        return null;
    }

    @Override
    public void willSwitchFromPhoto(MessageObject messageObject, TLRPC.FileLocation fileLocation, int index) {
    }

    @Override
    public void willHidePhotoViewer() {
        avatarImage.getImageReceiver().setVisible(true, true);
    }

    @Override
    public boolean isPhotoChecked(int index) {
        return false;
    }

    @Override
    public void setPhotoChecked(int index) {
    }

    @Override
    public boolean cancelButtonPressed() {
        return true;
    }

    @Override
    public void sendButtonPressed(int index) {
    }

    @Override
    public int getSelectedCount() {
        return 0;
    }

    public void performAskAQuestion() {
        final SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        int uid = preferences.getInt("support_id", 0);
        TLRPC.User supportUser = null;
        if (uid != 0) {
            supportUser = MessagesController.getInstance().getUser(uid);
            if (supportUser == null) {
                String userString = preferences.getString("support_user", null);
                if (userString != null) {
                    try {
                        byte[] datacentersBytes = Base64.decode(userString, Base64.DEFAULT);
                        if (datacentersBytes != null) {
                            SerializedData data = new SerializedData(datacentersBytes);
                            supportUser = TLRPC.User.TLdeserialize(data, data.readInt32(false), false);
                            if (supportUser != null && supportUser.id == 333000) {
                                supportUser = null;
                            }
                            data.cleanup();
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                        supportUser = null;
                    }
                }
            }
        }
        if (supportUser == null) {
            final ProgressDialog progressDialog = new ProgressDialog(getParentActivity());
            progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
            TLRPC.TL_help_getSupport req = new TLRPC.TL_help_getSupport();
            ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                @Override
                public void run(TLObject response, TLRPC.TL_error error) {
                    if (error == null) {

                        final TLRPC.TL_help_support res = (TLRPC.TL_help_support) response;
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt("support_id", res.user.id);
                                SerializedData data = new SerializedData();
                                res.user.serializeToStream(data);
                                editor.putString("support_user", Base64.encodeToString(data.toByteArray(), Base64.DEFAULT));
                                editor.commit();
                                data.cleanup();
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    FileLog.e("tmessages", e);
                                }
                                ArrayList<TLRPC.User> users = new ArrayList<>();
                                users.add(res.user);
                                MessagesStorage.getInstance().putUsersAndChats(users, null, true, true);
                                MessagesController.getInstance().putUser(res.user, false);
                                Bundle args = new Bundle();
                                args.putInt("user_id", res.user.id);
                                presentFragment(new ChatActivity(args));
                            }
                        });
                    } else {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    FileLog.e("tmessages", e);
                                }
                            }
                        });
                    }
                }
            });
        } else {
            MessagesController.getInstance().putUser(supportUser, true);
            Bundle args = new Bundle();
            args.putInt("user_id", supportUser.id);
            presentFragment(new ChatActivity(args));
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void saveSelfArgs(Bundle args) {

    }

    @Override
    public void restoreSelfArgs(Bundle args) {

    }

    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.updateInterfaces) {
            int mask = (Integer) args[0];
            if ((mask & MessagesController.UPDATE_MASK_AVATAR) != 0 || (mask & MessagesController.UPDATE_MASK_NAME) != 0) {
                updateUserData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        updateUserData();
        fixLayout();
        AnalyticsEventUtil.sendScreenName("Telehgram Settings Activity");

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    private void needLayout() {
        FrameLayout.LayoutParams layoutParams;
        int newTop = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight();
        if (listView != null) {
            layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
            if (layoutParams.topMargin != newTop) {
                layoutParams.topMargin = newTop;
                listView.setLayoutParams(layoutParams);
                extraHeightView.setTranslationY(newTop);
            }
        }

        if (avatarImage != null) {
            float diff = extraHeight / (float) AndroidUtilities.dp(88);
            extraHeightView.setScaleY(diff);
            shadowView.setTranslationY(newTop + extraHeight);
            final boolean setVisible = diff > 0.2f;
            avatarImage.setScaleX((42 + 18 * diff) / 42.0f);
            avatarImage.setScaleY((42 + 18 * diff) / 42.0f);
            float avatarY = (actionBar.getOccupyStatusBar() ? AndroidUtilities.statusBarHeight : 0) + ActionBar.getCurrentActionBarHeight() / 2.0f * (1.0f + diff) - 21 * AndroidUtilities.density + 27 * AndroidUtilities.density * diff;
            avatarImage.setTranslationX(-AndroidUtilities.dp(47) * diff);
            avatarImage.setTranslationY((float) Math.ceil(avatarY));
            nameTextView.setTranslationX(-21 * AndroidUtilities.density * diff);
            nameTextView.setTranslationY((float) Math.floor(avatarY) - (float) Math.ceil(AndroidUtilities.density) + (float) Math.floor(7 * AndroidUtilities.density * diff));
            onlineTextView.setTranslationX(-21 * AndroidUtilities.density * diff);
            onlineTextView.setTranslationY((float) Math.floor(avatarY) + AndroidUtilities.dp(22) + (float) Math.floor(11 * AndroidUtilities.density) * diff);
            nameTextView.setScaleX(1.0f + 0.12f * diff);
            nameTextView.setScaleY(1.0f + 0.12f * diff);
        }
    }

    private void fixLayout() {
        if (fragmentView == null) {
            return;
        }
        fragmentView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                if (fragmentView != null) {
                    needLayout();
                    fragmentView.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                return true;
            }
        });
        this.listView.setAdapter(listAdapter);
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int def = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
        actionBar.setBackgroundColor(themePrefs.getInt("prefHeaderColor", def));
        actionBar.setTitleColor(themePrefs.getInt("prefHeaderTitleColor", 0xffffffff));

        Drawable back = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_back);
        back.setColorFilter(themePrefs.getInt("prefHeaderIconsColor", 0xffffffff), PorterDuff.Mode.MULTIPLY);
        actionBar.setBackButtonDrawable(back);

        Drawable other = getParentActivity().getResources().getDrawable(R.drawable.ic_ab_other);
        other.setColorFilter(themePrefs.getInt("prefHeaderIconsColor", 0xffffffff), PorterDuff.Mode.MULTIPLY);
        int radius = AndroidUtilities.getIntDef("prefAvatarRadius", 32);
        avatarImage.setRoundRadius(radius);
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int bgColor = preferences.getInt("prefBGColor", 0xffffffff);
        int hColor = preferences.getInt("prefHeaderColor", def);
        listView.setBackgroundColor(bgColor);
        nameTextView.setTextColor(preferences.getInt("prefHeaderTitleColor", 0xffffffff));
        onlineTextView.setTextColor(preferences.getInt("prefHeaderStatusColor", AndroidUtilities.getIntDarkerColor("themeColor", -0x40)));
        extraHeightView.setBackgroundColor(hColor);

    }

    private void updateUserData() {
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        TLRPC.FileLocation photo = null;
        TLRPC.FileLocation photoBig = null;
        if (user.photo != null) {
            photo = user.photo.photo_small;
            photoBig = user.photo.photo_big;
        }
        AvatarDrawable avatarDrawable = new AvatarDrawable(user, true);
        avatarDrawable.setColor(0xff5c98cd);
        if (avatarImage != null) {
            avatarImage.setImage(photo, "50_50", avatarDrawable);
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);

            nameTextView.setText(UserObject.getUserName(user));

            SharedPreferences var1 = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
            if (var1.getBoolean("ghost_mode", false) == true) {
                onlineTextView.setText(LocaleController.getString("Hidden", R.string.Hidden));
            } else {
                onlineTextView.setText(LocaleController.getString("Online", R.string.Online));
            }
            avatarImage.getImageReceiver().setVisible(!PhotoViewer.getInstance().isShowingImage(photoBig), false);
        }
    }

    private void sendLogs() {
        try {
            ArrayList<Uri> uris = new ArrayList<>();
            File sdCard = ApplicationLoader.applicationContext.getExternalFilesDir(null);
            File dir = new File(sdCard.getAbsolutePath() + "/logs");
            File[] files = dir.listFiles();
            for (File file : files) {
                uris.add(Uri.fromFile(file));
            }

            if (uris.isEmpty()) {
                return;
            }
            Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{BuildVars.SEND_LOGS_EMAIL});
            i.putExtra(Intent.EXTRA_SUBJECT, "last logs");
            i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            getParentActivity().startActivityForResult(Intent.createChooser(i, "Select email application."), 500);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void restartApp() {
        Intent i = getParentActivity().getBaseContext().getPackageManager().getLaunchIntentForPackage(getParentActivity().getPackageName());
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(AccessibilityNodeInfoCompat.ACTION_PASTE);
        getParentActivity().startActivity(i);
    }

    private void reLunchApp() {
        Context context = getParentActivity().getBaseContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            intent.addFlags(0x8000);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1, pendingIntent);
        System.exit(2);
    }

    private void processSelectedOption(int option) {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
        SharedPreferences.Editor editor = preferences.edit();
        switch (option) {
            case 0:
                editor.putInt("defTab", 0);
                break;

            case 5:
                if (preferences.getBoolean("hideBots", false)) {
                    editor.putInt("defTab", 0);
                    break;
                } else {
                    editor.putInt("defTab", option);
                    break;
                }

            case 3:
                if (preferences.getBoolean("hideChannels", false)) {
                    editor.putInt("defTab", 0);
                    break;
                } else {
                    editor.putInt("defTab", option);
                    break;
                }
            case 4:
                if (preferences.getBoolean("hideGroups", false)) {
                    editor.putInt("defTab", 0);
                    break;
                } else {
                    editor.putInt("defTab", option);
                    break;
                }
            case 7:
                if (preferences.getBoolean("hideSGroups", false)) {
                    editor.putInt("defTab", 0);
                    break;
                } else {
                    editor.putInt("defTab", option);
                    break;
                }
            case 6:
                if (preferences.getBoolean("hideUsers", false)) {
                    editor.putInt("defTab", 0);
                    break;
                } else {
                    editor.putInt("defTab", option);
                    break;
                }
            case 8:
                if (preferences.getBoolean("hideFavs", false)) {
                    editor.putInt("defTab", 0);
                    break;
                } else {
                    editor.putInt("defTab", option);
                    break;
                }

        }
        editor.commit();
    }

    private static class LinkMovementMethodMy extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(@NonNull TextView widget, @NonNull Spannable buffer, @NonNull MotionEvent event) {
            try {
                return super.onTouchEvent(widget, buffer, event);
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            return false;
        }
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int i) {
            return i == fontRow || i == enableTabletMode || i == enable24HourFormat || i == PersianDateRow || i == ThemesRow || i == telehThemeRow || i == showMutualRow ||/* i == passwordRow ||*/
                    i == showGhostModeRow || i == ghostModeRow || i == showExactCountRow || i == enableRTLRow || i == moveTabsToBottom || i == hidePhoneRow || i == swipeTabRow || i == defaultTabRow || i == hiddenTabsRow || i == confirmatinAudioRow ||
                    i == directForwardRow || i == askQuestionRow || i == sendLogsRow || i == typingStatusRow || i == previewStickerRow ||
                    i == switchBackendButtonRow || i == telegramFaqRow || i == contactsSortRow || i == contactsReimportRow;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            int type = getItemViewType(i);

            if (type == 0) {
                if (view == null) {
                    view = new EmptyCell(mContext);
                }
                if (i == overscrollRow) {
                    ((EmptyCell) view).setHeight(AndroidUtilities.dp(88));
                } else {
                    ((EmptyCell) view).setHeight(AndroidUtilities.dp(0.0f));
                }
            } else if (type == 1) {
                if (view == null) {
                    view = new ShadowSectionCell(mContext);
                }
            } else if (type == 2) {
                if (view == null) {
                    view = new TextSettingsCell(mContext);
                }
                TextSettingsCell textCell = (TextSettingsCell) view;
                if (i == defaultTabRow) {
                    switch (ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getInt("defTab", 0)) {
                        case 5:
                            textCell.setTextAndValue(LocaleController.getString("RobotTab", R.string.RobotTab), LocaleController.getString("RobotTab", R.string.RobotTab), true);
                            return view;
                        case 3:
                            textCell.setTextAndValue(LocaleController.getString("DefaultTab", R.string.DefaultTab), LocaleController.getString("ChannelTab", R.string.ChannelTab), true);
                            return view;
                        case 4:
                            textCell.setTextAndValue(LocaleController.getString("DefaultTab", R.string.DefaultTab), LocaleController.getString("GroupsTab", R.string.GroupsTab), true);
                            return view;
                        case 7:
                            textCell.setTextAndValue(LocaleController.getString("DefaultTab", R.string.DefaultTab), LocaleController.getString("SGroupsTab", R.string.SGroupsTab), true);
                            return view;
                        case 6:
                            textCell.setTextAndValue(LocaleController.getString("DefaultTab", R.string.DefaultTab), LocaleController.getString("ContactTab", R.string.ContactTab), true);
                            return view;
                        case 8:
                            textCell.setTextAndValue(LocaleController.getString("DefaultTab", R.string.DefaultTab), LocaleController.getString("FavoriteTab", R.string.FavoriteTab), true);
                            return view;
                        case 0:
                            textCell.setTextAndValue(LocaleController.getString("DefaultTab", R.string.DefaultTab), LocaleController.getString("AllTab", R.string.AllTab), true);
                            return view;

                        default:
                            textCell.setTextAndValue(LocaleController.getString("DefaultTab", R.string.DefaultTab), LocaleController.getString("AllTab", R.string.AllTab), true);
                            return view;
                    }
                }
                if (i == contactsSortRow) {
                    String value;
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    int sort = preferences.getInt("sortContactsBy", 0);
                    if (sort == 0) {
                        value = LocaleController.getString("Default", R.string.Default);
                    } else if (sort == 1) {
                        value = LocaleController.getString("FirstName", R.string.SortFirstName);
                    } else {
                        value = LocaleController.getString("LastName", R.string.SortLastName);
                    }
                    textCell.setTextAndValue(LocaleController.getString("SortBy", R.string.SortBy), value, true);
                } else if (i == sendLogsRow) {
                    textCell.setText("Send Logs", true);
                } else if (i == askQuestionRow) {
                    textCell.setText(LocaleController.getString("AskAQuestion", R.string.AskAQuestion), true);
                } else if (i == switchBackendButtonRow) {
                    textCell.setText("Switch Backend", true);
                } else if (i == telehThemeRow) {
                    textCell.setText(LocaleController.getString("TelehTheme", R.string.TelehTheme), true);
                } else if (i == ThemesRow) {
                    textCell.setText(LocaleController.getString("Themes", R.string.Themes), true);
                } else if (i == telegramFaqRow) {
                    textCell.setText(LocaleController.getString("TelegramFAQ", R.string.TelegramFaq), true);
                } else if (i == contactsReimportRow) {
                    textCell.setText(LocaleController.getString("ImportContacts", R.string.ImportContacts), true);
                } else if (i == fontRow) {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0);
                    if (LocaleController.isRTL) {
                        textCell.setTextAndValue(LocaleController.getString("FontType", R.string.FontType), preferences.getString("font_type", "ایران سانس نازک"), false);
                        return view;
                    }
                    String fnt = preferences.getString("font_type", "ایران سانس نازک");

                    switch (fnt) {
                        case "هما":
                            fnt = "Hama";
                            break;
                        case "افسانه":
                            fnt = "Afsaneh";
                            break;
                        case "ایران سانس ضخیم":
                            fnt = "IransansBold";
                            break;
                        case "تلگرام":
                            fnt = "Telegram";
                            break;
                        case "دست نویس":
                            fnt = "Dastnevis";
                            break;
                        case "ایران سانس معمولی":
                            fnt = "Iransans";
                            break;
                        case "مروارید":
                            fnt = "Morvarid";
                            break;
                        case "ایران سانس نازک":
                            fnt = "IransansLight";
                            break;
                        case "ایران سانس متوسط":
                            fnt = "IransansMedium";
                            break;
                        case "یکان":
                            fnt = "Yekan";
                            break;
                        case "ترافیک":
                            fnt = "traffic";
                            break;
                        case "کودک":
                            fnt = "koodak";
                            break;
                        case "لوتوس":
                            fnt = "lotus";
                            break;
                        default:
                            break;
                    }

                    textCell.setTextAndValue(LocaleController.getString("FontType", R.string.FontType), fnt, false);
                    return view;
                }

            } else if (type == 3) {
                if (view == null) {
                    view = new TextCheckCell(mContext);
                }
                TextCheckCell textCell = (TextCheckCell) view;

                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                if (i == enableTabletMode) {
                    textCell.setTextAndCheck(LocaleController.getString("TabletMode", R.string.TabletMode),
                            preferences.getBoolean("tablet_mode", true), false);
                }
                if (i == previewStickerRow) {
                    textCell.setTextAndCheck(LocaleController.getString("PreviewSticker", R.string.PreviewSticker),
                            preferences.getBoolean("preview_sticker", false), false);
                }
                if (i == enable24HourFormat) {
                    textCell.setTextAndCheck(LocaleController.getString("Enable24HourFormat", R.string.Enable24HourFormat),
                            preferences.getBoolean("enable24HourFormat", false), false);
                }

                if (i == confirmatinAudioRow) {
                    textCell.setTextAndCheck(LocaleController.getString("ConfirmatinAudio", R.string.ConfirmatinAudio), preferences.getBoolean("confirmatin_audio", true), true);
                } else if (i == showMutualRow) {
                    textCell.setTextAndValueAndCheck(LocaleController.getString("ShowMutualContacts", R.string.ShowMutualContacts), LocaleController.getString("ShowMutualContactsDes", R.string.ShowMutualContactsDes), preferences.getBoolean("mutual_contact", true), true, false);

                    return view;
                } else if (i == typingStatusRow) {
                    textCell.setTextAndCheck(LocaleController.getString("HideTypingStatus", R.string.HideTypingStatus), preferences.getBoolean("hide_typing", false), true);
                    return view;
                } else if (i == PersianDateRow) {
                    textCell.setTextAndCheck(LocaleController.getString("UsePersianDate", R.string.UsePersianDate),
                            preferences.getBoolean("persian_date", false), false);
                } else if (i == hidePhoneRow) {
                    textCell.setTextAndValueAndCheck(LocaleController.getString("HidePhone", R.string.HidePhone), LocaleController.getString("HidePhoneDesRow", R.string.HidePhoneDesRow), preferences.getBoolean("hide_phone", false), true, false);

                    return view;
                } else if (i == showGhostModeRow) {
                    textCell.setTextAndValueAndCheck(LocaleController.getString("ShowGhostMode", R.string.ShowGhostMode), LocaleController.getString("ShowGhostModeDesRow", R.string.ShowGhostModeDesRow), preferences.getBoolean("show_ghost_state_icon", true), true, false);

                    return view;
                } else if (i == ghostModeRow) {
                    textCell.setTextAndCheck(LocaleController.getString("GhostMode", R.string.GhostMode), preferences.getBoolean("ghost_mode", false), false);
                    return view;
                } else if (i == showExactCountRow) {
                    textCell.setTextAndCheck(LocaleController.getString("ShowExactCount", R.string.ShowExactCount), preferences.getBoolean("exact_count", false), true);

                    return view;
                } else if (i == moveTabsToBottom) {
                    textCell.setTextAndCheck(LocaleController.getString("MoveTabsToBottom", R.string.MoveTabsToBottom), preferences.getBoolean("tabsToBottom", false), true);

                    return view;
                } else if (i == enableRTLRow) {
                    textCell.setTextAndCheck(LocaleController.getString("EnableRTLMode", R.string.EnableRTLMode), preferences.getBoolean("is_rtl", false), true);

                    return view;
                } else if (i == swipeTabRow) {
                    textCell.setTextAndCheck(LocaleController.getString("SwipeTabs", R.string.SwipeTabs), preferences.getBoolean("swipe_tabs", true), true);
                    return view;
                }
            } else if (type == 4) {
                if (view == null) {
                    view = new HeaderCell(mContext);
                }
                if (i == settingsSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("SETTINGS", R.string.SETTINGS));
                } else if (i == supportSectionRow2) {
                    ((HeaderCell) view).setText(LocaleController.getString("Support", R.string.Support));
                }
            } else if (type == 5) {
                if (view == null) {
                    view = new TextInfoCell(mContext);
                    try {
                        PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                        int code = pInfo.versionCode / 10;
                        String abi = "";
                        switch (pInfo.versionCode % 10) {
                            case 0:
                                abi = "arm";
                                break;
                            case 1:
                                abi = "arm-v7a";
                                break;
                            case 2:
                                abi = "x86";
                                break;
                            case 3:
                                abi = "universal";
                                break;
                        }
                        ((TextInfoCell) view).setText(String.format(Locale.US, "iTel for Android v%s (%d) %s", pInfo.versionName, code, abi));
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            } else if (type == 6) {
                if (view == null) {
                    view = new TextDetailSettingsCell(mContext);
                }
                TextDetailSettingsCell textCell = (TextDetailSettingsCell) view;
                String text;
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);

                if (i == hiddenTabsRow) {
                    text = "";
                    for (int a = 0; a < 6; a++) {
                        if (a == 0) {
                            if (preferences.getBoolean("hideFavs", false)) {
                                text = text + ", " + LocaleController.getString("FavoriteTab", R.string.FavoriteTab);
                            }
                        } else if (a == 1) {
                            if (preferences.getBoolean("hideUsers", false)) {
                                text = text + ", " + LocaleController.getString("ContactTab", R.string.ContactTab);
                            }
                        } else if (a == 2) {
                            if (preferences.getBoolean("hideGroups", false)) {
                                text = text + ", " + LocaleController.getString("GroupsTab", R.string.GroupsTab);
                            }
                        } else if (a == 3) {
                            if (preferences.getBoolean("hideSGroups", false)) {
                                text = text + ", " + LocaleController.getString("SGroupsTab", R.string.SGroupsTab);
                            }
                        } else if (a == 4) {
                            if (preferences.getBoolean("hideChannels", false)) {
                                text = text + ", " + LocaleController.getString("ChannelTab", R.string.ChannelTab);
                            }
                        } else if (a == 5 && preferences.getBoolean("hideBots", false)) {
                            text = text + ", " + LocaleController.getString("RobotTab", R.string.RobotTab);
                        }
                    }
                    textCell.setTextAndValue(LocaleController.getString("HiddenTabs", R.string.HiddenTabs), text, true);
                    return view;
                } else if (i != directForwardRow) {
                    return view;
                } else {
                    preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Context.MODE_PRIVATE);
                    text = "";
                    for (int a = 0; a < 4; a++) {
                        if (a == 0) {
                            if (preferences.getBoolean("direct_contact", false)) {
                                text = text + LocaleController.getString("ContactTab", R.string.ContactTab);
                            }
                        } else if (a == 1) {
                            if (preferences.getBoolean("direct_group", false)) {
                                text = text + ", " + LocaleController.getString("GroupsTab", R.string.GroupsTab);
                            }
                        } else if (a == 2) {
                            if (preferences.getBoolean("direct_channel", true)) {
                                text = text + ", " + LocaleController.getString("ChannelTab", R.string.ChannelTab);
                            }
                        } else if (a == 3 && preferences.getBoolean("direct_bot", true)) {
                            text = text + ", " + LocaleController.getString("RobotTab", R.string.RobotTab);
                        }
                    }
                    textCell.setTextAndValue(LocaleController.getString("DirectForward", R.string.DirectForward), text, true);
                    return view;
                }
            }
            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (i == emptyRow || i == overscrollRow) {
                return 0;
            }
            if (i == settingsSectionRow || i == supportSectionRow || i == contactsSectionRow) {
                return 1;
            } else if (i == enable24HourFormat || i == enableTabletMode || i == enableRTLRow || i == moveTabsToBottom || i == showMutualRow || i == showExactCountRow || i == hidePhoneRow || i == typingStatusRow || i == confirmatinAudioRow || i == previewStickerRow || i == PersianDateRow || i == swipeTabRow || i == showGhostModeRow || i == ghostModeRow) {
                return 3;
            } else if (i == askQuestionRow || i == ThemesRow || i == telehThemeRow || i == defaultTabRow || i == fontRow || i == sendLogsRow || i == switchBackendButtonRow || i == telegramFaqRow || i == contactsReimportRow || i == contactsSortRow) {
                return 2;
            } else if (i == versionRow) {
                return 5;
            } else if (i == hiddenTabsRow || i == directForwardRow) {
                return 6;
            } else if (i == settingsSectionRow2 || i == supportSectionRow2) {
                return 4;
            } else {
                return 2;
            }
        }

        @Override
        public int getViewTypeCount() {
            return TypeCount;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }
}
