package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

// FragmentManager : fragment 가져오거나, 삭제하는 등 Activity 내의 Fragment 관리
// FragmentPagerAdapter : 유저가 페이지로 다시 돌아올 수 있는 한, FragmentManager에서 관리하는 Fragment 나타내주는 PagerAdapter의 implementation
// PagerAdapter : adapter가 ViewPager 내부 차지할 수 있게 해주는 기본 클래스
// ViewPager : 유저가 페이지를 왼쪽 오른쪽으로 넘길 수 있게 함.

public class ViewPager_Adapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> items;
    private ArrayList<String> itext = new ArrayList<String>(); //tab_layout에 대한 텍스트 arraylist 선언.

    public ViewPager_Adapter(FragmentManager fm){
        super(fm);
        items = new ArrayList<Fragment>();
        items.add(new Fragment1());
        items.add(new Fragment2());
        items.add(new Frame3());

        itext.add("Address");
        itext.add("Gallery");
        itext.add("Taxi");// 각각의 tab에 대한 텍스트 저장.
    }

    @Override
    public Fragment getItem(int position) {
        return items.get(position);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return itext.get(position);
    } // tab_layout이랑 viewpager 연동하면 각각의 tab의 텍스트 안보이게되므로 getPageTitle추가함.
    // 각각의 tab에 표시할 텍스트를 getPageTitle에서 position에 따라 표시.

    @Override
    public int getCount() {
        return items.size();
    }
}
// ArrayList를 Fragment형으로 정의하여 선언 후 이전에 생성한 Fragment1~3을 add이용해 추가.
