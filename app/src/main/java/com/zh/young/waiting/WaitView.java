package com.zh.young.waiting;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.PathInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ViewAnimator;

import static android.animation.ValueAnimator.RESTART;
import static android.view.animation.Animation.INFINITE;
import static android.view.animation.Animation.RELATIVE_TO_SELF;


public class WaitView extends View implements ValueAnimator.AnimatorUpdateListener {

    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mCenterY;
    private int mRadius;
    private RectF mRectF;
    private Paint mPaint;
    private String TAG = "WaitView";
    private Path mPath;
    private ValueAnimator mAnimator;
    private PathMeasure mPathMeasure;
    private float mCircleParcent;
    private Path mDestPath;
    private int startAngle;
    private int minAngle;
    private int sweepAngle = 20;
    private int curAngle;
    private long mStartTime;
    private int mCount;
    private Path mSucceedPath;
    private boolean drawCircleIsFinished;
    private boolean drawSignal;
    private int mViewWidth;
    private int mViewHeight;

    public WaitView(Context context) {
        this(context, null);
    }

    public WaitView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaitView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAnimation();
        mStartTime = System.currentTimeMillis();
        setBackgroundColor(Color.GRAY);

    }


    private void initPath() {
        mPath = new Path();
        mPath.addCircle(mCenterX, mCenterX, mRadius - 40, Path.Direction.CW);
        mPathMeasure = new PathMeasure(mPath, false);
        mDestPath = new Path();
        mDestPath.lineTo(0, 0);
        mSucceedPath = new Path();
        mSucceedPath.moveTo(mCenterX - 80, mCenterY - 30);
        mSucceedPath.lineTo(mCenterX, mCenterY + 30);
        mSucceedPath.lineTo(mCenterX + 80, mCenterY - 50);


    }

    private void initAnimation() {
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(1000);
        mAnimator.addUpdateListener(this);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long curTime = System.currentTimeMillis();

        if (curTime - mStartTime >= 3000) {

            if (mCount < 1) {
                mAnimator.start();
                mCount++;
            }
            mPathMeasure.getSegment(0, mCircleParcent * mPathMeasure.getLength(), mDestPath, true);
            canvas.drawPath(mDestPath, mPaint);
        } else {
            canvas.translate(getPaddingLeft(), getPaddingTop());
            if (startAngle == minAngle) {
                sweepAngle += 6;
            }
            if (sweepAngle > 300 || startAngle > minAngle) {
                startAngle += 6;
                if (sweepAngle > 20) {
                    sweepAngle -= 6;
                }
            }

            if (startAngle > minAngle + 300) {
                startAngle %= 360;
                minAngle = startAngle;
                sweepAngle = 20;
            }

            canvas.rotate(curAngle += 6, mCenterX, mCenterY);
            canvas.drawArc(mRectF, startAngle, sweepAngle, false, mPaint);

        }
        if (mCircleParcent == 1) {
            if (drawCircleIsFinished && mCount == 1)
                drawSignal = true;
            drawCircleIsFinished = true;
            mAnimator.start();
            mPathMeasure.setPath(mSucceedPath, false);
            // mPathMeasure.nextContour();
            mCount = 0;
            if (drawSignal)
                return;
        }

        invalidate();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasure(widthMeasureSpec), widthMeasure(widthMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();

        mViewWidth = mWidth + getPaddingLeft() + getPaddingRight();
        mViewHeight = mHeight + getPaddingTop() + getPaddingBottom();
        initData();
        initPath();
    }

    /**
     * 初始化圆心，半径，画笔
     */
    private void initData() {
        mCenterX = mWidth / 2;
        mCenterY = mHeight / 2;
        mRadius = mWidth / 2;
        mPaint = createPaint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(20);
        mPaint.setColor(Color.BLUE);
        mRectF = new RectF(mCenterX - mRadius + mPaint.getStrokeWidth(), mCenterX - mRadius + mPaint.getStrokeWidth(), mCenterX + mRadius - mPaint.getStrokeWidth(), mCenterX + mRadius - mPaint.getStrokeWidth());
    }

    private Paint createPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);

        return paint;
    }

    private int widthMeasure(int widthMeasureSpec) {
        int size = MeasureSpec.getSize(widthMeasureSpec);
        int mode = MeasureSpec.getMode(widthMeasureSpec);
        switch (mode) {
            case MeasureSpec.AT_MOST:
                return Math.min(mViewWidth,mViewHeight);
            default:
                return size;
        }

    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        mCircleParcent = (float) animation.getAnimatedValue();
    }
}
