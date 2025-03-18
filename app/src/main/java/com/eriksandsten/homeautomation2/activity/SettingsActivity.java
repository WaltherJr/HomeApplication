package com.eriksandsten.homeautomation2.activity;

import android.os.Bundle;
import com.eriksandsten.homeautomation2.activity.main.BaseActivity;
import com.eriksandsten.homeautomation2.fragments.SettingsFragment;

public class SettingsActivity extends BaseActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
