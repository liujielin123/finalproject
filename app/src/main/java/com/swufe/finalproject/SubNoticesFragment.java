package com.swufe.finalproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SubNoticesFragment extends Fragment {

    private String fragmentText;
    private TextView fragmentTextView;
    private static final String TAG="SlideshowFragment";
    private ListView out;
    private String data[];
    private SimpleAdapter listItemAdapter;//适配器
    private DataManager manager;

    public SubNoticesFragment(String fragmentText) {
        this.fragmentText=fragmentText;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sub_notices, container, false);

        manager=new DataManager(getActivity());
        List<DataItem> data_list=(List<DataItem>)manager.listAll(DBHelper.TB_NAME2);
        data=new String[data_list.size()];
        for(int m=0;m<data_list.size();m++){
            data[m]=data_list.get(m).getCurName()+data_list.get(m).getCurData();
        }

        out=root.findViewById(R.id.resultList_lecture);
        List<HashMap<String,String>> dataList=new ArrayList<HashMap<String, String>>();
        for(int j=0;j<data.length;j++){
            HashMap<String,String> map=new HashMap<String,String>();
            map.put("ItemTitle",data[j].substring(0,data[j].indexOf("#")));
            map.put("ItemDetail",data[j].substring(data[j].indexOf("#")+1));
            dataList.add(map);
        }
        listItemAdapter=new SimpleAdapter(getContext(),dataList,//listItems数据簿
                R.layout.list_item,//listItem的XML布局实现
                new String[]{"ItemTitle","ItemDetail"},
                new int[]{R.id.itemTitle,R.id.itemDetail}
        );

        out.setAdapter(listItemAdapter);
        out.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> map=(HashMap<String, String>) out.getItemAtPosition(position);
                String site=map.get("ItemDetail");

                //打开浏览器
                Intent intent = new Intent();
                intent.setData(Uri.parse(site));//Url 就是你要打开的网址
                intent.setAction(Intent.ACTION_VIEW);
                getActivity().startActivity(intent); //启动浏览器
            }
        });

        Log.i(TAG,"onCreateView:done");

        return root;
    }
}