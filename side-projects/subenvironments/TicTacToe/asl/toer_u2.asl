
{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

// rules for obtaining the first free position on the linear representation of the board
getFirst([],X,I) :-
	X = I.
getFirst([0 | T],X,I) :-
	X = I.
getFirst([H | T],X,I) :-
	getFirst(T,X,I+1).

/* Initial goals */

/* Plans */

//goal triggered at the start of the execution
//the start turn signal may or may not be received before
+!start:true <-
	.print("Starting").

// plan executed when the start turn signal is received from the TicTacToe artifact
@startTurnPlan[atomic]
+startTurn(CurrentTurn) <-
	.print("Turn ", CurrentTurn, " has started");
	.wait(89);
	!toePlan(CurrentTurn).

// plan for finding a free position on the board and marking it
@toePlan[atomic]
+!toePlan(CurrentTurn)
	<-	getGameState(List);
		?getFirst(List, X, 0);
		.print("List: ", List, " - Free: ", X);
		mark(X div 3, X mod 3).
		
// this plan is for when the mark action doesn't validate
-!toePlan(CurrentTurn)[error_msg("validation"), op_error(ErrList)]
	<- .print(ErrList).

// generic failure plan
-!toePlan(CurrentTurn)[error_msg(ErrMsg)]
	<- .print("Non-standard error message: ", ErrMsg).
