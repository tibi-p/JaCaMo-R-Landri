
{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+!start:true <-
	.print("Starting").

+startTurn(CurrentTurn): true <-
	.print("O inceput! tura: ",CurrentTurn);
	getGameState(List);
	.print("Lista",List).

