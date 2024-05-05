package com.smartpos.emvsample.constant;

public interface EMVConstant
{
	final boolean debug = true;
	final boolean enableLED = true;
	final boolean enableBeep = true;
	final boolean enableEmvLog = true;
//	final boolean enableSignal = true;

	final boolean enableAntiShake = false;
	final boolean fastestContactless = false;
	final boolean forcePIN = true;
	final boolean additionalMag = true;

	final boolean offlineDemo = true;

	// Communication state
	byte COMM_CLOSED = 0x00;
	byte COMM_OPENED = 0x01;

	/**
	 * 0 - Disable log output
	 * 1 - Disable sensitive log output
	 * 2 - Full log
	 * */
	final int emvLogLevel = 2;

	final String APP_TAG = "emvsample";
	final String APP_VERSION = "000001";
	
	final int MAX_CAPK = 40;
	final int MAX_AID = 20;
	
	final byte ONLINE_FAIL    = -1;
	final byte ONLINE_DENIAL  =  0;
	final byte ONLINE_SUCCESS =  1;
	
	final int  DEFAULT_IDLE_TIME_SECONDS = 60;
	final byte[] DEFAULT_KEY = {(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11,(byte)0x11};
	final byte MAX_AMOUNT_LENGTH = 13;
	final int IDLE_TIME_SECONDS = 60;

	final int TIMEOUT_ANTI_SHAKE = 5000;  // Unit: ms; MAX value: 2S - 2000

	final String DefaultTermRiskManageData   = "647E000000000000";
	
	/*-----  TRANSACTION TYPES  ---------------------------------*/
	final byte TRAN_GOODS                     =  0;
	final byte TRAN_SETTLE                    =  1;
	final byte TRAN_BALANCE                   =  2;
	// offline
	final byte QUERY_CARD_RECORD              =  3;
	
	final byte QUERY_SPECIFIC                 =  4;
	final byte QUERY_TRANS_DETAIL             =  5;

	
	// EMV TRANS
	final static byte EMV_TRANS_GOODS_SERVICE = 0x00;
	final static byte EMV_TRANS_CASH          = 0x01;
	final static byte EMV_TRANS_PRE_AUTH      = 0x03;
	final static byte EMV_TRANS_INQUIRY       = 0x04;
	final static byte EMV_TRANS_TRANSFER      = 0x05;
	final static byte EMV_TRANS_PAYMENT       = 0x06;
	final static byte EMV_TRANS_ADMIN         = 0x07;
	final static byte EMV_TRANS_CASHBACK      = 0x09;
	final static byte EMV_TRANS_CARD_RECORD   = 0x0A;
	final static byte EMV_TRANS_EC_BALANCE    = 0x0B;
	final static byte EMV_TRANS_LOAD_RECORD   = 0x0C;
	final static byte EMV_TRANS_BALANCE       = 0x31;
	
	final byte T_NOCAPTURE      = 0x02;
	final byte T_NORECEIPT      = 0x04;
	final byte T_OFFLINE        = 0x08;
	
	/*-----  parameter set TYPES  ---------------------------------*/
	final byte PARAM_TID                = 1;
	final byte PARAM_MID                = 2;
	final byte PARAM_BATCH              = 3; // 设置批次号
	final byte PARAM_TRACE              = 4; // 设置流水号(凭证号)
    final byte PARAM_COMM_PRIMARY_IP    = 5;
    final byte PARAM_COMM_PRIMARY_PORT  = 6;
    final byte PARAM_MERCHANT_NAME      = 7;
    final byte PARAM_UPLOAD_TYPE        = 8;
    final byte PARAM_FORCE_ONLINE       = 9;
    final byte PARAM_RECEIPT            =10;
    // EMV
    final byte PARAM_COUNTRYCODE                      = 11;
    final byte PARAM_IFD                              = 12;
    final byte PARAM_CURRENCY_CODE                    = 13;
    final byte PARAM_CURRENCY_EXPONENT                = 14;
    final byte PARAM_TERMINAL_TYPE                    = 15;
    final byte PARAM_TERMINAL_CAPABILITIES            = 16;
    final byte PARAM_ADDITIONAL_TERMINAL_CAPABILITIES = 17;
    // QPBOC
    final byte PARAM_TTQ                              = 18;
    final byte PARAM_STATUS_CHECK                     = 19;
    // limit
    final byte PARAM_EC_TERM_TRANS_LIMIT              = 20;
    final byte PARAM_CONTACTLESS_LIMIT                = 21;
    final byte PARAM_CONTACTLESS_FLOOR_LIMIT          = 22;
    final byte PARAM_CVM_LIMIT                        = 23;
    
	// ProcessState
	final byte PROCESS_NORMAL         = 0;
	final byte PROCESS_CAPTURE_ONLINE = 1;
	final byte PROCESS_CONFIMATION    = 2;
	final byte PROCESS_REVERSAL       = 3;
	final byte PROCESS_ADVICE_ONLINE  = 4;
	final byte PROCESS_BATCH          = 5;
	final byte PROCESS_ADVICE_OFFLINE = 6;
	
	  
	//----------------------------------------------------------------------
	//  Transaction ENTRY mode
	//----------------------------------------------------------------------
    final byte MANUAL_ENTRY      = 1; //手工输入卡号
    final byte SWIPE_ENTRY       = 2; //读取磁条卡号
    final byte SCAN_ENTRY        = 3; //读取条形码
    final byte INSERT_ENTRY      = (byte)0x80; //读取IC卡
    final byte CONTACTLESS_ENTRY = 6;
    // PIN Mode
    final byte CAN_PIN      = 0;  //可输密码
    final byte CANNOT_PIN   = 1;  //不可输密码
    
    final byte LOGON_MODE  = 3;
    final byte LOGOFF_MODE = 4;
    final byte TEST_MODE   = 5;
    final byte CLOSE_MODE  = 6;

    final byte TRACK2_ERROR = 0x10;
    final byte TRACK1_ERROR = (TRACK2_ERROR << 1);
    final byte TRACK3_ERROR = (TRACK2_ERROR << 2);

    final byte TRY_CNT = 3;
    
    // transaction state
    final byte STATE_REQUEST_CARD            = 1;
    final byte STATE_CONFIRM_CARD            = 2;
	final byte STATE_INPUT_AMOUNT            = 3;
    final byte STATE_INPUT_EXPIRE_DATE       = 4;
    final byte STATE_INPUT_TRANS_DATE        = 5;
    final byte STATE_INPUT_TICKET            = 6;
    final byte STATE_INPUT_RRN               = 7;
    final byte STATE_INPUT_AUTH_CODE         = 8;
    final byte STATE_INPUT_TIP               = 9;
    final byte STATE_INPUT_ONLINE_PIN        =10;
    final byte STATE_PROCESS_ONLINE          =11;
    final byte STATE_INPUT_ADMIN_PASS        =12;
    final byte STATE_REQUEST_CARD_ERROR      =13;
    final byte STATE_SHOW_TRANS_INFO         =14;
	final byte STATE_INPUT_OFFLINE_PIN       =15;
    final byte STATE_PROCESS_EMV_CARD        =16;
    final byte STATE_SELECT_EMV_APP          =17;
    final byte STATE_CONFIRM_ID              =18;
    final byte STATE_CONFIRM_BYPASS_PIN      =19;
    final byte STATE_SHOW_EMV_CARD_TRANS     =20;
    final byte STATE_CONFIRM_REFERRAL        =21;

    final byte STATE_REMOVE_CARD             =32;
    
    final byte STATE_TRANS_END               =40;
    
    // Activity Notifier
	final int MSR_READ_DATA_NOTIFIER                =  1;
	final int MSR_READ_ERROR_NOTIFIER               =  2;
	final int MSR_OPEN_ERROR_NOTIFIER               =  3;
	final int COMM_CONNECTING_NOTIFIER              =  4;
	final int COMM_WRITE_DATA_NOTIFIER              =  5;
	final int COMM_READ_DATA_NOTIFIER               =  6;
	final int COMM_CONNECTED_NOTIFIER               =  7;
	final int COMM_CONNECT_ERROR_NOTIFIER           =  8;
	final int PACK8583_ERROR_NOTIFIER               =  9;
	final int PACK8583_SUCCESS_NOTIFIER             = 10;

	final int PIN_SUCCESS_NOTIFIER                  = 12;
	final int PIN_ERROR_NOTIFIER                    = 13;
	final int PIN_CANCELLED_NOTIFIER                = 14;
	final int PIN_TIMEOUT_NOTIFIER                  = 15;
	final int EMV_PROCESS_NEXT_COMPLETED_NOTIFIER   = 16;
	final int REMOVE_CARD_NOTIFIER                  = 17;
	final int CARD_OPEN_ERROR_NOTIFIER              = 18;
	final int AID_INFO_CHANGED_NOTIFIER             = 19;
	final int CAPK_INFO_CHANGED_NOTIFIER            = 20;
	final int EXCEPTION_FILE_INFO_CHANGED_NOTIFIER  = 21;
	final int REVOKED_CAPK_INFO_CHANGED_NOTIFIER    = 22;
	final int CARD_INSERT_NOTIFIER                  = 23;
	final int CARD_TAPED_NOTIFIER                   = 24;
	final int CARD_REMOVE_NOTIFIER                  = 25;
	final int CARD_ERROR_NOTIFIER                   = 26;
	final int CONTACTLESS_HAVE_MORE_CARD_NOTIFIER   = 27;
	final int PRINT_PAUSE_TIMER_NOTIFIER            = 28;

	final int PREPROCESS_ERROR_NOTIFIER				= 29;
	final int CARD_CONTACTLESS_ANTISHAKE			= 30;
	final int OFFLINE_PIN_NOTIFIER                  = 31;

	// 通讯设备状态
	final byte COMM_DISCONNECTED  = 0x00;
	final byte COMM_CONNECTING    = 0x01;
	final byte COMM_CONNECTED     = 0x02;
	
	final int SMART_CARD_EVENT_INSERT_CARD = 0;
	final int SMART_CARD_EVENT_REMOVE_CARD = 1;
//	final int SMART_CARD_EVENT_POWER_ON	   = 2;
//	final int SMART_CARD_EVENT_POWER_OFF   = 3;
	final int SMART_CARD_EVENT_POWERON_ERROR = 9;
	final int SMART_CARD_EVENT_CONTALESS_HAVE_MORE_CARD  = 10;
	final int 	SMART_CARD_EVENT_CONTALESS_ANTI_SHAKE	 = 11;
	
	// Key Type
	final int SINGLE_KEY = 0;
	final int DOUBLE_KEY = 1;
	
	// Timer Mode
	final byte TIMER_IDLE = 0;
	final byte TIMER_FINISH = 1;

	// EMV Kernel Type
	final byte CONTACT_EMV_KERNAL = 1;
	final byte CONTACTLESS_EMV_KERNAL = 2;

	// POLL Card Status
	final byte WAIT_INSERT_CARD = 1;
	final byte WAIT_REMOVE_CARD = 2;
	
	// IC Card Type
	final int CARD_CONTACT     = 1;
	final int CARD_CONTACTLESS = 2;

	// Pinpad Type
	final int PINPAD_CUSTOM_UI = 0;
	final int PINPAD_SYSTEM_UI = 2;
	final int PINPAD_NONE      = 3;
	
	// EMV STATUS
  	final byte STATUS_ERROR    		= 0; //执行报错
  	final byte STATUS_CONTINUE    	= 1; //还未完成
  	final byte STATUS_COMPLETION 	= 2; //完成 

  	// EMV Return Code
  	final byte EMV_START                    = 0;  // EMV Transaction Started
	final byte EMV_CANDIDATE_LIST           = 1;  // 
	final byte EMV_APP_SELECTED             = 2;  // Application Select Completed
	final byte EMV_READ_APP_DATA            = 3;  // Read Application Data Completed
	final byte EMV_DATA_AUTH                = 4;  // Data Authentication Completed
	final byte EMV_OFFLINE_PIN              = 5;
	final byte EMV_ONLINE_ENC_PIN           = 6;  // notify Application prompt Caldholder enter Online PIN

	final byte EMV_PROCESS_ONLINE           = 8;  // notify Application to Process Online
	final byte EMV_ID_CHECK                 = 9;  // notify Application Check Cardholder's Identification
	final byte EMV_GET_PROC_OPTION          = 10; // notify Get Process Option Completed
	final byte EMV_PROCESS_RESTRICT         = 11; // notify Process Restrict Completed
	final byte EMV_CARDHOLDER_VERIFY        = 12; // notify Cardholder Verify Completed
	final byte EMV_TERMINAL_RISK_MANAGEMENT = 13; // notify Terminal Risk Management Completed
	final byte EMV_PRESENT_CARD_AGAIN       = 14; // notify Present card again for mir recovery
	final byte EMV_TAA_CAA                  = 15; // notify Terminal/Card Action Analysis Completed
	final byte EMV_2TAP                     = 16; // notify Process 2 Tap for Rupay

  	final byte APPROVE_OFFLINE              = 1;	/** Transaction approved Offline */
  	final byte APPROVE_ONLINE               = 2;	/** Transaction approved Online */
  	final byte DECLINE_OFFLINE              = 3;  	/** Transaction declined Offline */
  	final byte DECLINE_ONLINE               = 4;	/** Transaction declined Online */

  	// emv error code
  	final byte ERROR_NO_APP              =  1; // Selected Application do not in the Candidate List when Application Select
  	final byte ERROR_CARD_BLOCKED        =  2;
  	final byte ERROR_APP_SELECT          =  3; // Parse Card Returned Data Error when Application Select
  	final byte ERROR_INIT_APP            =  4; // card return 6A81 when Application Select
  	final byte ERROR_EXPIRED_CARD        =  5; // Error when Application Select
  	final byte ERROR_APP_DATA            =  6; // Application Interchange Profile(AIP) and Application File Locator(AFL) not exist when Initialize Application	
  	final byte ERROR_DATA_INVALID        =  7; // Error when Initialize Application Data
  	final byte ERROR_DATA_AUTH           =  8;
  	final byte ERROR_GEN_AC              =  9;
  	final byte ERROR_PROCESS_CMD         = 10; // Error when Read Application Data
  	final byte ERROR_SERVICE_NOT_ALLOWED = 11; /** Service not Allowed */
  	final byte ERROR_PINENTERY_TIMEOUT	 = 12; /** PIN Entry timeout */
  	final byte ERROR_OFFLINE_VERIFY	     = 13; /** Check Offline PIN Error when Cardholder Verify */
  	final byte ERROR_NEED_ADVICE         = 14; /** Communication Error with Host, but the card need advice, halted the transaction */
  	final byte ERROR_USER_CANCELLED      = 15; /** User cancelled transaction  */
  	final byte ERROR_AMOUNT_OVER_LIMIT   = 16;
  	final byte ERROR_AMOUNT_ZERO         = 17;
  	final byte ERROR_OTHER_CARD          = 18;
  	final byte ERROR_APP_BLOCKED         = 20;
	final byte ERROR_POWER_ON_AGAIN	     = 21;
    final byte ERROR_CONTACT_DURING_CONTACTLESS = 22;

	final byte ERROR_MSD_NOT_SUPPORTED   = 30;
	final byte ERROR_AMOUNT_NOT_PRESENT  = 31;
	final byte ERROR_CCC                 = 32;
	final byte ERROR_EXCHANGE_RR_DATA    = 33;
	final byte ERROR_GET_PDOL_DATA       = 34;
	final byte ERROR_RESTART       	     = 35;
	final byte ERROR_SEE_PHONE           = 36;
	final byte ERROR_NEXT_AID            = 37;  // Used for AMEX Express or Discover ZIP: if Select PPSE return 6A82, Select AID List
	final byte ERROR_ANOTHER_INTERFACE   = 38;

}
