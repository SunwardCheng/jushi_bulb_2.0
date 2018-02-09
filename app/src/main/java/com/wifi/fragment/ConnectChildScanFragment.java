package com.wifi.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.java.bulb.ConfigWifi;
import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.widget.QMUIItemViewsAdapter;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.qmuiteam.qmui.widget.grouplist.QMUICommonListItemView;
import com.qmuiteam.qmui.widget.grouplist.QMUIGroupListView;
import com.qmuiteam.qmui.widget.pullRefreshLayout.QMUIPullRefreshLayout;
import com.wifi.controller.ConnectChildFragmentController;
import com.wifi.main.R;
import com.wifi.mdns.Utils;
import com.wifi.utils.MyApplication;
import com.wifi.utils.MyListViewAdapter;
import com.wifi.utils.WifiUtils;

public class ConnectChildScanFragment extends Fragment implements OnClickListener{
	private Button StartConnect,scan;
	//目的主机ip和端口
	private EditText IP,Port;
	//本机监听端口
	//private EditText LOCAL_PORT;
	private TextView destination,porttext;

	//目的ip和端口
	private String Server_IP;
	private String Server_Port;

	//存放全局变量
	private MyApplication myApplication;
	private Map<String, Object> map;

	private WifiManager mWifiManager;

	Handler handler = new Handler();

	//请求权限
	private static final int REQUEST_CODE_CHANGE_WIFI_MULTICAST_STATE= 1;

	//mdns服务类型
	private String type = "_udpserver._udp.local.";
//    private String type = "_sleep-proxy._udp.local.";

	//组播锁
	private MulticastLock lock;
	//展示扫描的mDNS服务
	private ListView mListView;
	private Utils utils = new Utils();

	//存放扫描的mDNS服务
	private List<Map<String, Object>> mdnsList= new ArrayList<Map<String,Object>>();
	private Map<String, Object> mdnsMap;
	private boolean flag =true;

	private QMUIPullRefreshLayout mPullRefreshLayout;

	private TextView down_refresh;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.child_fragment_connect_scan, container, false);

		initViews(view);

		//解决主线程不能进行网络操作问题
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		initData();



        /*mlistView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {


				AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

				Server_IP = mdnsList.get(position).get("Service_IP").toString();
				Server_Port = mdnsList.get(position).get("Service_Port").toString();
	            alert.setTitle(Server_IP);
	            alert.setMessage("点击获取");

	            alert.setPositiveButton("获取", new DialogInterface.OnClickListener(){
	                @Override
	                public void onClick(DialogInterface dialog, int which) {
	                    IP.setText(Server_IP);
	                    Port.setText(Server_Port);

	                }
	            });
	            alert.setNegativeButton("取消", new DialogInterface.OnClickListener(){
	                @Override
	                public void onClick(DialogInterface dialog, int which) {
	                    //
	                    //mWifiAdmin.removeWifi(mWifiAdmin.getNetworkId());
	                }
	            });
	            alert.create();
	            alert.show();

			}
		});*/
		return view;
	}





	/**
	 * 打开wifi组播服务
	 */
	public void openBroadcast(Context context) {
		mWifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
		lock =mWifiManager.createMulticastLock(getClass().getSimpleName());
		lock.setReferenceCounted(true);
		lock.acquire();//to receive multicast packets
	}


	/**
	 * 打开CHANGE_WIFI_MULTICAST_STATE
	 */
	public void requestMutiCastPermission(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
			//判断是否具有权限
			if (ContextCompat.checkSelfPermission(getActivity(),
					Manifest.permission.CHANGE_WIFI_MULTICAST_STATE) != PackageManager.PERMISSION_GRANTED) {
				//判断是否需要向用户解释为什么需要申请该权限
				if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
						Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)) {
					Toast.makeText(getActivity(), "自Android 6.0开始需要打开位置权限才可以搜索到WIFI设备", Toast.LENGTH_SHORT);

				}
				//请求权限
				ActivityCompat.requestPermissions(getActivity(),
						new String[]{Manifest.permission.CHANGE_WIFI_MULTICAST_STATE},
						REQUEST_CODE_CHANGE_WIFI_MULTICAST_STATE);
			}
		}
	}
	/**
	 * 控件初始化
	 */
	public void initViews(View view){

//		down_refresh = (TextView) view.findViewById(R.id.down_refresh);

		//下拉刷新
		mPullRefreshLayout = (QMUIPullRefreshLayout)view.findViewById(R.id.pull_to_refresh);

		//扫描到的mdns服务
		mListView=(ListView) view.findViewById(R.id.mdns_list);
//		down_refresh.setOnClickListener(this);

	}

	private void initData() {

		mPullRefreshLayout.setOnPullListener(new QMUIPullRefreshLayout.OnPullListener() {
			@Override
			public void onMoveTarget(int offset) {

			}

			@Override
			public void onMoveRefreshView(int offset) {

			}

			@Override
			public void onRefresh() {
				mPullRefreshLayout.postDelayed(new Runnable() {

					@Override
					public void run() {
						//扫描时间
						long scanTime = 0;
						final QMUITipDialog tipDialog;

						requestMutiCastPermission();
						openBroadcast(getContext());

						//加载扫描到的服务
						ConfigWifi configWifi = ConfigWifi.init();
						configWifi.scanMDNS();
						mdnsList = configWifi.getMdnsList();
						flag = true;

						while(flag) {
							if (mdnsList.size() > 0) {
		        		/*ListAdapter mAdapter = new MyAdapter(getContext(), mdnsList);
		        		mlistView.setAdapter(mAdapter);
		        		new WifiUtils.Utility().setListViewHeightBasedOnChildren(mlistView);
		        		flag=false;*/
		        				//显示服务信息
								initListView(mdnsList);
								flag = false;
							}
							if ((System.currentTimeMillis() - scanTime) > 10000) {
								scanTime = System.currentTimeMillis();
							} else {
								//超时退出
								flag = false;
							}
						}
						if (!flag){
							tipDialog = new QMUITipDialog.Builder(getContext())
									.setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
									.setTipWord("扫描超时，请稍候重试")
									.create();
							tipDialog.show();

							//显示两秒关闭
							mListView.postDelayed(new Runnable() {
							@Override
							public void run() {
								tipDialog.dismiss();
							}
						},2000);

						}
						mPullRefreshLayout.finishRefresh();


					}
				}, 10000);
			}
		});
	}


	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()){
			/*case R.id.startCon:
//				myApplication = (MyApplication)this.getApplicationContext();
				if (IP.getText().toString().trim().equals("")||
						//LOCAL_PORT.getText().toString().trim().equals("")||
						Port.getText().toString().trim().equals("")) {
					Toast.makeText(getContext(), "请输入完整", Toast.LENGTH_SHORT).show();
				}else {
					Server_IP = IP.getText().toString();
					Server_Port = Port.getText().toString();
					Toast.makeText(getContext(),Server_IP+"   "+Server_Port, Toast.LENGTH_SHORT).show();
					//Application存储全局变量
					mdnsMap = new HashMap<String, Object>();
					mdnsMap.put("IP", Server_IP);
					mdnsMap.put("Port", Server_Port);

					myApplication.setMap(mdnsMap);

				}
				break;*/

			/*case R.id.scanService:
				new Thread(){
					@Override
					public void run()
					{
						openBroadcast(getContext());

						//加载扫描到的服务
						ConfigWifi configWifi = ConfigWifi.init();
						configWifi.scanMDNS();
						mdnsList = configWifi.getMdnsList();
						flag = true;

					}
				}.start();
				while(flag){
					if (mdnsList.size()>0) {
		        		*//*ListAdapter mAdapter = new MyAdapter(getContext(), mdnsList);
		        		mlistView.setAdapter(mAdapter);
		        		new WifiUtils.Utility().setListViewHeightBasedOnChildren(mlistView);
		        		flag=false;*//*
						initGroupListView(mdnsList);
						flag = false;
					}

				}
				break;*/
			default:
				break;
		}

	}


	public void initListView(final List<Map<String, Object>> list){
		ListAdapter mAdapter = new MyListViewAdapter(getActivity(), list);
		mListView.setAdapter(mAdapter);
		new WifiUtils.Utility().setListViewHeightBasedOnChildren(mListView);

		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
				new QMUIDialog.MessageDialogBuilder(getActivity())
						.setTitle("连接站点")
						.setMessage("是否连接？")
						.addAction("取消", new QMUIDialogAction.ActionListener() {
							@Override
							public void onClick(QMUIDialog dialog, int index) {
								dialog.dismiss();
							}
						})
						.addAction("确定", new QMUIDialogAction.ActionListener() {
							@Override
							public void onClick(QMUIDialog dialog, int index) {
								myApplication = (MyApplication)getActivity().getApplicationContext();

								Server_IP = list.get(position).get("Service_IP").toString();
								Server_Port = list.get(position).get("Service_Port").toString();

								//Application存储全局变量
								map = new HashMap<String, Object>();
								map.put("IP", Server_IP);
								map.put("Port", Server_Port);

								myApplication.setMap(map);

								dialog.dismiss();
							}
						})
						.show();
			}
		});

	}

}