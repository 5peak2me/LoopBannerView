package com.jinlin.loopbannerview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private String[] mRes = new String[]{
            "http://img.alicdn.com/imgextra/i3/2563613536/TB2C0TUX8LzQeBjSZFoXXc5gFXa_!!2563613536.jpg_q50.jpg",
            "http://img.alicdn.com/tps/TB1DA4ANXXXXXXnXVXXXXXXXXXX-1125-352.jpg_q50.jpg",
            "http://img.alicdn.com/tps/TB1xPGCNXXXXXbpXpXXXXXXXXXX-1125-352.jpg_q50.jpg"
    };

    private void assignViews() {
        LoopViewPager viewPager = (LoopViewPager) findViewById(R.id.viewPager);
        LinePageIndicator linePageIndicator = (LinePageIndicator) findViewById(R.id.linePageIndicator);
        viewPager.setData(Arrays.asList(mRes));
        linePageIndicator.setViewPager(viewPager);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        assignViews();
    }

}
