package com.example.animationdemo;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

public class Util {
    public static float toDp(float value, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics());
    }

    public static float toSp(float value, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.getResources().getDisplayMetrics());
    }

    public static float getScreenWidth(Context context){
        return context.getResources().getDisplayMetrics().widthPixels;
    }
    public static float getScreenHeight(Context context){
        return context.getResources().getDisplayMetrics().heightPixels;
    }

}
