package com.eriksandsten.homeautomation2.fragments.spotify;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.main.BaseActivity;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import com.eriksandsten.homeautomation2.helper.SpotifyPlayerHelper;
import com.eriksandsten.homeautomation2.jscontroller.SpotifyPlayerJSController;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.eriksandsten.homeautomation2.utils.injection.DOMTarget;
import com.eriksandsten.homeautomation2.utils.injection.JSInjection;
import com.eriksandsten.homeautomation2.utils.injection.InlineJavascript;
import com.eriksandsten.homeautomation2.webviewclient.SpotifyPlayerChromeClient;
import org.springframework.http.HttpHeaders;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SpotifyPlayerFragment extends BaseFragment {
    private String spotifyPlayerJavaScript;
    private SpotifyPlayerJSController spotifyPlayerJSController;
    private final Pattern contextMenuAppendHandle = Pattern.compile("\\(t=n\\)\\.appendChild\\(e\\)");
    private VideoListBrowser videoListBrowser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spotifyPlayerJSController = new SpotifyPlayerJSController(this);
        spotifyPlayerJavaScript = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "js/spotify_player.js");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BaseActivity activity = (BaseActivity) getActivity();
        WebView.enableSlowWholeDocumentDraw();
        View view = inflater.inflate(R.layout.fragment_spotify_player, container, false);
        WebView webView = view.findViewById(R.id.wvSpotifyPlayer);

        HomeAutomationUtils.setupDefaultWebView(webView,
                new JSInjection(new InlineJavascript("spotify-player-script", DOMTarget.BODY, spotifyPlayerJavaScript)), null,
                spotifyPlayerJSController, this::onPageFinishedCallback, this::shouldInterceptRequestCallback, null, new SpotifyPlayerChromeClient());

        videoListBrowser = new VideoListBrowser(view, webView, spotifyPlayerJSController, this);
        spotifyPlayerJSController.setVideoListWebView(view, videoListBrowser);
        webView.post(() -> webView.loadUrl(activity.getProperty("spotify_player_start_url")));
        videoListBrowser.getWebView().post(() -> videoListBrowser.getWebView().loadUrl("https://www.youtube.com/results?search_query=backstreet+boys+i+want+it+that+way"));

        return view;
    }

    private void onPageFinishedCallback(WebView webView, String url) {
        webView.post(() -> webView.evaluateJavascript("javascript:(function(){" + """
                const targetNode = document.body;
                const config = { attributes: false, childList: true, subtree: false };
                const observer = new MutationObserver((mutationList, observer) => {
                  for (const mutation of mutationList) {
                    if (mutation.type === 'childList') {
                      for (const addedNode of mutation.addedNodes) {
                        var parentMenu = addedNode.querySelector('div.TReaGMNscQs7kVELeT__');
                        if (parentMenu) {
                            const closeButton = parentMenu.nextSibling;
                            var subMenu = addedNode.querySelector('div.MlWxT7rKXrHQxSRJJmLg');
                            var artistName = addedNode.querySelector('.HzKyzW43C0TNcAFtjuMz span:last-of-type').innerHTML;
                            var songName = addedNode.querySelector('.HzKyzW43C0TNcAFtjuMz span:first-of-type').innerHTML;

                            if (closeButton.innerHTML !== 'Stäng') {
                                alert('Close button does not have "Stäng" text!');
                            }
                            addedNode.querySelector('[data-action-id="play-music-video"]').addEventListener('click', () => { javaInterface.playMusicVideoByArtistAndSongName(artistName, songName); closeButton.click(); });
                            addedNode.querySelector('[data-action-id="browse-song-youtube"]').addEventListener('click', () => { javaInterface.browseSongInVideoBrowser(artistName, songName); closeButton.click(); });
                            addedNode.querySelector('[data-action-id="browse-artist-youtube"]').addEventListener('click', () => { javaInterface.browseArtistInVideoBrowser(artistName); closeButton.click(); });
                        }
                      }
                    }
                  }
                });

                observer.observe(targetNode, config);
                // observer.disconnect();
            """ + "})()", null));
    }

    private WebResourceResponse shouldInterceptRequestCallback(WebResourceRequest request, WebView webView, WebResourceResponse defaultResponse) {
        if (request.getUrl().toString().startsWith(getAssociatedActivity().getProperty("spotify_player_main_js_url_prefix"))) {
            try {
                String url = request.getUrl().toString();
                OkHttpClient httpClient = new OkHttpClient();
                Request okRequest = new Request.Builder()
                        .url(url)
                        .build();
                Response response = httpClient.newCall(okRequest).execute();
                String originalResponseBody = response.body().string();

                Matcher m = contextMenuAppendHandle.matcher(originalResponseBody);
                String finalResponseJavascript = originalResponseBody;

                if (m.find()) {
                    finalResponseJavascript = SpotifyPlayerHelper.addContextMenuItems(finalResponseJavascript,
                            new SpotifyPlayerHelper.MenuItem("play-music-video", getString(R.string.play_music_video)),
                            new SpotifyPlayerHelper.MenuItem("browse-song-youtube", getString(R.string.browse_song_on_youtube)),
                            new SpotifyPlayerHelper.MenuItem("browse-artist-youtube", getString(R.string.browse_artist_on_youtube)));
                    Log.d("HomeAutomation2", "Replaced context menu JS snippet");
                } else {
                    Log.d("HomeAutomation2", "Could not find context menu append JS snippet");
                }
                Response modifiedResponse = response.newBuilder().body(ResponseBody.create(finalResponseJavascript, response.body().contentType())).build();

                return new WebResourceResponse("application/javascript",
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
