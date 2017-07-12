/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import android.Manifest;
import android.content.pm.PackageManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;

public class ImagePicker extends CordovaPlugin {

	//required permissions
	String [] permissions = {
	        Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
	};

	private CallbackContext callbackContext;

	private Integer max = 20;
	private Integer desiredWidth = 0;
	private Integer desiredHeight = 0;
	private Integer quality = 100;

	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
	    this.callbackContext = callbackContext;

        JSONObject params = args.getJSONObject(0);

        if (action.equals("getPictures")) {

			if (params.has("maximumImagesCount")) {
				max = params.getInt("maximumImagesCount");
			}
			if (params.has("width")) {
				desiredWidth = params.getInt("width");
			}
			if (params.has("height")) {
				desiredHeight = params.getInt("height");
			}
			if (params.has("quality")) {
				quality = params.getInt("quality");
			}

			if(cordova.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && cordova.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                selectImages();
			} else {
			    getReadPermission();
			}
		}
		return true;
	}

    private void selectImages() {
	    /*
	    * ATENTION:
	    * Starts an extra activity because Matisse requires an Activity context
	    * and passing `this.cordova.getActivity()` context, it doesn't return activity result
	    * for this instance of CordovaPlugin
	    * */
        Intent intent = new Intent(this.cordova.getActivity(), ImgPickerActivity.class);
        intent.putExtra(ImgPickerActivity.MAX_IMAGES_KEY, max);
        intent.putExtra(ImgPickerActivity.WIDTH_KEY, desiredWidth);
        intent.putExtra(ImgPickerActivity.HEIGHT_KEY, desiredHeight);
        intent.putExtra(ImgPickerActivity.QUALITY_KEY, quality);
        this.cordova.startActivityForResult(this, intent, 0);
    }


    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK && data != null) {
			ArrayList<String> names = data.getStringArrayListExtra("MULTIPLEFILENAMES");
            JSONArray res = new JSONArray(names);
			this.callbackContext.success(res);
		} else {
			this.callbackContext.error("No images selected");
		}
	}


	protected void getReadPermission() {
    	cordova.requestPermissions(this, 0, permissions);
	}

	public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
	    for(int r:grantResults) {
	        if(r == PackageManager.PERMISSION_DENIED) {
				this.callbackContext.error("Permission denied!");
	            return;
	        }
	    }

		selectImages();
	}

}
