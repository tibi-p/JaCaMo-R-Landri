// Agent ag1 in project roulette.mas2j

{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */
betSum(2).
diff(0).

+!start	<-	true.

@startFirstTurn[atomic]
+startTurn(N): N = 1
	<-	.print("Turn ",1," has started");
		getBalance(B);
		+startSum(B);
		.print("My initial balance is ",B);
		!decideBet.
		
@startTurn[atomic]
+startTurn(N) : N>1
	<- 
		.print("Turn ",N," has started");
		!decideBet.
		
@decideSafeBetPlan[atomic]
+!decideBet : diff(D) & D<=0
	<-	getBalance(Balance);
		?startSum(Start);
		if(Start-Balance < 30)
		{
			?betSum(Sum);
			!safeBet(Sum);
		}.
		
-!decideBet
	<- .print("Not betting").
	
@decideRiskyBetPlan[atomic]
+!decideBet : diff(D) & D>0
	<-	
		?betSum(Sum);
		!riskyBet(Sum).
		
+!safeBet(Sum)
	<-	
		.print("Decided to make a safe bet");
		bet("Column1", Sum, Turn, Payoff);
		.print(" --------- Payoff in turn ",Turn," is ",Payoff, " --------- ");
		?diff(Diff);
		-+diff(Diff+Payoff).
		
-!safeBet(Sum)[error_msg("validation"),op_error(ErrList)]
	<-	.print(" Error: ",ErrList).

-!safeBet(Sum)[error_msg(Msg)]
	<-	.print(" Error: ",Msg).
	
+!riskyBet(Sum)
	<- 
		.print("Decided to make a risky bet");
		bet("Single",[9],Sum, Turn, Payoff);
		.print(" --------- Payoff in turn ",Turn," is ",Payoff, " --------- ");
		?diff(Diff);
		-+diff(Diff+Payoff).
		
-!riskyBet(Sum)[error_msg("validation"),op_error(ErrList)]
	<-	.print(" Error: ",ErrList).
	
-!riskyBet(Sum)[error_msg(Msg)]
	<-	.print(" Error: ",Msg).
