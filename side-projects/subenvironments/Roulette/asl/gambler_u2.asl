// Agent ag1 in project roulette.mas2j

{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

betSum(2).
diff(0).

/* Initial goals */

/* Plans */

+!start : true
	<-	true.

@startFirstTurn[atomic]
+startTurn(N): N = 1
	<-	.print("Turn ",1," has started");
		getBalance(B);
		+startSum(B);
		.print("My initial balance is ",B);
		!decideBet.

//+startTurn(N): .my_name(agent_1_1)
//	<- .print("I am a rogue agent").


@slowpokeStartTurn[atomic]
+startTurn(N): .my_name(agent_1_2)
	<- .print("I am slowpoke agent");
		.wait(999);
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
		bet("Column1", Sum).
		
-!safeBet(Sum)[error_msg("validation"),op_error(ErrList)]
	<-	.print(" Error: ",ErrList).

-!safeBet(Sum)[error_msg(Msg)]
	<-	.print(" Error: ",Msg).

+!riskyBet(Sum)
	<- 
		.print("Decided to make a risky bet");
		bet("Single",[9],Sum).
		
-!riskyBet(Sum)[error_msg("validation"),op_error(ErrList)]
	<-	.print(" Error: ",ErrList).
	
-!riskyBet(Sum)[error_msg(Msg)]
	<-	.print(" Error: ",Msg).

@payoffPlan[atomic]
+payoff(Turn,Number,Color,Payoff)
	<-	.print(" --------- Payoff in turn ",Turn," is ",Payoff, " --------- ");
		?diff(Diff);
		-+diff(Diff+Payoff).
		
/*	TODOs

- comentarii cod jason + ruleta
- pretty printing

*/




