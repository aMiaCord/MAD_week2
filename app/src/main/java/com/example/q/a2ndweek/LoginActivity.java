package com.example.q.a2ndweek;

import android.content.Intent;
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

        /** Thread checkID = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //set data
                    EditText idView = findViewById(R.id.idEditText);
                    String id = idView.getText().toString();

                    EditText pwView = findViewById(R.id.pwEditText);
                    String pw = pwView.getText().toString();

                     //make socket
                    socket = new Socket("52.231.65.151", 8080);

                    //send request
                    DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

                    dOut.writeBytes("GET /login/" + id + " HTTP/1.1\r\nHost: 127.0.0.1:8080/\r\nContent-Type: text/plain\r\n" + "Content-Length: "+pw.length() + "\r\n\r\n"+pw);
                    dOut.flush(); // Send off the data

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //read response
                    String currentLIne = in.readLine();
                    String beforeLine = null;
                    while(currentLIne!=null){
                        Log.d("read line",currentLIne);
                        beforeLine = currentLIne;
                        currentLIne = in.readLine();
                    }
                    socket.close();
                    if(beforeLine.equals("true")) {
                        intent = new Intent(getApplicationContext(), Main2Activity.class);
                        intent.putExtra("id", id);
                        startActivity(intent);
                    }
                }catch (IOException e){e.getStackTrace(); Log.d("login failed","at check thread");}
            }
        });
        checkID.start(); **/

    }

    public void onRegister(View view){
        startActivity(new Intent(this,MakeUserActivity.class));
    }
}
