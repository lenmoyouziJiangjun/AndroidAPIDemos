package com.example.android.architecture.blueprints.todoapp.tasks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.lll.architecturemvvm.R;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class TasksAdapter extends BaseAdapter {

    private List<TaskItem> mTasks;

    public TasksAdapter(List<TaskItem> tasks) {
        mTasks = tasks;
    }

    public void replaceData(List<TaskItem> tasks) {
        setList(tasks);
        notifyDataSetChanged();
    }

    private void setList(List<TaskItem> tasks) {
        mTasks = checkNotNull(tasks);
    }


    @Override
    public int getCount() {
        return mTasks.size();
    }

    @Override
    public Object getItem(int position) {
        return mTasks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        TaskItemViewHolder viewHolder;

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            rowView = inflater.inflate(R.layout.item_task, parent, false);

            viewHolder = new TaskItemViewHolder(rowView);
            rowView.setTag(viewHolder);
        } else {
            viewHolder = (TaskItemViewHolder) rowView.getTag();
        }

        final TaskItem taskItem = (TaskItem) getItem(position);
        viewHolder.bindItem(taskItem);
        return rowView;
    }
}
