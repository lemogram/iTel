-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-ignorewarnings
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable,*Annotation*
-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**
-keep public class * extends Exception
-keep public class * extends android.app.Activity
-keep class com.woxthebox.draglistview.** { *; }
-keepclassmembers public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends com.google.**
-keep public class * extends android.app.IntentService
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.service.chooser.ChooserTargetService
-keep class com.android.vending.billing
-keepclassmembers class org.telegram.messenger.ApplicationLoader { *; }
-keepclassmembers class org.telegram.messenger.MediaController { *; }
-keepclassmembers class org.telegram.messenger.BuildVars { *; }
-keepclassmembers class org.telegram.messenger.AppStartReceiver { *; }
-keepclassmembers class org.telegram.messenger.Utilities { *; }
-keepclassmembers class org.telegram.messenger.NativeLoader { *; }
-keepclassmembers class org.telegram.messenger.AuthenticatorService { *; }
-keepclassmembers class org.telegram.messenger.AutoMessageHeardReceiver { *; }
-keepclassmembers class org.telegram.messenger.AutoMessageReplyReceiver { *; }
-keepclassmembers class org.telegram.messenger.ClearCacheService { *; }
-keepclassmembers class org.telegram.messenger.ContactsSyncAdapterService { *; }
-keepclassmembers class org.telegram.messenger.GcmInstanceIDListenerService { *; }
-keepclassmembers class org.telegram.messenger.GcmPushListenerService { *; }
-keepclassmembers class org.telegram.messenger.GcmRegistrationIntentService { *; }
-keepclassmembers class org.telegram.messenger.MediaController { *; }
-keepclassmembers class org.telegram.messenger.MusicPlayerReceiver { *; }
-keepclassmembers class org.telegram.messenger.MusicPlayerService { *; }
-keepclassmembers class org.telegram.messenger.NotificationRepeat { *; }
-keepclassmembers class org.telegram.messenger.NotificationsService { *; }
-keepclassmembers class org.telegram.messenger.SmsListener { *; }
-keepclassmembers class org.telegram.messenger.SpecialService { *; }
-keepclassmembers class org.telegram.messenger.TgChooserTargetService { *; }
-keepclassmembers class org.telegram.messenger.VideoEncodingService { *; }
-keepclassmembers class org.telegram.messenger.WearReplyReceiver { *; }
-keep class org.telegram.tgnet.NativeByteBuffer { *; }
-keep class org.telegram.tgnet.AbstractSerializedData { *; }
-keep class org.telegram.tgnet.ConnectionsManager { *; }
-keep interface org.telegram.tgnet.QuickAckDelegate { *; }
-keep interface org.telegram.tgnet.RequestDelegate { *; }
-keep interface org.telegram.tgnet.RequestDelegateInternal { *; }
-keepclassmembers class org.telegram.tgnet.SerializedData { *; }
-keepclassmembers class org.telegram.tgnet.TLClassStore { *; }
-keepclassmembers class org.telegram.tgnet.TLRPC { *; }
-keepclassmembers class org.telegram.tgnet.TLObject { *; }
-keepclassmembers class org.telegram.SQLite.DatabaseHandler { *; }
-keepclassmembers class org.telegram.SQLite.SQLiteCursor { *; }
-keepclassmembers class org.telegram.SQLite.SQLiteDatabase { *; }
-keepclassmembers class org.telegram.SQLite.SQLitePreparedStatement { *; }
-keepclassmembers class org.telegram.PhoneFormat.CallingCodeInfo { *; }
-keepclassmembers class org.telegram.PhoneFormat.PhoneFormat { *; }
-keepclassmembers class org.telegram.PhoneFormat.PhoneRule { *; }
-keepclassmembers class org.telegram.PhoneFormat.RuleSet { *; }
-keepclassmembers class org.telegram.ui.IntroActivity { *; }
-keepclassmembers class org.telegram.ui.LaunchActivity { *; }
-keepclassmembers class org.telegram.ui.ManageSpaceActivity { *; }
-keepclassmembers class org.telegram.ui.PhotoViewer { *; }
-keepclassmembers class org.telegram.ui.PopupNotificationActivity { *; }
-keepclassmembers class org.telegram.ui.ProfileActivity { *; }
-keepclassmembers class org.telegram.ui.StickersActivity { *; }
-keepclassmembers class org.telegram.ui.Cell.ChatContactCell { *; }
-keepclassmembers class org.telegram.ui.ActionBar.ActionBar { *; }
-keepclassmembers class org.telegram.ui.ActionBar.ActionBarLayout { *; }
-keepclassmembers class org.telegram.ui.ActionBar.ActionBarMenu { *; }
-keepclassmembers class org.telegram.ui.ActionBar.ActionBarMenuItem { *; }
-keepclassmembers class org.telegram.ui.ActionBar.ActionBarPopupWindow { *; }
-keepclassmembers class org.telegram.ui.ActionBar.BackDrawable { *; }
-keepclassmembers class org.telegram.ui.ActionBar.BaseFragment { *; }
-keepclassmembers class org.telegram.ui.ActionBar.BottomSheet { *; }
-keepclassmembers class org.telegram.ui.ActionBar.MenuDrawable { *; }
-keepclassmembers class org.telegram.ui.ActionBar.DrawerLayoutContainer { *; }
-keepclassmembers class org.telegram.ui.Components.AnimatedFileDrawable { *; }
-keepclassmembers class org.telegram.ui.Components.BackupImageView { *; }
-keepclassmembers class org.telegram.ui.Components.ChatActivityEnterView { *; }
-keepclassmembers class org.telegram.ui.Components.ChatAttachView { *; }
-keepclassmembers class org.telegram.ui.Components.RecyclerListView { *; }
-keepclassmembers class org.telegram.ui.Components.URLSpanBotCommand { *; }
-keepclassmembers class org.telegram.ui.Components.URLSpanReplacement { *; }
-keepclassmembers class org.telegram.ui.Components.URLSpanNoUnderline { *; }
-keepclassmembers class org.telegram.ui.Components.URLSpanNoUnderlineBold { *; }
-keepclassmembers class org.telegram.ui.Components.VideoSeekBarView { *; }
-keepclassmembers class org.telegram.ui.Components.VideoTimelineView { *; }
-dontwarn org.telegram.tgnet.**
-dontwarn org.telegram.SQLite.**
-dontwarn org.telegram.PhoneFormat.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.common.cache.**
-dontwarn com.google.common.primitives.**
-dontwarn com.google.common.primitives.**
-dontwarn com.squareup.okhttp.**
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.google.gson.** { *; }
-keepclassmembers enum * { *; }
-keep class **.R$* { *; }
-keep interface ir.tapsell.sdk.NoProguard
-keep class * implements ir.tapsell.sdk.NoProguard { *; }
-keep interface * extends ir.tapsell.sdk.NoProguard { *; }
-keepnames class * extends android.app.Activity
-dontwarn com.unity3d.player.**

-keep class com.google.** {*;}
-keepclassmembers class com.google.** {*;}

-keepclasseswithmembernames class * {
    native <methods>;
}
-keep public class com.google.android.gms.* { public *; }
-keepnames @com.google.android.gms.common.annotation.KeepName class *
-keepclassmembernames class * {
    @com.google.android.gms.common.annotation.KeepName *;
}
-dontwarn com.google.android.gms.**
-dontwarn com.google.common.cache.**
-dontwarn com.google.common.primitives.**
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}


# -- Action Bar Sherlock --
# from http://actionbarsherlock.com/faq.html

-keep class com.actionbarsherlock.** { *; }
-keep interface com.actionbarsherlock.** { *; }

# -- Nine Old Androids --
# same configs as ABS from http://actionbarsherlock.com/faq.html just changed package

-keep class com.nineoldandroids.** { *; }
-keep interface com.nineoldandroids.** { *; }

# -- ACRA --
# from https://github.com/ACRA/acra/wiki/Proguard

# Required to display line numbers and so in ACRA reports
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# keep this class so that logging will show 'ACRA' and not a obfuscated name like 'a'.
# Note: if you are removing log messages elsewhere in this file then this isn't necessary
-keep class org.acra.ACRA {
	*;
}

# keep this around for some enums that ACRA needs
-keep class org.acra.ReportingInteractionMode {
    *;
}
-keepnames class org.acra.ReportField {
    *;
}

# keep this otherwise it is removed by ProGuard



# -- Rest Template --

-keepclassmembers public class org.springframework {
   public *;
}

-dontwarn org.springframework.http.**
-keep class com.nostra13.universalimageloader.**{ *; }
-keep class universal-image-loader-1.9.3-with-sources.** { *; }
-dontnote android.net.http.*
-dontwarn org.apache.*
-dontwarn com.squareup.okhttp.**
-dontwarn android.support.**