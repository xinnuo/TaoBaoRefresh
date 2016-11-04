package com.dalong.refreshlayout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import static com.dalong.refreshlayout.RefreshStatus.LOAD_BEFORE;
import static com.dalong.refreshlayout.RefreshStatus.REFRESH_CANCEL;

public class RefreshLayout extends RefreshInterceptLauyout {

    // 事件监听接口
    private OnRefreshListener listener;
    // Layout状态
    private RefreshStatus status = RefreshStatus.DEFAULT;
    //阻尼系数
    private float damp = 0.5f;
    //恢复动画的执行时间
    public int SCROLL_TIME = 300;
    //是否刷新完成
    private boolean isRefreshSuccess = false;
    //是否加载完成
    private boolean isLoadSuccess = false;
    //正在加载中
    public boolean isLoading = false;
    //正在刷新中
    public boolean isRefreshing = false;
    //正在加载布局中
    public boolean isBottom = false;
    //是否自动下拉刷新
    private boolean isAutoRefresh = false;
    private Context context;

    public RefreshLayout(Context context) {
        super(context);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置是否支持下拉刷新
     *
     * @param isCanRefresh
     */
    public void setCanRefresh(boolean isCanRefresh) {
        this.isCanRefresh = isCanRefresh;
    }

    /**
     * 设置是否支持加载更多
     *
     * @param isCanLoad
     */
    public void setCanLoad(boolean isCanLoad) {
        this.isCanLoad = isCanLoad;
    }

    /**
     * 设置是否支持自动刷新
     *
     * @param isAutoRefresh
     */
    public void setAutoRefresh(boolean isAutoRefresh) {
        this.isAutoRefresh = isAutoRefresh;
        autoRefresh();
    }
    Handler mHandler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    if (listener != null)
                        listener.onLoadBottom();
                    break;
                case 1:
                    scrolltoDefaultStatus(REFRESH_CANCEL);
                    break;
            }
        }
    };

    /**
     * 自动刷新
     */
    public void autoRefresh() {
        if (!isAutoRefresh) return;
        isRefreshing = true;
        measureView(header);
        int end = headerContent.getMeasuredHeight();
        performAnim(0, -end, new AnimListener() {
            @Override
            public void onGoing() {
                updateStatus(status.REFRESH_READY);
            }

            @Override
            public void onEnd() {
                updateStatus(status.REFRESH_DOING);
            }
        });

    }

    /**
     * 测量view
     *
     * @param v
     */
    public void measureView(View v) {
        if (v == null) {
            return;
        }
        int w = MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED);
        int h = MeasureSpec.makeMeasureSpec(0,
                MeasureSpec.UNSPECIFIED);
        v.measure(w, h);
    }

    /**
     * 设置接口回调
     *
     * @param listener
     */
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE: {
                // 计算本次滑动的Y轴增量(距离)
                int dy = y - lastYMove;
                // 如果getScrollY<0，即下拉操作
                if (getScrollY() <= 0) {
                    if (header != null && !isLoading && !isRefreshing && !isBottom) {
                        // 进行Y轴上的滑动
                        performScroll(dy);
                        if (Math.abs(getScrollY()) <= headerContent.getMeasuredHeight()) {
                            updateStatus(status.REFRESH_BEFORE);
                        } else {
                            if (Math.abs(getScrollY()) <= getheight() * 0.25) {//刷新
                                updateStatus(status.REFRESH_AFTER);
                            } else {//弹出全部头部
                                updateStatus(status.REFRESH_BUJU);
                            }
                        }
                    }
                }
                // 如果getScrollY>=0，即上拉操作
                else {
                    if (footer != null && !isRefreshing && !isLoading) {
                        // 进行Y轴上的滑动
                        performScroll(dy);
                        if (getScrollY() >= bottomScroll + footer.getMeasuredHeight()) {
                            updateStatus(status.LOAD_AFTER);
                        } else {
                            updateStatus(LOAD_BEFORE);
                        }
                    }
                }
                // 记录y坐标
                lastYMove = y;
                break;
            }

            case MotionEvent.ACTION_UP: {
                // 判断本次触摸系列事件结束时,Layout的状态
                switch (status) {
                    //下拉刷新
                    case REFRESH_BEFORE:
                        scrolltoDefaultStatus(REFRESH_CANCEL);
                        break;
                    case REFRESH_AFTER://刷新
                        scrolltoRefreshStatus();
                        break;
                    //加载布局
                    case REFRESH_BUJU:
                        if (!isBottom) scrolltoBottomStatus();
                        break;
                    //上拉加载更多
                    case LOAD_BEFORE:
                        scrolltoDefaultStatus(status.LOAD_CANCEL);
                        break;
                    case LOAD_AFTER:
                        scrolltoLoadStatus();
                        break;
                }
            }
        }
        lastYIntercept = 0;
        postInvalidate();
        return true;
    }

    /**
     * 刷新状态
     *
     * @param status
     */
    private void updateStatus(RefreshStatus status) {
        this.status = status;
        int scrollY = getScrollY();
        // 判断本次触摸系列事件结束时,Layout的状态
        switch (status) {
            //默认状态
            case DEFAULT:
                onDefault();
                break;
            //下拉刷新
            case REFRESH_BEFORE:
                mOnHeaderListener.onRefreshBefore(scrollY, headerContent.getMeasuredHeight(), header.getMeasuredHeight());
                break;
            //加载布局
            case REFRESH_BUJU:
                mOnHeaderListener.onRefreshMiddle(scrollY, headerContent.getMeasuredHeight(), header.getMeasuredHeight());
                break;
            //松手刷新
            case REFRESH_AFTER:
                mOnHeaderListener.onRefreshAfter(scrollY, headerContent.getMeasuredHeight(), header.getMeasuredHeight());
                break;
            //准备刷新
            case REFRESH_READY:
                mOnHeaderListener.onRefreshReady(scrollY, headerContent.getMeasuredHeight(), header.getMeasuredHeight());
                break;
            //刷新中
            case REFRESH_DOING:
                mOnHeaderListener.onRefreshing(scrollY, headerContent.getMeasuredHeight(), header.getMeasuredHeight());
                if (listener != null)
                    listener.onRefresh();
                break;
            //刷新完成
            case REFRESH_COMPLETE:
                mOnHeaderListener.onRefreshComplete(scrollY, headerContent.getMeasuredHeight(), header.getMeasuredHeight(), isRefreshSuccess);
                break;
            //取消刷新
            case REFRESH_CANCEL:
                mOnHeaderListener.onRefreshCancel(scrollY, headerContent.getMeasuredHeight(), header.getMeasuredHeight());
                break;
            //上拉加载更多
            case LOAD_BEFORE:
                mOnFooterListener.onLoadBefore(scrollY);
                break;
            //松手加载
            case LOAD_AFTER:
                mOnFooterListener.onLoadAfter(scrollY);
                break;
            //准备加载
            case LOAD_READY:
                mOnFooterListener.onLoadReady(scrollY);
                break;
            //加载中
            case LOAD_DOING:
                mOnFooterListener.onLoading(scrollY);
                if (listener != null)
                    listener.onLoadMore();
                break;
            //加载完成
            case LOAD_COMPLETE:
                mOnFooterListener.onLoadComplete(scrollY, isLoadSuccess);
                break;
            //取消加载
            case LOAD_CANCEL:
                mOnFooterListener.onLoadCancel(scrollY);
                break;
        }
    }

    /**
     * 默认状态
     */
    private void onDefault() {
        isRefreshSuccess = false;
        isLoadSuccess = false;
        isBottom = false;
    }

    /**
     * 滚动到加载状态
     */
    private void scrolltoLoadStatus() {
        isLoading = true;
        int start = getScrollY();
        int end = footer.getMeasuredHeight() + bottomScroll;
        performAnim(start, end, new AnimListener() {
            @Override
            public void onGoing() {
                updateStatus(status.LOAD_READY);
            }

            @Override
            public void onEnd() {
                updateStatus(status.LOAD_DOING);
            }
        });

    }

    /**
     * 滚动到刷新状态
     */
    private void scrolltoRefreshStatus() {
        isRefreshing = true;
        int start = getScrollY();
        int end = -headerContent.getMeasuredHeight();
        performAnim(start, end, new AnimListener() {
            @Override
            public void onGoing() {
                updateStatus(status.REFRESH_READY);
            }

            @Override
                public void onEnd() {
                updateStatus(status.REFRESH_DOING);
            }
        });
    }

    /**
     * 滚动到默认状态
     *
     * @param startStatus
     */
    private void scrolltoDefaultStatus(final RefreshStatus startStatus) {
        int start = getScrollY();
        int end = 0;
        performAnim(start, end, new AnimListener() {
            @Override
            public void onGoing() {
                updateStatus(startStatus);
            }

            @Override
            public void onEnd() {
                updateStatus(status.DEFAULT);
            }
        });
    }

    /**
     * 滚动到底部
     */
    private void scrolltoBottomStatus() {
        isBottom = true;
        int start = getScrollY();
        int end = - header.getHeight() ;
        performAnim(start, end, new AnimListener() {
            @Override
            public void onGoing() {
            }

            @Override
            public void onEnd() {
                mHandler.sendEmptyMessageDelayed(0, 3000);
                mHandler.sendEmptyMessageDelayed(1, 3500);
            }
        });
    }

    /**
     * 滚动到指定位置
     *
     * @param startStatus
     */
    private void scrolltoStatus(final RefreshStatus startStatus, final int start, final int end) {
        performAnim(start, end, new AnimListener() {
            @Override
            public void onGoing() {
                updateStatus(startStatus);
            }

            @Override
            public void onEnd() {
                updateStatus(status.DEFAULT);
            }
        });
    }

    /**
     * 停止刷新
     *
     * @param isSuccess
     */
    public void stopRefresh(boolean isSuccess) {
        isRefreshSuccess = isSuccess;
        isRefreshing = false;
        scrolltoDefaultStatus(RefreshStatus.REFRESH_COMPLETE);
    }

    /**
     * 停止加载更多
     *
     * @param isSuccess
     */
    public void stopLoadMore(boolean isSuccess) {
        isLoadSuccess = isSuccess;
        isLoading = false;
        scrolltoDefaultStatus(RefreshStatus.LOAD_COMPLETE);
    }

    /**
     * 执行滑动
     *
     * @param dy
     */
    public void performScroll(int dy) {
        scrollBy(0, (int) (-dy * damp));
    }

    /**
     * 执行动画
     *
     * @param start
     * @param end
     * @param listener
     */
    private void performAnim(int start, int end, final AnimListener listener) {
        ValueAnimator animator = ValueAnimator.ofInt(start, end);
        animator.setDuration(SCROLL_TIME).start();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                scrollTo(0, value);
                postInvalidate();
                listener.onGoing();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    interface AnimListener {
        void onGoing();

        void onEnd();
    }

    //获取屏幕高度
    public int getheight() {
        WindowManager wm = (WindowManager) getContext()
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        return height;
    }


}
