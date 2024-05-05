package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.smartpos.emvsample.R;
import com.smartpos.util.StringUtil;

public class Balance extends FuncActivity
{
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        appState.setTranType(TRAN_BALANCE);
		appState.trans.setTransType(TRAN_BALANCE);
		appState.getCurrentDateTime();
		appState.trans.setTransDate(   appState.currentYear
                                     + StringUtil.fillZero(Integer.toString(appState.currentMonth), 2)
                                     + StringUtil.fillZero(Integer.toString(appState.currentDay), 2)
                                   );
		appState.trans.setTransTime(   StringUtil.fillZero(Integer.toString(appState.currentHour), 2)
                                     + StringUtil.fillZero(Integer.toString(appState.currentMinute), 2)
                                     + StringUtil.fillZero(Integer.toString(appState.currentSecond), 2)
                                   );
		if (appState.batchInfo.getSettlePending() != 0)
		{
			appState.setErrorCode(R.string.error_settle_first);
	    	showTransResult();
			return;
		}

		requestCard(true, true, true);
    }
        
	@Override
	public void onStart()
	{
        super.onStart();
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
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(   requestCode != STATE_TRANS_END
		   && appState.getErrorCode() > 0
		  )
		{
			showTransResult();
			return;
		}
		if(resultCode != Activity.RESULT_OK)
		{
			exitTrans();
			return;
		}
		switch(requestCode)
		{
		case STATE_REQUEST_CARD:
            if(   appState.trans.getCardEntryMode() == INSERT_ENTRY
	           || appState.trans.getCardEntryMode() == CONTACTLESS_ENTRY
              )
            {
                processEMVCard();
            }
            else
            {
                confirmCard();
            }
			break;
        case STATE_CONFIRM_CARD:
            inputOnlinePIN();
            break;
		case STATE_INPUT_ONLINE_PIN:
			processOnline();
			break;
		case STATE_PROCESS_ONLINE:
			showTransResult();
			break;
		case STATE_PROCESS_EMV_CARD:
			showTransResult();
			break;
		case STATE_TRANS_END:
		case STATE_REMOVE_CARD:
			exitTrans();
			break;
		}
	}
}