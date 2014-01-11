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
*		.maximumImagesCount - max images to be selected, defaults to 15. If this is set to 1, 
*		                      upon selection of a single image, the plugin will return it.
*		.fullSizeImage - whether to return the full size image, defaults to true. If this value
*		                 is true, width and height are ignored.
*		.width - width to resize image to (if one of height/width is 0, will resize to fit the
*		         other while keeping aspect ratio)
*		.height - height to resize image to
*		.quality - quality of resized image, defaults to 100
*/
ImagePicker.prototype.getPictures = function(success, fail, options) {
	if (!options) {
		options = {};
	}
	
	var params = {
		maximumImagesCount: options.maximumImagesCount ? options.maximumImagesCount : 15,
		fullSizeImage: (typeof options.fullSizeImage !== "undefined") ? options.fullSizeImage : 1,
		width: options.width ? options.width : 0,
		height: options.height ? options.height : 0,
		quality: options.quality ? options.quality : 100
	};

	return cordova.exec(success, fail, "ImagePicker", "getPictures", [params]);
};

window.imagePicker = new ImagePicker();
