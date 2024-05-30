package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.cloudpos.jniinterface.EMVJNIInterface;
import com.cloudpos.jniinterface.PinPadCallbackHandler;
import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;

public class InputOfflinePINActivity extends FuncActivity implements PinPadCallbackHandler {
	private TextView textTitle  = null;
	private TextView textPin = null;
	private Button   buttonBack = null;
	private Button   buttonMore = null;

	char[] stars = "●●●●●●●●●●●●●●●●".toCharArray();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_input_pin);
		// title
		textTitle = (TextView)findViewById(R.id.tAppTitle);
		textTitle.setText(appState.getString(TransDefine.transInfo[appState.getTranType()].id_display_en));

		textPin = (TextView) findViewById(R.id.input_pin);
		if(textPin != null) textPin.setText("");

		buttonBack = (Button)findViewById(R.id.btn_back);
		buttonBack.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));

		buttonMore = (Button)findViewById(R.id.btn_more);
		buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		if(appState.pinpadType == PINPAD_CUSTOM_UI){
			EMVJNIInterface.setEmvOfflinePinCallbackHandler(InputOfflinePINActivity.this);
		}
		EMVJNIInterface.emv_process_next();
		mHandler.setFunActivity(this);
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

	@Override
	public void handleMessageSafe(Message msg)
	{
		/*Here's how to process information*/
		switch (msg.what)
		{
		case OFFLINE_PIN_NOTIFIER:
			textPin.setText(stars, 0, msg.arg1 & 0x0F);
			break;
		case EMV_PROCESS_NEXT_COMPLETED_NOTIFIER:
			setResult(Activity.RESULT_OK, getIntent());
			exit();
			break;
		}
	}

	@Override
	public void processCallback(byte[] data) {
		processCallback(data[0], data[1]);
	}
	@Override
	public void processCallback(int nCount, int nExtra)
	{
		Message msg = new Message();
		msg.what = OFFLINE_PIN_NOTIFIER;
		msg.arg1 = nCount;
		msg.arg2 = nExtra;
		mHandler.sendMessage(msg);
	}

	@Override
	public void onPointerCaptureChanged(boolean hasCapture) {

	}
}