package com.example.q.a2ndweek;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class PostActivity extends AppCompatActivity {
    String[] post_info;
    ArrayList<String> images;
    ArrayList<String> image_name;
    String _id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Intent intent = getIntent();
        _id = intent.getStringExtra("_id");
        post_info = new String[4]; //title,name,time,content
        images = new ArrayList<>();
        image_name = new ArrayList<>();
        LoadPost loadpost = new LoadPost();
        loadpost.execute();
    }
    private class LoadPost extends AsyncTask<Void, Void, Void> {
        ArrayList<String[]> post_data_list;
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... a) {
            //make socket
            try {
                Socket socket = new Socket("52.231.65.151", 8080);

                //send request
                DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

                dOut.writeBytes("GET /post/"+ _id + " HTTP/1.1\r\nHost: 127.0.0.1:8080/" + "\r\n\r\n");
                dOut.flush(); // Send off the data

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //read response
                String currentline = in.readLine();
                while(!currentline.equals("**line seperate**")){
                    currentline = in.readLine();
                    Log.d("reading",currentline);
                }
                post_info[0] = in.readLine();
                post_info[1] = in.readLine();
                post_info[2] = in.readLine();
                post_info[3] = in.readLine();
                while((currentline=in.readLine())!=null){
                    if(currentline.equals("**image seperate**")) {
                        image_name.add(in.readLine());
                        images.add(in.readLine());
                    }
                }
                socket.close();
            }catch (IOException e){e.printStackTrace();}
            return null;
        }
        @Override
        protected void onPostExecute(Void size){
            TextView postTitle = findViewById(R.id.postTitle);
            postTitle.setText(post_info[0]);
            TextView nameView = findViewById(R.id.nameText);
            nameView.setText(post_info[1]);
            TextView timeView = findViewById(R.id.timeText);
            timeView.setText(post_info[2]);
            TextView postContent = findViewById(R.id.postContent);
            postContent.setText(post_info[3]);

            ViewGroup contentLayout = findViewById(R.id.contentLayout);
            for(int i=0;i<images.size();i++){
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setImageBitmap(Base64ToBitmap(images.get(i)));
                contentLayout.addView(imageView);
            }

            super.onPostExecute(size);

        }
    }
    public Bitmap Base64ToBitmap(String code){
        byte[] decodedString = Base64.decode(code, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        Log.d("get image","okay");
        return decodedByte;
    }
}
