package com.smartpos.emvsample.activity;

import java.text.NumberFormat;
import java.util.Locale;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

import android.widget.Button;
import android.widget.TextView;

public class EnquireTransActivity extends FuncActivity 
{
	private TextView textLine1 = null;
	private TextView textLine2 = null;
	private TextView textLine3 = null;
	private TextView textLine4 = null;
	private TextView textLine5 = null;
	private TextView textLine6 = null;
	private Button buttonCancel = null;
	private Button buttonPrev = null;
	private Button buttonNext = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_enquire_trans);
        
        buttonPrev = (Button)findViewById(R.id.bEnquireTrans_Prev);
        buttonPrev.setOnClickListener(new ClickListener());
        buttonNext = (Button)findViewById(R.id.bEnquireTrans_Next);
        buttonNext.setOnClickListener(new ClickListener());
        buttonCancel = (Button)findViewById(R.id.btn_digit_cancel);
        buttonCancel.setOnClickListener(new ClickListener());
        
        textLine1 = (TextView)findViewById(R.id.tEnquireTrans_Line1);
        textLine2 = (TextView)findViewById(R.id.tEnquireTrans_Line2);
        textLine3 = (TextView)findViewById(R.id.tEnquireTrans_Line3);
        textLine4 = (TextView)findViewById(R.id.tEnquireTrans_Line4);
        textLine5 = (TextView)findViewById(R.id.tEnquireTrans_Line5);
        textLine6 = (TextView)findViewById(R.id.tEnquireTrans_Line6);
    }

    @Override
    public void onStart()
    {
    	boolean ret = false;
    	
    	super.onStart();
    	if(appState.getTranType() == QUERY_SPECIFIC)
    	{
	    	ret = appState.transDetailService.findByTrace(appState.trans.getTrace(), appState.trans);
			if(ret == false)
			{
				go2Error(R.string.error_trans_not_found);
				return;
			}
			buttonPrev.setEnabled(false);
			buttonNext.setEnabled(false);
    	}
    	else
    	{
    		ret = appState.transDetailService.startQuery(appState.trans);
			if(ret == false)
			{
				go2Error(R.string.error_trans_empty);
				return;
			}
    	}
    	showTrans();
        startIdleTimer(TIMER_IDLE, DEFAULT_IDLE_TIME_SECONDS);
    }
	
    @Override 
    protected void onPause() { 
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
    
    private void showTrans()
    {
		textLine1.setText("CARD: " + appState.trans.getPAN());
		textLine2.setText(TransDefine.transInfo[appState.trans.getTransType()].id_display_en);
		textLine3.setText("DATE: " + appState.trans.getTransDate());
		textLine4.setText("TIME: " + appState.trans.getTransTime());
		textLine5.setText("AMOUNT: " + NumberFormat.getCurrencyInstance(Locale.CHINA).format(appState.trans.getTransAmount()/100));
		textLine6.setText("AUTH CODE:" + appState.trans.getAuthCode());
    }
    
	public class ClickListener implements View.OnClickListener
    {
		@Override
		public void onClick(View v) 
		{
			switch(v.getId())
			{
			case R.id.bEnquireTrans_Prev:
				if(appState.getTranType() == QUERY_TRANS_DETAIL)
				{
					if(appState.transDetailService.queryPrev(appState.trans) == true)
					{
						showTrans();
					}
				}
				break;
			case R.id.bEnquireTrans_Next:
				if(appState.getTranType() == QUERY_TRANS_DETAIL)
				{
					if(appState.transDetailService.queryNext(appState.trans) == true)
					{
						showTrans();
					}
				}
				break;
			case R.id.btn_digit_cancel:
				if(appState.getTranType() == QUERY_TRANS_DETAIL)
				{
					appState.transDetailService.endQuery();
				}
				exit();
				break;	
			}
		}
	
    }
}
