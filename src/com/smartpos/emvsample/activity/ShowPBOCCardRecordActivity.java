package com.smartpos.emvsample.activity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.smartpos.emvsample.R;
import com.smartpos.util.AppUtil;
import com.smartpos.util.NumberUtil;
import com.smartpos.util.StringUtil;

import java.io.UnsupportedEncodingException;

import static com.cloudpos.jniinterface.EMVJNIInterface.emv_get_card_record;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_get_tag_data;

public class ShowPBOCCardRecordActivity extends FuncActivity
{
	private int currentTransIndex = 0;
	
	private TextView textTitle  = null;
	private Button   buttonBack = null;
    private Button   buttonMore = null;
    
	private TextView textTransType = null; 
	private TextView textLine1 = null;
	private TextView textLine2 = null;
	private TextView textLine3 = null;
	private TextView textLine4 = null;
	private TextView textLine5 = null;
	private TextView textLine6 = null;
	private TextView textLine7 = null;
	private TextView textLine8 = null;
	private Button buttonCancel = null;
	private Button buttonPrev = null;
	private Button buttonNext = null;
	
	private byte[] recordList = new byte[450];
	private int recordNumber = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show_pboc_card_record);
        
        // title
        textTitle = (TextView)findViewById(R.id.tAppTitle);
        if(appState.recordType == 0x00)
        {
        	textTitle.setText("Transaction Log");
        }
        else{
        	textTitle.setText("Transfer Log");
        }
        
	    buttonBack = (Button)findViewById(R.id.btn_back);
        buttonBack.setOnClickListener(new ClickListener());
        
        buttonMore = (Button)findViewById(R.id.btn_more);
        buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));
        
        buttonPrev = (Button)findViewById(R.id.bCardRecord_Prev);
        buttonPrev.setOnClickListener(new ClickListener());
        buttonNext = (Button)findViewById(R.id.bCardRecord_Next);
        buttonNext.setOnClickListener(new ClickListener());
        buttonCancel = (Button)findViewById(R.id.btn_digit_cancel);
        buttonCancel.setOnClickListener(new ClickListener());
        
        textTransType = (TextView)findViewById(R.id.tCardRecord_TransType);
        textLine1 = (TextView)findViewById(R.id.tCardRecord_Line1);
        textLine2 = (TextView)findViewById(R.id.tCardRecord_Line2);
        textLine3 = (TextView)findViewById(R.id.tCardRecord_Line3);
        textLine4 = (TextView)findViewById(R.id.tCardRecord_Line4);
        textLine5 = (TextView)findViewById(R.id.tCardRecord_Line5);
        textLine6 = (TextView)findViewById(R.id.tCardRecord_Line6);
        textLine7 = (TextView)findViewById(R.id.tCardRecord_Line7);
        textLine8 = (TextView)findViewById(R.id.tCardRecord_Line8);
    }

    @Override
    public void onStart()
    {
    	super.onStart();
    	recordNumber = emv_get_card_record(recordList, recordList.length);
    	if(recordNumber == 0)
    	{
    		appState.setErrorCode(R.string.error_no_trans_in_card);
    		finish();
    		return;
    	}
		currentTransIndex = 1;
		if(appState.recordType == 0x00)
		{
			textTransType.setText("Transaction details (" + currentTransIndex + "/" + recordNumber + ")");
		}
		else{
			textTransType.setText("Transfer details (" + currentTransIndex + "/" + recordNumber + ")");
		}
		buttonPrev.setClickable(false);
		if(currentTransIndex == recordNumber)
		{
			buttonNext.setClickable(false);
		}
    	showTrans();
        startIdleTimer(TIMER_IDLE, DEFAULT_IDLE_TIME_SECONDS);
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
    
    private void showTrans()
    {
		if(appState.recordType == 0x00)
		{
	    	byte[] transDate = new byte[3];
			byte[] transTime = new byte[3];
			byte[] authAmount = new byte[6];
			byte[] otherAmount = new byte[6];
			byte[] termCountryCode = new byte[2];
			byte[] transCurrencyCode = new byte[2];
			byte[] merchantName = new byte[20];
			byte transType;
			byte[] atc = new byte[2];
			
			int offset = (currentTransIndex-1)*45;
	  		System.arraycopy(recordList, offset,     transDate,         0,  3);
	   		System.arraycopy(recordList, offset + 3, transTime,         0,  3);
	   		System.arraycopy(recordList, offset + 6, authAmount,        0,  6);
		    System.arraycopy(recordList, offset +12, otherAmount,       0,  6);
		    System.arraycopy(recordList, offset +18, termCountryCode,   0,  2);
		    System.arraycopy(recordList, offset +20, transCurrencyCode, 0,  2);
		    System.arraycopy(recordList, offset +22, merchantName,      0, 20);
	   		transType = recordList[offset+42];
	   		System.arraycopy(recordList, offset +43, atc,               0,  2);
	
	    	textLine1.setText("20" + StringUtil.toHexString(transDate,false).substring(0,2)
	    			          + "/" + StringUtil.toHexString(transDate,false).substring(2,4)
	    			          + "/" + StringUtil.toHexString(transDate,false).substring(4,6)
	    			          + " " + StringUtil.toHexString(transTime,false).substring(0,2)
	    			          + ":" + StringUtil.toHexString(transTime,false).substring(2,4)
	    			          + ":" + StringUtil.toHexString(transTime,false).substring(4,6)
	    			         );
	    	textLine2.setText("Authorized amount:" + AppUtil.formatAmount(Long.parseLong(StringUtil.toHexString(authAmount, false))));
	    	textLine3.setText("Other amount:" + AppUtil.formatAmount(AppUtil.toAmount(otherAmount)));
	    	textLine4.setText("Terminal country code:" + StringUtil.toHexString(termCountryCode,false));
	    	textLine5.setText("Transaction currency code:" + StringUtil.toHexString(transCurrencyCode,false));
	    	try {
				textLine6.setText("Business Name:" + new String(merchantName, "GB2312").trim());
			} catch (UnsupportedEncodingException e) {
			}
	    	textLine7.setText("Transaction Type:" + StringUtil.fillZero(Byte.toString(transType),2));
	    	// textLine8.setText("ATC:" + StringUtil.toHexString(atc,false));
	    	textLine8.setText("ATC:" + Short.toString(NumberUtil.byte2ToShort(atc)));
		}
		else
		{
			int offset = (currentTransIndex-1)*45;
			byte[] p1 = {recordList[offset]};
			byte[] p2 = {recordList[offset + 1]};
			textLine1.setText("P1:" + StringUtil.toHexString(p1, false) + "   P2:" + StringUtil.toHexString(p2, false));
			
			byte[] preAmount = new byte[6];
			System.arraycopy(recordList, offset + 2, preAmount, 0, 6);
			textLine2.setText("Balance before transfer:" + AppUtil.formatAmount(Long.parseLong(StringUtil.toHexString(preAmount, false))));
			
			byte[] aftAmount = new byte[6];
			System.arraycopy(recordList, offset + 8, aftAmount, 0, 6);
			textLine3.setText("Balance after transfer:" + AppUtil.formatAmount(Long.parseLong(StringUtil.toHexString(aftAmount, false))));
			
			offset += 14;
			byte[] tagData = new byte[256];
			int tagDataLength = 0;
			tagDataLength = emv_get_tag_data(0xDF4F, tagData, tagData.length);
			if(tagDataLength > 0)
			{
				byte[] transDate = null;
				byte[] transTime = null;
				byte[] tcc = null;
				byte[] merchantName= null;
				byte[] atc = null;
				
				for( int i=0; i<tagDataLength;)
				{
					if((byte)(tagData[i] & 0xff) == (byte)0x9A)
					{
						// date
						transDate = new byte[tagData[i+1]];
						System.arraycopy(recordList, offset, transDate, 0, transDate.length);
						offset += transDate.length;
						i += 2;
					}
					else if(   (byte)(tagData[i]   & 0xff) == (byte)0x9F 
							&& (byte)(tagData[i+1] & 0xff) == (byte)0x21
						   )
					{
						// time
						transTime = new byte[tagData[i+2]];
						System.arraycopy(recordList, offset, transTime, 0, transTime.length);
						offset += transTime.length;
						i += 3;
					}
					else if(   (byte)(tagData[i]   & 0xff) == (byte)0x9F 
							&& (byte)(tagData[i+1] & 0xff) == (byte)0x1A
						   )
					{
						// tcc
						tcc = new byte[tagData[i+2]];
						System.arraycopy(recordList, offset, tcc, 0, tcc.length);
						offset += tcc.length;
						i += 3;
					}
					else if(   (byte)(tagData[i]   & 0xff) == (byte)0x9F 
							&& (byte)(tagData[i+1] & 0xff) == (byte)0x4E
						   )
					{
						// merchant name
						merchantName = new byte[tagData[i+2]];
						System.arraycopy(recordList, offset, merchantName, 0, merchantName.length);
						offset += merchantName.length;
						i += 3;
					}
					else if(   (byte)(tagData[i]   & 0xff) == (byte)0x9F 
							&& (byte)(tagData[i+1] & 0xff) == (byte)0x36
						   )
					{
						// atc
						atc = new byte[tagData[i+2]];
						System.arraycopy(recordList, offset, atc, 0, atc.length);
						offset += atc.length;
						i += 3;
					}	
				}
				if(transDate != null && transTime != null)
				{
					textLine4.setText("20" + StringUtil.toHexString(transDate,false).substring(0,2)
	  			          + "/" + StringUtil.toHexString(transDate,false).substring(2,4)
	  			          + "/" + StringUtil.toHexString(transDate,false).substring(4,6)
	  			          + " " + StringUtil.toHexString(transTime,false).substring(0,2)
	  			          + ":" + StringUtil.toHexString(transTime,false).substring(2,4)
	  			          + ":" + StringUtil.toHexString(transTime,false).substring(4,6)
	  			         );
				}
				if(tcc != null)
				{
					textLine5.setText("Terminal country code:" + StringUtil.toHexString(tcc,false));
				}
				if(merchantName != null)
				{
			    	try {
						textLine6.setText("Business Name:" + new String(merchantName, "GB2312").trim());
					} catch (UnsupportedEncodingException e) {
					}
				}
				if(atc != null)
				{
			    	textLine7.setText("ATC:" + Short.toString(NumberUtil.byte2ToShort(atc)));
				}
			}
		}
    }
    
	public class ClickListener implements View.OnClickListener
    {
		@Override
		public void onClick(View v) 
		{
			onTouch();
			switch(v.getId())
			{
			case R.id.bCardRecord_Prev:
				currentTransIndex --;
				if(appState.recordType == 0x00)
				{
					textTransType.setText("Transaction details (" + currentTransIndex + "/" + recordNumber + ")");
				}
				else{
					textTransType.setText("Transfer details (" + currentTransIndex + "/" + recordNumber + ")");
				}
				if(currentTransIndex ==  1)
				{
					buttonPrev.setClickable(false);
				}
				if(currentTransIndex == (recordNumber - 1))
				{
					buttonNext.setClickable(true);
				}
				showTrans();
				break;
			case R.id.bCardRecord_Next:
				currentTransIndex ++;
				if(appState.recordType == 0x00)
				{
					textTransType.setText("Transaction details (" + currentTransIndex + "/" + recordNumber + ")");
				}
				else{
					textTransType.setText("Transer details (" + currentTransIndex + "/" + recordNumber + ")");
				}
				if(currentTransIndex == recordNumber)
				{
					buttonNext.setClickable(false);
				}
				if(currentTransIndex ==  2)
				{
					buttonPrev.setClickable(true);
				}
				showTrans();
				break;
			case R.id.btn_back:
			case R.id.btn_digit_cancel:
				finish();
				break;	
			}
		}
    }
}
