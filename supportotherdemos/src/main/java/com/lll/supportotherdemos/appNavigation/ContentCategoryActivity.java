package com.lll.supportotherdemos.appNavigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;

import com.lll.supportotherdemos.R;

public class ContentCategoryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_category);
        ActionBarCompat.setDisplayHomeAsUpEnabled(this, true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);//返回上一页
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onViewContent(View view){
        Intent target = new Intent(this, ContentViewActivity.class);
        startActivity(target);
    }


}
