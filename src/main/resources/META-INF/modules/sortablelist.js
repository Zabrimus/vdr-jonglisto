define([ "jquery" ], function($) {

    return function(baseUri, clazz) {
        var myBaseUri = baseUri;
        
        $("." + clazz).sortable({
            
            start: function(event, ui) {
                /*
                 ui.item.startPos = ui.item.index();
                 */
            },
            
            update: function(event, ui) {
                /*
                 var from = ui.item.startPos;
                 var to   = ui.item.index();
                 
                 
                 var id2 = $(ui.item).attr('data-channel-drag');
                 var to2 = $(this).attr('data-group-name');
                 var from2 = $(ui.item).attr('data-group-drag');
                 
                 
                 var cd = ui.item.connectedDrop;
                 
                 alert("type of: " + typeof cd + " -> id2: " + id2 + ", to2: " + to2 + ", from2: " + from2);
                 
                 if( typeof cd === 'undefined') {
                     // no connected drop -> inform jonglisto
                     $.ajax({
                         url: myBaseUri + "?from=" + from + "&to=" + to,
                         type: 'GET'
                     });
                 } else {
                     // connected drop, change group of dragged item
                     $(ui.item).attr('data-group-drag', $(this).attr('data-group-name'));    
                 }
                 */
            }
        }).disableSelection();
    }
})