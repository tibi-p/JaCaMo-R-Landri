import org.aria.rlandri.generic.artifacts.RealTimeSinglePlayerCoordinator;
import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;

public class Factorial extends RealTimeSinglePlayerCoordinator {

	private boolean started = false;
	
	@Override
	protected void updateCurrency() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void updateRank() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void saveState() {
		// TODO Auto-generated method stub
	}

	@GAME_OPERATION(validator = "foreverAlone")
	protected void start() {
		if (!started) {
			defineObsProperty("goal", 5);
			started = true;
		}
	}

	protected void foreverAlone() {

	}

	@GAME_OPERATION(validator = "catzelush")
	protected void derp(int result) {
		System.out.println("good job, brah: " + result);
		
		for (AgentId agentId : primeAgents.getAgentIds())
			signal(agentId, "stopGame");
	}

	protected void catzelush(int result) {
		if (result != 120)
			failed("nu te-am avut in hotel cismigiu");
	}

}
