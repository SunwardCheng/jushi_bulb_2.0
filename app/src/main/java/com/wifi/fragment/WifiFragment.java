package com.wifi.fragment;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.java.bulb.ConfigWifi;
import com.java.bulb.impl.wifi.WIFIAdmin;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITabSegment;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.wifi.main.R;
import com.wifi.utils.WifiUtils;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


public class WifiFragment extends Fragment implements View.OnClickListener{
	public static final String TAG = "WIFIActivity";  
	//展示扫描的wifi信号
    private ListView mlistView;  
    protected WIFIAdmin mWifiAdmin;  
    private List<ScanResult> mWifiList;  
    public int level;  
    protected String ssid;

    private String SSID,Password;
    private String SERVER_IP;
    private int SERVER_PORT;
    private ConfigWifi configWifi ;

    private RelativeLayout relativeLayout;
    private Button scan_wifi;
	private TextView wifi_info;

	private QMUITopBar topBar;
	//普通浮层
	private QMUIPopup mNormalPopup;
    //定时器
    private Timer timer ;
    /**
     * 接收的消息
     */
    private String message;
    
    private Thread thread;
    //请求权限
	private static final int REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){
		View view = inflater.inflate(R.layout.fragment_wifi, container, false);

		mWifiAdmin = new WIFIAdmin(getContext());

        SERVER_IP = mWifiAdmin.getWifiIP();
        SERVER_PORT = 9090;
        
        if (SERVER_IP!=""&&SERVER_IP!=null) {
        	 //创建配置类
            configWifi = ConfigWifi.initWifi(SERVER_IP);
		}
        IntentFilter filter = new IntentFilter(
        		WifiManager.NETWORK_STATE_CHANGED_ACTION);
        
        //监听wifi变化
        getContext().registerReceiver(mReceiver, filter);

		initViews(view);
		getSSID();

        return  view;
    }

    private class MyListViewItemListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			ssid=mWifiList.get(position).SSID;
			//SSID是初始时连接的名称，ssid是即将要连接的名称
			SSID = ssid;

			final  QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
			builder.setTitle(SSID)
					.setPlaceholder("输入WIFI密码")
					.setInputType(InputType.TYPE_CLASS_TEXT)
					.addAction("取消", new QMUIDialogAction.ActionListener() {
						@Override
						public void onClick(QMUIDialog dialog, int index) {
							dialog.dismiss();
						}
					})
					. addAction("配置到站点", new QMUIDialogAction.ActionListener() {
						@Override
						public void onClick(QMUIDialog dialog, int index) {
							Password = builder.getEditText().getText().toString();
							if(null == Password  || Password.length() < 8){
								Toast.makeText(getContext(), "密码至少8位", Toast.LENGTH_SHORT).show();
							}else{
								getSSID();
								//如果未收到成功回复每隔1秒发送一次SSID和密码
								timer = new Timer();
								timer.schedule(new Mytask(), 0, 100);
								dialog.dismiss();
							}

						}
					}).show();
		}
	}

	/**
	 * 控件初始化
	 */
	public void initViews(View view){
		mlistView=(ListView) view.findViewById(R.id.wifi_list);

		relativeLayout = (RelativeLayout) view.findViewById(R.id.wifi_relative);

		wifi_info = (TextView) view.findViewById(R.id.wifi_info);
		scan_wifi = (Button)view.findViewById(R.id.scan_wifi_);

		//设置标题栏
		topBar = (QMUITopBar) view.findViewById(R.id.topbar);
		TextView title = topBar.setTitle(R.string.app_name);
		title.setTextSize(23);
		title.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
		topBar.setBackgroundColor(getResources().getColor(R.color.qmui_config_color_blue));
		topBar.showTitleView(true);

		//顶部右边按钮事件
		topBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_change_button)
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						showBottomSheetList();
					}
				});



		relativeLayout.setOnClickListener(this);
		scan_wifi.setOnClickListener(this);

		mlistView.setOnItemClickListener(new MyListViewItemListener());

	}

	//查看WIFI信息的初始化普通浮层
	public void initNormalPopupIfNeed(){
		if (mNormalPopup == null){
			mNormalPopup = new QMUIPopup(getContext(),QMUIPopup.DIRECTION_NONE);
			TextView textView = new TextView(getContext());
			textView.setLayoutParams(mNormalPopup.generateLayoutParam(
					QMUIDisplayHelper.dp2px(getContext(), 250),
					WRAP_CONTENT
			));
			textView.setLineSpacing(QMUIDisplayHelper.dp2px(getContext(), 4), 1.0f);
			int padding = QMUIDisplayHelper.dp2px(getContext(), 20);
			textView.setPadding(padding, padding, padding, padding);
			textView.setText("SSID： "+SSID+"\n"+"密码： "+"******"+"\n");
			textView.setTextColor(ContextCompat.getColor(getContext(), R.color.app_color_description));
			mNormalPopup.setContentView(textView);
			mNormalPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					wifi_info.setText(getContext().getResources().getString(R.string.wifi_info));
				}
			});
		}
	}


	/**
	 * 底部弹出选择框
	 */
	private void showBottomSheetList() {
		new QMUIBottomSheet.BottomListSheetBuilder(getActivity())
				.addItem(getResources().getString(R.string.check_wifi))
				.addItem(getResources().getString(R.string.open_wifi))
				.addItem(getResources().getString(R.string.close_wifi))
				.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
					 @Override
					 public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
						 dialog.dismiss();
						 switch (position) {
							 case 0:
								 mWifiAdmin.checkState(getActivity());
								 break;
							 case 1:
								 mWifiAdmin.openWifi(getActivity());
								 break;
							 case 2:
								 mWifiAdmin.closeWifi(getActivity());
								 break;
						 }
					 }
				 })
				.build()
				.show();
	}

					/**
     * 获取监听消息的Handler
     */
    Handler controlHandler = new Handler(){
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = new Bundle();
			bundle = msg.getData();
			message = bundle.getString("config_receive"); 
			
		}
    };

	@Override
	public void onClick(View v) {

		switch (v.getId()){
			case R.id.wifi_relative:
				initNormalPopupIfNeed();
				mNormalPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);
				mNormalPopup.setPreferredDirection(QMUIPopup.DIRECTION_TOP);
				mNormalPopup.show(v);
				wifi_info.setText(getContext().getResources().getString(R.string.hide_wifi_info));
				break;
			case R.id.scan_wifi_:

				requestLocationPermission();

				mWifiAdmin = new WIFIAdmin(getActivity());
				mWifiAdmin.startScan(getActivity());
				mWifiList = mWifiAdmin.getWifiList();

				if(mWifiList!=null&&mWifiList.size()>0){
					mlistView.setAdapter(new MyAdapter(getContext(),mWifiList));
					new WifiUtils.Utility().setListViewHeightBasedOnChildren(mlistView);
				}
				break;


		}
	}


	/**
	 * 判断SDK版本，高于23，要打开ACCESS_COARSE_LOCATION位置权限
	 */
	public void requestLocationPermission(){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//如果 API level 是大于等于 23(Android 6.0) 时
			//判断是否具有权限
			if (ContextCompat.checkSelfPermission(getActivity(),
					Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				//判断是否需要向用户解释为什么需要申请该权限
				if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
						Manifest.permission.ACCESS_COARSE_LOCATION)) {
					Toast.makeText(getActivity(), "自Android 6.0开始需要打开位置权限才可以搜索到WIFI设备", Toast.LENGTH_SHORT);

				}
				//请求权限
				ActivityCompat.requestPermissions(getActivity(),
						new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
						REQUEST_CODE_ACCESS_COARSE_LOCATION);
			}
		}
	}
	/**
	 * 用户处理申请权限的回调结果
	 * @param requestCode
	 * @param permissions
	 * @param grantResults
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CODE_ACCESS_COARSE_LOCATION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				//用户允许改权限，0表示允许，-1表示拒绝 PERMISSION_GRANTED = 0， PERMISSION_DENIED = -1
				//permission was granted, yay! Do the contacts-related task you need to do.
				//这里进行授权被允许的处理
			} else {
				//permission denied, boo! Disable the functionality that depends on this permission.
				//这里进行权限被拒绝的处理
			}
		} else {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
	/**
     * 时钟任务
     * @author 17993
     *
     */
    class Mytask extends TimerTask{

		@Override
		public void run() {
			Message msg = new Message();
            Bundle bundle = new Bundle();
            
            //把数据放到buddle中
            bundle.putString("receive", "test");
            //把buddle传递到message
            msg.setData(bundle);
            myHandler.sendMessage(msg);
          
		}
		
	}
    /**
     * 使用Handler传递数据，避免内部线程不能创建handler
     */
    Handler myHandler = new Handler(){
    	int i = 0;
    	@Override
		public void handleMessage(Message msg)										
		{											
			super.handleMessage(msg);
			Bundle bundle = new Bundle();
			bundle = msg.getData();
			
			
			if (message!=null&&message.equals("success")){
				Toast.makeText(getContext(), "配置成功！", Toast.LENGTH_SHORT).show();
				//连接到配置的WIFI上
				mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(ssid, Password, 3));
				
				//取消定时
				timer.cancel();
//				Intent intent = new Intent();
//				intent.setClass(WIFIActivity.this, IPActivity.class);
//	        	startActivity(intent);
//	        	finish();
	        	
			}else {
				i++;
				if (i>3) {
					message="success";
					i=0;
				}
				
				//Toast.makeText(WIFIActivity.this, "正在配置，请稍等...", Toast.LENGTH_SHORT).show();
				//给站点配置wifi
				
				new Thread(){  
					   @Override  
					   public void run()  
					   {  
						   configWifi.config(ssid, Password);
					  }  
					}.start();
			}
		}		
	};

	
    /**
     * 获取SSID
     */
    public void getSSID() {
    	SSID =  mWifiAdmin.getSSID();
	}
	
	public class MyAdapter extends BaseAdapter{
		LayoutInflater inflater;
		List<ScanResult> list;
		
		public MyAdapter(Context context, List<ScanResult> list) {
			this.inflater = LayoutInflater.from(context);
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();  
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);  
		}

		@Override
		public long getItemId(int position) {
			return position;  
		}

		//忽略指定的警告
		@SuppressLint({ "ViewHolder", "InflateParams" })  
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			//针对每一个数据（即每一个List ID）创建一个ListView实例，
			View view = null;
			view = inflater.inflate(R.layout.wifi_listitem, null);
			ScanResult result = list.get(position);
			TextView wifi_ssid=(TextView) view.findViewById(R.id.ssid);  
			ImageView wifi_level=(ImageView) view.findViewById(R.id.wifi_level); 
			wifi_ssid.setText(result.SSID);  
			Log.i(TAG, "scanResult.SSID="+result);
			
			level=WifiManager.calculateSignalLevel(result.level,5);  
			
			//两种wifi图像 
			if(result.capabilities.contains("WEP")||result.capabilities.contains("PSK")||  
					result.capabilities.contains("EAP")){  
                wifi_level.setImageResource(R.mipmap.wifilock);
            }else{  
                wifi_level.setImageResource(R.mipmap.wifi);
            }  
            wifi_level.setImageLevel(level);  
            //判断信号强度，显示对应的指示图标    
             return view;  
		}
		
	}

    /**
     * 监听wifi状态  
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver (){    
        @Override    
        public void onReceive(Context context, Intent intent) {     
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
  
            NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);     
            if(wifiInfo.isConnected()){  
                WifiManager wifiManager = (WifiManager) context  
                        .getSystemService(Context.WIFI_SERVICE);  
                String wifiSSID = wifiManager.getConnectionInfo()  
                        .getSSID();  
//                Toast.makeText(context, wifiSSID+"连接成功", Toast.LENGTH_SHORT).show();
            }
        }     
        
    };   
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	configWifi.stopListen();
    }

}
