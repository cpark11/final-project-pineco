package com.pineco.flickrtron;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

public class VerifyActivity extends AppCompatActivity {
    private TextView alertMessage;
    private Button button;
    private String frob;
    static final String SECRET = "93398852639b6343";
    static final String API_KEY = "379c73dfd6eede56394f7dc6ab60921a";
    static final String TOKEN_URL = "http://flickr.com/services/rest/?method=flickr.auth.getToken";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        alertMessage = (TextView)findViewById(R.id.textView);
        button = (Button)findViewById(R.id.button);
        frob = getIntent().getStringExtra("frob");
        new CheckAuth().execute();

    }
    class CheckAuth extends AsyncTask<Void, Void, String> {

        private Exception exception;
        protected void onPreExecute() {

        }

        protected String doInBackground(Void... urls) {

            try {
                String plaintext = SECRET+"api_key379c73dfd6eede56394f7dc6ab60921afrob"+frob+"methodflickr.auth.getToken";
                MessageDigest m = MessageDigest.getInstance("MD5");
                m.update(plaintext.getBytes());
                byte[] digest = m.digest();
                BigInteger bigInt = new BigInteger(1,digest);
                String hashtext = bigInt.toString(16);
                while(hashtext.length() < 32 ){
                    hashtext = "0"+hashtext;
                }
                URL url = new URL(TOKEN_URL+"&api_key="+API_KEY+"&frob="+frob+"&api_sig="+hashtext);
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
                String[] rspLines = response.split("\n");
                String frob = rspLines[2].substring(6,rspLines[2].length()-7);
                String plaintext = SECRET+"api_key379c73dfd6eede56394f7dc6ab60921afrob"+frob+"permswrite";
                MessageDigest m = MessageDigest.getInstance("MD5");
                m.update(plaintext.getBytes());
                byte[] digest = m.digest();
                BigInteger bigInt = new BigInteger(1,digest);
                String hashtext = bigInt.toString(16);
                while(hashtext.length() < 32 ){
                    hashtext = "0"+hashtext;
                }

            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return;
            }


        }
    }
}
