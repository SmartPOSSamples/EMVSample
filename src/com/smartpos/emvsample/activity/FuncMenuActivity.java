package com.smartpos.emvsample.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudpos.AlgorithmConstants;
import com.cloudpos.pinpad.PINPadDevice;
import com.smartpos.emvsample.R;
import com.smartpos.util.StringUtil;

public class FuncMenuActivity extends FuncActivity
{
	private TextView textTitle  = null;
	private Button   buttonBack = null;
	private Button   buttonMore = null;

	private Button buttonSale = null;
	private Button buttonLastPBOC = null;
	private Button buttonTrans = null;
	private Button buttonSettle = null;
	private Button buttonEncrypt = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_func_menu);

		textTitle = (TextView)findViewById(R.id.tAppTitle);
		textTitle.setText("MAIN");

		buttonBack = (Button)findViewById(R.id.btn_back);
		buttonBack.setOnClickListener(new ClickListener());

		buttonMore = (Button)findViewById(R.id.btn_more);
		buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));

		buttonSale = (Button)findViewById(R.id.bFunc_Sale);
		buttonSale.setOnClickListener(new ClickListener());


		buttonLastPBOC = (Button)findViewById(R.id.bFunc_LastPBOC);
		buttonLastPBOC.setOnClickListener(new ClickListener());

		buttonTrans = (Button)findViewById(R.id.bFunc_Trans);
		buttonTrans.setOnClickListener(new ClickListener());

		buttonSettle = (Button)findViewById(R.id.bFunc_Settle);
		buttonSettle.setOnClickListener(new ClickListener());

		buttonEncrypt = (Button)findViewById(R.id.bFunc_encrypt);
		buttonEncrypt.setOnClickListener(new ClickListener());
	}

	@Override
	protected void onStart()
	{
		if(debug)Log.d(APP_TAG, "FuncMenuActivity onStart");
		super.onStart();
		if(appState.emvParamChanged == true)
		{
			setEMVTermInfo();
		}
		startIdleTimer(TIMER_IDLE, DEFAULT_IDLE_TIME_SECONDS);
	}

	@Override
	protected void onResume()
	{
		if(debug)Log.d(APP_TAG, "FuncMenuActivity onResume");
		super.onResume();
	}

	@Override
	protected void onStop()
	{
		if(debug)Log.d(APP_TAG, "FuncMenuActivity onStop");
		super.onStop();
	}

	@Override
	protected void onPause()
	{
		if(debug)Log.d(APP_TAG, "FuncMenuActivity onPause");
		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		if(debug)Log.d(APP_TAG, "FuncMenuActivity onDestroy");
		super.onDestroy();
	}

	@Override
	public void onBackPressed(){
		go2Idle();
	}

	@Override
	protected void onCancel()
	{
		onBackPressed();
	}

	@Override
	protected void onBack()
	{
		onBackPressed();
	}

	public class ClickListener implements View.OnClickListener
	{
		@Override
		public void onClick(View v)
		{
			switch(v.getId())
			{
			case R.id.bFunc_Sale:
				appState.needCard = true;
				sale();
				// saleInsertCardFirst();
				break;

			case R.id.bFunc_LastPBOC:
				showLastPBOC();
				break;

			case R.id.bFunc_Trans:
				//queryCardRecord((byte)0x00);
				balance();
				break;

			case R.id.bFunc_Settle:
				settle();
				break;

			case R.id.bFunc_encrypt:
				Encrypt();

				break;

			case R.id.btn_back:
				go2Idle();
				break;
			}
		}
	}
	private static final String EncryptTag = "EncryptTest";

	private void Encrypt(){
		String sResult = "";
		if(appState.pinpadService.open())
		{


			byte[] inArray = StringUtil.hexString2bytes("57132223000010364419D19122010000000005230F000000");

			//NOTE the old code is as follows, the hard-coded index is not used in the new code.
			//int nKeyType, int nMasterKeyID, int nUserKeyID, int nAlgorith
			//if(PinPadInterface.setKey(1,2,0,DOUBLE_KEY) >=0)

			byte[] outArray = appState.pinpadService.encryptData(PINPadDevice.KEY_TYPE_MK_SK,appState.terminalConfig.getKeyIndex(),0, AlgorithmConstants.ALG_3DES,inArray);
			if(outArray!=null){
				Log.i(EncryptTag,"outArray:" + StringUtil.toHexString(outArray,true));
				Toast.makeText(this,
					"in"+ StringUtil.toHexString(inArray,true) + "\n" +
						"out:"+StringUtil.toHexString(outArray,true),
					Toast.LENGTH_SHORT).show();
			}else {
				Log.w(EncryptTag,"encryptData Error!!!!!!!" );
				Toast.makeText(this,
					"encryptData Error!",
					Toast.LENGTH_SHORT).show();
			}
		}else{
			Toast.makeText(this,
				"Pinpad open failed!!!!!",
				Toast.LENGTH_SHORT).show();
		}
		appState.pinpadService.close();
	}

	public byte[] createByteArray(int length) {
		byte[] array = new byte[length];
		int random;
		for (int i = 0; i < length; i++) {
			random = (int) (Math.random() * (0xFF));
			array[i] = (byte) random;
		}
		return array;
	}
	public byte[] subBuffer(byte[] buf, int length) {
		if (length < 0) {
			return null;
		}
		byte[] result = new byte[length];
		if (buf.length >= length) {
			System.arraycopy(buf, 0, result, 0, length);
		}
		return result;
	}
}
