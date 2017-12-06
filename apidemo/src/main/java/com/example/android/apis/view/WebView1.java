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
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.example.android.apis.R;

import static android.webkit.WebSettings.LOAD_CACHE_ONLY;


/**
 * Version 1.0
 * Created by lll on 11/1/17.
 * Description
 *  1、WebView 使用：http://blog.csdn.net/carson_ho/article/details/64904691
 *  2、Android 和 js 的通信：
 *     1、Android调用js代码：
 *        1. 通过WebView的loadUrl（）
          2. 通过WebView的evaluateJavascript（）
       2、js调用android 代码：
          1. 通过WebView的addJavascriptInterface（）进行对象映射
          2. 通过 WebViewClient 的shouldOverrideUrlLoading ()方法回调拦截 url
          3. 通过 WebChromeClient 的onJsAlert()、onJsConfirm()、onJsPrompt（）方法回调拦截JS对话框alert()、confirm()、prompt（） 消息
 * copyright generalray4239@gmail.com
 */
public class WebView1 extends Activity {


    private WebView mWebView;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.webview_1);

        mWebView = (WebView) findViewById(R.id.wv1);
        initWebView();
    }

    private void initWebView() {
        final WebSettings webSettings = mWebView.getSettings();
        //如果访问的页面中要与Javascript交互，则webview必须设置支持Javascript
        webSettings.setJavaScriptEnabled(true);

        //支持插件
        webSettings.setPluginState(WebSettings.PluginState.ON);

        //设置自适应屏幕，两者合用
        webSettings.setUseWideViewPort(true); //将图片调整到适合webview的大小
        webSettings.setLoadWithOverviewMode(true); // 缩放至屏幕的大小

        //缩放操作
        webSettings.setSupportZoom(true); //支持缩放，默认为true。是下面那个的前提。
        webSettings.setBuiltInZoomControls(true); //设置内置的缩放控件。若为false，则该WebView不可缩放
        webSettings.setDisplayZoomControls(false); //隐藏原生的缩放控件

        webSettings.setAllowFileAccess(true); //设置可以访问文件
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true); //支持通过JS打开新窗口
        webSettings.setLoadsImagesAutomatically(true); //支持自动加载图片
        webSettings.setDefaultTextEncodingName("utf-8");//设置编码格式


        /*webView缓存
        * 1、缓存模式如下：
             LOAD_CACHE_ONLY: 不使用网络，只读取本地缓存数据。
             LOAD_DEFAULT: 默认设置，如果有本地缓存，且缓存有效未过期，则直接使用本地缓存，否则加载网络数据
             LOAD_NO_CACHE: 不使用缓存，只从网络获取数据.
             LOAD_CACHE_ELSE_NETWORK，只要本地有，无论是否过期，都使用缓存中的数据。 本地没有则加载网络数据
          2、本地缓存目录：
             /data/data/${packagename}/cache/org.chromium.android_webview/


        * */
        webSettings.setCacheMode(LOAD_CACHE_ONLY);

        /*h5的缓存机制
          1、Dom Storage:
             存储方式为键值对存储，K/V 的数据格式为字符串类型，如果需要保存非字符串，
             需要在读写的时候进行类型转换或使用 JSON 序列化。为此，
             Dom Storage 不适合存储复杂或者存储空间要求大的数据（如图片数据等），
             一般用于存储一些服务器或者本地的临时数据，和 Android SharePreference 机制类似。
          2、Web数据库：
          3、应用程序缓存机制：
        */
        webSettings.setDomStorageEnabled(true); // 开启 DOM storage API 功能
        webSettings.setDatabaseEnabled(true);   //开启 database storage API 功能
        webSettings.setAppCacheEnabled(true);//开启 Application Caches 功能

//        String cacheDirPath = getFilesDir().getAbsolutePath() + APP_CACAHE_DIRNAME;
//        webSettings.setAppCachePath(cacheDirPath); //设置  Application Caches 缓存目录

        /*
          Cache-Control介绍
           相对值，单位是秒，指定某个文件从发出请求开始起的有效时长，
           在这个有效时长之内，浏览器直接使用缓存，而不发送请求。
           Cache-Control 不用要求服务器与客户端的时间同步，也不用服务器时刻同步修改配置 Expired 中的绝对时间，
           从而可以避免额外的网络请求。优先级比 Expires 更高。
         */

    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //销毁Webview
        //在关闭了Activity时，如果Webview的音乐或视频，还在播放。就必须销毁Webview
        //但是注意：webview调用destory时,webview仍绑定在Activity上
        //这是由于自定义webview构建时传入了该Activity的context对象
        //因此需要先从父容器中移除webview,然后再销毁webview:

        mWebView.clearCache(false);//清楚缓存，false，表示不清楚sd上面的缓存
        mWebView.destroy();
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }


    /**
     * 加载assert目录的本地网页数据
     *
     * @param view
     */
    public void loadAssert(View view) {
       mWebView.loadUrl("file://android_asset/test.htm");
    }

    /**
     * 加载html data
     *
     * @param view
     */
    public void loadHtml(View view) {
        final String mimeType = "text/html";
        mWebView.loadData("<a href='x'>Hello World! - 1</a>", mimeType, null);
    }

    /**
     * 加载url
     *
     * @param view
     */
    public void loadUrl(View view) {
        mWebView.loadUrl("http://www.baidu.com");
    }
}
