package com.eriksandsten.homeautomation2.jscontroller;

import android.content.res.AssetManager;
import android.webkit.JavascriptInterface;
import androidx.preference.PreferenceManager;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.main.MainActivity;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import com.eriksandsten.homeautomation2.fragments.HomeFragment;
import com.eriksandsten.homeautomation2.helper.HttpHelper;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import okhttp3.FormBody;

public class HomeJSController extends JSController {
    private final AssetManager assetManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HomeFragment homeFragment;

    public HomeJSController(BaseFragment fragment, AssetManager assetManager) {
        super(fragment);
        this.homeFragment = (HomeFragment) fragment;
        this.assetManager = assetManager;
    }

    @JavascriptInterface
    public String getDeviceList() throws JsonProcessingException {
        MainActivity mainActivity = (MainActivity) fragment.getActivity();
        return objectMapper.writeValueAsString(mainActivity.wrappedDeviceList.getDevices());
    }

    @JavascriptInterface
    public void setMediaServerOnStatus(boolean status) {
        MainActivity activity = (MainActivity) fragment.getActivity();
        activity.setMediaServerOnStatus(status);
    }

    @JavascriptInterface
    public String setTVStandbyStatus(boolean status) {
        return HttpHelper.performPutRequest(fragment.getAssociatedActivity().getProperty("radxa_rock_server_url"), "/tv/standby", "{\"standby\": \"%s\"}".formatted(status));
    }

    @JavascriptInterface
    public void toggleIsOn(String deviceId) {
        MainActivity activity = (MainActivity) fragment.getActivity();
        activity.toggleIsOn(deviceId);
    }

    @JavascriptInterface
    public void setLightLevel(String deviceId, String lightLevel) {
        MainActivity activity = (MainActivity) fragment.getActivity();
        activity.setLightLevel(deviceId, lightLevel);
    }

    @JavascriptInterface
    public String pingAsusMediaServer() {
        return HttpHelper.performGetRequest(fragment.getAssociatedActivity().getProperty("asus_media_server_url"), "/");
    }
}
