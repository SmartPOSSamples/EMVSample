package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;
import com.smartpos.util.AppUtil;
import com.smartpos.util.StringUtil;

import static com.cloudpos.jniinterface.EMVJNIInterface.emv_anti_shake_finish;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_fastest_qpboc_process;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_force_online;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_other_amount;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_tag_data;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_trans_amount;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_trans_type;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_trans_initialize;

public class RequestCardActivity extends FuncActivity
{
    private static final String TAG = "RequestCardActivity";
	private TextView textTitle  = null;
	private Button   buttonBack = null;
    private Button   buttonMore = null;
	private Button   buttonSwipeCard = null;
    
	private TextView txtTransType = null;
	private TextView txtPrompt = null;
	private TextView txtError = null;
	private TextView txtAmount = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_request_card);
        // title
        textTitle = (TextView)findViewById(R.id.tAppTitle);
		textTitle.setText(appState.getString(TransDefine.transInfo[appState.getTranType()].id_display_en));
		
	    buttonBack = (Button)findViewById(R.id.btn_back);
        buttonBack.setOnClickListener(new ClickListener());
        
        buttonMore = (Button)findViewById(R.id.btn_more);
        buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));
        
        txtTransType = (TextView)findViewById(R.id.tRequestCard_TransType);
        txtError = (TextView)findViewById(R.id.tRequestCard_Error);
        txtAmount = (TextView)findViewById(R.id.tRequestCard_Amount);
        txtPrompt = (TextView)findViewById(R.id.tRequestCard_Prompt);
        
        if(appState.resetCardError == true)
        {
        	txtError.setText("IC POWERON ERROR");
        }
        else if(appState.trans.getEmvCardError() == true)
        {
        	txtError.setText("TRANS HALTED");
        	if(appState.trans.getTransAmount() > 0)
        	{
        		txtAmount.setText("AMOUNT: " + AppUtil.formatAmount(appState.trans.getTransAmount()));
        	}
        }

		buttonSwipeCard = (Button)findViewById(R.id.bSwipeCard);
		if(additionalMag && appState.acceptMSR)
		{
			buttonSwipeCard.setOnClickListener(new ClickListener());
		}
		else{
			buttonSwipeCard.setVisibility(View.GONE);
		}

    }
    @Override
	public void handleMessageSafe(Message msg)
	{
		/*Here's how to process information*/
		switch (msg.what)
		{
		case MSR_READ_DATA_NOTIFIER:
//			if(   appState.trans.getServiceCode().length() > 0
//				&& (   appState.trans.getServiceCode().getBytes()[0] == '2'
//				    || appState.trans.getServiceCode().getBytes()[0] == '6'
//			       )
//			  )
//			{
//				if(appState.trans.getEmvCardError() == false)
//				{
//					startMSR();
//					appState.promptCardIC = true;
//					Toast.makeText(this, "Please Insert/Tap Card", Toast.LENGTH_SHORT).show();
//				}
//				else{
//					cancelAllCard();
//					setResult(Activity.RESULT_OK, getIntent());
//					finish();
//				}
//			}
//			else{
				if(   appState.trans.getServiceCode().length() > 0
					&& appState.trans.getServiceCode().getBytes()[0] == '1'
					)
				{
					appState.trans.setEmvCardError(false);
					appState.trans.setPanViaMSR(true);
				}
				else{
					appState.trans.setEmvCardError(false);
					appState.trans.setPanViaMSR(false);
				}
				cancelAllCard();
				setResult(Activity.RESULT_OK, getIntent());
				finish();
//			}
			break;
		case MSR_OPEN_ERROR_NOTIFIER:
			appState.msrError = true;
			appState.acceptMSR = false;
			txtPrompt.setText(appState.getString(R.string.insert_card));
			break;
		case MSR_READ_ERROR_NOTIFIER:
			readAllCard();
			break;
		case CARD_INSERT_NOTIFIER:
			emv_anti_shake_finish(1);
			Bundle bundle = msg.getData();
			int nEventID = bundle.getInt("nEventID");
			int nSlotIndex = bundle.getInt("nSlotIndex");
			if(debug)Log.d(APP_TAG, "get CONTACT_CARD_EVENT_NOTIFIER,event[" + nEventID + "]slot[" + nSlotIndex + "]" );
			if(   nSlotIndex == 0
				&& nEventID == SMART_CARD_EVENT_INSERT_CARD
				)
			{
				appState.trans.setEmvCardError(false);
				if(appState.acceptContactlessCard == true)
				{
					cancelContactlessCard();
				}
				appState.trans.setCardEntryMode(INSERT_ENTRY);
				setResult(Activity.RESULT_OK, getIntent());
				exit();
			}
			break;
		case CARD_TAPED_NOTIFIER:
			bundle = msg.getData();
			nEventID = bundle.getInt("nEventID");
			if(nEventID == SMART_CARD_EVENT_INSERT_CARD)
			{
				cancelContactCard();
				cancelMSRThread();
				appState.trans.setCardEntryMode(CONTACTLESS_ENTRY);
				if(debug)Log.d(APP_TAG, "get CONTACTLESS_CARD_EVENT_NOTIFIER" );
				setResult(Activity.RESULT_OK, getIntent());
				exit();
			}
			break;
		case CONTACTLESS_HAVE_MORE_CARD_NOTIFIER:
			if(debug)Log.d(APP_TAG, "error, have more card" );
			appState.setErrorCode(R.string.error_more_card);
			setResult(Activity.RESULT_OK, getIntent());
			exit();
			break;
		case CARD_ERROR_NOTIFIER:
			txtError.setText("IC POWERON ERROR");
			txtPrompt.setText("PLEASE INSERT CARD");
			appState.trans.setEmvCardError(true);
			break;
		case CARD_CONTACTLESS_ANTISHAKE:
			new Thread(new Runnable() {
				@Override
				public void run() {
					try
					{
						Thread.sleep(TIMEOUT_ANTI_SHAKE);
					} catch (InterruptedException e)
					{
						e.printStackTrace();
					}
					Log.i(TAG, "anti shake finish");
					if(appState.msrPollResult == -1)
					{
						emv_anti_shake_finish(0);
					}
					else
					{
						emv_anti_shake_finish(1);
					}
				}
			}).start();
			break;
		case EMV_PROCESS_NEXT_COMPLETED_NOTIFIER:
			if (debug) Log.d(APP_TAG, "In RequestCard, get EMV_PROCESS_NEXT_COMPLETED_NOTIFIER");
			if (fastestContactless) {
				appState.cardType = CARD_CONTACTLESS;
				appState.trans.setCardEntryMode(CONTACTLESS_ENTRY);
				setResult(Activity.RESULT_OK, getIntent());
				exit();
			}
			break;
		}
	}

	@Override
    protected void onStart() { 
        super.onStart();
        mHandler.setFunActivity(this);

        txtTransType.setText(TransDefine.transInfo[appState.getTranType()].id_display_en);
   		setPrompt();
	    if(fastestContactless)
	    {
		    emv_trans_initialize();
		    emv_set_fastest_qpboc_process(fastestContactless?1:0);
		    emv_set_force_online(appState.terminalConfig.getforceOnline());
		    setEMVTransAmount(Integer.toString(appState.trans.getTransAmount()));
		    if(appState.getTranType() == QUERY_CARD_RECORD)
		    {
			    emv_set_trans_amount(new byte[]{'0', 0x00});
			    emv_set_other_amount(new byte[]{'0', 0x00});
			    if(appState.recordType == 0x00)
			    {
				    emv_set_trans_type(EMV_TRANS_CARD_RECORD);
			    }
			    else
			    {
				    emv_set_trans_type(EMV_TRANS_LOAD_RECORD);
			    }
		    }
		    else{
			    emv_set_tag_data(0x9A,   StringUtil.hexString2bytes(appState.trans.getTransDate().substring(2)), 3);
			    emv_set_tag_data(0x9F21, StringUtil.hexString2bytes(appState.trans.getTransTime()), 3);
			    emv_set_tag_data(0x9F41, StringUtil.hexString2bytes(StringUtil.fillZero(Integer.toString(appState.trans.getTrace()), 8)), 4);

			    emv_set_trans_type(EMV_TRANS_GOODS_SERVICE);
		    }
	    }
        readAllCard();
        super.startIdleTimer(TIMER_FINISH, DEFAULT_IDLE_TIME_SECONDS);
	}

    private void setPrompt()
    {
		if(additionalMag)
		{
			if(appState.acceptContactCard && appState.acceptContactlessCard){
				txtPrompt.setText("PLEASE INSERT OR TAP YOUR CARD");
			}
			else if(appState.acceptContactCard)
			{
				txtPrompt.setText("PLEASE INSERT CARD");
			}
			else if(appState.acceptContactlessCard)
			{
				txtPrompt.setText("PLEASE TAP CARD");
			}
		}
		else{
			txtPrompt.setText("PLEASE USE YOUR CARD");
		}
    }
	
    @Override
    protected void onPause()
    {
        super.onPause();
    }
    
    @Override
    protected void onStop()
    {
    	//cancelAllCard();
    	super.onStop();
    }
    
    @Override
    public void onBackPressed()
    {
    	cancelAllCard();
    	setResult(Activity.RESULT_CANCELED, getIntent());
    	finish();
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch(requestCode)
		{
		case STATE_REQUEST_CARD_ERROR:
			if(resultCode == Activity.RESULT_OK)
			{
				setResult(Activity.RESULT_OK, getIntent());
			}
			else
			{
				setResult(Activity.RESULT_CANCELED, getIntent());
			}
			finish();
			break;
		default:
			break;
		}
	}
	
	public class ClickListener implements View.OnClickListener
    {
		@Override
		public void onClick(View v) 
		{
			switch(v.getId())
			{
			case R.id.btn_back:
		    	cancelAllCard();
		    	setResult(Activity.RESULT_CANCELED, getIntent());
		    	finish();
				break;
			case R.id.bSwipeCard:
				buttonSwipeCard.setVisibility(View.GONE);
				txtPrompt.setText("PLEASE SWIPE CARD");
				cancelContactCard();
				cancelContactlessCard();
				startMSR();
				break;
			}
		}
    }
}
