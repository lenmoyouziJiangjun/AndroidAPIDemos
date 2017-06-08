package com.lll.mediademo.basicmediarouter;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lll.mediademo.R;

/**
 * Version 1.0
 * Created by lll on 17/6/8.
 * Description 定义一个副屏dialog
 * copyright generalray4239@gmail.com
 */
public class SamplePresentation extends Presentation {

    private LinearLayout mLayout;
    private TextView mText;

    public SamplePresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.router_display);

        mLayout = (LinearLayout) findViewById(R.id.display_layout);
        mText = (TextView) findViewById(R.id.display_text);

        TextView smallText = (TextView) findViewById(R.id.display_smalltext);
        final String name = getDisplay().getName();
        smallText.setText(getResources().getString(R.string.display_name, name));
    }

    public void setColor(int color) {
        mLayout.setBackgroundColor(color);

        // Display the color as a string on screen
        String s = getResources().getString(R.string.display_color, color);
        mText.setText(s);
    }
}
