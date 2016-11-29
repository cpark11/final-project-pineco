package com.pineco.flickrtron;


import android.Manifest;
import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.provider.MediaStore;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LocationListener{

    EditText tag;
//    TextView responseView;
    ProgressBar progressBar;
    LocationManager locationManager;


    static final String API_KEY = "379c73dfd6eede56394f7dc6ab60921a";
    static final String API_URL = "https://api.flickr.com/services/rest/?method=flickr.photos.search";
    static final String LOC_URL = "https://api.flickr.com/services/rest/?method=flickr.places.findbyLatLon&nojsoncallback=1&api_key=379c73dfd6eede56394f7dc6ab60921a";
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;

    public static final String PREFS_NAME = "PrefsFile";
    //static final int REQUEST_IMAGE_CAPTURE = 1;

    //ID for camera permission request
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_LOCATION = 1;
    //Permission to access geolocation
    public String myLocation = "";
    public double latitude;
    public double longitude;
    public Criteria criteria;
    public String bestProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageButton locationButton = (ImageButton)findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
                //searchWithLocation(38.01538, -78.30396);
            }
        });
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Camera permission has not been granted.
                    requestCameraPermission();

                } else {

                    // Camera permissions is already available, show the camera preview.
                    Log.i("INFO",
                            "CAMERA permission has already been granted. Displaying camera preview.");
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_CAMERA);
                }



                }
            }
        });

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String editTextValue = settings.getString("editTextValue", "none");

        EditText search = (EditText)findViewById(R.id.tag);
        search.setText(editTextValue);

//        responseView = (TextView) findViewById(R.id.responseView);
        tag = (EditText) findViewById(R.id.tag);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Set Search EditText onActionDone Listener
        search.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    new RetrieveFeedTask().execute(tag.getText().toString());
                    return true;
                }
                return false;
            }
        });

    }

    /**
     * All Location finding code
     * @param context
     * @return
     */
    public static boolean isLocationEnabled(Context context)
    {
        //...............
        return true;
    }

    protected void getLocation(){
        if (isLocationEnabled(MainActivity.this)){
            locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
            criteria = new Criteria();
            bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Fine Location Access permission has not been granted.
                //Toast.makeText(getApplicationContext(), "permission 1", Toast.LENGTH_LONG).show();
                requestLocationPermission();

            }
            else{
                Location location = locationManager.getLastKnownLocation(bestProvider);
                if (location != null) {
                    Toast.makeText(getApplicationContext(), "get last known location", Toast.LENGTH_LONG).show();
                    Log.e("TAG", "GPS is on");
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
                    searchWithLocation(latitude, longitude);
                }
                else {
                    //If you do locationManager.GPS_PROVIDER in rice, for some reason it doesn't work/takes a long time
                    locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 0, 0, this);
                }
            }

        }
        else{
            //prompt user to enable location....
            //.................
        }
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            // Fine Location Access permission has not been granted.
//
//            requestLocationPermission();
//
//        }
//        else {
//            locationManager.removeUpdates(this);
//        }
//    }

    @Override
    public void onLocationChanged(Location location) {
        //remove location callback:
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Fine Location Access permission has not been granted.

            requestLocationPermission();

        }
        else{
            locationManager.removeUpdates(this);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Toast.makeText(MainActivity.this, "latitude:" + latitude + " longitude:" + longitude, Toast.LENGTH_SHORT).show();
            searchWithLocation(latitude, longitude);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public void searchWithLocation(double lat, double longi){
        Log.i("searchworks", "lat: " + lat + ", long: " + longi);
        String[] latlon = {lat+"",longi+""};
        new Locator().execute(latlon);
    }


    /**
     * All Permission related code
     */
    private void requestCameraPermission() {
        Log.i("INFO", "CAMERA permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i("INFO",
                    "Displaying camera permission rationale to provide additional context.");
            Snackbar.make(findViewById(R.id.coordinator_layout), R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA);
                        }
                    })
                    .show();
        } else { // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
        // END_INCLUDE(camera_permission_request)
    }

    /**
     * Requests the Location permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestLocationPermission() {
        Log.i("INFO", "LOCATION permission has NOT been granted. Requesting permission.");

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i("INFO",
                    "Displaying location permission rationale to provide additional context.");
            Snackbar.make(findViewById(R.id.coordinator_layout), R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {// No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CAMERA) {

            Log.i("INFO", "Received response for Camera permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i("INFO", "CAMERA permission has now been granted.");
                Snackbar.make(findViewById(R.id.coordinator_layout), R.string.permision_available_camera,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i("INFO", "CAMERA permission was NOT granted.");
                Snackbar.make(findViewById(R.id.coordinator_layout), R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();

            }

        } else if (requestCode == REQUEST_LOCATION) {
            Log.i("INFO", "Received response for location permissions request.");
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("INFO", "Location permissions were granted.");
                Snackbar.make(findViewById(R.id.coordinator_layout), R.string.permision_available_location,
                        Snackbar.LENGTH_SHORT)
                        .show();
            } else {
                Log.i("INFO", "Location permissions were NOT granted.");
                Snackbar.make(findViewById(R.id.coordinator_layout), R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    class RetrieveFeedTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            //responseView.setText("");
        }

        protected String doInBackground(String... args) {
            try {
                URL url = new URL(API_URL + "&per_page=5&nojsoncallback=1&format=json&tags=" + args[0] + "&api_key=" + API_KEY);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            progressBar.setVisibility(View.GONE);
            Log.i("INFO", response);
//            responseView.setText(response);
            // TODO: check this.exception
            // TODO: do something with the feed
            try {
                RecyclerView recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
                recyclerView.setHasFixedSize(true);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerView.setLayoutManager(layoutManager);

                //Fill list of images for recyclerview
                ArrayList<FlickrImage> imageList = new ArrayList<>();
                JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                JSONObject photos = object.getJSONObject("photos");
                JSONArray photoArray = photos.getJSONArray("photo");
                for(int i = 0; i < photoArray.length(); i++){
                    JSONObject p = photoArray.getJSONObject(i);
                    FlickrImage f = new FlickrImage(p.getString("id"), p.getString("secret"),
                            p.getString("server"), p.getInt("farm"), p.getString("title"));
                    imageList.add(f);
                }

                DataAdapter adapter = new DataAdapter(getApplicationContext(),imageList);
                recyclerView.setAdapter(adapter);


//                JSONObject p1 = photoArray.getJSONObject(0);
//                url = "http://farm" + p1.get("farm") + ".static.flickr.com/"
//                        + p1.getString("server") + "/" + p1.getString("id") + "_" + p1.getString("secret") + "_m.jpg";
                //populateImages();

            } catch (JSONException e) {
                setTextField("No Photos");
            }
        }
    }
    class Locator extends AsyncTask<String[], Void, String> {
        private Exception exception;
        protected void onPreExecute() {
        }

        protected String doInBackground(String[]... args) {
            try {
                URL url = new URL(LOC_URL + "&lat=" + args[0][0] +"&lon=" + args[0][1]+"&format=json");
                Log.d("lat",args[0][0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                }
                finally{
                    urlConnection.disconnect();
                }
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return null;
            }
        }

        protected void onPostExecute(String response) {
            if(response == null) {
                response = "THERE WAS AN ERROR";
            }
            Log.i("INFO", response);
            try {
                JSONObject jsonreader = (JSONObject) new JSONTokener(response).nextValue();
                JSONObject places = jsonreader.getJSONObject("places");
                JSONArray place = places.getJSONArray("place");
                JSONObject myPlace = place.getJSONObject(0);
                myLocation=myPlace.getString("name").split(",")[0];
                setTextField(myLocation);
                Log.d("search",myLocation);
                myLocation=myLocation.replace(" ","_");
                new RetrieveFeedTask().execute(myLocation);
                // TODO: check this.exception
                // TODO: do something with the feed
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private void setTextField(String s) {
        tag.setText(s);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Using Preferences
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        EditText editText = (EditText) findViewById(R.id.tag);
        editor.putString("editTextValue", editText.getText().toString());

        // Commit the edits!
        editor.commit();
    }

}
