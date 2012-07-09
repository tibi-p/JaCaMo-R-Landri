package org.aria.rlandri.generic.artifacts.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.aria.rlandri.generic.artifacts.util.ReflectionUtils;

import cartago.CartagoException;
import cartago.IArtifactOp;

public abstract class GuardedAnnotation {

	private final Class<? extends Annotation> annotationClass;
	private final Constructor<?> opMethodConstructor;
	private final boolean mandatoryValidator;
	private final Method guardMethod;
	private final Method validatorMethod;

	public GuardedAnnotation(Class<? extends Annotation> annotationClass,
			Class<? extends IArtifactOp> opMethodClass,
			boolean mandatoryValidator) throws CartagoException {
		try {
			this.annotationClass = annotationClass;
			this.opMethodConstructor = opMethodClass.getConstructors()[0];
			this.mandatoryValidator = mandatoryValidator;
			this.guardMethod = ReflectionUtils.getMethodInHierarchy(
					annotationClass, "guard");
			this.validatorMethod = ReflectionUtils.getMethodInHierarchy(
					annotationClass, "validator");
		} catch (SecurityException e) {
			throw new CartagoException(e.getMessage());
		}
		if (this.guardMethod == null)
			throw new CartagoException("the annotation has no guard method");
		if (this.validatorMethod == null)
			throw new CartagoException("the annotation has no validator method");
	}

	public Class<? extends Annotation> getAnnotationClass() {
		return annotationClass;
	}

	public Constructor<?> getOpMethodConstructor() {
		return opMethodConstructor;
	}

	/**
	 * Returns <tt>true</tt> if a validator is mandatory.
	 * 
	 * @return <tt>true</tt> if a validator is mandatory
	 */
	public boolean isValidatorMandatory() {
		return mandatoryValidator;
	}

	public Method getGuardMethod() {
		return guardMethod;
	}

	public Method getValidatorMethod() {
		return validatorMethod;
	}

	public boolean isMethodAnnotated(Method method) {
		return method.isAnnotationPresent(annotationClass);
	}

	public Annotation getMethodAnnotation(Method method) {
		return method.getAnnotation(annotationClass);
	}

	public String invokeGuardMethod(Object obj) {
		Object value = ReflectionUtils.invokeSafely(guardMethod, obj);
		if (value instanceof String)
			return (String) value;
		else
			return null;
	}

	public String invokeValidatorMethod(Object obj) {
		Object value = ReflectionUtils.invokeSafely(validatorMethod, obj);
		if (value instanceof String)
			return (String) value;
		else
			return null;
	}

	public abstract void processMethod(Method method) throws CartagoException;

}
