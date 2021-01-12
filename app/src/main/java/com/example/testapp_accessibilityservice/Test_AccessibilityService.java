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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
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

    FrameLayout ctrlPanel;
    WindowManager wm;
    WindowManager.LayoutParams para = new WindowManager.LayoutParams();

    private ScheduledExecutorService schedule;
    /** スレッドUI操作用ハンドラ */
    private Handler mHandler = new Handler();
    /** テキストオブジェクト */
    private Runnable updateText;

    boolean onOff;

    int loopCounter = 0;

    //アクションネーム
    public final String ACTION_SOLO = "SOLO";
    public final String ACTION_MULTI = "MULTI";
    public final String ACTION_END = "END";

    //true = stop画像・false = play画像
    private boolean onoff_flg = false;



    ArrayList<PointManage> listPointView = new ArrayList<PointManage>();


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        String action = intent.getAction();

        //ソロ、マルチフラグ
        int SM_flg = 2;

        //アクションを確認
        if(action.equals(ACTION_END)){

            //コントロールパネルの削除
            if(ctrlPanel != null) {
                wm.removeViewImmediate(ctrlPanel);
                ctrlPanel = null;
            }

            //スケジュールのシャットダウン
            if(schedule != null) {
                onoff_flg = false;
            }
            //ポインターを削除
            while(0 != listPointView.size()){
                wm.removeView(listPointView.get(listPointView.size() - 1).pointView);
                listPointView.remove(listPointView.get(listPointView.size() - 1));
            }

            return START_STICKY;

        }else if(action.equals(ACTION_SOLO)){
            SM_flg = 1;
            //コントロールパネルを表示
            addCtrlPanel(SM_flg);

            //ポインター削除
            while(0 != listPointView.size()){
                wm.removeView(listPointView.get(listPointView.size() - 1).pointView);
                listPointView.remove(listPointView.get(listPointView.size() - 1));
            }


            //ポインターを1つ追加する
            addPoint();

        }else if(action.equals(ACTION_MULTI)){
            SM_flg = 2;
            //コントロールパネルを表示
            addCtrlPanel(SM_flg);
        }



        //再生非再生を取得
        ImageView imgOnOff = this.ctrlPanel.findViewById(R.id.imgOnOff);
        //ポインタ追加を取得
        ImageView imgPlus = this.ctrlPanel.findViewById(R.id.imgPlus);
        //ポインタ削除を取得
        ImageView imgMinus = this.ctrlPanel.findViewById(R.id.imgMinus);
        //スワイプ追加を取得
        ImageView imgSwip = this.ctrlPanel.findViewById(R.id.imgSwipe);
        //終了を取得
        ImageView imgClose = this.ctrlPanel.findViewById(R.id.imgClose);
        //コントロールパネルのドラッグを取得
        ImageView imgTouch = this.ctrlPanel.findViewById(R.id.imgTouch);

        //再生非再生処理
        imgOnOff.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //切り替え
                if(onoff_flg){
                    onoff_flg = false;
                    //ポインターの状態を変更する
                    changePointerParam();


                    //PLAY画像に変更する
                    cangeOnOffImage(2);



                }else{
                    //ポインターの有無確認
                    if(0 != listPointView.size()){
                        //ポインターが1つ以上の場合
                        onoff_flg = true;
                        //ポインターの状態を変更する
                        changePointerParam();

                        //ループ開始
                        setSchedule();

                        //STOP画像に変更する
                        cangeOnOffImage(1);

                    }
                }

                return false;
            }
        });

        //ポインタ追加処理
        imgPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ポインター追加
                addPoint();
            }
        });

        //ポインタ削除処理
        imgMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ポインター1つ削除
                if(0 != listPointView.size()){
                    wm.removeView(listPointView.get(listPointView.size() - 1).pointView);
                    listPointView.remove(listPointView.get(listPointView.size() - 1));
                }
            }
        });

        return START_STICKY;

    }

    //-------------------------------------------------------------------------
    //  cangeOnOffImage
    //  引数      int         flg     1 = STOP
    //                                2 = PLAY
    //  返り値    void
    //  備考
    //-------------------------------------------------------------------------
    private void cangeOnOffImage(int flg){
        ImageView iv = this.ctrlPanel.findViewById(R.id.imgOnOff);
        if(1 == flg){
            iv.setImageResource(R.drawable.img_stop);
        }else if(2 == flg){
            iv.setImageResource(R.drawable.img_play);
        }

    }

    //-------------------------------------------------------------------------
    //  addPoint
    //  引数      void
    //  返り値    void
    //  備考
    //-------------------------------------------------------------------------
    @SuppressLint("ClickableViewAccessibility")
    private void addPoint(){
        //ポインターを作成
        int pointerNum = listPointView.size() + 1;
        PointManage addPoint =new PointManage(this, wm, pointerNum);

        //作成したポインターをリストに追加
        listPointView.add(addPoint);

        //ポインターの座標を画面の中央に設定
        int w = addPoint.pointView.getResources().getDisplayMetrics().widthPixels;
        int h = addPoint.pointView.getResources().getDisplayMetrics().heightPixels;
        addPoint.para.x = (w / 2) - (100 / 2);
        addPoint.para.y = (h / 2) - (100 / 2);

//            addPoint.para.flags = para.flags & -17;

        addPoint.pointView.postInvalidate();

        onOff = false;

    }

    //-------------------------------------------------------------------------
    //  autoTap
    //  引数      void
    //  返り値    void
    //  備考
    //-------------------------------------------------------------------------
    private void autoTap(){
        try {
            //オートクリックコマンド
            int x = 0;
            int y = 0;

            //nullチェック      ※必要ない？
            if(listPointView.get(loopCounter) == null) {
                return;
            }
            int zahyo[] = new int[2];

            //ポインターの座標取得
            listPointView.get(loopCounter).pointView.getLocationOnScreen(zahyo);
            x = zahyo[0];
            y = zahyo[1];

            //ポインター座標にタップ位置を設定
            Point position = new Point(x, y);
            Path p = new Path();
            p.moveTo(position.x, position.y);

            //ポインタに設定されたdelay_time後に、15mS間のタップ設定
            GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(p, 15, 100);
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

    //-------------------------------------------------------------------------
    //  changePointerParam
    //  引数      void
    //  返り値    void
    //  備考
    //-------------------------------------------------------------------------
    private void changePointerParam(){
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
    }

    //-------------------------------------------------------------------------
    //  addCtrlPanel
    //  引数      int     SM_flg      :サービス起動時のアクションネーム
    //  返り値    void
    //  備考
    //-------------------------------------------------------------------------
    private void addCtrlPanel(int SM_flg){
        if(null != ctrlPanel) {
            wm.removeView(ctrlPanel);
        }
        //ウィンドウマネージャー作成
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ctrlPanel = new FrameLayout(this);

        //ウィンドウマネージャーのパラメータ設定
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();

        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_FULLSCREEN;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;


        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.ctrlpanel, ctrlPanel);

        //ソロ・マルチレイアウト変更
        if(1 == SM_flg){
            //プラス・マイナス・スワイプを非表示
            this.ctrlPanel.findViewById(R.id.imgPlus).setVisibility(View.GONE);
            this.ctrlPanel.findViewById(R.id.imgMinus).setVisibility(View.GONE);
            this.ctrlPanel.findViewById(R.id.imgSwipe).setVisibility(View.GONE);
        }

        //レイアウトを追加
        wm.addView(ctrlPanel, lp);

    }

    //------------------------------------------------------
    //      スケジュール設定
    //------------------------------------------------------
    private void setSchedule(){
        schedule = Executors.newSingleThreadScheduledExecutor();

        updateText = new Runnable() {
            public void run() {

                //現在のスケジュールを消す
                mHandler.removeCallbacks(updateText);

                //onOffフラグがtrueかチェック
                if(onoff_flg) {
                    //タップアクション
                    autoTap();

                    //delay_time+15mS後に、スケジュールを設定する
                    mHandler.postDelayed(updateText, listPointView.get(loopCounter).delay_time + 15);
                }

                //1ターン終了時、先頭に戻すそうでなければ、カウントを進める
                if (loopCounter + 1 == listPointView.size()){
                    loopCounter = 0;
                }else{
                    loopCounter++;
                }
            }
        };
        //初回スケジュールを設定
        mHandler.postDelayed(updateText, 100);
    }

    /*
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
    */


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
    public void onDestroy(){
        super.onDestroy();
        //コントロールパネルの削除
        if(ctrlPanel != null) {
            wm.removeViewImmediate(ctrlPanel);
            ctrlPanel = null;
        }

        //スケジュールのシャットダウン
        if(schedule != null) {
            onoff_flg = false;
        }
        //ポインターを削除
        while(0 != listPointView.size()){
            wm.removeView(listPointView.get(listPointView.size() - 1).pointView);
            listPointView.remove(listPointView.get(listPointView.size() - 1));
        }
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
