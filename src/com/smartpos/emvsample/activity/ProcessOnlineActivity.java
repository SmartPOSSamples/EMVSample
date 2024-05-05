package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;

import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_online_result;

public class ProcessOnlineActivity extends FuncActivity
{
	private TextView textTitle  = null;
	private Button   buttonBack = null;
    private Button   buttonMore = null;

	private TextView textLine1;
	private TextView textLine2;
	private TextView textLine3;
	private TextView textLine4;

    boolean socketThreadRun = false;
    boolean requestDataReady = false;

	@Override
    public void onCreate(Bundle savedInstanceState)
	{
		if(debug)Log.d(APP_TAG, "processOnlineActivity onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_process_online);
    	// title
        textTitle = (TextView)findViewById(R.id.tAppTitle);
		textTitle.setText(appState.getString(TransDefine.transInfo[appState.getTranType()].id_display_en));
		
	    buttonBack = (Button)findViewById(R.id.btn_back);
        buttonBack.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));
        
        buttonMore = (Button)findViewById(R.id.btn_more);
        buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));

		textLine1 = (TextView) findViewById(R.id.tProcessOnline_Line1);
		textLine2 = (TextView) findViewById(R.id.tProcessOnline_Line2);
		textLine3 = (TextView) findViewById(R.id.tProcessOnline_Line3);
		textLine4 = (TextView) findViewById(R.id.tProcessOnline_Line4);

	    appState.trans.setTrace(appState.terminalConfig.getTrace());

    	appState.terminalConfig.incTrace();
		if(offlineDemo){
			appState.trans.setResponseCode(new byte[]{'0','0'});
			processResult();
		}
		else{
			sendPackage();
		}
		return;
	}
	
	private void processResult()
	{
		if(debug)Log.d(APP_TAG, "processResult");
		switch(appState.getProcessState())
		{
		case PROCESS_NORMAL:
			if (    appState.trans.getResponseCode() != null
				 &&	appState.trans.getResponseCode()[0] == '0'
				 && appState.trans.getResponseCode()[1] == '0'
			   ) 
			{
    			if(appState.trans.getEMVOnlineFlag() == true)
    			{
					appState.trans.setEMVOnlineResult(ONLINE_SUCCESS);
					// Set Sample Script
				    // 8A: '00'
				    // 91: 00 00 00 00 00 00 00 00
				    appState.trans.setIssuerAuthData(new byte[]{(byte)0x8A, 0x02, 0x30, 0x30, (byte)0x91, 0x08, 0x00,0x00, 0x00,0x00, 0x00,0x00, 0x00,0x00}, 0, 14);
					byte[] issuerData = appState.trans.getIssuerAuthData();
					if(issuerData != null && issuerData.length > 0)
    				{
    					emv_set_online_result(appState.trans.getEMVOnlineResult(), appState.trans.getResponseCode(), issuerData, issuerData.length);
    				}
    				else
    				{
    					emv_set_online_result(appState.trans.getEMVOnlineResult(), appState.trans.getResponseCode(), new byte[]{' '}, 0);
    				}
    			}
				break;
			}
			else if(   appState.trans.getResponseCode() != null
				    && appState.trans.getResponseCode()[0] == 'F'
				    && appState.trans.getResponseCode()[1] == 'F'
			       ) 
			{
				appState.trans.setEMVOnlineResult(ONLINE_FAIL);
				emv_set_online_result(appState.trans.getEMVOnlineResult(), appState.trans.getResponseCode(), new byte[]{' '}, 0);
			}
			else{
				appState.trans.setEMVOnlineResult(ONLINE_DENIAL);
				byte[] issuerData = appState.trans.getIssuerAuthData();
				if(issuerData != null && issuerData.length > 0)
				{
					emv_set_online_result(appState.trans.getEMVOnlineResult(), appState.trans.getResponseCode(), issuerData, issuerData.length);
				}
				else
				{
					emv_set_online_result(appState.trans.getEMVOnlineResult(), appState.trans.getResponseCode(), new byte[]{' '}, 0);
				}
			}
			break;
		case PROCESS_ADVICE_OFFLINE:
			break;
		}
		if(debug)Log.d(APP_TAG, "ProcessOnlineActivity finish success");
		setResult(Activity.RESULT_OK, getIntent());
		exit();
	}

	private void sendPackage() {
		appState.threadPool.execute(new CommThread());
	}
}
