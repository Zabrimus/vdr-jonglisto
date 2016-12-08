define(
        [ "jquery", "t5/core/zone" ],
        function($, zoneManager) {
            return function(elementId, clientEvent, listenerURI, zoneElementId) {
                var $element = $("#" + elementId);

                if (clientEvent) {
                    $element.on(clientEvent, observe);
                }

                function observe() {
                    var listenerURIWithValue = listenerURI;

                    alert(JSON.stringify($element.parent().parent(), null, 4));

                    var test = $element.parent().parent();
                    alert(JSON.stringify($(test).children(':hidden:first'),
                            null, 4));
                    alert($(test).children(':hidden:first').val());

                    if ($element.val()) {
                        listenerURIWithValue = appendQueryStringParameter(
                                listenerURIWithValue, 'param', $element.val());
                    }

                    zoneManager.deferredZoneUpdate(zoneElementId,
                            listenerURIWithValue);
                }
            }

            function appendQueryStringParameter(url, name, value) {
                if (url.indexOf('?') < 0) {
                    url += '?'
                } else {
                    url += '&';
                }
                value = escape(value);
                url += name + '=' + value;
                return url;
            }
        });