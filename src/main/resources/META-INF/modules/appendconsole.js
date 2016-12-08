define([ "jquery" ], function($) {

    return function(command, id, clearid, appendtext) {
        $("<div class='commandtext' />").html("&gt; " + command).appendTo($('#' + id));
        
        // $("<div />").text("< log line " + appendtext).appendTo($('#' + id));

        $.each(appendtext, function(idx, value) {
            $("<div class='commandresult'/>").html("&lt; " + value).appendTo($('#' + id));
        });
        
        var height = $('#' + id).get(0).scrollHeight;
        $('#' + id).animate({
            scrollTop: height
        }, 200);
        
        $('#' + clearid).val('');        
      }
})