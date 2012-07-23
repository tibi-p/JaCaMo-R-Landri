// Agent ag1 in project roulette.mas2j

{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

betSum(2).

/* Initial goals */

/* Plans */

+!start : true
	<-	true.

+startTurn(N)
	<-	
	.print("Turn ",N, " has started");
		?betSum(Sum);
		!betPlan(Sum).

+!betPlan(Sum)
	<- 
		getBallance(Ballance);
		.print("My ballance is: ",Ballance);
		bet("Street",[1,2,3],Sum).
	
-!betPlan(BetSum)[error_msg("validation"),op_error(ErrList)]
	<-	.print(" Error: ",ErrList).

+payoff(Turn,Number,Color,Payoff): Payoff > 0 
	<-	
		.print("Won ",Payoff);
		-+betSum(2).	

+payoff(Turn,Number,Color,Payoff): Payoff < 0
	<- 
		.print("Lost ",-Payoff);
		-+betSum(2*(-Payoff)).
