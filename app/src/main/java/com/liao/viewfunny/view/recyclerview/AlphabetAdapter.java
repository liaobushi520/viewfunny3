package com.liao.viewfunny.view.recyclerview;


import android.nfc.Tag;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.liao.viewfunny.config.StandardAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by liaozhongjun on 2017/5/5.
 */

public abstract class AlphabetAdapter extends RecyclerView.Adapter {
    public static final AlphabetItem.AlphabetComparator COMPARATOR = new AlphabetItem.AlphabetComparator();
    private static final String TAG = "AlphabetAdapter";


    private List<AlphabetItem> mItems;
    private Map<Character, Integer> mStartPosArray;

    private boolean needSort;

    public AlphabetAdapter(List<AlphabetItem> items, boolean sort) {
        if (sort) {
            Collections.sort(items, COMPARATOR);
            mStartPosArray = findStartPosition(items);
        }
        needSort = sort;
        mItems = items;

    }

    public void addItems(int start, List<AlphabetItem> items) {
        if (needSort) {
            Log.w(TAG, "not support add collection for order list");
            return;
        }
        if (items != null && items.size() > 0) {
            mItems.addAll(start, items);
            notifyItemRangeInserted(start, items.size());
        }
    }

    public void addItems(List<AlphabetItem> items) {
        int start = mItems.size() - 1;
        addItems(start, items);
    }

    public void addItem(AlphabetItem item) {
        if (!needSort) {
            mItems.add(item);
            notifyItemInserted(mItems.size() - 1);
            return;
        }


        if (item == null) {
            return;
        }
        char firstChar = item.mKey.charAt(0);
        char nextChar = (char) (firstChar + 1);
        int startPos = mStartPosArray.get(firstChar);
        int endPos = mStartPosArray.get(nextChar) - 1;
        int i = startPos;
        boolean isAdded = false;
        for (; i <= endPos; i++) {
            if (COMPARATOR.compare(item, mItems.get(i)) < 0) {
                mItems.add(i, item);
                isAdded = true;
                break;
            }
        }
        if (!isAdded) {
            mItems.add(i, item);
            notifyItemInserted(i);
        }
        Iterator<Map.Entry<Character, Integer>> iterator = mStartPosArray.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Character, Integer> entry = iterator.next();
            if (entry.getKey() > firstChar) {
                entry.setValue(entry.getValue() + 1);
            }
        }

    }

    private Map<Character, Integer> findStartPosition(List<AlphabetItem> data) {
        Map<Character, Integer> startPosList = new HashMap();
        char curChar = '+';
        for (int i = 0; i < data.size(); i++) {
            AlphabetItem item = data.get(i);
            char firstChar = Character.toUpperCase(item.mKey.charAt(0));
            if (curChar != firstChar) {
                startPosList.put(firstChar, i);
                curChar = firstChar;
            }
        }
        return startPosList;
    }


    public AlphabetItem getItem(int pos) {
        return mItems.get(pos);
    }

    public int getStartPositinForChar(char c) {
        char upperChar = Character.toUpperCase(c);
        return mStartPosArray.get(upperChar) == null ? -1 : mStartPosArray.get(upperChar);
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }


}
