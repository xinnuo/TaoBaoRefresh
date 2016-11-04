# TaoBaoRefresh
高仿淘宝下拉刷新和加载更多，下拉一定距离时加载广告页。

该代码是在[PullRefresh](https://github.com/dalong982242260/PullRefresh)的基础上进行了一定的修改。


##效果图
![image](https://github.com/xinnuo/TaoBaoRefresh/blob/master/gif/taobao.gif?raw=true)

##使用

###1、自定义HeadView
###2、代码调用

        refreshview = (TaoBaoRefreshLayout) findViewById(R.id.refreshview);


        refreshview.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.removeMessages(0);
                mHandler.sendEmptyMessageDelayed(0, 3000);
            }

            @Override
            public void onLoadMore() {
                mHandler.removeMessages(1);
                mHandler.sendEmptyMessageDelayed(1, 3000);
            }

            @Override
            public void onLoadBottom() {
                Intent intent = new Intent(MainActivity.this, IntentActivity.class);
                startActivity(intent);
            }
        });



