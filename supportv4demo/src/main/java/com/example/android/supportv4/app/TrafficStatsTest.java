package com.example.android.supportv4.app;

import android.app.Activity;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;

import com.example.android.supportv4.R;

import java.lang.ref.WeakReference;

public class TrafficStatsTest extends Activity {

    private long mlastTotalStats;

    private static class MyHandler extends Handler {
        private WeakReference<TrafficStatsTest> weakActivity;

        public MyHandler(TrafficStatsTest activity) {
            super();
            weakActivity = new WeakReference<TrafficStatsTest>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            TrafficStatsTest test = weakActivity.get();
            if (test != null) {
                test.updateUi();
            }
        }
    }

    private MyHandler mHandler = new MyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traffic);
        setWifi();
    }

    public void updateUi() {

    }

    private void setWifi() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                StringBuilder sb = new StringBuilder();
                //获取开机起手机接收流量，不包含WiFi流量
                long mobileRx = TrafficStats.getMobileRxBytes();
                //获取开机起手机发送流量，不包含WiFi流量
                long mobileTx = TrafficStats.getMobileTxBytes();

                //开机起，所有接收发送留量
                long totalRx = TrafficStats.getTotalRxBytes();
                long totalTx = TrafficStats.getTotalTxBytes();
                long totalStats = totalRx + totalTx;

                //获取某个进程app的流量.通过Process.myUid()获取当前进程的uid;获取指定包名的uid:getPackageManager().getApplicationInfo().uid
                long appRx = TrafficStats.getUidRxBytes(Process.myUid());

                Message msg = mHandler.obtainMessage();
                msg.what = 1;
                if (mlastTotalStats != 0) {
                    long current = totalStats - mlastTotalStats;
                    sb.append("curent====" + current);
                    msg.obj = sb.toString();
                }
                mHandler.sendMessage(msg);
            }
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler = null;
    }
}
