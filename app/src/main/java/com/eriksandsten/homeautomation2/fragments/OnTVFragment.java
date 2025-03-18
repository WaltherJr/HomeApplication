package com.eriksandsten.homeautomation2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.jscontroller.OnTVJSController;
import com.eriksandsten.homeautomation2.utils.injection.CSSInjection;
import com.eriksandsten.homeautomation2.utils.injection.DOMTarget;
import com.eriksandsten.homeautomation2.utils.injection.LocalJavaScript;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.eriksandsten.homeautomation2.utils.injection.JSInjection;
import com.eriksandsten.homeautomation2.webviewclient.DefaultWebChromeClient;
import com.eriksandsten.homeautomation2.utils.injection.LocalStylesheet;

public class OnTVFragment extends BaseFragment {
    private String onTVJavaScript;
    private String onTVCSS;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onTVJavaScript = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "js/on_tv.js");
        onTVCSS = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "css/on_tv.css");
    }

    // TODO: use header_remove("X-Frame-Options");
    // https://stackoverflow.com/questions/12182768/x-frame-options-sameorigin-blocking-iframe-on-my-domain
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_on_tv, container, false);
        WebView webView = view.findViewById(R.id.wvOnTV);
        OnTVJSController onTVJsController = new OnTVJSController(this);

        HomeAutomationUtils.setupDefaultWebView(webView,
                new JSInjection(new LocalJavaScript("on-tv-script", DOMTarget.BODY, onTVJavaScript)),
                new CSSInjection(new LocalStylesheet("on-tv-stylesheet", onTVCSS)),
                onTVJsController, null, null, null, new DefaultWebChromeClient());
        webView.post(() -> webView.loadUrl(getAssociatedActivity().getProperty("allente_tv_guide_url")));

        return view;
    }

    /*
    var apa = (WebResourceRequest request, WebView webView, WebResourceResponse defaultResponse) -> {
        if (request.getUrl().toString().startsWith("https://www.youtube.com/results")) {
            try {

                String url = request.getUrl().toString();
                OkHttpClient httpClient = new OkHttpClient();
                Request okRequest = new Request.Builder()
                        .url(url)
                        .build();
                Response response = httpClient.newCall(okRequest).execute();
                Response modifiedResponse = response.newBuilder()
                        .removeHeader("x-frame-options")
                        .removeHeader("frame-options")
                        .build();
                return new WebResourceResponse("text/html",
                        modifiedResponse.header("content-encoding", "utf-8"),
                        modifiedResponse.body().byteStream()
                );

            } catch (final IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return defaultResponse;
        }
    }*/
}
