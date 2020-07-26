

package com.example.android.bluetoothlegatt;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.policy.UploadPolicy;
import com.example.android.bluetoothlegatt.BluetoothLe.BluetoothLeService;
import com.example.android.bluetoothlegatt.BluetoothLe.DeviceScanActivity;
import com.example.android.bluetoothlegatt.LoginRegister.LoginActivity;
import com.example.android.bluetoothlegatt.LoginRegister.RegisterActivity;
import com.example.android.bluetoothlegatt.LoginRegister.SessionManager;
import com.example.android.bluetoothlegatt.Registros.Registros;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserInterfaz extends Activity {
    Button btnSalir, btnRegistro, btneditFoto, btnStopService;
    TextView txtMensajeEnviado, txtNombre, txtSend, txtLatitud, txtLongitud, txtCoordenadas,
            txtStreet, txtEstado, txtNotificacion, txtInternetStatus, txtStatusService, txtStatusNotification;
    CircleImageView imgProfile;
    RelativeLayout snackBarInternet;

    private final static String TAG = UserInterfaz.class.getSimpleName();
    private static final int conectarBT = 1;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private BluetoothLeService mBluetoothLeService;
    private String mDeviceName;
    private String mDeviceAddress;
    private boolean veriConnectedStatusBT = false;

    SessionManager sessionManager;
    String nameSessMang;
    String usernameSessMang;
    String numeroSessMang;
    String cedulaSessMang;
    String imagenSessMang;

    LocationManager mlocManager;
    Localizacion Local = new Localizacion();
    private double doubleLongitud;
    private double doubleLatitud;
    String stringLatitud;
    String stringLongitud;
    private boolean veriStateLocalizacion = false;

    private static final int cloudinaryImg = 2;
    private Bitmap bitmap;

    private static final String URL_INSERTAR_UBICACION = DatabaseURL.INSERTAR_UBICACION;
    private static final String URL_OBTENER_REGISTROS = DatabaseURL.OBTERNER_REGISTROS;
    private static final String URL_ELIMINAR_DIRECCION = DatabaseURL.ELIMINAR_DIRECCION;

    int veriRegistrosGps = 0;

    boolean veriConnectedNet = false;
    boolean veriConnectedInternet = false;
    boolean veriUploadLocation = false;

    String idEliminar;

    //@SuppressLint("WrongViewCast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_interfaz);

        btnSalir = (Button) findViewById(R.id.btnSalir);
        btneditFoto = (Button) findViewById(R.id.btnEditarfoto);
        btnRegistro = (Button) findViewById(R.id.btnRegistro);
        btnStopService = (Button) findViewById(R.id.btnStopService);
        txtMensajeEnviado = (TextView) findViewById(R.id.txtMensajeEnviado);
        txtSend = (TextView) findViewById(R.id.txtMensajeEnviado);
        txtCoordenadas = (TextView) findViewById(R.id.txtCoordenadas);
        txtStreet = (TextView) findViewById(R.id.txtStreet);
        txtLatitud = (TextView) findViewById(R.id.txtLatitud);
        txtLongitud = (TextView) findViewById(R.id.txtLongitud);
        txtEstado = (TextView) findViewById(R.id.txtestadoBT);
        txtNombre = (TextView) findViewById(R.id.txtNombre);
        txtNotificacion = (TextView) findViewById(R.id.txtNotification);
        txtInternetStatus = (TextView) findViewById(R.id.txtInternet);
        txtStatusService = (TextView) findViewById(R.id.txtStatusService);
        txtStatusNotification = (TextView) findViewById(R.id.txtStatusNotification);
        imgProfile = findViewById(R.id.imgPerfil);
        snackBarInternet = findViewById(R.id.rlInternet);



        sessionManager = new SessionManager(UserInterfaz.this);
        sessionManager.checkLogin();

        HashMap<String, String> user = sessionManager.getUserDetail();
        nameSessMang = user.get(sessionManager.NAME);
        usernameSessMang = user.get(sessionManager.USERNAME);
        numeroSessMang = user.get(sessionManager.NUMERO);
        cedulaSessMang = user.get(sessionManager.CEDULA);
        imagenSessMang = user.get(sessionManager.IMAGEN);


        //****************************************************
        if(nameSessMang != null && usernameSessMang != null) {
            registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
            UpdateInternetState();
            CheckInternetConnection();
            InternetVerificarRegistro();

        }
        //****************************************************

        txtNombre.setText(nameSessMang + "\nUSER: " + usernameSessMang + "\nCEDULA: " + cedulaSessMang);


        permissionsLocation();
        getImageUrl(imagenSessMang + ".jpg");
        txtEstado.setBackgroundResource(R.drawable.ic_bluetooth_desconnect);

        clearUI();

        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(veriUploadLocation){
                    veriUploadLocation=false;
                    Intent intentService = new Intent(UserInterfaz.this, UbicacionService.class);
                    stopService(intentService);
                    txtMensajeEnviado.setText("Tracking Stopped...");
                }

            }
        });

        btnSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (veriConnectedStatusBT == true) {
                    mBluetoothLeService.disconnect();
                    mlocManager.removeUpdates((LocationListener) Local);
                    veriStateLocalizacion = false;
                    Intent intentService = new Intent(UserInterfaz.this, UbicacionService.class);
                    stopService(intentService);
                    veriConnectedStatusBT = false;
                }
                clearUI();
                txtStreet.setText(" ");
                sessionManager.logout();
            }
        });
        txtStatusNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (veriConnectedInternet && mBluetoothLeService.veriStateService() == false){
                    mBluetoothLeService.readCustomCharacteristic();
                    txtStatusNotification.setBackgroundResource(R.color.colorSerCharDes);
                }else{
                    txtStatusNotification.setBackgroundResource(R.color.colorSerCharCon);
                }
            }
        });
        txtStatusService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (veriConnectedInternet && mBluetoothLeService.veriStateService() == false){
                    mBluetoothLeService.readCustomCharacteristic();
                    txtStatusNotification.setBackgroundResource(R.color.colorSerCharDes);
                }else{
                    txtStatusNotification.setBackgroundResource(R.color.colorSerCharCon);
                }
            }
        });

        btneditFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //subirFoto();
            }
        });

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInterfaz.this, Registros.class);
                intent.putExtra("username", usernameSessMang);
                startActivity(intent);
                if (veriConnectedStatusBT) {
                    mBluetoothLeService.writeCustomCharacteristic("Y");

                }else{
                  //
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
            Toast.makeText(getApplicationContext(), "data1", Toast.LENGTH_LONG).show();
            txtEstado.setBackgroundResource(R.drawable.ic_bluetooth_connect);
        }
        UpdateInternetState();

    }

    @Override
    protected void onPause() {
        super.onPause();
        //

        //unregisterReceiver(mGattUpdateReceiver);
        //if (mBluetoothLeService != null){
        //    displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
        //}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
        if(veriConnectedStatusBT) {
            unbindService(mServiceConnection);
            mBluetoothLeService = null;
            mlocManager.removeUpdates((LocationListener) Local);
        }
        Intent intentService = new Intent(UserInterfaz.this, UbicacionService.class);
        stopService(intentService);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case conectarBT:
                if (resultCode == Activity.RESULT_OK) {
                    mDeviceName = data.getStringExtra(EXTRAS_DEVICE_NAME);
                    mDeviceAddress = data.getStringExtra(EXTRAS_DEVICE_ADDRESS);
                    if (mDeviceAddress != null && mDeviceName != null) {//////edit aqui
                        getActionBar().setTitle(mDeviceName);
                        getActionBar().setDisplayHomeAsUpEnabled(false); //
                        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (mBluetoothLeService != null) {
                                    mBluetoothLeService.readCustomCharacteristic();
                                    //locationStart(); /// no activar
                                }
                            }
                        }, 3000);
                    }

                } else {

                    Toast.makeText(getApplicationContext(), "Bluetooth no Conectado", Toast.LENGTH_LONG).show();
                    txtEstado.setBackgroundResource(R.drawable.ic_bluetooth_desconnect);
                }
                break;

            case cloudinaryImg:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    Toast.makeText(getBaseContext(), "Si entre", Toast.LENGTH_SHORT).show();

                    Uri path = data.getData();
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                        imgProfile.setImageBitmap(bitmap);
                        uploadCloudinary(path, usernameSessMang);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // User cancelled the image capture
                    //finish();
                }

                break;
        }
    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "No se puede inicializar Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothLeService != null) {
                        mBluetoothLeService.readCustomCharacteristic();
                        //locationStart();
                    }
                }
            }, 3000);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                veriConnectedStatusBT = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                veriConnectedStatusBT = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();

                txtEstado.setBackgroundResource(R.drawable.ic_bluetooth_desconnect);
                txtLatitud.setText("");
                txtLongitud.setText("");
                txtStreet.setText("");
                //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                //txtMensajeEnviado.setText("Rastreando Movil..");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                //displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));

            }
        }
    };

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //mConnectionState.setText(resourceId);
            }
        });
    }
    private void displayData(String data) {
        Toast.makeText(UserInterfaz.this, data, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "DATO RECIBIDO POR BT: " + data);
        if (data != null) {
            String dato = data.replaceAll("\\s", "");
            Toast.makeText(UserInterfaz.this, dato, Toast.LENGTH_SHORT).show();
            if (dato.equals("begin")) {
                //code inicia sistema
                    sendMessageDB();
            }else if (dato.equals("cancela")){
                Log.d(TAG, "Cancela Service " + data);
                Intent intentService = new Intent(UserInterfaz.this, UbicacionService.class);
                stopService(intentService);
            }
        }
    }
    private void sendMessageDB(){
        if (veriRegistrosGps == 0) {
            txtMensajeEnviado.setText("Enviando mensaje....");
            veriUploadLocation = false;
            //while (!veriUploadLocation) {
                if (veriConnectedInternet && veriConnectedNet) {
                    uploadLocationDB(URL_INSERTAR_UBICACION);
                }
            //}
            //InternetInsertarUbicacion();
        } else {
            Toast.makeText(UserInterfaz.this, "Veri 2", Toast.LENGTH_SHORT).show();
            //updateVeriRegistroDB(musername);
            Log.d(TAG, "Registros pos 0: " + idEliminar);
            if (veriRegistrosGps == 1){
                Log.d(TAG, "Eliminando Registro ");
                //BDeliminarDireccion(idEliminar);
                //sendMessageDB();
            }
            //alertBuiler("Importante", "Usted cuenta con un Registro");
        }

    }

    private void clearUI() {
        txtMensajeEnviado.setText(R.string.no_data);
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (veriConnectedStatusBT) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothLeService != null) {
                        clearUI();
                        locationStart();
                        txtEstado.setBackgroundResource(R.drawable.ic_bluetooth_connect);
                        Log.d(TAG, "Service status: "+ mBluetoothLeService.veriStateService());
                        Log.d(TAG, "Notification status: "+ mBluetoothLeService.veriStateNotification());
                        if(mBluetoothLeService.veriStateService()){
                            txtStatusService.setBackgroundResource(R.color.colorSerCharCon);
                            Log.d(TAG, "Service conected");
                            if (mBluetoothLeService.veriStateNotification()){
                            txtStatusNotification.setBackgroundResource(R.color.colorSerCharCon);
                                Log.d(TAG, "Notification conected");
                            }else{ txtStatusNotification.setBackgroundResource(R.color.colorSerCharDes); }
                        }else{txtStatusService.setBackgroundResource(R.color.colorSerCharDes);}
                    }
                }
            }, 2000);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            clearMenuLocation();
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                //txtEstado.setBackgroundResource(R.drawable.ic_bluetooth_connect);
                Intent intentDeviceScan = new Intent(UserInterfaz.this, DeviceScanActivity.class);
                startActivityForResult(intentDeviceScan, conectarBT);
                return true;
            case R.id.menu_disconnect:
                clearMenuLocation();
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void locationStart() {
        veriStateLocalizacion = true;
        mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Local.setUserInterfaz(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (LocationListener) Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) Local);
    }
    public void permissionsLocation(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1000);
        }

    }
    public class Localizacion implements LocationListener {
        UserInterfaz mainActivity;

        public UserInterfaz getMainActivity() {
            return mainActivity;
        }
        public void setUserInterfaz(UserInterfaz mainActivity) {
            this.mainActivity = mainActivity;
        }
        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coo rdenadas
            // debido a la deteccion de un cambio de ubicacion


            doubleLatitud = loc.getLatitude();
            doubleLongitud = loc.getLongitude();

            stringLatitud =String.valueOf(doubleLatitud) ;
            stringLongitud= String.valueOf(doubleLongitud);

            txtLatitud.setText(stringLatitud);
            txtLongitud.setText(stringLongitud);

            Street(doubleLatitud, doubleLongitud);
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
    public void Street(double lat, double lon) {
        //Obtener la direccion de la calle a partir de la latitud y la longitud
        if (lat != 0.0 && lon != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(UserInterfaz.this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(
                        lat, lon, 1);
                if (!list.isEmpty()) {
                    Address DirCalle = list.get(0);
                    txtStreet.setText(DirCalle.getAddressLine(0));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void uploadLocationDB(String URL){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //handler.removeMessages(0);
                Log.d(TAG, "BD INSERTAR UBICACION");
                Toast.makeText(UserInterfaz.this, "Mensaje Enviado con Exito", Toast.LENGTH_SHORT).show();
                txtMensajeEnviado.setText("Mensaje Enviado con Exito");
                veriUploadLocation = true;
                //mBluetoothLeService.writeCustomCharacteristic("Y");
                Vibrator vibrator = (Vibrator)getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(1000);

                Intent intentService = new Intent(UserInterfaz.this, UbicacionService.class);
                intentService.putExtra("username", usernameSessMang);
                startService(intentService);
                btnStopService.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txtMensajeEnviado.setText("Rastreando Movil...");
                    }
                }, 1000);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(UserInterfaz.this, "Mensaje no Enviado", Toast.LENGTH_SHORT).show();
                txtMensajeEnviado.setText("Mensaje No Enviado");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txtMensajeEnviado.setText("Reintentando...");
                        //UpdateInternetState();
                    }
                }, 100);

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("latitud", stringLatitud);
                params.put("longitud", stringLongitud);
                params.put("name", nameSessMang);
                params.put("username", usernameSessMang);
                params.put("numero", numeroSessMang);
                params.put("cedula", cedulaSessMang);
                params.put("imagen", imagenSessMang+".jpg");
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }


    public void getImageUrl (String url_img){
        Picasso.with(this).load(url_img)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(imgProfile, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onError() {
                    }
                });

    }
    public void subirFoto (){
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent. ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Seleccione Aplicacion"), 10);
    }
    public void uploadCloudinary(Uri path, String ipUser){
        //File to upload to cloudinary
        Map config = new HashMap();
        config.put("cloud_name", "leimerg");
        config.put("api_key", "242175622951595");
        config.put("api_secret", "w-NqQT7LOxdwsn4bi9Y8YenIFGo");

        //Cloudinary cloudinary = new Cloudinary(config);
        MediaManager.init(this,config);

        MediaManager.get().upload(path)
                .option("public_id", ipUser)
                .option("tags", ipUser)
                .policy(new UploadPolicy.Builder()
                        .maxRetries(7)
                        .backoffCriteria(3000, UploadPolicy.BackoffPolicy.LINEAR)
                        .networkPolicy(UploadPolicy.NetworkType.UNMETERED)
                        .build())
                .dispatch();

        //mimagen = MediaManager.get().url().generate(ipUser);
        //obtenerImagenUrl(mimagen+".jpg");

    }

    private void updateVeriRegistroDB(final String username){
        Log.d(TAG, "BDvericarRegistro: ");
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_OBTENER_REGISTROS+"?username="+username, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    JSONArray jsonArray = jsonResponse.getJSONArray("registro");
                    veriRegistrosGps = jsonArray.length();
                    Log.d(TAG, "Numero de Registros pos 1: " + veriRegistrosGps);
                    //********************************
                    if( veriRegistrosGps == 1) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            idEliminar = jsonArray.getJSONObject(i).getString("id");
                        }
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
    private void BDeliminarDireccion(final String ID){
        Log.d(TAG, "BDeliminarDireccion");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_ELIMINAR_DIRECCION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("success");
                    if (success.equals("1")){
                        Log.d(TAG, "Registro Eliminado");
                    } else if(success.equals("0")){
                        Log.d(TAG, "Registro NO Eliminado");
                    }
                    sendMessageDB();

                }catch (JSONException e){
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Error en Eliminar");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError{
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", ID);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void clearMenuLocation(){
        if (veriStateLocalizacion){
            mlocManager.removeUpdates((LocationListener) Local);
            txtEstado.setBackgroundResource(R.drawable.ic_bluetooth_desconnect);
            txtLatitud.setText("");
            txtLongitud.setText("");
            txtStreet.setText("");
            veriStateLocalizacion = false;
        }

    }


    private BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = manager.getActiveNetworkInfo();
            onNetworkChange(ni);
        }
    };
    private void onNetworkChange(NetworkInfo networkInfo) {
        Log.d(TAG, "NonNetworkChange");
        veriConnectedNet = false;
        if (networkInfo != null) {
            if (networkInfo.getState() == NetworkInfo.State.CONNECTED && networkInfo.isAvailable()) {
                veriConnectedNet = true;
                Log.d(TAG, "Conected Network");
            }
        }
        if(veriConnectedNet){
            txtInternetStatus.setText("Access Internet available");
        }else{
            txtInternetStatus.setText("Access Internet not available");
        }


    }

    private void alertBuiler(String titte, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(UserInterfaz.this);
        builder.setMessage(titte)
                .setNegativeButton(message, null)
                .create()
                .show();
    }




    private void CheckInternetConnection(){
        //Log.d(TAG, "InternetAcces");
        new Thread(new Runnable() {
            @Override
            public void run() {
                veriConnectedInternet = false;
                while (true) {
                    //if (!veriConnectedInternet) {

                        if (veriConnectedNet) {
                            Runtime runtime = Runtime.getRuntime();
                            try {

                                Process ipProcess = runtime.exec("ping -c 2 -w 4 google.com");
                                int exitValue = ipProcess.waitFor();
                                //Log.d(TAG, "exitValue: " + exitValue);
                                if (exitValue == 0 ) {
                                    veriConnectedInternet = true;
                                    //Log.d(TAG, "conected");
                                }else{
                                    veriConnectedInternet = false;
                                    Log.d(TAG, "no conected");
                                }


                            } catch (IOException e) {
                                e.printStackTrace();
                                veriConnectedInternet = false;
                                Log.d(TAG, "no conected");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                veriConnectedInternet = false;
                                Log.d(TAG, "no conected");
                            }

                        }
                    //}

                }
            }
        }).start();

        //return veriConnectedInternet;
    }

    private void InternetVerificarRegistro(){
        Log.d(TAG, "UpdateInternetState");
        new Thread(new Runnable() {
            @Override
            public void run() {

                    while (true) {
                        if(veriConnectedInternet && veriConnectedNet) {
                            updateVeriRegistroDB(usernameSessMang);
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


            }
        }).start();

    }

    private void UpdateInternetState(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while (true) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (veriConnectedInternet && veriConnectedNet) {
                                    txtInternetStatus.setText("Access Internet available");
                                }else{
                                    txtInternetStatus.setText("Access Internet not available");
                                    //Log.d(TAG, "no conected inter");
                                }
                                if (veriRegistrosGps == 1){
                                    txtNotificacion.setVisibility(View.VISIBLE);
                                    txtNotificacion.setText("1");
                                }else{
                                    txtNotificacion.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                        Thread.sleep(1000);
                    }
                }catch (Exception e){}

            }
        }).start();

    }

    private void InternetInsertarUbicacion(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    while (!veriUploadLocation) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (veriConnectedInternet && veriConnectedNet) {
                                    uploadLocationDB(URL_INSERTAR_UBICACION);
                                }
                            }
                        });
                        Thread.sleep(10);
                    }

                }catch (Exception e){}

            }
        }).start();

    }

}
