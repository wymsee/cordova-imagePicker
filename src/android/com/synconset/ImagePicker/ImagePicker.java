/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
			int desiredWidth = 0;
			int desiredHeight = 0;
			int quality = 100;
			if (this.params.has("maximumImagesCount")) {
				max = this.params.getInt("maximumImagesCount");
			}
			if (this.params.has("width")) {
				desiredWidth = this.params.getInt("width");
			}
			if (this.params.has("height")) {
				desiredWidth = this.params.getInt("height");
			}
			if (this.params.has("quality")) {
				quality = this.params.getInt("quality");
			}
			intent.putExtra("MAX_IMAGES", max);
			intent.putExtra("WIDTH", desiredWidth);
			intent.putExtra("HEIGHT", desiredHeight);
			intent.putExtra("QUALITY", quality);
			if (this.cordova != null) {
				this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
			}
		}
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			
			//HashMap<String, Object> result = new HashMap<String, Object>();
			
			//ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
			//ArrayList<String> fileThumbNames = data.getStringArrayListExtra("MULTIPLEFILETHUMBNAMES");
			
			ArrayList<FileNameItem> fileNameList = new ArrayList<FileNameItem>();
			
			Bundle bundle = data.getExtras();
			if( bundle != null ){
				 fileNameList = bundle.getParcelableArrayList("MULTIPLEFILENAMES");
			}
			
			Gson gson = new Gson();
			String str_json = gson.toJson(fileNameList);
			JSONArray res;
			try {
				res = new JSONArray(str_json);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				String error = "error";
				this.callbackContext.error(error);
				return;
			}
			//JsonArray res = gson.toJsonTree(fileNameList).getAsJsonArray();
			
			//result.put( "actual", fileNames );
			//result.put("thumb", fileThumbNames );
			
			//JSONObject res_object = new JSONObject(result);
			//JSONArray res = new JSONArray(fileNameList);
			
			//JSONArray res = new JSONArray(fileNames);
			/*JSONArray res = new JSONArray();
			try {
				res = new JSONArray( "[" + res_object.toString() + "]" );
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			this.callbackContext.success(res);
		} else if (resultCode == Activity.RESULT_CANCELED && data != null) {
			String error = data.getStringExtra("ERRORMESSAGE");
			this.callbackContext.error(error);
		} else if (resultCode == Activity.RESULT_CANCELED) {
			JSONArray res = new JSONArray();
			this.callbackContext.success(res);
		} else {
			this.callbackContext.error("No images selected");
		}
	}
}