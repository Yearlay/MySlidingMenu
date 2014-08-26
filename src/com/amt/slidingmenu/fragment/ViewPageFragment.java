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

package com.amt.slidingmenu.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.amt.slidingmenu.activity.SlidingActivity;
import com.amt.slidingmenu.R;

public class ViewPageFragment extends Fragment implements OnClickListener,
                ViewPager.OnPageChangeListener{
    private MyFragmentPagerAdapter mAdapter;
    private ViewPager mPager;
    private ArrayList<Fragment> pagerItemList = new ArrayList<Fragment>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View viewRoot = inflater.inflate(R.layout.view_pager, null);
        
        ((Button) viewRoot.findViewById(R.id.showLeft)).setOnClickListener(this);
        ((Button) viewRoot.findViewById(R.id.showRight)).setOnClickListener(this);
        
        pagerItemList.add(new PageFragment1());  // Add page 1.
        pagerItemList.add(new PageFragment2());  // Add page 2.
        mAdapter = new MyFragmentPagerAdapter(getFragmentManager());  // new adapter.
        
        mPager = (ViewPager) viewRoot.findViewById(R.id.pager);
        mPager.setAdapter(mAdapter);
        mPager.setOnPageChangeListener(this);
        
        return viewRoot;
    }
    
    @Override
    public void onPageSelected(int position) {
        if (myPageChangeListener != null)
            myPageChangeListener.onPageSelected(position);
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
    }

    @Override
    public void onPageScrollStateChanged(int position) {
    }
    
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.showLeft:
                ((SlidingActivity) getActivity()).showLeftMenu();
                break;
            case R.id.showRight:
                ((SlidingActivity) getActivity()).showRightDetails();
                break;
            default:
                break;
        }
    }

    public boolean isFirst() {
        return (mPager.getCurrentItem() == 0);
    }

    public boolean isEnd() {
        return (mPager.getCurrentItem() == pagerItemList.size() - 1);
    }
    
    public boolean isSingle() {
        return (pagerItemList.size() == 1);
    }

    public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
        public MyFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return pagerItemList.size();
        }

        @Override
        public Fragment getItem(int position) {
            return pagerItemList.get(position < pagerItemList.size() ? position : 0);
        }
    }
    
    /**
     * Set the interface MyPageChangeListener. <br>
     * And define myPageChangeListener, setMyPageChangeListener.
     */
    private MyPageChangeListener myPageChangeListener;

    public void setMyPageChangeListener(MyPageChangeListener listener) {
        myPageChangeListener = listener;
    }

    public interface MyPageChangeListener {
        public void onPageSelected(int position);
    }
}
