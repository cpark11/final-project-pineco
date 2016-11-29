package com.pineco.flickrtron;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static com.pineco.flickrtron.MainActivity.API_URL;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    static final String SECRET = "93398852639b6343";
    static final String API_KEY = "379c73dfd6eede56394f7dc6ab60921a";
    static final String FROB_URL = "https://flickr.com/services/rest/?api_key=379c73dfd6eede56394f7dc6ab60921a&method=flickr.auth.getFrob&api_sig=28ced0166fbd5c24c8bfe02bb91b2fb1";
    static final String AUTH_URL = "https://flickr.com/services/auth/?api_key=379c73dfd6eede56394f7dc6ab60921a&perms=write&frob=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    return true;
                }
                return false;
            }
        });


        Button LoginButton = (Button) findViewById(R.id.login_button);
        LoginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new Authenticator().execute();
            }
        });


    }
    class Authenticator extends AsyncTask<Void, Void, String> {

        private Exception exception;
        String frob;
        protected void onPreExecute() {

        }

        protected String doInBackground(Void... urls) {
            try {
                URL url = new URL(FROB_URL);
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
                Log.d("myHash",AUTH_URL+frob+"&api_sig="+hashtext);
                WebView myWebView = (WebView) findViewById(R.id.webview);
                myWebView.loadUrl(AUTH_URL+frob+"&api_sig="+hashtext);
                Intent intent = new Intent(getApplicationContext(),VerifyActivity.class);
                intent.putExtra("frob",frob);
                startActivity(intent);
            }
            catch(Exception e) {
                Log.e("ERROR", e.getMessage(), e);
                return;
            }


        }
    }

}

