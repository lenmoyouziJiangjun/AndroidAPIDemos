package com.example.android.supportv7.widget.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

public class TestRecyclerViewActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RecyclerView recyclerView = new RecyclerView(this);
    }

    private void setUpRecyclerView(RecyclerView recyclerView) {
//        recyclerView
    }
}
