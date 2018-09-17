package com.wz.easydownload.service;

import android.content.Context;
import android.content.Intent;

import com.wz.easydownload.bean.FileInfo;
import com.wz.easydownload.bean.ThreadInfo;
import com.wz.easydownload.db.ThreadDAO;
import com.wz.easydownload.db.ThreadDAOImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author HuangFusheng
 * @date 2018/9/17
 * @description 下载任务
 */
public class DownloadTask {

    private Context mContext;
    private FileInfo mFileInfo;
    private ThreadDAO mDAO;
    private long mFinished = 0;
    public boolean isPause = false;


    public DownloadTask(Context context, FileInfo fileInfo) {
        mContext = context;
        mFileInfo = fileInfo;
        mDAO = new ThreadDAOImpl(context);
    }

    public void download() {
        //读取数据库线程信息
        List<ThreadInfo> threads = mDAO.getThreads(mFileInfo.getUrl());
        ThreadInfo threadInfo = null;
        if (threads.size() == 0) {
            //初始化线程信息对象
            threadInfo = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(),0);

        }else {
            threadInfo = threads.get(0);
        }

        //创建子线程进行下载
        new DownloadThread(threadInfo).start();

    }

    /**
     * 下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;

        public DownloadThread(ThreadInfo threadInfo) {
            mThreadInfo = threadInfo;
        }

        @Override
        public void run() {
            super.run();

            //向数据库插入线程信息
            if (!mDAO.isExists(mThreadInfo.getUrl(), mThreadInfo.getId())) {
                mDAO.insertThread(mThreadInfo);
            }

            HttpURLConnection connection = null;
            RandomAccessFile randomAccessFile = null;
            InputStream input = null;
            try {

                URL url = new URL(mThreadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setRequestMethod("GET");
                //设置下载位置
                long start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                //设置写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(start);

                Intent intent = new Intent(DownloadService.ACTION_UPDARE);
                mFinished += mThreadInfo.getFinished();
                //开始下载
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL||connection.getResponseCode()==HttpURLConnection.HTTP_OK) {
                    //读取数据
                     input = connection.getInputStream();
                    byte[] bytes = new byte[1024*4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = input.read(bytes)) != -1) {
                        //写入文件
                        randomAccessFile.write(bytes, 0, len);
                        //下载进度发送广播给Activity
                        mFinished += len;
                        if (System.currentTimeMillis() - time >= 500) {
                            time = System.currentTimeMillis();
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            mContext.sendBroadcast(intent);
                        }
                        //在下载暂停时 保存下载进度
                        if (isPause) {
                            mDAO.upgradeThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mFinished);
                            return;
                        }
                    }
                }

                //删除线程信息
                mDAO.deleteThread(mThreadInfo.getUrl(), mThreadInfo.getId());

            } catch (Exception e) {
                e.printStackTrace();
            }finally {

                try {
                    connection.disconnect();
                    randomAccessFile.close();
                    input.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }
}
