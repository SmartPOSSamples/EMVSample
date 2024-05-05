package com.smartpos.emvsample.activity;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ConfirmCardActivity extends FuncActivity 
{
	private TextView textTitle  = null;
	private Button   buttonBack = null;
    private Button   buttonMore = null;
    
	private EditText textCard;
	private TextView textTransType;
	private Button buttonCancel = null;
	private Button buttonOK = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_confirm_card);
        
        // title
        textTitle = (TextView)findViewById(R.id.tAppTitle);
		textTitle.setText(appState.getString(TransDefine.transInfo[appState.getTranType()].id_display_en));
		
	    buttonBack = (Button)findViewById(R.id.btn_back);
        buttonBack.setOnClickListener(new ClickListener());
        
        buttonMore = (Button)findViewById(R.id.btn_more);
        buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));
        
        buttonOK = (Button)findViewById(R.id.btn_digit_enter);
        buttonOK.setOnClickListener(new ClickListener());
        
        buttonCancel = (Button)findViewById(R.id.btn_digit_cancel);
        buttonCancel.setOnClickListener(new ClickListener());
        
        textTransType = (TextView)findViewById(R.id.tConfirmCard_TransType);
        textTransType.setText(TransDefine.transInfo[appState.getTranType()].id_display_en);
        
        textCard = (EditText)findViewById(R.id.tConfirmCard_Card);
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();
    	textCard.setText(appState.trans.getPAN());
        startIdleTimer(TIMER_FINISH, DEFAULT_IDLE_TIME_SECONDS);
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
