package com.eriksandsten.homeautomation2.fragments;

import android.os.Bundle;
import android.webkit.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.main.MainActivity;
import com.eriksandsten.homeautomation2.domain.laundrybooking.LaundryBookingEvent;
import com.eriksandsten.homeautomation2.jscontroller.LaundryBookingJSController;
import com.eriksandsten.homeautomation2.utils.HomeAutomationUtils;
import com.eriksandsten.homeautomation2.utils.injection.DOMTarget;
import com.eriksandsten.homeautomation2.utils.injection.JSInjection;
import com.eriksandsten.homeautomation2.utils.injection.LocalJavaScript;
import com.eriksandsten.homeautomation2.webviewclient.LaundryBookingChromeClient;
import java.time.LocalTime;
import java.util.regex.Pattern;
import lombok.Getter;

public class LaundryBookingFragment extends BaseFragment {
    private static final Pattern bookingMadeMessageRegex = Pattern.compile("Ditt valda pass ([a-zåäö]+) ([0-9]{1,2}) ([a-z]+) ([0-9]{2}:[0-9]{2})-([0-9]{2}:[0-9]{2}) är bokat\\.");
    private String laundryBookingJavaScript;
    private String aptusPortalLoginScript;
    private String aptusPortalNavigateToBookingsScript;
    private LaundryBookingEvent.AptusPortalCredentials aptusPortalCredentials;

    @Getter
    private LaundryBookingChromeClient laundryBookingChromeClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        aptusPortalCredentials = new LaundryBookingEvent.AptusPortalCredentials(getAssociatedActivity().getProperty("aptus_portal_username"), getAssociatedActivity().getProperty("aptus_portal_password"));
        laundryBookingJavaScript = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "js/laundry booking/laundry_booking.js");
        aptusPortalLoginScript = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "js/laundry booking/aptus_portal_login.js");
        aptusPortalNavigateToBookingsScript = HomeAutomationUtils.loadAssetFileAsString(getResources().getAssets(), "js/laundry booking/aptus_portal_navigate_to_bookings.js");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_laundry_booking, container, false);
        laundryBookingChromeClient = new LaundryBookingChromeClient((MainActivity) getActivity());

        WebView webView = view.findViewById(R.id.wvLaundryBooking);
        HomeAutomationUtils.setupDefaultWebView(webView,
                new JSInjection(new LocalJavaScript("laundry-booking-script", DOMTarget.BODY, laundryBookingJavaScript)),
                null, new LaundryBookingJSController(this), this::onPageFinishedCallback, null, null,
                laundryBookingChromeClient);
        webView.post(() -> webView.loadUrl("https://engelsmannen4.safeteam.se/AptusPortalStyra/Account/Login?ReturnUrl=%2fAptusPortalStyra%2f"));

        return view;
    }

    private void onPageFinishedCallback(WebView webView, String url) {
        if (url.endsWith("/AptusPortalStyra/Account/Login?ReturnUrl=%2fAptusPortalStyra%2f")) {
            webView.post(() -> webView.evaluateJavascript("javascript:" +
                    aptusPortalLoginScript.formatted(aptusPortalCredentials.username(), aptusPortalCredentials.password()), null));

        } else if (url.endsWith("/AptusPortalStyra/")) {
            webView.post(() -> webView.evaluateJavascript("javascript:" + aptusPortalNavigateToBookingsScript, null));

        } else if (url.endsWith("/AptusPortalStyra/CustomerBooking")) {
            checkForLaundryBookingMade(webView);
        }
    }

    private void checkForLaundryBookingMade(WebView webView) {
        webView.evaluateJavascript("(function() { return document.documentElement.outerHTML; })();", html -> {
            var bookingMadeMessage = bookingMadeMessageRegex.matcher(html);

            if (bookingMadeMessage.find()) {
                laundryBookingChromeClient.performLaundryBookingAction(new LaundryBookingEvent(LaundryBookingEvent.LaundryBookingEventType.BOOKING_MADE,
                        new LaundryBookingEvent.LaundryBookingEventData(bookingMadeMessage.group(1), Integer.parseInt(bookingMadeMessage.group(2)), bookingMadeMessage.group(3),
                                LocalTime.parse(bookingMadeMessage.group(4)), LocalTime.parse(bookingMadeMessage.group(5)))));
            }
        });
    }
}
