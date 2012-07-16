// Agent ag1 in project roulette.mas2j

{
	include("common_s_generic.asl")
}

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+start:true
	print("Starting").

+startTurn(currentTurn): true <-
	.print("O inceput! tura: ",currentTurn).

