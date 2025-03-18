package com.eriksandsten.homeautomation2.jscontroller;

import android.content.res.AssetManager;
import android.webkit.JavascriptInterface;
import androidx.preference.PreferenceManager;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.main.MainActivity;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import com.eriksandsten.homeautomation2.helper.OnTVHelper;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import okhttp3.FormBody;

public class HomeJSController extends JSController {
    private final AssetManager assetManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HomeJSController(BaseFragment fragment, AssetManager assetManager) {
        super(fragment);
        this.assetManager = assetManager;
    }

    @JavascriptInterface
    public String getDeviceRooms() {
        try {
            final String roomDevicesTemplateHTML = HomeAutomationUtils.loadAssetFileAsString(assetManager, "html/room_devices.phtml");
            final Boolean showDeviceIds = PreferenceManager.getDefaultSharedPreferences(fragment.getContext()).getBoolean("show_ikea_home_device_ids_prefkey", Boolean.FALSE);
            final String deviceItemClasses = "ikea-home-device" + (showDeviceIds ? " device-id-shown" : "");
            final String deviceItemDefaultCustomName = fragment.getString(R.string.device_item_default_custom_name);
            final String roomDefaultName = fragment.getString(R.string.room_default_name);
            final MainActivity activity = (MainActivity) fragment.getActivity();
            final String roomsHtml = HomeAutomationUtils.renderAsHTML(roomDevicesTemplateHTML,
                    Map.of("deviceItemClasses", deviceItemClasses,
                            "roomDefaultName", roomDefaultName,
                            "deviceItemDefaultCustomName", deviceItemDefaultCustomName,
                            "rooms", activity.roomsWithDevices));

            return roomsHtml;
        } catch (final IOException | RuntimeException e) {
            return "";
        }
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
        return OnTVHelper.performPutRequest(fragment.getAssociatedActivity().getProperty("radxa_rock_media_server_url"), "/tv/standby",
                new FormBody.Builder().add("standby", String.valueOf(status)).build());
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
        return OnTVHelper.performGetRequest(fragment.getAssociatedActivity().getProperty("asus_media_server_url"), "/");
    }
}
