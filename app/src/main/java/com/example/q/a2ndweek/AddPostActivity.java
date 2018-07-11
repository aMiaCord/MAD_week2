package com.example.q.a2ndweek;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AddPostActivity extends AppCompatActivity {

    public RequestQueue queue;
    int REQ_CODE_SELECT_IMAGE = 3;
    ArrayList<AddViewItem> addViewItems = new ArrayList<>();
    private ConstraintLayout mCLayout;
    ArrayList<String> id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);
        mCLayout = findViewById(R.id.cLayout);
        id = new ArrayList<>();
        queue = Volley.newRequestQueue(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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

        //get text
        EditText titleView = findViewById(R.id.editTitle);
        EditText contentView = findViewById(R.id.editContent);
        final String title = titleView.getText().toString();
        final String content = contentView.getText().toString();



        AndroidMultiPartEntity volleyMultipartRequest = new AndroidMultiPartEntity(Request.Method.POST, EndPoints.ROOT_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.d("return value",response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("return value error", error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
                params.put("title", title);
                params.put("content",content);
                params.put("size",String.valueOf(addViewItems.size()));
                return params;
            }
            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, AndroidMultiPartEntity.DataPart> getByteData() {
                Map<String, AndroidMultiPartEntity.DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                for(int i=0;i<addViewItems.size();i++) {
                    params.put("image" + i, new AndroidMultiPartEntity.DataPart(imagename + ".png", getFileDataFromDrawable(addViewItems.get(i).getImage())));
                    Log.d("image count",String.valueOf(i));
                }
                return params;
            }
        };

        //adding the request to volley
        queue.add(volleyMultipartRequest);




        setResult(Activity.RESULT_OK);
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
    private void uploadBitmap(final Bitmap bitmap) {

        //getting the tag from the edittext
        final String tags = "x";

        //our custom volley request
        AndroidMultiPartEntity volleyMultipartRequest = new AndroidMultiPartEntity(Request.Method.POST, EndPoints.ROOT_URL,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.d("return value",response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("return value error", error.getMessage());
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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
                params.put("tags", tags);
                params.put("images", tags);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, AndroidMultiPartEntity.DataPart> getByteData() {
                Map<String, AndroidMultiPartEntity.DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                for(int i=0;i<addViewItems.size();i++)
                    params.put("image"+i, new AndroidMultiPartEntity.DataPart(imagename + ".png", getFileDataFromDrawable(addViewItems.get(i).getImage())));
                return params;
            }
        };

        //adding the request to volley
        queue.add(volleyMultipartRequest);
    }
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
