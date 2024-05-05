package com.smartpos.emvsample.cloudposInterface;

import android.content.Context;
import android.util.Log;

import com.cloudpos.AlgorithmConstants;
import com.cloudpos.DeviceException;
import com.cloudpos.OperationListener;
import com.cloudpos.POSTerminal;
import com.cloudpos.TimeConstants;
import com.cloudpos.jniinterface.PinPadCallbackHandler;
import com.cloudpos.pinpad.KeyInfo;
import com.cloudpos.pinpad.extend.PINPadExtendDevice;

public class PinpadImpl implements AlgorithmConstants,TimeConstants{
    private static final String TAG = PinpadImpl.class.getSimpleName();

    private PINPadExtendDevice device = null;
    private static PinpadImpl instance;

    public static PinpadImpl getInstance(Context context){
        if(instance == null){
            instance = new PinpadImpl(context);
        }
        return instance;
    }

    private PinpadImpl(Context ct){
        device = (PINPadExtendDevice) POSTerminal.getInstance(ct)
            .getDevice("cloudpos.device.pinpad");
    }

    public boolean open() {
        try {
            device.open();
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean close(){
        try {
            device.close();
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean setText(int line,String sText){
        try {
            device.showText(line,sText);
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUserKey(int masterKeyIndex,int userKeyIndex,byte[] userKey){
        try {
            device.updateUserKey(masterKeyIndex, userKeyIndex, userKey);
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            if(e.getCode() ==DeviceException.DUPLICATE_USER_KEY ){
                Log.w(TAG,"DUPLICATE_USER_KEY");
                return true;
            }
            Log.i(TAG,"updateUserKey errorCode:" + e.getCode());
            return false;
        }

    }

    public boolean setGUIConfiguration(String key, String value){
        try {
            device.setGUIConfiguration(key, value);
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            Log.i(TAG,"setGUIConfiguration errorCode:" + e.getCode());
            return false;
        }

    }

    public boolean inputPin(OperationListener listener,int keyType,int masterKeyIndex,int userKeyIndex,String pan){
        return inputPin(listener,keyType,masterKeyIndex,userKeyIndex,pan, ALG_3DES,4,6);
    }

    public boolean inputPin(OperationListener listener, int keyType, int masterKeyIndex, int userKeyIndex, String pan, int algorithm, int pinMinLen, int pinMaxLen){
        KeyInfo keyInfo = new KeyInfo(keyType, masterKeyIndex, userKeyIndex, algorithm);
        try {
            device.setPINLength(pinMinLen,pinMaxLen);
            device.listenForPinBlock(keyInfo, pan, false, listener, 60*SECOND);
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            return false;
        }


    }

    public byte[] encryptData(int keyType,int masterKeyIndex,int userKeyIndex,int algorithm,byte[] plainText){
        KeyInfo keyInfo = new KeyInfo(keyType, masterKeyIndex, userKeyIndex,
            algorithm);
        try {
            byte[] cipher = device.encryptData(keyInfo, plainText);
            return cipher;
        } catch (DeviceException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setAllowByPassPin(boolean bValue){
        try {
            device.setAllowByPass(bValue);
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setPinPadCallback(PinPadCallbackHandler handler){
        try {
            device.setupCallbackHandler(handler);
        } catch (DeviceException e) {
            e.printStackTrace();
        }
    }



}
