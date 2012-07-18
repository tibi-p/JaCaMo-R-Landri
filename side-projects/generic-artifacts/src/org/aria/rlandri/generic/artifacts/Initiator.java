package org.aria.rlandri.generic.artifacts;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

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
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream("config.properties"));
			this.environmentType = prop.getProperty("environment_type");
			this.coordinatorClass = prop.getProperty("coordinator_class");
		} catch (FileNotFoundException e) {
			throw new CartagoException(e.getMessage());
		} catch (IOException e) {
			throw new CartagoException(e.getMessage());
		}
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
