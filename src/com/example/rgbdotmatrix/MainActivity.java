package com.example.rgbdotmatrix;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private static final String TAG = "BluetoothListActivity";
	private static final boolean DEBUG = true;

	// Message types sent from the BluetoothConnection Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothConnection Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	public static boolean sConnectStat = false;
	public static String mListItem = "list";
	
	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// BluetoothAdapter
	private BluetoothAdapter mBluetoothAdapter = null;
		
	private int mCurrentColor = Color.BLACK;
	private ProgressDialog mProgress;
	private final int LED_COUNT = 64;
	
	// Name of the connected device
	private String mConnectedDeviceName = null;
	
	// Bluetooth _Connection
	private static BluetoothConnection mConnection = null;

	private Button[] mBtnView = new Button[LED_COUNT];
	private int[] mDotColor = new int[LED_COUNT];
	
	private Button btnCurrentColor;
	
	private final ArrayList<String> mListName = new ArrayList<String>();

	private HashMap<String, int[]> mMatrixValues = new HashMap<String, int[]>();
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		
		mProgress = new ProgressDialog(MainActivity.this);
		mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgress.setTitle("");
		mProgress.setMessage("잠시만 기다려주세요...");
		
		
		// 기본적인 패턴 3가지를 리스트에 등록
		mMatrixValues.put("하트", Heart);
		mListName.add("하트");
		mMatrixValues.put("스마일", Smile);
		mListName.add("스마일");
		mMatrixValues.put("컬러풀", Colorful);
		mListName.add("컬러풀");

		DotViewInit();
		// 도트 칼라 초기화
		DotColorClear();

		// 현재 입력할 수 있는 색상을 표시하는 버튼
		btnCurrentColor = (Button) findViewById(R.id.btnCurrentColor);
		btnCurrentColor.setBackgroundColor(mCurrentColor);

		// 칼라 피커 다이얼로그 동작 버튼
		Button btnColorPicker = (Button) findViewById(R.id.btnColor);
		btnColorPicker.setOnClickListener(myOnButtonClickListener);

		// 리스트 저장 버튼 저장하기위환 다이얼로그 실핼
		Button btnSave = (Button) findViewById(R.id.btnSave);
		btnSave.setOnClickListener(myOnButtonClickListener);

		// 리스트 목록을 넣어 리스트 구현 버튼
		Button btnListActivity = (Button) findViewById(R.id.btnListActivity);
		btnListActivity.setOnClickListener(myOnButtonClickListener);

		// DotView 버튼과 mDotColor 초기화
		Button btnClear = (Button) findViewById(R.id.btnClear);
		btnClear.setOnClickListener(myOnButtonClickListener);

		// 리스트에서 선택된 리스트를 확인 리스트의 정보를 가지고 와 DotView에 구현
		final TextView txtListItem = (TextView) findViewById(R.id.txtListItem);
		txtListItem.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,int count) {
				int[] dotBuff = mMatrixValues.get(txtListItem.getText().toString());

				if (dotBuff != null) {
					for (int i = 0; i < dotBuff.length; i++)
						mDotColor[i] = dotBuff[i];
						DotColorSet();
				} else { /*Do Nothing!!! */ }
			}

			public void beforeTextChanged(CharSequence s, int start, int count,	int after) { /*Do nothing!!!*/ }
			public void afterTextChanged(Editable s) { /*Do nothing!!!*/ }
		});

		// 블루투스 DotView 데이터 전송
		Button btnSend = (Button) findViewById(R.id.btnSend);
		btnSend.setOnClickListener(myOnButtonClickListener);
		
	} //onCreate
	
	View.OnClickListener myOnButtonClickListener = new OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btnColor:
			{
				// 칼라 피커 다이얼로그 동작 버튼
				ColorPickerDialog dialog = new ColorPickerDialog(
						MainActivity.this, mCurrentColor, null,
						new ColorPickerDialog.OnColorChangedListener() {

							public void colorChanged(int color) {
								// TODO Auto-generated method stub
								mCurrentColor = color;
								btnCurrentColor.setBackgroundColor(color);
							}
						});
				dialog.show();
				break;
			}	
			case R.id.btnClear:					// DotView 버튼과 mDotColor 초기화
			{
				DotColorClear();
				break;
			}	
			case R.id.btnListActivity:			// 리스트 목록을 넣어 리스트 구현 버튼
			{
				Intent intent = new Intent(MainActivity.this,ListActivity.class);
				String[] result = (String[]) mListName.toArray(new String[mListName.size()]);
				intent.putExtra("list", result);
				startActivity(intent);
				break;
			}	
			case R.id.btnSave:					// 리스트 저장 버튼 저장하기위환 다이얼로그 실행
			{
				showDialog(0);
				break;
			}	
			case R.id.btnSend:					// 블루투스 DotView 데이터 전송
			{
				DotColorGet();
				byte[] sendData = ProtocolCreate.toIntsToBytes(mDotColor);
				sendMessage(sendData);
				break;
			}
			default:
				break;
			}
		}
	};

	// ViewButton 클릭 이벤트 클릭된 뷰의 BackgroundColor 변경
	View.OnClickListener myOnDotClickListener = new OnClickListener() {
		public void onClick(View v) {
			v.setBackgroundColor(mCurrentColor);
		}
	};


	public static void sendMessage(byte[] mMsg) {
		if (mConnection.getState() != BluetoothConnection.STATE_CONNECTED) {
			return;
		}
		mConnection.write(mMsg);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (mConnection != null) {
			mConnection.stop();
			sConnectStat = false;
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();


		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT); 
		} else {
			if (mConnection == null)
				setupConnection();
		}
	}

	private void setupConnection() {
		// Initialize the BluetoothConnection to perform bluetooth connections
		mConnection = new BluetoothConnection(this, mHandler);
	}


	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (DEBUG) {
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				}
				
				switch (msg.arg1) {
				case BluetoothConnection.STATE_CONNECTED:
					mProgress.dismiss();
					break;

				case BluetoothConnection.STATE_CONNECTING:
					mProgress.show();
					break;

				case BluetoothConnection.STATE_LISTEN:
					mProgress.dismiss();
					break;

				case BluetoothConnection.STATE_NONE:
					sConnectStat = false;
					mProgress.dismiss();
					break;
				}

				break;

			case MESSAGE_WRITE:
				break;
			case MESSAGE_READ:
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (DEBUG) {
			Log.d(TAG, "onActivityResult " + resultCode);
		}
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
				mConnection.connect(device);

				Log.d("TAG", "connect ture");
				// Set button to display current status
				sConnectStat = true;
			}
			break;

		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				setupConnection();
			} else {
				// User did not enable Bluetooth or an error occured
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, "Bluetooth_not_enabled_leaving",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}



	private void DotViewInit() {

		for(int i=0; i<8; i++) {
		   for(int j=0; j<8; j++) {
			    String buttonID = "view" + i + "" + j;
			    int resID = getResources().getIdentifier(buttonID, "id", "com.example.rgbdotmatrix");
			    
			    int k = i*8+j;
			    mBtnView[k] = ((Button) findViewById(resID));
			    mBtnView[k].setOnClickListener(myOnDotClickListener);
			    mBtnView[k].setBackgroundColor(mCurrentColor);
			    Log.e(TAG, "id->" + buttonID + "  k->" + k);
		   }
		}
		
	}

	
	public void DotColorClear() {
		
		for (int i = 0; i < mDotColor.length; i++) {
			mDotColor[i] = Color.BLACK;
			mBtnView[i].setBackgroundColor(mDotColor[i]);
		}
	}

	public int getColor(Button btn) {
		int color;
		btn.buildDrawingCache();
		Bitmap bitmap = btn.getDrawingCache();
		color = bitmap.getPixel(0,0);
		btn.destroyDrawingCache();
		return color;
	}

	public void DotColorGet() {
		for (int i = 0; i < mDotColor.length; i++)
		{
			mDotColor[i] = getColor(mBtnView[i]);
		}
	}

	
	public void DotColorSet() {
		for (int i = 0; i < mDotColor.length; i++)
		{
			mBtnView[i].setBackgroundColor(mDotColor[i]);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		// 화면 갱신시에 리스트에 선택된 리스트 표시
		TextView txtListItem = (TextView) findViewById(R.id.txtListItem);
		txtListItem.setText(mListItem);

		if (mConnection != null) {
			if (mConnection.getState() == BluetoothConnection.STATE_NONE) {
				mConnection.start();
			}
		}
	}

	/*
	 * @Override public boolean onCreateOptionsMenu(Menu menu) {
	 * getMenuInflater().inflate(R.menu.activity_main, menu); return true; }
	 */
	@Override
	protected Dialog onCreateDialog(int id, Bundle args) {
		Dialog saveDialog = new Dialog(this);

		saveDialog.setTitle("저장하시겠습니까?");
		View view = getLayoutInflater().inflate(R.layout.activity_save_dialog, null);
		saveDialog.setContentView(view);

		final EditText editSaveName = (EditText) saveDialog.findViewById(R.id.editFileName);

		Button btnDlgSave = (Button) saveDialog.findViewById(R.id.btnDlgSave);
		btnDlgSave.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				mListName.add(editSaveName.getText().toString());
				DotColorGet();
				int[] putbuf = new int[mDotColor.length];
				for (int i = 0; i < mDotColor.length; i++)
				{
					putbuf[i] = mDotColor[i];
				}
				mMatrixValues.put(editSaveName.getText().toString(), putbuf);	//TODO 시리얼라이즈 저장.
				editSaveName.setText("");
				dismissDialog(0);
			}
		});
		Button btnDlgCancle = (Button) saveDialog.findViewById(R.id.btnDlgCancle);
		btnDlgCancle.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				editSaveName.setText("");
				dismissDialog(0);
			}
		});

		return saveDialog;
	}

	
	//+++++ option menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 0, 0, "디바이스 연결");
		menu.add(0, 1, 0, "디바이스 해제");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 0:
			if (sConnectStat) {
				Toast.makeText(MainActivity.this, "현재 연결중입니다.", Toast.LENGTH_SHORT).show();
			} else {
				Intent intent = new Intent(MainActivity.this,DeviceListActivity.class);
				startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
			}

			break;

		case 1:
			if (sConnectStat) {
				mConnection.disconnect();
			}
			break;
		}
		return true;
	}
	
	//++++
	int[] Heart = new int[] { Color.BLACK, Color.RED, Color.RED, Color.BLACK,
			Color.BLACK, Color.RED, Color.RED, Color.BLACK, Color.RED,
			Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED,
			Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED,
			Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED,
			Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLACK,
			Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED,
			Color.BLACK, Color.BLACK, Color.RED, Color.RED, Color.RED,
			Color.RED, Color.RED, Color.RED, Color.BLACK, Color.BLACK,
			Color.BLACK, Color.RED, Color.RED, Color.RED, Color.RED,
			Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
			Color.RED, Color.RED, Color.BLACK, Color.BLACK, Color.BLACK };

	int[] Smile = new int[] { Color.BLACK, Color.BLACK, Color.BLACK,
			Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
			Color.BLACK, Color.YELLOW, Color.BLACK, Color.BLACK, Color.BLACK,
			Color.BLACK, Color.YELLOW, Color.BLACK, Color.YELLOW, Color.BLACK,
			Color.YELLOW, Color.BLACK, Color.BLACK, Color.YELLOW, Color.BLACK,
			Color.YELLOW, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
			Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
			Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
			Color.BLACK, Color.BLACK, Color.BLACK, Color.YELLOW, Color.BLACK,
			Color.BLACK, Color.BLACK, Color.BLACK, Color.YELLOW, Color.BLACK,
			Color.BLACK, Color.BLACK, Color.YELLOW, Color.YELLOW, Color.YELLOW,
			Color.YELLOW, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
			Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
			Color.BLACK };

	int[] Colorful = new int[] { Color.BLACK, Color.BLUE, Color.CYAN,
			Color.DKGRAY, Color.GRAY, Color.GREEN, Color.LTGRAY, Color.MAGENTA,
			Color.RED, Color.TRANSPARENT, Color.WHITE, Color.YELLOW,
			Color.BLACK, Color.BLUE, Color.CYAN, Color.DKGRAY, Color.BLACK,
			Color.BLUE, Color.CYAN, Color.DKGRAY, Color.GRAY, Color.GREEN,
			Color.LTGRAY, Color.MAGENTA, Color.RED, Color.TRANSPARENT,
			Color.WHITE, Color.YELLOW, Color.BLACK, Color.BLUE, Color.CYAN,
			Color.DKGRAY, Color.BLACK, Color.BLUE, Color.CYAN, Color.DKGRAY,
			Color.GRAY, Color.GREEN, Color.LTGRAY, Color.MAGENTA, Color.RED,
			Color.TRANSPARENT, Color.WHITE, Color.YELLOW, Color.BLACK,
			Color.BLUE, Color.CYAN, Color.DKGRAY, Color.BLACK, Color.BLUE,
			Color.CYAN, Color.DKGRAY, Color.GRAY, Color.GREEN, Color.LTGRAY,
			Color.MAGENTA, Color.RED, Color.TRANSPARENT, Color.WHITE,
			Color.YELLOW, Color.BLACK, Color.BLUE, Color.CYAN, Color.DKGRAY };
} //end of class
