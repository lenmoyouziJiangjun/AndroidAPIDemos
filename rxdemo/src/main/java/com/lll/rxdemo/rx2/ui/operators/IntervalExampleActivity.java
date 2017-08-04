package com.lll.rxdemo.rx2.ui.operators;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.lll.rxdemo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedules.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Version 1.0
 * Created by lll on 17/8/4.
 * Description
 * copyright generalray4239@gmail.com
 */
public class IntervalExampleActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    private BannerImageAdapter mAdapter;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);
        mViewPager = (ViewPager) findViewById(R.id.vp_banner);
        initViewPager();
        doInterval();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    private void doInterval() {
        mDisposable.add(getObservable()
                //// Run on a background thread
                .subscribeOn(Schedulers.io())
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(getObserver()));
    }

    private Observable<? extends Long> getObservable() {
        return Observable.interval(3, 3, TimeUnit.SECONDS);
    }

    private DisposableObserver<Long> getObserver() {
        return new DisposableObserver<Long>() {
            @Override
            public void onNext(@NonNull Long aLong) {
                int currentIndex = mViewPager.getCurrentItem();
                if (++currentIndex == mAdapter.getCount()) {
                    mViewPager.setCurrentItem(0, true);
                } else {
                    mViewPager.setCurrentItem(currentIndex, true);
                }
            }

            @Override
            public void onError(@NonNull Throwable e) {
                Log.e("lll", "--------", e);
            }

            @Override
            public void onComplete() {
                Log.e("lll", "--------+onComplete");
            }
        };
    }


    private void initViewPager() {
        mAdapter = new BannerImageAdapter();
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(0);
    }

    private static class BannerImageAdapter extends PagerAdapter {

        private List<Integer> bitmapIds = new ArrayList<>();

        public BannerImageAdapter() {
            bitmapIds.add(R.drawable.timg);
            bitmapIds.add(R.drawable.timg1);
            bitmapIds.add(R.drawable.timg2);
            bitmapIds.add(R.drawable.timg3);
            bitmapIds.add(R.drawable.timg4);
            bitmapIds.add(R.drawable.timg5);
            bitmapIds.add(R.drawable.timg6);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(container.getContext());
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imageView.setLayoutParams(params);
            imageView.setImageResource(bitmapIds.get(position));
            container.addView(imageView);
            return new ViewHolder(imageView, position);
        }

        @Override
        public int getCount() {
            return bitmapIds.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((ViewHolder) object).view == view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(((ViewHolder) object).view);
        }
    }

    private static class ViewHolder {
        final ImageView view;
        final int position;

        public ViewHolder(ImageView view, int position) {
            this.view = view;
            this.position = position;
        }
    }


}
