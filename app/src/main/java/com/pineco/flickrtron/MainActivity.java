package com.pineco.flickrtron;


import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.provider.MediaStore;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

public class MainActivity extends AppCompatActivity {

    EditText tag;
//    TextView responseView;
    ProgressBar progressBar;
    LocationManager locationManager;


    static final String API_KEY = "379c73dfd6eede56394f7dc6ab60921a";
    static final String API_URL = "https://api.flickr.com/services/rest/?method=flickr.photos.search";
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 1;

    public static final String PREFS_NAME = "PrefsFile";
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        ImageButton locationButton = (ImageButton)findViewById(R.id.locationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) throws SecurityException{
                Location location= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                searchWithLocation(location.getLatitude(), location.getLongitude());
                tag.setText(location.getLatitude()+"");
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
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

    public void searchWithLocation(double lat, double longi){
        new RetrieveFeedTask().execute(lat+", "+longi);
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
                e.printStackTrace();
            }
        }
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
