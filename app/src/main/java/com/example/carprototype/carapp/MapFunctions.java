package com.example.carprototype.carapp;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.facebook.FacebookSdk.getApplicationContext;

/**************************************************
* File Name: MapFunctions.java
*
* Description: This class stores the marker objects used
* in MapsActivity and handles the movement and placing of
* them. Also, contains the functions for camera controls.
**************************************************/
public class MapFunctions {
    public static final int USER_LOCATION_MARKER = 0;
    public static final int PICKUP_MARKER = 1;
    public static final int DESTINATION_MARKER = 2;

    public static final int UNCERTAIN_STATUS = 0;
    public static final int CONFIRMED_STATUS = 1;

    public static final int MAX_RESULTS = 50;

    private GoogleMap mMap;
    private Geocoder geoCoder;
    private Marker userLocationMarker, pickupMarker, destinationMarker;
    private String[] markerTitleArr, markerSnippetArr;

    /*Title:                            MapFunctions
    * Description:                      Class constructor function that initializes mMap.
    *                                   and initializes marker variables.
    *
    * @param        map                 Reference to Map object.
    */
    public MapFunctions(GoogleMap map) {
        mMap = map;

        geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        markerTitleArr = new String[3];
        markerSnippetArr = new String[3];
    }

    public void setDestinationMarkerLocation(Location loc, int status) {
        Double lat = (double) (loc.getLatitude());
        Double lon = (double) (loc.getLongitude());

        LatLng location = new LatLng(lat, lon);

        if (destinationMarker != null)
            destinationMarker.remove();

        if (status == UNCERTAIN_STATUS)
            destinationMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker))
                    .position(location)
                    .title(markerTitleArr[DESTINATION_MARKER])
                    .snippet(markerSnippetArr[DESTINATION_MARKER]));
        else if (status == CONFIRMED_STATUS)
            destinationMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.dest_marker_map_icon))
                    .position(location)
                    .title(markerTitleArr[DESTINATION_MARKER])
                    .snippet(markerSnippetArr[DESTINATION_MARKER]));
    };
    public void setUserMarkerLocation(Location loc) {
        Double lat = (double) (loc.getLatitude());
        Double lon = (double) (loc.getLongitude());

        LatLng location = new LatLng(lat, lon);

        pickupMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker))
                .position(location)
                .title(markerTitleArr[PICKUP_MARKER])
                .snippet(markerSnippetArr[PICKUP_MARKER]));
    };
    public void setPickupMarkerLocation(Location loc, int status) {
        Double lat = (double) (loc.getLatitude());
        Double lon = (double) (loc.getLongitude());

        LatLng location = new LatLng(lat, lon);

        if (pickupMarker != null)
            pickupMarker.remove();

        if (status == UNCERTAIN_STATUS)
            pickupMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker))
                    .position(location)
                    .title(markerTitleArr[PICKUP_MARKER])
                    .snippet(markerSnippetArr[PICKUP_MARKER]));
        else if (status == CONFIRMED_STATUS)
            pickupMarker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.house_map_icon))
                    .position(location)
                    .title(markerTitleArr[PICKUP_MARKER])
                    .snippet(markerSnippetArr[PICKUP_MARKER]));
    }

    /*Title:                   setMarkerText
    * Description:             Changes the Title and Snippet part of specified
    *                          marker's information window.
    *
    * @param       markerId    Integer representing the marker to manipulate.
    * @param       title       String specifying the title.
    * @param       snippet     String specifying the body.
    */
    public void setMarkerText(int markerId, String title, String snippet) {
        markerTitleArr[markerId] = title;
        markerSnippetArr[markerId] = snippet;

        Marker marker = getMarkerFromId(markerId);

        if (marker != null)
            marker.remove();

        marker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.red_marker))
                .position(marker.getPosition())
                .title(markerTitleArr[markerId])
                .snippet(markerSnippetArr[markerId]));
    }

    /*Title:                   getMarkerFromId
    * Description:             Returns Marker object from integer Id.
    *
    * @param       markerId    Integer representing the marker to find.
    * @return                  Marker corresponding to markerId.
    */
    public Marker getMarkerFromId(int markerId) {
        Marker marker;

        switch (markerId) {
            case USER_LOCATION_MARKER:
                marker = userLocationMarker;
                break;
            case PICKUP_MARKER:
                marker = pickupMarker;
                break;
            case DESTINATION_MARKER:
                marker = destinationMarker;
                break;
            default:
                return null;
        }

        return marker;
    }

    /*Title:               getAddressFromLocation
    * Description:         Acquires the most likely address of a specific Location object.
    *
    * @param       loc     Location object which dictates the lat/long to determine
    *                      the address of.
    */
    public Address getAddressFromLocation (Location loc) {
        List<Address> addresses;

        try {
            addresses = geoCoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
        } catch (IOException e) { addresses = null; }

        if (addresses != null)
            return addresses.get(0);
        else
            return null;
    }

    public Location getLocationFromAddress (Address addr) {
        Location retLoc = new Location("returnLocation");
        retLoc.setLatitude(addr.getLatitude());
        retLoc.setLongitude(addr.getLongitude());

        return retLoc;
    }


    /*Title:                navigateToCoordinates
    * Description:          Opens default navigation software to begin routing to specified
    *                       latitude and longitude.
    *
    * @param        lat     Input latitude of location to route to.
    * @param        lon     Input longitude of location to route to.
    */
    public void navigateToCoordinates(double lat, double lon) {
        String format = "geo:0,0?q=" + lat + "," + lon + "( Location title)";

        Uri uri = Uri.parse(format);


        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        getApplicationContext().startActivity(intent);
    }

    public List<Address> resultListFromUserInput(String input, Location radialCenter) {
        Geocoder geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {
            double lowerLeftLat = radialCenter.getLatitude()
                    - getRadialDegreeDistance("Lat", radialCenter);
            double lowerLeftLong = radialCenter.getLongitude()
                    - getRadialDegreeDistance("Long", radialCenter);
            double upperRightLat = radialCenter.getLatitude()
                    + getRadialDegreeDistance("Lat", radialCenter);
            double upperRightLong = radialCenter.getLongitude()
                    + getRadialDegreeDistance("Long", radialCenter);

            List<Address> addressList = geoCoder.getFromLocationName(input, MAX_RESULTS, lowerLeftLat,
                    lowerLeftLong, upperRightLat, upperRightLong);

            return addressList;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getRadialDegreeDistance(String param, Location radialCenter) {
        switch (param) {
            case "Lat":
                return (1 / 110.54) * 30 * 0.621371;
            case "Long":
                return (1 / (111.320 * Math.cos(radialCenter.getLatitude())))
                        * 30 * 0.621371;
            default:
                return 0;
        }
    }
}
