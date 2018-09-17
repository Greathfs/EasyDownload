package com.wz.easydownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.wz.easydownload.bean.FileInfo;
import com.wz.easydownload.service.DownloadService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView mRecyclerView;
    private List<FileInfo> mFileList;
    private DownloadAdapter mDownloadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();

    }

    private void initData() {

    }

    private void initView() {
        mRecyclerView = findViewById(R.id.rv_main);

        //创建集合
        mFileList = new ArrayList<>();

        //创建文件信息
        FileInfo fileInfo = new FileInfo(0,
                "http://files.huitui.wizhong.com/media?file=1389f4a5-7b79-4e00-9722-d53168d6108a&type=media_stream&time=1536654530218",
                "碟中谍6", 0, 0);
        FileInfo fileInfo1 = new FileInfo(1,
                "http://files.huitui.wizhong.com/media?file=1389f4a5-7b79-4e00-9722-d53168d6108a&type=media_stream&time=1536654530218",
                "王牌对王牌", 0, 0);
        FileInfo fileInfo2 = new FileInfo(2,
                "http://files.huitui.wizhong.com/media?file=2542e618-adfb-4d3b-9386-b55b3d2c0a1b&type=media_stream&time=1536828789901",
                "我就是演员", 0, 0);

        mFileList.add(fileInfo);
        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);

        mDownloadAdapter = new DownloadAdapter(this, mFileList);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mDownloadAdapter);

        //注册广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadService.ACTION_UPDATE);
        //添加新的action
        intentFilter.addAction(DownloadService.ACTION_FINISH);
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

            if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
                //更新进度
                long finished = intent.getLongExtra("finished", 0);
                int id = intent.getIntExtra("id", 0);
                mDownloadAdapter.updateProgress(id, (int) finished);
            } else if (DownloadService.ACTION_FINISH.equals(intent.getAction())) {
                //结束
                FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
                mDownloadAdapter.updateProgress(fileInfo.getId(), 0);
                Log.d(TAG, "onReceive: " + fileInfo.getFileName() + "  下载完成!");
            }

        }
    };
}
