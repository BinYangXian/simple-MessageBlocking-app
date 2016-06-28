package com.jikexueyuan.messageblocking;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.text.SimpleDateFormat;

/**
 * Created by fangc on 2016/2/5.
 */
public class SmsReceiver extends BroadcastReceiver {

    SQLiteDatabase dbWrite, dbRead;
    private boolean block = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null)
            return;

        Object[] pdus = (Object[]) extras.get("pdus");
        SmsMessage message = null;
        ContentValues cv = new ContentValues();
        for (Object pdu : pdus) {
            message = SmsMessage.createFromPdu((byte[]) pdu);
        }

        assert message != null;
        String address = message.getOriginatingAddress();   //来信电话号码
        String body = message.getMessageBody(); //短信内容
        dbWrite = MainActivity.blockingDatabase.getWritableDatabase();
        dbRead = MainActivity.blockingDatabase.getReadableDatabase();
        if (address.contains("+86")) {
            address = address.substring(3);
        }
        Cursor cursorBlockWords = dbRead.query("blockWords", new String[]{"keywords"}, null, null, null, null, null);
        Cursor cursorBlockNumber = dbRead.query("blockNumber", new String[]{"number"}, "number=?", new String[]{address}, null, null, null);
        if (cursorBlockNumber.moveToPosition(0)) {//如果查询结果有匹配address，那就就拦截，这里为什么不能用moveToFirst，老师测试后却说能

            block = true;
            abortBroadcast();
        }
        if (!block) {   //如果已经是拦截号码了，就不用再查关键字后拦截了
            cursorBlockWords.moveToPosition(-1);//直接定位到-1
            while (cursorBlockWords.moveToNext()) {
                if (body.contains(cursorBlockWords.getString(cursorBlockWords.getColumnIndex("keywords")))) {
                    block = true;
                    abortBroadcast();
                    break;
                }
            }
        }
        if (!cursorBlockNumber.isClosed()) {
            cursorBlockNumber.close();
        }
        if (!cursorBlockWords.isClosed()) {
            cursorBlockWords.close();
        }

        if (block) {
            long date = message.getTimestampMillis();         //短信时间
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");//创建格式化日期时间对象
            String dateStr = format.format(date);              //格式化日期时间对象
            cv.put("address", address);
            cv.put("body", body);
            cv.put("date", dateStr);
            dbWrite.insert("sms", null, cv);
            block = false;
        if (callback!=null)//改作业后草稿2
        callback.callMainActivityViewRefresh();
//            callback=null; //不能有此，下一次接收到广播时，acllback就不会再执行回调方法，且可能不在MainActivity中时，但这里的特定作用，可以不用
        }
        dbRead.close();
        dbWrite.close();
    }
    public  static ICallBack callback=null;

}

