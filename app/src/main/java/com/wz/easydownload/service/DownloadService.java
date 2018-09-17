package com.wz.easydownload.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wz.easydownload.bean.FileInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HuangFusheng
 * @date 2018/9/17
 * @description
 */
public class DownloadService extends Service {
    private static final String TAG = "DownloadService";

    /**
     * 下载路径
     */
    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/download/";
    /**
     * 开始下载命令
     */
    public static final String ACTION_START = "ACTION_START";
    /**
     * 结束下载命令
     */
    public static final String ACTION_STOP = "ACTION_STOP";
    /**
     * 更新UI命令
     */
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    /**
     * 下载完成
     */
    public static final String ACTION_FINISH = "ACTION_FINISH";
    /**
     * 初始化标识
     */
    public static final int MSG_INIT = 0;
    /**
     * 下载任务集合
     */
    private Map<Integer, DownloadTask> mTasks = new LinkedHashMap<>();

    private InitThread mInitThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获取Activity传来参数
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.d(TAG, "START fileInfo: " + fileInfo.toString());
            //启动初始化线程
            mInitThread =new InitThread(fileInfo);
            DownloadTask.sExecutorService.execute(mInitThread);

        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.d(TAG, "STOP fileInfo: " + fileInfo.toString());
            //从集合中取出下载任务
            DownloadTask downloadTask = mTasks.get(fileInfo.getId());
            if (downloadTask != null) {
                //停止下载
                downloadTask.isPause = true;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.d(TAG, "handleMessage: " + fileInfo.toString());
                    //启动下载任务
                    DownloadTask downloadTask = new DownloadTask(DownloadService.this, fileInfo,3);
                    downloadTask.download();
                    //吧下载任务添加到集合中
                    mTasks.put(fileInfo.getId(), downloadTask);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 初始化子线程
     */
    class InitThread extends Thread {
        private FileInfo mFileInfo;

        public InitThread(FileInfo fileInfo) {
            this.mFileInfo = fileInfo;
        }


        @Override
        public void run() {
            super.run();

            HttpURLConnection connection = null;
            RandomAccessFile randomAccessFile = null;
            try {

                //连接网络文件
                URL url = new URL(mFileInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(30000);
                connection.setRequestMethod("GET");
                long length = -1;
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //获取文件长度
                    length = connection.getContentLength();
                }
                if (length <= 0) {
                    return;
                }
                File dir = new File(DOWNLOAD_PATH);
                if (!dir.exists()) {
                    dir.mkdir();
                }
                //在本地创建文件长度
                File file = new File(DOWNLOAD_PATH + mFileInfo.getFileName());
                randomAccessFile = new RandomAccessFile(file, "rwd");
                //设置文件长度
                randomAccessFile.setLength(length);
                mFileInfo.setLength(length);
                mHandler.obtainMessage(MSG_INIT, mFileInfo).sendToTarget();

            } catch (Exception e) {
                e.printStackTrace();
            }finally {

                try {
                    connection.disconnect();
                    randomAccessFile.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }
    }
}
