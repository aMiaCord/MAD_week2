package com.example.q.a2ndweek;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.AsyncTask;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Main2Activity extends AppCompatActivity {

    int REQ_CODE_UPLOAD=2;
    private ListView lv;
    private String searchKeyword;
    private ArrayList<Member> data;
    private  ListviewAdapter adapter;
    Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        lv = (ListView) findViewById(R.id.listview);

        facebookLogIn();

/*        myProfile();

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
*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTablayout();
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
    public void setTablayout(){
        TabLayout tabLayout = findViewById(R.id.tabs);
        findViewById(R.id.boardLayout).setVisibility(View.GONE);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                FrameLayout frameLayout = findViewById(R.id.frameLayout);
                switch (tab.getPosition()) {
                    case 0:
                        findViewById(R.id.tab1).setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        new loadBoard().execute();
                        findViewById(R.id.boardLayout).setVisibility(View.VISIBLE);
                        FloatingActionButton fab = findViewById(R.id.fab);
                        fab.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivityForResult(new Intent(getApplicationContext(),AddPostActivity.class),REQ_CODE_UPLOAD);
                            }
                        });
                        break;
                    case 2:
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
                        break;
                    case 2:
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

    public void myProfile(){
        ImageView iv = (ImageView) findViewById(R.id.myimage);
        iv.setBackground(new ShapeDrawable(new OvalShape()));
        if (Build.VERSION.SDK_INT >= 21) {
            iv.setClipToOutline(true);
        }
        Glide.with(getApplication())
                .load(R.drawable.ic_launcher_foreground)
                .into(iv);

        TextView tvName = (TextView) findViewById(R.id.myname);
        String id = getIntent().getStringExtra("id");
        tvName.setText(id);

        TextView tvStatus = (TextView) findViewById(R.id.mystatus);
        String status = null;

        ArrayList<Member> users = getList();
        for(int i=0; i<users.size(); i++){
            if(users.get(i).id.equals(id)) {
                status = users.get(i).status;
            }
        }
        tvStatus.setText(status);

        LinearLayout profileDetail = (LinearLayout) findViewById(R.id.myprofile);
        profileDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.my_profile, null);
                ImageView iv = dialogView.findViewById(R.id.myimage);
                TextView tvName = dialogView.findViewById(R.id.myname);
                TextView tvStatus = dialogView.findViewById(R.id.mystatus);

                iv.setBackground(new ShapeDrawable(new OvalShape()));
                if (Build.VERSION.SDK_INT >= 21) {
                    iv.setClipToOutline(true);
                }
                Glide.with(getApplication())
                        .load(R.drawable.ic_launcher_foreground)
                        .into(iv);

                String id = getIntent().getStringExtra("id");
                tvName.setText(id);
                String status = null;

                ArrayList<Member> users = getList();
                for(int i=0; i<users.size(); i++){
                    if(users.get(i).id == id)
                        status = users.get(i).status;
                }
                tvStatus.setText(status);

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

    private void displayList() throws Exception {

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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }








    public class loadBoard extends AsyncTask<Void, Void, Void> {
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
