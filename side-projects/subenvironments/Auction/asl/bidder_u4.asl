// Agent bidder_ in project sandbox

/* Initial beliefs and rules */
/* Initial goals */
{include("common_s_generic.asl")}
+you_are_a_winrar: true
	<- logInfo("I have purchased hotel Cismigiu").

+bidEvent: true
	<-	!buy.
/* Plans */
	
		
+!start : true
	<-	getRandomBudget(Budget);
		+budget(Budget);
		+lastBid(0).
		
+!buy : true
	<-	?budget(Budget);
		?lastBid(Bid);
		poll(HighestBid);
		if(Bid < HighestBid & HighestBid < Budget){
			bid(HighestBid + 1);
			-+lastBid(HighestBid + 1);
			logInfo("bidded ", HighestBid + 1, " for Hotel Cismigiu");
		}
		.
		

+!buy : true
	<-	?budget(Budget);
		poll(HighestBid);
		HighestBid >= Budget.

		
+!handleError ([error(already_bidded)|_])
	<- 
		logError("already bidded")
		.
		
+!handleError([error(bid_too_low)|_])
	<- 	logError("Bid too low");
		!buy
		.
+!handleError([])
	<- 	true
		.
		
+!handleError (error(time_is_up)|_)
	<- 	
		logInfo("I have failed you, master")
		.
	
-!buy [error_msg("validation"), op_error(ErrList)]
	<- 	!handleError(ErrList).
		
-!buy <- !buy.
		