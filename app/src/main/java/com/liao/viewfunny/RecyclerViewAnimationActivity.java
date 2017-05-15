package com.liao.viewfunny;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;

import com.liao.viewfunny.config.StandardAdapter;
import com.liao.viewfunny.view.recyclerview.AlphabetAdapter;
import com.liao.viewfunny.view.recyclerview.DispersedAnimator;


public class RecyclerViewAnimationActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSrL;
    private RecyclerView mRv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view_animation);
        mRv = (RecyclerView) findViewById(R.id.rv);
        mSrL = (SwipeRefreshLayout) findViewById(R.id.srl);
        mRv.setAdapter(new StandardAdapter(false));
        mRv.setItemAnimator(new DispersedAnimator());
        mSrL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRv.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AlphabetAdapter adapter = (AlphabetAdapter) mRv.getAdapter();
                        adapter.addItems(1,StandardAdapter.getData());
                        mSrL.setRefreshing(false);
                    }
                }, 3000);
            }
        });
    }
}
