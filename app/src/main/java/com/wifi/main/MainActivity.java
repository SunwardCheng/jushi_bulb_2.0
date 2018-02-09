package com.wifi.main;

import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.wifi.fragment.ConnectFragment;
import com.wifi.fragment.ControlFragment;
import com.wifi.fragment.WifiFragment;
import com.wifi.utils.MyFragmentPagerAdapter;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener{


    //用于滑动切换的ViewPager
    private ViewPager mPager;
    private ArrayList<Fragment> fragmentList;
    private RadioButton home,connect,configwifi;
    private RadioGroup bottomGroup;
    private long exitTime = 0;


    //三个界面的Fragment
    private ControlFragment homeFragment;
    private ConnectFragment connectFragment;
    private WifiFragment wifiFragment;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initViews();

        initViewPager();

    }

    /**
     * 初始化控件
     */
    public void initViews(){
        mPager=(ViewPager)findViewById(R.id.viewPager);
        bottomGroup = (RadioGroup)findViewById(R.id.main_radiogroup);
        home = (RadioButton) findViewById(R.id.main_rab_house);
        connect = (RadioButton) findViewById(R.id.main_rab_connect);
        configwifi = (RadioButton) findViewById(R.id.main_rab_wifi);

        bottomGroup.setOnCheckedChangeListener(this);
    }

    private void initViewPager(){
        homeFragment = new ControlFragment();
        connectFragment = new ConnectFragment();
        wifiFragment = new WifiFragment();

        fragmentList=new ArrayList<Fragment>();
        fragmentList.add(0,homeFragment);
        fragmentList.add(1,connectFragment);
        fragmentList.add(2,wifiFragment);

        //ViewPager设置适配器
        mPager.setAdapter(new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentList));
        //ViewPager显示第一个Fragment
        mPager.setCurrentItem(0);
        //ViewPager页面切换监听
        mPager.addOnPageChangeListener(new MyOnPageChangeListener());
    }


    /**
     *ViewPager切换Fragment,RadioGroup做相应变化
     */
    private class MyOnPageChangeListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch (position){
                case 0:
                    bottomGroup.check(R.id.main_rab_house);
                    break;
                case 1:
                    bottomGroup.check(R.id.main_rab_connect);
                    break;
                case 2:
                    bottomGroup.check(R.id.main_rab_wifi);
                    break;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    /**
     * RadioButton切换Fragment
     * @param group
     * @param checkedId
     */
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId) {
            case R.id.main_rab_house:
                //ViewPager显示第一个Fragment且关闭页面切换动画效果
                mPager.setCurrentItem(0,true);
                break;
            case R.id.main_rab_connect:
                mPager.setCurrentItem(1,true);
                break;
            case R.id.main_rab_wifi:
                mPager.setCurrentItem(2,true);
                break;
        }
    }

    /**
     * 监听Back键按下事件,方法1:
     * 注意:
     * super.onBackPressed()会自动调用finish()方法,关闭
     * 当前Activity.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO 按两次返回键退出应用程序
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 判断间隔时间 小于2秒就退出应用
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                // 应用名
                String applicationName = getResources().getString(
                        R.string.app_name);
                String msg = "再按一次返回键退出";
                //String msg1 = "再按一次返回键回到桌面";
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                // 计算两次返回键按下的时间差
                exitTime = System.currentTimeMillis();
            } else {
                // 关闭应用程序
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
