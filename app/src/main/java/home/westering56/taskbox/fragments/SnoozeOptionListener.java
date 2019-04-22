package home.westering56.taskbox.fragments;

import org.dmfs.rfc5545.recur.RecurrenceRule;

import java.time.LocalDateTime;

public interface SnoozeOptionListener {
    /**
     * @param snoozeUntil the chosen time until which the task will snooze
     * @param rule        {@link RecurrenceRule} that describes how this task repeats after snoozing,
     *                    or null if the task does not repeat
     */
    void onSnoozeOptionSelected(LocalDateTime snoozeUntil, RecurrenceRule rule);
}
