package com.eriksandsten.homeautomation2.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.eriksandsten.homeautomation2.R;

public class CustomTooltip extends LinearLayout {
    private TextView tooltipText;

    public CustomTooltip(Context context) {
        super(context);
        init(context);
    }

    public CustomTooltip(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomTooltip(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.custom_tooltip, null);

        // Find TextViews inside the custom layout
        tooltipText = layout.findViewById(R.id.tooltipText);

        // Add layout to this Button instance
        this.addView(layout);
    }

    // Set text for bottom TextView
    public void setTooltipText(String text) {
        tooltipText.setText(text);
    }
}
