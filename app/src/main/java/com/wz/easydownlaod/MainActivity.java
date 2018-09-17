package com.wz.easydownlaod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wz.easydownlaod.bean.FileInfo;
import com.wz.easydownlaod.service.DownloadService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private TextView mTvFileName;
    private ProgressBar mPbProgress;
    private Button mBtnStart;
    private Button mbtnStop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_down);

        initData();
        initView();

    }

    private void initData() {

    }

    private void initView() {
        mTvFileName = findViewById(R.id.tv_item_title);
        mPbProgress = findViewById(R.id.pb_item_progress);
        mBtnStart = findViewById(R.id.btn_start);
        mbtnStop = findViewById(R.id.btn_stop);

        mPbProgress.setMax(100);

        //创建文件信息
        final FileInfo fileInfo = new FileInfo(0,
                "http://files.huitui.wizhong.com/media?file=1389f4a5-7b79-4e00-9722-d53168d6108a&type=media_stream&time=1536654530218",
                "会推测试", 0, 0);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //给Service传递参数
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });

        mbtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //给Service传递参数
                Intent intent = new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);
                startService(intent);
            }
        });

        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDARE);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);

    }

    /**
     * 更新Ui广播接收器
     */
    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (DownloadService.ACTION_UPDARE.equals(intent.getAction())) {
                long finished = intent.getLongExtra("finished", 0);
                Log.d(TAG, "onReceive: finished=" + finished);
                mPbProgress.setProgress((int) finished);
            }

        }
    };
}
