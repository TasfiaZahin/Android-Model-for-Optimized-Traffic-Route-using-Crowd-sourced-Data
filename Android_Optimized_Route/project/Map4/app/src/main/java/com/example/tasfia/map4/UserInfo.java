package com.example.tasfia.map4;

/**
 * Created by tasfia on 11/11/17.
 */

class UserInfo {

    public String id;
    public String name;
    public double latitude;
    public double longitude;
    public String time;
    public double speed;
    public int roadID;

    public UserInfo()
    {

    }

    public UserInfo(String id, String name, double latitude, double longitude,String time)
    {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.speed = 0;
        this.roadID = -1;
    }
}
