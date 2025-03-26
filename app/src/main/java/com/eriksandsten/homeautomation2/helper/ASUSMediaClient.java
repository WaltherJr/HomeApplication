package com.eriksandsten.homeautomation2.helper;

import com.eriksandsten.homeautomation2.activity.main.BaseActivity;

public class ASUSMediaClient extends BaseClient {
    public ASUSMediaClient(BaseActivity activity) {
        super(activity.getProperty("asus_media_server_url"), 2000);
    }

    public Object setCurrentTVChannel(String channelId) {
        return performRequest(() -> HttpHelper.performPutRequest(getServerUrl(), "/tv/current-channel", "{\"channelId\": \"%s\"}".formatted(channelId)));
    }

    public Object setTVChannelPlayStateRequest(boolean playing) {
        return performRequest(() -> HttpHelper.performPutRequest(getServerUrl(), "/music/music-video/mutestate", "{\"playState\": \"%s\"}".formatted(playing ? "playing" : "paused")));
    }

    public Object setTVChannelMuteRequest(boolean muted) {
        return performRequest(() -> HttpHelper.performPutRequest(getServerUrl(), "/tv/current-channel/mutestate", "{\"muteState\": \"%s\"}".formatted(muted ? "muted" : "unmuted")));
    }
    public Object setMusicVideoPlayStateRequest(boolean playing) {
        return performRequest(() -> HttpHelper.performPutRequest(getServerUrl(), "/music/music-video/playstate", "{\"playState\": \"%s\"}".formatted(playing ? "playing" : "paused")));
    }

    public Object setMusicVideoMuteStateRequest(boolean muted) {
        return performRequest(() -> HttpHelper.performPutRequest(getServerUrl(), "/music/music-video/mutestate", "{\"muteState\": \"%s\"}".formatted(muted ? "muted" : "unmuted")));
    }

    public Object setTVChannelVolumeRequest(int volume) {
        return performRequest(() -> HttpHelper.performPutRequest(getServerUrl(), "/tv/current-channel/volume", "{\"volume\": %s}".formatted(String.valueOf(volume))));
    }
}
