// Agent ag1 in project roulette.mas2j

{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

betSum(2).
diff(0).

/* Initial goals */

/* Plans */

//start event ignored
+!start : true
	<-	true.

/*
	start turn plan for the first time step
	the initial economic balance is obtained and retained in a belief
	a betting decision goal is made
	the plan is made atomic so that another start turn event won't be treated until the plan for the current one is finished
*/
@startFirstTurn[atomic]
+startTurn(N): N = 1
	<-	.print("Turn ",1," has started");
		getBalance(B);
		+startSum(B);
		.print("My initial balance is ",B);
		!decideBet.

//+startTurn(N): .my_name(agent_1_1)
//	<- .print("I am a rogue agent").

/*
	example of plan for a slow agent
	the bet is intended to be sent too late and the action should fail
*/
@slowpokeStartTurn[atomic]
+startTurn(N): .my_name(agent_1_2)
	<-  .print("I am slowpoke agent");
		.wait(999);
		!decideBet.

/*
	start turn plan for all time steps excepting the first
	a betting decision goal is made
	the plan is made atomic so that another start turn event won't be treated until the plan for the current one is finished
*/
@startTurn[atomic]
+startTurn(N) : N>1
	<-  .print("Turn ",N," has started");
		!decideBet.

/*
	plan that will be executed if the agent has a negative economical outcome until now
	the balance is obtained from the Roulette artifact. if the agent lost less than 30 gold coins he makes a safe bet goal
*/
@decideSafeBetPlan[atomic]
+!decideBet : diff(D) & D<=0
	<-	getBalance(Balance);
		?startSum(Start);
		if(Start-Balance < 30)
		{
			?betSum(Sum);
			!safeBet(Sum);
		}.

/*
	if the safe bet decision plan fails this plan is executed
*/
-!decideBet
	<- .print("Not betting").

/*
	plan that will be executed if the agent has a positive economical outcome until now
	the balance is obtained from the Roulette artifact. The agent makes a risky bet goal
*/
@decideRiskyBetPlan[atomic]
+!decideBet : diff(D) & D>0
	<-	?betSum(Sum);
		!riskyBet(Sum).

/*
	safe bet plan
	the agent bets on Column1(3k+1 numbers from 1 to 34). Column bets are considered safe
*/
+!safeBet(Sum)
	<-	.print("Decided to make a safe bet");
		bet("Column1", Sum).

/*
	safe bet failure plan for validation errors
*/
-!safeBet(Sum)[error_msg("validation"),op_error(ErrList)]
	<-	.print(" Error: ",ErrList).

/*
	generic safe bet failure plan
*/
-!safeBet(Sum)[error_msg(Msg)]
	<-	.print(" Error: ",Msg).

/*
	risky bet plan
	the agent bets on a Single Number
*/
+!riskyBet(Sum)
	<- .print("Decided to make a risky bet");
		bet("Single",[9],Sum).

/*
	risky bet failure plan for validation errors
*/
-!riskyBet(Sum)[error_msg("validation"),op_error(ErrList)]
	<-	.print(" Error: ",ErrList).

/*
	generic risky bet failure plan
*/
-!riskyBet(Sum)[error_msg(Msg)]
	<-	.print(" Error: ",Msg).

/*
	payoff plan
	the economic difference is updated with the payoff from the last turn
*/
@payoffPlan[atomic]
+payoff(Turn,Number,Color,Payoff)
	<-	.print(" --------- Payoff in turn ",Turn," is ",Payoff, " --------- ");
		?diff(Diff);
		-+diff(Diff+Payoff).
		
/*	TODOs

- comentarii cod ruleta
- pretty printing

*/
