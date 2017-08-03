package com.lll.demo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.apis.ApiDemos;
import com.example.android.support.design.SupportDesignDemos;
import com.example.android.supportv13.Support13Demos;
import com.example.android.supportv4.Support4Demos;
import com.example.android.supportv7.Support7Demos;
import com.lll.mediademo.MediaDemos;
import com.lll.rxdemo.RxAndroidDemo;
import com.lll.supportotherdemos.SupportOtherDemos;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public void goV4(View view) {
        Intent intent = new Intent(this, Support4Demos.class);
        startActivity(intent);
    }

    public void goV7(View view) {
        Intent intent = new Intent(this, Support7Demos.class);
        startActivity(intent);
    }

    public void goV13(View view) {
        Intent intent = new Intent(this, Support13Demos.class);
        startActivity(intent);
    }

    public void goDesign(View view) {
        Intent intent = new Intent(this, SupportDesignDemos.class);
        startActivity(intent);
    }

    public void goAPIDemo(View view) {
        Intent intent = new Intent(this, ApiDemos.class);
        startActivity(intent);
    }

    public void goOtherDemo(View view) {
        Intent intent = new Intent(this, SupportOtherDemos.class);
        startActivity(intent);
    }

    public void goMedia(View view) {
        Intent intent = new Intent(this, MediaDemos.class);
        startActivity(intent);
    }

    public void goRxDemo(View view) {
        Intent intent = new Intent(this, RxAndroidDemo.class);
        startActivity(intent);
    }
}
