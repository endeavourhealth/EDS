// *************** Keycloak auth token ***************

var token;

function exampleAuthToken() {
	document.getElementById('authhost').value = "https://devauth.discoverydataservice.net";
	document.getElementById('client').value = "eds-data-checker";
	document.getElementById('user').value = "FredBloggs";
	document.getElementById('pass').value = "Pa55w0rd";
}

function getAuthToken() {
	var host = document.getElementById('authhost').value;
	var client = encodeURIComponent(document.getElementById('client').value);
	var user = encodeURIComponent(document.getElementById('user').value);
	var pass = encodeURIComponent(document.getElementById('pass').value);
	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", host + "/auth/realms/endeavour/protocol/openid-connect/token", false);
	xhttp.withCredentials = true;
	xhttp.setRequestHeader("Accept", "*/*");
	xhttp.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
	try {
		xhttp.send("client_id="+client+"&username=" + user + "&password=" + pass + "&grant_type=password");
		try {
			var response = JSON.parse(xhttp.responseText);
			token = response.access_token;
			document.getElementById('authresponse').value = JSON.stringify(response, null, 2);
		} catch (error) {
			document.getElementById('authresponse').value = xhttp.responseText;
		}
	} catch (error) {
		document.getElementById('authresponse').value = error;
	}
}

// *************** Resource types ***************

function exampleResourceTypes() {
	document.getElementById('apihost').value = "https://devgateway.discoverydataservice.net/data-assurance";
	document.getElementById('token1').value = token;
}

function getResourceTypes() {
	var host = document.getElementById('apihost').value;
	var token1 = document.getElementById('token1').value;
	var xhttp = new XMLHttpRequest();
	xhttp.open("GET", host + "/api/fhir/resourceType", false);
	xhttp.withCredentials = true;
	xhttp.setRequestHeader("Authorization", "Bearer " + token1);
	try {
		xhttp.send();
		try {
			var response = JSON.parse(xhttp.responseText);
			document.getElementById('typesresponse').value = JSON.stringify(response, null, 2);
		} catch (error) {
			document.getElementById('typesresponse').value = xhttp.responseText;
		}
	} catch (error) {
		document.getElementById('typesresponse').value = error;
	}
}

// *************** Patient by NHS ***************

function examplePatients() {
	document.getElementById('apihost2').value = "https://devgateway.discoverydataservice.net/data-assurance";
	document.getElementById('token2').value = token;
	document.getElementById('nhsnumber').value = "8111119275";
}

function getPatients() {
	var host = document.getElementById('apihost2').value;
	var nhs = encodeURIComponent(document.getElementById('nhsnumber').value);
	var token2 = document.getElementById('token2').value;
	var xhttp = new XMLHttpRequest();
	xhttp.open("GET", host + "/api/fhir/patients?nhsNumber="+nhs, false);
	xhttp.withCredentials = true;
	xhttp.setRequestHeader("Authorization", "Bearer " + token2);
	try {
		xhttp.send();
		try {
			var response = JSON.parse(xhttp.responseText);
			document.getElementById('patientresponse').value = JSON.stringify(response, null, 2);
			document.getElementById('patients').value = JSON.stringify(response, null, 2);
		} catch (error) {
			document.getElementById('patientresponse').value = xhttp.responseText;
		}
	} catch (error) {
		document.getElementById('patientresponse').value = error;
	}
}

// *************** Patient resources ***************

function exampleResources() {
	document.getElementById('apihost3').value = "https://devgateway.discoverydataservice.net/data-assurance";
	document.getElementById('token3').value = token;
	document.getElementById('resources').value = '"Patient", "Condition"';
	document.getElementById('patients').value = document.getElementById('patientresponse').value;
}

function getResources() {
	var host = document.getElementById('apihost3').value;
	var token3 = document.getElementById('token3').value;
	var types = document.getElementById('resources').value;
	var pats = document.getElementById('patients').value;

	var request = "{ \"resources\": [" + types + "], \"patients\": " + pats + "}";

	var xhttp = new XMLHttpRequest();
	xhttp.open("POST", host + "/api/fhir/resources", false);
	xhttp.withCredentials = true;
	xhttp.setRequestHeader("Authorization", "Bearer " + token3);
	xhttp.setRequestHeader("Content-Type", "Application/Json");
	try {
		xhttp.send(request);
		try {
			var response = JSON.parse(xhttp.responseText);
			document.getElementById('resourceresponse').value = JSON.stringify(response, null, 2);
		} catch (error) {
			document.getElementById('resourceresponse').value = xhttp.responseText;
		}
	} catch (error) {
		document.getElementById('resourceresponse').value = error;
	}
}

// *************** References (admin resources) ***************

function exampleReference() {
	document.getElementById('reference-api').value = "https://devgateway.discoverydataservice.net/data-assurance";
	document.getElementById('reference-token').value = token;
	document.getElementById('reference-reference').value = 'Location/0045c497-11cb-4e64-86b8-1178354dc5ec';
}

function getReference() {
	var host = document.getElementById('reference-api').value;
	var token = document.getElementById('reference-token').value;
	var reference = encodeURIComponent(document.getElementById('reference-reference').value);

	var xhttp = new XMLHttpRequest();
	xhttp.open("GET", host + "/api/fhir/reference?reference="+reference, false);
	xhttp.withCredentials = true;
	xhttp.setRequestHeader("Authorization", "Bearer " + token);
	xhttp.setRequestHeader("Content-Type", "Application/Json");
	try {
		xhttp.send();
		try {
			var response = JSON.parse(xhttp.responseText);
			document.getElementById('reference-response').value = JSON.stringify(response, null, 2);
		} catch (error) {
			document.getElementById('reference-response').value = xhttp.responseText;
		}
	} catch (error) {
		document.getElementById('reference-response').value = error;
	}
}

// *************** Patient flag (frailty) ***************

function exampleFlag() {
	document.getElementById('apihost4').value = "https://devgateway.discoverydataservice.net/eds-api";
	document.getElementById('token4').value = token;
	document.getElementById('nhs2').value = "8111146787";
	document.getElementById('requester').value = "111TESTORG";
	document.getElementById('flag').value = "289999999105";
}

function getFlag() {
	var api4 = document.getElementById('apihost4').value;
	var token4 = document.getElementById('token4').value;
	var nhs2 = encodeURIComponent(document.getElementById('nhs2').value);
	var req = document.getElementById('requester').value;
	var flag = encodeURIComponent(document.getElementById('flag').value);

	var xhttp = new XMLHttpRequest();
	xhttp.open("GET", api4 + "/api/subscriber/flag?code=" + flag + "&subject=" + nhs2, false);
	xhttp.withCredentials = true;
	xhttp.setRequestHeader("Authorization", "Bearer " + token4);
	xhttp.setRequestHeader("OdsCode", req);
	try {
		xhttp.send();
		try {
			var response = JSON.parse(xhttp.responseText);
			document.getElementById('flagresponse').value = JSON.stringify(response, null, 2);
		} catch (error) {
			document.getElementById('flagresponse').value = xhttp.responseText;
		}
	} catch (error) {
		document.getElementById('flagresponse').value = error;
	}
}

// *************** UPRN ***************

function exampleUPRN() {
    document.getElementById('apihost5').value = "https://apiuprn.discoverydataservice.net:8443";
    document.getElementById('token5').value = token;
    document.getElementById('uprn-address').value = "10 Downing St,Westminster,London,SW1A2AA";
}

function getUPRN() {
    var api5 = document.getElementById('apihost5').value;
    var token5 = document.getElementById('token5').value;
    var adrec = encodeURIComponent(document.getElementById('uprn-address').value);
    var qpost = encodeURIComponent(document.getElementById('uprn-qpost').value);
    var orgpost = encodeURIComponent(document.getElementById('uprn-org').value);
    var country = encodeURIComponent(document.getElementById('uprn-country').value);
    var summary = encodeURIComponent(document.getElementById('uprn-summary').value);

    var url = api5 + "/api/getinfo?adrec=" + adrec;
    if (qpost && qpost !== '') url += "&qpost="+qpost;
    if (orgpost && orgpost !== '') url += "&orgpost="+orgpost;
    if (country && country !== '') url += "&country="+country;
    if (summary && summary !== '') url += "&summary="+summary;

    var xhttp = new XMLHttpRequest();
    xhttp.open("GET", url, false);
    xhttp.withCredentials = true;
    xhttp.setRequestHeader("Authorization", "Bearer " + token5);
    try {
        xhttp.send();
        try {
            var response = JSON.parse(xhttp.responseText);
            document.getElementById('uprn-response').value = JSON.stringify(response, null, 2);
        } catch (error) {
            document.getElementById('uprn-response').value = xhttp.responseText;
        }
    } catch (error) {
        document.getElementById('uprn-response').value = error;
    }
}

// *************** Access request form ***************
function validEmail(email) {
	var re = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;
	return re.test(email);
}

function validateForm() {
	document.querySelector(".email-invalid").style.display = "none";
	document.querySelector(".recap-invalid").style.display = "none";

	var forename = document.querySelector("#forename");
	if (forename.value === "") {
		forename.focus();
		return false;
	}

	var surname = document.querySelector("#surname");
	if (surname.value === "") {
		surname.focus();
		return false;
	}

	var email = document.querySelector("#email");
	if (email.value === "" || !validEmail(email.value)) {
		email.focus();
		document.querySelector(".email-invalid").style.display = "block";
		return false;
	}

	var reason = document.querySelector("#reason");
	if (reason.value === "") {
		reason.focus();
		return false;
	}

	if (grecaptcha.getResponse() === "") {
		document.querySelector(".recap-invalid").style.display = "block";
		return false;
	}

	// Validation passed, send form!
	return sendForm();
}

function sendForm() {
	document.querySelector("#submit").disabled = true;
	var form = document.querySelector(".gform");
	var data = getFormData(form);
	var url = "https://script.google.com/macros/s/AKfycbwxnFzg-nESYcg3KAJZQ-_XUcXqcsCZ5jkVfji8fEIrq6c286nW/exec";
	var xhr = new XMLHttpRequest();
	xhr.open('POST', url);
	// xhr.withCredentials = true;
	xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	xhr.onreadystatechange = function() {
		if (xhr.status === 200) {
			document.querySelector("#submit").style.display = "none";
			document.querySelector("#entry-form").style.display = "none";
			document.querySelector("#thankyou_message").style.display = "block";
		} else {
			document.querySelector("#submit").disabled = false;
		}
		return;
	};
	// url encode form data for sending as post data
	var encoded = Object.keys(data).map(function(k) {
		return encodeURIComponent(k) + "=" + encodeURIComponent(data[k]);
	}).join('&');
	xhr.send(encoded);

	return false;
}

function getFormData(form) {
	var elements = form.elements;

	var fields = Object.keys(elements).filter(function(k) {
		return (elements[k].name !== "honeypot");
	}).map(function(k) {
		if(elements[k].name !== undefined) {
			return elements[k].name;
			// special case for Edge's html collection
		}else if(elements[k].length > 0){
			return elements[k].item(0).name;
		}
	}).filter(function(item, pos, self) {
		return self.indexOf(item) == pos && item;
	});

	var formData = {};
	fields.forEach(function(name){
		var element = elements[name];

		// singular form elements just have one value
		formData[name] = element.value;

		// when our element has multiple items, get their values
		if (element.length) {
			var data = [];
			for (var i = 0; i < element.length; i++) {
				var item = element.item(i);
				if (item.checked || item.selected) {
					data.push(item.value);
				}
			}
			formData[name] = data.join(', ');
		}
	});

	// add form-specific values into the data
	formData.formDataNameOrder = JSON.stringify(fields);
	formData.formGoogleSheetName = form.dataset.sheet || "responses"; // default sheet name
	formData.formGoogleSendEmail = form.dataset.email || ""; // no email by default

	return formData;
}
