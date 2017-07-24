package com.lll.supportotherdemos.leanback.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.Nullable;
import android.support.v17.leanback.app.OnboardingFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.lll.supportotherdemos.R;

import java.util.ArrayList;

public class OnboardingDemoFragment extends OnboardingFragment {

    private static final long ANIMATION_DURATION = 1000;

    private static final int[] CONTENT_BACKGROUNDS = {
            R.drawable.tv_bg,
            R.drawable.gallery_photo_6,
            R.drawable.gallery_photo_8
    };

    private static final int[] CONTENT_ANIMATIONS = {
            R.drawable.tv_content,
            android.R.drawable.stat_sys_download,
            android.R.drawable.ic_popup_sync
    };

    private String[] mTitles;
    private String[] mDescriptions;

    private View mBackgroundView;
    private View mContentView;
    private ImageView mContentBackgroundView;
    private ImageView mContentAnimationView;

    private Animator mContentAnimator;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mTitles = getResources().getStringArray(R.array.onboarding_page_titles);
        mDescriptions = getResources().getStringArray(R.array.onboarding_page_descriptions);
        setLogoResourceId(R.drawable.ic_launcher);
    }

    @Override
    protected int getPageCount() {
        return mTitles.length;
    }

    @Override
    protected CharSequence getPageTitle(int i) {
        return mTitles[i];
    }

    @Override
    protected CharSequence getPageDescription(int i) {
        return mDescriptions[i];
    }

    @Nullable
    @Override
    protected View onCreateBackgroundView(LayoutInflater inflater, ViewGroup container) {
        mBackgroundView = inflater.inflate(R.layout.onboarding_image, container, false);
        return mBackgroundView;
    }

    @Nullable
    @Override
    protected View onCreateContentView(LayoutInflater inflater, ViewGroup container) {
        mContentView = inflater.inflate(R.layout.onboarding_content, container, false);
        mContentBackgroundView = (ImageView) mContentView.findViewById(R.id.background_image);
        mContentAnimationView = (ImageView) mContentView.findViewById(R.id.animation_image);
        return mContentView;
    }

    @Nullable
    @Override
    protected View onCreateForegroundView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    @Nullable
    @Override
    protected Animator onCreateEnterAnimation() {
        ArrayList<Animator> animators = new ArrayList<>();
        animators.add(createFadeInAnimator(mBackgroundView));

        mContentBackgroundView.setImageResource(CONTENT_BACKGROUNDS[0]);

        mContentAnimationView.setImageResource(CONTENT_ANIMATIONS[0]);

        mContentAnimator = createFadeInAnimator(mContentView);
        animators.add(mContentAnimator);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ((AnimationDrawable) mContentAnimationView.getDrawable()).start();
            }
        });
        return set;
    }

    @Override
    protected void onPageChanged(final int newPage, int previousPage) {
        if (mContentAnimator != null) {
            mContentAnimator.cancel();
        }
        ((AnimationDrawable) mContentAnimationView.getDrawable()).stop();

        Animator fadeOut = createFadeOutAnimator(mContentView);
        fadeOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mContentBackgroundView.setImageResource(CONTENT_BACKGROUNDS[newPage]);
                mContentAnimationView.setImageResource(CONTENT_ANIMATIONS[newPage]);
            }
        });

        Animator fadeIn = createFadeInAnimator(mContentView);
        fadeIn.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ((AnimationDrawable) mContentAnimationView.getDrawable()).start();
            }
        });
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(fadeOut, fadeIn);
        set.start();
        mContentAnimator = set;

    }

    private Animator createFadeInAnimator(View view) {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0.0f, 1.0f).setDuration(ANIMATION_DURATION);
    }

    private Animator createFadeOutAnimator(View view) {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 1.0f, 0.0f).setDuration(ANIMATION_DURATION);
    }

    @Override
    protected void onFinishFragment() {
        getActivity().finish();
    }
}
