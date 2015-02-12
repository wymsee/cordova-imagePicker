/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class ImagePicker extends CordovaPlugin {
	public static String TAG = "ImagePicker";
	 
	private CallbackContext callbackContext;
	private JSONObject params;
	int desiredWidth = 0;
	int desiredHeight = 0;
	int quality = 100;
	public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
		 this.callbackContext = callbackContext;
		 this.params = args.getJSONObject(0);
		if (action.equals("getPictures")) {
			Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
			int max = 20;

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

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
    
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
    
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
    
        return inSampleSize;
    }

    private int calculateNextSampleSize(int sampleSize) {
        double logBaseTwo = (int)(Math.log(sampleSize) / Math.log(2));
        return (int)Math.pow(logBaseTwo + 1, 2);
    }
    
    private float calculateScale(int width, int height) {
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
        
        return scale;
    }

    private Bitmap tryToGetBitmap(File file, BitmapFactory.Options options, int rotate, boolean shouldScale) throws IOException, OutOfMemoryError {
        Bitmap bmp;
        if (options == null) {
            bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
        } else {
            bmp = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        }
        if (bmp == null) {
            throw new IOException("The image file could not be opened.");
        }
        if (options != null && shouldScale) {
            float scale = calculateScale(options.outWidth, options.outHeight);
            bmp = this.getResizedBitmap(bmp, scale);
        }
        if (rotate != 0) {
            Matrix matrix = new Matrix();
            matrix.setRotate(rotate);
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        }
        return bmp;
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

    private File storeImage(Bitmap bmp, String fileName) throws IOException {
        int index = fileName.lastIndexOf('.');
        String name = "Temp_" + fileName.substring(0, index);
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

    private ArrayList<String> ResizeImages(ArrayList<String> fileNames) throws IOException
    {
    	ArrayList<String> al = new ArrayList<String>();
    	Bitmap bmp = null;
    	int rotate = 0;
    	for(int i=0; i<fileNames.size();i++)
    	{
    		String filename=fileNames.get(i);
    		filename = filename.replaceAll("file://", "");
    		File file = new File(filename);
		    BitmapFactory.Options options = new BitmapFactory.Options();
		    options.inSampleSize = 1;
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(file.getAbsolutePath(), options);
		    int width = options.outWidth;
		    int height = options.outHeight;
		    float scale = calculateScale(width, height);
		    if (scale < 1) {
		        int finalWidth = (int)(width * scale);
		        int finalHeight = (int)(height * scale);
		        int inSampleSize = calculateInSampleSize(options, finalWidth, finalHeight);
		        options = new BitmapFactory.Options();
		        options.inSampleSize = inSampleSize;
		        try {
		            try {
						bmp = this.tryToGetBitmap(file, options, rotate, true);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        } catch (OutOfMemoryError e) {
		            options.inSampleSize = calculateNextSampleSize(options.inSampleSize);
		            try {
		                try {
							bmp = this.tryToGetBitmap(file, options, rotate, false);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		            } catch (OutOfMemoryError e2) {
		                throw new IOException("Unable to load image into memory.");
		            }
		        }
		    } else {
		        try {
		            bmp = this.tryToGetBitmap(file, null, rotate, false);
		        } catch(OutOfMemoryError e) {
		            options = new BitmapFactory.Options();
		            options.inSampleSize = 2;
		            try {
		                bmp = this.tryToGetBitmap(file, options, rotate, false);
		            } catch(OutOfMemoryError e2) {
		                options = new BitmapFactory.Options();
		                options.inSampleSize = 4;
		                try {
		                    bmp = this.tryToGetBitmap(file, options, rotate, false);
		                } catch (OutOfMemoryError e3) {
		                    throw new IOException("Unable to load image into memory.");
		                }
		            }
		        }
		    }

		    file = this.storeImage(bmp, file.getName());
		    al.add(Uri.fromFile(file).toString());
    	}
    	return al;
    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && data != null) {
			ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
			ArrayList<String> newfiles=null;
			try {
				newfiles=ResizeImages(fileNames);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			JSONArray res = new JSONArray(newfiles);
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