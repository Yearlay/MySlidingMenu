/*
 * Copyright (C) 2012 yueyueniao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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

package com.amt.slidingmenu.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.amt.slidingmenu.fragment.LeftMenuFragment;
import com.amt.slidingmenu.fragment.RightDetailsFragment;
import com.amt.slidingmenu.fragment.ViewPageFragment;
import com.amt.slidingmenu.fragment.ViewPageFragment.MyPageChangeListener;
import com.amt.slidingmenu.view.SlidingMenu;
import com.amt.slidingmenu.R;

public class SlidingActivity extends FragmentActivity implements MyPageChangeListener {
    private SlidingMenu mSlidingMenu;
    private LeftMenuFragment mLeftFragment;
    private RightDetailsFragment mRightFragment;
    private ViewPageFragment mViewPageFragment;
    
    public enum PageViewState {
        SINGLE,
        FIRST,
        END,
        MIDDLE,
        NONE
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.main_activity);
        initView();
    }

    private void initView() {
        mSlidingMenu = (SlidingMenu) findViewById(R.id.slidingMenu);
        mSlidingMenu.setLeftView(getLayoutInflater().inflate(R.layout.left_frame, null));
        mSlidingMenu.setRightView(getLayoutInflater().inflate(R.layout.right_frame, null));
        mSlidingMenu.setCenterView(getLayoutInflater().inflate(R.layout.center_frame, null));

        mLeftFragment = new LeftMenuFragment();
        mRightFragment = new RightDetailsFragment();
        mViewPageFragment = new ViewPageFragment();
        mViewPageFragment.setMyPageChangeListener(this);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.left_frame, mLeftFragment);
        transaction.replace(R.id.right_frame, mRightFragment);
        transaction.replace(R.id.center_frame, mViewPageFragment);
        transaction.commit();
    }

    @Override
    protected void onResume() {
        mSlidingMenu.setCanSliding(getPageViewState());
        super.onResume();
    }

    public void showLeftMenu() {
        mSlidingMenu.clickLeftButtonEvent(getPageViewState());
    }

    public void showRightDetails() {
        mSlidingMenu.clickRightBottonEvent(getPageViewState());
    }

    @Override
    public void onPageSelected(int position) {
        mSlidingMenu.setCanSliding(getPageViewState());
    }
    
    public PageViewState getPageViewState() {
        PageViewState pageViewState;
        if (mViewPageFragment == null) {
            pageViewState = PageViewState.NONE;
        } else if (mViewPageFragment.isSingle()) {
            pageViewState = PageViewState.SINGLE;
        } else if (mViewPageFragment.isFirst()) {
            pageViewState = PageViewState.FIRST;
        } else if (mViewPageFragment.isEnd()) {
            pageViewState = PageViewState.END;
        } else {
            pageViewState = PageViewState.MIDDLE;
        }
        return pageViewState;
    }

}
