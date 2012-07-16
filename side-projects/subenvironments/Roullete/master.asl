// Agent master in project roulette.mas2j

{
	include("master_s_generic.asl")
}

/* Initial beliefs and rules */


/* Initial goals */

/* Plans */


+stopTurn(iteration): true <-
	spinWheel;
	payout.
