package com.asha.md360player4android;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by hzqiujiadi on 2017/4/21.
 * hzqiujiadi ashqalcn@gmail.com
 */

public class HoverView extends View {

    private Paint paint;
    private float x,y,radius;
    int times=0;
    private boolean foucus=true;//是否可以正常聚焦
    Bitmap bmp_back;

    public void setFoucus(boolean foucus) {
        this.foucus = foucus;
    }

    public HoverView(Context context) {
        super(context);
        initView();
    }

    public HoverView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public HoverView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HoverView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        paint = new Paint();
        paint.setColor(0xFF0000FF);
//        bmp_back = ImageLoader.getInstance().loadImageSync("assets://back.png");
    }

    @Override
    public boolean onHoverEvent(MotionEvent event) {
        super.onHoverEvent(event);

        final int action = event.getActionMasked();
        switch (action){
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_HOVER_MOVE:
                   //1.foucus 标识焦点
                   //2.times  防止重复更新】
                   //3.判定聚焦时间
                    if (foucus&&times==0&&VideoPlayerActivity.time>3000){
                        foucus=false;
                        x = event.getX();
                        y = event.getY();
                        radius = (event.getEventTime() - event.getDownTime()) / 100 + 1;
                        times=times+1;//在这里可以发送实时进度到外部
                    }
                break;

            case MotionEvent.ACTION_HOVER_EXIT:
                  times=0;
                VideoPlayerActivity.time=0;
//                  radius = 0;
                break;
        }
        return true;
    }



    @Override protected void onDraw(Canvas canvas) {
        if (radius != 0){
//            canvas.drawBitmap(bmp_back,0,0,paint);//
//            canvas.drawCircle(x, y, radius, paint);
            canvas.drawRect(0,0,x,300,paint);
            Log.i("canvas","x:"+x+",y:"+y+",width:"+this.getWidth()+",height:"+getHeight());
            Log.i("canvas","进度值:"+(x/3)+" %");
        }else{

        }


    }
}
