package com.example.fmodule;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import easynet.network.Session;

public class ReplyActivity extends AppCompatActivity {

    private String wxId;
    private String nickname;
    private String contactWxId;
    private String contactNickname;
    private String contactConRemark;
    private ListView replyListView;
    private ReplyListAdapter listAdapter;
    private MService mService;
    private Session session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        setTitle("自动回复");
        Intent intent = getIntent();
        wxId = intent.getStringExtra("wxId");
        nickname = intent.getStringExtra("nickname");
        contactWxId = intent.getStringExtra("contactWxId");
        contactNickname = intent.getStringExtra("contactNickname");
        contactConRemark = intent.getStringExtra("contactConRemark");
        replyListView = findViewById(R.id.replyList);
        Intent serviceIntent = new Intent(this, MService.class);
        bindService(serviceIntent, conn, Service.BIND_AUTO_CREATE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != 1 || requestCode != 1) {
            return;
        }
        listAdapter.refreshData();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_reply_msg, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add) {
            Intent intent = new Intent(ReplyActivity.this, ReplySettingsActivity.class);
            intent.putExtra("wxId", wxId);
            intent.putExtra("contactWxId", contactWxId);
            startActivityForResult(intent, 1);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onDestroy() {
        unbindService(conn);
        mService = null;
        session.unregisterErrorCallback(errorCallback);
        super.onDestroy();
    }
    //绑定服务回调
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MService.LocalBinder binder = (MService.LocalBinder)service;
            mService = binder.getService();
            SQLiteDatabase db = mService.getSQLiteHelper().getWritableDatabase();
            session = mService.getWxSession(wxId);
            session.registerErrorCallback(errorCallback);
            listAdapter = new ReplyListAdapter(ReplyActivity.this, wxId, contactWxId, db, session);
            replyListView.setAdapter(listAdapter);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };
    //session异常回调
    private Session.ErrorCallback errorCallback = new Session.ErrorCallback() {
        @Override
        public void run(Session session, Exception e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    };
}