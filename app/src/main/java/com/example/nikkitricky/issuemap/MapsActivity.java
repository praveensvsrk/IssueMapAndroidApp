package com.example.nikkitricky.issuemap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerDragListener, GoogleMap.OnMarkerClickListener, LocationListener {

    private GoogleMap mMap;
    private LocationManager lm;
    Double lon, lat, offset;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "MapsActivity";
    JSONArray fetchedIssues;
    Set<Integer> oldIssues = new HashSet<>();
    Set<Integer> newIssues = new HashSet<>();
    Marker curMarker;

    String url = "http://153eaebe.ngrok.io/issue/issuelist/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void fetchNearbyIssues() {

        String l_url = url + "?lat=" + lat + "&long=" + lon + "&offset=" + Math.abs(offset);

        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
            (Request.Method.GET, l_url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    fetchedIssues = response;
                    JSONObject obj = null;

                    Double l_lat = null;
                    Double l_lon = null;
                    LatLng l_latlon = null;

                    if(oldIssues == null){
                        for (int i=0; i<fetchedIssues.length(); i++){
                            try {
                                oldIssues.add(fetchedIssues.getJSONObject(i).getInt("id"));
                                obj = fetchedIssues.getJSONObject(i);
                                int issueId = obj.getInt("id");
                                l_lat = obj.getDouble("latitude");
                                l_lon = obj.getDouble("longitude");
                                l_latlon = new LatLng(l_lat, l_lon);

                                String l_desc = obj.getString("description");

                                Marker marker = mMap.addMarker(new MarkerOptions().position(l_latlon).title(
                                        l_desc.substring(0,l_desc.length() > 10 ? 10 : l_desc.length())));
                                marker.setTag(issueId);

                                Log.d("HII", "Adding " + l_latlon.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        for (int i=0; i<fetchedIssues.length(); i++){
                            try {
                                newIssues.add(fetchedIssues.getJSONObject(i).getInt("id"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        newIssues.removeAll(oldIssues);

                        for (int i=0; i<fetchedIssues.length(); i++){

                            try {
                                obj = fetchedIssues.getJSONObject(i);
                                int issueId = obj.getInt("id");
                                if(!newIssues.contains(issueId))
                                    continue;
                                oldIssues.add(issueId);
                                l_lat = obj.getDouble("latitude");
                                l_lon = obj.getDouble("longitude");
                                l_latlon = new LatLng(l_lat, l_lon);

                                String l_desc = obj.getString("description");

                                Marker marker = mMap.addMarker(new MarkerOptions().position(l_latlon).title(
                                        l_desc.substring(0,l_desc.length() > 10 ? 10 : l_desc.length())));
                                marker.setTag(issueId);

                                Log.d("HII", "Adding " + l_latlon.toString());

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }


                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO: Handle error
                    error.printStackTrace();
                }
            });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Access the RequestQueue through your singleton class.
        RequestQueueSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
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

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            }

            return;
        }

        Location location = lm.getLastKnownLocation(lm.NETWORK_PROVIDER);
        onLocationChanged(location);

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason ==REASON_GESTURE) {
                    //Toast.makeText(MapsActivity.this, "The user gestured on the map.",
                      //      Toast.LENGTH_SHORT).show();

                    LatLngBounds curScreen = mMap.getProjection().getVisibleRegion().latLngBounds;
                    offset = (curScreen.getCenter().latitude - curScreen.northeast.latitude);
                    lat = curScreen.getCenter().latitude;
                    lon = curScreen.getCenter().longitude;

                    fetchNearbyIssues();

                } else if (reason ==REASON_API_ANIMATION) {
//                    Toast.makeText(MapsActivity.this, "The user tapped something on the map.",
//                            Toast.LENGTH_SHORT).show();
                } else if (reason ==REASON_DEVELOPER_ANIMATION) {
                  //  Toast.makeText(MapsActivity.this, "The app moved the camera.",
                    //        Toast.LENGTH_SHORT).show();
                }
            }

        });

        curMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        curMarker.setDraggable(true);
        curMarker.setTag(1);

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon),13.0f));
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMarkerDragListener(this);

    }

    public boolean onMarkerClick(final Marker marker) {

        Integer issueId = (Integer) marker.getTag();

        if (issueId != null && issueId != 1) {
          //  Toast.makeText(this,
            //        marker.getTitle() + " clicked, id: " + issueId,
              //      Toast.LENGTH_SHORT).show();
            viewIssueDetail(issueId);
        } else if(issueId == 1){
          //
            // Toast.makeText(this," current loc", Toast.LENGTH_SHORT).show();
        }


        return false;
    }

    public void home(View v){

    }
    public void profile(View v){

    }
    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        LatLng l_latlon = marker.getPosition();
        lat = l_latlon.latitude;
        lon = l_latlon.longitude;
    }

    public File getFile(){
        File folder = new File("sdcard/issuemap");
        if(!folder.exists()){
            folder.mkdir();
        }
        File img_file = new File(folder, "issue_image.jpg");
        return img_file;
    }

    public void newissue(View v){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = getFile();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    public void viewIssueDetail(int issueId){
        Intent intent = new Intent(this, IssueDetailViewActivity.class);
        intent.putExtra("id", issueId);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent in = new Intent(this, NewIssue.class);
        Bundle bundle = new Bundle();
        LatLng l_latlon = curMarker.getPosition();
        lat = l_latlon.latitude;
        lon = l_latlon.longitude;
        bundle.putString("lat", lat + "");
        bundle.putString("lon", lon + "");
        in.putExtras(bundle);
        startActivity(in);
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


}

