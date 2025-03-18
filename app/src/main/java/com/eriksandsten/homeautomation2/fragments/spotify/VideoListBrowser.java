package com.eriksandsten.homeautomation2.fragments.spotify;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import com.eriksandsten.homeautomation2.fragments.FragmentWindow;
import com.eriksandsten.homeautomation2.jscontroller.SpotifyPlayerJSController;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.eriksandsten.homeautomation2.utils.injection.CSSInjection;
import com.eriksandsten.homeautomation2.utils.injection.DOMTarget;
import com.eriksandsten.homeautomation2.utils.injection.JSInjection;
import com.eriksandsten.homeautomation2.utils.injection.LocalJavaScript;
import com.eriksandsten.homeautomation2.utils.injection.LocalStylesheet;
import com.eriksandsten.homeautomation2.webviewclient.VideoBrowserChromeClient;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MimeTypeUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VideoListBrowser extends FragmentWindow {
    private BaseFragment spotifyPlayerFragment;
    private String videoBrowserJavaScript;
    private String videoBrowserCSS;
    private static final float VIDEO_BROWSER_WIDTH = 450;
    private WebView videoListBrowserWebView;

    public VideoListBrowser(View view, WebView webView, SpotifyPlayerJSController spotifyPlayerJSController, BaseFragment spotifyPlayerFragment) {
        super(view);
        this.spotifyPlayerFragment = spotifyPlayerFragment;
        this.videoListBrowserWebView = createVideoListWebView(view, webView, spotifyPlayerJSController);
    }

    public WebView getWebView() {
        return videoListBrowserWebView;
    }

    private void minimizeButtonClick(Button maximizeButton, Button minimizeButton, View view) {
        final Context context = spotifyPlayerFragment.getContext();

        if (spotifyPlayerFragment.getString(R.string.minimize).equals(minimizeButton.getText())) {
            minimizeButton.setText(spotifyPlayerFragment.getString(R.string.restore));
            maximizeButton.setText(spotifyPlayerFragment.getString(R.string.maximize));
            view.findViewById(R.id.videoListFrame).setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HomeAutomationUtils.convertDpToPx(context, 50), Gravity.BOTTOM));
        } else {
            minimizeButton.setText(spotifyPlayerFragment.getString(R.string.minimize));
            maximizeButton.setText(spotifyPlayerFragment.getString(R.string.maximize));
            view.findViewById(R.id.videoListFrame).setLayoutParams(new FrameLayout.LayoutParams(HomeAutomationUtils.convertDpToPx(context, VIDEO_BROWSER_WIDTH), HomeAutomationUtils.convertDpToPx(context, VIDEO_BROWSER_WIDTH), Gravity.CENTER));
        }
    }

    private void maximizeButtonClick(Button maximizeButton, Button minimizeButton, View view) {
        final Context context = spotifyPlayerFragment.getContext();

        if (spotifyPlayerFragment.getString(R.string.maximize).equals(maximizeButton.getText())) {
            minimizeButton.setText(spotifyPlayerFragment.getString(R.string.minimize));
            maximizeButton.setText(spotifyPlayerFragment.getString(R.string.restore));
            maximizeWindow(view);
        } else {
            minimizeButton.setText(spotifyPlayerFragment.getString(R.string.minimize));
            maximizeButton.setText(spotifyPlayerFragment.getString(R.string.maximize));
            view.findViewById(R.id.videoListFrame).setLayoutParams(new FrameLayout.LayoutParams(HomeAutomationUtils.convertDpToPx(context, VIDEO_BROWSER_WIDTH), HomeAutomationUtils.convertDpToPx(context, VIDEO_BROWSER_WIDTH), Gravity.CENTER));
        }
    }

    public WebView createVideoListWebView(View view, WebView webView, SpotifyPlayerJSController spotifyPlayerJSController) {
        videoBrowserJavaScript = HomeAutomationUtils.loadAssetFileAsString(spotifyPlayerFragment.getResources().getAssets(), "js/video_browser.js");
        videoBrowserCSS = HomeAutomationUtils.loadAssetFileAsString(spotifyPlayerFragment.getResources().getAssets(), "css/grocery_list.css");
        WebView videoListWebView = view.findViewById(R.id.wvSpotifyPlayerVideoBrowser);
        Button goBackButton = view.findViewById(R.id.goBackBtn);
        Button goForwardButton = view.findViewById(R.id.goForwardBtn);
        Button minimizeButton = view.findViewById(R.id.minimizeWindowBtn);
        Button maximizeButton = view.findViewById(R.id.maximizeWindowBtn);
        Button closeButton = view.findViewById(R.id.closeWindowBtn);
        final Context context = view.getContext();

        goBackButton.setOnClickListener(v -> {
            if (videoListWebView.canGoBack()) {
                videoListWebView.goBack();
            }
        });
        goForwardButton.setOnClickListener(v -> {
            if (videoListWebView.canGoForward()) {
                videoListWebView.goForward();
            }
        });

        minimizeButton.setOnClickListener((__) -> minimizeButtonClick(maximizeButton, minimizeButton, view));
        maximizeButton.setOnClickListener((__) -> maximizeButtonClickWithTransition(maximizeButton, minimizeButton, view, webView,
                HomeAutomationUtils.convertPxToDp(context, view.getMeasuredWidth()), HomeAutomationUtils.convertPxToDp(context, view.getMeasuredHeight())));
        closeButton.setOnClickListener(v -> hideWindow(view));

        HomeAutomationUtils.setupDefaultWebView(videoListWebView,
                new JSInjection(new LocalJavaScript("video-browser-script", DOMTarget.BODY, videoBrowserJavaScript)),
                new CSSInjection(new LocalStylesheet("css/video_browser.css", videoBrowserCSS)), spotifyPlayerJSController, null, VideoListBrowser::shouldInterceptRequestCallback,
                null, new VideoBrowserChromeClient());

        return videoListWebView;
    }

    private void maximizeButtonClickWithTransition(Button maximizeButton, Button minimizeButton, View view, WebView webView, float maximizedWidth, float maximizedHeight) {
        final Context context = spotifyPlayerFragment.getContext();

        if (spotifyPlayerFragment.getString(R.string.maximize).equals(maximizeButton.getText())) {
            minimizeButton.setText(spotifyPlayerFragment.getString(R.string.minimize));
            maximizeButton.setText(spotifyPlayerFragment.getString(R.string.restore));

            // Get the current layout and its width and height
            final View videoListFrame = view.findViewById(R.id.videoListFrame);
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) videoListFrame.getLayoutParams();

            // Define the target width and height for maximizing the window (full screen example)
            final int targetWidth = HomeAutomationUtils.convertDpToPx(spotifyPlayerFragment.getContext(), maximizedWidth);  // Set this to the width you want when maximized
            final int targetHeight = HomeAutomationUtils.convertDpToPx(spotifyPlayerFragment.getContext(), maximizedHeight);  // Set this to the width you want when maximized

            // Get the current width and height of the view
            final int initialWidth = layoutParams.width;
            final int initialHeight = layoutParams.height;

            // Create a ValueAnimator for width animation
            ValueAnimator widthAnimator = ValueAnimator.ofInt(initialWidth, targetWidth);
            widthAnimator.setDuration(250); // Set the duration of the animation (500ms)

            // Create a ValueAnimator for height animation
            ValueAnimator heightAnimator = ValueAnimator.ofInt(initialHeight, targetHeight);
            heightAnimator.setDuration(250); // Set the duration of the animation (500ms)

            // Add update listeners to update the layout width and height during the animation
            widthAnimator.addUpdateListener(animation -> {
                int animatedWidth = (int) animation.getAnimatedValue();
                layoutParams.width = animatedWidth; // Update the layout width
                videoListFrame.setLayoutParams(layoutParams); // Apply the updated layout parameters
            });

            heightAnimator.addUpdateListener(animation -> {
                int animatedHeight = (int) animation.getAnimatedValue();
                layoutParams.height = animatedHeight; // Update the layout height
                videoListFrame.setLayoutParams(layoutParams); // Apply the updated layout parameters
            });

            // Use AnimatorSet to play both animations together
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(widthAnimator, heightAnimator);
            animatorSet.start(); // Start the animations

        } else {
            minimizeButton.setText(spotifyPlayerFragment.getString(R.string.minimize));
            maximizeButton.setText(spotifyPlayerFragment.getString(R.string.maximize));

            // Get the current layout and its width and height
            final View videoListFrame = view.findViewById(R.id.videoListFrame);
            final FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) videoListFrame.getLayoutParams();

            // Define the target width and height for restoring the window (smaller size)
            final int targetWidth = HomeAutomationUtils.convertDpToPx(spotifyPlayerFragment.getContext(), VIDEO_BROWSER_WIDTH); // Smaller width for minimized state
            final int targetHeight = HomeAutomationUtils.convertDpToPx(spotifyPlayerFragment.getContext(), 50); // Smaller height for minimized state

            // Get the current width and height of the view
            final int initialWidth = layoutParams.width;
            final int initialHeight = layoutParams.height;

            // Create a ValueAnimator for width animation
            ValueAnimator widthAnimator = ValueAnimator.ofInt(initialWidth, targetWidth);
            widthAnimator.setDuration(250); // Set the duration of the animation (500ms)

            // Create a ValueAnimator for height animation
            ValueAnimator heightAnimator = ValueAnimator.ofInt(initialHeight, targetHeight);
            heightAnimator.setDuration(250); // Set the duration of the animation (500ms)

            // Add update listeners to update the layout width and height during the animation
            widthAnimator.addUpdateListener(animation -> {
                int animatedWidth = (int) animation.getAnimatedValue();
                layoutParams.width = animatedWidth; // Update the layout width
                videoListFrame.setLayoutParams(layoutParams); // Apply the updated layout parameters
            });

            heightAnimator.addUpdateListener(animation -> {
                int animatedHeight = (int) animation.getAnimatedValue();
                layoutParams.height = animatedHeight; // Update the layout height
                videoListFrame.setLayoutParams(layoutParams); // Apply the updated layout parameters
            });

            // Use AnimatorSet to play both animations together
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(widthAnimator, heightAnimator);
            animatorSet.start(); // Start the animations
        }
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
