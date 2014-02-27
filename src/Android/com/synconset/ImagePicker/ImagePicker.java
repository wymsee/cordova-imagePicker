/**
 * An Internal Storage Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class ImagePicker extends CordovaPlugin {
    
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		if (action.equals("getPictures")) {
			Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
			intent.putExtra("MAX_IMAGES", 20);
    
            if (this.cordova != null) {
                this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
            }
		}
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    Log.d("PLUGIN", "result code = " + resultCode);
        if (resultCode == 1 && data != null) {
            ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILNAMES");
            for (int i = 0; i < fileNames.size(); i++) {
                Log.d("PLUGIN", fileNames.get(i));
            }
        }
	}
}
