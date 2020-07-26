package com.example.android.bluetoothlegatt.LoginRegister;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.android.bluetoothlegatt.LoginRegister.LoginActivity;

import java.util.HashMap;

public class SessionManager {

    SharedPreferences sharedPreferences;
    public SharedPreferences.Editor editor;
    public Context context;
    int PRIVATE_MODE = 0;

    public static final String PREF_NAME = "LOGIN";
    public static final String LOGIN = "IS_LOGIN";
    public static final String NAME = "NAME";
    public static final String USERNAME = "USERNAME";
    public static final String NUMERO = "NUMERO";
    public static final String CEDULA = "CEDULA";
    public static final String IMAGEN = "IMAGEN";


    public SessionManager(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = sharedPreferences.edit();
    }

    public void createSession(String name, String username, String numero, String cedula, String imagen){
        editor.putBoolean(LOGIN, true);
        editor.putString(NAME, name);
        editor.putString(USERNAME, username);
        editor.putString(NUMERO, numero);
        editor.putString(CEDULA, cedula);
        editor.putString(IMAGEN, imagen);
        editor.apply();
    }

    public boolean isLoggin(){
        return sharedPreferences.getBoolean(LOGIN, false);
    }

    public void checkLogin(){
        if (!this.isLoggin()){
            Toast.makeText(context, "Bluetooth no Conectado", Toast.LENGTH_LONG).show();
            Intent i = new Intent(this.context, LoginActivity.class);
            context.startActivity(i);
            //((UserInterfaz) context).finish();
        }
    }

    public HashMap<String, String> getUserDetail(){
        HashMap<String, String> user = new HashMap<>();
        user.put(NAME, sharedPreferences.getString(NAME, null));
        user.put(USERNAME, sharedPreferences.getString(USERNAME, null));
        user.put(NUMERO, sharedPreferences.getString(NUMERO, null));
        user.put(CEDULA, sharedPreferences.getString(CEDULA, null));
        user.put(IMAGEN, sharedPreferences.getString(IMAGEN, null));
        return user;
    }

    public void logout(){
        editor.clear();
        editor.commit();
        Intent i = new Intent(context, LoginActivity.class);
        context.startActivity(i);
        //((UserInterfaz) context).finish();
    }
}
