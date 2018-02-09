package com.wifi.controller;



import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.wifi.fragment.ConnectFragment;
import com.wifi.fragment.ControlFragment;
import com.wifi.fragment.WifiFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 17993 on 2018/2/1.
 */

public class FragmentController {
    private int containerId;
    private FragmentManager fm;
    private List<Fragment> fragments;

    private ControlFragment controlFragment;
    private ConnectFragment connectFragment;
    private WifiFragment wifiFragment;

    private static FragmentController controller;

    public static FragmentController getInstance(FragmentActivity activity, int containerId) {
        if (controller == null) {
            controller = new FragmentController(activity, containerId);
        }
        return controller;
    }

    private FragmentController(FragmentActivity activity, int containerId) {
        this.containerId = containerId;
        fm = activity.getSupportFragmentManager();
        initFragment();
    }



    private void initFragment() {
        fragments = new ArrayList<Fragment>();
        controlFragment = new ControlFragment();
        connectFragment = new ConnectFragment();
        wifiFragment = new WifiFragment();

        fragments.add(0,controlFragment);
        fragments.add(1,connectFragment);
        fragments.add(2,wifiFragment);

        FragmentTransaction ft = fm.beginTransaction();
        for (Fragment fragment : fragments) {
            ft.add(containerId, fragment);
        }
        ft.commit();
    }

    public List<Fragment> getFragmentList(){
        return fragments;
    }

    public void showFragment(int position) {
        hideFragments();
        Fragment fragment = fragments.get(position);
        FragmentTransaction ft = fm.beginTransaction();
        ft.show(fragment);
        ft.commit();
    }

    public void hideFragments() {
        FragmentTransaction ft = fm.beginTransaction();
        for (Fragment fragment : fragments) {
            if (fragment != null) {
                ft.hide(fragment);
            }
        }
        ft.commit();
    }
}