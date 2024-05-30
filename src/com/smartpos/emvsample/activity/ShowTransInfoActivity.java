package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;

import java.text.NumberFormat;
import java.util.Locale;

public class ShowTransInfoActivity extends FuncActivity 
{
	private TextView textLine1 = null;
	private TextView textLine2 = null;
	private TextView textLine3 = null;
	private TextView textLine4 = null;
	private TextView textLine5 = null;
	private TextView textLine6 = null;
	private Button buttonCancel = null;
	private Button buttonOK = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_trans_info);
        
        buttonOK = (Button)findViewById(R.id.btn_digit_enter);
        buttonOK.setOnClickListener(new ClickListener());
        
        buttonCancel = (Button)findViewById(R.id.btn_digit_cancel);
        buttonCancel.setOnClickListener(new ClickListener());
        
        textLine1 = (TextView)findViewById(R.id.tShowTrans_Line1);
        textLine2 = (TextView)findViewById(R.id.tShowTrans_Line2);
        textLine3 = (TextView)findViewById(R.id.tShowTrans_Line3);
        textLine4 = (TextView)findViewById(R.id.tShowTrans_Line4);
        textLine5 = (TextView)findViewById(R.id.tShowTrans_Line5);
        textLine6 = (TextView)findViewById(R.id.tShowTrans_Line6);
    }

    @Override
    public void onStart()
    {
    	super.onStart();
		boolean ret = appState.transDetailService.findByTrace(appState.trans.getTrace(), appState.trans);
		if(ret == false)
		{
			appState.setErrorCode(R.string.error_trans_not_found);
			exit();
			return;
		}
		textLine1.setText("Card number: " + appState.trans.getPAN());
		textLine2.setText(TransDefine.transInfo[appState.trans.getTransType()].id_display_en);
		textLine3.setText("Date: " + appState.trans.getTransDate());
		textLine4.setText("Time: " + appState.trans.getTransTime());
		textLine5.setText("Amount: " + NumberFormat.getCurrencyInstance(Locale.CHINA).format(appState.trans.getTransAmount()/100));
		textLine6.setText("Authorization code:" + appState.trans.getAuthCode());
        startIdleTimer(TIMER_FINISH, DEFAULT_IDLE_TIME_SECONDS);
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
    public void onBackPressed(){

    }


	public class ClickListener implements View.OnClickListener
    {
		@Override
		public void onClick(View v) 
		{
			Intent intent = getIntent();
			switch(v.getId())
			{
			case R.id.btn_digit_enter:
				setResult(Activity.RESULT_OK, intent);
				exit();
				break;
			case R.id.btn_digit_cancel:
				setResult(Activity.RESULT_CANCELED, intent);
				exit();
				break;	
			}
		}
	
    }
}
