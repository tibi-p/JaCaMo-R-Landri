package org.aria.rlandri.generic.artifacts.opmethod;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aria.rlandri.generic.artifacts.Coordinator;

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

	protected void validate(Artifact artifact, Object[] actualParams) {
		try {
			if (validatorMethod != null)
				validatorMethod.invoke(artifact, actualParams);
		} catch (IllegalArgumentException e) {
			coordinator.failWithMessage("validatorMethod", e.getMessage());
		} catch (IllegalAccessException e) {
			coordinator.failWithMessage("validatorMethod", e.getMessage());
		} catch (InvocationTargetException e) {
			coordinator.failWithMessage("validatorMethod", e.getMessage());
		}
	}

}
