/**
 * An Image Picker plugin for Cordova
 * 
 * Developed by Wymsee for Sync OnSet
 */

var ImagePicker = function() {

};

ImagePicker.prototype.getPictures = function() {

}

cordova.addContstructor(function() {
	window.imagePicker = new ImagePicker();
});