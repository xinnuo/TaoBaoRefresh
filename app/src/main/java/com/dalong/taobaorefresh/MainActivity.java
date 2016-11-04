package com.dalong.taobaorefresh;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.dalong.refreshlayout.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TaoBaoRefreshLayout refreshview;
    public List<String> list=new ArrayList<>();
    private ListView listview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        refreshview=(TaoBaoRefreshLayout)findViewById(R.id.refreshview);
        listview=(ListView)findViewById(R.id.listview);
        listview.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, list));
        refreshview.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mHandler.removeMessages(0);
                mHandler.sendEmptyMessageDelayed(0,3000);
            }

            @Override
            public void onLoadMore() {
                mHandler.removeMessages(1);
                mHandler.sendEmptyMessageDelayed(1,3000);
            }

            @Override
            public void onLoadBottom() {
                Intent intent=new Intent(MainActivity.this,IntentActivity.class);
                startActivity(intent);
            }
        });

    }

    Handler mHandler= new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    refreshview.stopRefresh(true);
                    break;
                case 1:
                    refreshview.stopLoadMore(true);
                    break;
            }
        }
    };

    public void initData(){
        for (int i=0;i<14;i++){
            list.add("测试的"+i);
        }
    }
}
