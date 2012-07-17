// Agent master in project roulette.mas2j

{
	include("master_s_generic.asl")
}

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+!start : true
	<-	true.

+stopTurn(CurrentTurn)
	<-	spinWheel;
		payout.
