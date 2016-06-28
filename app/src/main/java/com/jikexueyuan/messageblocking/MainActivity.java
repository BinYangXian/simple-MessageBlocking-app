package com.jikexueyuan.messageblocking;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;


public class MainActivity extends ListActivity {//ListActivity的显示不带app名字
    private SQLiteDatabase dbWrite, dbRead;
    private SimpleCursorAdapter adapter;
    private TextView addressView, dateView, bodyView;
    protected static BlockingDatabase blockingDatabase;
    //    private SmsReceiver receiver;//不可重复new reveiver，因为在manifest中已经相当于new并注册了。
    private Cursor c = null;//打算让拦截到短信刷新界面后关闭？

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);//此处无用，由于在ListActivity中已设为自身默认的ContentView
        blockingDatabase = new BlockingDatabase(this);//新建app 数据库信息
        dbWrite = blockingDatabase.getWritableDatabase();// 开启数据读写库权限
        dbRead = blockingDatabase.getReadableDatabase();
        addressView = (TextView) findViewById(R.id.addressView);
        dateView = (TextView) findViewById(R.id.dateView);
        bodyView = (TextView) findViewById(R.id.bodyView);

//        myReceiver();//接收器部分，这里用manifest中注册法替代了

        adapter = new SimpleCursorAdapter(this, R.layout.sms_block_list_cell, null, new String[]{"address", "date", "body"}
                , new int[]{R.id.addressView, R.id.dateView, R.id.bodyView});// 这里第三个参数应该先注明，而不是在refreshListView()中才注明
        setListAdapter(adapter);

        SmsReceiver.callback = new ICallBack() {//回调函数法，实现了进程之间的通讯（在此类实例化了 实现了
        // ICallBack接口的匿名类是关键！！）也可以使用Intent 进行进程间的通信吧？？？
            @Override
            public void callMainActivityViewRefresh() {
                refreshListView();
            }
        };
        refreshListView();
        getListView().setOnItemLongClickListener(listVievItemLongClickListener);

    }

    private void myReceiver() {//这句这里无用，manifests中有类似信息了！
//        IntentFilter filter = new IntentFilter();
//        filter.setPriority(1000);
//        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
//        if (receiver.isInitialStickyBroadcast()) //加上这句后，退出app就对注册这件事不报错了！且注册后不用注销了？amazing！
//            registerReceiver(receiver, filter);
    }

    //长按删除对话框
    private AdapterView.OnItemLongClickListener listVievItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            new AlertDialog.Builder(MainActivity.this).setTitle("提醒").setMessage("您确定要删除该条吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dbWrite = blockingDatabase.getWritableDatabase();// 开启数据读写库权限
                    c = adapter.getCursor();
                    c.moveToPosition(position);  //position在内部类中需要是final 也就是常量
                    int itemId = c.getInt(c.getColumnIndex("_id"));//Db类中创建的用户表中的_id
                    dbWrite.delete("sms", "_id=?", new String[]{itemId + ""});//"_id="+itemId 此种写法不太安全,所以把itemId传到下一个参数中
                    refreshListView();
                }
            }).setNegativeButton("取消", null).show();
            return true;//返回一个true值表示让操作系统做出相应长按反馈动作，faluse表示此次长按是不成功的
        }
    };

    private void refreshListView() {
        dbRead = blockingDatabase.getReadableDatabase();
        c = dbRead.query("sms", null, null, null, null, null, null);
        adapter.changeCursor(c);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, Menu.FIRST + 2, 2, "编辑").setIcon(android.R.drawable.ic_menu_edit);
        // return true才会起作用
        return true;
    }

    // 菜单项被选择事件
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case Menu.FIRST + 2:
                startActivity(new Intent(MainActivity.this, EditActivityView.class));
                break;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  //按返回键退出了app，依然能拦截短信，receiver这个是后台运行的吗？是的
        dbWrite.close();
        dbRead.close();  //关闭数据库
    }
}


