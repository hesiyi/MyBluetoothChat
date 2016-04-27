package com.example.mybluetoothchat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

@SuppressLint({ "NewApi", "HandlerLeak" })
public class BluetoothChat extends Activity implements OnClickListener,
		OnItemClickListener {
	// Debugging test
	private static final String TAG = BluetoothChat.class.getSimpleName();
	private static final boolean DEBUG = true;
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	private static final int REQUEST_GET_IMG = 4;
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	// Layout ID
	private static final int LayoutId_Me = R.layout.list_say_me_item;
	private static final int LayoutId_Other = R.layout.list_say_other_item;
	private static final int LayoutId_Room = R.layout.activity_multi_chat_room;
	private static final int LayoutId_Face = R.layout.face_list;
	// View ID
	private static final int ViewId_faceButton = R.id.imgButton;
	private static final int ViewId_messageEditext = R.id.MessageText;
	private static final int ViewId_messageButton = R.id.MessageButton;
	private static final int ViewId_talkRecodeView = R.id.list;
	private static final int ViewId_searchButton = R.id.search;
	private static final int ViewId_bagsetButton = R.id.bag_set;
	private static final int ViewId_mainLayout = R.id.mainlayout;

	// Member object for the chat services
	private BluetoothChatService mChatService = null;
	// 本地蓝牙设备信息
	private BluetoothDevice bluetoothDevice;
	// Name of the connected device
	private String mConnectedDeviceName = null;
	// String buffer for outgoing messages
	private StringBuffer mOutStringBuffer;
	// 泡沫消息框的数据包
	private ChatMsgEntity newMessage;
	// 表情的图片按钮
	private ImageButton faceButton;
	// 发送消息的编辑框
	private EditText messageEditext;
	// 发送消息的按钮
	private Button messageButton;
	// 消息记录列表
	private ListView talkRecodeView;
	// 信息集合
	private ArrayList<ChatMsgEntity> msgList;
	// PopupWindow of showing varoius faces
	private PopupWindow imgPopupWindow;
	// 对象搜素按钮
	private Button searchButton;
	// 背景设置按钮
	private Button bagsetButton;
	// 背景设置画布
	private View roomView;
	// 背景Drawable
	private Drawable backgroundDrawable;
	// 表情信息
	private FaceInfo mFaceInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		if (DEBUG)
			Log.e(TAG, "+++ ON CREATE +++");
		// Set up the window layout
		setContentView(LayoutId_Room);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.theme_attr); // titlebar为自己标题栏的布局

		// Views initialization with Ids from window layout
		roomView = findViewById(ViewId_mainLayout);
		faceButton = (ImageButton) findViewById(ViewId_faceButton);
		messageEditext = (EditText) findViewById(ViewId_messageEditext);
		messageButton = (Button) findViewById(ViewId_messageButton);
		talkRecodeView = (ListView) findViewById(ViewId_talkRecodeView);
		searchButton = (Button) findViewById(ViewId_searchButton);
		bagsetButton = (Button) findViewById(ViewId_bagsetButton);
		// 表情的信息
		mFaceInfo = new FaceInfo(this);
		// 创建弹出窗口的GrideView的对象
		GridView gvPopupWindow = (GridView) getLayoutInflater().inflate(
				LayoutId_Face, null);
		gvPopupWindow.setAdapter(new GalleryAdapter(this, mFaceInfo.mfacesId));
		gvPopupWindow.setOnItemClickListener(this);
		// popuwindow 初始化
		imgPopupWindow = new PopupWindow(gvPopupWindow,
				LayoutParams.WRAP_CONTENT, 160);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		if (DEBUG)
			Log.e(TAG, "++ ON START ++");
		// If the adapter is null, then Bluetooth is not supported
		bluetoothDevice = new BluetoothDevice();
		if (!bluetoothDevice.isPhoneDevice_Support()) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		// 初始化信息列
		msgList = new ArrayList<ChatMsgEntity>();
		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!bluetoothDevice.isBluetoothAdapter_Enable()) {
			// Otherwise, setup the chat session
			Toast.makeText(this, "亲，先开启蓝牙服务。", Toast.LENGTH_LONG).show();
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (DEBUG) Log.e(TAG, "+ ON RESUME +");

		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started  already
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
			}
		}
		if (messageEditext.getText().toString().length() > 0) {
			messageEditext.setText(mOutStringBuffer);
		}
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (DEBUG)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (DEBUG)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (DEBUG)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	// Intent 返回处理机制
	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_CONNECT_DEVICE_SECURE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK) {
					connectDevice(data, true);
				}
				break;
			case REQUEST_CONNECT_DEVICE_INSECURE:
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK) {
					connectDevice(data, false);
				}
				break;
			case REQUEST_ENABLE_BT:
				// When the request to enable Bluetooth returns
				if (resultCode == Activity.RESULT_OK) {
					// Bluetooth is now enabled, so set up a chat session
					setupChat();
				} else {
					// User did not enable Bluetooth or an error occurred
					Log.d(TAG, "BT not enabled");
					Toast.makeText(this, R.string.bt_not_enabled_leaving,
							Toast.LENGTH_SHORT).show();
					finish();
				}
				break;
			case REQUEST_GET_IMG:
				if (resultCode == Activity.RESULT_OK) {
					Uri uri = data.getData();
					ContentResolver cresolver = getContentResolver();
					try {
						InputStream is = cresolver.openInputStream(uri);
						backgroundDrawable = Drawable.createFromStream(is, null);
						roomView.setBackgroundDrawable(backgroundDrawable);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.multi_chat_room, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
			case R.id.startService:
				// Start bluetooth service
				Intent enableIntent = new Intent(
						bluetoothDevice.Action_Bluetooth_On);
				startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
				return true;
			case R.id.secure_connect_scan:
				// Launch the DeviceListActivity to see devices and do scan
				serverIntent = new Intent(this, com.example.mybluetoothchat.DevicesListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
				return true;
			case R.id.insecure_connect_scan:
				// Launch the DeviceListActivity to see devices and do scan
				serverIntent = new Intent(this, com.example.mybluetoothchat.DevicesListActivity.class);
				startActivityForResult(serverIntent,
						REQUEST_CONNECT_DEVICE_INSECURE);
				return true;
			case R.id.discoverable:
				// Ensure this device is discoverable by others
				ensureDiscoverable();
				return true;
		}
		return false;
	}

	/* 处理点击事件函数 */
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Onclick");
		if (view == messageButton) {
			if (bluetoothDevice.isBluetoothAdapter_Enable()) {
				String msg = messageEditext.getText().toString();
				sendMessage(msg);
			} else {
				Log.i(TAG, "Unable");
				messageEditext.setText(mOutStringBuffer);
				Toast.makeText(this, "不在线", Toast.LENGTH_LONG).show();
			}
			return;
		}
		if (view == faceButton) {
			if (imgPopupWindow != null) {
				if (imgPopupWindow.isShowing()) {
					imgPopupWindow.dismiss();
				} else {
					imgPopupWindow.showAtLocation(faceButton, Gravity.BOTTOM,
							0, 0);
				}
			}
			return;
		}
		if (view == searchButton) {
			Intent serverIntent = null;
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, com.example.mybluetoothchat.DevicesListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return;
		}
		if (view == bagsetButton) {
			Intent imgIntent = new Intent();
			// Launch the DeviceListActivity to see devices and do scan
			imgIntent.setType("image/*");
			imgIntent.setAction(Intent.ACTION_GET_CONTENT);
			/* 取得相片后返回本画面 */
			startActivityForResult(imgIntent, REQUEST_GET_IMG);

		}
	}

	/*
	 * 自定义函数
	 */
	public void setupChat() {
		Log.i(TAG, ">>setup chat<<");
		// 消息发送按钮点击监听
		messageButton.setOnClickListener(this);
		// 表情按钮点击监听
		faceButton.setOnClickListener(this);
		// 搜素按钮点击监听
		searchButton.setOnClickListener(this);
		// 背景设置按钮点击监听
		bagsetButton.setOnClickListener(this);
		// Initialize the BluetoothChatService to perform bluetooth connections
		mChatService = new BluetoothChatService(this, mHandler);
		// Initialize the buffer for outgoing messages
		mOutStringBuffer = new StringBuffer("");
		imgPopupWindow.setFocusable(true);
	}

	public void ensureDiscoverable() {
		Log.i(TAG, ">>ensure discoverable<<");
		if (bluetoothDevice.getBluetoothScanMode() != bluetoothDevice.Mode_Connectable_Discoverable) {
			Intent discoverableIntent = new Intent(
					bluetoothDevice.Action_Bluetooth_Discoverable);
			discoverableIntent.putExtra(bluetoothDevice.Data_Dicoverable, 300);
			startActivity(discoverableIntent);
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				com.example.mybluetoothchat.DevicesListActivity.EXTRA_DEVICE_ADDRESS);
		Log.e(TAG, address);
		// Get the BluetoothDevice object
		android.bluetooth.BluetoothDevice device = bluetoothDevice
				.getRomteBluetoothDevice(address);
		// Attempt to connect to the device
		mChatService.connect(device, secure);
	}

	private final void setStatus(int resId) {
		// final ActionBar actionBar = getActionBar();
		// actionBar.setSubtitle(resId);
	}

	@SuppressLint("NewApi")
	private final void setStatus(CharSequence subTitle) {
		// final ActionBar actionBar = getActionBar();
		// actionBar.setSubtitle(subTitle);
	}

	/**
	 * Sends a message.
	 *
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothChatService to write
			byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field
			mOutStringBuffer.setLength(0);
			messageEditext.setText(mOutStringBuffer);
		}
	}

	/* The Handler that gets information back from the BluetoothChatService */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MESSAGE_STATE_CHANGE:
					switch (msg.arg1) {
						case BluetoothChatService.STATE_CONNECTED:
							setStatus(getString(R.string.title_connected_to,
									mConnectedDeviceName));
							break;
						case BluetoothChatService.STATE_CONNECTING:
							setStatus(R.string.title_connecting);
							break;
						case BluetoothChatService.STATE_LISTEN:
						case BluetoothChatService.STATE_NONE:
							setStatus(R.string.title_not_connected);
							break;
					}
					break;
				case MESSAGE_WRITE:
					byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
					String writeMessage = new String(writeBuf);
					// 记录聊天信息
					newMessage = new ChatMsgEntity("我", "1013-05-15", writeMessage,
							LayoutId_Me);
					msgList.add(newMessage);
					talkRecodeView.setAdapter(new ChatMsgViewAdapter(
							BluetoothChat.this, msgList));
					break;
				case MESSAGE_READ:
					byte[] readBuf = (byte[]) msg.obj;
					// construct a string from the valid bytes in the buffer
					String readMessage = new String(readBuf, 0, msg.arg1);
					newMessage = new ChatMsgEntity(mConnectedDeviceName,
							"1013-05-15", readMessage, LayoutId_Other);
					msgList.add(newMessage);
					talkRecodeView.setAdapter(new ChatMsgViewAdapter(
							BluetoothChat.this, msgList));
					break;
				case MESSAGE_DEVICE_NAME:
					// save the connected device's name
					mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
					Toast.makeText(getApplicationContext(),
							"Connected to " + mConnectedDeviceName,
							Toast.LENGTH_SHORT).show();
					break;
				case MESSAGE_TOAST:
					Toast.makeText(getApplicationContext(),
							msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
							.show();
					break;
			}
		}
	};

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
							long arg3) {
		// TODO Auto-generated method stub
		imgPopupWindow.dismiss();
		String oldstr = messageEditext.getText().toString();
		String newstr = oldstr + "#{" + mFaceInfo.facesStr[position] + "}";
		SpannableStringBuilder ss = mFaceInfo.ImagistSpan(newstr);
		messageEditext.setText(ss);
		messageEditext.setSelection(ss.length());
	}

}
