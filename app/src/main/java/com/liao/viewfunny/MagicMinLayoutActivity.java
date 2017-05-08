package com.liao.viewfunny;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.liao.viewfunny.config.Cheeses;
import com.liao.viewfunny.config.StandardAdapter;
import com.liao.viewfunny.view.MagicMinLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MagicMinLayoutActivity extends AppCompatActivity {
    private RecyclerView imgsRecyclerView;

    private List imgs;
    private ImageView imageView;
    private MagicMinLayout magicMinLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.magic_min_layout);

        imgsRecyclerView = (RecyclerView) findViewById(R.id.imgs_rv);
        imgsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        imgs = new ArrayList();
        for (int i = 0; i < 30; i++) {
            imgs.add(Cheeses.getRandomNetworkImage());
        }
        imgsRecyclerView.setAdapter(new ImageAdapter(imgs));

        imageView = (ImageView) findViewById(R.id.video);
        magicMinLayout = (MagicMinLayout) findViewById(R.id.magic_min);
        recyclerView = (RecyclerView) findViewById(R.id.other);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RecyclerView.Adapter adepter = new StandardAdapter();
        recyclerView.setAdapter(adepter);


    }

    public class ImageAdapter extends RecyclerView.Adapter {
        private List mImgs;

        public ImageAdapter(List imgs) {
            this.mImgs = imgs;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_img_item, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            Glide.with(imageViewHolder.itemView.getContext())
                    .load(mImgs.get(position))
                    .fitCenter()
                    .into(imageViewHolder.iv);
            imageViewHolder.iv.setBackgroundColor(Color.BLUE);
            imageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Glide.with(v.getContext())
                            .load(mImgs.get(position))
                            .fitCenter()
                            .into(imageView);
                    magicMinLayout.onClick(v);
                }
            });

        }

        @Override
        public int getItemCount() {
            return mImgs == null ? 0 : mImgs.size();
        }
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView iv;

        public ImageViewHolder(View itemView) {
            super(itemView);
            iv = (ImageView) itemView.findViewById(R.id.image);
        }
    }


}
