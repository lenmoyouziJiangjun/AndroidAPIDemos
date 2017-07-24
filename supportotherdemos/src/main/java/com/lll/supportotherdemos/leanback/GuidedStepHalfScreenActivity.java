package com.lll.supportotherdemos.leanback;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;

import com.lll.supportotherdemos.R;

import java.util.List;

public class GuidedStepHalfScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_framelayout);
        GuidedStepFragment.addAsRoot(this, new FirstStepFragment(), R.id.fl_container);
    }

    public static class FirstStepFragment extends GuidedStepFragment {
        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.guidedstep_first_title);
            String breadcrumb = getString(R.string.guidedstep_first_breadcrumb);
            String description = getString(R.string.guidedstep_first_description);
            Drawable icon = getActivity().getResources().getDrawable(R.drawable.ic_main_icon);
            return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            Context context = getActivity();
            actions.add(new GuidedAction.Builder(context)
                    .clickAction(GuidedAction.ACTION_ID_CONTINUE)
                    .description("Just do it")
                    .build());
            actions.add(new GuidedAction.Builder(context)
                    .clickAction(GuidedAction.ACTION_ID_CANCEL)
                    .description("Never mind")
                    .build());
        }

        public FirstStepFragment() {
            setEntranceTransitionType(GuidedStepFragment.SLIDE_FROM_BOTTOM);
        }

        /**
         * This fragment could be used by an activity using theme
         * {@code Theme.Leanback.GuidedStep.Half} or something else (BrowseActivity).
         * In order to provide a consistent half screen experience under
         * both scenarios, we override onProvideTheme method.
         */
        @Override
        public int onProvideTheme() {
            return R.style.Theme_Example_Leanback_GuidedStep_Half;
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            FragmentManager fm = getFragmentManager();
            if (action.getId() == GuidedAction.ACTION_ID_CONTINUE) {
                GuidedStepFragment.add(fm, new SecondStepFragment(), R.id.lb_guidedstep_host);
            } else if (action.getId() == GuidedAction.ACTION_ID_CANCEL) {
                finishGuidedStepFragments();
            }
        }
    }

    public static class SecondStepFragment extends GuidedStepFragment {
        @Override
        public int onProvideTheme() {
            return R.style.Theme_Example_Leanback_GuidedStep_Half;
        }

        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.guidedstep_second_title);
            String breadcrumb = getString(R.string.guidedstep_second_breadcrumb);
            String description = getString(R.string.guidedstep_second_description);
            Drawable icon = getActivity().getResources().getDrawable(R.drawable.ic_main_icon);
            return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            Context context = getActivity();
            actions.add(new GuidedAction.Builder(context)
                    .clickAction(GuidedAction.ACTION_ID_FINISH)
                    .description("Done")
                    .build());
            actions.add(new GuidedAction.Builder(context)
                    .clickAction(GuidedAction.ACTION_ID_CANCEL)
                    .description("Never mind")
                    .build());
        }

        @Override
        public void onCreateButtonActions(List<GuidedAction> actions, Bundle savedInstanceState) {
            actions.add(new GuidedAction.Builder(getActivity())
                    .clickAction(GuidedAction.ACTION_ID_CANCEL)
                    .description("Cancel")
                    .build());
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            FragmentManager fm = getFragmentManager();
            fm.popBackStack();
        }
    }
}
