import org.aria.rlandri.generic.artifacts.PlayerAlternatedCoordinator;
import org.aria.rlandri.generic.artifacts.annotation.GAME_OPERATION;
import org.aria.rlandri.generic.artifacts.tools.ValidationResult;
import org.aria.rlandri.generic.artifacts.tools.ValidationType;

import cartago.OPERATION;
import cartago.OpFeedbackParam;
import cartago.AgentId;
/**
 *  
 */
public class TicTacToe extends PlayerAlternatedCoordinator {

	/**
	 * Maximum number of games the duration of all the games should be less than
	 * the permitted execution time for the subenv.
	 */
	int maxGames;

	/**
	 * The state of the game represented by a 3 by 3 matrix.
	 */
	private int[][] gameState = new int[3][3];

	/**
	 * Retains the number of times each of the two players won.
	 */
	private int[] score = new int[2];

	/**
	 * Function that checks if the game has ended( if the state the game has
	 * reached is final)
	 * 
	 * @return 0 if the game has not ended, -1 if the game ended and the first
	 *         player won and 1 if the game ended and the second player won
	 */
	private int ended() {
		for (int i = 0; i < 3; i++) {
			if (gameState[i][0] == gameState[i][1]
					&& gameState[i][1] == gameState[i][2]) {
				int cell = gameState[i][0];
				if (cell != 0)
					return cell;
			}

			if (gameState[0][i] == gameState[1][i]
					&& gameState[1][i] == gameState[2][i]) {
				int cell = gameState[0][i];
				if (cell != 0)
					return cell;
			}
		}

		if (gameState[0][0] == gameState[1][1]
				&& gameState[1][1] == gameState[2][2]) {
			int cell = gameState[0][0];
			if (cell != 0)
				return cell;
		}

		if (gameState[0][2] == gameState[1][1]
				&& gameState[1][1] == gameState[2][0]) {
			int cell = gameState[0][2];
			if (cell != 0)
				return cell;
		}

		return 0;
	}

	/**
	 * Game operation that permits an agent to mark a cell and thus modify the
	 * state of the game.
	 * 
	 * @param x
	 *            the line on which the agent will mark
	 * @param y
	 *            the column on which the agent will mark
	 */
	@GAME_OPERATION(validator = "validateMark")
	void mark(int x, int y) {
		if (currentIndex == 0) {
			gameState[x][y] = 1;
		} else {
			gameState[x][y] = -1;
		}

		int end = ended();
		if (end != 0) {
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++) {
					gameState[i][j] = 0;
				}
			
			for (AgentId agentId : primeAgents.getAgentIds())
				signal(agentId, "stopGame");

			if (end < 0) {
				score[0]++;
				if (score[0] >= maxGames) {
					// GAME ENDS. PLAYER 0 WINS
				}
			} else {
				score[1]++;
				if (score[1] >= maxGames) {
					// GAME ENDS. PLAYER 1 WINS
				}
			}
		}
	}

	/**
	 * Operation that gives agents a linear representation of the state of the
	 * game.
	 * 
	 * @param result
	 */
	@OPERATION
	void getGameState(OpFeedbackParam<Integer[]> result) {
		Integer[] res = new Integer[9];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++) {
				res[3 * i + j] = gameState[i][j];
			}
		result.set(res);
	}

	/**
	 * Validation for the mark operation.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	protected ValidationResult validateMark(int x, int y) {
		ValidationResult vres = new ValidationResult(getOpUserName());

		if (x < 0 || x > 2)
			vres.addReason("row_between_0_2", ValidationType.ERROR);

		if (y < 0 || y > 2)
			vres.addReason("column_between_0_2", ValidationType.ERROR);

		return vres;
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
