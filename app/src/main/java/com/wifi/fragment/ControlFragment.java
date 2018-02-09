package com.wifi.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.java.bulb.BulbControl;
import com.qmuiteam.qmui.alpha.QMUIAlphaImageButton;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.popup.QMUIPopup;
import com.wifi.main.R;
import com.wifi.utils.CommonUtils;
import com.wifi.utils.MyApplication;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ControlFragment extends Fragment implements View.OnClickListener {

    private TextView mTextMessage;

    public static final String TAG = "MainActivity";
    private Button send_udp,receive_udp,coapServer,coapClient;
    private long exitTime = 0;

    //发送或者接收的文本
    public TextView send_msg,receive_msg;
    //目的主机IP
    private String SERVER_IP;
    private int SERVER_PORT;

    private TextView infomation;

    private String message;
    private byte[] meeeagebyte;

    //用户输入的灯泡ID
    private int  bulbID;

    //用来存储全局变量，用于Activity之间的传递
    private MyApplication myApplication;
    //定时器,用来存储全局变量检测是否接收到成功消息
    private Timer timer;

    //灯泡的状态
    private String status = "off";

    private Thread thread;

    //灯泡控制类
    private BulbControl bulbControl;

    //顶部标题栏
    private QMUITopBar topBar;

    //普通浮层
    private QMUIPopup mNormalPopup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, container, false);
        initViews(view);
        //udpUtils = new UDPUtils(SERVER_IP,SERVER_PORT);

//        infomation.append("目的IP： "+SERVER_IP+"\n"+"目的端口： "+SERVER_PORT+"\n");

        return view;
    }



    /**
     * 控件初始化
     */
    public void initViews(View view){
        send_udp = (Button) view.findViewById(R.id.send_udp);
//        receive_udp = (Button) findViewById(R.id.receive_udp);
        send_msg =  (TextView)view.findViewById(R.id.messageID);
//        receive_msg = (EditText) view.findViewById(R.id.receive);
        infomation =(TextView) view.findViewById(R.id.information);
//        coapServer = (Button) findViewById(R.id.coapServer);
//        coapClient = (Button) findViewById(R.id.coapClient);

        //设置标题栏
        topBar = (QMUITopBar) view.findViewById(R.id.topbar);
        TextView title = topBar.setTitle(R.string.app_name);
        title.setTextSize(23);
        title.setTextColor(getResources().getColor(R.color.qmui_config_color_white));
        topBar.setBackgroundColor(getResources().getColor(R.color.qmui_config_color_blue));
        topBar.showTitleView(true);
        //添加左边返回按钮
//        QMUIAlphaImageButton left = topBar.addLeftBackImageButton();


//        receive_udp.setOnClickListener(MainActivity.this);
        send_udp.setOnClickListener(ControlFragment.this);

        send_msg.setOnClickListener(ControlFragment.this);
        infomation.setOnClickListener(ControlFragment.this);
//        coapClient.setOnClickListener(MainActivity.this);
//        coapServer.setOnClickListener(MainActivity.this);
    }

    //初始化普通浮层
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
            textView.setText("目的IP： "+SERVER_IP+"\n"+"目的端口： "+SERVER_PORT+"\n");
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.app_color_description));
            mNormalPopup.setContentView(textView);
            mNormalPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    infomation.setText(getContext().getResources().getString(R.string.connect_info));
                }
            });
        }
    }

    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.send_udp:
                //创建配置类
                bulbControl = BulbControl.init(SERVER_IP,SERVER_PORT);

                getIPAndPort();
                //启动监听线程
           /*     thread = new Thread(){
                    @Override
                    public void run(){
                        bulbControl.startListen(controlHandler);
                    }
                };
                thread.start();*/

                if (SERVER_IP==null||SERVER_IP==""||SERVER_PORT==0){
                    Toast.makeText(getContext(), "请输入合法IP和端口！", Toast.LENGTH_SHORT).show();
                }
                String ID = send_msg.getText().toString().trim();
                if (!ID.equals("")&&ID!=null&& CommonUtils.isInteger(ID)){
                    bulbID = Integer.parseInt(ID);
//                		if (send_udp.getText().equals("打开")) {
//                			for (int i = 0; i < 3; i++) {
//                				new Thread(){
//                					@Override
//									public void run(){
//                						bulbControl.openBulb(bulbID);
//                					}
//                				}.start();
//							}
//                			send_udp.setText("关闭");
//						}else {
//							for (int i = 0; i < 3; i++) {
//								new Thread(){
//                					@Override
//									public void run(){
//                						bulbControl.closeBulb(bulbID);
//                					}
//                				}.start();
//							}
//							send_udp.setText("打开");
//						}

                        timer = new Timer();
                        //每隔1秒执行一次
                        timer.schedule(new Mytask(), 0, 100);


                    }else {
                        Toast.makeText(getContext(), "请输入合法数字！", Toast.LENGTH_SHORT).show();
                    }

                break;
	       /* case R.id.receive_udp:
	        	thread = new Thread(udpUtils);
	        	thread.start();
	        	udpUtils.setMessage(udpUtils.getMessage());
	        	break;*/

	       /* case R.id.coapServer:
	        	Intent intent = new Intent();
	        	intent.setClass(MainActivity.this, CoAPServerActivity.class);
	        	this.startActivity(intent);
	        	break;
	        case R.id.coapClient:
	        	intent = new Intent();
	        	intent.setClass(MainActivity.this, CoAPClientActivity.class);
	        	this.startActivity(intent);
	        	break;*/
	       //输入灯泡ID弹出对话框
            case R.id.messageID:
                final  QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(getActivity());
                builder.setTitle("灯泡ID输入")
                        .setPlaceholder("请在此输入灯泡ID")
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
                    String ID = text.toString().trim();
                    if (!ID.equals("")&&ID!=null&&CommonUtils.isInteger(ID)) {
                        bulbID = Integer.parseInt(ID);
                        if(bulbID>=0&&bulbID<=32767) {
                            send_msg.setText(bulbID+"");
                            dialog.dismiss();
                        }else {
                            Toast.makeText(getActivity(), "请填入合法灯泡ID", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "请填入合法灯泡ID", Toast.LENGTH_SHORT).show();
                    }
                }
            }).show();
                break;
            case R.id.information:
                getIPAndPort();

                initNormalPopupIfNeed();
                mNormalPopup.setAnimStyle(QMUIPopup.ANIM_GROW_FROM_CENTER);
                mNormalPopup.setPreferredDirection(QMUIPopup.DIRECTION_TOP);
                mNormalPopup.show(view);
                infomation.setText(getContext().getResources().getString(R.string.hide_connect_info));
                break;

            default:
                break;
        }
    }
    Handler controlHandler = new Handler(){
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = new Bundle();
            bundle = msg.getData();
//            receive_msg.setText(bundle.getString("config_receive"));
            message = bundle.getString("config_receive");
            meeeagebyte = bundle.getByteArray("bulb_receive");
        }
    };

    /**
     * 获取IP和端口
     */
    public void getIPAndPort(){
        myApplication = (MyApplication) getActivity().getApplicationContext();

        Map<String, Object> map;

        map = myApplication.getMap();

        if (!map.isEmpty()) {
            SERVER_IP = map.get("IP").toString();
            SERVER_PORT = Integer.parseInt(map.get("Port").toString().trim());

        }else {
            SERVER_PORT = 1111;
            SERVER_IP = "255.255.255.0";
        }
    }
    /**
     * 定时执行的类
     * @author 17993
     *
     */
    class Mytask extends TimerTask {

        @Override
        public void run() {

            //监听到的返回信息
            Message msg = new Message();
            Bundle bundle = new Bundle();

            //String receiveMessage = udpUtils.getMessage();

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


            //如果没有收到对灯泡操作的确认消息
            if (message!=null&&message.equals("success")){
//			if (bulbControl.isControlSuccess(meeeagebyte)){
                message = "test";
                //如果反馈回来的灯泡打开成功
                if (send_udp.getText().equals("打开灯泡")) {
                    send_udp.setText("关闭灯泡");
                }else if (send_udp.getText().equals("关闭灯泡")){
                    send_udp.setText("打开灯泡");
                }
                //线程终止
                timer.cancel();

            }else {
                i++;
                if (i>3) {
                    message="success";
                    i=0;
                }
                if (send_udp.getText().equals("打开")) {
                    //在子线程内进行网络操作，否则Android5.1不能发数据
                    new Thread(){
                        @Override
                        public void run()
                        {
                            bulbControl.openBulb(bulbID);
                        }
                    }.start();
                    //Toast.makeText(MainActivity.this, "正在打开，请等一哈...", Toast.LENGTH_SHORT).show();
                }else {
                    new Thread(){
                        @Override
                        public void run()
                        {
                            bulbControl.closeBulb(bulbID);
                        }
                    }.start();
                    //Toast.makeText(MainActivity.this, "正在关闭，请等一哈...", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
//        bulbControl.stopListen();
    }

}
