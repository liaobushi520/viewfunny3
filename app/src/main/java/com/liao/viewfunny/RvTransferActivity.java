package com.liao.viewfunny;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.liao.viewfunny.transfer.ObservableItemAnimator;
import com.liao.viewfunny.transfer.TransferLayout;

import java.util.ArrayList;
import java.util.List;

public class RvTransferActivity extends AppCompatActivity {

    public static final String TAG = "RV";
    private RecyclerView recyclerView;
    private Button send;
    private EditText editText;
    private TransferLayout transferLayout;

    private Adapter mAdapter;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_rv_transfer);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        recyclerView = (RecyclerView) findViewById(R.id.rv);
        send = (Button) findViewById(R.id.btn);
        editText = (EditText) findViewById(R.id.et);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {

            }
        });

        transferLayout = (TransferLayout) findViewById(R.id.transfer_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new Adapter();
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new ObservableItemAnimator(recyclerView, transferLayout));

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = editText.getEditableText().toString();
                if (!TextUtils.isEmpty(s)) {
                    ChatItem chatItem = new ChatItem();
                    chatItem.text = s;
                    chatItem.time = System.currentTimeMillis();
                    chatItem.type = chatItem.time % 2 == 1 ? ChatItem.MINE_CHAT_ITEM : ChatItem.OTHER_CHAT_ITEM;
                    mAdapter.addItem(chatItem);
                    recyclerView.scrollToPosition(mAdapter.getItemCount() - 1);
                }

            }
        });
    }


    public static class ChatItem {
        public static final int MINE_CHAT_ITEM = 1;
        public static final int OTHER_CHAT_ITEM = 2;

        public int type;
        public CharSequence text;
        public long time;
    }

    public class Adapter extends RecyclerView.Adapter<LiaoViewHolder> {

        private List<ChatItem> mChatItmes;

        public Adapter() {
            mChatItmes = new ArrayList<>();
        }

        public void addItem(ChatItem chatItem) {
            if (chatItem != null) {
                mChatItmes.add(chatItem);
                notifyItemInserted(mChatItmes.size());
            }
        }

        public ChatItem getItem(int pos) {
            if (pos < 0 || pos >= mChatItmes.size()) {
                return null;
            }
            return mChatItmes.get(pos);

        }

        @Override
        public LiaoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_chat_item, parent, false);
            return new LiaoViewHolder(v);
        }

        @Override
        public void onBindViewHolder(LiaoViewHolder holder, int position) {
            holder.chatText.setText(mChatItmes.get(position).text);

        }

        @Override
        public int getItemCount() {
            return mChatItmes.size();
        }
    }

    public static class LiaoViewHolder extends RecyclerView.ViewHolder {

        public TextView chatText;

        public LiaoViewHolder(View itemView) {
            super(itemView);
            chatText = (TextView) itemView.findViewById(R.id.chat_item_tv);
        }
    }

}
