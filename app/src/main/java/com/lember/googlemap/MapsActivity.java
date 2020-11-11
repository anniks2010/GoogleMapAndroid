package com.lember.googlemap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    FusedLocationProviderClient client;
    SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        client= LocationServices.getFusedLocationProviderClient(this);
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
       /* mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
       getCurrentLocation();
    }
    private  void getCurrentLocation(){
        if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.ACCESS_COARSE_LOCATION)!=
                        PackageManager.PERMISSION_GRANTED){
          return;
        }
        Task<Location> locationTask = client.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null){
                    mapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            mMap=googleMap;

                            final LatLng latLng = new LatLng(location.getAltitude(),location.getLongitude());
                            mMap.addMarker(new MarkerOptions().position(latLng).title("You're here"));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15)); ///15 on tänava levelil, 20 on maja levelil
                            GroundOverlayOptions locationOverlay= new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.ring)).position(latLng,100);
                            mMap.addGroundOverlay(locationOverlay);

                            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker) {
                                    ///Tahan saada API-ga saada infot
                                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                                    String MapUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s,%s&radius=2500&type=restaurant&keyword=cruise&key=AIzaSyBg7bQghUMfGmLRDkZZC7Rh8zNCSFtQbcg";

                                    String latitude = String.valueOf(location.getLatitude());
                                    String longitude= String.valueOf(location.getLatitude());

                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, String.format(MapUrl, latitude, longitude), null, new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try{
                                                final StringBuilder formattedResult = new StringBuilder();
                                                JSONArray responseJSON = response.getJSONArray("result");
                                                for(int i =0; i<responseJSON.length(); i++){
                                                    formattedResult.append("\n" + responseJSON.getJSONObject(i).get("name") +": \t" + responseJSON.getJSONObject(i).get("rating"));
                                                }
                                                new AlertDialog.Builder(MapsActivity.this)
                                                        .setTitle("List of resturants near you")
                                                        .setMessage("Name: " + "\t Rating"+formattedResult)
                                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                            }
                                                        }).show();
                                            } catch (JSONException jsonException){
                                                Log.e("JSON", String.valueOf(jsonException));
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e("JSON", String.valueOf(error));
                                        }
                                    });
                                    requestQueue.add(jsonObjectRequest);
                                    return true;
                                }
                            });
                        }
                    });
                }
            }
        });
    }
}