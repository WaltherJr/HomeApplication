package com.eriksandsten.homeautomation2.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.fragment.app.Fragment;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.jscontroller.JSController;
import com.eriksandsten.homeautomation2.utils.injection.CSSInjection;
import com.eriksandsten.homeautomation2.utils.injection.JSInjection;
import com.eriksandsten.homeautomation2.utils.injection.Javascript;
import com.eriksandsten.homeautomation2.utils.injection.InlineStylesheet;
import com.eriksandsten.homeautomation2.webviewclient.DefaultWebChromeClient;
import com.eriksandsten.homeautomation2.webviewclient.DefaultWebViewClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.loader.FileLoader;
import io.pebbletemplates.pebble.loader.StringLoader;
import io.pebbletemplates.pebble.template.PebbleTemplate;

public final class HomeAutomationUtils {

    public static final String ATOB_UTF8_FUNCTION = """
            if (typeof atobUTF8 !== 'function') {
                function atobUTF8(data) {
                    const decodedData = atob(data);
                    const utf8data = new Uint8Array(decodedData.length);
                    const decoder = new TextDecoder("utf-8");

                    for (let i = 0; i < decodedData.length; i++) {
                        utf8data[i] = decodedData.charCodeAt(i);
                    }

                    return decoder.decode(utf8data);
                }
            }
            """;
    private static final String LOAD_CSS_IN_HEAD = """
            if (!document.getElementById('%s')) {
                var headElement = document.querySelector('head');
                var stylesheet = document.createElement('style');

                stylesheet.id = '%s';
                stylesheet.innerHTML = atobUTF8('%s');

                headElement.appendChild(stylesheet);
            }
            """;

    private static final String LOAD_JS_IN_HEAD_URL = """
                    if (!document.getElementById('%s')) {
                        var headElement = document.getElementsByTagName('head').item(0);
                        var script = document.createElement('script');

                        script.type = 'text/javascript';
                        script.id = '%s';
                        script.src = '%s';

                        headElement.appendChild(script);
                    } else {
                        console.error('Page head JS [%s] already loaded');
                    }
            """;

    private static final String LOAD_JS_IN_BODY = """
                    if (!document.getElementById('%s')) {
                        var bodyElement = document.getElementsByTagName('body').item(0);
                        var script = document.createElement('script');

                        script.type = 'text/javascript';
                        script.defer = true;
                        script.id = '%s';
                        script.innerHTML = atobUTF8('%s');

                        bodyElement.prepend(script);
                    } else {
                        console.error('Page body JS [%s] already loaded');
                    }
            """;

    private static final Pattern imageFilePattern = Pattern.compile("\\.(:gif|png|jpg)$");

    public static String getDefaultHTMLDocumentTitle(Context context, String pageTitle) {
        return String.format("%s - %s", context.getApplicationInfo().loadLabel(context.getPackageManager()), pageTitle);
    }

    public static void showMessageBox(String title, String message, Fragment fragment) {
        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(fragment.getContext().getString(R.string.ok), (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public static void setupDefaultWebView(WebView webView, JSInjection pageJavaScript, CSSInjection pageCSS, JSController jsController, BiConsumer<WebView, String> onPageFinishedCallback) {
        setupDefaultWebView(webView, pageJavaScript, pageCSS, jsController, onPageFinishedCallback, null, null, null);
    }

    public static String toJSON(Map<String, Object> values) {
        return values.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry ->
                "\"%s\": \"%s\"".formatted(entry.getKey().replaceAll("\"", "\\\\\""), entry.getValue().toString().replaceAll("\"", "\\\\\""))
        ).collect(Collectors.joining(", ", "{", "}"));
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    public static void setupDefaultWebView(WebView webView, JSInjection pageJavaScript, CSSInjection pageCSS, JSController jsController,
                                           BiConsumer<WebView, String> onPageFinishedCallback,
                                           TriFunction<WebResourceRequest, WebView, WebResourceResponse, WebResourceResponse> shouldInterceptRequestCallback,
                                           BiFunction<WebResourceRequest, WebView, Boolean> shouldOverrideUrlLoadingCallback,
                                           WebChromeClient webChromeClient) {

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        // webSettings.setAllowFileAccessFromFileURLs(true);
        // webSettings.setAllowUniversalAccessFromFileURLs(true);
/*
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(fragment.getContext()))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(fragment.getContext()))
                .build();
*/
        if (jsController != null) {
            webView.post(() -> webView.addJavascriptInterface(jsController, "javaInterface"));
        }

        webView.post(() -> webView.setWebViewClient(new DefaultWebViewClient(pageJavaScript, pageCSS, onPageFinishedCallback, shouldInterceptRequestCallback, shouldOverrideUrlLoadingCallback)));
        webView.post(() -> webView.setWebChromeClient(webChromeClient == null ? new DefaultWebChromeClient() : webChromeClient));
    }

    public static String loadAssetImagesAsString(AssetManager assetManager, String... imageFileNames) {
        StringBuffer sb = new StringBuffer();

        Arrays.stream(imageFileNames).forEach(imageFileName -> {
            String imageData = loadAssetFileAsString(assetManager, imageFileName, StandardCharsets.UTF_8);
            Matcher imageFileMatcher = imageFilePattern.matcher(imageFileName);

            if (imageFileMatcher.find()) {
                String imageFileType = imageFileMatcher.group(1);
                String cssClassName = imageFileMatcher.replaceAll("").toLowerCase();
                String base64EncodedFile = new String(android.util.Base64.encode(imageData.getBytes(), android.util.Base64.NO_WRAP), StandardCharsets.UTF_8);
                sb.append("""
                .%s {
                    background-image: url('https://upload.wikimedia.org/wikipedia/en/5/5c/Mario_by_Shigehisa_Nakaue.png');
                }""".formatted(cssClassName));

                /*
                sb.append("""
                .%s {
                    background-image: url('data:image/%s;base64,%s');
                }

                """.formatted(cssClassName, imageFileType, base64EncodedFile));*/
            }
        });

        return sb.toString();
    }

    public static int convertDpToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
    }

    public static String renderAsHTML(String template, Map<String, Object> data) throws IOException {
        PebbleEngine engine = new PebbleEngine.Builder().strictVariables(true).loader(new StringLoader()).build();
        PebbleTemplate compiledTemplate = engine.getTemplate(template);

        Map<String, Object> dataCopy = data.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Writer htmlWriter = new StringWriter();
        compiledTemplate.evaluate(htmlWriter, dataCopy);
        return htmlWriter.toString();
    }

    public static int convertPxToDp(Context context, float px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float density = metrics.density;
        return (int) (px / density);
    }

    public static String loadAssetFileAsString(AssetManager assetManager, String filename) {
        return loadAssetFileAsString(assetManager, filename, StandardCharsets.UTF_8);
    }

    public static String loadAssetFileAsString(AssetManager assetManager, String filename, Charset encoding) {
        try (InputStream is = assetManager.open(filename)) {
            return new String(is.readAllBytes(), encoding);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getApplicationPreference(Activity activity, String preferenceKey, String defaultValue) {
        return activity.getPreferences(Context.MODE_PRIVATE).getString(preferenceKey, defaultValue);
    }

    public static void setApplicationPreference(Activity activity, String preferenceKey, String value) {
        SharedPreferences.Editor editor = activity.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(preferenceKey, value);
        editor.apply();
    }

    public static void injectStylesheet(WebView webView, InlineStylesheet stylesheet) {
        String encodedStyleSheet = Base64.getEncoder().encodeToString(stylesheet.getStylesheetContent().getBytes());
        String finalCSSInHead = "javascript:(function(){" + ATOB_UTF8_FUNCTION + LOAD_CSS_IN_HEAD.formatted(stylesheet.getStylesheetName(),
                stylesheet.getStylesheetName(), encodedStyleSheet) + "})();";
        webView.post(() -> webView.evaluateJavascript(finalCSSInHead, null));
    }

    public static void injectJavaScript(WebView webView, Javascript javaScript) {
        injectJavaScript(webView, javaScript, null);
    }

    public static void injectJavaScript(WebView webView, Javascript javaScript, ValueCallback<String> resultCallback) {
        final String jsInjectionCode = "javascript:(function(){" + javaScript.getInjectionCode() + "})();";
        webView.post(() -> webView.evaluateJavascript(jsInjectionCode, resultCallback));
    }
}
