package com.example.carprototype.carapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MilesActivity extends AppCompatActivity {
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

        final MilesCircleLayout circleLayout = (MilesCircleLayout) findViewById(R.id.miles_circle_layout);
        FrameLayout circleFrameLayout = (FrameLayout) findViewById(R.id.circle_frame_layout);
        circleLayout.setMilesTextView((TextView) findViewById(R.id.circle_miles_text));

        circleFrameLayout.setOnClickListener(new ViewGroup.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleLayout.cycleView(true);
            }
        });
    }
}
