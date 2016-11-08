package com.pineco.flickrtron;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Eric on 11/7/2016.
 */

public class FlickrImage {
    String id;
    String owner;
    String secret;
    String server;
    String farm;
    String title;

    Bitmap FlickrBitmap;

    FlickrImage(String _Id, String _Owner, String _Secret,
                String _Server, String _Farm, String _Title){
        id = _Id;
        owner = _Owner;
        secret = _Secret;
        server = _Server;
        farm = _Farm;
        title = _Title;

        FlickrBitmap = preloadBitmap();
    }

    private Bitmap preloadBitmap(){
        Bitmap bm= null;

        String FlickrPhotoPath =
                "http://farm" + farm + ".static.flickr.com/"
                        + server + "/" + id + "_" + secret + "_m.jpg";

        URL FlickrPhotoUrl = null;

        try {
            FlickrPhotoUrl = new URL(FlickrPhotoPath);

            HttpURLConnection httpConnection
                    = (HttpURLConnection) FlickrPhotoUrl.openConnection();
            httpConnection.setDoInput(true);
            httpConnection.connect();
            InputStream inputStream = httpConnection.getInputStream();
            bm = BitmapFactory.decodeStream(inputStream);

        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return bm;
    }

    public Bitmap getBitmap(){
        return FlickrBitmap;
    }

}
