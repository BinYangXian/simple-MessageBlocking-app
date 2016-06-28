package com.jikexueyuan.messageblocking;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Created by fangc on 2016/2/5.
 */
public class BlockingDatabase extends SQLiteOpenHelper {
    public BlockingDatabase(Context context) {
        super(context, "db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String message = "create table if not exists sms("
                + "_id integer primary key autoincrement,"
                + "address TEXT DEFAULT \"\","
                + "body TEXT DEFAULT \"\","
                + "date varchar(255))";
        String blockWords = "create table if not exists blockWords("
                + "_id integer primary key autoincrement,"
                + "keywords TEXT DEFAULT \"\")";
        String blockNumber = "create table if not exists blockNumber("
                + "_id integer primary key autoincrement,"
                + "number TEXT DEFAULT \"\")";
        db.execSQL(blockWords);
        db.execSQL(blockNumber);
        db.execSQL(message);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
