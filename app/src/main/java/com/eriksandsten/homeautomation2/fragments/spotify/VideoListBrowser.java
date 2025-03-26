package com.eriksandsten.homeautomation2.fragments.spotify;

import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import com.eriksandsten.homeautomation2.fragments.windows.BrowserWindow;
import com.eriksandsten.homeautomation2.jscontroller.SpotifyPlayerJSController;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.eriksandsten.homeautomation2.utils.injection.CSSInjection;
import com.eriksandsten.homeautomation2.utils.injection.DOMTarget;
import com.eriksandsten.homeautomation2.utils.injection.JSInjection;
import com.eriksandsten.homeautomation2.utils.injection.InlineJavascript;
import com.eriksandsten.homeautomation2.utils.injection.InlineStylesheet;
import com.eriksandsten.homeautomation2.webviewclient.VideoBrowserChromeClient;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MimeTypeUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VideoListBrowser extends BrowserWindow {
    private BaseFragment spotifyPlayerFragment;
    private String videoBrowserJavaScript;
    private String videoBrowserCSS;

    public VideoListBrowser(View spotifyPlayerView, WebView webView, SpotifyPlayerJSController spotifyPlayerJSController, BaseFragment spotifyPlayerFragment) {
        super(spotifyPlayerView, webView, R.id.videoListBrowserWindow, spotifyPlayerFragment, 400, 600, 50);
        this.spotifyPlayerFragment = spotifyPlayerFragment;
        createWebView(spotifyPlayerView, spotifyPlayerJSController);
    }

    public WebView createWebView(View spotifyPlayerView, SpotifyPlayerJSController spotifyPlayerJSController) {
        videoBrowserJavaScript = HomeAutomationUtils.loadAssetFileAsString(spotifyPlayerFragment.getResources().getAssets(), "js/video_browser.js");
        videoBrowserCSS = HomeAutomationUtils.loadAssetFileAsString(spotifyPlayerFragment.getResources().getAssets(), "css/grocery_list.css");
        WebView webView = spotifyPlayerView.findViewById(R.id.wvSpotifyPlayerVideoBrowser);

        HomeAutomationUtils.setupDefaultWebView(webView,
                new JSInjection(new InlineJavascript("video-browser-script", DOMTarget.BODY, videoBrowserJavaScript)),
                new CSSInjection(new InlineStylesheet("css/video_browser.css", videoBrowserCSS)), spotifyPlayerJSController, null, VideoListBrowser::shouldInterceptRequestCallback,
                null, new VideoBrowserChromeClient());

        return webView;
    }

    public static WebResourceResponse shouldInterceptRequestCallback(WebResourceRequest request, WebView webView, WebResourceResponse defaultResponse) {
        if (request.getRequestHeaders() != null && request.getRequestHeaders().get("Accept").startsWith(MimeTypeUtils.TEXT_HTML_VALUE)) {
            try {
                String url = request.getUrl().toString();
                OkHttpClient httpClient = new OkHttpClient();
                Request okRequest = new Request.Builder()
                        .url(url)
                        .build();
                Response response = httpClient.newCall(okRequest).execute();
                Response modifiedResponse = response.newBuilder()
                        .removeHeader("Content-Security-Policy")
                        .build();
                return new WebResourceResponse(MimeTypeUtils.TEXT_HTML_VALUE,
                        modifiedResponse.header(HttpHeaders.CONTENT_ENCODING, StandardCharsets.UTF_8.name()),
                        modifiedResponse.body().byteStream()
                );
            } catch (final IOException e) {
                e.printStackTrace();
                return defaultResponse;
            }
        } else {
            return defaultResponse;
        }
    }
}
