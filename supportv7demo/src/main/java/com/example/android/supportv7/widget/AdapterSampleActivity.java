package com.example.android.supportv7.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.android.supportv7.R;
import com.example.android.supportv7.widget.adapter.AnimatorAdapter;

import java.util.ArrayList;
import java.util.Arrays;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.AnimationAdapter;
import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInLeftAnimationAdapter;
import jp.wasabeef.recyclerview.adapters.SlideInRightAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;

public class AdapterSampleActivity extends AppCompatActivity {

    private static String[] data = new String[]{
            "Apple", "Ball", "Camera", "Day", "Egg", "Foo", "Google", "Hello", "Iron", "Japan", "Coke",
            "Dog", "Cat", "Yahoo", "Sony", "Canon", "Fujitsu", "USA", "Nexus", "LINE", "Haskell", "C++",
            "Java", "Go", "Swift", "Objective-c", "Ruby", "PHP", "Bash", "ksh", "C", "Groovy", "Kotlin",
            "Chip", "Japan", "U.S.A", "San Francisco", "Paris", "Tokyo", "Silicon Valley", "London",
            "Spain", "China", "Taiwan", "Asia", "New York", "France", "Kyoto", "Android", "Google",
            "iPhone", "iPad", "iPod", "Wasabeef"
    };

    enum Type {
        AlphaIn {
            @Override
            public AnimationAdapter get(Context context) {
                AnimatorAdapter adapter = new AnimatorAdapter(context, new ArrayList<>(Arrays.asList(data)));
                return new AlphaInAnimationAdapter(adapter);
            }
        },
        ScaleIn {
            @Override
            public AnimationAdapter get(Context context) {
                AnimatorAdapter adapter = new AnimatorAdapter(context, new ArrayList<>(Arrays.asList(data)));
                return new ScaleInAnimationAdapter(adapter);
            }
        },
        SlideInBottom {
            @Override
            public AnimationAdapter get(Context context) {
                AnimatorAdapter adapter = new AnimatorAdapter(context, new ArrayList<>(Arrays.asList(data)));
                return new SlideInBottomAnimationAdapter(adapter);
            }
        },
        SlideInLeft {
            @Override
            public AnimationAdapter get(Context context) {
                AnimatorAdapter adapter = new AnimatorAdapter(context, new ArrayList<>(Arrays.asList(data)));
                return new SlideInLeftAnimationAdapter(adapter);
            }
        },
        SlideInRight {
            @Override
            public AnimationAdapter get(Context context) {
                AnimatorAdapter adapter = new AnimatorAdapter(context, new ArrayList<>(Arrays.asList(data)));
                return new SlideInRightAnimationAdapter(adapter);
            }
        };

        public abstract AnimationAdapter get(Context context);
    }

    private RecyclerView mRecycleView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animator_simple);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        initRecycleView();
        initSpinner();
    }

    private void initSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        for (Type type : Type.values()) {
            spinnerAdapter.add(type.name());
        }
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AnimationAdapter adapter = Type.values()[position].get(AdapterSampleActivity.this);
                adapter.setFirstOnly(true);
                adapter.setDuration(500);
                adapter.setInterpolator(new OvershootInterpolator(.5f));
                mRecycleView.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initRecycleView() {
        mRecycleView = (RecyclerView) findViewById(R.id.list);
        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleView.setItemAnimator(new FadeInAnimator());
        AnimatorAdapter adapter = new AnimatorAdapter(this, new ArrayList<>(Arrays.asList(data)));
        AlphaInAnimationAdapter alphaAdapter = new AlphaInAnimationAdapter(adapter);
        alphaAdapter.setFirstOnly(true);
        alphaAdapter.setDuration(500);
        alphaAdapter.setInterpolator(new OvershootInterpolator(.5f));
        mRecycleView.setAdapter(alphaAdapter);
    }


}
