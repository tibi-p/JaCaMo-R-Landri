/**
 * The prime agent for a simultaneously-executed turn-based sub-environment.
 */

{
	include("prime_agent_s_generic.asl")
}

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+!prepare_environment : true
	<-	!makeAndJoinDefaultWorkspace;
		!makeStandardArtifacts;
		!startSubenv
	.

+!makeStandardArtifacts: true
	<-	!makeLogger;
		!makeCoordinator.

+!makeAndJoinDefaultWorkspace: true
	<-	createWorkspace("SubenvDefaultWorkspace");
		joinWorkspace("SubenvDefaultWorkspace", Id);
		cartago.set_current_wsp(Id).

+!startSubenv: true
	<-	.wait(2000);
		startSubenv.
