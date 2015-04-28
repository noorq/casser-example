

function onUnsubscribe() {

	clearErrors();
	
	var receipt = $("#unsub-receipt").val();
	var email = $("#unsub-email").val();
	
	if (!validateReceipt(receipt)) {
		showError("invalid receipt number");
		return;
	}

	if (!validateEmail(email)) {
		showError("invalid email");
		return;
	}
	
	JsRoutes.controllers.Application.doUnsubscribe(receipt, email)
	.ajax({
	    success : function(data) { 
			$("#unsubscribe-form").hide();
			$("#message-form").show();
	    },
	    error: function( jqXhr, textStatus, errorThrown ){
	    	showError('Internal Server Error'); 
	    }
	});

}


