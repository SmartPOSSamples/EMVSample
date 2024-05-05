package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;
import com.smartpos.util.StringUtil;

import java.util.ArrayList;

import static com.cloudpos.jniinterface.EMVJNIInterface.emv_set_candidate_list_result;

public class SelectEMVAppListActivity extends FuncActivity
{
	private TextView textTitle  = null;
	private Button   buttonBack = null;
    private Button   buttonMore = null;
    
	private Button buttonCancel = null;
	private Button buttonOK = null;
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
	private Button buttonClear     = null;
	
	private TextView textPrompt = null;
	private TextView textValue = null;
	private ListView mListView;
	private ArrayList<String> items = new ArrayList<String>();
	private ArrayAdapter<String> mAdapter;
	
	private int selectValue = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_select_emv_app_list);
		
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
        
        buttonClear =  (Button)findViewById(R.id.btn_digit_clear);
        buttonClear.setOnClickListener(new ClickListener());
        
        textPrompt = (TextView)findViewById(R.id.tSelectApp_Prompt);
        textValue = (TextView)findViewById(R.id.tSelectApp_Value);
        mListView = (ListView) findViewById(R.id.XSelectApp_View);
  		mListView.setOnItemClickListener(new OnItemClickListener() { 
   		   public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,  long arg3) { 
   			   selectValue = arg2 + 1;
   			   textValue.setText(Integer.toString(selectValue));
   		   } 
   		});
	}

    @Override
    public void onStart()
    {
    	super.onStart();
		Log.i(APP_TAG, "SelectEMVAppListActivity onstart");
		selectValue = -1;
    	textValue.setText("");
    	
        if(appState.trans.getAppSelected() == false)
        {
            textPrompt.setText("PLS select APP");
        }
        else{
        	textPrompt.setText("PLS select again");
        }
        
    	int offset = 0;
    	int textLength = 0;
    	byte[] text = null;
    	for(int i=0; i< appState.aidNumber; i++)
    	{
    		
    		textLength = appState.aidList[offset];
    		text = new byte[textLength];
			Log.i(APP_TAG, StringUtil.toHexString(text, false));
    		System.arraycopy(appState.aidList, offset+1, text, 0, text.length);
    		offset += (1 + textLength);
    		items.add(Integer.toString(i+1) + ". " + StringUtil.toString(text));
    	}
    	
		mAdapter = new ArrayAdapter<String>(this, R.layout.list_item, items);
		mListView.setAdapter(mAdapter);

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
		setResult(Activity.RESULT_CANCELED, getIntent());
		exit();
    }
    
    public void setText(int digital)
    {
    	if(digital <= appState.aidNumber)
    	{
    		textValue.setText(Integer.toString(digital));
    		selectValue = digital;
    	}
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
				break;
			case R.id.btn_digit_1:
				setText(1);
				break;
			case R.id.btn_digit_2:
				setText(2);
				break;
			case R.id.btn_digit_3:
				setText(3);
				break;
			case R.id.btn_digit_4:
				setText(4);
				break;
			case R.id.btn_digit_5:
				setText(5);
				break;
			case R.id.btn_digit_6:
				setText(6);
				break;
			case R.id.btn_digit_7:
				setText(7);
				break;
			case R.id.btn_digit_8:
				setText(8);
				break;
			case R.id.btn_digit_9:
				setText(9);
				break;
			case R.id.btn_digit_clear:
				break;
			case R.id.btn_digit_enter:
				if(selectValue > 0 && selectValue <= appState.aidNumber)
				{
					appState.trans.setAppSelected(true);
					emv_set_candidate_list_result(selectValue - 1);
					setResult(Activity.RESULT_OK, intent);
					exit();
				}
				break;
			case R.id.btn_back:
			case R.id.btn_digit_cancel:
				setResult(Activity.RESULT_CANCELED, intent);
				exit();
				break;			
			}
		}
    }
}
