package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;
import com.smartpos.util.AppUtil;

public class InputAmountActivity extends FuncActivity
{
	private TextView textTitle  = null;
	private Button   buttonBack = null;
    private Button   buttonMore = null;
    
	private Button buttonCancel = null;
	private Button buttonClear  = null;
	private Button buttonBackSpace = null;
	private Button buttonEnter  = null;
	private Button buttonDigital_0 = null;
	private Button buttonDigital_1 = null;
	private Button buttonDigital_2 = null;
	private Button buttonDigital_3 = null;
	private Button buttonDigital_4 = null;
	private Button buttonDigital_5 = null;
	private Button buttonDigital_6 = null;
	private Button buttonDigital_7 = null;
	private Button buttonDigital_8 = null;
	private Button buttonDigital_9 = null;

	private TextView textTransType = null;
	private TextView textPrompt = null;
	private EditText textAmount = null;
	private int amount = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_input_amount);
        // title
        textTitle = (TextView)findViewById(R.id.tAppTitle);
		textTitle.setText(appState.getString(TransDefine.transInfo[appState.getTranType()].id_display_en));
		
	    buttonBack = (Button)findViewById(R.id.btn_back);
        buttonBack.setOnClickListener(new ClickListener());
        
        buttonMore = (Button)findViewById(R.id.btn_more);
        buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));
        // digit pad
        buttonEnter = (Button)findViewById(R.id.btn_digit_enter);
        buttonEnter.setOnClickListener(new ClickListener());
        
        buttonCancel = (Button)findViewById(R.id.btn_digit_cancel);
        buttonCancel.setOnClickListener(new ClickListener());
        
        buttonClear =  (Button)findViewById(R.id.btn_digit_clear);
        buttonClear.setOnClickListener(new ClickListener());
        
        buttonBackSpace =  (Button)findViewById(R.id.btn_digit_backspace);
        buttonBackSpace.setOnClickListener(new ClickListener());
        
        buttonDigital_0 =  (Button)findViewById(R.id.btn_digit_0);
        buttonDigital_0.setOnClickListener(new ClickListener());
        
        buttonDigital_1 =  (Button)findViewById(R.id.btn_digit_1);
        buttonDigital_1.setOnClickListener(new ClickListener());
        
        buttonDigital_2 =  (Button)findViewById(R.id.btn_digit_2);
        buttonDigital_2.setOnClickListener(new ClickListener());
        
        buttonDigital_3 =  (Button)findViewById(R.id.btn_digit_3);
        buttonDigital_3.setOnClickListener(new ClickListener());
        
        buttonDigital_4 =  (Button)findViewById(R.id.btn_digit_4);
        buttonDigital_4.setOnClickListener(new ClickListener());
        
        buttonDigital_5 =  (Button)findViewById(R.id.btn_digit_5);
        buttonDigital_5.setOnClickListener(new ClickListener());
        
        buttonDigital_6 =  (Button)findViewById(R.id.btn_digit_6);
        buttonDigital_6.setOnClickListener(new ClickListener());
        
        buttonDigital_7 =  (Button)findViewById(R.id.btn_digit_7);
        buttonDigital_7.setOnClickListener(new ClickListener());
        
        buttonDigital_8 =  (Button)findViewById(R.id.btn_digit_8);
        buttonDigital_8.setOnClickListener(new ClickListener());
        
        buttonDigital_9 =  (Button)findViewById(R.id.btn_digit_9);
        buttonDigital_9.setOnClickListener(new ClickListener());
        
        textTransType = (TextView)findViewById(R.id.tInputAmount_TransType);
        textPrompt =(TextView)findViewById(R.id.tInputAmount_Prompt);
        textPrompt.setText("PLEASE INPUT TRANS AMOUNT:");
        textAmount = (EditText)findViewById(R.id.tInputAmount_Amount);
		textAmount.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if(event.getAction() == KeyEvent.ACTION_DOWN)
					InputAmountActivity.this.onKeyDown(keyCode, event);
				return true;
			}
		});
		textAmount.requestFocus();
    }
    
    @Override
    public void onStart()
    {
    	super.onStart(); 
    	textTransType.setText(TransDefine.transInfo[appState.getTranType()].id_display_en);
        amount = 0;
        setTextAmount(amount);
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

    }
    
    private void setTextAmount(int digital)
    {
    	if(amount < 100000000)
    	{
    		amount = amount * 10 + digital;
	    	textAmount.setText(AppUtil.formatAmount(amount)); 
    	}
    }

	@Override
	protected void onKeyCode(char key)
	{
		if(key != '.')
		{
			int keyCode = key - '0';
			setTextAmount(keyCode);
		}
	}

	@Override
	protected void onDel()
	{
		amount = amount / 10;
		textAmount.setText(AppUtil.formatAmount(amount));
	}

	@Override
	protected void onEnter()
	{
		if(amount > 0 && amount<= 999999999)
		{
			appState.trans.setTransAmount(amount);
			setResult(Activity.RESULT_OK, getIntent());
			exit();
		}
	}

	@Override
	protected void onCancel()
	{
		setResult(Activity.RESULT_CANCELED, getIntent());
		exit();
	}

	@Override
	protected void onBack()
	{
		onCancel();
	}

	public class ClickListener implements View.OnClickListener
    {
		@Override
		public void onClick(View v) 
		{
			Intent intent = getIntent();
			onTouch();
			switch(v.getId())
			{
			case R.id.btn_digit_0:
				setTextAmount(0);
				break;
			case R.id.btn_digit_1:
				setTextAmount(1);
				break;
			case R.id.btn_digit_2:
				setTextAmount(2);
				break;
			case R.id.btn_digit_3:
				setTextAmount(3);
				break;
			case R.id.btn_digit_4:
				setTextAmount(4);
				break;
			case R.id.btn_digit_5:
				setTextAmount(5);
				break;
			case R.id.btn_digit_6:
				setTextAmount(6);
				break;
			case R.id.btn_digit_7:
				setTextAmount(7);
				break;
			case R.id.btn_digit_8:
				setTextAmount(8);
				break;
			case R.id.btn_digit_9:
				setTextAmount(9);
				break;
			case R.id.btn_digit_backspace:
				amount = amount / 10;
		    	textAmount.setText(AppUtil.formatAmount(amount)); 
				break;
			case R.id.btn_digit_clear:
				amount = 0;
				setTextAmount(0); 
				break;
			case R.id.btn_digit_enter:
				if(amount > 0 && amount<= 999999999)
				{
					appState.trans.setTransAmount(amount);
					setResult(Activity.RESULT_OK, intent);
					exit();
				}
				break;
			case R.id.btn_digit_cancel:
			case R.id.btn_back:
				setResult(Activity.RESULT_CANCELED, intent);
				exit();
				break;			
			}
		}
    }
	
}
