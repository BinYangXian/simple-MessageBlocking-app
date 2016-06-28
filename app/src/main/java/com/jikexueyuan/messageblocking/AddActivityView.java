package com.jikexueyuan.messageblocking;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class AddActivityView extends ListActivity {
    private DialogInterface.OnClickListener addKeywordsListener;
    private DialogInterface.OnClickListener addAddressListener;
    private int flag;
    private SQLiteDatabase dbWrite, dbRead;
    private EditText add_edit_keywords, add_edit_address;
    private SimpleCursorAdapter adapterKeywords;
    private Cursor c=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_activity_view);
        Intent i = getIntent();
        flag=i.getFlags();
//        flag = i.getIntExtra("falg", 0);//获取编辑界面传来的添加分类标志
        if (flag == 1) {
            adapterKeywords = new SimpleCursorAdapter(this, R.layout.block_list_cell, null, new String[]{"keywords"}, new int[]{R.id.block_tv});//
        } else if (flag == 2) {
            adapterKeywords = new SimpleCursorAdapter(this, R.layout.block_list_cell, null, new String[]{"number"}, new int[]{R.id.block_tv});//
        }
        setListAdapter(adapterKeywords);
        dbWrite = MainActivity.blockingDatabase.getWritableDatabase();// 开启数据读写库权限
        dbRead = MainActivity.blockingDatabase.getReadableDatabase();
        refreshListView();         //刷新列表
        //监听ListView 的item点击事件
        ((ListView) findViewById(android.R.id.list)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                new AlertDialog.Builder(AddActivityView.this).setTitle("提醒").setMessage("您确定要删除该项吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    c = adapterKeywords.getCursor();
                    c.moveToPosition(position);  //position在内部类中需要是final 也就是常量
                    int itemId = c.getInt(c.getColumnIndex("_id"));//Db类中创建的用户表中的_id
                    if (flag == 1)
                        dbWrite.delete("blockWords", "_id=?", new String[]{itemId + ""});//"_id="+itemId 此种写法不太安全,所以把itemId传到下一个参数中
                    else if (flag == 2) {
                        dbWrite.delete("blockNumber", "_id=?", new String[]{itemId + ""});//"_id="+itemId 此种写法不太安全,所以把itemId传到下一个参数中
                    }
                    refreshListView();
                }
            }).setNegativeButton("取消",null).show();
            }
        });
//向数据库中的sms表添加 关键字、来信号码
        addKeywordsListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ContentValues cv = new ContentValues();
                String keywords = "";
                if (flag == 1) {
                    keywords = add_edit_keywords.getText().toString().trim();
                } else if (flag == 2) {
                    keywords = add_edit_address.getText().toString().trim();
                }

                if (!keywords.equals("")) {
                    if (flag == 1) {
                        cv.put("keywords", keywords);
                        dbWrite.insert("blockWords", null, cv);
                    } else if (flag == 2) {
                        cv.put("number", keywords);
                        dbWrite.insert("blockNumber", null, cv);
                    }
                    refreshListView();

                }

            }
        };
        //当点击添加按钮时候,根据编辑界面传来的标志，显示相应的添加对话框
        findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = null;
                if (flag == 1) {
                    view = View.inflate(AddActivityView.this, R.layout.add_cell_keywords, null);
                    add_edit_keywords = (EditText) view.findViewById(R.id.add_edit_keywords);
                } else if (flag == 2) {
                    view = View.inflate(AddActivityView.this, R.layout.add_cell_address, null);
                    add_edit_address = (EditText) view.findViewById(R.id.add_edit_address);
                }
                new AlertDialog.Builder(AddActivityView.this)
                        .setTitle("请输入内容")
                        .setView(view)
                        .setPositiveButton("确定", addKeywordsListener)
                        .setNegativeButton("取消", null)
                        .show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbWrite.close();
        dbRead.close();  //关闭数据库
    }

    private void refreshListView() {
        if (flag == 1) {
            c = dbRead.query("blockWords", null, null, null, null, null, null);
        } else if (flag == 2) {
            c = dbRead.query("blockNumber", null, null, null, null, null, null);
        }
        adapterKeywords.changeCursor(c);//刷新Cursor对象得以刷新adapter，来完成显示刷新
    }

}
