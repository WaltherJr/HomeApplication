window.originalCancelLaundryBookingFunction = window.Unbook;

window.Unbook = function() {
    var cancelBookingDataStr = (document.querySelector('#ErrorMessageDiv #PassData') || {innerHTML: ''}).innerHTML;
    var cancelBookingData = cancelBookingDataStr.match(/Vill du avboka din bokning <b>([0-9]{2}:[0-9]{2})-([0-9]{2}:[0-9]{2})<\/b> på ([a-zåäö]+) ([0-9]{1,2}) ([a-z]+)\?/);

    if (cancelBookingData) {
        originalCancelLaundryBookingFunction();
        javaInterface.cancelLaundryBooking(JSON.stringify({
            eventType: 'BOOKING_CANCELED',
            data: {
                day: cancelBookingData[3],
                dayOfMonth: cancelBookingData[4],
                month: cancelBookingData[5],
                hoursFrom: cancelBookingData[1],
                hoursTo: cancelBookingData[2]
            }
        }));
    }
}
