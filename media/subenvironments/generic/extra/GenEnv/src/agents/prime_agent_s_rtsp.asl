/**
 * The prime agent for a real-time single-player sub-environment.
 */

{
	include("prime_agent_s_generic.asl")
}

/* Initial beliefs and rules */
endSign("no_more").
/* Initial goals */

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

+!makeAndJoinDefaultWorkspace: true
		<- 	createWorkspace("SubenvDefaultWorkspace");
			joinWorkspace("SubenvDefaultWorkspace", Id);
			cartago.set_current_wsp(Id).

+!startSubenv: true
		<- 	.wait(2000);
			startSubenv.
