package com.underdog.raver;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.underdog.raver.fragment.FragmentFifth;
import com.underdog.raver.fragment.FragmentFirst;
import com.underdog.raver.fragment.FragmentFourth;
import com.underdog.raver.fragment.FragmentSecond;
import com.underdog.raver.fragment.FragmentThird;

public class MyAdapter extends FragmentStateAdapter {

    public int mCount;

    public MyAdapter(FragmentActivity fa, int count) {
        super(fa);
        mCount = count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int index = getRealPosition(position);

        if(index==0) return new FragmentFirst();
        else if(index==1) return new FragmentSecond();
        else if(index==2) return new FragmentThird();
        else if(index==3) return new FragmentFourth();
        else return new FragmentFifth();

    }

    @Override
    public int getItemCount() {
        return 2000;
    }

    public int getRealPosition(int position) { return position % mCount; }

}