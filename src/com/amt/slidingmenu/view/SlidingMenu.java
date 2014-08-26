/*
 * Copyright (C) 2013 
 *
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

package com.amt.slidingmenu.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.amt.slidingmenu.activity.SlidingActivity.PageViewState;
import com.amt.slidingmenu.R;

public class SlidingMenu extends RelativeLayout implements Runnable {
    private static final int VELOCITY = 50;

    private static final int SCROLL_DURATION = 500;

    private View mPagerView; // ViewPageFragment.
    private View mMenuView; // LeftMenuFragment.
    private View mDetailsView; // RightDetailsFragment.

    private LayoutParams[] mLayoutParams = new LayoutParams[4];

    private RelativeLayout mBackgroundShadeLayout;

    private Scroller mScroller;

    private VelocityTracker mVelocityTracker;

    private float mLastMotionX;
    private float mLastMotionY;

    private boolean mIsBeingDragged = true;

    private boolean mIsCanShowLeft = true;
    private boolean mIsCanShowRight = false;

    private PageViewState mPageViewState = PageViewState.FIRST;

    private enum SlidingState {
        LEFT_SHOW,
        PAGER_SHOW,
        RIGHT_SHOW
    }

    private SlidingState mSlidingState = SlidingState.PAGER_SHOW;

    public SlidingMenu(Context context) {
        super(context);
        init(context);
    }

    public SlidingMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        initLayoutParams(context);
        initShadeLayout(context);
        mScroller = new Scroller(getContext());
    }

    private void initLayoutParams(Context context) {
        WindowManager windowManager = ((Activity) context).getWindow().getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        // mMenuView LayoutParams.
        mLayoutParams[0] = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
        // LayoutParams.
        mLayoutParams[1] = new LayoutParams(screenWidth, screenHeight);
        mLayoutParams[1].addRule(RelativeLayout.CENTER_IN_PARENT);
        // mDetailsView LayoutParams.
        mLayoutParams[2] = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);
        mLayoutParams[2].addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        // mPagerView LayoutParams.
        mLayoutParams[3] = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    }

    private void initShadeLayout(Context context) {
        mBackgroundShadeLayout = new RelativeLayout(context);
        mBackgroundShadeLayout.setLayoutParams(mLayoutParams[1]);
        mBackgroundShadeLayout.addView(getShadeView(context), mLayoutParams[1]);
    }

    private View getShadeView(Context context) {
        View shadeView = new View(context);
        shadeView.setBackgroundDrawable(getResources().getDrawable(R.drawable.shade_bg));
        return shadeView;
    }

    /** Template method. */
    public void setViews(View left, View center, View right) {
        setLeftView(left);
        setRightView(right);
        setCenterView(center);
    }

    public void setLeftView(View leftView) {
        addView(leftView, mLayoutParams[0]);
        mMenuView = leftView;
    }

    public void setCenterView(View centerView) {
        addView(mBackgroundShadeLayout, mLayoutParams[1]);
        addView(centerView, mLayoutParams[3]);
        mPagerView = centerView;
        // Show the PagerView.
        mPagerView.bringToFront();
    }

    public void setRightView(View RightView) {
        addView(RightView, mLayoutParams[2]);
        mDetailsView = RightView;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        postInvalidate();
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                int oldX = mPagerView.getScrollX();
                int oldY = mPagerView.getScrollY();
                int x = mScroller.getCurrX();
                int y = mScroller.getCurrY();
                if (oldX != x || oldY != y) {
                    if (mPagerView != null) {
                        mPagerView.scrollTo(x, y);
                        mBackgroundShadeLayout.scrollTo(x + ((x < 0) ? 20 : -20), y);// 背景阴影右偏
                    }
                }
                invalidate();
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                mIsBeingDragged = false;
                break;

            case MotionEvent.ACTION_MOVE:
                final float dx = x - mLastMotionX;
                final float xDiff = Math.abs(dx);
                final float yDiff = Math.abs(y - mLastMotionY);
                if (xDiff > ViewConfiguration.get(getContext()).getScaledTouchSlop() &&
                        xDiff > yDiff) {
                    if ((mIsCanShowRight && (mPagerView.getScrollX() > 0 || dx < 0)) ||
                            (mIsCanShowLeft && (mPagerView.getScrollX() < 0 || dx > 0))) {
                        mIsBeingDragged = true;
                        mLastMotionX = x;
                    }
                }
                break;
        }
        return mIsBeingDragged;
    }

    @SuppressLint("Recycle")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionX = x;
                mLastMotionY = y;
                if ((mPagerView.getScrollX() == -getMenuViewWidth() && mLastMotionX < getMenuViewWidth())
                        ||
                        (mPagerView.getScrollX() == getDetailViewWidth() && mLastMotionX > getMenuViewWidth())) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsBeingDragged) {
                    float scrollX = fixScrollValue(mLastMotionX - x, mPagerView.getScrollX());
                    if (mPagerView != null) {
                        mPagerView.scrollTo((int) scrollX, mPagerView.getScrollY());
                        mBackgroundShadeLayout.scrollTo((int) scrollX + ((scrollX < 0) ? 20 : -20),
                                mPagerView.getScrollY());
                    }
                    mLastMotionX = x;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    mVelocityTracker.computeCurrentVelocity(100);
                    doActionUp(mVelocityTracker.getXVelocity(), mPagerView.getScrollX());
                }
                break;
        }
        return true;
    }

    private void doActionUp(float xVelocity, int oldScrollX) {
        if (oldScrollX >= 0 && mIsCanShowRight) {
            if (xVelocity < -VELOCITY || oldScrollX > getDetailViewWidth() / 2) {
                showRightDetailsView(getDetailViewWidth() - oldScrollX);
            } else if (xVelocity > VELOCITY || oldScrollX <= getDetailViewWidth() / 2) {
                hideRightDetailsView(oldScrollX);
            }
        }
        if (oldScrollX <= 0 && mIsCanShowLeft) { // left view
            if (xVelocity > VELOCITY || oldScrollX < -getMenuViewWidth() / 2) {
                showLeftMenuView(getMenuViewWidth() + oldScrollX);
            } else if (xVelocity < -VELOCITY || oldScrollX >= -getMenuViewWidth() / 2) {
                hideLeftMenuView(-oldScrollX);
            }
        }
    }

    private float fixScrollValue(float deltaX, float oldScrollX) {
        float scrollX = oldScrollX + deltaX;
        if (deltaX < 0 && oldScrollX < 0) { // slide right : show left or hide right
            scrollX = scrollX < -getMenuViewWidth() ? -getMenuViewWidth() : scrollX;
        } else if (deltaX > 0 && oldScrollX > 0) { // slide left : hide left or show right
            scrollX = scrollX > getDetailViewWidth() ? getDetailViewWidth() : scrollX;
        } else if (deltaX > 0 && oldScrollX <= 0 && scrollX >= 0 && mSlidingState == SlidingState.LEFT_SHOW) {
            scrollX = 0;
        } else if (deltaX < 0 && oldScrollX >= 0 && scrollX <= 0 && mSlidingState == SlidingState.RIGHT_SHOW) {
            scrollX = 0;
        }
        return scrollX;
    }

    private int getMenuViewWidth() {
        return (mMenuView == null) ? 0 : mMenuView.getWidth();
    }

    private int getDetailViewWidth() {
        return (mDetailsView == null) ? 0 : mDetailsView.getWidth();
    }

    private void smoothScrollTo(int dx) {
        mScroller.startScroll(mPagerView.getScrollX(), mPagerView.getScrollY(), dx,
                mPagerView.getScrollY(), SCROLL_DURATION);
        invalidate();
    }

    public void setCanSliding(PageViewState pageViewState) {
        mPageViewState = pageViewState;
        if (mSlidingState == SlidingState.LEFT_SHOW) {
            updateCanShowFlag(true, false);
        } else if (mSlidingState == SlidingState.RIGHT_SHOW) {
            updateCanShowFlag(false, true);
        } else {
            if (mPageViewState == PageViewState.SINGLE) {
                updateCanShowFlag(true, true);
            } else if (mPageViewState == PageViewState.FIRST) {
                updateCanShowFlag(true, false);
            } else if (mPageViewState == PageViewState.END) {
                updateCanShowFlag(false, true);
            } else if (mPageViewState == PageViewState.MIDDLE) {
                updateCanShowFlag(false, false);
            } else { // NONE : default can show left
                updateCanShowFlag(true, false);
            }
        }
        mMenuView.setVisibility(mIsCanShowLeft ? View.VISIBLE : View.INVISIBLE);
        mDetailsView.setVisibility(mIsCanShowRight ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateCanShowFlag(boolean isCanShowLeft, boolean isCanShowRight) {
        mIsCanShowLeft = isCanShowLeft;
        mIsCanShowRight = isCanShowRight;
    }

    /*
     * Show left menu view.
     */
    public void clickLeftButtonEvent(PageViewState pageViewState) {
        mPageViewState = pageViewState;
        setCanSliding(pageViewState);
        if (mPagerView.getScrollX() == 0) {
            mSlidingState = SlidingState.LEFT_SHOW;
            setCanSliding(mPageViewState);
            showLeftMenuView(mMenuView.getWidth());
        } else if (mPagerView.getScrollX() == -mMenuView.getWidth()) {
            hideLeftMenuView(mMenuView.getWidth());
        }
    }

    private void showLeftMenuView(int menuWidth) {
        mSlidingState = SlidingState.LEFT_SHOW;
        setCanSliding(mPageViewState);
        smoothScrollTo(-menuWidth);
        postDelayed(this, SCROLL_DURATION);
    }

    private void hideLeftMenuView(int menuWidth) {
        mSlidingState = SlidingState.PAGER_SHOW;
        smoothScrollTo(menuWidth);
        postDelayed(this, SCROLL_DURATION);
    }

    /* Show right details view. */
    public void clickRightBottonEvent(PageViewState pageViewState) {
        mPageViewState = pageViewState;
        setCanSliding(mPageViewState);
        if (mPagerView.getScrollX() == 0) {
            mSlidingState = SlidingState.RIGHT_SHOW;
            setCanSliding(mPageViewState);
            showRightDetailsView(mDetailsView.getWidth());
        } else if (mPagerView.getScrollX() == mDetailsView.getWidth()) {
            hideRightDetailsView(mDetailsView.getWidth());
        }
    }

    private void showRightDetailsView(int menuWidth) {
        mSlidingState = SlidingState.RIGHT_SHOW;
        smoothScrollTo(menuWidth);
        postDelayed(this, SCROLL_DURATION);
    }

    private void hideRightDetailsView(int menuWidth) {
        smoothScrollTo(-menuWidth);
        mSlidingState = SlidingState.PAGER_SHOW;
        postDelayed(this, SCROLL_DURATION); 
    }

    @Override
    public void run() {
        setCanSliding(mPageViewState);
    }
}
