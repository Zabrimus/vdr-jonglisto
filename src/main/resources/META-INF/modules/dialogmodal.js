define(["jquery", "bootstrap/modal"], function($) {
    
    var activate = function(modalId, options) {
        $('#' + modalId).modal(options);
    }
    
    var hide = function(modalId) {
        var $modal = $('#' + modalId);

        if ($modal.length > 0) {
            $modal.modal('hide');
        } else {
            $('body').removeClass('modal-open');
            $('.modal-backdrop').remove();
        }
    }    
    
    return {
        activate : activate,
        hide : hide
    }
    
});
