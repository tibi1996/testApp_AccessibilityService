package com.example.testapp_accessibilityservice;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class PointManage {


    WindowManager.LayoutParams para = new WindowManager.LayoutParams();

    private float oldX;
    private float oldY;

    private float newX;
    private float newY;

    PointView pointView;

    @SuppressLint("ClickableViewAccessibility")
    public PointManage (Context context, WindowManager wm){

        //WindowManager.LayoutParams設定
        para.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        para.format = PixelFormat.TRANSLUCENT;

        para.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_FULLSCREEN;

        /*
        int w = pointView.getResources().getDisplayMetrics().widthPixels;
        int h = pointView.getResources().getDisplayMetrics().heightPixels;

        para.x = (w / 2) - (100 / 2);
        para.y = (h / 2) - (100 / 2);
        */

        para.width = 100;
        para.height = 100;

        para.gravity = 0;


        pointView = new PointView(context, 100, 1, true);

        pointView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        oldX = event.getRawX();
                        oldY = event.getRawY();

                        break;

                    case MotionEvent.ACTION_MOVE:

                        newX = event.getRawX();
                        newY = event.getRawY();

                        para.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
                        para.format = PixelFormat.TRANSLUCENT;

                        para.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                | WindowManager.LayoutParams.FLAG_FULLSCREEN;


                        para.width = 100;
                        para.height = 100;
                        para.gravity = 0;

                        para.x = ((int)(newX - oldX));
                        para.y = ((int)(newY - oldY));

                        wm.updateViewLayout(pointView, para);
                        break;

                    case MotionEvent.ACTION_UP:

                        break;
                }

                return false;
            }

        });


        wm.addView(pointView, para);
    }

}
