import java.util.HashMap;
import java.util.Map;

import org.aria.rlandri.generic.artifacts.SimultaneouslyExecutedCoordinator;
import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.MASTER_OPERATION;

import cartago.AgentId;
import cartago.OpFeedbackParam;

/**
 * Artifact that implements the auction.
 */
public class RouletteFeedback extends SimultaneouslyExecutedCoordinator {

	int[] numbers = new int[] { 0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13,
			36, 11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29,
			7, 28, 12, 35, 3, 26 };

	private final HashMap<String, Integer> payoffs = new HashMap<String, Integer>();

	{
		payoffs.put("0", 35);
		payoffs.put("Single", 35);
		payoffs.put("Split", 17);
		payoffs.put("Street", 11);
		payoffs.put("Corner", 8);
		payoffs.put("Six", 5);
		payoffs.put("Column1", 2);
		payoffs.put("Column2", 2);
		payoffs.put("Column3", 2);
		payoffs.put("Dozen1", 2);
		payoffs.put("Dozen2", 2);
		payoffs.put("Dozen3", 2);
		payoffs.put("Manque", 1);
		payoffs.put("Passe", 1);
		payoffs.put("Red", 1);
		payoffs.put("Black", 1);
		payoffs.put("Odd", 1);
		payoffs.put("Even", 1);
	}

	private final Map<AgentId, Double> standings = new HashMap<AgentId, Double>();

	private String winningColor;
	private int winningNumber;

	@Override
	protected void doPreEvaluation() {
		for (AgentId aid : masterAgents.getAgentIds()) {
			signal(aid, "spinWheel", currentStep);
		}
	}

	private double payoff(String betType, double betSum) {
		return betSum * payoffs.get(betType).intValue();
	}

	private void updateStandings(AgentId player, double value) {
		if (standings.containsKey(player)) {
			double oldValue = standings.get(player);
			standings.put(player, oldValue + value);
		} else {
			standings.put(player, value);
		}
	}

	@GAME_OPERATION(validator = "validateBet")
	void bet(String betName, double sum, OpFeedbackParam<Integer> currentStep,
			OpFeedbackParam<Double> payoff) {
		this.bet(betName, null, sum, currentStep, payoff);
	}

	void validateBet(String betName, double sum,
			OpFeedbackParam<Integer> currentStep, OpFeedbackParam<Double> payoff) {
	}

	@GAME_OPERATION(validator = "validateBet")
	void bet(String betName, Object betValues[], double sum,
			OpFeedbackParam<Integer> currentStep, OpFeedbackParam<Double> payoff) {
		AgentId aid = getOpUserId();

		System.out.println(aid + " bets " + sum + " gold coins on " + betName);

		Bet bet = new Bet();
		System.out.println("MONEY: " + sum + "AGENT: " + aid);
		bet.sum = sum;
		bet.type = betName;
		bet.betValues = betValues;

		double pay = computePayoffForPlayer(bet);
		updateStandings(aid, pay);

		currentStep.set(this.currentStep);
		payoff.set(pay);
	}

	void validateBet(String betName, Object betValues[], double sum,
			OpFeedbackParam<Integer> currentStep, OpFeedbackParam<Double> payoff) {
	}

	@MASTER_OPERATION(validator = "validateSpinWheel")
	public void spinWheel() {
		int value = (int) (Math.random() * 37);

		winningNumber = numbers[value];
		winningColor = "red";
		if (value % 2 == 0) {
			if (value == 0)
				winningColor = "green";
			winningColor = "black";
		}
		setPreEvaluationDone(true);
		System.out.println("Winning number: " + winningNumber + " and color: "
				+ winningColor);
	}

	void validateSpinWheel() {

	}

	double computePayoffForPlayer(Bet bet) {
		String betType = bet.type;
		double betSum = bet.sum;
		Object[] values = bet.betValues;

		boolean won = false;

		if (betType.equals("0")) {
			if (winningNumber == 0)
				won = true;
		}

		if (betType.equals("Single")) {
			int betValue = ((Number) values[0]).intValue();
			if (winningNumber == betValue)
				won = true;
		}

		if (betType.equals("Split") || betType.equals("Street")
				|| betType.equals("Corner") || betType.equals("Six")) {

			for (int i = 0; i < values.length; i++) {
				int value = ((Number) values[i]).intValue();
				if (winningNumber == value) {
					won = true;
					break;
				}
			}
		}

		if (betType.equals("Column1")) {
			for (int i = 1; i <= 36; i += 3) {
				if (winningNumber == i) {
					won = true;
					break;
				}
			}
		}
		if (betType.equals("Column2")) {
			for (int i = 2; i <= 36; i += 3) {
				if (winningNumber == i) {
					won = true;
					break;
				}
			}
		}

		if (betType.equals("Column3")) {
			for (int i = 3; i <= 36; i += 3) {
				if (winningNumber == i) {
					won = true;
					break;
				}
			}
		}

		if (betType.equals("Dozen1")) {
			for (int i = 1; i <= 12; i += 1) {
				if (winningNumber == i) {
					won = true;
					break;
				}
			}
		}

		if (betType.equals("Dozen2")) {
			for (int i = 13; i <= 24; i += 1) {
				if (winningNumber == i) {
					won = true;
					break;
				}
			}
		}

		if (betType.equals("Dozen3")) {
			for (int i = 25; i <= 36; i += 1) {
				if (winningNumber == i) {
					won = true;
					break;
				}
			}
		}

		if (betType.equals("Manque")) {
			if (winningNumber >= 1 && winningNumber <= 18)
				won = true;
		}

		if (betType.equals("Passe")) {
			if (winningNumber >= 19 && winningNumber <= 36)
				won = true;
		}

		if (betType.equals("Red")) {
			if (winningColor.equals("red"))
				won = true;
		}

		if (betType.equals("Black")) {
			if (winningColor.equals("black"))
				won = true;
		}

		if (betType.equals("Odd")) {
			if (winningNumber % 2 == 1)
				won = true;
		}

		if (betType.equals("Even")) {
			if (winningNumber % 2 == 0 && winningNumber > 0)
				won = true;
		}

		if (won)
			return payoff(betType, betSum);
		else
			return -betSum;
	}

	@Override
	protected void updateRank() {

	}

	@Override
	protected void updateCurrency() {

	}

	@Override
	protected void saveState() {

	}

}
