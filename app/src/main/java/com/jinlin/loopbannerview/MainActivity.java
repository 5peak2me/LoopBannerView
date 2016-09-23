package com.jinlin.loopbannerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jinlin.loopbannerview.banner.BannerAdapter;
import com.jinlin.loopbannerview.banner.LoopViewPager;
import com.jinlin.loopbannerview.indicator.CirclePageIndicator;
import com.jinlin.loopbannerview.indicator.LinePageIndicator;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private void assignViews() {
        LoopViewPager viewPager = (LoopViewPager) findViewById(R.id.viewPager);
        LinePageIndicator linePageIndicator = (LinePageIndicator) findViewById(R.id.linePageIndicator);
        CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.circlePageIndicator);
        viewPager.setData(new BannerAdapter<String>(Arrays.asList(Images.URLS)) {
            @Override
            protected void bindTips(TextView tv, String o) {

            }

            @Override
            protected void bindImage(ImageView imageView, String s) {
                Glide.with(MainActivity.this).load(s).into(imageView);
            }

            @Override
            protected void onClick(View v, int position, String s) {
                Toast.makeText(MainActivity.this, position + "", Toast.LENGTH_SHORT).show();
            }

        });
        linePageIndicator.setViewPager(viewPager);
        circlePageIndicator.setViewPager(viewPager);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
    }

}
