package com.eriksandsten.homeautomation2.fragments;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.eriksandsten.homeautomation2.fragments.spotify.SpotifyPlayerFragment;

public class MainViewPagerAdapter extends FragmentStateAdapter {
    public MainViewPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new HomeFragment();
            case 1 -> new LaundryBookingFragment();
            case 2 -> new GroceryListFragment();
            case 3 -> new OnTVFragment();
            case 4 -> new SpotifyPlayerFragment();
            default -> new HomeFragment();
        };
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}