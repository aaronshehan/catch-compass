/*
 * Fills the location fields on the catch form from the browser's Geolocation API.
 *
 * Progressive enhancement: the fields work perfectly well typed by hand. This
 * only ever populates them, and every failure path tells the user to do that
 * instead. A catch must never be un-loggable because the GPS did not cooperate.
 */
(function () {
    'use strict';

    var form = document.querySelector('[data-location-form]');
    if (!form) {
        return;
    }

    var requestButton = form.querySelector('[data-location-request]');
    var clearButton = form.querySelector('[data-location-clear]');
    var status = form.querySelector('[data-location-status]');

    var latitude = form.querySelector('#latitude');
    var longitude = form.querySelector('#longitude');
    var accuracy = form.querySelector('#locationAccuracyMeters');
    var recordedAt = form.querySelector('#locationRecordedAt');

    function say(message) {
        status.textContent = message;
    }

    if (!('geolocation' in navigator)) {
        requestButton.disabled = true;
        say('This browser cannot provide your location. Enter coordinates below.');
        return;
    }

    // Geolocation is refused outside a secure context. localhost counts as
    // secure; a phone loading this over plain http on your wifi does not.
    if (!window.isSecureContext) {
        requestButton.disabled = true;
        say('Location needs an HTTPS connection. Enter coordinates below.');
        return;
    }

    requestButton.addEventListener('click', function () {
        requestButton.disabled = true;
        say('Finding your location...');

        navigator.geolocation.getCurrentPosition(onSuccess, onFailure, {
            enableHighAccuracy: true,
            timeout: 15000,
            maximumAge: 0
        });
    });

    clearButton.addEventListener('click', function () {
        latitude.value = '';
        longitude.value = '';
        accuracy.value = '';
        recordedAt.value = '';
        say('Location cleared. This catch will be saved without coordinates.');
    });

    function onSuccess(position) {
        var coords = position.coords;

        // Six decimal places is roughly 0.1 m, well beyond any phone's accuracy,
        // and matches the NUMERIC(8,6) / NUMERIC(9,6) columns.
        latitude.value = coords.latitude.toFixed(6);
        longitude.value = coords.longitude.toFixed(6);
        accuracy.value = coords.accuracy ? coords.accuracy.toFixed(2) : '';
        recordedAt.value = new Date(position.timestamp).toISOString();

        say('Location captured, accurate to about ' + Math.round(coords.accuracy) +
            ' m. Check it below and correct it if it looks wrong.');
        requestButton.disabled = false;
    }

    function onFailure(error) {
        say(explain(error));
        requestButton.disabled = false;
    }

    function explain(error) {
        switch (error.code) {
            case error.PERMISSION_DENIED:
                return 'Location permission was denied. You can still type coordinates below.';
            case error.POSITION_UNAVAILABLE:
                return 'Your device could not work out where it is. Try again, or enter coordinates below.';
            case error.TIMEOUT:
                return 'Finding your location took too long. Try again, or enter coordinates below.';
            default:
                return 'Location is unavailable. Enter coordinates below.';
        }
    }
})();
