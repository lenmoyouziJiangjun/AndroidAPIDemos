package android.support.v7.recycleview.widget;

import android.content.Context;
import android.database.Observable;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.os.ParcelableCompat;
import android.support.v4.os.ParcelableCompatCreatorCallbacks;
import android.support.v4.os.TraceCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.ListView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Version 1.0
 * Created by lll on 17/7/5.
 * Description 学习recycleView 小框架的设计模式，原理。
 * copyright generalray4239@gmail.com
 */
public class RecyclerView extends ViewGroup implements ScrollingView, NestedScrollingChild {

    static final String TAG = "RecyclerView";

    static final boolean DEBUG = false;

    private static final int[] NESTED_SCROLLING_ATTRS
            = {16843830 /* android.R.attr.nestedScrollingEnabled */};

    private static final int[] CLIP_TO_PADDING_ATTR = {android.R.attr.clipToPadding};

    /**
     * On Kitkat and JB MR2, there is a bug which prevents DisplayList from being invalidated if
     * a View is two levels deep(wrt to ViewHolder.itemView). DisplayList can be invalidated by
     * setting View's visibility to INVISIBLE when View is detached. On Kitkat and JB MR2, Recycler
     * recursively traverses itemView and invalidates display list for each ViewGroup that matches
     * this criteria.
     */
    static final boolean FORCE_INVALIDATE_DISPLAY_LIST = Build.VERSION.SDK_INT == 18
            || Build.VERSION.SDK_INT == 19 || Build.VERSION.SDK_INT == 20;
    /**
     * On M+, an unspecified measure spec may include a hint which we can use. On older platforms,
     * this value might be garbage. To save LayoutManagers from it, RecyclerView sets the size to
     * 0 when mode is unspecified.
     */
    static final boolean ALLOW_SIZE_IN_UNSPECIFIED_SPEC = Build.VERSION.SDK_INT >= 23;

    static final boolean POST_UPDATES_ON_ANIMATION = Build.VERSION.SDK_INT >= 16;

    /**
     * On L+, with RenderThread, the UI thread has idle time after it has passed a frame off to
     * RenderThread but before the next frame begins. We schedule prefetch work in this window.
     */
    private static final boolean ALLOW_PREFETCHING = Build.VERSION.SDK_INT >= 21;

    static final boolean DISPATCH_TEMP_DETACH = false;

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public static final int NO_POSITION = -1;
    public static final long NO_ID = -1;
    public static final int INVALID_TYPE = -1;

    /**
     * Constant for use with {@link #setScrollingTouchSlop(int)}. Indicates
     * that the RecyclerView should use the standard touch slop for smooth,
     * continuous scrolling.
     */
    public static final int TOUCH_SLOP_DEFAULT = 0;

    /**
     * Constant for use with {@link #setScrollingTouchSlop(int)}. Indicates
     * that the RecyclerView should use the standard touch slop for scrolling
     * widgets that snap to a page or other coarse-grained barrier.
     */
    public static final int TOUCH_SLOP_PAGING = 1;

    static final int MAX_SCROLL_DURATION = 2000;

    /**
     * RecyclerView is calculating a scroll.
     * If there are too many of these in Systrace, some Views inside RecyclerView might be causing
     * it. Try to avoid using EditText, focusable views or handle them with care.
     */
    static final String TRACE_SCROLL_TAG = "RV Scroll";

    /**
     * OnLayout has been called by the View system.
     * If this shows up too many times in Systrace, make sure the children of RecyclerView do not
     * update themselves directly. This will cause a full re-layout but when it happens via the
     * Adapter notifyItemChanged, RecyclerView can avoid full layout calculation.
     */
    private static final String TRACE_ON_LAYOUT_TAG = "RV OnLayout";

    /**
     * NotifyDataSetChanged or equal has been called.
     * If this is taking a long time, try sending granular notify adapter changes instead of just
     * calling notifyDataSetChanged or setAdapter / swapAdapter. Adding stable ids to your adapter
     * might help.
     */
    private static final String TRACE_ON_DATA_SET_CHANGE_LAYOUT_TAG = "RV FullInvalidate";

    /**
     * RecyclerView is doing a layout for partial adapter updates (we know what has changed)
     * If this is taking a long time, you may have dispatched too many Adapter updates causing too
     * many Views being rebind. Make sure all are necessary and also prefer using notify*Range
     * methods.
     */
    private static final String TRACE_HANDLE_ADAPTER_UPDATES_TAG = "RV PartialInvalidate";

    /**
     * RecyclerView is rebinding a View.
     * If this is taking a lot of time, consider optimizing your layout or make sure you are not
     * doing extra operations in onBindViewHolder call.
     */
    static final String TRACE_BIND_VIEW_TAG = "RV OnBindView";

    /**
     * RecyclerView is attempting to pre-populate off screen views.
     */
    private static final String TRACE_PREFETCH_TAG = "RV Prefetch";

    /**
     * RecyclerView is creating a new View.
     * If too many of these present in Systrace:
     * - There might be a problem in Recycling (e.g. custom Animations that set transient state and
     * prevent recycling or ItemAnimator not implementing the contract properly. ({@link
     * > Adapter#onFailedToRecycleView(ViewHolder)})
     * <p>
     * - There might be too many item view types.
     * > Try merging them
     * <p>
     * - There might be too many itemChange animations and not enough space in RecyclerPool.
     * >Try increasing your pool size and item cache size.
     */
    static final String TRACE_CREATE_VIEW_TAG = "RV CreateView";

    private static final Class<?>[] LAYOUT_MANAGER_CONSTRUCTOR_SIGNATURE =
            new Class[]{Context.class, AttributeSet.class, int.class, int.class};

    private final RecyclerViewDataObserver mObserver = new RecyclerViewDataObserver();

    final Recycler mRecycler = new Recycler();

    private SavedState mPendingSavedState;

    /**
     * Handles adapter updates
     */
    AdapterHelper mAdapterHelper;

    /**
     * Handles abstraction between LayoutManager children and RecyclerView children
     */
    ChildHelper mChildHelper;

    /**
     * Keeps data about views to be used for animations
     */
    final ViewInfoStore mViewInfoStore = new ViewInfoStore();

    /**
     * Prior to L, there is no way to query this variable which is why we override the setter and
     * track it here.
     */
    boolean mClipToPadding;

    final Rect mTempRect = new Rect();
    private final Rect mTemRect2 = new Rect();
    final RectF mTempRectF = new RectF();
    Adapter mAdapter;
    @VisibleForTesting
    LayoutManager mLayout;
    RecyclerListener mRecyclerListener;

    final ArrayList<ItemDecoration> mItemDecorations = new ArrayList<>();

    private final ArrayList<OnItemTouchListener> mOnItemTouchListeners = new ArrayList<>();

    private OnItemTouchListener mActiveOnItemTouchListener;
    boolean mIsAttached;
    boolean mHasFixedSize;
    @VisibleForTesting
    boolean mFirstLayoutComplete;




    /**
     * Note: this Runnable is only ever posted if:
     * 1) We've been through first layout
     * 2) We know we have a fixed size (mHasFixedSize)
     * 3) We're attached
     */
    final Runnable mUpdateChildViewsRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mFirstLayoutComplete || isLayoutRequested()) {
                // a layout request will happen, we should not do layout here.
                return;
            }
            if (!mIsAttached) {
                requestLayout();
                // if we are not attached yet, mark us as requiring layout and skip
                return;
            }
            if (mLayoutFrozen) {
                mLayoutRequestEaten = true;
                return;
            }
            consumePendingUpdateOperations();
        }
    };

    final State mState = new State();

    public RecyclerView(Context context) {
        super(context);
    }

    public RecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }

    /**
     * Returns whether RecyclerView is currently computing a layout.
     * <p>
     * If this method returns true, it means that RecyclerView is in a lockdown state and any
     * attempt to update adapter contents will result in an exception because adapter contents
     * cannot be changed while RecyclerView is trying to compute the layout.
     * <p>
     * It is very unlikely that your code will be running during this state as it is
     * called by the framework when a layout traversal happens or RecyclerView starts to scroll
     * in response to system events (touch, accessibility etc).
     * <p>
     * This case may happen if you have some custom logic to change adapter contents in
     * response to a View callback (e.g. focus change callback) which might be triggered during a
     * layout calculation. In these cases, you should just postpone the change using a Handler or a
     * similar mechanism.
     *
     * @return <code>true</code> if RecyclerView is currently computing a layout, <code>false</code>
     * otherwise
     */
    public boolean isComputingLayout() {
        return mLayoutOrScrollCounter > 0;
    }

    /**
     * Checks if RecyclerView is in the middle of a layout or scroll and throws an
     * {@link IllegalStateException} if it <b>is</b>.
     *
     * @param message The message for the exception. Can be null.
     */
    void assertNotInLayoutOrScroll(String message) {
        if (isComputingLayout()) {
            if (message == null) {
                throw new IllegalStateException("Cannot call this method while RecyclerView is "
                        + "computing a layout or scrolling");
            }
            throw new IllegalStateException(message);
        }
        if (mDispatchScrollCounter > 0) {

        }
    }

    @VisibleForTesting
    boolean setChildImportantForAccessibilityInternal(ViewHolder viewHolder, int importantForAccessibility) {
        if (isComputingLayout()) {
            viewHolder.mPendingAccessibilityState = importantForAccessibility;
            mPendingAccessibilityImportanceChange.add(viewHolder);
            return false;
        }
        ViewCompat.setImportantForAccessibility(viewHolder.itemView, importantForAccessibility);
        return true;
    }

    void dispatchPendingImportantForAccessibilityChanges() {
        for (int i = mPendingAccessibilityImportanceChange.size() - 1; i >= 0; i--) {
            ViewHolder viewHolder = mPendingAccessibilityImportanceChange.get(i);
            if (viewHolder.itemView.getParent() != this || viewHolder.shouldIgnore()) {
                return;
            }
            int state = viewHolder.mPendingAccessibilityState;
            if (state != ViewHolder.PENDING_ACCESSIBILITY_STATE_NOT_SET) {
                //noinspection WrongConstant
                ViewCompat.setImportantForAccessibility(viewHolder.itemView, state);
                viewHolder.mPendingAccessibilityState =
                        ViewHolder.PENDING_ACCESSIBILITY_STATE_NOT_SET;
            }
        }
        mPendingAccessibilityImportanceChange.clear();
    }

    int getAdapterPositionFor(ViewHolder viewHolder) {
        if (viewHolder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID |
                ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN)
                || !viewHolder.isBound()) {
            return RecyclerView.NO_POSITION;
        }
        return mAdapterHelper.applyPendingUpdatesToPosition(viewHolder.mPosition);
    }

    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mScrollingChildHelper == null) {
            mScrollingChildHelper = new NestedScrollingChildHelper(this);
        }
        return mScrollingChildHelper;
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getScrollingChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getScrollingChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getScrollingChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }

    /**
     * A Recycler is responsible for managing scrapped or detached item views for reuse.
     * <p>
     * <p>A "scrapped" view is a view that is still attached to its parent RecyclerView but
     * that has been marked for removal or reuse.</p>
     * <p>
     * <p>Typical use of a Recycler by a {@link RecyclerView.LayoutManager} will be to obtain views for
     * an adapter's data set representing the data at a given position or item ID.
     * If the view to be reused is considered "dirty" the adapter will be asked to rebind it.
     * If not, the view can be quickly reused by the LayoutManager with no further work.
     * Clean views that have not {@link android.view.View#isLayoutRequested() requested layout}
     * may be repositioned by a LayoutManager without remeasurement.</p>
     */
    public final class Recycler {
        final ArrayList<ViewHolder> mAttachedScrap = new ArrayList<>();
        ArrayList<ViewHolder> mChangedScrap = null;

        final ArrayList<ViewHolder> mCachedViews = new ArrayList<>();

        private final List<ViewHolder> mUnmodifiableAttachedScrap = Collections.unmodifiableList(mAttachedScrap);

        private int mRequestedCacheMax = DEFAULT_CACHE_SIZE;
        int mViewCacheMax = DEFAULT_CACHE_SIZE;
        private RecycledViewPool mRecyclerPool;

        private ViewCacheExtension mViewCacheExtension;

        static final int DEFAULT_CACHE_SIZE = 2;

        /**
         * Clear scrap views out of this recycler. Detached views contained within a
         * recycled view pool will remain.
         */
        public void clear() {
            mAttachedScrap.clear();
            recycleAndClearCachedViews();
        }

        /**
         * Set the maximum number of detached, valid views we should retain for later use.
         *
         * @param viewCount Number of views to keep before sending views to the shared pool
         */
        public void setViewCacheSize(int viewCount) {
            mRequestedCacheMax = viewCount;
            updateViewCacheSize();
        }

        void updateViewCacheSize() {
            int extraCache = 0;
            if (mLayout != null && ALLOW_PREFETCHING) {
                extraCache = mLayout.isItemPrefetchEnabled() ? mLayout.getItemPrefetchCount() : 0;
            }
            mViewCacheMax = mRequestedCacheMax + extraCache;
            // first, try the views that can be recycled
            for (int i = mCachedViews.size() - 1;
                 i >= 0 && mCachedViews.size() > mViewCacheMax; i--) {
                recycleCachedViewAt(i);
            }
        }

        /**
         * Returns an unmodifiable list of ViewHolders that are currently in the scrap list.
         *
         * @return List of ViewHolders in the scrap list.
         */
        public List<ViewHolder> getScrapList() {
            return mUnmodifiableAttachedScrap;
        }

        /**
         * Helper method for getViewForPosition.
         * <p>
         * Checks whether a given view holder can be used for the provided position.
         *
         * @param holder ViewHolder
         * @return true if ViewHolder matches the provided position, false otherwise
         */
        boolean validateViewHolderForOffsetPosition(ViewHolder holder) {
            // if it is a removed holder, nothing to verify since we cannot ask adapter anymore
            // if it is not removed, verify the type and id.
            if (holder.isRemoved()) {
                return mState.isPreLayout();
            }
            if (holder.mPosition < 0 || holder.mPosition >= mAdapter.getItemCount()) {
                throw new IndexOutOfBoundsException("Inconsistency detected. Invalid view holder "
                        + "adapter position" + holder);
            }
            if (!mState.isPreLayout()) {
                // don't check type if it is pre-layout.
                final int type = mAdapter.getItemViewType(holder.mPosition);
                if (type != holder.getItemViewType()) {
                    return false;
                }
            }
            if (mAdapter.hasStableIds()) {
                return holder.getItemId() == mAdapter.getItemId(holder.mPosition);
            }
            return true;
        }

        /**
         * Binds the given View to the position. The View can be a View previously retrieved via
         * {@link #getViewForPosition(int)} or created by
         * {@link Adapter#onCreateViewHolder(ViewGroup, int)}.
         * <p>
         * Generally, a LayoutManager should acquire its views via {@link #getViewForPosition(int)}
         * and let the RecyclerView handle caching. This is a helper method for LayoutManager who
         * wants to handle its own recycling logic.
         * <p>
         * Note that, {@link #getViewForPosition(int)} already binds the View to the position so
         * you don't need to call this method unless you want to bind this View to another position.
         *
         * @param view     The view to update.
         * @param position The position of the item to bind to this View.
         */
        public void bindViewToPosition(View view, int position) {
            ViewHolder viewHolder = getChildViewHolderInt(view);
            if (viewHolder == null) {
                throw new IllegalArgumentException("The view does not have a ViewHolder. You cannot"
                        + " pass arbitrary views to this method, they should be created by the "
                        + "Adapter");
            }
            final int offsetPosition = mAdapterHelper.findPositionOffset(position);
            if (offsetPosition < 0 || offsetPosition >= mAdapter.getItemCount()) {
                throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item "
                        + "position " + position + "(offset:" + offsetPosition + ")."
                        + "state:" + mState.getItemCount());
            }
            viewHolder.mOwnerRecyclerView = RecyclerView.this;
            mAdapter.bindViewHolder(viewHolder, offsetPosition);
            attachAccessibilityDelegate(view);
            if (mState.isPreLayout()) {
                viewHolder.mPreLayoutPosition = position;
            }
            final ViewGroup.LayoutParams lp = viewHolder.itemView.getLayoutParams();
            final LayoutParams rvLayoutParams;
            if (lp == null) {
                rvLayoutParams = (LayoutParams) generateDefaultLayoutParams();
                viewHolder.itemView.setLayoutParams(rvLayoutParams);
            } else if (!checkLayoutParams(lp)) {
                rvLayoutParams = (LayoutParams) generateLayoutParams(lp);
                viewHolder.itemView.setLayoutParams(rvLayoutParams);
            } else {
                rvLayoutParams = (LayoutParams) lp;
            }
            rvLayoutParams.mInsetsDirty = true;
            rvLayoutParams.mViewHolder = viewHolder;
            rvLayoutParams.mPendingInvalidate = viewHolder.itemView.getParent() == null;
        }

        /**
         * RecyclerView provides artificial position range (item count) in pre-layout state and
         * automatically maps these positions to {@link Adapter} positions when
         * {@link #getViewForPosition(int)} or {@link #bindViewToPosition(View, int)} is called.
         * <p>
         * Usually, LayoutManager does not need to worry about this. However, in some cases, your
         * LayoutManager may need to call some custom component with item positions in which
         * case you need the actual adapter position instead of the pre layout position. You
         * can use this method to convert a pre-layout position to adapter (post layout) position.
         * <p>
         * Note that if the provided position belongs to a deleted ViewHolder, this method will
         * return -1.
         * <p>
         * Calling this method in post-layout state returns the same value back.
         *
         * @param position The pre-layout position to convert. Must be greater or equal to 0 and
         *                 less than {@link State#getItemCount()}.
         */
        public int converPreLayoutPositionToPostLayout(int position) {
            if (position < 0 || position >= mState.getItemCount()) {
                throw new IndexOutOfBoundsException("invalid position " + position + ". State "
                        + "item count is " + mState.getItemCount());
            }
            if (!mState.isPreLayout()) {
                return position;
            }
            return mAdapterHelper.findPositionOffset(position);
        }

        /**
         * Obtain a view initialized for the given position.
         * <p>
         * This method should be used by {@link LayoutManager} implementations to obtain
         * views to represent data from an {@link Adapter}.
         * <p>
         * The Recycler may reuse a scrap or detached view from a shared pool if one is
         * available for the correct view type. If the adapter has not indicated that the
         * data at the given position has changed, the Recycler will attempt to hand back
         * a scrap view that was previously initialized for that data without rebinding.
         *
         * @param position Position to obtain a view for
         * @return A view representing the data at <code>position</code> from <code>adapter</code>
         */
        public View getViewForPosition(int position) {
            return getViewForPosition(position, false);
        }

        View getViewForPosition(int position, boolean dryRun) {
            if (position < 0 || position >= mState.getItemCount()) {
                throw new IndexOutOfBoundsException("Invalid item position " + position
                        + "(" + position + "). Item count:" + mState.getItemCount());
            }
            boolean fromScrap = false;
            ViewHolder holder = null;
            // 0) If there is a changed scrap, try to find from there
            if (mState.isPreLayout()) {
                holder = getChangedScrapViewForPosition(position);
                fromScrap = holder != null;
            }
            // 1) Find from scrap by position
            if (holder == null) {
                holder = getScrapViewForPosition(position, INVALID_TYPE, dryRun);
                if (holder != null) {
                    if (!validateViewHolderForOffsetPosition(holder)) {
                        // recycle this scrap
                        if (!dryRun) {
                            // we would like to recycle this but need to make sure it is not used by
                            // animation logic etc.
                            holder.addFlags(ViewHolder.FLAG_INVALID);
                            if (holder.isScrap()) {
                                removeDetachedView(holder.itemView, false);
                                holder.unScrap();
                            } else if (holder.wasReturnedFromScrap()) {
                                holder.clearReturnedFromScrapFlag();
                            }
                            recycleViewHolderInternal(holder);
                        }
                        holder = null;
                    } else {
                        fromScrap = true;
                    }
                }
            }
            if (holder == null) {
                final int offsetPosition = mAdapterHelper.findPositionOffset(position);
                if (offsetPosition < 0 || offsetPosition >= mAdapter.getItemCount()) {
                    throw new IndexOutOfBoundsException("Inconsistency detected. Invalid item "
                            + "position " + position + "(offset:" + offsetPosition + ")."
                            + "state:" + mState.getItemCount());
                }
                final int type = mAdapter.getItemViewType(offsetPosition);
                // 2) Find from scrap via stable ids, if exists
                if (mAdapter.hasStableIds()) {
                    holder = getScrapViewForId(mAdapter.getItemId(offsetPosition), type, dryRun);
                    if (holder != null) {
                        // update position
                        holder.mPosition = offsetPosition;
                        fromScrap = true;
                    }
                }
                if (holder == null && mViewCacheExtension != null) {
                    // We are NOT sending the offsetPosition because LayoutManager does not
                    // know it.
                    final View view = mViewCacheExtension
                            .getViewForPositionAndType(this, position, type);
                    if (view != null) {
                        holder = getChildViewHolder(view);
                        if (holder == null) {
                            throw new IllegalArgumentException("getViewForPositionAndType returned"
                                    + " a view which does not have a ViewHolder");
                        } else if (holder.shouldIgnore()) {
                            throw new IllegalArgumentException("getViewForPositionAndType returned"
                                    + " a view that is ignored. You must call stopIgnoring before"
                                    + " returning this view.");
                        }
                    }
                }
                if (holder == null) {//fallback to recycler
                    // try recycler.
                    // Head to the shared pool.

                    holder = getRecycledViewPool().getRecycledView(type);
                    if (holder != null) {
                        holder.resetInternal();
                        if (FORCE_INVALIDATE_DISPLAY_LIST) {
                            invalidateDisplayListInt(holder);
                        }
                    }
                }
                if (holder == null) {
                    holder = mAdapter.createViewHolder(RecyclerView.this, type);
                }
            }

            // This is very ugly but the only place we can grab this information
            // before the View is rebound and returned to the LayoutManager for post layout ops.
            // We don't need this in pre-layout since the VH is not updated by the LM.
            if (fromScrap && !mState.isPreLayout() && holder.hasAnyOfTheFlags(ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST)) {
                holder.setFlags(0, ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
                if (mState.mRunSimpleAnimations) {
                    int changeFlags = ItemAnimator.buildAdapterChangeFlagsForAnimations(holder);
                    changeFlags |= ItemAnimator.FLAG_APPEARED_IN_PRE_LAYOUT;
                    final ItemAnimator.ItemHolderInfo info = mItemAnimator.recordPreLayoutInformation(mState,
                            holder, changeFlags, holder.getUnmodifiedPayloads());
                    recordAnimationInfoIfBouncedHiddenView(holder, info);
                }
            }
            boolean bound = false;
            if (mState.isPreLayout() && holder.isBound()) {
                // do not update unless we absolutely have to.
                holder.mPreLayoutPosition = position;
            } else if (!holder.isBound() || holder.needsUpdate() || holder.isInvalid()) {

                final int offsetPosition = mAdapterHelper.findPositionOffset(position);
                holder.mOwnerRecyclerView = RecyclerView.this;
                mAdapter.bindViewHolder(holder, offsetPosition);
                attachAccessibilityDelegate(holder.itemView);
                bound = true;
                if (mState.isPreLayout()) {
                    holder.mPreLayoutPosition = position;
                }
            }
            final ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            final LayoutParams rvLayoutParams;
            if (lp == null) {
                rvLayoutParams = (LayoutParams) generateDefaultLayoutParams();
                holder.itemView.setLayoutParams(rvLayoutParams);
            } else if (!checkLayoutParams(lp)) {
                rvLayoutParams = (LayoutParams) generateLayoutParams(lp);
                holder.itemView.setLayoutParams(rvLayoutParams);
            } else {
                rvLayoutParams = (LayoutParams) lp;
            }
            rvLayoutParams.mViewHolder = holder;
            rvLayoutParams.mPendingInvalidate = fromScrap && bound;
            return holder.itemView;
        }

        private void attachAccessibilityDelegate(View itemView) {
            if (isAccessibilityEnabled()) {
                if (ViewCompat.getImportantForAccessibility(itemView) ==
                        ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                    ViewCompat.setImportantForAccessibility(itemView,
                            ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
                }
                if (!ViewCompat.hasAccessibilityDelegate(itemView)) {
                    ViewCompat.setAccessibilityDelegate(itemView,
                            mAccessibilityDelegate.getItemDelegate());
                }
            }
        }

        private void invalidateDisplayListInt(ViewHolder holder) {
            if (holder.itemView instanceof ViewGroup) {
                invalidateDisplayListInt(holder.itemView, false);
            }
        }

        private void invalidateDisplayListInt(ViewGroup viewGroup, boolean invalidateThis) {
            for (int i = viewGroup.getChildCount() - 1; i >= 0; i++) {
                final View view = viewGroup.getChildAt(i);
                if (view instanceof ViewGroup) {
                    invalidateDisplayListInt((ViewGroup) view, true);
                }
            }
            if (!invalidateThis) {
                return;
            }
            // we need to force it to become invisible
            if (viewGroup.getVisibility() == View.INVISIBLE) {
                viewGroup.setVisibility(View.VISIBLE);
                viewGroup.setVisibility(View.INVISIBLE);
            } else {
                final int visibility = viewGroup.getVisibility();
                viewGroup.setVisibility(View.INVISIBLE);
                viewGroup.setVisibility(visibility);
            }
        }

        /**
         * Recycle a detached view. The specified view will be added to a pool of views
         * for later rebinding and reuse.
         * <p>
         * <p>A view must be fully detached (removed from parent) before it may be recycled. If the
         * View is scrapped, it will be removed from scrap list.</p>
         *
         * @param view Removed view for recycling
         * @see LayoutManager#removeAndRecycleView(View, Recycler)
         */
        public void recycleView(View view) {
            // This public recycle method tries to make view recycle-able since layout manager
            // intended to recycle this view (e.g. even if it is in scrap or change cache)
            ViewHolder viewHolder = getChildViewHolderInt(view);
            if (viewHolder.isTmpDetached()) {
                removeDetachedView(view, false);
            }
            if (viewHolder.isScrap()) {
                viewHolder.unScrap();
            } else if (viewHolder.wasReturnedFromScrap()) {
                viewHolder.clearReturnedFromScrapFlag();
            }
            recycleViewHolderInternal(viewHolder);
        }

        /**
         * Internally, use this method instead of {@link #recycleView(android.view.View)} to
         * catch potential bugs.
         *
         * @param view
         */
        void recycleViewInternal(View view) {
            recycleViewHolderInternal(getChildViewHolderInt(view));
        }

        void recycleAndClearCachedViews() {
            final int count = mCachedViews.size();
            for (int i = count - 1; i >= 0; i++) {
                recycleCachedViewAt(i);
            }
            mCachedViews.clear();
            if (ALLOW_PREFETCHING) {
                mViewPrefetcher.clearPrefetchPositions();
            }
        }

        /**
         * Recycles a cached view and removes the view from the list. Views are added to cache
         * if and only if they are recyclable, so this method does not check it again.
         * <p>
         * A small exception to this rule is when the view does not have an animator reference
         * but transient state is true (due to animations created outside ItemAnimator). In that
         * case, adapter may choose to recycle it. From RecyclerView's perspective, the view is
         * still recyclable since Adapter wants to do so.
         *
         * @param cachedViewIndex The index of the view in cached views list
         */
        void recycleCachedViewAt(int cachedViewIndex) {

            ViewHolder viewHolder = mCachedViews.get(cachedViewIndex);

            addViewHolderToRecycledViewPool(viewHolder);
            mCachedViews.remove(cachedViewIndex);
        }

        /**
         * internal implementation checks if view is scrapped or attached and throws an exception
         * if so.
         * Public version un-scraps before calling recycle.
         */
        void recycleViewHolderInternal(ViewHolder holder) {
            if (holder.isScrap() || holder.itemView.getParent() != null) {
                throw new IllegalArgumentException(
                        "Scrapped or attached views may not be recycled. isScrap:"
                                + holder.isScrap() + " isAttached:"
                                + (holder.itemView.getParent() != null));
            }
            if (holder.isTmpDetached()) {
                throw new IllegalArgumentException("Tmp detached view should be removed "
                        + "from RecyclerView before it can be recycled: " + holder);
            }

            if (holder.shouldIgnore()) {
                throw new IllegalArgumentException("Trying to recycle an ignored view holder. You"
                        + " should first call stopIgnoringView(view) before calling recycle.");
            }
            final boolean transientStatePreventsRecycling = holder.doesTransientStatePreventRecycling();
            final boolean forceRecycle = mAdapter != null
                    && transientStatePreventsRecycling
                    && mAdapter.onFailedToRecycleView(holder);
            boolean cached = false;
            boolean recycled = false;

            if (forceRecycle || holder.isRecyclable()) {
                if (mViewCacheMax > 0 && !holder.hasAnyOfTheFlags(ViewHolder.FLAG_INVALID
                        | ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_UPDATE)) {
                    //retire oldest cached view;
                    int cachedViewSize = mCachedViews.size();
                    if (cachedViewSize >= mViewCacheMax && cachedViewSize > 0) {
                        recycleCachedViewAt(0);
                        cachedViewSize--;
                    }
                    int targetCacheIndex = cachedViewSize;
                    if (ALLOW_PREFETCHING
                            && cachedViewSize > 0
                            && !mViewPrefetcher.lastPrefetchIncludedPosition(holder.mPosition)) {
                        // when adding the view, skip past most recently prefetched views
                        int cacheIndex = cachedViewSize - 1;
                        while (cacheIndex >= 0) {
                            int cachedPos = mCachedViews.get(cacheIndex).mPosition;
                            if (!mViewPrefetcher.lastPrefetchIncludedPosition(cachedPos)) {
                                break;
                            }
                            cacheIndex--;
                        }
                        targetCacheIndex = cacheIndex + 1;
                    }
                    mCachedViews.add(targetCacheIndex, holder);
                    cached = true;
                }
                if (!cached) {
                    addViewHolderToRecycledViewPool(holder);
                    recycled = true;
                }
            }
            mViewInfoStore.removeViewHolder(holder);
            if (!cached && !recycled && transientStatePreventsRecycling) {
                holder.mOwnerRecyclerView = null;
            }
        }

        void addViewHolderToRecycledViewPool(ViewHolder holder) {
            ViewCompat.setAccessibilityDelegate(holder.itemView, null);
            dispatchViewRecycled(holder);
            holder.mOwnerRecyclerView = null;
            getRecycledViewPool().putRecycledView(holder);
        }

        /**
         * Used as a fast path for unscrapping and recycling a view during a bulk operation.
         * The caller must call {@link #clearScrap()} when it's done to update the recycler's
         * internal bookkeeping.
         */
        void quickRecycleScrapView(View view) {
            final ViewHolder holder = getChildViewHolderInt(view);
            holder.mScrapContainer = null;
            holder.mInChangeScrap = false;
            holder.clearReturnedFromScrapFlag();
            recycleViewHolderInternal(holder);
        }

        /**
         * Mark an attached view as scrap.
         * <p>
         * <p>"Scrap" views are still attached to their parent RecyclerView but are eligible
         * for rebinding and reuse. Requests for a view for a given position may return a
         * reused or rebound scrap view instance.</p>
         *
         * @param view View to scrap
         */
        void scrapView(View view) {
            final ViewHolder holder = getChildViewHolderInt(view);
            if (holder.hasAnyOfTheFlags(ViewHolder.FLAG_REMOVED | ViewHolder.FLAG_INVALID)
                    || !holder.isUpdated() || canReuseUpdatedViewHolder(holder)) {
                if (holder.isInvalid() && !holder.isRemoved() && !mAdapter.hasStableIds()) {
                    throw new IllegalArgumentException("Called scrap view with an invalid view."
                            + " Invalid views cannot be reused from scrap, they should rebound from"
                            + " recycler pool.");
                }
                holder.setScrapContainer(this, false);
                mAttachedScrap.add(holder);
            } else {
                if (mChangedScrap == null) {
                    mChangedScrap = new ArrayList<>();
                }
                holder.setScrapContainer(this, true);
                mChangedScrap.add(holder);
            }
        }

        /**
         * Remove a previously scrapped view from the pool of eligible scrap.
         * <p>
         * <p>This view will no longer be eligible for reuse until re-scrapped or
         * until it is explicitly removed and recycled.</p>
         */
        void unscrapView(ViewHolder holder) {
            if (holder.mInChangeScrap) {
                mChangedScrap.remove(holder);
            } else {
                mAttachedScrap.remove(holder);
            }
            holder.mScrapContainer = null;
            holder.mInChangeScrap = false;
            holder.clearReturnedFromScrapFlag();
        }

        int getScrapCount() {
            return mAttachedScrap.size();
        }

        View getScrapViewAt(int index) {
            return mAttachedScrap.get(index).itemView;
        }

        void clearScrap() {
            mAttachedScrap.clear();
            if (mChangedScrap != null) {
                mChangedScrap.clear();
            }
        }


        void dispatchViewRecycled(ViewHolder holder) {
            if (mRecyclerListener != null) {
                mRecyclerListener.onViewRecycled(holder);
            }
            if (mAdapter != null) {
                mAdapter.onViewRecycled(holder);
            }
            if (mState != null) {
                mViewInfoStore.removeViewHolder(holder);
            }
        }

        void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter,
                              boolean compatibleWithPrevious) {
            clear();
            getRecycledViewPool().onAdapterChanged(oldAdapter, newAdapter, compatibleWithPrevious);
        }

        ViewHolder getChangedScrapViewForPosition(int position) {
            // If pre-layout, check the changed scrap for an exact match.
            final int changedScrapSize;
            if (mChangedScrap == null || (changedScrapSize = mChangedScrap.size()) == 0) {
                return null;
            }
            // find by position
            for (int i = 0; i < changedScrapSize; i++) {
                final ViewHolder holder = mChangedScrap.get(i);
                if (!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position) {
                    holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                    return holder;
                }
            }
            //find by id
            if (mAdapter.hasStableIds()) {
                final int offsetPosition = mAdapterHelper.findPositionOffset(position);
                if (offsetPosition > 0 && offsetPosition < mAdapter.getItemCount()) {
                    final long id = mAdapter.getItemId(offsetPosition);
                    for (int i = 0; i < changedScrapSize; i++) {
                        final ViewHolder holder = mChangedScrap.get(i);
                        if (!holder.wasReturnedFromScrap() && holder.getItemId() == id) {
                            holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                            return holder;
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Returns a scrap view for the position. If type is not INVALID_TYPE, it also checks if
         * ViewHolder's type matches the provided type.
         *
         * @param position Item position
         * @param type     View type
         * @param dryRun   Does a dry run, finds the ViewHolder but does not remove
         * @return a ViewHolder that can be re-used for this position.
         */
        ViewHolder getScrapViewForPosition(int position, int type, boolean dryRun) {
            final int scrapCount = mAttachedScrap.size();
            for (int i = 0; i < scrapCount; i++) {
                final ViewHolder holder = mAttachedScrap.get(i);
                if (!holder.wasReturnedFromScrap() && holder.getLayoutPosition() == position
                        && !holder.isInvalid() && (mState.mInPreLayout || !holder.isRemoved())) {
                    if (type != INVALID_TYPE && holder.getItemViewType() != type) {
                        break;
                    }
                    holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                    return holder;
                }
            }
            if (!dryRun) {
                View view = mChildHelper.findHiddenNonRemovedView(position, type);
                if (view != null) {
                    // This View is good to be used. We just need to unhide, detach and move to the
                    // scrap list.
                    final ViewHolder viewHolder = getChildViewHolderInt(view);
                    mChildHelper.unhide(view);
                    int layoutIndex = mChildHelper.indexOfChild(view);
                    if (layoutIndex == RecyclerView.NO_POSITION) {
                        throw new IllegalStateException("layout index should not be -1 after "
                                + "unhiding a view:" + viewHolder);
                    }
                    mChildHelper.detachViewFromParent(layoutIndex);
                    scrapView(view);
                    viewHolder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP
                            | ViewHolder.FLAG_BOUNCED_FROM_HIDDEN_LIST);
                    return viewHolder;
                }
            }

            //Search in our first-level recycled view cache.
            final int cacheSize = mCachedViews.size();
            for (int i = 0; i < cacheSize; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                // invalid view holders may be in cache if adapter has stable ids as they can be
                // retrieved via getScrapViewForId
                if (!holder.isInvalid() && holder.getLayoutPosition() == position) {
                    if (!dryRun) {
                        mCachedViews.remove(i);
                    }
                    return holder;
                }
            }
            return null;
        }

        ViewHolder getScrapViewForId(long id, int type, boolean dryRun) {
            //look in our attached views first;
            final int count = mAttachedScrap.size();
            for (int i = count - 1; i >= 0; i++) {
                final ViewHolder holder = mAttachedScrap.get(i);
                if (holder.getItemId() == id && !holder.wasReturnedFromScrap()) {
                    if (type == holder.getItemViewType()) {
                        holder.addFlags(ViewHolder.FLAG_RETURNED_FROM_SCRAP);
                        if (holder.isRemoved()) {
                            // this might be valid in two cases:
                            // > item is removed but we are in pre-layout pass
                            // >> do nothing. return as is. make sure we don't rebind
                            // > item is removed then added to another position and we are in
                            // post layout.
                            // >> remove removed and invalid flags, add update flag to rebind
                            // because item was invisible to us and we don't know what happened in
                            // between.
                            if (!mState.isPreLayout()) {
                                holder.setFlags(ViewHolder.FLAG_UPDATE, ViewHolder.FLAG_UPDATE |
                                        ViewHolder.FLAG_INVALID | ViewHolder.FLAG_REMOVED);
                            }
                        }
                        return holder;
                    } else if (!dryRun) {
                        // if we are running animations, it is actually better to keep it in scrap
                        // but this would force layout manager to lay it out which would be bad.
                        // Recycle this scrap. Type mismatch.
                        mAttachedScrap.remove(i);
                        removeDetachedView(holder.itemView, false);
                        quickRecycleScrapView(holder.itemView);
                    }
                }
            }
            //Search the first-level cache
            final int cacheSize = mCachedViews.size();
            for (int i = cacheSize - 1; i >= 0; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder.getItemId() == id) {
                    if (type == holder.getItemViewType()) {
                        if (!dryRun) {
                            mCachedViews.remove(i);
                        }
                        return holder;
                    } else if (!dryRun) {
                        recycleCachedViewAt(i);
                    }
                }
            }
            return null;
        }

        void offsetPositionRecordsForMove(int from, int to) {
            final int start, end, inBetweenOffset;
            if (from < to) {
                start = from;
                end = to;
                inBetweenOffset = -1;
            } else {
                start = to;
                end = from;
                inBetweenOffset = 1;
            }
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder == null || holder.mPosition < start || holder.mPosition > end) {
                    continue;
                }
                if (holder.mPosition == from) {
                    holder.offsetPosition(to - from, false);
                } else {
                    holder.offsetPosition(inBetweenOffset, false);
                }

            }
        }

        void offsetPositionRecordsForInsert(int insertedAt, int count) {
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder != null && holder.mPosition >= insertedAt) {

                    holder.offsetPosition(count, true);
                }
            }
        }

        /**
         * @param removedFrom      Remove start index
         * @param count            Remove count
         * @param applyToPreLayout If true, changes will affect ViewHolder's pre-layout position, if
         *                         false, they'll be applied before the second layout pass
         */
        void offsetPositionRecordsForRemove(int removedFrom, int count, boolean applyToPreLayout) {
            final int removedEnd = removedFrom + count;
            final int cachedCount = mCachedViews.size();
            for (int i = cachedCount - 1; i >= 0; i--) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder != null) {
                    if (holder.mPosition >= removedEnd) {

                        holder.offsetPosition(-count, applyToPreLayout);
                    } else if (holder.mPosition >= removedFrom) {
                        // Item for this view was removed. Dump it from the cache.
                        holder.addFlags(ViewHolder.FLAG_REMOVED);
                        recycleCachedViewAt(i);
                    }
                }
            }
        }

        void setViewCacheExtension(ViewCacheExtension extension) {
            mViewCacheExtension = extension;
        }

        void setRecycledViewPool(RecycledViewPool pool) {
            if (mRecyclerPool != null) {
                mRecyclerPool.detach();
            }
            mRecyclerPool = pool;
            if (pool != null) {
                mRecyclerPool.attach(getAdapter());
            }
        }

        RecycledViewPool getRecycledViewPool() {
            if (mRecyclerPool == null) {
                mRecyclerPool = new RecycledViewPool();
            }
            return mRecyclerPool;
        }

        void viewRangeUpdate(int positionStart, int itemCount) {
            final int positionEnd = positionStart + itemCount;
            final int cachedCount = mCachedViews.size();
            for (int i = cachedCount - 1; i >= 0; i--) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder == null) {
                    continue;
                }

                final int pos = holder.getLayoutPosition();
                if (pos >= positionStart && pos < positionEnd) {
                    holder.addFlags(ViewHolder.FLAG_UPDATE);
                    recycleCachedViewAt(i);
                    // cached views should not be flagged as changed because this will cause them
                    // to animate when they are returned from cache.
                }
            }
        }

        void setAdapterPositionsAsUnknown() {
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                if (holder != null) {
                    holder.addFlags(ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN);
                }
            }
        }

        void markKnownViewsInvalid() {
            if (mAdapter != null && mAdapter.hasStableIds()) {
                final int cachedCount = mCachedViews.size();
                for (int i = 0; i < cachedCount; i++) {
                    final ViewHolder holder = mCachedViews.get(i);
                    if (holder != null) {
                        holder.addFlags(ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID);
                        holder.addChangePayload(null);
                    }
                }
            } else {
                // we cannot re-use cached views in this case. Recycle them all
                recycleAndClearCachedViews();
            }
        }

        void clearOldPositions() {
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                holder.clearOldPosition();
            }
            final int scrapCount = mAttachedScrap.size();
            for (int i = 0; i < scrapCount; i++) {
                mAttachedScrap.get(i).clearOldPosition();
            }
            if (mChangedScrap != null) {
                final int changedScrapCount = mChangedScrap.size();
                for (int i = 0; i < changedScrapCount; i++) {
                    mChangedScrap.get(i).clearOldPosition();
                }
            }
        }

        void markItemDecorInsetsDirty() {
            final int cachedCount = mCachedViews.size();
            for (int i = 0; i < cachedCount; i++) {
                final ViewHolder holder = mCachedViews.get(i);
                LayoutParams layoutParams = (LayoutParams) holder.itemView.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.mInsetsDirty = true;
                }
            }
        }

        boolean isPrefetchPositionAttached(int position) {
            final int childCount = mChildHelper.getUnfilteredChildCount();
            for (int i = 0; i < childCount; i++) {
                View attachedView = mChildHelper.getUnfilteredChildAt(i);
                ViewHolder holder = getChildViewHolderInt(attachedView);
                // TODO: consider ignoring if holder isInvalid
                // Note: can use mPosition here because adapter doesn't have pending updates
                if (holder.mPosition == position) {
                    return true;
                }
            }
            return false;
        }

        void prefetch(int[] itemPrefetchArray, int viewCount) {
            if (viewCount == 0) return;

            int childPosition = itemPrefetchArray[viewCount - 1];
            if (childPosition < 0) {
                throw new IllegalArgumentException("Recycler requested to prefetch invalid view "
                        + childPosition);
            }

            View prefetchView = null;
            if (!isPrefetchPositionAttached(childPosition)) {
                // only prefetch if child not already attached
                prefetchView = getViewForPosition(childPosition);
            }
            if (viewCount > 1) {
                prefetch(itemPrefetchArray, viewCount - 1);
            }
            if (prefetchView != null) {
                recycleView(prefetchView);
            }
        }
    }

    /**
     * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
     * <p>{@link RecyclerView.Adapter} implementations should subclass ViewHolder and add fields for caching
     * potentially expensive {@link View#findViewById(int)} results.</p>
     * <p>
     * <p>While {@link RecyclerView.LayoutParams} belong to the {@link RecyclerView.LayoutManager},
     * {@link RecyclerView.ViewHolder ViewHolders} belong to the adapter. Adapters should feel free to use
     * their own custom ViewHolder implementations to store data that makes binding view contents
     * easier. Implementations should assume that individual item views will hold strong references
     * to <code>ViewHolder</code> objects and that <code>RecyclerView</code> instances may hold
     * strong references to extra off-screen item views for caching purposes</p>
     */
    public static abstract class ViewHolder {
        public final View itemView;
        int mPosition = NO_POSITION;
        int mOldPosition = NO_POSITION;
        long mItemId = NO_ID;
        int mItemViewType = INVALID_TYPE;
        int mPreLayoutPosition = NO_POSITION;

        // The item that this holder is shadowing during an item change event/animation
        ViewHolder mShadowedHolder = null;
        // The item that is shadowing this holder during an item change event/animation
        ViewHolder mShadowingHolder = null;

        /**
         * This ViewHolder has been bound to a position; mPosition, mItemId and mItemViewType
         * are all valid.
         */
        static final int FLAG_BOUND = 1 << 0;

        /**
         * The data this ViewHolder's view reflects is stale and needs to be rebound
         * by the adapter. mPosition and mItemId are consistent.
         */
        static final int FLAG_UPDATE = 1 << 1;

        /**
         * This ViewHolder's data is invalid. The identity implied by mPosition and mItemId
         * are not to be trusted and may no longer match the item view type.
         * This ViewHolder must be fully rebound to different data.
         */
        static final int FLAG_INVALID = 1 << 2;

        /**
         * This ViewHolder points at data that represents an item previously removed from the
         * data set. Its view may still be used for things like outgoing animations.
         */
        static final int FLAG_REMOVED = 1 << 3;

        /**
         * This ViewHolder should not be recycled. This flag is set via setIsRecyclable()
         * and is intended to keep views around during animations.
         */
        static final int FLAG_NOT_RECYCLABLE = 1 << 4;

        /**
         * This ViewHolder is returned from scrap which means we are expecting an addView call
         * for this itemView. When returned from scrap, ViewHolder stays in the scrap list until
         * the end of the layout pass and then recycled by RecyclerView if it is not added back to
         * the RecyclerView.
         */
        static final int FLAG_RETURNED_FROM_SCRAP = 1 << 5;

        /**
         * This ViewHolder is fully managed by the LayoutManager. We do not scrap, recycle or remove
         * it unless LayoutManager is replaced.
         * It is still fully visible to the LayoutManager.
         */
        static final int FLAG_IGNORE = 1 << 7;

        /**
         * When the View is detached form the parent, we set this flag so that we can take correct
         * action when we need to remove it or add it back.
         */
        static final int FLAG_TMP_DETACHED = 1 << 8;

        /**
         * Set when we can no longer determine the adapter position of this ViewHolder until it is
         * rebound to a new position. It is different than FLAG_INVALID because FLAG_INVALID is
         * set even when the type does not match. Also, FLAG_ADAPTER_POSITION_UNKNOWN is set as soon
         * as adapter notification arrives vs FLAG_INVALID is set lazily before layout is
         * re-calculated.
         */
        static final int FLAG_ADAPTER_POSITION_UNKNOWN = 1 << 9;

        /**
         * Set when a addChangePayload(null) is called
         */
        static final int FLAG_ADAPTER_FULLUPDATE = 1 << 10;

        /**
         * Used by ItemAnimator when a ViewHolder's position changes
         */
        static final int FLAG_MOVED = 1 << 11;

        /**
         * Used by ItemAnimator when a ViewHolder appears in pre-layout
         */
        static final int FLAG_APPEARED_IN_PRE_LAYOUT = 1 << 12;

        static final int PENDING_ACCESSIBILITY_STATE_NOT_SET = -1;

        /**
         * Used when a ViewHolder starts the layout pass as a hidden ViewHolder but is re-used from
         * hidden list (as if it was scrap) without being recycled in between.
         * <p>
         * When a ViewHolder is hidden, there are 2 paths it can be re-used:
         * a) Animation ends, view is recycled and used from the recycle pool.
         * b) LayoutManager asks for the View for that position while the ViewHolder is hidden.
         * <p>
         * This flag is used to represent "case b" where the ViewHolder is reused without being
         * recycled (thus "bounced" from the hidden list). This state requires special handling
         * because the ViewHolder must be added to pre layout maps for animations as if it was
         * already there.
         */
        static final int FLAG_BOUNCED_FROM_HIDDEN_LIST = 1 << 13;

        private int mFlags;

        private static final List<Object> FULLUPDATE_PAYLOADS = Collections.EMPTY_LIST;

        List<Object> mPayloads = null;
        List<Object> mUnmodifiedPayloads = null;

        private int mIsRecyclableCount = 0;

        // If non-null, view is currently considered scrap and may be reused for other data by the
        // scrap container.
        private Recycler mScrapContainer = null;
        // Keeps whether this ViewHolder lives in Change scrap or Attached scrap
        private boolean mInChangeScrap = false;

        // Saves isImportantForAccessibility value for the view item while it's in hidden state and
        // marked as unimportant for accessibility.
        private int mWasImportantForAccessibilityBeforeHidden =
                ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
        // set if we defer the accessibility state change of the view holder
        private int mPendingAccessibilityState = PENDING_ACCESSIBILITY_STATE_NOT_SET;

        /**
         * Is set when VH is bound from the adapter and cleaned right before it is sent to
         * {@link RecyclerView.RecycledViewPool}.
         */
        RecyclerView mOwnerRecyclerView;

        public ViewHolder(View itemView) {
            if (itemView == null) {
                throw new IllegalArgumentException("itemView may not be null");
            }
            this.itemView = itemView;
        }

        void flagRemovedAndOffsetPosition(int newPosition, int offset, boolean applyToPreLayout) {
            addFlags(ViewHolder.FLAG_REMOVED);
            offsetPosition(offset, applyToPreLayout);
            mPosition = newPosition;
        }


        void offsetPosition(int offset, boolean applyToPreLayout) {
            if (mOldPosition == NO_POSITION) {
                mOldPosition = mPosition;
            }

            if (mPreLayoutPosition == NO_POSITION) {
                mPreLayoutPosition = mPosition;
            }
            if (applyToPreLayout) {
                mPreLayoutPosition += offset;
            }
            mPosition += offset;
            if (itemView.getLayoutParams() != null) {
                ((LayoutParams) itemView.getLayoutParams()).mInsetsDirty = true;
            }
        }

        void clearOldPosition() {
            mOldPosition = NO_POSITION;
            mPreLayoutPosition = NO_POSITION;
        }

        void saveOldPosition() {
            if (mOldPosition == NO_POSITION) {
                mOldPosition = mPosition;
            }
        }

        boolean shouldIgnore() {
            return (mFlags & FLAG_IGNORE) != 0;
        }

        /**
         * @see #getLayoutPosition()
         * @see #getAdapterPosition()
         * @deprecated This method is deprecated because its meaning is ambiguous due to the async
         * handling of adapter updates. Please use {@link #getLayoutPosition()} or
         * {@link #getAdapterPosition()} depending on your use case.
         */
        @Deprecated
        public final int getPosition() {
            return mPreLayoutPosition == NO_POSITION ? mPosition : mPreLayoutPosition;
        }

        /**
         * Returns the position of the ViewHolder in terms of the latest layout pass.
         * <p>
         * This position is mostly used by RecyclerView components to be consistent while
         * RecyclerView lazily processes adapter updates.
         * <p>
         * For performance and animation reasons, RecyclerView batches all adapter updates until the
         * next layout pass. This may cause mismatches between the Adapter position of the item and
         * the position it had in the latest layout calculations.
         * <p>
         * LayoutManagers should always call this method while doing calculations based on item
         * positions. All methods in {@link RecyclerView.LayoutManager}, {@link RecyclerView.State},
         * {@link RecyclerView.Recycler} that receive a position expect it to be the layout position
         * of the item.
         * <p>
         * If LayoutManager needs to call an external method that requires the adapter position of
         * the item, it can use {@link #getAdapterPosition()} or
         * {@link RecyclerView.Recycler#convertPreLayoutPositionToPostLayout(int)}.
         *
         * @return Returns the adapter position of the ViewHolder in the latest layout pass.
         * @see #getAdapterPosition()
         */
        public final int getLayoutPosition() {
            return mPreLayoutPosition == NO_POSITION ? mPosition : mPreLayoutPosition;
        }

        /**
         * Returns the Adapter position of the item represented by this ViewHolder.
         * <p>
         * Note that this might be different than the {@link #getLayoutPosition()} if there are
         * pending adapter updates but a new layout pass has not happened yet.
         * <p>
         * RecyclerView does not handle any adapter updates until the next layout traversal. This
         * may create temporary inconsistencies between what user sees on the screen and what
         * adapter contents have. This inconsistency is not important since it will be less than
         * 16ms but it might be a problem if you want to use ViewHolder position to access the
         * adapter. Sometimes, you may need to get the exact adapter position to do
         * some actions in response to user events. In that case, you should use this method which
         * will calculate the Adapter position of the ViewHolder.
         * <p>
         * Note that if you've called {@link RecyclerView.Adapter#notifyDataSetChanged()}, until the
         * next layout pass, the return value of this method will be {@link #NO_POSITION}.
         *
         * @return The adapter position of the item if it still exists in the adapter.
         * {@link RecyclerView#NO_POSITION} if item has been removed from the adapter,
         * {@link RecyclerView.Adapter#notifyDataSetChanged()} has been called after the last
         * layout pass or the ViewHolder has already been recycled.
         */
        public final int getAdapterPosition() {
            if (mOwnerRecyclerView == null) {
                return NO_POSITION;
            }
            return mOwnerRecyclerView.getAdapterPositionFor(this);
        }

        /**
         * When LayoutManager supports animations, RecyclerView tracks 3 positions for ViewHolders
         * to perform animations.
         * <p>
         * If a ViewHolder was laid out in the previous onLayout call, old position will keep its
         * adapter index in the previous layout.
         *
         * @return The previous adapter index of the Item represented by this ViewHolder or
         * {@link #NO_POSITION} if old position does not exists or cleared (pre-layout is
         * complete).
         */
        public final int getOldPosition() {
            return mOldPosition;
        }

        /**
         * Returns The itemId represented by this ViewHolder.
         *
         * @return The item's id if adapter has stable ids, {@link RecyclerView#NO_ID}
         * otherwise
         */
        public final long getItemId() {
            return mItemId;
        }

        /**
         * @return The view type of this ViewHolder.
         */
        public final int getItemViewType() {
            return mItemViewType;
        }

        boolean isScrap() {
            return mScrapContainer != null;
        }

        void unScrap() {
            mScrapContainer.unscrapView(this);
        }

        boolean wasReturnedFromScrap() {
            return (mFlags & FLAG_RETURNED_FROM_SCRAP) != 0;
        }

        void clearReturnedFromScrapFlag() {
            mFlags = mFlags & ~FLAG_RETURNED_FROM_SCRAP;
        }

        void clearTmpDetachFlag() {
            mFlags = mFlags & ~FLAG_TMP_DETACHED;
        }

        void stopIgnoring() {
            mFlags = mFlags & ~FLAG_IGNORE;
        }

        void setScrapContainer(Recycler recycler, boolean isChangeScrap) {
            mScrapContainer = recycler;
            mInChangeScrap = isChangeScrap;
        }

        boolean isInvalid() {
            return (mFlags & FLAG_INVALID) != 0;
        }

        boolean needsUpdate() {
            return (mFlags & FLAG_UPDATE) != 0;
        }

        boolean isBound() {
            return (mFlags & FLAG_BOUND) != 0;
        }

        boolean isRemoved() {
            return (mFlags & FLAG_REMOVED) != 0;
        }

        boolean hasAnyOfTheFlags(int flags) {
            return (mFlags & flags) != 0;
        }

        boolean isTmpDetached() {
            return (mFlags & FLAG_TMP_DETACHED) != 0;
        }

        boolean isAdapterPositionUnknown() {
            return (mFlags & FLAG_ADAPTER_POSITION_UNKNOWN) != 0 || isInvalid();
        }

        void setFlags(int flags, int mask) {
            mFlags = (mFlags & ~mask) | (flags & mask);
        }

        void addFlags(int flags) {
            mFlags |= flags;
        }

        void addChangePayload(Object payload) {
            if (payload == null) {
                addFlags(FLAG_ADAPTER_FULLUPDATE);
            } else if ((mFlags & FLAG_ADAPTER_FULLUPDATE) == 0) {
                createPayloadsIfNeeded();
                mPayloads.add(payload);
            }
        }

        private void createPayloadsIfNeeded() {
            if (mPayloads == null) {
                mPayloads = new ArrayList<>();
                mUnmodifiedPayloads = Collections.unmodifiableList(mPayloads);
            }
        }

        void clearPayload() {
            if (mPayloads != null) {
                mPayloads.clear();
            }
            mFlags = mFlags & ~FLAG_ADAPTER_FULLUPDATE;
        }

        List<Object> getUnmodifiedPayloads() {
            if ((mFlags & FLAG_ADAPTER_FULLUPDATE) == 0) {
                if (mPayloads == null || mPayloads.size() == 0) {
                    // Initial state,  no update being called.
                    return FULLUPDATE_PAYLOADS;
                }
                // there are none-null payloads
                return mUnmodifiedPayloads;
            } else {
                // a full update has been called.
                return FULLUPDATE_PAYLOADS;
            }
        }

        void resetInternal() {
            mFlags = 0;
            mPosition = NO_POSITION;
            mOldPosition = NO_POSITION;
            mItemId = NO_ID;
            mPreLayoutPosition = NO_POSITION;
            mIsRecyclableCount = 0;
            mShadowedHolder = null;
            mShadowingHolder = null;
            clearPayload();
            mWasImportantForAccessibilityBeforeHidden = ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
            mPendingAccessibilityState = PENDING_ACCESSIBILITY_STATE_NOT_SET;
        }

        /**
         * Called when the child view enters the hidden state
         *
         * @param parent
         */
        private void onEnteredHiddenState(RecyclerView parent) {
            // While the view item is in hidden state, make it invisible for the accessibility.
            mWasImportantForAccessibilityBeforeHidden =
                    ViewCompat.getImportantForAccessibility(itemView);
            parent.setChildImportantForAccessibilityInternal(this,
                    ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }

        /**
         * Called when the child view leaves the hidden state
         *
         * @param parent
         */
        private void onLeftHiddenState(RecyclerView parent) {
            parent.setChildImportantForAccessibilityInternal(this,
                    mWasImportantForAccessibilityBeforeHidden);
            mWasImportantForAccessibilityBeforeHidden = ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ViewHolder{" +
                    Integer.toHexString(hashCode()) + " position=" + mPosition + " id=" + mItemId +
                    ", oldPos=" + mOldPosition + ", pLpos:" + mPreLayoutPosition);
            if (isScrap()) {
                sb.append(" scrap ")
                        .append(mInChangeScrap ? "[changeScrap]" : "[attachedScrap]");
            }
            if (isInvalid()) sb.append(" invalid");
            if (!isBound()) sb.append(" unbound");
            if (needsUpdate()) sb.append(" update");
            if (isRemoved()) sb.append(" removed");
            if (shouldIgnore()) sb.append(" ignored");
            if (isTmpDetached()) sb.append(" tmpDetached");
            if (!isRecyclable()) sb.append(" not recyclable(" + mIsRecyclableCount + ")");
            if (isAdapterPositionUnknown()) sb.append(" undefined adapter position");

            if (itemView.getParent() == null) sb.append(" no parent");
            sb.append("}");
            return sb.toString();
        }

        /**
         * Informs the recycler whether this item can be recycled. Views which are not
         * recyclable will not be reused for other items until setIsRecyclable() is
         * later set to true. Calls to setIsRecyclable() should always be paired (one
         * call to setIsRecyclabe(false) should always be matched with a later call to
         * setIsRecyclable(true)). Pairs of calls may be nested, as the state is internally
         * reference-counted.
         *
         * @param recyclable
         */
        public final void setIsRecyclable(boolean recyclable) {
            mIsRecyclableCount = recyclable ? mIsRecyclableCount - 1 : mIsRecyclableCount + 1;
            if (mIsRecyclableCount < 0) {
                mIsRecyclableCount = 0;
            } else if (!recyclable && mIsRecyclableCount == 1) {
                mFlags |= FLAG_NOT_RECYCLABLE;
            } else if (recyclable && mIsRecyclableCount == 0) {
                mFlags &= ~FLAG_NOT_RECYCLABLE;
            }
        }

        /**
         * @return true if this item is available to be recycled, false otherwise.
         */
        public final boolean isRecyclable() {
            return (mFlags & FLAG_NOT_RECYCLABLE) == 0 && !ViewCompat.hasTransientState(itemView);
        }

        /**
         * Returns whether we have animations referring to this view holder or not.
         * This is similar to isRecyclable flag but does not check transient state.
         *
         * @return
         */
        private boolean shouldBeKeptAsChild() {
            return (mFlags & FLAG_NOT_RECYCLABLE) != 0;
        }

        /**
         * @return True if ViewHolder is not referenced by RecyclerView animations but has
         * transient state which will prevent it from being recycled.
         */
        private boolean doesTransientStatePreventRecycling() {
            return (mFlags & FLAG_NOT_RECYCLABLE) == 0 && ViewCompat.hasTransientState(itemView);
        }

        boolean isUpdated() {
            return (mFlags & FLAG_UPDATE) != 0;
        }
    }

    /**
     * ViewCacheExtension is a helper class to provide an additional layer of view caching that can
     * be controlled by the developer.
     * <p>
     * When {@link Recycler#getViewForPosition(int)} is called, Recycler checks attached scrap and
     * first level cache to find a matching View. If it cannot find a suitable View, Recycler will
     * call the {@link #getViewForPositionAndType(Recycler, int, int)} before checking
     * {@link RecycledViewPool}.
     * <p>
     * Note that, Recycler never sends Views to this method to be cached. It is developers
     * responsibility to decide whether they want to keep their Views in this custom cache or let
     * the default recycling policy handle it.
     */
    public abstract static class ViewCacheExtension {

        /**
         * Returns a View that can be binded to the given Adapter position.
         * <p>
         * This method should <b>not</b> create a new View. Instead, it is expected to return
         * an already created View that can be re-used for the given type and position.
         * If the View is marked as ignored, it should first call
         * {@link LayoutManager#stopIgnoringView(View)} before returning the View.
         * <p>
         * RecyclerView will re-bind the returned View to the position if necessary.
         *
         * @param recycler The Recycler that can be used to bind the View
         * @param position The adapter position
         * @param type     The type of the View, defined by adapter
         * @return A View that is bound to the given position or NULL if there is no View to re-use
         * @see LayoutManager#ignoreView(View)
         */
        abstract public View getViewForPositionAndType(Recycler recycler, int position, int type);
    }

    /**
     * Base class for an Adapter
     * <p>
     * <p>Adapters provide a binding from an app-specific data set to views that are displayed
     * within a {@link RecyclerView}.</p>
     */
    public static abstract class Adapter<VH extends ViewHolder> {
        private final AdapterDataObservable mObservable = new AdapterDataObservable();
        private boolean mHasStableIds = false;

        /**
         * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
         * an item.
         * <p>
         * This new ViewHolder should be constructed with a new View that can represent the items
         * of the given type. You can either create a new View manually or inflate it from an XML
         * layout file.
         * <p>
         * The new ViewHolder will be used to display items of the adapter using
         * {@link #onBindViewHolder(ViewHolder, int, List)}. Since it will be re-used to display
         * different items in the data set, it is a good idea to cache references to sub views of
         * the View to avoid unnecessary {@link View#findViewById(int)} calls.
         *
         * @param parent   The ViewGroup into which the new View will be added after it is bound to
         *                 an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         * @see #getItemViewType(int)
         * @see #onBindViewHolder(ViewHolder, int)
         */
        public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);


        /**
         * Called by RecyclerView to display the data at the specified position. This method should
         * update the contents of the {@link ViewHolder#itemView} to reflect the item at the given
         * position.
         * <p>
         * Note that unlike {@link android.widget.ListView}, RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the <code>position</code> parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
         * have the updated adapter position.
         * <p>
         * Override {@link #onBindViewHolder(ViewHolder, int, List)} instead if Adapter can
         * handle efficient partial bind.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        public abstract void onBindViewHolder(VH holder, int position);

        /**
         * Called by RecyclerView to display the data at the specified position. This method
         * should update the contents of the {@link ViewHolder#itemView} to reflect the item at
         * the given position.
         * <p>
         * Note that unlike {@link android.widget.ListView}, RecyclerView will not call this method
         * again if the position of the item changes in the data set unless the item itself is
         * invalidated or the new position cannot be determined. For this reason, you should only
         * use the <code>position</code> parameter while acquiring the related data item inside
         * this method and should not keep a copy of it. If you need the position of an item later
         * on (e.g. in a click listener), use {@link ViewHolder#getAdapterPosition()} which will
         * have the updated adapter position.
         * <p>
         * Partial bind vs full bind:
         * <p>
         * The payloads parameter is a merge list from {@link #notifyItemChanged(int, Object)} or
         * {@link #notifyItemRangeChanged(int, int, Object)}.  If the payloads list is not empty,
         * the ViewHolder is currently bound to old data and Adapter may run an efficient partial
         * update using the payload info.  If the payload is empty,  Adapter must run a full bind.
         * Adapter should not assume that the payload passed in notify methods will be received by
         * onBindViewHolder().  For example when the view is not attached to the screen, the
         * payload in notifyItemChange() will be simply dropped.
         *
         * @param holder   The ViewHolder which should be updated to represent the contents of the
         *                 item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         * @param payloads A non-null list of merged payloads. Can be empty list if requires full
         *                 update.
         */
        public void onBindViewHolder(VH holder, int position, List<Object> payloads) {
            onBindViewHolder(holder, position);
        }

        /**
         * This method calls {@link #onCreateViewHolder(ViewGroup, int)} to create a new
         * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
         *
         * @see #onCreateViewHolder(ViewGroup, int)
         */
        public final VH createViewHolder(ViewGroup parent, int viewType) {
            TraceCompat.beginSection(TRACE_CREATE_VIEW_TAG);
            final VH holder = onCreateViewHolder(parent, viewType);
            holder.mItemViewType = viewType;
            TraceCompat.endSection();
            return holder;
        }

        /**
         * This method internally calls {@link #onBindViewHolder(ViewHolder, int)} to update the
         * {@link ViewHolder} contents with the item at the given position and also sets up some
         * private fields to be used by RecyclerView.
         *
         * @see #onBindViewHolder(ViewHolder, int)
         */
        public final void bindViewHolder(VH holder, int position) {
            holder.mPosition = position;
            if (hasStableIds()) {
                holder.mItemId = getItemId(position);
            }
            holder.setFlags(ViewHolder.FLAG_BOUND,
                    ViewHolder.FLAG_BOUND | ViewHolder.FLAG_UPDATE | ViewHolder.FLAG_INVALID
                            | ViewHolder.FLAG_ADAPTER_POSITION_UNKNOWN);
            TraceCompat.beginSection(TRACE_BIND_VIEW_TAG);
            onBindViewHolder(holder, position, holder.getUnmodifiedPayloads());
            holder.clearPayload();
            final ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof RecyclerView.LayoutParams) {
                ((LayoutParams) layoutParams).mInsetsDirty = true;
            }
            TraceCompat.endSection();
        }

        /**
         * Return the view type of the item at <code>position</code> for the purposes
         * of view recycling.
         * <p>
         * <p>The default implementation of this method returns 0, making the assumption of
         * a single view type for the adapter. Unlike ListView adapters, types need not
         * be contiguous. Consider using id resources to uniquely identify item view types.
         *
         * @param position position to query
         * @return integer value identifying the type of the view needed to represent the item at
         * <code>position</code>. Type codes need not be contiguous.
         */
        public int getItemViewType(int position) {
            return 0;
        }

        /**
         * Indicates whether each item in the data set can be represented with a unique identifier
         * of type {@link java.lang.Long}.
         *
         * @param hasStableIds Whether items in data set have unique identifiers or not.
         * @see #hasStableIds()
         * @see #getItemId(int)
         */
        public void setHasStableIds(boolean hasStableIds) {
            if (hasObservers()) {
                throw new IllegalStateException("Cannot change whether this adapter has " +
                        "stable IDs while the adapter has registered observers.");
            }
            mHasStableIds = hasStableIds;
        }

        /**
         * Return the stable ID for the item at <code>position</code>. If {@link #hasStableIds()}
         * would return false this method should return {@link #NO_ID}. The default implementation
         * of this method returns {@link #NO_ID}.
         *
         * @param position Adapter position to query
         * @return the stable ID of the item at position
         */
        public long getItemId(int position) {
            return NO_ID;
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        public abstract int getItemCount();

        /**
         * Returns true if this adapter publishes a unique <code>long</code> value that can
         * act as a key for the item at a given position in the data set. If that item is relocated
         * in the data set, the ID returned for that item should be the same.
         *
         * @return true if this adapter's items have stable IDs
         */
        public final boolean hasStableIds() {
            return mHasStableIds;
        }

        /**
         * Called when a view created by this adapter has been recycled.
         * <p>
         * <p>A view is recycled when a {@link LayoutManager} decides that it no longer
         * needs to be attached to its parent {@link RecyclerView}. This can be because it has
         * fallen out of visibility or a set of cached views represented by views still
         * attached to the parent RecyclerView. If an item view has large or expensive data
         * bound to it such as large bitmaps, this may be a good place to release those
         * resources.</p>
         * <p>
         * RecyclerView calls this method right before clearing ViewHolder's internal data and
         * sending it to RecycledViewPool. This way, if ViewHolder was holding valid information
         * before being recycled, you can call {@link ViewHolder#getAdapterPosition()} to get
         * its adapter position.
         *
         * @param holder The ViewHolder for the view being recycled
         */
        public void onViewRecycled(VH holder) {

        }

        /**
         * Called by the RecyclerView if a ViewHolder created by this Adapter cannot be recycled
         * due to its transient state. Upon receiving this callback, Adapter can clear the
         * animation(s) that effect the View's transient state and return <code>true</code> so that
         * the View can be recycled. Keep in mind that the View in question is already removed from
         * the RecyclerView.
         * <p>
         * In some cases, it is acceptable to recycle a View although it has transient state. Most
         * of the time, this is a case where the transient state will be cleared in
         * {@link #onBindViewHolder(ViewHolder, int)} call when View is rebound to a new position.
         * For this reason, RecyclerView leaves the decision to the Adapter and uses the return
         * value of this method to decide whether the View should be recycled or not.
         * <p>
         * Note that when all animations are created by {@link RecyclerView.ItemAnimator}, you
         * should never receive this callback because RecyclerView keeps those Views as children
         * until their animations are complete. This callback is useful when children of the item
         * views create animations which may not be easy to implement using an {@link ItemAnimator}.
         * <p>
         * You should <em>never</em> fix this issue by calling
         * <code>holder.itemView.setHasTransientState(false);</code> unless you've previously called
         * <code>holder.itemView.setHasTransientState(true);</code>. Each
         * <code>View.setHasTransientState(true)</code> call must be matched by a
         * <code>View.setHasTransientState(false)</code> call, otherwise, the state of the View
         * may become inconsistent. You should always prefer to end or cancel animations that are
         * triggering the transient state instead of handling it manually.
         *
         * @param holder The ViewHolder containing the View that could not be recycled due to its
         *               transient state.
         * @return True if the View should be recycled, false otherwise. Note that if this method
         * returns <code>true</code>, RecyclerView <em>will ignore</em> the transient state of
         * the View and recycle it regardless. If this method returns <code>false</code>,
         * RecyclerView will check the View's transient state again before giving a final decision.
         * Default implementation returns false.
         */
        public boolean onFailedToRecycleView(VH holder) {
            return false;
        }

        /**
         * Called when a view created by this adapter has been attached to a window.
         * <p>
         * <p>This can be used as a reasonable signal that the view is about to be seen
         * by the user. If the adapter previously freed any resources in
         * {@link #onViewDetachedFromWindow(RecyclerView.ViewHolder) onViewDetachedFromWindow}
         * those resources should be restored here.</p>
         *
         * @param holder Holder of the view being attached
         */
        public void onViewAttachedToWindow(VH holder) {
        }

        /**
         * Called when a view created by this adapter has been detached from its window.
         * <p>
         * <p>Becoming detached from the window is not necessarily a permanent condition;
         * the consumer of an Adapter's views may choose to cache views offscreen while they
         * are not visible, attaching and detaching them as appropriate.</p>
         *
         * @param holder Holder of the view being detached
         */
        public void onViewDetachedFromWindow(VH holder) {
        }

        /**
         * Returns true if one or more observers are attached to this adapter.
         *
         * @return true if this adapter has observers
         */
        public final boolean hasObservers() {
            return mObservable.hasObservers();
        }

        /**
         * Register a new observer to listen for data changes.
         * <p>
         * <p>The adapter may publish a variety of events describing specific changes.
         * Not all adapters may support all change types and some may fall back to a generic
         * {@link android.support.v7.widget.RecyclerView.AdapterDataObserver#onChanged()
         * "something changed"} event if more specific data is not available.</p>
         * <p>
         * <p>Components registering observers with an adapter are responsible for
         * {@link #unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver)
         * unregistering} those observers when finished.</p>
         *
         * @param observer Observer to register
         * @see #unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver)
         */
        public void registerAdapterDataObserver(AdapterDataObserver observer) {
            mObservable.registerObserver(observer);
        }

        /**
         * Unregister an observer currently listening for data changes.
         * <p>
         * <p>The unregistered observer will no longer receive events about changes
         * to the adapter.</p>
         *
         * @param observer Observer to unregister
         * @see #registerAdapterDataObserver(RecyclerView.AdapterDataObserver)
         */
        public void unregisterAdapterDataObserver(AdapterDataObserver observer) {
            mObservable.unregisterObserver(observer);
        }

        /**
         * Called by RecyclerView when it starts observing this Adapter.
         * <p>
         * Keep in mind that same adapter may be observed by multiple RecyclerViews.
         *
         * @param recyclerView The RecyclerView instance which started observing this adapter.
         * @see #onDetachedFromRecyclerView(RecyclerView)
         */
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        }

        /**
         * Called by RecyclerView when it stops observing this Adapter.
         *
         * @param recyclerView The RecyclerView instance which stopped observing this adapter.
         * @see #onAttachedToRecyclerView(RecyclerView)
         */
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        }

        /**
         * Notify any registered observers that the data set has changed.
         * <p>
         * <p>There are two different classes of data change events, item changes and structural
         * changes. Item changes are when a single item has its data updated but no positional
         * changes have occurred. Structural changes are when items are inserted, removed or moved
         * within the data set.</p>
         * <p>
         * <p>This event does not specify what about the data set has changed, forcing
         * any observers to assume that all existing items and structure may no longer be valid.
         * LayoutManagers will be forced to fully rebind and relayout all visible views.</p>
         * <p>
         * <p><code>RecyclerView</code> will attempt to synthesize visible structural change events
         * for adapters that report that they have {@link #hasStableIds() stable IDs} when
         * this method is used. This can help for the purposes of animation and visual
         * object persistence but individual item views will still need to be rebound
         * and relaid out.</p>
         * <p>
         * <p>If you are writing an adapter it will always be more efficient to use the more
         * specific change events if you can. Rely on <code>notifyDataSetChanged()</code>
         * as a last resort.</p>
         *
         * @see #notifyItemChanged(int)
         * @see #notifyItemInserted(int)
         * @see #notifyItemRemoved(int)
         * @see #notifyItemRangeChanged(int, int)
         * @see #notifyItemRangeInserted(int, int)
         * @see #notifyItemRangeRemoved(int, int)
         */
        public final void notifyDataSetChanged() {
            mObservable.notifyChanged();
        }

        /**
         * Notify any registered observers that the item at <code>position</code> has changed.
         * Equivalent to calling <code>notifyItemChanged(position, null);</code>.
         * <p>
         * <p>This is an item change event, not a structural change event. It indicates that any
         * reflection of the data at <code>position</code> is out of date and should be updated.
         * The item at <code>position</code> retains the same identity.</p>
         *
         * @param position Position of the item that has changed
         * @see #notifyItemRangeChanged(int, int)
         */
        public final void notifyItemChanged(int position) {
            mObservable.notifyItemRangeChanged(position, 1);
        }

        /**
         * Notify any registered observers that the item at <code>position</code> has changed with an
         * optional payload object.
         * <p>
         * <p>This is an item change event, not a structural change event. It indicates that any
         * reflection of the data at <code>position</code> is out of date and should be updated.
         * The item at <code>position</code> retains the same identity.
         * </p>
         * <p>
         * <p>
         * Client can optionally pass a payload for partial change. These payloads will be merged
         * and may be passed to adapter's {@link #onBindViewHolder(ViewHolder, int, List)} if the
         * item is already represented by a ViewHolder and it will be rebound to the same
         * ViewHolder. A notifyItemRangeChanged() with null payload will clear all existing
         * payloads on that item and prevent future payload until
         * {@link #onBindViewHolder(ViewHolder, int, List)} is called. Adapter should not assume
         * that the payload will always be passed to onBindViewHolder(), e.g. when the view is not
         * attached, the payload will be simply dropped.
         *
         * @param position Position of the item that has changed
         * @param payload  Optional parameter, use null to identify a "full" update
         * @see #notifyItemRangeChanged(int, int)
         */
        public final void notifyItemChanged(int position, Object payload) {
            mObservable.notifyItemRangeChanged(position, 1, payload);
        }

        /**
         * Notify any registered observers that the <code>itemCount</code> items starting at
         * position <code>positionStart</code> have changed.
         * Equivalent to calling <code>notifyItemRangeChanged(position, itemCount, null);</code>.
         * <p>
         * <p>This is an item change event, not a structural change event. It indicates that
         * any reflection of the data in the given position range is out of date and should
         * be updated. The items in the given range retain the same identity.</p>
         *
         * @param positionStart Position of the first item that has changed
         * @param itemCount     Number of items that have changed
         * @see #notifyItemChanged(int)
         */
        public final void notifyItemRangeChanged(int positionStart, int itemCount) {
            mObservable.notifyItemRangeChanged(positionStart, itemCount);
        }

        /**
         * Notify any registered observers that the <code>itemCount</code> items starting at
         * position <code>positionStart</code> have changed. An optional payload can be
         * passed to each changed item.
         * <p>
         * <p>This is an item change event, not a structural change event. It indicates that any
         * reflection of the data in the given position range is out of date and should be updated.
         * The items in the given range retain the same identity.
         * </p>
         * <p>
         * <p>
         * Client can optionally pass a payload for partial change. These payloads will be merged
         * and may be passed to adapter's {@link #onBindViewHolder(ViewHolder, int, List)} if the
         * item is already represented by a ViewHolder and it will be rebound to the same
         * ViewHolder. A notifyItemRangeChanged() with null payload will clear all existing
         * payloads on that item and prevent future payload until
         * {@link #onBindViewHolder(ViewHolder, int, List)} is called. Adapter should not assume
         * that the payload will always be passed to onBindViewHolder(), e.g. when the view is not
         * attached, the payload will be simply dropped.
         *
         * @param positionStart Position of the first item that has changed
         * @param itemCount     Number of items that have changed
         * @param payload       Optional parameter, use null to identify a "full" update
         * @see #notifyItemChanged(int)
         */
        public final void notifyItemRangeChanged(int positionStart, int itemCount, Object payload) {
            mObservable.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        /**
         * Notify any registered observers that the item reflected at <code>position</code>
         * has been newly inserted. The item previously at <code>position</code> is now at
         * position <code>position + 1</code>.
         * <p>
         * <p>This is a structural change event. Representations of other existing items in the
         * data set are still considered up to date and will not be rebound, though their
         * positions may be altered.</p>
         *
         * @param position Position of the newly inserted item in the data set
         * @see #notifyItemRangeInserted(int, int)
         */
        public final void notifyItemInserted(int position) {
            mObservable.notifyItemRangeInserted(position, 1);
        }

        /**
         * Notify any registered observers that the item reflected at <code>fromPosition</code>
         * has been moved to <code>toPosition</code>.
         * <p>
         * <p>This is a structural change event. Representations of other existing items in the
         * data set are still considered up to date and will not be rebound, though their
         * positions may be altered.</p>
         *
         * @param fromPosition Previous position of the item.
         * @param toPosition   New position of the item.
         */
        public final void notifyItemMoved(int fromPosition, int toPosition) {
            mObservable.notifyItemMoved(fromPosition, toPosition);
        }

        /**
         * Notify any registered observers that the currently reflected <code>itemCount</code>
         * items starting at <code>positionStart</code> have been newly inserted. The items
         * previously located at <code>positionStart</code> and beyond can now be found starting
         * at position <code>positionStart + itemCount</code>.
         * <p>
         * <p>This is a structural change event. Representations of other existing items in the
         * data set are still considered up to date and will not be rebound, though their positions
         * may be altered.</p>
         *
         * @param positionStart Position of the first item that was inserted
         * @param itemCount     Number of items inserted
         * @see #notifyItemInserted(int)
         */
        public final void notifyItemRangeInserted(int positionStart, int itemCount) {
            mObservable.notifyItemRangeInserted(positionStart, itemCount);
        }

        /**
         * Notify any registered observers that the item previously located at <code>position</code>
         * has been removed from the data set. The items previously located at and after
         * <code>position</code> may now be found at <code>oldPosition - 1</code>.
         * <p>
         * <p>This is a structural change event. Representations of other existing items in the
         * data set are still considered up to date and will not be rebound, though their positions
         * may be altered.</p>
         *
         * @param position Position of the item that has now been removed
         * @see #notifyItemRangeRemoved(int, int)
         */
        public final void notifyItemRemoved(int position) {
            mObservable.notifyItemRangeRemoved(position, 1);
        }

        /**
         * Notify any registered observers that the <code>itemCount</code> items previously
         * located at <code>positionStart</code> have been removed from the data set. The items
         * previously located at and after <code>positionStart + itemCount</code> may now be found
         * at <code>oldPosition - itemCount</code>.
         * <p>
         * <p>This is a structural change event. Representations of other existing items in the data
         * set are still considered up to date and will not be rebound, though their positions
         * may be altered.</p>
         *
         * @param positionStart Previous position of the first item that was removed
         * @param itemCount     Number of items removed from the data set
         */
        public final void notifyItemRangeRemoved(int positionStart, int itemCount) {
            mObservable.notifyItemRangeRemoved(positionStart, itemCount);
        }
    }

    /**
     * A <code>LayoutManager</code> is responsible for measuring and positioning item views
     * within a <code>RecyclerView</code> as well as determining the policy for when to recycle
     * item views that are no longer visible to the user. By changing the <code>LayoutManager</code>
     * a <code>RecyclerView</code> can be used to implement a standard vertically scrolling list,
     * a uniform grid, staggered grids, horizontally scrolling collections and more. Several stock
     * layout managers are provided for general use.
     * <p/>
     * If the LayoutManager specifies a default constructor or one with the signature
     * ({@link Context}, {@link AttributeSet}, {@code int}, {@code int}), RecyclerView will
     * instantiate and set the LayoutManager when being inflated. Most used properties can
     * be then obtained from {@link #getProperties(Context, AttributeSet, int, int)}. In case
     * a LayoutManager specifies both constructors, the non-default constructor will take
     * precedence.
     */
    public static abstract class LayoutManager {
        ChildHelper mChildHelper;
        RecyclerView mRecyclerView;

        SmoothScroller mSmoothScroller;

        boolean mRequestedSimpleAnimations = false;

        boolean mIsAttachedToWindow = false;

        boolean mAutoMeasure = false;

        /**
         * LayoutManager has its own more strict measurement cache to avoid re-measuring a child
         * if the space that will be given to it is already larger than what it has measured before.
         */
        private boolean mMeasurementCacheEnabled = true;

        private boolean mItemPrefetchEnabled = true;

        /**
         * These measure specs might be the measure specs that were passed into RecyclerView's
         * onMeasure method OR fake measure specs created by the RecyclerView.
         * For example, when a layout is run, RecyclerView always sets these specs to be
         * EXACTLY because a LayoutManager cannot resize RecyclerView during a layout pass.
         * <p>
         * Also, to be able to use the hint in unspecified measure specs, RecyclerView checks the
         * API level and sets the size to 0 pre-M to avoid any issue that might be caused by
         * corrupt values. Older platforms have no responsibility to provide a size if they set
         * mode to unspecified.
         */
        private int mWidthMode, mHeightMode;
        private int mWidth, mHeight;

        void setRecyclerView(RecyclerView recyclerView) {
            if (recyclerView == null) {
                mRecyclerView = null;
                mChildHelper = null;
                mWidth = 0;
                mHeight = 0;
            } else {
                mRecyclerView = recyclerView;
                mChildHelper = recyclerView.mChildHelper;
                mWidth = recyclerView.getWidth();
                mHeight = recyclerView.getHeight();
            }
            mWidthMode = MeasureSpec.EXACTLY;
            mHeightMode = MeasureSpec.EXACTLY;
        }

        void setMeasureSpecs(int wSpec, int hSpec) {
            mWidth = MeasureSpec.getSize(wSpec);
            mWidthMode = MeasureSpec.getMode(wSpec);

            if (mWidthMode == MeasureSpec.UNSPECIFIED && !ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                mWidth = 0;
            }
            mHeight = MeasureSpec.getSize(hSpec);
            mHeightMode = MeasureSpec.getMode(hSpec);

            if (mHeightMode == MeasureSpec.UNSPECIFIED && !ALLOW_SIZE_IN_UNSPECIFIED_SPEC) {
                mHeight = 0;
            }
        }

        /**
         * Called after a layout is calculated during a measure pass when using auto-measure.
         * <p>
         * It simply traverses all children to calculate a bounding box then calls
         * {@link #setMeasuredDimension(Rect, int, int)}. LayoutManagers can override that method
         * if they need to handle the bounding box differently.
         * <p>
         * For example, GridLayoutManager override that method to ensure that even if a column is
         * empty, the GridLayoutManager still measures wide enough to include it.
         *
         * @param widthSpec  The widthSpec that was passing into RecyclerView's onMeasure
         * @param heightSpec The heightSpec that was passing into RecyclerView's onMeasure
         */
        void setMeasuredDimensionFromChildren(int widthSpec, int heightSpec) {
            final int count = getChildCount();
            if (count == 0) {
                mRecyclerView.defaultOnMeasure(widthSpec, heightSpec);
                return;
            }
            int minX = Integer.MAX_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                final Rect bounds = mRecyclerView.mTempRect;
                getDecoratedBoundsWithMargins(child, bounds);
                if (bounds.left < minX) {
                    minX = bounds.left;
                }
                if (bounds.right > maxX) {
                    maxX = bounds.right;
                }
                if (bounds.top < minY) {
                    minY = bounds.top;
                }
                if (bounds.bottom > maxY) {
                    maxY = bounds.bottom;
                }
            }
            mRecyclerView.mTempRect.set(minX, minY, maxX, maxY);
            setMeasuredDimension(mRecyclerView.mTempRect, widthSpec, heightSpec);
        }

        /**
         * Sets the measured dimensions from the given bounding box of the children and the
         * measurement specs that were passed into {@link RecyclerView#onMeasure(int, int)}. It is
         * called after the RecyclerView calls
         * {@link LayoutManager#onLayoutChildren(Recycler, State)} during a measurement pass.
         * <p>
         * This method should call {@link #setMeasuredDimension(int, int)}.
         * <p>
         * The default implementation adds the RecyclerView's padding to the given bounding box
         * then caps the value to be within the given measurement specs.
         * <p>
         * This method is only called if the LayoutManager opted into the auto measurement API.
         *
         * @param childrenBounds The bounding box of all children
         * @param wSpec          The widthMeasureSpec that was passed into the RecyclerView.
         * @param hSpec          The heightMeasureSpec that was passed into the RecyclerView.
         * @see #setAutoMeasureEnabled(boolean)
         */
        public void setMeasuredDimension(Rect childrenBounds, int wSpec, int hSpec) {
            int usedWidth = childrenBounds.width() + getPaddingLeft() + getPaddingRight();
            int usedHeight = childrenBounds.height() + getPaddingTop() + getPaddingBottom();
            int width = chooseSize(wSpec, usedWidth, getMinimumWidth());
            int height = chooseSize(hSpec, usedHeight, getMinimumHeight());
            setMeasuredDimension(width, height);
        }

        /**
         * Calls {@code RecyclerView#requestLayout} on the underlying RecyclerView
         */
        public void requestLayout() {
            if (mRecyclerView != null) {
                mRecyclerView.requestLayout();
            }
        }

        /**
         * Checks if RecyclerView is in the middle of a layout or scroll and throws an
         * {@link IllegalStateException} if it <b>is not</b>.
         *
         * @param message The message for the exception. Can be null.
         * @see #assertNotInLayoutOrScroll(String)
         */
        public void assertInLayoutOrScroll(String message) {
            if (mRecyclerView != null) {
                mRecyclerView.assertInLayoutOrScroll(message);
            }
        }

        /**
         * Chooses a size from the given specs and parameters that is closest to the desired size
         * and also complies with the spec.
         *
         * @param spec    The measureSpec
         * @param desired The preferred measurement
         * @param min     The minimum value
         * @return A size that fits to the given specs
         */
        public static int chooseSize(int spec, int desired, int min) {
            final int mode = View.MeasureSpec.getMode(spec);
            final int size = View.MeasureSpec.getSize(spec);
            switch (mode) {
                case MeasureSpec.EXACTLY://match_parent
                    return size;
                case MeasureSpec.AT_MOST:
                    return Math.min(size, Math.min(desired, min));
                case MeasureSpec.UNSPECIFIED:
                default:
                    return Math.max(desired, min);
            }
        }

        /**
         * Checks if RecyclerView is in the middle of a layout or scroll and throws an
         * {@link IllegalStateException} if it <b>is</b>.
         *
         * @param message The message for the exception. Can be null.
         * @see #assertInLayoutOrScroll(String)
         */
        public void assertNotInLayoutOrScroll(String message) {
            if (mRecyclerView != null) {
                mRecyclerView.assertNotInLayoutOrScroll(message);
            }
        }

        /**
         * Defines whether the layout should be measured by the RecyclerView or the LayoutManager
         * wants to handle the layout measurements itself.
         * <p>
         * This method is usually called by the LayoutManager with value {@code true} if it wants
         * to support WRAP_CONTENT. If you are using a public LayoutManager but want to customize
         * the measurement logic, you can call this method with {@code false} and override
         * {@link LayoutManager#onMeasure(int, int)} to implement your custom measurement logic.
         * <p>
         * AutoMeasure is a convenience mechanism for LayoutManagers to easily wrap their content or
         * handle various specs provided by the RecyclerView's parent.
         * It works by calling {@link LayoutManager#onLayoutChildren(Recycler, State)} during an
         * {@link RecyclerView#onMeasure(int, int)} call, then calculating desired dimensions based
         * on children's positions. It does this while supporting all existing animation
         * capabilities of the RecyclerView.
         * <p>
         * AutoMeasure works as follows:
         * <ol>
         * <li>LayoutManager should call {@code setAutoMeasureEnabled(true)} to enable it. All of
         * the framework LayoutManagers use {@code auto-measure}.</li>
         * <li>When {@link RecyclerView#onMeasure(int, int)} is called, if the provided specs are
         * exact, RecyclerView will only call LayoutManager's {@code onMeasure} and return without
         * doing any layout calculation.</li>
         * <li>If one of the layout specs is not {@code EXACT}, the RecyclerView will start the
         * layout process in {@code onMeasure} call. It will process all pending Adapter updates and
         * decide whether to run a predictive layout or not. If it decides to do so, it will first
         * call {@link #onLayoutChildren(Recycler, State)} with {@link State#isPreLayout()} set to
         * {@code true}. At this stage, {@link #getWidth()} and {@link #getHeight()} will still
         * return the width and height of the RecyclerView as of the last layout calculation.
         * <p>
         * After handling the predictive case, RecyclerView will call
         * {@link #onLayoutChildren(Recycler, State)} with {@link State#isMeasuring()} set to
         * {@code true} and {@link State#isPreLayout()} set to {@code false}. The LayoutManager can
         * access the measurement specs via {@link #getHeight()}, {@link #getHeightMode()},
         * {@link #getWidth()} and {@link #getWidthMode()}.</li>
         * <li>After the layout calculation, RecyclerView sets the measured width & height by
         * calculating the bounding box for the children (+ RecyclerView's padding). The
         * LayoutManagers can override {@link #setMeasuredDimension(Rect, int, int)} to choose
         * different values. For instance, GridLayoutManager overrides this value to handle the case
         * where if it is vertical and has 3 columns but only 2 items, it should still measure its
         * width to fit 3 items, not 2.</li>
         * <li>Any following on measure call to the RecyclerView will run
         * {@link #onLayoutChildren(Recycler, State)} with {@link State#isMeasuring()} set to
         * {@code true} and {@link State#isPreLayout()} set to {@code false}. RecyclerView will
         * take care of which views are actually added / removed / moved / changed for animations so
         * that the LayoutManager should not worry about them and handle each
         * {@link #onLayoutChildren(Recycler, State)} call as if it is the last one.
         * </li>
         * <li>When measure is complete and RecyclerView's
         * {@link #onLayout(boolean, int, int, int, int)} method is called, RecyclerView checks
         * whether it already did layout calculations during the measure pass and if so, it re-uses
         * that information. It may still decide to call {@link #onLayoutChildren(Recycler, State)}
         * if the last measure spec was different from the final dimensions or adapter contents
         * have changed between the measure call and the layout call.</li>
         * <li>Finally, animations are calculated and run as usual.</li>
         * </ol>
         *
         * @param enabled <code>True</code> if the Layout should be measured by the
         *                RecyclerView, <code>false</code> if the LayoutManager wants
         *                to measure itself.
         * @see #setMeasuredDimension(Rect, int, int)
         * @see #isAutoMeasureEnabled()
         */
        public void setAutoMeasureEnabled(boolean enabled) {
            mAutoMeasure = enabled;
        }

        /**
         * Returns whether the LayoutManager uses the automatic measurement API or not.
         *
         * @return <code>True</code> if the LayoutManager is measured by the RecyclerView or
         * <code>false</code> if it measures itself.
         * @see #setAutoMeasureEnabled(boolean)
         */
        public boolean isAutoMeasureEnabled() {
            return mAutoMeasure;
        }

        /**
         * Returns whether this LayoutManager supports automatic item animations.
         * A LayoutManager wishing to support item animations should obey certain
         * rules as outlined in {@link #onLayoutChildren(Recycler, State)}.
         * The default return value is <code>false</code>, so subclasses of LayoutManager
         * will not get predictive item animations by default.
         * <p>
         * <p>Whether item animations are enabled in a RecyclerView is determined both
         * by the return value from this method and the
         * {@link RecyclerView#setItemAnimator(ItemAnimator) ItemAnimator} set on the
         * RecyclerView itself. If the RecyclerView has a non-null ItemAnimator but this
         * method returns false, then simple item animations will be enabled, in which
         * views that are moving onto or off of the screen are simply faded in/out. If
         * the RecyclerView has a non-null ItemAnimator and this method returns true,
         * then there will be two calls to {@link #onLayoutChildren(Recycler, State)} to
         * setup up the information needed to more intelligently predict where appearing
         * and disappearing views should be animated from/to.</p>
         *
         * @return true if predictive item animations should be enabled, false otherwise
         */
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        /**
         * Sets whether the LayoutManager should be queried for views outside of
         * its viewport while the UI thread is idle between frames.
         * <p>
         * <p>If enabled, the LayoutManager will be queried for items to inflate/bind in between
         * view system traversals on devices running API 21 or greater. Default value is true.</p>
         * <p>
         * <p>On platforms API level 21 and higher, the UI thread is idle between passing a frame
         * to RenderThread and the starting up its next frame at the next VSync pulse. By
         * prefetching out of window views in this time period, delays from inflation and view
         * binding are much less likely to cause jank and stuttering during scrolls and flings.</p>
         * <p>
         * <p>While prefetch is enabled, it will have the side effect of expanding the effective
         * size of the View cache to hold prefetched views.</p>
         *
         * @param enabled <code>True</code> if items should be prefetched in between traversals.
         * @see #isItemPrefetchEnabled()
         */
        public final void setItemPrefetchEnabled(boolean enabled) {
            if (enabled != mItemPrefetchEnabled) {
                mItemPrefetchEnabled = enabled;
                if (mRecyclerView != null) {
                    mRecyclerView.mRecycler.updateViewCacheSize();
                }
            }
        }

        /**
         * Sets whether the LayoutManager should be queried for views outside of
         * its viewport while the UI thread is idle between frames.
         *
         * @return true if item prefetch is enabled, false otherwise
         * @see #setItemPrefetchEnabled(boolean)
         */
        public final boolean isItemPrefetchEnabled() {
            return mItemPrefetchEnabled;
        }

        int getItemPrefetchCount() {
            return 0;
        }

        int gatherPrefetchIndices(int dx, int dy, State state, int[] outIndices) {
            return 0;
        }

        void dispatchAttachedToWindow(RecyclerView view) {
            mIsAttachedToWindow = true;
            onAttachedToWindow(view);
        }

        void dispatchDetachedFromWindow(RecyclerView view, Recycler recycler) {
            mIsAttachedToWindow = false;
            onDetachedFromWindow(view, recycler);
        }

        /**
         * Returns whether LayoutManager is currently attached to a RecyclerView which is attached
         * to a window.
         *
         * @return True if this LayoutManager is controlling a RecyclerView and the RecyclerView
         * is attached to window.
         */
        public boolean isAttachedToWindow() {
            return mIsAttachedToWindow;
        }

        /**
         * Causes the Runnable to execute on the next animation time step.
         * The runnable will be run on the user interface thread.
         * <p>
         * Calling this method when LayoutManager is not attached to a RecyclerView has no effect.
         *
         * @param action The Runnable that will be executed.
         * @see #removeCallbacks
         */
        public void postOnAnimation(Runnable action) {
            if (mRecyclerView != null) {
                ViewCompat.postOnAnimation(mRecyclerView, action);
            }
        }

        /**
         * Removes the specified Runnable from the message queue.
         * <p>
         * Calling this method when LayoutManager is not attached to a RecyclerView has no effect.
         *
         * @param action The Runnable to remove from the message handling queue
         * @return true if RecyclerView could ask the Handler to remove the Runnable,
         * false otherwise. When the returned value is true, the Runnable
         * may or may not have been actually removed from the message queue
         * (for instance, if the Runnable was not in the queue already.)
         * @see #postOnAnimation
         */
        public boolean removeCallbacks(Runnable action) {
            if (mRecyclerView != null) {
                return mRecyclerView.removeCallbacks(action);
            }
            return false;
        }

        /**
         * Called when this LayoutManager is both attached to a RecyclerView and that RecyclerView
         * is attached to a window.
         * <p>
         * If the RecyclerView is re-attached with the same LayoutManager and Adapter, it may not
         * call {@link #onLayoutChildren(Recycler, State)} if nothing has changed and a layout was
         * not requested on the RecyclerView while it was detached.
         * <p>
         * Subclass implementations should always call through to the superclass implementation.
         *
         * @param view The RecyclerView this LayoutManager is bound to
         * @see #onDetachedFromWindow(RecyclerView, Recycler)
         */
        @CallSuper
        public void onAttachedToWindow(RecyclerView view) {
        }

        /**
         * @deprecated override {@link #onDetachedFromWindow(RecyclerView, Recycler)}
         */
        @Deprecated
        public void onDetachedFromWindow(RecyclerView view) {

        }

        /**
         * Called when this LayoutManager is detached from its parent RecyclerView or when
         * its parent RecyclerView is detached from its window.
         * <p>
         * LayoutManager should clear all of its View references as another LayoutManager might be
         * assigned to the RecyclerView.
         * <p>
         * If the RecyclerView is re-attached with the same LayoutManager and Adapter, it may not
         * call {@link #onLayoutChildren(Recycler, State)} if nothing has changed and a layout was
         * not requested on the RecyclerView while it was detached.
         * <p>
         * If your LayoutManager has View references that it cleans in on-detach, it should also
         * call {@link RecyclerView#requestLayout()} to ensure that it is re-laid out when
         * RecyclerView is re-attached.
         * <p>
         * Subclass implementations should always call through to the superclass implementation.
         *
         * @param view     The RecyclerView this LayoutManager is bound to
         * @param recycler The recycler to use if you prefer to recycle your children instead of
         *                 keeping them around.
         * @see #onAttachedToWindow(RecyclerView)
         */
        @CallSuper
        public void onDetachedFromWindow(RecyclerView view, Recycler recycler) {
            onDetachedFromWindow(view);
        }

        /**
         * Check if the RecyclerView is configured to clip child views to its padding.
         *
         * @return true if this RecyclerView clips children to its padding, false otherwise
         */
        public boolean getClipToPadding() {
            return mRecyclerView != null && mRecyclerView.mClipToPadding;
        }

        /**
         * Lay out all relevant child views from the given adapter.
         * <p>
         * The LayoutManager is in charge of the behavior of item animations. By default,
         * RecyclerView has a non-null {@link #getItemAnimator() ItemAnimator}, and simple
         * item animations are enabled. This means that add/remove operations on the
         * adapter will result in animations to add new or appearing items, removed or
         * disappearing items, and moved items. If a LayoutManager returns false from
         * {@link #supportsPredictiveItemAnimations()}, which is the default, and runs a
         * normal layout operation during {@link #onLayoutChildren(Recycler, State)}, the
         * RecyclerView will have enough information to run those animations in a simple
         * way. For example, the default ItemAnimator, {@link DefaultItemAnimator}, will
         * simply fade views in and out, whether they are actually added/removed or whether
         * they are moved on or off the screen due to other add/remove operations.
         * <p>
         * <p>A LayoutManager wanting a better item animation experience, where items can be
         * animated onto and off of the screen according to where the items exist when they
         * are not on screen, then the LayoutManager should return true from
         * {@link #supportsPredictiveItemAnimations()} and add additional logic to
         * {@link #onLayoutChildren(Recycler, State)}. Supporting predictive animations
         * means that {@link #onLayoutChildren(Recycler, State)} will be called twice;
         * once as a "pre" layout step to determine where items would have been prior to
         * a real layout, and again to do the "real" layout. In the pre-layout phase,
         * items will remember their pre-layout positions to allow them to be laid out
         * appropriately. Also, {@link LayoutParams#isItemRemoved() removed} items will
         * be returned from the scrap to help determine correct placement of other items.
         * These removed items should not be added to the child list, but should be used
         * to help calculate correct positioning of other views, including views that
         * were not previously onscreen (referred to as APPEARING views), but whose
         * pre-layout offscreen position can be determined given the extra
         * information about the pre-layout removed views.</p>
         * <p>
         * <p>The second layout pass is the real layout in which only non-removed views
         * will be used. The only additional requirement during this pass is, if
         * {@link #supportsPredictiveItemAnimations()} returns true, to note which
         * views exist in the child list prior to layout and which are not there after
         * layout (referred to as DISAPPEARING views), and to position/layout those views
         * appropriately, without regard to the actual bounds of the RecyclerView. This allows
         * the animation system to know the location to which to animate these disappearing
         * views.</p>
         * <p>
         * <p>The default LayoutManager implementations for RecyclerView handle all of these
         * requirements for animations already. Clients of RecyclerView can either use one
         * of these layout managers directly or look at their implementations of
         * onLayoutChildren() to see how they account for the APPEARING and
         * DISAPPEARING views.</p>
         *
         * @param recycler Recycler to use for fetching potentially cached views for a
         *                 position
         * @param state    Transient state of RecyclerView
         */
        public void onLayoutChildren(Recycler recycler, State state) {
            Log.e(TAG, "You must override onLayoutChildren(Recycler recycler, State state) ");
        }

        /**
         * Called after a full layout calculation is finished. The layout calculation may include
         * multiple {@link #onLayoutChildren(Recycler, State)} calls due to animations or
         * layout measurement but it will include only one {@link #onLayoutCompleted(State)} call.
         * This method will be called at the end of {@link View#layout(int, int, int, int)} call.
         * <p>
         * This is a good place for the LayoutManager to do some cleanup like pending scroll
         * position, saved state etc.
         *
         * @param state Transient state of RecyclerView
         */
        public void onLayoutCompleted(State state) {
        }

        /**
         * Create a default <code>LayoutParams</code> object for a child of the RecyclerView.
         * <p>
         * <p>LayoutManagers will often want to use a custom <code>LayoutParams</code> type
         * to store extra information specific to the layout. Client code should subclass
         * {@link RecyclerView.LayoutParams} for this purpose.</p>
         * <p>
         * <p><em>Important:</em> if you use your own custom <code>LayoutParams</code> type
         * you must also override
         * {@link #checkLayoutParams(LayoutParams)},
         * {@link #generateLayoutParams(android.view.ViewGroup.LayoutParams)} and
         * {@link #generateLayoutParams(android.content.Context, android.util.AttributeSet)}.</p>
         *
         * @return A new LayoutParams for a child view
         */
        public abstract LayoutParams generateDefaultLayoutParams();

        /**
         * Determines the validity of the supplied LayoutParams object.
         * <p>
         * <p>This should check to make sure that the object is of the correct type
         * and all values are within acceptable ranges. The default implementation
         * returns <code>true</code> for non-null params.</p>
         *
         * @param lp LayoutParams object to check
         * @return true if this LayoutParams object is valid, false otherwise
         */
        public boolean checkLayoutParams(LayoutParams lp) {
            return lp != null;
        }

        /**
         * Create a LayoutParams object suitable for this LayoutManager, copying relevant
         * values from the supplied LayoutParams object if possible.
         * <p>
         * <p><em>Important:</em> if you use your own custom <code>LayoutParams</code> type
         * you must also override
         * {@link #checkLayoutParams(LayoutParams)},
         * {@link #generateLayoutParams(android.view.ViewGroup.LayoutParams)} and
         * {@link #generateLayoutParams(android.content.Context, android.util.AttributeSet)}.</p>
         *
         * @param lp Source LayoutParams object to copy values from
         * @return a new LayoutParams object
         */
        public LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
            if (lp instanceof LayoutParams) {
                return new LayoutParams((LayoutParams) lp);
            } else if (lp instanceof MarginLayoutParams) {
                return new LayoutParams((MarginLayoutParams) lp);
            } else {
                return new LayoutParams(lp);
            }
        }

        /**
         * Create a LayoutParams object suitable for this LayoutManager from
         * an inflated layout resource.
         * <p>
         * <p><em>Important:</em> if you use your own custom <code>LayoutParams</code> type
         * you must also override
         * {@link #checkLayoutParams(LayoutParams)},
         * {@link #generateLayoutParams(android.view.ViewGroup.LayoutParams)} and
         * {@link #generateLayoutParams(android.content.Context, android.util.AttributeSet)}.</p>
         *
         * @param c     Context for obtaining styled attributes
         * @param attrs AttributeSet describing the supplied arguments
         * @return a new LayoutParams object
         */
        public LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
            return new LayoutParams(c, attrs);
        }

        /**
         * Scroll horizontally by dx pixels in screen coordinates and return the distance traveled.
         * The default implementation does nothing and returns 0.
         *
         * @param dx       distance to scroll by in pixels. X increases as scroll position
         *                 approaches the right.
         * @param recycler Recycler to use for fetching potentially cached views for a
         *                 position
         * @param state    Transient state of RecyclerView
         * @return The actual distance scrolled. The return value will be negative if dx was
         * negative and scrolling proceeeded in that direction.
         * <code>Math.abs(result)</code> may be less than dx if a boundary was reached.
         */
        public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
            return 0;
        }

        /**
         * Scroll vertically by dy pixels in screen coordinates and return the distance traveled.
         * The default implementation does nothing and returns 0.
         *
         * @param dy       distance to scroll in pixels. Y increases as scroll position
         *                 approaches the bottom.
         * @param recycler Recycler to use for fetching potentially cached views for a
         *                 position
         * @param state    Transient state of RecyclerView
         * @return The actual distance scrolled. The return value will be negative if dy was
         * negative and scrolling proceeeded in that direction.
         * <code>Math.abs(result)</code> may be less than dy if a boundary was reached.
         */
        public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
            return 0;
        }

        /**
         * Query if horizontal scrolling is currently supported. The default implementation
         * returns false.
         *
         * @return True if this LayoutManager can scroll the current contents horizontally
         */
        public boolean canScrollHorizontally() {
            return false;
        }

        /**
         * Query if vertical scrolling is currently supported. The default implementation
         * returns false.
         *
         * @return True if this LayoutManager can scroll the current contents vertically
         */
        public boolean canScrollVertically() {
            return false;
        }

        /**
         * Scroll to the specified adapter position.
         * <p>
         * Actual position of the item on the screen depends on the LayoutManager implementation.
         *
         * @param position Scroll to this adapter position.
         */
        public void scrollToPosition(int position) {
            if (DEBUG) {
                Log.e(TAG, "You MUST implement scrollToPosition. It will soon become abstract");
            }
        }

        /**
         * <p>Smooth scroll to the specified adapter position.</p>
         * <p>To support smooth scrolling, override this method, create your {@link SmoothScroller}
         * instance and call {@link #startSmoothScroll(SmoothScroller)}.
         * </p>
         *
         * @param recyclerView The RecyclerView to which this layout manager is attached
         * @param state        Current State of RecyclerView
         * @param position     Scroll to this adapter position.
         */
        public void smoothScrollToPosition(RecyclerView recyclerView, State state,
                                           int position) {
            Log.e(TAG, "You must override smoothScrollToPosition to support smooth scrolling");
        }

        /**
         * <p>Starts a smooth scroll using the provided SmoothScroller.</p>
         * <p>Calling this method will cancel any previous smooth scroll request.</p>
         *
         * @param smoothScroller Instance which defines how smooth scroll should be animated
         */
        public void startSmollScroll(SmoothScroller smoothScroller) {
            if (mSmoothScroller != null && smoothScroller != mSmoothScroller
                    && mSmoothScroller.isRunning()) {
                mSmoothScroller.stop();
            }
            mSmoothScroller = smoothScroller;
            mSmoothScroller.start(mRecyclerView, this);
        }

        /**
         * @return true if RecycylerView is currently in the state of smooth scrolling.
         */
        public boolean isSmoothScrolling() {
            return mSmoothScroller != null && mSmoothScroller.isRunning();
        }


        /**
         * Returns the resolved layout direction for this RecyclerView.
         *
         * @return {@link android.support.v4.view.ViewCompat#LAYOUT_DIRECTION_RTL} if the layout
         * direction is RTL or returns
         * {@link android.support.v4.view.ViewCompat#LAYOUT_DIRECTION_LTR} if the layout direction
         * is not RTL.
         */
        public int getLayoutDirection() {
            return ViewCompat.getLayoutDirection(mRecyclerView);
        }

        /**
         * Ends all animations on the view created by the {@link ItemAnimator}.
         *
         * @param view The View for which the animations should be ended.
         * @see RecyclerView.ItemAnimator#endAnimations()
         */
        public void endAnimation(View view) {
            if (mRecyclerView.mItemAnimator != null) {
                mRecyclerView.mItemAnimator.endAnimation(getChildViewHolderInt(view));
            }
        }

        /**
         * To be called only during {@link #onLayoutChildren(Recycler, State)} to add a view
         * to the layout that is known to be going away, either because it has been
         * {@link Adapter#notifyItemRemoved(int) removed} or because it is actually not in the
         * visible portion of the container but is being laid out in order to inform RecyclerView
         * in how to animate the item out of view.
         * <p>
         * Views added via this method are going to be invisible to LayoutManager after the
         * dispatchLayout pass is complete. They cannot be retrieved via {@link #getChildAt(int)}
         * or won't be included in {@link #getChildCount()} method.
         *
         * @param child View to add and then remove with animation.
         */
        public void addDisappearingView(View child) {
            addDisappearingView(child, -1);
        }

        /**
         * To be called only during {@link #onLayoutChildren(Recycler, State)} to add a view
         * to the layout that is known to be going away, either because it has been
         * {@link Adapter#notifyItemRemoved(int) removed} or because it is actually not in the
         * visible portion of the container but is being laid out in order to inform RecyclerView
         * in how to animate the item out of view.
         * <p>
         * Views added via this method are going to be invisible to LayoutManager after the
         * dispatchLayout pass is complete. They cannot be retrieved via {@link #getChildAt(int)}
         * or won't be included in {@link #getChildCount()} method.
         *
         * @param child View to add and then remove with animation.
         * @param index Index of the view.
         */
        public void addDisappearingView(View child, int index) {
            addViewInt(child, index, true);
        }

        /**
         * Add a view to the currently attached RecyclerView if needed. LayoutManagers should
         * use this method to add views obtained from a {@link Recycler} using
         * {@link Recycler#getViewForPosition(int)}.
         *
         * @param child View to add
         */
        public void addView(View child) {
            addView(child, -1);
        }

        /**
         * Add a view to the currently attached RecyclerView if needed. LayoutManagers should
         * use this method to add views obtained from a {@link Recycler} using
         * {@link Recycler#getViewForPosition(int)}.
         *
         * @param child View to add
         * @param index Index to add child at
         */
        public void addView(View child, int index) {
            addViewInt(child, index, false);
        }

        private void addViewInt(View child, int index, boolean disappearing) {
            final ViewHolder holder = getChildViewHolderInt(child);
            if (disappearing || holder.isRemoved()) {
                // these views will be hidden at the end of the layout pass.
                mRecyclerView.mViewInfoStore.addToDisappearedInLayout(holder);
            } else {
                // This may look like unnecessary but may happen if layout manager supports
                // predictive layouts and adapter removed then re-added the same item.
                // In this case, added version will be visible in the post layout (because add is
                // deferred) but RV will still bind it to the same View.
                // So if a View re-appears in post layout pass, remove it from disappearing list.
                mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(holder);
            }
            final LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            if (holder.wasReturnedFromScrap() || holder.isScrap()) {
                if (holder.isScrap()) {
                    holder.unScrap();
                } else {
                    holder.clearReturnedFromScrapFlag();
                }
                mChildHelper.attachViewToParent(child, index, child.getLayoutParams(), false);
                if (DISPATCH_TEMP_DETACH) {
                    ViewCompat.dispatchFinishTemporaryDetach(child);
                }
            } else if (child.getParent() == mRecyclerView) {// it was not a scrap but a valid child
                //ensure in correct position
                int currentIndex = mChildHelper.indexOfChild(child);
                if (index == -1) {
                    index = mChildHelper.getChildCount();
                }
                if (currentIndex == -1) {
                    throw new IllegalStateException("Added View has RecyclerView as parent but"
                            + " view is not a real child. Unfiltered index:"
                            + mRecyclerView.indexOfChild(child));
                }
                if (currentIndex != index) {
                    mRecyclerView.mLayout.moveView(currentIndex, index);
                }
            } else {
                mChildHelper.addView(child, index, false);
                layoutParams.mInsetsDirty = true;
                if (mSmoothScroller != null && mSmoothScroller.isRunning()) {
                    mSmoothScroller.onChildAttachedToWindow(child);
                }
            }
            if (layoutParams.mPendingInvalidate) {
                holder.itemView.invalidate();
                layoutParams.mPendingInvalidate = false;
            }
        }

        /**
         * Remove a view from the currently attached RecyclerView if needed. LayoutManagers should
         * use this method to completely remove a child view that is no longer needed.
         * LayoutManagers should strongly consider recycling removed views using
         * {@link Recycler#recycleView(android.view.View)}.
         *
         * @param child View to remove
         */
        public void removeView(View child) {
            mChildHelper.removeView(child);
        }

        /**
         * Remove a view from the currently attached RecyclerView if needed. LayoutManagers should
         * use this method to completely remove a child view that is no longer needed.
         * LayoutManagers should strongly consider recycling removed views using
         * {@link Recycler#recycleView(android.view.View)}.
         *
         * @param index Index of the child view to remove
         */
        public void removeViewAt(int index) {
            final View child = getChildAt(index);
            if (child != null) {
                mChildHelper.removeViewAt(index);
            }
        }

        /**
         * Remove all views from the currently attached RecyclerView. This will not recycle
         * any of the affected views; the LayoutManager is responsible for doing so if desired.
         */
        public void removeAllViews() {
            //Only remove non-animating views
            final int childCount = getChildCount();
            for (int i = childCount - 1; i <= 0; i--) {
                mChildHelper.removeViewAt(i);
            }
        }

        /**
         * Returns offset of the RecyclerView's text baseline from the its top boundary.
         *
         * @return The offset of the RecyclerView's text baseline from the its top boundary; -1 if
         * there is no baseline.
         */
        public int getBaseline() {
            return -1;
        }

        /**
         * Returns the adapter position of the item represented by the given View. This does not
         * contain any adapter changes that might have happened after the last layout.
         *
         * @param view The view to query
         * @return The adapter position of the item which is rendered by this View.
         */
        public int getPosition(View view) {
            return ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        }

        /**
         * Returns the View type defined by the adapter.
         *
         * @param view The view to query
         * @return The type of the view assigned by the adapter.
         */
        public int getItemViewType(View view) {
            return getChildViewHolderInt(view).getItemViewType();
        }

        /**
         * Traverses the ancestors of the given view and returns the item view that contains it
         * and also a direct child of the LayoutManager.
         * <p>
         * Note that this method may return null if the view is a child of the RecyclerView but
         * not a child of the LayoutManager (e.g. running a disappear animation).
         *
         * @param view The view that is a descendant of the LayoutManager.
         * @return The direct child of the LayoutManager which contains the given view or null if
         * the provided view is not a descendant of this LayoutManager.
         * @see RecyclerView#getChildViewHolder(View)
         * @see RecyclerView#findContainingViewHolder(View)
         */
        public View findContainingItemView(View view) {
            if (mRecyclerView == null) {
                return null;
            }
            View found = mRecyclerView.findContainingItemView(view);
            if (found == null) {
                return null;
            }
            if (mChildHelper.isHidden(found)) {
                return null;
            }
            return found;
        }

        /**
         * Finds the view which represents the given adapter position.
         * <p>
         * This method traverses each child since it has no information about child order.
         * Override this method to improve performance if your LayoutManager keeps data about
         * child views.
         * <p>
         * If a view is ignored via {@link #ignoreView(View)}, it is also ignored by this method.
         *
         * @param position Position of the item in adapter
         * @return The child view that represents the given position or null if the position is not
         * laid out
         */
        public View findViewByPosition(int position) {
            final int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                ViewHolder viewHolder = getChildViewHolderInt(child);
                if (viewHolder == null) {
                    continue;
                }
                if (viewHolder.getLayoutPosition() == position && !viewHolder.shouldIgnore()
                        && (mRecyclerView.mState.isPreLayout() || !viewHolder.isRemoved())) {
                    return child;
                }
            }
            return null;
        }

        /**
         * Temporarily detach a child view.
         * <p>
         * <p>LayoutManagers may want to perform a lightweight detach operation to rearrange
         * views currently attached to the RecyclerView. Generally LayoutManager implementations
         * will want to use {@link #detachAndScrapView(android.view.View, RecyclerView.Recycler)}
         * so that the detached view may be rebound and reused.</p>
         * <p>
         * <p>If a LayoutManager uses this method to detach a view, it <em>must</em>
         * {@link #attachView(android.view.View, int, RecyclerView.LayoutParams) reattach}
         * or {@link #removeDetachedView(android.view.View) fully remove} the detached view
         * before the LayoutManager entry point method called by RecyclerView returns.</p>
         *
         * @param child Child to detach
         */
        public void detachView(View child) {
            final int ind = mChildHelper.indexOfChild(child);
            if (ind >= 0) {
                detachViewInternal(ind, child);
            }
        }

        /**
         * Temporarily detach a child view.
         * <p>
         * <p>LayoutManagers may want to perform a lightweight detach operation to rearrange
         * views currently attached to the RecyclerView. Generally LayoutManager implementations
         * will want to use {@link #detachAndScrapView(android.view.View, RecyclerView.Recycler)}
         * so that the detached view may be rebound and reused.</p>
         * <p>
         * <p>If a LayoutManager uses this method to detach a view, it <em>must</em>
         * {@link #attachView(android.view.View, int, RecyclerView.LayoutParams) reattach}
         * or {@link #removeDetachedView(android.view.View) fully remove} the detached view
         * before the LayoutManager entry point method called by RecyclerView returns.</p>
         *
         * @param index Index of the child to detach
         */
        public void detachViewAt(int index) {
            detachViewInternal(index, getChildAt(index));
        }

        private void detachViewInternal(int index, View view) {
            if (DISPATCH_TEMP_DETACH) {
                ViewCompat.dispatchFinishTemporaryDetach(view);
            }
            mChildHelper.detachViewFromParent(index);
        }

        /**
         * Reattach a previously {@link #detachView(android.view.View) detached} view.
         * This method should not be used to reattach views that were previously
         * {@link #detachAndScrapView(android.view.View, RecyclerView.Recycler)}  scrapped}.
         *
         * @param child Child to reattach
         * @param index Intended child index for child
         * @param lp    LayoutParams for child
         */
        public void attachView(View child, int index, LayoutParams lp) {
            ViewHolder viewHolder = getChildViewHolderInt(child);
            if (viewHolder.isRemoved()) {
                mRecyclerView.mViewInfoStore.addToDisappearedInLayout(viewHolder);
            } else {
                mRecyclerView.mViewInfoStore.removeFromDisappearedInLayout(viewHolder);
            }
            mChildHelper.attachViewToParent(child, index, lp, viewHolder.isRemoved());
            if (DISPATCH_TEMP_DETACH) {
                ViewCompat.dispatchFinishTemporaryDetach(child);
            }
        }

        /**
         * Reattach a previously {@link #detachView(android.view.View) detached} view.
         * This method should not be used to reattach views that were previously
         * {@link #detachAndScrapView(android.view.View, RecyclerView.Recycler)}  scrapped}.
         *
         * @param child Child to reattach
         * @param index Intended child index for child
         */
        public void attachView(View child, int index) {
            attachView(child, index, (LayoutParams) child.getLayoutParams());
        }

        /**
         * Reattach a previously {@link #detachView(android.view.View) detached} view.
         * This method should not be used to reattach views that were previously
         * {@link #detachAndScrapView(android.view.View, RecyclerView.Recycler)}  scrapped}.
         *
         * @param child Child to reattach
         */
        public void attachView(View child) {
            attachView(child, -1);
        }

        /**
         * Finish removing a view that was previously temporarily
         * {@link #detachView(android.view.View) detached}.
         *
         * @param child Detached child to remove
         */
        public void removeDetachedView(View child) {
            mRecyclerView.removeDetachedView(child, false);
        }

        /**
         * Moves a View from one position to another.
         *
         * @param fromIndex The View's initial index
         * @param toIndex   The View's target index
         */
        public void moveView(int fromIndex, int toIndex) {
            View view = getChildAt(fromIndex);
            if (view == null) {
                throw new IllegalArgumentException("Cannot move a child from non-existing index:"
                        + fromIndex);
            }
            detachViewAt(fromIndex);
            attachView(view, toIndex);
        }

        /**
         * Detach a child view and add it to a {@link Recycler Recycler's} scrap heap.
         * <p>
         * <p>Scrapping a view allows it to be rebound and reused to show updated or
         * different data.</p>
         *
         * @param child    Child to detach and scrap
         * @param recycler Recycler to deposit the new scrap view into
         */
        public void detachAndScrapView(View child, Recycler recycler) {
            int index = mChildHelper.indexOfChild(child);
            scrapOrRecycleView(recycler, index, child);
        }

        /**
         * Detach a child view and add it to a {@link Recycler Recycler's} scrap heap.
         * <p>
         * <p>Scrapping a view allows it to be rebound and reused to show updated or
         * different data.</p>
         *
         * @param index    Index of child to detach and scrap
         * @param recycler Recycler to deposit the new scrap view into
         */
        public void detachAndScrapViewAt(int index, Recycler recycler) {
            final View child = getChildAt(index);
            scrapOrRecycleView(recycler, index, child);
        }

        /**
         * Remove a child view and recycle it using the given Recycler.
         *
         * @param child    Child to remove and recycle
         * @param recycler Recycler to use to recycle child
         */
        public void removeAndRecycleView(View child, Recycler recycler) {
            removeView(child);
            recycler.recycleView(child);
        }

        /**
         * Remove a child view and recycle it using the given Recycler.
         *
         * @param index    Index of child to remove and recycle
         * @param recycler Recycler to use to recycle child
         */
        public void removeAndRecycleViewAt(int index, Recycler recycler) {
            final View view = getChildAt(index);
            removeViewAt(index);
            recycler.recycleView(view);
        }

        /**
         * Return the current number of child views attached to the parent RecyclerView.
         * This does not include child views that were temporarily detached and/or scrapped.
         *
         * @return Number of attached children
         */
        public int getChildCount() {
            return mChildHelper != null ? mChildHelper.getChildCount() : 0;
        }

        /**
         * Return the child view at the given index
         *
         * @param index Index of child to return
         * @return Child view at index
         */
        public View getChildAt(int index) {
            return mChildHelper != null ? mChildHelper.getChildAt(index) : null;
        }

        /**
         * Return the width measurement spec mode of the RecyclerView.
         * <p>
         * This value is set only if the LayoutManager opts into the auto measure api via
         * {@link #setAutoMeasureEnabled(boolean)}.
         * <p>
         * When RecyclerView is running a layout, this value is always set to
         * {@link View.MeasureSpec#EXACTLY} even if it was measured with a different spec mode.
         *
         * @return Width measure spec mode.
         * @see View.MeasureSpec#getMode(int)
         * @see View#onMeasure(int, int)
         */
        public int getWidthMode() {
            return mWidthMode;
        }

        /**
         * Return the height measurement spec mode of the RecyclerView.
         * <p>
         * This value is set only if the LayoutManager opts into the auto measure api via
         * {@link #setAutoMeasureEnabled(boolean)}.
         * <p>
         * When RecyclerView is running a layout, this value is always set to
         * {@link View.MeasureSpec#EXACTLY} even if it was measured with a different spec mode.
         *
         * @return Height measure spec mode.
         * @see View.MeasureSpec#getMode(int)
         * @see View#onMeasure(int, int)
         */
        public int getHeightMode() {
            return mHeightMode;
        }

        /**
         * Return the width of the parent RecyclerView
         *
         * @return Width in pixels
         */
        public int getWidth() {
            return mWidth;
        }

        /**
         * Return the height of the parent RecyclerView
         *
         * @return Height in pixels
         */
        public int getHeight() {
            return mHeight;
        }

        /**
         * Return the left padding of the parent RecyclerView
         *
         * @return Padding in pixels
         */
        public int getPaddingLeft() {
            return mRecyclerView != null ? mRecyclerView.getPaddingLeft() : 0;
        }

        /**
         * Return the top padding of the parent RecyclerView
         *
         * @return Padding in pixels
         */
        public int getPaddingTop() {
            return mRecyclerView != null ? mRecyclerView.getPaddingTop() : 0;
        }

        /**
         * Return the right padding of the parent RecyclerView
         *
         * @return Padding in pixels
         */
        public int getPaddingRight() {
            return mRecyclerView != null ? mRecyclerView.getPaddingRight() : 0;
        }

        /**
         * Return the bottom padding of the parent RecyclerView
         *
         * @return Padding in pixels
         */
        public int getPaddingBottom() {
            return mRecyclerView != null ? mRecyclerView.getPaddingBottom() : 0;
        }

        /**
         * Return the start padding of the parent RecyclerView
         *
         * @return Padding in pixels
         */
        public int getPaddingStart() {
            return mRecyclerView != null ? ViewCompat.getPaddingStart(mRecyclerView) : 0;
        }

        /**
         * Return the end padding of the parent RecyclerView
         *
         * @return Padding in pixels
         */
        public int getPaddingEnd() {
            return mRecyclerView != null ? ViewCompat.getPaddingEnd(mRecyclerView) : 0;
        }

        /**
         * Returns true if the RecyclerView this LayoutManager is bound to has focus.
         *
         * @return True if the RecyclerView has focus, false otherwise.
         * @see View#isFocused()
         */
        public boolean isFocused() {
            return mRecyclerView != null && mRecyclerView.isFocused();
        }

        /**
         * Returns true if the RecyclerView this LayoutManager is bound to has or contains focus.
         *
         * @return true if the RecyclerView has or contains focus
         * @see View#hasFocus()
         */
        public boolean hasFocus() {
            return mRecyclerView != null && mRecyclerView.hasFocus();
        }

        /**
         * Returns the item View which has or contains focus.
         *
         * @return A direct child of RecyclerView which has focus or contains the focused child.
         */
        public View getFocusedChild() {
            if (mRecyclerView == null) {
                return null;
            }
            final View focused = mRecyclerView.getFocusedChild();
            if (focused == null || mChildHelper.isHidden(focused)) {
                return null;
            }
            return focused;
        }

        /**
         * Returns the number of items in the adapter bound to the parent RecyclerView.
         * <p>
         * Note that this number is not necessarily equal to {@link State#getItemCount()}. In
         * methods where State is available, you should use {@link State#getItemCount()} instead.
         * For more details, check the documentation for {@link State#getItemCount()}.
         *
         * @return The number of items in the bound adapter
         * @see State#getItemCount()
         */
        public int getItemCount() {
            final Adapter a = mRecyclerView != null ? mRecyclerView.getAdapter() : null;
            return a != null ? a.getItemCount() : 0;
        }

        /**
         * Offset all child views attached to the parent RecyclerView by dx pixels along
         * the horizontal axis.
         *
         * @param dx Pixels to offset by
         */
        public void offsetChildrenHorizontal(int dx) {
            if (mRecyclerView != null) {
                mRecyclerView.offsetChildrenHorizontal(dx);
            }
        }

        /**
         * Offset all child views attached to the parent RecyclerView by dy pixels along
         * the vertical axis.
         *
         * @param dy Pixels to offset by
         */
        public void offsetChildrenVertical(int dy) {
            if (mRecyclerView != null) {
                mRecyclerView.offsetChildrenVertical(dy);
            }
        }

        /**
         * Flags a view so that it will not be scrapped or recycled.
         * <p>
         * Scope of ignoring a child is strictly restricted to position tracking, scrapping and
         * recyling. Methods like {@link #removeAndRecycleAllViews(Recycler)} will ignore the child
         * whereas {@link #removeAllViews()} or {@link #offsetChildrenHorizontal(int)} will not
         * ignore the child.
         * <p>
         * Before this child can be recycled again, you have to call
         * {@link #stopIgnoringView(View)}.
         * <p>
         * You can call this method only if your LayoutManger is in onLayout or onScroll callback.
         *
         * @param view View to ignore.
         * @see #stopIgnoringView(View)
         */
        public void ignoreView(View view) {
            if (view.getParent() != mRecyclerView || mRecyclerView.indexOfChild(view) == -1) {
                // checking this because calling this method on a recycled or detached view may
                // cause loss of state.
                throw new IllegalArgumentException("View should be fully attached to be ignored");
            }
            final ViewHolder vh = getChildViewHolderInt(view);
            vh.addFlags(ViewHolder.FLAG_IGNORE);
            mRecyclerView.mViewInfoStore.removeViewHolder(vh);
        }

        /**
         * View can be scrapped and recycled again.
         * <p>
         * Note that calling this method removes all information in the view holder.
         * <p>
         * You can call this method only if your LayoutManger is in onLayout or onScroll callback.
         *
         * @param view View to ignore.
         */
        public void stopIgnoringView(View view) {
            final ViewHolder vh = getChildViewHolderInt(view);
            vh.stopIgnoring();
            vh.resetInternal();
            vh.addFlags(ViewHolder.FLAG_INVALID);
        }

        /**
         * Temporarily detach and scrap all currently attached child views. Views will be scrapped
         * into the given Recycler. The Recycler may prefer to reuse scrap views before
         * other views that were previously recycled.
         *
         * @param recycler Recycler to scrap views into
         */
        public void detachAndScrapAttachedViews(Recycler recycler) {
            final int childCount = getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View v = getChildAt(i);
                scrapOrRecycleView(recycler, i, v);
            }
        }

        private void scrapOrRecycleView(Recycler recycler, int index, View view) {
            final ViewHolder viewHolder = getChildViewHolderInt(view);
            if (viewHolder.shouldIgnore()) {
                return;
            }
            if (viewHolder.isInvalid() && !viewHolder.isRemoved() &&
                    !mRecyclerView.mAdapter.hasStableIds()) {
                removeViewAt(index);
                recycler.recycleViewHolderInternal(viewHolder);
            } else {
                detachViewAt(index);
                recycler.scrapView(view);
                mRecyclerView.mViewInfoStore.onViewDetached(viewHolder);
            }
        }

        /**
         * Recycles the scrapped views.
         * <p>
         * When a view is detached and removed, it does not trigger a ViewGroup invalidate. This is
         * the expected behavior if scrapped views are used for animations. Otherwise, we need to
         * call remove and invalidate RecyclerView to ensure UI update.
         *
         * @param recycler Recycler
         */
        void removeAndRecycleScrapInt(Recycler recycler) {
            final int scrapCount = recycler.getScrapCount();
            // Loop backward, recycler might be changed by removeDetachedView()
            for (int i = scrapCount - 1; i >= 0; i--) {
                final View scrap = recycler.get
            }
        }


    }

    /**
     * {@link android.view.ViewGroup.MarginLayoutParams LayoutParams} subclass for children of
     * {@link RecyclerView}. Custom {@link LayoutManager layout managers} are encouraged
     * to create their own subclass of this <code>LayoutParams</code> class
     * to store any additional required per-child view metadata about the layout.
     */
    public static class LayoutParams extends MarginLayoutParams {
        ViewHolder mViewHolder;
        final Rect mDecorInsets = new Rect();
        boolean mInsetsDirty = true;

        // Flag is set to true if the view is bound while it is detached from RV.
        // In this case, we need to manually call invalidate after view is added to guarantee that
        // invalidation is populated through the View hierarchy
        boolean mPendingInvalidate = false;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        /**
         * Returns true if the view this LayoutParams is attached to needs to have its content
         * updated from the corresponding adapter.
         *
         * @return
         */
        public boolean viewNeedsUpdate() {
            return mViewHolder.needsUpdate();
        }

        /**
         * Returns true if the view this LayoutParams is attached to is now representing
         * potentially invalid data. A LayoutManager should scrap/recycle it.
         *
         * @return
         */
        public boolean isViewInvalid() {
            return mViewHolder.isInvalid();
        }

        /**
         * Returns true if the adapter data item corresponding to the view this LayoutParams
         * is attached to has been removed from the data set. A LayoutManager may choose to
         * treat it differently in order to animate its outgoing or disappearing state.
         *
         * @return true if the item the view corresponds to was removed from the data set
         */
        public boolean isItemRemoved() {
            return mViewHolder.isRemoved();
        }

        /**
         * Returns true if the adapter data item corresponding to the view this LayoutParams
         * is attached to has been changed in the data set. A LayoutManager may choose to
         * treat it differently in order to animate its changing state.
         *
         * @return true if the item the view corresponds to was changed in the data set
         */
        public boolean isItemChanged() {
            return mViewHolder.isUpdated();
        }

        /**
         * @deprecated use {@link #getViewLayoutPosition()} or {@link #getViewAdapterPosition()}
         */
        @Deprecated
        public int getViewPosition() {
            return mViewHolder.getPosition();
        }

        /**
         * Returns the adapter position that the view this LayoutParams is attached to corresponds
         * to as of latest layout calculation.
         *
         * @return the adapter position this view as of latest layout pass
         */
        public int getViewLayoutPosition() {
            return mViewHolder.getLayoutPosition();
        }

        /**
         * Returns the up-to-date adapter position that the view this LayoutParams is attached to
         * corresponds to.
         *
         * @return the up-to-date adapter position this view. It may return
         * {@link RecyclerView#NO_POSITION} if item represented by this View has been removed or
         * its up-to-date position cannot be calculated.
         */
        public int getViewAdapterPosition() {
            return mViewHolder.getAdapterPosition();
        }
    }

    /**
     * <p>Base class for smooth scrolling. Handles basic tracking of the target view position and
     * provides methods to trigger a programmatic scroll.</p>
     */
    public static abstract class SmoothScroller {
        private int mTargetPosition = RecyclerView.NO_POSITION;
        private RecyclerView mRecyclerView;
        private LayoutManager mLayoutManager;
        private boolean mPendingInitialRun;
        private boolean mRunning;
        private View mTargetView;
        private final Action mRecyclingAction;

        public SmoothScroller() {
            mRecyclingAction = new Action(0, 0);
        }

        /**
         * Starts a smooth scroll for the given target position.
         * <p>In each animation step, {@link RecyclerView} will check
         * for the target view and call either
         * {@link #onTargetFound(android.view.View, RecyclerView.State, SmoothScroller.Action)} or
         * {@link #onSeekTargetStep(int, int, RecyclerView.State, SmoothScroller.Action)} until
         * SmoothScroller is stopped.</p>
         * <p>
         * <p>Note that if RecyclerView finds the target view, it will automatically stop the
         * SmoothScroller. This <b>does not</b> mean that scroll will stop, it only means it will
         * stop calling SmoothScroller in each animation step.</p>
         *
         * @param recyclerView
         * @param layoutManager
         */
        void start(RecyclerView recyclerView, LayoutManager layoutManager) {
            mRecyclerView = recyclerView;
            mLayoutManager = layoutManager;
            if (mTargetPosition == RecyclerView.NO_POSITION) {
                throw new IllegalArgumentException("Invalid target position");
            }
            mRecyclerView.mState.mTargetPosition = mTargetPosition;
            mRunning = true;
            mPendingInitialRun = true;
            mTargetView = findViewByPosition(getTargetPosition());
            onStart();
            mRecyclerView.mViewFlinger.postOnAnimation();
        }

        public void setTargetPosition(int targetPosition) {
            this.mTargetPosition = targetPosition;
        }

        /**
         * Returns the adapter position of the target item
         *
         * @return Adapter position of the target item or
         * {@link RecyclerView#NO_POSITION} if no target view is set.
         */
        public int getTargetPosition() {
            return mTargetPosition;
        }

        /**
         * @return The LayoutManager to which this SmoothScroller is attached. Will return
         * <code>null</code> after the SmoothScroller is stopped.
         */
        @Nullable
        public LayoutManager getLayoutManager() {
            return mLayoutManager;
        }

        /**
         * Stops running the SmoothScroller in each animation callback. Note that this does not
         * cancel any existing {@link Action} updated by
         * {@link #onTargetFound(android.view.View, RecyclerView.State, SmoothScroller.Action)} or
         * {@link #onSeekTargetStep(int, int, RecyclerView.State, SmoothScroller.Action)}.
         */
        final protected void stop() {
            if (!mRunning) {
                return;
            }
            onStop();
            mRecyclerView.mState.mTargetPosition = RecyclerView.NO_POSITION;
            mTargetView = null;
            mTargetPosition = RecyclerView.NO_POSITION;
            mPendingInitialRun = false;
            mRunning = false;
            // trigger a cleanup
            mLayoutManager.onSmoothScrollerStopped(this);
            // clear references to avoid any potential leak by a custom smooth scroller
            mLayoutManager = null;
            mRecyclerView = null;
        }

        /**
         * Returns true if SmoothScroller has been started but has not received the first
         * animation
         * callback yet.
         *
         * @return True if this SmoothScroller is waiting to start
         */
        public boolean isPendingInitialRun() {
            return mPendingInitialRun;
        }


        /**
         * @return True if SmoothScroller is currently active
         */
        public boolean isRunning() {
            return mRunning;
        }

        /**
         * @param dx
         * @param dy
         */
        private void onAnimation(int dx, int dy) {
            final RecyclerView recyclerView = mRecyclerView;
            if (!mRunning || mTargetPosition == RecyclerView.NO_POSITION || recyclerView == null) {
                stop();
            }
            mPendingInitialRun = false;
            if (mTargetView != null) {
                //verify target position
                if (getChildPosition(mTargetView) == mTargetPosition) {
                    onTargetFound(mTargetView, recyclerView.mState, mRecyclingAction);
                    mRecyclingAction.runIfNecessary(recyclerView);
                    stop();
                } else {
                    mTargetView = null;
                }
            }
            if (mRunning) {
                onSeekTargetStep(dx, dy, recyclerView.mState, mRecyclingAction);
                boolean hadJumpTarget = mRecyclingAction.hasJumpTarget();
                mRecyclingAction.runIfNecessary(recyclerView);
                if (hadJumpTarget) {
                    // It is not stopped so needs to be restarted
                    if (mRunning) {
                        mPendingInitialRun = true;
                        recyclerView.mViewFlinger.postOnAnimation();
                    } else {
                        stop(); // done
                    }
                }
            }
        }

        public int getChildPosition(View view) {
            return mRecyclerView.getChildLayoutPosition(view);
        }

        /**
         * @see RecyclerView.LayoutManager#getChildCount()
         */
        public int getChildCount() {
            return mRecyclerView.mLayout.getChildCount();
        }

        /**
         * @see RecyclerView.LayoutManager#findViewByPosition(int)
         */
        public View findViewByPosition(int position) {
            return mRecyclerView.mLayout.findViewByPosition(position);
        }

        /**
         * @see RecyclerView#scrollToPosition(int)
         * @deprecated Use {@link Action#jumpTo(int)}.
         */
        @Deprecated
        public void instantScrollToPosition(int position) {
            mRecyclerView.scrollToPosition(position);
        }

        protected void onChildAttachedToWindow(View child) {
            if (getChildPosition(child) == getTargetPosition()) {
                mTargetView = child;
            }
        }

        protected void normalize(PointF scrollVector) {
            final double magnitude = Math.sqrt(scrollVector.x * scrollVector.x + scrollVector.y
                    * scrollVector.y);
            scrollVector.x /= magnitude;
            scrollVector.y /= magnitude;
        }

        /**
         * Called when smooth scroll is started. This might be a good time to do setup.
         */
        abstract protected void onStart();

        /**
         * Called when smooth scroller is stopped. This is a good place to cleanup your state etc.
         *
         * @see #stop()
         */
        abstract protected void onStop();

        /**
         * <p>RecyclerView will call this method each time it scrolls until it can find the target
         * position in the layout.</p>
         * <p>SmoothScroller should check dx, dy and if scroll should be changed, update the
         * provided {@link Action} to define the next scroll.</p>
         *
         * @param dx     Last scroll amount horizontally
         * @param dy     Last scroll amount vertically
         * @param state  Transient state of RecyclerView
         * @param action If you want to trigger a new smooth scroll and cancel the previous one,
         *               update this object.
         */
        abstract protected void onSeekTargetStep(int dx, int dy, State state, Action action);

        /**
         * Called when the target position is laid out. This is the last callback SmoothScroller
         * will receive and it should update the provided {@link Action} to define the scroll
         * details towards the target view.
         *
         * @param targetView The view element which render the target position.
         * @param state      Transient state of RecyclerView
         * @param action     Action instance that you should update to define final scroll action
         *                   towards the targetView
         */
        abstract protected void onTargetFound(View targetView, State state, Action action);


        /**
         * Holds information about a smooth scroll request by a {@link SmoothScroller}.
         */
        public static class Action {
            public static final int UNDEFINED_DURATION = Integer.MIN_VALUE;
            private int mDx;
            private int mDy;
            private int mDuration;
            private int mJumpToPosition = NO_POSITION;

            private Interpolator mInterpolator;

            private boolean changed = false;

            // we track this variable to inform custom implementer if they are updating the action
            // in every animation callback
            private int consecutiveUpdates = 0;

            /**
             * @param dx Pixels to scroll horizontally
             * @param dy Pixels to scroll vertically
             */
            public Action(int dx, int dy) {
                this(dx, dy, UNDEFINED_DURATION, null);
            }

            /**
             * @param dx       Pixels to scroll horizontally
             * @param dy       Pixels to scroll vertically
             * @param duration Duration of the animation in milliseconds
             */
            public Action(int dx, int dy, int duration) {
                this(dx, dy, duration, null);
            }

            /**
             * @param dx           Pixels to scroll horizontally
             * @param dy           Pixels to scroll vertically
             * @param duration     Duration of the animation in milliseconds
             * @param interpolator Interpolator to be used when calculating scroll position in each
             *                     animation step
             */
            public Action(int dx, int dy, int duration, Interpolator interpolator) {
                mDx = dx;
                mDy = dy;
                mDuration = duration;
                mInterpolator = interpolator;
            }

            /**
             * Instead of specifying pixels to scroll, use the target position to jump using
             * {@link RecyclerView#scrollToPosition(int)}.
             * <p>
             * You may prefer using this method if scroll target is really far away and you prefer
             * to jump to a location and smooth scroll afterwards.
             * <p>
             * Note that calling this method takes priority over other update methods such as
             * {@link #update(int, int, int, Interpolator)}, {@link #setX(float)},
             * {@link #setY(float)} and #{@link #setInterpolator(Interpolator)}. If you call
             * {@link #jumpTo(int)}, the other changes will not be considered for this animation
             * frame.
             *
             * @param targetPosition targetPosition The target item position to scroll to using instant scrolling.
             */
            public void jumpTo(int targetPosition) {
                mJumpToPosition = targetPosition;
            }

            boolean hasJumpTarget() {
                return mJumpToPosition >= 0;
            }

            void runIfNecessary(RecyclerView recyclerView) {
                if (hasJumpTarget()) {
                    final int position = mJumpToPosition;
                    mJumpToPosition = NO_POSITION;
                    recyclerView.jumpToPositionForSmoothScroller(position);
                    changed = false;
                    return;
                }
                if (changed) {
                    validate();
                    if (mInterpolator == null) {
                        if (mDuration == UNDEFINED_DURATION) {
                            recyclerView.mViewFlinger.smoothScrollBy(mDx, mDy);
                        } else {
                            recyclerView.mViewFlinger.smoothScrollBy(mDx, mDy, mDuration);
                        }
                    } else {
                        recyclerView.mViewFlinger.smoothScrollBy(mDx, mDy, mDuration, mInterpolator);
                    }
                    consecutiveUpdates++;
                    if (consecutiveUpdates > 10) {
                        // A new action is being set in every animation step. This looks like a bad
                        // implementation. Inform developer.
                        Log.e("lll", "Smooth Scroll action is being updated too frequently. Make sure"
                                + " you are not changing it unless necessary");
                    }
                    changed = false;
                } else {
                    consecutiveUpdates = 0;
                }
            }

            private void validate() {
                if (mInterpolator != null && mDuration < 1) {
                    throw new IllegalStateException("If you provide an interpolator, you must"
                            + " set a positive duration");
                } else if (mDuration < 1) {
                    throw new IllegalStateException("Scroll duration must be a positive number");
                }
            }

            public int getDx() {
                return mDx;
            }

            public void setDx(int dx) {
                changed = true;
                mDx = dx;
            }

            public int getDy() {
                return mDy;
            }

            public void setDy(int dy) {
                changed = true;
                mDy = dy;
            }

            public int getDuration() {
                return mDuration;
            }

            public void setDuration(int duration) {
                changed = true;
                mDuration = duration;
            }

            public Interpolator getInterpolator() {
                return mInterpolator;
            }

            /**
             * Sets the interpolator to calculate scroll steps
             *
             * @param interpolator The interpolator to use. If you specify an interpolator, you must
             *                     also set the duration.
             * @see #setDuration(int)
             */
            public void setInterpolator(Interpolator interpolator) {
                changed = true;
                mInterpolator = interpolator;
            }

            /**
             * Updates the action with given parameters.
             *
             * @param dx           Pixels to scroll horizontally
             * @param dy           Pixels to scroll vertically
             * @param duration     Duration of the animation in milliseconds
             * @param interpolator Interpolator to be used when calculating scroll position in each
             *                     animation step
             */
            public void update(int dx, int dy, int duration, Interpolator interpolator) {
                mDx = dx;
                mDy = dy;
                mDuration = duration;
                mInterpolator = interpolator;
                changed = true;
            }

        }

        /**
         * An interface which is optionally implemented by custom {@link RecyclerView.LayoutManager}
         * to provide a hint to a {@link SmoothScroller} about the location of the target position.
         */
        public interface ScrollVectorProvider {
            /**
             * Should calculate the vector that points to the direction where the target position
             * can be found.
             * <p>
             * This method is used by the {@link LinearSmoothScroller} to initiate a scroll towards
             * the target position.
             * <p>
             * The magnitude of the vector is not important. It is always normalized before being
             * used by the {@link LinearSmoothScroller}.
             * <p>
             * LayoutManager should not check whether the position exists in the adapter or not.
             *
             * @param targetPosition the target position to which the returned vector should point
             * @return the scroll vector for a given position.
             */
            PointF computeScrollVectorForPosition(int targetPosition);
        }
    }


    /**
     * Observer base class for watching changes to an {@link RecyclerView.Adapter}.
     * See {@link RecyclerView.Adapter#registerAdapterDataObserver(RecyclerView.AdapterDataObserver)}.
     */
    public static abstract class AdapterDataObserver {
        public void onChange() {

        }

        public void onItemRangeChanged(int positionStart, int itemCount) {

        }

        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            // fallback to onItemRangeChanged(positionStart, itemCount) if app
            // does not override this method.
            onItemRangeChanged(positionStart, itemCount);
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {

        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {

        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            // do nothing
        }

    }

    private class RecyclerViewDataObserver extends AdapterDataObserver {
        RecyclerViewDataObserver() {
        }

        @Override
        public void onChange() {
            assertNotInLayoutOrScroll(null);
            if (mAdapter.hasStableIds()) {
                // TODO Determine what actually changed.
                // This is more important to implement now since this callback will disable all
                // animations because we cannot rely on positions.
                mState.mStructureChanged = true;
                setDataSetChangedAfterLayout();
            } else {
                mState.mStructureChanged = true;
                setDataSetChangedAfterLayout();
            }
            if (!mAdapterHelper.hasPendingUpdates()) {
                requestLayout();
            }
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            assertNotInLayoutOrScroll(null);
            if (mAdapterHelper.onItemRangeChanged(positionStart, itemCount, payload)) {
                triggerUpdateProcessor();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            assertNotInLayoutOrScroll(null);
            if (mAdapterHelper.onItemRangeInserted(positionStart, itemCount)) {
                triggerUpdateProcessor();
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            assertNotInLayoutOrScroll(null);
            if (mAdapterHelper.onItemRangeRemoved(positionStart, itemCount)) {
                triggerUpdateProcessor();
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            assertNotInLayoutOrScroll(null);
            if (mAdapterHelper.onItemRangeMoved(fromPosition, toPosition, itemCount)) {
                triggerUpdateProcessor();
            }
        }

        void triggerUpdateProcessor() {
            if (POST_UPDATES_ON_ANIMATION && mHasFixedSize && mIsAttached) {
                ViewCompat.postOnAnimation(RecyclerView.this, mUpdateChildViewsRunnable);
            } else {
                mAdapterUpdateDuringMeasure = true;
                requestLayout();
            }
        }
    }

    /**
     * RecycledViewPool lets you share Views between multiple RecyclerViews.
     * <p>
     * If you want to recycle views across RecyclerViews, create an instance of RecycledViewPool
     * and use {@link RecyclerView#setRecycledViewPool(RecycledViewPool)}.
     * <p>
     * RecyclerView automatically creates a pool for itself if you don't provide one.
     */
    public static class RecycledViewPool {
        private SparseArray<ArrayList<ViewHolder>> mScrap = new SparseArray<>();
        private SparseIntArray mMaxScrap = new SparseIntArray();
        private int mAttachCount = 0;
        private static final int DEFAULT_MAX_SCRAP = 5;

        private void clear() {
            mScrap.clear();
        }

        public void setMaxRecycledViews(int viewType, int max) {
            mMaxScrap.put(viewType, max);
            final ArrayList<ViewHolder> scrapHeap = mScrap.get(viewType);
            if (scrapHeap != null) {
                while (scrapHeap.size() > max) {
                    scrapHeap.remove(scrapHeap.size() - 1);
                }
            }
        }

        public ViewHolder getRecycledView(int viewType) {
            final ArrayList<ViewHolder> scrapHeap = mScrap.get(viewType);
            if (scrapHeap != null && !scrapHeap.isEmpty()) {
                final int index = scrapHeap.size() - 1;
                final ViewHolder scrap = scrapHeap.get(index);
                scrapHeap.remove(index);
                return scrap;
            }
            return null;
        }

        int size() {
            int count = 0;
            for (int i = 0; i < mScrap.size(); i++) {
                ArrayList<ViewHolder> viewHolders = mScrap.valueAt(i);
                if (viewHolders != null) {
                    count += viewHolders.size();
                }
            }
            return count;
        }

        public void putRecycledView(ViewHolder scrap) {
            final int viewType = scrap.getItemViewType();
            final ArrayList scrapHeap = getScrapHeapForType(viewType);
            if (mMaxScrap.get(viewType) <= scrapHeap.size()) {
                return;
            }
            scrap.resetInternal();
            scrapHeap.add(scrap);
        }

        void attach(Adapter adapter) {
            mAttachCount++;
        }

        void detach() {
            mAttachCount--;
        }

        /**
         * Detaches the old adapter and attaches the new one.
         * <p>
         * RecycledViewPool will clear its cache if it has only one adapter attached and the new
         * adapter uses a different ViewHolder than the oldAdapter.
         *
         * @param oldAdapter             The previous adapter instance. Will be detached.
         * @param newAdapter             The new adapter instance. Will be attached.
         * @param compatibleWithPrevious True if both oldAdapter and newAdapter are using the same
         *                               ViewHolder and view types.
         */
        void onAdapterChanged(Adapter oldAdapter, Adapter newAdapter, boolean compatibleWithPrevious) {
            if (oldAdapter != null) {
                detach();
            }
            if (!compatibleWithPrevious && mAttachCount == 0) {
                clear();
            }
            if (newAdapter != null) {
                attach(newAdapter);
            }
        }

        private ArrayList<ViewHolder> getScrapHeapForType(int viewType) {
            ArrayList<ViewHolder> scrap = mScrap.get(viewType);
            if (scrap == null) {
                scrap = new ArrayList<>();
                mScrap.put(viewType, scrap);
                if (mMaxScrap.indexOfKey(viewType) < 0) {
                    mMaxScrap.put(viewType, DEFAULT_MAX_SCRAP);
                }
            }
            return scrap;
        }
    }

    static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        public boolean haseObservers() {
            return mObservers.isEmpty();
        }

        public void notifyChanged() {
            // since onChanged() is implemented by the app, it could do anything, including
            // removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
            for (int i = mObservers.size() - 1; i >= 0; i++) {
                mObservers.get(i).onChange();
            }
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(positionStart, itemCount, null);
        }

        public void notifyItemRangeChanged(int positionStart, int itemCount, Object payload) {
            // since onItemRangeChanged() is implemented by the app, it could do anything, including
            // removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeChanged(positionStart, itemCount, payload);
            }
        }

        public void notifyItemRangeInserted(int positionStart, int itemCount) {
            // since onItemRangeInserted() is implemented by the app, it could do anything,
            // including removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeInserted(positionStart, itemCount);
            }
        }

        public void notifyItemRangeRemoved(int positionStart, int itemCount) {
            // since onItemRangeRemoved() is implemented by the app, it could do anything, including
            // removing itself from {@link mObservers} - and that could cause problems if
            // an iterator is used on the ArrayList {@link mObservers}.
            // to avoid such problems, just march thru the list in the reverse order.
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeRemoved(positionStart, itemCount);
            }
        }

        public void notifyItemMoved(int fromPosition, int toPosition) {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onItemRangeMoved(fromPosition, toPosition, 1);
            }
        }
    }

    /**
     * This is public so that the CREATOR can be access on cold launch.
     */
    public static class SavedState extends AbsSavedState {

        Parcelable mLayoutState;

        /**
         * called by CREATOR
         */
        SavedState(Parcel in, ClassLoader loader) {
            super(in, loader);
            mLayoutState = in.readParcelable(
                    loader != null ? loader : LayoutManager.class.getClassLoader());
        }

        /**
         * Called by onSaveInstanceState
         */
        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(mLayoutState, 0);
        }

        void copyFrom(SavedState other) {
            mLayoutState = other.mLayoutState;
        }

        public static final Creator<SavedState> CREATOR = ParcelableCompat.newCreator(
                new ParcelableCompatCreatorCallbacks<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel in, ClassLoader loader) {
                        return new SavedState(in, loader);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                });
    }

    /**
     * <p>Contains useful information about the current RecyclerView state like target scroll
     * position or view focus. State object can also keep arbitrary data, identified by resource
     * ids.</p>
     * <p>Often times, RecyclerView components will need to pass information between each other.
     * To provide a well defined data bus between components, RecyclerView passes the same State
     * object to component callbacks and these components can use it to exchange data.</p>
     * <p>If you implement custom components, you can use State's put/get/remove methods to pass
     * data between your components without needing to manage their lifecycles.</p>
     */
    public static class State {
        static final int STEP_START = 1;
        static final int STEP_LAYOUT = 1 << 1;
        static final int STEP_ANIMATIONS = 1 << 2;

        @IntDef(flag = true, value = {
                STEP_START, STEP_LAYOUT, STEP_ANIMATIONS
        })
        @Retention(RetentionPolicy.SOURCE)
        @interface LayoutState {
        }

        private int mTargetPosition = RecyclerView.NO_POSITION;

        @LayoutState
        int mLayoutStep = STEP_START;

        private SparseArray<Object> mData;

        /**
         * Number of items adapter has.
         */
        int mItemCount = 0;

        /**
         * Number of items adapter had in the previous layout.
         */
        int mPreviousLayoutItemCount = 0;

        /**
         * Number of items that were NOT laid out but has been deleted from the adapter after the
         * previous layout.
         */
        int mDeletedInvisibleItemCountSincePreviousLayout = 0;

        boolean mStructureChanged = false;

        boolean mInPreLayout = false;

        boolean mRunSimpleAnimations = false;

        boolean mRunPredictiveAnimations = false;

        boolean mTrackOldChangeHolders = false;

        boolean mIsMeasuring = false;

        /**
         * This data is saved before a layout calculation happens. After the layout is finished,
         * if the previously focused view has been replaced with another view for the same item, we
         * move the focus to the new item automatically.
         */
        int mFocusedItemPosition;
        long mFocusedItemId;
        // when a sub child has focus, record its id and see if we can directly request focus on
        // that one instead
        int mFocusedSubChildId;

        void assertLayoutStep(int accepted) {
            if ((accepted & mLayoutStep) == 0) {
                throw new IllegalStateException("Layout state should be one of "
                        + Integer.toBinaryString(accepted) + " but it is "
                        + Integer.toBinaryString(mLayoutStep));
            }
        }

        State reset() {
            mTargetPosition = RecyclerView.NO_POSITION;
            if (mData != null) {
                mData.clear();
            }
            mItemCount = 0;
            mStructureChanged = false;
            mIsMeasuring = false;
            return this;
        }

        /**
         * Returns true if the RecyclerView is currently measuring the layout. This value is
         * {@code true} only if the LayoutManager opted into the auto measure API and RecyclerView
         * has non-exact measurement specs.
         * <p>
         * Note that if the LayoutManager supports predictive animations and it is calculating the
         * pre-layout step, this value will be {@code false} even if the RecyclerView is in
         * {@code onMeasure} call. This is because pre-layout means the previous state of the
         * RecyclerView and measurements made for that state cannot change the RecyclerView's size.
         * LayoutManager is always guaranteed to receive another call to
         * {@link LayoutManager#onLayoutChildren(Recycler, State)} when this happens.
         *
         * @return True if the RecyclerView is currently calculating its bounds, false otherwise.
         */
        public boolean isMeasuring() {
            return mIsMeasuring;
        }

        /**
         * Returns true if
         *
         * @return
         */
        public boolean isPreLayout() {
            return mInPreLayout;
        }

        /**
         * Returns whether RecyclerView will run predictive animations in this layout pass
         * or not.
         *
         * @return true if RecyclerView is calculating predictive animations to be run at the end
         * of the layout pass.
         */
        public boolean willRunPredictiveAnimations() {
            return mRunPredictiveAnimations;
        }

        /**
         * Returns whether RecyclerView will run simple animations in this layout pass
         * or not.
         *
         * @return true if RecyclerView is calculating simple animations to be run at the end of
         * the layout pass.
         */
        public boolean willRunSimpleAnimations() {
            return mRunSimpleAnimations;
        }

        public void remove(int resourceId) {
            if (mData == null) {
                return;
            }
            mData.remove(resourceId);
        }

        /**
         * Gets the Object mapped from the specified id, or <code>null</code>
         * if no such data exists.
         *
         * @param resourceId Id of the resource you want to remove. It is suggested to use R.id.*
         *                   to
         *                   preserve cross functionality and avoid conflicts.
         */
        public <T> T get(int resourceId) {
            if (mData == null) {
                return null;
            }
            return (T) mData.get(resourceId);
        }

        /**
         * Adds a mapping from the specified id to the specified value, replacing the previous
         * mapping from the specified key if there was one.
         *
         * @param resourceId Id of the resource you want to add. It is suggested to use R.id.* to
         *                   preserve cross functionality and avoid conflicts.
         * @param data       The data you want to associate with the resourceId.
         */
        public void put(int resourceId, Object data) {
            if (mData == null) {
                mData = new SparseArray<>();
            }
            mData.put(resourceId, data);
        }

        /**
         * If scroll is triggered to make a certain item visible, this value will return the
         * adapter index of that item.
         *
         * @return Adapter index of the target item or
         * {@link RecyclerView#NO_POSITION} if there is no target
         * position.
         */
        public int getTargetScrollPosition() {
            return mTargetPosition;
        }

        /**
         * Returns if current scroll has a target position.
         *
         * @return true if scroll is being triggered to make a certain position visible
         * @see #getTargetScrollPosition()
         */
        public boolean hasTargetScrollPosition() {
            return mTargetPosition != RecyclerView.NO_POSITION;
        }

        /**
         * @return true if the structure of the data set has changed since the last call to
         * onLayoutChildren, false otherwise
         */
        public boolean didStructureChange() {
            return mStructureChanged;
        }

        /**
         * Returns the total number of items that can be laid out. Note that this number is not
         * necessarily equal to the number of items in the adapter, so you should always use this
         * number for your position calculations and never access the adapter directly.
         * <p>
         * RecyclerView listens for Adapter's notify events and calculates the effects of adapter
         * data changes on existing Views. These calculations are used to decide which animations
         * should be run.
         * <p>
         * To support predictive animations, RecyclerView may rewrite or reorder Adapter changes to
         * present the correct state to LayoutManager in pre-layout pass.
         * <p>
         * For example, a newly added item is not included in pre-layout item count because
         * pre-layout reflects the contents of the adapter before the item is added. Behind the
         * scenes, RecyclerView offsets {@link Recycler#getViewForPosition(int)} calls such that
         * LayoutManager does not know about the new item's existence in pre-layout. The item will
         * be available in second layout pass and will be included in the item count. Similar
         * adjustments are made for moved and removed items as well.
         * <p>
         * You can get the adapter's item count via {@link LayoutManager#getItemCount()} method.
         *
         * @return The number of items currently available
         * @see LayoutManager#getItemCount()
         */
        public int getItemCount() {
            return mInPreLayout ?
                    (mPreviousLayoutItemCount - mDeletedInvisibleItemCountSincePreviousLayout) :
                    mItemCount;
        }
    }

    /**
     * This class defines the behavior of fling if the developer wishes to handle it.
     * <p>
     * Subclasses of {@link OnFlingListener} can be used to implement custom fling behavior.
     *
     * @see #setOnFlingListener(OnFlingListener)
     */
    public static abstract class OnFlingListener {
        /**
         * Override this to handle a fling given the velocities in both x and y directions.
         * Note that this method will only be called if the associated {@link LayoutManager}
         * supports scrolling and the fling is not handled by nested scrolls first.
         *
         * @param velocityX the fling velocity on the X axis
         * @param velocityY the fling velocity on the Y axis
         * @return true if the fling washandled, false otherwise.
         */
        public abstract boolean onFling(int velocityX, int velocityY);
    }

    /**
     * An ItemDecoration allows the application to add a special drawing and layout offset
     * to specific item views from the adapter's data set. This can be useful for drawing dividers
     * between items, highlights, visual grouping boundaries and more.
     * <p>
     * <p>All ItemDecorations are drawn in the order they were added, before the item
     * views (in {@link ItemDecoration#onDraw(Canvas, RecyclerView, RecyclerView.State) onDraw()}
     * and after the items (in {@link ItemDecoration#onDrawOver(Canvas, RecyclerView,
     * RecyclerView.State)}.</p>
     */
    public static abstract class ItemDecoration {
        /**
         * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
         * Any content drawn by this method will be drawn before the item views are drawn,
         * and will thus appear underneath the views.
         *
         * @param c      Canvas to draw into
         * @param parent RecyclerView this ItemDecoration is drawing into
         * @param state  The current state of RecyclerView
         */
        public void onDraw(Canvas c, RecyclerView parent, State state) {
            onDraw(c, parent);
        }

        /**
         * @deprecated Override {@link #onDraw(Canvas, RecyclerView, RecyclerView.State)}
         */
        @Deprecated
        public void onDraw(Canvas c, RecyclerView parent) {
        }

        /**
         * Draw any appropriate decorations into the Canvas supplied to the RecyclerView.
         * Any content drawn by this method will be drawn after the item views are drawn
         * and will thus appear over the views.
         *
         * @param c      Canvas to draw into
         * @param parent RecyclerView this ItemDecoration is drawing into
         * @param state  The current state of RecyclerView.
         */
        public void onDrawOver(Canvas c, RecyclerView parent, State state) {
            onDrawOver(c, parent);
        }

        /**
         * @deprecated Override {@link #onDrawOver(Canvas, RecyclerView, RecyclerView.State)}
         */
        @Deprecated
        public void onDrawOver(Canvas c, RecyclerView parent) {
        }


        /**
         * @deprecated Use {@link #getItemOffsets(Rect, View, RecyclerView, State)}
         */
        @Deprecated
        public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
            outRect.set(0, 0, 0, 0);
        }

        /**
         * Retrieve any offsets for the given item. Each field of <code>outRect</code> specifies
         * the number of pixels that the item view should be inset by, similar to padding or margin.
         * The default implementation sets the bounds of outRect to 0 and returns.
         * <p>
         * <p>
         * If this ItemDecoration does not affect the positioning of item views, it should set
         * all four fields of <code>outRect</code> (left, top, right, bottom) to zero
         * before returning.
         * <p>
         * <p>
         * If you need to access Adapter for additional data, you can call
         * {@link RecyclerView#getChildAdapterPosition(View)} to get the adapter position of the
         * View.
         *
         * @param outRect Rect to receive the output.
         * @param view    The child view to decorate
         * @param parent  RecyclerView this ItemDecoration is decorating
         * @param state   The current state of RecyclerView.
         */
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            getItemOffsets(outRect, ((LayoutParams) view.getLayoutParams()).getViewLayoutPosition(),
                    parent);
        }
    }

    /**
     * An OnItemTouchListener allows the application to intercept touch events in progress at the
     * view hierarchy level of the RecyclerView before those touch events are considered for
     * RecyclerView's own scrolling behavior.
     * <p>
     * <p>This can be useful for applications that wish to implement various forms of gestural
     * manipulation of item views within the RecyclerView. OnItemTouchListeners may intercept
     * a touch interaction already in progress even if the RecyclerView is already handling that
     * gesture stream itself for the purposes of scrolling.</p>
     *
     * @see SimpleOnItemTouchListener
     */
    public interface OnItemTouchListener {
        /**
         * Silently observe and/or take over touch events sent to the RecyclerView
         * before they are handled by either the RecyclerView itself or its child views.
         * <p>
         * <p>The onInterceptTouchEvent methods of each attached OnItemTouchListener will be run
         * in the order in which each listener was added, before any other touch processing
         * by the RecyclerView itself or child views occurs.</p>
         *
         * @param e MotionEvent describing the touch event. All coordinates are in
         *          the RecyclerView's coordinate system.
         * @return true if this OnItemTouchListener wishes to begin intercepting touch events, false
         * to continue with the current behavior and continue observing future events in
         * the gesture.
         */
        boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e);

        /**
         * Process a touch event as part of a gesture that was claimed by returning true from
         * a previous call to {@link #onInterceptTouchEvent}.
         *
         * @param e MotionEvent describing the touch event. All coordinates are in
         *          the RecyclerView's coordinate system.
         */
        void onTouchEvent(RecyclerView rv, MotionEvent e);

        /**
         * Called when a child of RecyclerView does not want RecyclerView and its ancestors to
         * intercept touch events with
         * {@link ViewGroup#onInterceptTouchEvent(MotionEvent)}.
         *
         * @param disallowIntercept True if the child does not want the parent to
         *                          intercept touch events.
         * @see ViewParent#requestDisallowInterceptTouchEvent(boolean)
         */
        void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept);
    }

    /**
     * An implementation of {@link RecyclerView.OnItemTouchListener} that has empty method bodies and
     * default return values.
     * <p>
     * You may prefer to extend this class if you don't need to override all methods. Another
     * benefit of using this class is future compatibility. As the interface may change, we'll
     * always provide a default implementation on this class so that your code won't break when
     * you update to a new version of the support library.
     */
    public static class SimpleOnItemTouchListener implements OnItemTouchListener {

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    /**
     * An OnScrollListener can be added to a RecyclerView to receive messages when a scrolling event
     * has occurred on that RecyclerView.
     * <p>
     *
     * @see RecyclerView#addOnScrollListener(OnScrollListener)
     * @see RecyclerView#clearOnChildAttachStateChangeListeners()
     */
    public abstract static class OnScrollListener {

        /**
         * Callback method to be invoked when RecyclerView's scroll state changes.
         *
         * @param recyclerView The RecyclerView whose scroll state has changed.
         * @param newState     The updated scroll state. One of {@link #SCROLL_STATE_IDLE},
         *                     {@link #SCROLL_STATE_DRAGGING} or {@link #SCROLL_STATE_SETTLING}.
         */
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

        }

        /**
         * Callback method to be invoked when the RecyclerView has been scrolled. This will be
         * called after the scroll has completed.
         * <p>
         * This callback will also be called if visible item range changes after a layout
         * calculation. In that case, dx and dy will be 0.
         *
         * @param recyclerView The RecyclerView which scrolled.
         * @param dx           The amount of horizontal scroll.
         * @param dy           The amount of vertical scroll.
         */
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        }

    }

    /**
     * A RecyclerListener can be set on a RecyclerView to receive messages whenever
     * a view is recycled.
     *
     * @see RecyclerView#setRecyclerListener(RecyclerListener)
     */
    public interface RecyclerListener {
        /**
         * This method is called whenever the view in the ViewHolder is recycled.
         * <p>
         * RecyclerView calls this method right before clearing ViewHolder's internal data and
         * sending it to RecycledViewPool. This way, if ViewHolder was holding valid information
         * before being recycled, you can call {@link ViewHolder#getAdapterPosition()} to get
         * its adapter position.
         *
         * @param holder The ViewHolder containing the view that was recycled
         */
        void onViewRecycled(ViewHolder viewHolder);
    }

    /**
     * A Listener interface that can be attached to a RecylcerView to get notified
     * whenever a ViewHolder is attached to or detached from RecyclerView.
     */
    public interface OnChildAttachStateChangeListener {
        /**
         * Called when a view is attached to the RecyclerView.
         *
         * @param view The View which is attached to the RecyclerView
         */
        void onChildViewAttachedToWindow(View view);

        /**
         * Called when a view is detached from RecyclerView.
         *
         * @param view The View which is being detached from the RecyclerView
         */
        void onChildViewDetachedFromWindow(View view);
    }

    /**
     * This class defines the animations that take place on items as changes are made
     * to the adapter.
     * <p>
     * Subclasses of ItemAnimator can be used to implement custom animations for actions on
     * ViewHolder items. The RecyclerView will manage retaining these items while they
     * are being animated, but implementors must call {@link #dispatchAnimationFinished(ViewHolder)}
     * when a ViewHolder's animation is finished. In other words, there must be a matching
     * {@link #dispatchAnimationFinished(ViewHolder)} call for each
     * {@link #animateAppearance(ViewHolder, ItemHolderInfo, ItemHolderInfo) animateAppearance()},
     * {@link #animateChange(ViewHolder, ViewHolder, ItemHolderInfo, ItemHolderInfo)
     * animateChange()}
     * {@link #animatePersistence(ViewHolder, ItemHolderInfo, ItemHolderInfo) animatePersistence()},
     * and
     * {@link #animateDisappearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
     * animateDisappearance()} call.
     * <p>
     * <p>By default, RecyclerView uses {@link DefaultItemAnimator}.</p>
     *
     * @see #setItemAnimator(ItemAnimator)
     */
    public static abstract class ItemAnimator {
        /**
         * The Item represented by this ViewHolder is updated.
         * <p>
         *
         * @see #recordPreLayoutInformation(State, ViewHolder, int, List)
         */
        public static final int FLAG_CHANGED = ViewHolder.FLAG_UPDATE;

        /**
         * The Item represented by this ViewHolder is removed from the adapter.
         * <p>
         *
         * @see #recordPreLayoutInformation(State, ViewHolder, int, List)
         */
        public static final int FLAG_REMOVED = ViewHolder.FLAG_REMOVED;

        /**
         * Adapter {@link Adapter#notifyDataSetChanged()} has been called and the content
         * represented by this ViewHolder is invalid.
         * <p>
         *
         * @see #recordPreLayoutInformation(State, ViewHolder, int, List)
         */
        public static final int FLAG_INVALIDATED = ViewHolder.FLAG_INVALID;

        /**
         * The position of the Item represented by this ViewHolder has been changed. This flag is
         * not bound to {@link Adapter#notifyItemMoved(int, int)}. It might be set in response to
         * any adapter change that may have a side effect on this item. (e.g. The item before this
         * one has been removed from the Adapter).
         * <p>
         *
         * @see #recordPreLayoutInformation(State, ViewHolder, int, List)
         */
        public static final int FLAG_MOVED = ViewHolder.FLAG_MOVED;

        /**
         * This ViewHolder was not laid out but has been added to the layout in pre-layout state
         * by the {@link LayoutManager}. This means that the item was already in the Adapter but
         * invisible and it may become visible in the post layout phase. LayoutManagers may prefer
         * to add new items in pre-layout to specify their virtual location when they are invisible
         * (e.g. to specify the item should <i>animate in</i> from below the visible area).
         * <p>
         *
         * @see #recordPreLayoutInformation(State, ViewHolder, int, List)
         */
        public static final int FLAG_APPEARED_IN_PRE_LAYOUT
                = ViewHolder.FLAG_APPEARED_IN_PRE_LAYOUT;

        /**
         * The set of flags that might be passed to
         * {@link #recordPreLayoutInformation(State, ViewHolder, int, List)}.
         */
        @IntDef(flag = true, value = {
                FLAG_CHANGED, FLAG_REMOVED, FLAG_MOVED, FLAG_INVALIDATED,
                FLAG_APPEARED_IN_PRE_LAYOUT
        })
        @Retention(RetentionPolicy.SOURCE)
        public @interface AdapterChanges {
        }

        private ItemAnimatorListener mListener = null;

        private long mAddDuration = 120;
        private long mRemoveDuration = 120;
        private long mMoveDuration = 250;
        private long mChangeDuration = 250;

        private ArrayList<ItemAnimatorFinishedListener> mFinishedListeners = new ArrayList<>();

        /**
         * The interface to be implemented by listeners to animation events from this
         * ItemAnimator. This is used internally and is not intended for developers to
         * create directly.
         */
        interface ItemAnimatorListener {
            void onAnimationFinished(ViewHolder item);
        }

        /**
         * This interface is used to inform listeners when all pending or running animations
         * in an ItemAnimator are finished. This can be used, for example, to delay an action
         * in a data set until currently-running animations are complete.
         *
         * @see #isRunning(ItemAnimatorFinishedListener)
         */
        public interface ItemAnimatorFinishedListener {
            void onAnimationsFinished();
        }

        /**
         * A simple data structure that holds information about an item's bounds.
         * This information is used in calculating item animations. Default implementation of
         * {@link #recordPreLayoutInformation(RecyclerView.State, ViewHolder, int, List)} and
         * {@link #recordPostLayoutInformation(RecyclerView.State, ViewHolder)} returns this data
         * structure. You can extend this class if you would like to keep more information about
         * the Views.
         * <p>
         * If you want to provide your own implementation but still use `super` methods to record
         * basic information, you can override {@link #obtainHolderInfo()} to provide your own
         * instances.
         */
        public static class ItemHolderInfo {
            /**
             * The left edge of the View (excluding decorations)
             */
            public int left;

            /**
             * The top edge of the View (excluding decorations)
             */
            public int top;

            /**
             * The right edge of the View (excluding decorations)
             */
            public int right;

            /**
             * The bottom edge of the View (excluding decorations)
             */
            public int bottom;

            /**
             * The change flags that were passed to
             * {@link #recordPreLayoutInformation(RecyclerView.State, ViewHolder, int, List)}.
             */
            @AdapterChanges
            public int changeFlags;

            public ItemHolderInfo() {
            }

            /**
             * Sets the {@link #left}, {@link #top}, {@link #right} and {@link #bottom} values from
             * the given ViewHolder. Clears all {@link #changeFlags}.
             *
             * @param holder The ViewHolder whose bounds should be copied.
             * @return This {@link ItemHolderInfo}
             */
            public ItemHolderInfo setFrom(RecyclerView.ViewHolder holder) {
                return setFrom(holder, 0);
            }

            public ItemHolderInfo setFrom(RecyclerView.ViewHolder holder, @AdapterChanges int flags) {
                final View view = holder.itemView;
                this.left = view.getLeft();
                this.top = view.getTop();
                this.right = view.getRight();
                this.bottom = view.getBottom();
                return this;
            }
        }

        /**
         * Gets the current duration for which all move animations will run.
         *
         * @return The current move duration
         */
        public long getMoveDuration() {
            return mMoveDuration;
        }

        /**
         * Sets the duration for which all move animations will run.
         *
         * @param moveDuration The move duration
         */
        public void setMoveDuration(long moveDuration) {
            mMoveDuration = moveDuration;
        }

        /**
         * Gets the current duration for which all add animations will run.
         *
         * @return The current add duration
         */
        public long getAddDuration() {
            return mAddDuration;
        }

        /**
         * Sets the duration for which all add animations will run.
         *
         * @param addDuration The add duration
         */
        public void setAddDuration(long addDuration) {
            mAddDuration = addDuration;
        }

        /**
         * Gets the current duration for which all remove animations will run.
         *
         * @return The current remove duration
         */
        public long getRemoveDuration() {
            return mRemoveDuration;
        }

        /**
         * Sets the duration for which all remove animations will run.
         *
         * @param removeDuration The remove duration
         */
        public void setRemoveDuration(long removeDuration) {
            mRemoveDuration = removeDuration;
        }

        /**
         * Gets the current duration for which all change animations will run.
         *
         * @return The current change duration
         */
        public long getChangeDuration() {
            return mChangeDuration;
        }

        /**
         * Sets the duration for which all change animations will run.
         *
         * @param changeDuration The change duration
         */
        public void setChangeDuration(long changeDuration) {
            mChangeDuration = changeDuration;
        }

        /**
         * Internal only:
         * Sets the listener that must be called when the animator is finished
         * animating the item (or immediately if no animation happens). This is set
         * internally and is not intended to be set by external code.
         *
         * @param listener The listener that must be called.
         */
        void setListener(ItemAnimatorListener listener) {
            mListener = listener;
        }

        /**
         * Called by the RecyclerView before the layout begins. Item animator should record
         * necessary information about the View before it is potentially rebound, moved or removed.
         * <p>
         * The data returned from this method will be passed to the related <code>animate**</code>
         * methods.
         * <p>
         * Note that this method may be called after pre-layout phase if LayoutManager adds new
         * Views to the layout in pre-layout pass.
         * <p>
         * The default implementation returns an {@link ItemHolderInfo} which holds the bounds of
         * the View and the adapter change flags.
         *
         * @param state       The current State of RecyclerView which includes some useful data
         *                    about the layout that will be calculated.
         * @param viewHolder  The ViewHolder whose information should be recorded.
         * @param changeFlags Additional information about what changes happened in the Adapter
         *                    about the Item represented by this ViewHolder. For instance, if
         *                    item is deleted from the adapter, {@link #FLAG_REMOVED} will be set.
         * @param payloads    The payload list that was previously passed to
         *                    {@link Adapter#notifyItemChanged(int, Object)} or
         *                    {@link Adapter#notifyItemRangeChanged(int, int, Object)}.
         * @return An ItemHolderInfo instance that preserves necessary information about the
         * ViewHolder. This object will be passed back to related <code>animate**</code> methods
         * after layout is complete.
         * @see #recordPostLayoutInformation(State, ViewHolder)
         * @see #animateAppearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * @see #animateDisappearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * @see #animateChange(ViewHolder, ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * @see #animatePersistence(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         */
        public
        @NonNull
        ItemHolderInfo recordPreLayoutInformation(@NonNull State state,
                                                  @NonNull ViewHolder viewHolder, @AdapterChanges int changeFlags,
                                                  @NonNull List<Object> payloads) {
            return obtainHolderInfo().setFrom(viewHolder);
        }

        /**
         * Called by the RecyclerView after the layout is complete. Item animator should record
         * necessary information about the View's final state.
         * <p>
         * The data returned from this method will be passed to the related <code>animate**</code>
         * methods.
         * <p>
         * The default implementation returns an {@link ItemHolderInfo} which holds the bounds of
         * the View.
         *
         * @param state      The current State of RecyclerView which includes some useful data about
         *                   the layout that will be calculated.
         * @param viewHolder The ViewHolder whose information should be recorded.
         * @return An ItemHolderInfo that preserves necessary information about the ViewHolder.
         * This object will be passed back to related <code>animate**</code> methods when
         * RecyclerView decides how items should be animated.
         * @see #recordPreLayoutInformation(State, ViewHolder, int, List)
         * @see #animateAppearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * @see #animateDisappearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * @see #animateChange(ViewHolder, ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * @see #animatePersistence(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         */
        public
        @NonNull
        ItemHolderInfo recordPostLayoutInformation(@NonNull State state,
                                                   @NonNull ViewHolder viewHolder) {
            return obtainHolderInfo().setFrom(viewHolder);
        }

        /**
         * Called by the RecyclerView when a ViewHolder has disappeared from the layout.
         * <p>
         * This means that the View was a child of the LayoutManager when layout started but has
         * been removed by the LayoutManager. It might have been removed from the adapter or simply
         * become invisible due to other factors. You can distinguish these two cases by checking
         * the change flags that were passed to
         * {@link #recordPreLayoutInformation(State, ViewHolder, int, List)}.
         * <p>
         * Note that when a ViewHolder both changes and disappears in the same layout pass, the
         * animation callback method which will be called by the RecyclerView depends on the
         * ItemAnimator's decision whether to re-use the same ViewHolder or not, and also the
         * LayoutManager's decision whether to layout the changed version of a disappearing
         * ViewHolder or not. RecyclerView will call
         * {@link #animateChange(ViewHolder, ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateChange} instead of {@code animateDisappearance} if and only if the ItemAnimator
         * returns {@code false} from
         * {@link #canReuseUpdatedViewHolder(ViewHolder) canReuseUpdatedViewHolder} and the
         * LayoutManager lays out a new disappearing view that holds the updated information.
         * Built-in LayoutManagers try to avoid laying out updated versions of disappearing views.
         * <p>
         * If LayoutManager supports predictive animations, it might provide a target disappear
         * location for the View by laying it out in that location. When that happens,
         * RecyclerView will call {@link #recordPostLayoutInformation(State, ViewHolder)} and the
         * response of that call will be passed to this method as the <code>postLayoutInfo</code>.
         * <p>
         * ItemAnimator must call {@link #dispatchAnimationFinished(ViewHolder)} when the animation
         * is complete (or instantly call {@link #dispatchAnimationFinished(ViewHolder)} if it
         * decides not to animate the view).
         *
         * @param viewHolder     The ViewHolder which should be animated
         * @param preLayoutInfo  The information that was returned from
         *                       {@link #recordPreLayoutInformation(State, ViewHolder, int, List)}.
         * @param postLayoutInfo The information that was returned from
         *                       {@link #recordPostLayoutInformation(State, ViewHolder)}. Might be
         *                       null if the LayoutManager did not layout the item.
         * @return true if a later call to {@link #runPendingAnimations()} is requested,
         * false otherwise.
         */
        public abstract boolean animateDisappearance(@NonNull ViewHolder viewHolder,
                                                     @NonNull ItemHolderInfo preLayoutInfo, @Nullable ItemHolderInfo postLayoutInfo);

        /**
         * Called by the RecyclerView when a ViewHolder is added to the layout.
         * <p>
         * In detail, this means that the ViewHolder was <b>not</b> a child when the layout started
         * but has  been added by the LayoutManager. It might be newly added to the adapter or
         * simply become visible due to other factors.
         * <p>
         * ItemAnimator must call {@link #dispatchAnimationFinished(ViewHolder)} when the animation
         * is complete (or instantly call {@link #dispatchAnimationFinished(ViewHolder)} if it
         * decides not to animate the view).
         *
         * @param viewHolder     The ViewHolder which should be animated
         * @param preLayoutInfo  The information that was returned from
         *                       {@link #recordPreLayoutInformation(State, ViewHolder, int, List)}.
         *                       Might be null if Item was just added to the adapter or
         *                       LayoutManager does not support predictive animations or it could
         *                       not predict that this ViewHolder will become visible.
         * @param postLayoutInfo The information that was returned from {@link
         *                       #recordPreLayoutInformation(State, ViewHolder, int, List)}.
         * @return true if a later call to {@link #runPendingAnimations()} is requested,
         * false otherwise.
         */
        public abstract boolean animateAppearance(@NonNull ViewHolder viewHolder,
                                                  @Nullable ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo);

        /**
         * Called by the RecyclerView when a ViewHolder is present in both before and after the
         * layout and RecyclerView has not received a {@link Adapter#notifyItemChanged(int)} call
         * for it or a {@link Adapter#notifyDataSetChanged()} call.
         * <p>
         * This ViewHolder still represents the same data that it was representing when the layout
         * started but its position / size may be changed by the LayoutManager.
         * <p>
         * If the Item's layout position didn't change, RecyclerView still calls this method because
         * it does not track this information (or does not necessarily know that an animation is
         * not required). Your ItemAnimator should handle this case and if there is nothing to
         * animate, it should call {@link #dispatchAnimationFinished(ViewHolder)} and return
         * <code>false</code>.
         * <p>
         * ItemAnimator must call {@link #dispatchAnimationFinished(ViewHolder)} when the animation
         * is complete (or instantly call {@link #dispatchAnimationFinished(ViewHolder)} if it
         * decides not to animate the view).
         *
         * @param viewHolder     The ViewHolder which should be animated
         * @param preLayoutInfo  The information that was returned from
         *                       {@link #recordPreLayoutInformation(State, ViewHolder, int, List)}.
         * @param postLayoutInfo The information that was returned from {@link
         *                       #recordPreLayoutInformation(State, ViewHolder, int, List)}.
         * @return true if a later call to {@link #runPendingAnimations()} is requested,
         * false otherwise.
         */
        public abstract boolean animatePersistence(@NonNull ViewHolder viewHolder,
                                                   @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo);


        /**
         * Called by the RecyclerView when an adapter item is present both before and after the
         * layout and RecyclerView has received a {@link Adapter#notifyItemChanged(int)} call
         * for it. This method may also be called when
         * {@link Adapter#notifyDataSetChanged()} is called and adapter has stable ids so that
         * RecyclerView could still rebind views to the same ViewHolders. If viewType changes when
         * {@link Adapter#notifyDataSetChanged()} is called, this method <b>will not</b> be called,
         * instead, {@link #animateAppearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)} will be
         * called for the new ViewHolder and the old one will be recycled.
         * <p>
         * If this method is called due to a {@link Adapter#notifyDataSetChanged()} call, there is
         * a good possibility that item contents didn't really change but it is rebound from the
         * adapter. {@link DefaultItemAnimator} will skip animating the View if its location on the
         * screen didn't change and your animator should handle this case as well and avoid creating
         * unnecessary animations.
         * <p>
         * When an item is updated, ItemAnimator has a chance to ask RecyclerView to keep the
         * previous presentation of the item as-is and supply a new ViewHolder for the updated
         * presentation (see: {@link #canReuseUpdatedViewHolder(ViewHolder, List)}.
         * This is useful if you don't know the contents of the Item and would like
         * to cross-fade the old and the new one ({@link DefaultItemAnimator} uses this technique).
         * <p>
         * When you are writing a custom item animator for your layout, it might be more performant
         * and elegant to re-use the same ViewHolder and animate the content changes manually.
         * <p>
         * When {@link Adapter#notifyItemChanged(int)} is called, the Item's view type may change.
         * If the Item's view type has changed or ItemAnimator returned <code>false</code> for
         * this ViewHolder when {@link #canReuseUpdatedViewHolder(ViewHolder, List)} was called, the
         * <code>oldHolder</code> and <code>newHolder</code> will be different ViewHolder instances
         * which represent the same Item. In that case, only the new ViewHolder is visible
         * to the LayoutManager but RecyclerView keeps old ViewHolder attached for animations.
         * <p>
         * ItemAnimator must call {@link #dispatchAnimationFinished(ViewHolder)} for each distinct
         * ViewHolder when their animation is complete
         * (or instantly call {@link #dispatchAnimationFinished(ViewHolder)} if it decides not to
         * animate the view).
         * <p>
         * If oldHolder and newHolder are the same instance, you should call
         * {@link #dispatchAnimationFinished(ViewHolder)} <b>only once</b>.
         * <p>
         * Note that when a ViewHolder both changes and disappears in the same layout pass, the
         * animation callback method which will be called by the RecyclerView depends on the
         * ItemAnimator's decision whether to re-use the same ViewHolder or not, and also the
         * LayoutManager's decision whether to layout the changed version of a disappearing
         * ViewHolder or not. RecyclerView will call
         * {@code animateChange} instead of
         * {@link #animateDisappearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateDisappearance} if and only if the ItemAnimator returns {@code false} from
         * {@link #canReuseUpdatedViewHolder(ViewHolder) canReuseUpdatedViewHolder} and the
         * LayoutManager lays out a new disappearing view that holds the updated information.
         * Built-in LayoutManagers try to avoid laying out updated versions of disappearing views.
         *
         * @param oldHolder      The ViewHolder before the layout is started, might be the same
         *                       instance with newHolder.
         * @param newHolder      The ViewHolder after the layout is finished, might be the same
         *                       instance with oldHolder.
         * @param preLayoutInfo  The information that was returned from
         *                       {@link #recordPreLayoutInformation(State, ViewHolder, int, List)}.
         * @param postLayoutInfo The information that was returned from {@link
         *                       #recordPreLayoutInformation(State, ViewHolder, int, List)}.
         * @return true if a later call to {@link #runPendingAnimations()} is requested,
         * false otherwise.
         */
        public abstract boolean animateChange(@NonNull ViewHolder oldHolder, @NonNull ViewHolder newHolder,
                                              @NonNull ItemHolderInfo preLayoutInfo, @NonNull ItemHolderInfo postLayoutInfo);

        @ItemAnimator.AdapterChanges
        static int buildAdapterChangeFlagsForAnimations(ViewHolder viewHolder) {
            int flags = viewHolder.mFlags & (FLAG_INVALIDATED | FLAG_REMOVED | FLAG_CHANGED);
            if (viewHolder.isInvalid()) {
                return FLAG_INVALIDATED;
            }
            if ((flags & FLAG_INVALIDATED) == 0) {
                final int oldPos = viewHolder.getOldPosition();
                final int pos = viewHolder.getAdapterPosition();
                if (oldPos != NO_POSITION && pos != NO_POSITION && oldPos != pos) {
                    flags |= FLAG_MOVED;
                }
            }
            return flags;
        }

        /**
         * Called when there are pending animations waiting to be started. This state
         * is governed by the return values from
         * {@link #animateAppearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateAppearance()},
         * {@link #animateChange(ViewHolder, ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateChange()}
         * {@link #animatePersistence(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animatePersistence()}, and
         * {@link #animateDisappearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateDisappearance()}, which inform the RecyclerView that the ItemAnimator wants to be
         * called later to start the associated animations. runPendingAnimations() will be scheduled
         * to be run on the next frame.
         */
        abstract public void runPendingAnimations();

        /**
         * Method called when an animation on a view should be ended immediately.
         * This could happen when other events, like scrolling, occur, so that
         * animating views can be quickly put into their proper end locations.
         * Implementations should ensure that any animations running on the item
         * are canceled and affected properties are set to their end values.
         * Also, {@link #dispatchAnimationFinished(ViewHolder)} should be called for each finished
         * animation since the animations are effectively done when this method is called.
         *
         * @param item The item for which an animation should be stopped.
         */
        abstract public void endAnimation(ViewHolder item);

        /**
         * Method called when all item animations should be ended immediately.
         * This could happen when other events, like scrolling, occur, so that
         * animating views can be quickly put into their proper end locations.
         * Implementations should ensure that any animations running on any items
         * are canceled and affected properties are set to their end values.
         * Also, {@link #dispatchAnimationFinished(ViewHolder)} should be called for each finished
         * animation since the animations are effectively done when this method is called.
         */
        abstract public void endAnimations();

        /**
         * Method which returns whether there are any item animations currently running.
         * This method can be used to determine whether to delay other actions until
         * animations end.
         *
         * @return true if there are any item animations currently running, false otherwise.
         */
        abstract public boolean isRunning();

        /**
         * Method to be called by subclasses when an animation is finished.
         * <p>
         * For each call RecyclerView makes to
         * {@link #animateAppearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateAppearance()},
         * {@link #animatePersistence(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animatePersistence()}, or
         * {@link #animateDisappearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateDisappearance()}, there
         * should
         * be a matching {@link #dispatchAnimationFinished(ViewHolder)} call by the subclass.
         * <p>
         * For {@link #animateChange(ViewHolder, ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateChange()}, subclass should call this method for both the <code>oldHolder</code>
         * and <code>newHolder</code>  (if they are not the same instance).
         *
         * @param viewHolder The ViewHolder whose animation is finished.
         * @see #onAnimationFinished(ViewHolder)
         */
        public final void dispatchAnimationFinished(ViewHolder viewHolder) {
            onAnimationFinished(viewHolder);
            if (mListener != null) {
                mListener.onAnimationFinished(viewHolder);
            }
        }

        /**
         * Called after {@link #dispatchAnimationFinished(ViewHolder)} is called by the
         * ItemAnimator.
         *
         * @param viewHolder The ViewHolder whose animation is finished. There might still be other
         *                   animations running on this ViewHolder.
         * @see #dispatchAnimationFinished(ViewHolder)
         */
        public void onAnimationFinished(ViewHolder viewHolder) {
        }

        /**
         * Method to be called by subclasses when an animation is started.
         * <p>
         * For each call RecyclerView makes to
         * {@link #animateAppearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateAppearance()},
         * {@link #animatePersistence(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animatePersistence()}, or
         * {@link #animateDisappearance(ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateDisappearance()}, there should be a matching
         * {@link #dispatchAnimationStarted(ViewHolder)} call by the subclass.
         * <p>
         * For {@link #animateChange(ViewHolder, ViewHolder, ItemHolderInfo, ItemHolderInfo)
         * animateChange()}, subclass should call this method for both the <code>oldHolder</code>
         * and <code>newHolder</code> (if they are not the same instance).
         * <p>
         * If your ItemAnimator decides not to animate a ViewHolder, it should call
         * {@link #dispatchAnimationFinished(ViewHolder)} <b>without</b> calling
         * {@link #dispatchAnimationStarted(ViewHolder)}.
         *
         * @param viewHolder The ViewHolder whose animation is starting.
         * @see #onAnimationStarted(ViewHolder)
         */
        public final void dispatchAnimationStarted(ViewHolder viewHolder) {
            onAnimationStarted(viewHolder);
        }

        /**
         * Called when a new animation is started on the given ViewHolder.
         *
         * @param viewHolder The ViewHolder which started animating. Note that the ViewHolder
         *                   might already be animating and this might be another animation.
         * @see #dispatchAnimationStarted(ViewHolder)
         */
        public void onAnimationStarted(ViewHolder viewHolder) {

        }

        /**
         * Like {@link #isRunning()}, this method returns whether there are any item
         * animations currently running. Additionally, the listener passed in will be called
         * when there are no item animations running, either immediately (before the method
         * returns) if no animations are currently running, or when the currently running
         * animations are {@link #dispatchAnimationsFinished() finished}.
         * <p>
         * <p>Note that the listener is transient - it is either called immediately and not
         * stored at all, or stored only until it is called when running animations
         * are finished sometime later.</p>
         *
         * @param listener A listener to be called immediately if no animations are running
         *                 or later when currently-running animations have finished. A null listener is
         *                 equivalent to calling {@link #isRunning()}.
         * @return true if there are any item animations currently running, false otherwise.
         */
        public final boolean isRunning(ItemAnimatorFinishedListener listener) {
            boolean running = isRunning();
            if (listener != null) {
                if (!running) {
                    listener.onAnimationsFinished();
                } else {
                    mFinishedListeners.add(listener);
                }
            }
            return running;
        }

        /**
         * When an item is changed, ItemAnimator can decide whether it wants to re-use
         * the same ViewHolder for animations or RecyclerView should create a copy of the
         * item and ItemAnimator will use both to run the animation (e.g. cross-fade).
         * <p>
         * Note that this method will only be called if the {@link ViewHolder} still has the same
         * type ({@link Adapter#getItemViewType(int)}). Otherwise, ItemAnimator will always receive
         * both {@link ViewHolder}s in the
         * {@link #animateChange(ViewHolder, ViewHolder, ItemHolderInfo, ItemHolderInfo)} method.
         * <p>
         * If your application is using change payloads, you can override
         * {@link #canReuseUpdatedViewHolder(ViewHolder, List)} to decide based on payloads.
         *
         * @param viewHolder The ViewHolder which represents the changed item's old content.
         * @return True if RecyclerView should just rebind to the same ViewHolder or false if
         * RecyclerView should create a new ViewHolder and pass this ViewHolder to the
         * ItemAnimator to animate. Default implementation returns <code>true</code>.
         * @see #canReuseUpdatedViewHolder(ViewHolder, List)
         */
        public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder) {
            return true;
        }

        /**
         * When an item is changed, ItemAnimator can decide whether it wants to re-use
         * the same ViewHolder for animations or RecyclerView should create a copy of the
         * item and ItemAnimator will use both to run the animation (e.g. cross-fade).
         * <p>
         * Note that this method will only be called if the {@link ViewHolder} still has the same
         * type ({@link Adapter#getItemViewType(int)}). Otherwise, ItemAnimator will always receive
         * both {@link ViewHolder}s in the
         * {@link #animateChange(ViewHolder, ViewHolder, ItemHolderInfo, ItemHolderInfo)} method.
         *
         * @param viewHolder The ViewHolder which represents the changed item's old content.
         * @param payloads   A non-null list of merged payloads that were sent with change
         *                   notifications. Can be empty if the adapter is invalidated via
         *                   {@link RecyclerView.Adapter#notifyDataSetChanged()}. The same list of
         *                   payloads will be passed into
         *                   {@link RecyclerView.Adapter#onBindViewHolder(ViewHolder, int, List)}
         *                   method <b>if</b> this method returns <code>true</code>.
         * @return True if RecyclerView should just rebind to the same ViewHolder or false if
         * RecyclerView should create a new ViewHolder and pass this ViewHolder to the
         * ItemAnimator to animate. Default implementation calls
         * {@link #canReuseUpdatedViewHolder(ViewHolder)}.
         * @see #canReuseUpdatedViewHolder(ViewHolder)
         */
        public boolean canReuseUpdatedViewHolder(@NonNull ViewHolder viewHolder,
                                                 @NonNull List<Object> payloads) {
            return canReuseUpdatedViewHolder(viewHolder);
        }

        /**
         * This method should be called by ItemAnimator implementations to notify
         * any listeners that all pending and active item animations are finished.
         */
        public final void dispatchAnimationsFinished() {
            final int count = mFinishedListeners.size();
            for (int i = 0; i < count; i++) {
                mFinishedListeners.get(i).onAnimationsFinished();
            }
            mFinishedListeners.clear();
        }

        /**
         * Returns a new {@link ItemHolderInfo} which will be used to store information about the
         * ViewHolder. This information will later be passed into <code>animate**</code> methods.
         * <p>
         * You can override this method if you want to extend {@link ItemHolderInfo} and provide
         * your own instances.
         *
         * @return A new {@link ItemHolderInfo}.
         */
        public ItemHolderInfo obtainHolderInfo() {
            return new ItemHolderInfo();
        }

    }

    /**
     * Internal listener that manages items after animations finish. This is how items are
     * retained (not recycled) during animations, but allowed to be recycled afterwards.
     * It depends on the contract with the ItemAnimator to call the appropriate dispatch*Finished()
     * method on the animator's listener when it is done animating any item.
     */
    private class ItemAnimatorRestoreListener implements ItemAnimator.ItemAnimatorListener {

        ItemAnimatorRestoreListener() {

        }

        @Override
        public void onAnimationFinished(ViewHolder item) {
            item.setIsRecyclable(true);
            if (item.mShadowedHolder != null && item.mShadowingHolder == null) {
                item.mShadowedHolder = null;
            }
            // always null this because an OldViewHolder can never become NewViewHolder w/o being
            // recycled.
            item.mShadowingHolder = null;
            if (!item.shouldBeKeptAsChild()) {
                if (!removeAnimatingView(item.itemView) && item.isTmpDetached()) {
                    removeDetachedView(item.itemView, false);
                }
            }
        }
    }

    /**
     * A callback interface that can be used to alter the drawing order of RecyclerView children.
     * <p>
     * It works using the {@link ViewGroup#getChildDrawingOrder(int, int)} method, so any case
     * that applies to that method also applies to this callback. For example, changing the drawing
     * order of two views will not have any effect if their elevation values are different since
     * elevation overrides the result of this callback.
     */
    public interface ChildDrawingOrderCallback {
        /**
         * Returns the index of the child to draw for this iteration. Override this
         * if you want to change the drawing order of children. By default, it
         * returns i.
         *
         * @param i The current iteration.
         * @return The index of the child to draw this iteration.
         * @see RecyclerView#setChildDrawingOrderCallback(RecyclerView.ChildDrawingOrderCallback)
         */
        int onGetChildDrawingOrder(int childCount, int i);
    }


}
