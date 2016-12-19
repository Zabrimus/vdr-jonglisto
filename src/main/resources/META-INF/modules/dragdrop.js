define([ "jquery" ], function($) {

    return function(baseUri, dragclass, dropclass) {
        $(dragclass).draggable({
            revert: "invalid",
            
            helper: "clone",
            containment:"document"
        })
        
        $(dropclass).droppable({
          drop: function( event, ui ) {
              // inform jonglisto
              $.ajax({
                  url: baseUri + "?from=" + $(ui.draggable).attr('data-channel-drag') + "&to=" + $(this).attr('data-channel-drop') + "&name=" + $(ui.draggable).attr('data-channel-name'),
                  type: 'GET'
               });
              
              // change attribute
              $(ui.draggable).attr('data-channel-drag', $(this).attr('data-channel-drop'));
              
              // append dragged element
              ui.draggable.detach().appendTo($(this));
          }
        });
    }
})
