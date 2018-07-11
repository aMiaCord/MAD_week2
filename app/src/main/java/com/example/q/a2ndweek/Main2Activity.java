package com.example.q.a2ndweek;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Member;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Main2Activity extends AppCompatActivity {

    public RequestQueue queue;
    public static Context mContext;
    private final int REQ_CODE_UPLOAD=2;
    private ListView lv;
    private String searchKeyword;
    private ArrayList<Member> data;
    private  ListviewAdapter adapter;
    private final int CAMERA_CODE = 1111;
    private final int GALLERY_CODE = 1112;
    private final int REQ_CODE_POST = 33;
    int width;
    Socket socket;
    Bitmap profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mContext = this;

        lv = (ListView) findViewById(R.id.listview);

        facebookLogIn();


        queue = Volley.newRequestQueue(this);
        myProfile();

        try {
            EditText searchBox = (EditText) findViewById(R.id.search_box);
            searchBox.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable arg0) {
                    // ignore
                }
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                    // ignore
                }
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    try {
                        searchKeyword = s.toString();
                        displayList();
                        } catch (Exception e) {
                        Log.e("", e.getMessage(), e);
                        }
                }
                });
            displayList();
            } catch (Exception e) {
            Log.e("", e.getMessage(), e);
            }

        GalleryActivity galleryActivity = new GalleryActivity();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;

        LinearLayout listView = findViewById(R.id.galleryListView);
        galleryActivity.getImageList(queue,listView,(Activity)this,width);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTablayout();
    }

    public void setTablayout(){
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                FrameLayout frameLayout = findViewById(R.id.frameLayout);
                switch (tab.getPosition()) {
                    case 0:
                        findViewById(R.id.tab1).setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        loadBoard();
                        findViewById(R.id.boardLayout).setVisibility(View.VISIBLE);
                        FloatingActionButton fab = findViewById(R.id.fab);
                        fab.setVisibility(View.VISIBLE);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent  intent = new Intent(new Intent(getApplicationContext(),AddPostActivity.class));
                                intent.putExtra("id",getIntent().getStringExtra("id"));
                                startActivityForResult(intent,REQ_CODE_UPLOAD);
                            }
                        });
                        break;
                    case 2:
                        findViewById(R.id.galleryListView).setVisibility(View.VISIBLE);
                        LinearLayout listView = findViewById(R.id.galleryListView);
                        listView.removeAllViews();
                        GalleryActivity galleryActivity = new GalleryActivity();
                        galleryActivity.getImageList(queue,listView,(Activity)mContext,width);
                        break;
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        findViewById(R.id.tab1).setVisibility(View.GONE);
                        break;
                    case 1:
                        findViewById(R.id.boardLayout).setVisibility(View.GONE);
                        findViewById(R.id.fab).setVisibility(View.GONE);
                        break;
                    case 2:
                        findViewById(R.id.galleryListView).setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    public void facebookLogIn(){
        CallbackManager callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        // If using in a fragment
        //loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    public void displayProfile(){
        String id = getIntent().getStringExtra("id");
        ImageView iv = (ImageView) findViewById(R.id.myimage);
        iv.setBackground(new ShapeDrawable(new OvalShape()));
        if (Build.VERSION.SDK_INT >= 21) {
            iv.setClipToOutline(true);
        }

        isThereImage(id, iv);

        TextView tvName = (TextView) findViewById(R.id.myname);
        tvName.setText(id);

        TextView tvStatus = (TextView) findViewById(R.id.mystatus);
        String status = null;

        ArrayList<Member> users = getList();
        for(int i=0; i<users.size(); i++){
            if(users.get(i).id.equals(id)) {
                status = users.get(i).status;
            }
        }
        if(status.equals("null")) {
            tvStatus.setText("");
        }
        else{
            tvStatus.setText(status);

        }
    }

    public void isThereImage(final String id, final ImageView iv){

        AndroidMultiPartEntity volleyMultipartRequest = new AndroidMultiPartEntity(Request.Method.GET,
                "http://52.231.65.151:8080/check-image/" +  id,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        iv.setImageBitmap(BitmapFactory.decodeByteArray(response.data, 0, response.data.length));
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //  Log.d("ErrorLog", error.getMessage());
                    }
                });
        queue.add(volleyMultipartRequest);
    }

    View editView;
    public void myProfile(){
        displayProfile();

        final LinearLayout profileDetail = (LinearLayout) findViewById(R.id.myprofile);
        profileDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String id = getIntent().getStringExtra("id");

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.my_profile, null);
                ImageView iv = dialogView.findViewById(R.id.myimage);
                TextView tvName = dialogView.findViewById(R.id.myname);
                TextView tvStatus = dialogView.findViewById(R.id.mystatus);

                iv.setBackground(new ShapeDrawable(new OvalShape()));
                if (Build.VERSION.SDK_INT >= 21) {
                    iv.setClipToOutline(true);
                }

                isThereImage(id, iv);

                tvName.setText(id);
                String status = null;

                ArrayList<Member> users = getList();
                for(int k=0; k<users.size(); k++){
                    if(users.get(k).id.equals(id))
                        status = users.get(k).status;
                }
                if(status.equals("null")){
                    tvStatus.setText("");
                }
                else {
                    tvStatus.setText(status);
                }

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Main2Activity.this);

                // 제목셋팅
                alertDialogBuilder.setTitle("내 프로필");
                alertDialogBuilder.setView(dialogView);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        })
                        .setNegativeButton("수정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                LayoutInflater inflater = getLayoutInflater();
                                editView = inflater.inflate(R.layout.profile_edit, null);
                                ImageView imageView = editView.findViewById(R.id.imageEdit);
                                TextView nameView = editView.findViewById(R.id.myname);

                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        final String[] items = {"앨범에서 사진 선택", "카메라로 사진 촬영", "기본 이미지로 변경", "취소"};
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                                        builder.setTitle("프로필 사진 수정");
                                        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int which) {
                                                switch(which){
                                                    case 0:
                                                        //앨범에서 사진 선택
                                                        selectGallery();
                                                        dialogInterface.dismiss();
                                                        break;
                                                    case 1:
                                                        //카메라로 사진 촬영
                                                        selectPhoto();
                                                        dialogInterface.dismiss();
                                                        break;
                                                    case 2:
                                                        //기본 이미지로 변경
                                                        ImageView iv = editView.findViewById(R.id.imageEdit);
                                                        iv.setBackground(new ShapeDrawable(new OvalShape()));
                                                        if (Build.VERSION.SDK_INT >= 21) {
                                                            iv.setClipToOutline(true);
                                                        }
                                                        Glide.with(getApplication())
                                                                .load(R.drawable.ic_launcher_foreground)
                                                                .into(iv);
                                                        profilePic = null;
                                                        dialogInterface.dismiss();
                                                        break;
                                                    case 3:
                                                        //취소
                                                        dialogInterface.cancel();
                                                        break;

                                                        default:
                                                            dialogInterface.dismiss();
                                                            break;
                                                }
                                            }
                                        });
                                        builder.show();
                                    }
                                });

                                imageView.setBackground(new ShapeDrawable(new OvalShape()));
                                if (Build.VERSION.SDK_INT >= 21) {
                                    imageView.setClipToOutline(true);
                                }
                                final String id = getIntent().getStringExtra("id");
                                isThereImage(id, imageView);

                                nameView.setText(id);

                                final EditText statusView = editView.findViewById(R.id.statusEdit);
                                String status = null;

                                ArrayList<Member> users = getList();
                                for(int k=0; k<users.size(); k++){
                                    if(users.get(k).id.equals(id))
                                        status = users.get(k).status;
                                }
                                if(status.equals("null")){
                                    statusView.setText("");
                                }
                                else {
                                    statusView.setText(status);
                                }
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Main2Activity.this);
                                alertDialogBuilder.setTitle("정보 수정");
                                alertDialogBuilder.setView(editView);

                                alertDialogBuilder
                                        .setCancelable(false)
                                        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {

                                                new Thread() {
                                                    public void run() {
                                                        try {
                                                            String status = statusView.getText().toString();
                                                            TextView tvStatus = (TextView) findViewById(R.id.mystatus);
                                                            if (status.equals("")) {
                                                                tvStatus.setText(status);
                                                                status = "null";
                                                            }
                                                            if(profilePic==null){
                                                                socket = new Socket("52.231.65.151", 8080);
                                                                DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                                                                final String id = getIntent().getStringExtra("id");
                                                                dOut.writeBytes("GET /init-profile?id=" + id+ " HTTP/1.1\r\nHost: 127.0.0.1:8080/\r\n\r\n");
                                                                dOut.flush(); // Send off the data
                                                            }
                                                            else {
                                                                uploadBitmap(profilePic);
                                                            }
                                                            socket = new Socket("52.231.65.151", 8080);
                                                            DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                                                            final String id = getIntent().getStringExtra("id");
                                                            dOut.writeBytes("GET /update-profile?id=" + id + "&status=" + status + " HTTP/1.1\r\nHost: 127.0.0.1:8080/\r\n\r\n");
                                                            dOut.flush(); // Send off the data
                                                        } catch (IOException e)
                                                        {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }.start();

                                                Toast.makeText(Main2Activity.this, "성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();
                                                displayProfile();
                                                displayList();
                                            }
                                        })
                                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.cancel();
                                            }
                                        });
                                alertDialogBuilder.show();
                            }
                        });
                alertDialogBuilder.show();
            }
        });
    }

    public class Member {
        private String photoUri;
        private String id;
        private String status;

        public String getPhotoUri(){return photoUri;}
        public String getID(){return id;}
        public String getStatus(){return status;}
        public Member(String photoUri,String id, String status){
            this.photoUri = photoUri;
            this.id=id;
            this.status=status;
        }
    }

    private void uploadBitmap(final Bitmap bitmap) {

        //getting the tag from the edittext
        //our custom volley request
        AndroidMultiPartEntity volleyMultipartRequest = new AndroidMultiPartEntity(Request.Method.POST, EndPoints.UPROAD_PROFILE_PIC,
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
                String id = getIntent().getStringExtra("id");
                params.put("id", id);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, AndroidMultiPartEntity.DataPart> getByteData() {
                Map<String, AndroidMultiPartEntity.DataPart> params = new HashMap<>();
                long imagename = System.currentTimeMillis();
                params.put("image", new AndroidMultiPartEntity.DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
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

    public AndroidMultiPartEntity downloadBitmap(final String id, final ImageView iv) {
        //getting the tag from the edittext
        //our custom volley request
        Log.d("download 들어왔닝???", "들어와따!!");
        AndroidMultiPartEntity volleyMultipartRequest = new AndroidMultiPartEntity(Request.Method.GET, EndPoints.DOWNROAD_PROFILE_PIC+id,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Log.d("여기는 들어왔닝??", response.data.toString());
                        iv.setImageBitmap(BitmapFactory.decodeByteArray(response.data, 0, response.data.length));
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
                params.put("id", id);
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

    private void selectPhoto(){
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {

                }
                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(intent, CAMERA_CODE);
                }
            }
        }
    }

    private File createImageFile() throws IOException {
        File dir = new File(Environment.getExternalStorageDirectory() + "/DCIM/Camera/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mImageCaptureName = timeStamp + ".png";

        File storageDir = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/DCIM/Camera/"

                + mImageCaptureName);
        currentPhotoPath = storageDir.getAbsolutePath();

        return storageDir;

    }

    private void getPictureForPhoto() {
        ImageView imageView = editView.findViewById(R.id.imageEdit);
        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 80, 80, true);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(currentPhotoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation;
        int exifDegree;

        if (exif != null) {
            exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            exifDegree = exifOrientationToDegrees(exifOrientation);
        } else {
            exifDegree = 0;
        }
        profilePic = rotate(resized, exifDegree);
        imageView.setImageBitmap(profilePic);//이미지 뷰에 비트맵 넣기
    }


    private void selectGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case GALLERY_CODE:
                    sendPicture(data.getData()); //갤러리에서 가져오기
                    //System.out.println("파일 이름은 : "+mImageCaptureName);
                    break;
                case CAMERA_CODE:
                    getPictureForPhoto(); //카메라에서 가져오기
                    //System.out.println("파일 이름은 : "+mImageCaptureName);
                    break;

                case REQ_CODE_UPLOAD:
                    Log.d("REQ","OK");
                    loadBoard();
                    break;
                case REQ_CODE_POST:
                    Log.d("REQ","OK");
                    loadBoard();
                    break;
                default:
                    System.out.println("불가능한 접근\n");
                    break;
            }

        }
    }

    private void sendPicture(Uri imgUri) {
        ImageView imageView = editView.findViewById(R.id.imageEdit);
        Log.d("imgUri야 잘 들어왂ㅆ닝ㅇㅇㅇ", imgUri.toString());
        String imagePath = getRealPathFromURI(imgUri); // path 경로
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int exifDegree = exifOrientationToDegrees(exifOrientation);

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);//경로를 통해 비트맵으로 전환
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 80, 80, true);
        profilePic = rotate(resized, exifDegree);
        imageView.setImageBitmap(profilePic);//이미지 뷰에 비트맵 넣기
    }

    private int exifOrientationToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    private Bitmap rotate(Bitmap src, float degree) {
        // Matrix 객체 생성
        Matrix matrix = new Matrix();
        // 회전 각도 셋팅
        matrix.postRotate(degree);
        // 이미지와 Matrix 를 셋팅해서 Bitmap 객체 생성
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(),
                src.getHeight(), matrix, true);
    }

    private String getRealPathFromURI(Uri contentUri) {
        int column_index=0;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        }

        return cursor.getString(column_index);
    }



    private void displayList(){

        data = null;

        data = getList();

        adapter = new ListviewAdapter(getApplication(), R.layout.member, data);
        lv.setAdapter(adapter);
    }

    private ArrayList<Member> getList(){
        final ArrayList<Member> data = new ArrayList<>();
        final ArrayList<Member> temp = new ArrayList<>();
        final String photoUri =null;

        final Thread getUsers = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket("52.231.65.151", 8080);

                    //send request
                    DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());

                    dOut.writeBytes("GET /uu" + " HTTP/1.1\r\nHost: 127.0.0.1:8080\r\n\r\n");
                    dOut.flush(); // Send off the data

                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    //read response
                    String beforeline = in.readLine();
                    String currentline = in.readLine();
                    while(currentline != null){
                        Log.d("read",beforeline);
                        beforeline = currentline;
                        currentline = in.readLine();
                    }
                    Log.d("read_final",beforeline);

                    String[] allUsers = beforeline.split("/");
                    for(int i=0;i<allUsers.length; i++){
                        String[] oneUser = allUsers[i].split("!!");
                        String id = oneUser[0];
                        String status = oneUser[1];

                        Member member = new Member(photoUri, id, status);
                        temp.add(member);
                    }
                    socket.close();
                }catch (IOException e){e.getStackTrace(); Log.d("login failed","at check thread");}}});
        getUsers.start();
        try {
            getUsers.join();
        }catch (InterruptedException e){e.printStackTrace();}

        for(int i=0; i<temp.size();i++) {

            boolean isAdd = false;
            if (searchKeyword != null && "".equals(searchKeyword.trim()) == false) {
                if (temp.get(i).id.contains(searchKeyword)) {
                    isAdd = true;
                }
            } else {
                isAdd = true;
            }
            if (isAdd) {
                Member member = new Member(photoUri, temp.get(i).id, temp.get(i).status);
                data.add(member);
            }
        }

        return  data;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main2, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }


    public void loadBoard(){
        final ArrayList<String[]> post_data_list;
        post_data_list = new ArrayList<>();
        StringRequest myReq = new StringRequest(Request.Method.GET,
                "http://52.231.65.151:8080/board",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String[] item_list = response.split("/");
                        for(String item : item_list){
                            String[] item_info = item.split("\t");
                            if(!item_info[0].equals(""))
                                post_data_list.add(item_info);
                        }
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
                                    startActivityForResult(intent,REQ_CODE_POST);
                                }
                            });
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
    public class BoardAdapter extends BaseAdapter {
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
