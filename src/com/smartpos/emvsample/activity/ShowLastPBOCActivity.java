package com.smartpos.emvsample.activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.smartpos.emvsample.R;

public class ShowLastPBOCActivity extends FuncActivity
{
	private TextView textTitle     = null;
	private Button   buttonBack = null;
    private Button   buttonMore = null;
    
	private Button buttonEnter     = null;
	private Button buttonCancel    = null;
	private TextView textLine1 = null;
	private TextView textLine2 = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_last_pboc);
        
        // title
        textTitle = (TextView)findViewById(R.id.tAppTitle);
  		textTitle.setText("LAST EMV INFO");

	    buttonBack = (Button)findViewById(R.id.btn_back);
        buttonBack.setOnClickListener(new ClickListener());
        
        buttonMore = (Button)findViewById(R.id.btn_more);
        buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));
        
  		buttonEnter = (Button)findViewById(R.id.btn_digit_enter);
  		buttonEnter.setOnClickListener(new ClickListener());
  	    
  	    buttonCancel = (Button)findViewById(R.id.btn_digit_cancel);
  	    buttonCancel.setOnClickListener(new ClickListener());

        textLine1 = (TextView)findViewById(R.id.tShowLastPBOC_Line1);
        textLine2 = (TextView)findViewById(R.id.tShowLastPBOC_Line2);
    }
    
    @Override
    public void onStart()
    {
    	super.onStart(); 
    	
    	textLine1.setText("TVR:" + appState.terminalConfig.getLastTVR() );
    	textLine2.setText("TSI:" + appState.terminalConfig.getLastTSI() );
    	startIdleTimer(TIMER_FINISH, DEFAULT_IDLE_TIME_SECONDS);
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
    	finish();
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

	@Override
	protected void onEnter()
	{
		onBackPressed();
	}

	public class ClickListener implements View.OnClickListener
    {
		@Override
		public void onClick(View v) 
		{
			onTouch();
			switch(v.getId())
			{
			case R.id.btn_digit_enter:
			case R.id.btn_digit_cancel:
			case R.id.btn_back:
				finish();
				break;	
			}
		}
	
    }}
