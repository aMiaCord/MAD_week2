package com.example.q.a2ndweek;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class LoginActivity extends AppCompatActivity {
    Intent intent;
    Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }
    public void onLogin(View view){
        Thread checkID = new Thread(new Runnable() {
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

                    dOut.writeBytes("GET /login/" + id + " HTTP/1.1\r\nHost: 127.0.0.1:8080/\r\nContent-Type: text/pain\r\n" + "Content-Length: "+pw.length() + "\r\n\r\n"+pw);
                    dOut.flush(); // Send off the data

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //read response
                    String fromServer = in.readLine();
                    while(fromServer!=null){
                        Log.d("read line",fromServer);
                        fromServer = in.readLine();
                    }
                    socket.close();
                    intent = new Intent(getApplicationContext(),Main2Activity.class);
                    intent.putExtra("id",id);
                }catch (IOException e){e.getStackTrace(); Log.d("login failed","at check thread");}
            }
        });
        checkID.start();
        try {
            checkID.join();
            startActivity(intent);
        }catch (InterruptedException e){e.printStackTrace();}
    }

}
