package com.liao.viewfunny.view.recyclerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.liao.viewfunny.R;

import java.util.ArrayList;
import java.util.List;

public class AlphabetActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    String[] NAMES = {"Youku","youtube","Google","Ibm","Yahoo","alibaba", "Apple","Baidu", "Cancon","Letv","Iqiyi", "Jingdong", "Flipboard","Facebook"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alphabet);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new AlphabetItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        List<AlphabetItem> data = new ArrayList<>();
        for (String name : NAMES) {
            data.add(new Item(name));
        }
        recyclerView.setAdapter(new MyAdapter(data));


    }

    private static class Item extends AlphabetItem {
        private String name;

        public Item(String name) {
            this.name = name;
            mKey = name;
        }
    }

    public class MyAdapter extends AlphabetAdapter {

        public MyAdapter(List<AlphabetItem> items) {
            super(items);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Item item = (Item) getItem(position);
            ((MyViewHolder) holder).textView.setText(item.name);

        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.text);
        }
    }


}
