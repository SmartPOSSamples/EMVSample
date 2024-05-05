package com.smartpos.util;

import android.util.Log;

public class WizarTypeUtil {
	private static String TAG = "WizarTypeUtil";
	private static WIZARTYPE wizarType = null;

	public enum WIZARTYPE {
		WIZARPOS_1,
		WIZARPOS_1_V2,
		WIZARHAND_Q1,
		WIZARHAND_Q1_V2,
		WIZARHAND_Q2,
		WIZARPAD_1,
		WIZARHAND_Q3,
		WIZARHAND_Q3F
	}

	public static WIZARTYPE getWizarType() {
		if (wizarType == null) {
			loadWizarType();
		}
		return wizarType;
	}

	private static void loadWizarType() {
		String model = ReflectUtil.getSystemProperty("ro.product.model");
		String wpModel = ReflectUtil.getSystemProperty("ro.wp.product.model");
		Log.i(TAG, "ro.product.model:" + model);
		Log.i(TAG, "ro.wp.product.model:" + wpModel);
		if (model != null) {
			model = model.toUpperCase();
		} else {
			model = "";
		}

		if (wpModel != null) {
			wpModel = wpModel.toUpperCase();
		} else {
			wpModel = "";
		}

		switch (wpModel) {
			case "W1":
				wizarType = WIZARTYPE.WIZARPOS_1;
				break;
			case "W1V2":
				wizarType = WIZARTYPE.WIZARPOS_1_V2;
				break;
			case "PAD1":
				wizarType = WIZARTYPE.WIZARPAD_1;
				break;
			case "Q1":
				wizarType = WIZARTYPE.WIZARHAND_Q1;
				break;
			case "Q1V2":
				wizarType = WIZARTYPE.WIZARHAND_Q1_V2;
				break;
			case "Q2":
				wizarType = WIZARTYPE.WIZARHAND_Q2;
				break;
			case "Q3A7":
				wizarType = WIZARTYPE.WIZARHAND_Q3;
				break;
		}

		if (wizarType == null) {
			switch (model) {
				case "WIZARPOS_1":
				case "WIZARPOS 1":
					wizarType = WIZARTYPE.WIZARPOS_1;
					break;
				case "WIZARPAD_1":
				case "WIZARPAD 1":
					wizarType = WIZARTYPE.WIZARPAD_1;
					break;
				case "WIZARHAND_Q1":
				case "WIZARHAND Q1":
					wizarType = WIZARTYPE.WIZARHAND_Q1;
					break;
				case "WIZARPOS_Q2":
					wizarType = WIZARTYPE.WIZARHAND_Q2;
					break;
				case "WIZARPOS_Q3":
					wizarType = WIZARTYPE.WIZARHAND_Q3;
					break;
				case "WIZARPOS_Q3F":
					wizarType = WIZARTYPE.WIZARHAND_Q3F;
					break;
			}
		}


		Log.i(TAG, "wizartype:" + wizarType);
	}
}
