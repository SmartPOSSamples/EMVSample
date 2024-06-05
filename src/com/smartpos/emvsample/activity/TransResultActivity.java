package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.printer.PrinterException;
import com.smartpos.emvsample.printer.PrinterHelper;
import com.smartpos.emvsample.transaction.TransDefine;
import com.smartpos.util.WizarTypeUtil;

import java.util.Timer;
import java.util.TimerTask;

public class TransResultActivity extends FuncActivity
{
	private TextView textTitle  = null;
	private Button   buttonBack = null;
    private Button   buttonMore = null;

	private TextView textLine1;
	private TextView textLine2;
	private TextView textLine3;
	private TextView textLine4;

	private Button buttonOK;
	private Button buttonCancel;

	private int mPrintTimer = 0;
	private int intSeconds = 0;
	private Timer mTimerSeconds;
	private boolean printPaused = false;

	@Override
	public void handleMessageSafe(Message msg)
	{
		/*Here's how to process information*/
		switch (msg.what)
		{
		case PRINT_PAUSE_TIMER_NOTIFIER:
			if(printPaused == true)
			{
				cancelPrintPauseTimer();
				continuePrintReceipt();
			}
			break;
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_trans_result);
        // title
        textTitle = (TextView)findViewById(R.id.tAppTitle);
		textTitle.setText(appState.getString(TransDefine.transInfo[appState.getTranType()].id_display_en));

	    buttonBack = (Button)findViewById(R.id.btn_back);
        buttonBack.setOnClickListener(new ClickListener());

        buttonMore = (Button)findViewById(R.id.btn_more);
        buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));

        textLine1 = (TextView)findViewById(R.id.tTransResult_Line1);
        textLine2 = (TextView)findViewById(R.id.tTransResult_Line2);
        textLine3 = (TextView)findViewById(R.id.tTransResult_Line3);
        textLine4 = (TextView)findViewById(R.id.tTransResult_Line4);

        buttonOK  = (Button)findViewById(R.id.btn_digit_enter);
        buttonOK.setOnClickListener(new ClickListener());

        buttonCancel  = (Button)findViewById(R.id.btn_digit_cancel);
        buttonCancel.setOnClickListener(new ClickListener());

        mHandler.setFunActivity(this);

        if(appState.getErrorCode() > 0)
        {
        	textLine2.setText(appState.getErrorCode());
        	if(   appState.trans.getCardEntryMode() == INSERT_ENTRY
        	   || appState.trans.getCardEntryMode() == CONTACTLESS_ENTRY
        	  )
        	{
        			textLine3.setText("TVR: " + appState.trans.getTVR());
        			textLine4.setText("TSI: " + appState.trans.getTSI());
        	}

        }
        else
        {
        	if(appState.getTranType() == TRAN_SETTLE)
        	{
        		textLine1.setText("SETTLE FINISH");
        	}
        	else{
	        	if(appState.trans.getCardEntryMode()  == SWIPE_ENTRY)
	        	{
	        		if(   appState.trans.getResponseCode()[0] == '0'
	        		   || appState.trans.getResponseCode()[1] == '0'
	        		  )
	        		{
						textLine1.setText(R.string.response_00);
						if(( TransDefine.transInfo[appState.getTranType()].flag & T_NORECEIPT) == 0)
						{
		        			textLine2.setText("PRINTING...");
		        			appState.printReceipt = 0;
		        			printReceipt();
						}
	        		}
	        		else{
	        			textLine1.setText("DECLINED");
	        		}
	        	}
	        	else{
		        	if(   appState.trans.getEMVRetCode() == APPROVE_OFFLINE
		        	   || appState.trans.getEMVRetCode() == APPROVE_ONLINE
		        	  )
		        	{
						if(appState.trans.getEMVRetCode() == APPROVE_ONLINE)
						{
							textLine1.setText("ACCEPTED ONLINE");
						}
						else{
							textLine1.setText("ACCEPTED OFFLINE");
						}
						if(( TransDefine.transInfo[appState.getTranType()].flag & T_NORECEIPT) == 0)
						{
		        			textLine2.setText("PRINTING...");
		        			appState.printReceipt = 0;
		        			printReceipt();
						}
		    		}
					else
					{
						if(appState.trans.getEMVRetCode() == DECLINE_ONLINE)
						{
							textLine1.setText("DECLINED ONLINE");
						}
						else{
							textLine1.setText("DECLINED OFFLINE");
						}
					}
        			textLine3.setText("TVR: " + appState.trans.getTVR());
        			textLine4.setText("TSI: " + appState.trans.getTSI());
	        	}
        	}
        }
    }

	@Override
	protected void onStart()
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
    public void onBackPressed(){

    }

	public class ClickListener implements View.OnClickListener
    {
		@Override
		public void onClick(View v)
		{
			if(printPaused == true)
			{
				switch(v.getId())
				{
				case R.id.btn_digit_enter:
					// print
					cancelPrintPauseTimer();
					continuePrintReceipt();
					break;
				case R.id.btn_digit_cancel:
					cancelPrintPauseTimer();
					if(appState.needClearPinpad == true)
			        {
			        	// clear pinpad
			        	appState.needClearPinpad = false;
						appState.pinpadService.open();
						appState.pinpadService.setText(0, " ");
						appState.pinpadService.setText(1, " ");
						appState.pinpadService.close();
						appState.pinpadOpened = false;
			        }
					setResult(Activity.RESULT_OK, getIntent());
					exit();
					break;
				}
			}
			else{
				switch(v.getId())
				{
				case R.id.btn_digit_enter:
				case R.id.btn_digit_cancel:
					if(appState.getErrorCode() > 0)
					{
						appState.setErrorCode(0);
					}
					setResult(Activity.RESULT_OK, getIntent());
					exit();
					break;
				}
			}
		}
    }

    private void startPrintPauseTimer(int timerSecond)
    {
    	printPaused = true;
    	mPrintTimer = timerSecond;
    	intSeconds = 0;
            //create timer to tick every second
        mTimerSeconds = new Timer();
        mTimerSeconds.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                intSeconds++;
                if (intSeconds == mPrintTimer)
                {
		    		Message m = new Message();
		    		m.what = PRINT_PAUSE_TIMER_NOTIFIER;
		    		mHandler.sendMessage(m);
                }
            }
        }, 0, 1000);
    }

    private void cancelPrintPauseTimer()
    {
    	printPaused = false;
    	intSeconds = 0;
    	mTimerSeconds.cancel();
    }


    private void continuePrintReceipt()
    {
    	appState.printReceipt++;
    	printReceipt();
    }

    private void printReceipt()
    {
		if (appState.wizarType == WizarTypeUtil.WIZARTYPE.WIZARHAND_Q3
				|| appState.wizarType == WizarTypeUtil.WIZARTYPE.WIZARHAND_Q3F) {
			return;
		}

		try {
			PrinterHelper.getInstance(getBaseContext()).printReceiptWithAAR(appState, appState.printReceipt);
		} catch (PrinterException e) {
			e.printStackTrace();
		}

		if( appState.terminalConfig.getReceipt() > (appState.printReceipt + 1) )
		{
			textLine2.setText("CONTINUE PRINT?");
			startPrintPauseTimer(4);
		}
		else
		{
			textLine2.setText("PRINT_COMPLETED");
		}
    }
}
