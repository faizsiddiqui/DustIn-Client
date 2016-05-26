package com.major.dustinclient.model;

import android.content.Context;

import com.major.dustinclient.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Faiz on 25-05-2016.
 */
public class Location {

    Context context;
    Double latitude;
    Double longitude;

    public Location(Context context) {
        this.context = context;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(getContext().getResources().getString(R.string.LATITUDE), getLatitude());
            jsonObject.put(getContext().getResources().getString(R.string.LONGITUDE), getLongitude());
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

}
