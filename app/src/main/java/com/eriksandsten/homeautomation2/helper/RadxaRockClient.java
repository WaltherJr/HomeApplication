package com.eriksandsten.homeautomation2.helper;

import com.eriksandsten.homeautomation2.activity.main.BaseActivity;
import com.eriksandsten.homeautomation2.domain.StandbyState;

public class RadxaRockClient extends BaseClient {
    public RadxaRockClient(BaseActivity activity) {
        super(activity.getProperty("radxa_rock_server_url"), 8000);
    }

    public Object getHDMICECConfiguration() {
        return performRequest(() -> HttpHelper.performGetRequest(getServerUrl(), "/hdmi-cec/configuration"));
    }

    public Object getHDMICECTopology() {
        return performRequest(() -> HttpHelper.performGetRequest(getServerUrl(), "/hdmi-cec/topology"));
    }

    public Object setCurrentHDMIChannelRequest(int channel) {
        return performRequest(() -> HttpHelper.performPutRequest(getServerUrl(), "/current-hdmi-channel", "{\"channel\": \"%s\"}".formatted(channel)));
    }

    public Object setTVStandbyStateRequest(StandbyState standbyState) {
        return performRequest(() -> HttpHelper.performPutRequest(getServerUrl(), "/tv/standbystate", "{\"standbyState\": \"%s\"}".formatted(standbyState.getValue())));
    }
}
