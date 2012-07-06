// Agent common in project sandbox
!prep.

/* Plans */

+!prep : true 
	<- 	!registerWorkspace;
		!registerCoordinator;
		!registerLogger.
	
+!registerWorkspace: true
	<- 	joinWorkspace("SubenvDefaultWorkspace", Id);
		+subenv_default_workspace(Id);
		.
	
-!registerWorkspace: true
	<- 	.wait(10);
		!registerWorkspace.
	
+!registerLogger: true
	<- 	lookupArtifact("logger", Logger);
		focus(Logger);
		+logger_id(Logger);
		.
	
-!registerLogger: true
	<- 	.wait(10);
		!registerLogger.
	
+!registerCoordinator: true
	<- 	lookupArtifact("coordinator", Coord);
		+coord_id(Coord);
		focus(Coord);
		.
	
-!registerCoordinator: true
	<- 	.wait(10);
		!registerCoordinator.
	
+startSubenv[artifact_name(_, "coordinator")]
	<- 	registerAgent(Wsp);
		+wsp_to_join(Wsp);
		!joinPrivateWorkspace;
		!start.
		
+!joinPrivateWorkspace: wsp_to_join("NA")
	<- 	-wsp_to_join(_);
		true.
	
+!joinPrivateWorkspace: not wsp_to_join("NA")
	<- 	?wsp_to_join(WspName);
		joinWorkspace(WspName, _);
		-wsp_to_join(_).