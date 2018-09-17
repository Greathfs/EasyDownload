package com.wz.easydownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.wz.easydownload.bean.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HuangFusheng
 * @date 2018/9/17
 * @description 数据访问借口实现
 */
public class ThreadDAOImpl implements ThreadDAO {

    private DatabaseHelper mDatabaseHelper;

    public ThreadDAOImpl(Context context) {
        mDatabaseHelper = DatabaseHelper.getInstance(context);
    }

    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(?,?,?,?,?)",
                new Object[]{threadInfo.getId(),threadInfo.getUrl(),threadInfo.getStart(),threadInfo.getEnd(),threadInfo.getFinished()});

        db.close();
    }

    @Override
    public synchronized void deleteThread(String url) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url = ? ",
                new Object[]{url});

        db.close();
    }

    @Override
    public synchronized void upgradeThread(String url, int threadId, long finished) {
        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        db.execSQL("update thread_info  set finished = ? where url = ? and thread_id = ?",
                new Object[]{finished,url,threadId});

        db.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        List<ThreadInfo> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("select * from thread_info where url = ?", new String[]{url});
        while (cursor.moveToNext()) {
            ThreadInfo threadInfo = new ThreadInfo();
            threadInfo.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            threadInfo.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            threadInfo.setStart(cursor.getLong(cursor.getColumnIndex("start")));
            threadInfo.setEnd(cursor.getLong(cursor.getColumnIndex("end")));
            threadInfo.setFinished(cursor.getLong(cursor.getColumnIndex("finished")));
            list.add(threadInfo);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public boolean isExists(String url, int threadId) {

        boolean flag = false;

        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = null;
        try {

            cursor = db.rawQuery("select * from thread_info where url = ? and thread_id = ?",
                    new String[]{url, String.valueOf(threadId)});

            flag = cursor.moveToNext();

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            cursor.close();
            db.close();
        }

        return flag;
    }
}
