package com.smartpos.emvsample.cloudposInterface;

import android.content.Context;

import com.cloudpos.DeviceException;
import com.cloudpos.OperationListener;
import com.cloudpos.POSTerminal;
import com.cloudpos.TimeConstants;
import com.cloudpos.msr.MSRDevice;

public class MSRImpl {

    private static final String TAG = MSRImpl.class.getSimpleName();

    private MSRDevice device = null;
    private static MSRImpl instance;
    private boolean isOpened = false;

    public static MSRImpl getInstance(Context context){
        if(instance == null){
            instance = new MSRImpl(context);
        }
        return instance;
    }

    private MSRImpl(Context ct){
        device = (MSRDevice) POSTerminal.getInstance(ct)
            .getDevice("cloudpos.device.msr");
    }

    public boolean open() {
        if(isOpened)
            return true;

        try {
            device.open();
            isOpened = true;
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean startRedMSR(OperationListener listener){
        try {
            device.listenForSwipe(listener, TimeConstants.FOREVER);
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            close();
            return false;
        }
    }

    public boolean cancelRequest() {
        if(!isOpened)
            return true;
        try {
            device.cancelRequest();
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean close(){
        if(!isOpened)
            return true;

        try {
            device.close();
            isOpened = false;
            return true;
        } catch (DeviceException e) {
            e.printStackTrace();
            return false;
        }
    }
}
