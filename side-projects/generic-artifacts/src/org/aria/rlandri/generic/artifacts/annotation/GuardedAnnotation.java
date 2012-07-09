package org.aria.rlandri.generic.artifacts.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aria.rlandri.generic.artifacts.util.ReflectionUtils;

import cartago.CartagoException;
import cartago.IArtifactOp;

public abstract class GuardedAnnotation {

	private final Class<? extends Annotation> annotationClass;
	private final Constructor<?> opMethodConstructor;
	private final Method guardMethod;

	public GuardedAnnotation(Class<? extends Annotation> annotationClass,
			Class<? extends IArtifactOp> opMethodClass) throws CartagoException {
		try {
			this.annotationClass = annotationClass;
			this.opMethodConstructor = opMethodClass.getConstructors()[0];
			this.guardMethod = ReflectionUtils.getMethodInHierarchy(
					annotationClass, "guard");
		} catch (SecurityException e) {
			throw new CartagoException(e.getMessage());
		}
		if (this.guardMethod == null)
			throw new CartagoException("the annotation has no guard method");
	}

	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}

	public Constructor<?> getOpMethodConstructor() {
		return opMethodConstructor;
	}

	public Method getGuardMethod() {
		return guardMethod;
	}

	public boolean isMethodAnnotated(Method method) {
		return method.isAnnotationPresent(annotationClass);
	}

	public Annotation getMethodAnnotation(Method method) {
		return method.getAnnotation(annotationClass);
	}

	public String invokeGuardMethod(Object obj) {
		try {
			return (String) guardMethod.invoke(obj);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO remove this! use exceptions instead!
		return null;
	}

	public abstract void processMethod(Method method) throws CartagoException;

}
