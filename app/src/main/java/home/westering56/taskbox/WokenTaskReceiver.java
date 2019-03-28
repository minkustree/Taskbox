package home.westering56.taskbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.time.Instant;

public class WokenTaskReceiver extends BroadcastReceiver {
    private static final String TAG = "WokenTaskRx";
    public static final String EXTRA_LAST_SEEN = "last_seen";
    public static final String ACTION_NOTIFY_NEW_ACTIVE_TASKS = "home.westering56.taskbox.actions.notify_new_active_tasks";

    /**
     * Called when we're asked to check if any snoozed item has active.
     * Typically these are scheduled to happen for when the next task is scheduled to be un-snoozed.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            Log.w(TAG, "Ignoring broadcast with null Intent");
            return;
        }
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            onReceiveMyPackageReplaced(context);
        } else if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            onReceiveBootCompleted(context);
        } else if (ACTION_NOTIFY_NEW_ACTIVE_TASKS.equals(intent.getAction())) {
            onReceiveNotifyForNewActiveTasks(context, intent);
        } else {
            Log.w(TAG, "Ignoring broadcast for unknown action " + intent.getAction());
        }
    }

    private void onReceiveMyPackageReplaced(Context context) {
        Log.d(TAG, "package replaced - scheduling notification");
        scheduleFirstNotificationCheck(context);
    }

    private void onReceiveBootCompleted(Context context) {
        Log.d(TAG, "boot completed - scheduling notification");
        scheduleFirstNotificationCheck(context);
    }

    private void scheduleFirstNotificationCheck(Context context) {
        SnoozeNotificationManager snm = SnoozeNotificationManager.getInstance(context);
        // don't notify on all of the items that woke between EPOCH and now, as that's a lot of
        // tasks that we just don't care about any more. Accept that tasks that woke while no-one
        // was looking are just not going to get notifications.
        snm.scheduleNextNotificationCheck();
    }

    private void onReceiveNotifyForNewActiveTasks(Context context, Intent intent) {
        Log.d(TAG, "onReceiveNotifyForNewActiveTasks");
        Bundle extras = intent.getExtras();
        Instant lastChecked = extras == null ? Instant.EPOCH: (Instant) extras.get(EXTRA_LAST_SEEN);
        if (lastChecked == null) { lastChecked = Instant.EPOCH; }
        SnoozeNotificationManager.getInstance(context).checkForWokenTasksAndNotify(lastChecked);
    }


}
