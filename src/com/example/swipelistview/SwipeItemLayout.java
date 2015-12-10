package com.example.swipelistview;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.support.v4.widget.ScrollerCompat;

public class SwipeItemLayout extends FrameLayout {
	private View contentView = null;
	private View menuView = null;
	private Interpolator closeInterpolator = null;
	private Interpolator openInterpolator = null;
	
	private ScrollerCompat mOpenScroller;
	private ScrollerCompat mCloseScroller;
	
	private int mBaseX;
	private int mDownX;
	private int state = STATE_CLOSE;
	
	private static final int STATE_CLOSE = 0;
	private static final int STATE_OPEN = 1;
	
	public SwipeItemLayout(View contentView,View menuView,Interpolator closeInterpolator, Interpolator openInterpolator){
		super(contentView.getContext());
		this.contentView = contentView;
		this.menuView = menuView;
		this.closeInterpolator = closeInterpolator;
		this.openInterpolator = openInterpolator;
		
		init();
	}
	
	private void init(){
		setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		if (closeInterpolator != null) {
			mCloseScroller = ScrollerCompat.create(getContext(),
					closeInterpolator);
		} else {
			mCloseScroller = ScrollerCompat.create(getContext());
		}
		if (openInterpolator != null) {
			mOpenScroller = ScrollerCompat.create(getContext(),
					openInterpolator);
		} else {
			mOpenScroller = ScrollerCompat.create(getContext());
		}
		
		LayoutParams contentParams = new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		contentView.setLayoutParams(contentParams);

		menuView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

		addView(contentView);
		addView(menuView);
	}
	
	public boolean onSwipe(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownX = (int) event.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.i("byz", "downX = " + mDownX + ", moveX = " + event.getX());
			int dis = (int) (mDownX - event.getX());
			if (state == STATE_OPEN) {
				dis += menuView.getWidth();
			}
			swipe(dis);
			break;
		case MotionEvent.ACTION_UP:
			if ((mDownX - event.getX()) > (menuView.getWidth() / 2)) {
				// open
				smoothOpenMenu();
			} else {
				// close
				smoothCloseMenu();
				return false;
			}
			break;
		}
		return true;
	}
	
	public boolean isOpen() {
		return state == STATE_OPEN;
	}
	
	private void swipe(int dis) {
		if (dis > menuView.getWidth()) {
			dis = menuView.getWidth();
		}
		if (dis < 0) {
			dis = 0;
		}
		contentView.layout(-dis, contentView.getTop(),
				contentView.getWidth() - dis, getMeasuredHeight());
		menuView.layout(contentView.getWidth() - dis, menuView.getTop(),
				contentView.getWidth() + menuView.getWidth() - dis,
				menuView.getBottom());
	}
	
	@Override
	public void computeScroll() {
		if (state == STATE_OPEN) {
			if (mOpenScroller.computeScrollOffset()) {
				swipe(mOpenScroller.getCurrX());
				postInvalidate();
			}
		} else {
			if (mCloseScroller.computeScrollOffset()) {
				swipe(mBaseX - mCloseScroller.getCurrX());
				postInvalidate();
			}
		}
	}
	
	public void smoothCloseMenu() {
		state = STATE_CLOSE;
		mBaseX = -contentView.getLeft();
		System.out.println(mBaseX);
		mCloseScroller.startScroll(0, 0, mBaseX, 0, 350);
		postInvalidate();
	}
	
	public void smoothOpenMenu() {
		state = STATE_OPEN;
		mOpenScroller.startScroll(-contentView.getLeft(), 0,
				menuView.getWidth(), 0, 350);
		postInvalidate();
	}
	public void closeMenu() {
		if (mCloseScroller.computeScrollOffset()) {
			mCloseScroller.abortAnimation();
		}
		if (state == STATE_OPEN) {
			state = STATE_CLOSE;
			swipe(0);
		}
	}

	public void openMenu() {
		if (state == STATE_CLOSE) {
			state = STATE_OPEN;
			swipe(menuView.getWidth());
		}
	}

	public View getContentView() {
		return contentView;
	}

	public View getMenuView() {
		return menuView;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		menuView.measure(MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(
				getMeasuredHeight(), MeasureSpec.EXACTLY));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		contentView.layout(0, 0, getMeasuredWidth(),
				contentView.getMeasuredHeight());
		menuView.layout(getMeasuredWidth(), 0,
				getMeasuredWidth() + menuView.getMeasuredWidth(),
				contentView.getMeasuredHeight());
		// setMenuHeight(mContentView.getMeasuredHeight());
		// bringChildToFront(mContentView);
	}
}
