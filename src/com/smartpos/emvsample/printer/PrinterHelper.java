package com.smartpos.emvsample.printer;

import android.content.Context;

import com.cloudpos.DeviceException;
import com.cloudpos.POSTerminal;
import com.cloudpos.printer.Format;
import com.cloudpos.printer.PrinterDevice;
import com.smartpos.emvsample.MainApp;
import com.smartpos.emvsample.R;
import com.smartpos.emvsample.constant.EMVConstant;
import com.smartpos.emvsample.transaction.TransDefine;
import com.smartpos.util.AppUtil;
import com.smartpos.util.StringUtil;

import java.io.UnsupportedEncodingException;

/**
 * Print operations
 * 
 * @author lianyi
 */
public class PrinterHelper 
{
    private static PrinterHelper _instance;
	private PrinterDevice device = null;

    private PrinterHelper(Context mContext) {
		if (device == null) {
			device = (PrinterDevice) POSTerminal.getInstance(mContext)
				.getDevice("cloudpos.device.printer");
		}
    }

    synchronized public static PrinterHelper getInstance(Context mContext)
    {
		if (null == _instance){
		    _instance = new PrinterHelper(mContext);
		}
		return _instance;
    }

	/**
	 * Print purchase order
	 *
	 * @throws PrinterException
	 */
	synchronized public void printReceiptWithAAR(MainApp appState, int receipt) throws PrinterException
	{
		try {
			Format format = new Format();
			device.open();

			format.setParameter(Format.FORMAT_FONT_SIZE, Format.FORMAT_FONT_SIZE_LARGE);
			format.setParameter(Format.FORMAT_FONT_BOLD, Format.FORMAT_FONT_VAL_TRUE);
			format.setParameter(Format.FORMAT_ALIGN, Format.FORMAT_ALIGN_CENTER);
			device.printlnText(format, "POS SLIP");
			format.setParameter(Format.FORMAT_FONT_SIZE, Format.FORMAT_FONT_SIZE_MEDIUM);
			device.printlnText(format,"--------------------------------");
			device.printlnText(format,appState.terminalConfig.getMerchantName1());

			if(receipt == 0)
			{
				device.printlnText("MERCHANT COPY");
			}
			else if(receipt == 1)
			{
				device.printlnText("CARDHOLDER COPY");
			}
			else if(receipt == 2)
			{
				device.printlnText("BANK COPY");
			}
			device.printlnText("--------------------------------");

			format.clear();
			format.setParameter(Format.FORMAT_FONT_SIZE, Format.FORMAT_FONT_SIZE_SMALL);
			format.setParameter(Format.FORMAT_FONT_BOLD, Format.FORMAT_FONT_VAL_FALSE);
			format.setParameter(Format.FORMAT_ALIGN, Format.FORMAT_ALIGN_LEFT);
			device.printlnText(format,appState.getString(R.string.tid_tag) + " " + appState.terminalConfig.getTID()  		+ "\n" +
								  		 appState.getString(R.string.mid_tag) + " " + appState.terminalConfig.getMID()
			);

			String pan = appState.getString(R.string.pan_tag) + " " + appState.trans.getPAN();
			switch(appState.trans.getCardEntryMode())
			{
			case 0:
				pan = pan + " N";
				break;
			case EMVConstant.SWIPE_ENTRY:
				pan = pan + " S";
				break;
			case EMVConstant.INSERT_ENTRY:
				pan = pan + " I";
				break;
			case EMVConstant.MANUAL_ENTRY:
				pan = pan + " M";
				break;
			default:
				pan = pan + " C";
				break;
			}
			format.clear();
			format.setParameter(Format.FORMAT_FONT_SIZE, Format.FORMAT_FONT_SIZE_MEDIUM);
			format.setParameter(Format.FORMAT_FONT_BOLD, Format.FORMAT_FONT_VAL_TRUE);
			device.printlnText(format,pan);
			format.clear();
			format.setParameter(Format.FORMAT_FONT_SIZE, Format.FORMAT_FONT_SIZE_SMALL);
			format.setParameter(Format.FORMAT_FONT_BOLD, Format.FORMAT_FONT_VAL_FALSE);
			device.printlnText(format,appState.getString(R.string.date_tag)
				+ " " + appState.trans.getTransDate().substring(0, 4)
				+ "/" + appState.trans.getTransDate().substring(4, 6)
				+ "/" + appState.trans.getTransDate().substring(6, 8)
				+ " " + appState.trans.getTransTime().substring(0, 2)
				+ ":" + appState.trans.getTransTime().substring(2, 4)
				+ ":" + appState.trans.getTransTime().substring(4, 6));


			device.printlnText("TICKET:" + StringUtil.fillZero(Integer.toString(appState.trans.getTrace()), 6));
			format.clear();
			format.setParameter(Format.FORMAT_FONT_SIZE, Format.FORMAT_FONT_SIZE_MEDIUM);
			format.setParameter(Format.FORMAT_FONT_BOLD, Format.FORMAT_FONT_VAL_TRUE);
			device.printlnText(format,appState.getString(TransDefine.transInfo[appState.trans.getTransType()].id_display_en));
			device.printlnText("AMOUNT:" + StringUtil.fillString(AppUtil.formatAmount(appState.trans.getTransAmount()), 22, ' ', true));

			if(appState.trans.getCardEntryMode() != EMVConstant.SWIPE_ENTRY)
			{
				format.clear();
				format.setParameter(Format.FORMAT_FONT_SIZE, Format.FORMAT_FONT_SIZE_SMALL);
				format.setParameter(Format.FORMAT_FONT_BOLD, Format.FORMAT_FONT_VAL_FALSE);

				device.printlnText(format,"CSN:" + StringUtil.fillZero(Byte.toString(appState.trans.getCSN()),2)  	+ "\n" +
					"UNPR NUM:" + appState.trans.getUnpredictableNumber()   											+ "\n" +
					"AC:" + appState.trans.getAC()   																	+ "\n" +
					"TVR:" + appState.trans.getTVR()   																	+ "\n" +
					"AID:" + appState.trans.getAID()   																	+ "\n" +
					"TSI:" + appState.trans.getTSI()   																	+ "\n" +
					"APPLAB:" + appState.trans.getAppLabel()   															+ "\n" +
					"APPNAME:" + appState.trans.getAppName()   															+ "\n" +
					"AIP:" + appState.trans.getAIP()   																	+ "\n" +
					"IAD:" + appState.trans.getIAD()   																	+ "\n" +
					"TermCap:" + appState.terminalConfig.getTerminalCapabilities()
				);
			}

			device.printlnText("\n\nCARDHOLDER SIGNATURE");
			device.printlnText("\n\n");
			format.clear();
			format.setParameter(Format.FORMAT_FONT_SIZE, Format.FORMAT_FONT_SIZE_MEDIUM);
			device.printlnText(format,"--------------------------------");
			format.clear();
			format.setParameter(Format.FORMAT_FONT_SIZE, Format.FORMAT_FONT_SIZE_SMALL);
			device.printlnText("\n\n\n");
		} catch (IllegalArgumentException e) {
			throw new PrinterException(e.getMessage(), e);
		}catch(DeviceException e){
			throw new PrinterException(e.getMessage(), e);
		} finally {
			try {
				device.close();
			} catch (DeviceException e) {
				throw new PrinterException(e.getMessage(), e);
			}
		}
	}





    /**
     * Print purchase order
     * 
     * @throws PrinterException
     */
    synchronized public void printReceiptWithESC(MainApp appState, int receipt) throws PrinterException
    {
		try {
		    device.open();
		    printerWrite(PrinterCommand.init());
		    printerWrite(PrinterCommand.setHeatTime(180));
		    printerWrite(PrinterCommand.setAlignMode(1));
		    printerWrite(PrinterCommand.setFontBold(1));

		    printerWrite(("POS SLIP").getBytes("GB2312"));
		    printerWrite(PrinterCommand.feedLine(2));
		    printerWrite(PrinterCommand.setAlignMode(0));
		   
		    printerWrite("--------------------------------".getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    
		    printerWrite(appState.terminalConfig.getMerchantName1().getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
			    
	    	if(receipt == 0)
		    {
		    	printerWrite(("MERCHANT COPY").getBytes("GB2312"));
			    printerWrite(PrinterCommand.linefeed());
		    }
		    else if(receipt == 1)
		    {
		    	printerWrite(("CARDHOLDER COPY").getBytes("GB2312"));
		    	
			    printerWrite(PrinterCommand.linefeed());
		    }
		    else if(receipt == 2)
		    {
		    	printerWrite(("BANK COPY").getBytes("GB2312"));
			    printerWrite(PrinterCommand.linefeed());
		    }
		    printerWrite("--------------------------------".getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    printerWrite((appState.getString(R.string.tid_tag) + " " + appState.terminalConfig.getTID()).getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    
		    printerWrite((appState.getString(R.string.mid_tag) + " " + appState.terminalConfig.getMID()).getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
 
		    String pan = appState.getString(R.string.pan_tag) + " " + appState.trans.getPAN();
		    switch(appState.trans.getCardEntryMode())
		    {
		    case 0:
		    	pan = pan + " N";
		    	break;
		    case EMVConstant.SWIPE_ENTRY:
		    	pan = pan + " S";
		    	break;
		    case EMVConstant.INSERT_ENTRY:
		    	pan = pan + " I";
		    	break;
		    case EMVConstant.MANUAL_ENTRY:
		    	pan = pan + " M";
		    	break;
		    default:
		    	pan = pan + " C";
		    	break;
		    }
		    printerWrite(pan.getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    
		    printerWrite((  appState.getString(R.string.date_tag) 
		    		      + " " + appState.trans.getTransDate().substring(0, 4)
		    		      + "/" + appState.trans.getTransDate().substring(4, 6)
		    		      + "/" + appState.trans.getTransDate().substring(6, 8)
		    		      + " " + appState.trans.getTransTime().substring(0, 2)
		    		      + ":" + appState.trans.getTransTime().substring(2, 4)
		    		      + ":" + appState.trans.getTransTime().substring(4, 6) ).getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    
		    printerWrite(( "TICKET:" + StringUtil.fillZero(Integer.toString(appState.trans.getTrace()), 6)).getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed()); 
	    
		    printerWrite(appState.getString(TransDefine.transInfo[appState.trans.getTransType()].id_display_en).getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());
		    
		    printerWrite(("AMOUNT:" + StringUtil.fillString(AppUtil.formatAmount(appState.trans.getTransAmount()), 22, ' ', true)).getBytes("GB2312"));
		    printerWrite(PrinterCommand.linefeed());

		    if(appState.trans.getCardEntryMode() != EMVConstant.SWIPE_ENTRY)
		    {
			    printerWrite(("CSN:" + StringUtil.fillZero(Byte.toString(appState.trans.getCSN()),2)).getBytes());
			    printerWrite(PrinterCommand.linefeed());
		    	
			    printerWrite(("UNPR NUM:" + appState.trans.getUnpredictableNumber()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
		    	printerWrite(("AC:" + appState.trans.getAC()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("TVR:" + appState.trans.getTVR()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("AID:" + appState.trans.getAID()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("TSI:" + appState.trans.getTSI()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("APPLAB:" + appState.trans.getAppLabel()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("APPNAME:" + appState.trans.getAppName()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("AIP:" + appState.trans.getAIP()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("IAD:" + appState.trans.getIAD()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
			    
			    printerWrite(("TermCap:" + appState.terminalConfig.getTerminalCapabilities()).getBytes());
			    printerWrite(PrinterCommand.linefeed());
		    }
//		    if( appState.trans.getNeedSignature() == 1)
//		    {
			    String sig = "\n\nCARDHOLDER SIGNATURE";
			    printerWrite(sig.getBytes("GB2312"));
			    printerWrite(PrinterCommand.feedLine(3)); 
			    printerWrite("--------------------------------".getBytes("GB2312"));
			    printerWrite(PrinterCommand.linefeed());
//		    }
		    printerWrite(PrinterCommand.feedLine(2));
		    
		} catch (UnsupportedEncodingException e) {
		    throw new PrinterException("PrinterHelper.printReceipt():" + e.getMessage(), e);
		} catch (IllegalArgumentException e) {
		    throw new PrinterException(e.getMessage(), e);
		}catch(DeviceException e){
			throw new PrinterException(e.getMessage(), e);
		} finally {
			try {
				device.close();
			} catch (DeviceException e) {
				throw new PrinterException(e.getMessage(), e);
			}
		}
    }
    
    public void printerWrite(byte[] data) throws DeviceException {
    	device.sendESCCommand(data);
    }
}
