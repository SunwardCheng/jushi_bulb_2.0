package com.wifi.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.wifi.controller.ConnectChildFragmentController;
import com.wifi.main.MainActivity;
import com.wifi.main.R;
import com.wifi.mdns.Utils;
import com.wifi.utils.MyApplication;
import com.wifi.utils.MyFragmentPagerAdapter;

public class ConnectFragment extends Fragment {
    private Button StartConnect,scan;
    //目的主机ip和端口
    private EditText IP,Port;
    //本机监听端口
    //private EditText LOCAL_PORT;
    private TextView destination,porttext;
    
    //目的ip和端口
    private String Server_IP;
    private String Server_Port;
    
    private Map<String, Object> map;
    private MyApplication myApplication;
    
    private WifiManager mWifiManager;
    
    Handler handler = new Handler();
    
    //mdns服务类型
    private String type = "_udpserver._udp.local.";
//    private String type = "_sleep-proxy._udp.local.";
    
    //组播锁
    private MulticastLock lock;
    //展示扫描的mDNS服务
    private QMUIGroupListView mgroupListView;
    private Utils utils = new Utils();
    
  //存放扫描的mDNS服务
	private List<Map<String, Object>> mdnsList= new ArrayList<Map<String,Object>>();
    private Map<String, Object> mdnsMap;
	private boolean flag =true;

	//顶部标题栏
	private QMUITopBar topBar;

    private ViewPager mContentViewPager;
    private Map<ContentPage, View> mPageMap = new HashMap<>();
    private ContentPage mDestPage = ContentPage.Item1;

    //Connect界面的两个嵌套Fragment
    private ConnectChildInputFragment connectChildInputFragment;
    private ConnectChildScanFragment connectChildScanFragment;
    List<Fragment> fragmentList;

    private ConnectChildFragmentController controller;
    //顶部导航栏切换
    private QMUITabSegment mTabSegment;

    //无用的
    private PagerAdapter mPagerAdapter = new PagerAdapter() {
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return ContentPage.SIZE;
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {
            ContentPage page = ContentPage.getPage(position);
            View view = getPageView(page);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            container.addView(view, params);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_connect, container, false);
        //初始化标题栏
        initViews(view);

        controller = ConnectChildFragmentController.getInstance(this,R.id.id_fragment);
        controller.showFragment(0);

        initTabAndPager();

		return view;
	}

    
    /**
     * 控件初始化
     */
    public void initViews(View view){
        mContentViewPager = view.findViewById(R.id.contentViewPager);
        mTabSegment = view.findViewById(R.id.tabSegment);

    	//设置标题栏
		topBar = (QMUITopBar) view.findViewById(R.id.topbar);
		TextView title = topBar.setTitle(R.string.app_name);
		title.setTextSize(23);
		title.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
		topBar.setBackgroundColor(getResources().getColor(R.color.qmui_config_color_blue));
		topBar.showTitleView(true);
        
    }

    private void initTabAndPager() {

        //无用的
        mContentViewPager.setAdapter(mPagerAdapter);
        controller.showFragment(0);
        mTabSegment.setHasIndicator(true);
        mTabSegment.setIndicatorPosition(false);
        mTabSegment.setIndicatorWidthAdjustContent(true);
        mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.scan_mdns)));
        mTabSegment.addTab(new QMUITabSegment.Tab(getString(R.string.hand_input)));
        //无用的
        mTabSegment.setupWithViewPager(mContentViewPager, false);
        mTabSegment.setMode(QMUITabSegment.MODE_FIXED);

        mTabSegment.addOnTabSelectedListener(new QMUITabSegment.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int index) {
                mTabSegment.hideSignCountView(index);
                controller.showFragment(index);
            }

            @Override
            public void onTabUnselected(int index) {

            }

            @Override
            public void onTabReselected(int index) {
                mTabSegment.hideSignCountView(index);
                controller.showFragment(index);
            }

            @Override
            public void onDoubleTap(int index) {

            }
        });

    }

    //无用的
    private View getPageView(ContentPage page) {
        View view = mPageMap.get(page);
        if (view == null) {
            TextView textView = new TextView(getContext());
            view = textView;
            mPageMap.put(page, view);
        }
        return view;
    }

    //无用的
    public enum ContentPage {
        Item1(0),
        Item2(1);
        public static final int SIZE = 2;
        private final int position;

        ContentPage(int pos) {
            position = pos;
        }

        public static ContentPage getPage(int position) {
            switch (position) {
                case 0:
                    return Item1;
                case 1:
                    return Item2;
                default:
                    return Item1;
            }
        }

        public int getPosition() {
            return position;
        }
    }


}