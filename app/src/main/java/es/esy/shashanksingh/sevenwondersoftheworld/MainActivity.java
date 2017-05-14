package es.esy.shashanksingh.sevenwondersoftheworld;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;

import java.util.ArrayList;
import java.util.Random;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnStreetViewPanoramaReadyCallback,OnMapReadyCallback {


    StreetViewPanorama mStreetViewPanorama;
    private LatLng latLngToBeUsed;

    private int bearingToBeUsed;
    private GoogleMap mMap;
    private Marker marker;
    private int REQUEST_PLACE_PICKER=1691;
    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission[] ={ android.Manifest.permission.ACCESS_FINE_LOCATION};
    private ArrayList<MyLocation> allLocations;
    private Boolean fullScreen;
    private Boolean halfMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fullScreen=false;
        halfMap=true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        allLocations=new ArrayList<MyLocation>();
        addAllLocations();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Check if the version of Android is Lollipop or higher
        if (Build.VERSION.SDK_INT >= 21) {
            // Set the status bar to dark-semi-transparentish
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // Set paddingTop of toolbar to height of status bar.
            // Fixes statusbar covers toolbar issue
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        try {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != MockPackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        mPermission, REQUEST_CODE_PERMISSION);
                // If any permission above not allowed by user, this condition will execute every time, else your else part will work
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        final View mapFrag= findViewById(R.id.frag_map);
        final ImageButton mapArrow=(ImageButton) findViewById(R.id.up_the_map);
        final ImageButton fullscreenButton=(ImageButton) findViewById(R.id.full_screen);
        fullscreenButton.setBackgroundResource(R.drawable.ic_fullscreen_white_48dp);
        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fullScreen){
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,2.0f);
                    mapFrag.setLayoutParams(param);
                    fullScreen=false;
                    fullscreenButton.setBackgroundResource(R.drawable.ic_fullscreen_white_48dp);
                }else{
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0, 0.0f);
                    mapFrag.setLayoutParams(param);
                    fullScreen=true;
                    halfMap=false;
                    mapArrow.setBackgroundResource(R.drawable.ic_arrow_drop_up_black_48dp);
                    fullscreenButton.setBackgroundResource(R.drawable.ic_fullscreen_exit_white_48dp);
                }
            }
        });


        mapArrow.setBackgroundResource(R.drawable.ic_arrow_drop_down_black_48dp);
        mapArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(halfMap){
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,2.0f);
                    mapFrag.setLayoutParams(param);
                    halfMap=false;
                    mapArrow.setBackgroundResource(R.drawable.ic_arrow_drop_up_black_48dp);
                }else{
                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0, 4.0f);
                    mapFrag.setLayoutParams(param);
                    halfMap=true;
                    mapArrow.setBackgroundResource(R.drawable.ic_arrow_drop_down_black_48dp);
                }
            }
        });



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager()
                .findFragmentById(R.id.fragment_main);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);
        mapSet(allLocations.get(30));
    }

    private void showCase(){
        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(50);// half second between each showcase view
        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(this, "12345");
        sequence.setConfig(config);
        sequence.addSequenceItem(findViewById(R.id.full_screen),
                "Tap to enter or leave full screen street view mode.", "NEXT");
        sequence.addSequenceItem(findViewById(R.id.up_the_map),
                "Tap to resize the map.", "NEXT");
        sequence.addSequenceItem(findViewById(R.id.street_map),
                "Tap arrows or double tap anywhere to move there and swipe left or right to rotate the panorama view.", "NEXT");
        sequence.addSequenceItem(findViewById(R.id.toolbar),
                "Tap on search to enter  or pick a place manually.\n\nTap on shuffle to see a random place automatically.", "NEXT");
        sequence.addSequenceItem(findViewById(R.id.frag_map),
                "Click anywhere to see the street view from that location.", "OK");
        sequence.start();
    }

    private void addAllLocations(){
        /*00*/allLocations.add(new MyLocation(new LatLng(27.173713, 78.041996),"Taj Mahal, India",0));
        /*01*/allLocations.add(new MyLocation(new LatLng(-22.951850, -43.210085),"Christ the Redeemer, Rio de Janeiro, Brazil",255));
        /*02*/allLocations.add(new MyLocation(new LatLng(-13.163214, -72.544909),"Machu Picchu, Peru",180));
        /*03*/allLocations.add(new MyLocation(new LatLng(20.686530, -88.567440),"Chichén Itzá, Mexio",0));
        /*04*/allLocations.add(new MyLocation(new LatLng(41.891247, 12.491033),"The Colosseum, Rome, Italy",140));
        /*05*/allLocations.add(new MyLocation(new LatLng(30.329205, 35.442494),"Petra, Jordan",180));
        /*06*/allLocations.add(new MyLocation(new LatLng(40.431908, 116.570374),"The Great Wall of China, China",0));
        /*07*/allLocations.add(new MyLocation(new LatLng(29.977822, 31.130332),"Pyramids of Giza, Egypt",180));
        /*08*/allLocations.add(new MyLocation(new LatLng(51.178863, -1.826215),"Stonehenge, Amesbury, England",0));
        /*09*/allLocations.add(new MyLocation(new LatLng(36.056927, -112.172047),"The Grand Canyon, Arizona, USA",0));
        /*10*/allLocations.add(new MyLocation(new LatLng(38.787402, -9.390745),"Park and National Palace of Pena",0));
        /*11*/allLocations.add(new MyLocation(new LatLng(48.858373, 2.294367),"Eiffel Tower, Paris, France",300));
        /*12*/allLocations.add(new MyLocation(new LatLng(-32.023521, 115.450708),"Fish Hook Bay, Australia",120));
        /*13*/allLocations.add(new MyLocation(new LatLng(-25.687667, -54.443836),"Iguazú National Park, Argentina",300));
        /*14*/allLocations.add(new MyLocation(new LatLng(23.487231, 120.959349),"Yushan North Peak, Taiwan",180));
        /*15*/allLocations.add(new MyLocation(new LatLng(45.832589, 6.864094),"Mont Blanc, Europe",0));
        /*16*/allLocations.add(new MyLocation(new LatLng(33.883976, 136.098088),"Hama-kaidou",90));
        /*17*/allLocations.add(new MyLocation(new LatLng(-3.838784, -32.410736),"Fernando de Noronha National Marine Park - Cachorro Beack",270));
        /*18*/allLocations.add(new MyLocation(new LatLng(33.881836, 133.760136),"Obokekyo, Japan",0));
        /*19*/allLocations.add(new MyLocation(new LatLng(-22.912165, -43.230248),"Estádio do Maracanã, Brazil's World cup stadiums",0));
        /*20*/allLocations.add(new MyLocation(new LatLng(47.595250, -122.331644),"CenturyLink Field, Seattle ",0));
        /*21*/allLocations.add(new MyLocation(new LatLng(36.452225, -111.837163),"Colorado River ",0));
        /*22*/allLocations.add(new MyLocation(new LatLng(45.432805, 12.340583),"Venice",0));
        /*23*/allLocations.add(new MyLocation(new LatLng(25.197184, 55.274378),"Burj Khalifa, Dubai ",0));
        /*24*/allLocations.add(new MyLocation(new LatLng(37.290100, 13.589661),"Valley of the Temples - Early-Christian Necropolis",0));
        /*25*/allLocations.add(new MyLocation(new LatLng(51.500564, -0.122339),"Thames River, London, England",210));
        /*26*/allLocations.add(new MyLocation(new LatLng(41.497808, 76.425437)," Eki-Naryn Valley, Kyrgyzstan",0));
        /*27*/allLocations.add(new MyLocation(new LatLng(58.758444, -93.231829),"Churchill, Manitoba, Canada ",0));
        /*28*/allLocations.add(new MyLocation(new LatLng(42.435147, 1.536002),"Naturlandia Ski Resort",120));
        /*29*/allLocations.add(new MyLocation(new LatLng(63.734793, -68.515493),"Iqaluit, Canadian Arctic ",0));
        /*30*/allLocations.add(new MyLocation(new LatLng(32.250471, -64.823307),"Horseshoe Bay Cove, Bermuda",180));
        /*31*/allLocations.add(new MyLocation(new LatLng(35.582077, 23.593284),"Balos Beach, Greece",0));
        /*32*/allLocations.add(new MyLocation(new LatLng(34.828293, 137.668250),"Ryutanji, Japan",0));
        /*33*/allLocations.add(new MyLocation(new LatLng(32.276571, 35.889265),"Jerash Monuments, Jordan",0));
        /*34*/allLocations.add(new MyLocation(new LatLng(37.577886, 126.976966),"Gyeongbokgung Palace, Korea",0));
        /*35*/allLocations.add(new MyLocation(new LatLng(1.281790, 103.856961),"Marina Bay, Singapore",270));
        /*36*/allLocations.add(new MyLocation(new LatLng(36.915956, -4.772380),"The King's little pathway, Spain",0));
        /*37*/allLocations.add(new MyLocation(new LatLng(40.758732, -73.985364),"Times Square, New York",0));
        /*38*/allLocations.add(new MyLocation(new LatLng(48.628411, -113.864322),"Glacier National Park, Montana, USA",0));
        /*39*/allLocations.add(new MyLocation(new LatLng(36.426871, 25.430793),"Santorini Island, Greece",0));


        //  /*4*/allLocations.add(new MyLocation(new LatLng()," ",0));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        showCase();
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("Req Code", "" + requestCode);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length == 1 &&
                    grantResults[0] == MockPackageManager.PERMISSION_GRANTED ) {
                // Success Stuff here
            }
            else{
                Toast.makeText(MainActivity.this,"Location Permissions not granted",Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void mapSet(MyLocation myLocation){
        latLngToBeUsed=myLocation.mLatLng;
        bearingToBeUsed=myLocation.mBearing;
        if(mMap!=null) {
            if(marker==null){
                marker=mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                        .title("You are here")
                        .position(latLngToBeUsed));}
            else{
                marker.remove();
                marker=mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                        .title("You are here")
                        .position(latLngToBeUsed));
            }
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLngToBeUsed, 18);
            mMap.animateCamera(update);
        }
        if(mStreetViewPanorama!=null){
            mStreetViewPanorama.setPosition(latLngToBeUsed);
            StreetViewPanoramaCamera camera = new StreetViewPanoramaCamera.Builder()
                    .bearing(bearingToBeUsed)
                    .build();
            mStreetViewPanorama.animateTo(camera,1000);
        }
        if(myLocation.mName.length()>0)
            Snackbar.make(findViewById(R.id.main_coordinator_layout),myLocation.mName, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        showCase();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.place_picker:
                try {
                    PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                    Intent intent = intentBuilder.build(MainActivity.this);
                    // Start the Intent by requesting a result, identified by a request code.
                    startActivityForResult(intent, REQUEST_PLACE_PICKER);
                } catch (GooglePlayServicesRepairableException e) {
                    GooglePlayServicesUtil
                            .getErrorDialog(e.getConnectionStatusCode(), MainActivity.this, 0);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Toast.makeText(MainActivity.this, "Google Play Services is not available.",
                            Toast.LENGTH_LONG)
                            .show();
                }
                break;

            case R.id.random_place:
                Random random=new Random();
                mapSet(allLocations.get(random.nextInt(allLocations.size())));
                break;
        }
        return false;
    }

    private void showAboutDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("About this app");
        builder.setMessage("--> App only works with ACTIVE INTERNET connection.\n\n--> BLACK SCREEN means the street view is not available on google.\n" +
                "\n--> CLICK or DOUBLE CLICK on street view or ARROWS to move forward.\n\n--> CLICK on the map to select different location and view it in STREET VIEW.\n" +
                "\n--> Select a custom location by clicking on SEARCH button.\n\n" +
                "--> Click on FULLSCREEN BUTTON to enter full screen streetview mode and CLICK IT AGAIN to leave it.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
                    // Create the AlertDialog object and return it
         builder.create().show();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                mapSet(new MyLocation(place.getLatLng(),place.getName().toString(),0));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        googleMap.setTrafficEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLngToBeUsed, 18);
        googleMap.animateCamera(update);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng latlng)
            {
                mapSet(new MyLocation(latlng,"",0));
            }
        });
    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {
        mStreetViewPanorama=panorama;
        panorama.setZoomGesturesEnabled(false);
        panorama.setStreetNamesEnabled(false);
        panorama.setOnStreetViewPanoramaChangeListener(new StreetViewPanorama.OnStreetViewPanoramaChangeListener() {
            @Override
            public void onStreetViewPanoramaChange(StreetViewPanoramaLocation streetViewPanoramaLocation) {
                if (streetViewPanoramaLocation==null)
                    return;
                if(mMap!=null){
                    if(marker==null){
                        marker=mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                                .title("You are here")
                                .position(streetViewPanoramaLocation.position));}
                    else{
                        marker.remove();
                        marker=mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_marker))
                                .title("You are here")
                                .position(streetViewPanoramaLocation.position));
                    }
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(streetViewPanoramaLocation.position, 18);
                    mMap.animateCamera(update);
                }
            }
        });
        panorama.setPosition(latLngToBeUsed);
        StreetViewPanoramaCamera camera = new StreetViewPanoramaCamera.Builder()
                .bearing(bearingToBeUsed)
                .build();
        panorama.animateTo(camera,1000);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_taj_mahal) {
            mapSet(allLocations.get(0));
        }else if (id == R.id.nav_feedback) {

        }else if (id == R.id.nav_share) {

        }else if (id == R.id.nav_about) {
            showAboutDialogBox();
        }else if (id == R.id.nav_christ_the_redeemer) {
            mapSet(allLocations.get(1));
        } else if (id == R.id.nav_machu_picchu) {
            mapSet(allLocations.get(2));
        } else if (id == R.id.nav_chichen_itza) {
            mapSet(allLocations.get(3));
        } else if (id == R.id.nav_the_colosseum) {
            mapSet(allLocations.get(4));
        } else if (id == R.id.nav_petra) {
            mapSet(allLocations.get(5));
        }else if (id == R.id.nav_great_wall) {
            mapSet(allLocations.get(6));
        }else if (id == R.id.nav_yushan_peak) {
            mapSet(allLocations.get(14));
        }else if (id == R.id.nav_fish_hook_bay) {
            mapSet(allLocations.get(12));
        }else if (id == R.id.nav_grand_canyon) {
            mapSet(allLocations.get(9));
        }else if (id == R.id.nav_horse_shoe_bay) {
            mapSet(allLocations.get(30));
        } else if (id == R.id.nav_eiffel_tower) {
            mapSet(allLocations.get(11));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
