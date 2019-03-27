package home.westering56.taskbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.time.Instant;

public class WokenTaskReceiver extends BroadcastReceiver {
    private static final String TAG = "SnoozeManagerBR";
    public static final String EXTRA_LAST_SEEN = "last_seen";

    /**
     * Called when we're asked to check if any snoozed item has active.
     * Typically these are scheduled to happen for when the next task is scheduled to be un-snoozed.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        onReceiveCheckForAwakeTasks(context, intent);
        // TODO: Handle system startup broadcast
    }

    private void onReceiveCheckForAwakeTasks(Context context, Intent intent) {
        Log.d(TAG, "onReceiveCheckForAwakeTasks");
        Bundle extras = intent.getExtras();
        Instant lastChecked = extras == null ? Instant.MIN : (Instant) extras.get(EXTRA_LAST_SEEN);
        assert lastChecked != null;
        SnoozeNotificationManager.getInstance(context).checkForWokenTasksAndNotify(lastChecked);
    }


}
