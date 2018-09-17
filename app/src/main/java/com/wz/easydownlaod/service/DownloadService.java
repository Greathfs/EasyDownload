package com.wz.easydownlaod.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.wz.easydownlaod.bean.FileInfo;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author HuangFusheng
 * @date 2018/9/17
 * @description
 */
public class DownloadService extends Service {
    private static final String TAG = "DownloadService";

    public static final String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/download/";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_UPDARE = "ACTION_UPDATE";

    public static final int MSG_INIT = 0;
    private DownloadTask mDownloadTask;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获取Activity传来参数
        if (ACTION_START.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.d(TAG, "START fileInfo: " + fileInfo.toString());
            //启动初始化线程
            new InitThread(fileInfo).start();
        } else if (ACTION_STOP.equals(intent.getAction())) {
            FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
            Log.d(TAG, "STOP fileInfo: " + fileInfo.toString());
            if (mDownloadTask != null) {
                mDownloadTask.isPause = true;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_INIT:
                    FileInfo fileInfo = (FileInfo) msg.obj;
                    Log.d(TAG, "handleMessage: " + fileInfo.toString());
                    //启动下载任务
                    mDownloadTask = new DownloadTask(DownloadService.this, fileInfo);
                    mDownloadTask.download();
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
                connection.setConnectTimeout(3000);
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
