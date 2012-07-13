package org.aria.rlandri.generic.artifacts;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import cartago.Artifact;
import cartago.ArtifactConfig;
import cartago.CartagoException;
import cartago.OPERATION;
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

	// it's @PRIME_AGENT_OPERATION actually
	@OPERATION
	void makeCoordinatorArtifact(String artifactName) throws OperationException {
		makeArtifact(artifactName, coordinatorClass,
				ArtifactConfig.DEFAULT_CONFIG);
	}

}
