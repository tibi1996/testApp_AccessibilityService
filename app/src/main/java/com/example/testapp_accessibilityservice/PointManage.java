package com.example.testapp_accessibilityservice;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PixelFormat;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;
import android.widget.LinearLayout;

public class PointManage {


    WindowManager.LayoutParams para = new WindowManager.LayoutParams();

    private float oldX;
    private float oldY;

    private float newX;
    private float newY;

    //Milli_second
    public int delay_time;
    private boolean tapFLG;

    PointView pointView;

    @SuppressLint("ClickableViewAccessibility")
    public PointManage (Context context, WindowManager wm, int pointerNum){

        delay_time = 100;
        tapFLG = false;

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


        pointView = new PointView(context, 100, pointerNum, true);

        pointView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        oldX = event.getRawX();
                        oldY = event.getRawY();

                        tapFLG = true;

                        break;

                    case MotionEvent.ACTION_MOVE:

                        newX = event.getRawX();
                        newY = event.getRawY();

                        if((Math.abs(oldX - newX) > 15.0f) || ((Math.abs(oldY - newY) > 15.0f))) {
                            para.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
                            para.format = PixelFormat.TRANSLUCENT;

                            para.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                    | WindowManager.LayoutParams.FLAG_FULLSCREEN;

                            para.gravity = 51;

                            para.width = 100;
                            para.height = 100;

                            para.x = ((int) (newX)) - para.width / 2;
                            para.y = ((int) (newY)) - para.height / 2;

                            wm.updateViewLayout(pointView, para);
                            tapFLG = false;
                            Log.i("PointManage", "ドラッグテスト");
                        }
                        break;


                    case MotionEvent.ACTION_UP:

                        if (tapFLG == true){
                            create_dialog(context);
                            Log.i("PointManage", "タップテスト");
                        }


                        break;
                }

                return false;
            }

        });


        wm.addView(pointView, para);
    }

    private void create_dialog(Context context){
        EditText numText = new EditText(context);
        numText.setInputType(InputType.TYPE_CLASS_NUMBER);
        numText.setText(String.valueOf(this.delay_time));

        @SuppressLint("ResourceType")
        AlertDialog create = new AlertDialog.Builder(context)
                .setTitle("タイトル")
                .setView(numText)
                .setPositiveButton("セット", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delay_time = Integer.parseInt(String.valueOf(numText.getText()));
                    }
                })
                .create();



        create.getWindow()
                .setType(2032);
        create.show();

    }

}
