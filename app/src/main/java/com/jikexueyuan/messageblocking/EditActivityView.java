package com.jikexueyuan.messageblocking;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class EditActivityView extends AppCompatActivity implements View.OnClickListener {

    private int addKeywordsOrSends = 0;//1表示key，2表示send
//    private Intent i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_activity_view);
        findViewById(R.id.edit_keywords).setOnClickListener(this);
        findViewById(R.id.edit_senders).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(EditActivityView.this, AddActivityView.class);
        switch (v.getId()) {
            case R.id.edit_keywords:
                intent.addFlags(1);
//                intent.putExtra("falg", 1);
                break;
            case R.id.edit_senders:
                intent.addFlags(2);
//                intent.putExtra("falg", 2);
                break;
        }
        startActivity(intent);
    }
}
