package com.lll.mediademo.basicmediarouter;

import android.app.Activity;
import android.app.MediaRouteActionProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaRouter;
import android.os.Bundle;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lll.mediademo.R;

/**
 * Version 1.0
 * Created by lll on 17/6/7.
 * Description
 * copyright generalray4239@gmail.com
 */
public class BasicMediaRouterActivity extends Activity {

    private MediaRouter mMediaRouter;

    private SamplePresentation mPresentation;

    private TextView mTextView;
    private Button mButton;
    private int mColor = 0;
    public int[] mColors;


    /**
     * Implementing a {@link android.media.MediaRouter.Callback} to update the displayed
     * {@link android.app.Presentation} when a route is selected, unselected or the
     * presentation display has changed. The provided stub implementation
     * {@link android.media.MediaRouter.SimpleCallback} is extended and only
     * {@link android.media.MediaRouter.SimpleCallback#onRouteSelected(android.media.MediaRouter, int, android.media.MediaRouter.RouteInfo)}
     * ,
     * {@link android.media.MediaRouter.SimpleCallback#onRouteUnselected(android.media.MediaRouter, int, android.media.MediaRouter.RouteInfo)}
     * and
     * {@link android.media.MediaRouter.SimpleCallback#onRoutePresentationDisplayChanged(android.media.MediaRouter, android.media.MediaRouter.RouteInfo)}
     * are overridden to update the displayed {@link android.app.Presentation} in
     * {@link #updatePresentation()}. These callbacks enable or disable the
     * second screen presentation based on the routing provided by the
     * {@link android.media.MediaRouter} for {@link android.media.MediaRouter#ROUTE_TYPE_LIVE_VIDEO}
     * streams. @
     */
    private final MediaRouter.SimpleCallback mMediaRouterCallBack = new MediaRouter.SimpleCallback() {
        @Override
        public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            updatePresentation();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
            updatePresentation();
        }

        @Override
        public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
            updatePresentation();
        }
    };

    /**
     * Listens for dismissal of the {@link SamplePresentation} and removes its
     * reference.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (dialog == mPresentation) {
                        mPresentation = null;
                    }
                }
            };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_router);
        mTextView = (TextView) findViewById(R.id.textStatus);
        mColors = getResources().getIntArray(R.array.androidcolors);
        mButton = (Button) findViewById(R.id.button1);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        mMediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
    }

    /**
     * Updates the displayed presentation to enable a secondary screen if it has
     * been selected in the {@link android.media.MediaRouter} for the
     * {@link android.media.MediaRouter#ROUTE_TYPE_LIVE_VIDEO} type. If no screen has been
     * selected by the {@link android.media.MediaRouter}, the current screen is disabled.
     * Otherwise a new {@link SamplePresentation} is initialized and shown on
     * the secondary screen.
     */
    private void updatePresentation() {
        MediaRouter.RouteInfo selectedRoute = mMediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_AUDIO);
        // Get its Display if a valid route has been selected
        Display selectedDiaplay = null;
        if (selectedRoute != null) {
            selectedDiaplay = selectedRoute.getPresentationDisplay();
        }

        /*
         * Dismiss the current presentation if the display has changed or no new
         * route has been selected
         */
        if (mPresentation != null && mPresentation.getDisplay() != selectedDiaplay) {
            mPresentation.dismiss();
            mPresentation = null;
            mButton.setEnabled(false);
            mTextView.setText(R.string.secondary_notconnected);
        }

        if (mPresentation == null && selectedDiaplay != null) {
            // Initialise a new Presentation for the Display
            mPresentation = new SamplePresentation(this, selectedDiaplay);
            mPresentation.setOnDismissListener(mOnDismissListener);

            // Try to show the presentation, this might fail if the display has
            // gone away in the mean time
            try {
                mPresentation.show();
                mTextView.setText(getResources().getString(R.string.secondary_connected,
                        selectedRoute.getName(BasicMediaRouterActivity.this)));
                mButton.setEnabled(true);
                showNextColor();
            } catch (Exception e) {
                e.printStackTrace();
                mPresentation = null;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallBack);

        mButton.setEnabled(false);
        mTextView.setText(R.string.secondary_notconnected);
        updatePresentation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMediaRouter.removeCallback(mMediaRouterCallBack);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_media_router, menu);

        // BEGIN_INCLUDE(MediaRouteActionProvider)
        // Configure the media router action provider
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.menu_media_route);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) mediaRouteMenuItem.getActionProvider();
        mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        // BEGIN_INCLUDE(MediaRouteActionProvider)

        return true;
    }

    /**
     * Displays the next color on the secondary screen if it is activate.
     */
    private void showNextColor() {
        if (mPresentation != null) {
            // a second screen is active and initialized, show the next color
            mPresentation.setColor(mColors[mColor]);
            mColor = (mColor + 1) % mColors.length;
        }
    }
}
