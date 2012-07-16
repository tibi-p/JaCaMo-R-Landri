// Agent common in project sandbox

{
	include("base_s_generic.asl")
}

/* Plans */

+startSubenv[artifact_name(_, "coordinator")]
	<-	!registration;
		!joinPrivateWorkspace;
		!start.

+!registration: true
	<-	registerAgent(Wsp);
		+wsp_to_join(Wsp).

+!joinPrivateWorkspace: wsp_to_join("NA")
	<-	-wsp_to_join(_).

+!joinPrivateWorkspace: not wsp_to_join("NA")
	<-	?wsp_to_join(WspName);
		joinWorkspace(WspName, _);
		-wsp_to_join(_).
