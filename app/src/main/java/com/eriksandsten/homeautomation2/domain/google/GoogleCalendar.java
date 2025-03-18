package com.eriksandsten.homeautomation2.domain.google;

import com.eriksandsten.homeautomation2.utils.CapitalizedResourceString;
import com.eriksandsten.homeautomation2.R;
import com.eriksandsten.homeautomation2.activity.main.MainActivity;
import com.eriksandsten.homeautomation2.domain.laundrybooking.LaundryBookingEvent;
import com.eriksandsten.homeautomation2.utils.ResourceString;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.CalendarListEntry;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class GoogleCalendar {
    private static final Locale localeSvSE = Locale.forLanguageTag("sv-SE");
    private static final DateTimeFormatter defaultHoursPattern = DateTimeFormatter.ofPattern("HH:mm", localeSvSE);
    private static final Executor calendarRequestExecutor = Executors.newSingleThreadExecutor();
    private final MainActivity mainActivity;
    public Map<String, Integer> swedishMonthMappings;
    private Map<Integer, String> swedishMessageMappings;

    public GoogleCalendar(MainActivity mainActivity) {
        this.mainActivity = mainActivity;

        // Use only Swedish locale strings since the booking application is in Swedish only
        mainActivity.getLocalizedResources(localeSvSE, resources -> {
            swedishMonthMappings = IntStream.range(1, 12 + 1).boxed().collect(Collectors
                    .toMap(monthIndex -> resources.getString(mainActivity.getStringIdentifier("month_" + monthIndex)), monthIndex -> monthIndex));

            swedishMessageMappings = Stream.of(ResourceString.from(R.string.laundry_time_from_to), CapitalizedResourceString.from(R.string.laundry_time))
                    .collect(Collectors.toMap(ResourceString::getResourceId, resourceStr -> resourceStr.getString(resources)));
        });
    }

    private void initCalendarRequest(GoogleAccountCredential googleAccountCredentials, BiConsumer<Calendar, CalendarListEntry> actionCallback) {
        calendarRequestExecutor.execute(() -> {
            try {
                HttpTransport transport = AndroidHttp.newCompatibleTransport();
                JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
                final Calendar calendarClient = new Calendar.Builder(transport, jsonFactory, googleAccountCredentials).setApplicationName(mainActivity.getString(R.string.app_name)).build();
                List<CalendarListEntry> calendars = calendarClient.calendarList().list().execute().getItems();
                CalendarListEntry mainAccountCalendar = calendars.stream().filter(calendar -> calendar.getId().equals(googleAccountCredentials.getSelectedAccountName())).findFirst()
                        .orElseThrow(() -> new NoSuchElementException(mainActivity.getString(R.string.no_calendar_for_the_google_account).formatted(googleAccountCredentials.getSelectedAccountName())));
                actionCallback.accept(calendarClient, mainAccountCalendar);

            } catch (final IOException e) {
                throw new RuntimeException(e.getCause());
            }
        });
    }

    public void addLaundryCalendarEvent(GoogleAccountCredential credentials, LaundryBookingEvent laundryBookingEvent, Runnable successCallback, Consumer<String> failureCallback) {
        initCalendarRequest(credentials, (calendar, mainAccountCalendar) -> {
            try {
                final Event newLaundryBooking = new Event().setStart(createEventDateTime(laundryBookingEvent, laundryBookingEvent.data.hoursFrom))
                        .setEnd(createEventDateTime(laundryBookingEvent, laundryBookingEvent.data.hoursTo))
                        .setSummary(createBookingEventSummary(laundryBookingEvent));

                // Check that booking has not already been added
                final Optional<Event> existingLaundryBooking = getExistingCalendarLaundryBooking(calendar, mainAccountCalendar.getId(), newLaundryBooking);

                if (existingLaundryBooking.isEmpty()) {
                    calendar.events().insert(mainAccountCalendar.getId(), newLaundryBooking).execute();
                    successCallback.run();
                } else {
                    // Booking already exists
                    failureCallback.accept(mainActivity.getString(R.string.laundry_booking_already_made_for_this_point_in_time));
                }
            } catch (final IOException e) {
                throw new RuntimeException(e.getCause());
            }
        });
    }

    public void deleteLaundryCalendarEvent(GoogleAccountCredential credentials, LaundryBookingEvent laundryBookingEvent, Consumer<Event> successCallback, Consumer<String> failureCallback) {
        initCalendarRequest(credentials, (calendar, mainAccountCalendar) -> {
            try {
                final Event existingLaundryBookingToMatch = new Event().setStart(createEventDateTime(laundryBookingEvent, laundryBookingEvent.data.hoursFrom))
                        .setEnd(createEventDateTime(laundryBookingEvent, laundryBookingEvent.data.hoursTo))
                        .setSummary(createBookingEventSummary(laundryBookingEvent));
                final Optional<Event> existingLaundryBooking = getExistingCalendarLaundryBooking(calendar, mainAccountCalendar.getId(), existingLaundryBookingToMatch);

                if (existingLaundryBooking.isEmpty()) {
                    failureCallback.accept(mainActivity.getString(R.string.no_laundry_booking_to_remove));
                } else {
                    calendar.events().delete(mainAccountCalendar.getId(), existingLaundryBooking.get().getId()).execute();
                    successCallback.accept(existingLaundryBookingToMatch);
                }
            } catch (final IOException e) {
                throw new RuntimeException(e.getCause());
            }
        });
    }

    private Optional<Event> getExistingCalendarLaundryBooking(Calendar calendar, String mainAccountCalendarId, Event newLaundryBooking) throws IOException {
        final List<Event> existingLaundryBookings = calendar.events().list(mainAccountCalendarId).setQ(newLaundryBooking.getSummary()).execute().getItems();
        return existingLaundryBookings.isEmpty() ? Optional.empty() : isBookingAlreadyMade(existingLaundryBookings, newLaundryBooking);
    }

    public Optional<Event> isBookingAlreadyMade(List<Event> existingLaundryBookings, Event newBookingEvent) {
        // Important to use only dateTime field in Event.getStart() and Event.getEnd(), else the objects won't be considered equal because timezone is still missing in the new one
        return existingLaundryBookings.stream().map(existingLaundryBooking -> {
            List<Boolean> conditions = List.of(existingLaundryBooking.getStart().getDateTime().equals(newBookingEvent.getStart().getDateTime()),
                                                existingLaundryBooking.getEnd().getDateTime().equals(newBookingEvent.getEnd().getDateTime()),
                                                existingLaundryBooking.getSummary().equals(newBookingEvent.getSummary()));

            return conditions.stream().allMatch(condition -> condition.equals(Boolean.TRUE)) ? existingLaundryBooking : null;
        }).filter(Objects::nonNull).findAny();
    }

    private String createBookingEventSummary(LaundryBookingEvent laundryBookingEvent) {
        return swedishMessageMappings.get(R.string.laundry_time_from_to).formatted(laundryBookingEvent.data.hoursFrom.format(defaultHoursPattern),
                laundryBookingEvent.data.hoursTo.format(defaultHoursPattern));
    }

    public String constructRFC3339String(LaundryBookingEvent laundryBookingEvent, LocalTime hoursPart) {
        // TODO: check if making booking in january and it's december, then this code is wrong. Fix timezone also
        final String upperCaseMonth = laundryBookingEvent.data.month.substring(0, 1).toUpperCase() + laundryBookingEvent.data.month.substring(1);
        final Integer mappedMonth = swedishMonthMappings.get(upperCaseMonth);
        final String dateString = "%s-%s-%sT%s:00".formatted(LocalDate.now().getYear(), String.format(localeSvSE, "%02d", mappedMonth),
                String.format(localeSvSE, "%02d", laundryBookingEvent.data.dayOfMonth), hoursPart.format(defaultHoursPattern));

        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(ZonedDateTime.of(LocalDateTime.parse(dateString), ZoneId.systemDefault()));
    }

    private EventDateTime createEventDateTime(LaundryBookingEvent laundryBookingEvent, LocalTime hoursPart) {
        return new EventDateTime().setDateTime(DateTime.parseRfc3339(constructRFC3339String(laundryBookingEvent, hoursPart)));
    }
}
