// Agent ag1 in project roulette.mas2j

{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+!start : true
	<-	true.

+startTurn(1)
	<-	
	//.print("Turn ", CurrentTurn, " has started");
		!betPlan(5).

+!betPlan(BetSum)
	<- 
		getBallance(Ballance);
		.print("My ballance is: ",Ballance);
		bet("Six",[1,2,3,4,5,5],BetSum).
	
-!betPlan(BetSum)[error_msg("validation"),op_error(ErrList)]
	<-	.print(" Error: ",ErrList).

+payoff(Turn,Number,Color,Payoff): Payoff > 0 
	<- 
	//.print("At turn ",Turn," the result was: ",Number,Color,". The bet was won. Amount won:",Payoff);
	.print("I won some cash and i won't play anymore!").
		
		

+payoff(Turn,Number,Color,Payoff): Payoff < 0
	<- 
	//.print("At turn ",Turn," the result was: ",Number,Color,". The bet was lost. Amount lost:",-Payoff).
		.print("Doubling the sum and playing again after turn ", Turn);
		!betPlan(2*(-Payoff)).

