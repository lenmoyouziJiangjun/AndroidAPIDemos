/**
 * Copyright (c) 2016, The Android Open Source Project
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.apis.content;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.example.android.apis.R;

import java.util.List;

/**
 * Example stub job to monitor when there is a change to any media: content URI.
 */


public class MediaContentJob extends JobService {
    static final Uri MEDIA_URI = Uri.parse("content://" + MediaStore.AUTHORITY + "/");

    final Handler mHandler = new Handler();
    final Runnable mWorker = new Runnable() {
        @Override
        public void run() {
            scheduleJob(MediaContentJob.this);
            jobFinished(mRunningParams, false);
        }
    };

    JobParameters mRunningParams;


    /**
     * 开启服务
     * @param context
     */
    public static void scheduleJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        JobInfo.Builder builder = new JobInfo.Builder(JobIds.MEDIA_CONTENT_JOB,
                new ComponentName(context, MediaContentJob.class));
        builder.addTriggerContentUri(new JobInfo.TriggerContentUri(MEDIA_URI,
                JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));

//        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE); //设置需要的网络条件，默认NETWORK_TYPE_NONE
//        builder.setPeriodic(3000);//设置间隔时间
//
//        builder.setMinimumLatency(3000);// 设置任务运行最少延迟时间
//
//        builder.setOverrideDeadline(50000);// 设置deadline，若到期还没有达到规定的条件则会开始执行
//
//        builder.setRequiresCharging(true);// 设置是否充电的条件,默认false
//
//        builder.setRequiresDeviceIdle(false);// 设置手机是否空闲的条件,默认false
//
//        builder.setPersisted(true);//设备重启之后你的任务是否还要继续执行
        js.schedule(builder.build());
        Log.i("MediaContentJob", "JOB SCHEDULED!");
    }

    public static boolean isScheduled(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        List<JobInfo> jobs = js.getAllPendingJobs();
        if (jobs == null) {
            return false;
        }
        for (int i = 0; i < jobs.size(); i++) {
            if (jobs.get(i).getId() == JobIds.MEDIA_CONTENT_JOB) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param context
     */
    public static void cancelJob(Context context) {
        JobScheduler js = context.getSystemService(JobScheduler.class);
        js.cancel(JobIds.MEDIA_CONTENT_JOB);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i("MediaContentJob", "JOB STARTED!");
        mRunningParams = params;
        StringBuilder sb = new StringBuilder();
        sb.append("Media content has changed:\n");
        if (params.getTriggeredContentAuthorities() != null) {
            sb.append("Authorities: ");
            boolean first = true;
            for (String auth : params.getTriggeredContentAuthorities()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append(auth);
            }
            if (params.getTriggeredContentUris() != null) {
                for (Uri uri : params.getTriggeredContentUris()) {
                    sb.append("\n");
                    sb.append(uri);
                }
            }
        } else {
            sb.append("(No content)");
        }
        Toast.makeText(this, sb.toString(), Toast.LENGTH_LONG).show();
        // We will emulate taking some time to do this work, so we can see batching happen.
        mHandler.postDelayed(mWorker, 10 * 1000);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mHandler.removeCallbacks(mWorker);
        return false;
    }
}
