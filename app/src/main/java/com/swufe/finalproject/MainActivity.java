package com.swufe.finalproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,Runnable {

    private final String TAG = "MainActivity";
    DataManager manager = new DataManager(this);//获取数据库对象
    private Handler handler;
    private String updateTime;//构建公告更新字符串，表示上次更新时间
    private boolean error;//用于检查网络是否故障,没有故障为false
    private FragmentManager mfragmentManger;
    QuerySubFragment fragmentSub;
    SubNoticesFragment fragmentNotices;
    LinearLayout subLinear;
    LinearLayout notiLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Thread thread=new Thread(this);
        thread.start();
        subLinear= (LinearLayout) findViewById(R.id.sub);
        notiLinear= (LinearLayout) findViewById(R.id.notice);
        subLinear.setOnClickListener(this);
        notiLinear.setOnClickListener(this);
        mfragmentManger = getSupportFragmentManager();
        subLinear.performClick();
    }
    @Override
    public void onClick(View v) {
        FragmentTransaction fragmentTransaction = mfragmentManger.beginTransaction();//只能是局部变量，不能为全局变量，否则不能重复commit
        //FragmentTransaction只能使用一次
        hideAllFragment(fragmentTransaction);
        switch (v.getId()){
            case R.id.sub:
                setAllFalse();
                subLinear.setSelected(true);
                if (fragmentSub==null){
                    fragmentSub=new QuerySubFragment("Home");
                    fragmentTransaction.add(R.id.fragment_frame,fragmentSub);
                }else{
                    fragmentTransaction.show(fragmentSub);
                }
                break;
            case R.id.notice:
                setAllFalse();
                notiLinear.setSelected(true);
                if(fragmentNotices==null){
                    fragmentNotices=new SubNoticesFragment("List");
                    fragmentTransaction.add(R.id.fragment_frame,fragmentNotices);
                }else {
                    fragmentTransaction.show(fragmentNotices);
                }
                break;
        }
        fragmentTransaction.commit();//记得必须要commit,否则没有效果
    }
    private void setAllFalse() {
        subLinear.setSelected(false);
        notiLinear.setSelected(false);
    }

    private void hideAllFragment(FragmentTransaction fragmentTransaction) {
        if(fragmentSub!=null){
            fragmentTransaction.hide(fragmentSub);
        }
        if(fragmentNotices!=null){
            fragmentTransaction.hide(fragmentNotices);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            //转向搜索页面
            Intent query = new Intent(this, QueryActivity.class);
            startActivity(query);
        } else if (item.getItemId() == R.id.action_helper) {
            //弹出帮助对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("使用帮助").setMessage("这是一款针对成都市民的APP，在这个APP中您能看到下面的信息：\n" +
                    "1、成都地铁最近的通知公告；\n" +
                    "2、地铁最短路线查询；\n" +
                    "APP每天都会更新一次信息").setNegativeButton("使用帮助", null);
            builder.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void run() {
        Log.i(TAG,"getNews():getNews()....");
        //获取网络数据
        Document doc = null;
        List<DataItem> dataList=new ArrayList<DataItem>();
        try {
            doc = Jsoup.connect("http://cd.bendibao.com/traffic/chengduditie/").get();
            Log.i(TAG,"getNews():"+doc.title());
            //获取a中的数据
            Elements lis=doc.select("div.rim").select("li.J-has-share.listNews-item-s1.clearfix");
            String title,detail;
            for(int i=0;i<lis.size();i++){
                title=lis.get(i).select("a.J-share-a").text()+"——"+lis.get(i).select("p.desc").text()+"——"+lis.get(i).select("p.from").text()+"#";
                detail=lis.get(i).select("a.J-share-a").attr("href");
                Log.i(TAG,"getNews():"+title+detail);
                dataList.add(new DataItem(title,detail));
            }

            //把数据写入数据库
            manager.deleteAll(DBHelper.TB_NAME2);
            manager.addAll(dataList,DBHelper.TB_NAME2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}