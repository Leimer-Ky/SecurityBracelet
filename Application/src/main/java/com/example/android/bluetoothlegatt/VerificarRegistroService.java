package com.example.android.bluetoothlegatt;

import android.app.Service;
import android.content.Intent;
import android.location.LocationListener;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VerificarRegistroService extends Service {

    private static final String URL_OBTENER_REGISTROS = DatabaseURL.OBTERNER_REGISTROS;
    String musername;
    int verificarRegistros = 0;

    public void onCreate() {
        super.onCreate();
        //aLatitud = new ArrayList();
        //aLongitud = new ArrayList();
        //aId = new ArrayList();
    }

    public int onStartCommand(Intent intent, int flags, int starId) {
        super.onStartCommand(intent, flags, starId);
        musername = intent.getStringExtra("username");
        String p = intent.getStringExtra("p");
        Log.d(VerificarRegistroService.class.getSimpleName(), "Init Servce");
        if (p!=null){
            //Toast.makeText(getApplicationContext(), "Eliminado", Toast.LENGTH_SHORT).show();

        }

        BDverificarRegistro(musername);

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
        //mlocManager.removeUpdates((LocationListener) Local);
    }

    private void BDverificarRegistro(final String username){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_OBTENER_REGISTROS+"?username="+username, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d(VerificarRegistroService.class.getSimpleName(), "Init Servce");
                    //veri = veri +1;
                    //Toast.makeText(UserInterfaz.this, "dato Registro: ", Toast.LENGTH_SHORT).show();
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("registro");
                    verificarRegistros = jsonArray.length();
                    if (verificarRegistros == 1){
                        //txtNotificacion.setVisibility(View.VISIBLE);
                        //txtNotificacion.setText("1");
                    }else{
                        //txtNotificacion.setVisibility(View.INVISIBLE);
                    }

                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //alertBuiler("Error..!", "No Hay Conexion a Internet..");
            }
        });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);


    }

    public int Regreso(){
        int i = 0;
        i = i + 1;

        return i;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
