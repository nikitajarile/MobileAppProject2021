package com.example.logintask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkTask extends AsyncTask<Double,Void,String>{

    private static final String PLACES_BASE_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static final String LOCATION = "location";
    private static final String RADIUS = "radius";
    private static final int PROXIMITY_RADIUS = 10000;
    private static final String TYPES = "police";
    private static final String GOOGLE_BROWSER_API_KEY = "AIzaSyAVT9I2J6LzDOTwhJNbK347xZjc6Ginz8U";
    private static final String TAG = NetworkTask.class.getSimpleName();
    private double latitude;
    private double longitude;
    String id, place_id, placeName = null, reference, icon, vicinity = null;
    GoogleMap mMap;

    public NetworkTask(GoogleMap mMap) {
        this.mMap = mMap;
    }

    @Override
    protected String doInBackground(Double... doubles) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String placesJSONString = null;
        latitude = doubles[0];
        longitude = doubles[1];

        try {
            StringBuilder googlePlacesUrl =
                    new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            googlePlacesUrl.append("location=").append(latitude).append(",").append(longitude);
            googlePlacesUrl.append("&radius=").append(PROXIMITY_RADIUS);
            googlePlacesUrl.append("&types=").append(TYPES);
            googlePlacesUrl.append("&sensor=true");
            googlePlacesUrl.append("&key=" + GOOGLE_BROWSER_API_KEY);

            URL requestURL = new URL(googlePlacesUrl.toString());

            Log.d(TAG, requestURL.toString());

            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Get the InputStream.
            InputStream inputStream = urlConnection.getInputStream();

            Log.d(TAG, inputStream.toString());

            // Create a buffered reader from that input stream.
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Use a StringBuilder to hold the incoming response.
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                // Add the current line to the string.
                builder.append(line);

                // Since this is JSON, adding a newline isn't necessary (it won't
                // affect parsing) but it does make debugging a *lot* easier
                // if you print out the completed buffer for debugging.
                builder.append("\n");
            }

            if (builder.length() == 0) {
                // Stream was empty.  Exit without parsing.return null;
            }

            placesJSONString = builder.toString();
            Log.d(TAG, "placesJSONString : "+placesJSONString);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the connection and the buffered reader.
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return placesJSONString;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);


        try {
            JSONObject result = new JSONObject(s);
            JSONArray jsonArray = result.getJSONArray("results");

            Log.d(TAG, "JSON Array : "+jsonArray.toString());

            if(mMap != null) {
                mMap.clear();
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject place = jsonArray.getJSONObject(i);

                //id = place.getString("id");
                place_id = place.getString("place_id");
                if (!place.isNull("name")) {
                    placeName = place.getString("name");
                }
                if (!place.isNull("vicinity")) {
                    vicinity = place.getString("vicinity");
                }
                latitude = place.getJSONObject("geometry").getJSONObject("location")
                        .getDouble(String.valueOf("lat"));
                longitude = place.getJSONObject("geometry").getJSONObject("location")
                        .getDouble(String.valueOf("lng"));
                reference = place.getString("reference");
                icon = place.getString("icon");

                MarkerOptions markerOptions = new MarkerOptions();
                LatLng latLng = new LatLng(latitude, longitude);
                markerOptions.position(latLng);
                markerOptions.title(placeName + " : " + vicinity);

                mMap.addMarker(markerOptions);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera( CameraUpdateFactory.zoomTo( 13.0f ) );
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
