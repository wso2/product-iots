function GeoMap(divId, latitude, longitude, zoom) {
    this.divId = divId;
    this.latitude = latitude;
    this.longitude = longitude;
    this.zoom = zoom;
    this.markers = new Object();

    // Create the map
    this.map = L.map(divId).setView([latitude, longitude], zoom);

    L.Icon.Default.imagePath = '/portal/store/carbon.super/fs/gadget/Android_Location_Map/img/marker';

    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw', {
        maxZoom: 18,
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
            '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
            'Imagery © <a href="http://mapbox.com">Mapbox</a>',
        id: 'mapbox.streets'
    }).addTo(this.map);

    this.addMarker = function(id, latitude, longitude, message) {

        // Move the map to keep the current location at the center
        this.center(latitude, longitude);

        if (!this.markers[id]) {
            marker = L.marker([latitude, longitude])
            marker.bindPopup(message).openPopup();

            // Add to the list of this.markers
            this.markers[id] = marker;

            // Add to the map
            this.map.addLayer(marker)
        } else {
            marker = this.markers[id];
            marker.setLatLng([latitude, longitude]);
            marker.setPopupContent(message);
            marker.update();
        }
    };

    this.removeMarker = function(id) {
        if (this.markers[id]) {
            this.map.removeLayer(this.markers[id])
            delete this.markers[id];
        }
    };

    this.center = function(latitude, longitude) {
        this.map.panTo([latitude, longitude]);
    };

    this.remove = function() {
        this.map.remove();
        document.getElementById('bottomBox').innerHTML = "";
    };

    this.showCurrentLocation = function(id, latitude, longitude, message) {
        this.addMarker(id, latitude, longitude, message);
    };

    this.showHeatMap = function(data) {
        this.heat = L.heatLayer(data).addTo(this.map);
    }

    this.showTimeline = function(data) {
        // Move the map to keep the current location at the center
        this.center(data.geometry.coordinates[0][1], data.geometry.coordinates[0][0]);

        // Get start/end times
        var startTime = new Date(data.properties.time[0]);
        var endTime = new Date(data.properties.time[data.properties.time.length - 1]);

        // Create a DataSet with data
        var timelineData = new vis.DataSet([{ start: startTime, end: endTime }]);

        // Set timeline options
        var timelineOptions = {
            "width": "100%",
            "height": "120px",
            "style": "box",
            "axisOnTop": true,
            "showCustomTime": true
        };

        // Setup timeline
        var timeline = new vis.Timeline(document.getElementById('bottomBox'), timelineData, timelineOptions);

        // Set custom time marker (blue)
        timeline.setCustomTime(startTime);

        // Playback options
        var playbackOptions = {
            playControl: true,
            dateControl: true,

            // layer and marker options
            layer: {
                pointToLayer: function(featureData, latlng) {
                    var result = {};

                    if (featureData && featureData.properties && featureData.properties.path_options) {
                        result = featureData.properties.path_options;
                    }

                    if (!result.radius) {
                        result.radius = 1;
                    }

                    return new L.CircleMarker(latlng, result);
                }
            },

            marker: {
                getPopup: function(featureData) {
                    var result = '';

                    if (featureData && featureData.properties && featureData.properties.title) {
                        result = featureData.properties.title;
                    }

                    return result;
                }
            }

        };

        // Initialize playback
        var playback = new L.Playback(this.map, null, onPlaybackTimeChange, playbackOptions);

        playback.setData(data);
        // playback.addData(blueMountain);

        // Uncomment to test data reset;
        //playback.setData(blueMountain);    

        // Set timeline time change event, so cursor is set after moving custom time (blue)
        timeline.on('timechange', onCustomTimeChange);

        // A callback so timeline is set after changing playback time
        function onPlaybackTimeChange(ms) {
            timeline.setCustomTime(new Date(ms));
        };

        // 
        function onCustomTimeChange(properties) {
            if (!playback.isPlaying()) {
                playback.setCursor(properties.time.getTime());
            }
        }
    };
}
