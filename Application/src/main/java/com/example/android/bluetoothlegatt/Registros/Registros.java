package com.example.android.bluetoothlegatt.Registros;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.bluetoothlegatt.DatabaseURL;
import com.example.android.bluetoothlegatt.R;
import com.example.android.bluetoothlegatt.UbicacionService;
import com.example.android.bluetoothlegatt.UserInterfaz;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

public class Registros extends Activity {

    private ListView ListaRegistro;
    private final static String TAG = Registros.class.getSimpleName();

    private ArrayList aname, acedula, ausername , aId;
    boolean connected = false;
    String musername;
    private AsyncHttpClient cliente;

    private static final String URL_OBTENER_REGISTROS = DatabaseURL.OBTERNER_REGISTROS;
    private static final String URL_ELIMINAR_DIRECCION = DatabaseURL.ELIMINAR_DIRECCION;

    Handler handler;

    @Override
    protected void onCreate (Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registros);

        ListaRegistro = (ListView) findViewById(R.id.ListaRegistro);

        aname = new ArrayList();
        acedula = new ArrayList();
        ausername = new ArrayList();
        aId = new ArrayList();

        cliente = new AsyncHttpClient();

        getData();
    }

    private void getData(){
        Intent intent = getIntent();
        musername = intent.getStringExtra("username");
        if (musername != null) {
            handler = new Handler();
            Runnable refresh = new Runnable(){
                @Override
                public void run(){
                    checkNetworkConnection(Registros.this);
                    if (connected){
                        getRegistroDB(musername);
                    }else{
                        Toast.makeText(getBaseContext(), "Sin Conexion a Internet.\n   Reintentando...", Toast.LENGTH_SHORT).show();
                    }
                    handler.postDelayed(this,1000);
                }
            };
            handler.postDelayed(refresh,1000);
        }


    }

    public void getRegistroDB(final String username){
        aname.clear();
        acedula.clear();
        aId.clear();
        ausername.clear();
        Log.d(Registros.class.getSimpleName(), "BDObtener Registro");
        Toast.makeText(getBaseContext(), "url: " + URL_OBTENER_REGISTROS+"?username="+username, Toast.LENGTH_SHORT).show();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_OBTENER_REGISTROS+"?username="+username, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("registro");
                    for (int i = 0; i < jsonArray.length(); i++){
                        aId.add(jsonArray.getJSONObject(i).getString("id"));
                        aname.add(jsonArray.getJSONObject(i).getString("name"));
                        acedula.add(jsonArray.getJSONObject(i).getString("cedula"));
                        ausername.add(jsonArray.getJSONObject(i).getString("username"));

                    }
                    handler.removeMessages(0);
                    //Log.d(Registros.class.getSimpleName(), "BDObtener Registro 2");
                    if (jsonArray.length() == 1){

                    }else{
                        Toast.makeText(getBaseContext(), "jsonArray: " + jsonArray, Toast.LENGTH_SHORT).show();

                        Log.d(Registros.class.getSimpleName(), "Usted no cuenta con ningun Registro.!");
                        Toast.makeText(getBaseContext(), "Usted no cuenta con ningun Registro.!: d" + URL_OBTENER_REGISTROS+"?username="+username , Toast.LENGTH_SHORT).show();
                        //alertBuiler("Importante", "Usted no cuenta con ningun Registro");
                        aname.clear();
                        acedula.clear();
                        aId.clear();
                        ausername.clear();
                        //ListaRegistro.setAdapter(new CustomAdapter(getApplicationContext()));

                    }
                    ListaRegistro.setAdapter(new CustomAdapter(getApplicationContext()));


                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getBaseContext(), "Sin Internet.!\nIntentelo de nuevo", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);

    }

    private void removeLocationDB(final String ID){
        Toast.makeText(getBaseContext(), "Id: "+ ID, Toast.LENGTH_SHORT).show();
        //String url = "https://lguambanac.000webhostapp.com/SBOPAbd/Eliminar.php?Id="+ID;
        cliente.post(URL_ELIMINAR_DIRECCION+ID, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                if(statusCode == 200){
                    Log.d(TAG, "Eliminado con exito");
                    Toast.makeText(getApplicationContext(), "Direccion Eliminada con Exito", Toast.LENGTH_SHORT).show();
                    try {
                        Thread.sleep(1000);
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                    getRegistroDB(musername);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    private class CustomAdapter extends BaseAdapter {

        Context ctx;
        LayoutInflater layoutInflater;
        Button btnBorrar;
        TextView txtDatos;


        public CustomAdapter(Context applicationContext) {
            this.ctx = applicationContext;
            layoutInflater = (LayoutInflater) this.ctx.getSystemService(LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return aId.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewGroup viewGroup = (ViewGroup) layoutInflater.inflate(R.layout.list_registros, null);
            txtDatos = (TextView) viewGroup.findViewById(R.id.IdDatos);
            btnBorrar = (Button) viewGroup.findViewById(R.id.bEliminar);

            final String ID = aId.get(position).toString();


            Log.d(TAG, "LISTA\n" + "Nombre: " + aname.get(position).toString() + "\nUsuario: " + ausername.get(position).toString() + "\nCedula: " + acedula.get(position).toString());
            btnBorrar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                    if (networkInfo != null && networkInfo.isConnected()) {
                        removeLocationDB(ID);
                    } else {
                        alertBuiler("Error..!", "No Hay Conexion a Internet..");
                    }

                }
            });

            txtDatos.setText("Nombre: " + aname.get(position).toString() + "\nUsuario: " + ausername.get(position).toString() + "\nCedula: " + acedula.get(position).toString() ) ;


            return viewGroup;
        }

    }

    private boolean checkNetworkConnection(Context context) {

        connected = false;

        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Recupera todas las redes (tanto móviles como wifi)
        NetworkInfo[] redes = connec.getAllNetworkInfo();

        for (int i = 0; i < redes.length; i++) {
            // Si alguna red tiene conexión, se devuelve true
            if (redes[i].getState() == NetworkInfo.State.CONNECTED) {
                connected = true;
            }
        }
        return connected;
    }

    private void alertBuiler(String titte, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(Registros.this);
        builder.setMessage(titte)
                .setNegativeButton(message, null)
                .create()
                .show();

    }
}
