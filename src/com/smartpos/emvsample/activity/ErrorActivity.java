package com.smartpos.emvsample.activity;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class ErrorActivity extends FuncActivity 
{
	private TextView textTitle  = null;
	private Button   buttonBack = null;
    private Button   buttonMore = null;
    
	private TextView textLine1;
	private TextView textLine2;
	private TextView textLine3;
	private TextView textLine4;
	private Button buttonOK;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_error);
        // title
        textTitle = (TextView)findViewById(R.id.tAppTitle);
		textTitle.setText(appState.getString(TransDefine.transInfo[appState.getTranType()].id_display_en));
		
	    buttonBack = (Button)findViewById(R.id.btn_back);
        buttonBack.setOnClickListener(new ClickListener());
        
        buttonMore = (Button)findViewById(R.id.btn_more);
        buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));
        
        textLine1 = (TextView)findViewById(R.id.tError_Line1);
        textLine2 = (TextView)findViewById(R.id.tError_Line2);
        textLine3 = (TextView)findViewById(R.id.tError_Line3);
        textLine4 = (TextView)findViewById(R.id.tError_Line4);
        buttonOK  = (Button)findViewById(R.id.btn_digit_enter);
        buttonOK.setOnClickListener(new ClickListener());
    }
    
	@Override
	protected void onStart()
	{
		super.onStart();
        if(appState.getErrorCode() > 0)
        {
        	textLine2.setText(appState.getErrorCode());
            startIdleTimer(TIMER_IDLE, DEFAULT_IDLE_TIME_SECONDS);
        }
        else
        {
        	go2Idle();
        }

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
			switch(v.getId())
			{
			case R.id.btn_digit_enter:
		        go2Idle();
				break;
			}
		}
    }
}