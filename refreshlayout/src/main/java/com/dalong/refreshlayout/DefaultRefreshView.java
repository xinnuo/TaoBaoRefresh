package com.dalong.refreshlayout;


import android.content.Context;
import android.util.AttributeSet;

public class DefaultRefreshView extends RefreshLayout {

    public DefaultRefreshView(Context context) {
        super(context);
    }

    public DefaultRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        init();
    }

    public void init() {
        HeaderView header = new HeaderView(getContext());
        FooterView footer = new FooterView(getContext());

        addHeader(header);
        addFooter(footer);
        setOnHeaderListener(header);
        setOnFooterListener(footer);
    }
}
