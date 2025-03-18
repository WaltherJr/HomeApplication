package com.eriksandsten.homeautomation2.jscontroller;

import android.webkit.JavascriptInterface;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import com.eriksandsten.homeautomation2.helper.OnTVHelper;
import java.util.HashMap;
import java.util.Map;
import okhttp3.FormBody;

public class OnTVJSController extends JSController {
    private static Map<String, String> subscribedTVChannels = new HashMap<>();

    static {
        subscribedTVChannels.put("0148", "20001"); // SVT1
        subscribedTVChannels.put("0282", "20002"); // SVT2
        subscribedTVChannels.put("0290", "20003"); // TV3
        subscribedTVChannels.put("0227", "20004"); // TV4
        subscribedTVChannels.put("0279", "20005"); // Kanal 5
        subscribedTVChannels.put("0360", "20064"); // Kanal 6
        subscribedTVChannels.put("0232", "20007"); // Kanal 7
        subscribedTVChannels.put("666", "20008");  // Kanal 8
        subscribedTVChannels.put("474", "20009");  // Kanal 9
        subscribedTVChannels.put("667", "20060");  // Kanal 10
        subscribedTVChannels.put("0235", "20011"); // Kanal 11
        subscribedTVChannels.put("664", "20012");  // Kanal 12
        subscribedTVChannels.put("1000", "20104"); // ATG Live
        subscribedTVChannels.put("722", "20093");  // Godare
        subscribedTVChannels.put("0149", "20013"); // Kunskapskanalen HD
        subscribedTVChannels.put("1053", "20122"); // AXESS TV
        subscribedTVChannels.put("0147", "20010"); // SVT Barn HD
    }

    public OnTVJSController(BaseFragment fragment) {
        super(fragment);
    }

    @JavascriptInterface
    public String getTVChannelMapping(String channelKey) {
        return subscribedTVChannels.get(channelKey);
    }

    @JavascriptInterface
    public boolean isSubscribedTVChannel(String channelKey) {
        return subscribedTVChannels.containsKey(channelKey);
    }

    @JavascriptInterface
    public String setActiveTVChannel(String channelId) {
        return OnTVHelper.performPutRequest(fragment.getAssociatedActivity().getProperty("asus_media_server_url"),
                "/tv/channel/active", new FormBody.Builder().add("channelId", channelId).build());
    }

    @JavascriptInterface
    public String viewTVChannelEPG() {
        return OnTVHelper.performGetRequest(fragment.getAssociatedActivity().getProperty("asus_media_server_url"), "/tv/channel/epg");
    }

    @JavascriptInterface
    public String setActiveTVChannelPlayState(String isPlaying) {
        return OnTVHelper.performPutRequest(fragment.getAssociatedActivity().getProperty("asus_media_server_url"),
                "/tv/channel/active/playstate", new FormBody.Builder().add("isPlaying", isPlaying).build());
    }

    @JavascriptInterface
    public String getYoutubeFrameJS() {
        return """
setTimeout(function() {
    window.parent.postMessage('test!' + this.querySelectorAll('ytm-media-item').length, '*')
}, 5000);""";
        // return "window.parent.postMessage('dede!', '*');";
    }

    @JavascriptInterface
    public void switchTVHDMIChannel(String channel) {
        String response = OnTVHelper.performPutRequest(fragment.getAssociatedActivity().getProperty("radxa_rock_media_server_url"),
                "/tv/hdmi/channel", new FormBody.Builder().add("channel", channel).build());
    }

    @JavascriptInterface
    public String turnOnTVChannel(String channelId) {
        return OnTVHelper.performPutRequest(fragment.getAssociatedActivity().getProperty("asus_media_server_url"), "/tv", new FormBody.Builder()
                .add("channelId", channelId).build());
    }

    @JavascriptInterface
    public String getActiveTVChannel() {
        return OnTVHelper.performGetRequest(fragment.getAssociatedActivity().getProperty("asus_media_server_url"), "/tv/channel/active", 200);
    }
}
