package com.example.q.a2ndweek;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GalleryActivity {
    public ArrayList<Bitmap> images;
    public ArrayList<String> image_names;
    GalleryActivity(){
        images = new ArrayList<>();
        image_names = new ArrayList<>();
    }
    public void getImageList(final RequestQueue queue, final LinearLayout listView, final Activity activity, final int width){
        StringRequest myReq = new StringRequest(Request.Method.GET,
                "http://52.231.65.151:8080/imagenames",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("HiLog", response);
                        String[] lines = response.split("\n");
                        int count = 0;
                        LayoutInflater inflater = activity.getLayoutInflater();
                        LinearLayout image_layout =(LinearLayout) inflater.inflate(R.layout.gallery_column, null);
                        listView.addView(image_layout);
                        for(int i=0;i<lines.length;i++){
                            if(lines[i].equals("**image seperate**")){
                                image_names.add(lines[i+1]);
                                i++;
                                queue.add(downloadBitmap(lines[i],image_layout,count,width));
                                count++;
                                if(count==4){
                                    count = 0;
                                    inflater = activity.getLayoutInflater();
                                    image_layout =(LinearLayout) inflater.inflate(R.layout.gallery_column, null);
                                    listView.addView(image_layout);
                                }
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                          Log.d("ErrorLog", error.getMessage());
                    }
                });

        queue.add(myReq);
    }

    public AndroidMultiPartEntity downloadBitmap(String image_name, final LinearLayout linearLayout, final int count,final int width) {
        //getting the tag from the edittext
        image_name = image_name.replace("/","*");
        //our custom volley request
        AndroidMultiPartEntity volleyMultipartRequest = new AndroidMultiPartEntity(Request.Method.POST, EndPoints.GET_PICS_URL+image_name,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.d("return value",response.toString());
                        ImageView imageView = (ImageView)linearLayout.getChildAt(count);
                        images.add(BitmapFactory.decodeByteArray(response.data, 0, response.data.length));
                        imageView.setImageBitmap(BitmapFactory.decodeByteArray(response.data, 0, response.data.length));
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(width/4,width/4));
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

}