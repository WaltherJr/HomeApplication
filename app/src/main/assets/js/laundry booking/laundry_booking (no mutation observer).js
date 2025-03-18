
function pollForBookingEvent() {
    var feedbackDialogDiv = document.getElementById('FeedbackDialogDiv'); // TODO: optimize (direct descendant of body)

    if (feedbackDialogDiv && feedbackDialogDiv.style.display !== 'none') {
        // console.log('FOUND FEEDBACK DIALOG INFORMATION!');
        var feedbackDialog = document.getElementById('FeedbackDialogInformation');

        if (feedbackDialog.innerHTML.match(/^Ditt pass har blivit avbokat\.$/)) {
            var closeButton = document.getElementById('FeedbackDialogCloseButton');
            closeButton.addEventListener('click', listenForBookingEvent);
            console.log('BOOKING CANCELED EVENT! innerHTML of feedback dialog: ' + feedbackDialog.innerHTML + ", of close button: " + closeButton.innerHTML);

            return {eventType: 'BOOKING_CANCELED', data: {}};
        } else {
            var dialogInfoBookingMade = feedbackDialog.innerHTML.match(/^Ditt valda pass ([a-zåäö]+) ([0-9]{1,2}) ([a-zåäö]+) ([0-9][0-9]:[0-9][0-9])-([0-9][0-9]:[0-9][0-9]) är bokat\.$/);

            if (dialogInfoBookingMade) {
                var closeButton = document.getElementById('FeedbackDialogCloseButton');
                console.log('BOOKING MADE EVENT! innerHTML of feedback dialog: ' + feedbackDialog.innerHTML + ", of close button: " + closeButton.innerHTML);
                closeButton.addEventListener('click', listenForBookingEvent);

                return {
                    eventType: 'BOOKING_MADE', data: {
                        day: dialogInfoBookingMade[1],
                        dayOfMonth: dialogInfoBookingMade[2],
                        month: dialogInfoBookingMade[3],
                        hoursFrom: dialogInfoBookingMade[4],
                        hoursTo: dialogInfoBookingMade[5]
                    }
                };
            }
        }
    }

    return null;
}

function listenForBookingEvent() {
    localStorage.setItem('bookingInterval', setInterval(bookingPolling, 1000));
}

function bookingPolling() {
    var bookingEventPresent = pollForBookingEvent();

    if (bookingEventPresent) {
        console.log('CLEARING INTERVAL...');
        clearInterval(localStorage.getItem('bookingInterval'));
        console.log(JSON.stringify(bookingEventPresent));
    }
}

listenForBookingEvent();
