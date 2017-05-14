package es.esy.shashanksingh.sevenwondersoftheworld;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by shashank on 12-Apr-17.
 */

public class MyLocation {
    public LatLng mLatLng;
    public String mName;
    public int mBearing;

    public MyLocation(LatLng latLng,String name,int bearing){
        mLatLng=latLng;
        mName=name;
        mBearing=bearing;
    }
}
