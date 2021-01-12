package com.example.testapp_accessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Test_AccessibilityService extends AccessibilityService implements View.OnTouchListener{

    FrameLayout layout;
    WindowManager wm;
    WindowManager.LayoutParams para = new WindowManager.LayoutParams();

    private ScheduledExecutorService schedule;
    /** スレッドUI操作用ハンドラ */
    private Handler mHandler = new Handler();
    /** テキストオブジェクト */
    private Runnable updateText;

    boolean onOff;
    TextView texViewArry[] = new TextView[20];
    PointManage pointViewArry[] = new PointManage[20];

    int loopCounter = 0;

    ArrayList<PointManage> listPointView = new ArrayList<PointManage>();


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        // レイアウトを作るか確認
        if(intent.getAction().equals("ON")) {
            wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            layout = new FrameLayout(this);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();



            lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
            lp.format = PixelFormat.TRANSLUCENT;

            lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_FULLSCREEN;


            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER_VERTICAL|Gravity.LEFT;


            LayoutInflater inflater = LayoutInflater.from(this);
            inflater.inflate(R.layout.menu, layout);

            //レイアウトを追加
            wm.addView(layout, lp);
        }else if(intent.getAction().equals("OFF")) {
            if(layout != null) {
                //レイアウトを削除
                wm.removeViewImmediate(layout);
            }
            if(schedule != null) {
                schedule.shutdown();
            }
            int cnt = 0;
            while (cnt < listPointView.size()){
                wm.removeView(listPointView.get(cnt).pointView);
                listPointView.remove(cnt);
                cnt++;
            }

            //ユーザー補助をオフにする
            disableSelf();

        }else{

        }

        TextView txtG = this.layout.findViewById(R.id.txtG);
        TextView txtB = this.layout.findViewById(R.id.txtB);
        TextView txtP = this.layout.findViewById(R.id.txtP);
        TextView txtY = this.layout.findViewById(R.id.txtY);
        TextView txtO = this.layout.findViewById(R.id.txtO);
        TextView txtR = this.layout.findViewById(R.id.txtR);

        txtG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("txtG_onClick", "onClick");
            }
        });

        txtB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTextView();
            }
        });

        txtP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tap();
            }
        });

        txtO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSchedule();
            }
        });

        txtR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLayout();

            }
        });

        txtG.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        //down
                        Log.i("txtG_onTouch", "ACTION_DOWN");
                        break;

                    case MotionEvent.ACTION_MOVE:
                        //move
                        Log.i("txtG_onTouch", "ACTION_MOVE");
                        break;

                    case MotionEvent.ACTION_UP:
                        //up
                        if(schedule != null) {
                            schedule.shutdown();
                        }
                        Log.i("txtG_onTouch", "ACTION_UP");
                        break;
                }

                return true;
            }

        });

        txtY.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        //down
                        Log.i("txtG_onTouch", "ACTION_DOWN");

                        int cnt = 0;
                        while (cnt < listPointView.size()) {
                            if (!onOff) {
                                //叩ける
                                listPointView.get(cnt).para.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                                        | WindowManager.LayoutParams.FLAG_FULLSCREEN;
                                wm.updateViewLayout(listPointView.get(cnt).pointView, listPointView.get(cnt).para);
                                Log.i("add_View", String.valueOf(onOff));
                            } else {
                                //叩けない
                                listPointView.get(cnt).para.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                                        | WindowManager.LayoutParams.FLAG_FULLSCREEN;
                                wm.updateViewLayout(listPointView.get(cnt).pointView, listPointView.get(cnt).para);
                                Log.i("add_View", String.valueOf(onOff));
                            }
                            cnt++;
                        }

                        onOff = !onOff;

                        break;

                    case MotionEvent.ACTION_MOVE:
                        //move
                        Log.i("txtG_onTouch", "ACTION_MOVE");
                        break;

                    case MotionEvent.ACTION_UP:
                        //up
                        Log.i("txtG_onTouch", "ACTION_UP");
                        break;


                }

                return true;
            }
        });

        return START_STICKY;
    }

    //追加
    @SuppressLint("ClickableViewAccessibility")
    private void addTextView(){


        //多分　100は大きさ　1は文字数字　trueは？
        //PointView addPoint = new PointView(this, 100, 1, true);
        int pointerNum = listPointView.size() + 1;
        PointManage addPoint =new PointManage(this, wm, pointerNum);



        listPointView.add(addPoint);

            //para = e(100);
            int w = addPoint.pointView.getResources().getDisplayMetrics().widthPixels;
            int h = addPoint.pointView.getResources().getDisplayMetrics().heightPixels;
            addPoint.para.x = (w / 2) - (100 / 2);
            addPoint.para.y = (h / 2) - (100 / 2);

            Log.i("add_View", String.valueOf(w));
            Log.i("add_View", String.valueOf(h));

            addPoint.para.flags = para.flags & -17;
            //wm.addView(listPointView.get(0), addPoint.para);

            addPoint.pointView.postInvalidate();

            onOff = false;

    }

    //autoTap
    private void tap(){
        try {
            //オートクリックコマンド
            int x = 0;
            int y = 0;

            if(listPointView.get(loopCounter) == null) {
                return;
            }
                int zahyo[] = new int[2];

                //座標取得
                listPointView.get(loopCounter).pointView.getLocationOnScreen(zahyo);
                x = zahyo[0];
                y = zahyo[1];


                Log.i("add_View", "bbb");




            //GestureDescriptionテスト-----------------------------
            //場所決め?
            //Point position = new Point(x -10, y-10);
            Point position = new Point(x, y);
            Path p = new Path();
            p.moveTo(position.x, position.y);

            GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(p, listPointView.get(loopCounter).delay_time, 15);
            GestureDescription.Builder builder = new GestureDescription.Builder();
            builder.addStroke(strokeDescription);

            GestureDescription gesture = builder.build();
            //実行?
            boolean isDispatched = dispatchGesture(gesture, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.i("tap_comp", "ok");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.i("tap_cancel", "ok");
                }
            }, null);



        }catch (Exception e){
            Log.i("add_View", e.getMessage().toString());
        }
    }


    //------------------------------------------------------
    //      スケジュール設定
    //------------------------------------------------------
    private void setSchedule(){
        schedule = Executors.newSingleThreadScheduledExecutor();

        updateText = new Runnable() {
            public void run() {
                tap();

                mHandler.removeCallbacks(updateText);
                mHandler.postDelayed(updateText, listPointView.get(loopCounter).delay_time + 15);

                if (loopCounter + 1 == listPointView.size()){
                    loopCounter = 0;
                }else{
                    loopCounter++;
                }


            }
        };
        mHandler.postDelayed(updateText, 0);
    }

    //メニュー縦横変更テスト
    private void changeLayout() {
        LinearLayout ll = this.layout.findViewById(R.id.main_layout);
        if(ll.getOrientation() == LinearLayout.VERTICAL) {
            ll.setOrientation(LinearLayout.HORIZONTAL);

            ViewGroup.LayoutParams para = ll.getLayoutParams();

            para.height = para.width;
            para.width = para.height;
            ll.setLayoutParams(para);
            this.layout.findViewById(R.id.txtB).setVisibility(View.VISIBLE);

        }else{
            ll.setOrientation(LinearLayout.VERTICAL);

            ViewGroup.LayoutParams para = ll.getLayoutParams();
            int height = para.height;
            int width = para.width;
            para.height = width;
            para.width = height;
            ll.setLayoutParams(para);


            //ll.removeView(this.layout.findViewById(R.id.txtB));
            this.layout.findViewById(R.id.txtB).setVisibility(View.GONE);
        }

    }


    public void onServiceConnected() {
        super.onServiceConnected();
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event){

    }

    @Override
    public void onInterrupt(){

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.i("add_View", "d");
        return false;
    }
    /* access modifiers changed from: protected */
    public WindowManager.LayoutParams e(int i2) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(i2, i2, 2032, 1288, -3);
        layoutParams.gravity = 51;

        return layoutParams;
    }
/*
    private void q() {
        this.k.updateViewLayout(this.m, this.n);
    }

 */

}
