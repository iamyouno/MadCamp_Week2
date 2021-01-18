package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.CallbackManager;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.material.tabs.TabLayout;
import java.util.Arrays;

//import static com.example.myapplication.Fragment1.fragmentActivity1;

public class MainActivity extends AppCompatActivity {

    public static Activity mainActivity;
    static Button btn_custom_login, btn_custom_logout;
    static TextView profile_name;
    static ImageView profile_image;
    static ProfilePictureView profilePictureView;

    LoginCallback mLoginCallback;
    CallbackManager mCallbackManager;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = MainActivity.this;

        ViewPager vp = findViewById(R.id.viewpager);
        ViewPager_Adapter adapter = new ViewPager_Adapter(getSupportFragmentManager());
        vp.setAdapter(adapter);

        //아래부터 tab_layout이랑 viewpager 연동하는 코드
        TabLayout tab = findViewById(R.id.tab_layout);
        tab.setupWithViewPager(vp);

        profile_name = findViewById(R.id.profile_name);
        profile_image = findViewById(R.id.profile_image);
        profile_image.setBackground(new ShapeDrawable(new OvalShape()));
        profile_image.setClipToOutline(true);

        profilePictureView = findViewById(R.id.image);
        mCallbackManager = CallbackManager.Factory.create();
        mLoginCallback = new LoginCallback();
        btn_custom_login = (Button) findViewById(R.id.login);
        btn_custom_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                LoginManager loginManager = LoginManager.getInstance();
                loginManager.logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "email"));
                loginManager.registerCallback(mCallbackManager, mLoginCallback);
            }
        });

        btn_custom_logout = (Button) findViewById(R.id.logout);
        btn_custom_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginManager.getInstance().logOut();
                btn_custom_login.setVisibility(View.VISIBLE);
                btn_custom_logout.setVisibility(View.INVISIBLE);
                profile_name.setVisibility(View.INVISIBLE);
                profile_image.setImageResource(R.drawable.user);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

} // ViewPager, ViewPager_Adapter 생성 후 연결