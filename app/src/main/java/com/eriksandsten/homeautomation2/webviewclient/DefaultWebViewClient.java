package com.eriksandsten.homeautomation2.webviewclient;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.eriksandsten.homeautomation2.utils.TriFunction;
import com.eriksandsten.homeautomation2.utils.injection.CSSInjection;
import com.eriksandsten.homeautomation2.utils.injection.JSInjection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class DefaultWebViewClient extends WebViewClient {
    private final JSInjection pageJavaScript;
    private final CSSInjection pageCSS;
    private final BiConsumer<WebView, String> onPageFinishedCallback;
    private final TriFunction<WebResourceRequest, WebView, WebResourceResponse, WebResourceResponse> shouldInterceptRequestCallback;
    private final BiFunction<WebResourceRequest, WebView, Boolean> shouldOverrideUrlLoadingCallback;

    public DefaultWebViewClient(JSInjection pageJavaScript, CSSInjection pageCSS, BiConsumer<WebView, String> onPageFinishedCallback,
                                TriFunction<WebResourceRequest, WebView, WebResourceResponse, WebResourceResponse> shouldInterceptRequestCallback,
                                BiFunction<WebResourceRequest, WebView, Boolean> shouldOverrideUrlLoadingCallback) {

        this.pageJavaScript = pageJavaScript;
        this.pageCSS = pageCSS;
        this.onPageFinishedCallback = onPageFinishedCallback;
        this.shouldInterceptRequestCallback = shouldInterceptRequestCallback;
        this.shouldOverrideUrlLoadingCallback = shouldOverrideUrlLoadingCallback;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView webView, WebResourceRequest request) {
        WebResourceResponse defaultResponse = super.shouldInterceptRequest(webView, request);

        if (shouldInterceptRequestCallback != null) {
            return shouldInterceptRequestCallback.apply(request, webView, defaultResponse);
        } else {
            return defaultResponse;
        }
                /*
                var response = super.shouldInterceptRequest(webView, request);
                if (request.getUrl().toString().equals("https://epg-events.s3-eu-west-1.amazonaws.com/allente/se/index.html")) {
                    if (response == null) {
                        response = new WebResourceResponse(null, null, null);
                    }
                    if (response.getResponseHeaders() == null) {
                        response.setResponseHeaders(new HashMap<>());
                    }
                    response.getResponseHeaders().put("Access-Control-Allow-Origin", "*");
                }
                return response;
                */
    }
/*
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest webResourceRequest) {
        return shouldOverrideUrlLoadingCallback != null ? shouldOverrideUrlLoadingCallback.apply(webResourceRequest, view) : false;
        // System.out.println("when you click on any interlink on webview that time you got url :-" + url);
        // return super.shouldOverrideUrlLoading(view, url);
    }
*/
    @Override
    public void onPageFinished(WebView webView, String url) {
        super.onPageFinished(webView, url);

        injectPageStylesheets(webView);
        injectPageJavaScripts(webView);
        Optional.ofNullable(onPageFinishedCallback).ifPresent(consumer -> consumer.accept(webView, url));
    }

    private void injectPageStylesheets(WebView webView) {
        if (pageCSS != null) {
            Stream.of(pageCSS.getStylesheets()).forEach(stylesheet -> HomeAutomationUtils.injectStylesheet(webView, stylesheet));
        }
    }

    private void injectPageJavaScripts(WebView webView) {
        if (pageJavaScript != null) {
            /* if (jQueryJavaScript == null) {
                jQueryJavaScript = new JSInjection(new JavaScriptByURL("jquery-script", "https://code.jquery.com/jquery-3.7.1.min.js"));
            }

            HomeAutomationUtils.injectJavaScript(webView, jQueryJavaScript.getScripts()[0]);
            */
            Stream.of(pageJavaScript.getScripts()).forEach(script -> HomeAutomationUtils.injectJavaScript(webView, script));

            /*
            HomeAutomationUtils.injectJavaScript(webView, jQueryJavaScript.getScripts().get(0), (callback) -> {
                pageJavaScript.getScripts().forEach(script -> HomeAutomationUtils.injectJavaScript(webView, script));
            });
            */
        }
    }
}
