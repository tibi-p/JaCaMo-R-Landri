import java.util.Random;

import org.aria.rlandri.generic.artifacts.RealTimeMultiPlayerCoordinator;
import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.tools.ValidationResult;
import org.aria.rlandri.generic.artifacts.tools.ValidationType;

import cartago.AgentId;
import cartago.OpFeedbackParam;

public class Auction extends RealTimeMultiPlayerCoordinator {

	private final Random random = new Random();
	private AgentId lastBidder;
	private int currentBid = 1;
	private boolean timeUp = false, started = false;

	@Override
	protected void updateRank() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateCurrency() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void saveState() {
		// TODO Auto-generated method stub

	}

	@GAME_OPERATION(validator = "derp")
	protected void startAuction() throws InterruptedException {
		started = true;
		signal("bidEvent");
	}

	@GAME_OPERATION(validator = "derp")
	protected void stopAuction() {
		timeUp = true;
		System.out.println(lastBidder + " is winrar");
		signal(lastBidder, "you_are_a_winrar");
		signalPrimeAgents("stopGame");
	}

	@GAME_OPERATION(validator = "priceValid")
	protected void bid(int price) {
		currentBid = price;
		signal("bidEvent");
		lastBidder = getOpUserId();

		System.out.println(lastBidder + " bidded " + price);
	}

	protected ValidationResult priceValid(int price) {
		ValidationResult vr = new ValidationResult(getOpUserName());
		vr.setDefaultType(ValidationType.ERROR);
		if (!started) {
			vr.addReason("not_yet_started");
			return vr;
		}
		if (timeUp) {
			vr.addReason("time_is_up");
			return vr;
		}
		if (getOpUserId().equals(lastBidder)) {
			vr.addReason("already_bidded");
			return vr;
		}
		if (price <= currentBid) {
			vr.addReason("bid_too_low");
			return vr;
		}
		return null;
	}

	@GAME_OPERATION(validator = "herp")
	protected void poll(OpFeedbackParam<Integer> price) {
		price.set(currentBid);
	}

	protected ValidationResult derp() {
		return null;
	}

	@GAME_OPERATION(validator = "herp")
	protected void getRandomBudget(OpFeedbackParam<Integer> budget) {
		budget.set(random.nextInt(100));
	}

	protected ValidationResult herp(OpFeedbackParam<Integer> number) {
		return null;
	}

}
