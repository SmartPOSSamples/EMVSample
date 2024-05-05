package com.smartpos.emvsample.transaction;

import com.smartpos.emvsample.R;
import com.smartpos.emvsample.constant.EMVConstant;

public class TransDefine implements EMVConstant
{
	public static TransInfo[] transInfo = { 
		new TransInfo( TRAN_GOODS,  		R.string.tran_sale_en, 			(byte)0  ),
		new TransInfo( TRAN_SETTLE,  	    R.string.tran_settlement_en,	(byte)(T_NOCAPTURE               ) 	),
		new TransInfo( TRAN_BALANCE,  	    R.string.tran_balance_en,	(byte)(T_NOCAPTURE) ),
		new TransInfo( QUERY_CARD_RECORD,	R.string.query_trans_record_en,	(byte)(T_NOCAPTURE	+ T_NORECEIPT)  ),
		
		new TransInfo( QUERY_SPECIFIC,  	R.string.query_specific_en,		(byte)(T_NOCAPTURE	+ T_NORECEIPT) 	),
		new TransInfo( QUERY_TRANS_DETAIL,	R.string.query_trans_detail_en,	(byte)(T_NOCAPTURE	+ T_NORECEIPT)  ),
	};
}
