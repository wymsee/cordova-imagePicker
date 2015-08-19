package com.synconset;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class FileNameItem implements Parcelable{
	public String thumb;
	public String original;
	public Integer rotation;
	
	public FileNameItem(){
		super();
	}
	
	public FileNameItem(Parcel in) {
        super(); 
        readFromParcel(in);
    }

    public static final Parcelable.Creator<FileNameItem> CREATOR = new Parcelable.Creator<FileNameItem>() {
        public FileNameItem createFromParcel(Parcel in) {
            return new FileNameItem(in);
        }

        public FileNameItem[] newArray(int size) {

            return new FileNameItem[size];
        }

    };

    public void readFromParcel(Parcel in) {
    	thumb = in.readString();
        original = in.readString();
        rotation = in.readInt();
    }
    
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags ) {
		// TODO Auto-generated method stub
		dest.writeString(thumb);
		dest.writeString(original);
		dest.writeInt(rotation);
	}
	
	/*public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("thumb", thumb);
            obj.put("original", orginal);
            obj.put("rotation", rotation);
        } catch (JSONException e) {
            //trace("DefaultListItem.toString JSONException: "+e.getMessage());
        }
        return obj;
    }*/
}
