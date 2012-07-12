// Agent ag1 in project roulette.mas2j

{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+!start : true <-
	?discover_roulette(_).

+ bet(_) : true
	<- 
		.print("can bet");
		bet("Split",[8,9],3).

+?discover_roulette(R) : true
  <- lookupArtifact("rou",R).

-?discover_roulette(R)
  <- .wait(10);
     ?discover_roulette(R).
