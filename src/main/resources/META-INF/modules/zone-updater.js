// For an element (elementId), listens to it for a given event (clientEvent), and responds by issuing 
// an AJAX request (listenerURI) with the element value appended as a query string, 
// to update a zone (zoneId).
//
// Based on http://tinybits.blogspot.com/2010/03/new-and-better-zoneupdater.html
// and some help from Inge Solvoll.
//
// copied from tapestry jumpstart http://jumpstart.doublenegative.com.au/jumpstart7/examples/ajax/onevent

define([ "jquery", "t5/core/zone" ],
        function($, zoneManager) {

            return function(elementId, clientEvent, listenerURI, zoneElementId, addElement, addconstant) {
                var $element = $("#" + elementId);

                if (clientEvent) {
                    $element.on(clientEvent, updateZone);
                }

                function updateZone() {
                    var listenerURIWithValue = listenerURI;

                    var addvalue;

                    if (addElement) {
                        addvalue = eval(addElement).val();

                    }

                    listenerURIWithValue = appendQueryStringParameter(listenerURIWithValue, 'param', $element.val(), addvalue, addconstant);

                    zoneManager.deferredZoneUpdate(zoneElementId, listenerURIWithValue);
                }
            }

            function appendQueryStringParameter(url, name, value, addvalue, addconstant) {
                if (url.indexOf('?') < 0) {
                    url += '?'
                } else {
                    url += '&';
                }

                value = encodeURIComponent(value);

                url += name + '=' + value;

                if (addvalue) {
                    url += '&add=' + encodeURIComponent(addvalue);
                }

                if (addconstant) {
                    url += '&constant=' + encodeURIComponent(addconstant)
                }
                
                return url;
            }

        });