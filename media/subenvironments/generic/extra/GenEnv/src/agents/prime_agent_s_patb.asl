/* Initial beliefs and rules */

/* Initial goals */

!prepare_environment.

/* Plans */

+!prepare_environment : true 
		<- 	!makeAndJoinDefaultWorkspace;
			!makeStandardArtifacts;
			!startSubenv.
			
+!makeAndJoinDefaultWorkspace: true
		<- 	createWorkspace("SubenvDefaultWorkspace");
			joinWorkspace("SubenvDefaultWorkspace", Id);
			cartago.set_current_wsp(Id).
			
+!makeStandardArtifacts: true
		<- 	!makeLogger;
			!makeCoordinator.

+!makeLogger: true
		<- 	makeArtifact("logger","org.aria.rlandri.generic.artifacts.SubenvLogger",[],_).

+!makeCoordinator: true
		<- 	makeArtifact("coordinator","org.aria.rlandri.generic.artifacts.PlayerAlternatedCoordinator",[],_);
			lookupArtifact("coordinator", _).

+!startSubenv: true
		<- 	.wait(2000);
			startSubenv.
