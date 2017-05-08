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

//            for (int i = 0; i < s1.length(); i++) {
//                if (s2.length() <= i) {
//                    return 1;
//                }
//                s1.compareToIgnoreCase(s2);
//
//                if (s1.charAt(i) < s2.charAt(i)) {
//                    return -1;
//                } else if (s1.charAt(i) > s2.charAt(i)) {
//                    return 1;
//                }
//            }
//            if (s1.length() == s2.length()) {
//                return 0;
//            } else if (s1.length() < s2.length()) {
//                return -1;
//            } else {
//                return 1;
//            }
        }
    }
}
