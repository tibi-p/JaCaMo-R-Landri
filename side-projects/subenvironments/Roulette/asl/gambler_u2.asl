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
	<-	getBalance(B);
		+startSum(B);
		.print("My initial balance is ",B).
		
+startTurn(N) 
	<-	.print("Turn ",N," has started");
		!decideBet.
		
+!decideBet : diff <= 0
	<-	getBalance(Balance);
		startSum(Start);
		Start-Balance < 30;
		?betSum(Sum);
		!safeBet(Sum).

+!decideBet : diff > 0
	<-	
		?betSum(Sum);
		!riskyBet(Sum).

+!safeBet(Sum)
	<-	
		.print("Decided to make a safe bet");
		bet("Column1", Sum).
	
+!riskyBet(Sum)
	<- 
		.print("Decided to make a risky bet");
		bet("Single",9).

+payoff(Turn,Number,Color,Payoff):
	<-	.print("Payoff in turn ",Turn," is ",Payoff);
		?diff(Diff);
		-+diff(Diff+Payoff).

-!betPlan(BetSum)[error_msg("validation"),op_error(ErrList)]
	<-	.print(" Error: ",ErrList).

