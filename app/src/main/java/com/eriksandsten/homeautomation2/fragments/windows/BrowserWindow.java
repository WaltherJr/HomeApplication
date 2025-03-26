package com.eriksandsten.homeautomation2.fragments.windows;

import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import lombok.Getter;

@Getter
public class BrowserWindow extends FragmentWindow implements BackAndForwardOperations {
    private final WebView webView;
    private final Button backButton;
    private final Button forwardButton;

    public BrowserWindow(View mainView, WebView view, int windowId, BaseFragment fragment, int windowWidth, int windowHeight, int minimizedHeight) {
        super(mainView, windowId, fragment, windowWidth, windowHeight, minimizedHeight);

        this.webView = view;
        this.backButton = initButton(R.id.goBackBtn, fragment.getString(R.string.back), (__) -> goBack());
        this.forwardButton = initButton(R.id.goForwardBtn, fragment.getString(R.string.forward), (__) -> goForward());
    }

    @Override
    public void goBack() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    @Override
    public void goForward() {
        if (webView.canGoForward()) {
            webView.goForward();
        }
    }
}
