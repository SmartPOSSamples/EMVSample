package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.cloudpos.OperationListener;
import com.cloudpos.OperationResult;
import com.cloudpos.jniinterface.IFuntionListener;
import com.cloudpos.msr.MSROperationResult;
import com.cloudpos.msr.MSRTrackData;
import com.smartpos.emvsample.MainApp;
import com.smartpos.emvsample.R;
import com.smartpos.emvsample.constant.EMVConstant;
import com.smartpos.emvsample.transaction.TransDefine;
import com.smartpos.util.ByteUtil;
import com.smartpos.util.NumberUtil;
import com.smartpos.util.StringUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import static android.content.ContentValues.TAG;
import static com.cloudpos.jniinterface.EMVJNIInterface.close_reader;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_aidparam_add;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_aidparam_clear;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_capkparam_add;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_capkparam_clear;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_exception_file_add;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_exception_file_clear;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_preprocess_qpboc;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_revoked_cert_add;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_revoked_cert_clear;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_anti_shake;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_trans_amount;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_terminal_param_set_tlv;
import static com.cloudpos.jniinterface.EMVJNIInterface.get_card_type;
import static com.cloudpos.jniinterface.EMVJNIInterface.open_reader;

public class FuncActivity extends Activity implements EMVConstant, IFuntionListener
{
	protected static WeakReferenceHandler mHandler = new WeakReferenceHandler(null);
	protected static Socket socket = null;
	private static DataOutputStream dos;
	private static DataInputStream dis;
	protected static byte[] commRequestData = new byte[1024];
	protected static int commRequestDataLength = 0;
	protected static byte[] commResponseData = new byte[1024];
	protected static int commResponseDataLength = 0;
	protected static boolean commThreadRun = false;

	protected static FuncActivity funcRef;
	protected static MainApp appState = null;

	protected static boolean contactOpened = false;
	protected static boolean contactlessOpened = false;

	OperationListener msrListener = null;

	protected static Thread mOpenPinpadThread = null;

	private Timer mTimerSeconds;
	private int mIntIdleSeconds;
	private boolean mBoolInitialized=false;
	private byte mTimerMode = 0;
	private int idleTimer = DEFAULT_IDLE_TIME_SECONDS;

	private final ReentrantLock commLock = new ReentrantLock();

	public void handleMessageSafe(Message msg){}

	protected static class WeakReferenceHandler extends Handler{

		private WeakReference<FuncActivity> mActivity;
		public WeakReferenceHandler(FuncActivity activity){
			mActivity = new WeakReference<FuncActivity>(activity);
		}

		public void setFunActivity(FuncActivity activity){
			mActivity = new WeakReference<FuncActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			FuncActivity activity = mActivity.get();
			if(activity != null){
				activity.handleMessageSafe(msg);
			}
		}
	}

	public void capkChecksumErrorDialog(Context context)
	{
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle("提示");
		builder.setMessage("CAPK:" + appState.failedCAPKInfo + "\nChecksum Error");

		builder.setPositiveButton("确认", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	public void emvProcessCallback(byte[] data)
	{
		if(debug)Log.d(APP_TAG, "emvProcessNextCompleted");
		appState.trans.setEMVStatus(data[0]);
		appState.trans.setEMVRetCode(data[1]);

		Message msg = new Message();
		msg.what = EMV_PROCESS_NEXT_COMPLETED_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	public void processSignalCallBack(int type, byte[] data, int length){
	}

	public void cardEventOccured(int eventType)
	{
		if(debug)Log.d(APP_TAG, "get cardEventOccured");
		Message msg = new Message();
		if(eventType == SMART_CARD_EVENT_INSERT_CARD)
		{
			appState.cardType = get_card_type();
			if(debug)Log.d(APP_TAG, "cardType = " + appState.cardType);

			if(appState.cardType == CARD_CONTACT)
			{
				msg.what = CARD_INSERT_NOTIFIER;
				mHandler.sendMessage(msg);
			}
			else if(appState.cardType == CARD_CONTACTLESS)
			{
				msg.what = CARD_TAPED_NOTIFIER;
				mHandler.sendMessage(msg);
			}
			else{
				appState.cardType = -1;
			}
		}
		else if(eventType == SMART_CARD_EVENT_POWERON_ERROR)
		{
			appState.cardType = -1;
			msg.what = CARD_ERROR_NOTIFIER;
			mHandler.sendMessage(msg);
		}
		else if(eventType == SMART_CARD_EVENT_REMOVE_CARD)
		{
			if(debug)Log.d(APP_TAG, "card Removed");
			appState.cardType = -1;
		}
		else if(eventType == SMART_CARD_EVENT_CONTALESS_HAVE_MORE_CARD)
		{
			appState.cardType = -1;
			msg.what = CONTACTLESS_HAVE_MORE_CARD_NOTIFIER;
			mHandler.sendMessage(msg);
		}
		else if(eventType == SMART_CARD_EVENT_CONTALESS_ANTI_SHAKE){
			msg.what = CARD_CONTACTLESS_ANTISHAKE;
			mHandler.sendMessage(msg);
		}

	}

	// This pinpad callback is from emv kernel
	public void pinpadCallback(int nCount, int nExtra) {

		byte[] tempByte = new byte[2];
		tempByte[0] = (byte)(nCount & 0xFF);
		tempByte[1] = (byte)(nExtra & 0xFF);
		if (debug) Log.d(TAG, ByteUtil.arrayToHexStr("EMV OFFLINE pinpadCallback: ", tempByte, 0, tempByte.length));
		Message msg = new Message();
		msg.what = OFFLINE_PIN_NOTIFIER;
		msg.arg1 = nCount;
		msg.arg2 = nExtra;
		mHandler.sendMessage(msg);
	}

	public void setEMVTermInfo()
	{
		byte[] termInfo = new byte[256];
		int offset = 0;
		// 5F2A: Transaction Currency Code
		termInfo[offset] = (byte)0x5F;
		termInfo[offset+1] = 0x2A;
		termInfo[offset+2] = 2;
		offset += 3;
		System.arraycopy(StringUtil.hexString2bytes(appState.terminalConfig.getCurrencyCode()),
			0, termInfo, offset, 2);
		offset += 2;
		// 5F36: Transaction Currency Exponent
		termInfo[offset] = (byte)0x5F;
		termInfo[offset+1] = 0x36;
		termInfo[offset+2] = 1;
		termInfo[offset+3] = appState.terminalConfig.getCurrencyExponent();
		offset += 4;
		// 9F16: Merchant Identification
		if (appState.terminalConfig.getMID().length() == 15) {
			termInfo[offset] = (byte)0x9F;
			termInfo[offset+1] = 0x16;
			termInfo[offset+2] = 15;
			offset += 3;
			System.arraycopy(appState.terminalConfig.getMID().getBytes(), 0, termInfo, offset, 15);
			offset += 15;
		}
		// 9F1A: Terminal Country Code
		termInfo[offset] = (byte)0x9F;
		termInfo[offset+1] = 0x1A;
		termInfo[offset+2] = 2;
		offset += 3;
		System.arraycopy(StringUtil.hexString2bytes(appState.terminalConfig.getCountryCode()),
			0, termInfo, offset, 2);
		offset += 2;
		// 9F1C: Terminal Identification
		if (appState.terminalConfig.getTID().length() == 8) {
			termInfo[offset] = (byte)0x9F;
			termInfo[offset+1] = 0x1C;
			termInfo[offset+2] = 8;
			offset += 3;
			System.arraycopy(appState.terminalConfig.getTID().getBytes(), 0, termInfo, offset, 8);
			offset += 8;
		}
		// 9F1E: IFD Serial Number
		String ifd = android.os.Build.SERIAL;
		if(ifd.length() > 0)
		{
			termInfo[offset] = (byte) 0x9F;
			termInfo[offset + 1] = 0x1E;
			termInfo[offset + 2] = (byte) ifd.length();
			offset += 3;
			System.arraycopy(ifd.getBytes(), 0, termInfo, offset, ifd.length());
			offset += ifd.length();
		}
		// 9F33: Terminal Capabilities
		termInfo[offset] = (byte)0x9F;
		termInfo[offset+1] = 0x33;
		termInfo[offset+2] = 3;
		offset += 3;
		System.arraycopy(StringUtil.hexString2bytes(appState.terminalConfig.getTerminalCapabilities()),
			0, termInfo, offset, 3);
		offset += 3;
		// 9F35: Terminal Type
		termInfo[offset] = (byte)0x9F;
		termInfo[offset+1] = 0x35;
		termInfo[offset+2] = 1;
		termInfo[offset+3] = StringUtil.hexString2bytes(appState.terminalConfig.getTerminalType())[0];
		offset += 4;
		// 9F40: Additional Terminal Capabilities
		termInfo[offset] = (byte)0x9F;
		termInfo[offset+1] = 0x40;
		termInfo[offset+2] = 5;
		offset += 3;
		System.arraycopy(StringUtil.hexString2bytes(appState.terminalConfig.getAdditionalTerminalCapabilities()),
			0, termInfo, offset, 5);
		offset += 5;
		// 9F4E: Merchant Name and Location
		int merNameLength = appState.terminalConfig.getMerchantName1().length();
		if (merNameLength > 0) {
			termInfo[offset] = (byte)0x9F;
			termInfo[offset+1] = 0x4E;
			termInfo[offset+2] = (byte)merNameLength;
			offset += 3;
			System.arraycopy(appState.terminalConfig.getMerchantName1().getBytes(), 0, termInfo, offset, merNameLength);
			offset += merNameLength;
		}
		// 9F66: TTQ first byte
		termInfo[offset] = (byte)0x9F;
		termInfo[offset+1] = 0x66;
		termInfo[offset+2] = 1;
		termInfo[offset+3] = appState.terminalConfig.getTTQ();
		offset += 4;
		// DF19: Contactless floor limit
		if(appState.terminalConfig.getContactlessFloorLimit() >= 0)
		{
			termInfo[offset] = (byte)0xDF;
			termInfo[offset+1] = 0x19;
			termInfo[offset+2] = 6;
			offset += 3;
			System.arraycopy(NumberUtil.intToBcd(appState.terminalConfig.getContactlessFloorLimit(), 6),
				0, termInfo, offset, 6);
			offset += 6;
		}
		// DF20: Contactless transaction limit
		if(appState.terminalConfig.getContactlessLimit() >= 0)
		{
			termInfo[offset] = (byte)0xDF;
			termInfo[offset+1] = 0x20;
			termInfo[offset+2] = 6;
			offset += 3;
			System.arraycopy(NumberUtil.intToBcd(appState.terminalConfig.getContactlessLimit(), 6),
				0, termInfo, offset, 6);
			offset += 6;
		}
		// DF21: CVM limit
		if(appState.terminalConfig.getCvmLimit() >= 0)
		{
			termInfo[offset] = (byte)0xDF;
			termInfo[offset+1] = 0x21;
			termInfo[offset+2] = 6;
			offset += 3;
			System.arraycopy(NumberUtil.intToBcd(appState.terminalConfig.getCvmLimit(), 6),
				0, termInfo, offset, 6);
			offset += 6;
		}
		// EF01: Status check support
		termInfo[offset] = (byte)0xEF;
		termInfo[offset+1] = 0x01;
		termInfo[offset+2] = 1;
		termInfo[offset+3] = appState.terminalConfig.getStatusCheckSupport();
		offset += 4;

		emv_terminal_param_set_tlv(termInfo, offset);
	}

	void setEMVTransAmount(String strAmt)
	{
		byte[] amt = new byte[strAmt.length() + 1];
		System.arraycopy(strAmt.getBytes(), 0, amt, 0, strAmt.length());
		emv_set_trans_amount(amt);
	}


	public static boolean loadAID()
	{
		appState.aids = appState.aidService.query();
		emv_aidparam_clear();
		byte[] aidInfo = null;
		for(int i=0; i< appState.aids.length; i++)
		{
			if(appState.aids[i] != null)
			{
				aidInfo = appState.aids[i].getDataBuffer();
				if(emv_aidparam_add(aidInfo, aidInfo.length) < 0)
					return false;
			}
			else
			{
				break;
			}
		}
		return true;
	}

	public static int loadCAPK()
	{
		appState.capks = appState.capkService.query();
		emv_capkparam_clear();
		byte[] capkInfo = null;
		for(int i=0; i< appState.capks.length; i++)
		{
			if(appState.capks[i] != null)
			{
				capkInfo = appState.capks[i].getDataBuffer();
				int ret = emv_capkparam_add(capkInfo, capkInfo.length);
				if( ret < 0)
				{
					appState.failedCAPKInfo = appState.capks[i].getRID() + "_" + appState.capks[i].getCapki();
					return ret;
				}
			}
			else
			{
				break;
			}
		}
		return 0;
	}

	public static boolean loadExceptionFile()
	{
		appState.exceptionFiles = appState.exceptionFileService.query();
		emv_exception_file_clear();
		byte[] exceptionFileInfo = null;
		for(int i=0; i< appState.exceptionFiles.length; i++)
		{
			if(appState.exceptionFiles[i] != null)
			{
				exceptionFileInfo = appState.exceptionFiles[i].getDataBuffer();
				if(emv_exception_file_add(exceptionFileInfo) < 0)
					return false;
			}
			else
			{
				break;
			}
		}
		return true;
	}

	public static boolean loadRevokedCAPK()
	{
		appState.revokedCapks = appState.revokedCAPKService.query();
		emv_revoked_cert_clear();
		byte[] revokedCAPKInfo = null;
		for(int i=0; i< appState.revokedCapks.length; i++)
		{
			if(appState.revokedCapks[i] != null)
			{
				revokedCAPKInfo = appState.revokedCapks[i].getDataBuffer();
				if(revokedCAPKInfo != null)
				{
					if(emv_revoked_cert_add(revokedCAPKInfo) < 0)
						return false;
				}
			}
			else
			{
				break;
			}
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		appState = ((MainApp)getApplicationContext());
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	public void onTouch()
	{
		//if(debug)Log.d(APP_TAG, "mIntIdleSeconds = 0");
		mIntIdleSeconds=0;
	}

	public void cancelIdleTimer()
	{
		mIntIdleSeconds=0;
		if (mTimerSeconds != null)
		{
			if(debug)Log.d(APP_TAG, "timer cancelled");
			mTimerSeconds.cancel();
		}
	}

	public void startIdleTimer(byte timerMode, int timerSecond)
	{
		idleTimer = timerSecond;
		mTimerMode = timerMode;
		//initialize idle counter
		mIntIdleSeconds=0;
		if (mBoolInitialized == false)
		{
			if(debug)Log.d(APP_TAG, "timer start");
			mBoolInitialized = true;
			//create timer to tick every second
			mTimerSeconds = new Timer();
			mTimerSeconds.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					mIntIdleSeconds++;
					if (mIntIdleSeconds == idleTimer)
					{
						if(mTimerMode == TIMER_IDLE)
						{
							go2Idle();
						}
						else
						{
							if(appState.needClearPinpad == true)
							{
								// clear pinpad
								appState.needClearPinpad = false;

								appState.pinpadService.open();
								appState.pinpadService.setText(0, " ");
								appState.pinpadService.setText(1, " ");

								appState.pinpadService.close();
								appState.pinpadOpened = false;
							}

							setResult(Activity.RESULT_CANCELED, getIntent());
							exit();
						}
					}
				}
			}, 0, 1000);
		}
	}

	protected boolean connectSocket(String ip, int port, int timeout)
	{
		try {
			socket = new Socket();
			socket.setSoTimeout(timeout); // 设置读超时
			SocketAddress remoteAddr = new InetSocketAddress(ip, port);
			if(debug)
			{
				Log.d(APP_TAG, "Connect IP[" + ip + "]port[" + port + "]");
			}
			socket.connect(remoteAddr, timeout);
		} catch (UnknownHostException e) {

		} catch (IOException e) {

		}
		if(socket!= null && socket.isConnected())
		{
			return true;
		}
		return false;
	}

	protected void disconnectSocket()
	{
		if(debug)Log.d(APP_TAG, "disconnectSocket");
		try {
			if(socket != null)
			{
				socket.close();
			}
		} catch (IOException e) {

		}
	}


	protected synchronized void readAllCard()
	{
		if(appState.acceptMSR && additionalMag == false)
		{
			startMSR();
		}

		if(appState.acceptContactCard)
		{
			contactOpened = true;
			open_reader(1);
		}
		if(appState.acceptContactlessCard)
		{
			contactlessOpened = true;
			emv_set_anti_shake(enableAntiShake?1:0);
			open_reader(2);
		}
	}

	protected void waitContactCard()
	{
		contactOpened = true;
		open_reader(1);
	}

	protected void cancelAllCard()
	{
		cancelMSRThread();
		cancelContactCard();
		cancelContactlessCard();
	}

	protected void cancelContactCard()
	{
		if(debug)Log.d(APP_TAG, "Close contact card");
		if(contactOpened)
		{
			contactOpened = false;
			close_reader(1);
		}
	}


	protected void cancelContactlessCard()
	{
		if(contactlessOpened)
		{
			contactlessOpened = false;
			close_reader(2);
		}
	}

	private void notifyContactlessCardOpenError()
	{
		Message msg = new Message();
		msg.what = CARD_OPEN_ERROR_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	protected void startMSR()
	{
		appState.msrPollResult = -1;
		appState.msrError = false;
		if(msrListener==null){
			msrListener = new OperationListener() {
				@Override
				public void handleResult(OperationResult operationResult) {
					appState.msrPollResult = 1;
					if (operationResult.getResultCode() == OperationResult.SUCCESS) {
						MSRTrackData data = ((MSROperationResult) operationResult).getMSRTrackData();
						// Reading Track2
						if(data.getTrackError(1) == MSRTrackData.NO_ERROR && read_track2_data(data.getTrackData(1))){
							// Reading track1 & track3
							if(data.getTrackError(2) == MSRTrackData.NO_ERROR)
								read_track3_data(data.getTrackData(2));
							if(data.getTrackError(0) == MSRTrackData.NO_ERROR)
							{
								read_track1_data(data.getTrackData(0));
							}
							notifyMSR();
						}else{
							notifyMsrReadError();
						}
					}else if(operationResult.getResultCode() == OperationResult.CANCEL){

					} else {
						notifyMsrReadError();
					}

				}
			};
		}

		if(appState.msrService.open()){
			appState.msrService.startRedMSR(msrListener);
		}else{
			notifyMsrOpenError();
		}
	}

	protected void cancelMSRThread()
	{
		appState.msrService.cancelRequest();
		appState.msrService.close();
	}

	protected void notifyMSR()
	{
		Message msg = new Message();
		msg.what = MSR_READ_DATA_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	protected void notifyMsrOpenError()
	{
		Message msg = new Message();
		msg.what = MSR_OPEN_ERROR_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	protected void notifyMsrReadError()
	{
		Message msg = new Message();
		msg.what = MSR_READ_ERROR_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	protected boolean read_track2_data(byte[] byteArry)
	{
		if(debug)Log.d(APP_TAG, "read_track2_data");
		int trackDatalength = byteArry.length;
		if(debug)
		{
			String strDebug = "";
			for(int i=0; i<trackDatalength; i++)
				strDebug += String.format("%02X ", byteArry[i]);
			Log.d(APP_TAG, "track2 Data: " + strDebug);
		}
		if(trackDatalength > 0)
		{
			if(   trackDatalength > 37
				|| trackDatalength < 21
			)
			{
				return false;
			}

			int panStart = -1;
			int panEnd = -1;
			for (int i = 0; i < trackDatalength; i++)
			{
				if (byteArry[i] >= (byte) '0' && byteArry[i] <= (byte) '9')
				{
					if( panStart == -1)
					{
						panStart = i;
					}
				}
				else if (byteArry[i] == (byte) '=')
				{
					/* Field separator */
					panEnd = i;
					break;
				}
				else
				{
					panStart = -1;
					panEnd = -1;
					break;
				}
			}
			if (panEnd == -1 || panStart == -1)
			{
				return false;
			}
			appState.trans.setPAN(new String(byteArry, panStart, panEnd - panStart));
			appState.trans.setExpiry(new String(byteArry, panEnd + 1, 4));
			appState.trans.setServiceCode(new String(byteArry, panEnd + 5, 3));
			appState.trans.setTrack2Data(byteArry, 0, trackDatalength);
			appState.trans.setCardEntryMode(SWIPE_ENTRY);
			return true;
		}
		return false;
	}

	protected void read_track3_data(byte[] byteArry)
	{
		if(debug)Log.d(APP_TAG, "read_track3_data");
		int trackDatalength = byteArry.length;
		if(debug)
		{
			String strDebug = "";
			for(int i=0; i<trackDatalength; i++)
				strDebug += String.format("%02X ", byteArry[i]);
			Log.d(APP_TAG, "track3 Data: " + strDebug);
		}
		if(trackDatalength > 0)
		{
			appState.trans.setTrack3Data(byteArry, 0, trackDatalength);
		}
	}

	protected void read_track1_data(byte[] byteArry)
	{
		int nameStart = -1;
		int nameEnd = -1;

		if (byteArry.length > 0)
		{
			for (int i = 0; i < byteArry.length; i++)
			{
				if (byteArry[i] == (byte) '^') {
					/* Field separator */
					if (nameStart == -1) {
						nameStart = i + 1;
					} else {
						nameEnd = i;
						break;
					}
				}
			}
			if (nameEnd != -1)
			{
				appState.trans.setCardHolderName(new String(byteArry, nameStart, nameEnd - nameStart).trim());
			}
			appState.trans.setTrack1Data(new String(byteArry, 0, byteArry.length));
		}
	}


	protected void offlineSuccess()
	{
		transSuccess();
	}

	public void saveTrans()
	{
		if(debug)Log.d(APP_TAG, "save trans");
		appState.transDetailService.save(appState.trans);
	}

	public void saveAdvice()
	{
		if(debug)Log.d(APP_TAG, "save advice");
		appState.adviceService.save(appState.trans);
	}

	public void clearTrans()
	{
		appState.transDetailService.clearTable();
	}

	public void clearAdvice()
	{
		appState.adviceService.clearTable();
	}

	public void transSuccess()
	{
		if(appState.getTranType() != TRAN_SETTLE)
		{
			if ((TransDefine.transInfo[appState.getTranType()].flag & T_NOCAPTURE) == 0)
			{
				saveTrans();
				appState.batchInfo.incSale(appState.trans.getTransAmount());
			}
		}
	}

	public void exit()
	{
		cancelIdleTimer();
		finish();
	}

	public void exitTrans()
	{
		cancelContactlessCard();
		cancelMSRThread();
		if(appState.cardType == CARD_CONTACT)
		{
			removeCard();
		}
		else
		{
			appState.initData();
			finish();
		}
	}

	// ilde
	public void go2Idle()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, IdleActivity.class);
		startActivity(intent);
	}

	public void go2Error(int errorCode)
	{
		cancelIdleTimer();
		appState.setErrorCode(errorCode);
		Intent intent = new Intent(this, ErrorActivity.class);
		startActivity(intent);
	}

	// menu
	public void requestFuncMenu()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, FuncMenuActivity.class);
		startActivity(intent);
	}

	public void requestEnquireTrans()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, EnquireTransActivity.class);
		startActivity(intent);
	}

	public void showLastPBOC()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, ShowLastPBOCActivity.class);
		startActivity(intent);
	}

	// trans flow For Result
	public void requestCard(boolean acceptMSR, boolean acceptContact, boolean acceptContactless)
	{
		cancelIdleTimer();
		appState.setState(STATE_REQUEST_CARD);
		if(appState.msrError == false)
		{
			appState.acceptMSR = acceptMSR;
		}
		else{
			appState.acceptMSR = false;
		}
		appState.acceptContactCard = acceptContact;
		appState.acceptContactlessCard = acceptContactless;
		Intent intent = new Intent(this, RequestCardActivity.class);
		startActivityForResult(intent, appState.getState());
	}

	public void removeCard()
	{
		cancelIdleTimer();
		appState.setState(STATE_REMOVE_CARD);
		Intent intent = new Intent(this, RemoveCardActivity.class);
		startActivityForResult(intent, appState.getState());
	}

	public void confirmCard()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, ConfirmCardActivity.class);
		startActivityForResult(intent, STATE_CONFIRM_CARD);
	}

	public void inputAmount()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, InputAmountActivity.class);
		startActivityForResult(intent, STATE_INPUT_AMOUNT);
	}

	public void inputOnlinePIN()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, InputOnlinePINActivity.class);
		startActivityForResult(intent, STATE_INPUT_ONLINE_PIN);
	}

	public void inputOfflinePIN()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, InputOfflinePINActivity.class);
		startActivityForResult(intent, STATE_INPUT_OFFLINE_PIN);
	}

	public void processOnline()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, ProcessOnlineActivity.class);
		startActivityForResult(intent, STATE_PROCESS_ONLINE);
	}

	public void selectEMVAppList()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, SelectEMVAppListActivity.class);
		startActivityForResult(intent, STATE_SELECT_EMV_APP);
	}

	public void showPBOCCardRecord()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, ShowPBOCCardRecordActivity.class);
		startActivityForResult(intent, STATE_SHOW_EMV_CARD_TRANS);
	}

	public void showTransInfo()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, ShowTransInfoActivity.class);
		startActivityForResult(intent, STATE_SHOW_TRANS_INFO);
	}

	public void processEMVCard()
	{
		Intent intent = new Intent(this, ProcessEMVCardActivity.class);
		startActivityForResult(intent, STATE_PROCESS_EMV_CARD);
	}

	public void showTransResult()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, TransResultActivity.class);
		startActivityForResult(intent, STATE_TRANS_END);
	}

	// Trans Object
	public void sale()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, Sale.class);
		startActivity(intent);
	}

	public void balance()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, Balance.class);
		startActivity(intent);
	}

	public void saleInsertCardFirst()
	{
		appState.getPanBeforeAmount = true;
		cancelIdleTimer();
		Intent intent = new Intent(this, SaleInsertCardFirst.class);
		startActivity(intent);
	}

	public void settle()
	{
		cancelIdleTimer();
		Intent intent = new Intent(this, Settle.class);
		startActivity(intent);
	}

	public void queryCardRecord(byte recordType)
	{
		appState.recordType = recordType;
		cancelIdleTimer();
		Intent intent = new Intent(this, QueryCardRecord.class);
		startActivity(intent);
	}
	//=============== Q1 keyboard =============
	protected void onEnter()
	{
	}

	protected void onCancel()
	{
	}

	protected void onBack()
	{
	}

	protected void onDel()
	{
	}

	protected void onKeyCode(char key)
	{}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(debug) Log.i("FuncActivity", "onKeyDown:"+keyCode);
		onTouch();
		switch (keyCode)
		{
		case KeyEvent.KEYCODE_BACK:
			onBack();
			break;
		case KeyEvent.KEYCODE_ESCAPE:
			onCancel();
			break;
		case KeyEvent.KEYCODE_DEL:
			onDel();
			break;
		case KeyEvent.KEYCODE_ENTER:
			onEnter();
			break;
		case 232://'.'
			onKeyCode('.');
			break;
		default:
			onKeyCode((char) ('0'+(keyCode-KeyEvent.KEYCODE_0)));
			break;
		}
		return true;
	}
	//=============== Q1 keyboard =============

	protected boolean preProcessQpboc()
	{
		//pre-process
		int res = emv_preprocess_qpboc();
		if(res < 0)
		{
			if(res == -2)
			{
				appState.setErrorCode(R.string.error_amount_zero);
			}
			else
			{
				appState.setErrorCode(R.string.error_amount_over_limit);
			}
			return false;
		}
		return true;
	}

	class CommThread extends Thread {
		private int commOpen() {
			commLock.lock();
			int ret = -1;
			socket = new Socket();
			try {
				Log.i(APP_TAG, "socket connect " + appState.terminalConfig.getPrimaryHostIP() + " " + appState.terminalConfig.getPrimaryHostPort());
				socket.setSoTimeout(5000);
				SocketAddress remoteAddr = new InetSocketAddress(appState.terminalConfig.getPrimaryHostIP(), appState.terminalConfig.getPrimaryHostPort());
				socket.setReuseAddress(true);
				socket.connect(remoteAddr, 5000);    // timeout
				dos = new DataOutputStream(socket.getOutputStream());
				dis = new DataInputStream(socket.getInputStream());
				ret = 0;
				Log.i(APP_TAG, "socket local:" + socket.getLocalSocketAddress());
			} catch (Exception e) {
				ret = -1;
				e.printStackTrace();
			}

			commLock.unlock();
			return ret;
		}


		private int commRead(byte[] buffer, int expectedLen) {
			int ret = -1;

			byte[] tmpBuff = new byte[1];
			ret = expectedLen;
			for (int i = 0; i < expectedLen; i++) {
				try {
					int tmp = dis.read(tmpBuff);
					if (tmp > 0) {
						buffer[i] = tmpBuff[0];
					}
				} catch (Exception e) {
					e.printStackTrace();
					ret = -1;
					break;
				}
			}

			return ret;
		}

		private void commWrite(byte[] data, int len) {
			try {
				dos.write(data, 0, len);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void run() {
			commThreadRun = true;

			byte[] byte2 = new byte[2];
			int ret;

			super.run();

			if (debug) Log.d(APP_TAG, "CommThread Start");
			if (appState.getCommState() != COMM_OPENED) {
				ret = commOpen();

				if (ret >= 0) {
					appState.setCommState(COMM_OPENED);
				} else {
					commThreadRun = false;
					notifyCommError();
					return;
				}
			}
			packCommData();
			commWrite(commRequestData, commRequestDataLength);
			// ReadResp
			commResponseDataLength = 0;
			ret = commRead(commResponseData, 4);
			if (ret >= 0 && commResponseData[0] == 0x02) {
				if (debug) Log.d(APP_TAG, "Not get STX");
				System.arraycopy(commResponseData, 2, byte2, 0, 2);
				commResponseDataLength = NumberUtil.byte2ToShort(byte2);
				if (commResponseDataLength > 0) {
					ret = commRead(commResponseData, commResponseDataLength);
					if (ret < 0) {
						if (debug) Log.d(APP_TAG, "commRead data timeout");
						commThreadRun = false;

						commClose();
						notifyCommError();
						return;
					}
				}
				commThreadRun = false;
				commClose();
				Log.i(APP_TAG, "comm read:" + ByteUtil.arrayToHexStr(commResponseData, commResponseDataLength));
				notifyCommReadData();
				return;
			} else {
				if (debug) Log.d(APP_TAG, "commRead STX timeout");
				commThreadRun = false;
				commClose();
				notifyCommError();
				return;
			}
		}
	}

	void packCommData() {
		short dataLength = 0;
		byte[] data = new byte[1024];
		commRequestDataLength = 0;

		commRequestData[commRequestDataLength] = 0x02;
		commRequestDataLength++;
		byte msgType = 0x00;
		switch (appState.getProcessState()){
		case PROCESS_NORMAL:
			msgType = (byte) 0x41;
			if(appState.trans.getPinBlock() != null){
				System.arraycopy(new byte[]{(byte) 0x99, 0x08}, 0, data, dataLength, 2);
				dataLength += 2;
				System.arraycopy(appState.trans.getPinBlock(), 0, data, dataLength, 8);
				dataLength += 8;
			}
			if(appState.trans.getCardEntryMode() == SWIPE_ENTRY && appState.trans.getEmvCardError() == true){
				System.arraycopy(new byte[]{(byte) 0x9F, 0x39, 0x01, (byte) 0x80}, 0, data, dataLength, 4);
			}else{
				System.arraycopy(new byte[]{(byte) 0x9F, 0x39, 0x01, appState.trans.getCardEntryMode()}, 0, data, dataLength, 4);
			}
			dataLength += 4;
			if(TextUtils.isEmpty(appState.trans.getTrack2Data()) == false
				&& appState.trans.getCardEntryMode() != INSERT_ENTRY
			){
				System.arraycopy(new byte[]{(byte) 0xDF, (byte) 0x81, 0x07}, 0, data, dataLength, 3);
				dataLength += 3;
				byte track2DataLength = (byte) ((appState.trans.getTrack2Data().length() + 1) / 2);
				data[dataLength] = track2DataLength;
				dataLength++;
				ByteUtil.asciiToBCD(appState.trans.getTrack2Data().getBytes(), 0, data, dataLength, appState.trans.getTrack2Data().length(), 0);
				dataLength += track2DataLength;
			}

			int iccDataLength = appState.trans.getICCData().length();
			if(iccDataLength > 0){
				System.arraycopy(StringUtil.hexString2bytes(appState.trans.getICCData()), 0, data, dataLength, iccDataLength / 2);
				dataLength += iccDataLength / 2;
			}
			break;

		case PROCESS_CONFIMATION:
		case PROCESS_BATCH:
			if(appState.getProcessState() == PROCESS_BATCH){
				msgType = (byte) 0x44;
			}else{
				msgType = (byte) 0x43;
			}
			System.arraycopy(new byte[]{(byte) 0x9F, 0x39, 0x01, appState.trans.getCardEntryMode()}, 0, data, dataLength, 4);
			dataLength += 4;
			if(TextUtils.isEmpty(appState.trans.getTrack2Data()) == false
				&& appState.trans.getCardEntryMode() != INSERT_ENTRY
			){
				System.arraycopy(new byte[]{(byte) 0xDF, (byte) 0x81, 0x07}, 0, data, dataLength, 3);
				dataLength += 3;
				byte track2DataLength = (byte) ((appState.trans.getTrack2Data().length() + 1) / 2);
				data[dataLength] = track2DataLength;
				dataLength++;
				ByteUtil.asciiToBCD(appState.trans.getTrack2Data().getBytes(), 0, data, dataLength, appState.trans.getTrack2Data().length(), 0);
				dataLength += track2DataLength;
			}

			iccDataLength = appState.trans.getICCData().length();
			if(iccDataLength > 0){
				System.arraycopy(StringUtil.hexString2bytes(appState.trans.getICCData()), 0, data, dataLength, iccDataLength / 2);
				dataLength += iccDataLength / 2;
			}else{
				if(debug)
					Log.e(APP_TAG, "icc Data is null");
			}
			byte transResult = 0x01;
			System.arraycopy(new byte[]{0x03, 0x01, transResult}, 0, data, dataLength, 3);
			dataLength += 3;
			break;
		case PROCESS_ADVICE_OFFLINE:
		case PROCESS_ADVICE_ONLINE:
			msgType = (byte) 0x45;
			iccDataLength = appState.trans.getICCData().length();
			if(iccDataLength > 0){
				System.arraycopy(StringUtil.hexString2bytes(appState.trans.getICCData()), 0, data, dataLength, iccDataLength / 2);
				dataLength += iccDataLength / 2;
			}
			break;
		case PROCESS_REVERSAL:
			msgType = (byte) 0x46;
			iccDataLength = appState.trans.getICCData().length();
			if(iccDataLength > 0){
				System.arraycopy(StringUtil.hexString2bytes(appState.trans.getICCData()), 0, data, dataLength, iccDataLength / 2);
				dataLength += iccDataLength / 2;
			}
			break;
		}
		commRequestData[commRequestDataLength] = msgType;
		commRequestDataLength++;

		commRequestData[commRequestDataLength] = (byte) (dataLength / 256);
		commRequestData[commRequestDataLength + 1] = (byte) (dataLength % 256);
		commRequestDataLength += 2;
		if (dataLength > 0) {
			System.arraycopy(data, 0, commRequestData, commRequestDataLength, dataLength);
			commRequestDataLength += dataLength;
		}
		if (debug) Log.d(APP_TAG, ByteUtil.arrayToHexStr("commRequestData: ", commRequestData, commRequestDataLength));
		return;
	}


	public void commClose() {
		commLock.lock();
		appState.setCommState(COMM_CLOSED);

		if (socket != null) {
			try {
				socket.close();
				socket = null;
				Log.i(APP_TAG, "socket closed");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		commLock.unlock();
	}

	public void notifyCommError() {
		Message msg = new Message();
		msg.what = COMM_CONNECT_ERROR_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	public void notifyCommConnected() {
		Message msg = new Message();
		msg.what = COMM_CONNECTED_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	public void notifyCommWriteData() {
		Message msg = new Message();
		msg.what = COMM_WRITE_DATA_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	public void notifyCommReadData() {
		Message msg = new Message();
		msg.what = COMM_READ_DATA_NOTIFIER;
		mHandler.sendMessage(msg);
	}
}
