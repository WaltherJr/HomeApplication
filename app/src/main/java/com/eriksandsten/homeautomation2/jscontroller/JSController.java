package com.eriksandsten.homeautomation2.jscontroller;

import android.webkit.JavascriptInterface;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;

public abstract class JSController {
    protected BaseFragment fragment;

    public JSController(BaseFragment fragment) {
        this.fragment = fragment;
    }

    @JavascriptInterface
    public void showMessage(String title, String message) {
        HomeAutomationUtils.showMessageBox(title, message, fragment);
    }
}
