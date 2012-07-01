// Agent prime_agent in project sandbox

/* Initial beliefs and rules */

/* Initial goals */

!prepare_environment.

/* Plans */

+!prepare_environment : true 
		<- 	!makeAndJoinDefaultWorkspace;
			!makeStandardArtifacts;
			!startSubenv
		.
			
+!makeStandardArtifacts: true
		<- 	!makeLogger;
			!makeCoordinator.
			
+!makeCoordinator: true
		<- 	makeArtifact("coordinator", "Coordinator", [], _).
			
+!makeLogger: true
		<- 	makeArtifact("logger", "SubenvLogger", [], _).
		
+!makeAndJoinDefaultWorkspace: true
		<- 	createWorkspace("SubenvDefaultWorkspace");
			joinWorkspace("SubenvDefaultWorkspace", Id);
			cartago.set_current_wsp(Id).

+!startSubenv: true
		<- 	.wait(2000); 
			startSubenv.