package com.example.q.a2ndweek;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends AppCompatActivity {
    int REQ_CODE_UPLOAD=2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        new loadBoard().execute();

        FloatingActionButton fab = findViewById(R.id.addPost);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(getApplicationContext(),AddPostActivity.class),REQ_CODE_UPLOAD);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQ_CODE_UPLOAD) {
            Log.d("REQ","OK");
            if(resultCode== Activity.RESULT_OK) {
                Log.d("RESULT","OK");
                new loadBoard().execute();
            }
        }
    }
    private class loadBoard extends AsyncTask<Void, Void, Void> {
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

                dOut.writeBytes("GET /board" + " HTTP/1.1\r\nHost: 127.0.0.1:8080/" + "\r\n\r\n");
                dOut.flush(); // Send off the data

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //read response
                post_data_list = new ArrayList<>();
                String currentline = in.readLine();
                while(currentline!=null){
                    if(currentline.equals("**data seperate**"))
                        break;
                    currentline = in.readLine();
                }
                if(currentline!=null) {
                    if (currentline.equals("**data seperate**")) {
                        currentline = in.readLine();
                        if(currentline!=null) {
                            String[] posts = currentline.split("/");
                            for (String one_post : posts) {
                                String[] post_data = one_post.split("\t");
                                post_data_list.add(post_data);
                                Log.d("one_post", one_post);
                            }
                        }
                    }
                }
                socket.close();
            }catch (IOException e){e.printStackTrace();}
            return null;
        }
        @Override
        protected void onPostExecute(Void size){
            super.onPostExecute(size);
            if(!post_data_list.isEmpty()) {
                ListView boardLayout = findViewById(R.id.boardLayout);
                BoardAdapter boardAdapter = new BoardAdapter(getApplicationContext(), post_data_list);
                boardLayout.setAdapter(boardAdapter);

                boardLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String post_id = (String) view.getTag();
                        Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                        intent.putExtra("_id", post_id);
                        startActivity(intent);
                    }
                });
            }
        }
    }
    private class BoardAdapter extends BaseAdapter{
        ArrayList<String[]> post_data;
        public BoardAdapter(Context context) {
            post_data = new ArrayList<>();
        }
        public BoardAdapter(Context context, ArrayList<String[]> data){
            post_data = data;
        }
        @Override
        public Object getItem(int i) {
            return post_data.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getCount() {
            return post_data.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ConstraintLayout postLayout = (ConstraintLayout)getLayoutInflater().inflate(R.layout.post_item,null);
            String[] post = post_data.get(position);
            postLayout.setTag(post[0]);
            TextView title = (TextView)postLayout.getChildAt(0);
            title.setText(post[1]);
            TextView username = (TextView)postLayout.getChildAt(1);
            username.setText(post[2]);
            TextView time = (TextView)postLayout.getChildAt(2);
            time.setText(post[3]);
            return postLayout;
        }
    }
}
