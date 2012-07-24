// Agent fibonaci in project sandbox

/* Initial beliefs and rules */

/* Initial goals */

{include("common_s_generic.asl")}

/* Plans */

+!start : true
	<-	?coord_id(Id);
		start[artifact_id(Id)].

+goal(V) <- !calculate(V, 1).

+!calculate(1, Res): true
	<-	?logger_id(Log);
		logInfo("Who's your daddy? ", Res)[artifact_id(Log)];
		?coord_id(Facto);
		derp(Res)[artifact_id(Facto)].

+!calculate(Count, Res): Count > 1
	<-	!calculate(Count - 1, Res * Count).
