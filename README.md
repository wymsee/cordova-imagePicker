cordova-imagePicker
===================

Cordova Plugin For Multiple Image Selection - currently implemented only for
iOS, Android coming soon.

This plugin uses the ELCImagePickerController, with slight modifications for the iOS image picker.

https://github.com/B-Sides/ELCImagePickerController

## Installing the plugin

The plugin conforms to the Cordova plugin specification, it can be installed
using the Cordova / Phonegap command line interface.

```
phonegap plugin add https://github.com/CSullivan102/cordova-imagePicker.git

cordova plugin add https://github.com/CSullivan102/cordova-imagePicker.git
```

## Using the plugin

The plugin creates the object `window.imagePicker` with the method `getPictures(success, fail, options)`

Example:
```javascript
window.imagePicker.getPictures(
	function(results) {
		for (var i = 0; i < results.length; i++) {
			console.log('Image URI: ' + results[i]);
		}
	}, function (error) {
		console.log('Error: ' + error);
	}, options
);
```

### Options

```javascript
options = {
	maximumImagesCount: int,
	// max images to be selected, defaults to 15. If this is set to 1, upon
	// selection of a single image, the plugin will return it.
	width: int,
	// width to resize image to (if one of height/width is 0, will resize 
	// to fit the other while keeping aspect ratio)
	height: int,
	// height to resize image to
	quality: int (0-100)
	// quality of resized image, defaults to 100
};
```

## License

The MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.