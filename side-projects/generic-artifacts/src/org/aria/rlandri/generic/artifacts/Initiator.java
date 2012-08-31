package org.aria.rlandri.generic.artifacts;

import cartago.Artifact;
import cartago.ArtifactId;
import cartago.CartagoException;
import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.OperationException;

public class Initiator extends Artifact {

	private String environmentType;
	private String coordinatorClass;

	protected void init() throws CartagoException {
		Configuration configuration = Configuration.getInstance();
		this.environmentType = configuration.getProperty("environment_type");
		this.coordinatorClass = configuration.getProperty("coordinator_class");
	}

	@OPERATION
	void makeCoordinatorArtifact(String artifactName, Object[] params,
			OpFeedbackParam<ArtifactId> aid) throws OperationException {
		// Emulate a PRIME_AGENT_OPERATION
		String primeName = String.format("prime_agent_s_%s", environmentType);
		String agentName = getOpUserName();
		if (primeName.equals(agentName)) {
			ArtifactId workspace = lookupArtifact("workspace");
			execLinkedOp(workspace, "makeArtifact", artifactName,
					coordinatorClass, params, aid);
		} else {
			failed("Only the prime agent can create the coordinator artifact");
		}
	}

}
