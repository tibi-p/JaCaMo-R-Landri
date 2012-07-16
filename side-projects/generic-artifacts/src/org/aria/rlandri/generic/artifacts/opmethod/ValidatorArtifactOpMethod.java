package org.aria.rlandri.generic.artifacts.opmethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aria.rlandri.generic.artifacts.Coordinator;
import org.aria.rlandri.generic.tools.ValidationResult;

import cartago.Artifact;
import cartago.ArtifactOpMethod;

public class ValidatorArtifactOpMethod extends ArtifactOpMethod {

	protected final Coordinator coordinator;
	private final Method validatorMethod;

	public ValidatorArtifactOpMethod(Coordinator coordinator, Method method,
			Method validatorMethod) {
		super(coordinator, method);
		this.coordinator = coordinator;
		this.validatorMethod = validatorMethod;
		// completely ignore any qualifier like private or protected
		if (validatorMethod != null)
			validatorMethod.setAccessible(true);
	}

	protected void validate(Artifact artifact, Object[] actualParams)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if (validatorMethod != null){
			ValidationResult vres = (ValidationResult) validatorMethod.invoke(artifact, actualParams);
			if(vres != null) coordinator.addValidationResult(vres);
		}
	}

	protected Object invokeParameterless(String methodName)
			throws SecurityException, NoSuchMethodException,
			IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		Class<?> clazz = getClass();
		Method method = clazz.getDeclaredMethod(methodName);
		return method.invoke(this);
	}

}
