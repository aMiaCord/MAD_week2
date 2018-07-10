package com.example.q.a2ndweek;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class AddPostActivity extends AppCompatActivity {

    int REQ_CODE_SELECT_IMAGE = 3;
    ArrayList<AddViewItem> addViewItems = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        Toast.makeText(getBaseContext(), "resultCode : "+resultCode,Toast.LENGTH_SHORT).show();

        if(requestCode == REQ_CODE_SELECT_IMAGE)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                try {
                    //Uri에서 이미지 이름을 얻어온다.
                    String name_Str = getImageNameToUri(data.getData());

                    //이미지 데이터를 비트맵으로 받아온다.
                    Bitmap image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());


                    addViewItems.add(new AddViewItem(image_bitmap,name_Str));
                    //배치해놓은 ImageView에 set

                    ListView view = findViewById(R.id.addImageList);
                    AddedImageAdapter addedImageAdapter = new AddedImageAdapter(this,addViewItems);
                    view.setAdapter(addedImageAdapter);
                    //Toast.makeText(getBaseContext(), "name_Str : "+name_Str , Toast.LENGTH_SHORT).show();


                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onClickLoadPic(View view){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
    }
    public void onSaveClick(View view){
        Thread save = new Thread(new Runnable() {
            @Override
            public void run() {
                //get text
                EditText titleView = findViewById(R.id.editTitle);
                EditText contentView = findViewById(R.id.editContent);
                String title = titleView.getText().toString();
                String content = contentView.getText().toString();
                //get image
                ArrayList<String> images = new ArrayList<>();
                for(AddViewItem addViewItem : addViewItems){
                    images.add(BitMapToString(addViewItem.getImage()));
                }

                try {
                    Socket socket = new Socket("52.231.65.151", 8080);
                    //send request
                    DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                    //save title and text
                    String request_message = "GET /add-post?title=" + title +" HTTP/1.1\r\nHost: 127.0.0.1:8080/\r\nContent-Type: text/plain\r\nContent-Length: "+content.length();
                    dOut.writeBytes(request_message + "\r\n\r\n" + content + "\r\n");
                    dOut.flush(); // Send off the data
                    //get post id
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String beforeline = in.readLine();
                    String currentline = in.readLine();
                    while(currentline != null){
                        Log.d("read",beforeline);
                        beforeline = currentline;
                        currentline = in.readLine();
                    }
                    Log.d("read",beforeline);
                    String id = beforeline;

                    for(int i=0;i<images.size();i++) {
                        socket = new Socket("52.231.65.151", 8080);
                        dOut = new DataOutputStream(socket.getOutputStream());
                        String[] image_name = addViewItems.get(i).getImage_name().split("\\.");
                        Log.d("image string is",images.get(i));
                        Log.d("image string size is",String.valueOf(images.get(i).length()));
                        request_message = "GET /add-image?id=" + id +"&name="+image_name[0]+" HTTP/1.1\r\nHost: 127.0.0.1:8080/\r\nContent-Type: image/*\r\nContent-Length: "+images.get(i).length();
                        dOut.writeBytes(request_message + "\r\n\r\n" + images.get(i) + "\r\n");
                        dOut.flush(); // Send off the data
                    }
                }catch (IOException e){e.printStackTrace();}

            }
        });
        save.start();
        try {
            save.join();
        }catch (InterruptedException e){e.printStackTrace();}
        finish();
    }

    public class AddedImageAdapter extends BaseAdapter{
        ArrayList<AddViewItem> addViewItems;
        AddedImageAdapter(Context context,ArrayList<AddViewItem> addViewItems){
            this.addViewItems = addViewItems;
        }
        @Override
        public Object getItem(int i) {
            return addViewItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public int getCount() {
            return addViewItems.size();
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewGroup postLayout =(ViewGroup) getLayoutInflater().inflate(R.layout.add_image_item,null);

            ImageView addImageView = (ImageView)postLayout.getChildAt(0);
            addImageView.setImageBitmap(addViewItems.get(i).getImage());

            TextView addTextView = (TextView)postLayout.getChildAt(1);
            addTextView.setText(addViewItems.get(i).getImage_name());
            return postLayout;
        }
    }
    public class AddViewItem{
        private Bitmap image;
        private String image_name;
        AddViewItem(Bitmap image,String image_name){
            this.image = image;
            this.image_name = image_name;
        }

        public Bitmap getImage() {
            return image;
        }

        public String getImage_name() {
            return image_name;
        }
    }
    public String getImageNameToUri(Uri data) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        return imgName;
    }


    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
}
