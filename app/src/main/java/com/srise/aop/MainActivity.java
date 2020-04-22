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


public class MainActivity extends Activity {
    private static final String TAG = "shixi";

    @MyAnnotation(R.id.txt)
    TextView mView;

    @MyAnnotation(R.id.img)
    ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        ButterKnife.bind(this);
        MyUtil.bind(this);
        mView.setText("11111111111111W");
        mImageView.setBackgroundColor(Color.parseColor("#00FF00"));
        MyCall myCall = new MyCall();
        myCall.myOutCall();
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
