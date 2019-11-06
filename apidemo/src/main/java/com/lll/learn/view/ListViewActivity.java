package com.lll.learn.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class ListViewActivity extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }


    private static class MyAdapter extends BaseAdapter {


        @Override
        public int getItemViewType(int position) {
            //获取当前position对应的type
            return super.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return 5;//返回有几种类型的itemView
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            if (convertView == null) {

            } else {

            }

            return convertView;
        }


        public View newItemView(int position, ViewGroup parent) {
            int itemType = getItemViewType(position);
            View convertView = null;
            switch (itemType) {
                case 0:
                    convertView = new TextView(parent.getContext());

                    break;
                case 1:
                    convertView = new ImageView(parent.getContext());
                    break;
                case 2:
                    convertView = new EditText(parent.getContext());
                    break;
            }
            return convertView;
        }


        public void bindView(int position, View convertView) {
            int itemType = getItemViewType(position);
            switch (itemType) {
                case 0:
                    ((TextView)convertView).setText("kkkkkkkksadasdf");
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        }
    }
}
