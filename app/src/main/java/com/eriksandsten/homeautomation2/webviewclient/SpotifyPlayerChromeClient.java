package com.eriksandsten.homeautomation2.webviewclient;

import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;

public class SpotifyPlayerChromeClient extends WebChromeClient {
    @Override
    public void onPermissionRequest(PermissionRequest permissionRequest) {
        // https://stackoverflow.com/questions/59829808/embed-spotify-not-working-properly-at-androids-webview
        var resources = permissionRequest.getResources();
        for (var resource : resources) {
            if (PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID.equals(resource)) {
                permissionRequest.grant(resources);
                return;
            }
        }
        super.onPermissionRequest(permissionRequest);
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (consoleMessage.message().startsWith("Hello!")) {
            System.out.println("Hello!");
        }
        return false;
    }
}
