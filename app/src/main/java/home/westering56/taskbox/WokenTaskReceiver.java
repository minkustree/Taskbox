package home.westering56.taskbox;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.time.Instant;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import home.westering56.taskbox.data.room.Task;

public class WokenTaskReceiver extends BroadcastReceiver {
    private static final String TAG = "SnoozeManagerBR";
    public static final String EXTRA_LAST_SEEN = "last_seen";

    private static final int NF_ID_SUMMARY = 314;
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

    private void notifyNewlyActiveTasks(@NonNull Context context, @NonNull List<Task> newlyActiveTasks) {
        Log.d(TAG, "Posting notifications for new tasks. New tasks: "+ newlyActiveTasks.size());
        ensureNotificationChannel(context);
        int id = 0;
        for (Task task : newlyActiveTasks) {
            // notifications should group due to the way they're built
            // Will update existing notification group if there's one already there
            notifyTask(context, id, task);
            id += 1; // different ID for each notification in the group
        }
        updateSummaryNotification(context);
    }

    private void notifyTask(@NonNull Context context, int notificationId, @NonNull Task task) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_1")
                .setSmallIcon(R.drawable.ic_notify_small_icon)
                .setAutoCancel(true)
                .setColor(context.getApplicationContext().getResources().getColor(R.color.colorPrimary, null))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(task.summary)
                .setContentIntent(getPendingIntentForTask(context, task))
                .setGroup("home.westering56.taskbox.NOTIFICATION_GROUP_KEY");

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private void updateSummaryNotification(@NonNull Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_1")
                .setSmallIcon(R.drawable.ic_notify_small_icon)
                .setAutoCancel(true)
                .setColor(context.getApplicationContext().getResources().getColor(R.color.colorPrimary, null))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(getPendingIntentForMainActivity(context))
                .setGroup("home.westering56.taskbox.NOTIFICATION_GROUP_KEY")
                .setGroupSummary(true)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

        NotificationManagerCompat.from(context).notify(NF_ID_SUMMARY, builder.build());
    }

    private PendingIntent getPendingIntentForTask(@NonNull Context context, @NonNull final Task task) {
        Intent taskDetailIntent = new Intent(context, TaskDetailActivity.class);
        Log.d(TAG, "Creating pending intent for task '" + task.summary + "' with ID " + task.uid);
        taskDetailIntent.putExtra(MainActivity.EXTRA_TASK_ID, Long.valueOf(task.uid));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(taskDetailIntent);
        // don't re-use pending intents, as each pending intent points to a different task, and
        // re-using them would have them all point to the latest task
        // Include a different request code for each task, otherwise they all get the same pending
        // intent and the task ID extra doesn't go through properly.
        return stackBuilder.getPendingIntent(task.uid, 0);
    }

    private PendingIntent getPendingIntentForMainActivity(@NonNull Context context) {
        Log.d(TAG, "Creating pending intent for main activity");
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

}
