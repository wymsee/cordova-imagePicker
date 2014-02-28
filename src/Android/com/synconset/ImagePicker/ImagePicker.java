/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

public class ImagePicker extends CordovaPlugin {
    public static String TAG = "ImagePicker";
    
    private CallbackContext callbackContext;
    private JSONObject params;
    
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
	    this.callbackContext = callbackContext;
	    this.params = args.getJSONObject(0);
		if (action.equals("getPictures")) {
			Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
			int max = 20;
			if (this.params.has("maximumImagesCount")) {
			    max = this.params.getInt("maximumImagesCount");
			}
			intent.putExtra("MAX_IMAGES", max);
            if (this.cordova != null) {
                this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
            }
		}
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
            JSONArray res = new JSONArray();
            try {
            	for (int i = 0; i < fileNames.size(); i++) {
            		File file = new File(fileNames.get(i));
                	Bitmap bmp = this.getBitmap(file);
                	int width = bmp.getWidth();
            		int height = bmp.getHeight();
                	int desiredWidth = 0;
                	int desiredHeight = 0;
                	if (this.params.has("width")) {
                	    desiredWidth = this.params.getInt("width");
                	}
                	if (this.params.has("height")) {
                	    desiredHeight = this.params.getInt("height");
                	}
                	float widthScale = 1.0f;
                	float heightScale = 1.0f;
                	float scale = 1.0f;
                	if (desiredWidth > 0 || desiredHeight > 0) {
                		if (desiredHeight == 0 && desiredWidth < width) {
                			scale = (float)desiredWidth/width;
                		} else if (desiredWidth == 0 && desiredHeight < height) {
                			scale = (float)desiredHeight/height;
                		} else {
                			if (desiredWidth > 0 && desiredWidth < width) {
                				widthScale = (float)desiredWidth/width;
                			}
                			if (desiredHeight > 0 && desiredHeight < height) {
                				heightScale = (float)desiredHeight/height;
                			}
                			if (widthScale < heightScale) {
                			    scale = widthScale;
                			} else {
                			    scale = heightScale;
                			}
                		}
                	}
                	if (scale < 1) {
                		bmp = this.getResizedBitmap(bmp, scale);
                	}
                	file = this.storeImage(bmp, file.getName());
                    res.put(Uri.fromFile(file).toString());
                }
                this.callbackContext.success(res);
            } catch(IOException e) {
            	this.callbackContext.error("There was an error importing pictures");
            } catch (JSONException e) {
            	this.callbackContext.error("There was an error importing pictures");
			}
        } else if (resultCode == Activity.RESULT_CANCELED) {
            JSONArray res = new JSONArray();
            this.callbackContext.success(res);
        } else {
            this.callbackContext.error("No images selected");
        }
	}
	

    /*
    * The following functions are originally from
    * https://github.com/raananw/PhoneGap-Image-Resizer
    * 
    * They have been modified by Andrew Stephan for Sync OnSet
    *
	* The software is open source, MIT Licensed.
    * Copyright (C) 2012, webXells GmbH All Rights Reserved.
    */
	private File storeImage(Bitmap bmp, String fileName) throws JSONException, IOException {
        int quality = 100;
        if (this.params.has("quality")) {
            quality = this.params.getInt("quality");
        }
        int index = fileName.lastIndexOf('.');
        String name = fileName.substring(0, index);
        String ext = fileName.substring(index);
        File file = File.createTempFile(name, ext);
        OutputStream outStream = new FileOutputStream(file);
        if (ext.compareToIgnoreCase(".png") == 0) {
            bmp.compress(Bitmap.CompressFormat.PNG, quality, outStream);
        } else {
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, outStream);
        }
		outStream.flush();
		outStream.close();
		return file;
	}

	private Bitmap getResizedBitmap(Bitmap bm, float factor) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(factor, factor);
		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		return resizedBitmap;
	}

	private Bitmap getBitmap(File file) throws IOException {
		Bitmap bmp;
        bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        if (bmp == null) {
            throw new IOException("The image file could not be opened.");
        }
		return bmp;
	}
}