package com.liao.viewfunny.config;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.liao.viewfunny.AlphabetActivity;
import com.liao.viewfunny.R;
import com.liao.viewfunny.view.recyclerview.AlphabetAdapter;
import com.liao.viewfunny.view.recyclerview.AlphabetItem;

import java.util.ArrayList;

import java.util.List;

/**
 * Created by liaozhongjun on 2017/5/8.
 */


public class StandardAdapter extends AlphabetAdapter {
    public static class Item extends AlphabetItem {
        private String name;
        private int imgId;

        public Item(String name, int imgId) {
            this.name = name;
            mKey = name;
            this.imgId = imgId;
        }
    }

    public static class StandardViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;

        public StandardViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.name);
            imageView = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }

    private static String[] NAMES = {"Youku", "youtube", "Google", "Ibm", "Yahoo", "alibaba", "Apple", "Baidu", "Cancon", "Letv", "Iqiyi", "Jingdong", "Flipboard", "Facebook"};

    public static List<AlphabetItem> getData() {
        List<AlphabetItem> items = new ArrayList<>();
        for (String name : NAMES) {
            items.add(new StandardAdapter.Item(name, Cheeses.getRandomCheeseDrawable()));
        }
        return items;
    }

    public StandardAdapter(boolean sort) {
        this(getData(), sort);
    }

    public StandardAdapter(List<AlphabetItem> items, boolean sort) {
        super(items, false);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new StandardViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Item item = (Item) getItem(position);
        ((StandardViewHolder) holder).textView.setText(item.name);
        Glide.with(holder.itemView.getContext()).load(Cheeses.getRandomCheeseDrawable()).into(((StandardViewHolder) holder).imageView);
    }


}



