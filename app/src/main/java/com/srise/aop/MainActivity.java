package com.srise.aop;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.srise.aptlib.MyUtil;
import com.srise.libannotation.MyAnnotation;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class MainActivity extends Activity {
    private static final String TAG = "shixi";

    @MyAnnotation(R.id.txt)
    TextView mView;

    @MyAnnotation(R.id.img)
    ImageView mImageView;

    @MyAnnotation(R.id.btn)
    Button mBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyUtil.bind(this);
        mView.setText("11111111111111W");
        mImageView.setBackgroundColor(Color.parseColor("#00FF00"));

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, MainActivityTwo.class));
            }
        });
        MyCall myCall = new MyCall();
        myCall.myOutCall();
        EventBus.getDefault().post(new MyEvent());
        EventBus.getDefault().postSticky(new MyEvent());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(MyEvent myEvent){

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
