package com.example.q.a2ndweek;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    ArrayList<String> img_path;
    URL url;
    HttpURLConnection conn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);


        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = new Socket("52.231.65.151",8080);
                    DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                    String custom_data;
                    dOut.writeBytes("GET /"+"CUSTOM_DATA"+" HTTP/1.1\r\nHost: 127.0.0.1:8080/\r\n\r\n");
                    dOut.flush(); // Send off the data
                }catch (IOException e){e.getStackTrace();Log.d("error","occured");}
            }
        });
        setGalleryAdapter();
        thread.start();
    }

    GalleryAdapter galleryAdapter;

    public ArrayList<String> loadGallery() {

        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;

        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        //set query
        final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
        cursor = getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");

        //set variable to get query data
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        //query(get image path)
        ArrayList<String> imagePath = new ArrayList<>();
        while (cursor.moveToNext()) {
            absolutePathOfImage = cursor.getString(column_index_data);
            imagePath.add(absolutePathOfImage);
        }
        cursor.close();
        return imagePath;
    }

    public void setGalleryAdapter() {
        GridView gridView = findViewById(R.id.galleryView);
        ViewGroup root = findViewById(R.id.gallery_root_layout);
        img_path = loadGallery();
        //set adapter
        galleryAdapter = new GalleryAdapter(getApplicationContext(), img_path);
        gridView.setAdapter(galleryAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                final Bitmap img = ((BitmapDrawable)((ImageView)view).getDrawable()).getBitmap();
                String[] img_path = ((String)view.getTag()).split("/");
                final String img_name = img_path[img_path.length-1];
                final String pure_img_name = img_path[0];
                // Post로 Request하기
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket("52.231.65.151",8080);
                            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                            String custom_data = BitMapToString(img);
                            Log.d("img string",custom_data);
                            dOut.writeBytes("GET /image/"+img_name+" HTTP/1.1\r\nHost: 127.0.0.1:8080/\r\nContent-Type: image/*\r\nContent-Length: "+custom_data.length()+"\r\n\r\n"+custom_data);
                            dOut.flush(); // Send off the data
                        }catch (IOException e){Log.d("error","occured");}
                    }
                }).start();


            }
        });
    }
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
    public class GalleryAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> imagePath;

        GalleryAdapter(Context context, ArrayList<String> imagePath) {
            this.context = context;
            this.imagePath = imagePath;
        }

        public int getCount() {
            return imagePath.size();
        }

        public Object getItem(int position) {
            return imagePath.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            //set parameter of imageView
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int widthPixels = displayMetrics.widthPixels;
            final ImageView imageView = new ImageView(getApplicationContext());
            //imageView.setImageBitmap(image);
            imageView.setTag((String) getItem(position));
            imageView.setLayoutParams(new GridView.LayoutParams(GridView.AUTO_FIT, widthPixels / 4));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //read image from image path string
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 8;
                    Bitmap image = BitmapFactory.decodeFile((String) imagePath.get(position), options);
                    final Bitmap image2 = ThumbnailUtils.extractThumbnail(image, 64, 64);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            imageView.setImageBitmap(image2);
                        }
                    });
                }
            }).start();
            return imageView;
        }


    }
}