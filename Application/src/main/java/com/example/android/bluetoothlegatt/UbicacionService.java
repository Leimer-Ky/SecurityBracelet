package com.example.android.bluetoothlegatt;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.bluetoothlegatt.LoginRegister.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UbicacionService extends Service {
    String musername, ID, latitud, longitud, newlatitud, newlongitud;
    private static int tiempoUbicacion = 5000;
    private ArrayList aLongitud, aLatitud, aId;
    double Latitud, Longitud;

    LocationManager mlocManager;
    Localizacion Local = new Localizacion();

    private static final String URL_OBTERNER_REGISTROS = DatabaseURL.OBTERNER_REGISTROS;
    private static final String URL_MODIFICAR_UBICACION = DatabaseURL.MODIFICAR_UBICACION;

    private final static String TAG = UbicacionService.class.getSimpleName();

    public void onCreate() {
        super.onCreate();
        aLatitud = new ArrayList();
        aLongitud = new ArrayList();
        aId = new ArrayList();
    }

    public int onStartCommand(Intent intent, int flags, int starId) {
        super.onStartCommand(intent, flags, starId);
        musername = intent.getStringExtra("username");
        String p = intent.getStringExtra("p");

        if (p!=null){
            Toast.makeText(getApplicationContext(), "Eliminado", Toast.LENGTH_SHORT).show();

        }

        checkLatLonDB(musername);
        Toast.makeText(UbicacionService.this, " Entre al service", Toast.LENGTH_SHORT).show();

        /**
         new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
        //code inizialite
        }
        }, 10000);
         **/

        return START_STICKY;

    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Stop Service");
        mlocManager.removeUpdates((LocationListener) Local);
    }

    private void checkLatLonDB(final String username) {
        aLatitud.clear();
        aLongitud.clear();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_OBTERNER_REGISTROS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d(TAG, "Comprobar Lat y Lon");
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("registro");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        aId.add(jsonArray.getJSONObject(i).getString("id"));
                        aLatitud.add(jsonArray.getJSONObject(i).getString("latitud"));
                        aLongitud.add(jsonArray.getJSONObject(i).getString("longitud"));
                        ID = aId.get(i).toString();
                    }

                    if (jsonArray.length() == 1 && aLatitud.toString() != null && aLongitud.toString() != null) {
                        latitud = aLatitud.toString();
                        longitud = aLongitud.toString();
                        Log.d(TAG, "localizacion Start");
                        locationStart();
                    }else{
                        Log.d(TAG, "No localizacion Start");
                        mlocManager.removeUpdates((LocationListener) Local);
                        stopSelf();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                ///**************
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void locationStart() {
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Local.setUserInterfaz(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, tiempoUbicacion, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, tiempoUbicacion, 0, (LocationListener) Local);
    }

    public class Localizacion implements LocationListener {
        UbicacionService MainActivity;
        public UbicacionService getMainActivity() {

            return MainActivity;
        }
        public void setUserInterfaz(UbicacionService MainActivity) {

            this.MainActivity = MainActivity;
        }
        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            Latitud = loc.getLatitude();
            Longitud = loc.getLongitude();

            newlatitud =String.valueOf(Latitud) ;
            newlongitud= String.valueOf(Longitud);

            if (newlatitud != latitud || newlongitud != longitud){
                Toast.makeText(UbicacionService.this, "Lat: " + newlatitud + " \nLon:" + newlongitud, Toast.LENGTH_SHORT).show();
                updateLocationDB(newlatitud, newlongitud);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //checkLatLonDB(musername);
                    }
                }, 500);
            }


            //modifico datos
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }

    public void  updateLocationDB(final String Latitud, final String Longitud){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_MODIFICAR_UBICACION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Si modifique la Ubicacion");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                ///**************
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", ID);
                params.put("latitud", Latitud );
                params.put("longitud", Longitud );
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
