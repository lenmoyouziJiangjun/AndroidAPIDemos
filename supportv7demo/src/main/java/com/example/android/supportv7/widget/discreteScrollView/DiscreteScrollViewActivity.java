package com.example.android.supportv7.widget.discreteScrollView;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DiscreteScrollViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DiscreteScrollViewOptions.init(getApplicationContext());
    }
}
