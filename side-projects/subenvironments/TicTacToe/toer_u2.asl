
{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

// this should be impossible to reach
getFirst([],X,I) :-
	X = -1.

getFirst([0 | T],X,I) :-
	X = I.

getFirst([H | T],X,I) :-
	getFirst(T,X,I+1).

/* Initial goals */

/* Plans */

+!start:true <-
	.print("Starting").

+startTurn(CurrentTurn) <-
	.print("Turn ", CurrentTurn, " has started");
	.wait(89);
	!toePlan(CurrentTurn).

+!toePlan(CurrentTurn)
	<-	getGameState(List);
		?getFirst(List, X, 0);
		.print("List: ", List, " - Free: ", X);
		mark(X div 3, X mod 3).

-!toePlan(CurrentTurn)[error_msg("validation"), op_error(ErrList)]
	<- .print(ErrList).

-!toePlan(CurrentTurn)[error_msg(ErrMsg)]
	<- .print("Non-standard error message: ", ErrMsg).
