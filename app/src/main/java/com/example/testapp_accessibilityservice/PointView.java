package com.example.testapp_accessibilityservice;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

public class PointView extends View {



    /* renamed from: b  reason: collision with root package name */
    public int f12386b;

    /* renamed from: c  reason: collision with root package name */
    public int f12387c;

    /* renamed from: d  reason: collision with root package name */
    public int f12388d;

    /* renamed from: e  reason: collision with root package name */
    private int f12389e;

    /* renamed from: f  reason: collision with root package name */
    private boolean f12390f;

    /* renamed from: g  reason: collision with root package name */
    Paint f12391g;

    /* renamed from: h  reason: collision with root package name */
    Paint f12392h;
    Paint i;
    TextPaint j;


    public PointView(Context context, int i2, int i3, boolean z) {
        super(context);
        this.f12388d = i2;
        this.f12389e = i3;
        this.f12390f = z;
        f();
    }

    public PointView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.f12389e = 0;
        this.f12390f = true;
        f();
    }

    private int a() {
        return this.f12388d / 10;
    }

    private void b() {
        int[] iArr = new int[2];
        getLocationOnScreen(iArr);
        int i2 = iArr[0];
        int i3 = this.f12388d;
        this.f12386b = i2 + (i3 / 2);
        this.f12387c = iArr[1] + (i3 / 2);
    }

    private int d() {
        int i2 = this.f12388d;
        return (i2 / 2) - (i2 / 15);
    }

    private int e() {
        return (this.f12388d * 2) / 25;
    }

    private void f() {
        Paint paint = new Paint();
        this.f12392h = paint;
        paint.setStyle(Paint.Style.FILL);
        this.f12392h.setColor(Color.parseColor(this.f12390f ? "#1a008cff" : "#A0008cff"));
        Paint paint2 = new Paint(1);
        this.f12391g = paint2;
        paint2.setStyle(Paint.Style.STROKE);
        this.f12391g.setStrokeWidth((float) e());
        this.f12391g.setColor(Color.parseColor("#0074d4"));
        Paint paint3 = new Paint(1);
        this.i = paint3;
        paint3.setStyle(Paint.Style.FILL);
        this.i.setColor(Color.parseColor("#0074d4"));
        TextPaint textPaint = new TextPaint(1);
        this.j = textPaint;
        textPaint.setColor(Color.parseColor("#FF6D00"));
        this.j.setTextSize((float) g());





    }

    public WindowManager.LayoutParams e(int i2) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(i2, i2, 2032, 1288, -3);
        layoutParams.gravity = 51;

        return layoutParams;
    }


    private int g() {
        return (this.f12388d * 2) / 3;
    }

    public void c(int i2) {
        this.f12388d = i2;
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = i2;
        layoutParams.height = i2;
        setLayoutParams(layoutParams);
        f();
        invalidate();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int width = getWidth() / 2;
        float f2 = (float) width;
        float height = (float) (getHeight() / 2);
        canvas.drawCircle(f2, height, (float) d(), this.f12392h);
        canvas.drawCircle(f2, height, (float) d(), this.f12391g);
        if (this.f12390f) {
            canvas.drawCircle(f2, height, (float) a(), this.i);
        }
        int i2 = this.f12389e;
        if (i2 > 0) {
            canvas.drawText(String.valueOf(this.f12389e), (float) (width - ((int) (this.j.measureText(String.valueOf(i2)) / 2.0f))), (float) ((int) (height - ((this.j.descent() + this.j.ascent()) / 2.0f))), this.j);
        }
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i2, int i3) {
        super.onMeasure(i2, i3);
        b();
    }


}