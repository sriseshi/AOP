package com.srise.aop;

import android.app.Activity;
import android.os.Bundle;


public class MainActivity extends Activity {
    private static final String TAG = "shixi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
