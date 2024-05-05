package com.smartpos.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by fengshuopeng on 2017/5/31.
 */

public class ReflectUtil {
	private static String TAG = "ReflectUtil";

	public static String getSystemProperty(String name) {
		Object obj = reflectInvoke("android.os.SystemProperties", "get", new Class[]{String.class}, name, true);
		if (obj != null)
			return (String) obj;
		else
			return null;
	}

	public static Object reflectInvoke(String className, String method, Class[] parameterTypes, Object args, boolean isStatic) {
		try {
			Class mClass = Class.forName(className);
			Object cInstance = null;
			if (!isStatic) {
				cInstance = mClass.newInstance();
			}
			Method mMethod = mClass.getMethod(method, parameterTypes);
			return mMethod.invoke(cInstance, args);

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}
}
