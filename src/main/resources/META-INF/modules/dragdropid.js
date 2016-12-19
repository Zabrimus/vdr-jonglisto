define([ "jquery" ], function($) {

    return function(baseUri, id, dragclass, dropclass) {
        $(dragclass).draggable({
            revert: "invalid",
            
            helper: "clone",
            containment:"document"
        })
        
        $(dropclass).droppable({
          drop: function( event, ui ) {
              // inform jonglisto
              $.ajax({
                  url: baseUri + "?from=" + $(ui.draggable).attr('data-id-drag' + id) + "&to=" + $(this).attr('data-id-drop' + id) + "&name=" + $(ui.draggable).attr('data-id-name' + id) + "&providerid=" + id,
                  type: 'GET'
               });
              
              // change attribute
              $(ui.draggable).attr('data-id-drag' + id, $(this).attr('data-id-drop' + id));
              
              // append dragged element
              ui.draggable.detach().appendTo($(this));
          }
        });
    }
})
