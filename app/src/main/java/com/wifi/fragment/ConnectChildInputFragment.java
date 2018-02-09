package com.wifi.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.wifi.controller.ConnectChildFragmentController;
import com.wifi.main.R;
import com.wifi.utils.CommonUtils;
import com.wifi.utils.MyApplication;

import java.util.HashMap;
import java.util.Map;

public class ConnectChildInputFragment extends Fragment implements OnClickListener{

    //目的ip和端口
    private String Server_IP;
    private String Server_Port;

    //输入的IP和端口
    private TextView IP,Port;
    //输入端口和IP的布局
	private RelativeLayout IP_Relative,Port_Relative;

	//信息提交
    private Button commit_input;

	//存放全局变量
	private MyApplication myApplication;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}


    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState){

		View view = inflater.inflate(R.layout.child_fragment_connect_input, container, false);
		initViews(view);
		return view;
	}
    

    
    /**
     * 控件初始化
     */
    public void initViews(View view) {

    	//IP和端口输入框
		IP = (TextView) view.findViewById(R.id.IPText);
		Port = (TextView) view.findViewById(R.id.PortText);

//		iptext = (TextView) view.findViewById(R.id.iptext);
//		porttext = (TextView) view.findViewById(R.id.porttext);

		IP_Relative = view.findViewById(R.id.IP_Relative);
		Port_Relative = view.findViewById(R.id.Port_Relative);

		commit_input = (Button) view.findViewById(R.id.commit_input);

		commit_input.setOnClickListener(this);
		IP_Relative.setOnClickListener(this);
		Port_Relative.setOnClickListener(this);

	}

	/**
	 * 按钮点击事件
	 */
	@Override
	public void onClick(View v){
		final  QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());

		switch (v.getId()) {
			case R.id.commit_input:
				if (IP.getText().toString().trim().equals("")||
						//LOCAL_PORT.getText().toString().trim().equals("")||
						Port.getText().toString().trim().equals("")) {
					Toast.makeText(getActivity(),"请输入完整！", Toast.LENGTH_SHORT).show();
				}else {
					myApplication = (MyApplication)getActivity().getApplicationContext();
					Server_IP = IP.getText().toString();
					Server_Port = Port.getText().toString();

					//Application存储全局变量
					Map<String,Object> map = new HashMap<String, Object>();
					map.put("IP", Server_IP);
					map.put("Port", Server_Port);

					myApplication.setMap(map);

					//操作提示
					final QMUITipDialog tipDialog;
					tipDialog = new QMUITipDialog.Builder(getContext())
							.setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
							.setTipWord("操作成功")
							.create();
					tipDialog.show();

					//显示2秒
					Port_Relative.postDelayed(new Runnable() {
						@Override
						public void run() {
							tipDialog.dismiss();
						}
					},2000);

				}
				break;

			case R.id.IP_Relative:
				builder.setTitle("站点IP输入")
						.setPlaceholder("请在此输入站点IP！")
						.setInputType(InputType.TYPE_CLASS_TEXT)
						.addAction("取消", new QMUIDialogAction.ActionListener() {
							@Override
							public void onClick(QMUIDialog dialog, int index) {
								dialog.dismiss();
							}
						})
						. addAction("确定", new QMUIDialogAction.ActionListener() {
							@Override
							public void onClick(QMUIDialog dialog, int index) {
								CharSequence text = builder.getEditText().getText();
								//获得出输入的IP
								String IPv4 = text.toString().trim();
								if (CommonUtils.isValidIP(IPv4)) {
									IP.setText(IPv4);
									dialog.dismiss();
								} else {
									Toast.makeText(getActivity(), "请填入合法的IP", Toast.LENGTH_SHORT).show();
								}
							}
						}).show();
				break;
			case R.id.Port_Relative:
				builder.setTitle("站点端口输入")
						.setPlaceholder("请在此输入站点端口！")
						.setInputType(InputType.TYPE_CLASS_TEXT)
						.addAction("取消", new QMUIDialogAction.ActionListener() {
							@Override
							public void onClick(QMUIDialog dialog, int index) {
								dialog.dismiss();
							}
						})
						. addAction("确定", new QMUIDialogAction.ActionListener() {
							@Override
							public void onClick(QMUIDialog dialog, int index) {
								CharSequence text = builder.getEditText().getText();
								//获得出入的ID
								String station_Port = text.toString().trim();
								if (CommonUtils.isInteger(station_Port)) {
									Port.setText(station_Port);
									dialog.dismiss();
								} else {
									Toast.makeText(getActivity(), "请填入合法的IP", Toast.LENGTH_SHORT).show();
								}
							}
						}).show();
				break;
			default:
				break;
		}

	}


}