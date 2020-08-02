package com.example.tasfia.map4;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.io.IOException;

/**
 * Created by tasfia on 11/30/17.
 */

class GetRoutesData extends AsyncTask<Object, String, String> {
    GoogleMap mMap;
    String url;
    String googleRoutesData;
    String duration, distance;
    LatLng latlng;
    int mode;


    @Override
    protected String doInBackground(Object... objects) {
        mMap = (GoogleMap)objects[0];
        url = (String) objects[1];
        latlng = (LatLng) objects[2];
        mode = (int) objects[3];

        DownloadURL downloadURL = new DownloadURL();
        try {
            googleRoutesData = downloadURL.readURL(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googleRoutesData;
    }

    @Override
    protected void onPostExecute(String s)
    {
        String[] routesList;
        DataParser parser = new DataParser();
        routesList = parser.parseRoutes(s);

        displayRoutes(routesList);

    }

    public void displayRoutes(String[] routesList)
    {
        int count = routesList.length;

        for(int i = 0; i < count; i++)
        {
            PolylineOptions options = new PolylineOptions();
            if(mode == 1)
            {
                options.color(Color.RED);
            }
            else if(mode == 2)
            {
                options.color(Color.YELLOW);
            }
            else if(mode == 3)
            {
                options.color(Color.GREEN);
            }
            else if(mode == 4)
            {
                options.color(Color.BLUE);
            }
            options.width(15);
            options.addAll(PolyUtil.decode(routesList[i]));

            mMap.addPolyline(options);
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }

}
