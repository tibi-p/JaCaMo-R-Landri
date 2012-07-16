// Agent ag1 in project roulette.mas2j

{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+!start : true <-
	?coord_id(R);
	.print("Moartea neagra", R).

+startTurn(currentTurn) : true <-
	.print("O inceput! tura: ", currentTurn);
	bet("Split",[8,9],3).
