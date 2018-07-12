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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostActivity extends AppCompatActivity {
    String[] post_info;
    ArrayList<Bitmap> images;
    ArrayList<String> image_name;
    String _id;
    public RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        Intent intent = getIntent();
        _id = intent.getStringExtra("_id");
        post_info = new String[4]; //title,name,time,content
        images = new ArrayList<>();
        image_name = new ArrayList<>();



        queue = Volley.newRequestQueue(this);
        StringRequest myReq = new StringRequest(Request.Method.GET,
                "http://52.231.65.151:8080/post/"+_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("HiLog", response);
                        String[] lines = response.split("\n");
                        int i=0;
                        for(;i<lines.length;i++){
                            if(lines[i].equals("**line seperate**")){
                                break;
                            }
                        }
                        ((TextView)findViewById(R.id.postTitle)).setText(lines[i+1]);
                        ((TextView)findViewById(R.id.nameText)).setText(lines[i+2]);
                        ((TextView)findViewById(R.id.timeText)).setText(lines[i+3]);
                        ((TextView)findViewById(R.id.postContent)).setText(lines[i+4]);

                        for(i+=5;i<lines.length;i++){
                            if(lines[i].equals("**image seperate**")){
                                image_name.add(lines[i+1]);
                                i++;
                                queue.add(downloadBitmap(lines[i].replace("/","*")));
                            }
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




    public AndroidMultiPartEntity downloadBitmap(final String image_name) {
        //getting the tag from the edittext
        //our custom volley request
        AndroidMultiPartEntity volleyMultipartRequest = new AndroidMultiPartEntity(Request.Method.GET, EndPoints.GET_PICS_URL+image_name,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        images.add(BitmapFactory.decodeByteArray(response.data, 0, response.data.length));
                        LinearLayout contentLayout = findViewById(R.id.contentLayout);
                        ImageView imageView = new ImageView(getApplicationContext());
                        imageView.setImageBitmap(BitmapFactory.decodeByteArray(response.data, 0, response.data.length));
                        contentLayout.addView(imageView);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("image_name", image_name);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, AndroidMultiPartEntity.DataPart> getByteData() {
                Map<String, AndroidMultiPartEntity.DataPart> params = new HashMap<>();
                return params;
            }
        };
        //adding the request to volley
        return volleyMultipartRequest;
    }


    public void onDelete(View view){
        StringRequest myReq = new StringRequest(Request.Method.GET,
                "http://52.231.65.151:8080/post_delete?_id="+_id+"&user="+getIntent().getStringExtra("user_id"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.equals("fail"))
                            Toast.makeText(getApplicationContext(),"권한이 없습니다.",Toast.LENGTH_SHORT).show();
                        else {
                            setResult(RESULT_OK);
                            finish();
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
}
