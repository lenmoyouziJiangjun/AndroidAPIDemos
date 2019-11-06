/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.example.android.apis.R;

/**
 * Version 1.0
 * Created by lll on 2018/5/2.
 * Description
 * <pre>
 *     web和H5之间的互掉
 * </pre>
 * copyright generalray4239@gmail.com
 */
public class WebView2 extends Activity {

    private WebView mWebView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.webview_1);

        mWebView = (WebView) findViewById(R.id.wv1);
        initWebView();
        mWebView.loadUrl("file:///android_asset/AndroidH5Learn.html");
    }

    private void initWebView() {
        WebSettings settings = mWebView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        settings.setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new NativeInterface(),"android");
    }


    public static class NativeInterface{

        @JavascriptInterface
        public void login(String str){
            Log.e("lll","调用我的login方法了哟======="+str);
        }
    }


}
