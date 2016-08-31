package com.example.carprototype.carapp;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, LocationListener,
        GoogleApiClient.OnConnectionFailedListener {

    private final int LOCATION_LISTENER_INTERVAL = 10 * 1000;
    private final int LOCATION_LISTENER_FASTEST_INTERVAL = 5 * 1000;
    private final int LOCATION_SMALLEST_DISPLACEMENT = 5;

    private final int CAMERA_ZOOM_LEVEL = 16;
    private final int CAMERA_ZOOM_SPEED = 4 * 1000;

    private AutoCompleteTextView searchTextview;
    private BlockingArrayAdapter searchAdapter;
    private Address[] searchTextAutoResults;
    private Address selectedAddress;
    private String currentSearchText;
    private Thread searchThread;

    private TextView pickupLocationTextview, destinationLocationTextview, confirmPickupDestButton;
    private ImageView houseImageView, markerImageView;
    private LinearLayout locationInputMenu, pickupLayout, destinationLayout;

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private Location userLocation, pickupLocation, destinationLocation;

    private Providers providers;
    private MapFunctions mapFunctions;

    private boolean firstRun = true;
    private boolean pickupSearchMode = false;
    private boolean destinationSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize providers helper class
        providers = new Providers(this);

        // Initialize search bar
        ArrayList<String> searchResults = new ArrayList<String>();
        Drawable search = ContextCompat.getDrawable(this, R.drawable.search);
        search.setBounds(0, 0, 100, 100);
        searchAdapter = new BlockingArrayAdapter(MapsActivity.this,
                R.layout.auto_complete_textview_item, searchResults);
        searchAdapter.setNotifyOnChange(true);
        searchTextview = (AutoCompleteTextView) findViewById(R.id.search_text);
        searchTextview.setAdapter(searchAdapter);
        searchTextview.setCompoundDrawables(search, null, null, null);
        searchTextview.setCursorVisible(false);
        searchTextview.setFocusable(true);
        searchTextview.setFocusableInTouchMode(true);
        searchTextview.setClickable(true);
        searchTextview.setThreshold(1);
        searchTextAutoResults = new Address[MapFunctions.MAX_RESULTS];

        searchTextview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View arg1, int position, long id) {
                selectedAddress = searchTextAutoResults[position];
                Location addressLoc = mapFunctions.getLocationFromAddress(selectedAddress);

                if (pickupSearchMode)
                    mapFunctions.setPickupMarkerLocation(addressLoc, MapFunctions.UNCERTAIN_STATUS);
                else if (destinationSearchMode)
                    mapFunctions.setDestinationMarkerLocation(addressLoc, MapFunctions.UNCERTAIN_STATUS);

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(addressLoc.getLatitude(),
                        addressLoc.getLongitude()),CAMERA_ZOOM_LEVEL), CAMERA_ZOOM_SPEED, null);

                // Hide keyboard.
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                // Show confirm button
                confirmPickupDestButton.setVisibility(View.VISIBLE);

                searchTextview.setText("");
            }
        });

        searchTextview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    searchTextview.setCursorVisible(true);
                    searchTextview.setHint("");
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(searchTextview, InputMethodManager.SHOW_IMPLICIT);
                    return true;
                }
                return false;
            }
        });



        searchTextview.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence charsequence, int i, int j, int k) {
                /* Sets global currentText and attempts to create adapter before next character
                   is entered. */
                currentSearchText = charsequence.toString();
                final Handler mHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        String[] resultsArray = (String[]) msg.obj;

                        if (resultsArray != null) {
                            searchAdapter.clear();
                            searchAdapter.addAll(resultsArray);
                        }
                        else {
                            searchAdapter.clear();
                        }
                    }
                };

                searchThread = new Thread((new Runnable() {
                    public void run() {
                        ArrayAdapter<String> adapter;
                        List<Address> addresses = null;
                        Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.US);

                        if (providers.isNetworkEnabled())
                            addresses = mapFunctions.resultListFromUserInput(currentSearchText, userLocation);
                        else {
                            // Throw Connection Error
                        }

                        String tText = currentSearchText;

                        if (addresses != null) {
                            String[] array = new String[addresses.size()];
                            for (int l = 0; l < addresses.size(); l++) {
                                Address indAddress = addresses.get(l);
                                searchTextAutoResults[l] = indAddress;
                                array[l] = indAddress.getAddressLine(0) + " "
                                        + indAddress.getAddressLine(1) + " "
                                        + indAddress.getAddressLine(2);
                            }



                            if (tText.equals(currentSearchText)) {
                                Message msg = new Message();
                                msg.obj = array;
                                mHandler.sendMessage(msg);
                                return;
                            }
                        }
                        else {
                            if (tText.equals(currentSearchText)) {
                                Message msg = new Message();
                                msg.obj = null;
                                mHandler.sendMessage(msg);
                                return;
                            }
                        }
                    }
                }));

                searchThread.start();
            }

            @Override
            public void beforeTextChanged(CharSequence charsequence, int i, int j, int k) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // Initialize menu buttons
        pickupLocationTextview = (TextView) findViewById(R.id.pickup_location_textview);
        destinationLocationTextview = (TextView) findViewById(R.id.dest_location_textview);
        confirmPickupDestButton = (TextView) findViewById(R.id.confirm_pickup_dest_button);
        houseImageView = (ImageView) findViewById(R.id.house_image_view);
        markerImageView = (ImageView) findViewById(R.id.dest_image_view);
        locationInputMenu = (LinearLayout) findViewById(R.id.location_input_menu);
        pickupLayout = (LinearLayout) findViewById(R.id.pickup_layout);
        destinationLayout = (LinearLayout) findViewById(R.id.dest_layout);

        confirmPickupDestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pickupSearchMode) {
                    setPickupLocation(mapFunctions.getLocationFromAddress(selectedAddress));
                    houseImageView.setImageDrawable(getResources().getDrawable(R.drawable.checkmark));
                    mapFunctions.setPickupMarkerLocation( mapFunctions.getLocationFromAddress(selectedAddress),
                            MapFunctions.CONFIRMED_STATUS);
                    pickupSearchMode = false;
                }
                else if (destinationSearchMode) {
                    setDestinationLocation(mapFunctions.getLocationFromAddress(selectedAddress));
                    markerImageView.setImageDrawable(getResources().getDrawable(R.drawable.checkmark));
                    mapFunctions.setDestinationMarkerLocation( mapFunctions.getLocationFromAddress(selectedAddress),
                            MapFunctions.CONFIRMED_STATUS);
                    destinationSearchMode = false;
                }

                confirmPickupDestButton.setVisibility(View.INVISIBLE);
            }
        });

        pickupLayout.setOnClickListener(new LinearLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickupSearchMode = true;
                destinationLayout.setBackgroundColor(0x00000000);
                pickupLayout.setBackgroundColor(getResources().getColor(R.color.darkRed));

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchTextview, InputMethodManager.SHOW_IMPLICIT);
                searchTextview.setCursorVisible(true);
            }
        });

        destinationLayout.setOnClickListener(new LinearLayout.OnClickListener() {
            @Override
            public void onClick(View v) {
                destinationSearchMode = true;
                pickupLayout.setBackgroundColor(0x00000000);
                destinationLayout.setBackgroundColor(getResources().getColor(R.color.darkRed));

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(searchTextview, InputMethodManager.SHOW_IMPLICIT);
                searchTextview.setCursorVisible(true);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        try {
            googleMap.setMyLocationEnabled(false);
        } catch (SecurityException e) { }

        mMap = googleMap;

        if (mMap != null) {
            // Initialize mapFunctions object
            mapFunctions = new MapFunctions(mMap);

            // Build and connect to google play services
            buildGoogleApiClient();
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
       // Login.super.onBackPressed();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (mLocationManager.getProviders(new Criteria(), true) == null) {
            // Throw Location Error
        }

        if (providers.isGpsEnabled())
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        mLocationRequest, this);
            } catch (SecurityException e) {
                // Throw Location Error
            }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Throw Service Error
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Throw Service Error
    }

    @Override
    public void onLocationChanged(Location location) {
        userLocation = location;
        mapFunctions.setUserMarkerLocation(location);

        if (firstRun) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(),
                    userLocation.getLongitude()),CAMERA_ZOOM_LEVEL), CAMERA_ZOOM_SPEED, null);
            firstRun = false;
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_LISTENER_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_LISTENER_FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(LOCATION_SMALLEST_DISPLACEMENT);
    }

    /*Title:            setPickupLocation
    * Description:      Sets the pickup location variable, the respective textview,
    *                   and begins the animation of showing the textview. (Add animating the house
    *                   into a checkmark as well)
    *
    * @param    loc     Location to set the pickup location to. If null, will UNSET pickup location.
    */
    public void setPickupLocation(Location loc) {
        if (loc != null) {
            pickupLocation = loc;
            pickupLocationTextview.setText(mapFunctions.getAddressFromLocation(loc).getAddressLine(0)
                    + " " + mapFunctions.getAddressFromLocation(loc).getAddressLine(1)
                    + " " + mapFunctions.getAddressFromLocation(loc).getAddressLine(2));
            pickupLocationTextview.animate().translationY(-locationInputMenu.getHeight());
            houseImageView.setImageDrawable((Drawable) getResources().getDrawable(R.drawable.checkmark));
        }
        else {
            pickupLocationTextview.animate().translationY(0);
            houseImageView.setImageDrawable((Drawable) getResources().getDrawable(R.drawable.house));
        }

        pickupLayout.setBackgroundColor(0x00000000);
    }

    /*Title:            setDestinationLocation
    * Description:      Sets the destination location variable, the respective textview,
    *                   and begins the animation of showing the textview. (Add animating the house
    *                   into a checkmark as well)
    *
    * @param    loc     Location to set the destination location to. If null, will UNSET dest. loc.
    */
    public void setDestinationLocation(Location loc) {
        if (loc != null) {
            destinationLocation = loc;
            destinationLocationTextview.setText(mapFunctions.getAddressFromLocation(loc).getAddressLine(0)
                    + " " + mapFunctions.getAddressFromLocation(loc).getAddressLine(1)
                    + " " + mapFunctions.getAddressFromLocation(loc).getAddressLine(2));
            destinationLocationTextview.animate().translationY(-locationInputMenu.getHeight());
            markerImageView.setImageDrawable((Drawable) getResources().getDrawable(R.drawable.checkmark));
        }
        else {
            destinationLocationTextview.animate().translationY(0);
            markerImageView.setImageDrawable((Drawable) getResources().getDrawable(R.drawable.dest_marker));
        }

        destinationLayout.setBackgroundColor(0x00000000);
    }
}
