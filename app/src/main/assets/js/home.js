setTimeout(function() {
    $('.device-room-heading').on('click', function() {
        $(this).siblings('.device-room-content').slideToggle();
    });

    $('#devicesList button.device-can-toggle-standby').on('click', function() {
        var newState = javaInterface.toggleIsOn($(this).closest('[data-device-id]').attr('data-device-id'));
        // $(this).toggleClass('is-active', newState);
    });

    $('body').on('change', '.slider-light-level', function() {
        javaInterface.setLightLevel($(this).closest('[data-device-id]').attr('data-device-id'), $(this).val());
    });

    setInterval(function() {
        var devices = JSON.parse(javaInterface.getDeviceList());

        for (const device in devices) {
            var button = $('[data-device-id="' + devices[device].id + '"] button.device-can-toggle-standby');

            if (button.length === 1) {
                var isReachable = devices[device].isReachable;
                var isOn = devices[device].attributes.isOn;
                // alert(devices[device].attributes.customName + ' - isReachable: ' + isReachable + ', isOn: ' + isOn);
                button.toggleClass('is-disabled', isReachable === false);
                button.toggleClass('is-active', isReachable === true && isOn === true);
            }
        }
    }, 500);
}, 200);
