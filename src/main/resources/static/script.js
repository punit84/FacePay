
var loadingOverlay = document.getElementById('loading-overlay');

// Function to enable the loading overlay
function showLoadingOverlay() {
	loadingOverlay.classList.remove('disabled');
}

// Function to disable the loading overlay
function hideLoadingOverlay() {
	loadingOverlay.classList.add('disabled');
}

function fileSelected() {

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

}

function searchSelectedFile() {

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

	searchFaceId();

}


function addImage() {
	var url = '/api/addImage';
	var method = 'POST';
	var fd = new FormData();

	var count = document.getElementById('imageFileSelected').files.length;
	var imageID = document.getElementById('imageID');

	var imageIDValue = imageID.value;

	for (var index = 0; index < count; index++) {

		var file = document.getElementById('imageFileSelected').files[index];

		fd.append('myFile', file);
		fd.append('imageID', imageIDValue)

	}

	var xhr = new XMLHttpRequest();

	xhr.upload.addEventListener("progress", uploadProgress, false);

	xhr.addEventListener("load", uploadComplete, false);

	xhr.addEventListener("error", uploadFailed, false);

	xhr.addEventListener("abort", uploadCanceled, false);

	xhr.open(method, url, false); // true for asynchronous request

	xhr.send(fd);

}

function searchFaceId() {
	var url = '/api/facepay';
	var method = 'POST';
	var fd = new FormData();

	var count = document.getElementById('imageFileSelected').files.length;

	for (var index = 0; index < count; index++) {

		var file = document.getElementById('imageFileSelected').files[index];

		fd.append('myFile', file);

	}


	var xhr = new XMLHttpRequest();

	xhr.upload.addEventListener("progress", uploadProgress, false);

	xhr.addEventListener("load", redirectToPay, false);

	xhr.addEventListener("error", uploadFailed, false);

	xhr.addEventListener("abort", uploadCanceled, false);

	xhr.open(method, url, true); // true for asynchronous request

	xhr.send(fd);

	showLoadingOverlay();
}

window.addEventListener('DOMContentLoaded', function() {
	var loadingOverlay = document.getElementById('loading-overlay');

	// Function to enable the loading overlay
	function showLoadingOverlay() {
		loadingOverlay.classList.remove('disabled');
	}

	// Function to disable the loading overlay
	function hideLoadingOverlay() {
		loadingOverlay.classList.add('disabled');
	}

	// Simulating a delay for demonstration purposes
	setTimeout(function() {
		// Enable the loading overlay
		showLoadingOverlay();

		// Simulating another delay before disabling the overlay
		setTimeout(function() {
			// Disable the loading overlay
			hideLoadingOverlay();
		}, 2000); // Adjust the delay as per your requirements
	}, 2000); // Adjust the delay as per your requirements
});


function uploadProgress(evt) {

	if (evt.lengthComputable) {

		var percentComplete = Math.round(evt.loaded * 100 / evt.total);
		document.getElementById('progress').innerHTML = percentComplete.toString() + '%';
	}
	else {

		document.getElementById('progress').innerHTML = 'unable to compute';

	}
}

function uploadComplete(evt) {

	/* This event is raised when the server send back a response */

	alert(evt.target.responseText);

}

function redirectToPay(evt) {

	/* This event is raised when the server send back a response */
	//alert(evt.target.responseText);
	//alert(evt.target.responseText);
	hideLoadingOverlay();
	 
	document.getElementById('details').innerHTML += 'UPI url with given face : ' + evt.target.responseText + '<br>' ;


	loadingOverlay.style.display = 'none';

	window.location.href = evt.target.responseText;


}

function uploadFailed(evt) {
	loadingOverlay.style.display = 'none';


	alert("There was an error attempting to upload the file.");

}

function uploadCanceled(evt) {

	loadingOverlay.style.display = 'none';

	alert("The upload has been canceled by the user or the browser dropped the connection.");

}