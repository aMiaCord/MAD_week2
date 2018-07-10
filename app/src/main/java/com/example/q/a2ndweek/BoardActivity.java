package com.example.q.a2ndweek;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BoardActivity extends AppCompatActivity {
    WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);
        new loadBoard().execute();

        FloatingActionButton fab = findViewById(R.id.addPost);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),AddPostActivity.class));
            }
        });
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
                String beforeline = in.readLine();
                String currentline = in.readLine();
                while(currentline != null){
                    Log.d("read",beforeline);
                    beforeline = currentline;
                    currentline = in.readLine();
                }
                Log.d("read",beforeline);
                String[] posts = beforeline.split("/");
                for(String one_post : posts){
                    Log.d("one_post=",one_post);
                    String[] post_data = one_post.split("\t");
                    post_data_list.add(post_data);
                }
                socket.close();
            }catch (IOException e){e.printStackTrace();}
            return null;
        }
        @Override
        protected void onPostExecute(Void size){
            super.onPostExecute(size);
            ListView boardLayout = findViewById(R.id.boardLayout);
            BoardAdapter boardAdapter = new BoardAdapter(getApplicationContext(),post_data_list);
            boardLayout.setAdapter(boardAdapter);
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
            Log.d("post_at_adapter = ",post.toString());
            TextView title = (TextView)postLayout.getChildAt(0);
            title.setText(post[0]);
            TextView username = (TextView)postLayout.getChildAt(1);
            username.setText(post[1]);
            TextView time = (TextView)postLayout.getChildAt(2);
            time.setText(post[2]);
            return postLayout;
        }
    }
}
