package com.example.android.architecture.blueprints.todoapp.tasks;

import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.lll.architecturemvvm.R;

import rx.functions.Action0;
import rx.functions.Action1;

public class TaskItemViewHolder implements View.OnClickListener, CheckBox.OnCheckedChangeListener {

    private View mRow;

    public TextView mTitle;
    public CheckBox mCheckBox;

    private Action0 mOnItemClickAction;
    private Action1<Boolean> mOnCheckAction;


    public TaskItemViewHolder(View itemView) {

        mRow = itemView;
        mTitle = (TextView) itemView.findViewById(R.id.title);
        mCheckBox = (CheckBox) itemView.findViewById(R.id.complete);

        itemView.setOnClickListener(this);
    }

    public void bindItem(TaskItem taskItem) {
        // disable the listener for the setup
        mCheckBox.setOnCheckedChangeListener(null);
        mTitle.setText(taskItem.getTask().getTitleForList());
        mCheckBox.setChecked(taskItem.getTask().isCompleted());
        mRow.setBackgroundResource(taskItem.getBackground());

        mOnCheckAction = taskItem.getOnCheckAction();
        mOnItemClickAction = taskItem.getOnClickAction();
        mCheckBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        if (mOnItemClickAction != null) {
            mOnItemClickAction.call();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mOnCheckAction != null) {
            mOnCheckAction.call(isChecked);
        }
    }
}
