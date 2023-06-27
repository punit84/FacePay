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

	xhr.open(method, url, false); // true for asynchronous request

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

function uploadComplete(evt) {

	/* This event is raised when the server send back a response */

	alert(evt.target.responseText);

}

function redirectToPay(evt) {

	/* This event is raised when the server send back a response */
	//alert(evt.target.responseText);

	window.location.href = evt.target.responseText;
}

function uploadFailed(evt) {

	alert("There was an error attempting to upload the file.");

}

function uploadCanceled(evt) {

	alert("The upload has been canceled by the user or the browser dropped the connection.");

}