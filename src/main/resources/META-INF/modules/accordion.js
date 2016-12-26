define([ "jquery" ], function($) {

    return function() {
        $(".accordion").accordion({
            collapsible: true,
            active: false,
            heightStyle: "content"
        });
    }
})