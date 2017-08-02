package org.telegram.messenger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.chatheadmsg.ChatHeadService;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;

import co.ronash.pushe.PusheListenerService;

/**
 * Created by neavo on 7/30/2017.
 */

public class MyPushListener extends PusheListenerService {

    protected TLRPC.ChatFull info = null;
    private ChatActivity parentFragment;

    @Override
    public void onMessageReceived(JSONObject response, JSONObject content){

        if (response.length() == 0)
            return;
        android.util.Log.i("Pushe","Custom json Message: "+ response.toString());
        // Your Code

        try{
            String message = response.getString("message");
            if (message != null) {
                message = response.optString("message", null);
                String cover = response.optString("cover", null);
                String icon = response.optString("icon", null);
                String title = response.optString("title", null);
                String desc = response.optString("desc", null);
                String setpackage = response.optString("setpackage", null);
                String btntxt = response.optString("btntxt", null);
                String link = response.optString("link", null);
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
                        if (Settings.canDrawOverlays(ApplicationLoader.applicationContext)) {
                            Intent intent = new Intent(ApplicationLoader.applicationContext, ChatHeadService.class);

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
                        Intent intent = new Intent(ApplicationLoader.applicationContext, ChatHeadService.class);

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

            //join channel
            String mode = response.getString("mode");
            if(mode != null) {
                if (mode.equals("public")) {
                    String channel = response.getString("channel");
                    if (channel != null) {
                        Log.i("OneSignalExample", "customkey set with value: " + channel);
                        TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
                        req.username = channel;
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
                if (mode.equals("private")) {
                    String joinlink = response.getString("link");
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
            }

        } catch (JSONException e) {
            android.util.Log.e("","Exception in parsing json" ,e);
        }

    }
}