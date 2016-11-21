package com.pineco.flickrtron;

/**
 * Created by Eric on 11/7/2016.
 */

public class FlickrImage {
    private String id;
    private String secret;
    private String server;
    private int farm;
    private String title;
    private String url;


    FlickrImage(String _Id, String _Secret,
                String _Server, int _Farm, String _Title){
        id = _Id;
        secret = _Secret;
        server = _Server;
        farm = _Farm;
        title = _Title;

        url = "http://farm" + farm + ".static.flickr.com/"
                        + server + "/" + id + "_" + secret + "_m.jpg";
    }

    public String getCaption() {
        return title;
    }

    public void setCaption(String c) {
        this.title = c;
    }

    public String geturl() {
        return url;
    }

    public void seturl(String url) {
        this.url = url;
    }

}
