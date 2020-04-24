package com.srise.aop;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.srise.aptlib.MyUtil;
import com.srise.libannotation.MyAnnotation;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivityTwo extends Activity {
    private static final String TAG = "shixi";

    @MyAnnotation(R.id.txt)
//    @BindView(R.id.txt)
    TextView mView;

    @MyAnnotation(R.id.img)
//    @BindView(R.id.img)
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_two);
//        ButterKnife.bind(this);
        MyUtil.bind(this);
        mView.setText("2222222222W");
        mImageView.setBackgroundColor(Color.parseColor("#0000FF"));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
