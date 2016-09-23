package com.jinlin.loopbannerview.banner;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView.ScaleType;
import android.widget.Scroller;

import com.jinlin.loopbannerview.R;
import com.jinlin.loopbannerview.ResizableImageView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class LoopViewPager extends ViewPager {

    private PagerAdapter mAdapter;   //原始的Adapter
    private LoopAdapterWrapper mLoopAdapter;    //实现了循环滚动的Adapter

    private OnPageChangeListener loopPageChangeListener;  //内部定义的监听器
    private ArrayList<OnPageChangeListener> mOnPageChangeListeners;   //外部通过add传进来的

    private Handler mHandler;  //处理轮播的Handler
    private boolean mIsAutoLoop = true;  //是否自动轮播
    private int mDelayTime = 4000; //轮播的延时时间
    private boolean isDetached;    //是否被回收过
    private int currentPosition;   //当前的条目位置
    private static final int MSG_AUTO_SCROLL = 0;

    public LoopViewPager(Context context) {
        this(context, null);
    }

    public LoopViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        loopPageChangeListener = new MyOnPageChangeListener();
        super.addOnPageChangeListener(loopPageChangeListener);

        TypedArray a = getResources().obtainAttributes(attrs, R.styleable.LoopViewPager);
        mIsAutoLoop = a.getBoolean(R.styleable.LoopViewPager_lvp_isAutoLoop, mIsAutoLoop);
        mDelayTime = a.getInteger(R.styleable.LoopViewPager_lvp_delayTime, mDelayTime);
        a.recycle();
        setAutoLoop(mIsAutoLoop, mDelayTime);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isDetached) {
            if (loopPageChangeListener != null) {
                super.addOnPageChangeListener(loopPageChangeListener);
            }
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessageDelayed(0, mDelayTime);
            }
            isDetached = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (loopPageChangeListener != null) {
            super.removeOnPageChangeListener(loopPageChangeListener);
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        isDetached = true;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putInt("currentPosition", currentPosition);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
            currentPosition = bundle.getInt("currentPosition");
        } else {
            super.onRestoreInstanceState(state);
        }
        setCurrentItem(currentPosition);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {
        mAdapter = adapter;
        mLoopAdapter = new LoopAdapterWrapper(adapter);
        super.setAdapter(mLoopAdapter);
        setCurrentItem(0, false);
    }

    /**
     * 默认返回的是传进来的Adapter
     */
    @Override
    public PagerAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setCurrentItem(int position, boolean smoothScroll) {
        int realPosition = mLoopAdapter == null ? 0 : mLoopAdapter.getInnerPosition(position);
        super.setCurrentItem(realPosition, smoothScroll);
    }

    @Override
    public void setCurrentItem(int item) {
        setCurrentItem(item, true);
    }

    @Override
    public int getCurrentItem() {
        return mLoopAdapter == null ? 0 : mLoopAdapter.toRealPosition(super.getCurrentItem());
    }

    @Override
    public void clearOnPageChangeListeners() {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.clear();
        }
    }

    @Override
    public void addOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners == null) {
            mOnPageChangeListeners = new ArrayList<>();
        }
        mOnPageChangeListeners.add(listener);
    }

    @Override
    public void removeOnPageChangeListener(OnPageChangeListener listener) {
        if (mOnPageChangeListeners != null) {
            mOnPageChangeListeners.remove(listener);
        }
    }

    public void setData(final BannerAdapter adapter) {
        setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return adapter.getCount();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, final int position) {
                ResizableImageView imageView = new ResizableImageView(getContext());
                imageView.setScaleType(ScaleType.CENTER_CROP);
                adapter.bindImage(imageView, adapter.getItem(position));
                imageView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.onClick(v, position, adapter.getItem(position));
                    }
                });
                container.addView(imageView);
                return imageView;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }
        });
    }

    private class MyOnPageChangeListener implements OnPageChangeListener {
        // 上一次的偏移量
        private float mPreviousOffset = -1;
        // 上一次的位置
        private float mPreviousPosition = -1;

        @Override
        public void onPageSelected(int position) {
            int realPosition = mLoopAdapter == null ? 0 : mLoopAdapter.toRealPosition(position);
            currentPosition = realPosition;
            if (mPreviousPosition != realPosition) {
                mPreviousPosition = realPosition;
                // 分发事件给外部传进来的监听
                if (mOnPageChangeListeners != null) {
                    for (int i = 0, z = mOnPageChangeListeners.size(); i < z; i++) {
                        OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                        if (listener != null) {
                            listener.onPageSelected(realPosition);
                        }
                    }
                }
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int realPosition = mLoopAdapter == null ? 0 : mLoopAdapter.toRealPosition(position);
            /*
                positionOffset = 0:滚动完成，
                position = 0 :开始的边界
                position = mAdapter.getCount()-1:结束的边界
             */
            if (positionOffset == 0 && mPreviousOffset == 0 && (position == 0 || position == mLoopAdapter.getCount() - 1)) {
                // 强制回到映射位置
                setCurrentItem(realPosition, false);
            }
            mPreviousOffset = positionOffset;
            // 分发事件给外部传进来的监听
            if (mOnPageChangeListeners != null) {
                for (int i = 0, z = mOnPageChangeListeners.size(); i < z; i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        //如果内层的位置没有达到最后一个，内层滚动监听器正常设置
                        if (realPosition != mLoopAdapter.getRealCount() - 1) {
                            listener.onPageScrolled(realPosition, positionOffset, positionOffsetPixels);
                        } else {
                            //如果到达最后一个位置，当偏移量达到0.5以上，这告诉监听器，这个页面已经到达内层的第一个位置
                            //否则还是最后一个位置
                            if (positionOffset > 0.5) {
                                listener.onPageScrolled(0, 0, 0);
                            } else {
                                listener.onPageScrolled(realPosition, 0, 0);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            switch (state) {
                case ViewPager.SCROLL_STATE_DRAGGING:
                    stopScrolling();
                    break;
                default:
                    if (!mIsAutoLoop) {
                        startScrolling();
                    }
                    break;
            }
            //当滑动到了第一页 或者 最后一页的时候，跳转到指定的对应页
            //不能在onPageSelected中写这段逻辑，因为onPageSelected当松手的时候，就调用了
            //不是在滑动结束后再调用
            int position = LoopViewPager.super.getCurrentItem();
            int realPosition = mLoopAdapter == null ? 0 : mLoopAdapter.toRealPosition(position);
            int count = mLoopAdapter == null ? 0 : mLoopAdapter.getCount();
            if (state == ViewPager.SCROLL_STATE_IDLE && (position == 0 || position == count - 1)) {
                setCurrentItem(realPosition, false);
            }

            //分发事件给外部传进来的监听
            if (mOnPageChangeListeners != null) {
                for (int i = 0, z = mOnPageChangeListeners.size(); i < z; i++) {
                    OnPageChangeListener listener = mOnPageChangeListeners.get(i);
                    if (listener != null) {
                        listener.onPageScrollStateChanged(state);
                    }
                }
            }
        }
    }

    /**
     * 设置是否自动轮播  delayTime延时的毫秒
     */
    public void setAutoLoop(boolean isAutoLoop, int delayTime) {
        mIsAutoLoop = isAutoLoop;
        mDelayTime = delayTime;
        if (mIsAutoLoop) {
            if (mHandler == null) {
                mHandler = new LoopHandler(this);
                mHandler.sendEmptyMessageDelayed(0, mDelayTime);
            } else {
                mHandler.removeCallbacksAndMessages(null);
                mHandler.sendEmptyMessageDelayed(0, mDelayTime);
            }
        } else {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
        }
    }

    public void startScrolling() {
        mIsAutoLoop = canRunning();
        if (mHandler != null) {
            mHandler.removeMessages(MSG_AUTO_SCROLL);
            if (mIsAutoLoop) {
                mHandler.sendEmptyMessageDelayed(MSG_AUTO_SCROLL, mDelayTime);
            }
        }
    }

    public boolean canRunning() {
        return getVisibility() == View.VISIBLE
                && getAdapter() != null && getAdapter().getCount() > 1;
    }

    public void stopScrolling() {
        mIsAutoLoop = false;
        if (mHandler != null) {
            mHandler.removeMessages(MSG_AUTO_SCROLL);
        }
    }

    /**
     * 自动轮播的Handler
     */
    private static class LoopHandler extends Handler {
        private WeakReference<LoopViewPager> mViewGroup;

        LoopHandler(LoopViewPager viewGroup) {
            mViewGroup = new WeakReference<>(viewGroup);
        }

        @Override
        public void handleMessage(Message msg) {
            LoopViewPager viewPager = mViewGroup.get();
            if (viewPager != null) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                sendEmptyMessageDelayed(MSG_AUTO_SCROLL, viewPager.mDelayTime);
            }
        }
    }

    /**
     * 用该类包装一个需要实现循环滚动的Adapter
     */
    class LoopAdapterWrapper extends PagerAdapter {

        private PagerAdapter mAdapter;

        LoopAdapterWrapper(PagerAdapter adapter) {
            this.mAdapter = adapter;
        }

        @Override
        public int getCount() {
            // 如果ViewPager中有两个或两个以上的Item的时候，则映射出边界Item，否则显示与内层个数一致
            return getRealCount() > 1 ? getRealCount() + 2 : getRealCount();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return mAdapter.instantiateItem(container, toRealPosition(position));
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mAdapter.destroyItem(container, toRealPosition(position), object);
        }

        //重写对Adapter的操作
        @Override
        public void finishUpdate(ViewGroup container) {
            mAdapter.finishUpdate(container);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return mAdapter.isViewFromObject(view, object);
        }

        @Override
        public void restoreState(Parcelable bundle, ClassLoader classLoader) {
            mAdapter.restoreState(bundle, classLoader);
        }

        @Override
        public Parcelable saveState() {
            return mAdapter.saveState();
        }

        @Override
        public void startUpdate(ViewGroup container) {
            mAdapter.startUpdate(container);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            mAdapter.setPrimaryItem(container, position, object);
        }

        /**
         * original adapter position    [0,1,2,3]
         * modified adapter position  [0,1,2,3,4,5]
         * modified     realPosition  [3,0,1,2,3,0]
         * modified     InnerPosition [4,1,2,3,4,1]
         * 根据外层position的获取内层的position
         *
         * @param position 外层ViewPager的position
         * @return 外层viewPager当前数据位置对应的内层viewPager对应的位置。
         */
        int toRealPosition(int position) {
            // viewPager真正的可用的个数
            int realCount = getRealCount();
            // 内层没有可用的Item则换回为零
            if (realCount == 0)
                return 0;
            int realPosition = (position - 1) % realCount;
            if (realPosition < 0)
                realPosition += realCount;
            return realPosition;
        }

        /**
         * 根据传进来的真实位置，得到该 loopAdapter 的真实条目位置
         *
         * @param realPosition 内层position的位置
         * @return 无限轮播ViewPager的切换位置
         */
        int getInnerPosition(int realPosition) {
            if (getRealCount() > 1) {
                return realPosition + 1;
            } else return realPosition;
        }

        /**
         * @return 内层ViewPager中可用的item个数
         */
        int getRealCount() {
            return mAdapter.getCount();
        }
    }

    public void setTransformDuration(int duration) {
        FixedSpeedScroller scroller = new FixedSpeedScroller(getContext(), duration);
        scroller.changScrollDuration(this, duration);
    }

    // FixedSpeedScroller.java
    class FixedSpeedScroller extends Scroller {
        // 默认1秒，可以通过上面的方法控制
        private int mPagerChangeDuration = 1000;

        FixedSpeedScroller(Context context, int duration) {
            super(context);
            // 修正banner页面切换时间
            mPagerChangeDuration = duration;
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mPagerChangeDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mPagerChangeDuration);
        }

        void changScrollDuration(ViewPager viewPager, int duration) {
            mPagerChangeDuration = duration;
            try {
                Field mScroller = ViewPager.class.getDeclaredField("mScroller");
                mScroller.setAccessible(true);
                mScroller.set(viewPager, this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
