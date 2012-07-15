/**
 * The skeleton of a regular (non-prime) agent.
 */

!prep.

/* Plans */

+!prep : true
	<-	!registerWorkspace;
		!registerCoordinator;
		!registerLogger.

+!registerWorkspace: true
	<-	joinWorkspace("SubenvDefaultWorkspace", Id);
		+subenv_default_workspace(Id);
		.

-!registerWorkspace: true
	<-	.wait(10);
		!registerWorkspace.

+!registerLogger: true
	<-	lookupArtifact("logger", Logger);
		focus(Logger);
		+logger_id(Logger);
		.

-!registerLogger: true
	<-	.wait(10);
		!registerLogger.

+!registerCoordinator: true
	<-	lookupArtifact("coordinator", Coord);
		+coord_id(Coord);
		focus(Coord);
		.

-!registerCoordinator: true
	<-	.wait(10);
		!registerCoordinator.
