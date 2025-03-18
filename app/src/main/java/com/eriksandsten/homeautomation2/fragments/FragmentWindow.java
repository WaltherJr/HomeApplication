package com.eriksandsten.homeautomation2.fragments;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.eriksandsten.homeautomation2.R;

public abstract class FragmentWindow {
    private final View view;

    public FragmentWindow(View view) {
        this.view = view;
    }

    public void maximizeWindow(View view) {
        view.findViewById(R.id.videoListFrame).setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
    }

    public void hideWindow(View view) {
        view.findViewById(R.id.videoListFrame).setVisibility(View.INVISIBLE);
    }

    public void showWindow(View view) {
        view.findViewById(R.id.videoListFrame).setVisibility(View.VISIBLE);
    }
}
