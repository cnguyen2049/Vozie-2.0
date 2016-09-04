package com.example.carprototype.carapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

public class MilesCircleLayout extends View {
    private final static int TOTAL_DEGREE = 360;
    private final static int START_DEGREE = -90;

    private TextView milesTextView, priceTextView;
    private Paint mPaint;
    private RectF mOvalRect = null;
    private Context mContext;

    private int mItemCount = 3;
    private int mSweepAngle;
    private float currentRotationDegree = 0;

    private int mInnerRadius;
    private int mOuterRadius;
    private int[] mColors = {getResources().getColor(R.color.red),
            getResources().getColor(R.color.lightRed),
            getResources().getColor(R.color.red)};
    private String[] mTitles = {"10", "25", "100"};

    public boolean dayMode = true;
    public int currentMilesRotation = 10;

    public MilesCircleLayout(Context context) {
        this(context, null);
    }

    public MilesCircleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MilesCircleLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.mContext = context;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(2);

        mSweepAngle = TOTAL_DEGREE / mItemCount;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        mInnerRadius = width/2 - dpToPx(25);
        mOuterRadius = width/2 - dpToPx(10);

        if (mOvalRect == null) {
            mOvalRect = new RectF(width / 2 - mOuterRadius, height / 2 - mOuterRadius, width / 2 + mOuterRadius, height / 2 + mOuterRadius);
        }

        for (int i = 0; i < mItemCount; i++) {
            int startAngle = START_DEGREE + i * mSweepAngle;
            mPaint.setColor(mColors[i]);
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawArc(mOvalRect, startAngle, mSweepAngle, true, mPaint);

            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawArc(mOvalRect, startAngle, mSweepAngle, true, mPaint);

            int centerX = (int) ((mOuterRadius + mInnerRadius) / 2 * Math.cos(Math.toRadians(startAngle + mSweepAngle / 2)));
            int centerY = (int) ((mOuterRadius + mInnerRadius) / 2 * Math.sin(Math.toRadians(startAngle + mSweepAngle / 2)));
        }

        mPaint.setColor(getResources().getColor(R.color.gray));
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(width / 2, height / 2, mInnerRadius, mPaint);
        //canvas.drawBitmap(mCenterIcon, width / 2 - mCenterIcon.getWidth() / 2, height / 2 - mCenterIcon.getHeight() / 2, null);

        super.onDraw(canvas);
    }

    public void setTextViews(TextView milesTextView, TextView priceTextView) {
        this.milesTextView = milesTextView;
        this.priceTextView = priceTextView;
    }

    public void cycleView(boolean clockwise) {
        if (currentMilesRotation == 10) {
            if (clockwise)
                changeMiles(25, clockwise);
            else
                changeMiles(100, clockwise);
        }
        else if (currentMilesRotation == 25) {
            if (clockwise)
                changeMiles(100, clockwise);
            else
                changeMiles(10, clockwise);
        }
        else if (currentMilesRotation == 100) {
            if (clockwise)
                changeMiles(10, clockwise);
            else
                changeMiles(25, clockwise);
        }
    }

    private void changeMiles(int miles, boolean clockwise) {
        switch (miles) {
            case 10:
                currentMilesRotation = 10;
                milesTextView.setText("10");
                if (dayMode)
                    priceTextView.setText("$17.50");
                else
                    priceTextView.setText("$23.10");
                rotate(clockwise);
                break;
            case 25:
                currentMilesRotation = 25;
                milesTextView.setText("25");
                if (dayMode)
                    priceTextView.setText("$42.50");
                else
                    priceTextView.setText("$56.50");
                rotate(clockwise);
                break;
            case 100:
                currentMilesRotation = 100;
                milesTextView.setText("100");
                if (dayMode)
                    priceTextView.setText("$164.00");
                else
                    priceTextView.setText("$222.00");
                rotate(clockwise);
                break;
        }
    }

    private void rotate(boolean clockwise) {
        Animation rotateAnim;
        float centerX = this.getX() + this.getWidth()  / 2;
        float centerY = this.getY() + this.getHeight() / 2;

        if (clockwise) {
            rotateAnim = new RotateAnimation(currentRotationDegree, currentRotationDegree + 120.0f, centerX, centerY);
            currentRotationDegree += 120.0f;
        }
        else {
            rotateAnim = new RotateAnimation(currentRotationDegree, currentRotationDegree - 120.0f, centerX, centerY);
            currentRotationDegree -= 120.0f;
        }

        // Set the animation's parameters
        rotateAnim.setDuration(1000);               // duration in ms
        rotateAnim.setRepeatCount(0);                // -1 = infinite repeated
        rotateAnim.setFillAfter(true);               // keep rotation after animation

        this.startAnimation(rotateAnim);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
