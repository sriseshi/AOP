package com.srise.aptlib;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;


public class MyUtil {
    static Map<Class<? extends Activity>, Constructor<? extends MyBinder>> sBinderMap = new HashMap<>();

    public static void bind(Activity activity) {
        Constructor constructor = sBinderMap.get(activity.getClass());

        try {
            if (constructor == null) {
                String forname = activity.getClass().getCanonicalName() + "_MyBinder";
                constructor = Class.forName(forname).getConstructor(activity.getClass(), View.class);
                sBinderMap.put(activity.getClass(), constructor);
            }

            constructor.newInstance(activity, activity.getWindow().getDecorView());
        } catch (Exception e) {
            Log.e("MyUtil", e.getMessage(), e);
        }
    }
}
