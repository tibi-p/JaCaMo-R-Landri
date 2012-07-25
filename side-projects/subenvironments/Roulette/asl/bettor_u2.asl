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
	<-	.print("Turn ",N, " has started");
		?betSum(Sum);
		!betPlan(Sum).

+!betPlan(Sum)
	<-	getBalance(Balance);
		.print("My balance is: ", Balance);
		bet("Street", [10], Sum).

-!betPlan(BetSum)[error_msg("validation"),op_error(ErrList)]
	<-	.print(" Error: ",ErrList).

+payoff(Turn,Number,Color,Payoff): Payoff > 0
	<-	.print("Won ",Payoff);
		-+betSum(2).

+payoff(Turn,Number,Color,Payoff): Payoff < 0
	<-	.print("Lost ",-Payoff);
		-+betSum(2*(-Payoff)).