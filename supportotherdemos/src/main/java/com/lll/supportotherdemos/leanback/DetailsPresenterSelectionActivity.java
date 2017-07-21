package com.lll.supportotherdemos.leanback;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.GuidedStepFragment;
import android.support.v17.leanback.widget.GuidanceStylist;
import android.support.v17.leanback.widget.GuidedAction;

import com.lll.supportotherdemos.R;

import java.util.List;

public class DetailsPresenterSelectionActivity extends Activity {
    private static final int OPTION_CHECK_SET_ID = 10;

    private static final long ACTION_ID_SWITCH_LEGACY_ON = 10000;
    private static final long ACTION_ID_SWITCH_LEGACY_OFF = 10001;

    public static boolean USE_LEGACY_PRESENTER = false;

    private static final String[] OPTION_NAMES = {"Use new details presenter", "Use legacy details presenter"};
    private static final String[] OPTION_DESCRIPTIONS = {"Use new details presenter",
            "Use legacy details presenter"};
    private static final long[] OPTION_IDS = {ACTION_ID_SWITCH_LEGACY_OFF, ACTION_ID_SWITCH_LEGACY_ON};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GuidedStepFragment.addAsRoot(this, new SetupFragment(), android.R.id.content);
    }

    private static void addAction(List<GuidedAction> actions, long id, String title, String desc) {
        actions.add(new GuidedAction.Builder()
                .id(id)
                .title(title)
                .description(desc)
                .build());
    }

    private static void addCheckedAction(List<GuidedAction> actions, Context context,
                                         long id, String title, String desc, boolean checked) {
        actions.add(new GuidedAction.Builder()
                .title(title)
                .description(desc)
                .id(id)
                .checkSetId(OPTION_CHECK_SET_ID)
                .checked(checked)
                .build());
    }

    public static class SetupFragment extends GuidedStepFragment {
        @NonNull
        @Override
        public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
            String title = getString(R.string.guidedstep_second_title);
            String breadcrumb = getString(R.string.guidedstep_second_breadcrumb);
            String description = getString(R.string.guidedstep_second_description);
            Drawable icon = getActivity().getDrawable(R.drawable.ic_main_icon);
            return new GuidanceStylist.Guidance(title, description, breadcrumb, icon);
        }

        @Override
        public GuidanceStylist onCreateGuidanceStylist() {
            return new GuidanceStylist() {
                @Override
                public int onProvideLayoutId() {
                    return R.layout.guidedstep_second_guidance;
                }
            };
        }

        @Override
        public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
            for (int i = 0; i < OPTION_NAMES.length; i++) {
                boolean checked = false;
                if (OPTION_IDS[i] == ACTION_ID_SWITCH_LEGACY_ON) {
                    if (USE_LEGACY_PRESENTER) {
                        checked = true;
                    }
                } else if (OPTION_IDS[i] == ACTION_ID_SWITCH_LEGACY_OFF) {
                    if (!USE_LEGACY_PRESENTER) {
                        checked = true;
                    }
                }
                addCheckedAction(actions, getActivity(), OPTION_IDS[i], OPTION_NAMES[i],
                        OPTION_DESCRIPTIONS[i], checked);
            }
        }

        @Override
        public void onGuidedActionClicked(GuidedAction action) {
            if (action.getId() == ACTION_ID_SWITCH_LEGACY_ON) {
                USE_LEGACY_PRESENTER = action.isChecked();
            } else if (action.getId() == ACTION_ID_SWITCH_LEGACY_OFF) {
                USE_LEGACY_PRESENTER = !action.isChecked();
            }
            getActivity().finish();
        }
    }


}
