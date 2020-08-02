package com.example.tasfia.map4;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tasfia on 11/30/17.
 */

class DataParser {

    private HashMap<String, String> getDuration(JSONArray googleDirectionsJson)
    {
        HashMap<String, String> googleDirectionsMap = new HashMap<>();
        String duration = "";
        String distance = "";

        if(googleDirectionsJson != null) { //condition given

            Log.d("json response", googleDirectionsJson.toString());

            try {
                duration = googleDirectionsJson.getJSONObject(0).getJSONObject("duration").getString("text");
                distance = googleDirectionsJson.getJSONObject(0).getJSONObject("distance").getString("text");

                googleDirectionsMap.put("duration", duration);
                googleDirectionsMap.put("distance", distance);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return googleDirectionsMap;

    }

    private HashMap<String, String> getRoute(JSONArray googleDirectionsJson)
    {
        HashMap<String, String> googleDirectionsMap = new HashMap<>();
        String duration = "";
        String distance = "";

        Log.d("json response", googleDirectionsJson.toString());
        try {
            duration = googleDirectionsJson.getJSONObject(0).getJSONObject("duration").getString("text");
            distance = googleDirectionsJson.getJSONObject(0).getJSONObject("distance").getString("text");

            googleDirectionsMap.put("duration", duration);
            googleDirectionsMap.put("distance", distance);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return googleDirectionsMap;

    }

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson)
    {
        HashMap<String, String> googlePlacesMap = new HashMap<>();
        String placeName = "--NA--";
        String vicinity = "--NA--";
        String latitude = "";
        String longitude = "";
        String reference = "";

        Log.d("DataParser","jsonobject ="+googlePlaceJson.toString());

        try {
            if(!googlePlaceJson.isNull("name"))
            {
                placeName = googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull("vicinity"))
            {
                vicinity = googlePlaceJson.getString("vicinity");
            }
            latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");

            reference = googlePlaceJson.getString("reference");

            googlePlacesMap.put("place_name", placeName);
            googlePlacesMap.put("vicinity", vicinity);
            googlePlacesMap.put("lat", latitude);
            googlePlacesMap.put("lng", longitude);
            googlePlacesMap.put("reference", reference);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return googlePlacesMap;
    }

    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray)
    {
        int count = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap = null;

        for(int i = 0; i < count; i++)
        {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placesList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return placesList;
    }

    public List<HashMap<String,String>> parse(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject ;

        Log.d("json data", jsonData);

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPlaces(jsonArray);
    }


    public HashMap<String, String> parseDirections(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject ;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs"); // legs array
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getDuration(jsonArray);

    }


    public String[] parseRoutes(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject ;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps"); // legs array
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getPaths(jsonArray);

    }

    public String[] getPaths(JSONArray googleStepsJson)
    {
        int count = 0;

        try {
            count = googleStepsJson.length();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("getPaths","googleStepsJson.length() from null value\n");
        }

        String[] polylines = new String[count];

        for (int i = 0; i < count; i++) {
            try {
                polylines[i] = getPath(googleStepsJson.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return polylines;

    }

    public String getPath(JSONObject googlePathJson)
    {
        String polyline = "";
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }
}
