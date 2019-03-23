package home.westering56.taskbox;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.time.Instant;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import home.westering56.taskbox.data.room.Task;

public class WokenTaskReceiver extends BroadcastReceiver {
    private static final String TAG = "SnoozeManagerBR";
    public static final String EXTRA_LAST_SEEN = "last_seen";

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
        Bundle extras = intent.getExtras();
        Instant lastSeen = extras == null ? Instant.MIN : (Instant) extras.get(EXTRA_LAST_SEEN);
        Instant now = Instant.now();
        // Pop a notification that will end up showing the newly un-snoozed.
        List<Task> newlyActiveTasks = td.getNewlyActiveTasks(lastSeen, now);
        notifyNewlyActiveTasks(context, newlyActiveTasks);
    }

    private void ensureNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel("CHANNEL_1", "Default",
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManagerCompat.from(context).createNotificationChannel(channel);
    }

    private void notifyNewlyActiveTasks(Context context, List<Task> newlyActiveTasks) {
        Log.d(TAG, "Posting notifications for new tasks. New tasks: "+ newlyActiveTasks.size());
        ensureNotificationChannel(context);
        int id = 0;
        for (Task task : newlyActiveTasks) {
            // notifications should group due to the way they're built
            // Will update existing notification group if there's one already there
            notifyTask(context, id, task);
            id += 1; // different ID for each notification in the group
        }
    }

    private void notifyTask(Context context, int notificationId, Task task) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_1")
                .setSmallIcon(R.drawable.ic_notify_small_icon)
                .setContentTitle(task.summary)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setGroup("home.westering56.taskbox.NOTIFICATION_GROUP_KEY");

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

}
