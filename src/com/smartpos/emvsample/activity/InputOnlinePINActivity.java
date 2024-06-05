package com.smartpos.emvsample.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.cloudpos.OperationListener;
import com.cloudpos.OperationResult;
import com.cloudpos.jniinterface.PinPadCallbackHandler;
import com.cloudpos.pinpad.PINPadDevice;
import com.cloudpos.pinpad.PINPadOperationResult;
import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;
import com.smartpos.util.AppUtil;


public class InputOnlinePINActivity extends FuncActivity implements PinPadCallbackHandler {
	private final int PINPAD_CANCEL  = -65792;
	private final int PINPAD_TIMEOUT = -65538;

	private TextView textTitle  = null;
	private TextView textPin = null;
	private Button   buttonBack = null;
	private Button   buttonMore = null;
	private Handler mHandler;
	private OperationListener pinListener = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_input_pin);
		// title
		textTitle = (TextView) findViewById(R.id.tAppTitle);
		textTitle.setText(appState.getString(TransDefine.transInfo[appState.getTranType()].id_display_en));

		textPin = (TextView) findViewById(R.id.input_pin);
		if (textPin != null)
			textPin.setText("");

		buttonBack = (Button) findViewById(R.id.btn_back);
		buttonBack.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));

		buttonMore = (Button) findViewById(R.id.btn_more);
		buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));

		mHandler = new Handler() {
			public void handleMessage(Message msg) { /*Here's how to process information*/
				switch (msg.what) {
				case PIN_SUCCESS_NOTIFIER:
					setResult(Activity.RESULT_OK, getIntent());
					break;
				case PIN_ERROR_NOTIFIER:
					appState.setErrorCode(R.string.error_pinpad);
					break;
				case PIN_CANCELLED_NOTIFIER:
					appState.setErrorCode(R.string.error_user_cancelled);
					break;
				case PIN_TIMEOUT_NOTIFIER:
					appState.setErrorCode(R.string.error_input_timeout);
					break;
				}
				exit();
			}
		};
		if (pinListener == null)
		{
			pinListener = new OperationListener() {
				@Override
				public void handleResult(OperationResult operationResult) {
					if (operationResult.getResultCode() == OperationResult.SUCCESS) {
						byte[] pinBlock = ((PINPadOperationResult) operationResult).getEncryptedPINBlock();
						if(pinBlock==null || pinBlock.length == 0){
							appState.trans.setPinEntryMode(CANNOT_PIN);
						}else{
							appState.trans.setPinBlock(pinBlock);
							appState.trans.setPinEntryMode(CAN_PIN);
						}
						notifyPinSuccess();
					} else if (operationResult.getResultCode() == OperationResult.CANCEL) {
						notifyPinCancel();
					} else if (operationResult.getResultCode() == OperationResult.ERR_TIMEOUT) {
						notifyPinTimeout();
					} else {
						notifyPinError();
					}
					appState.pinpadService.close();
					appState.pinpadOpened = false;
				}
			};
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if(appState.pinpadOpened == false)
		{
			if(!appState.pinpadService.open())
			{
				notifyPinError();
				return;
			}
			appState.pinpadService.setGUIConfiguration("sound", "true");
			appState.pinpadService.setGUIConfiguration("soundType", "1");
			appState.pinpadOpened = true;
			//Don't allow byPasspin
			appState.pinpadService.setAllowByPassPin(false);
			//Custom UI
			if(appState.pinpadType == PINPAD_CUSTOM_UI)
			{
				appState.pinpadService.setPinPadCallback(InputOnlinePINActivity.this);
			}
			if(appState.trans.getTransAmount() > 0)
			{
				String sText = AppUtil.formatAmount(appState.trans.getTransAmount());
				appState.pinpadService.setText(0, sText);
			}

			String cardPAN = "0000000000000000";
			if(!appState.pinpadService.inputPin(pinListener, PINPadDevice.KEY_TYPE_MK_SK,appState.terminalConfig.getKeyIndex(),0,cardPAN)){
				notifyPinError();
				return;
			}
		}
	}

	@Override
	protected void onPause()
	{
		super.onPause();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
	}

	@Override
	public void onBackPressed(){

	}

	private void notifyPinSuccess()
	{
		Message msg = new Message();
		msg.what = PIN_SUCCESS_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	private void notifyPinError()
	{
		Message msg = new Message();
		msg.what = PIN_ERROR_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	private void notifyPinCancel()
	{
		Message msg = new Message();
		msg.what = PIN_CANCELLED_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	private void notifyPinTimeout()
	{
		Message msg = new Message();
		msg.what = PIN_TIMEOUT_NOTIFIER;
		mHandler.sendMessage(msg);
	}

	protected char[] stars = "●●●●●●●●●●●●●●●●".toCharArray();
	public static final int PIN_AMOUNT_SHOW  = 0x10000;
	public static final int PIN_KEY_CALLBACK = 0x10001;
	private Handler commHandler = createCommHandler();



	public void processCallback(byte[] data) {
		Log.i("processCallback", "" + data);
		if(data != null)
			commHandler.obtainMessage(PIN_KEY_CALLBACK, data[0], data[1]).sendToTarget();
	}

	public void processCallback(int nCount, int nExtra)
	{
		Log.i("processCallback", "nCount:" + nCount + ",nExtra:" + nExtra);
		commHandler.obtainMessage(PIN_KEY_CALLBACK, nCount, nExtra).sendToTarget();
	}

	@SuppressLint("HandlerLeak")
	protected Handler createCommHandler()
	{	// 无 Pinpad时跳过. DuanCS@[20141001]
		return new Handler()
		{
			public void handleMessage(Message msg)
			{ /* Here's how to process information */
				switch (msg.what)
				{
				case PIN_AMOUNT_SHOW:	// Its value has been displayed via onFlush. DuanCS@[20150907]
//					setTextById(R.id.amount, msg.obj.toString());
					textPin.setText(msg.obj.toString());	// This line will also not be executed because Pinpad.showText() does not trigger the callback... DuanCS@[20150912]
					break;
				case PIN_KEY_CALLBACK:
					textPin.setText(stars, 0, msg.arg1 & 0x0F);
					break;
				}
			}
		};
	}

	@Override
	public void onPointerCaptureChanged(boolean hasCapture) {
	}
}