package com.eriksandsten.homeautomation2.jscontroller;

import android.view.View;
import android.webkit.JavascriptInterface;
import com.eriksandsten.homeautomation2.fragments.spotify.SpotifyPlayerFragment;
import com.eriksandsten.homeautomation2.fragments.spotify.VideoListBrowser;
import com.eriksandsten.homeautomation2.helper.HttpHelper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import okhttp3.FormBody;

public class SpotifyPlayerJSController extends JSController {
    private final SpotifyPlayerFragment spotifyPlayerFragment;
    private View view;
    private VideoListBrowser videoListBrowser;

    public SpotifyPlayerJSController(SpotifyPlayerFragment fragment) {
        super(fragment);
        this.spotifyPlayerFragment = fragment;
    }

    public void setVideoListWebView(View view, VideoListBrowser videoListBrowser) {
        this.view = view;
        this.videoListBrowser = videoListBrowser;
    }

    @JavascriptInterface
    public String playMusicVideoByArtistAndSongName(String artistName, String songName) {
        String response = HttpHelper.performPutRequest(fragment.getAssociatedActivity().getProperty("asus_media_server_url"),
                "/music-video/artist-and-song", "{\"artistName\": \"%s\", \"songName\": \"%s\"}".formatted(artistName, songName));

        return response;
    }

    @JavascriptInterface
    public void browseSongInVideoBrowser(String artistName, String songName) {
        videoListBrowser.getWebView().post(() -> {
            videoListBrowser.getWebView().loadUrl("https://www.youtube.com/results?search_query=" + URLEncoder.encode(artistName.toLowerCase() + " - " + songName.toLowerCase(), StandardCharsets.UTF_8));
            videoListBrowser.showWindow();
            videoListBrowser.maximizeWindow();
        });

    }

    @JavascriptInterface
    public void browseArtistInVideoBrowser(String artistName) {
        videoListBrowser.getWebView().post(() -> {
            videoListBrowser.getWebView().loadUrl("https://www.youtube.com/results?search_query=" + URLEncoder.encode(artistName.toLowerCase(), StandardCharsets.UTF_8));
            videoListBrowser.showWindow();
            videoListBrowser.maximizeWindow();
        });
    }

    @JavascriptInterface
    public void playMusicVideoByURL(String url) {
        String response = HttpHelper.performPutRequest(fragment.getAssociatedActivity().getProperty("asus_media_server_url"),
                "/music-video/url", "{\"url\": \"%s\"}".formatted(url));
    }
}
