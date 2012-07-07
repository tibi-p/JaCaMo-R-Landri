package org.aria.rlandri.generic.artifacts.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aria.rlandri.generic.artifacts.util.ReflectionUtils;

import cartago.CartagoException;
import cartago.IArtifactOp;

public abstract class GuardedAnnotation {

	private final Class<? extends Annotation> annotationClass;
	private final Class<? extends IArtifactOp> opMethodClass;
	private final Method guardMethod;

	public GuardedAnnotation(Class<? extends Annotation> annotationClass,
			Class<? extends IArtifactOp> opMethodClass) throws CartagoException {
		this.annotationClass = annotationClass;
		this.opMethodClass = opMethodClass;
		this.guardMethod = ReflectionUtils.getMethodInHierarchy(
				annotationClass, "guard");
		if (this.guardMethod == null)
			throw new CartagoException("the annotation has no guard method");
	}

	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}

	public Class<? extends IArtifactOp> getOpMethodClass() {
		return opMethodClass;
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
