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
		bet("Manque",3).

+payoff(Turn,Number,Color,Payoff): Payoff > 0 
	<- .print("At turn ",Turn," the result was: ",Number,Color,". The bet was won. Amount won:",Payoff).

+payoff(Turn,Number,Color,Payoff): Payoff < 0
	<- .print("At turn ",Turn," the result was: ",Number,Color,". The bet was lost. Amount lost:",-Payoff).
