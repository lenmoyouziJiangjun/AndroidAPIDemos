package com.lll.supportotherdemos.leanback.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v17.leanback.media.PlaybackControlGlue;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.SparseArrayObjectAdapter;
import android.util.Log;

import com.lll.supportotherdemos.R;
import com.lll.supportotherdemos.leanback.PlaybackOverlayActivity;
import com.lll.supportotherdemos.leanback.helper.PlaybackControlHelper;

public class PlaybackOverlayFragment extends android.support.v17.leanback.app.PlaybackOverlayFragment implements PlaybackOverlayActivity.PictureInPictureListener {
    private static final String TAG = "leanback.PlaybackControlsFragment";

    /**
     * Change this to choose a different overlay background.
     */
    private static final int BACKGROUND_TYPE = PlaybackOverlayFragment.BG_LIGHT;

    /**
     * Change the number of related content rows.
     */
    private static final int RELATED_CONTENT_ROWS = 3;

    /**
     * Change this to select hidden
     */
    private static final boolean SECONDARY_HIDDEN = false;

    private static final int ROW_CONTROLS = 0;

    private PlaybackControlHelper mGlue;
    private PlaybackControlsRowPresenter mPlaybackControlsRowPresenter;
    private ListRowPresenter mListRowPresenter;

    private OnItemViewClickedListener mOnItemViewClickedListener = new OnItemViewClickedListener() {
        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
            if (item instanceof Action) {
                mGlue.onActionClicked((Action) item);
            }
        }
    };

    private OnItemViewSelectedListener mOnItemViewSelectedListener = new OnItemViewSelectedListener() {
        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {
            Log.i("lll", "onItemSelected: " + item + " row " + row);
        }
    };

    public SparseArrayObjectAdapter getAdapter() {
        return (SparseArrayObjectAdapter) super.getAdapter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setBackgroundType(BACKGROUND_TYPE);
        setOnItemViewClickedListener(mOnItemViewClickedListener);
        createComponents(getActivity());
    }

    private void createComponents(Context context) {
        mGlue = new PlaybackControlHelper(context, this) {
            @Override
            public int getUpdatePeriod() {
                int totalTime = getControlsRow().getTotalTime();
                if (getView() == null || getView().getWidth() == 0 || totalTime <= 0) {
                    return 1000;
                }
                return Math.max(16, totalTime / getView().getWidth());
            }

            @Override
            protected void onRowChanged(PlaybackControlsRow row) {
                if (getAdapter() == null) {
                    return;
                }
                int index = getAdapter().indexOf(row);
                if (index >= 0) {
                    getAdapter().notifyArrayItemRangeChanged(index, 1);
                }
            }

            @Override
            public void onActionClicked(Action action) {
                if (action.getId() == R.id.lb_control_picture_in_picture) {
                    getActivity().enterPictureInPictureMode();
                    return;
                }
                super.onActionClicked(action);
            }
        };

        mPlaybackControlsRowPresenter = mGlue.getControlsRowPresenter();
        mPlaybackControlsRowPresenter.setSecondaryActionsHidden(SECONDARY_HIDDEN);
        mListRowPresenter = new ListRowPresenter();

        setAdapter(new SparseArrayObjectAdapter(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object item) {
                if (item instanceof PlaybackControlsRow) {
                    return mPlaybackControlsRowPresenter;
                } else if (item instanceof ListRow) {
                    return mListRowPresenter;
                }
                throw new IllegalArgumentException("Unhandled object: " + item);
            }
        }));
        // Add the controls row
        getAdapter().set(ROW_CONTROLS, mGlue.getControlsRow());
    }

    @Override
    public void onStart() {
        super.onStart();
        mGlue.setFadingEnabled(true);
        mGlue.enableProgressUpdating(mGlue.hasValidMedia() && mGlue.isMediaPlaying());
        ((PlaybackOverlayActivity) getActivity()).registerPictureInPictureListener(this);
    }

    @Override
    public void onStop() {
        mGlue.enableProgressUpdating(false);
        ((PlaybackOverlayActivity) getActivity()).unregisterPictureInPictureListener(this);
        super.onStop();
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        if (isInPictureInPictureMode) {
            // Hide the controls in picture-in-picture mode.
            setFadingEnabled(true);
            fadeOut();
        } else {
            setFadingEnabled(mGlue.isMediaPlaying());
        }
    }
}
