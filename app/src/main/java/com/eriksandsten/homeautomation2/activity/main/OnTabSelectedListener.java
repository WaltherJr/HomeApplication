package com.eriksandsten.homeautomation2.activity.main;

import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;

public class OnTabSelectedListener implements TabLayout.OnTabSelectedListener {
    private final ViewPager2 viewPager;

    public OnTabSelectedListener(ViewPager2 viewPager) {
        this.viewPager = viewPager;
    }
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }
}
