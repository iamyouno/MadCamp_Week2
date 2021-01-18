package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import org.json.JSONException;
import org.json.JSONObject;
import static com.example.myapplication.Fragment1.fragmentActivity1;
import static com.example.myapplication.Fragment3.fragmentActivity3;
import static com.example.myapplication.MainActivity.btn_custom_login;
import static com.example.myapplication.MainActivity.btn_custom_logout;
import static com.example.myapplication.MainActivity.mainActivity;
import static com.example.myapplication.MainActivity.profile_image;
import static com.example.myapplication.MainActivity.profile_name;

public class LoginCallback implements FacebookCallback<LoginResult> {

    String str_profile_name;

    // 로그인 성공 시 호출 됩니다. Access Token 발급 성공.
    @Override
    public void onSuccess(LoginResult loginResult) {
        Log.e("Callback :: ", "onSuccess");
        requestMe(loginResult.getAccessToken());
    }

    // 로그인 창을 닫을 경우, 호출됩니다.
    @Override
    public void onCancel() {
        Log.e("Callback :: ", "onCancel");
    }

    // 로그인 실패 시에 호출됩니다.
    @Override
    public void onError(FacebookException error) {
        Log.e("Callback :: ", "onError : " + error.getMessage());
    }

    // 사용자 정보 요청
    public void requestMe(AccessToken token) {
        GraphRequest graphRequest = GraphRequest.newMeRequest(token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        try {
                            str_profile_name = object.getString("name");
                            profile_name.setVisibility(View.VISIBLE);
                            profile_name.setText(str_profile_name);
                            String url = object.getJSONObject("picture").getJSONObject("data").getString("url");

                            Glide.with(mainActivity).load(url).apply(new RequestOptions()
                                            .fitCenter()
                                            .format(DecodeFormat.PREFER_ARGB_8888)
                                            .override(Target.SIZE_ORIGINAL))
                                    .into(profile_image);

                            Frame1.facebookId = object.getString("id");
//
                            Fragment importFragment_1 = new Frame1();
                            FragmentManager fragmentManager_1 = fragmentActivity1.getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction_1 = fragmentManager_1.beginTransaction();
                            fragmentTransaction_1.add(R.id.frameLayout_1, importFragment_1);
                            fragmentTransaction_1.replace(R.id.frameLayout_1, importFragment_1);
//                            fragmentTransaction_1.addToBackStack(null);
                            fragmentTransaction_1.commit();
//
////////
//                            Fragment importFragment_3 = new Frame3();
//                            FragmentManager fragmentManager_3 = fragmentActivity3.getSupportFragmentManager();
//                            FragmentTransaction fragmentTransaction_3 = fragmentManager_3.beginTransaction();
//                            fragmentTransaction_3.add(R.id.frameLayout_3, importFragment_3);
////                            fragmentTransaction_3.replace(R.id.frameLayout_3, importFragment_3);
//                            fragmentTransaction_3.addToBackStack(null);
//                            fragmentTransaction_3.commit();



                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,picture"); //name gender email
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();

        btn_custom_login.setVisibility(View.INVISIBLE);
        btn_custom_logout.setVisibility(View.VISIBLE);
    }

}
