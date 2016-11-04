package com.dalong.refreshlayout;

/**
 * 下拉刷新 加载更多接口
 */
public interface OnRefreshListener {

    /**
     * 刷新回调
     */
    void onRefresh();

    /**
     * 加载更多回调
     */
    void onLoadMore();

    /**
     * 底部回调
     */
    void onLoadBottom();
}
