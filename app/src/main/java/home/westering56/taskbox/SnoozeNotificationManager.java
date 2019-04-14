package home.westering56.taskbox;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.RemoteViews;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;
import home.westering56.taskbox.data.room.Task;

import static home.westering56.taskbox.MainActivity.EXTRA_TASK_ID;

public class SnoozeNotificationManager extends BroadcastReceiver {
    private static final String TAG = "SnoozeNotifyMgr";

    private static final int NF_ID_SUMMARY = -314159; // constant ID for the summary notification

    private static final String ACTION_SCHEDULE_NOTIFICATION_CHECK = "home.westering56.taskbox.actions.notification.schedule_check";
    private static final String ACTION_NOTIFICATION_CHECK_TRIGGERED = "home.westering56.taskbox.actions.notification.do_check";
    private static final String ACTION_NOTIFICATION_DONE = "home.westering56.taskbox.action.notification.done";
    private static final String ACTION_NOTIFICATION_UNDO = "home.westering56.taskbox.action.notification.undo";
    private static final String ACTION_NOTIFICATION_DISMISS = "home.westering56.taskbox.action.notification.dismiss";

    private static final String EXTRA_NOTIFICATION_ID = "home.westering56.taskbox.extra.notification_id";
    private static final String EXTRA_LAST_SEEN = "home.westering56.taskbox.extra.last_seen";

    private static Handler sHandler;

    public static DataSetObserver newTaskDataObserver(Context context) {
        Log.d(TAG, "getting new task data observer for Snooze notification manager");
        final Context appContext = context.getApplicationContext();
        // Every time the task data changes, tell this schedule the next notification check.
        return new DataSetObserver() {
            @Override
            public void onChanged() {
                triggerScheduleNotificationCheck(appContext);
            }
        };
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, "Received Intent with action: " + action);
        if (action == null) return;
        switch (action) {
            case Intent.ACTION_BOOT_COMPLETED: // fall through
            case Intent.ACTION_MY_PACKAGE_REPLACED:
                onDeviceOrAppStart(context);
                break;
            case ACTION_SCHEDULE_NOTIFICATION_CHECK:
                scheduleNotificationCheck(context);
                break;
            case ACTION_NOTIFICATION_CHECK_TRIGGERED:
                onNotificationCheckTriggered(context, intent);
                break;
            case ACTION_NOTIFICATION_DISMISS:
                onNotificationActionDismissed(context);
                break;
            case ACTION_NOTIFICATION_DONE:
                onNotificationActionDone(context, intent);
                break;
            case ACTION_NOTIFICATION_UNDO:
                onNotificationActionUndo(context, intent);
                break;
            default:
                // no-op
        }
    }

    private void onDeviceOrAppStart(Context context) {
        /* We don't need to register as an observer of task data, It's registered the first time an
         * instance of TaskData is created. */
        Log.d(TAG, "boot completed or app replaced - scheduling notification check");
        scheduleNotificationCheck(context);
    }

    private void onNotificationCheckTriggered(Context context, Intent intent) {
        Log.d(TAG, "Notification check triggered");
        Bundle extras = intent.getExtras();
        Instant lastChecked = extras == null ? Instant.EPOCH : (Instant) extras.get(EXTRA_LAST_SEEN);
        if (lastChecked == null) {
            lastChecked = Instant.EPOCH;
        }
        checkForWokenTasksAndNotify(context, lastChecked);
    }

    private void onNotificationActionDismissed(Context context) {
        Log.d(TAG, "Notification dismissed");
        updateSummaryNotification(context);
    }

    private void onNotificationActionDone(Context context, Intent intent) {
        Log.d(TAG, "Notification done clicked");
        TaskData taskData = TaskData.getInstance(context);
        if (intent.hasExtra(EXTRA_TASK_ID)) {
            int taskId = intent.getIntExtra(EXTRA_TASK_ID, -1);
            Task task = taskData.getTask(taskId);
            task.done();
            taskData.updateTask(task);

            if (intent.hasExtra(EXTRA_NOTIFICATION_ID)) {
                int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
                Log.d(TAG, "Building replacement 'done/undo?' notification with notification ID" + notificationId);
                // TODO: Consider having this done screen be different for a 'done' repeating task?

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_done);
                remoteViews.setOnClickPendingIntent(R.id.notification_done_button, getPendingIntentForUndo(context, taskId, notificationId));

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_1")
                        .setSmallIcon(R.drawable.ic_notify_small_icon)
                        .setAutoCancel(true)
                        .setColor(context.getApplicationContext().getResources().getColor(R.color.colorPrimary, null))
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setCustomContentView(remoteViews)
                        .setDeleteIntent(getPendingIntentForNotificationDismiss(context, notificationId))
                        // .setTimeoutAfter(5 * 1000 /*ms*/) // use cancellable Handler instead
                        .setOnlyAlertOnce(true)
                        .setGroup(context.getString(R.string.notification_group_key));
                NotificationManagerCompat.from(context).notify(notificationId, builder.build());
                // replacing / updating existing notification - no need to adjust notification count
                scheduleRemovalOfTaskNotification(context, notificationId);
            }
        }
    }

    private void onNotificationActionUndo(Context context, Intent intent) {
        Log.d(TAG, "Notification undo");
        if (intent.hasExtra(EXTRA_TASK_ID)) {
            int taskId = intent.getIntExtra(EXTRA_TASK_ID, -1);
            TaskData taskData = TaskData.getInstance(context);
            taskData.undoLast();
            if (intent.hasExtra(EXTRA_NOTIFICATION_ID)) {
                int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
                cancelRemovalOfTaskNotification(notificationId);
                notifyTask(context, notificationId, taskData.getTask(taskId)); // replace with new notification
            }
        }
    }

    /**
     * Schedule a wakeup to check for newly active, previously snoozed tasks.
     * Usually scheduled for the time that the next snoozed task is due to become active.
     */
    private void scheduleNotificationCheck(Context context) {
        Instant nextWakeInstant = TaskData.getInstance(context).getNextWakeupInstant();
        if (nextWakeInstant != null) {
            Log.d(TAG, "Scheduling next notification check for " + nextWakeInstant.toString());
            PendingIntent pendingIntent = getPendingIntentForCheckTriggered(context);
            AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextWakeInstant.toEpochMilli(), pendingIntent);
        } else {
            Log.d(TAG, "No snoozed tasks, no notification check scheduled");
        }
    }

    /*
     * Issuing and updating notifications, interaction with NotificationManager, etc.
     */

    private void checkForWokenTasksAndNotify(Context context, @NonNull Instant lastChecked) {
        Log.d(TAG, "checkForWokenTasksAndNotify");
        Instant now = Instant.now();
        List<Task> newlyWokenTasks = TaskData.getInstance(context).getNewlyActiveTasks(lastChecked, now);
        notifyTasks(context, newlyWokenTasks);
        // One or more snoozed tasks may have become un-snoozed. Anything that looks at this data
        // will need to be poked so it can re-assess what's snoozed and what's not.
        TaskData.getInstance(context).notifyDataSetChanged(); // will also schedule a new check
    }

    private void ensureNotificationChannel(Context context) {
        NotificationChannel channel = new NotificationChannel("CHANNEL_1", "Default",
                NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManagerCompat.from(context).createNotificationChannel(channel);
    }

    private void notifyTasks(Context context, @NonNull List<Task> newlyWokenTasks) {
        Log.d(TAG, "Posting notifications for newly woken tasks. New tasks: " + newlyWokenTasks.size());
        ensureNotificationChannel(context);
        for (Task task : newlyWokenTasks) {
            // notifications should group due to the way they're built
            // Will update existing notification group if there's one already there
            notifyTask(context, task.uid, task);
        }
        updateSummaryNotification(context);
    }

    private void notifyTask(Context context, int notificationId, @NonNull Task task) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_1")
                .setSmallIcon(R.drawable.ic_notify_small_icon)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.colorPrimary, null))
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentTitle(task.summary)
                .setContentIntent(getPendingIntentForTask(context, task))
                .addAction(R.drawable.ic_notify_small_icon, "Done", getPendingIntentForDone(context, task.uid, notificationId))
                .setDeleteIntent(getPendingIntentForNotificationDismiss(context, notificationId))
                .setTimeoutAfter(0)
                .setOnlyAlertOnce(true)
                .setGroup(context.getString(R.string.notification_group_key));

        NotificationManagerCompat.from(context).notify(notificationId, builder.build());
    }

    private void updateSummaryNotification(Context context) {
        NotificationManager managerActual = context.getSystemService(NotificationManager.class);
        StatusBarNotification[] statusBarNotifications = managerActual.getActiveNotifications();

        Log.d(TAG, "Active notification count: " + statusBarNotifications.length);
        if (statusBarNotifications.length == 0) {
            Log.d(TAG, "No notifications, so no summary needed");
        } else if (statusBarNotifications.length == 1) {
            if (statusBarNotifications[0].getId() == NF_ID_SUMMARY) {
                Log.d(TAG, "Summary is the last remaining notification. Cancelling it.");
                NotificationManagerCompat.from(context).cancel(NF_ID_SUMMARY);
            } else {
                Log.d(TAG, "Single notification doesn't need a summary. No change.");
            }
        } else {
            Log.d(TAG, "Multiple notifications, creating or updating summary notification");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_1")
                    .setSmallIcon(R.drawable.ic_notify_small_icon)
                    .setAutoCancel(true)
                    .setColor(context.getResources().getColor(R.color.colorPrimary, null))
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(getPendingIntentForMainActivity(context))
                    .setGroup(context.getString(R.string.notification_group_key))
                    .setGroupSummary(true)
                    .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN);

            NotificationManagerCompat.from(context).notify("summary", NF_ID_SUMMARY, builder.build());
        }
    }

    /*
     * Utilities to manually schedule the removal of notifications after a certain time (and cancel
     * that removal if we need to)
     */

    private static Handler getDefaultHandler() {
        synchronized (SnoozeNotificationManager.class) {
            if (sHandler == null) {
                sHandler = new Handler();
            }
        }
        return sHandler;
    }

    private static final Map<Integer, Object> sTokenMap = new ConcurrentHashMap<>();

    private void scheduleRemovalOfTaskNotification(final Context context, final int notificationId) {
        Log.d(TAG, "Scheduling removal of notification with ID: " + notificationId);
        Handler h = getDefaultHandler();
        Object token = new Object();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // once we're running, no point keeping the cancellation token any more
                sTokenMap.remove(notificationId);
                removeTaskNotification(context, notificationId);
            }
        }, token, 5 * 1000);
        sTokenMap.put(notificationId, token);
    }

    private void cancelRemovalOfTaskNotification(int notificationId) {
        Object token = sTokenMap.remove(notificationId);
        if (token != null) {
            getDefaultHandler().removeCallbacksAndMessages(token);
        }
    }

    private void removeTaskNotification(Context context, int notificationId) {
        Log.d(TAG, "Cancelling notification with ID: " + notificationId);
        NotificationManagerCompat.from(context).cancel(notificationId);
        updateSummaryNotification(context);
    }

    /*
     * Builds a pending intents to be used with notifications.
     * e.g. SnoozeManagerPendingIntentFactory
     */

    private static PendingIntent getPendingIntentForTask(Context context, @NonNull final Task task) {
        Intent taskDetailIntent = new Intent(context, TaskDetailActivity.class);
        Log.d(TAG, "Creating pending intent for task '" + task.summary + "' with ID " + task.uid);
        taskDetailIntent.putExtra(EXTRA_TASK_ID, task.uid);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(taskDetailIntent);
        // don't re-use pending intents, as each pending intent points to a different task, and
        // re-using them would have them all point to the latest task
        // Include a different request code for each task, otherwise they all get the same pending
        // intent and the task ID extra doesn't go through properly.
        return stackBuilder.getPendingIntent(task.uid, 0);
    }

    private static PendingIntent getPendingIntentForCheckTriggered(Context context) {
        Intent intent = new Intent(context, SnoozeNotificationManager.class);
        intent.setAction(ACTION_NOTIFICATION_CHECK_TRIGGERED);
        // used to determine what became active between now and wakeup, for notification use
        intent.putExtra(EXTRA_LAST_SEEN, Instant.now());
        // If we already have an intent pending, use it - its original 'last seen' time will be
        // earlier than now. However, cancel the intent once it's been sent, so we don't get
        // yesterday's 'last seen' times if the intent has already fired and done its job.
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    private static PendingIntent getPendingIntentForMainActivity(Context context) {
        Log.d(TAG, "Creating pending intent for main activity");
        Intent intent = new Intent(context, MainActivity.class);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private static PendingIntent getPendingIntentForNotificationDismiss(Context context, int notificationId) {
        Log.d(TAG, "Creating pending intent for notification dismissal, notification id: " + notificationId);
        Intent intent = new Intent(context, SnoozeNotificationManager.class);
        intent.setAction(ACTION_NOTIFICATION_DISMISS);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        return PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_ONE_SHOT);
    }

    private static PendingIntent getPendingIntentForDone(@NonNull Context context, int taskId, int notificationId) {
        Log.d(TAG, "Creating pending intent for notification done action, notification id: " + notificationId);
        Intent intent = new Intent(context, SnoozeNotificationManager.class);
        intent.setAction(ACTION_NOTIFICATION_DONE);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        return PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getPendingIntentForUndo(@NonNull Context context, int taskId, int notificationId) {
        Log.d(TAG, "Creating pending intent for notification undo action, notification id: " + notificationId);
        Intent intent = new Intent(context, SnoozeNotificationManager.class);
        intent.setAction(ACTION_NOTIFICATION_UNDO);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        return PendingIntent.getBroadcast(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /*
     * Static methods for triggering snooze manager events from other classes
     */

    private static void triggerScheduleNotificationCheck(Context context) {
        SnoozeNotificationManager snoozeNotificationManager = new SnoozeNotificationManager();
        snoozeNotificationManager.scheduleNotificationCheck(context);
    }


    public static void testNotification(Context context) {
        SnoozeNotificationManager manager = new SnoozeNotificationManager();
        manager.ensureNotificationChannel(context);
        Task task = TaskData.getInstance(context).getTask(1);
        manager.notifyTask(context, task.uid, task);
        manager.updateSummaryNotification(context);
    }

}
