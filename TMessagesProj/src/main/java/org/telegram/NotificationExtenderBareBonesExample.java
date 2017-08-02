package org.telegram.messenger;

/**
 * Created by neavo on 7/30/2017.
 */

import com.onesignal.OSNotificationPayload;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

public class NotificationExtenderBareBonesExample extends NotificationExtenderService {
    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult receivedResult) {
        // Read properties from result.

        // Return true to stop the notification from displaying.
        return true;
    }
}
