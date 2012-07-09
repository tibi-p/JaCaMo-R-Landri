// Agent prime_agent in project sandbox

/* Initial beliefs and rules */
endSign("no_more").
/* Initial goals */

!prepare_environment.

/* Plans */

+!prepare_environment : true 
		<- 	!makeAndJoinDefaultWorkspace;
			!makeStandardArtifacts;
			getNextAgent(ParticipantWsp);
			+nextParticipant(ParticipantWsp);
			!makePrivateWsp;
			!startSubenv
		.
			
+!makeStandardArtifacts: true
		<- 	!makeLogger;
			!makeCoordinator.
			
+!makePrivateWsp: nextParticipant("no_more")
		<-	true.

+!makePrivateWsp: not nextParticipant("no_more")
		<- 	?nextParticipant(AgentWsp);
			createWorkspace(AgentWsp);
			getNextAgent(ParticipantWsp);
			-+nextParticipant(ParticipantWsp);
			!makePrivateWsp.
			
+!makeCoordinator: true
		<- 	makeArtifact("coordinator",
				"org.aria.rlandri.generic.artifacts.RealTimeSinglePlayerCoordinator", [], _
			).

+!makeLogger: true
		<- 	makeArtifact("logger",
				"org.aria.rlandri.generic.artifacts.SubenvLogger", [], _
			).

+!makeAndJoinDefaultWorkspace: true
		<- 	createWorkspace("SubenvDefaultWorkspace");
			joinWorkspace("SubenvDefaultWorkspace", Id);
			cartago.set_current_wsp(Id).

+!startSubenv: true
		<- 	.wait(2000);
			startSubenv.
