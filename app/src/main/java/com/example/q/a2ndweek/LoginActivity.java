package com.example.q.a2ndweek;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    Intent intent;
    Socket socket;
    public RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE));
        permissions.add(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE));
        permissions.add(checkPermission(Manifest.permission.INTERNET));
        while(permissions.remove(null));
        if(!permissions.isEmpty())
            getPermission(permissions.toArray(new String[permissions.size()]), permissions.size());

    }
    public void getPermission(String[] permissions,int request_code){
        ActivityCompat.requestPermissions(this, permissions, request_code);
    }
    public String checkPermission(String request){
        if(ContextCompat.checkSelfPermission(this,request) != PackageManager.PERMISSION_GRANTED){
            return request;
        }
        else
            return null;
    }
    public void onLogin(View view){
        EditText idView = findViewById(R.id.idEditText);
        final String id = idView.getText().toString();

        EditText pwView = findViewById(R.id.pwEditText);
        final String pw = pwView.getText().toString();
        queue = Volley.newRequestQueue(this);

        StringRequest myReq = new StringRequest(Request.Method.GET,
                                                 "http://52.231.65.151:8080/login/" +  id + "?password=" + pw,
                                                new Response.Listener<String>() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        Log.d("HiLog", response);
                                                        if (response.equals("true")) {
                                                            intent = new Intent(getApplicationContext(), Main2Activity.class);
                                                            intent.putExtra("id", id);
                                                            startActivity(intent);
                                                        }
                                                    }
                                                },
                                                new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {
                                                      //  Log.d("ErrorLog", error.getMessage());
                                                    }
                                                });

        queue.add(myReq);

    }

    public void onRegister(View view){
        startActivity(new Intent(this,MakeUserActivity.class));
    }
}
