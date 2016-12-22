define([ "jquery" ], function($) {

    return function(baseUri) {

        var myBaseUri = baseUri;
        
        $(".portlet-column").sortable({
          connectWith: ".portlet-column",
          handle: ".portlet-header",
          cancel: ".portlet-toggle",
          placeholder: "portlet-placeholder ui-corner-all"
        });
     
        $(".portlet")
          .addClass( "ui-widget ui-widget-content ui-helper-clearfix ui-corner-all" )
          .find(".portlet-header")
          .addClass( "ui-widget-header ui-corner-all" )
          .prepend( "<span class='ui-icon ui-icon-plusthick portlet-toggle'></span>");
     
        $(".portlet-toggle").on("click", function() {
          var icon = $(this);
          icon.toggleClass( "ui-icon-minusthick ui-icon-plusthick" );
          icon.closest(".portlet").find(".portlet-content").toggle();
        });
     
        $(".sortable").sortable({
            
            start: function(event, ui) {
                ui.item.startPos = ui.item.index();
            },
            
            update: function(event, ui) {
                 var from = ui.item.startPos;
                 var to   = ui.item.index();
                 
                 $.ajax({
                     url: myBaseUri + "?from=" + from + "&to=" + to + "&group=true",
                     type: 'GET'
                 });
            }
        });
        
        $(".connectedSortable").sortable({
            connectWith: ".connectedSortable",
            
            start: function(event, ui) {
                ui.item.startPos = ui.item.index();

                // initial status
                ui.item.connectedDrop = false;
            },

            receive: function( event, ui ) {                
                // save all values                
                var id = $(ui.item).attr('data-channel-drag');
                var to = $(this).attr('data-group-name');
                var from = $(ui.item).attr('data-group-drag');
                var fromidx = ui.item.startPos;
                var toIdx = ui.item.index();
                
                // change group of dragged item
                $(ui.item).attr('data-group-drag', $(this).attr('data-group-name'));
                
                // save status
                ui.item.connectedDrop = true;
                
                // inform jonglisto
                $.ajax({
                    url: myBaseUri + "?id=" + id + "&to=" + to + "&from=" + from + "&fromidx=" + fromidx + "&toidx=" + toIdx + "&group=false",
                    type: 'GET'
                 });
            },
            
            stop: function(event, ui) {
                // save all values                
                var id = $(ui.item).attr('data-channel-drag');
                var from = $(this).attr('data-group-name');
                var to = $(ui.item).attr('data-group-drag');
                var fromidx = ui.item.startPos;
                var toIdx = ui.item.index();

                if (! ui.item.connectedDrop) {
                    // inform jonglisto only when this not already happened
                    $.ajax({
                        url: myBaseUri + "?id=" + id + "&to=" + to + "&from=" + from + "&fromidx=" + fromidx + "&toidx=" + toIdx + "&group=false",
                        type: 'GET'
                    });
                }
            }
        }).disableSelection();
    }      
})
