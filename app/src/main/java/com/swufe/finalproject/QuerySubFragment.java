package com.swufe.finalproject;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuerySubFragment extends Fragment implements Runnable{

    private String fragmentText;
    private TextView fragmentTextView;
    ArrayAdapter<String> lineAdapter;  //省级适配器
    ArrayAdapter<String> stationAdapter;    //地级适配器
    private static final String TAG="QuerySubFragment";
    private Handler handler;
    private String updateTime;//构建公告更新字符串，表示上次更新时间
    private DataManager manager= new DataManager(getContext());

    //线路
    private String[] lines;
    //站点
    private String[][] stations;

    public QuerySubFragment(String fragmentText) {
        this.fragmentText=fragmentText;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_query_sub, container, false);

        /*//获取上次更新的时间
        SharedPreferences sp = getActivity().getSharedPreferences("myUpdate", Activity.MODE_PRIVATE);
        updateTime = sp.getString("updateTime", "0000.00.00");

        //获取系统当前时间
        SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");//设置日期格式
        final String OSTime = df.format(new Date());//获取系统当前时间
        Log.i(TAG, "onCreate:OSTime=" + OSTime);

        if(updateTime != OSTime){
            //开启子线程完成更新，并将数据保存至数据库

            updateTime = OSTime;
            Log.i(TAG, "run:更新数据");
        }*/
        Thread thread=new Thread(this);
        thread.start();
        //处理子线程的返回
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                if (msg.what == 5) {
                    /*SharedPreferences.Editor edit = getActivity().getSharedPreferences("myUpdate", Activity.MODE_PRIVATE).edit();
                    edit.putString("updateTime", updateTime);
                    edit.commit();
                    Log.i(TAG, "run:更新日期：" + updateTime);*/
                    List<DataItem> data_list=(List<DataItem>)msg.obj;
                    lines=new String[data_list.size()];
                    stations=new String[data_list.size()][];
                    for(int m=0;m<data_list.size();m++){
                        lines[m]=data_list.get(m).getCurName();
                        stations[m]=data_list.get(m).getCurData().split("=>");
                    }
                    setSpinner(root.findViewById(R.id.spinner1),root.findViewById(R.id.spinner2));
                    setSpinner(root.findViewById(R.id.spinner3),root.findViewById(R.id.spinner4));
                    Log.i(TAG, "onActivityResult:handlerMessage:committing of updating finished");
                    Toast.makeText(getActivity(), "更新成功", Toast.LENGTH_SHORT).show();
                }
                super.handleMessage(msg);
            }
        };

        TextView show=root.findViewById(R.id.showShortestPath);
        Button query=root.findViewById(R.id.searchButton);
        query.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Spinner spinner2=root.findViewById(R.id.spinner2);
                Spinner spinner4=root.findViewById(R.id.spinner4);
                FindShortestPath find=new FindShortestPath();
                show.setText(find.getPath(spinner2.getSelectedItem().toString(),spinner4.getSelectedItem().toString()));
            }
        });
        return root;
    }

    private void setSpinner(Spinner lineSpinner,Spinner stationSpinner) {
        //绑定适配器和值
        lineAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,lines);
        lineSpinner.setAdapter(lineAdapter);
        lineSpinner.setSelection(0,true);  //设置默认选中项，此处为默认选中第4个值

        stationAdapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, stations[0]);
        stationSpinner.setAdapter(stationAdapter);
        stationSpinner.setSelection(0,true);  //默认选中第0个


        //省级下拉框监听
        lineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            // 表示选项被改变的时候触发此方法，主要实现办法：动态改变地级适配器的绑定值
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                //position为当前省级选中的值的序号

                //将地级适配器的值改变为city[position]中的值
                stationAdapter = new ArrayAdapter<String>(
                        getContext(), android.R.layout.simple_spinner_item, stations[position]);
                // 设置二级下拉列表的选项内容适配器
                stationSpinner.setAdapter(stationAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

    }
    @Override
    public void run() {
        Log.i(TAG, "getSubway():getSubway....");
        //获取网络数据
        Document doc = null;
        List<DataItem> dataList = new ArrayList<DataItem>();
        try {
            doc = Jsoup.connect("http://cd.bendibao.com/ditie/linemap.shtml").get();
            Log.i(TAG, "getSubway():" + doc.title());
            Elements lines = doc.select("div.line-list");
            //Log.i(TAG,"getSubway():"+detail);
            String linena, sites = "";
            for (int i = 0; i < lines.size(); i++) {
                linena = lines.get(i).select("strong").text();
                Elements divs = lines.get(i).select("div.station");
                for (int j = 0; j < divs.size(); j++) {
                    sites = sites + divs.get(j).select("a.link").text() + "=>";
                }
                Log.i(TAG, "getSubway():" + linena + sites);
                dataList.add(new DataItem(linena, sites));
                sites = "";
            }

            //把数据写入数据库
            Message msg=handler.obtainMessage(7);
            msg.what=5;
            //msg.obj="Hello from run()";
            msg.obj=dataList;
            handler.sendMessage(msg);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*public void run() {
        try {
            getSubway();//获取地铁站点
            getNews();//获取公告
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "run:请检查网络，如果网络没问题则说明网页已改变，那么请修改解析网页源代码");
        }
    }*/

    private void getNews(){
        Log.i(TAG,"getNews():getNews()....");
        //获取网络数据
        Document doc = null;
        List<DataItem> dataList=new ArrayList<DataItem>();
        try {
            doc = Jsoup.connect("http://cd.bendibao.com/traffic/chengduditie/").get();
            Log.i(TAG,"getNews():"+doc.title());
            //获取a中的数据
            Elements lis=doc.select("div.rim").select("li");
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