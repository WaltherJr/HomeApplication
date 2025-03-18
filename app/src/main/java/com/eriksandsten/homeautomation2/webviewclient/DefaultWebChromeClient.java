package com.eriksandsten.homeautomation2.webviewclient;

import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

public class DefaultWebChromeClient extends WebChromeClient {
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        System.out.println("test:" + consoleMessage.message());
        return false;
    }
}
