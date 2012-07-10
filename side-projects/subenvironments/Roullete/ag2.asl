// Agent ag2 in project roulette.mas2j

/* Initial beliefs and rules */

/* Initial goals */

!start.

/* Plans */

+!start : true <- 
	?discover_roulette(_).
	
+ bet(_) : true
	<- 
		bet("Odd",2).

+?discover_roulette(R) : true  
  <- lookupArtifact("rou",R). 

-?discover_roulette(R)  
  <- .wait(10);  
     ?discover_roulette(R).  
