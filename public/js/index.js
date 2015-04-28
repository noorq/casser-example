

function onSubscribe() {
	
	clearErrors();
	
	var receipt = $("#sub-receipt").val();
	var email = $("#sub-email").val();
	
	if (!validateReceipt(receipt)) {
		showError("invalid receipt number");
		return;
	}

	if (!validateEmail(email)) {
		showError("invalid email");
		return;
	}

	JsRoutes.controllers.Application.doSubscribe(receipt, email)
	.ajax({
	    success : function(data) { 
			$("#subscribe-form").hide();
			$("#message-form").show();
	    },
	    error: function( jqXhr, textStatus, errorThrown ){
	    	showError('Internal Server Error'); 
	    }
	});

}

function onReload() {
	window.location.reload();
}

