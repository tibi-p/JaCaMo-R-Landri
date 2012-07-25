import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.aria.rlandri.generic.artifacts.SimultaneouslyExecutedCoordinator;
import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.MASTER_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.PRIME_AGENT_OPERATION;
import org.aria.rlandri.generic.artifacts.tools.ValidationResult;
import org.aria.rlandri.generic.artifacts.tools.ValidationType;

import cartago.AgentId;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

/**
 * Artifact that implements the auction.
 */
public class Roulette extends SimultaneouslyExecutedCoordinator {

	private static final int[] numbers = new int[] { 0, 32, 15, 19, 4, 21, 2,
			25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10, 5, 24, 16, 33, 1, 20,
			14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26 };

	private static final int[] streetBets = new int[] { 1, 4, 7, 10, 13, 16,
			19, 22, 25, 28, 31, 34 };

	private static final int[] cornerBets = new int[] { 1, 2, 4, 5, 7, 8, 10,
			11, 13, 14, 16, 17, 19, 20, 22, 23, 25, 26, 28, 29, 31, 32 };

	private static final int[] sixBets = new int[] { 1, 7, 13, 19, 25, 31 };

	private final HashMap<String, Integer> payoffs = new HashMap<String, Integer>();

	{
		payoffs.put("0", 35);
		payoffs.put("Single", 35);
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

	private final Map<AgentId, Bet> bets = new HashMap<AgentId, Bet>();
	private final Map<AgentId, Double> standings = new HashMap<AgentId, Double>();

	private String winningColor;
	private int winningNumber;

	@Override
	protected void doPostEvaluation() {
		for (AgentId aid : masterAgents.getAgentIds()) {
			signal(aid, "spinWheel", currentStep);
		}
	}

	private double payoff(String betType, double betSum) {
		return betSum * payoffs.get(betType);
	}

	private void initStandings() {

		Set<AgentId> ids = regularAgents.getAgentIds();
		for (AgentId id : ids) {
			double value = 30 + (int) (Math.random() * 20);
			standings.put(id, value);
		}
		System.out.println("Initial standings: " + standings);
	}

	@PRIME_AGENT_OPERATION
	protected void startSubenv() {
		super.startSubenv();
		initStandings();
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
	void bet(String betName, double sum) {
		this.bet(betName, null, sum);
	}

	ValidationResult validateBet(String betName, double sum) {
		return validateBet(betName, null, sum);
	}

	@GAME_OPERATION(validator = "validateBet")
	void bet(String betName, Object betValues[], double sum) {
		AgentId aid = getOpUserId();

		System.out.println(aid + " bets " + sum + " gold coins on " + betName);

		Bet bet = new Bet();
		System.out.println("MONEY: " + sum + "AGENT: " + aid);
		bet.sum = sum;
		bet.type = betName;
		bet.betValues = betValues;
		bets.put(aid, bet);

	}

	ValidationResult validateBet(String betName, Object betValues[], double sum) {

		AgentId aid = getOpUserId();
		System.out.println("VALIDATION " + aid);
		ValidationResult vr = new ValidationResult(aid.getAgentName());
		double money = standings.get(aid);
		if (money < sum) {
			vr.addReason("insufficient_funds", ValidationType.ERROR);
		}

		if (betName.equals("Single")) {
			if (betValues.length != 1)
				vr.addReason(
						"invalid_single_number_bet(wrong_number_of_arguments)",
						ValidationType.ERROR);
			int value = ((Number) betValues[0]).intValue();
			if (value < 0 || value > 36)
				vr.addReason("invalid_single_number_bet", ValidationType.ERROR);
		}

		if (betName.equals("Split")) {
			if (betValues.length != 2)
				vr.addReason("invalid_split_bet(wrong_number_of_arguments)",
						ValidationType.ERROR);
			int val1 = ((Number) betValues[0]).intValue();
			int val2 = ((Number) betValues[1]).intValue();
			int abs = Math.abs(val1 - val2);
			if (abs != 1 && abs != 3)
				vr.addReason("invalid_split_bet", ValidationType.ERROR);
		}

		if (betName.equals("Street")) {
			if (betValues.length != 1)
				vr.addReason("invalid_street_bet(wrong_number_of_arguments)",
						ValidationType.ERROR);
			int val = ((Number) betValues[0]).intValue();
			if (!contains(streetBets, val))
				vr.addReason("invalid_street_bet", ValidationType.ERROR);
		}

		if (betName.equals("Corner")) {
			if (betValues.length != 1)
				vr.addReason("invalid_corner_bet(wrong_number_of_arguments)",
						ValidationType.ERROR);
			int val = ((Number) betValues[0]).intValue();
			if (!contains(cornerBets, val))
				vr.addReason("invalid_corner_bet", ValidationType.ERROR);
		}

		if (betName.equals("Six")) {
			if (betValues.length != 1)
				vr.addReason("invalid_six_bet(wrong_number_of_arguments)",
						ValidationType.ERROR);
			int val = ((Number) betValues[0]).intValue();
			if (!contains(sixBets, val))
				vr.addReason("invalid_six_bet", ValidationType.ERROR);
		}

		return vr;
	}

	private boolean contains(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value)
				return true;
		}
		return false;
	}

	@MASTER_OPERATION(validator = "validateSpinWheel")
	public void spinWheel() {
		int value = (int) (Math.random() * 37);

		winningNumber = numbers[value];
		winningColor = "red";
		if (value % 2 == 0) {
			winningColor = "black";
			if (value == 0)
				winningColor = "green";
		}

		System.out.println("Turn" + currentStep + ". Winning number: "
				+ winningNumber + " and color: " + winningColor);
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

		if (betType.equals("Split")) {
			int value1 = ((Number) values[0]).intValue();
			int value2 = ((Number) values[1]).intValue();
			if (winningNumber == value1 || winningNumber == value2)
				won = true;
		}

		if (betType.equals("Street")) {
			int value = ((Number) values[0]).intValue();
			if(winningNumber>=value && winningNumber<value+3)
				won = true;
		}

		if (betType.equals("Corner")) {
			int value1 = ((Number) values[0]).intValue();
			int value2 = value1 + 1;
			int value3 = value1 + 3;
			int value4 = value1 + 4;
			if (winningNumber == value1 || winningNumber == value2
					|| winningNumber == value3 || winningNumber == value4) {
				won = true;
			}
		}

		if (betType.equals("Six")) {

			int value = ((Number) values[0]).intValue();
			if(winningNumber>=value && winningNumber<value+6)
				won = true;
		}

		if (betType.equals("Column1")) {
			if(winningNumber>0 && winningNumber<=36 && winningNumber%3==1)
				won = true;
		}
		if (betType.equals("Column2")) {
			if(winningNumber>0 && winningNumber<=36 && winningNumber%3==2)
				won = true;
		}

		if (betType.equals("Column3")) {
			if(winningNumber>0 && winningNumber<=36 && winningNumber%3==0)
				won = true;
		}

		if (betType.equals("Dozen1")) {
			if(winningNumber>0 && winningNumber<=12)
				won = true;
		}

		if (betType.equals("Dozen2")) {
			if(winningNumber>12 && winningNumber<=24)
				won = true;
		}

		if (betType.equals("Dozen3")) {
			if(winningNumber>24 && winningNumber<=36)
				won = true;
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

	@MASTER_OPERATION(validator = "validatePayout")
	public void payout() {
		for (Map.Entry<AgentId, Bet> entry : bets.entrySet()) {
			AgentId player = entry.getKey();
			Bet bet = entry.getValue();
			double payoff = computePayoffForPlayer(bet);
			updateStandings(player, payoff);
			signal(player, "payoff", currentStep, winningNumber, winningColor,
					payoff);
		}
		bets.clear();
		setPostEvaluationDone(true);
		String msgFmt = "Standings at turn %d: %s";
		System.out.println(String.format(msgFmt, currentStep, standings));
	}

	void validatePayout() {

	}

	/**
	 * Operation used by the agents to obtain their economic status.
	 */
	@OPERATION
	void getBalance(OpFeedbackParam<Double> result) {
		AgentId aid = getOpUserId();
		double res = standings.get(aid);
		result.set(res);
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
