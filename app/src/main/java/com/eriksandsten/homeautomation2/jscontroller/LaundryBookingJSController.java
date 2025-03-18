package com.eriksandsten.homeautomation2.jscontroller;

import android.webkit.JavascriptInterface;
import com.eriksandsten.homeautomation2.domain.laundrybooking.LaundryBookingEvent;
import com.eriksandsten.homeautomation2.fragments.BaseFragment;
import com.eriksandsten.homeautomation2.fragments.LaundryBookingFragment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class LaundryBookingJSController extends JSController {
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public LaundryBookingJSController(BaseFragment fragment) {
        super(fragment);
    }
    @JavascriptInterface
    public void cancelLaundryBooking(String laundryBookingJSONData) {
        try {
            LaundryBookingEvent laundryBookingEvent = objectMapper.readValue(laundryBookingJSONData, LaundryBookingEvent.class);
            ((LaundryBookingFragment) fragment).getLaundryBookingChromeClient().performLaundryBookingAction(laundryBookingEvent);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
