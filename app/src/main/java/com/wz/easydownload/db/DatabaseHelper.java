package com.wz.easydownload.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author HuangFusheng
 * @date 2018/9/17
 * @description 数据库帮助类
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "download.db";
    private static final int VERSION = 1;
    private static Context sContext;

    private static final String SQL_CREATE = "create table thread_info(_id integer primary key autoincrement," +
            "thread_id integer,url text,start long,end long,finished long)";
    private static final String SQL_DROP = "drop table if exists thread_info";

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static DatabaseHelper getInstance(Context context) {
        sContext = context;
        return SingleHolder.INSTANCE;
    }

    private static class SingleHolder {
        public static final DatabaseHelper INSTANCE = new DatabaseHelper(sContext);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        db.execSQL(SQL_CREATE);

    }
}
