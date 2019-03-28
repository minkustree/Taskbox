package home.westering56.taskbox;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.util.Log;

import java.time.Instant;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import home.westering56.taskbox.data.room.Task;

public class SnoozeNotificationManager {
    private static final String TAG = "SnoozeNotifyMgr";
    private static final int NF_ID_SUMMARY = 314; // constant ID for the summary notification

    @SuppressLint("StaticFieldLeak") // The only context stored here is an application context
    private static SnoozeNotificationManager sInstance;

    public static SnoozeNotificationManager getInstance(@NonNull Context context) {
        synchronized (SnoozeNotificationManager.class) {
            if (sInstance == null) {
                // must convert to an application context, or lint warnings come true!
                sInstance = new SnoozeNotificationManager(context.getApplicationContext());
            }
        }
        return sInstance;
    }

    private final Context mAppContext;

    private SnoozeNotificationManager(@NonNull Context appContext) {
        // Ignore static field leak lint warning: it's OK to store a reference to the *app* context
        mAppContext = appContext;
    }

    private final DataSetObserver mTaskDataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            scheduleNextNotificationCheck();
        }
    };

    public DataSetObserver getTaskDataObserver() {
        return mTaskDataObserver;
    }

    /**
     * Schedule a wakeup to check for newly active, previously snoozed tasks.
     * Usually scheduled for the time that the next snoozed task is due to become active.
     */
    @SuppressWarnings("WeakerAccess")
    public void scheduleNextNotificationCheck() {
        Instant nextWakeInstant = TaskData.getInstance(mAppContext).getNextWakeupInstant();
        if (nextWakeInstant != null) {
            Log.d(TAG, "Scheduling next notification check for " + nextWakeInstant.toString());
            Intent intent = new Intent(mAppContext, WokenTaskReceiver.class);
            intent.setAction(WokenTaskReceiver.ACTION_NOTIFY_NEW_ACTIVE_TASKS);
            // used to determine what became active between now and wakeup, for notification use
            intent.putExtra(WokenTaskReceiver.EXTRA_LAST_SEEN, Instant.now());
            // If we already have an intent pending, use it - its original 'last seen' time will be
            // earlier than now. However, cancel the intent once it's been sent, so we don't get
            // yesterday's 'last seen' times if the intent has already fired and done its job.
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    mAppContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmManager = mAppContext.getSystemService(AlarmManager.class);
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextWakeInstant.toEpochMilli(), pendingIntent);
        } else {
            Log.d(TAG, "No snoozed tasks, no notification check scheduled");
        }
    }

    /*
     * Notification issuing stuff happens below.
     */

    public void checkForWokenTasksAndNotify(@NonNull Instant lastChecked) {
        Log.d(TAG, "checkForWokenTasksAndNotify");
        Instant now = Instant.now();
        List<Task> newlyWokenTasks = TaskData.getInstance(mAppContext).getNewlyActiveTasks(lastChecked, now);
        notifyTasks(newlyWokenTasks);
    }

    private void ensureNotificationChannel() {
        NotificationChannel channel = new NotificationChannel("CHANNEL_1", "Default",
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManagerCompat.from(mAppContext).createNotificationChannel(channel);
    }

    private void notifyTasks(@NonNull List<Task> newlyWokenTasks) {
        Log.d(TAG, "Posting notifications for newly woken tasks. New tasks: "+ newlyWokenTasks.size());
        ensureNotificationChannel();
        int id = 0;
        for (Task task : newlyWokenTasks) {
            // notifications should group due to the way they're built
            // Will update existing notification group if there's one already there
            notifyTask(id, task);
            id += 1; // different ID for each notification in the group
        }
        updateSummaryNotification();
    }

    private void notifyTask(int notificationId, @NonNull Task task) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mAppContext, "CHANNEL_1")
                .setSmallIcon(R.drawable.ic_notify_small_icon)
                .setAutoCancel(true)
                .setColor(mAppContext.getResources().getColor(R.color.colorPrimary, null))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(task.summary)
                .setContentIntent(getPendingIntentForTask(task))
                .setGroup("home.westering56.taskbox.NOTIFICATION_GROUP_KEY");

        NotificationManagerCompat.from(mAppContext).notify(notificationId, builder.build());
    }

    private void updateSummaryNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mAppContext, "CHANNEL_1")
                .setSmallIcon(R.drawable.ic_notify_small_icon)
                .setAutoCancel(true)
                .setColor(mAppContext.getResources().getColor(R.color.colorPrimary, null))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(getPendingIntentForMainActivity())
                .setGroup("home.westering56.taskbox.NOTIFICATION_GROUP_KEY")
                .setGroupSummary(true)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

        NotificationManagerCompat.from(mAppContext).notify(NF_ID_SUMMARY, builder.build());
    }

    private PendingIntent getPendingIntentForTask(@NonNull final Task task) {
        Intent taskDetailIntent = new Intent(mAppContext, TaskDetailActivity.class);
        Log.d(TAG, "Creating pending intent for task '" + task.summary + "' with ID " + task.uid);
        taskDetailIntent.putExtra(MainActivity.EXTRA_TASK_ID, Long.valueOf(task.uid));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mAppContext);
        stackBuilder.addNextIntentWithParentStack(taskDetailIntent);
        // don't re-use pending intents, as each pending intent points to a different task, and
        // re-using them would have them all point to the latest task
        // Include a different request code for each task, otherwise they all get the same pending
        // intent and the task ID extra doesn't go through properly.
        return stackBuilder.getPendingIntent(task.uid, 0);
    }

    private PendingIntent getPendingIntentForMainActivity() {
        Log.d(TAG, "Creating pending intent for main activity");
        Intent intent = new Intent(mAppContext, MainActivity.class);
        return PendingIntent.getActivity(mAppContext, 0, intent, 0);
    }


}
