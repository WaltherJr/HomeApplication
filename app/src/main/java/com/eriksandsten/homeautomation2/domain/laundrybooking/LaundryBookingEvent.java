package com.eriksandsten.homeautomation2.domain.laundrybooking;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LaundryBookingEvent {
    public LaundryBookingEventType eventType;
    public LaundryBookingEventData data;

    public enum LaundryBookingEventType {
        BOOKING_MADE, BOOKING_CANCELED
    };

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class LaundryBookingEventData {
        public String day;
        public Integer dayOfMonth;
        public String month;
        public LocalTime hoursFrom;
        public LocalTime hoursTo;
    }

    public record AptusPortalCredentials(String username, String password) {}
}
