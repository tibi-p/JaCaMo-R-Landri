import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aria.rlandri.generic.artifacts.SimultaneouslyExecutedCoordinator;
import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.annotation.MASTER_OPERATION;
import org.aria.rlandri.generic.artifacts.tools.ValidationResult;

import cartago.AgentId;
import cartago.OPERATION;
import cartago.OpFeedbackParam;

public class IteratedPrisonerDilemma extends SimultaneouslyExecutedCoordinator {

	public static final String DEFECT = "defect";
	public static final String COOPERATE = "cooperate";

	public static final int A = 1; // outcome if both players cooperate
	public static final int B = 0; // outcome for the defecting player if the
									// other cooperates
	public static final int C = 12; // outcome for the cooperating player if the
									// other defects
	public static final int D = 3; // outcome if both players defect

	private final Map<AgentId, Integer> standings = new HashMap<AgentId, Integer>();
	private final Map<AgentId, String> actions = new HashMap<AgentId, String>();

	private final List<AgentId> order = new ArrayList<AgentId>();

	@GAME_OPERATION(validator = "validateAction")
	void action(String action) {
		AgentId aid = getOpUserId();
		System.out.println(aid + action + "s");
		actions.put(aid, action);
	}

	ValidationResult validateAction(String action) {
		AgentId aid = getOpUserId();
		ValidationResult vr = new ValidationResult(aid.getAgentName());

		if (!action.equals(DEFECT) && !action.equals(COOPERATE))
			vr.addReason("unknown_action");

		return vr;

	}

	private void updateStandings(AgentId aid, int value) {
		if (standings.containsKey(aid)) {
			int oldValue = standings.get(aid);
			standings.put(aid, oldValue + value);
		} else {
			standings.put(aid, value);
		}
	}

	@OPERATION
	protected void registerAgent(OpFeedbackParam<String> wsp) {
		super.registerAgent(wsp);

		actions.put(getOpUserId(), null);
		standings.put(getOpUserId(), 0);
		order.add(getOpUserId());

		wsp.set("NA");
	}

	@MASTER_OPERATION(validator = "validateEvaluateActions")
	void evaluateActions() {
		AgentId firstAid = order.get(0);
		AgentId secondAid = order.get(1);
		String first = actions.get(firstAid);
		String second = actions.get(secondAid);
		if (first.equals(second)) {
			if (first.equals(DEFECT)) {
				updateStandings(firstAid, D);
				updateStandings(secondAid, D);
			} else {
				updateStandings(firstAid, A);
				updateStandings(secondAid, A);
			}
		} else {
			if (first.equals(DEFECT)) {
				updateStandings(firstAid, B);
				updateStandings(secondAid, C);
			} else {
				updateStandings(firstAid, C);
				updateStandings(secondAid, B);
			}
		}
		setPostEvaluationDone(true);
	}

	void validateEvaluateActions() {

	}

	@Override
	protected void saveState() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateCurrency() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void updateRank() {
		// TODO Auto-generated method stub

	}

}
