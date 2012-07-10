// Agent master in project roulette.mas2j

/* Initial beliefs and rules */

iter(1).
limit(3).

/* Initial goals */

!start.

/* Plans */


+!start : true <- 
	makeArtifact("rou","Roulette",[],_);
	!roulette;
	.stopMAS.

+!roulette: true <-
	?iter(N);
	.print("Iteration " , N);
	.broadcast(tell,bet(N));
	.wait(1000);
	spinWheel;
	payout;
	-+iter(N+1);
	?limit(Lim);
	N<Lim;
	!roulette.
	
-!roulette: true <-
	.print("Stop");
	.wait(2000).
