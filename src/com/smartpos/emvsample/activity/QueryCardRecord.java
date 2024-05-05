package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class QueryCardRecord extends FuncActivity
{
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
       	appState.setTranType(QUERY_CARD_RECORD);

        requestCard(false, true, true);
    }
        
	@Override
	public void onStart()
	{
        super.onStart();
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
			processEMVCard();
			break;
		case STATE_PROCESS_EMV_CARD:
			showPBOCCardRecord();
			break;
		case STATE_SHOW_EMV_CARD_TRANS:
		case STATE_REMOVE_CARD:
		case STATE_TRANS_END:
			exitTrans();
			break;
		}
	}
}