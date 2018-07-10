package com.example.q.a2ndweek;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        checkID.start();
    }

    public void onRegister(View view){
        startActivity(new Intent(this,MakeUserActivity.class));
    }
}
