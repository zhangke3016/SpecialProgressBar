package com.mrzk.specialprolibrary;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 *  SpecialProgressBarView
 * Created by zhangke3016 on 2016/8/8.
 */
public class SpecialProgressBarView extends View{
    //画笔
    private Paint mBgPaint, paint,mErrorPaint,mSuccessPaint;
    private Path p;
    private Bitmap downloadBitmap;
    private Bitmap loadingBitmap;

    private Paint mTextPaint;

    float[] POS = new float[2];

    float radiu = 0;
    float scaleX = 0;
    float scaleY = 0;
    private Matrix matrix;
    private String strText = "";

    float center_scaleX = 1;
    float center_scaleY = 1;
    private RectF moveBounds;
    private Camera camera;
    private Matrix cameraMatrix = new Matrix();

    int rotateX = 0;
    int rotateY = 0;//成功时Y轴旋转的值

    private PathMeasure pm;//路径辅助工具
    int offsetY = 0;//摆幅偏移量
    float progressOffsetX = 0;//进度

    private static final int STATE_READY = 0;
    private static final int STATE_READY_CHANGEING = 1;
    private static final int STATE_READYING = 2;
    private static final int STATE_ERROR = 3;
    private static final int STATE_STARTING = 4;
    private static final int STATE_SUCCESS = 5;
    private static final int STATE_BACK = 6;
    private static final int STATE_BACK_HOME = 7;
    private static final int DONE = 8;

    private int state = STATE_READY;

    private int startX = 0,startY = 0,endX = 0,endY = 0;

    private List<ValueAnimator> vas_List = new ArrayList<>();

    private int max =1;
    private int progress;

    private float pointStartX = -1f;
    float downX;//手指按下时的X坐标
    //动画结束监听  开始初始化进度设置
    private AnimationEndListener animationEndListener;

    private OntextChangeListener ontextChangeListener;

    private Path pp = new Path();

    private float textSize;//字体大小
    private float progressBarHeight;//进度条宽度
    private int textColorSuccess;//成功字体颜色
    private int textColorError;//失败字体颜色
    private int textColorNormal;//默认字体颜色
    private int startDrawable;//开始时图片
    private int progressBarBgColor;//进度条背景颜色
    private int progressBarColor;//进度条颜色
    //是否为第一次设置监听
    private boolean isFirstSetListener = false;
    //是否可以拖拽改变进度
    private boolean isCanDrag = true;
    //是否成功后返回原来效果
    private boolean isCanReBack = true;

    private int endSuccessDrawable;
    private int endSuccessBackgroundColor;
    //成功后是否可以再次点击
    private boolean isCanEndSuccessClickable = true;

    private boolean isTempCanEndSuccessClickable = true;

    private BitmapFactory.Options optionsEndSuccess = null;
    private BitmapFactory.Options optionsLoadingDrawable = null;
    //可点击区域
    private RectF rectClickRange;

    private Rect bounds;

    /**
     * 获取加载中的图片显示
     * @return
     */
    public Bitmap getLoadingBitmap(){
        return loadingBitmap;
    }
    public void setOptionsLoadingDrawable(BitmapFactory.Options optionsLoadingDrawable){
        if (optionsLoadingDrawable != null){
            this.optionsLoadingDrawable = optionsLoadingDrawable;
            loadingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_chat_bubble_white_36dp,optionsLoadingDrawable);
        }
    }
    public SpecialProgressBarView setCanEndSuccessClickable(boolean isCanEndSuccessClickable){
        this.isTempCanEndSuccessClickable = isCanEndSuccessClickable;
        return this;
    }
    public SpecialProgressBarView setCanReBack(boolean isCanReBack){
        this.isCanReBack = isCanReBack;
        return this;
    }
    /**
     * 设置当文字改变时监听
     * @param ontextChangeListener
     */
    public void setOntextChangeListener(OntextChangeListener ontextChangeListener){
        this.ontextChangeListener = ontextChangeListener;
    }

    /**
     * 是否可以拖拽
     * @param isCanDrag
     */
    public SpecialProgressBarView setCanDragChangeProgress(boolean isCanDrag){
        this.isCanDrag = isCanDrag;
        return this;
    }
    /**
     * 设置当动画结束时监听
     * @param mlistener
     */
    public void setOnAnimationEndListener(AnimationEndListener mlistener){
        this.animationEndListener = mlistener;
        isFirstSetListener = true;
    }
    public SpecialProgressBarView(Context context) {
        this(context, null);
    }
    public SpecialProgressBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public SpecialProgressBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray typedArray = context.getResources().obtainAttributes(attrs,R.styleable.SpecialProgressBarStyle);
        textSize =  typedArray.getDimension(R.styleable.SpecialProgressBarStyle_textSize, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        progressBarHeight = typedArray.getDimension(R.styleable.SpecialProgressBarStyle_progressBarHeight, dip2px(getContext(), 4));
        textColorSuccess = typedArray.getColor(R.styleable.SpecialProgressBarStyle_textColorSuccess, Color.parseColor("#66A269"));
        textColorError = typedArray.getColor(R.styleable.SpecialProgressBarStyle_textColorError, Color.parseColor("#BC5246"));
        textColorNormal = typedArray.getColor(R.styleable.SpecialProgressBarStyle_textColorNormal, Color.parseColor("#491C14"));
        startDrawable = typedArray.getResourceId(R.styleable.SpecialProgressBarStyle_startDrawable, R.drawable.ic_get_app_white_36dp);
        endSuccessDrawable = typedArray.getResourceId(R.styleable.SpecialProgressBarStyle_endSuccessDrawable, R.drawable.ic_done_white_36dp);
        progressBarBgColor = typedArray.getColor(R.styleable.SpecialProgressBarStyle_progressBarBgColor, Color.parseColor("#491C14"));
        progressBarColor = typedArray.getColor(R.styleable.SpecialProgressBarStyle_progressBarColor, Color.WHITE);
        isCanReBack = typedArray.getBoolean(R.styleable.SpecialProgressBarStyle_canReBackable, true);
        isCanDrag = typedArray.getBoolean(R.styleable.SpecialProgressBarStyle_canDragable,true);
        endSuccessBackgroundColor = progressBarBgColor;
        typedArray.recycle();
        init();
    }

    /**
     * 设置字体颜色
     * @param textSize
     */
    public SpecialProgressBarView setTextSize(float textSize) {
        this.textSize = textSize;
        mTextPaint.setTextSize(textSize);
        return this;
    }

    /**
     * 设置进度条高度
     * @param progressBarHeight
     */
    public SpecialProgressBarView setProgressBarHeight(float progressBarHeight) {
        this.progressBarHeight = progressBarHeight;
        mBgPaint.setStrokeWidth(progressBarHeight);
        paint.setStrokeWidth(progressBarHeight);
        return this;
    }

    /**
     * 设置成功时字体颜色
     * @param textColorSuccess
     */
    public SpecialProgressBarView setTextColorSuccess(int textColorSuccess) {
        this.textColorSuccess = textColorSuccess;
        mSuccessPaint.setColor(textColorSuccess);
        return this;
    }

    /**
     * 设置失败时字体颜色
     * @param textColorError
     */
    public SpecialProgressBarView setTextColorError(int textColorError) {
        this.textColorError = textColorError;
        mErrorPaint.setColor(textColorError);
        return this;
    }

    /**
     * 设置默认字体颜色
     * @param textColorNormal
     */
    public SpecialProgressBarView setTextColorNormal(int textColorNormal) {
        this.textColorNormal = textColorNormal;
        mTextPaint.setColor(textColorNormal);
        return this;
    }

    /**
     * 设置中间图片
     * @param startDrawable
     * @param options  可选  可为null
     */
    public SpecialProgressBarView setStartDrawable(int startDrawable,BitmapFactory.Options options) {
        this.startDrawable = startDrawable;
        if (options!=null){
            downloadBitmap = BitmapFactory.decodeResource(getResources(), startDrawable,options);
        }else {
            downloadBitmap = BitmapFactory.decodeResource(getResources(), startDrawable);
        }
        return this;
    }
    /**
     * 结束成功时
     * @param endSuccessDrawable
     * @param options
     * @return
     */
    public SpecialProgressBarView setEndSuccessDrawable(int endSuccessDrawable,BitmapFactory.Options options) {
        this.endSuccessDrawable = endSuccessDrawable;
        this.optionsEndSuccess = options;
        return this;
    }

    public SpecialProgressBarView setEndSuccessBackgroundColor(int endSuccessBackgroundColor){
        this.endSuccessBackgroundColor = endSuccessBackgroundColor;
        return this;
    }

    /**
     * 设置进度背景颜色
     * @param progressBarBgColor
     */
    public SpecialProgressBarView setProgressBarBgColor(int progressBarBgColor) {
        this.progressBarBgColor = progressBarBgColor;
        mBgPaint.setColor(progressBarBgColor);
        return this;
    }

    /**
     * 设置进度颜色
     * @param progressBarColor
     */
    public SpecialProgressBarView setProgressBarColor(int progressBarColor) {
        this.progressBarColor = progressBarColor;
        paint.setColor(progressBarColor);
        return this;
    }

    /**
     * 设置多种颜色
     *
     */
//    public SpecialProgressBarView setprogressBarColors(int color1,int color2){
//        LinearGradient sg = new LinearGradient(0,getHeight()/2,getWidth(),getHeight()/2,color1,color2, Shader.TileMode.REPEAT);
//        paint.setShader(sg);
//        return this;
//    }

    /**
     * 初始化数据
     */
    private void init() {
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setStrokeCap(Paint.Cap.ROUND);
//		mBgPaint.setStrokeJoin(Join.ROUND);
        mBgPaint.setColor(progressBarBgColor);
        mBgPaint.setStyle(Paint.Style.STROKE);
        mBgPaint.setStrokeWidth(progressBarHeight);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);//消除锯齿
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(progressBarColor);
        paint.setStrokeWidth(progressBarHeight);
        paint.setStrokeCap(Paint.Cap.ROUND);


        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setColor(textColorNormal);
        mTextPaint.setFakeBoldText(true);//设置字体加粗
        mTextPaint.setTextSize(textSize);
        //mBgPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.XOR));

        mErrorPaint = new Paint(mTextPaint);
        mErrorPaint.setColor(textColorError);
        mErrorPaint.setTextSize(textSize);

        mSuccessPaint = new Paint(mErrorPaint);
        mSuccessPaint.setColor(textColorSuccess);

        p = new Path();
        downloadBitmap = BitmapFactory.decodeResource(getResources(), startDrawable);
        loadingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_chat_bubble_white_36dp);

        bounds= new Rect();
        camera = new Camera();
    }

    /**
     * 初始化状态
     */
    private void initStateData(){
        center_scaleX = 1;
        center_scaleY = 1;
        rotateX = 0;
        rotateY = 0;
        radiu = 0;//半径
        scaleX = 0;
        scaleY = 0;
        strText = "";
        offsetY = 0;
        startX = getWidth()/2;
        startY = getHeight()/2;
        endX = getWidth()/2;
        endY = getHeight()/2;
        state = STATE_READY;
        progressOffsetX = 0;
        offsetY = 0;
        //progress = 0;
        pointStartX = -1f;
        downX = 0;
        moveBounds = null;
        paint.setColor(progressBarColor);
        downloadBitmap = BitmapFactory.decodeResource(getResources(), startDrawable);
        vas_List.clear();
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        if (widthSpecMode == MeasureSpec.AT_MOST
                && heightSpecMode == MeasureSpec.AT_MOST) {
            // 指定wrap_content模式（MeasureSpec.AT_MOST）的默认宽高，比如200px
            setMeasuredDimension(dip2px(getContext(),300), loadingBitmap.getHeight()*2);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(dip2px(getContext(),300), heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, loadingBitmap.getHeight()*2);
        }

        startX = getWidth()/2;
        startY = getHeight()/2;
        endX = getWidth()/2;
        endY = getHeight()/2;
        radiu = (Math.min(getWidth(), getHeight())- mBgPaint.getStrokeWidth()*2)/2;

        rectClickRange = new RectF(getWidth()/2-radiu,getHeight()/2-radiu,getWidth()/2+radiu,getHeight()/2+radiu);

         matrix = new Matrix();
        matrix.setTranslate(progressOffsetX, getHeight() / 2 - mBgPaint.getStrokeWidth() / 2-downloadBitmap.getHeight());

        if (progress == 0){
            progressOffsetX = 0;
        }else {
            progressOffsetX = (progress*(getWidth()- mBgPaint.getStrokeWidth()-loadingBitmap.getWidth())*1.0f/max);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        switch (state) {
            case STATE_BACK_HOME:
            case STATE_READY:
                p.reset();
                mBgPaint.setStyle(Paint.Style.FILL);

                p.addCircle(getWidth() / 2, getHeight() / 2, radiu, Path.Direction.CCW);
                canvas.drawPath(p, mBgPaint);
                matrix.reset();
                matrix.setScale(center_scaleX, center_scaleY);
                matrix.preTranslate(0,0);
                matrix.postTranslate(getWidth() / 2 - downloadBitmap.getWidth() / 2*Math.max(center_scaleX,center_scaleY), getHeight() / 2 - downloadBitmap.getHeight() / 2*Math.max(center_scaleX,center_scaleY));

                canvas.drawBitmap(downloadBitmap, matrix, mBgPaint);
                break;
            case STATE_READY_CHANGEING:
                p.reset();
                p.moveTo(startX, startY);
                p.lineTo(endX, endY);
                canvas.drawPath(p, mBgPaint);
                break;
            case STATE_READYING:
                p.reset();
                p.moveTo(mBgPaint.getStrokeWidth(), getHeight() / 2);//-offsetY*0.6f
                if(offsetY == 0){
                    p.quadTo(0+ mBgPaint.getStrokeWidth(), getHeight()/2, getWidth()- mBgPaint.getStrokeWidth()-loadingBitmap.getWidth(), getHeight()/2);
                }else{
                    p.quadTo(getWidth()/2, getHeight()/2 - offsetY, getWidth()- mBgPaint.getStrokeWidth() -loadingBitmap.getWidth(), getHeight()/2);//-offsetY*0.6f
                }
                canvas.drawPath(p, mBgPaint);

                matrix.reset();
                matrix.setScale(scaleX, scaleY);
                matrix.setTranslate(progressOffsetX, getHeight() / 2 - mBgPaint.getStrokeWidth() / 2 - downloadBitmap.getHeight());
                canvas.drawBitmap(loadingBitmap, matrix, mBgPaint);
                break;
            case STATE_BACK:

                matrix.reset();
                matrix.setScale(scaleX, scaleY);

                matrix.preTranslate(0, 0);
                matrix.postTranslate(POS[0], POS[1] - mBgPaint.getStrokeWidth() / 2 - downloadBitmap.getHeight()*scaleY);
                canvas.drawBitmap(loadingBitmap, matrix, mBgPaint);

                p.reset();
                p.moveTo(startX, getHeight() / 2);//-offsetY*0.6f
                p.quadTo(startX, getHeight() / 2, endX, getHeight() / 2);//-offsetY*0.6f

                canvas.drawPath(p, paint);

                break;
            case STATE_SUCCESS:
                camera.save();
                camera.rotateY(rotateY);
                camera.getMatrix(cameraMatrix);
                camera.restore();

                cameraMatrix.preTranslate(0, -loadingBitmap.getHeight() / 2);
                cameraMatrix.postTranslate(POS[0], POS[1] - loadingBitmap.getHeight() / 2);

                canvas.drawBitmap(loadingBitmap, cameraMatrix, mBgPaint);
                if (rotateY >= 330){
                    strText ="done";
                    if (ontextChangeListener!=null && ontextChangeListener.onSuccessTextChange(this,max,progress)!=null){
                        strText = ontextChangeListener.onSuccessTextChange(this,max,progress);
                    }
                    mSuccessPaint.getTextBounds(strText, 0, strText.length(), bounds);
                    canvas.drawText(strText,POS[0]+loadingBitmap.getWidth() / 2-bounds.right / 2,POS[1]- loadingBitmap.getHeight()/2 + bounds.bottom ,mSuccessPaint);
                }
                p.reset();
                p.moveTo(mBgPaint.getStrokeWidth(), getHeight() / 2);//-offsetY*0.6f
                p.quadTo(mBgPaint.getStrokeWidth(), getHeight() / 2, getWidth() - mBgPaint.getStrokeWidth() - loadingBitmap.getWidth(), getHeight() / 2);//-offsetY*0.6f

                canvas.drawPath(p, paint);
                break;
            case STATE_ERROR:
            case STATE_STARTING:
                p.reset();
                p.moveTo(mBgPaint.getStrokeWidth(), getHeight() / 2);//-offsetY*0.6f
                    if (progressOffsetX>radiu){
                        if (progressOffsetX>=(getWidth()- mBgPaint.getStrokeWidth() -loadingBitmap.getWidth())){
                            p.quadTo(getWidth()/2, getHeight()/2 - offsetY, getWidth()- mBgPaint.getStrokeWidth()-loadingBitmap.getWidth(), getHeight()/2);//-offsetY*0.6f
                        }else{
                            p.quadTo(progressOffsetX, getHeight(), getWidth()- mBgPaint.getStrokeWidth()-loadingBitmap.getWidth(), getHeight()/2);
                        }
                    }else{
                         p.quadTo(getWidth() / 2, getHeight() / 2 - offsetY, getWidth() - mBgPaint.getStrokeWidth() - loadingBitmap.getWidth(), getHeight() / 2);//-offsetY*0.6f
                    }
                canvas.drawPath(p, mBgPaint);

                pm = new PathMeasure(p,false);
                pp.reset();
                //在安卓4.4或者之前的版本，在默认开启硬件加速的情况下，
                // 更改 dst 的内容后可能绘制会出现问题，请关闭硬件加速或者给 dst 添加一个单个操作.
                pp.rLineTo(0,0);
                if (progressOffsetX>=pm.getLength()){//(getWidth()-mBgPaint.getStrokeWidth() -loadingBitmap.getWidth())
                    pm.getSegment(0, pm.getLength(), pp, true);
                    pm.getPosTan(pm.getLength(), POS, null);
                }else{
                    pm.getSegment(0, progressOffsetX, pp, true);
                    pm.getPosTan(progressOffsetX, POS, null);
                }
                canvas.drawPath(pp, paint);

                moveBounds = new RectF(POS[0],POS[1]-loadingBitmap.getHeight(),POS[0]+loadingBitmap.getWidth(),POS[1]);

               if (state == STATE_ERROR){
                   camera.save();
                   camera.rotateX(rotateX);
                   camera.getMatrix(cameraMatrix);
                   camera.restore();

                   cameraMatrix.preTranslate(-loadingBitmap.getWidth() / 2, -loadingBitmap.getHeight() / 2);
                   cameraMatrix.postTranslate(POS[0]+loadingBitmap.getWidth()/2,POS[1]+loadingBitmap.getHeight()/2);//

                   canvas.drawBitmap(loadingBitmap, cameraMatrix, mBgPaint);
               }else{
                   if (state != STATE_SUCCESS){
                       matrix.reset();
                       matrix.setScale(scaleX, scaleY);
                       matrix.setTranslate(POS[0], POS[1] - mBgPaint.getStrokeWidth() / 2 - downloadBitmap.getHeight());
                       canvas.drawBitmap(loadingBitmap, matrix, mBgPaint);
                   }
               }

                bounds.setEmpty();
                if (progressOffsetX>=pm.getLength())
                    progressOffsetX = pm.getLength();
                strText = (int)(progressOffsetX*100/pm.getLength())+"%";
                if (ontextChangeListener!=null && ontextChangeListener.onProgressTextChange(this,max,progress)!=null){
                    strText = ontextChangeListener.onProgressTextChange(this,max,progress);
                }
                mTextPaint.getTextBounds(strText, 0, strText.length(), bounds);

                if (state == STATE_ERROR){
                    moveBounds = new RectF(POS[0],POS[1],POS[0]+loadingBitmap.getWidth(),POS[1]+loadingBitmap.getHeight());
                    if (rotateX <= -100){
                        strText ="fail";
                        if (ontextChangeListener!=null && ontextChangeListener.onErrorTextChange(this, max, progress)!=null){
                            strText = ontextChangeListener.onErrorTextChange(this, max, progress);
                        }
                        mErrorPaint.getTextBounds(strText, 0, strText.length(), bounds);
                        canvas.drawText(strText,POS[0]+ mBgPaint.getStrokeWidth(),POS[1]+loadingBitmap.getHeight()*5/6,mErrorPaint);
                    }
                }else{
                    if (state!=STATE_SUCCESS)
                        canvas.drawText(strText,POS[0]+loadingBitmap.getWidth() / 2-bounds.right / 2,POS[1]-loadingBitmap.getHeight() / 2 - bounds.bottom / 2,mTextPaint);
                }

                if (progressOffsetX == pm.getLength() && state != STATE_SUCCESS){
                    changeStateSuccess();
                }
                break;
        }
    }

    public int getMax(){
        return max;
    }
    public int getProgress(){
        return progress;
    }
    public void setMax(int max){
        this.max = max;
    }
    public void setProgress(int progress){
        if (state != STATE_STARTING)
            return;
        this.progress = progress;
        if (progress == 0){
            progressOffsetX = 0;
            return;
        }
        if (max == 0){
            throw new RuntimeException("max不能为0!");
        }
        strText = progress*100/max+"%";
        progressOffsetX = (progress*(getWidth()- mBgPaint.getStrokeWidth() -loadingBitmap.getWidth())*1.0f/max);

        for (ValueAnimator va:vas_List){
            if (va.isRunning()){
                va.end();
            }
        }
        state = STATE_STARTING;
        postInvalidate();
    }

    /**
     * 进度出错
     */
    public void setError(){
        if (state == STATE_STARTING){
            for (ValueAnimator va:vas_List){
                if (va.isRunning()){
                    va.end();
//                    va.cancel();
                }
            }
            state = STATE_ERROR;
            changeStateError();
        }
    }
    /**
     * 成功状态
     */
    private void changeStateSuccess() {
        if (state != STATE_SUCCESS){
            this.progress = max;
            state = STATE_SUCCESS;
            final ValueAnimator va = ValueAnimator.ofInt(0,360);
            va.setInterpolator(new AnticipateOvershootInterpolator());
            va.setDuration(1000);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (Integer) animation.getAnimatedValue();
                    rotateY = value;
                    invalidate();
                }
            });
            va.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (isCanReBack) {
                        postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                changeStateBack();
                            }
                        }, 500);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    va.start();
                }
            }, 100);
            vas_List.add(va);//添加动画到集合中
        }
    }

    /**
     * 开始
     */
    public void beginStarting(){
        initStateData();

        if(state == STATE_READY){
            ValueAnimator va = ValueAnimator.ofInt((int)(Math.min(getWidth(), getHeight())- mBgPaint.getStrokeWidth()*2)/2,(int) mBgPaint.getStrokeWidth());
            va.setInterpolator(new AnticipateInterpolator());
            va.setDuration(800);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (Integer) animation.getAnimatedValue();
                    radiu = value;
                    center_scaleX = (1 - animation.getAnimatedFraction());
                    center_scaleY = (1 - animation.getAnimatedFraction());
                    invalidate();
                }
            });
            va.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }
                @Override
                public void onAnimationEnd(Animator animation) {
                    state = STATE_READY_CHANGEING;//准备阶段
                    mBgPaint.setStyle(Paint.Style.STROKE);
                    mBgPaint.setColor(Color.BLACK);
                    changeStateReadyChanging();
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                }
                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            va.start();
            vas_List.add(va);//添加动画到集合中
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isCanEndSuccessClickable){
            return false;
        }
        for (ValueAnimator va:vas_List){
            if (va.isRunning()){
                return false;
            }
        }
        if (state == STATE_READY || state == DONE){
            if (rectClickRange.contains(event.getX(),event.getY())){
                state = STATE_READY;
                for (ValueAnimator va:vas_List){
                    va.cancel();
                }
                beginStarting();
            }
            return false;
        }
        if (state != STATE_STARTING || isFirstSetListener || !isCanDrag){
            return false;
        }
        //downX = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                 downX = (int) event.getX();
                float downY = (int) event.getY();
                if (moveBounds.contains(downX,downY)){
                    pointStartX = downX;
                }else{
                    pointStartX = -1f;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pointStartX!=-1){
                    float moveX = (int) event.getX();
                    progressOffsetX = moveX;

                    if (progressOffsetX>=(getWidth()- mBgPaint.getStrokeWidth() -loadingBitmap.getWidth())){
                        progressOffsetX = (getWidth()- mBgPaint.getStrokeWidth() -loadingBitmap.getWidth());
                    }
                    if (progressOffsetX<0)
                        progressOffsetX = 0;

                    progress = (int)(POS[0]*max*1.0f/(getWidth()- mBgPaint.getStrokeWidth() -loadingBitmap.getWidth()));

                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                pointStartX = -1f;
                break;
        }
        return true;
    }
    /**
     * 成功后返回动画
     */
    private void changeStateBack() {
        state = STATE_BACK;

        ValueAnimator va = ValueAnimator.ofInt(getWidth()/2-(int) mBgPaint.getStrokeWidth(),(int) mBgPaint.getStrokeWidth());
        va.setInterpolator(new AccelerateInterpolator());
        va.setDuration(300);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                startX = getWidth() / 2 + value;
                endX = getWidth() / 2 - value;
                POS[0] = startX;
                scaleX = 1 - animation.getAnimatedFraction();
                scaleY = 1 - animation.getAnimatedFraction();
                invalidate();
            }
        });
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                changeStateBackHome();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        va.start();
        vas_List.add(va);//添加动画到集合中
    }

    public void changeStateError(){
        // rotateX
        final ValueAnimator va = ValueAnimator.ofInt(0,-180);
        va.setInterpolator(new DampingInterpolator());
        va.setDuration(300);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                rotateX = value;
                invalidate();
            }
        });
        va.start();
        vas_List.add(va);//添加动画到集合中
    }
    private void changeStateBackHome(){
        state = STATE_BACK_HOME;
        if (optionsEndSuccess!=null){
            downloadBitmap = BitmapFactory.decodeResource(getResources(), endSuccessDrawable,optionsEndSuccess);
        }else{
            downloadBitmap = BitmapFactory.decodeResource(getResources(), endSuccessDrawable);
        }

        mBgPaint.setColor(endSuccessBackgroundColor);

        ValueAnimator va = ValueAnimator.ofInt((int) mBgPaint.getStrokeWidth(),(int)(Math.min(getWidth(), getHeight())- mBgPaint.getStrokeWidth()*2)/2);
        va.setInterpolator(new AnticipateOvershootInterpolator());
        va.setDuration(800);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                radiu = value;
                center_scaleX = animation.getAnimatedFraction();
                center_scaleY = animation.getAnimatedFraction();

                invalidate();
            }
        });
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                //将设置的临时变量设置回去
                isCanEndSuccessClickable = isTempCanEndSuccessClickable;
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        state = DONE;
                    }
                }, 50);
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        va.start();
        vas_List.add(va);//添加动画到集合中
    }

    private void changeStateReadyChanging() {
        ValueAnimator va = ValueAnimator.ofInt((int) mBgPaint.getStrokeWidth(),getWidth()/2-(int) mBgPaint.getStrokeWidth());
        va.setInterpolator(new LinearInterpolator());
        va.setDuration(200);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (Integer) animation.getAnimatedValue();
                startX = getWidth() / 2 - value;
                endX = getWidth() / 2 + value;
                invalidate();
            }
        });
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                changeStateStart();
            }
            @Override
            public void onAnimationCancel(Animator animation) {
            }
            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        va.start();
        vas_List.add(va);//添加动画到集合中
    }
    private void changeStateStart() {
        state = STATE_READYING;

        offsetY = 0;
        invalidate();

        ValueAnimator va = ValueAnimator.ofInt(-getHeight()/2,0,getHeight()/2,0);
        va.setInterpolator(new DampingInterpolator(2, 0.3f));
        va.setDuration(1000);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                offsetY = (Integer) animation.getAnimatedValue();
                scaleX = animation.getAnimatedFraction();
                scaleY = animation.getAnimatedFraction();

                if (animation.getAnimatedFraction() >= 1.0f) {
                    state = STATE_STARTING;
                }
                invalidate();
            }
        });
        va.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (animationEndListener != null) {
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            animationEndListener.onAnimationEnd();
                            strText = progress*100/max+"%";
                            isFirstSetListener = false;
                        }
                    }, 200);
                } else {
                    isFirstSetListener = false;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        va.start();
        vas_List.add(va);//添加动画到集合中
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        for (ValueAnimator va:vas_List){
            va.cancel();
        }
        vas_List.clear();
        getHandler().removeCallbacksAndMessages(null);
    }

    public interface OntextChangeListener{
        //进度变化时调用
        String onProgressTextChange(SpecialProgressBarView specialProgressBarView, int max, int progress);
        //失败
        String onErrorTextChange(SpecialProgressBarView specialProgressBarView, int max, int progress);
       //成功
        String onSuccessTextChange(SpecialProgressBarView specialProgressBarView, int max, int progress);
    }
    /**
     * 动画结束监听
     */
    public interface AnimationEndListener{
       void onAnimationEnd();
    }
    public int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}
