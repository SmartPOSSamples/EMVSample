package com.smartpos.emvsample.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.transaction.TransDefine;

public class RemoveCardActivity extends FuncActivity
{
	private TextView textTitle  = null;
	private Button   buttonBack = null;
    private Button   buttonMore = null;
    private Thread   mWaitCardRemoveThread = null;
    private boolean  cardRemoveThreadRun = false;

    @Override
	public void handleMessageSafe(Message msg)
	{
		/*这里是处理信息的方法*/
		switch (msg.what)
		{
		case CARD_REMOVE_NOTIFIER:
			setResult(Activity.RESULT_OK, getIntent());
			finish();
			break;
		}
	}

    @Override
    public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_remove_card);
        // title
        textTitle = (TextView)findViewById(R.id.tAppTitle);
		textTitle.setText(appState.getString(TransDefine.transInfo[appState.getTranType()].id_display_en));
		
	    buttonBack = (Button)findViewById(R.id.btn_back);
        buttonBack.setOnClickListener(new ClickListener());
        
        buttonMore = (Button)findViewById(R.id.btn_more);
        buttonMore.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_blank));

        mHandler.setFunActivity(this);
    }

    @Override 
    protected void onStart() { 
        super.onStart();
		mWaitCardRemoveThread=new WaitCardRemoveThread(); 
		mWaitCardRemoveThread.start();
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
    public void onBackPressed(){
		cardRemoveThreadRun = false;
		appState.cardType = -1;
		setResult(Activity.RESULT_OK, getIntent());
		finish();
    }

	@Override
	protected void onEnter()
	{
		onBackPressed();
	}

	@Override
	protected void onBack()
	{
		onBackPressed();
	}

	@Override
	protected void onCancel()
	{
		onBackPressed();
	}

	class WaitCardRemoveThread extends Thread
    {
    	public void run() 
    	{ 
    		super.run();
    		cardRemoveThreadRun = true;
    		while(appState.cardType != -1)
    		{
    			try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
				}
    			if(cardRemoveThreadRun == false)
    			{
    				return;
    			}
    		}
        	Message msg = new Message();
        	msg.what = CARD_REMOVE_NOTIFIER;
        	mHandler.sendMessage(msg);
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
				cardRemoveThreadRun = false;
				appState.cardType = -1;
				setResult(Activity.RESULT_OK, getIntent());
	   			finish();
				break;
			}
		}
    }
}
