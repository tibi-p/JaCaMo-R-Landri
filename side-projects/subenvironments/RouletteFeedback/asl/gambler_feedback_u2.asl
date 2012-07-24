// Agent ag1 in project roulette.mas2j

{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+!start : true
	<-	true.

+startTurn(CurrentTurn)
	<-	.print("Turn ", CurrentTurn, " has started");
		bet("Split",[8,9],3,Turn,Payoff);
		.print("Payoff at turn",Turn," is ",Payoff).
