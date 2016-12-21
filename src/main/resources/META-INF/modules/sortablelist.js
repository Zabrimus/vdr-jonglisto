define([ "jquery" ], function($) {

    return function(clazz) {
        $("." + clazz).sortable();
        $("." + clazz).disableSelection();    
    }
})