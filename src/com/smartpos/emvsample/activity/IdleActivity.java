package com.smartpos.emvsample.activity;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.constant.EMVConstant;
import com.smartpos.util.StringUtil;

import static com.cloudpos.jniinterface.EMVJNIInterface.emv_get_config_checksum;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_get_kernel_checksum;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_get_kernel_id;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_get_process_type;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_get_version_string;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_kernel_initialize;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_send_signal_initialize;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_kernel_attr;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_log_level;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_mastercard_signal_attr;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_terminal_param_set_drl;
import static com.cloudpos.jniinterface.EMVJNIInterface.loadEMVKernel;
import static com.cloudpos.jniinterface.EMVJNIInterface.registerFunctionListener;

public class IdleActivity extends FuncActivity implements EMVConstant
{
	private TextView textTitle  = null;
	private Button buttonBack = null;
    private Button   buttonMore = null;
    
	private TextView idleLine1;
	private TextView idleLine2;
	private TextView idleLine3;
	private TextView idleLine4;
	private TextView idleLine5;

	@Override
    public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_idle);
        
        textTitle = (TextView)findViewById(R.id.tAppTitle);
		textTitle.setText("smartpos");
		
	    buttonBack = (Button)findViewById(R.id.btn_back);
        buttonBack.setOnClickListener(new ClickListener());
        
        buttonMore = (Button)findViewById(R.id.btn_more);
        buttonMore.setOnClickListener(new ClickListener());
        
        if(debug) Log.e(APP_TAG, "idleActivity onCreate");
        
        idleLine1 = (TextView)findViewById(R.id.idleLine1);
    	idleLine2 = (TextView)findViewById(R.id.idleLine2);
    	idleLine3 = (TextView)findViewById(R.id.idleLine3);
		idleLine4 = (TextView)findViewById(R.id.idleLine4);
		idleLine5 = (TextView)findViewById(R.id.idleLine5);

    	if(appState.icInitFlag == false)
    	{
    		if(appState.contactService.open())
    		{
    			Log.d(APP_TAG, "ContactICCard open OK");
    			appState.icInitFlag = true;
    		}
    		if(appState.contactlessService.open())
    		{
    			Log.d(APP_TAG, "ContactlessICCard open OK");
    			appState.icInitFlag = true;
    		}
			appState.contactService.close();
			appState.contactlessService.close();
    	}
    }

    @Override
    public void handleMessageSafe(Message msg)
    {
        /*这里是处理信息的方法*/
        switch (msg.what)
        {
        case CARD_INSERT_NOTIFIER:
            Bundle bundle = msg.getData();
            int nEventID = bundle.getInt("nEventID");
            int nSlotIndex = bundle.getInt("nSlotIndex");
            if(debug)Log.d(APP_TAG, "get CONTACT_CARD_EVENT_NOTIFIER,event[" + nEventID + "]slot[" + nSlotIndex + "]" );
            if(   nSlotIndex == 0
                && nEventID == SMART_CARD_EVENT_INSERT_CARD
                )
            {
                cancelMSRThread();
                appState.resetCardError = false;
                appState.trans.setCardEntryMode(INSERT_ENTRY);
                appState.needCard = false;
                sale();
            }
            break;
        case CARD_ERROR_NOTIFIER:
            cancelMSRThread();
            appState.trans.setEmvCardError(true);
            appState.resetCardError = true;
            appState.needCard = true;
            sale();
            break;
        }
    }

    @Override 
    protected void onStart() { 
    	if(debug)Log.e(APP_TAG, "idleActivity onStart"); 
        super.onStart(); 
        appState.initData();

        appState.idleFlag = true;

		if(appState.emvParamLoadFlag == false){
			loadEMVParam();
		}else{
			if(appState.emvParamChanged == true){
				setEMVTermInfo();
			}
		}
		idleLine1.setText("GOODS / SERVICE");
		idleLine2.setText("PLEASE INSERT CARD");

		byte[] version = new byte[32];
		byte[] kernelChecksum = new byte[8];
		byte[] configChecksum = new byte[8];

		int len = emv_get_version_string(version, version.length);
		idleLine3.setText(new String(version, 0, len));

		if(emv_get_kernel_checksum(kernelChecksum, kernelChecksum.length) > 0){
			idleLine4.setText("KC: " + StringUtil.toHexString(kernelChecksum, false));
		}
		if(emv_get_config_checksum(configChecksum, configChecksum.length) > 0){
			idleLine5.setText("CC: " + StringUtil.toHexString(configChecksum, false));
		}
		mHandler.setFunActivity(this);

		if(appState.icInitFlag != true){
			appState.idleFlag = false;
			go2Error(R.string.error_init_ic);
			return;
		}
		waitContactCard();
    }
    
    @Override 
    protected void onResume() { 
    	if(debug)Log.e(APP_TAG, "idleActivity onResume"); 
        super.onResume(); 
    }
    
    @Override 
    protected void onStop() { 
    	if(debug)Log.e(APP_TAG, "idleActivity onStop");
        super.onStop(); 
    }
    
    @Override
    public void onBackPressed(){
		appState.idleFlag = false;
		cancelMSRThread();
		cancelContactCard();
        requestFuncMenu();
    }

	@Override
	protected void onBack()
	{
		onBackPressed();
	}

	@Override
	protected void onCancel()
	{
		onBackPressed();
	}

	@Override
	protected void onEnter()
	{
		onBackPressed();
	}

	public void loadEMVParam()
    {
    	//lib path
		String tmpEmvLibDir = "";
	    byte[] kernelAttr = new byte[]{0x20, 0x08};
		tmpEmvLibDir = this.getApplicationInfo().nativeLibraryDir + "/libEMVKernal.so";

	    Log.i("IdleActivity", "tmpEmvLibDir:" + tmpEmvLibDir);

		if (loadEMVKernel(tmpEmvLibDir.getBytes(),tmpEmvLibDir.getBytes().length) == 0)
    	{
			registerFunctionListener(this);
			emv_kernel_initialize();
//			if(enableSignal)
//			{
				emv_send_signal_initialize();
				emv_set_mastercard_signal_attr(1, 1, 0, 0);
//			}
		    if(emvLogLevel == 0)
		    {
			    kernelAttr[1] |= 0x04;
		    }
		    else if(emvLogLevel == 1)
		    {
			    kernelAttr[1] |= 0x02;
		    }
			emv_set_kernel_attr(kernelAttr, kernelAttr.length);

			if(enableEmvLog)
			{
				emv_set_log_level(1);
			}
			emv_terminal_param_set_drl(new byte[]{0x00}, 1);
			if(loadCAPK() == -2)
			{
				capkChecksumErrorDialog(IdleActivity.this);
			}
			loadAID();
			loadExceptionFile();
			loadRevokedCAPK();
			setEMVTermInfo();

			Log.i("test", "kernel id:"+emv_get_kernel_id());
			Log.i("test", "process type:"+emv_get_process_type());

			appState.emvParamLoadFlag = true;
    	}
    	//update WK
        // masterKey is new byte[]{'1','1','1','1','1','1','1','1' }
//        byte[] defaultPINKey = new byte[]{'2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2'};
//        try{
//	        if(appState.pinpadService.open()){
//		        appState.pinpadService.updateUserKey(appState.terminalConfig.getKeyIndex(), 0, defaultPINKey);
//		        appState.pinpadService.close();
//	        }
//        }catch(Exception e){
//
//        }
    }
    
    public class ClickListener implements View.OnClickListener
    {
		@Override
		public void onClick(View v) 
		{
			switch(v.getId())
			{
			case R.id.btn_back:
			case R.id.btn_more:
				appState.idleFlag = false;
				cancelMSRThread();
				cancelContactCard();
		        requestFuncMenu();
				break;
			}
		}
    }

}
