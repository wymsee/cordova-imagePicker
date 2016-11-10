/**
 * Created by Christian on 02.09.2016.
 * christian@helbighof.de
 */

function resize(inputFile, height, width, quality) {
    var inputStream;
    var outputStream;
    var encoderId;
    var pixels;
    var pixelFormat;
    var alphaMode;
    var dpiX;
    var dpiY;
    var outputFilename;
    var file;
    var newWidth;
    var newHeight;

    return new WinJS.Promise(function (completeDispatch, errorDispatch, progressDispatch) {


        inputFile.openAsync(Windows.Storage.FileAccessMode.read)
            .then(function (stream) {
                inputStream = stream;
                return Windows.Graphics.Imaging.BitmapDecoder.createAsync(inputStream)
            }).then(function (decoder) {
                var transform = new Windows.Graphics.Imaging.BitmapTransform();
                newHeight = transform.scaledHeight = height;
                newWidth = transform.scaledWidth = width;
                if (height == null) {
                    newHeight = transform.scaledHeight = ((decoder.pixelHeight / decoder.pixelWidth) * width) || decoder.pixelHeight;
                }
                if (width == null) {
                    newWidth = transform.scaledHeight = ((decoder.pixelWidth / decoder.pixelHeight) * height) || decoder.pixelWidth;
                }
                transform.interpolationMode = Windows.Graphics.Imaging.BitmapInterpolationMode.fant;
                pixelFormat = decoder.bitmapPixelFormat;
                alphaMode = decoder.bitmapAlphaMode;
                dpiX = (decoder.dpiX / 100) * quality;
                dpiY = (decoder.dpiY / 100) * quality;
                DisplayWidthNonScaled = decoder.orientedPixelWidth;
                DisplayHeightNonScaled = decoder.orientedPixelHeight;

                return decoder.getPixelDataAsync(
                    pixelFormat,
                    alphaMode,
                    transform,
                    Windows.Graphics.Imaging.ExifOrientationMode.respectExifOrientation,
                    Windows.Graphics.Imaging.ColorManagementMode.colorManageToSRgb
                );
            })

            .then(function (pixelProvider) {
                pixels = pixelProvider.detachPixelData();

                return Windows.Storage.ApplicationData.current.temporaryFolder.createFileAsync(new Date().getTime() + inputFile.fileType, Windows.Storage.CreationCollisionOption.replaceExisting)
                    .then(function (_file) {
                        file = _file
                        outputFilename = file.name;
                        switch (inputFile.fileType) {
                            case ".jpg":
                                encoderId = Windows.Graphics.Imaging.BitmapEncoder.jpegEncoderId;
                                break;
                            case ".bmp":
                                encoderId = Windows.Graphics.Imaging.BitmapEncoder.bmpEncoderId;
                                break;
                            case ".png":
                            default:
                                encoderId = Windows.Graphics.Imaging.BitmapEncoder.pngEncoderId;
                                break;
                        }


                        return file.openAsync(Windows.Storage.FileAccessMode.readWrite);
                    });




            }).then(function (stream) {
                outputStream = stream;
                outputStream.size = 0;
                return Windows.Graphics.Imaging.BitmapEncoder.createAsync(encoderId, outputStream);
            }).then(function (encoder) {
                encoder.setPixelData(
                    pixelFormat,
                    alphaMode,
                    newWidth,
                    newHeight,
                    dpiX,
                    dpiY,
                    pixels
                );

                return encoder.flushAsync();
            }).then(function () {
                WinJS.log && WinJS.log("Successfully saved a copy: " + outputFilename, "sample", "status");
                completeDispatch([file.path]);
            }, function (error) {
                WinJS.log && WinJS.log("Failed to update file: " + error.message, "sample", "error");
                resetSessionState();
                resetPersistedState();
                errorDispatch("Failed to create image");
            }).done(function () {
                inputStream && inputStream.close();
                outputStream && outputStream.close();
            });


    })


};

var ImageOpener = {
    getPictures: function(win, fail, args){
        var maxImage = args[0].maximumImagesCount || 1;
        var height = args[0].height || null;
        var width = args[0].width || null;
        var quality = args[0].quality || 100;

        var openPicker = new Windows.Storage.Pickers.FileOpenPicker();
        openPicker.viewMode = Windows.Storage.Pickers.PickerViewMode.thumbnail;
        openPicker.suggestedStartLocation = Windows.Storage.Pickers.PickerLocationId.picturesLibrary;
        openPicker.fileTypeFilter.replaceAll([".png", ".jpg", ".jpeg"]);


        var originalWidth;
        var originalHeight;
        var encoder;
        var file;
        var fileStream;
        var memStream = new Windows.Storage.Streams.InMemoryRandomAccessStream();

        if (maxImage == 1) {
            openPicker.pickSingleFileAsync().then(function (inputFile) {
                if (inputFile != null) {

                    win(resize(inputFile, height, width, quality));

                } else {
                    fail("Operation cancelled.");
                }
            }, function (error) {
                fail(error);
            });
        }
        else {
            openPicker.pickMultipleFilesAsync().then(function (files) {
                var promiseArray = files.map(function(file){
                    return resize(file,height,width,quality);
                });
                WinJS.Promise.join(promiseArray)
                    .then(function(success){
                        win(success)
                    }, function(error){
                        fail(error);
                    });
            }, function (error) {
                fail(error);
            })
        }

    }
};

require("cordova/exec/proxy").add("ImagePicker", ImageOpener);