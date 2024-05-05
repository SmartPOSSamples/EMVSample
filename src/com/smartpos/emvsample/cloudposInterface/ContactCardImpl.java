package com.smartpos.emvsample.cloudposInterface;

import android.content.Context;

import com.cloudpos.DeviceException;
import com.cloudpos.POSTerminal;
import com.cloudpos.smartcardreader.SmartCardReaderDevice;


public class ContactCardImpl {
    private static final String TAG = ContactCardImpl.class.getSimpleName();

    private SmartCardReaderDevice device = null;
    private static ContactCardImpl instance;

    public static ContactCardImpl getInstance(Context context){
        if(instance == null){
            instance = new ContactCardImpl(context);
        }
        return instance;
    }

    private ContactCardImpl(Context ct){
        device = (SmartCardReaderDevice) POSTerminal.getInstance(ct)
            .getDevice("cloudpos.device.smartcardreader");
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

}
