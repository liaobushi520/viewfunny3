package com.liao.viewfunny.view.recyclerview;

import java.util.Comparator;

/**
 * Created by liaozhongjun on 2017/5/5.
 */

public class AlphabetItem {
    protected String mKey;

    public static class AlphabetComparator implements Comparator<AlphabetItem> {


        @Override
        public int compare(AlphabetItem o1, AlphabetItem o2) {
            String s1 = o1.mKey;
            String s2 = o2.mKey;
            return s1.compareToIgnoreCase(s2);
        }
    }
}
