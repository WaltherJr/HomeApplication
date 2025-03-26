package com.eriksandsten.homeautomation2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.utils.injection.CSSInjection;
import com.eriksandsten.homeautomation2.utils.injection.DOMTarget;
import com.eriksandsten.homeautomation2.utils.injection.InlineJavascript;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.eriksandsten.homeautomation2.utils.injection.JSInjection;
import com.eriksandsten.homeautomation2.utils.injection.InlineStylesheet;

public class GroceryListFragment extends BaseFragment {

    private String groceryListJavaScript;
    private String groceryListCSS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groceryListJavaScript = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "js/grocery_list.js");
        groceryListCSS = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "css/grocery_list.css");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        WebView.enableSlowWholeDocumentDraw();
        View view = inflater.inflate(R.layout.fragment_grocery_list, container, false);
        WebView webView = view.findViewById(R.id.wvGroceryList);
        HomeAutomationUtils.setupDefaultWebView(webView,
                new JSInjection(new InlineJavascript("grocery-list-script", DOMTarget.BODY, groceryListJavaScript)),
                new CSSInjection(new InlineStylesheet("grocery-list-stylesheet", groceryListCSS)),
                null, null);

        webView.post(() -> webView.loadUrl(getAssociatedActivity().getProperty("hemkop_offers_url")));
        return view;
    }
}
