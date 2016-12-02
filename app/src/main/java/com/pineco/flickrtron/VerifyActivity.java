package com.pineco.flickrtron;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Stack;

public class VerifyActivity extends AppCompatActivity {
    private TextView alertMessage;
    private Button button;
    private String frob;
    private String token;
    private String nsid;
    private String name;
    static final String SECRET = "93398852639b6343";
    static final String API_KEY = "379c73dfd6eede56394f7dc6ab60921a";
    static final String TOKEN_URL = "http://flickr.com/services/rest/?method=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);
        alertMessage = (TextView)findViewById(R.id.textView);
        button = (Button)findViewById(R.id.button2);
        button.setText("Confirm Identity");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.putExtra("token",token);
                intent.putExtra("nsid",nsid);
                intent.putExtra("name",name);
                if(token!=null)
                    startActivity(intent);
            }
        });
        frob = getIntent().getStringExtra("frob");
        new CheckAuth().execute();

    }
    class CheckAuth extends AsyncTask<Void, Void, String> {

        private Exception exception;
        protected void onPreExecute() {

        }

        protected String doInBackground(Void... urls) {
            try {
                Stack<String> stk = new Stack<>();
                stk.push("nojsoncallback=1");
                stk.push("method=flickr.auth.getToken");
                stk.push("frob="+frob);
                stk.push("format=json");
                stk.push("api_key=379c73dfd6eede56394f7dc6ab60921a");
                String s = MainActivity.generateSig(stk);
                Log.d("url",s);
                URL url = new URL(s);
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
                JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                JSONObject auth = object.getJSONObject("auth");
                JSONObject token = auth.getJSONObject("token");
                String myToken = token.getString("_content");
                JSONObject user = auth.getJSONObject("user");
                String fullname = user.getString("fullname");
                String nsid = user.getString("nsid");
                setUserInfo(myToken,fullname,nsid);
            }
            catch(Exception e){
                Log.e("ERROR", e.getMessage(), e);
                return;
            }

        }
    }

    private void setUserInfo(String myToken, String fullname, String nsid) {
        alertMessage.setText("Welcome,"+fullname+", your token is "+myToken);
        this.name=fullname;
        this.token=myToken;
        this.nsid=nsid;

    }
}
