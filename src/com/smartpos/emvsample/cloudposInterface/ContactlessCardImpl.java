package com.smartpos.emvsample.cloudposInterface;

import android.content.Context;

import com.cloudpos.DeviceException;
import com.cloudpos.POSTerminal;
import com.cloudpos.rfcardreader.RFCardReaderDevice;

public class ContactlessCardImpl {
    private static final String TAG = ContactlessCardImpl.class.getSimpleName();

    private RFCardReaderDevice device = null;
    private static ContactlessCardImpl instance;

    public static ContactlessCardImpl getInstance(Context context){
        if(instance == null){
            instance = new ContactlessCardImpl(context);
        }
        return instance;
    }

    private ContactlessCardImpl(Context ct){
        if (device == null) {
            device = (RFCardReaderDevice) POSTerminal.getInstance(ct)
                .getDevice("cloudpos.device.rfcardreader");
        }
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
