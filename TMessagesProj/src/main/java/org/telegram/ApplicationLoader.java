/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package org.telegram.messenger;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.multidex.MultiDexApplication;
import android.util.Base64;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.*;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.analytics.ExceptionReporter;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONObject;
import org.telegram.SQLite.DatabaseHandler;
import org.telegram.chatheadmsg.ChatHeadService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Analytics.AnalyticsExceptionParser;
import org.telegram.ui.Components.ForegroundDetector;
import org.telegram.ui.LaunchActivity;

import java.io.File;
import java.io.RandomAccessFile;

import co.ronash.pushe.Pushe;

public class ApplicationLoader extends MultiDexApplication {

    private static final Object sync = new Object();
    public static int i = 0;
    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;
    public static DatabaseHandler databaseHandler;
    public static boolean KEEP_ORIGINAL_FILENAME;
    public static boolean SHOW_ANDROID_EMOJI;
    public static boolean USE_DEVICE_FONT;
    public static volatile boolean isScreenOn = false;
    public static volatile boolean mainInterfacePaused = true;
    public static String trans = "0";
    private static Drawable cachedWallpaper;
    private static int selectedColor;
    private static boolean isCustomTheme;
    private static int serviceMessageColor;
    private static int serviceSelectedMessageColor;
    private static volatile boolean applicationInited = false;
    private static ApplicationLoader mInstaceApplication;

    private static GoogleAnalytics sAnalytics;
    private Tracker mTracker;



    public static boolean isCustomTheme() {
        return isCustomTheme;
    }

    public static int getSelectedColor() {
        return selectedColor;
    }

    public static synchronized ApplicationLoader getInstance() {
        if (mInstaceApplication == null) {
            mInstaceApplication = new ApplicationLoader();
        }

        return mInstaceApplication;
    }

    public static void reloadWallpaper() {
        cachedWallpaper = null;
        serviceMessageColor = 0;
        ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE).edit().remove("serviceMessageColor").commit();
        loadWallpaper();
    }

    private static void calcBackgroundColor() {
        int result[] = AndroidUtilities.calcDrawableColor(cachedWallpaper);
        serviceMessageColor = result[0];
        serviceSelectedMessageColor = result[1];
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        preferences.edit().putInt("serviceMessageColor", serviceMessageColor).putInt("serviceSelectedMessageColor", serviceSelectedMessageColor).commit();
    }

    public static int getServiceMessageColor() {
        return serviceMessageColor;
    }

    public static int getServiceSelectedMessageColor() {
        return serviceSelectedMessageColor;
    }

    public static void loadWallpaper() {
        if (cachedWallpaper != null) {
            return;
        }
        Utilities.searchQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                synchronized (sync) {
                    int selectedColor = 0;
                    try {
                        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
                        int selectedBackground = preferences.getInt("selectedBackground", 1000001);
                        selectedColor = preferences.getInt("selectedColor", 0);
                        serviceMessageColor = preferences.getInt("serviceMessageColor", 0);
                        serviceSelectedMessageColor = preferences.getInt("serviceSelectedMessageColor", 0);
                        if (selectedColor == 0) {
                            if (selectedBackground == 1000001) {
                                cachedWallpaper = applicationContext.getResources().getDrawable(R.drawable.background_hd);
                                isCustomTheme = false;
                            } else {
                                File toFile = new File(getFilesDirFixed(), "wallpaper.jpg");
                                if (toFile.exists()) {
                                    cachedWallpaper = Drawable.createFromPath(toFile.getAbsolutePath());
                                    isCustomTheme = true;
                                } else {
                                    cachedWallpaper = applicationContext.getResources().getDrawable(R.drawable.background_hd);
                                    isCustomTheme = false;
                                }
                            }
                        }
                    } catch (Throwable throwable) {
                        //ignore
                    }
                    if (cachedWallpaper == null) {
                        if (selectedColor == 0) {
                            selectedColor = -2693905;
                        }
                        cachedWallpaper = new ColorDrawable(selectedColor);
                    }
                    if (serviceMessageColor == 0) {
                        calcBackgroundColor();
                    }
                }
            }
        });
    }

    public static Drawable getCachedWallpaper() {
        synchronized (sync) {
            return cachedWallpaper;
        }
    }

    private static void convertConfig() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("dataconfig", Context.MODE_PRIVATE);
        if (preferences.contains("currentDatacenterId")) {
            SerializedData buffer = new SerializedData(32 * 1024);
            buffer.writeInt32(2);
            buffer.writeBool(preferences.getInt("datacenterSetId", 0) != 0);
            buffer.writeBool(true);
            buffer.writeInt32(preferences.getInt("currentDatacenterId", 0));
            buffer.writeInt32(preferences.getInt("timeDifference", 0));
            buffer.writeInt32(preferences.getInt("lastDcUpdateTime", 0));
            buffer.writeInt64(preferences.getLong("pushSessionId", 0));
            buffer.writeBool(false);
            buffer.writeInt32(0);
            try {
                String datacentersString = preferences.getString("datacenters", null);
                if (datacentersString != null) {
                    byte[] datacentersBytes = Base64.decode(datacentersString, Base64.DEFAULT);
                    if (datacentersBytes != null) {
                        SerializedData data = new SerializedData(datacentersBytes);
                        buffer.writeInt32(data.readInt32(false));
                        buffer.writeBytes(datacentersBytes, 4, datacentersBytes.length - 4);
                        data.cleanup();
                    }
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }

            try {
                File file = new File(getFilesDirFixed(), "tgnet.dat");
                RandomAccessFile fileOutputStream = new RandomAccessFile(file, "rws");
                byte[] bytes = buffer.toByteArray();
                fileOutputStream.writeInt(Integer.reverseBytes(bytes.length));
                fileOutputStream.write(bytes);
                fileOutputStream.close();
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            buffer.cleanup();
            preferences.edit().clear().commit();
        }
    }

    public static File getFilesDirFixed() {
        for (int a = 0; a < 10; a++) {
            File path = ApplicationLoader.applicationContext.getFilesDir();
            if (path != null) {
                return path;
            }
        }
        try {
            ApplicationInfo info = applicationContext.getApplicationInfo();
            File path = new File(info.dataDir, "files");
            path.mkdirs();
            return path;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return new File("/data/data/ir.limon.itel/files");
    }

    public static void postInitApplication() {
        if (applicationInited) {
            return;
        }

        applicationInited = true;
        convertConfig();

        try {
            LocaleController.getInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            final BroadcastReceiver mReceiver = new ScreenReceiver();
            applicationContext.registerReceiver(mReceiver, filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            PowerManager pm = (PowerManager) ApplicationLoader.applicationContext.getSystemService(Context.POWER_SERVICE);
            isScreenOn = pm.isScreenOn();
            FileLog.e("tmessages", "screen state = " + isScreenOn);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        UserConfig.loadConfig();
        String deviceModel;
        String langCode = "fa";
        String appVersion;
        String systemVersion;
        String configPath = getFilesDirFixed().toString();

        try {
            langCode = "fa";
            //langCode = LocaleController.getLocaleStringIso639();
            deviceModel = Build.MANUFACTURER + Build.MODEL;
            PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
            appVersion = pInfo.versionName + " (" + pInfo.versionCode + ")";
            systemVersion = "SDK " + Build.VERSION.SDK_INT;
        } catch (Exception e) {
            langCode = "fa";
            deviceModel = "Android unknown";
            appVersion = "App version unknown";
            systemVersion = "SDK " + Build.VERSION.SDK_INT;
        }
        if (langCode.trim().length() == 0) {
            langCode = "fa";
        }
        if (deviceModel.trim().length() == 0) {
            deviceModel = "Android unknown";
        }
        if (appVersion.trim().length() == 0) {
            appVersion = "App version unknown";
        }
        if (systemVersion.trim().length() == 0) {
            systemVersion = "SDK Unknown";
        }

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("Notifications", Activity.MODE_PRIVATE);
        boolean enablePushConnection = preferences.getBoolean("pushConnection", true);

        MessagesController.getInstance();
        ConnectionsManager.getInstance().init(BuildVars.BUILD_VERSION, TLRPC.LAYER, BuildVars.APP_ID, deviceModel, systemVersion, appVersion, langCode, configPath, FileLog.getNetworkLogPath(), UserConfig.getClientUserId(), enablePushConnection);
        if (UserConfig.getCurrentUser() != null) {
            MessagesController.getInstance().putUser(UserConfig.getCurrentUser(), true);
            ConnectionsManager.getInstance().applyCountryPortNumber(UserConfig.getCurrentUser().phone);
            MessagesController.getInstance().getBlockedUsers(true);
            SendMessagesHelper.getInstance().checkUnsentMessages();
        }

        ApplicationLoader app = (ApplicationLoader) ApplicationLoader.applicationContext;
        app.initPlayServices();
        FileLog.e("tmessages", "app initied");

        ContactsController.getInstance().checkAppAccount();
        MediaController.getInstance();
    }

    public static void startPushService() {
        SharedPreferences preferences = applicationContext.getSharedPreferences("Notifications", MODE_PRIVATE);

        if (preferences.getBoolean("pushService", true)) {
            applicationContext.startService(new Intent(applicationContext, NotificationsService.class));
        } else {
            stopPushService();
        }
    }

    public static void stopPushService() {
        applicationContext.stopService(new Intent(applicationContext, NotificationsService.class));

        PendingIntent pintent = PendingIntent.getService(applicationContext, 0, new Intent(applicationContext, NotificationsService.class), 0);
        AlarmManager alarm = (AlarmManager) applicationContext.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pintent);
    }

    private void setupUncaughtException() {
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new ExceptionReporter(
                getDefaultTracker(),
                Thread.getDefaultUncaughtExceptionHandler(),
                this);

        if (uncaughtExceptionHandler instanceof ExceptionReporter) {
            ExceptionReporter exceptionReporter = (ExceptionReporter) uncaughtExceptionHandler;
            exceptionReporter.setExceptionParser(new AnalyticsExceptionParser());

            Thread.setDefaultUncaughtExceptionHandler(exceptionReporter);
        }
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            //TODO fix this
            mTracker = analytics.newTracker("UA-85194217-2");
            mTracker.enableExceptionReporting(true);
        }
        return mTracker;
    }

    /*public static void sendRegIdToBackend(final String token) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                UserConfig.pushString = token;
                UserConfig.registeredForPush = false;
                UserConfig.saveConfig(false);
                if (UserConfig.getClientUserId() != 0) {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            MessagesController.getInstance().registerForPush(token);
                        }
                    });
                }
            }
        });
    }*/

    public void initImageLoader() {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(getApplicationContext());
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        //config.writeDebugLogs(); // Remove for release app
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().init(config.build());
    }

    @Override
    public void onCreate() {

        super.onCreate();
        setupUncaughtException();
        mInstaceApplication = this;

        if (Build.VERSION.SDK_INT < 11) {
            java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
            java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
        }
        applicationContext = getApplicationContext();

        sAnalytics = GoogleAnalytics.getInstance(this);


        /*url = "http://in3taplus.ir/s/getcount.php";
        StringRequest strReq = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray jsonArrayTotal = new JSONArray(response);
                            JSONObject jsnobjectState = jsonArrayTotal.getJSONObject(0);
                            String count = jsnobjectState.getString("count");

                            if(Integer.valueOf(count) > 0){
                                Toast.makeText(getApplicationContext(), count + "maande", Toast.LENGTH_SHORT).show();
                                ServerSendCount();
                            }
                        } catch (Exception e) {
                            // TODO: handle exception
                        }

                    }// End onResponse
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                try {
                    // TODO MAP_ServerGetListPrice
                    params.put("telegramid", "samaanak");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return params;
            }

            @Override
            public Priority getPriority() {
                return Priority.HIGH;
            }

        };
        strReq.setRetryPolicy(new DefaultRetryPolicy(300000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);*/


        OneSignal.startInit(this).setNotificationReceivedHandler(new OneSignal.NotificationReceivedHandler() {

            @Override
            public void notificationReceived(OSNotification notification) {
                JSONObject data = notification.payload.additionalData;
                String customKey;
                String joinlink;
                String message;

                if (data != null) {
                    message = data.optString("message", null);
                    String cover = data.optString("cover", null);
                    String icon = data.optString("icon", null);
                    String title = data.optString("title", null);
                    String desc = data.optString("desc", null);
                    String setpackage = data.optString("setpackage", null);
                    String btntxt = data.optString("btntxt", null);
                    String link = data.optString("link", null);
                    if(message != null &&
                        cover != null &&
                        icon != null &&
                        title != null &&
                        desc != null &&
                        setpackage != null &&
                        btntxt != null &&
                        link != null
                            ){
                        if(Build.VERSION.SDK_INT >= 23) {
                            if (Settings.canDrawOverlays(applicationContext)) {
                                Intent intent = new Intent(applicationContext, ChatHeadService.class);

                                intent.putExtra("message", message);
                                intent.putExtra("cover", cover);
                                intent.putExtra("icon", icon);
                                intent.putExtra("title", title);
                                intent.putExtra("desc", desc);
                                intent.putExtra("setpackage", setpackage);
                                intent.putExtra("btntxt", btntxt);
                                intent.putExtra("link", link);

                                startService(intent);
                            }
                        }else{
                            Intent intent = new Intent(applicationContext, ChatHeadService.class);

                            intent.putExtra("message", message);
                            intent.putExtra("cover", cover);
                            intent.putExtra("icon", icon);
                            intent.putExtra("title", title);
                            intent.putExtra("desc", desc);
                            intent.putExtra("setpackage", setpackage);
                            intent.putExtra("btntxt", btntxt);
                            intent.putExtra("link", link);

                            startService(intent);
                        }
                    }
                }

                if (data != null) {
                    joinlink = data.optString("link", null);
                    if (joinlink != null) {
                        TLRPC.TL_messages_importChatInvite req = new TLRPC.TL_messages_importChatInvite();
                        req.hash = joinlink;
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
                                        if (error == null) {
                                            TLRPC.Updates updates = (TLRPC.Updates) response;
                                            if (!updates.chats.isEmpty()) {
                                                TLRPC.Chat chat = updates.chats.get(0);
                                                chat.left = false;
                                                chat.kicked = false;
                                                MessagesController.getInstance().putUsers(updates.users, false);
                                                MessagesController.getInstance().putChats(updates.chats, false);
                                            }
                                        }
                                    }
                                });
                            }
                        });
                    }
                }

                if (data != null) {
                    customKey = data.optString("channel", null);
                    if (customKey != null) {
                        Log.i("OneSignalExample", "customkey set with value: " + customKey);
                        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
                        req.username = customKey;
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
                }

                // The following can be used to open an Activity of your choice.

                // Intent intent = new Intent(getApplicationContext(), YourActivity.class);
                // intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
                // startActivity(intent);

                // Follow the instructions in the link below to prevent the launcher Activity from starting.
                // https://documentation.onesignal.com/docs/android-notification-customizations#changing-the-open-action-of-a-notification
            }
        }).init();

        initImageLoader();
        NativeLoader.initNativeLibs(ApplicationLoader.applicationContext);
        ConnectionsManager.native_setJava(Build.VERSION.SDK_INT == 14 || Build.VERSION.SDK_INT == 15);
        new ForegroundDetector(this);

        applicationHandler = new Handler(applicationContext.getMainLooper());
        databaseHandler = new DatabaseHandler(applicationContext);
        SharedPreferences plusPreferences = applicationContext.getSharedPreferences("plusconfig", 0);
        SHOW_ANDROID_EMOJI = plusPreferences.getBoolean("showAndroidEmoji", false);
        KEEP_ORIGINAL_FILENAME = plusPreferences.getBoolean("keepOriginalFilename", false);
        USE_DEVICE_FONT = plusPreferences.getBoolean("useDeviceFont", false);
        startPushService();
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        try {
            LocaleController.getInstance().onDeviceConfigurationChange(newConfig);
            AndroidUtilities.checkDisplaySize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPlayServices() {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (checkPlayServices()) {
                    if (UserConfig.pushString != null && UserConfig.pushString.length() != 0) {
                        FileLog.d("tmessages", "GCM regId = " + UserConfig.pushString);
                    } else {
                        FileLog.d("tmessages", "GCM Registration not found.");
                    }

                    //if (UserConfig.pushString == null || UserConfig.pushString.length() == 0) {
                    Intent intent = new Intent(applicationContext, GcmRegistrationIntentService.class);
                    startService(intent);
                    //} else {
                    //    FileLog.d("tmessages", "GCM regId = " + UserConfig.pushString);
                    //}
                } else {
                    FileLog.d("tmessages", "No valid Google Play Services APK found.");
                }
            }
        }, 1000);
    }

    /*private void initPlayServices() {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (checkPlayServices()) {
                    if (UserConfig.pushString != null && UserConfig.pushString.length() != 0) {
                        FileLog.d("tmessages", "GCM regId = " + UserConfig.pushString);
                    } else {
                        FileLog.d("tmessages", "GCM Registration not found.");
                    }
                    try {
                        if (!FirebaseApp.getApps(ApplicationLoader.applicationContext).isEmpty()) {
                            String token = FirebaseInstanceId.getInstance().getToken();
                            if (token != null) {
                                sendRegIdToBackend(token);
                            }
                        }
                    } catch (Throwable e) {
                        FileLog.e("tmessages", e);
                    }
                } else {
                    FileLog.d("tmessages", "No valid Google Play Services APK found.");
                }
            }
        }, 2000);
    }*/



    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    public static final String TAG = ApplicationLoader.class.getSimpleName();

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public com.android.volley.toolbox.ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new com.android.volley.toolbox.ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }






    private boolean checkPlayServices() {
        try {
            int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
            return resultCode == ConnectionResult.SUCCESS;
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return true;

        /*if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("tmessages", "This device is not supported.");
            }
            return false;
        }
        return true;*/
    }
}
