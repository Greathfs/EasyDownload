package com.wz.easydownload.service;

import android.content.Context;
import android.content.Intent;

import com.wz.easydownload.bean.FileInfo;
import com.wz.easydownload.bean.ThreadInfo;
import com.wz.easydownload.db.ThreadDAO;
import com.wz.easydownload.db.ThreadDAOImpl;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    /**
     * 线程数量
     */
    private int mThreadCount = 1;
    /**
     * 线程集合
     */
    private List<DownloadThread> mThreadList;
    /**
     * 线程池
     */
    public static ExecutorService sExecutorService =
            Executors.newCachedThreadPool();


    public DownloadTask(Context context, FileInfo fileInfo, int threadCount) {
        mContext = context;
        mFileInfo = fileInfo;
        mThreadCount = threadCount;
        mDAO = new ThreadDAOImpl(context);
    }

    /**
     * 下载
     */
    public void download() {
        //读取数据库下载进度
        List<ThreadInfo> threads = mDAO.getThreads(mFileInfo.getUrl());
        if (threads.size() == 0) {
            //获取每个线程下载长度
            long length = mFileInfo.getLength() / mThreadCount;

            for (int i = 0; i < mThreadCount; i++) {
                //创建线程信息
                ThreadInfo threadInfo = new ThreadInfo(i, mFileInfo.getUrl(), length * i, (i + 1) * length - 1, 0);
                //判断是否是最后一个,防止除不尽
                if (i == mThreadCount - 1) {
                    threadInfo.setEnd(mFileInfo.getLength());
                }

                //添加到线程信息集合
                threads.add(threadInfo);

                //向数据库插入线程信息
                mDAO.insertThread(threadInfo);

            }

        }
        mThreadList = new ArrayList<>();

        //启动多个线程进行下载
        for (ThreadInfo info : threads) {
            DownloadThread downloadThread = new DownloadThread(info);
//            downloadThread.start();
            DownloadTask.sExecutorService.execute(downloadThread);
            //添加线程到集合
            mThreadList.add(downloadThread);
        }

    }

    /**
     * 判断是否所有线程都执行完毕
     */
    private synchronized void checkAllThreadFinish() {
        boolean allFinished = true;
        //遍历线程,判断是否执行完毕
        for (DownloadThread thread : mThreadList) {
            if (!thread.isFinished) {
                allFinished = false;
                break;
            }
        }

        if (allFinished) {
            //删除下载信息
            mDAO.deleteThread(mFileInfo.getUrl());
            //发送广播,通知UI下载结束
            Intent intent = new Intent(DownloadService.ACTION_FINISH);
            intent.putExtra("fileInfo", mFileInfo);
            mContext.sendBroadcast(intent);
        }
    }

    /**
     * 数据下载线程
     */
    class DownloadThread extends Thread {
        private ThreadInfo mThreadInfo;
        /**
         * 线程是否执行结束
         */
        public boolean isFinished;

        public DownloadThread(ThreadInfo threadInfo) {
            mThreadInfo = threadInfo;
        }

        @Override
        public void run() {
            super.run();

            HttpURLConnection connection = null;
            RandomAccessFile randomAccessFile = null;
            InputStream input = null;
            try {

                URL url = new URL(mThreadInfo.getUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(30000);
                connection.setRequestMethod("GET");
                //设置下载位置
                long start = mThreadInfo.getStart() + mThreadInfo.getFinished();
                connection.setRequestProperty("Range", "bytes=" + start + "-" + mThreadInfo.getEnd());
                //设置写入位置
                File file = new File(DownloadService.DOWNLOAD_PATH, mFileInfo.getFileName());
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(start);

                Intent intent = new Intent(DownloadService.ACTION_UPDATE);
                mFinished += mThreadInfo.getFinished();
                //开始下载
                if (connection.getResponseCode() == HttpURLConnection.HTTP_PARTIAL || connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    //读取数据
                    input = connection.getInputStream();
                    byte[] bytes = new byte[1024 * 4];
                    int len = -1;
                    long time = System.currentTimeMillis();
                    while ((len = input.read(bytes)) != -1) {
                        //写入文件
                        randomAccessFile.write(bytes, 0, len);
                        //累加整个文件完成进度
                        mFinished += len;
                        //累加,每个线程完成进度
                        mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                        if (System.currentTimeMillis() - time >= 1000) {
                            time = System.currentTimeMillis();
                            //发送进度到Activity
                            intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
                            intent.putExtra("id", mFileInfo.getId());
                            mContext.sendBroadcast(intent);
                        }
                        //在下载暂停时 保存下载进度
                        if (isPause) {
                            mDAO.upgradeThread(mThreadInfo.getUrl(), mThreadInfo.getId(), mThreadInfo.getFinished());
                            return;
                        }
                    }
                }

                //标识线程执行完毕
                isFinished = true;

                //检测下载u任务是否完成
                checkAllThreadFinish();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {

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
