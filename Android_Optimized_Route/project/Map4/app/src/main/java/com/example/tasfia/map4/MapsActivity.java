package com.example.tasfia.map4;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMarkerDragListener {

    private GoogleMap mMap;
    HashMap< String,ArrayList<UserInfo> > info;
    HashMap< String,ArrayList<UserInfo> > finalInfo;

    ArrayList <ArrayList<UserInfo>> road1;
    ArrayList <ArrayList<UserInfo>> road2;
    ArrayList <ArrayList<UserInfo>> road3;
    ArrayList <ArrayList<UserInfo>> road4;

    int change;
    static final Double EARTH_RADIUS = 6371.00;



    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;
    public static final int REQUEST_LOCATION_CODE = 99;
    int PROXIMITY_RADIUS = 10000;
    double latitude, longitude;
    double end_latitude= 23.810332;
    double end_longitude = 90.412518;

    private FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            checkLocationPermission();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        info = new HashMap< String,ArrayList<UserInfo> >();
        finalInfo = new HashMap< String,ArrayList<UserInfo> >();

        road1 = new ArrayList<ArrayList<UserInfo>>();
        road2 = new ArrayList<ArrayList<UserInfo>>();
        road3 = new ArrayList<ArrayList<UserInfo>>();
        road4 = new ArrayList<ArrayList<UserInfo>>();

        change = 1;

    }

    public Boolean checkLocationPermission()
    {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch(requestCode)
        {
            case REQUEST_LOCATION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    //permission is granted
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        if(client == null)
                        {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }
                else
                {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
                }
        }
    }

    protected synchronized void buildGoogleApiClient()
    {
        client = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        client.connect();
    }

    public void onClick(View v)
    {
        Object dataTransfer[];
        String url="";

        switch(v.getId()) {
            case R.id.B_search:
                EditText tf_location = (EditText) findViewById(R.id.TF_location);
                String location = tf_location.getText().toString();
                List<Address> addressList;

                if (!location.equals("")) {
                    Geocoder geocoder = new Geocoder(this);

                    try {
                        addressList = geocoder.getFromLocationName(location, 5);

                        if(addressList != null)
                        {
                            for(int i = 0;i<addressList.size();i++)
                            {
                                LatLng latLng = new LatLng(addressList.get(i).getLatitude() , addressList.get(i).getLongitude());
                                end_latitude = addressList.get(i).getLatitude();
                                end_longitude = addressList.get(i).getLongitude();
                                dataTransfer = new Object[3];
                                url = getDirectionsUrl(new LatLng(latitude,longitude),new LatLng(end_latitude,end_longitude));
                                GetDirectionsData getDirectionsData = new GetDirectionsData();
                                //GetRoutesData getRoutesData = new GetRoutesData();
                                dataTransfer[0] = mMap;
                                dataTransfer[1] = url;
                                dataTransfer[2] = new LatLng(end_latitude, end_longitude);

                                //Colour(new LatLng(latitude,longitude), new LatLng(end_latitude,end_longitude),4);
                                getDirectionsData.execute(dataTransfer);
                                //getRoutesData.execute(dataTransfer);

                                if(i == 0)
                                {
                                    break;
                                }

                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case R.id.showRoute:
                try {
                    Colour(new LatLng(latitude, longitude), new LatLng(end_latitude, end_longitude), 4);
                }catch(Exception e)
                {
                    Log.e("OnCLick","could not execute colour route\n");
                }
                break;
        }
    }

    private void changeState()
    {
        if(change == 1)
        {
            change = 2;
        }
        else if(change == 2)
        {
            change = 1;
        }

        Log.d("change","state = " + change + '\n');

        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users");
        db.removeValue();

        road1 = new ArrayList<ArrayList<UserInfo>>();
        road2 = new ArrayList<ArrayList<UserInfo>>();
        road3 = new ArrayList<ArrayList<UserInfo>>();
        road4 = new ArrayList<ArrayList<UserInfo>>();

        saveUserInfo1(change);
        saveUserInfo2(change);
        saveUserInfo3(change);
        saveUserInfo4(change);
        saveUserInfo5(change);

        waitTimer();

    }


    private void waitTimer() {
        new CountDownTimer(3000, 1000) { // 3 seconds timer

            public void onTick(long millisUntilFinished) {
                // Do nothing
            }

            public void onFinish() {

                getSpeeds("User1");
                getSpeeds("User2");
                getSpeeds("User3");
                getSpeeds("User4");
                getSpeeds("User5");

                SortAccordingToRoad();

                mMap.clear();

                ColourRoute("Road1");
                ColourRoute("Road2");
                ColourRoute("Road3");
                ColourRoute("Road4");

                this.cancel();

            }

        }.start();
    }

    private void SortAccordingToRoad()
    {
        try {
            for (Map.Entry<String, ArrayList<UserInfo>> entry : info.entrySet()) {

                ArrayList<UserInfo> temp = entry.getValue();
                if (temp != null) {
                    if (temp.get(0).roadID == 1) {
                        road1.add(temp);
                    } else if (temp.get(0).roadID == 2) {
                        road2.add(temp);
                    } else if (temp.get(0).roadID == 3) {
                        road3.add(temp);
                    } else if (temp.get(0).roadID == 4) {
                        road4.add(temp);
                    }
                }
                //System.out.println(entry.getKey() + ":" + entry.getValue().toString());
            }
            Log.d("sorting", "road1 size: " + road1.size());
            Log.d("sorting", "road2 size: " + road2.size());
            Log.d("sorting", "road3 size: " + road3.size());
            Log.d("sorting", "road4 size: " + road4.size());
        }catch(Exception e)
        {
            Log.e("Sorting","error in sorting to roads " + e + '\n');
        }

        ArrayList <UserInfo> finalArray1;
        ArrayList <UserInfo> finalArray2;
        ArrayList <UserInfo> finalArray3;
        ArrayList <UserInfo> finalArray4;

        try {

            if (road1.size() > 1) {
                for (int i = 0; i < road1.get(0).size(); i++) {
                    double sum = road1.get(0).get(i).speed;
                    for (int j = 1; j < road1.get(0).size(); j++) {
                        sum += road1.get(j).get(i).speed;
                    }

                    double avgSpeed = sum / road1.size();
                    road1.get(0).get(i).speed = avgSpeed;
                }

                ArrayList temp = road1.get(0);
                finalArray1 = temp;
            } else {
                finalArray1 = road1.get(0);
            }

            if (road2.size() > 1) {

                for (int i = 0; i < road2.get(0).size(); i++) {
                    double sum = road2.get(0).get(i).speed;
                    for (int j = 1; j < road2.size(); j++) {
                        sum += road2.get(j).get(i).speed;
                    }

                    double avgSpeed = sum / road2.size();
                    road2.get(0).get(i).speed = avgSpeed;
                }

                ArrayList temp = road2.get(0);
                finalArray2 = temp;
            } else {
                finalArray2 = road2.get(0);
            }

            if (road3.size() > 1) {
                for (int i = 0; i < road3.get(0).size(); i++) {
                    double sum = road3.get(0).get(i).speed;
                    for (int j = 1; j < road3.get(0).size(); j++) {
                        sum += road3.get(j).get(i).speed;
                    }

                    double avgSpeed = sum / road3.size();
                    road3.get(0).get(i).speed = avgSpeed;
                }

                ArrayList temp = road3.get(0);
                finalArray3 = temp;
            } else {
                finalArray3 = road3.get(0);
            }

            if (road4.size() > 1) {
                for (int i = 0; i < road4.get(0).size(); i++) {
                    double sum = road4.get(0).get(i).speed;
                    for (int j = 1; j < road3.get(0).size(); j++) {
                        sum += road4.get(j).get(i).speed;
                    }

                    double avgSpeed = sum / road4.size();
                    road4.get(0).get(i).speed = avgSpeed;
                }

                ArrayList temp = road4.get(0);
                finalArray4 = temp;
            } else {
                finalArray4 = road4.get(0);
            }

            Log.d("sorting", "finalArray1 size: " + finalArray1.size());
            Log.d("sorting", "finalArray2 size: " + finalArray2.size());
            Log.d("sorting", "finalArray3 size: " + finalArray3.size());
            Log.d("sorting", "finalArray4 size: " + finalArray4.size());

            finalInfo.put("Road1",finalArray1);
            finalInfo.put("Road2",finalArray2);
            finalInfo.put("Road3",finalArray3);
            finalInfo.put("Road4",finalArray4);

        }catch(Exception e)
        {
            Log.e("Sorting","error in finding avg speed " + e + '\n');
        }



        /*for(int i=0;i<finalArray1.size();i++)
        {
            Log.d("doubleuser","doubleuser speed1: "+ finalArray1.get(i).speed + '\n');
        }

        for(int i=0;i<finalArray2.size();i++)
        {
            Log.d("doubleuser","doubleuser speed2: "+ finalArray2.get(i).speed + '\n');
        }
        for(int i=0;i<finalArray3.size();i++)
        {
            Log.d("doubleuser","doubleuser speed3: "+ finalArray3.get(i).speed + '\n');
        }*/

        try {
            for (int i = 0; i < finalInfo.get("Road1").size(); i++) {
                Log.d("doubleuser", "doubleuser speed1: " + finalInfo.get("Road1").get(i).speed + '\n');
            }

            for (int i = 0; i < finalInfo.get("Road2").size(); i++) {
                Log.d("doubleuser", "doubleuser speed2: " + finalInfo.get("Road2").get(i).speed + '\n');
            }
            for (int i = 0; i < finalInfo.get("Road3").size(); i++) {
                Log.d("doubleuser", "doubleuser speed3: " + finalInfo.get("Road3").get(i).speed + '\n');
            }
            for (int i = 0; i < finalInfo.get("Road4").size(); i++) {
                Log.d("doubleuser", "doubleuser speed4: " + finalInfo.get("Road4").get(i).speed + '\n');
            }
        }catch(Exception e)
        {
            Log.e("sorting","error in printing " + e + '\n');
        }

    }


    private void Colour(LatLng latlonSrc, LatLng latlonDest,int mode)
    {
        Object dataTransfer[];
        String url = "";

        dataTransfer = new Object[4];
        url = getDirectionsUrl(latlonSrc, latlonDest);
        GetRoutesData getRoutesData = new GetRoutesData();
        dataTransfer[0] = mMap;
        dataTransfer[1] = url;
        dataTransfer[2] = new LatLng(latlonDest.latitude, latlonDest.longitude);
        dataTransfer[3] = mode;

        getRoutesData.execute(dataTransfer);
    }

    private void ColourRoute(String name)
    {
        ArrayList <UserInfo> user = finalInfo.get(name);

        if(user != null) {
            for (int i = 1; i < user.size(); i++) {
                if (user.get(i).speed >= 0 && user.get(i).speed <= 5) {
                    double latsrc = user.get(i).latitude;
                    double lonsrc = user.get(i).longitude;
                    LatLng dest = new LatLng(latsrc, lonsrc);

                    double latdest = user.get(i - 1).latitude;
                    double londest = user.get(i - 1).longitude;
                    LatLng src = new LatLng(latdest, londest);

                    Colour(src, dest, 1);
                }

                if (user.get(i).speed > 5 && user.get(i).speed <= 10) {
                    double latsrc = user.get(i).latitude;
                    double lonsrc = user.get(i).longitude;
                    LatLng dest = new LatLng(latsrc, lonsrc);

                    double latdest = user.get(i - 1).latitude;
                    double londest = user.get(i - 1).longitude;
                    LatLng src = new LatLng(latdest, londest);

                    Colour(src, dest, 2);
                }

                if (user.get(i).speed > 10) {
                    double latsrc = user.get(i).latitude;
                    double lonsrc = user.get(i).longitude;
                    LatLng dest = new LatLng(latsrc, lonsrc);

                    double latdest = user.get(i - 1).latitude;
                    double londest = user.get(i - 1).longitude;
                    LatLng src = new LatLng(latdest, londest);

                    Colour(src, dest, 3);
                }

            }
        }
    }

    private String getDirectionsUrl(LatLng latlonSrc, LatLng latlonDest)
    {
        StringBuilder googleDirectionsUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?");

        googleDirectionsUrl.append("origin="+latlonSrc.latitude+","+latlonSrc.longitude);
        googleDirectionsUrl.append("&destination="+latlonDest.latitude+","+latlonDest.longitude);
        googleDirectionsUrl.append("&key="+"AIzaSyBBNCP4tm6zshsn9-rKCFOik7_s2qkR5PM");

        return googleDirectionsUrl.toString();
    }


    private void getSpeeds(String name)
    {
        ArrayList user = info.get(name);
        ArrayList temp = new ArrayList();

        if(user != null) {
            for (int i = 1; i < user.size(); i++) {
                UserInfo second = (UserInfo) user.get(i);
                UserInfo first = (UserInfo) user.get(i - 1);

                double latNew = second.latitude;
                double lonNew = second.longitude;
                double latOld = first.latitude;
                double lonOld = first.longitude;

                double speed = calculateVelocity(latNew, lonNew, latOld, lonOld);
                temp.add(speed);
                ((UserInfo) user.get(i)).speed = speed;

            }

            info.put(name, user);

        }

    }

    private double calculateVelocity(double latNew, double lonNew, double latOld, double lonOld) {


        double distance = CalculationByDistance(latNew, lonNew, latOld, lonOld);
        double distanceInMeter = distance*1000.0;

        //Toast.makeText(getApplicationContext(), "\nDistance is: " + distMeter, Toast.LENGTH_LONG).show();


        double time = 10.0;
        double speed = distanceInMeter/time;


        //Toast.makeText(getApplicationContext(), "Distance is: "+distanceInMeter+"\nSpeed is: "+speed +" m/s", Toast.LENGTH_LONG).show();
        //speeds.add(speed);

        return speed;

    }

    public double CalculationByDistance(double lat1, double lon1, double lat2, double lon2) {

        double Radius = EARTH_RADIUS;
        double dLat = Math.toRadians(lat2-lat1);
        double dLon = Math.toRadians(lon2-lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Radius * c;
    }

    private void saveUserInfo1(int c) {


        DatabaseReference mDatabase1 = FirebaseDatabase.getInstance().getReference().child("Users").child("User1");
        //VALUES OF USER 1 WHO TRAVELS FROM COLLEGE OF HOME ECONOMICS ROAD TOWARDS POLASHI MOR

        String name1 = "User1";

        if(c == 1) {

            double lat1 = 23.738705;
            double lon1 = 90.390864;
            String time1 = "2017.12.14.15.22.10"; //time format "YYYY.MM.DD.HH.MM.SS"


            String key = mDatabase1.push().getKey();
            UserInfo user1 = new UserInfo(key, name1, lat1, lon1, time1);
            user1.roadID = 1;
            mDatabase1.child(key).setValue(user1);


            double lat2 = 23.736813;
            double lon2 = 90.389741;
            String time2 = "2017.12.14.15.22.20";

            key = mDatabase1.push().getKey();
            UserInfo user2 = new UserInfo(key, name1, lat2, lon2, time2);
            mDatabase1.child(key).setValue(user2);

            double lat3 = 23.735504;
            double lon3 = 90.388810;
            String time3 = "2017.12.14.15.22.30";

            key = mDatabase1.push().getKey();
            UserInfo user3 = new UserInfo(key, name1, lat3, lon3, time3);
            mDatabase1.child(key).setValue(user3);


            double lat4 = 23.734372;
            double lon4 = 90.387879;
            String time4 = "2017.12.14.15.22.40";

            key = mDatabase1.push().getKey();
            UserInfo user4 = new UserInfo(key, name1, lat4, lon4, time4);
            mDatabase1.child(key).setValue(user4);


            double lat5 = 23.732159;
            double lon5 = 90.386953;
            String time5 = "2017.12.14.15.22.50";

            key = mDatabase1.push().getKey();
            UserInfo user5 = new UserInfo(key, name1, lat5, lon5, time5);
            mDatabase1.child(key).setValue(user5);


            double lat6 = 23.730500;
            double lon6 = 90.387221;
            String time6 = "2017.12.14.15.23.00";

            key = mDatabase1.push().getKey();
            UserInfo user6 = new UserInfo(key, name1, lat6, lon6, time6);
            mDatabase1.child(key).setValue(user6);


            double lat7 = 23.729543;
            double lon7 = 90.387654;
            String time7 = "2017.12.14.15.23.10";

            key = mDatabase1.push().getKey();
            UserInfo user7 = new UserInfo(key, name1, lat7, lon7, time7);
            mDatabase1.child(key).setValue(user7);


            double lat8 = 23.728912;
            double lon8 = 90.388190;
            String time8 = "2017.12.14.15.23.20";

            key = mDatabase1.push().getKey();
            UserInfo user8 = new UserInfo(key, name1, lat8, lon8, time8);
            mDatabase1.child(key).setValue(user8);


            double lat9 = 23.728492;
            double lon9 = 90.388573;
            String time9 = "2017.12.14.15.23.30";

            key = mDatabase1.push().getKey();
            UserInfo user9 = new UserInfo(key, name1, lat9, lon9, time9);
            mDatabase1.child(key).setValue(user9);


            double lat10 = 23.728164;
            double lon10 = 90.388841;
            String time10 = "2017.12.14.15.23.40";

            key = mDatabase1.push().getKey();
            UserInfo user10 = new UserInfo(key, name1, lat10, lon10, time10);
            mDatabase1.child(key).setValue(user10);

        }

        else if(c == 2)
        {
            double lat1 = 23.729306;
            double lon1 = 90.387865;
            String time1 = "2017.12.14.15.22.10"; //time format "YYYY.MM.DD.HH.MM.SS"


            String key = mDatabase1.push().getKey();
            UserInfo user1 = new UserInfo(key, name1, lat1, lon1, time1);
            user1.roadID = 1;
            mDatabase1.child(key).setValue(user1);


            double lat2 = 23.729268;
            double lon2 = 90.387891;
            String time2 = "2017.12.14.15.22.20";

            key = mDatabase1.push().getKey();
            UserInfo user2 = new UserInfo(key, name1, lat2, lon2, time2);
            mDatabase1.child(key).setValue(user2);

            double lat3 = 23.729227;
            double lon3 = 90.387926;
            String time3 = "2017.12.14.15.22.30";

            key = mDatabase1.push().getKey();
            UserInfo user3 = new UserInfo(key, name1, lat3, lon3, time3);
            mDatabase1.child(key).setValue(user3);


            double lat4 = 23.729217;
            double lon4 = 90.387939;
            String time4 = "2017.12.14.15.22.40";

            key = mDatabase1.push().getKey();
            UserInfo user4 = new UserInfo(key, name1, lat4, lon4, time4);
            mDatabase1.child(key).setValue(user4);


            double lat5 = 23.729184;
            double lon5 = 90.387961;
            String time5 = "2017.12.14.15.22.50";

            key = mDatabase1.push().getKey();
            UserInfo user5 = new UserInfo(key, name1, lat5, lon5, time5);
            mDatabase1.child(key).setValue(user5);


            double lat6 = 23.729160;
            double lon6 = 90.387985;
            String time6 = "2017.12.14.15.23.00";

            key = mDatabase1.push().getKey();
            UserInfo user6 = new UserInfo(key, name1, lat6, lon6, time6);
            mDatabase1.child(key).setValue(user6);


            double lat7 = 23.728948;
            double lon7 = 90.388167;
            String time7 = "2017.12.14.15.23.10";

            key = mDatabase1.push().getKey();
            UserInfo user7 = new UserInfo(key, name1, lat7, lon7, time7);
            mDatabase1.child(key).setValue(user7);


            double lat8 = 23.728787;
            double lon8 = 90.388312;
            String time8 = "2017.12.14.15.23.20";

            key = mDatabase1.push().getKey();
            UserInfo user8 = new UserInfo(key, name1, lat8, lon8, time8);
            mDatabase1.child(key).setValue(user8);


            double lat9 = 23.728673;
            double lon9 = 90.388426;
            String time9 = "2017.12.14.15.23.30";

            key = mDatabase1.push().getKey();
            UserInfo user9 = new UserInfo(key, name1, lat9, lon9, time9);
            mDatabase1.child(key).setValue(user9);


            double lat10 = 23.728508;
            double lon10 = 90.388577;
            String time10 = "2017.12.14.15.23.40";

            key = mDatabase1.push().getKey();
            UserInfo user10 = new UserInfo(key, name1, lat10, lon10, time10);
            mDatabase1.child(key).setValue(user10);

        }
        else if(c == 3)
        {

        }

    }


    private void saveUserInfo2(int c) {

        DatabaseReference mDatabase2 = FirebaseDatabase.getInstance().getReference().child("Users").child("User2");

        //VALUES OF USER 2 WHO TRAVELS FROM AZIMPUR ROAD TOWARDS POLASHI MOR
        String name1 = "User2";

        if(c == 1) {

            double lat1 = 23.726843;
            double lon1 = 90.384984;
            String time1 = "2017.12.14.15.22.10"; //time format "YYYY.MM.DD.HH.MM.SS"

            String key = mDatabase2.push().getKey();
            UserInfo user1 = new UserInfo(key, name1, lat1, lon1, time1);
            user1.roadID = 2;
            mDatabase2.child(key).setValue(user1);


            double lat2 = 23.726870;
            double lon2 = 90.385682;
            String time2 = "2017.12.14.15.22.20";

            key = mDatabase2.push().getKey();
            UserInfo user2 = new UserInfo(key, name1, lat2, lon2, time2);
            mDatabase2.child(key).setValue(user2);

            double lat3 = 23.726898;
            double lon3 = 90.386274;
            String time3 = "2017.12.14.15.22.30";

            key = mDatabase2.push().getKey();
            UserInfo user3 = new UserInfo(key, name1, lat3, lon3, time3);
            mDatabase2.child(key).setValue(user3);


            double lat4 = 23.726954;
            double lon4 = 90.386607;
            String time4 = "2017.12.14.15.22.40";

            key = mDatabase2.push().getKey();
            UserInfo user4 = new UserInfo(key, name1, lat4, lon4, time4);
            mDatabase2.child(key).setValue(user4);


            double lat5 = 23.726954;
            double lon5 = 90.386790;
            String time5 = "2017.12.14.15.22.50";

            key = mDatabase2.push().getKey();
            UserInfo user5 = new UserInfo(key, name1, lat5, lon5, time5);
            mDatabase2.child(key).setValue(user5);


            double lat6 = 23.727061;
            double lon6 = 90.387634;
            String time6 = "2017.12.14.15.23.00";

            key = mDatabase2.push().getKey();
            UserInfo user6 = new UserInfo(key, name1, lat6, lon6, time6);
            mDatabase2.child(key).setValue(user6);


            double lat7 = 23.727148;
            double lon7 = 90.388201;
            String time7 = "2017.12.14.15.23.10";

            key = mDatabase2.push().getKey();
            UserInfo user7 = new UserInfo(key, name1, lat7, lon7, time7);
            mDatabase2.child(key).setValue(user7);


            double lat8 = 23.727176;
            double lon8 = 90.388413;
            String time8 = "2017.12.14.15.23.20";

            key = mDatabase2.push().getKey();
            UserInfo user8 = new UserInfo(key, name1, lat8, lon8, time8);
            mDatabase2.child(key).setValue(user8);


            double lat9 = 23.727190;
            double lon9 = 90.388656;
            String time9 = "2017.12.14.15.23.30";

            key = mDatabase2.push().getKey();
            UserInfo user9 = new UserInfo(key, name1, lat9, lon9, time9);
            mDatabase2.child(key).setValue(user9);


            double lat10 = 23.727273;
            double lon10 = 90.388792;
            String time10 = "2017.12.14.15.23.40";

            key = mDatabase2.push().getKey();
            UserInfo user10 = new UserInfo(key, name1, lat10, lon10, time10);
            mDatabase2.child(key).setValue(user10);

        }
        else if(c == 2)
        {
            double lat1 = 23.727366;
            double lon1 = 90.389408;
            String time1 = "2017.12.14.15.22.10"; //time format "YYYY.MM.DD.HH.MM.SS"


            String key = mDatabase2.push().getKey();
            UserInfo user1 = new UserInfo(key, name1, lat1, lon1, time1);
            user1.roadID = 2;
            mDatabase2.child(key).setValue(user1);


            double lat2 = 23.727242;
            double lon2 = 90.389006;
            String time2 = "2017.12.14.15.22.20";

            key = mDatabase2.push().getKey();
            UserInfo user2 = new UserInfo(key, name1, lat2, lon2, time2);
            mDatabase2.child(key).setValue(user2);

            double lat3 = 23.727161;
            double lon3 = 90.388707;
            String time3 = "2017.12.14.15.22.30";

            key = mDatabase2.push().getKey();
            UserInfo user3 = new UserInfo(key, name1, lat3, lon3, time3);
            mDatabase2.child(key).setValue(user3);


            double lat4 = 23.727031;
            double lon4 = 90.388164;
            String time4 = "2017.12.14.15.22.40";

            key = mDatabase2.push().getKey();
            UserInfo user4 = new UserInfo(key, name1, lat4, lon4, time4);
            mDatabase2.child(key).setValue(user4);


            double lat5 = 23.726953;
            double lon5 = 90.387641;
            String time5 = "2017.12.14.15.22.50";

            key = mDatabase2.push().getKey();
            UserInfo user5 = new UserInfo(key, name1, lat5, lon5, time5);
            mDatabase2.child(key).setValue(user5);


            double lat6 = 23.726914;
            double lon6 = 90.387324;
            String time6 = "2017.12.14.15.23.00";

            key = mDatabase2.push().getKey();
            UserInfo user6 = new UserInfo(key, name1, lat6, lon6, time6);
            mDatabase2.child(key).setValue(user6);


            double lat7 = 23.726841;
            double lon7 = 90.386434;
            String time7 = "2017.12.14.15.23.10";

            key = mDatabase2.push().getKey();
            UserInfo user7 = new UserInfo(key, name1, lat7, lon7, time7);
            mDatabase2.child(key).setValue(user7);


            double lat8 = 23.726822;
            double lon8 = 90.386317;
            String time8 = "2017.12.14.15.23.20";

            key = mDatabase2.push().getKey();
            UserInfo user8 = new UserInfo(key, name1, lat8, lon8, time8);
            mDatabase2.child(key).setValue(user8);


            double lat9 = 23.726821;
            double lon9 = 90.386277;
            String time9 = "2017.12.14.15.23.30";

            key = mDatabase2.push().getKey();
            UserInfo user9 = new UserInfo(key, name1, lat9, lon9, time9);
            mDatabase2.child(key).setValue(user9);


            double lat10 = 23.726817;
            double lon10 = 90.386237;
            String time10 = "2017.12.14.15.23.40";

            key = mDatabase2.push().getKey();
            UserInfo user10 = new UserInfo(key, name1, lat10, lon10, time10);
            mDatabase2.child(key).setValue(user10);

        }
        else if(c == 3)
        {

        }

    }


    private void saveUserInfo3(int c) {

        DatabaseReference mDatabase3 = FirebaseDatabase.getInstance().getReference().child("Users").child("User3");

        //VALUES OF USER 3 WHO TRAVELS FROM POLASHI MOR ALONG BUET MAIN CAMPUS ROAD
        String name1 = "User3";

        if(c == 1) {

            double lat1 = 23.727393;
            double lon1 = 90.389717;
            String time1 = "2017.12.14.15.22.10"; //time format "YYYY.MM.DD.HH.MM.SS"


            String key = mDatabase3.push().getKey();
            UserInfo user1 = new UserInfo(key, name1, lat1, lon1, time1);
            user1.roadID = 3;
            mDatabase3.child(key).setValue(user1);


            double lat2 = 23.727282;
            double lon2 = 90.389824;
            String time2 = "2017.12.14.15.22.20";

            key = mDatabase3.push().getKey();
            UserInfo user2 = new UserInfo(key, name1, lat2, lon2, time2);
            mDatabase3.child(key).setValue(user2);

            double lat3 = 23.727219;
            double lon3 = 90.389884;
            String time3 = "2017.12.14.15.22.30";

            key = mDatabase3.push().getKey();
            UserInfo user3 = new UserInfo(key, name1, lat3, lon3, time3);
            mDatabase3.child(key).setValue(user3);


            double lat4 = 23.727177;
            double lon4 = 90.389937;
            String time4 = "2017.12.14.15.22.40";

            key = mDatabase3.push().getKey();
            UserInfo user4 = new UserInfo(key, name1, lat4, lon4, time4);
            mDatabase3.child(key).setValue(user4);


            double lat5 = 23.726997;
            double lon5 = 90.390150;
            String time5 = "2017.12.14.15.22.50";

            key = mDatabase3.push().getKey();
            UserInfo user5 = new UserInfo(key, name1, lat5, lon5, time5);
            mDatabase3.child(key).setValue(user5);


            double lat6 = 23.726860;
            double lon6 = 90.390272;
            String time6 = "2017.12.14.15.23.00";

            key = mDatabase3.push().getKey();
            UserInfo user6 = new UserInfo(key, name1, lat6, lon6, time6);
            mDatabase3.child(key).setValue(user6);


            double lat7 = 23.726791;
            double lon7 = 90.390408;
            String time7 = "2017.12.14.15.23.10";

            key = mDatabase3.push().getKey();
            UserInfo user7 = new UserInfo(key, name1, lat7, lon7, time7);
            mDatabase3.child(key).setValue(user7);


            double lat8 = 23.726555;
            double lon8 = 90.390704;
            String time8 = "2017.12.14.15.23.20";

            key = mDatabase3.push().getKey();
            UserInfo user8 = new UserInfo(key, name1, lat8, lon8, time8);
            mDatabase3.child(key).setValue(user8);


            double lat9 = 23.726249;
            double lon9 = 90.391213;
            String time9 = "2017.12.14.15.23.30";

            key = mDatabase3.push().getKey();
            UserInfo user9 = new UserInfo(key, name1, lat9, lon9, time9);
            mDatabase3.child(key).setValue(user9);


            double lat10 = 23.725881;
            double lon10 = 90.391774;
            String time10 = "2017.12.14.15.23.40";

            key = mDatabase3.push().getKey();
            UserInfo user10 = new UserInfo(key, name1, lat10, lon10, time10);
            mDatabase3.child(key).setValue(user10);

        }
        else if (c == 2)
        {
            double lat1 = 23.725430;
            double lon1 = 90.392634;
            String time1 = "2017.12.14.15.22.10"; //time format "YYYY.MM.DD.HH.MM.SS"


            String key = mDatabase3.push().getKey();
            UserInfo user1 = new UserInfo(key, name1, lat1, lon1, time1);
            user1.roadID = 3;
            mDatabase3.child(key).setValue(user1);


            double lat2 = 23.725562;
            double lon2 = 90.392341;
            String time2 = "2017.12.14.15.22.20";

            key = mDatabase3.push().getKey();
            UserInfo user2 = new UserInfo(key, name1, lat2, lon2, time2);
            mDatabase3.child(key).setValue(user2);

            double lat3 = 23.725796;
            double lon3 = 90.391909;
            String time3 = "2017.12.14.15.22.30";

            key = mDatabase3.push().getKey();
            UserInfo user3 = new UserInfo(key, name1, lat3, lon3, time3);
            mDatabase3.child(key).setValue(user3);


            double lat4 = 23.725869;
            double lon4 = 90.391786;
            String time4 = "2017.12.14.15.22.40";

            key = mDatabase3.push().getKey();
            UserInfo user4 = new UserInfo(key, name1, lat4, lon4, time4);
            mDatabase3.child(key).setValue(user4);


            double lat5 = 23.725911;
            double lon5 = 90.391662;
            String time5 = "2017.12.14.15.22.50";

            key = mDatabase3.push().getKey();
            UserInfo user5 = new UserInfo(key, name1, lat5, lon5, time5);
            mDatabase3.child(key).setValue(user5);


            double lat6 = 23.726021;
            double lon6 = 90.391434;
            String time6 = "2017.12.14.15.23.00";

            key = mDatabase3.push().getKey();
            UserInfo user6 = new UserInfo(key, name1, lat6, lon6, time6);
            mDatabase3.child(key).setValue(user6);


            double lat7 = 23.726230;
            double lon7 = 90.391080;
            String time7 = "2017.12.14.15.23.10";

            key = mDatabase3.push().getKey();
            UserInfo user7 = new UserInfo(key, name1, lat7, lon7, time7);
            mDatabase3.child(key).setValue(user7);


            double lat8 = 23.726493;
            double lon8 = 90.390699;
            String time8 = "2017.12.14.15.23.20";

            key = mDatabase3.push().getKey();
            UserInfo user8 = new UserInfo(key, name1, lat8, lon8, time8);
            mDatabase3.child(key).setValue(user8);


            double lat9 = 23.726724;
            double lon9 = 90.390367;
            String time9 = "2017.12.14.15.23.30";

            key = mDatabase3.push().getKey();
            UserInfo user9 = new UserInfo(key, name1, lat9, lon9, time9);
            mDatabase3.child(key).setValue(user9);


            double lat10 = 23.726756;
            double lon10 = 90.390326;
            String time10 = "2017.12.14.15.23.40";

            key = mDatabase3.push().getKey();
            UserInfo user10 = new UserInfo(key, name1, lat10, lon10, time10);
            mDatabase3.child(key).setValue(user10);

        }
        else if(c == 3)
        {

        }
    }


    private void saveUserInfo4(int c) {

        DatabaseReference mDatabase3 = FirebaseDatabase.getInstance().getReference().child("Users").child("User4");

        //VALUES OF USER 4 SAME AS USER @ AZIMPUT ROAD
        String name1 = "User4";

        if(c == 1) {

            double lat1 = 23.727501;
            double lon1 = 90.389656;
            String time1 = "2017.12.14.15.22.10"; //time format "YYYY.MM.DD.HH.MM.SS"


            String key = mDatabase3.push().getKey();
            UserInfo user1 = new UserInfo(key, name1, lat1, lon1, time1);
            user1.roadID = 2;
            mDatabase3.child(key).setValue(user1);


            double lat2 = 23.727505;
            double lon2 = 90.389648;
            String time2 = "2017.12.14.15.22.20";

            key = mDatabase3.push().getKey();
            UserInfo user2 = new UserInfo(key, name1, lat2, lon2, time2);
            mDatabase3.child(key).setValue(user2);

            double lat3 = 23.727551;
            double lon3 = 90.389739;
            String time3 = "2017.12.14.15.22.30";

            key = mDatabase3.push().getKey();
            UserInfo user3 = new UserInfo(key, name1, lat3, lon3, time3);
            mDatabase3.child(key).setValue(user3);


            double lat4 = 23.727596;
            double lon4 = 90.389855;
            String time4 = "2017.12.14.15.22.40";

            key = mDatabase3.push().getKey();
            UserInfo user4 = new UserInfo(key, name1, lat4, lon4, time4);
            mDatabase3.child(key).setValue(user4);


            double lat5 = 23.727656;
            double lon5 = 90.390007;
            String time5 = "2017.12.14.15.22.50";

            key = mDatabase3.push().getKey();
            UserInfo user5 = new UserInfo(key, name1, lat5, lon5, time5);
            mDatabase3.child(key).setValue(user5);


            double lat6 = 23.727678;
            double lon6 = 90.390067;
            String time6 = "2017.12.14.15.23.00";

            key = mDatabase3.push().getKey();
            UserInfo user6 = new UserInfo(key, name1, lat6, lon6, time6);
            mDatabase3.child(key).setValue(user6);


            double lat7 = 23.727703;
            double lon7 = 90.390153;
            String time7 = "2017.12.14.15.23.10";

            key = mDatabase3.push().getKey();
            UserInfo user7 = new UserInfo(key, name1, lat7, lon7, time7);
            mDatabase3.child(key).setValue(user7);


            double lat8 = 23.727800;
            double lon8 = 90.390402;
            String time8 = "2017.12.14.15.23.20";

            key = mDatabase3.push().getKey();
            UserInfo user8 = new UserInfo(key, name1, lat8, lon8, time8);
            mDatabase3.child(key).setValue(user8);


            double lat9 = 23.727881;
            double lon9 = 90.390627;
            String time9 = "2017.12.14.15.23.30";

            key = mDatabase3.push().getKey();
            UserInfo user9 = new UserInfo(key, name1, lat9, lon9, time9);
            mDatabase3.child(key).setValue(user9);


            double lat10 = 23.727894;
            double lon10 = 90.390667;
            String time10 = "2017.12.14.15.23.40";

            key = mDatabase3.push().getKey();
            UserInfo user10 = new UserInfo(key, name1, lat10, lon10, time10);
            mDatabase3.child(key).setValue(user10);

        }
        else if(c == 2)
        {
            double lat1 = 23.727522;
            double lon1 = 90.389716;
            String time1 = "2017.12.14.15.22.10"; //time format "YYYY.MM.DD.HH.MM.SS"


            String key = mDatabase3.push().getKey();
            UserInfo user1 = new UserInfo(key, name1, lat1, lon1, time1);
            user1.roadID = 2;
            mDatabase3.child(key).setValue(user1);


            double lat2 = 23.727576;
            double lon2 = 90.389868;
            String time2 = "2017.12.14.15.22.20";

            key = mDatabase3.push().getKey();
            UserInfo user2 = new UserInfo(key, name1, lat2, lon2, time2);
            mDatabase3.child(key).setValue(user2);

            double lat3 = 23.727704;
            double lon3 = 90.390200;
            String time3 = "2017.12.14.15.22.30";

            key = mDatabase3.push().getKey();
            UserInfo user3 = new UserInfo(key, name1, lat3, lon3, time3);
            mDatabase3.child(key).setValue(user3);


            double lat4 = 23.727765;
            double lon4 = 90.390364;
            String time4 = "2017.12.14.15.22.40";

            key = mDatabase3.push().getKey();
            UserInfo user4 = new UserInfo(key, name1, lat4, lon4, time4);
            mDatabase3.child(key).setValue(user4);


            double lat5 = 23.727860;
            double lon5 = 90.390564;
            String time5 = "2017.12.14.15.22.50";

            key = mDatabase3.push().getKey();
            UserInfo user5 = new UserInfo(key, name1, lat5, lon5, time5);
            mDatabase3.child(key).setValue(user5);


            double lat6 = 23.727782;
            double lon6 = 90.390439;
            String time6 = "2017.12.14.15.23.00";

            key = mDatabase3.push().getKey();
            UserInfo user6 = new UserInfo(key, name1, lat6, lon6, time6);
            mDatabase3.child(key).setValue(user6);


            double lat7 = 23.727802;
            double lon7 = 90.390481;
            String time7 = "2017.12.14.15.23.10";

            key = mDatabase3.push().getKey();
            UserInfo user7 = new UserInfo(key, name1, lat7, lon7, time7);
            mDatabase3.child(key).setValue(user7);


            double lat8 = 23.727867;
            double lon8 = 90.390644;
            String time8 = "2017.12.14.15.23.20";

            key = mDatabase3.push().getKey();
            UserInfo user8 = new UserInfo(key, name1, lat8, lon8, time8);
            mDatabase3.child(key).setValue(user8);


            double lat9 = 23.727922;
            double lon9 = 90.390778;
            String time9 = "2017.12.14.15.23.30";

            key = mDatabase3.push().getKey();
            UserInfo user9 = new UserInfo(key, name1, lat9, lon9, time9);
            mDatabase3.child(key).setValue(user9);


            double lat10 = 23.728003;
            double lon10 = 90.391010;
            String time10 = "2017.12.14.15.23.40";

            key = mDatabase3.push().getKey();
            UserInfo user10 = new UserInfo(key, name1, lat10, lon10, time10);
            mDatabase3.child(key).setValue(user10);
        }
        else if(c == 3)
        {

        }

    }


    private void saveUserInfo5(int c) {

        DatabaseReference mDatabase3 = FirebaseDatabase.getInstance().getReference().child("Users").child("User5");

        //VALUES OF USER 5 WHO TRAVELS DHAKESHORI ROAD
        String name1 = "User5";

        if(c == 1)
        {
            double lat1 = 23.727323;
            double lon1 = 90.389493;
            String time1 = "2017.12.14.15.22.10"; //time format "YYYY.MM.DD.HH.MM.SS"


            String key = mDatabase3.push().getKey();
            UserInfo user1 = new UserInfo(key, name1, lat1, lon1, time1);
            user1.roadID = 4;
            mDatabase3.child(key).setValue(user1);


            double lat2 = 23.727299;
            double lon2 = 90.389493;
            String time2 = "2017.12.14.15.22.20";

            key = mDatabase3.push().getKey();
            UserInfo user2 = new UserInfo(key, name1, lat2, lon2, time2);
            mDatabase3.child(key).setValue(user2);


            double lat3 = 23.726382;
            double lon3 = 90.389282;
            String time3 = "2017.12.14.15.22.30";

            key = mDatabase3.push().getKey();
            UserInfo user3 = new UserInfo(key, name1, lat3, lon3, time3);
            mDatabase3.child(key).setValue(user3);


            double lat4 = 23.725644;
            double lon4 = 90.389135;
            String time4 = "2017.12.14.15.22.40";

            key = mDatabase3.push().getKey();
            UserInfo user4 = new UserInfo(key, name1, lat4, lon4, time4);
            mDatabase3.child(key).setValue(user4);


            double lat5 = 23.724937;
            double lon5 = 90.388992;
            String time5 = "2017.12.14.15.22.50";

            key = mDatabase3.push().getKey();
            UserInfo user5 = new UserInfo(key, name1, lat5, lon5, time5);
            mDatabase3.child(key).setValue(user5);


            double lat6 = 23.724481;
            double lon6 = 90.3888992;
            String time6 = "2017.12.14.15.23.00";

            key = mDatabase3.push().getKey();
            UserInfo user6 = new UserInfo(key, name1, lat6, lon6, time6);
            mDatabase3.child(key).setValue(user6);


            double lat7 = 23.723845;
            double lon7 = 90.388758;
            String time7 = "2017.12.14.15.23.10";

            key = mDatabase3.push().getKey();
            UserInfo user7 = new UserInfo(key, name1, lat7, lon7, time7);
            mDatabase3.child(key).setValue(user7);


            double lat8 = 23.723535;
            double lon8 = 90.388707;
            String time8 = "2017.12.14.15.23.20";

            key = mDatabase3.push().getKey();
            UserInfo user8 = new UserInfo(key, name1, lat8, lon8, time8);
            mDatabase3.child(key).setValue(user8);


            double lat9 = 23.723136;
            double lon9 = 90.388694;
            String time9 = "2017.12.14.15.23.30";

            key = mDatabase3.push().getKey();
            UserInfo user9 = new UserInfo(key, name1, lat9, lon9, time9);
            mDatabase3.child(key).setValue(user9);


            double lat10 = 23.722870;
            double lon10 = 90.388697;
            String time10 = "2017.12.14.15.23.40";

            key = mDatabase3.push().getKey();
            UserInfo user10 = new UserInfo(key, name1, lat10, lon10, time10);
            mDatabase3.child(key).setValue(user10);


        }

        else if(c == 2)
        {
            double lat1 = 23.727297;
            double lon1 = 90.389492;
            String time1 = "2017.12.14.15.22.10"; //time format "YYYY.MM.DD.HH.MM.SS"


            String key = mDatabase3.push().getKey();
            UserInfo user1 = new UserInfo(key, name1, lat1, lon1, time1);
            user1.roadID = 4;
            mDatabase3.child(key).setValue(user1);


            double lat2 = 23.726903;
            double lon2 = 90.389400;
            String time2 = "2017.12.14.15.22.20";

            key = mDatabase3.push().getKey();
            UserInfo user2 = new UserInfo(key, name1, lat2, lon2, time2);
            mDatabase3.child(key).setValue(user2);

            double lat3 = 23.726607;
            double lon3 = 90.389336;
            String time3 = "2017.12.14.15.22.30";

            key = mDatabase3.push().getKey();
            UserInfo user3 = new UserInfo(key, name1, lat3, lon3, time3);
            mDatabase3.child(key).setValue(user3);


            double lat4 = 23.726467;
            double lon4 = 90.389293;
            String time4 = "2017.12.14.15.22.40";

            key = mDatabase3.push().getKey();
            UserInfo user4 = new UserInfo(key, name1, lat4, lon4, time4);
            mDatabase3.child(key).setValue(user4);


            double lat5 = 23.726391;
            double lon5 = 90.389285;
            String time5 = "2017.12.14.15.22.50";

            key = mDatabase3.push().getKey();
            UserInfo user5 = new UserInfo(key, name1, lat5, lon5, time5);
            mDatabase3.child(key).setValue(user5);


            double lat6 = 23.726246;
            double lon6 = 90.389255;
            String time6 = "2017.12.14.15.23.00";

            key = mDatabase3.push().getKey();
            UserInfo user6 = new UserInfo(key, name1, lat6, lon6, time6);
            mDatabase3.child(key).setValue(user6);


            double lat7 = 23.726124;
            double lon7 = 90.389239;
            String time7 = "2017.12.14.15.23.10";

            key = mDatabase3.push().getKey();
            UserInfo user7 = new UserInfo(key, name1, lat7, lon7, time7);
            mDatabase3.child(key).setValue(user7);


            double lat8 = 23.725723;
            double lon8 = 90.389153;
            String time8 = "2017.12.14.15.23.20";

            key = mDatabase3.push().getKey();
            UserInfo user8 = new UserInfo(key, name1, lat8, lon8, time8);
            mDatabase3.child(key).setValue(user8);


            double lat9 = 23.725365;
            double lon9 = 90.389086;
            String time9 = "2017.12.14.15.23.30";

            key = mDatabase3.push().getKey();
            UserInfo user9 = new UserInfo(key, name1, lat9, lon9, time9);
            mDatabase3.child(key).setValue(user9);


            double lat10 = 23.724837;
            double lon10 = 90.388968;
            String time10 = "2017.12.14.15.23.40";

            key = mDatabase3.push().getKey();
            UserInfo user10 = new UserInfo(key, name1, lat10, lon10, time10);
            mDatabase3.child(key).setValue(user10);

        }

        else if(c == 3)
        {

        }

    }

    private void getUserInfo() {


        DatabaseReference mUser = FirebaseDatabase.getInstance().getReference().child("Users");

        mUser.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot levelOne : dataSnapshot.getChildren())
                {
                    ArrayList<UserInfo> temp = new ArrayList<>();
                    String name = new String();

                    for(DataSnapshot levelTwo : levelOne.getChildren()) {

                        UserInfo user = levelTwo.getValue(UserInfo.class);
                        name = user.name;
                        temp.add(user);
                        LatLng loc = new LatLng(user.latitude, user.longitude);
                        //mMap.addMarker(new MarkerOptions().position(loc).title(user.name));


                    }

                    info.put(name,temp);
                    Log.d("getinfo","info size: " + info.size() + " ," + name + ", arraysize: " + temp.size());

                }

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        mMap.setOnMarkerDragListener(this);
        mMap.setOnMarkerClickListener(this);

        change = 1;

        DatabaseReference db = FirebaseDatabase.getInstance().getReference().child("Users");
        db.removeValue();

        road1 = new ArrayList<ArrayList<UserInfo>>();
        road2 = new ArrayList<ArrayList<UserInfo>>();
        road3 = new ArrayList<ArrayList<UserInfo>>();
        road4 = new ArrayList<ArrayList<UserInfo>>();

        Log.d("change","state = " + change + '\n');

        saveUserInfo1(change);
        saveUserInfo2(change);
        saveUserInfo3(change);
        saveUserInfo4(change);
        saveUserInfo5(change);

        getUserInfo();

        waitTimer();

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(23.727869,90.389186)));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        lastLocation = location;
        if(currentLocationMarker != null)
        {
            currentLocationMarker.remove();
        }
        Log.d("lat = ",""+latitude);

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        currentLocationMarker = mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomBy(15));

        if(client != null)
        {
            LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
        }

    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
