package home.westering56.taskbox;


import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskItemViewHolder> {

    private String[] dataSet;

    public TaskAdapter(String[] dataSet) {
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public TaskItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView v = (TextView)LayoutInflater.from(parent.getContext()).inflate(
                android.R.layout.simple_list_item_1, parent, false);
        TaskItemViewHolder vh = new TaskItemViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull TaskItemViewHolder holder, int position) {
        holder.textView.setText(dataSet[position]);
    }


    @Override
    public int getItemCount() {
        return dataSet.length;
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
