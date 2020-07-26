package com.example.android.bluetoothlegatt.LoginRegister;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.example.android.bluetoothlegatt.DatabaseURL;
import com.example.android.bluetoothlegatt.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends Activity {

    EditText etName, etUsername, etPassword, etCedula, etNumero;
    Button btnRegister, btnImagen;
    ProgressBar loading;


    private Bitmap bitmap;
    Uri path;
    String url_img = "";
    boolean cedulaCorrecta = false;
    private static final String URL_REGISTER = DatabaseURL.REGISTER;
    private final static String TAG = RegisterActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = (EditText) findViewById(R.id.etName);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etCedula = (EditText) findViewById(R.id.etCedula);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etNumero = (EditText) findViewById(R.id.etNumero);
        btnRegister =(Button) findViewById(R.id.bRegister);
        btnImagen =(Button) findViewById(R.id.bImagen);
        loading = findViewById(R.id.loading);

        btnImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFoto();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String mName= etName.getText().toString();
                String mUsername = etUsername.getText().toString();
                String mCedula = etCedula.getText().toString();
                String mPass = etPassword.getText().toString();
                String mNumero = etNumero.getText().toString();

                if (!mName.isEmpty() && !mUsername.isEmpty() && !mCedula.isEmpty() && !mPass.isEmpty() && !mNumero.isEmpty()) {
                    if (url_img.length() > 0 ){
                        validadorDeCedula(mCedula);
                        if (cedulaCorrecta){
                            loading.setVisibility(View.VISIBLE);
                            btnRegister.setVisibility(View.GONE);
                            uploadRegisterBD(URL_REGISTER);

                        }else{
                            alertBuiler("Cedula Incorrecta", "Aceptar");
                        }

                    }else{
                        alertBuiler("Por Favor Selecciona una imagen", "Aceptar");
                    }
                }else{
                    etName.setError("PorFavor Inserte Nombre");
                    etUsername.setError("PorFavor Inserte Usuario");
                    etPassword.setError("PorFavor Inserte Password");
                    etCedula.setError("PorFavor Inserte Cedula");
                    etNumero.setError("PorFavor Inserte su Numero");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
    }



    public boolean validadorDeCedula(String cedula) {
        cedulaCorrecta = false;
        try {

            if (cedula.length() == 10) {
                int tercerDigito = Integer.parseInt(cedula.substring(2, 3));
                if (tercerDigito < 6) {
                    int[] coefValCedula = { 2, 1, 2, 1, 2, 1, 2, 1, 2 };
                    int verificador = Integer.parseInt(cedula.substring(9,10));
                    int suma = 0;
                    int digito = 0;
                    for (int i = 0; i < (cedula.length() - 1); i++) {
                        digito = Integer.parseInt(cedula.substring(i, i + 1))* coefValCedula[i];
                        suma += ((digito % 10) + (digito / 10));
                    }
                    if ((suma % 10 == 0) && (suma % 10 == verificador)) {
                        cedulaCorrecta = true;
                    }else if ((10 - (suma % 10)) == verificador) {
                        cedulaCorrecta = true;
                    }else{
                        cedulaCorrecta = false;
                    }
                }else{
                    cedulaCorrecta = false;
                }
            }else{
                cedulaCorrecta = false;
            }
        }catch (NumberFormatException nfe) {
            cedulaCorrecta = false;
        }catch (Exception err) {
            System.out.println("Una excepcion ocurrio en el proceso de validadcion");
            cedulaCorrecta = false;
        }

        if (!cedulaCorrecta) {
            System.out.println("La Cédula ingresada es Incorrecta");
        }
        return cedulaCorrecta;
    }

    public void uploadFoto (){
        Intent intent = new Intent();
        intent.setType("image/");
        intent.setAction(Intent. ACTION_GET_CONTENT);
        startActivityForResult(intent.createChooser(intent, "Seleccione Aplicacion"), 10);


    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode== 10 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Toast.makeText(getBaseContext(), "Cargando imagen espera!", Toast.LENGTH_SHORT).show();

            path = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), path);
                //img_perfil.setImageBitmap(bitmap);
                uploadCloudinary (path, etUsername.getText().toString());
            }catch (IOException e){
                e.printStackTrace();
            }
        } else if (resultCode == RESULT_CANCELED) {
            // User cancelled the image capture
            //finish();
        }
    }

    public void uploadCloudinary(Uri path, String ipUser){
        //File to upload to cloudinary
        Map config = new HashMap();
        config.put("cloud_name", "leimerg");
        config.put("api_key", "242175622951595");
        config.put("api_secret", "w-NqQT7LOxdwsn4bi9Y8YenIFGo");

        MediaManager.init(this,config);

        MediaManager.get().upload(path)
                .option("version", ipUser)
                .option("public_id", ipUser)
                .option("tags", ipUser)
                .policy(new UploadPolicy.Builder()
                        .maxRetries(7)
                        .backoffCriteria(3000, UploadPolicy.BackoffPolicy.LINEAR)
                        .networkPolicy(UploadPolicy.NetworkType.UNMETERED)
                        .build())
                .dispatch();

        url_img = MediaManager.get().url().generate(ipUser);
        Toast.makeText(getBaseContext(), "Imagen cargada con exito!", Toast.LENGTH_SHORT).show();
    }

    private void uploadRegisterBD(String URL){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String success = jsonResponse.getString("success");
                    if (success.equals("2")){
                        Toast.makeText(RegisterActivity.this, "Registro Usuario Exitoso", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        RegisterActivity.this.startActivity(intent);
                    }
                    if(success.equals("0")){
                        alertBuiler("Usuario no disponible!!\n Reintente con otro..", "Reintentar");
                        loading.setVisibility(View.GONE);
                        btnRegister.setVisibility(View.VISIBLE);
                    }
                    if (success.equals("1")){
                        alertBuiler("Cedula ya Registrada!!", "Reintentar");
                        loading.setVisibility(View.GONE);
                        btnRegister.setVisibility(View.VISIBLE);
                    }

                }catch (JSONException e){
                    e.printStackTrace();
                    loading.setVisibility(View.GONE);
                    btnRegister.setVisibility(View.VISIBLE);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                alertBuiler("Registro Fallido", "Reintentar");
                loading.setVisibility(View.GONE);
                btnRegister.setVisibility(View.VISIBLE);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError{
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", etName.getText().toString());
                params.put("username", etUsername.getText().toString());
                params.put("numero", etNumero.getText().toString());
                params.put("cedula", etCedula.getText().toString());
                params.put("password", etPassword.getText().toString());
                params.put("imagen", url_img);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void alertBuiler(String titte, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
        builder.setMessage(titte)
                .setNegativeButton(message, null)
                .create()
                .show();

    }
}
