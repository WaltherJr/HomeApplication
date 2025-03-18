package com.eriksandsten.homeautomation2.fragments;

import androidx.fragment.app.Fragment;
import com.eriksandsten.homeautomation2.activity.main.BaseActivity;

public abstract class BaseFragment extends Fragment {
    public BaseActivity getAssociatedActivity() {
        return (BaseActivity) super.getActivity();
    }
}
