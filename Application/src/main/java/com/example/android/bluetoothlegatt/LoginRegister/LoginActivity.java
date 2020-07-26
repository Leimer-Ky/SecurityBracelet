package com.example.android.bluetoothlegatt.LoginRegister;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.bluetoothlegatt.DatabaseURL;
import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.UserInterfaz;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LoginActivity extends Activity {

    private EditText etUsername, etPassword;
    private TextView registerLink;
    private Button btnLogin;
    ProgressBar loading;
    private static String URL_LOGIN = DatabaseURL.LOGIN;
    private final static String TAG = LoginActivity.class.getSimpleName();
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getActionBar().setTitle("Security Bracalet");
        sessionManager = new SessionManager(this);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        registerLink = (TextView) findViewById(R.id.tvRegisterHere);
        btnLogin =(Button) findViewById(R.id.bLogin);
        loading = findViewById(R.id.loading);

        //handleSSLHandshake();
        //new NukeSSLCerts().nuke();
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerIntent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mUsername = etUsername.getText().toString().trim();
                String mPass = etPassword.getText().toString().trim();

                if (!mUsername.isEmpty() && !mPass.isEmpty()){
                    loading.setVisibility(View.VISIBLE);
                    btnLogin.setVisibility(View.GONE);
                    LoginDB(mUsername,mPass);
                } else {
                    etUsername.setError("PorFavor Inserte Usuario");
                    etPassword.setError("PorFavor Inserte Password");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }
    public static class NukeSSLCerts {
        protected static final String TAG = "NukeSSLCerts";

        public static void nuke() {
            try {
                TrustManager[] trustAllCerts = new TrustManager[] {
                        new X509TrustManager() {
                            public X509Certificate[] getAcceptedIssuers() {
                                X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                                return myTrustedAnchors;
                            }

                            @Override
                            public void checkClientTrusted(X509Certificate[] certs, String authType) {}

                            @Override
                            public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                        }
                };

                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String arg0, SSLSession arg1) {
                        return true;
                    }
                });
            } catch (Exception e) {
            }
        }
    }
    private void updateAndroidSecurityProvider() {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (Exception e) {
            Log.e("SecurityException", "Google Play Services not available.");
        }
    }
    private void LoginDB(final String username, final String password){
       // updateAndroidSecurityProvider();
        Toast.makeText(LoginActivity.this, "Entre a login", Toast.LENGTH_SHORT).show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN, new Response.Listener<String>() {
            @Override

            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("success");
                    JSONArray jsonArray = jsonResponse.getJSONArray("login");
                    Toast.makeText(LoginActivity.this, "json: "+jsonArray.toString(), Toast.LENGTH_SHORT).show();
                    if (success.equals("1")){
                        for (int i=0; i < jsonArray.length(); i++){
                            JSONObject object = jsonArray.getJSONObject(i);
                            //Toast.makeText(LoginActivity.class, "Datos insertado con éxito", Toast.LENGTH_LONG).show();

                            String name = object.getString("name").trim();
                            String username = object.getString("username").trim();
                            String numero = object.getString("numero").trim();
                            String cedula = object.getString("cedula").trim();
                            String imagen = object.getString("imagen").trim();

                            sessionManager.createSession(name, username, numero, cedula, imagen);

                            Intent intent = new Intent(LoginActivity.this, UserInterfaz.class);
                            intent.putExtra("name", name);
                            intent.putExtra("username", username);
                            intent.putExtra("cedula", cedula);
                            intent.putExtra("imagen", imagen);
                            startActivity(intent);
                        }

                    }else{
                        alertBuiler("Error!!! \nUsuario ó Contraseña Incorrecta","Reintentar");
                        loading.setVisibility(View.GONE);
                        btnLogin.setVisibility(View.VISIBLE);

                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    alertBuiler(e.toString(),"Reintentar");
                    loading.setVisibility(View.GONE);
                    btnLogin.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                alertBuiler(error.toString(),"Reintentar");
                loading.setVisibility(View.GONE);
                btnLogin.setVisibility(View.VISIBLE);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void alertBuiler(String titte, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setMessage(titte)
                .setNegativeButton(message, null)
                .create()
                .show();

    }
}
