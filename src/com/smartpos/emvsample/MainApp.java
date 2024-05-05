package com.smartpos.emvsample;

import static com.cloudpos.jniinterface.EMVJNIInterface.emv_aidparam_add;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_aidparam_clear;
import static com.cloudpos.jniinterface.EMVJNIInterface.emv_contactless_aidparam_clear;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.smartpos.emvsample.cloudposInterface.ContactCardImpl;
import com.smartpos.emvsample.cloudposInterface.ContactlessCardImpl;
import com.smartpos.emvsample.cloudposInterface.MSRImpl;
import com.smartpos.emvsample.cloudposInterface.PinpadImpl;
import com.smartpos.emvsample.constant.EMVConstant;
import com.smartpos.emvsample.db.AIDService;
import com.smartpos.emvsample.db.AIDTable;
import com.smartpos.emvsample.db.AdviceService;
import com.smartpos.emvsample.db.CAPKService;
import com.smartpos.emvsample.db.CAPKTable;
import com.smartpos.emvsample.db.DatabaseOpenHelper;
import com.smartpos.emvsample.db.ExceptionFileService;
import com.smartpos.emvsample.db.ExceptionFileTable;
import com.smartpos.emvsample.db.RevokedCAPKService;
import com.smartpos.emvsample.db.RevokedCAPKTable;
import com.smartpos.emvsample.db.TransDetailInfo;
import com.smartpos.emvsample.db.TransDetailService;
import com.smartpos.emvsample.parameter.BatchInfo;
import com.smartpos.emvsample.parameter.TerminalConfig;
import com.smartpos.util.WizarTypeUtil;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainApp extends Application implements EMVConstant
{
    private byte tranType = TRAN_GOODS;
    private byte paramType = -1;   // 参数设置类型
	private byte processState = 0;  // 处理阶段
	private byte state = 0;         // 
    private int  errorCode = 0;
    private byte commState = COMM_DISCONNECTED;
    private SharedPreferences terminalPref; 
    private SharedPreferences batchPref;  
    private Calendar mCalendar;

    //模块调用
	public static PinpadImpl pinpadService;
	public static ContactCardImpl contactService;
	public static ContactlessCardImpl contactlessService;
	public static MSRImpl msrService;
    
    private static MainApp _instance;
    public static final boolean ContactlessRetry = false;

	public DatabaseOpenHelper dbOpenHelper = null;
	public SQLiteDatabase db = null;
	
	public TransDetailInfo trans = new TransDetailInfo();
	public boolean needCard = false;
	public boolean enableContactlessCard = false;
	public boolean promptCardCanRemoved = false;
	public boolean promptOfflineDataAuthSucc = false;
	public boolean resetCardError = false;
	
	public int cardType = -1;
	public boolean msrError = false;
	
	public boolean acceptMSR = true;
	public boolean acceptContactCard = true;
	public boolean acceptContactlessCard = true;
	public boolean promptCardIC = false;
	
	public byte recordType = 0x00;
	public BatchInfo batchInfo;
	public TerminalConfig terminalConfig;
	
	public boolean emvParamLoadFlag = false;
	public boolean emvParamChanged = false;
	public boolean need2Tap = false;
	
	public TransDetailService transDetailService;
	public AdviceService adviceService;
	public AIDService aidService;
	public CAPKService capkService;
	public RevokedCAPKService revokedCAPKService;
	public ExceptionFileService exceptionFileService;
	public int aidNumber = 0;
	public byte[] aidList = new byte[300];
	public byte pollCardState = 0;
	
	public AIDTable[] aids;
	public int aidsIndex = 0;
	public boolean aidsInfoChanged = false;
	
	public CAPKTable[] capks;
	public int capksIndex = 0;
	public boolean capkInfoChanged = false;
	public String failedCAPKInfo = "";
	
	public ExceptionFileTable[] exceptionFiles;
	public int exceptionFilesIndex = 0;
	public boolean exceptionFileInfoChanged = false;
	
	public RevokedCAPKTable[] revokedCapks;
	public int revokedCapksIndex = 0;
	public boolean revokedCapkInfoChanged = false;
	
	public int currentYear; 
	public int currentMonth; 
	public int currentDay; 
	public int currentHour; 
	public int currentMinute; 
	public int currentSecond;
	
	public int printReceipt = 0;
	// 读卡设备信息
	public boolean icInitFlag = false;       // IC卡是否已初始化
	public boolean idleFlag = false;
	// 密码键盘
	public boolean pinpadOpened = false;
	public boolean needClearPinpad = false;
	public boolean getPanBeforeAmount = false;

	public int pinpadType = PINPAD_SYSTEM_UI; // PINPAD_CUSTOM_UI

	//anti shake
	public volatile int msrPollResult = -1;

	public WizarTypeUtil.WIZARTYPE wizarType;

	public ExecutorService threadPool = Executors.newCachedThreadPool();

    public static MainApp getInstance()
    {
		if (null == _instance)
		    _instance = new MainApp();
		return _instance;
    }

    @Override
    public void onCreate()
    {
		super.onCreate();
		if (null == _instance)
		    _instance = MainApp.this;
		
		loadData();
		initData();

		wizarType = WizarTypeUtil.getWizarType();

		//显示上下导航栏 全系统有效
//		try {
//			Object service = getSystemService("statusbar");
//			Class statusBarManager = null;
//			statusBarManager = Class.forName("android.app.StatusBarManager");
//			Method method = statusBarManager.getMethod("hideBars", int.class);
//			method.invoke(service,0);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}


	}
    
    private void loadData()
    {
    	dbOpenHelper = new DatabaseOpenHelper(getBaseContext());
		db = dbOpenHelper.getWritableDatabase();
		
    	terminalPref  = getSharedPreferences("terminalConfig", Context.MODE_PRIVATE); 
		terminalConfig = new TerminalConfig(terminalPref);
		
	    batchPref     = getSharedPreferences("batchInfo", Context.MODE_PRIVATE);  
	    batchInfo = new BatchInfo(batchPref);
		
		transDetailService = new TransDetailService(getBaseContext());
		adviceService = new AdviceService(getBaseContext());
		aidService = new AIDService(db);
		capkService = new CAPKService(db);
		revokedCAPKService = new RevokedCAPKService(db);
		exceptionFileService = new ExceptionFileService(db);
		
		terminalConfig.loadTerminalConfig();
		batchInfo.loadBatch();

		//模块初始化
		pinpadService = PinpadImpl.getInstance(getBaseContext());
		contactService = ContactCardImpl.getInstance(getBaseContext());
		contactlessService = ContactlessCardImpl.getInstance(getBaseContext());
		msrService = MSRImpl.getInstance(getBaseContext());

		if(aidService.getAIDCount() == 0)
		{
		//	dbOpenHelper.clearTable(db, DatabaseOpenHelper.TABLE_AID);
			aidService.createDefaultAID();
		}
		
		if(capkService.getCAPKCount() == 0)
		{
		//	dbOpenHelper.clearTable(db, DatabaseOpenHelper.TABLE_CAPK);
			capkService.createDefaultCAPK();
		}

		setPinpadUI();
    }

    public void initData()
    {
    	tranType = TRAN_GOODS;    // 交易类型
    	paramType = -1;
    	processState = 0;  // 处理阶段
    	state = 0;         // 
        errorCode = 0;
        cardType = -1;
        idleFlag = false;
        promptCardCanRemoved = false;
        promptOfflineDataAuthSucc = false;
        printReceipt = 0;
        resetCardError = false;
		msrPollResult = -1;
	    getPanBeforeAmount = false;
		need2Tap = false;
		trans.init();
        trans.setTrace(terminalConfig.getTrace());
    }
    
    // tranType
    public byte getTranType()
    {
    	return tranType;
    }
    
    public void setTranType(byte tranType)
    {
    	this.tranType = tranType;
    }
    
    // paramType
    public byte getParamType()
    {
    	return paramType;
    }
    
    public void setParamType(byte paramState)
    {
    	this.paramType = paramState;
    }
    
    // processState
    public byte getProcessState()
    {
    	return processState;
    }
    
    public void setProcessState(byte processState)
    {
    	this.processState = processState;
    }
    
    // state
    public byte getState()
    {
    	return state;
    }
    
    public void setState(byte state)
    {
    	this.state = state;
    }
    
    // errorCode
    public int getErrorCode()
    {
    	if(debug)Log.d(APP_TAG, "getErrorCode = " + errorCode  );
    	return errorCode;
    }
    
    public void setErrorCode(int errorCode)
    {
    	if(debug)Log.d(APP_TAG, "setErrorCode = " + errorCode);
    	this.errorCode = errorCode;
    }
    
    // commState
    public byte getCommState()
    {
    	return commState;
    }
    
    public void setCommState(byte state)
    {
    	commState = state;
    }
    
    public void getCurrentDateTime()
    {
		long time = System.currentTimeMillis(); 
		/*透过Calendar对象来取得小时与分钟*/ 
		mCalendar = Calendar.getInstance(); 
		mCalendar.setTimeInMillis(time); 
		currentYear = mCalendar.get(Calendar.YEAR);
		currentMonth = mCalendar.get(Calendar.MONTH)+1;
		currentDay = mCalendar.get(Calendar.DAY_OF_MONTH);
		currentHour = mCalendar.get(Calendar.HOUR); 
		if(mCalendar.get(Calendar.AM_PM) == Calendar.PM)
		{
			currentHour += 12;
		}
		currentMinute = mCalendar.get(Calendar.MINUTE);
		currentSecond = mCalendar.get(Calendar.SECOND);
    }

    private void setPinpadUI()
    {
    	if(pinpadService != null){
		    if(pinpadService.open()){
			    pinpadService.setGUIConfiguration("sound", "true");
			    pinpadService.close();
		    }
	    }
    }

	public static void loadAIDDefault()
	{
		byte[] buf = null;

		emv_aidparam_clear();
		emv_contactless_aidparam_clear();

		// Contact AID
		AIDTable aidTable = new AIDTable();
		//01 VISA - A0000000031010
		aidTable.setAid("A0000000031010");
		aidTable.setAppPriority((byte)0);
		aidTable.setTermFloorLimit(0);
		aidTable.setTACDefault("D84000A800");
		aidTable.setTACDenial( "0010000000");
		aidTable.setTACOnline( "D84004F800");
		aidTable.setThresholdValue(0);
		aidTable.setMaxTargetPercentage((byte)99);
		aidTable.setTargetPercentage((byte)99);
		aidTable.setAppVersionNumber("0140");
		aidTable.setTransReferCurrencyCode("0840");
		aidTable.setTransReferCurrencyExponent((byte)2);
		aidTable.setDefaultDDOL("9F3704");
		aidTable.setNeedCompleteMatching((byte)0);
		aidTable.setSupportOnlinePin((byte)1);
		aidTable.setTermRiskManageData("");
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );

		//02 VISA - A0000000032010
		aidTable.setAid("A0000000032010");
		aidTable.setSupportOnlinePin((byte)0);
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );

		//03 VISA - A0000000033010
		aidTable.setAid("A0000000033010");
		aidTable.setSupportOnlinePin((byte)0);
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );

		//04 VISA CSS - A0000007421010
		aidTable.setAid("A0000007421010");
		aidTable.setSupportOnlinePin((byte)1);
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );

		//05 MasterCard - A0000000041010
		aidTable.setAid("A0000000041010");
		aidTable.setTACDefault("FC5080A000");
		aidTable.setTACDenial( "0400000000");
		aidTable.setTACOnline( "F85080F800");
		aidTable.setAppVersionNumber("0002");
		aidTable.setSupportOnlinePin((byte)1);
		aidTable.setTermRiskManageData(DefaultTermRiskManageData);
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );

		//06 MasterCard - A0000000043060
		aidTable.setAid("A0000000043060");
		aidTable.setTermRiskManageData(DefaultTermRiskManageData);
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );

		//07 JCB - A0000000651010
		aidTable.setTermRiskManageData("");
		aidTable.setAid("A0000000651010");
		aidTable.setTACDefault("FC6024A800");
		aidTable.setTACDenial( "0010000000");
		aidTable.setTACOnline( "FC60ACF800");
		aidTable.setAppVersionNumber("0200");
		aidTable.setSupportOnlinePin((byte)1);
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );

		//08 CUP - A000000333010101
		aidTable.setAid("A000000333010101");
		aidTable.setTACDefault("D84000A800");
		aidTable.setTACDenial( "0010000000");
		aidTable.setTACOnline( "D84004F800");
		aidTable.setAppVersionNumber("0020");
		aidTable.setSupportOnlinePin((byte)1);
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );

		//09 CUP - A000000333010102
		aidTable.setAid("A000000333010102");
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );

		//10 CUP - A000000333010103
		aidTable.setAid("A000000333010103");
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );

		//11 CUP - A000000333010106
		aidTable.setAid("A000000333010106");
		buf = aidTable.getDataBuffer();
		emv_aidparam_add(buf, buf.length );
	}
}
