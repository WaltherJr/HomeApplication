package com.eriksandsten.homeautomation2.webviewclient;

import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.main.MainActivity;
import com.eriksandsten.homeautomation2.domain.google.GoogleCalendar;
import com.eriksandsten.homeautomation2.domain.laundrybooking.LaundryBookingEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;

public class LaundryBookingChromeClient extends WebChromeClient {
    private final MainActivity mainActivity;
    private final GoogleCalendar googleCalendar;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    public LaundryBookingChromeClient(MainActivity mainActivity) {
        this.googleCalendar = new GoogleCalendar(mainActivity);
        this.mainActivity = mainActivity;
    }

    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        if (consoleMessage.message().startsWith("{") && consoleMessage.message().endsWith("}")) {
            try {
                performLaundryBookingAction(objectMapper.readValue(consoleMessage.message(), LaundryBookingEvent.class));
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return false;
    }

    public void performLaundryBookingAction(LaundryBookingEvent laundryBookingEvent) {
        if (laundryBookingEvent.eventType == LaundryBookingEvent.LaundryBookingEventType.BOOKING_CANCELED) {
            googleCalendar.deleteLaundryCalendarEvent(mainActivity.getGoogleAccountCredentials(), laundryBookingEvent, (calendarLaundryBookings) -> {
                final String message = calendarLaundryBookings.isEmpty() ? mainActivity.getString(R.string.laundry_booking_already_removed) : mainActivity.getString(R.string.laundry_booking_removed);
                Snackbar.make(mainActivity.findViewById(R.id.wvLaundryBooking), message, Snackbar.LENGTH_LONG).show();

            }, (errorMessage) -> Snackbar.make(mainActivity.findViewById(R.id.wvLaundryBooking), errorMessage, Snackbar.LENGTH_LONG).show());

        } else if (laundryBookingEvent.eventType == LaundryBookingEvent.LaundryBookingEventType.BOOKING_MADE) {
            googleCalendar.addLaundryCalendarEvent(mainActivity.getGoogleAccountCredentials(), laundryBookingEvent,
                () -> Snackbar.make(mainActivity.findViewById(R.id.wvLaundryBooking), mainActivity.getString(R.string.laundry_booking_created), Snackbar.LENGTH_LONG).show(),
                (errorMessage) -> Snackbar.make(mainActivity.findViewById(R.id.wvLaundryBooking), errorMessage, Snackbar.LENGTH_LONG).show());
        }
    }
}
