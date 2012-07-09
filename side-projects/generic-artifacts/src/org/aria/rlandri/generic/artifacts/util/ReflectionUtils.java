package org.aria.rlandri.generic.artifacts.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtils {

	public static final Method getMethodInHierarchy(Class<?> cl, String name,
			Class<?>... types) {
		try {
			do {
				try {
					return cl.getDeclaredMethod(name, types);
				} catch (NoSuchMethodException e) {
					cl = cl.getSuperclass();
				}
			} while (cl != null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static final Object invokeSafely(Method method, Object obj,
			Object... args) {
		try {
			return method.invoke(obj, args);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO remove this! use exceptions instead!
		return null;
	}

}
