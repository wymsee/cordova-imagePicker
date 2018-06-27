/*global cordova,window,console*/
/**
 * An Image Picker plugin for Cordova
 *
 * Developed by Wymsee for Sync OnSet
 */

var ImagePicker = function() {

};

ImagePicker.prototype.OutputType = {
	FILE_URI: 0,
	BASE64_STRING: 1
};

ImagePicker.prototype.validateOutputType = function(options){
	var outputType = options.outputType;
	if(outputType){
		if(outputType !== this.OutputType.FILE_URI && outputType !== this.OutputType.BASE64_STRING){
			console.log('Invalid output type option entered. Defaulting to FILE_URI. Please use window.imagePicker.OutputType.FILE_URI or window.imagePicker.OutputType.BASE64_STRING');
			options.outputType = this.OutputType.FILE_URI;
		}
	}
};

ImagePicker.prototype.hasReadPermission = function(callback) {
  return cordova.exec(callback, null, "ImagePicker", "hasReadPermission", []);
};

ImagePicker.prototype.requestReadPermission = function(callback, failureCallback) {
  return cordova.exec(callback, failureCallback, "ImagePicker", "requestReadPermission", []);
};

/*
*	success - success callback
*	fail - error callback
*	options
*		.maximumImagesCount - max images to be selected, defaults to 15. If this is set to 1,
*		                      upon selection of a single image, the plugin will return it.
*		.width - width to resize image to (if one of height/width is 0, will resize to fit the
*		         other while keeping aspect ratio, if both height and width are 0, the full size
*		         image will be returned)
*		.height - height to resize image to
*		.quality - quality of resized image, defaults to 100
*       .outputType - type of output returned. defaults to file URIs.
*					  Please see ImagePicker.OutputType for available values.
*/
ImagePicker.prototype.getPictures = function(success, fail, options) {
	if (!options) {
		options = {};
	}

	this.validateOutputType(options);

	var params = {
		maximumImagesCount: options.maximumImagesCount ? options.maximumImagesCount : 15,
		width: options.width ? options.width : 0,
		height: options.height ? options.height : 0,
		quality: options.quality ? options.quality : 100,
		allow_video: options.allow_video ? options.allow_video : false,
		title: options.title ? options.title : 'Select an Album', // the default is the message of the old plugin impl
		message: options.message ? options.message : null, // the old plugin impl didn't have it, so passing null by default
		outputType: options.outputType ? options.outputType : this.OutputType.FILE_URI,
		disable_popover: options.disable_popover ? options.disable_popover : false // Disable the iOS popover as seen on iPad
	};

	return cordova.exec(success, fail, "ImagePicker", "getPictures", [params]);
};

window.imagePicker = new ImagePicker();
