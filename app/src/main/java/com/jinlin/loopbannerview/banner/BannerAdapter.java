package com.jinlin.loopbannerview.banner;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by J!nl!n on 2016/9/23 15:58.
 * Copyright © 1990-2016 J!nl!n™ Inc. All rights reserved.
 * ━━━━━━神兽出没━━━━━━
 * 　　　┏┓　　　┏┓
 * 　　┏┛┻━━━┛┻┓
 * 　　┃　　　　　　　┃
 * 　　┃　　　━　　　┃
 * 　　┃　┳┛　┗┳　┃
 * 　　┃　　　　　　　┃
 * 　　┃　　　┻　　　┃
 * 　　┃　　　　　　　┃
 * 　　┗━┓　　　┏━┛Code is far away from bug with the animal protecting
 * 　　　　┃　　　┃    神兽保佑,代码无bug
 * 　　　　┃　　　┃
 * 　　　　┃　　　┗━━━┓
 * 　　　　┃　　　　　　　┣┓
 * 　　　　┃　　　　　　　┏┛
 * 　　　　┗┓┓┏━┳┓┏┛
 * 　　　　　┃┫┫　┃┫┫
 * 　　　　　┗┻┛　┗┻┛
 * ━━━━━━感觉萌萌哒━━━━━━
 */
public abstract class BannerAdapter<T> {

    private List<T> mData;

    protected BannerAdapter(List<T> data) {
        mData = data;
    }

    List<T> getData() {
        return mData;
    }

    int getCount() {
        return mData == null ? 0 : mData.size();
    }

    Object getItem(int position) {
        return mData.get(position);
    }

    protected abstract void bindTips(TextView tv, T t);

    protected abstract void bindImage(ImageView imageView, T t);

    protected abstract void onClick(View v, int position, T t);

}
