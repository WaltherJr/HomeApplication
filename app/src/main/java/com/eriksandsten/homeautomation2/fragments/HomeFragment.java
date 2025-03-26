package com.eriksandsten.homeautomation2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import androidx.preference.PreferenceManager;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.main.MainActivity;
import com.eriksandsten.homeautomation2.domain.Room;
import com.eriksandsten.homeautomation2.jscontroller.HomeJSController;
import com.eriksandsten.homeautomation2.utils.CapitalizedResourceString;
import com.eriksandsten.homeautomation2.utils.injection.CSSInjection;
import com.eriksandsten.homeautomation2.utils.injection.DOMTarget;
import com.eriksandsten.homeautomation2.utils.injection.RemoteJavascript;
import com.eriksandsten.homeautomation2.utils.injection.InlineJavascript;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.eriksandsten.homeautomation2.utils.injection.JSInjection;
import com.eriksandsten.homeautomation2.utils.injection.InlineStylesheet;
import org.springframework.util.MimeTypeUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends BaseFragment {
    private MainActivity mainActivity;
    private String baseJavaScript;
    private String homeJavaScript;
    private String baseCSS;
    private String homeCSS;
    private String mainHTMLTemplate;
    private String roomsFragmentHTMLTemplate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseJavaScript = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "js/base.js");
        homeJavaScript = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "js/home.js");
        baseCSS = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "css/base.css");
        homeCSS = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "css/home.css");
        mainHTMLTemplate = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "html/home.phtml");
        roomsFragmentHTMLTemplate = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "html/room_devices.phtml");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        WebView webView = view.findViewById(R.id.wvHome);

        HomeAutomationUtils.setupDefaultWebView(webView,
                new JSInjection(
                    new RemoteJavascript("jquery-script", DOMTarget.BODY,
                            getAssociatedActivity().getProperty("jquery_script_file_url"),
                            getAssociatedActivity().getProperty("jquery_script_file_integrity")),
                    new InlineJavascript("base-script", DOMTarget.BODY, baseJavaScript),
                    new InlineJavascript("home-script", DOMTarget.BODY, homeJavaScript)
                ), new CSSInjection(
                    new InlineStylesheet("base-stylesheet", baseCSS),
                    new InlineStylesheet("home-stylesheet", homeCSS)
                ),
                new HomeJSController(this, getResources().getAssets()), null);

        try {
            final String roomsFragmentHtml = getRoomsFragmentRenderedHTML();
            final String renderedHtml = HomeAutomationUtils.renderAsHTML(mainHTMLTemplate, Map.of(
            "documentLanguage", Locale.getDefault().toLanguageTag(),
            "documentCharacterSet", StandardCharsets.UTF_8.name(),
            "documentTitle", HomeAutomationUtils.getDefaultHTMLDocumentTitle(getContext(), CapitalizedResourceString.from(R.string.home).getString(getResources())),
            "devicesList", mainActivity.getRoomsWithDevices(),
            "roomDevices", roomsFragmentHtml
            ));

            webView.post(() -> webView.loadDataWithBaseURL("file:///android_asset/", renderedHtml, MimeTypeUtils.TEXT_HTML_VALUE, StandardCharsets.UTF_8.name(), null));

        } catch (final IOException e) {
            throw new RuntimeException(e.getCause());
        }

        return view;
    }

    public String getRoomsFragmentRenderedHTML() throws IOException {
        final boolean showDeviceIds = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("show_ikea_home_device_ids_prefkey", Boolean.FALSE);
        final String deviceItemClasses = "ikea-home-device" + (showDeviceIds ? " device-id-shown" : "");
        final String deviceItemDefaultCustomName = getString(R.string.device_item_default_custom_name);
        final String roomDefaultName = getString(R.string.room_default_name);
        final List<Room> rooms = mainActivity.getRoomsWithDevices();

        return HomeAutomationUtils.renderAsHTML(roomsFragmentHTMLTemplate, Map.of(
                "deviceItemClasses", deviceItemClasses,
                "roomDefaultName", roomDefaultName,
                "deviceItemDefaultCustomName", deviceItemDefaultCustomName,
                "rooms", rooms)
        );
    }
}
