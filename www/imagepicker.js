/*global cordova,window,console*/
/**
 * An Image Picker plugin for Cordova
 * 
 * Developed by Wymsee for Sync OnSet
 */

var ImagePicker = function() {

};

/*
*	success - success callback
*	fail - error callback
*	options
*		.maximumImagesCount
*		.maxWidth
*		.maxHeight
*/
ImagePicker.prototype.getPictures = function(success, fail, options) {
	if (!options) {
		options = {};
	}
	
	var params = {
		maximumImagesCount: options.maximumImagesCount ? options.maximumImagesCount : 15,
		width: options.width ? options.width : 0,
		height: options.height ? options.height : 0,
		quality: options.quality ? options.quality : 100
	};

	return cordova.exec(success, fail, "ImagePicker", "getPictures", [params]);
};

cordova.addConstructor(function() {
	window.imagePicker = new ImagePicker();

	// backwards compatibility	
	window.plugins = window.plugins || {};
	window.plugins.imagePicker = window.imagePicker;
	console.log("Image Picker Registered under window.imagePicker");
});
