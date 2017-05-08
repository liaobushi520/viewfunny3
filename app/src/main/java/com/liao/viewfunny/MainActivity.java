package com.liao.viewfunny;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //      int[] array = {1, 22, 3, 66, 7, 88, 99, 10, 23, 12, 56};
        // quickSort(array, 0, array.length - 1);
//        insertSort(array);
//        for (int i = 0; i < array.length; i++) {
//            Log.e("array", "array[" + i + "]=" + array[i]);
//        }
    }


    //insert sort
    private void insertSort(int[] array) {
        for (int i = 0; i < array.length; i++) {
            if (i == 0) {
                continue;
            }
            int temp = array[i];
            int j = i - 1;
            for (; j >= 0 && array[j] > temp; j--) {
                array[j + 1] = array[j];
            }
            array[j + 1] = temp;
        }
    }

    /**
     * quick sort
     * @param array
     * @param start include
     * @param end   include
     */
    private void quickSort(int[] array, int start, int end) {
        if (start < end) {
            int mid = q(array, start, end);
            quickSort(array, start, mid - 1);
            quickSort(array, mid + 1, end);
        }
    }

    private int q(int[] array, int start, int end) {
        int cache = array[start];
        while (start < end) {
            while (start < end && array[end] > cache) end--;
            array[start] = array[end];
            while (start < end && array[start] < cache) start++;
            array[end] = array[start];
        }
        array[start] = cache;
        return start;
    }
}
