package com.example.myapplication;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class Fragment1 extends Fragment {
//
    static FragmentActivity fragmentActivity1;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            fragmentActivity1 = (FragmentActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_1, container, false);

        Fragment importFragment_1 = new Frame1();
        FragmentManager fragmentManager_1 = fragmentActivity1.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction_1 = fragmentManager_1.beginTransaction();
        fragmentTransaction_1.add(R.id.frameLayout_1, importFragment_1);
        fragmentTransaction_1.replace(R.id.frameLayout_1, importFragment_1);
//                            fragmentTransaction_1.addToBackStack(null);
        fragmentTransaction_1.commit();

        return view;
    }
}

