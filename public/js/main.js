
function clearErrors() {
	$("#errors").hide();
}

function showError(msg) {
	$("#error-message").text("Error: " + msg);
	$("#errors").show();
}

function validateReceipt(receipt) {
	var re = /^LIN[0-9]{10}$/
	return re.test(receipt);
}

function validateEmail(email) {
    var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
    return re.test(email);
}

