package com.example.carprototype.carapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class MilesActivity extends AppCompatActivity {
    private MilesCircleLayout circleLayout;
    private ImageView dayNightImg;
    private TextView priceText;
    private float mLastTouchX, mLastTouchY;
    private boolean dayMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MyAppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miles);

        // Initialize menu bar and navigation drawer
        MenuBarHandler menuBarHandler = new MenuBarHandler(this);
        menuBarHandler.init();

        // Initialize circle layout
        priceText = (TextView) findViewById(R.id.price_text);
        circleLayout = (MilesCircleLayout) findViewById(R.id.miles_circle_layout);
        FrameLayout circleFrameLayout = (FrameLayout) findViewById(R.id.circle_frame_layout);
        circleLayout.setTextViews((TextView) findViewById(R.id.circle_miles_text), priceText);

        circleFrameLayout.setOnClickListener(new ViewGroup.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleLayout.cycleView(true);
            }
        });

        // Initialize rotation arrows
        ImageView back = (ImageView) findViewById(R.id.back_button);
        ImageView next = (ImageView) findViewById(R.id.next_button);

        back.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleLayout.cycleView(false);
            }
        });

        next.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleLayout.cycleView(true);
            }
        });

        // Initialize day/night button
        dayNightImg = (ImageView) findViewById(R.id.day_night_image);
        dayNightImg.setOnClickListener(new ImageView.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dayMode)
                    setDay(false);
                else
                    setDay(true);
            }
        });
    }

    public void setDay(boolean day) {
        dayMode = day;
        circleLayout.dayMode = day;
        int currentRot = circleLayout.currentMilesRotation;

        if (day) {
            dayNightImg.setImageDrawable(getResources().getDrawable(R.drawable.sun));

            if (currentRot == 10)
                priceText.setText("$17.50");
            else if (currentRot == 25)
                priceText.setText("$42.50");
            else if (currentRot == 100)
                priceText.setText("$164.00");
        }
        else {
            dayNightImg.setImageDrawable(getResources().getDrawable(R.drawable.moon));

            if (currentRot == 10)
                priceText.setText("$23.10");
            else if (currentRot == 25)
                priceText.setText("$56.50");
            else if (currentRot == 100)
                priceText.setText("$222.00");
        }
    }
}
