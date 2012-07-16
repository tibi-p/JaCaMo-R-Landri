// Agent prime_agent in project sandbox

/* Initial beliefs and rules */

/* Initial goals */

!prepare_environment.

/* Plans */

+!makeCoordinator: true
	<-	makeArtifact("initiator",
			"org.aria.rlandri.generic.artifacts.Initiator", [], _
		);
		makeCoordinatorArtifact("coordinator", [], Coordinator);
		focus(Coordinator);
		registerPrimeAgent[artifact_id(Coordinator)].

+!makeLogger: true
	<-	makeArtifact("logger",
			"org.aria.rlandri.generic.artifacts.SubenvLogger", [], _
		).

+stopGame
	<-	.stopMAS.
