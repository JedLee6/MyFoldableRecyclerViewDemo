package com.happylee.mydemo.utils;

import android.content.Context;

import com.happylee.mydemo.utils.download.DBuilder;

public class DUtil {

    /**
     * 下载
     *
     * @param context
     * @return
     */
    public static DBuilder init(Context context) {
        return new DBuilder(context);
    }

  }
