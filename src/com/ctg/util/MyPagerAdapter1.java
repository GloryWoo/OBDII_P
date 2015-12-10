package com.ctg.util;

import java.util.List;

import com.ctg.ui.Base;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class MyPagerAdapter1 extends PagerAdapter {

	private List<View> list;

	public MyPagerAdapter1(List<View> list) {
		this.list = list;
	}

	@Override
	public void destroyItem(View view, int position, Object arg2) {
		ViewPager pViewPager = ((ViewPager) view);
		pViewPager.removeView(list.get(position));
	}

	@Override
	public void finishUpdate(View arg0) {
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object instantiateItem(View view, int position) {
		ViewPager pViewPager = ((ViewPager) view);
		pViewPager.addView(list.get(position), Base.mWidth*16/25, Base.mHeight);		
		return list.get(position);
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
	}

}