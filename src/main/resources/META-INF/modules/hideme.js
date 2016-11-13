define(["jquery"], function($) {

    return function(trigger) {
    	// hide all
    	$(trigger).next(".hideme").addClass('hidden');
    	$(trigger).parent("fieldset").removeClass('fieldset-border');
    	
    	// add event handler
    	$(trigger).click(function() {
    		if ( $(this).parent("fieldset").hasClass('fieldset-border')) {
    			$(this).parent("fieldset").removeClass('fieldset-border');
    		} else {
    			$(this).parent("fieldset").addClass('fieldset-border');
    		}
    		
    		$(this).next(".hideme").toggleClass('hidden');    		
    	});
    }
})