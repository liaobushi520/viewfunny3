package com.liao.viewfunny;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.widget.LinearLayout;


import com.liao.viewfunny.config.StandardAdapter;
import com.liao.viewfunny.view.recyclerview.AlphabetItem;
import com.liao.viewfunny.view.recyclerview.AlphabetItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class AlphabetActivity extends AppCompatActivity {

    RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alphabet);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new AlphabetItemDecoration(getApplicationContext(), LinearLayout.VERTICAL));
        recyclerView.setAdapter(new StandardAdapter());
    }


}
