package org.aria.rlandri.generic.artifacts;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cartago.Artifact;
import cartago.ArtifactOpMethod;

public class ValidatorArtifactOpMethod extends ArtifactOpMethod {

	private Method validatorMethod;

	public ValidatorArtifactOpMethod(Artifact artifact, Method method,
			Method validatorMethod) {
		super(artifact, method);
		this.validatorMethod = validatorMethod;
	}

	protected void validate(Artifact artifact, Object[] actualParams)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		if (validatorMethod != null)
			validatorMethod.invoke(artifact, actualParams);
	}

}
