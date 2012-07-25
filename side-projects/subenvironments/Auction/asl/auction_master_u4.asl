// Agent auction_master in project sandbox

/* Initial beliefs and rules */
{include("common_s_generic.asl")}
/* Initial goals */


/* Plans */

+!start : true 
	<- 	?coord_id(Coord);
		startAuction;
		.wait(2000);
		stopAuction.
