define([ "jquery" ], function($) {

    return function() {
        $("#optgroup").multiselect({
            keepRenderingSort: true,
            submitAllLeft: false,
            submitAllRight: true
        });
    }
})