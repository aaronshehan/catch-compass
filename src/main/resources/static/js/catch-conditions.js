/*
 * Prefills the conditions fields from the weather provider.
 *
 * Same principle as the location script: this only ever fills fields the angler
 * could type themselves, and every failure ends with them doing exactly that.
 * It also tracks whether fetched values were subsequently edited, which is what
 * the conditions_source column records.
 */
(function () {
    'use strict';

    var form = document.querySelector('[data-location-form]');
    if (!form) {
        return;
    }

    var button = form.querySelector('[data-conditions-request]');
    var status = form.querySelector('[data-conditions-status]');
    var sourceField = form.querySelector('#conditionsSource');
    if (!button) {
        return;
    }

    var fields = {
        airTemperatureC: form.querySelector('#airTemperatureC'),
        windSpeedMetersPerSecond: form.querySelector('#windSpeedMetersPerSecond'),
        windDirectionDegrees: form.querySelector('#windDirectionDegrees'),
        barometricPressureHpa: form.querySelector('#barometricPressureHpa'),
        skyCondition: form.querySelector('#skyCondition'),
        tideHeightMeters: form.querySelector('#tideHeightMeters'),
        tideState: form.querySelector('#tideState')
    };

    var latitude = form.querySelector('#latitude');
    var longitude = form.querySelector('#longitude');
    var caughtAt = form.querySelector('#caughtAt');
    var observedAt = form.querySelector('#observedAt');

    function say(message) {
        status.textContent = message;
    }

    // Any edit after a fetch downgrades the source. Registered once, up front,
    // so it catches edits regardless of when they happen.
    Object.keys(fields).forEach(function (key) {
        if (fields[key]) {
            fields[key].addEventListener('input', markEdited);
            fields[key].addEventListener('change', markEdited);
        }
    });

    function markEdited() {
        if (sourceField.value === 'WEATHER_API') {
            sourceField.value = 'WEATHER_API_EDITED';
        }
    }

    button.addEventListener('click', function () {
        if (!latitude.value || !longitude.value) {
            say('Set a location first - conditions are looked up for that spot.');
            return;
        }
        if (!caughtAt.value) {
            say('Set the catch time first - conditions are looked up for that moment.');
            return;
        }

        button.disabled = true;
        say('Looking up conditions...');

        var query = '?latitude=' + encodeURIComponent(latitude.value) +
                    '&longitude=' + encodeURIComponent(longitude.value) +
                    '&at=' + encodeURIComponent(new Date(caughtAt.value).toISOString());

        fetch('/api/conditions' + query, { headers: { 'Accept': 'application/json' } })
            .then(function (response) {
                if (!response.ok) {
                    throw new Error('lookup failed');
                }
                return response.json();
            })
            .then(apply)
            .catch(function () {
                say('Could not reach the weather service. Enter conditions manually below.');
            })
            .finally(function () {
                button.disabled = false;
            });
    });

    function apply(result) {
        if (!result.weatherAvailable) {
            say('No weather data for that place and time. Enter conditions manually below.');
            return;
        }

        set(fields.airTemperatureC, result.airTemperatureC);
        set(fields.windSpeedMetersPerSecond, result.windSpeedMetersPerSecond);
        set(fields.windDirectionDegrees, result.windDirectionDegrees);
        set(fields.barometricPressureHpa, result.barometricPressureHpa);
        set(fields.skyCondition, result.skyCondition);
        set(fields.tideHeightMeters, result.tideHeightMeters);
        set(fields.tideState, result.tideState);

        if (result.observedAt && observedAt) {
            // datetime-local wants "YYYY-MM-DDTHH:mm" with no zone or seconds.
            observedAt.value = result.observedAt.substring(0, 16);
        }

        sourceField.value = 'WEATHER_API';

        var note = result.tideState ? '' : ' Tide data is not available from this provider.';
        say('Conditions filled in from Open-Meteo. Check them and correct anything that looks wrong.' + note);
    }

    function set(field, value) {
        if (field && value !== null && value !== undefined) {
            field.value = value;
        }
    }
})();
