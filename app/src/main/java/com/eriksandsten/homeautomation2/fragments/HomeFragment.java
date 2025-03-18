package com.eriksandsten.homeautomation2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.main.MainActivity;
import com.eriksandsten.homeautomation2.jscontroller.HomeJSController;
import com.eriksandsten.homeautomation2.utils.injection.CSSInjection;
import com.eriksandsten.homeautomation2.utils.injection.DOMTarget;
import com.eriksandsten.homeautomation2.utils.injection.JavaScriptByURL;
import com.eriksandsten.homeautomation2.utils.injection.LocalJavaScript;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.eriksandsten.homeautomation2.utils.injection.JSInjection;
import com.eriksandsten.homeautomation2.utils.injection.LocalStylesheet;

public class HomeFragment extends BaseFragment {
    private String baseJavaScript;
    private String homeJavaScript;
    private String baseCSS;
    private String homeCSS;

    public HomeFragment() {
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        MainActivity activity = (MainActivity) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseJavaScript = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "js/base.js");
        homeJavaScript = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "js/home.js");
        baseCSS = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "css/base.css");
        homeCSS = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "css/home.css");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        WebView webView = view.findViewById(R.id.wvHome);

        HomeAutomationUtils.setupDefaultWebView(webView,
                new JSInjection(
                    new JavaScriptByURL("jquery-script", DOMTarget.BODY,
                            getAssociatedActivity().getProperty("jquery_script_file_url"),
                            getAssociatedActivity().getProperty("jquery_script_file_integrity")),
                    new LocalJavaScript("base-script", DOMTarget.BODY, baseJavaScript),
                    new LocalJavaScript("home-script", DOMTarget.BODY, homeJavaScript)
                ), new CSSInjection(
                    new LocalStylesheet("base-stylesheet", baseCSS),
                    new LocalStylesheet("home-stylesheet", homeCSS)
                ),
                new HomeJSController(this, getResources().getAssets()), null);

        webView.post(() -> webView.loadUrl("file:///android_asset/html/home.html"));

        return view;
    }
}
