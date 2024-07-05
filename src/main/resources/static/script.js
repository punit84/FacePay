
var loadingOverlay = document.getElementById('loading-overlay');

// Function to enable the loading overlay
function showLoadingOverlay() {

	var loadingOverlay = document.getElementById('loading-overlay');

	if (loadingOverlay) {
		loadingOverlay.classList.remove('disabled');
	} else {
		console.error("Element with ID 'loadingOverlay' not found");
	}
}

// Function to disable the loading overlay
function hideLoadingOverlay() {

	var loadingOverlay = document.getElementById('loading-overlay');

	if (loadingOverlay) {
		loadingOverlay.classList.add('disabled');
	} else {
		console.error("Element with ID 'loadingOverlay' not found");
	}
}

document.addEventListener("DOMContentLoaded", function() {
	var deviceType = detectDeviceType();
	var deviceTypeElement = document.getElementById("deviceType");
	deviceTypeElement.textContent = deviceTypedeviceType;
});

function detectDeviceType() {
	if (/iPhone|iPad|iPod/i.test(navigator.userAgent)) {
		return "iOS";
	} else if (/Android/i.test(navigator.userAgent)) {
		return "Android";
	}
	else if (/Macintosh/i.test(navigator.userAgent)) {
		return "Macintosh";
	}
	else {
		return navigator.userAgent;
	}
}

function toggleRegisterButton() {
	var checkBox = document.getElementById("consent");
	var registerButton = document.getElementById("registerButton");
	registerButton.disabled = !checkBox.checked || hasSpaces();
}

function hasSpaces() {
	var inputs = document.querySelectorAll('input[type="text"]');
	for (var i = 0; i < inputs.length; i++) {
		if (/\s/.test(inputs[i].value)) {
			alert("Please remove spaces from all fields.");
			return true;
		}
	}
	return false;
}

function validateForm() {
	var checkBox = document.getElementById("content");
	if (!checkBox.checked) {
		alert("Please agree to the terms and conditions.");
		return false;
	}
	return !hasSpaces();
}

window.onload = function() {
	//loadCachedImage();
}

function loadCachedImage() {
	var uploadedImage = localStorage.getItem('imageFile');

	//	if (uploadedImage) {
	//		document.getElementById('preview').setAttribute('src', uploadedImage);
	//		document.getElementById('imageFileSelected').files[0] = uploadedImage;
	//		updateFileDetails(uploadedImage)

	//	}
}

let fd = null;

function updateFileDetails(file) {
	var fileSize = 0;

	if (file.size > 1024 * 1024)

		fileSize = (Math.round(file.size * 100 / (1024 * 1024)) / 100).toString() + 'MB';

	else

		fileSize = (Math.round(file.size * 100 / 1024) / 100).toString() + 'KB';

	document.getElementById('details').innerHTML += 'Name: ' + file.name + '<br>Size: ' + fileSize + '<br>Type: ' + file.type;

	document.getElementById('details').innerHTML += '<p>';
}

function fileSelected() {
	var count = document.getElementById('imageFileSelected').files.length;
	document.getElementById('details').innerHTML = "";

	for (var index = 0; index < count; index++) {

		var file = document.getElementById('imageFileSelected').files[index];
		var device = detectDeviceType();
		fd = new FormData();
		fd.append('myFile', file);
		fd.append('device', device);


		var reader = new FileReader();
		reader.onload = function(e) {
			document.getElementById('preview').setAttribute('src', e.target.result);
			localStorage.setItem('imageFile', e.target.result);
		}
		reader.readAsDataURL(file);
		updateFileDetails(file)
	}

}

function searchSelectedFile() {

	fileSelected();

	searchFaceId(fd);

}

function fetchDocumentDetails() {

	fileSelected();
	fetchDocumentAPI(fd);
}



function searchQR() {

	var count = document.getElementById('imageFileSelected').files.length;

	document.getElementById('details').innerHTML = "";

	for (var index = 0; index < count; index++) {

		var file = document.getElementById('imageFileSelected').files[index];

		var fileSize = 0;

		if (file.size > 1024 * 1024)

			fileSize = (Math.round(file.size * 100 / (1024 * 1024)) / 100).toString() + 'MB';

		else

			fileSize = (Math.round(file.size * 100 / 1024) / 100).toString() + 'KB';

		document.getElementById('details').innerHTML += 'Name: ' + file.name + '<br>Size: ' + fileSize + '<br>Type: ' + file.type;

		document.getElementById('details').innerHTML += '<p>';

	}

	searchUserInfo();

}



function searchQARTFaceIDInfo() {
	var url = '/api/userbyface';
	var method = 'POST';
	var fd = new FormData();

	var faceid = localStorage.getItem('faceid');

	if (faceid == null || faceid == 'undefined' || faceid == '') {
		console.log('Face ID is null or undefined');

	} else {
		fd.append('faceid', faceid);

		var xhr = new XMLHttpRequest();

		xhr.addEventListener("load", displayInfo, false);


		xhr.open(method, url, true); // true for asynchronous request

		xhr.send(fd);

	}

}

function showDownloadButton(imageId) {
	var image = document.getElementById(imageId);
	var downloadBtn = document.querySelector(`#${imageId} + .download-btn`);
	downloadBtn.classList.remove('hidden');
}

function downloadImage(imageId) {
	var image = document.getElementById(imageId);
	var url = image.src;
	var filename = 'image.jpg'; // You can set a custom filename here
	var anchor = document.createElement('a');
	anchor.href = url;
	anchor.setAttribute('download', filename);
	document.body.appendChild(anchor);
	anchor.click();
	document.body.removeChild(anchor);
	window.location.href = window.location.href; // Redirect to the same page
	event.preventDefault(); // Prevent the default anchor behavior

}

function searchUserInfo() {
	var url = '/api/userinfo';
	var method = 'POST';
	var fd = new FormData();
	var device = detectDeviceType();

	var count = document.getElementById('imageFileSelected').files.length;

	for (var index = 0; index < count; index++) {

		var file = document.getElementById('imageFileSelected').files[index];

		fd.append('myFile', file);
		fd.append('device', device)

	}

	var xhr = new XMLHttpRequest();
	xhr.upload.addEventListener("progress", uploadProgress, false);
	xhr.addEventListener("load", displayInfo, false);
	xhr.addEventListener("error", uploadFailed, false);

	xhr.addEventListener("abort", uploadCanceled, false);


	xhr.open(method, url, true); // true for asynchronous request

	xhr.send(fd);

	//	showLoadingOverlay();
}

function displayInfo(evt) {

	/* This event is raised when the server send back a response */
	//alert(evt.target.responseText);
	//alert(evt.target.responseText);

	var text = evt.target.responseText;
	if (text == '') {
	}
	else if (text == 'REGISTER-FACE-FIRST-VISIT-ADMIN-PAGE') {
		var userResponse = confirm("Given face is not registered. Do you want to Enroll now?");
		if (userResponse) {
			window.location.href = 'admin';
		} else {
			// User canceled, do nothing
		}
	}
	else if (text == 'NO-HUMAN-FACE-FOUND') {
		alert("Only human faces are supported ");
	}
	else if (text == 'SERVER_ERROR') {
		alert("PLEASE TRY AFTER SOMETIME");
	}
	else {

		document.getElementById("userInfo").style.display = "block";

		var data = JSON.parse(text);
		localStorage.setItem('faceid', data.faceid);

		// Display user info
		//document.getElementById('name').textContent = data.name;
		document.getElementById('upi').value = data.value;
		document.getElementById('qart').src = data.qart;
		document.getElementById('image').src = data.image;

		//document.getElementById('details').innerHTML += 'UPI url with given face : ' + text + '<br>';
		loadingOverlay.style.display = 'none';

	}

}

function hasSpaces(value) {
	return /^\s*$/.test(value) || /\s/.test(value);
}

function registerFace() {

	var imageID = document.getElementById('imageID');

	var imageIDValue = imageID.value;
	if (hasSpaces(imageIDValue)) {
		alert("Kindly provide valid UPI ID or URL");
		return true;
	} else {

		var url = '/api/registerImage';
		var method = 'POST';
		var fd = new FormData();
		var count = document.getElementById('imageFileSelected').files.length;
		var imagePhoneValue = imagePhone.value;
		var imageEmailValue = imageEmail.value;

		for (var index = 0; index < count; index++) {
			var file = document.getElementById('imageFileSelected').files[index];
			fd.append('myFile', file);
			fd.append('imageID', imageIDValue);
			fd.append('imagePhone', imagePhoneValue);
			fd.append('imageEmail', imageEmailValue);

		}

		var xhr = new XMLHttpRequest();

		xhr.upload.addEventListener("progress", uploadProgress, false);

		xhr.addEventListener("load", registerComplete, false);

		xhr.addEventListener("error", uploadFailed, false);

		xhr.addEventListener("abort", uploadCanceled, false);

		xhr.open(method, url, false); // true for asynchronous request

		xhr.send(fd);
		//showLoadingOverlay();

	}



}



function fetchDocumentAPI(fd) {
	var url = '/api/kyc';
	var method = 'POST';
	// Include selected dropdown values in the form data
	var requestType = document.getElementById('requestType').value;
	var docType = document.getElementById('docType').value;
	fd.append('requestType', requestType);
	fd.append('docType', docType);


	showLoadingOverlay();

	var xhr = new XMLHttpRequest();

	xhr.upload.addEventListener("progress", uploadProgress, false);

	xhr.addEventListener("load", redirectToKYC, false);

	xhr.addEventListener("error", uploadFailed, false);

	xhr.addEventListener("abort", uploadCanceled, false);

	xhr.open(method, url, true); // true for asynchronous request

	xhr.send(fd);

}

function searchFaceId(fd) {
	var url = '/api/facepay';
	var method = 'POST';
	showLoadingOverlay();

	var xhr = new XMLHttpRequest();

	xhr.upload.addEventListener("progress", uploadProgress, false);

	xhr.addEventListener("load", redirectToPay, false);

	xhr.addEventListener("error", uploadFailed, false);

	xhr.addEventListener("abort", uploadCanceled, false);

	xhr.open(method, url, true); // true for asynchronous request

	xhr.send(fd);

}
function uploadProgress(evt) {

	if (evt.lengthComputable) {

		var percentComplete = Math.round(evt.loaded * 100 / evt.total);
		document.getElementById('progress').innerHTML = percentComplete.toString() + '%';
	}
	else {

		document.getElementById('progress').innerHTML = 'unable to compute';

	}
}

function registerComplete(evt) {

	/* This event is raised when the server send back a response */

	hideLoadingOverlay();

	var text = evt.target.responseText;

	if (text == 'Already Registered Contact Admin') {
		alert(evt.target.responseText);


	}
	else  {
		localStorage.setItem('faceid', text);
		alert("User enrolled Successfully");

	}

}

function profileDisplay(evt) {

	/* This event is raised when the server send back a response */
	document.getElementById('bedrock').innerHTML = '<br>' + evt.target.responseText + '<br><br>';

}

function redirectToKYC(evt) {
	hideLoadingOverlay(); // Assuming hideLoadingOverlay() is defined elsewhere

	try {
		var jsonResponse = evt.target.responseText;
		var parsedJson = JSON.parse(jsonResponse);

		var html = "<h3>Document Information:</h3><table>";

		// Check if parsedJson is an array and get the first object
		var jsonData = Array.isArray(parsedJson) ? parsedJson[0] : parsedJson;

		// Extract text content if present
		var contentText = (jsonData.content && jsonData.content.length > 0 && jsonData.content[0].text) ? jsonData.content[0].text : null;

		// Parse the inner JSON if contentText is valid JSON
		var innerJsonData = contentText ? JSON.parse(contentText) : jsonData;

		var docTypeSelect = document.getElementById('docType').value;
		// Iterate over the innerJsonData object to display key-value pairs
		for (var key in innerJsonData) {
			if (innerJsonData.hasOwnProperty(key)) {

				if (key === docTypeSelect ){
					document.getElementById("docTypeLabel").innerHTML = "Is "+ docTypeSelect +" : "+ innerJsonData[key];
					document.getElementById("docTypeLabel").style.color = "green"; // Change 'red' to any color you prefer
					document.getElementById("docTypeLabel").style.fontSize = "30px"; // Change '20px' to any size you prefer
					if (innerJsonData[key] === false || innerJsonData[key] === "false"){
						document.getElementById("docTypeLabel").style.color = "red"; // Change 'red' to any color you prefer
					}

				}
				html += "<tr><th>" + key.replace(/_/g, ' ') + "</th><td>" + innerJsonData[key] + "</td></tr>";
			}
		}
		html += "</table>";

		// Display the HTML content in the 'bedrock' element
		document.getElementById('bedrock').innerHTML = '<br>' + html + '<br><br>';

	} catch (error) {
		console.error("Error parsing or displaying JSON:", error);
		// Handle error display or logging as needed
	}
}
// Function to parse JSON and render as key-value pairs
function renderKeyValue(jsonResponse) {
	// Assuming jsonResponse is an array and we're accessing the first object
	var jsonContent = jsonResponse[0].text; // Assuming there's only one object in the array

	// Parse the JSON content
	var parsedJson = JSON.parse(jsonContent);

	// Prepare HTML for displaying key-value pairs
	var html = "<h3>Key-Value Form:</h3>";
	html += "<table>";

	// Iterate through each key-value pair in parsedJson
	for (var key in parsedJson) {
		// Check if the value is a boolean and convert to readable format
		var value = parsedJson[key];
		if (typeof value === 'boolean') {
			value = value ? 'TRUE' : 'FALSE'; // Convert boolean to string representation
		}

		// Build HTML row for key-value pair
		html += "<tr><th>" + key + "</th><td>" + value + "</td></tr>";
	}

	html += "</table>";

	// Display HTML content in 'bedrock' element
	document.getElementById("bedrock").innerHTML = html;
}
function redirectToPay(evt) {

	/* This event is raised when the server send back a response */
	//alert(evt.target.responseText);
	//alert(evt.target.responseText);
	hideLoadingOverlay();

	var text = evt.target.responseText;
	if (text == 'REGISTER-FACE-FIRST-VISIT-ADMIN-PAGE') {
		var userResponse = confirm("Given face is not registered. Do you want to Enroll now?");
		if (userResponse) {
			window.location.href = 'admin';
		} else {
			// User canceled, do nothing
		}
	}
	else if (text == 'NO-HUMAN-FACE-FOUND') {
		alert("Only human faces are supported ");
	}
	else if (text == 'SERVER_ERROR') {
		alert("PLEASE TRY AFTER SOMETIME");
	}
	else {
		document.getElementById('details').innerHTML += 'UPI url with given face : ' + text + '<br>';
		document.getElementById('details').value

		// Check if the anchor tag already exists
		let linkElement = document.getElementById("dynamic-link");

		// If it doesn't exist, create a new one
		if (!linkElement) {
			linkElement = document.createElement("a");
			linkElement.id = "dynamic-link";
			linkElement.textContent = "Click to Pay";
			const linkContainer = document.getElementById("link-container");
			linkContainer.appendChild(linkElement);
		}

		// Set the href attribute of the anchor tag
		linkElement.href = text;

		loadingOverlay.style.display = 'none';
		window.location.href = text;

		// Trigger click event on the anchor tag

		setTimeout(() => {
			linkElement.click();
		}, 20);
	}

	profile(fd)


}

function profile(fd) {
	var url = '/api/profile';
	var method = 'POST';

	var xhr = new XMLHttpRequest();

	xhr.upload.addEventListener("progress", uploadProgress, false);

	xhr.addEventListener("load", profileDisplay, false);

	xhr.addEventListener("error", uploadFailed, false);

	xhr.addEventListener("abort", uploadCanceled, false);

	xhr.open(method, url, true); // true for asynchronous request

	xhr.send(fd);

	showLoadingOverlay();
}

function uploadFailed(evt) {
	registerComplete
	alert("There was an error attempting to upload the file.");

}

function uploadCanceled(evt) {

	registerComplete
	alert("The upload has been canceled by the user or the browser dropped the connection.");

}

async function fetchDocumentData(url, method) {
	try {
		const response = await fetch(url, { method: method }); // Fetch data using provided URL and method
		if (!response.ok) {
			throw new Error('Network response was not ok');
		}
		return await response.json();
	} catch (error) {
		console.error('Error fetching documents:', error);
		return null; // Handle error gracefully
	}
}

// Function to update document type dropdown
async function updateDocTypeDropdown() {
	var requestType = document.getElementById('requestType').value;
	var docTypeSelect = document.getElementById('docType');
	docTypeSelect.innerHTML = ''; // Clear previous options

	try {
		const documentData = await fetchDocumentData('/api/doctype', 'GET'); // Call fetchDocumentData with URL and GET method
		if (documentData && requestType in documentData) {
			addOptionsToSelect(docTypeSelect, documentData[requestType]);
		} else {
			docTypeSelect.innerHTML = '<option disabled selected>No documents found</option>';
		}

		// Enable the docType select
		docTypeSelect.disabled = false;
	} catch (error) {
		console.error('Error fetching documents:', error);
		docTypeSelect.innerHTML = '<option disabled selected>Error fetching documents</option>';
	}
}

// Function to add options to select dropdown
function addOptionsToSelect(selectElement, options) {
	options.forEach(function(option) {
		var optionElement = document.createElement('option');
		optionElement.value = option;
		optionElement.textContent = option;
		selectElement.appendChild(optionElement);
	});
}