package home.westering56.taskbox;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import home.westering56.taskbox.room.Task;

public class TaskRecyclerViewAdapter extends RecyclerView.Adapter<TaskRecyclerViewAdapter.TaskItemViewHolder> {

    private List<Task> dataSet;
    private LayoutInflater inflater;

    public TaskRecyclerViewAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public TaskItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView v = (TextView)inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        return new TaskItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskItemViewHolder holder, int position) {
        if (dataSet != null) {
            Task current = dataSet.get(position);
            holder.textView.setText(current.summary);
        } else {
            // data not ready yet
            holder.textView.setText(R.string.task_item_view_holder_loading);
        }
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

    void setTasks(List<Task> tasks) {
        dataSet = tasks;
        notifyDataSetChanged(); // TODO: This is the least efficient notify - can we do better?
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class TaskItemViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;
        public TaskItemViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }


}
