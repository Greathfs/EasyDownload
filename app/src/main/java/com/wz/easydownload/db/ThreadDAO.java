package com.wz.easydownload.db;

import com.wz.easydownload.bean.ThreadInfo;

import java.util.List;

/**
 * @author HuangFusheng
 * @date 2018/9/17
 * @description 数据访问接口
 */
public interface ThreadDAO {
    /**
     * 插入线程信息
     *
     * @param threadInfo
     */
    void insertThread(ThreadInfo threadInfo);

    /**
     * 删除线程信息
     *
     * @param url
     * @param threadId
     */
    void deleteThread(String url, int threadId);

    /**
     * 更新下载进度
     *
     * @param url
     * @param threadId
     * @param finished
     */
    void upgradeThread(String url, int threadId, long finished);

    /**
     * 查询文件线程信息
     *
     * @param url
     * @return
     */
    List<ThreadInfo> getThreads(String url);

    /**
     * 线程信息是否存在
     * @param url
     * @param threadId
     * @return
     */
    boolean isExists(String url, int threadId);
}
