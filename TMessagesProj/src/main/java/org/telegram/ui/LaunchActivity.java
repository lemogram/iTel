/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.telegram.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.sinch.android.rtc.SinchError;

import org.telegram.PhoneFormat.PhoneFormat;
import org.telegram.calling.BaseActivity;
import org.telegram.calling.PlaceCallActivity;
import org.telegram.calling.SinchService;
import org.telegram.chatheadmsg.ChatHeadService;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NativeCrashManager;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.NotificationsController;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.messenger.query.DraftQuery;
import org.telegram.radar.RadarMainActivity;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Adapters.DrawerLayoutAdapter;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.PasscodeView;
import org.telegram.ui.Components.StickersAlert;
import org.telegram.ui.Telehgram.AntiReportfragment;
import org.telegram.ui.Telehgram.OnlineContactsActivity;
import org.telegram.ui.Telehgram.Premium;
import org.telegram.ui.Telehgram.UpdateActivity;
import org.telegram.ui.Telehgram.cleaner.MainActivity;
import org.telegram.ui.Telehgram.downloadManager.Download;
import org.telegram.ui.Telehgram.theming.market.ThemeMarket;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.ronash.pushe.Pushe;
import ir.tapsell.sdk.*;

public class LaunchActivity extends BaseActivity implements SinchService.StartFailedListener,ActionBarLayout.ActionBarLayoutDelegate, NotificationCenter.NotificationCenterDelegate, DialogsActivity.DialogsActivityDelegate {




    private static ArrayList<BaseFragment> mainFragmentsStack = new ArrayList<>();
    private static ArrayList<BaseFragment> layerFragmentsStack = new ArrayList<>();
    private static ArrayList<BaseFragment> rightFragmentsStack = new ArrayList<>();
    protected DrawerLayoutContainer drawerLayoutContainer;
    private boolean finished;
    private String videoPath;
    private String sendingText;
    private ArrayList<Uri> photoPathsArray;
    private ArrayList<String> documentsPathsArray;
    private ArrayList<Uri> documentsUrisArray;
    private String documentsMimeType;
    private ArrayList<String> documentsOriginalPathsArray;
    private ArrayList<TLRPC.User> contactsToSend;
    private int currentConnectionState;
    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    private ActionBarLayout actionBarLayout;
    private ActionBarLayout layersActionBarLayout;
    private ActionBarLayout rightActionBarLayout;
    private FrameLayout shadowTablet;
    private FrameLayout shadowTabletSide;
    private ImageView backgroundTablet;
    private DrawerLayoutAdapter drawerLayoutAdapter;
    private PasscodeView passcodeView;
    private AlertDialog visibleDialog;

    private Intent passcodeSaveIntent;
    private boolean passcodeSaveIntentIsNew;
    private boolean passcodeSaveIntentIsRestore;

    private boolean tabletFullSize;

    private Runnable lockRunnable;
    public static boolean mIsUprade = false;

    TapsellAd ad;
    private boolean showCompleteDialog = false;
    private boolean rewarded = false;
    private boolean completed = false;

    Tracker mTracker;

    public boolean prem() {
        if(!BuildConfig.IsIAPenable){
            return  true;
        }
        else {
           // return mIsUprade;
            return  true;
        }
    }

    public void goPurchas() {
        new AlertDialog.Builder(this)
                .setTitle("عضویت ویژه ")
                .setMessage("برای استفاده از کلیه امکانات به صورت مادام العمر کافی است مشترک ویژه ما شوید. ")
                .setPositiveButton("عضویت ویژه", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                          startActivity(new Intent(ApplicationLoader.applicationContext, Premium.class));
                          finish();
                    }
                })
                .setNegativeButton("منصرف شدم", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {// do nothing
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert).show();
    }

    public void goUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LaunchActivity.this);
        builder.setTitle("تغییرات برنامه");
        builder.setMessage("نسخه L-1.3 (تلگرام 4.0.1) - 13 تیر 96\n" +
                "◎ باصدا / بیصدا کردن سریع گفتگوها\n" +
                "◎ امکان حذف تب ها\n" +
                "◎ دسترسی سریع «نشان کردن و ارسال به پروفایل من»\n" +
                "◎ رفع ایراد «آیدی یاب»\n" +
                "◎ رفع ایراد فوروارد پس از ویرایش پست\n" +
                "\n" +
                "نسخه L-1.2 (7 تیر 96)\n" +
                "◎ رفع ایراد باز شدن تب ربات\n" +
                "(گزارش شده توسط کاربران)\n" +
                "\n" +
                "نسخه L-1.1 (6 تیر 96)\n" +
                "◎ توسعه بخش استیکرساز\n" +
                "+ استیکرهای آماده در سه دسته، ماسک، شغل و لیبل\n" +
                "+ قالب های آماده برش تصویر\n" +
                "+ افزوده شدن فونت و قابلیت حذف نوشته\n" +
                "\n" +
                "نسخه L-1.0 (23 خرداد 96)\n" +
                "◎ افزوده شدن «استیکر ساز»\n" +
                "◎ ارسال ویدیو با کیفیت های مختلف\n" +
                "◎ قابلیت نشانه گذاری پیام ها به عنوان خوانده شده\n" +
                "◎ مدیریت هوشمند چت روم ها\n" +
                "◎ قابلیت خاموش کردن لیموگرام");

        builder.setPositiveButton("متوجه شدم", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {// do nothing
                dialog.dismiss();
            }
        });
        showAlertDialog(builder);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ApplicationLoader.postInitApplication();
        NativeCrashManager.handleDumpFiles(this);


        mIsUprade = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", MODE_PRIVATE).getBoolean("ISPRE", false);
        if (!UserConfig.isClientActivated()) {
            Intent intent = getIntent();
            if (intent != null && intent.getAction() != null && (Intent.ACTION_SEND.equals(intent.getAction()) || intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE))) {
                super.onCreate(savedInstanceState);
                finish();
                return;
            }
            if (intent != null && !intent.getBooleanExtra("fromIntro", false)) {
                SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("logininfo2", MODE_PRIVATE);
                Map<String, ?> state = preferences.getAll();
                if (state.isEmpty()) {
                    if (BuildConfig.IsDownloadManagerEnable) {
                        //  Intent intent2 = new Intent(this, IntroActivity.class);
                        Intent intent2 = new Intent(this, org.telegram.ui.Telehgram.intro.MainActivity.class);
                        startActivity(intent2);
                    } else {
                        Intent intent2 = new Intent(this, IntroActivity.class);
                        startActivity(intent2);
                    }
                    super.onCreate(savedInstanceState);
                    finish();
                    return;
                }
            }
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setTheme(R.style.Theme_TMessages);
        getWindow().setBackgroundDrawableResource(R.drawable.transparent);

        super.onCreate(savedInstanceState);
        Theme.loadRecources(this);

        Tapsell.initialize(LaunchActivity.this, "beqctioekalmdofhleabtgokaekordinmjkipsfltseamatnnnqklhgsnrkisiprkkpjal");

        Tapsell.setRewardListener(new TapsellRewardListener() {
            @Override
            public void onAdShowFinished(TapsellAd ad, boolean completed) {
                Log.e("MainActivity","isCompleted? "+completed+ ", ad was rewarded?" + (ad!=null && ad.isRewardedAd()));
                showCompleteDialog = true;
                LaunchActivity.this.completed=completed;
                LaunchActivity.this.rewarded=(ad!=null && ad.isRewardedAd());

                if(completed){
                    loadAd("597dcb8546846521dae4a29e", false);
                    //Toast.makeText(LaunchActivity.this, ad.getZoneId().toString() , Toast.LENGTH_LONG).show();
                }
                // store user reward in local database
            }
        });

        loadAd("597dcb8546846521dae4a29e", false);

        SharedPreferences updatehdialog = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", MODE_PRIVATE);
        int versionCode = BuildConfig.VERSION_CODE;
        int updatestatus = updatehdialog.getInt("version", 1);
        if(updatestatus < versionCode){
            updatehdialog.edit().putInt("version",versionCode).apply();
            //goUpdateDialog();
        }

        if (UserConfig.passcodeHash.length() != 0 && UserConfig.appLocked) {
            UserConfig.lastPauseTime = ConnectionsManager.getInstance().getCurrentTime();
        }

        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            AndroidUtilities.statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }

        actionBarLayout = new ActionBarLayout(this);

        drawerLayoutContainer = new DrawerLayoutContainer(this);


        ApplicationLoader application = (ApplicationLoader) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName("LuanchActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());


        //Adad.initialize(getApplicationContext());
        //Banner banner = new Banner(ApplicationLoader.applicationContext);
     //   banner.setAdListener(this.mAdListener)
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        int def = themePrefs.getInt("themeColor", AndroidUtilities.defColor);
        int hColor = themePrefs.getInt("chatsHeaderColor", def);
        frameLayout.setBackgroundColor(hColor);
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
            frameLayout.setBackgroundDrawable(gd);
        }
        //frameLayout.addView(banner, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        linearLayout.addView(frameLayout);
        linearLayout.addView(this.drawerLayoutContainer);

       // setContentView(drawerLayoutContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(linearLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (AndroidUtilities.isTablet()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

            RelativeLayout launchLayout = new RelativeLayout(this);
            drawerLayoutContainer.addView(launchLayout);
            FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) launchLayout.getLayoutParams();
            layoutParams1.width = LayoutHelper.MATCH_PARENT;
            layoutParams1.height = LayoutHelper.MATCH_PARENT;
            launchLayout.setLayoutParams(layoutParams1);

            backgroundTablet = new ImageView(this);
            backgroundTablet.setScaleType(ImageView.ScaleType.CENTER_CROP);
            backgroundTablet.setImageResource(R.drawable.cats);
            launchLayout.addView(backgroundTablet);
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) backgroundTablet.getLayoutParams();
            relativeLayoutParams.width = LayoutHelper.MATCH_PARENT;
            relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
            backgroundTablet.setLayoutParams(relativeLayoutParams);

            launchLayout.addView(actionBarLayout);
            relativeLayoutParams = (RelativeLayout.LayoutParams) actionBarLayout.getLayoutParams();
            relativeLayoutParams.width = LayoutHelper.MATCH_PARENT;
            relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
            actionBarLayout.setLayoutParams(relativeLayoutParams);

            rightActionBarLayout = new ActionBarLayout(this);
            launchLayout.addView(rightActionBarLayout);
            relativeLayoutParams = (RelativeLayout.LayoutParams) rightActionBarLayout.getLayoutParams();
            relativeLayoutParams.width = AndroidUtilities.dp(320);
            relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
            rightActionBarLayout.setLayoutParams(relativeLayoutParams);
            rightActionBarLayout.init(rightFragmentsStack);
            rightActionBarLayout.setDelegate(this);

            shadowTabletSide = new FrameLayout(this);
            shadowTabletSide.setBackgroundColor(0x40696c6e);
            launchLayout.addView(shadowTabletSide);
            relativeLayoutParams = (RelativeLayout.LayoutParams) shadowTabletSide.getLayoutParams();
            relativeLayoutParams.width = AndroidUtilities.dp(1);
            relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
            shadowTabletSide.setLayoutParams(relativeLayoutParams);

            shadowTablet = new FrameLayout(this);
            shadowTablet.setVisibility(View.GONE);
            shadowTablet.setBackgroundColor(0x7F000000);
            launchLayout.addView(shadowTablet);
            relativeLayoutParams = (RelativeLayout.LayoutParams) shadowTablet.getLayoutParams();
            relativeLayoutParams.width = LayoutHelper.MATCH_PARENT;
            relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
            shadowTablet.setLayoutParams(relativeLayoutParams);
            shadowTablet.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (!actionBarLayout.fragmentsStack.isEmpty() && event.getAction() == MotionEvent.ACTION_UP) {
                        float x = event.getX();
                        float y = event.getY();
                        int location[] = new int[2];
                        layersActionBarLayout.getLocationOnScreen(location);
                        int viewX = location[0];
                        int viewY = location[1];

                        if (layersActionBarLayout.checkTransitionAnimation() || x > viewX && x < viewX + layersActionBarLayout.getWidth() && y > viewY && y < viewY + layersActionBarLayout.getHeight()) {
                            return false;
                        } else {
                            if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                                for (int a = 0; a < layersActionBarLayout.fragmentsStack.size() - 1; a++) {
                                    layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.fragmentsStack.get(0));
                                    a--;
                                }
                                layersActionBarLayout.closeLastFragment(true);
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });

            shadowTablet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

            layersActionBarLayout = new ActionBarLayout(this);
            layersActionBarLayout.setRemoveActionBarExtraHeight(true);
            layersActionBarLayout.setBackgroundView(shadowTablet);
            layersActionBarLayout.setUseAlphaAnimations(true);
            layersActionBarLayout.setBackgroundResource(R.drawable.boxshadow);
            launchLayout.addView(layersActionBarLayout);

            relativeLayoutParams = (RelativeLayout.LayoutParams) layersActionBarLayout.getLayoutParams();
            relativeLayoutParams.width = AndroidUtilities.dp(530);
            relativeLayoutParams.height = AndroidUtilities.dp(528);
            layersActionBarLayout.setLayoutParams(relativeLayoutParams);
            layersActionBarLayout.init(layerFragmentsStack);
            layersActionBarLayout.setDelegate(this);
            layersActionBarLayout.setDrawerLayoutContainer(drawerLayoutContainer);
            layersActionBarLayout.setVisibility(View.GONE);
        } else {
            drawerLayoutContainer.addView(actionBarLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }

        ListView listView = new ListView(this) {
            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }
        };
        listView.setBackgroundColor(0xffffffff);
        listView.setAdapter(drawerLayoutAdapter = new DrawerLayoutAdapter(this));
        listView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setVerticalScrollBarEnabled(false);
        drawerLayoutContainer.setDrawerLayout(listView);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) listView.getLayoutParams();
        Point screenSize = AndroidUtilities.getRealScreenSize();
        layoutParams.width = AndroidUtilities.isTablet() ? AndroidUtilities.dp(320) : Math.min(AndroidUtilities.dp(320), Math.min(screenSize.x, screenSize.y) - AndroidUtilities.dp(56));
        layoutParams.height = LayoutHelper.MATCH_PARENT;
        listView.setLayoutParams(layoutParams);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3) {
                    if (!MessagesController.isFeatureEnabled("chat_create", actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1))) {
                        return;
                    }
                    presentFragment(new GroupCreateActivity());
                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 4) {
                    Bundle args = new Bundle();
                    args.putBoolean("onlyUsers", true);
                    args.putBoolean("destroyAfterSelect", true);
                    args.putBoolean("createSecretChat", true);
                    args.putBoolean("allowBots", false);
                    presentFragment(new ContactsActivity(args));
                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 7) {

                    if(ad != null && ad.isValid()) {
                        //showAddBtn.setEnabled(false);
                        TapsellShowOptions showOptions = new TapsellShowOptions();
                        showOptions.setBackDisabled(true);
                        showOptions.setImmersiveMode(true);
                        showOptions.setRotationMode(TapsellShowOptions.ROTATION_UNLOCKED);
                        showOptions.setShowDialog(false);
                        if(! ad.isShown()){
                            ad.show(LaunchActivity.this, showOptions, new TapsellAdShowListener() {
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
                            loadAd("597dcb8546846521dae4a29e", true);
                            Toast.makeText(LaunchActivity.this, "با سپاس از شما\nبه زودی ویدئو آماده نمایش خواهد شد" , Toast.LENGTH_LONG).show();
                        }

                    } else if( ad == null ){
                        Log.e("tapsell","null ad");
                        Toast.makeText(LaunchActivity.this, "با سپاس از شما\nدر حال حاضر تبلیغی وجود ندارد، لطفا چند دقیقه دیگر امتحان کنید" , Toast.LENGTH_LONG).show();
                    } else {
                        Log.e("tapsell","ad file removed? " + ad.isFileRemoved());
                        Log.e("tapsell","invalid ad, id=" + ad.getId());
                        Toast.makeText(LaunchActivity.this, "با سپاس از شما\nدر حال حاضر تبلیغی وجود ندارد، لطفا چند دقیقه دیگر امتحان کنید" , Toast.LENGTH_LONG).show();
                    }

                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 9) {
                    if (prem()) {
                        Bundle args = new Bundle();
                        presentFragment(new OnlineContactsActivity(args));
                        drawerLayoutContainer.closeDrawer(false);
                    } else {
                        goPurchas();
                    }
                } else if (position == 5) {
                    if (!MessagesController.isFeatureEnabled("broadcast_create", actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1))) {
                        return;
                    }
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                    if (preferences.getBoolean("channel_intro", false)) {
                        Bundle args = new Bundle();
                        args.putInt("step", 0);
                        presentFragment(new ChannelCreateActivity(args));
                    } else {
                        presentFragment(new ChannelIntroActivity());
                        preferences.edit().putBoolean("channel_intro", true).commit();
                    }
                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 8) {

                    TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
                    MessagesController.getInstance().openChatOrProfileWith(user, null, mainFragmentsStack.get(mainFragmentsStack.size() - 1), 1);

                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 10) {
                    presentFragment(new ContactsActivity(null));
                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 11) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
//            Uri pictureUri = Uri.parse("file://my_picture");
                        Uri uri = Uri.parse("android.resource://ir.limon.itel/"
                                +R.drawable.sharebanner);
                        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                        Bitmap icon = BitmapFactory.decodeResource(ApplicationLoader.applicationContext.getResources(),R.drawable.sharebanner);
                        icon.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                        File f = new File(Environment.getExternalStorageDirectory() + File.separator + "itel_tmp.jpg");
                        try {
                            f.createNewFile();
                            FileOutputStream fo = new FileOutputStream(f);
                            fo.write(bytes.toByteArray());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/itel_tmp.jpg"));
//                        intent.setType("text/plain");
                        String link = "http://cafebazaar.ir/app/ir.limon.itel";
//            Log.d(TAG,link);
//        String myPhone = Defaults.getInstance().getMyToken().split("\\.")[0];
                        String myPhone = UserConfig.getCurrentUser().phone;
//            Log.d(TAG,myPhone);
                        String text;

                        text = "پیام رسان iTel، تماس صوتی و تصویری رایگان بر بستر تلگرام" + "\nدانلود از کافه بازار: "+link;
//            Log.d(TAG,Share_Text);
                        intent.putExtra(Intent.EXTRA_TEXT,text);
//            intent.putExtra(Intent.EXTRA_STREAM, uri);

                        intent.setType("image/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteFriends", R.string.InviteFriends)), 500);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }

                    /*try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_TEXT, "پیام رسان iTel، تماس صوتی و تصویری رایگان بر بستر تلگرام\nدانلود از کافه بازار:\nhttp://cafebazaar.ir/app/ir.limon.itel");
                        startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteFriends", R.string.InviteFriends)), 500);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }*/
                    drawerLayoutContainer.closeDrawer(false);
           /*     }else if (position == 10) {
                    final Dialog dialog = new Dialog(LaunchActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.dialog_finglish_to_persian);
                    final WebView webView = (WebView) dialog.findViewById(R.id.webView);
                    final TextView retry2 = (TextView) dialog.findViewById(R.id.textView4);
                    retry2.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                ////    webView.loadData("<iframe src=\"http://behnevis.com/api/write_short_text_single.html\" width=\"500px\" height=\"180px\" align=\"center\" frameborder=\"1\" marginwidth=\"0px\" marginheight=\"0px\" scrolling=\"auto\"</iframe>", "text/html",
                           // "utf-8");
                     webView.loadUrl("file:///android_asset/others/behnev.htm");
                    WebSettings webViewSettings = webView.getSettings();
                    webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                    webViewSettings.setJavaScriptEnabled(true);
                   // webViewSettings.setPluginsEnabled(true);
                  //  webViewSettings.setBuiltInZoomControls(true);
                    webViewSettings.setPluginState(WebSettings.PluginState.ON);
                    dialog.show();

                    drawerLayoutContainer.closeDrawer(false);
*/
                } else if (position == 12) {

                    presentFragment(new TelehgramSettingsActivity());
                    drawerLayoutContainer.closeDrawer(false);

                } else if (position == 13) {
                    if (prem()) {
                        //startActivity(new Intent(ApplicationLoader.applicationContext, MainActivity.class));
                        startActivity(new Intent(ApplicationLoader.applicationContext, RadarMainActivity.class));
                        drawerLayoutContainer.closeDrawer(false);
                    } else {
                        goPurchas();
                    }
                } else if (position == 14) {
                    presentFragment(new SettingsActivity());
                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 2) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://cafebazaar.ir/app/ir.limon.gram/?l=fa"));
                        intent.setPackage("com.farseitel.cafebazaar");
                        ApplicationLoader.applicationContext.startActivity(intent);
                    } catch (Exception e) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://cafebazaar.ir/app/ir.limon.gram/?l=fa"));
                        ApplicationLoader.applicationContext.startActivity(intent);
                    }

                    /*if (prem()) {
                        presentFragment(new TelehgramSettingsActivity());
                        drawerLayoutContainer.closeDrawer(false);
                    } else {
                        goPurchas();
                    }*/
                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 15) {
                    if (prem()) {
                     //   startActivity(new Intent(ApplicationLoader.applicationContext, FilterActivity.class));
                        startActivity(new Intent(ApplicationLoader.applicationContext, ThemeMarket.class));
                        drawerLayoutContainer.closeDrawer(false);
                    } else {
                        goPurchas();
                    }
                } else if (position == 16) {
                    if (BuildConfig.IsDownloadManagerEnable) {
                        presentFragment(new AntiReportfragment());
                        drawerLayoutContainer.closeDrawer(false);
                    } else {
                        MessagesController.getInstance();
                        MessagesController.openByUserName("itelapp", mainFragmentsStack.get(mainFragmentsStack.size() - 1), 1);
                        drawerLayoutContainer.closeDrawer(false);
                    }
                } else if (position == 17) {
                    if (prem()) {
                        LaunchActivity.this.startActivity(new Intent(LaunchActivity.this, Download.class));
                        LaunchActivity.this.drawerLayoutContainer.closeDrawer(false);
                    } else {
                        goPurchas();
                    }
                } else if (position == 18) {

                    if(Build.VERSION.SDK_INT >= 23) {
                        if (!Settings.canDrawOverlays(LaunchActivity.this)){
                            AlertDialog.Builder builder = new AlertDialog.Builder(LaunchActivity.this);
                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));

                            builder.setMessage("برای فعالیت بهتر و عدم از دست دادن تماس ها، لطفا دسترسی مربوط به نمایش روی صفحه را فعال نمایید.\nاین دسترسی را مجدد می توانید از همین قسمت غیرفعال کنید.");

                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:" + getPackageName()));
                                    startActivityForResult(intent, 1234);
                                }
                            });
                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                            showAlertDialog(builder);

                        } else {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, 1234);
                        }
                    }


                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 19) {

                    drawerLayoutContainer.closeDrawer(false);
                } else if (position == 20) {

                    //________________________________________________________________________________________

                    MessagesController.getInstance();
                    MessagesController.openByUserName("itelapp", mainFragmentsStack.get(mainFragmentsStack.size() - 1), 1);

                    drawerLayoutContainer.closeDrawer(false);
                    //________________________________________________
                } else if (position == 21){
                    Intent intent2 = new Intent(LaunchActivity.this, org.telegram.ui.Telehgram.intro.MainActivity.class);
                    startActivity(intent2);

                    drawerLayoutContainer.closeDrawer(false);
                }
            }
        });

        drawerLayoutContainer.setParentActionBarLayout(actionBarLayout);
        actionBarLayout.setDrawerLayoutContainer(drawerLayoutContainer);
        actionBarLayout.init(mainFragmentsStack);
        actionBarLayout.setDelegate(this);

        ApplicationLoader.loadWallpaper();

        passcodeView = new PasscodeView(this);
        drawerLayoutContainer.addView(passcodeView);
        FrameLayout.LayoutParams layoutParams1 = (FrameLayout.LayoutParams) passcodeView.getLayoutParams();
        layoutParams1.width = LayoutHelper.MATCH_PARENT;
        layoutParams1.height = LayoutHelper.MATCH_PARENT;
        passcodeView.setLayoutParams(layoutParams1);

        NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeOtherAppActivities, this);
        currentConnectionState = ConnectionsManager.getInstance().getConnectionState();

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.appDidLogout);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.mainUserInfoChanged);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeOtherAppActivities);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.didUpdatedConnectionState);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.needShowAlert);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);

        if (actionBarLayout.fragmentsStack.isEmpty()) {
            if (!UserConfig.isClientActivated()) {
                actionBarLayout.addFragmentToStack(new LoginActivity());
                drawerLayoutContainer.setAllowOpenDrawer(false, false);
            } else {
                actionBarLayout.addFragmentToStack(new DialogsActivity(null));
                drawerLayoutContainer.setAllowOpenDrawer(true, false);
            }

            try {
                if (savedInstanceState != null) {
                    String fragmentName = savedInstanceState.getString("fragment");
                    if (fragmentName != null) {
                        Bundle args = savedInstanceState.getBundle("args");
                        switch (fragmentName) {
                            case "chat":
                                if (args != null) {
                                    ChatActivity chat = new ChatActivity(args);
                                    if (actionBarLayout.addFragmentToStack(chat)) {
                                        chat.restoreSelfArgs(savedInstanceState);
                                    }
                                }
                                break;
                            case "settings": {
                                SettingsActivity settings = new SettingsActivity();
                                actionBarLayout.addFragmentToStack(settings);
                                settings.restoreSelfArgs(savedInstanceState);
                                break;
                            }
                            case "group":
                                if (args != null) {
                                    GroupCreateFinalActivity group = new GroupCreateFinalActivity(args);
                                    if (actionBarLayout.addFragmentToStack(group)) {
                                        group.restoreSelfArgs(savedInstanceState);
                                    }
                                }
                                break;
                            case "channel":
                                if (args != null) {
                                    ChannelCreateActivity channel = new ChannelCreateActivity(args);
                                    if (actionBarLayout.addFragmentToStack(channel)) {
                                        channel.restoreSelfArgs(savedInstanceState);
                                    }
                                }
                                break;
                            case "edit":
                                if (args != null) {
                                    ChannelEditActivity channel = new ChannelEditActivity(args);
                                    if (actionBarLayout.addFragmentToStack(channel)) {
                                        channel.restoreSelfArgs(savedInstanceState);
                                    }
                                }
                                break;
                            case "chat_profile":
                                if (args != null) {
                                    ProfileActivity profile = new ProfileActivity(args);
                                    if (actionBarLayout.addFragmentToStack(profile)) {
                                        profile.restoreSelfArgs(savedInstanceState);
                                    }
                                }
                                break;
                            case "wallpapers": {
                                WallpapersActivity settings = new WallpapersActivity();
                                actionBarLayout.addFragmentToStack(settings);
                                settings.restoreSelfArgs(savedInstanceState);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        } else {
            boolean allowOpen = true;
            if (AndroidUtilities.isTablet()) {
                allowOpen = actionBarLayout.fragmentsStack.size() <= 1 && layersActionBarLayout.fragmentsStack.isEmpty();
                if (layersActionBarLayout.fragmentsStack.size() == 1 && layersActionBarLayout.fragmentsStack.get(0) instanceof LoginActivity) {
                    allowOpen = false;
                }
            }
            if (actionBarLayout.fragmentsStack.size() == 1 && actionBarLayout.fragmentsStack.get(0) instanceof LoginActivity) {
                allowOpen = false;
            }
            drawerLayoutContainer.setAllowOpenDrawer(allowOpen, false);
        }
        //    if(UserConfig.isClientActivated()&&ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", 0).getBoolean("chaneljoined", true)) {
        //  Intent i = new Intent(Intent.ACTION_VIEW);
        // i.setData(Uri.parse("https://telegram.me/joinchat/BhJbiD57qFKAJbS0F3cf-Q"));
        //  handleIntent(i, false, savedInstanceState != null, false);

        String owrchannel = "itelapp";
        if (owrchannel != null) {
            Log.i("OneSignalExample", "customkey set with value: " + owrchannel);
            TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
            req.username = owrchannel;
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
        }

        handleIntent(getIntent(), false, savedInstanceState != null, false);
        needLayout();

        final View view = getWindow().getDecorView().getRootView();
        view.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = view.getMeasuredHeight();
                if (height > AndroidUtilities.dp(100) && height < AndroidUtilities.displaySize.y && height + AndroidUtilities.dp(100) > AndroidUtilities.displaySize.y) {
                    AndroidUtilities.displaySize.y = height;
                    FileLog.e("tmessages", "fix display size y to " + AndroidUtilities.displaySize.y);
                }
            }
        });

        //getChat();
    }

    private void loadAd(String zoneId, final Boolean iwant) {
        TapsellAdRequestOptions options = new TapsellAdRequestOptions(TapsellAdRequestOptions.CACHE_TYPE_CACHED);

        Tapsell.requestAd(LaunchActivity.this, zoneId, options, new TapsellAdRequestListener() {
            @Override
            public void onError(String error) {
                Log.d("Tapsell Sample","Error: "+error);
            }

            @Override
            public void onAdAvailable(TapsellAd ad) {

                LaunchActivity.this.ad = ad;
                //showAddBtn.setEnabled(true);
                Log.d("Tapsell Sample","Ad is available");
                if(iwant){
                    Toast.makeText(LaunchActivity.this, "ویدئوی حمایتی آماده نمایش است", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNoAdAvailable() {
                Log.d("Tapsell Sample","No ad available");

            }

            @Override
            public void onNoNetwork() {
                Log.d("Tapsell Sample","No network");
                Toast.makeText(LaunchActivity.this, "دستگاه به اینترنت متصل نیست", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onExpiring(TapsellAd ad) {
                //showAddBtn.setEnabled(false);
                loadAd("597dcb8546846521dae4a29e", false);
            }
        });

    }

    public void getChat(){
        Bundle args = new Bundle();
        args.putInt("chat_id", 1001824681);
        if (mainFragmentsStack.isEmpty() || MessagesController.checkCanOpenChat(args, mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
            ChatActivity fragment = new ChatActivity(args);
            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
            actionBarLayout.presentFragment(fragment, false, true, true);
        }
    }

    /*sinch codes*/
    private void registerSinchAsClinet(){
        TLRPC.User user = MessagesController.getInstance().getUser(UserConfig.getClientUserId());
        if(user != null){
            try {
                if (user.phone != null && user.phone.length() != 0) {
                    String mynum = user.id + "";

                    if (!getSinchServiceInterface().isStarted()) {
                        getSinchServiceInterface().startClient(mynum);
                        //Toast.makeText(this, mynum + " Registered!", Toast.LENGTH_SHORT).show();
                    } else {
                        //openPlaceCallActivity();
                    }
                }
            }catch (Exception e) {
                Log.d("Sinch","Exception registerSinchAsClinet");
            }
        } else {

        }
    }

    @Override
    protected void onServiceConnected() {
        Log.d("Sinch","onServiceConnected");
        registerSinchAsClinet();
        getSinchServiceInterface().setStartListener(this);
    }

    @Override
    public void onStartFailed(SinchError error) {
        //Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
        Log.d("Sinch", "onStartFailed: " + error.toString());
    }

    @Override
    public void onStarted() {
        //openPlaceCallActivity();
        ProfileActivity.sinchactive = true;
        Log.d("Sinch","onStarted");
    }
    /*sinch code*/


    private void showPasscodeActivity() {
        if (passcodeView == null) {
            return;
        }
        UserConfig.appLocked = true;
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(false, true);
        }
        passcodeView.onShow();
        UserConfig.isWaitingForPasscodeEnter = true;
        drawerLayoutContainer.setAllowOpenDrawer(false, false);
        passcodeView.setDelegate(new PasscodeView.PasscodeViewDelegate() {
            @Override
            public void didAcceptedPassword() {
                UserConfig.isWaitingForPasscodeEnter = false;
                if (passcodeSaveIntent != null) {
                    handleIntent(passcodeSaveIntent, passcodeSaveIntentIsNew, passcodeSaveIntentIsRestore, true);
                    passcodeSaveIntent = null;
                }
                drawerLayoutContainer.setAllowOpenDrawer(true, false);
                actionBarLayout.showLastFragment();
                if (AndroidUtilities.isTablet()) {
                    layersActionBarLayout.showLastFragment();
                    rightActionBarLayout.showLastFragment();
                }
            }
        });
    }

    private boolean handleIntent(Intent intent, boolean isNew, boolean restore, boolean fromPassword) {
        int flags = intent.getFlags();
        if (!fromPassword && (AndroidUtilities.needShowPasscode(true) || UserConfig.isWaitingForPasscodeEnter)) {
            showPasscodeActivity();
            passcodeSaveIntent = intent;
            passcodeSaveIntentIsNew = isNew;
            passcodeSaveIntentIsRestore = restore;
            UserConfig.saveConfig(false);
        } else {
            boolean pushOpened = false;

            Integer push_user_id = 0;
            Integer push_chat_id = 0;
            Integer push_enc_id = 0;
            Integer open_settings = 0;
            long dialogId = intent != null && intent.getExtras() != null ? intent.getExtras().getLong("dialogId", 0) : 0;
            boolean showDialogsList = false;
            boolean showPlayer = false;

            photoPathsArray = null;
            videoPath = null;
            sendingText = null;
            documentsPathsArray = null;
            documentsOriginalPathsArray = null;
            documentsMimeType = null;
            documentsUrisArray = null;
            contactsToSend = null;

            if (UserConfig.isClientActivated() && (flags & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
                if (intent != null && intent.getAction() != null && !restore) {
                    if (Intent.ACTION_SEND.equals(intent.getAction())) {
                        boolean error = false;
                        String type = intent.getType();
                        if (type != null && type.equals(ContactsContract.Contacts.CONTENT_VCARD_TYPE)) {
                            try {
                                Uri uri = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
                                if (uri != null) {
                                    ContentResolver cr = getContentResolver();
                                    InputStream stream = cr.openInputStream(uri);
                                    ArrayList<VcardData> vcardDatas = new ArrayList<>();
                                    VcardData currentData = null;

                                    String name = null;
                                    String nameEncoding = null;
                                    String nameCharset = null;
                                    ArrayList<String> phones = new ArrayList<>();
                                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
                                    String line;
                                    while ((line = bufferedReader.readLine()) != null) {
                                        FileLog.e("tmessages", line);
                                        String[] args = line.split(":");
                                        if (args.length != 2) {
                                            continue;
                                        }
                                        if (args[0].equals("BEGIN") && args[1].equals("VCARD")) {
                                            vcardDatas.add(currentData = new VcardData());
                                        } else if (args[0].equals("END") && args[1].equals("VCARD")) {
                                            currentData = null;
                                        }
                                        if (currentData == null) {
                                            continue;
                                        }
                                        if (args[0].startsWith("FN") || args[0].startsWith("ORG") && TextUtils.isEmpty(currentData.name)) {

                                            String[] params = args[0].split(";");
                                            for (String param : params) {
                                                String[] args2 = param.split("=");
                                                if (args2.length != 2) {
                                                    continue;
                                                }
                                                if (args2[0].equals("CHARSET")) {
                                                    nameCharset = args2[1];
                                                } else if (args2[0].equals("ENCODING")) {
                                                    nameEncoding = args2[1];
                                                }
                                            }
                                            currentData.name = args[1];
                                            if (nameEncoding != null && nameEncoding.equalsIgnoreCase("QUOTED-PRINTABLE")) {
                                                while (currentData.name.endsWith("=") && nameEncoding != null) {
                                                    currentData.name = currentData.name.substring(0, currentData.name.length() - 1);
                                                    line = bufferedReader.readLine();
                                                    if (line == null) {
                                                        break;
                                                    }
                                                    currentData.name += line;
                                                }
                                                byte[] bytes = AndroidUtilities.decodeQuotedPrintable(currentData.name.getBytes());
                                                if (bytes != null && bytes.length != 0) {
                                                    String decodedName = new String(bytes, nameCharset);
                                                    if (decodedName != null) {
                                                        currentData.name = decodedName;
                                                    }
                                                }
                                            }
                                        } else if (args[0].startsWith("TEL")) {
                                            String phone = PhoneFormat.stripExceptNumbers(args[1], true);
                                            if (phone.length() > 0) {
                                                currentData.phones.add(phone);
                                            }
                                        }
                                    }
                                    try {
                                        bufferedReader.close();
                                        stream.close();
                                    } catch (Exception e) {
                                        FileLog.e("tmessages", e);
                                    }
                                    for (int a = 0; a < vcardDatas.size(); a++) {
                                        VcardData vcardData = vcardDatas.get(a);
                                        if (vcardData.name != null && !vcardData.phones.isEmpty()) {
                                            if (contactsToSend == null) {
                                                contactsToSend = new ArrayList<>();
                                            }

                                            for (int b = 0; b < vcardData.phones.size(); b++) {
                                                String phone = vcardData.phones.get(b);
                                                TLRPC.User user = new TLRPC.TL_userContact_old2();
                                                user.phone = phone;
                                                user.first_name = vcardData.name;
                                                user.last_name = "";
                                                user.id = 0;
                                                contactsToSend.add(user);
                                            }
                                        }
                                    }
                                } else {
                                    error = true;
                                }
                            } catch (Exception e) {
                                FileLog.e("tmessages", e);
                                error = true;
                            }
                        } else {
                            String text = intent.getStringExtra(Intent.EXTRA_TEXT);
                            if (text == null) {
                                CharSequence textSequence = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
                                if (textSequence != null) {
                                    text = textSequence.toString();
                                }
                            }
                            String subject = intent.getStringExtra(Intent.EXTRA_SUBJECT);

                            if (text != null && text.length() != 0) {
                                if ((text.startsWith("http://") || text.startsWith("https://")) && subject != null && subject.length() != 0) {
                                    text = subject + "\n" + text;
                                }
                                sendingText = text;
                            } else if (subject != null && subject.length() > 0) {
                                sendingText = subject;
                            }

                            Parcelable parcelable = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                            if (parcelable != null) {
                                String path;
                                if (!(parcelable instanceof Uri)) {
                                    parcelable = Uri.parse(parcelable.toString());
                                }
                                Uri uri = (Uri) parcelable;
                                if (uri != null) {
                                    if (AndroidUtilities.isInternalUri(uri)) {
                                        error = true;
                                    }
                                }
                                if (!error) {
                                    if (uri != null && (type != null && type.startsWith("image/") || uri.toString().toLowerCase().endsWith(".jpg"))) {
                                        if (photoPathsArray == null) {
                                            photoPathsArray = new ArrayList<>();
                                        }
                                        photoPathsArray.add(uri);
                                    } else {
                                        path = AndroidUtilities.getPath(uri);
                                        if (path != null) {
                                            if (path.startsWith("file:")) {
                                                path = path.replace("file://", "");
                                            }
                                            if (type != null && type.startsWith("video/")) {
                                                videoPath = path;
                                            } else {
                                                if (documentsPathsArray == null) {
                                                    documentsPathsArray = new ArrayList<>();
                                                    documentsOriginalPathsArray = new ArrayList<>();
                                                }
                                                documentsPathsArray.add(path);
                                                documentsOriginalPathsArray.add(uri.toString());
                                            }
                                        } else {
                                            if (documentsUrisArray == null) {
                                                documentsUrisArray = new ArrayList<>();
                                            }
                                            documentsUrisArray.add(uri);
                                            documentsMimeType = type;
                                        }
                                    }
                                }
                            } else if (sendingText == null) {
                                error = true;
                            }
                        }
                        if (error) {
                            Toast.makeText(this, "Unsupported content", Toast.LENGTH_SHORT).show();
                        }
                    } else if (intent.getAction().equals(Intent.ACTION_SEND_MULTIPLE)) {
                        boolean error = false;
                        try {
                            ArrayList<Parcelable> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                            String type = intent.getType();
                            if (uris != null) {
                                for (int a = 0; a < uris.size(); a++) {
                                    Parcelable parcelable = uris.get(a);
                                    if (!(parcelable instanceof Uri)) {
                                        parcelable = Uri.parse(parcelable.toString());
                                    }
                                    Uri uri = (Uri) parcelable;
                                    if (uri != null) {
                                        if (AndroidUtilities.isInternalUri(uri)) {
                                            uris.remove(a);
                                            a--;
                                        }
                                    }
                                }
                                if (uris.isEmpty()) {
                                    uris = null;
                                }
                            }
                            if (uris != null) {
                                if (type != null && type.startsWith("image/")) {
                                    for (int a = 0; a < uris.size(); a++) {
                                        Parcelable parcelable = uris.get(a);
                                        if (!(parcelable instanceof Uri)) {
                                            parcelable = Uri.parse(parcelable.toString());
                                        }
                                        Uri uri = (Uri) parcelable;
                                        if (photoPathsArray == null) {
                                            photoPathsArray = new ArrayList<>();
                                        }
                                        photoPathsArray.add(uri);
                                    }
                                } else {
                                    for (int a = 0; a < uris.size(); a++) {
                                        Parcelable parcelable = uris.get(a);
                                        if (!(parcelable instanceof Uri)) {
                                            parcelable = Uri.parse(parcelable.toString());
                                        }
                                        String path = AndroidUtilities.getPath((Uri) parcelable);
                                        String originalPath = parcelable.toString();
                                        if (originalPath == null) {
                                            originalPath = path;
                                        }
                                        if (path != null) {
                                            if (path.startsWith("file:")) {
                                                path = path.replace("file://", "");
                                            }
                                            if (documentsPathsArray == null) {
                                                documentsPathsArray = new ArrayList<>();
                                                documentsOriginalPathsArray = new ArrayList<>();
                                            }
                                            documentsPathsArray.add(path);
                                            documentsOriginalPathsArray.add(originalPath);
                                        }
                                    }
                                }
                            } else {
                                error = true;
                            }
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                            error = true;
                        }
                        if (error) {
                            Toast.makeText(this, "Unsupported content", Toast.LENGTH_SHORT).show();
                        }
                    } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                        Uri data = intent.getData();
                        if (data != null) {
                            String username = null;
                            String group = null;
                            String sticker = null;
                            String botUser = null;
                            String botChat = null;
                            String message = null;
                            Integer messageId = null;
                            boolean hasUrl = false;
                            String scheme = data.getScheme();
                            if (scheme != null) {
                                if ((scheme.equals("http") || scheme.equals("https"))) {
                                    String host = data.getHost().toLowerCase();
                                    if (host.equals("telegram.me") || host.equals("telegram.dog")) {
                                        String path = data.getPath();
                                        if (path != null && path.length() > 1) {
                                            path = path.substring(1);
                                            if (path.startsWith("joinchat/")) {
                                                group = path.replace("joinchat/", "");
                                            } else if (path.startsWith("addstickers/")) {
                                                sticker = path.replace("addstickers/", "");
                                            } else if (path.startsWith("msg/") || path.startsWith("share/")) {
                                                message = data.getQueryParameter("url");
                                                if (message == null) {
                                                    message = "";
                                                }
                                                if (data.getQueryParameter("text") != null) {
                                                    if (message.length() > 0) {
                                                        hasUrl = true;
                                                        message += "\n";
                                                    }
                                                    message += data.getQueryParameter("text");
                                                }
                                            } else if (path.length() >= 1) {
                                                List<String> segments = data.getPathSegments();
                                                if (segments.size() > 0) {
                                                    username = segments.get(0);
                                                    if (segments.size() > 1) {
                                                        messageId = Utilities.parseInt(segments.get(1));
                                                        if (messageId == 0) {
                                                            messageId = null;
                                                        }
                                                    }
                                                }
                                                botUser = data.getQueryParameter("start");
                                                botChat = data.getQueryParameter("startgroup");
                                            }
                                        }
                                    }
                                } else if (scheme.equals("tg")) {
                                    String url = data.toString();
                                    if (url.startsWith("tg:resolve") || url.startsWith("tg://resolve")) {
                                        url = url.replace("tg:resolve", "tg://telegram.org").replace("tg://resolve", "tg://telegram.org");
                                        data = Uri.parse(url);
                                        username = data.getQueryParameter("domain");
                                        botUser = data.getQueryParameter("start");
                                        botChat = data.getQueryParameter("startgroup");
                                        messageId = Utilities.parseInt(data.getQueryParameter("post"));
                                        if (messageId == 0) {
                                            messageId = null;
                                        }
                                    } else if (url.startsWith("tg:join") || url.startsWith("tg://join")) {
                                        url = url.replace("tg:join", "tg://telegram.org").replace("tg://join", "tg://telegram.org");
                                        data = Uri.parse(url);
                                        group = data.getQueryParameter("invite");
                                    } else if (url.startsWith("tg:addstickers") || url.startsWith("tg://addstickers")) {
                                        url = url.replace("tg:addstickers", "tg://telegram.org").replace("tg://addstickers", "tg://telegram.org");
                                        data = Uri.parse(url);
                                        sticker = data.getQueryParameter("set");
                                    } else if (url.startsWith("tg:msg") || url.startsWith("tg://msg") || url.startsWith("tg://share") || url.startsWith("tg:share")) {
                                        url = url.replace("tg:msg", "tg://telegram.org").replace("tg://msg", "tg://telegram.org").replace("tg://share", "tg://telegram.org").replace("tg:share", "tg://telegram.org");
                                        data = Uri.parse(url);
                                        message = data.getQueryParameter("url");
                                        if (message == null) {
                                            message = "";
                                        }
                                        if (data.getQueryParameter("text") != null) {
                                            if (message.length() > 0) {
                                                hasUrl = true;
                                                message += "\n";
                                            }
                                            message += data.getQueryParameter("text");
                                        }
                                    }
                                }
                            }
                            if (username != null || group != null || sticker != null || message != null) {
                                runLinkRequest(username, group, sticker, botUser, botChat, message, hasUrl, messageId, 0);
                            } else {
                                try {
                                    Cursor cursor = getContentResolver().query(intent.getData(), null, null, null, null);
                                    if (cursor != null) {
                                        if (cursor.moveToFirst()) {
                                            int userId = cursor.getInt(cursor.getColumnIndex("DATA4"));
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                                            push_user_id = userId;
                                        }
                                        cursor.close();
                                    }
                                } catch (Exception e) {
                                    FileLog.e("tmessages", e);
                                }
                            }
                        }
                    } else if (intent.getAction().equals("org.telegram.messenger.OPEN_ACCOUNT")) {
                        open_settings = 1;
                    } else if (intent.getAction().startsWith("com.tmessages.openchat")) {
                        int chatId = intent.getIntExtra("chatId", 0);
                        int userId = intent.getIntExtra("userId", 0);
                        int encId = intent.getIntExtra("encId", 0);
                        if (chatId != 0) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            push_chat_id = chatId;
                        } else if (userId != 0) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            push_user_id = userId;
                        } else if (encId != 0) {
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                            push_enc_id = encId;
                        } else {
                            showDialogsList = true;
                        }
                    } else if (intent.getAction().equals("com.tmessages.openplayer")) {
                        showPlayer = true;
                    }
                }
            }

            if (push_user_id != 0) {
                Bundle args = new Bundle();
                args.putInt("user_id", push_user_id);
                if (mainFragmentsStack.isEmpty() || MessagesController.checkCanOpenChat(args, mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
                    ChatActivity fragment = new ChatActivity(args);
                    if (actionBarLayout.presentFragment(fragment, false, true, true)) {
                        pushOpened = true;
                    }
                }
            } else if (push_chat_id != 0) {
                Bundle args = new Bundle();
                args.putInt("chat_id", push_chat_id);
                if (mainFragmentsStack.isEmpty() || MessagesController.checkCanOpenChat(args, mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
                    ChatActivity fragment = new ChatActivity(args);
                    if (actionBarLayout.presentFragment(fragment, false, true, true)) {
                        pushOpened = true;
                    }
                }
            } else if (push_enc_id != 0) {
                Bundle args = new Bundle();
                args.putInt("enc_id", push_enc_id);
                ChatActivity fragment = new ChatActivity(args);
                if (actionBarLayout.presentFragment(fragment, false, true, true)) {
                    pushOpened = true;
                }
            } else if (showDialogsList) {
                if (!AndroidUtilities.isTablet()) {
                    actionBarLayout.removeAllFragments();
                } else {
                    if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                        for (int a = 0; a < layersActionBarLayout.fragmentsStack.size() - 1; a++) {
                            layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.fragmentsStack.get(0));
                            a--;
                        }
                        layersActionBarLayout.closeLastFragment(false);
                    }
                }
                pushOpened = false;
                isNew = false;
            } else if (showPlayer) {
                if (AndroidUtilities.isTablet()) {
                    for (int a = 0; a < layersActionBarLayout.fragmentsStack.size(); a++) {
                        BaseFragment fragment = layersActionBarLayout.fragmentsStack.get(a);
                        if (fragment instanceof AudioPlayerActivity) {
                            layersActionBarLayout.removeFragmentFromStack(fragment);
                            break;
                        }
                    }
                    actionBarLayout.showLastFragment();
                    rightActionBarLayout.showLastFragment();
                    drawerLayoutContainer.setAllowOpenDrawer(false, false);
                } else {
                    for (int a = 0; a < actionBarLayout.fragmentsStack.size(); a++) {
                        BaseFragment fragment = actionBarLayout.fragmentsStack.get(a);
                        if (fragment instanceof AudioPlayerActivity) {
                            actionBarLayout.removeFragmentFromStack(fragment);
                            break;
                        }
                    }
                    drawerLayoutContainer.setAllowOpenDrawer(true, false);
                }
                actionBarLayout.presentFragment(new AudioPlayerActivity(), false, true, true);
                pushOpened = true;
            } else if (videoPath != null || photoPathsArray != null || sendingText != null || documentsPathsArray != null || contactsToSend != null || documentsUrisArray != null) {
                if (!AndroidUtilities.isTablet()) {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                }
                if (dialogId == 0) {
                    Bundle args = new Bundle();
                    args.putBoolean("onlySelect", true);
                    if (contactsToSend != null) {
                        args.putString("selectAlertString", LocaleController.getString("SendContactTo", R.string.SendMessagesTo));
                        args.putString("selectAlertStringGroup", LocaleController.getString("SendContactToGroup", R.string.SendContactToGroup));
                    } else {
                        args.putString("selectAlertString", LocaleController.getString("SendMessagesTo", R.string.SendMessagesTo));
                        args.putString("selectAlertStringGroup", LocaleController.getString("SendMessagesToGroup", R.string.SendMessagesToGroup));
                    }
                    DialogsActivity fragment = new DialogsActivity(args);
                    fragment.setDelegate(this);
                    boolean removeLast;
                    if (AndroidUtilities.isTablet()) {
                        removeLast = layersActionBarLayout.fragmentsStack.size() > 0 && layersActionBarLayout.fragmentsStack.get(layersActionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity;
                    } else {
                        removeLast = actionBarLayout.fragmentsStack.size() > 1 && actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1) instanceof DialogsActivity;
                    }
                    actionBarLayout.presentFragment(fragment, removeLast, true, true);
                    pushOpened = true;
                    if (PhotoViewer.getInstance().isVisible()) {
                        PhotoViewer.getInstance().closePhoto(false, true);
                    }

                    drawerLayoutContainer.setAllowOpenDrawer(false, false);
                    if (AndroidUtilities.isTablet()) {
                        actionBarLayout.showLastFragment();
                        rightActionBarLayout.showLastFragment();
                    } else {
                        drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    }
                } else {
                    didSelectDialog(null, dialogId, false);
                }
            } else if (open_settings != 0) {
                actionBarLayout.presentFragment(new SettingsActivity(), false, true, true);
                if (AndroidUtilities.isTablet()) {
                    actionBarLayout.showLastFragment();
                    rightActionBarLayout.showLastFragment();
                    drawerLayoutContainer.setAllowOpenDrawer(false, false);
                } else {
                    drawerLayoutContainer.setAllowOpenDrawer(true, false);
                }
                pushOpened = true;
            }

            if (!pushOpened && !isNew) {
                if (AndroidUtilities.isTablet()) {
                    if (!UserConfig.isClientActivated()) {
                        if (layersActionBarLayout.fragmentsStack.isEmpty()) {
                            layersActionBarLayout.addFragmentToStack(new LoginActivity());
                            drawerLayoutContainer.setAllowOpenDrawer(false, false);
                        }
                    } else {
                        if (actionBarLayout.fragmentsStack.isEmpty()) {
                            actionBarLayout.addFragmentToStack(new DialogsActivity(null));
                            drawerLayoutContainer.setAllowOpenDrawer(true, false);
                        }
                    }
                } else {
                    if (actionBarLayout.fragmentsStack.isEmpty()) {
                        if (!UserConfig.isClientActivated()) {
                            actionBarLayout.addFragmentToStack(new LoginActivity());
                            drawerLayoutContainer.setAllowOpenDrawer(false, false);
                        } else {
                            actionBarLayout.addFragmentToStack(new DialogsActivity(null));
                            drawerLayoutContainer.setAllowOpenDrawer(true, false);
                        }
                    }
                }
                actionBarLayout.showLastFragment();
                if (AndroidUtilities.isTablet()) {
                    layersActionBarLayout.showLastFragment();
                    rightActionBarLayout.showLastFragment();
                }
            }

            intent.setAction(null);
            return pushOpened;
        }
        return false;
    }

    private void runLinkRequest(final String username, final String group, final String sticker, final String botUser, final String botChat, final String message, final boolean hasUrl, final Integer messageId, final int state) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(LocaleController.getString("Loading", R.string.Loading));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        int requestId = 0;

        if (username != null) {
            TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
            req.username = username;
            requestId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                @Override
                public void run(final TLObject response, final TLRPC.TL_error error) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!LaunchActivity.this.isFinishing()) {
                                try {
                                    progressDialog.dismiss();
                                } catch (Exception e) {
                                    FileLog.e("tmessages", e);
                                }
                                if (error == null && actionBarLayout != null) {
                                    final TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                                    MessagesController.getInstance().putUsers(res.users, false);
                                    MessagesController.getInstance().putChats(res.chats, false);
                                    MessagesStorage.getInstance().putUsersAndChats(res.users, res.chats, false, true);

                                    if (botChat != null) {
                                        final TLRPC.User user = !res.users.isEmpty() ? res.users.get(0) : null;
                                        if (user == null || user.bot && user.bot_nochats) {
                                            try {
                                                Toast.makeText(LaunchActivity.this, LocaleController.getString("BotCantJoinGroups", R.string.BotCantJoinGroups), Toast.LENGTH_SHORT).show();
                                            } catch (Exception e) {
                                                FileLog.e("tmessages", e);
                                            }
                                            return;
                                        }
                                        Bundle args = new Bundle();
                                        args.putBoolean("onlySelect", true);
                                        args.putInt("dialogsType", 2);
                                        args.putString("addToGroupAlertString", LocaleController.formatString("AddToTheGroupTitle", R.string.AddToTheGroupTitle, UserObject.getUserName(user), "%1$s"));
                                        DialogsActivity fragment = new DialogsActivity(args);
                                        fragment.setDelegate(new DialogsActivity.DialogsActivityDelegate() {
                                            @Override
                                            public void didSelectDialog(DialogsActivity fragment, long did, boolean param) {
                                                Bundle args = new Bundle();
                                                args.putBoolean("scrollToTopOnResume", true);
                                                args.putInt("chat_id", -(int) did);
                                                if (mainFragmentsStack.isEmpty() || MessagesController.checkCanOpenChat(args, mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
                                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                                                    MessagesController.getInstance().addUserToChat(-(int) did, user, null, 0, botChat, null);
                                                    actionBarLayout.presentFragment(new ChatActivity(args), true, false, true);
                                                }
                                            }
                                        });
                                        presentFragment(fragment);
                                    } else {
                                        long dialog_id;
                                        boolean isBot = false;
                                        Bundle args = new Bundle();
                                        if (!res.chats.isEmpty()) {
                                            args.putInt("chat_id", res.chats.get(0).id);
                                            dialog_id = -res.chats.get(0).id;
                                        } else {
                                            args.putInt("user_id", res.users.get(0).id);
                                            dialog_id = res.users.get(0).id;
                                        }
                                        if (botUser != null && res.users.size() > 0 && res.users.get(0).bot) {
                                            args.putString("botUser", botUser);
                                            isBot = true;
                                        }
                                        if (messageId != null) {
                                            args.putInt("message_id", messageId);
                                        }
                                        BaseFragment lastFragment = !mainFragmentsStack.isEmpty() ? mainFragmentsStack.get(mainFragmentsStack.size() - 1) : null;
                                        if (lastFragment == null || MessagesController.checkCanOpenChat(args, lastFragment)) {
                                            if (isBot && lastFragment != null && lastFragment instanceof ChatActivity && ((ChatActivity) lastFragment).getDialogId() == dialog_id) {
                                                ((ChatActivity) lastFragment).setBotUser(botUser);
                                            } else {
                                                ChatActivity fragment = new ChatActivity(args);
                                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                                                actionBarLayout.presentFragment(fragment, false, true, true);
                                            }
                                        }
                                    }
                                } else {
                                    try {
                                        Toast.makeText(LaunchActivity.this, LocaleController.getString("NoUsernameFound", R.string.NoUsernameFound), Toast.LENGTH_SHORT).show();
                                    } catch (Exception e) {
                                        FileLog.e("tmessages", e);
                                    }
                                }
                            }
                        }
                    });
                }
            });
        } else if (group != null) {
            if (state == 0) {
                final TLRPC.TL_messages_checkChatInvite req = new TLRPC.TL_messages_checkChatInvite();
                req.hash = group;
                requestId = ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
                    @Override
                    public void run(final TLObject response, final TLRPC.TL_error error) {
                        AndroidUtilities.runOnUIThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!LaunchActivity.this.isFinishing()) {
                                    try {
                                        progressDialog.dismiss();
                                    } catch (Exception e) {
                                        FileLog.e("tmessages", e);
                                    }
                                    if (error == null && actionBarLayout != null) {
                                        TLRPC.ChatInvite invite = (TLRPC.ChatInvite) response;
                                        if (invite.chat != null && !ChatObject.isLeftFromChat(invite.chat)) {
                                            MessagesController.getInstance().putChat(invite.chat, false);
                                            ArrayList<TLRPC.Chat> chats = new ArrayList<>();
                                            chats.add(invite.chat);
                                            MessagesStorage.getInstance().putUsersAndChats(null, chats, false, true);
                                            Bundle args = new Bundle();
                                            args.putInt("chat_id", invite.chat.id);
                                            if (mainFragmentsStack.isEmpty() || MessagesController.checkCanOpenChat(args, mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
                                                ChatActivity fragment = new ChatActivity(args);
                                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                                                actionBarLayout.presentFragment(fragment, false, true, true);
                                            }
                                        } else {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(LaunchActivity.this);
                                            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                            if (!invite.megagroup && invite.channel || ChatObject.isChannel(invite.chat) && !invite.chat.megagroup) {
                                                builder.setMessage(LocaleController.formatString("ChannelJoinTo", R.string.ChannelJoinTo, invite.chat != null ? invite.chat.title : invite.title));
                                            } else {
                                                builder.setMessage(LocaleController.formatString("JoinToGroup", R.string.JoinToGroup, invite.chat != null ? invite.chat.title : invite.title));
                                            }
                                            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    runLinkRequest(username, group, sticker, botUser, botChat, message, hasUrl, messageId, 1);
                                                }
                                            });
                                            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                            showAlertDialog(builder);
                                        }
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LaunchActivity.this);
                                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                        if (error.text.startsWith("FLOOD_WAIT")) {
                                            builder.setMessage(LocaleController.getString("FloodWait", R.string.FloodWait));
                                        } else {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", R.string.JoinToGroupErrorNotExist));
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                                        showAlertDialog(builder);
                                    }
                                }
                            }
                        });
                    }
                }, ConnectionsManager.RequestFlagFailOnServerErrors);
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
                                if (!LaunchActivity.this.isFinishing()) {
                                    try {
                                        progressDialog.dismiss();
                                    } catch (Exception e) {
                                        FileLog.e("tmessages", e);
                                    }
                                    if (error == null) {
                                        if (actionBarLayout != null) {
                                            TLRPC.Updates updates = (TLRPC.Updates) response;
                                            if (!updates.chats.isEmpty()) {
                                                TLRPC.Chat chat = updates.chats.get(0);
                                                chat.left = false;
                                                chat.kicked = false;
                                                MessagesController.getInstance().putUsers(updates.users, false);
                                                MessagesController.getInstance().putChats(updates.chats, false);
                                                Bundle args = new Bundle();
                                                args.putInt("chat_id", chat.id);
                                                if (mainFragmentsStack.isEmpty() || MessagesController.checkCanOpenChat(args, mainFragmentsStack.get(mainFragmentsStack.size() - 1))) {
                                                    ChatActivity fragment = new ChatActivity(args);
                                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                                                    actionBarLayout.presentFragment(fragment, false, true, true);
                                                }
                                            }
                                        }
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(LaunchActivity.this);
                                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                        if (error.text.startsWith("FLOOD_WAIT")) {
                                            builder.setMessage(LocaleController.getString("FloodWait", R.string.FloodWait));
                                        } else if (error.text.equals("USERS_TOO_MUCH")) {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorFull", R.string.JoinToGroupErrorFull));
                                        } else {
                                            builder.setMessage(LocaleController.getString("JoinToGroupErrorNotExist", R.string.JoinToGroupErrorNotExist));
                                        }
                                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                                        showAlertDialog(builder);
                                    }
                                }
                            }
                        });
                    }
                }, ConnectionsManager.RequestFlagFailOnServerErrors);
            }
        } else if (sticker != null) {
            if (!mainFragmentsStack.isEmpty()) {
                TLRPC.TL_inputStickerSetShortName stickerset = new TLRPC.TL_inputStickerSetShortName();
                stickerset.short_name = sticker;
                mainFragmentsStack.get(mainFragmentsStack.size() - 1).showDialog(new StickersAlert(LaunchActivity.this, stickerset, null, null));
            }
            return;
        } else if (message != null) {
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            DialogsActivity fragment = new DialogsActivity(args);
            fragment.setDelegate(new DialogsActivity.DialogsActivityDelegate() {
                @Override
                public void didSelectDialog(DialogsActivity fragment, long did, boolean param) {
                    Bundle args = new Bundle();
                    args.putBoolean("scrollToTopOnResume", true);
                    args.putBoolean("hasUrl", hasUrl);
                    int lower_part = (int) did;
                    int high_id = (int) (did >> 32);
                    if (lower_part != 0) {
                        if (high_id == 1) {
                            args.putInt("chat_id", lower_part);
                        } else {
                            if (lower_part > 0) {
                                args.putInt("user_id", lower_part);
                            } else if (lower_part < 0) {
                                args.putInt("chat_id", -lower_part);
                            }
                        }
                    } else {
                        args.putInt("enc_id", high_id);
                    }
                    if (MessagesController.checkCanOpenChat(args, fragment)) {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
                        DraftQuery.saveDraft(did, message, null, null, true);
                        actionBarLayout.presentFragment(new ChatActivity(args), true, false, true);
                    }
                }
            });
            presentFragment(fragment, false, true);
        }

        if (requestId != 0) {
            final int reqId = requestId;
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, LocaleController.getString("Cancel", R.string.Cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ConnectionsManager.getInstance().cancelRequest(reqId, true);
                    try {
                        dialog.dismiss();
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            });
            progressDialog.show();
        }
    }

    public AlertDialog showAlertDialog(AlertDialog.Builder builder) {
        try {
            if (visibleDialog != null) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        try {
            visibleDialog = builder.show();
            visibleDialog.setCanceledOnTouchOutside(true);
            visibleDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    visibleDialog = null;
                }
            });
            return visibleDialog;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent, true, false, false);
    }

    @Override
    public void didSelectDialog(DialogsActivity dialogsFragment, long dialog_id, boolean param) {
        if (dialog_id != 0) {
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);

            Bundle args = new Bundle();
            args.putBoolean("scrollToTopOnResume", true);
            if (!AndroidUtilities.isTablet()) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.closeChats);
            }
            if (lower_part != 0) {
                if (high_id == 1) {
                    args.putInt("chat_id", lower_part);
                } else {
                    if (lower_part > 0) {
                        args.putInt("user_id", lower_part);
                    } else if (lower_part < 0) {
                        args.putInt("chat_id", -lower_part);
                    }
                }
            } else {
                args.putInt("enc_id", high_id);
            }
            if (!MessagesController.checkCanOpenChat(args, dialogsFragment)) {
                return;
            }
            ChatActivity fragment = new ChatActivity(args);

            if (videoPath != null) {
                if (android.os.Build.VERSION.SDK_INT >= 16) {
                    if (AndroidUtilities.isTablet()) {
                        actionBarLayout.presentFragment(fragment, false, true, true);
                    } else {
                        actionBarLayout.addFragmentToStack(fragment, actionBarLayout.fragmentsStack.size() - 1);
                    }

                    if (!fragment.openVideoEditor(videoPath, dialogsFragment != null, false) && dialogsFragment != null) {
                        if (!AndroidUtilities.isTablet()) {
                            dialogsFragment.finishFragment(true);
                        }
                    }
                } else {
                    actionBarLayout.presentFragment(fragment, dialogsFragment != null, dialogsFragment == null, true);
                    SendMessagesHelper.prepareSendingVideo(videoPath, 0, 0, 0, 0, null, dialog_id, null);
                }
            } else {
                actionBarLayout.presentFragment(fragment, dialogsFragment != null, dialogsFragment == null, true);

                if (photoPathsArray != null) {
                    ArrayList<String> captions = null;
                    if (sendingText != null && photoPathsArray.size() == 1) {
                        captions = new ArrayList<>();
                        captions.add(sendingText);
                        sendingText = null;
                    }
                    SendMessagesHelper.prepareSendingPhotos(null, photoPathsArray, dialog_id, null, captions);
                }

                if (sendingText != null) {
                    SendMessagesHelper.prepareSendingText(sendingText, dialog_id);
                }

                if (documentsPathsArray != null || documentsUrisArray != null) {
                    SendMessagesHelper.prepareSendingDocuments(documentsPathsArray, documentsOriginalPathsArray, documentsUrisArray, documentsMimeType, dialog_id, null);
                }
                if (contactsToSend != null && !contactsToSend.isEmpty()) {
                    for (TLRPC.User user : contactsToSend) {
                        SendMessagesHelper.getInstance().sendMessage(user, dialog_id, null, null, null);
                    }
                }
            }

            photoPathsArray = null;
            videoPath = null;
            sendingText = null;
            documentsPathsArray = null;
            documentsOriginalPathsArray = null;
            contactsToSend = null;
        }
    }

    private void onFinish() {
        if (finished) {
            return;
        }
        finished = true;
        if (lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(lockRunnable);
            lockRunnable = null;
        }
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.appDidLogout);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.mainUserInfoChanged);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeOtherAppActivities);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.didUpdatedConnectionState);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.needShowAlert);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.wasUnableToFindCurrentLocation);
    }

    public void presentFragment(BaseFragment fragment) {
        actionBarLayout.presentFragment(fragment);
    }

    public boolean presentFragment(final BaseFragment fragment, final boolean removeLast, boolean forceWithoutAnimation) {
        return actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true);
    }

    public void needLayout() {
        if (AndroidUtilities.isTablet()) {
            RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) layersActionBarLayout.getLayoutParams();
            relativeLayoutParams.leftMargin = (AndroidUtilities.displaySize.x - relativeLayoutParams.width) / 2;
            int y = (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
            relativeLayoutParams.topMargin = y + (AndroidUtilities.displaySize.y - relativeLayoutParams.height - y) / 2;
            layersActionBarLayout.setLayoutParams(relativeLayoutParams);


            if (!AndroidUtilities.isSmallTablet() || getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                tabletFullSize = false;
                int leftWidth = AndroidUtilities.displaySize.x / 100 * 35;
                if (leftWidth < AndroidUtilities.dp(320)) {
                    leftWidth = AndroidUtilities.dp(320);
                }

                relativeLayoutParams = (RelativeLayout.LayoutParams) actionBarLayout.getLayoutParams();
                relativeLayoutParams.width = leftWidth;
                relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
                actionBarLayout.setLayoutParams(relativeLayoutParams);

                relativeLayoutParams = (RelativeLayout.LayoutParams) shadowTabletSide.getLayoutParams();
                relativeLayoutParams.leftMargin = leftWidth;
                shadowTabletSide.setLayoutParams(relativeLayoutParams);

                relativeLayoutParams = (RelativeLayout.LayoutParams) rightActionBarLayout.getLayoutParams();
                relativeLayoutParams.width = AndroidUtilities.displaySize.x - leftWidth;
                relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
                relativeLayoutParams.leftMargin = leftWidth;
                rightActionBarLayout.setLayoutParams(relativeLayoutParams);

                if (AndroidUtilities.isSmallTablet() && actionBarLayout.fragmentsStack.size() >= 2) {
                    for (int a = 1; a < actionBarLayout.fragmentsStack.size(); a++) {
                        BaseFragment chatFragment = actionBarLayout.fragmentsStack.get(a);
                        chatFragment.onPause();
                        actionBarLayout.fragmentsStack.remove(a);
                        rightActionBarLayout.fragmentsStack.add(chatFragment);
                        a--;
                    }
                    if (passcodeView.getVisibility() != View.VISIBLE) {
                        actionBarLayout.showLastFragment();
                        rightActionBarLayout.showLastFragment();
                    }
                }

                rightActionBarLayout.setVisibility(rightActionBarLayout.fragmentsStack.isEmpty() ? View.GONE : View.VISIBLE);
                backgroundTablet.setVisibility(rightActionBarLayout.fragmentsStack.isEmpty() ? View.VISIBLE : View.GONE);
                shadowTabletSide.setVisibility(!actionBarLayout.fragmentsStack.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                tabletFullSize = true;

                relativeLayoutParams = (RelativeLayout.LayoutParams) actionBarLayout.getLayoutParams();
                relativeLayoutParams.width = LayoutHelper.MATCH_PARENT;
                relativeLayoutParams.height = LayoutHelper.MATCH_PARENT;
                actionBarLayout.setLayoutParams(relativeLayoutParams);

                shadowTabletSide.setVisibility(View.GONE);
                rightActionBarLayout.setVisibility(View.GONE);
                backgroundTablet.setVisibility(!actionBarLayout.fragmentsStack.isEmpty() ? View.GONE : View.VISIBLE);

                if (!rightActionBarLayout.fragmentsStack.isEmpty()) {
                    for (int a = 0; a < rightActionBarLayout.fragmentsStack.size(); a++) {
                        BaseFragment chatFragment = rightActionBarLayout.fragmentsStack.get(a);
                        chatFragment.onPause();
                        rightActionBarLayout.fragmentsStack.remove(a);
                        actionBarLayout.fragmentsStack.add(chatFragment);
                        a--;
                    }
                    if (passcodeView.getVisibility() != View.VISIBLE) {
                        actionBarLayout.showLastFragment();
                    }
                }
            }
        }
    }

    public void fixLayout() {
        if (!AndroidUtilities.isTablet()) {
            return;
        }
        if (actionBarLayout == null) {
            return;
        }
        actionBarLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        needLayout();
                    }
                });
                if (actionBarLayout != null) {
                    if (Build.VERSION.SDK_INT < 16) {
                        actionBarLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    } else {
                        actionBarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (UserConfig.passcodeHash.length() != 0 && UserConfig.lastPauseTime != 0) {
            UserConfig.lastPauseTime = 0;
            UserConfig.saveConfig(false);
        }
        super.onActivityResult(requestCode, resultCode, data);
        if (actionBarLayout.fragmentsStack.size() != 0) {
            BaseFragment fragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
            fragment.onActivityResultFragment(requestCode, resultCode, data);
        }
        if (AndroidUtilities.isTablet()) {
            if (rightActionBarLayout.fragmentsStack.size() != 0) {
                BaseFragment fragment = rightActionBarLayout.fragmentsStack.get(rightActionBarLayout.fragmentsStack.size() - 1);
                fragment.onActivityResultFragment(requestCode, resultCode, data);
            }
            if (layersActionBarLayout.fragmentsStack.size() != 0) {
                BaseFragment fragment = layersActionBarLayout.fragmentsStack.get(layersActionBarLayout.fragmentsStack.size() - 1);
                fragment.onActivityResultFragment(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 3 || requestCode == 4 || requestCode == 5) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == 4) {
                    ImageLoader.getInstance().checkMediaPaths();
                } else if (requestCode == 5) {
                    ContactsController.getInstance().readContacts();
                }
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            if (requestCode == 3) {
                builder.setMessage(LocaleController.getString("PermissionNoAudio", R.string.PermissionNoAudio));
            } else if (requestCode == 4) {
                builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
            } else if (requestCode == 5) {
                builder.setMessage(LocaleController.getString("PermissionContacts", R.string.PermissionContacts));
            }
            builder.setNegativeButton(LocaleController.getString("PermissionOpenSettings", R.string.PermissionOpenSettings), new DialogInterface.OnClickListener() {
                @TargetApi(Build.VERSION_CODES.GINGERBREAD)
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + ApplicationLoader.applicationContext.getPackageName()));
                        startActivity(intent);
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }
            });
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            builder.show();
            return;
        } else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                NotificationCenter.getInstance().postNotificationName(NotificationCenter.locationPermissionGranted);
            }
        }
        if (actionBarLayout.fragmentsStack.size() != 0) {
            BaseFragment fragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
            fragment.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
        }
        if (AndroidUtilities.isTablet()) {
            if (rightActionBarLayout.fragmentsStack.size() != 0) {
                BaseFragment fragment = rightActionBarLayout.fragmentsStack.get(rightActionBarLayout.fragmentsStack.size() - 1);
                fragment.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
            }
            if (layersActionBarLayout.fragmentsStack.size() != 0) {
                BaseFragment fragment = layersActionBarLayout.fragmentsStack.get(layersActionBarLayout.fragmentsStack.size() - 1);
                fragment.onRequestPermissionsResultFragment(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApplicationLoader.mainInterfacePaused = true;
        onPasscodePause();
        actionBarLayout.onPause();
        if (AndroidUtilities.isTablet()) {
            rightActionBarLayout.onPause();
            layersActionBarLayout.onPause();
        }
        if (passcodeView != null) {
            passcodeView.onPause();
        }
        ConnectionsManager.getInstance().setAppPaused(true, false);
        AndroidUtilities.unregisterUpdates();
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().onPause();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Browser.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Browser.unbindCustomTabsService(this);
    }

    @Override
    protected void onDestroy() {
        PhotoViewer.getInstance().destroyPhotoViewer();
        SecretPhotoViewer.getInstance().destroyPhotoViewer();
        StickerPreviewViewer.getInstance().destroy();
        try {
            if (visibleDialog != null) {
                visibleDialog.dismiss();
                visibleDialog = null;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        try {
            if (onGlobalLayoutListener != null) {
                final View view = getWindow().getDecorView().getRootView();
                if (Build.VERSION.SDK_INT < 16) {
                    view.getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
                } else {
                    view.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        super.onDestroy();
        onFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ApplicationLoader.mainInterfacePaused = false;
        onPasscodeResume();
        if (passcodeView.getVisibility() != View.VISIBLE) {
            actionBarLayout.onResume();
            if (AndroidUtilities.isTablet()) {
                rightActionBarLayout.onResume();
                layersActionBarLayout.onResume();
            }
        } else {
            passcodeView.onResume();
        }
        AndroidUtilities.checkForCrashes(this);
        AndroidUtilities.checkForUpdates(this);
        ConnectionsManager.getInstance().setAppPaused(false, false);
        updateCurrentConnectionState();
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().onResume();
        }

        if(showCompleteDialog)
        {
            showCompleteDialog=false;
            /*new AlertDialog.Builder(LaunchActivity.this)
            .setTitle("iTel")
            .setMessage("با سپاس فراوان از شما بابت مشاهده ویدئوی حمایتی")
            .setNeutralButton("باشه", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            })
            .show();*/
        }
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        AndroidUtilities.checkDisplaySize();
        super.onConfigurationChanged(newConfig);
        fixLayout();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.appDidLogout) {
            if (drawerLayoutAdapter != null) {
                drawerLayoutAdapter.notifyDataSetChanged();
            }
            for (BaseFragment fragment : actionBarLayout.fragmentsStack) {
                fragment.onFragmentDestroy();
            }
            actionBarLayout.fragmentsStack.clear();
            if (AndroidUtilities.isTablet()) {
                for (BaseFragment fragment : layersActionBarLayout.fragmentsStack) {
                    fragment.onFragmentDestroy();
                }
                layersActionBarLayout.fragmentsStack.clear();
                for (BaseFragment fragment : rightActionBarLayout.fragmentsStack) {
                    fragment.onFragmentDestroy();
                }
                rightActionBarLayout.fragmentsStack.clear();
            }
            if (BuildConfig.IsDownloadManagerEnable) {

                Intent intent2 = new Intent(this, org.telegram.ui.Telehgram.intro.MainActivity.class);
                startActivity(intent2);
                onFinish();
                finish();
            } else {
                Intent intent2 = new Intent(this, IntroActivity.class);
                startActivity(intent2);
                onFinish();
                finish();
            }

        } else if (id == NotificationCenter.closeOtherAppActivities) {
            if (args[0] != this) {
                onFinish();
                finish();
            }
        } else if (id == NotificationCenter.didUpdatedConnectionState) {
            int state = ConnectionsManager.getInstance().getConnectionState();
            if (currentConnectionState != state) {
                FileLog.d("tmessages", "switch to state " + state);
                currentConnectionState = state;
                updateCurrentConnectionState();
            }
        } else if (id == NotificationCenter.mainUserInfoChanged) {
            drawerLayoutAdapter.notifyDataSetChanged();
        } else if (id == NotificationCenter.screenStateChanged) {
            if (!ApplicationLoader.mainInterfacePaused) {
                if (!ApplicationLoader.isScreenOn) {
                    onPasscodePause();
                } else {
                    onPasscodeResume();
                }
            }
        } else if (id == NotificationCenter.needShowAlert) {
            final Integer reason = (Integer) args[0];
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            if (reason != 2) {
                builder.setNegativeButton(LocaleController.getString("MoreInfo", R.string.MoreInfo), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!mainFragmentsStack.isEmpty()) {
                            MessagesController.openByUserName("spambot", mainFragmentsStack.get(mainFragmentsStack.size() - 1), 1);
                        }
                    }
                });
            }
            if (reason == 0) {
                builder.setMessage(LocaleController.getString("NobodyLikesSpam1", R.string.NobodyLikesSpam1));
            } else if (reason == 1) {
                builder.setMessage(LocaleController.getString("NobodyLikesSpam2", R.string.NobodyLikesSpam2));
            } else if (reason == 2) {
                builder.setMessage((String) args[1]);
            }
            if (!mainFragmentsStack.isEmpty()) {
                mainFragmentsStack.get(mainFragmentsStack.size() - 1).showDialog(builder.create());
            }
        } else if (id == NotificationCenter.wasUnableToFindCurrentLocation) {
            final HashMap<String, MessageObject> waitingForLocation = (HashMap<String, MessageObject>) args[0];
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
            builder.setNegativeButton(LocaleController.getString("ShareYouLocationUnableManually", R.string.ShareYouLocationUnableManually), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (mainFragmentsStack.isEmpty()) {
                        return;
                    }
                    BaseFragment lastFragment = mainFragmentsStack.get(mainFragmentsStack.size() - 1);
                    if (!AndroidUtilities.isGoogleMapsInstalled(lastFragment)) {
                        return;
                    }
                    LocationActivity fragment = new LocationActivity();
                    fragment.setDelegate(new LocationActivity.LocationActivityDelegate() {
                        @Override
                        public void didSelectLocation(TLRPC.MessageMedia location) {
                            for (HashMap.Entry<String, MessageObject> entry : waitingForLocation.entrySet()) {
                                MessageObject messageObject = entry.getValue();
                                SendMessagesHelper.getInstance().sendMessage(location, messageObject.getDialogId(), messageObject, null, null);
                            }
                        }
                    });
                    presentFragment(fragment);
                }
            });
            builder.setMessage(LocaleController.getString("ShareYouLocationUnable", R.string.ShareYouLocationUnable));
            if (!mainFragmentsStack.isEmpty()) {
                mainFragmentsStack.get(mainFragmentsStack.size() - 1).showDialog(builder.create());
            }
        }
    }

    private void onPasscodePause() {
        if (lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(lockRunnable);
            lockRunnable = null;
        }
        if (UserConfig.passcodeHash.length() != 0) {
            UserConfig.lastPauseTime = ConnectionsManager.getInstance().getCurrentTime();
            lockRunnable = new Runnable() {
                @Override
                public void run() {
                    if (lockRunnable == this) {
                        if (AndroidUtilities.needShowPasscode(true)) {
                            FileLog.e("tmessages", "lock app");
                            showPasscodeActivity();
                        } else {
                            FileLog.e("tmessages", "didn't pass lock check");
                        }
                        lockRunnable = null;
                    }
                }
            };
            if (UserConfig.appLocked) {
                AndroidUtilities.runOnUIThread(lockRunnable, 1000);
            } else if (UserConfig.autoLockIn != 0) {
                AndroidUtilities.runOnUIThread(lockRunnable, (long) UserConfig.autoLockIn * 1000 + 1000);
            }
        } else {
            UserConfig.lastPauseTime = 0;
        }
        UserConfig.saveConfig(false);
    }

    private void onPasscodeResume() {
        if (lockRunnable != null) {
            AndroidUtilities.cancelRunOnUIThread(lockRunnable);
            lockRunnable = null;
        }
        if (AndroidUtilities.needShowPasscode(true)) {
            showPasscodeActivity();
        }
        if (UserConfig.lastPauseTime != 0) {
            UserConfig.lastPauseTime = 0;
            UserConfig.saveConfig(false);
        }
    }

    private void updateCurrentConnectionState() {
        String text = null;
        if (currentConnectionState == ConnectionsManager.ConnectionStateWaitingForNetwork) {
            text = LocaleController.getString("WaitingForNetwork", R.string.WaitingForNetwork);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateConnecting) {
            text = LocaleController.getString("Connecting", R.string.Connecting);
        } else if (currentConnectionState == ConnectionsManager.ConnectionStateUpdating) {
            text = LocaleController.getString("Updating", R.string.Updating);
        }
        actionBarLayout.setTitleOverlayText(text);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            super.onSaveInstanceState(outState);
            BaseFragment lastFragment = null;
            if (AndroidUtilities.isTablet()) {
                if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = layersActionBarLayout.fragmentsStack.get(layersActionBarLayout.fragmentsStack.size() - 1);
                } else if (!rightActionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = rightActionBarLayout.fragmentsStack.get(rightActionBarLayout.fragmentsStack.size() - 1);
                } else if (!actionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
                }
            } else {
                if (!actionBarLayout.fragmentsStack.isEmpty()) {
                    lastFragment = actionBarLayout.fragmentsStack.get(actionBarLayout.fragmentsStack.size() - 1);
                }
            }

            if (lastFragment != null) {
                Bundle args = lastFragment.getArguments();
                if (lastFragment instanceof ChatActivity && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "chat");
                } else if (lastFragment instanceof SettingsActivity) {
                    outState.putString("fragment", "settings");
                } else if (lastFragment instanceof GroupCreateFinalActivity && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "group");
                } else if (lastFragment instanceof WallpapersActivity) {
                    outState.putString("fragment", "wallpapers");
                } else if (lastFragment instanceof ProfileActivity && ((ProfileActivity) lastFragment).isChat() && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "chat_profile");
                } else if (lastFragment instanceof ChannelCreateActivity && args != null && args.getInt("step") == 0) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "channel");
                } else if (lastFragment instanceof ChannelEditActivity && args != null) {
                    outState.putBundle("args", args);
                    outState.putString("fragment", "edit");
                }
                lastFragment.saveSelfArgs(outState);
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    @Override
    public void onBackPressed() {
        if (passcodeView.getVisibility() == View.VISIBLE) {
            finish();
            return;
        }
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
        } else if (drawerLayoutContainer.isDrawerOpened()) {
            drawerLayoutContainer.closeDrawer(false);
        } else if (AndroidUtilities.isTablet()) {
            if (layersActionBarLayout.getVisibility() == View.VISIBLE) {
                layersActionBarLayout.onBackPressed();
            } else {
                boolean cancel = false;
                if (rightActionBarLayout.getVisibility() == View.VISIBLE && !rightActionBarLayout.fragmentsStack.isEmpty()) {
                    BaseFragment lastFragment = rightActionBarLayout.fragmentsStack.get(rightActionBarLayout.fragmentsStack.size() - 1);
                    cancel = !lastFragment.onBackPressed();
                }
                if (!cancel) {
                    actionBarLayout.onBackPressed();
                }
            }
        } else {
            actionBarLayout.onBackPressed();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        actionBarLayout.onLowMemory();
        if (AndroidUtilities.isTablet()) {
            rightActionBarLayout.onLowMemory();
            layersActionBarLayout.onLowMemory();
        }
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        if (Build.VERSION.SDK_INT >= 23 && mode.getType() == ActionMode.TYPE_FLOATING) {
            return;
        }
        actionBarLayout.onActionModeStarted(mode);
        if (AndroidUtilities.isTablet()) {
            rightActionBarLayout.onActionModeStarted(mode);
            layersActionBarLayout.onActionModeStarted(mode);
        }
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        if (Build.VERSION.SDK_INT >= 23 && mode.getType() == ActionMode.TYPE_FLOATING) {
            return;
        }
        actionBarLayout.onActionModeFinished(mode);
        if (AndroidUtilities.isTablet()) {
            rightActionBarLayout.onActionModeFinished(mode);
            layersActionBarLayout.onActionModeFinished(mode);
        }
    }

    @Override
    public boolean onPreIme() {
        if (PhotoViewer.getInstance().isVisible()) {
            PhotoViewer.getInstance().closePhoto(true, false);
            return true;
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU && !UserConfig.isWaitingForPasscodeEnter) {
            if (AndroidUtilities.isTablet()) {
                if (layersActionBarLayout.getVisibility() == View.VISIBLE && !layersActionBarLayout.fragmentsStack.isEmpty()) {
                    layersActionBarLayout.onKeyUp(keyCode, event);
                } else if (rightActionBarLayout.getVisibility() == View.VISIBLE && !rightActionBarLayout.fragmentsStack.isEmpty()) {
                    rightActionBarLayout.onKeyUp(keyCode, event);
                } else {
                    actionBarLayout.onKeyUp(keyCode, event);
                }
            } else {
                if (actionBarLayout.fragmentsStack.size() == 1) {
                    if (!drawerLayoutContainer.isDrawerOpened()) {
                        if (getCurrentFocus() != null) {
                            AndroidUtilities.hideKeyboard(getCurrentFocus());
                        }
                        drawerLayoutContainer.openDrawer(false);
                    } else {
                        drawerLayoutContainer.closeDrawer(false);
                    }
                } else {
                    actionBarLayout.onKeyUp(keyCode, event);
                }
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean needPresentFragment(BaseFragment fragment, boolean removeLast, boolean forceWithoutAnimation, ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            drawerLayoutContainer.setAllowOpenDrawer(!(fragment instanceof LoginActivity || fragment instanceof CountrySelectActivity) && layersActionBarLayout.getVisibility() != View.VISIBLE, true);
            if (fragment instanceof DialogsActivity) {
                DialogsActivity dialogsActivity = (DialogsActivity) fragment;
                if (dialogsActivity.isMainDialogList() && layout != actionBarLayout) {
                    actionBarLayout.removeAllFragments();
                    actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, false);
                    layersActionBarLayout.removeAllFragments();
                    layersActionBarLayout.setVisibility(View.GONE);
                    drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    if (!tabletFullSize) {
                        shadowTabletSide.setVisibility(View.VISIBLE);
                        if (rightActionBarLayout.fragmentsStack.isEmpty()) {
                            backgroundTablet.setVisibility(View.VISIBLE);
                        }
                    }
                    return false;
                }
            }
            if (fragment instanceof ChatActivity) {
                if (!tabletFullSize && layout == rightActionBarLayout || tabletFullSize && layout == actionBarLayout) {
                    boolean result = !(tabletFullSize && layout == actionBarLayout && actionBarLayout.fragmentsStack.size() == 1);
                    if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                        for (int a = 0; a < layersActionBarLayout.fragmentsStack.size() - 1; a++) {
                            layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.fragmentsStack.get(0));
                            a--;
                        }
                        layersActionBarLayout.closeLastFragment(!forceWithoutAnimation);
                    }
                    if (!result) {
                        actionBarLayout.presentFragment(fragment, false, forceWithoutAnimation, false);
                    }
                    return result;
                } else if (!tabletFullSize && layout != rightActionBarLayout) {
                    rightActionBarLayout.setVisibility(View.VISIBLE);
                    backgroundTablet.setVisibility(View.GONE);
                    rightActionBarLayout.removeAllFragments();
                    rightActionBarLayout.presentFragment(fragment, removeLast, true, false);
                    if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                        for (int a = 0; a < layersActionBarLayout.fragmentsStack.size() - 1; a++) {
                            layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.fragmentsStack.get(0));
                            a--;
                        }
                        layersActionBarLayout.closeLastFragment(!forceWithoutAnimation);
                    }
                    return false;
                } else if (tabletFullSize && layout != actionBarLayout) {
                    actionBarLayout.presentFragment(fragment, actionBarLayout.fragmentsStack.size() > 1, forceWithoutAnimation, false);
                    if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                        for (int a = 0; a < layersActionBarLayout.fragmentsStack.size() - 1; a++) {
                            layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.fragmentsStack.get(0));
                            a--;
                        }
                        layersActionBarLayout.closeLastFragment(!forceWithoutAnimation);
                    }
                    return false;
                } else {
                    if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                        for (int a = 0; a < layersActionBarLayout.fragmentsStack.size() - 1; a++) {
                            layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.fragmentsStack.get(0));
                            a--;
                        }
                        layersActionBarLayout.closeLastFragment(!forceWithoutAnimation);
                    }
                    actionBarLayout.presentFragment(fragment, actionBarLayout.fragmentsStack.size() > 1, forceWithoutAnimation, false);
                    return false;
                }
            } else if (layout != layersActionBarLayout) {
                layersActionBarLayout.setVisibility(View.VISIBLE);
                drawerLayoutContainer.setAllowOpenDrawer(false, true);
                if (fragment instanceof LoginActivity) {
                    backgroundTablet.setVisibility(View.VISIBLE);
                    shadowTabletSide.setVisibility(View.GONE);
                    shadowTablet.setBackgroundColor(0x00000000);
                } else {
                    shadowTablet.setBackgroundColor(0x7F000000);
                }
                layersActionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, false);
                return false;
            }
            return true;
        } else {
            drawerLayoutContainer.setAllowOpenDrawer(!(fragment instanceof LoginActivity || fragment instanceof CountrySelectActivity), false);
            return true;
        }
    }

    @Override
    public boolean needAddFragmentToStack(BaseFragment fragment, ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            drawerLayoutContainer.setAllowOpenDrawer(!(fragment instanceof LoginActivity || fragment instanceof CountrySelectActivity) && layersActionBarLayout.getVisibility() != View.VISIBLE, true);
            if (fragment instanceof DialogsActivity) {
                DialogsActivity dialogsActivity = (DialogsActivity) fragment;
                if (dialogsActivity.isMainDialogList() && layout != actionBarLayout) {
                    actionBarLayout.removeAllFragments();
                    actionBarLayout.addFragmentToStack(fragment);
                    layersActionBarLayout.removeAllFragments();
                    layersActionBarLayout.setVisibility(View.GONE);
                    drawerLayoutContainer.setAllowOpenDrawer(true, false);
                    if (!tabletFullSize) {
                        shadowTabletSide.setVisibility(View.VISIBLE);
                        if (rightActionBarLayout.fragmentsStack.isEmpty()) {
                            backgroundTablet.setVisibility(View.VISIBLE);
                        }
                    }
                    return false;
                }
            } else if (fragment instanceof ChatActivity) {
                if (!tabletFullSize && layout != rightActionBarLayout) {
                    rightActionBarLayout.setVisibility(View.VISIBLE);
                    backgroundTablet.setVisibility(View.GONE);
                    rightActionBarLayout.removeAllFragments();
                    rightActionBarLayout.addFragmentToStack(fragment);
                    if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                        for (int a = 0; a < layersActionBarLayout.fragmentsStack.size() - 1; a++) {
                            layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.fragmentsStack.get(0));
                            a--;
                        }
                        layersActionBarLayout.closeLastFragment(true);
                    }
                    return false;
                } else if (tabletFullSize && layout != actionBarLayout) {
                    actionBarLayout.addFragmentToStack(fragment);
                    if (!layersActionBarLayout.fragmentsStack.isEmpty()) {
                        for (int a = 0; a < layersActionBarLayout.fragmentsStack.size() - 1; a++) {
                            layersActionBarLayout.removeFragmentFromStack(layersActionBarLayout.fragmentsStack.get(0));
                            a--;
                        }
                        layersActionBarLayout.closeLastFragment(true);
                    }
                    return false;
                }
            } else if (layout != layersActionBarLayout) {
                layersActionBarLayout.setVisibility(View.VISIBLE);
                drawerLayoutContainer.setAllowOpenDrawer(false, true);
                if (fragment instanceof LoginActivity) {
                    backgroundTablet.setVisibility(View.VISIBLE);
                    shadowTabletSide.setVisibility(View.GONE);
                    shadowTablet.setBackgroundColor(0x00000000);
                } else {
                    shadowTablet.setBackgroundColor(0x7F000000);
                }
                layersActionBarLayout.addFragmentToStack(fragment);
                return false;
            }
            return true;
        } else {
            drawerLayoutContainer.setAllowOpenDrawer(!(fragment instanceof LoginActivity || fragment instanceof CountrySelectActivity), false);
            return true;
        }
    }

    @Override
    public boolean needCloseLastFragment(ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            if (layout == actionBarLayout && layout.fragmentsStack.size() <= 1) {
                onFinish();
                finish();
                return false;
            } else if (layout == rightActionBarLayout) {
                if (!tabletFullSize) {
                    backgroundTablet.setVisibility(View.VISIBLE);
                }
            } else if (layout == layersActionBarLayout && actionBarLayout.fragmentsStack.isEmpty() && layersActionBarLayout.fragmentsStack.size() == 1) {
                onFinish();
                finish();
                return false;
            }
        } else {
            if (layout.fragmentsStack.size() <= 1) {
                onFinish();
                finish();
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRebuildAllFragments(ActionBarLayout layout) {
        if (AndroidUtilities.isTablet()) {
            if (layout == layersActionBarLayout) {
                rightActionBarLayout.rebuildAllFragmentViews(true);
                rightActionBarLayout.showLastFragment();
                actionBarLayout.rebuildAllFragmentViews(true);
                actionBarLayout.showLastFragment();
            }
        }
        drawerLayoutAdapter.notifyDataSetChanged();
    }

    private class VcardData {
        String name;
        ArrayList<String> phones = new ArrayList<>();
    }
}
