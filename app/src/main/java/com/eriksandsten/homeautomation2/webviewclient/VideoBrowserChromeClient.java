package com.eriksandsten.homeautomation2.webviewclient;

import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

public class VideoBrowserChromeClient extends WebChromeClient {
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        // Log JavaScript console messages (errors, warnings, etc.)
        return true;
    }
}
