package com.eriksandsten.homeautomation2.google;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.eriksandsten.homeautomation2.activity.main.MainActivity;
import com.eriksandsten.homeautomation2.domain.google.GoogleCalendar;
import com.eriksandsten.homeautomation2.domain.laundrybooking.LaundryBookingEvent;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.time.LocalTime;

@RunWith(AndroidJUnit4.class)
public class GoogleCalendarTest {
    @Test
    public void testConstructRFC3339String() {
        var laundryBooking = new LaundryBookingEvent(LaundryBookingEvent.LaundryBookingEventType.BOOKING_CANCELED,
                LaundryBookingEvent.LaundryBookingEventData.builder().day("måndag").month("January").dayOfMonth(5)
                        .hoursFrom(LocalTime.of(10, 0)).hoursTo(LocalTime.of(13, 0)).build());

        var a = new GoogleCalendar(new MainActivity()).constructRFC3339String(laundryBooking, LocalTime.of(10, 0));
        Assert.assertEquals("hejsan", a);
    }
}
