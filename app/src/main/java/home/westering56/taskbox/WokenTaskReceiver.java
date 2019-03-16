package home.westering56.taskbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WokenTaskReceiver extends BroadcastReceiver {
    private static final String TAG = "SnoozeManagerBR";

    /**
     * Called when we're asked to check if any snoozed item has active.
     * Typically these are scheduled to happen for when the next task is scheduled to be un-snoozed.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive, syncing adapters");
        TaskData td = TaskData.getInstance(context);
        td.syncAdapters();
        td.scheduleNextUpdate(context);
        // TODO: Pop a notification that will end up showing the newly un-snoozed.
    }

}
