
var ApplicationData = Windows.Storage.ApplicationData;
var Imaging = Windows.Graphics.Imaging;

function getPictures(success, error, options) {
    var fileOpenPicker = new Windows.Storage.Pickers.FileOpenPicker();

    fileOpenPicker.fileTypeFilter.replaceAll(['.png', '.jpg', '.jpeg']);
    fileOpenPicker.viewMode = Windows.Storage.Pickers.PickerViewMode.thumbnail;
    fileOpenPicker.suggestedStartLocation = Windows.Storage.Pickers.PickerLocationId.picturesLibrary;

    function calculateScaledSize(size, max) {
        if (!max.height && !max.width) {
            return size;
        } else if (max.width <= max.height || !max.height) {
            return {
                width: max.width,
                height: (size.height / size.width) * max.width
            };
        } else if (max.height <= max.width || !max.width) {
            return {
                width: (size.width / size.height) * max.height,
                height: max.height
            };
        }
    }

    fileOpenPicker.pickMultipleFilesAsync().then(function(files) {
        var tempFolder = ApplicationData.current.temporaryFolder;
        var results = [];

        var promises = files.map(function(inputFile) {
            var outputFile;
            var decoder;
            var detachedPixelData;
            var outputStream;
            var inputStream;

            var bitmapProperties = new Imaging.BitmapPropertySet();
            var encoderId;
            var fileType = inputFile.fileType.toUpperCase();
            if (fileType === '.JPG' ||
                fileType === '.JPEG') {
                encoderId = Imaging.BitmapEncoder.jpegEncoderId;
                var compression = Math.min(100, Math.max(0, options.quality));
                compression = compression / 100.0;
                bitmapProperties.insert('ImageQuality', new Imaging.BitmapTypedValue(compression, Windows.Foundation.PropertyType.single));

            } else if (fileType === '.PNG') {
                encoderId = Imaging.BitmapEncoder.pngEncoderId;
            } else {
                throw new Error('Invalid filetype');
            }

            return inputFile.openAsync(Windows.Storage.FileAccessMode.read).then(function(stream) {
                inputStream = stream;
                return tempFolder.createFileAsync('tmpImage' + fileType, Windows.Storage.CreationCollisionOption.generateUniqueName);
            }.bind(this)).then(function(file) {
                outputFile = file;
                return outputFile.openAsync(Windows.Storage.FileAccessMode.readWrite);
            }).then(function(output) {
                outputStream = output;
                return Imaging.BitmapDecoder.createAsync(inputStream);
            }.bind(this)).then(function(bitmapDecoder) {
                decoder = bitmapDecoder;
                return decoder.getPixelDataAsync();
            }.bind(this)).then(function(pixelData) {
                detachedPixelData = pixelData.detachPixelData();
                return Imaging.BitmapEncoder.createAsync(encoderId, outputStream, bitmapProperties);
            }.bind(this)).then(function(encoder) {
                encoder.setPixelData(
                    decoder.bitmapPixelFormat, decoder.bitmapAlphaMode,
                    decoder.orientedPixelWidth, decoder.orientedPixelHeight, decoder.dpiX, decoder.dpiY,
                    detachedPixelData);

                var scaledSize = calculateScaledSize(
                    { width: decoder.orientedPixelWidth, height: decoder.orientedPixelHeight },
                    { width: options.width, height: options.height });

                encoder.bitmapTransform.scaledWidth = scaledSize.width;
                encoder.bitmapTransform.scaledHeight = scaledSize.height;

                return encoder.flushAsync();
            }.bind(this)).then(function() {
                results.push('ms-appdata:///temp/' + outputFile.name);

            }, function(err) {
                error();
            });
        });

        return Promise.all(promises).then(function() {
            success(results);
        }, error);

    }, error);
}

var ImagePicker = {
    getPictures: function(success, error, params) {
        var options = params[0];
        getPictures(success, error, options);
    }
};

require('cordova/exec/proxy').add('ImagePicker', ImagePicker);
